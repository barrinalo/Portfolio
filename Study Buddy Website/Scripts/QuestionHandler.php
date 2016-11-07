<?php
require_once("ContentWrapperHandler.php");
class QuestionHandler extends ContentWrapperHandler {
	
	function __construct() {
		parent::__construct();
	}
	function GetTableQuestion($ID, $Username) {
		$Content = $this->GetContent($ID, $Username);
		try{
			$TableQuestionStuff = $this->conn->prepare("SELECT Topic, Aspect, TableEntry FROM " . $this->QuestionOptions . " WHERE QuestionID=:ID");
			$TableQuestionStuff->bindParam(":ID", $ID);
			if(!$TableQuestionStuff->execute()) return false;
			$TableArray = array();
			$TopicArray = array();
			$AspectArray = array();
			$TableEntries = array();
			while($row = $TableQuestionStuff->fetch(PDO::FETCH_ASSOC)) {
				if(!in_array($row['Topic'],$TopicArray)) {
					array_push($TopicArray, $row['Topic']);
					$TableEntries[$row['Topic']] = array();
				}
				if(!in_array($row['Aspect'],$AspectArray)) array_push($AspectArray, $row['Aspect']);
				$TableEntries[$row['Topic']][$row['Aspect']] = $row['TableEntry'];				
			}
		}
		catch(PDOException $e) {
		}
	}
	function EnterTableQuestion($ID, $Title, $Description, $Permissions, $Username, $Filters, $TableEntries) {
		try{
			$QuestionType = "Table";
			$ContentType = "Question";
			if($ID > 0) $QuestionID = $ID;
			else $QuestionID = 0;
			if($this->conn->beginTransaction()) {
				$ContentWrapperResult = $this->EnterContentWrapper($ID, $Title, $Description, $Permissions, $Username, $Filters, $ContentType, $QuestionType, "", array());
				if($ContentWrapperResult == 0) {
					$this->conn->rollBack();
					return false;
				}
				else $QuestionID = $ContentWrapperResult;
				if($ID > 0) {
					
				}
				else {
					$TableEntryOptions = $this->conn->prepare("INSERT INTO " . $this->QuestionOptions . " (QuestionID, Topic, Aspect, TableEntry) VALUES (:QuestionID, :Topic, :Aspect, :TableEntry)";
					$Topic = "";
					$Aspect = "";
					$TableEntry = "";
					$TableEntryOptions->bindParam(":QuestionID", $QuestionID);
					$TableEntryOptions->bindParam(":Aspect", $Aspect);
					$TableEntryOptions->bindParam(":Topic", $Topic);
					$TableEntryOptions->bindParam(":TableEntry", $TableEntry);
					for($i = 0; $i < count($TableEntries); $i++) {
						$Topic = $TableEntries[$i]->Topic;
						$Aspect = $TableEntries[$i]->Aspect;
						$TableEntry = $TableEntries[$i]->TableEntry;
						if(!$TableEntryOptions->execute()) {
							$this->conn->rollBack();
							return false;
						}
					}
				}
				
				$this->conn->commit();
				return $QuestionID;
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->getMessage();
			return false;
		}
	}
	function GetManualQuestion($ID, $Username) {
		$Content = $this->GetContent($ID, $Username);
		try{
			$ManualQuestionStuff = $this->conn->prepare("SELECT Stem, Explanation FROM " . $this->QuestionOptions . " WHERE QuestionID=:ID");
			$ManualQuestionStuff->bindParam(":ID", $ID);
			if(!$ManualQuestionStuff->execute()) return false;
			$Info = $ManualQuestionStuff->fetch(PDO::FETCH_ASSOC, PDO::FETCH_ORI_NEXT);
			$Content['Stem'] = $Info['Stem'];
			$Content['Explanation'] = $Info['Explanation'];
			return $Content;
		}
		catch(PDOException $e) {
			return false;
		}
	}
		else return false;
	}
	function EnterManualQuestion($ID, $Title, $Description, $Permissions, $Username, $Stem, $Explanation, $Filters) {
		try{
			$QuestionType = "Manual";
			$ContentType = "Question";
			if($ID > 0) $QuestionID = $ID;
			else $QuestionID = 0;
			if($this->conn->beginTransaction()) {
				$ContentWrapperResult = $this->EnterContentWrapper($ID, $Title, $Description, $Permissions, $Username, $Filters, $ContentType, $QuestionType, "", array());
				if($ContentWrapperResult == 0) {
					$this->conn->rollBack();
					return false;
				}
				else $QuestionID = $ContentWrapperResult;
				if($ID > 0) {
					$ManualQuestionOptions = $this->conn->prepare("UPDATE " . $this->QuestionOptions . " SET Stem=:Stem, Explanation=:Explanation WHERE QuestionID=:QuestionID");
				}
				else {
					$ManualQuestionOptions = $this->conn->prepare("INSERT INTO " . $this->QuestionOptions . " (QuestionID, Stem, Explanation) VALUES (:QuestionID, :Stem, :Explanation)");
				}
				$ManualQuestionOptions->bindParam(":Stem", $Stem);
				$ManualQuestionOptions->bindParam(":Explanation", $Explanation);
				$ManualQuestionOptions->bindParam(":QuestionID", $QuestionID);
				$ManualQuestionOptionsResult = $ManualQuestionOptions->execute();
				if(!$ManualQuestionOptionsResult) {
					$this->conn->rollBack();
					return false;
				}
				$this->conn->commit();
				return $QuestionID;
			}
			else return false;
		}
		catch (PDOException $e) {
			echo $e->getMessage();
			return false;
		}
	}
}
?>
