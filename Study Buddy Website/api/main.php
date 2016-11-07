<?php
require_once("../Scripts/APIHandler.php");
require_once("../Scripts/QuizManager.php");
$APIHandler = new APIHandler();
$QuizHandler = new QuizManager();
$ReturnResult = array();
$data = json_decode(file_get_contents("php://input"));
if(isset($data->AccessToken) && isset($data->Username)) {
	if($APIHandler->VerifyToken($data->Username, $data->AccessToken)) {
		if(isset($data->VerifyToken)) {
			$ReturnResult['TokenValid'] = true;
			echo json_encode($ReturnResult);
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
			$Result = json_decode($APIHandler->GetReadableContent($data->GetReadableContent, $data->Username, $data->Limit, $data->Offset, false, $Creator, $TitleFilter, $FiltersFilter, $DescriptionFilter));
			$ReturnResult['Content'] = $Result;
			echo json_encode($ReturnResult);
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
			$Result = json_decode($APIHandler->GetSubscriptions($data->GetSubscriptions, $data->Username, $data->Limit, $data->Offset, $TitleFilter, $FiltersFilter, $DescriptionFilter, $Creator));
			$ReturnResult['Content'] = $Result;
			echo json_encode($ReturnResult);
		}
		else if(isset($data->Subscribe) && isset($data->Username) && isset($data->ContentID)) {
			if($APIHandler->ReadContentPermitted($data->ContentID, $data->Username)) {
				$Result = $APIHandler->Subscribe($data->ContentID, $data->Username);
				$ReturnResult['SubscriptionResult'] = $Result;
				echo json_encode($ReturnResult);
			}
			else {
				$ReturnResult['SubscriptionResult'] = false;
				echo json_encode($ReturnResult);
			}
		}
		else if(isset($data->Unsubscribe) && isset($data->Username) && isset($data->ContentID)) {
			$Result = $APIHandler->Unsubscribe($data->ContentID, $data->Username);
			$ReturnResult['UnsubscriptionResult'] = $Result;
			echo json_encode($ReturnResult);
		}
		else if(isset($data->GetChildContent) && isset($data->Username) && isset($data->ContentID)) {
			if(isset($data->Override)) $Override = $data->Override;
			else $Override = 0;
			if($APIHandler->ReadContentPermitted($data->ContentID, $data->Username) || $APIHandler->Override($Override, $data->ContentID, $data->Username)) {
				$Result = json_decode($APIHandler->GetChildContent($data->ContentID, $data->Username, $data->GetChildContent));
				$ReturnResult['ChildContent'] = $Result;
				echo json_encode($ReturnResult);
			}
			else {
				$ReturnResult['ChildContent'] = false;
				return json_encode($ReturnResult);
			}
		}
		else if(isset($data->GetStats) && isset($data->Username) && isset($data->ContentID)) {
			if(isset($data->Override)) $Override = $data->Override;
			else $Override = 0;
			$Result = json_decode($QuizHandler->GetStats($data->ContentID, $data->Username, $data->GetStats, $Override));
			$ReturnResult['Stats'] = $Result;
			echo json_encode($ReturnResult);
		}
		else if(isset($data->GetQuestion) && isset($data->Username) && isset($data->ContentID) && isset($data->Override)) {
			echo $QuizHandler->GetQuestion($data->ContentID,$data->Username,$data->GetQuestion, $data->Override);
		}
		else if(isset($data->GetTestQuestions) && isset($data->Username) && isset($data->ContentID) && isset($data->Override)) {
			echo $QuizHandler->GetTestQuestions($data->ContentID,$data->Username, $data->Override);
		}
		else if(isset($data->RecordTest) && isset($data->QuestionRecords) && isset($data->TestRecord)) {
			echo $QuizHandler->RecordTest($data->QuestionRecords, $data->TestRecord);
		}
		else if(isset($data->RecordQuestion) && isset($data->QuestionRecord)) {
			echo $QuizHandler->RecordResults($data->QuestionRecord);
		}
	}
	else {
		$ReturnResult['Error'] = "Access Token invalid";
		echo json_encode($ReturnResult);
	}
}
else if(isset($data->Username) && isset($data->Password)) {
	$Result = $APIHandler->GenerateAccessToken($data->Username, $data->Password);
	if($Result === false) $ReturnResult['Error'] = "Unable to generate Access Token";
	else $ReturnResult['AccessToken'] = $Result;
	echo json_encode($ReturnResult);
}
?>
