<?php
	require_once("../includes/dbhandler.php");
	require_once("Scripts/ContentWrapperHandler.php");
	$SecureSession = new SecureSessionHandler();
	session_set_save_handler($SecureSession, true);
	$SecureSession->start();
	if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
		$SecureSession->forget();
		header("Location: index.php");
	}
	$QuestionTitle = "";
	$QuestionDescription = "";
	$QuestionFilters = "";
	$QuestionPermissions = "Private";
	$QuestionStem = "";
	$QuestionExplanation = "";
	$Handler = new ContentWrapperHandler();
	$Continue = false;
	if(isset($_POST['QuestionID'])) $Continue = $Handler->WriteContentPermitted($_POST['QuestionID'], $SecureSession->get("CurrentUser"));
	if($Continue) {
		if($_POST['QuestionType'] == "Manual") {
			$info = json_decode($Handler->GetContent($_POST['QuestionID'], $SecureSession->get("CurrentUser")));
			$QuestionTitle = $info->Title;
			$QuestionDescription = $info->Description;
			$QuestionFilters = $info->Filters;
			$QuestionPermissions = $info->Permissions;
			$ManualInfo = json_decode($Handler->GetChildContent($_POST['QuestionID'], $SecureSession->get("CurrentUser"), "Question"));
			if(count($ManualInfo) == 1) {
				$QuestionStem = $ManualInfo[0]->Stem;
				$QuestionExplanation = $ManualInfo[0]->Explanation;
				$ManualOptionID = $ManualInfo[0]->ID;
			}
		}
	}

