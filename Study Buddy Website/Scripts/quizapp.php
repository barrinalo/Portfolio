<?php
require_once("../../includes/dbhandler.php");
require_once("ContentWrapperHandler.php");
require_once("QuizManager.php");
$SecureSession = new SecureSessionHandler();
session_set_save_handler($SecureSession, true);
$SecureSession->start();
if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
	$SecureSession->forget();
	return false;
}
else {
	$data = json_decode(file_get_contents("php://input"));
	$Handler = new QuizManager();
	if(isset($data->GetQuestion) && isset($data->Username) && isset($data->ContentID) && isset($data->Override)) {
		echo $Handler->GetQuestion($data->ContentID,$data->Username,$data->GetQuestion, $data->Override);
	}
	else if(isset($data->RecordQuestion) && isset($data->QuestionRecord)) {
		echo $Handler->RecordResults($data->QuestionRecord);
	}
	else if(isset($data->GetTestQuestions) && isset($data->Username) && isset($data->ContentID) && isset($data->Override)) {
		echo $Handler->GetTestQuestions($data->ContentID,$data->Username, $data->Override);
	}
	else if(isset($data->RecordTest) && isset($data->QuestionRecords) && isset($data->TestRecord)) {
		echo $Handler->RecordTest($data->QuestionRecords, $data->TestRecord);
	}
}
?>
