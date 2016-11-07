<?php
	require_once("../includes/dbhandler.php");
	require_once("Scripts/ContentWrapperHandler.php");
	require_once("Scripts/QuizManager.php");
	$SecureSession = new SecureSessionHandler();
	session_set_save_handler($SecureSession, true);
	$SecureSession->start();
	if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
		$SecureSession->forget();
		header("Location: index.php");
	}
	$Title = "";
	$Description = "";
	$Filters = "";
	$Permissions = "Private";
	$Content = "";
	$Continue = false;
	$QuestionType = "";
	$Statistics = "";
	if(isset($_POST['ContentType'])) $ContentType = $_POST['ContentType'];
	else header("Location: index.php");
	$Handler = new ContentWrapperHandler();
	$StatsHandler = new QuizManager();
	if(isset($_POST['ContentID'])) $Continue = $Handler->ReadContentPermitted($_POST['ContentID'], $SecureSession->get("CurrentUser"));
	if(!$Continue && isset($_POST['Override']) && isset($_POST['ContentID'])) {
		$Continue = $Handler->Override($_POST['Override'], $_POST['ContentID'], $SecureSession->get("CurrentUser"));
		$Override = $_POST['Override'];
	}
	else if(isset($_POST['ContentID']) && !isset($_POST['Override'])) $Override = "";
	if($Continue) {
		$CurContent = json_decode($Handler->GetContent($_POST['ContentID'], $SecureSession->get("CurrentUser")));
		$Title = $CurContent->Title;
		$Description = $CurContent->Description;
		$Filters = $CurContent->Filters;
		$Permissions = $CurContent->Permissions;
		$ChildContent = json_decode($Handler->GetChildContent($_POST['ContentID'], $SecureSession->get("CurrentUser"), $ContentType));
		if(isset($_POST['Study'])) {
			$Stats = json_decode($StatsHandler->GetStats($_POST['ContentID'], $SecureSession->get("CurrentUser"), $ContentType, $Override));
			$Statistics = "<p>Attempted " . $Stats->AttemptedQuestions . " of " . $Stats->UniqueQuestions . " available questions</p>";
			$Statistics .= "<p>" . $Stats->NumberOfAttempts . " questions answered with average score " . round($Stats->AverageScore*100,2) . "% and average time taken " . round($Stats->AverageTimeTaken,2) . "s</p>";
			$Statistics .= "<p><b>Creator:</b> " . $Stats->Creator . "<br /><b>Contributors:</b> ";
			for($i = 0; $i < count($Stats->Contributors); $i++) $Statistics .= $Stats->Contributors[$i] . ",";
			if(count($Stats->Contributors) > 0) $Statistics = substr($Statistics,0, strlen($Statistics) - 1);
			$Statistics .= "</p>";
			$Statistics .= "<form target='_blank' method='post' action='quiz.php'><input type='hidden' name='ContentID' value=" . $_POST['ContentID'] . "><input type='hidden' name='ContentType' value='$ContentType'><input type='submit' value='Quiz Yourself' class='btn btn-default'></form>";
		}
		if($ContentType == "Note") {
			if(isset($_POST['Study'])) $Statistics .= "<form target='_blank' method='post' action='quiz.php'><input type='hidden' name='ContentID' value=" . $_POST['ContentID'] . "><input type='hidden' name='ContentType' value='$ContentType'><input type='hidden' name='IsTest' value=1><input type='submit' value='Take as a Test' class='btn btn-default'></form>";
			$Content = $CurContent->Content;
			if(preg_match_all('/[|]{2}[1-9][0-9]*[|]{2}/i',$Content, $matches, PREG_OFFSET_CAPTURE)) {
				$AdditionalOffset = 0;
				for($i = 0; $i < count($matches[0]); $i++) {
					$QuestionID = intval(substr($matches[0][$i][0],2,strlen($matches[0][$i][0])-2));
					$Offset = $matches[0][$i][1];
					$replacementcontent = "";
					for($j = 0; $j < count($ChildContent); $j++) {
						if($ChildContent[$j]->ID == $QuestionID && $ChildContent[$j]->QuestionType == 'Table') {
							$QuestionOptions = json_decode($Handler->GetChildContent($QuestionID, $SecureSession->get("CurrentUser"), 'Question'));
							$replacementcontent = "<div class='table-responsive'><table class='table'>";
							$Topics = array();
							$Aspects = array();
							for($k = 0; $k < count($QuestionOptions); $k++) {
								if(!in_array($QuestionOptions[$k]->Topic, $Topics)) array_push($Topics, $QuestionOptions[$k]->Topic);
								if(!in_array($QuestionOptions[$k]->Aspect, $Aspects)) array_push($Aspects, $QuestionOptions[$k]->Aspect);
							}
							$replacementcontent .= "<tr><th>Topics</th>";
							for($k = 0; $k < count($Aspects); $k++) $replacementcontent .= "<th>" . $Aspects[$k] . "</th>";
							$replacementcontent .= "</tr>";
							for($k = 0; $k < count($Topics); $k++) {
								$replacementcontent .= "<tr><td>" . $Topics[$k] . "</td>";
								for($l = 0; $l < count($QuestionOptions); $l++) if($QuestionOptions[$l]->Topic == $Topics[$k]) $replacementcontent .= "<td>" . $QuestionOptions[$l]->TableEntry . "</td>";
								$replacementcontent .= "</tr>";
							}
							$replacementcontent .= "</table></div>";
							break;
						}
						else if($ChildContent[$j]->ID == $QuestionID && $ChildContent[$j]->QuestionType == 'Graph') {
							$GraphID = "Graph" . $ChildContent[$j]->ID;
							$ExistingNodes = array();
							$replacementcontent = "<script>";
							$replacementcontent .= "GraphEntries['$GraphID'] = [];";
							$QuestionOptions = json_decode($Handler->GetChildContent($QuestionID, $SecureSession->get("CurrentUser"), 'Question'));
							for($k = 0; $k < count($QuestionOptions); $k++) {
								$Node1 = $QuestionOptions[$k]->Node1;
								$Node2 = $QuestionOptions[$k]->Node2;
								$Relationship = $QuestionOptions[$k]->Relationship;
								if(!in_array($Node1,$ExistingNodes)) array_push($ExistingNodes,$Node1);
								if(!in_array($Node2,$ExistingNodes)) array_push($ExistingNodes,$Node2);
								$replacementcontent .= "GraphEntries['$GraphID'].push({Node1 : '$Node1', Node2 : '$Node2', Relationship : '$Relationship'});";
							}
							$Options = "";
							for($k = 0; $k < count($ExistingNodes); $k++) {
								$Options .= "<option value='" . $ExistingNodes[$k] . "'>" . $ExistingNodes[$k] . "</option>";
							}
							$replacementcontent .= "</script><div class='row'><span class='col-xs-12'><select id='CurrentNode$GraphID' class='form-control' oninput='UpdateCurrentNode(\"$GraphID\")'>$Options</select></div><div class='row'><span class='col-xs-12'><canvas id='$GraphID' width='800' height='800' style='border:1px solid; width: 100%; height: 100%' class='col-xs-12'></span></div>";
						}
						else if($ChildContent[$j]->ID == $QuestionID && $ChildContent[$j]->QuestionType == 'Manual') {
							$QuestionOptions = json_decode($Handler->GetChildContent($QuestionID, $SecureSession->get("CurrentUser"), 'Question'));
							$ManualOptionStem = $QuestionOptions[0]->Stem;
							if(preg_match('/(((?={(@*[^{}@]+[|]{2})+@*[^{}@|]+})(?!{[^{}@|]+}))|(?={@[^{}@]+}))/i',$ManualOptionStem, $ManualOptions, PREG_OFFSET_CAPTURE)) {
								for($k = 0; $k < count($ManualOptions); $k++) {
									$start = $ManualOptions[$k][1];
									$end = strpos($ManualOptionStem, "}", $start) + 1;
									$Substr = substr($ManualOptionStem, $start, $end-$start);
									$Substr = str_replace("{","",$Substr);
									$Substr = str_replace("}","",$Substr);
									$SplitOptions = explode("||", $Substr);
									$CorrectContent = "<b>True Statements</b><br />";
									$WrongContent = "<b>False Statements</b><br />";
									$HasWrong = false;
									$HasCorrect = false;
									for($l = 0; $l < count($SplitOptions); $l++) {
										if(strpos($SplitOptions[$l],"@") !== false) {
											$CorrectContent .= str_replace("@","",$SplitOptions[$l]);
											$HasCorrect = true;
										}
										else {
											$WrongContent .= $SplitOptions[$l] . "<br />";
											$HasWrong = true;
										}
									}
									if($HasWrong && $HasCorrect) $replacementcontent .= $CorrectContent . $WrongContent;
									else if($HasCorrect) $replacementcontent .= substr($CorrectContent,28);
									break;
								}
							}
						}
					}
					$Content = substr($Content,0,$Offset + $AdditionalOffset) . $replacementcontent . substr($Content,$Offset + $AdditionalOffset + strlen($matches[0][$i][0]));
					$AdditionalOffset -= strlen($matches[0][$i][0]);
					$AdditionalOffset += strlen($replacementcontent);
				}
			}
			$Content .= "<h3>Attached Questions</h3><div class='table-responsive'><table class='table'><tr><th>Question Title</th><th>Question Type</th><th>View</th></tr>";
			for($i = 0; $i < count($ChildContent); $i++) {
				$Content .= "<tr>";
				$Content .= "<td>" . $ChildContent[$i]->Title ."</td>";
				$Content .= "<td>" . $ChildContent[$i]->QuestionType ."</td>";
				$Content .= "<td><form action='viewcontent.php' target='_blank' method='post'><input type='hidden' name='Override' value='" . $_POST['ContentID'] . "'><input type='hidden' name='ContentID' value=" . $ChildContent[$i]->ID ."><input type='hidden' name='ContentType' value='" . $ChildContent[$i]->ContentType . "'><input type='submit' value='View' class='btn btn-default'></form></td>";
				$Content .= "</tr>";
			}
			$Content .= "</table></div>";
		}
		else if($ContentType == "Package") {
			$Content .= "<div class='table-responsive'><table class='table'><tr><th>Note Title</th><th>Note Description</th><th>View</th><th>Quiz Yourself</th><th>Take as a Test</th></tr>";
			for($i = 0; $i < count($ChildContent); $i++) {
				$Content .= "<tr>";
				$Content .= "<td>" . $ChildContent[$i]->Title ."</td>";
				$Content .= "<td>" . $ChildContent[$i]->Description ."</td>";
				if(!isset($_POST['Study'])) {
					$Content .= "<td><form action='viewcontent.php' target='_blank' method='post'><input type='hidden' name='Override' value='" . $_POST['ContentID'] . "'><input type='hidden' name='ContentID' value=" . $ChildContent[$i]->ID ."><input type='hidden' name='ContentType' value='" . $ChildContent[$i]->ContentType . "'><input type='submit' value='View' class='btn btn-default'></form></td>";
					$Content .= "<td></td>";
					$Content .= "<td></td>";
				}
				else {
					$Content .= "<td><form action='viewcontent.php' target='_blank' method='post'><input type='hidden' name='Override' value='" . $_POST['ContentID'] . "'><input type='hidden' name='ContentID' value=" . $ChildContent[$i]->ID ."><input type='hidden' name='ContentType' value='" . $ChildContent[$i]->ContentType . "'><input type='hidden' name='Study' value=1><input type='submit' value='View' class='btn btn-default'></form></td>";
					$Content .= "<td><form action='quiz.php' target='_blank' method='post'><input type='hidden' name='Override' value='" . $_POST['ContentID'] . "'><input type='hidden' name='ContentID' value=" . $ChildContent[$i]->ID ."><input type='hidden' name='ContentType' value='" . $ChildContent[$i]->ContentType . "'><input type='submit' value='Quiz Yourself' class='btn btn-default'></form></td>";
					$Content .= "<td><form action='quiz.php' target='_blank' method='post'><input type='hidden' name='Override' value='" . $_POST['ContentID'] . "'><input type='hidden' name='ContentID' value=" . $ChildContent[$i]->ID ."><input type='hidden' name='ContentType' value='" . $ChildContent[$i]->ContentType . "'><input type='hidden' name='IsTest' value=1><input type='submit' value='Take as a Test' class='btn btn-default'></form></td>";
				}
				$Content .= "</tr>";
			}
			$Content .= "</table></div>";
		}
		else if($ContentType == "Question") {
			$QuestionType = $CurContent->QuestionType;
			if($QuestionType == "Manual") $Content = "<b>Stem:</b><br />" . $ChildContent[0]->Stem . "<br />" . "<b>Explanation:</b><br />" . $ChildContent[0]->Explanation;
			else if($QuestionType == "Table") {
				$Content = "<div class='table-responsive'><table class='table'>";
				$Topics = array();
				$Aspects = array();
				for($i = 0; $i < count($ChildContent); $i++) {
					if(!in_array($ChildContent[$i]->Topic, $Topics)) array_push($Topics, $ChildContent[$i]->Topic);
					if(!in_array($ChildContent[$i]->Aspect, $Aspects)) array_push($Aspects, $ChildContent[$i]->Aspect);
				}
				$Content .= "<tr><th>Topics</th>";
				for($i = 0; $i < count($Aspects); $i++) $Content .= "<th>" . $Aspects[$i] . "</th>";
				$Content .= "</tr>";
				for($i = 0; $i < count($Topics); $i++) {
					$Content .= "<tr><td>" . $Topics[$i] . "</td>";
					for($j = 0; $j < count($ChildContent); $j++) if($ChildContent[$j]->Topic == $Topics[$i]) $Content .= "<td>" . $ChildContent[$j]->TableEntry . "</td>";
					$Content .= "</tr>";
				}
				$Content .= "</table></div>";
			}
			else if($QuestionType == "Equation") {
			}
			else if($QuestionType == "Logical") {
				$Content = "<div class='table-responsive'><table class='table'><tr><th>Gate Name</th><th>Description</th><th>True State</th><th>False State</th></tr>";
				for($i = 0; $i < count($ChildContent); $i++) {
					$Content .= "<tr>";
					$Content .= "<td>" . $ChildContent[$i]->Stem . "</td>";
					$Content .= "<td>" . $ChildContent[$i]->Relationship . "</td>";
					$Content .= "<td>" . $ChildContent[$i]->Node1 . "</td>";
					$Content .= "<td>" . $ChildContent[$i]->Node2 . "</td>";
				}
			}
			else if($QuestionType == "Graph") {
				$GraphID = "Graph" . $CurContent->ID;
				$ExistingNodes = array();
				$Content = "<script>";
				$Content .= "GraphEntries['$GraphID'] = [];";
				for($i = 0; $i < count($ChildContent); $i++) {
					$Node1 = $ChildContent[$i]->Node1;
					$Node2 = $ChildContent[$i]->Node2;
					$Relationship = $ChildContent[$i]->Relationship;
					if(!in_array($Node1,$ExistingNodes)) array_push($ExistingNodes,$Node1);
					if(!in_array($Node2,$ExistingNodes)) array_push($ExistingNodes,$Node2);
					$Content .= "GraphEntries['$GraphID'].push({Node1 : '$Node1', Node2 : '$Node2', Relationship : '$Relationship'});";
				}
				$Options = "";
				for($i = 0; $i < count($ExistingNodes); $i++) {
					$Options .= "<option value='" . $ExistingNodes[$i] . "'>" . $ExistingNodes[$i] . "</option>";
				}
				$Content .= "</script><div class='row'><span class='col-xs-12'><select id='CurrentNode$GraphID' class='form-control' oninput='UpdateCurrentNode(\"$GraphID\")'>$Options</select></div><div class='row'><span class='col-xs-12'><canvas id='$GraphID' width='800' height='800' style='border:1px solid; width: 100%; height: 100%' class='col-xs-12'></span></div>";
			}
		}
	}