?>
<!DOCTYPE html>
<html lang="en">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		<script src="Scripts/editquestionapp.js"></script>
		<script src="ckeditor/ckeditor.js"></script>
		<link rel="stylesheet" href="site-theme.css">
	</head>
	<body data-ng-app="editquestionapp" ng-controller="editquestionctrl">
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
					<ul class="nav navbar-nav">
						<li><a href="#" ng-click="SubmitQuestionForm()">Save Changes</a>
					</ul>
					<ul class="nav navbar-nav navbar-right">
						<li><a href="Scripts/logout.php">Logout</a></li>
					</ul>
				</div>
			</div>
		</nav>
		<div class="container-fluid main-container">
			<div class="row">
				<div class="col-xs-12">
					<h1 ng-show="formData.QuestionType == 'Table'">Creating a Table Question</h1>
					<h1 ng-show="formData.QuestionType == 'Logical'">Creating a Logical Question</h1>
					<h1 ng-show="formData.QuestionType == 'Graph'">Creating a Graph Question</h1>
					<h1 ng-show="formData.QuestionType == 'Equation'">Creating an Equation Question</h1>
					<h1 ng-show="formData.QuestionType == 'Manual'">Creating a Manual Question</h1>
					<div class="panel panel-info" ng-show="formData.QuestionType == 'Manual'">
						<div class="panel-heading">How to specify answers</div>
						<div class="panel-body">
							<p>To specify answers, enclose them within curly braces, separated by two vertical lines and indicating the correct answer by prefixing with an <b>'@'</b> character. E.g <b>{@a||b||c}</b> means there are 3 options a, b, and c but only a is correct. You may specify any number of correct answers. You may enter this structure at any point in your question stem. For example: <b>The boy {was||were||@is} eating his lunch.</b> Images can be answers as well.
						</div>
					</div>
					<center>
						<span class="alert alert-info" ng-if="ProcessingForm">Saving Question...</span>
						<span class="alert alert-success" ng-if="QuestionSaveSuccess">Question Saved</span>
						<span class="alert alert-danger" ng-if="QuestionSaveFail">Unable to save question</span>
						<span class="alert alert-danger" ng-if="FormFeedbackFail != ''">{{FormFeedbackFail}}</span>
					</center>
					<div class="panel panel-danger" ng-show="CommonQuestionForm.$invalid && CommonQuestionForm.$submitted">
						<div class="panel-heading">There are some errors in your question</div>
						<div class="panel-body">
							<ul>
								<li ng-show="CommonQuestionForm.Title.$error.required">Question Title must be filled in</li>
								<li ng-show="CommonQuestionForm.Filters.$error.pattern">Question Filters must be preceded by a # and cannot contain whitespace</li>
								<li ng-show="CommonQuestionForm.Permissions.$error.required">Question Permissions must be filled in</li>
								<li ng-show="CommonQuestionForm.ManualStem.$error.stemValid">Question Stem must contain a valid set of options</li>
							</ul>
						</div>
					</div>
					<form name="CommonQuestionForm" id="CommonQuestionForm" novalidate>
						<div class="form-group">
							<input type='hidden' ng-model='formData.ContentID' ng-init="formData.ContentID='<?php if(isset($_POST['QuestionID'])) echo $_POST['QuestionID'];?>'">
							<input type='hidden' ng-model='formData.QuestionType' ng-init="formData.QuestionType='<?php echo $_POST['QuestionType'];?>'">
							<input type='hidden' ng-model='formData.Username' ng-init="formData.Username='<?php echo $SecureSession->get('CurrentUser');?>'">
							<input type='hidden' ng-model='formData.ContentType' ng-init="formData.ContentType='Question'">
							<label for="Title">Question Title:</label>
							<input type="text" class="form-control" id="Title" name="Title" ng-model="formData.Title" ng-required="true">
							<label for="Description">Question Description:</label>
							<input type="text" class="form-control" id="Description" name="Description" ng-model="formData.Description">
							<label for="Filters">Question Filters:</label>
							<input type="text" class="form-control" id="Filters" name="Filters" ng-model="formData.Filters" ng-pattern="FilterRegex">
							<label for="Permissions">Permissions:</label>
							<select form="CommonQuestionForm" name="Permissions" id="Permissions" ng-model="formData.Permissions" class="form-control" ng-required="true">
								<option value='Private'>Private</option>
								<option value='Selective'>Selective</option>
								<option value='Public'>Public</option>
							</select>
							<div ng-show="formData.QuestionType == 'Manual'" ng-init="ManualOptionID='<?php if(isset($ManualOptionID)) echo $ManualOptionID;?>'">
								<label for="Stem">Question Stem:</label>
								<textarea form="CommonQuestionForm" name="ManualStem" id="ManualStem" class="form-control" ng-model="ManualStem" ck-editor stem-valid></textarea>
								<label for="Explanation">Question Explanation:</label>
								<textarea form="CommonQuestionForm" name="ManualExplanation" id="ManualExplanation" class="form-control" ng-model="ManualExplanation" ck-editor></textarea>
							</div>
							<div ng-show="formData.QuestionType == 'Table'">
								<div class="row col-xs-12"><h3>Enter Table Data</h3></div>
								<div class="row">
									<div class="col-xs-6">
										<label for="CurAspect">Current Column:</label>
										<select ng-model="CurAspect" class="form-control" form="CommonQuestionForm" id="CurAspect">
											<option ng-repeat="aspect in TableAspects" value='{{aspect}}'>{{aspect}}</option>
										</select>
										<button class='btn btn-default' ng-click="RemoveAspect()">Remove Current Column</button>
									</div>
									<div class="col-xs-6">
										<label for='NewAspect'>New Column:</label>
										<input type='text' class='form-control' ng-model='NewAspect' id='NewAspect'>
										<button class='btn btn-default' ng-click="AddAspect()">Add Column</button>
									</div>
								</div>
								<div class="row">
									<div class='table-responsive col-xs-12'>
										<table class='table'>
											<tr><th>Rows</th><th>Column - {{CurAspect}}</th><th>Explanations</th></tr>
											<tr ng-repeat="(key,val) in TableEntries" ng-show="val.Aspect == CurAspect">
												<td><textarea form="CommonQuestionForm" ng-model="TableEntries[key].Topic" class="form-control" ng-change="OnTopicChange(TableEntries[key].Topic, '{{TableEntries[key].TopicID}}')" ck-editor></textarea>
											<button class='btn btn-default' ng-click="RemoveTopic(val.TopicID)">Remove</button></td>
												<td><textarea form="CommonQuestionForm" ng-model="TableEntries[key].TableEntry" class="form-control" ck-editor></textarea></td>
												<td><textarea form="CommonQuestionForm" ng-model="TableEntries[key].Explanation" class="form-control" ck-editor></textarea></td>
											</tr>
										</table>
									</div>
								</div>
								<div class='row col-xs-12'><button class='btn btn-default' ng-click='AddTopic()'>Add Row</button></div>
							</div>
							<div ng-show="formData.QuestionType == 'Logical'">
								<div class="row col-xs-12"><h3>Create Logical Gates</h3></div>
								<div class="row col-xs-12">
									<div class='alert alert-info'>Please only use these brackets () in your boolean expressions, Symbol for And : &, Symbol for Inclusive Or : |, Symbol for Negation : !</div>
								</div>
								<div class="row">
									<span class='col-xs-2'><label for='CreateGate'>Gate Name:</label></span>
									<span class='col-xs-8'><input type='text' id='CreateGate' ng-model='CreateGate' class='form-control'></span>
									<span class='col-xs-2'><button class='btn btn-default' ng-click='AddGate()'>Create Gate</button>
								</div>
								<div class="row">
									<div class="table-responsive col-xs-12">
										<table class="table">
											<tr><th>Gate Name</th><th>Boolean Logic</th><th>True Term</th><th>False Term</th><th>Remove</th></tr>
											<tr ng-repeat="(index,Gate) in LogicalGates"><td>{{Gate.Stem}}</td><td><input type='text' ng-model='LogicalGates[index].Relationship' class='form-control'><span ng-show="LogicalGates[index].EquationError!=''"><font color='red'>{{Gate.EquationError}}</font></span></td><td><input type='text' ng-model='LogicalGates[index].Node1' class='form-control'></td><td><input type='text' ng-model='LogicalGates[index].Node2' class='form-control'></td><td><button class='btn btn-default' ng-click='RemoveGate(index)'>Remove Gate</button></td></tr>
										</table>
									</div>
								</div>
							</div>
							<div ng-show="formData.QuestionType == 'Graph'">
								<div class="row col-xs-12"><h3>Defining Relations</h3></div>
								<div class="row">
									<span class="col-xs-2"><label for="CurrentNode">Current Node:</label></span>
									<span class="col-xs-3"><select ng-model="CurrentNode" id="CurrentNode" class="form-control" ng-change='DrawGraph()'>
										<option ng-repeat="Node in GraphNodes" value="{{Node}}">{{Node}}</option>
									</select></span>
									<span class="col-xs-2"><button class='btn btn-default' ng-click="RemoveNode()">Remove Node</button></span>
									<span class="col-xs-3"><input type='text' ng-model='NewNode' id='NewNode' class="form-control"></span>
									<span class="col-xs-2"><button class='btn btn-default' ng-click="AddNode()">Add Node</button><font color='red' ng-show="NewNodeFeedback != ''">{{NewNodeFeedback}}</font></span>
								</div>
								<div class="row">
									<span class="col-xs-2"><label for="NewNode1">Node1:</label></span>
									<span class="col-xs-5"><select ng-model="NewNode1" id="NewNode1" class="form-control">
										<option ng-repeat="Node in GraphNodes" value="{{Node}}">{{Node}}</option>
									</select></span>
									<span class="5"><font color='red' ng-show="NewRelationshipFeedback != ''">{{NewRelationshipFeedback}}</font></span>
								</div>
								<div class="row">
									<span class="col-xs-2"><label for="NewRelationship">Relationship:</label></span>
									<span class='col-xs-5'><input type='text' class='form-control' ng-model='NewRelationship' id='NewRelationship'></span>
									<span class="5"><button class='btn btn-default' ng-click="AddRelationship()">Add Unidirectional Relationship</button></span>
								</div>
								<div class="row">
									<span class="col-xs-2"><label for="NewNode2">Node2:</label></span>
									<span class="col-xs-5"><select ng-model="NewNode2" id="NewNode2" class="form-control">
										<option ng-repeat="Node in GraphNodes" value="{{Node}}">{{Node}}</option>
									</select></span>
									<span class="5"><button class='btn btn-default' ng-click="AddBidirectionalRelationship()">Add Bidirectional Relationship</button></span>
								</div>
								<div class="row">
									<span class="col-xs-7"><label for="DisableMultipleLinks">Disable Multiple Links: <br />Check this if you want to disable multiple linking when generating questions. E.g if you only want to check Node 1 has a relationship with Node 2 rather than if Node 1 has a relationship with Node 3 via Node 2 etc.</label></span>
									<span class="col-xs-1"><input type='checkbox' id="DisableMultipleLinks" ng-model="DisableMultipleLinks"></span>
									<span class="col-xs-4"></span>
								</div>
								<div class="table-responsive col-xs-6">
									<table class='table'>
										<tr><th>Node1</th><th>Relationship</th><th>Node2</th><th>Remove</th></tr>
										<tr ng-repeat="(index, GraphEntry) in GraphEntries" ng-show="ShowRelationship(GraphEntry)"><td>{{GraphEntry.Node1}}</td><td>{{GraphEntry.Relationship}}</td><td>{{GraphEntry.Node2}}</td><td><button class='btn btn-default' ng-click="RemoveRelationship(index)">Remove</button></td></tr>
									</table>
								</div>
								<div class='col-xs-6'>
									<b>Visual:</b><br />
									<canvas id="GraphCanvas" width="800" height="800" style="border:1px solid; width: 100%; height: 100%" class='col-xs-12'>
								</div>
							</div>
							<div ng-show="formData.QuestionType == 'Equation'">
								<div class="row col-xs-12">
									<label for="EquationStem">Question Stem:</label>
									<textarea form="CommonQuestionForm" name="EquationStem" id="EquationStem" class="form-control" ng-model="EquationStem" ck-editor></textarea>
								</div>
								<div class="row col-xs-12">
									<label for="EquationExplanation">Define Equation:</label>
									<textarea form="CommonQuestionForm" name="EquationExplanation" id="EquationExplanation" class="form-control" ng-model="EquationExplanation"></textarea>
								</div>
								<div class="row col-xs-12">
									<div class='row'>
										<h3><span class='col-xs-3'>Variables</span></h3><span class='col-xs-2'><label for='NewVariable'>New Variable:</label></span><span class='col-xs-5'><input type='text' id='NewVariable' class='form-control' ng-model='NewVariable'></span><span class='col-xs-2'><button class='btn btn-default' ng-click='AddVariable()'>Add Variable</button></span>
									</div>
									<div class='row'>
										<span class='col-xs-5'></span>
										<span class='col-xs-7'><font color='red'>{{NewVariableFeedback}}</font></span>
									</div>
									<div class='table-responsive col-xs-12'>
										<table class='table'>
											<tr><th>Variable</th><th>Type</th><th>Variable Range</th><th>Remove</th></tr>
											<tr ng-repeat="(index, EquationVariable) in EquationVariables">
												<td>{{EquationVariable.Topic}}</td>
												<td>
													<select ng-model='EquationVariables[index].Aspect' class='form-control'>
														<option value='Real Number'>Real Numbers</option>
														<option value='Integer'>Integers</option>
														<option value='Text'>Text</option>
													</select>
												</td>
												<td>
													<textarea ng-model='EquationVariables[index].TableEntry' class='form-control'></textarea>
												</td>
												<td>
													<button class='btn btn-default' ng-click="RemoveVariable(index)">Remove</button>
												</td>
											</tr>
										</table>
									</div>
								</div>
							</div>
						</div>
					</form>
				</div>
			</div>
		</div>
	</body>
</html>
