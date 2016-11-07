<?php
class QuizManager extends ContentWrapperHandler {
	protected $QuestionHistory = "questionhistory";
	protected $TestHistory = "testhistory";

	function __construct() {
		parent::__construct();
	}

	function GetStats($ID, $Username, $ContentType, $Override) {
		try{
			$Result = array();
			$continue = $this->ReadContentPermitted($ID, $Username);
			if($Override != "" && !$continue) $continue = $this->Override($Override, $ID, $Username);
			if($continue) {
				if($ContentType == "Note") {
					$CurContent = json_decode($this->GetContent($ID, $Username));
					$AvailableQuestions = json_decode($this->GetChildContent($ID, $Username, $ContentType));
					$Result = array();
					$Result['UniqueQuestions'] = count($AvailableQuestions);
					if(count($AvailableQuestions) > 0) {
						$sql = "FROM " . $this->QuestionHistory . " WHERE Username=:Username AND (";
						for($i = 0; $i < count($AvailableQuestions); $i++) {
							$sql .= "QuestionID=" . $AvailableQuestions[$i]->ID . " OR ";
						}
						$sql = substr($sql, 0, strlen($sql) - 4);
						$sql .= ")";
						$AttemptedQuestions = $this->conn->prepare("SELECT DISTINCT QuestionID " . $sql);
						$AttemptedQuestions->bindParam(":Username", $Username);
						if(!$AttemptedQuestions->execute()) return false;
						$Result['AttemptedQuestions'] = $AttemptedQuestions->rowCount();
						$Accuracy = $this->conn->prepare("SELECT * " . $sql);
						$Accuracy->bindParam(":Username", $Username);
						if(!$Accuracy->execute()) return false;
						$attemptcount = 0;
						$runningscore = 0;
						$runningtime = 0;
						while($row = $Accuracy->fetch(PDO::FETCH_ASSOC)) {
							if($attemptcount == 0) {
								$runningscore = $row['Result'];
								$runningtime = $row['TimeTaken'];
							}
							else {
								$runningscore += $row['Result'];
								$runningtime += $row['TimeTaken'];
							}
							$attemptcount++;
						}
						if($attemptcount != 0) {
							$runningscore /= $attemptcount;
							$runningtime /= $attemptcount;
						}
						$Result['AverageScore'] = $runningscore;
						$Result['AverageTimeTaken'] =$runningtime;
						$Result['NumberOfAttempts'] = $attemptcount;
						$Contributors = array();
						for($i = 0; $i < count($AvailableQuestions); $i++) if($AvailableQuestions[$i]->Creator != $CurContent->Creator && !in_array($AvailableQuestions[$i]->Creator, $Contributors)) $Contributors[] = $AvailableQuestions[$i]->Creator;
						$Result['Contributors'] = $Contributors;
					}
					else {
						$Result['Contributors'] = array();
						$Result['AttemptedQuestions'] = 0;
						$Result['AverageScore'] = 0;
						$Result['AverageTimeTaken'] = 0;
						$Result['NumberOfAttempts'] = 0;
					}
					$Result['Creator'] = $CurContent->Creator;
					return json_encode($Result);
				}
				else if($ContentType == "Package") {
					$CurContent = json_decode($this->GetContent($ID, $Username));
					$AvailableNotes = json_decode($this->GetChildContent($ID, $Username, $ContentType));
					if(count($AvailableNotes) > 0) {
						$AvailableQuestions = array();
						for($i = 0; $i < count($AvailableNotes); $i++) {
							$AvailableQuestions = array_merge($AvailableQuestions, json_decode($this->GetChildContent($AvailableNotes[$i]->ID, $Username, "Note")));
						}
						$QuestionIDs = array();
						for($i = 0; $i < count($AvailableQuestions); $i++) if(!in_array($AvailableQuestions[$i]->ID,$QuestionIDs)) $QuestionIDs[] = $AvailableQuestions[$i]->ID;
						if(count($QuestionIDs) > 0) {
							$sql = "FROM " . $this->QuestionHistory . " WHERE Username=:Username AND (";
							for($i = 0; $i < count($QuestionIDs); $i++) {
								$sql .= " QuestionID=" . $QuestionIDs[$i] . " OR ";
							}
							$sql = substr($sql, 0, strlen($sql) - 4);
							$sql .= ")";
							$AttemptedQuestions = $this->conn->prepare("SELECT DISTINCT QuestionID " . $sql);
							$AttemptedQuestions->bindParam(":Username", $Username);
							if(!$AttemptedQuestions->execute()) return false;
							$Result['AttemptedQuestions'] = $AttemptedQuestions->rowCount();
							$Accuracy = $this->conn->prepare("SELECT * " . $sql);
							$Accuracy->bindParam(":Username", $Username);
							if(!$Accuracy->execute()) return false;
							$attemptcount = 0;
							$runningscore = 0;
							$runningtime = 0;
							while($row = $Accuracy->fetch(PDO::FETCH_ASSOC)) {
								if($attemptcount == 0) {
									$runningscore = $row['Result'];
									$runningtime = $row['TimeTaken'];
								}
								else {
									$runningscore += $row['Result'];
									$runningtime += $row['TimeTaken'];
								}
								$attemptcount++;
							}
							if($attemptcount != 0) {
								$runningscore /= $attemptcount;
								$runningtime /= $attemptcount;
							}
							$Result['AverageScore'] = $runningscore;
							$Result['AverageTimeTaken'] =$runningtime;
							$Result['NumberOfAttempts'] = $attemptcount;
						}
						else {
							$Result['UniqueQuestions'] = 0;
							$Result['AttemptedQuestions'] = 0;
							$Result['AverageScore'] = 0;
							$Result['AverageTimeTaken'] = 0;
							$Result['NumberOfAttempts'] = 0;
						}
						$Contributors = array();
						for($i = 0; $i < count($AvailableQuestions); $i++) if($AvailableQuestions[$i]->Creator != $CurContent->Creator && !in_array($AvailableQuestions[$i]->Creator, $Contributors)) $Contributors[] = $AvailableQuestions[$i]->Creator;
						for($i = 0; $i < count($AvailableNotes); $i++) if($AvailableNotes[$i]->Creator != $CurContent->Creator && !in_array($AvailableNotes[$i]->Creator, $Contributors)) $Contributors[] = $AvailableNotes[$i]->Creator;
						$Result['Contributors'] = $Contributors;
						$Result['UniqueQuestions'] = count($QuestionIDs);
					}
					else {
						$Result['UniqueQuestions'] = 0;
						$Result['Contributors'] = array();
						$Result['AttemptedQuestions'] = 0;
						$Result['AverageScore'] = 0;
						$Result['AverageTimeTaken'] = 0;
						$Result['NumberOfAttempts'] = 0;
					}
					$Result['Creator'] = $CurContent->Creator;
					return json_encode($Result);
				}
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}

	function GetTest($ID, $Username, $Override) {
		try{
			$continue = $this->ReadContentPermitted($ID, $Username);
			if($Override != "" && !$continue) $continue = $this->Override($Override, $ID, $Username);
			if($continue) {
				$ChildContent = json_decode($this->GetChildContent($ID, $Username, 'Note'));
				for($i = 0; $i < count($ChildContent); $i++) $ChildContent[$i]->QuestionOptions = json_decode($this->GetChildContent($ChildContent[$i]->ID, $Username));
				return json_encode($ChildContent);
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function GetFlashcard($ID, $Username, $ContentType, $Override) {
		try{
			$continue = $this->ReadContentPermitted($ID, $Username);
			if($Override != "" && !$continue) $continue = $this->Override($Override, $ID, $Username);
			if($continue) {
				$QuestionID = $this->ChooseQuestion($ID, $Username, $ContentType);
				if(!$QuestionID) return false;
				$Result = json_decode($this->GetContent($QuestionID, $Username));
				$Result->QuestionOptions = json_decode($this->GetChildContent($QuestionID, $Username, 'Question'));
				return json_encode($Result);
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}		
	}
	function GetQuestion($ID, $Username, $ContentType, $Override) {
		try{
			$continue = $this->ReadContentPermitted($ID, $Username);
			if($Override != "" && !$continue) $continue = $this->Override($Override, $ID, $Username);
			if($continue) {
				$QuestionID = $this->ChooseQuestion($ID, $Username, $ContentType);
				if(!$QuestionID) return false;
				$Result = json_decode($this->GetContent($QuestionID, $Username));
				$Result->QuestionOptions = json_decode($this->GetChildContent($QuestionID, $Username, 'Question'));
				return json_encode($Result);
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}		
	}
	function GetTestQuestions($ID, $Username, $Override) {
		try{
			$continue = $this->ReadContentPermitted($ID, $Username);
			if($Override != "" && !$continue) $continue = $this->Override($Override, $ID, $Username);
			if($continue) {
				$Result = json_decode($this->GetChildContent($ID, $Username, "Note"));
				for($i = 0; $i < count($Result); $i++) {
					$Result[$i]->QuestionOptions = json_decode($this->GetChildContent($Result[$i]->ID, $Username, "Question"));
				}
				return json_encode($Result);
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}		
	}
	function GetDiagnostic($ID, $Username) {
	}
	/*function ScoreQuestion($stmt, $ID, $Username) {
		$stmt->bindParam(":ID", $ID);
		$stmt->bindParam(":Username", $Username);
		if(!$stmt->execute()) return false;
		$Freq = $stmt->rowCount();
		if($Freq > 0) {
			$Score = 0;
			$count = 0;
			while($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
				$Score += 0.5/$row['TimeTaken'] + $row['Result'] * 0.5;
				$count++;
			}
			$Score /= $count;
			return $Score;
		}
		else return 1;
	}*/
	function ChooseQuestion($ID, $Username, $ContentType) {
		try{
			if($ContentType == "Note") {
				$AvailableQuestions = json_decode($this->GetChildContent($ID, $Username, $ContentType));
				$GetQuestionHistory = $this->conn->prepare("SELECT * FROM " . $this->QuestionHistory . " WHERE Username=:Username AND DateAttempted >= NOW() - INTERVAL 30 DAY AND QuestionID=:ID");
				$CurID = 0;
				$GetQuestionHistory->bindParam(":Username", $Username);
				$GetQuestionHistory->bindParam(":ID", $CurID);
				$CheckPermutations = $this->conn->prepare("SELECT * FROM " . $this->QuestionOptions . " WHERE QuestionID=:CurID");
				$CheckPermutations->bindParam(":CurID", $CurID);
				if(count($AvailableQuestions) > 0) {
					$Highest = 0;
					for($i = 0; $i < count($AvailableQuestions); $i++) {
						$CurID = $AvailableQuestions[$i]->ID;
						if(!$CheckPermutations->execute()) return false;
						$Permutations = $CheckPermutations->rowCount();
						if(!$GetQuestionHistory->execute()) return false;
						$count = $GetQuestionHistory->rowCount();
						if($count > 0) {
							$Score = 0;
							while($row = $GetQuestionHistory->fetch(PDO::FETCH_ASSOC)) $Score += 0.5/$row['TimeTaken'] + (1-$row['Result']) * 0.5;
							$Score /= ($count*$count);
							$Score *= $Permutations;
							$AvailableQuestions[$i]->QuestionScore = $Score;
							if($Score > $Highest) $Highest = $Score;
						}
						else {
							$Highest = 1;
							$AvailableQuestions[$i]->QuestionScore = 1;
						}
					}
					for($i = 0; $i < count($AvailableQuestions); $i++) $AvailableQuestions[$i]->QuestionScore /= $Highest;
					$ChosenID = 0;
					while($ChosenID == 0) {
						$RandomQuestion = mt_rand(0,count($AvailableQuestions)-1);
						if($AvailableQuestions[$RandomQuestion]->QuestionScore > (1/(rand() + 1))) $ChosenID = $AvailableQuestions[$RandomQuestion]->ID;
					}
					return $ChosenID;
				}
				else return false;
			}
			else if($ContentType == "Package") {
				$GetNotes = json_decode($this->GetChildContent($ID, $Username, $ContentType));
				$AvailableQuestions = array();
				for($i = 0; $i < count($GetNotes); $i++) $AvailableQuestions = array_merge($AvailableQuestions, json_decode($this->GetChildContent($GetNotes[$i]->ID, $Username, "Note")));
				$GetQuestionHistory = $this->conn->prepare("SELECT * FROM " . $this->QuestionHistory . " WHERE Username=:Username AND DateAttempted >= NOW() - INTERVAL 30 DAY AND QuestionID=:ID");
				$CurID = 0;
				$GetQuestionHistory->bindParam(":Username", $Username);
				$GetQuestionHistory->bindParam(":ID", $CurID);
				$CheckPermutations = $this->conn->prepare("SELECT * FROM " . $this->QuestionOptions . " WHERE QuestionID=:CurID");
				$CheckPermutations->bindParam(":CurID", $CurID);
				if(count($AvailableQuestions) > 0) {
					$Highest = 0;
					for($i = 0; $i < count($AvailableQuestions); $i++) {
						$CurID = $AvailableQuestions[$i]->ID;
						if(!$CheckPermutations->execute()) return false;
						$Permutations = $CheckPermutations->rowCount();
						if(!$GetQuestionHistory->execute()) return false;
						$count = $GetQuestionHistory->rowCount();
						if($count > 0) {
							$Score = 0;
							while($row = $GetQuestionHistory->fetch(PDO::FETCH_ASSOC)) $Score += 0.5/$row['TimeTaken'] + (1-$row['Result']) * 0.5;
							$Score /= ($count*$count);
							$Score *= $Permutations;
							$AvailableQuestions[$i]->QuestionScore = $Score;
							if($Score > $Highest) $Highest = $Score;
						}
						else {
							$Highest = 1;
							$AvailableQuestions[$i]->QuestionScore = 1;
						}
					}
					for($i = 0; $i < count($AvailableQuestions); $i++) $AvailableQuestions[$i]->QuestionScore /= $Highest;
					$ChosenID = 0;
					while($ChosenID == 0) {
						$RandomQuestion = mt_rand(0,count($AvailableQuestions)-1);
						if($AvailableQuestions[$RandomQuestion]->QuestionScore > (1/(rand() + 1))) $ChosenID = $AvailableQuestions[$RandomQuestion]->ID;
					}
					return $ChosenID;
				}
				else return false;
			}
			else return false;
		}
		catch (PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function RecordTest($QuestionData, $TestData) {
		try{
			$stmt = $this->conn->prepare("INSERT INTO " . $this->TestHistory . " (NoteID, DateAttempted, TimeTaken, Result, Username) VALUES (:NoteID, CURDATE(), :TimeTaken, :Result, :Username)");
			$stmt->bindParam(":NoteID", $TestData->NoteID);
			$stmt->bindParam(":TimeTaken", $TestData->TimeTaken);
			$stmt->bindParam(":Result", $TestData->Result);
			$stmt->bindParam(":Username", $TestData->Username);
			if(!$stmt->execute()) return false;
			$TestID = $this->conn->lastInsertId();
			for($i = 0; $i < count($QuestionData); $i++) {
				$QuestionData[$i]->TestID = $TestID;
				$this->RecordResults($QuestionData[$i]);
			}
		}
		catch (PDOException $e) {
			echo $e->GetMessage();
			return false;
		}		
	}
	function RecordResults($data) {
		try{
			$stmt = $this->conn->prepare("INSERT INTO " . $this->QuestionHistory . " (QuestionID, Settings, DateAttempted, TimeTaken, Result, Username, TestID) VALUES (:QuestionID, :Settings, CURDATE(), :TimeTaken, :Result, :Username, :TestID)");
			$stmt->bindParam(":Username", $data->Username);
			$stmt->bindParam(":QuestionID", $data->ID);
			$stmt->bindParam(":TestID", $data->TestID);
			$stmt->bindParam(":TimeTaken", $data->TimeTaken);
			$stmt->bindParam(":Result", $data->Result);
			$stmt->bindParam(":Settings", $data->Settings);
			$stmt->execute(); 
		}
		catch (PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	
}
?>
