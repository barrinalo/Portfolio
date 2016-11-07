<?php
require_once("../../includes/dbhandler.php");
require_once("UserDetailsHandler.php");
$data = json_decode(file_get_contents("php://input"));
if(isset($data->LoginUsername) && isset($data->LoginPassword)) {
	$LoginObj = new UserDetailsHandler();
	$Result = $LoginObj->VerifyUser($data->LoginUsername, $data->LoginPassword);
	if($Result) {
		$SecureSession = new SecureSessionHandler();
		session_set_save_handler($SecureSession, true);
		$SecureSession->start();
		$SecureSession->put("CurrentUser", $data->LoginUsername);
	}
	echo $Result;
}
?>
