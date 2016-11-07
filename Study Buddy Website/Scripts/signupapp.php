<?php
require_once("../../includes/dbhandler.php");
require_once("UserDetailsHandler.php");

$data = json_decode(file_get_contents("php://input"));
if(isset($data->SignupUsername) && isset($data->SignupEmail) && isset($data->SignupPassword)) {
	$SignupObj = new UserDetailsHandler();
	$Emailexists = $SignupObj->CheckEmailExists($data->SignupEmail);
	$Userexists = $SignupObj->CheckUsernameExists($data->SignupUsername);
	if(!$Userexists && !$Emailexists) {
		$result = $SignupObj->RegisterNewUser($data->SignupUsername, $data->SignupEmail, $data->SignupPassword);
		echo $result;
	}
	else echo false;
}
else if(isset($data->SignupUsername)) {
	$SignupObj = new UserDetailsHandler();
	$Result = $SignupObj->CheckUsernameExists($data->SignupUsername);
	echo $Result;
}
else if(isset($data->SignupEmail)) {
	$SignupObj = new UserDetailsHandler();
	$Result = $SignupObj->CheckEmailExists($data->SignupEmail);
	echo $Result;
}
else if(isset($data->EmailExists)) {
	$SignupObj = new UserDetailsHandler();
	$Result = $SignupObj->CheckEmailExists($data->EmailExists);
	echo !$Result;
}
else if(isset($data->ChangePassword) && isset($data->Username) && isset($data->OldPassword) && isset($data->NewPassword)) {
	$SignupObj = new UserDetailsHandler();
	$Result = $SignupObj->ChangePassword($data->Username, $data->OldPassword, $data->NewPassword);
	echo $Result;
}
else if(isset($data->ResetPassword)) {
	$SignupObj = new UserDetailsHandler();
	$Result = $SignupObj->ResetPassword($data->ResetPassword);
	echo $Result;
}
?>
