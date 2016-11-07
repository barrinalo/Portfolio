<?php
require_once("../../includes/dbhandler.php");
require_once("QuestionHandler.php");
$SecureSession = new SecureSessionHandler();
session_set_save_handler($SecureSession, true);
$SecureSession->start();
if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
	$SecureSession->forget();
	return false;
}
else {
	$data = json_decode(file_get_contents("php://input"));
}
?>
