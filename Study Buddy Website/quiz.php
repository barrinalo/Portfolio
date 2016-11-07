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
	if(!isset($_POST['ContentID']) && !isset($_POST['ContentType'])) header("Location: index.php");
	$ContentID = $_POST['ContentID'];
	$ContentType = $_POST['ContentType'];
	$IsTest = "";
	$Override = "";
	if(isset($_POST['IsTest'])) $IsTest = "; IsTest=true";
	if(isset($_POST['Override'])) $Override = "; Override=" . $_POST['Override'];
?>
<!DOCTYPE html>
<html lang="en" data-ng-app="quizapp" ng-controller="quizctrl" ng-init="ContentID=<?php echo $ContentID;?>; ContentType='<?php echo $ContentType;?>'; Username='<?php echo $SecureSession->get('CurrentUser');?>'<?php echo $IsTest;?><?php echo $Override;?>">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular-sanitize.js"></script>
		<script src="Scripts/quizapp.js?"></script>
		<script src="ckeditor/ckeditor.js"></script>
		<link rel="stylesheet" href="site-theme.css">
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
					<ul class="nav navbar-nav" ng-show="!IsTest">
						<li ng-show="!Submitted && CurQuestion.QuestionStyle != 'FlashCard'"><a href="#" ng-click="SubmitAnswer()">Submit</a></li>
						<li ng-show="!Submitted && CurQuestion.QuestionStyle == 'FlashCard'"><a href="#" ng-click="FlipCard=true">Flip Card</a></li>
						<li ng-show="!Submitted && FlipCard"><a href="#" ng-click="SubmitFlashCard(1)">I got it right</a></li>
						<li ng-show="!Submitted && FlipCard"><a href="#" ng-click="SubmitFlashCard(0)">I got it wrong</a></li>
						<li ng-show='Submitted'><a href="#" ng-click="Review=true">Review</a></li>
						<li ng-show='Submitted'><a href="#" ng-click="GetQuestion()">Next Question</a></li>
					</ul>
					<ul class="nav navbar-nav" ng-show="IsTest">
						<li ng-show="TestCurQuestion != 0 && !Submitted"><a href="#" ng-click="PrevQuestion()">Previous Question</a></li>
						<li ng-show="TestCurQuestion != (PastQuestions.length - 1) && !Submitted"><a href="#" ng-click="NextQuestion()">Next Question</a></li>
						<li ng-show="!Submitted"><a href="#" ng-click="SubmitTest()">Submit Test</a></li>
						<li ng-show='Submitted && !Review'><a href="#" ng-click="Review=true">Review</a></li>
						<li ng-show="Submitted && !TestSubmitted"><a href="#" ng-click="RecordTest()">Record Test</a></li>
					</ul>
					<ul class="nav navbar-nav navbar-right">
						<li ng-show="IsTest">Question {{TestCurQuestion + 1}} of {{PastQuestions.length}}</li>
						<li><a href="#">{{Math.floor(TimeTaken/60)}}m {{TimeTaken%60}}s</a></li>
					</ul>
				</div>
			</div>
		</nav>
		<div class="container-fluid main-container" ng-show="!CurQuestion.ID">
			<h1>Sorry there are no questions available :(</h1>
		</div>
		<div class="container-fluid main-container" ng-show="CurQuestion.ID && IsTest">
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
			<div class="row col-xs-12" ng-show="!Review">
				<div>
					<b>Question:</b>
					<div ng-bind-html="CurQuestion.DisplayPrompt"></div>
					<div ng-show="CurQuestion.QuestionStyle=='MultipleChoice'">
						<div ng-repeat="x in CurQuestion.DisplayOptions">
							<input type='checkbox' ng-model='UserAnswers[x.ID]' ng-init="UserAnswers[x.ID] = x.UserAnswer" ng-disabled='Submitted'><span ng-bind-html="x.Display"></span><span ng-show="Submitted"><span ng-show="OptionCorrect(x.ID)"><font color='green'>Correct</font></span><span ng-show="!OptionCorrect(x.ID)"><font color='red'>Incorrect</font></span></span>
						</div>
					</div>
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

