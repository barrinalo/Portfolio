<?php
require_once("../../includes/dbhandler.php");
require_once("ContentWrapperHandler.php");
$SecureSession = new SecureSessionHandler();
session_set_save_handler($SecureSession, true);
$SecureSession->start();
if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
	$SecureSession->forget();
	return false;
}
else {
	$data = json_decode(file_get_contents("php://input"));
	$Handler = new ContentWrapperHandler();
	if(isset($data->GetPersonalContent) && isset($data->Username) && isset($data->Offset) && isset($data->Limit)) {
		if(isset($data->TitleFilter)) $TitleFilter = $data->TitleFilter;
		else $TitleFilter = "";
		if(isset($data->FiltersFilter)) $FiltersFilter = $data->FiltersFilter;
		else $FiltersFilter = "";
		if(isset($data->DescriptionFilter)) $DescriptionFilter = $data->DescriptionFilter;
		else $DescriptionFilter = "";
		$Result = $Handler->GetPersonalContent($data->GetPersonalContent, $data->Username, $data->Limit, $data->Offset, $TitleFilter, $FiltersFilter, $DescriptionFilter);
		echo $Result;
	}
	else if(isset($data->GetReadableContent) && isset($data->Username) && isset($data->Offset) && isset($data->Limit)) {
		if(isset($data->Creator)) $Creator = $data->Creator;
		else $Creator = "";
		if(isset($data->TitleFilter)) $TitleFilter = $data->TitleFilter;
		else $TitleFilter = "";
		if(isset($data->FiltersFilter)) $FiltersFilter = $data->FiltersFilter;
		else $FiltersFilter = "";
		if(isset($data->DescriptionFilter)) $DescriptionFilter = $data->DescriptionFilter;
		else $DescriptionFilter = "";
		$Result = $Handler->GetReadableContent($data->GetReadableContent, $data->Username, $data->Limit, $data->Offset, false, $Creator, $TitleFilter, $FiltersFilter, $DescriptionFilter);
		echo $Result;
	}
	else if(isset($data->GetNonPersonalContent) && isset($data->Username) && isset($data->Offset) && isset($data->Limit)) {
		if(isset($data->Creator)) $Creator = $data->Creator;
		else $Creator = "";
		if(isset($data->TitleFilter)) $TitleFilter = $data->TitleFilter;
		else $TitleFilter = "";
		if(isset($data->FiltersFilter)) $FiltersFilter = $data->FiltersFilter;
		else $FiltersFilter = "";
		if(isset($data->DescriptionFilter)) $DescriptionFilter = $data->DescriptionFilter;
		else $DescriptionFilter = "";
		$Result = $Handler->GetReadableContent($data->GetNonPersonalContent, $data->Username, $data->Limit, $data->Offset, true, $Creator, $TitleFilter, $FiltersFilter, $DescriptionFilter);
		echo $Result;
	}
	else if(isset($data->GetChildContent) && isset($data->Username) && isset($data->ContentID)) {
		if($Handler->ReadContentPermitted($data->ContentID, $data->Username)) {
			$Result = $Handler->GetChildContent($data->ContentID, $data->Username, $data->GetChildContent);
			echo $Result;
		}
	}
	else if(isset($data->Subscribe) && isset($data->Username) && isset($data->ContentID)) {
		if($Handler->ReadContentPermitted($data->ContentID, $data->Username)) {
			$Result = $Handler->Subscribe($data->ContentID, $data->Username);
			echo $Result;
		}
	}
	else if(isset($data->Unsubscribe) && isset($data->Username) && isset($data->ContentID)) {
		$Result = $Handler->Unsubscribe($data->ContentID, $data->Username);
		echo $Result;
	}
	else if(isset($data->GetSubscriptions) && isset($data->Username) && isset($data->Offset) && isset($data->Limit)) {
		if(isset($data->Creator)) $Creator = $data->Creator;
		else $Creator = "";
		if(isset($data->TitleFilter)) $TitleFilter = $data->TitleFilter;
		else $TitleFilter = "";
		if(isset($data->FiltersFilter)) $FiltersFilter = $data->FiltersFilter;
		else $FiltersFilter = "";
		if(isset($data->DescriptionFilter)) $DescriptionFilter = $data->DescriptionFilter;
		else $DescriptionFilter = "";
		$Result = $Handler->GetSubscriptions($data->GetSubscriptions, $data->Username, $data->Limit, $data->Offset, $TitleFilter, $FiltersFilter, $DescriptionFilter, $Creator);
		echo $Result;
	}
	else if(isset($data->GetContent) && isset($data->Username) && isset($data->ContentID)) {
		if($Handler->ReadContentPermitted($data->ContentID, $data->Username)) {
			$Result = $Handler->GetContent($data->ContentID, $data->Username);
			echo $Result;
		}
	}
	else if(isset($data->WriteContent) && isset($data->Username) && isset($data->ContentType) && isset($data->Title)) {
		if(!isset($data->QuestionType) && $data->ContentType == "Question") return false;
		else if (isset($data->QuestionType)) $QuestionType = $data->QuestionType;
		else $QuestionType = "";
		if(isset($data->Description)) $Description = $data->Description;
		else $Description = "";
		if(isset($data->Permissions)) $Permissions = $data->Permissions;
		else $Permissions = "";
		if(isset($data->Filters)) $Filters = $data->Filters;
		else $Filters = "";
		if(isset($data->Relations)) $Relations = $data->Relations;
		else $Relations = array();
		if(isset($data->Content)) $Content = $data->Content;
		else $Content = "";
		if(isset($data->ContentID)) if($data->ContentID == "") unset($data->ContentID);
		$Result = false;
		if(isset($data->ContentID)) {
			if($Handler->WriteContentPermitted($data->ContentID, $data->Username)) {
				$Result = $Handler->WriteContent($data->ContentID, $data->Title, $Description, $Permissions, $data->Username, $Filters, $data->ContentType, $QuestionType, $Content, $Relations);
			}
		}
		else $Result = $Handler->WriteContent(0, $data->Title, $Description, $Permissions, $data->Username, $Filters, $data->ContentType, $QuestionType, $Content, $Relations);
		echo $Result;
	}
	else if(isset($data->DeleteContent) && isset($data->ContentID) && isset($data->Username)) {
		$Result = $Handler->DeleteContent($data->ContentID, $data->Username, $data->DeleteContent);
		echo $Result;
	}
	else if(isset($data->GetPermittedUsers) && isset($data->ContentID) && isset($data->Username)) {
		$Result = $Handler->GetPermittedUsers($data->Username, $data->ContentID);
		echo $Result;
	}
	else if(isset($data->SearchUsers) && isset($data->SearchText)) {
		$Result = $Handler->SearchUsers($data->SearchText);
		echo $Result;
	}
	else if(isset($data->WritePermissions) && isset($data->Username) && isset($data->ContentID) && isset($data->UserList)) {
		$Result = $Handler->WritePermissions($data->Username, $data->ContentID, $data->UserList);
		echo $Result;
	}
	else echo false;
}
?>
