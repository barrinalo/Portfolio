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
	if(!isset($_POST['ContentID'])) header("Location: index.php");
?>
<!DOCTYPE html>
<html lang="en" data-ng-app="managepermissionsapp" ng-controller="managepermissionsctrl" ng-init="ContentID=<?php echo $_POST['ContentID'];?>">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		<script src="Scripts/managepermissionsapp.js"></script>
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
					<ul class="nav navbar-nav">
						<li><a href="#" ng-click="WritePermissions()">Save Changes</a>
					</ul>
					<ul class="nav navbar-nav navbar-right">
						<li><a href="Scripts/logout.php">Logout</a></li>
					</ul>
				</div>
			</div>
		</nav>
		<div class="container-fluid main-container">
			<div class="row col-xs-12">
				<h3>Update Content Permissions</h3>
				<center><span class='alert alert-success' ng-show="!Pending && Success === true">Permissions Updated</span><span class='alert alert-danger' ng-show="!Pending && Success === false">Unable to update Permissions</span><span class='alert alert-info' ng-show="Pending">Updating Permissions...</span></center>
			</div>
			<div class="row">
				<div class="col-xs-6">
					<h3>Search For Users</h3>
					<label for="UserSearch">Username Search:</label>
					<input type="text" id="UserSearch" ng-model="UserSearch" class="form-control" ng-change="SearchUsers()">
					<div class="table-responsive">
						<table class="table">
							<tr><th>Username</th><th>Add</th></tr>
							<tr ng-repeat="y in PotentialUsers" ng-show="!IsPermitted(y.Username)"><td>{{y.Username}}</td><td><button class='btn btn-default' ng-click="Add(y.Username)">Add</button></td></tr>
						</table>
					</div>
				</div>
				<div class="col-xs-6">
					<h3>Permitted Users</h3>
					<div class="table-responsive">
						<table class="table">
							<tr><th>Username</th><th>Remove</th></tr>
							<tr ng-repeat="x in PermittedUsers"><td>{{x.Username}}</td><td><button class='btn btn-default' ng-click="Remove(x.Username)">Remove</button></td></tr>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
