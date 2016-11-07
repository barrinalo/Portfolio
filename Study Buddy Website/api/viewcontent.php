<?php
	require_once("../../includes/dbhandler.php");
	require_once("../Scripts/APIHandler.php");
	if(!isset($_POST['Username']) && !isset($_POST['AccessToken'])) header("Location: index.php");
	if(!isset($_POST['ContentID']) && !isset($_POST['ContentType'])) header("Location: index.php");
	$ContentID = $_POST['ContentID'];
	$ContentType = $_POST['ContentType'];
	$Username = $_POST['Username'];
	$AccessToken = $_POST['AccessToken'];

	$Handler = new APIHandler();
	if($Handler->VerifyToken($Username,$AccessToken)) {
		$Continue = $Handler->ReadContentPermitted($ContentID, $Username);
		if(!$Continue && isset($_POST['Override'])) $Continue = $Handler->Override($_POST['Override'], $ContentID, $Username);
		if($Continue) {
			$CurContent = json_decode($Handler->GetContent($ContentID, $Username));
			$ChildContent = json_decode($Handler->GetChildContent($ContentID, $Username, $ContentType));
			if($ContentType == "Note") {
				$Content = $CurContent->Content;
				if(preg_match_all('/[|]{2}[1-9][0-9]*[|]{2}/i',$Content, $matches, PREG_OFFSET_CAPTURE)) {
					$AdditionalOffset = 0;
					for($i = 0; $i < count($matches[0]); $i++) {
						$QuestionID = intval(substr($matches[0][$i][0],2,strlen($matches[0][$i][0])-2));
						$Offset = $matches[0][$i][1];
						$replacementcontent = "";
						for($j = 0; $j < count($ChildContent); $j++) {
							if($ChildContent[$j]->ID == $QuestionID && $ChildContent[$j]->QuestionType == 'Table') {
								$QuestionOptions = json_decode($Handler->GetChildContent($QuestionID, $Username, 'Question'));
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
								$QuestionOptions = json_decode($Handler->GetChildContent($QuestionID, $Username, 'Question'));
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
								$replacementcontent .= "</script><div class='row'><span class='col-xs-12'><select id='CurrentNode$GraphID' class='form-control' onchange=\"UpdateCurrentNode('$GraphID')\">$Options</select></div><div class='row'><span class='col-xs-12'><canvas id='$GraphID' width='800' height='800' style='border:1px solid; width: 100%; height: 100%' class='col-xs-12'></span></div>";
							}
							else if($ChildContent[$j]->ID == $QuestionID && $ChildContent[$j]->QuestionType == 'Manual') {
								$QuestionOptions = json_decode($Handler->GetChildContent($QuestionID, $Username, 'Question'));
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
					$Content .= "</script><div class='row'><span class='col-xs-12'><select id='CurrentNode$GraphID' class='form-control' onchange=\"UpdateCurrentNode('$GraphID')\">$Options</select></div><div class='row'><span class='col-xs-12'><canvas id='$GraphID' width='800' height='800' style='border:1px solid; width: 100%; height: 100%' class='col-xs-12'></span></div>";
				}
			}
		}
	}
?>
<!DOCTYPE html>
<html lang="en" data-ng-app="viewcontentapp" ng-controller="viewcontentctrl" ng-init="ContentID=<?php echo $ContentID;?>; ContentType='<?php echo $ContentType;?>'; Username='<?php echo $Username;?>'; AccessToken='<?php echo $AccessToken;?>'">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular-sanitize.js"></script>
		<script src="viewcontentapp.js"></script>
		<link rel="stylesheet" href="../site-theme.css">
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
		<div class="container-fluid main-container">
		<?php echo $Content;?>
		</div>
	</body>
</html>
