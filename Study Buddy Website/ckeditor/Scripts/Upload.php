<?php
	require_once("../../../includes/dbhandler.php");
	$SecureSession = new SecureSessionHandler();
	session_set_save_handler($SecureSession, true);
	$SecureSession->start();
	if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
		$SecureSession->forget();
		header("Location: index.php");
	}
	$funcNum = $_GET['CKEditorFuncNum'];
	$CKEditor = $_GET['CKEditor'];
	$langCode = $_GET['langCode'];
	$token = $_POST['ckCsrfToken'];
	$url = '';
	$target = '../../Users/' . $SecureSession->get("CurrentUser") . "/" . basename($_FILES['upload']["name"]);
	$imageFileType = pathinfo($target,PATHINFO_EXTENSION);
	if(file_exists($target)) $message = "File already exists";
	else if($_FILES['upload']["size"] > 500000) $message = "File is larger than 500kb";
	else if($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg" && $imageFileType != "gif" ) $message = "Sorry only jpg, png, and gif allowed";
	else {
		if(move_uploaded_file($_FILES['upload']["tmp_name"], $target)) {
			$message = "File uploaded";
			$url = $target;
		}
		else $message = "Sorry unable to upload file";
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
		<link rel="stylesheet" href="site-theme.css">
	</head>
	<body>
		<div class='container-fluid'>
		<?php echo "<script type='text/javascript'>window.parent.CKEDITOR.tools.callFunction($funcNum, '$url', '$message');</script>";?>
		</div>
	</body>
</html>