?>
<!DOCTYPE html>
<html lang="en" data-ng-app="viewcontentapp" ng-controller="viewcontentctrl">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		<script src="Scripts/viewcontentapp.js"></script>
		<script src="ckeditor/ckeditor.js"></script>
		<link rel="stylesheet" href="site-theme.css">
		<script>
			var UpdateCurrentNode = function(GraphID) {
				CurrentNode[GraphID] = $("#CurrentNode" + GraphID).val();
				DrawGraph(GraphID);
			}
			var GraphEntries = {};
			var CurrentNode = {};
			var GraphNodes = {};
			var DrawGraph = function(GraphID) {
				var canvas = document.getElementById(GraphID);
				canvas.width = canvas.width;
				var ctx = canvas.getContext("2d");
				ctx.clearRect(0,0,canvas.width,canvas.height);
				var FontSize = 15;
				ctx.font = parseInt(FontSize) + "pt Arial";
				DrawNode(CurrentNode[GraphID], canvas.width/2, canvas.height/2, ctx, FontSize);
				var Relations = [];
				for(var i = 0; i < GraphEntries[GraphID].length; i++) {
					if(CurrentNode[GraphID] == GraphEntries[GraphID][i].Node1 || CurrentNode[GraphID] == GraphEntries[GraphID][i].Node2) {
						var bidir = false;
						for(var j = 0; j < Relations.length; j++) {
							if(Relations[j].Node1 == GraphEntries[GraphID][i].Node2 && Relations[j].Node2 == GraphEntries[GraphID][i].Node1 && Relations[j].Relationship == GraphEntries[GraphID][i].Relationship) {
							Relations[j].Bidirectional = true;
							bidir = true;
						}
					}
					if(!bidir) Relations.push({Node1 : GraphEntries[GraphID][i].Node1, Node2 : GraphEntries[GraphID][i].Node2, Bidirectional : false, Relationship : GraphEntries[GraphID][i].Relationship});
				}
			}
			var count = 0;
			for(var key in Relations) if(Relations.hasOwnProperty(key)) count++;
			var rotateamount = Math.PI * 2 / count;
			var count2 = 0;
			for(var key in Relations) {
				if(Relations.hasOwnProperty(key)) {
					var xpos = 300 * Math.cos(rotateamount * count2 + Math.PI/2) + canvas.width/2;
					var ypos = 300 * Math.sin(rotateamount * count2 + Math.PI/2) + 400;
					if(CurrentNode[GraphID] == Relations[key].Node1) DrawNode(Relations[key].Node2, xpos, ypos, ctx, FontSize);
					else DrawNode(Relations[key].Node1, xpos, ypos, ctx, FontSize);
					if(Relations[key].Bidirectional) DrawRelationship(canvas.width/2, canvas.height/2, xpos, ypos, ctx, Relations[key].Relationship, true, FontSize);
					else {
						if(CurrentNode[GraphID] == Relations[key].Node1) DrawRelationship(canvas.width/2, canvas.height/2, xpos, ypos, ctx, Relations[key].Relationship, false, FontSize);
						else DrawRelationship(xpos, ypos, canvas.width/2, canvas.height/2, ctx, Relations[key].Relationship, false, FontSize);
					}
					count2++;
				}
			}
		}
		var DrawNode = function(Text, xpos, ypos, ctx, FontSize) {
			var dim = ctx.measureText(Text);
			ctx.fillText(Text, xpos - dim.width/2, ypos + FontSize/2);
		}
		var DrawRelationship = function(x1,y1,x2,y2, ctx, Relationship, Bidirectional, FontSize) {
			var startRadians=Math.atan((y2-y1)/(x2-x1));
			startRadians+=((x2>=x1)?-90:90)*Math.PI/180;
			var endRadians=Math.atan((y2-y1)/(x2-x1));
			endRadians+=((x2>=x1)?90:-90)*Math.PI/180;
			var linelength = Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));
	
			ctx.moveTo(x1,y1);
			ctx.lineTo(x2,y2);
			ctx.stroke();
			
			DrawNode(Relationship, (x1 + x2)/2, (y1 + y2)/2, ctx, FontSize);
	
			ctx.save();
  	  ctx.beginPath();
  	  ctx.translate(x2,y2);
  	  ctx.rotate(endRadians);
  	  ctx.moveTo(0,0);
  	  ctx.lineTo(5,20);
  	  ctx.lineTo(-5,20);
  	  ctx.closePath();
  	  ctx.restore();
  	  ctx.fill();
	
			if(Bidirectional) {
				ctx.save();
  	  	ctx.beginPath();
  	  	ctx.translate(x1,y1);
  	  	ctx.rotate(startRadians);
  	  	ctx.moveTo(0,0);
  	  	ctx.lineTo(5,20);
  	  	ctx.lineTo(-5,20);
  	  	ctx.closePath();
  	  	ctx.restore();
				ctx.fill();
			}
		}
	</script>
	</head>
	<body>
		<nav class="navbar navbar-default navbar-fixed-top">
			<div class="container-fluid">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#mainNavBar">
						<span class="icon-bar"></span>
        		<span class="icon-bar"></span>
        		<span class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="#"><span id="Username"><?php echo $SecureSession->get("CurrentUser");?></span></a>
				</div>
				<div class="collapse navbar-collapse" id="mainNavBar">
				</div>
			</div>
		</nav>
		<div class="container-fluid main-container">
			<div class="row col-xs-12">
			<h1><?php echo $Title;?></h1>
			<div class="row col-xs-12"><?php echo $Statistics;?></div>
			<div class="row">
				<span class="col-xs-4"><b>Permission:</b> <?php echo $Permissions;?></span>
				<span class="col-xs-4"><b>Filters:</b> <?php echo $Filters;?></span>
				<span class="col-xs-4"><b>Content Type:</b> <?php echo $ContentType;?></span>
			</div>
			<div class="row col-xs-12"><b>Description:</b><p><?php echo $Description;?></p></div>
			<span ng-init="QuestionType = '<?php echo $QuestionType;?>'" ng-show="QuestionType != ''"><b>Question Type:</b> <?php echo $QuestionType;?><br /></span>
			<h3>Content</h3>
			<?php echo $Content;?>
			</div>
		</div>
	</body>
</html>
