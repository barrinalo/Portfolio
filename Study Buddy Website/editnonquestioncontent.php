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
	$Title = "";
	$Description = "";
	$Filters = "";
	$Permissions = "Private";
	$Content = "";
	$Continue = false;
	if(isset($_POST['ContentType'])) $ContentType = $_POST['ContentType'];
	else header("Location: index.php");
	$Handler = new ContentWrapperHandler();
	if(isset($_POST['ContentID'])) $Continue = $Handler->WriteContentPermitted($_POST['ContentID'], $SecureSession->get("CurrentUser"));
	if($Continue) {
		$CurContent = json_decode($Handler->GetContent($_POST['ContentID'], $SecureSession->get("CurrentUser")));
		$Title = $CurContent->Title;
		$Description = $CurContent->Description;
		$Filters = $CurContent->Filters;
		$Permissions = $CurContent->Permissions;
		if($ContentType == "Note") $Content = $CurContent->Content;
	}
?>
<!DOCTYPE html>
<html lang="en" data-ng-app="editnonquestioncontentapp" ng-controller="editnonquestioncontentctrl">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<script src="http://ajax.googleapis.com/ajax/libs/angularjs/1.4.8/angular.min.js"></script>
		<script src="Scripts/editnonquestioncontentapp.js"></script>
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
						<li><a href="#" ng-click="SubmitContentForm(AttachedContentType)">Save Changes</a>
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
					<h1>Creating a {{formData.ContentType}}</h1>
					<center>
						<span class="alert alert-info" ng-if="ProcessingForm">Saving Note...</span>
						<span class="alert alert-success" ng-if="ContentSaveSuccess">Note Saved</span>
						<span class="alert alert-danger" ng-if="ContentSaveFail">Unable to save Note</span>
					</center>
					<div class="panel panel-danger" ng-show="ContentForm.$invalid && ContentForm.$submitted">
						<div class="panel-heading">There are some errors in your {{formData.ContentType}}</div>
						<div class="panel-body">
							<ul>
								<li ng-show="ContentForm.Title.$error.required">{{formData.ContentType}} Title must be filled in</li>
								<li ng-show="ContentForm.Filters.$error.pattern">{{formData.ContentType}} Filters must be preceded by a # and cannot contain whitespace</li>
							</ul>
						</div>
					</div>
					<form name="ContentForm" id="ContentForm" novalidate>
						<div class="form-group">
							<input type='hidden' id='ContentID' ng-model='formData.ContentID' ng-init="formData.ContentID=<?php echo $_POST['ContentID'];?>" value="<?php echo $_POST['ContentID'];?>">
							<input type='hidden' ng-model='formData.Username' ng-init="formData.Username='<?php echo $SecureSession->get('CurrentUser');?>'">
							<input type='hidden' ng-model='formData.ContentType' ng-init="formData.ContentType='<?php echo $ContentType;?>'">
							<label for="Title">{{formData.ContentType}} Title:</label>
							<input type="text" class="form-control" id="Title" name="Title" ng-model="formData.Title" ng-required="true" ng-init="formData.Title='<?php echo $Title;?>'">
							<label for="Description">{{formData.ContentType}} Description:</label>
							<input type="text" class="form-control" id="Description" name="Description" ng-model="formData.Description" ng-init="formData.Description='<?php echo $Description;?>'">
							<label for="Filters">{{formData.ContentType}} Filters:</label>
							<input type="text" class="form-control" id="Filters" name="Filters" ng-model="formData.Filters" ng-init="formData.Filters='<?php echo $Filters;?>'" ng-pattern="FilterRegex">
							<label for="Permission">Permissions:</label>
							<select form="ContentForm" name="Permissions" id="Permissions" ng-model="formData.Permissions" ng-init="formData.Permissions='<?php echo $Permissions;?>'" class="form-control">
								<option value='Private'>Private</option>
								<option value='Selective'>Selective</option>
								<option value='Public'>Public</option>
							</select>
							<div ng-show="formData.ContentType == 'Note'">
								<label for="Content">Content:</label>
								<textarea form="ContentForm" name="Content" id="Content" class="form-control" ng-model="formData.Content" ck-editor ng-init="formData.Content='<?php echo $Content;?>'"></textarea>
							</div>
						</div>
					</form>
							<div class="row">
								<h2 class="col-xs-2">Filters</h2>
								<h2 class="col-xs-4">Available {{AttachedContentType}}s</h2>
								<h2 class="col-xs-6">{{AttachedContentType}}s Tagged to {{formData.ContentType}}</h2>
							</div>
							<div class="row">
								<div class="col-xs-2">
									<form name="ReadableContentFilter">
									<div class="form-group">
										<label for="ContentCreator">Creator:</label>
										<input type='text' ng-model='ContentCreator' class='form-control' ng-change='GetReadableContent(AttachedContentType)'>
										<label for="ContentTitleFilter">Title Filter:</label>
										<input type='text' class='form-control' id='ContentTitleFilter' ng-model="ContentManager[AttachedContentType].TitleFilter" ng-change='GetReadableContent(AttachedContentType)'>
										<label for="ContentFiltersFilter">Filters Filter:</label>
										<input type='text' class='form-control' id='ContentFiltersFilter' ng-model="ContentManager[AttachedContentType].FiltersFilter" ng-change='GetReadableContent(AttachedContentType)'>
										<label for="ContentDescriptionFilter">Description Filter:</label>
										<input type='text' class='form-control' id='ContentDescriptionFilter' ng-model="ContentManager[AttachedContentType].DescriptionFilter" ng-change='GetReadableContent(AttachedContentType)'>
									</div>
									</form>
								</div>
								<div class="table-responsive col-xs-4">
									<table class="table">
										<tr><th>Title</th><th>Creator</th><th>View</th><th>Add to {{formData.ContentType}}</th></tr>
										<tr ng-repeat="y in ContentManager[AttachedContentType].Content" ng-show="!IsAttached(y.ID, y.ContentType)">
										<td>{{y.Title}}</td><td>{{y.Creator}}</td><td><form action='viewcontent.php' method='post' target='_blank'><input type='hidden' name='ContentID' value={{y.ID}}><input type='hidden' name='ContentType' value='{{y.ContentType}}'><input type='submit' class='btn btn-default' value='View'></form></td>
										<td><button type='button' class='btn btn-default' ng-click="AddToContent(y, AttachedContentType)">Add</button></td>
										</tr>
									</table>
									<center><button type='button' class='btn btn-default' ng-show="HasPrevContent(AttachedContentType)" ng-click="PrevContent(AttachedContentType)">Previous</button><button type='button' class='btn btn-default' ng-show="HasNextContent(AttachedContentType)" ng-click="NextContent(AttachedContentType)">Next</button></center>
								</div>
								<div class="table-responsive col-xs-6">
									<table class="table">
										<tr><th>Title</th><th>Creator</th><th>View</th><th>Remove from {{formData.ContentType}}</th><th>Order</th><th>Insert into Note</th></tr>
										<tr ng-repeat="z in ContentManager[AttachedContentType].AttachedContent">
										<td>{{z.Title}}</td><td>{{z.Creator}}</td><td><form action='viewcontent.php' method='post' target='_blank'><input type='hidden' name='ContentID' value={{z.ID}}><input type='hidden' name='ContentType' value='{{z.ContentType}}'><input type='submit' class='btn btn-default' value='View'></form></td>
										<td><button type='button' class='btn btn-default' ng-click="RemoveFromContent(z, AttachedContentType)">Remove</button></td>
										<td><button type='button' class='btn btn-default' ng-click="MoveUp(z, AttachedContentType)" ng-show="NotFirst(z, AttachedContentType)"><span class="glyphicon glyphicon-triangle-top"></span></button><button type='button' class='btn btn-default' ng-click="MoveDown(z, AttachedContentType)" ng-show="NotLast(z, AttachedContentType)"><span class="glyphicon glyphicon-triangle-bottom"></span></button></td><td><button class='btn btn-default' ng-show="z.QuestionType == 'Table' || z.QuestionType == 'Graph' || z.QuestionType == 'Manual'" ng-click="CopyToClipboard('||' + z.ID + '||')">Insert</button></td>
										</tr>
									</table>
								</div>
							</div>
				</div>
			</div>
		</div>
	</body>
</html>
