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
	if(isset($data->QuestionType) && isset($data->Username) && isset($data->QuestionTitle)) {
		$QuestionHandlerObj = new QuestionHandler();
		if($data->QuestionType == "Manual") {
			$ID = 0;
			if(isset($data->QuestionID)) $ID = $data->QuestionID;
			echo $QuestionHandlerObj->EnterManualQuestion($ID,$data->QuestionTitle,$data->QuestionDescription,$data->QuestionPermissions,$data->Username,$data->QuestionStem, $data->QuestionExplanation, $data->QuestionFilters);
		}
	}
	else return false;
}
?>
