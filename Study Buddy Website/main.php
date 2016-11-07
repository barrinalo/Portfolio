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
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular-sanitize.js"></script>
		<script src="Scripts/mainapp.js"></script>
		<link rel="stylesheet" href="site-theme.css">
	</head>
	<body data-ng-app="mainapp" ng-controller="mainctrl">
		<nav class="navbar navbar-default navbar-fixed-top">
			<div class="container-fluid">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#mainNavBar">
						<span class="icon-bar"></span>
        		<span class="icon-bar"></span>
        		<span class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="#" ng-click="CurrentDisplay = 'Settings'"><span id="Username"><?php echo $SecureSession->get("CurrentUser");?></span></a>
				</div>
				<div class="collapse navbar-collapse" id="mainNavBar">
					<ul class="nav navbar-nav">
						<li><a href="#" ng-click="CurrentDisplay = 'Home'">Home</a></li>
						<li><a href="#" ng-click="CurrentDisplay = 'Notes'">My Notes</a></li>
						<li><a href="#" ng-click="CurrentDisplay = 'Questions'">My Questions</a></li>
						<li><a href="#" ng-click="CurrentDisplay = 'Packages'">My Packages</a></li>
						<li><a href="#" ng-click="CurrentDisplay = 'FindContent'">Find Content</a></li>
					</ul>
					<ul class="nav navbar-nav navbar-right">
						<li><a href="Scripts/logout.php">Logout</a></li>
					</ul>
				</div>
			</div>
		</nav>
		<div class="container-fluid main-container">
			<div class="row" ng-show="CurrentDisplay == 'Home'">
				<div class="row col-xs-12">
					<div class="col-xs-2">
						<h1>Filters</h1>
						<label for='SubscriptionsPackageCreator'>Creator:</label>
						<input type='text' id='SubscriptionsPackageCreator' value='' class='form-control' ng-model='SubscriptionsPackageCreator' ng-change="GetSubscriptions('SubscriptionsPackage')">
						<label for="SubscriptionsPackageTitleFilter">Title Filter:</label>
						<input type='text' class='form-control' id='SubscriptionsPackageTitleFilter' ng-model="ContentManager['SubscriptionsPackage'].TitleFilter" ng-change="GetSubscriptions('SubscriptionsPackage')">
						<label for="SubscriptionsPackageFiltersFilter">Filters Filter:</label>
						<input type='text' class='form-control' id='SubscriptionsPackageFiltersFilter' ng-model="ContentManager['SubscriptionsPackage'].FiltersFilter" ng-change="GetSubscriptions('SubscriptionsPackage')">
						<label for="SubscriptionsPackageDescriptionFilter">Description Filter:</label>
						<input type='text' class='form-control' id='SubscriptionsPackageDescriptionFilter' ng-model="ContentManager['SubscriptionsPackage'].DescriptionFilter" ng-change="GetSubscriptions('SubscriptionsPackage')">
					</div>
					<div class="col-xs-10">
						<h1>Subscribed Packages</h1>
						<div class="table-responsive">
							<table class="table">
								<tr><th>Title</th><th>Description</th><th>Creator</th><th>Unsubscribe</th><th>View</th></tr>
								<tr ng-repeat="h in ContentManager['SubscriptionsPackage'].Content">
								<td>{{h.Title}}</td><td>{{h.Description}}</td><td>{{h.Creator}}</td><td><button ng-click="Unsubscribe(h.ID, 'SubscriptionsPackage')">Unsubscribe</button></td><td><form action='viewcontent.php' method='post' target="_blank"><input type='hidden' name='ContentID' value={{h.ID}}><input type='hidden' name='ContentType' value='{{h.ContentType}}'><input type='hidden' name='Study' value='1'><input type='submit' value='View'></form></td>
								</tr>
							</table>
						</div>
						<center><button type='button' class='btn btn-default' ng-show="HasPrevContent('SubscriptionsPackage')" ng-click="PrevContent('SubscriptionsPackage')">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent('SubscriptionsPackage')" ng-click="NextContent('SubscriptionsPackage')">Next</button></center>
					</div>
				</div>
				<div class="row col-xs-12">
					<div class="col-xs-2">
						<h1>Filters</h1>
						<label for='SubscriptionsNoteCreator'>Creator:</label>
						<input type='text' id='SubscriptionsNoteCreator' value='' class='form-control' ng-model='SubscriptionsNoteCreator' ng-change="GetSubscriptions('SubscriptionsNote')">
						<label for="SubscriptionsNoteTitleFilter">Title Filter:</label>
						<input type='text' class='form-control' id='SubscriptionsNoteTitleFilter' ng-model="ContentManager['SubscriptionsNote'].TitleFilter" ng-change="GetSubscriptions('SubscriptionsNote')">
						<label for="SubscriptionsNoteFiltersFilter">Filters Filter:</label>
						<input type='text' class='form-control' id='SubscriptionsNoteFiltersFilter' ng-model="ContentManager['SubscriptionsNote'].FiltersFilter" ng-change="GetSubscriptions('SubscriptionsNote')">
						<label for="SubscriptionsNoteDescriptionFilter">Description Filter:</label>
						<input type='text' class='form-control' id='SubscriptionsNoteDescriptionFilter' ng-model="ContentManager['SubscriptionsNote'].DescriptionFilter" ng-change="GetSubscriptions('SubscriptionsNote')">
					</div>
					<div class="col-xs-10">
						<h1>Subscribed Notes</h1>
						<div class="table-responsive">
							<table class="table">
								<tr><th>Title</th><th>Description</th><th>Creator</th><th>Unsubscribe</th><th>View</th></tr>
								<tr ng-repeat="j in ContentManager['SubscriptionsNote'].Content">
								<td>{{j.Title}}</td><td>{{j.Description}}</td><td>{{j.Creator}}</td><td><button ng-click="Unsubscribe(j.ID, 'SubscriptionsNote')">Unsubscribe</button></td><td><form action='viewcontent.php' method='post' target="_blank"><input type='hidden' name='ContentID' value={{j.ID}}><input type='hidden' name='ContentType' value='{{j.ContentType}}'><input type='hidden' name='Study' value='1'><input type='submit' value='View'></form></td>
								</tr>
							</table>
						</div>
						<center><button type='button' class='btn btn-default' ng-show="HasPrevContent('SubscriptionsNote')" ng-click="PrevContent('SubscriptionsNote')">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent('SubscriptionsNote')" ng-click="NextContent('SubscriptionsNote')">Next</button></center>
					</div>
				</div>
			</div>
			<div class="row" ng-show="CurrentDisplay == 'FindContent'">
					<div class="row col-xs-12">
					<div class="col-xs-2">
						<h1>Filters</h1>
						<label for='PackageCreator'>Creator:</label>
						<input type='text' id='PackageCreator' value='' class='form-control' ng-model='PackageCreator' ng-change="GetNonPersonalContent('AvailableSubscriptionsPackage')">
						<label for="AvailableSubscriptionsPackageTitleFilter">Title Filter:</label>
						<input type='text' class='form-control' id='AvailableSubscriptionsPackageTitleFilter' ng-model="ContentManager['AvailableSubscriptionsPackage'].TitleFilter" ng-change="GetNonPersonalContent('AvailableSubscriptionsPackage')">
						<label for="AvailableSubscriptionsPackageFiltersFilter">Filters Filter:</label>
						<input type='text' class='form-control' id='AvailableSubscriptionsPackageFiltersFilter' ng-model="ContentManager['AvailableSubscriptionsPackage'].FiltersFilter" ng-change="GetNonPersonalContent('AvailableSubscriptionsPackage')">
						<label for="AvailableSubscriptionsPackageDescriptionFilter">Description Filter:</label>
						<input type='text' class='form-control' id='AvailableSubscriptionsPackageDescriptionFilter' ng-model="ContentManager['AvailableSubscriptionsPackage'].DescriptionFilter" ng-change="GetNonPersonalContent('AvailableSubscriptionsPackage')">
					</div>
					<div class="col-xs-10">
						<h1>Available Packages</h1>
						<div class="table-responsive">
							<table class="table">
								<tr><th>Title</th><th>Description</th><th>Creator</th><th>View</th><th>Subscribe</th></tr>
								<tr ng-repeat="f in ContentManager['AvailableSubscriptionsPackage'].Content">
								<td>{{f.Title}}</td><td>{{f.Description}}</td><td>{{f.Creator}}</td><td><form action='viewcontent.php' method='post' target="_blank"><input type='hidden' name='ContentID' value={{f.ID}}><input type='hidden' name='ContentType' value='{{f.ContentType}}'><input type='submit' value='View'></form></td><td><button ng-show="!Subscribed(f, 'SubscriptionsPackage')" ng-click="Subscribe(f.ID, 'SubscriptionsPackage')">Subscribe</button><span ng-show="Subscribed(f, 'SubscriptionsPackage')">Subscribed</span></td>
								</tr>
							</table>
						</div>
						<center><button type='button' class='btn btn-default' ng-show="HasPrevContent('AvailableSubscriptionsPackage')" ng-click="PrevContent('AvailableSubscriptionsPackage')">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent('AvailableSubscriptionsPackage')" ng-click="NextContent('AvailableSubscriptionsPackage')">Next</button></center>
					</div>
				</div>
				<div class="row col-xs-12">
					<div class="col-xs-2">
						<h1>Filters</h1>
						<label for='PackageCreator'>Creator:</label>
						<input type='text' id='PackageCreator' value='' class='form-control' ng-model='PackageCreator' ng-change="GetNonPersonalContent('AvailableSubscriptionsNote')">
						<label for="AvailableSubscriptionsNoteTitleFilter">Title Filter:</label>
						<input type='text' class='form-control' id='AvailableSubscriptionsNoteTitleFilter' ng-model="ContentManager['AvailableSubscriptionsNote'].TitleFilter" ng-change="GetNonPersonalContent('AvailableSubscriptionsNote')">
						<label for="AvailableSubscriptionsNoteFiltersFilter">Filters Filter:</label>
						<input type='text' class='form-control' id='AvailableSubscriptionsNoteFiltersFilter' ng-model="ContentManager['AvailableSubscriptionsNote'].FiltersFilter" ng-change="GetNonPersonalContent('AvailableSubscriptionsNote')">
						<label for="AvailableSubscriptionsNoteDescriptionFilter">Description Filter:</label>
						<input type='text' class='form-control' id='AvailableSubscriptionsNoteDescriptionFilter' ng-model="ContentManager['AvailableSubscriptionsNote'].DescriptionFilter" ng-change="GetNonPersonalContent('AvailableSubscriptionsNote')">
					</div>
					<div class="col-xs-10">
						<h1>Available Notes</h1>
						<div class="table-responsive">
							<table class="table">
								<tr><th>Title</th><th>Description</th><th>Creator</th><th>View</th><th>Subscribe</th></tr>
								<tr ng-repeat="d in ContentManager['AvailableSubscriptionsNote'].Content">
								<td>{{d.Title}}</td><td>{{d.Description}}</td><td>{{d.Creator}}</td><td><form action='viewcontent.php' method='post' target="_blank"><input type='hidden' name='ContentID' value={{d.ID}}><input type='hidden' name='ContentType' value='{{d.ContentType}}'><input type='submit' value='View'></form></td><td><button ng-show="!Subscribed(d, 'SubscriptionsNote')" ng-click="Subscribe(d.ID, 'SubscriptionsNote')">Subscribe</button><span ng-show="Subscribed(d, 'SubscriptionsNote')">Subscribed</span></td>
								</tr>
							</table>
						</div>
						<center><button type='button' class='btn btn-default' ng-show="HasPrevContent('AvailableSubscriptionsNote')" ng-click="PrevContent('AvailableSubscriptionsNote')">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent('AvailableSubscriptionsNote')" ng-click="NextContent('AvailableSubscriptionsNote')">Next</button></center>
					</div>
				</div>
			</div>
			<div class="row" ng-show="CurrentDisplay == 'Questions'">
				<div class="col-xs-12">
					<div class="row">
						<div class="col-xs-12">
							<h1>Create a Question</h1>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-12">
							<div class="row flex">
								<div class="col-xs-3">
									<div class="panel panel-default panel-flex">
										<div class="panel-heading">Table</div>
										<div class="panel-body panel-body-flex">
											<p>Questions are generated from data, including images, entered into a table. Types of questions generated include: Flashcards, Single Best Answer, Multiple Best Answer.</p>
										</div>
										<div class="panel-footer">
											<center><form action='editquestion.php' method='post' target="_blank"><input type='hidden' name='Username' value='<?php echo $SecureSession->get("CurrentUser");?>'><input type='hidden' name='QuestionType' value='Table'><input type="submit" class="btn btn-default" value='Create Question'></form></center>
										</div>
									</div>
								</div>
								<div class="col-xs-2">
									<div class="panel panel-default panel-flex">
										<div class="panel-heading">Logical</div>
										<div class="panel-body panel-body-flex">
											<p>Questions are generated from a set of logical gates</p>
										</div>
										<div class="panel-footer">
											<center><form action='editquestion.php' method='post' target="_blank"><input type='hidden' name='Username' value='<?php echo $SecureSession->get("CurrentUser");?>'><input type='hidden' name='QuestionType' value='Logical'><input type="submit" class="btn btn-default" value='Create Question'></form></center>
										</div>
									</div>
								</div>
								<div class="col-xs-2">
									<div class="panel panel-default panel-flex">
										<div class="panel-heading">Graph</div>
										<div class="panel-body panel-body-flex">
											<p>Questions are generated from a graph</p>
										</div>
										<div class="panel-footer">
											<center><form action='editquestion.php' method='post' target="_blank"><input type='hidden' name='Username' value='<?php echo $SecureSession->get("CurrentUser");?>'><input type='hidden' name='QuestionType' value='Graph'><input type="submit" class="btn btn-default" value='Create Question'></form></center>
										</div>
									</div>
								</div>
								<div class="col-xs-2">
									<div class="panel panel-default panel-flex">
										<div class="panel-heading">Equation</div>
										<div class="panel-body panel-body-flex">
											<p>Solution to the question is expressed in a set of equations. Types of questions generated: open ended numerical</p>
										</div>
										<div class="panel-footer">
											<center><form action='editquestion.php' method='post' target="_blank"><input type='hidden' name='Username' value='<?php echo $SecureSession->get("CurrentUser");?>'><input type='hidden' name='QuestionType' value='Equation'><input type="submit" class="btn btn-default" value='Create Question'></form></center>
										</div>
									</div>
								</div>
								<div class="col-xs-3">
									<div class="panel panel-default panel-flex">
										<div class="panel-heading">Manual</div>
										<div class="panel-body panel-body-flex">
											<p>Manually prescribe options for Single Best Answer, Multiple Best Answer, and Flashcards</p>
										</div>
										<div class="panel-footer">
											<center><form action='editquestion.php' method='post' target="_blank"><input type='hidden' name='Username' value='<?php echo $SecureSession->get("CurrentUser");?>'><input type='hidden' name='QuestionType' value='Manual'><input type="submit" class="btn btn-default" value='Create Question'></form></center>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-2">
							<h1>Filters</h1>
							<label for="PersonalQuestionsTitleFilter">Title Filter:</label>
							<input type='text' class='form-control' id='PersonalQuestionsTitleFilter' ng-model="ContentManager['Question'].TitleFilter" ng-change="GetPersonalContent('Question')">
							<label for="PersonalQuestionsFiltersFilter">Filters Filter:</label>
							<input type='text' class='form-control' id='PersonalQuestionsFiltersFilter' ng-model="ContentManager['Question'].FiltersFilter" ng-change="GetPersonalContent('Question')">
							<label for="PersonalQuestionsDescriptionFilter">Description Filter:</label>
							<input type='text' class='form-control' id='PersonalQuestionsDescriptionFilter' ng-model="ContentManager['Question'].DescriptionFilter" ng-change="GetPersonalContent('Question')">
						</div>
						<div class="col-xs-10">
							<h1>My Questions</h1>
							<div class="table-responsive">
								<table class="table">
									<tr><th>Title</th><th>Description</th><th>Type</th><th>Permissions</th><th>Edit</th><th>Delete</th></tr>
									<tr ng-repeat="v in ContentManager['Question'].Content">
									<td>{{v.Title}}</td><td>{{v.Description}}</td><td>{{v.QuestionType}}</td><td>{{v.Permissions}}<form action='managepermissions.php' method='post' target='_blank'><input type='hidden' name='ContentID' value='{{v.ID}}'><input type='submit' value='Manage' class='btn btn-default' ng-show="v.Permissions == 'Selective'"></form></td>
									<td><form action='editquestion.php' method='post' target="_blank"><input type='hidden' name='QuestionID' value='{{v.ID}}'><input type='hidden' name='QuestionType' value='{{v.QuestionType}}'><input type="submit" class='btn btn-default' value="Edit"></form></td><td><button type="button" class="btn btn-default" ng-click="DeletePersonalContent(v.ContentType, v.ID)">Delete</button></td>
									</tr>
								</table>
							</div>
							<center><button type='button' class='btn btn-default' ng-show="HasPrevContent('Question')" ng-click="PrevContent('Question')">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent('Question')" ng-click="NextContent('Question')">Next</button></center>
						</div>
					</div>
				</div>
			</div>
			<div class="row" ng-show="CurrentDisplay == 'Notes'">
				<div class="col-xs-12">
					<div class="row">
						<div class="col-xs-2">
							<h1>Filters</h1>
							<label for="PersonalNoteTitleFilter">Title Filter:</label>
							<input type='text' class='form-control' id='PersonalNoteTitleFilter' ng-model="ContentManager['Note'].TitleFilter" ng-change="GetPersonalContent('Note')">
							<label for="PersonalNoteFiltersFilter">Filters Filter:</label>
							<input type='text' class='form-control' id='PersonalNoteFiltersFilter' ng-model="ContentManager['Note'].FiltersFilter" ng-change="GetPersonalContent('Note')">
							<label for="PersonalNoteDescriptionFilter">Description Filter:</label>
							<input type='text' class='form-control' id='PersonalNoteDescriptionFilter' ng-model="ContentManager['Note'].DescriptionFilter" ng-change="GetPersonalContent('Note')">
						</div>
						<div class="col-xs-10">
							<h1 class="row"><span class="col-xs-10">My Notes</span><span class="col-xs-2"><form action='editnonquestioncontent.php' method='post' target="_blank"><input type='hidden' value='Note' name='ContentType'><input type='submit' value='New Note' class='btn btn-default'></form></span></h1>
							<div class="table-responsive">
								<table class="table">
									<tr><th>Title</th><th>Description</th><th>Permissions</th><th>Edit</th><th>Delete</th><th>Susbcribe</th></tr>
									<tr ng-repeat="y in ContentManager['Note'].Content">
									<td>{{y.Title}}</td><td>{{y.Description}}</td><td>{{y.Permissions}}<form action='managepermissions.php' method='post' target='_blank'><input type='hidden' name='ContentID' value='{{y.ID}}'><input type='submit' value='Manage' class='btn btn-default' ng-show="y.Permissions == 'Selective'"></form></td>
									<td><form action='editnonquestioncontent.php' method='post' target="_blank"><input type='hidden' name='ContentID' value='{{y.ID}}'><input type='hidden' name='ContentType' value='{{y.ContentType}}'><input type="submit" class='btn btn-default' value="Edit"></form></td><td><button type="button" class="btn btn-default" ng-click="DeletePersonalContent(y.ContentType, y.ID)">Delete</button></td><td><button ng-show="!Subscribed(y, 'SubscriptionsNote')" ng-click="Subscribe(y.ID, 'SubscriptionsNote')">Subscribe</button><span ng-show="Subscribed(y, 'SubscriptionsNote')">Subscribed</span></td>
									</tr>
								</table>
							</div>
							<center><button type='button' class='btn btn-default' ng-show="HasPrevContent('Note')" ng-click="PrevContent('Note')">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent('Note')" ng-click="NextContent('Note')">Next</button></center>
						</div>
					</div>
				</div>
			</div>
			<div class="row" ng-show="CurrentDisplay == 'Packages'">
				<div class="col-xs-12">
					<div class="row">
						<div class="col-xs-2">
							<h1>Filters</h1>
							<label for="PersonalPackageTitleFilter">Title Filter:</label>
							<input type='text' class='form-control' id='PersonalPackageTitleFilter' ng-model="ContentManager['Package'].TitleFilter" ng-change="GetPersonalContent('Package')">
							<label for="PersonalPackageFiltersFilter">Filters Filter:</label>
							<input type='text' class='form-control' id='PersonalPackageFiltersFilter' ng-model="ContentManager['Package'].FiltersFilter" ng-change="GetPersonalContent('Package')">
							<label for="PersonalPackageDescriptionFilter">Description Filter:</label>
							<input type='text' class='form-control' id='PersonalPackageDescriptionFilter' ng-model="ContentManager['Package'].DescriptionFilter" ng-change="GetPersonalContent('Package')">
						</div>
						<div class="col-xs-10">
							<h1 class="row"><span class="col-xs-10">My Packages</span><span class="col-xs-2"><form action='editnonquestioncontent.php' method='post' target="_blank"><input type='hidden' value='Package' name='ContentType'><input type='submit' value='New Package' class='btn btn-default'></form></span></h1>
							<div class="table-responsive">
								<table class="table">
									<tr><th>Title</th><th>Description</th><th>Permissions</th><th>Edit</th><th>Delete</th><th>Subscribe</th></tr>
									<tr ng-repeat="b in ContentManager['Package'].Content">
									<td>{{b.Title}}</td><td>{{b.Description}}</td><td>{{b.Permissions}}<form action='managepermissions.php' method='post' target='_blank'><input type='hidden' name='ContentID' value='{{b.ID}}'><input type='submit' value='Manage' class='btn btn-default' ng-show="b.Permissions == 'Selective'"></form></td>
									<td><form action='editnonquestioncontent.php' method='post' target="_blank"><input type='hidden' name='ContentID' value='{{b.ID}}'><input type='hidden' name='ContentType' value='{{b.ContentType}}'><input type="submit" class='btn btn-default' value="Edit"></form></td><td><button type="button" class="btn btn-default" ng-click="DeletePersonalContent(b.ContentType, b.ID)">Delete</button></td><td><button ng-show="!Subscribed(b, 'SubscriptionsPackage')" ng-click="Subscribe(b.ID, 'SubscriptionsPackage')">Subscribe</button><span ng-show="Subscribed(b, 'SubscriptionsPackage')">Subscribed</span></td>
									</tr>
								</table>
							</div>
							<center><button type='button' class='btn btn-default' ng-show="HasPrevContent('Package')" ng-click="PrevContent('Package')">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent('Package')" ng-click="NextContent('Package')">Next</button></center>
						</div>
					</div>
				</div>
			</div>
			<div class="row" ng-show="CurrentDisplay == 'Settings'">
				<div class="col-xs-12">
					<h1>Settings</h1>
					<form name='ChangePasswordForm' id='ChangePasswordForm' novalidate>
					<ul class="alert alert-danger" ng-show="ChangePasswordForm.$invalid && (ChangePasswordForm.$dirty || ChangePasswordForm.$submitted)">
						<li ng-show="ChangePasswordForm.ChangePassword.$error.required">New Password is required</li>
						<li ng-show="ChangePasswordForm.ChangePassword.$error.minlength">New Password must be at least 13 characters</li>
						<li ng-show="ChangePasswordForm.ChangePassword.$error.maxlength">New Password must be less than 256 characters</li>
						<li ng-show="ChangePasswordForm.ChangePassword.$error.sameAs && ChangePasswordForm.ChangePasswordConfirm.$error.sameAs">Passwords do not match</li>
						<li ng-show="ChangePasswordForm.ChangePasswordConfirm.$error.required">New Password confirmation is required</li>
						<li ng-show="ChangePasswordForm.OldPassword.$error.required">Old Password is required</li>
						<li ng-show="ChangePasswordForm.OldPassword.$error.minlength">Old Password must be at least 13 characters</li>
						<li ng-show="ChangePasswordForm.OldPassword.$error.maxlength">Old Password must be less than 256 characters</li>
					</ul>
					<label for="OldPassword">Old Password: </label>
					<input type='password' class='form-control' id="OldPassword" name="OldPassword" ng-model="OldPassword" ng-required="true"  ng-minlength="13" ng-maxlength="255">
					<label for="ChangePassword">New Password: </label>
					<input type='password' class='form-control' id="ChangePassword" name="ChangePassword" ng-model="ChangePassword" ng-required="true" same-as="ChangePasswordConfirm" ng-minlength="13" ng-maxlength="255">
					<label for="ChangePasswordConfirm">Confirm Password: </label>
					<input type='password' class='form-control' id="ChangePasswordConfirm" name="ChangePasswordConfirm" ng-model="ChangePasswordConfirm" ng-required="true" same-as="ChangePassword" ng-minlength="13" ng-maxlength="255">
					<button class='btn btn-default' ng-click="UpdatePassword()">Change password</button>
					</form>
					<span ng-bind-html="ChangePasswordResult"></span>
				</div>
			</div>
		</div>
	</body>
</html>
