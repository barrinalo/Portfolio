<?php 
	require_once("../../../includes/dbhandler.php");
	$SecureSession = new SecureSessionHandler();
	session_set_save_handler($SecureSession, true);
	$SecureSession->start();
	if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
		$SecureSession->forget();
		header("Location: index.php");
	}
	if(isset($_POST['FileToDelete'])) {
		$Dir = "../../Users/" . $SecureSession->get("CurrentUser");
		unlink($Dir . "/" . $_POST['FileToDelete']);
	}
?>
