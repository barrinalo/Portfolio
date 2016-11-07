<?php
	require_once("../../includes/dbhandler.php");
	require_once("../Scripts/ContentWrapperHandler.php");
	require_once("../Scripts/QuizManager.php");
	if(!isset($_POST['Username']) && !isset($_POST['AccessToken'])) header("Location: index.php");
	if(!isset($_POST['ContentID']) && !isset($_POST['ContentType'])) header("Location: index.php");
	$ContentID = $_POST['ContentID'];
	$ContentType = $_POST['ContentType'];
	$Username = $_POST['Username'];
	$AccessToken = $_POST['AccessToken'];
	$IsTest = "";
	$Override = "";
	if(isset($_POST['IsTest'])) $IsTest = "; IsTest=true";
	if(isset($_POST['Override'])) $Override = "; Override=" . $_POST['Override'];
?>
<!DOCTYPE html>
<html lang="en" data-ng-app="quizapp" ng-controller="quizctrl" ng-init="ContentID=<?php echo $ContentID;?>; ContentType='<?php echo $ContentType;?>'; Username='<?php echo $Username;?>'; AccessToken='<?php echo $AccessToken;?>'<?php echo $IsTest;?><?php echo $Override;?>">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular-sanitize.js"></script>
		<script src="quizapp.js"></script>
		<link rel="stylesheet" href="../site-theme.css">
	</head>
	<body>
		<div class="container-fluid main-container" ng-show="!CurQuestion.ID">
			<h1>Sorry there are no questions available :(</h1>
		</div>
		<div class="container-fluid main-container" ng-show="CurQuestion.ID && IsTest">
			<div class="row col-xs-12">
				<div>Question {{TestCurQuestion + 1}} of {{PastQuestions.length}}</div>
				<div>Time Taken: {{Math.floor(TimeTaken/60)}}m {{TimeTaken%60}}s</div>
				<button class='btn btn-default' ng-show="TestCurQuestion != 0 && !Submitted" ng-click="PrevQuestion()">Previous Question</button>
				<button class='btn btn-default' ng-show="TestCurQuestion != (PastQuestions.length - 1) && !Submitted" ng-click="NextQuestion()">Next Question</button>
				<button class='btn btn-default' ng-show="!Submitted" ng-click="SubmitTest()">Submit Test</button>
				<button class='btn btn-default' ng-show='Submitted && !Review' ng-click="Review=true">Review</button>
				<button class='btn btn-default' ng-show="Submitted && !TestSubmitted" ng-click="RecordTest()">Record Test</button>
			</div>
			<div class="row col-xs-12" ng-show="!Review">
				<div>
					<b>Question:</b>
					<div ng-bind-html="CurQuestion.DisplayPrompt"></div>
					<div ng-show="CurQuestion.QuestionStyle=='MultipleChoice'">
						<div ng-repeat="a in CurQuestion.DisplayOptions">
							<input type='checkbox' ng-model='UserAnswers[a.ID]' ng-init="UserAnswers[a.ID] = a.UserAnswer" ng-disabled='Submitted'><span ng-bind-html="a.Display"></span><span ng-show="Submitted"><span ng-show="OptionCorrect(a.ID)"><font color='green'>Correct</font></span><span ng-show="!OptionCorrect(a.ID)"><font color='red'>Incorrect</font></span></span>
						</div>
					</div>
				</div>
				<div ng-show="Submitted || FlipCard" ng-bind-html="CurQuestion.QuestionExplanation"></div>
			</div>
			<div class="row col-xs-12" ng-show="Review">
				<h1>Test Review</h1>
				<span ng-show="TestSubmitted">Your Result: {{TestResult*100}}%</span>
				<div class="table-responsive">
					<table class="table">
						<tr><th>Question Prompt</th><th>Result</th><th>Time Taken</th><th>Review</th></tr>
						<tr ng-repeat="b in PastQuestions"><td ng-bind-html="b.DisplayPrompt"></td><td><span ng-show="b.Result !== undefined">{{b.Result*100}}%</span><span ng-show="b.Result === undefined && b.QuestionStyle == 'FlashCard'"><button class='btn btn-default' ng-click='MarkFlashCard(1, b.ID)'>I got it right</button><button class='btn btn-default' ng-click='MarkFlashCard(0, b.ID)'>I got it wrong</button></span></td><td>{{Math.floor(b.TimeTaken/60)}}m {{b.TimeTaken%60}}s</td><td><button class='btn btn-default' ng-click="ReviewQuestion(b)">Review</button></td></tr>
					</table>
				</div>
			</div>
		</div>
		<div class="container-fluid main-container" ng-show="CurQuestion.ID && !IsTest">
			<div class="row col-xs-12">
				<div>Time Taken: {{Math.floor(TimeTaken/60)}}m {{TimeTaken%60}}s</div>
				<button class='btn btn-default' ng-show="!Submitted && CurQuestion.QuestionStyle != 'FlashCard'" ng-click="SubmitAnswer()">Submit</button>
				<button class='btn btn-default' ng-show="!Submitted && CurQuestion.QuestionStyle == 'FlashCard'" ng-click="FlipCard=true">Flip Card</button>
				<button class='btn btn-default' ng-show="!Submitted && FlipCard" ng-click="SubmitFlashCard(1)">I got it right</button>
				<button class='btn btn-default' ng-show="!Submitted && FlipCard" ng-click="SubmitFlashCard(0)">I got it wrong</button>
				<button class='btn btn-default' ng-show='Submitted' ng-click="Review=true">Review</button>
				<button class='btn btn-default' ng-show='Submitted' ng-click="GetQuestion()">Next Question</button>
			</div>
			<div class="row col-xs-12" ng-show="!Review">
				<div>
					<b>Question:</b>
					<div ng-bind-html="CurQuestion.DisplayPrompt"></div>
					<div ng-show="CurQuestion.QuestionStyle=='MultipleChoice'">
						<div ng-repeat="x in CurQuestion.DisplayOptions">
							<input type='checkbox' ng-model='UserAnswers[x.ID]' ng-init="UserAnswers[x.ID] = x.UserAnswer" ng-disabled='Submitted'><span ng-bind-html="x.Display"></span><span ng-show="Submitted"><span ng-show="OptionCorrect(x.ID)"><font color='green'>Correct</font></span><span ng-show="!OptionCorrect(x.ID)"><font color='red'>Incorrect</font></span></span>
						</div>
					</div>
					<!-- {{CurQuestion.DisplayOptions}} {{CurQuestion.QuestionStyle}} {{UserAnswers}}-->
				</div>
				<div ng-show="Submitted || FlipCard" ng-bind-html="CurQuestion.QuestionExplanation"></div>
			</div>
			<div class="row col-xs-12" ng-show="Review">
				<h1>Review</h1>
				<div class="table-responsive">
					<table class="table">
						<tr><th>Question Prompt</th><th>Result</th><th>Time Taken</th><th>Review</th></tr>
						<tr ng-repeat="y in PastQuestions"><td ng-bind-html="y.DisplayPrompt"></td><td>{{y.Result*100}}%</td><td>{{Math.floor(y.TimeTaken/60)}}m {{y.TimeTaken%60}}s</td><td><button class='btn btn-default' ng-click="ReviewQuestion(y)">Review</button></td></tr>
					</table>
				</div>
			</div>
		</div>
	</body>
</html>

