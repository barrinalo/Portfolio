<?php
	require_once("../includes/dbhandler.php");
	$SecureSession = new SecureSessionHandler();
	session_set_save_handler($SecureSession, true);
	$SecureSession->start();
	if($SecureSession->isValid(5) && $SecureSession->get("CurrentUser") != null) header("Location: main.php");
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
		<script src="Scripts/signupapp.js"></script>
		<link rel="stylesheet" href="site-theme.css">
	</head>
	<body data-ng-app="signupapp" ng-controller="signupctrl">
		<nav class="navbar navbar-default navbar-fixed-top">
			<div class="container-fluid">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#indexNavBar">
						<span class="icon-bar"></span>
        		<span class="icon-bar"></span>
        		<span class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="index.php">Study Buddy</a>
				</div>
				<div class="collapse navbar-collapse" id="indexNavBar">
					<ul class="nav navbar-nav">
						<li><a href="#Overview">Overview</a></li>
						<li><a href="#TakingNotes">Taking Notes</a></li>
						<li><a href="#SelfTesting">Testing Yourself</a></li>
						<li><a href="#Sharing">Sharing</a></li>
					</ul>
					<ul class="nav navbar-nav navbar-right">
						<li><a href="#Signup">Sign up</a></li>
						<li><a href="#Login">Login</a></li>
					</ul>
				</div>
			</div>
		</nav>
		<div class="container-fluid main-container">
			<div class="row">
				<div class="col-xs-12">
					<div class="jumbotron jumbotron-fullwindowheight" id="Overview">
						<h1>
							What is Study Buddy?
						</h1>
						<p>
							Study Buddy is a platform to create Notes and Questions to aid the studying process. Questions are linked to Notes to support the cyclic process of reading and testing understanding.
						</p>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<div class="jumbotron jumbotron-fullwindowheight" id="TakingNotes">
						<h1>
							Taking Notes with Study Buddy
						</h1>
						<p>
							Everyone has their own method for making Notes, and Study Buddy does not force you to use a particular method. Besides providing a rich text editor online for creating notes, Study Buddy will support other file formats for uploading notes created in another medium.  Notes should be short and focused on a single concept rather than monolithic.
						</p>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<div class="jumbotron jumbotron-fullwindowheight" id="SelfTesting">
						<h1>
							Testing Yourself with Study Buddy
						</h1>
						<p>
							Study Buddy aims to make the Question making process less laborious by providing data entry in forms such as Tables, Equations, Logical Statements, etc. to facilitate question generation.
						</p>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<div class="jumbotron jumbotron-fullwindowheight" id="Sharing">
						<h1>
							Sharing Notes and Questions
						</h1>
						<p>
							You can use Study Buddy privately, make your Notes and Questions publicly available, or share them with a select group of people.
						</p>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12">
					<div class="panel panel-default" id="Signup">
						<div class="panel-heading">
							Sign Up
						</div>
						<div class="panel-body">
							<form name="SignupForm" ng-submit="RegisterNewUser(SignupForm.$valid)" id="SignupForm" novalidate>
								<div class="row">
									<div class="col-xs-2">
										<label for="SignupUsername">Username:</label>
									</div>
									<div class="col-xs-6">
										<input type="text" id="SignupUsername" name="SignupUsername" class="form-control" ng-model="SignupUsername" ng-minlength="8" ng-maxlength="25" ng-pattern="usernameregex" ng-required="true" username-available>
									</div>
									<div class="col-xs-4">
										<span ng-if="SignupForm.SignupUsername.$pending">Checking if Username is available</span>
										<ul class="alert alert-danger" ng-show="SignupForm.SignupUsername.$invalid && SignupForm.SignupUsername.$dirty">
											<li ng-show="SignupForm.SignupUsername.$error.required">Username is required</li>
											<li ng-show="SignupForm.SignupUsername.$error.minlength">Username must be at least 8 characters</li>
											<li ng-show="SignupForm.SignupUsername.$error.maxlength">Username must be less than 26 characters</li>
											<li ng-show="SignupForm.SignupUsername.$error.usernameAvailable">Username is already in use</li>
											<li ng-show="SignupForm.SignupUsername.$error.pattern">Username can only contain alphanumeric, _, and - characters</li>
										</ul>
									</div>
								</div>
								<div class="row">
									<div class="col-xs-2">
										<label for="SignupEmail">Email:</label>
									</div>
									<div class="col-xs-6">
										<input type="email" id="SignupEmail" name="SignupEmail" class="form-control" ng-model="SignupEmail" ng-pattern="emailregex" ng-required="true" email-available>
									</div>
									<div class="col-xs-4">
										<span ng-if="SignupForm.SignupEmail.$pending">Checking if Email is available</span>
										<ul class="alert alert-danger" ng-show="SignupForm.SignupEmail.$invalid && SignupForm.SignupEmail.$dirty">
											<li ng-show="SignupForm.SignupEmail.$error.required">Email is required</li>
											<li ng-show="SignupForm.SignupEmail.$error.pattern">Please enter a valid email</li>
											<li ng-show="SignupForm.SignupEmail.$error.emailAvailable">Email already in use</li>
										</ul>
									</div>
								</div>
								<div class="row">
									<div class="col-xs-2">
										<label for="SignupPassword">Password:</label>
									</div>
									<div class="col-xs-6">
										<input type="password" id="SignupPassword" name="SignupPassword" class="form-control" ng-model="SignupPassword" ng-minlength="13" ng-maxlength="255" ng-required="true" same-as="SignupPasswordConfirm">
									</div>
									<div class="col-xs-4">
										<ul class="alert alert-danger" ng-show="SignupForm.SignupPassword.$invalid && SignupForm.SignupPassword.$dirty">
											<li ng-show="SignupForm.SignupPassword.$error.required">Password is required</li>
											<li ng-show="SignupForm.SignupPassword.$error.minlength">Password must be at least 13 characters</li>
											<li ng-show="SignupForm.SignupPassword.$error.maxlength">Password must be less than 256 characters</li>
											<li ng-show="SignupForm.SignupPassword.$error.sameAs">Passwords do not match</li>
										</ul>
									</div>
								</div>
								<div class="row">
									<div class="col-xs-2">
										<label for="SignupPasswordConfirm">Confirm Password:</label>
									</div>
									<div class="col-xs-6">
										<input type="password" id="SignupPasswordConfirm" name="SignupPasswordConfirm" class="form-control" ng-model="SignupPasswordConfirm" ng-minlength="13" ng-maxlength="255" ng-required="true" same-as="SignupPassword">
									</div>
									<div class="col-xs-4">
										<ul class="alert alert-danger" ng-show="SignupForm.SignupPasswordConfirm.$invalid && SignupForm.SignupPasswordConfirm.$dirty">
											<li ng-show="SignupForm.SignupPasswordConfirm.$error.required">Password confirmation is required</li>
											<li ng-show="SignupForm.SignupPasswordConfirm.$error.minlength">Password must be at least 13 characters</li>
											<li ng-show="SignupForm.SignupPasswordConfirm.$error.maxlength">Password must be less than 256 characters</li>
											<li ng-show="SignupForm.SignupPasswordConfirm.$error.sameAs">Passwords do not match</li>
										</ul>
									</div>
								</div>
								<div class="row">
									<center><input type="submit" value="Sign Up" ng-disabled="SignupForm.$invalid || SignupForm.$pending" class='btn btn-default'></center>
								</div>
								<div class="row">
									<center>
										<span ng-if="processingform" class="alert alert-info">Registering...</span>
										<span ng-if="registersuccess" class="alert alert-success">Your account has been successfully created</span>
										<span ng-if="registerfail" class="alert alert-danger">Sorry we were unable to create your account</span>
									</center>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12 fullheight">
					<div class="panel panel-default" id="Login">
						<div class="panel-heading">
							Login
						</div>
						<div class="panel-body">
							<form name="LoginForm" ng-submit="Login(LoginForm.$valid)" id="LoginForm" novalidate>
								<div class="row">
									<div class="col-xs-2">
										<label for="LoginUsername">Username:</label>
									</div>
									<div class="col-xs-6">
										<input type="text" id="LoginUsername" name="LoginUsername" class="form-control" ng-model="LoginUsername" ng-required="true">
									</div>
									<div class="col-xs-4">
										<span class="alert alert-danger" ng-if="LoginForm.LoginUsername.$error.required && LoginForm.LoginUsername.$dirty">Please enter your username</span>
									</div>
								</div>
								<div class="row">
									<div class="col-xs-2">
										<label for="LoginPassword">Password:</label>
									</div>
									<div class="col-xs-6">
										<input type="password" id="LoginPassword" name="LoginPassword" class="form-control" ng-model="LoginPassword" ng-required="true">
									</div>
									<div class="col-xs-4">
										<span class="alert alert-danger" ng-if="LoginForm.LoginUsername.$error.required && LoginForm.LoginPassword.$dirty">Please enter your password</span>
									</div>
								</div>
								<div class="row">
									<center><input type="submit" value="Login" ng-disabled="LoginForm.$invalid" class='btn btn-default'></center>
								</div>
								<div class="row">
									<center>
										<span class="alert alert-info" ng-if="processinglogin">Logging in...</span>
										<span class="alert alert-success" ng-if="loginsuccess">Login Success</span>
										<span class="alert alert-danger" ng-if="loginfail">Username and Password combination not recognised</span>
									</center>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
			<div class="row">
				<div class="col-xs-12 fullheight">
					<div class="panel panel-default" id="Login">
						<div class="panel-heading">
							Forgotten Password
						</div>
						<div class="panel-body">
							<form name="ForgottenPasswordForm" novalidate>
								<div class="row">
									<div class="col-xs-2">
										<label for="ForgottenEmail">Email:</label>
									</div>
									<div class="col-xs-6">
										<input type='text' id='ForgottenEmail' name="ForgottenEmail" ng-model="ForgottenEmail" ng-required="true" email-exists ng-pattern="emailregex" class='form-control'>
									</div>
									<div class="col-xs-4">
										<span ng-if="ForgottenPasswordForm.ForgottenEmail.$pending">Checking if Email exists</span>
										<ul class="alert alert-danger" ng-show="ForgottenPasswordForm.ForgottenEmail.$invalid && ForgottenPasswordForm.ForgottenEmail.$dirty">
											<li ng-show="ForgottenPasswordForm.ForgottenEmail.$error.required">Email is required</li>
											<li ng-show="ForgottenPasswordForm.ForgottenEmail.$error.pattern">Please enter a valid email</li>
											<li ng-show="ForgottenPasswordForm.ForgottenEmail.$error.emailExists">Email does not exist</li>
										</ul>
									</div>
								</div>
								<div class="row">
									<center><button class='btn btn-default' ng-click="ResetPassword()" ng-disabled="ForgottenPasswordForm.$invalid || ForgottenPasswordForm.$pending">Reset password</button></center>
								</div>
								<div class="row">
									<center><span class='alert alert-info' ng-show="PasswordResetResult">{{PasswordResetResult}}<span></center>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>

