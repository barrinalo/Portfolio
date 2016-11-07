<?php
class ContentWrapperHandler extends DBConnection {
	protected $ContentWrapper = "contentwrapper";
	protected $WrapperPermissions = "wrapperpermissions";
	protected $WrapperRelations = "wrapperrelations";
	protected $QuestionOptions = "questionoptions";
	protected $Subscriptions = "subscriptions";
	protected $UserDetails = "userdetails";
	function __construct() {
		parent::__construct();
	}
	function DeleteContent($ContentID, $Username, $ContentType) {
		try{
			if($this->WriteContentPermitted($ContentID, $Username)) {
				if($this->conn->beginTransaction()) {
					$DeleteWrapper = $this->conn->prepare("DELETE FROM " . $this->ContentWrapper . " WHERE ID=:ContentID");
					$DeleteWrapper->bindParam(":ContentID", $ContentID);
					if(!$DeleteWrapper->execute()) {
						$this->conn->rollBack();
						return false;
					}
					$DeletePermissions = $this->conn->prepare("DELETE FROM " . $this->WrapperPermissions . " WHERE ContentID=:ContentID");
					$DeletePermissions->bindParam(":ContentID", $ContentID);
					if(!$DeletePermissions->execute()) {
						$this->conn->rollBack();
						return false;
					}
					$DeleteRelations = $this->conn->prepare("DELETE FROM " . $this->WrapperRelations . " WHERE ParentID=:ContentID OR ChildID=:ContentID");
					$DeleteRelations->bindParam(":ContentID", $ContentID);
					if(!$DeleteRelations->execute()) {
						$this->conn->rollBack();
						return false;
					}
					$DeleteSubscriptions = $this->conn->prepare("DELETE FROM " . $this->Subscriptions . " WHERE ContentID=:ContentID");
					$DeleteSubscriptions->bindParam(":ContentID", $ContentID);
					if(!$DeleteSubscriptions->execute()) {
						$this->conn->rollBack();
						return false;
					}
					if($ContentType == "Question") {
						$DeleteQuestionOptions = $this->conn->prepare("DELETE FROM " . $this->QuestionOptions . " WHERE QuestionID=:ContentID");
						$DeleteQuestionOptions->bindParam(":ContentID", $ContentID);
						if(!$DeleteQuestionOptions->execute()) {
							$this->conn->rollBack();
							return false;
						}
					}
					$this->conn->commit();
					return $ContentType;
				}
				else return false;
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->getMessage();
			return false;
		}
	}
	function WriteContentPermitted($ID, $Username) {
		try{
			$ContentPermitted = $this->conn->prepare("SELECT COUNT(*) FROM " . $this->ContentWrapper . " WHERE ID=:ID AND Creator=:Username");
			$ContentPermitted->bindParam(":ID", $ID);
			$ContentPermitted->bindParam(":Username", $Username);
			if(!$ContentPermitted->execute()) return false;
			$ContentPermittedResult = $ContentPermitted->fetchColumn();
			if($ContentPermittedResult[0] >= 1) return true;
			else return false;
		}
		catch(PDOException $e) {
			return false;
		}
	}
	function Override($OverrideID, $ContentID, $Username) {
		try{
			if($this->ReadContentPermitted($OverrideID, $Username)) {
				$CurContent = json_decode($this->GetContent($OverrideID, $Username));
				$Children = json_decode($this->GetChildContent($OverrideID, $Username, $CurContent->ContentType));
				if($CurContent->ContentType == "Package") {
					for($i = 0; $i < count($Children); $i++) {
						$Children = array_merge($Children, json_decode($this->GetChildContent($Children[$i]->ID, $Username, 'Note')));
					}
				}
				for($i = 0; $i < count($Children); $i++) if($Children[$i]->ID == $ContentID) return true;
				return false;
			}
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function ReadContentPermitted($ID, $Username) {
		try{
			$ContentPermitted = $this->conn->prepare("SELECT Creator, Permissions FROM " . $this->ContentWrapper . " WHERE ID=:ID");
			$ContentPermitted->bindParam(":ID", $ID);
			if(!$ContentPermitted->execute()) return false;
			$ContentPermittedResult = $ContentPermitted->fetch(PDO::FETCH_ASSOC, PDO::FETCH_ORI_NEXT);
			if($ContentPermittedResult) {
				if($ContentPermittedResult['Permissions'] == "Public") return true;
				else if($ContentPermittedResult['Creator'] == $Username) return true;
				else if($ContentPermittedResult['Permissions'] == "Selective") {
					$SelectivePermissions = $this->conn->prepare("SELECT COUNT(*) FROM " . $this->WrapperPermissions . " WHERE ContentID=:ID AND Username=:Username");
					$SelectivePermissions->bindParam(":ID", $ID);
					$SelectivePermissions->bindParam(":Username", $Username);
					if(!$SelectivePermissions->execute()) return false;
					$SelectivePermissionsResult = $SelectivePermissions->fetchColumn();
					if($SelectivePermissionsResult[0] >= 1) return true;
					else return false;
				}
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function GetContent($ID, $Username) {
		try	{
			$GetContentWrapper = $this->conn->prepare("SELECT * FROM " .$this->ContentWrapper . " WHERE ID=:ID");
			$GetContentWrapper->bindParam(":ID", $ID);
			if(!$GetContentWrapper->execute()) return false;
			$Result = $GetContentWrapper->fetch(PDO::FETCH_ASSOC, PDO::FETCH_ORI_NEXT);
			return json_encode($Result);
		}
		catch(PDOException $e) {
			return false;
		}
	}
	function WriteContent($ID, $Title, $Description, $Permissions, $Username, $Filters, $ContentType, $QuestionType, $Content, $Relations) {
		try{
			if($this->conn->beginTransaction()) {
				if($ID > 0 ) {
					$ContentWrapperEntry = $this->conn->prepare("UPDATE " . $this->ContentWrapper . " SET Title=:Title, Description=:Description, ContentType=:ContentType, QuestionType=:QuestionType, Permissions=:Permissions, Creator=:Creator, Filters=:Filters, Content=:Content WHERE ID=:ID");
					$ContentWrapperEntry->bindParam(":ID", $ID);
				}
				else {
					$ContentWrapperEntry = $this->conn->prepare("INSERT INTO " . $this->ContentWrapper . " (Title, Description, ContentType, QuestionType, Permissions, Creator, Filters, Content) VALUES (:Title, :Description, :ContentType, :QuestionType, :Permissions, :Creator, :Filters, :Content)");
				}
				$ContentWrapperEntry->bindParam(":Title", $Title);
				$ContentWrapperEntry->bindParam(":Description", $Description);
				$ContentWrapperEntry->bindParam(":Permissions", $Permissions);
				$ContentWrapperEntry->bindParam(":Creator", $Username);
				$ContentWrapperEntry->bindParam(":Filters", $Filters);
				$ContentWrapperEntry->bindParam(":QuestionType", $QuestionType);
				$ContentWrapperEntry->bindParam(":ContentType", $ContentType);
				$ContentWrapperEntry->bindParam(":Content", $Content);
				$Result = $ContentWrapperEntry->execute();
				if(!$Result) {
					$this->conn->rollBack();
					return $Result;
				}
				if($ID > 0) $ContentID = $ID;
				else $ContentID = $this->conn->lastInsertId();
				if($ContentType == "Note" || $ContentType == "Package") {
					if(count($Relations) > 0) {
						$ChildIDs = array();
						foreach($Relations as $key => $val) {
							if(isset($Relations[$key]->ChildID)) {
								if(!$this->ReadContentPermitted($Relations[$key]->ChildID, $Username)) unset($Relations[$key]);
							}
						}
						for($i = 0; $i < count($Relations); $i++) if(isset($Relations[$i]->ChildID)) array_push($ChildIDs, $Relations[$i]->ChildID);
						$RelationID = 0;
						$ChildID = 0;
						$ChildOrder = 0;
						$sql = "DELETE FROM " . $this->WrapperRelations . " WHERE ParentID=:ParentID";
						if(count($ChildIDs) > 0) {
							$sql .= " AND ";
							for($i = 0; $i < count($ChildIDs); $i++) $sql .= "ChildID!=:ChildID$i AND ";
							$sql = substr($sql,0,strlen($sql)-4);
						}
						$DeleteRelations = $this->conn->prepare($sql);
						$DeleteRelations->bindParam(":ParentID", $ContentID);
						for($i = 0; $i < count($ChildIDs); $i++) $DeleteRelations->bindParam(":ChildID$i", $ChildIDs[$i], PDO::PARAM_INT);
						if(!$DeleteRelations->execute()) {
							$this->conn->rollBack();
							return false;
						}
						$sql = "INSERT INTO " . $this->WrapperRelations . " (ID, ParentID, ChildID, ChildOrder) VALUES (:RelationID, :ParentID, :ChildID, :ChildOrder) ON DUPLICATE KEY UPDATE ParentID=:ParentID, ChildID=:ChildID, ChildOrder=:ChildOrder";
						$UpdateRelations = $this->conn->prepare($sql);
						$UpdateRelations->bindParam(":RelationID", $RelationID);
						$UpdateRelations->bindParam(":ParentID", $ContentID);
						$UpdateRelations->bindParam(":ChildID", $ChildID);
						$UpdateRelations->bindParam(":ChildOrder", $ChildOrder);
						$sql = "SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" . $this->dbname . "' AND TABLE_NAME = '" . $this->WrapperRelations . "'";
						$GetNextID = $this->conn->prepare($sql);
						for($i = 0; $i < count($Relations); $i++) {
							if(!isset($Relations[$i]->ID)) {
								if(!$GetNextID->execute()) {
									$this->conn->rollBack();
									return false;
								}
								$NextID = $GetNextID->fetch(PDO::FETCH_ASSOC);
								$RelationID = $NextID['AUTO_INCREMENT'];
							}
							else $RelationID = $Relations[$i]->ID;
							$ChildID = $Relations[$i]->ChildID;
							$ChildOrder = $Relations[$i]->ChildOrder;
							if(!$UpdateRelations->execute()) {
								$this->conn->rollBack();
								return false;
							}
						}
						$this->conn->commit();
						return $ContentID;
					}
					else {
						$EraseRelations = $this->conn->prepare("DELETE FROM " . $this->WrapperRelations . " WHERE ParentID=:ParentID");
						$EraseRelations->bindParam(":ParentID", $ContentID);
						if(!$EraseRelations->execute()) {
							$this->conn->rollBack();
							return false;
						}
						$this->conn->commit();
						return $ContentID;
					}
				}
				else if($ContentType == "Question") {
					if(count($Relations) > 0) {
						$Topic = "";
						$Aspect = "";
						$TableEntry = "";
						$Node1 = "";
						$Node2 = "";
						$Relationship = "";
						$Stem = "";
						$Explanation = "";
						$OptionID = 0;
						$ChildIDs = array();
						for($i = 0; $i < count($Relations); $i++) if(isset($Relations[$i]->ID)) array_push($ChildIDs, $Relations[$i]->ID);
						$sql = "DELETE FROM " . $this->QuestionOptions . " WHERE QuestionID=:QuestionID";
						if(count($ChildIDs) > 0) {
							$sql .= " AND ";
							for($i = 0; $i < count($ChildIDs); $i++) $sql .= "ID!=:ID$i AND ";
							$sql = substr($sql,0,strlen($sql)-4);
						}
						$DeleteQuestionOptions = $this->conn->prepare($sql);
						$DeleteQuestionOptions->bindParam(":QuestionID", $ContentID);
						for($i = 0; $i < count($ChildIDs); $i++) $DeleteQuestionOptions->bindParam(":ID$i", $ChildIDs[$i], PDO::PARAM_INT);
						if(!$DeleteQuestionOptions->execute()) {
							$this->conn->rollBack();
							return false;
						}
						$sql = "INSERT INTO " . $this->QuestionOptions . "(ID, QuestionID, Topic, Aspect, TableEntry, Node1, Node2, Relationship, Stem, Explanation) VALUES (:OptionID, :QuestionID, :Topic, :Aspect, :TableEntry, :Node1, :Node2, :Relationship, :Stem, :Explanation) ON DUPLICATE KEY UPDATE QuestionID=:QuestionID, Topic=:Topic, Aspect=:Aspect, TableEntry=:TableEntry, Node1=:Node1, Node2=:Node2, Relationship=:Relationship, Stem=:Stem, Explanation=:Explanation";
						$UpdateOptions = $this->conn->prepare($sql);
						$UpdateOptions->bindParam(":OptionID", $OptionID);
						$UpdateOptions->bindParam(":QuestionID", $ContentID);
						$UpdateOptions->bindParam(":Topic", $Topic);
						$UpdateOptions->bindParam(":Aspect", $Aspect);
						$UpdateOptions->bindParam(":TableEntry", $TableEntry);
						$UpdateOptions->bindParam(":Node1", $Node1);
						$UpdateOptions->bindParam(":Node2", $Node2);
						$UpdateOptions->bindParam(":Relationship", $Relationship);
						$UpdateOptions->bindParam(":Stem", $Stem);
						$UpdateOptions->bindParam(":Explanation", $Explanation);
						$sql = "SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" . $this->dbname . "' AND TABLE_NAME = '" . $this->QuestionOptions . "'";
						$GetNextID = $this->conn->prepare($sql);
						for($i = 0; $i < count($Relations); $i++) {
							if(!isset($Relations[$i]->ID)) {
								if(!$GetNextID->execute()) {
									$this->conn->rollBack();
									return false;
								}
								$NextID = $GetNextID->fetch(PDO::FETCH_ASSOC);
								$OptionID = $NextID['AUTO_INCREMENT'];
							}
							else $OptionID = $Relations[$i]->ID;
							$Topic = $Relations[$i]->Topic;
							$Aspect = $Relations[$i]->Aspect;
							$TableEntry = $Relations[$i]->TableEntry;
							$Node1 = $Relations[$i]->Node1;
							$Node2 = $Relations[$i]->Node2;
							$Relationship = $Relations[$i]->Relationship;
							$Stem = $Relations[$i]->Stem;
							$Explanation = $Relations[$i]->Explanation;
							if(!$UpdateOptions->execute()) {
								$this->conn->rollBack();
								return false;
							}
						}
						$this->conn->commit();
						return $ContentID;
					}
					else {
						$EraseQuestionOptions = $this->conn->prepare("DELETE FROM " . $this->QuestionOptions . " WHERE QuestionID=:ID");
						$EraseQuestionOptions->bindParam(":ID", $ContentID);
						if(!$EraseQuestionOptions->execute()) {
							$this->conn->rollBack();
							return false;
						}
						$this->conn->commit();
						return $ContentID;
					}
				}
			}
			else return false;
		}
		catch (PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function GetPersonalContent($ContentType, $Username, $Limit, $Offset, $TitleFilter, $FiltersFilter, $DescriptionFilter) {
		try {
			$Result = array();
			$sql = "SELECT * FROM " . $this->ContentWrapper . " WHERE Creator=:Creator AND ContentType=:ContentType";
			if($TitleFilter != "") $sql .= " AND LOWER(Title) LIKE LOWER(:TitleFilter)";
			if($FiltersFilter != "") $sql .= " AND LOWER(Filters) LIKE LOWER(:FiltersFilter)";
			if($DescriptionFilter != "") $sql .= " AND LOWER(Description) LIKE LOWER(:DescriptionFilter)";
			$sql .= " ORDER BY Title ASC";
			if($Limit > 0) $GetPersonalContent = $this->conn->prepare($sql . " LIMIT $Limit OFFSET $Offset");
			else $GetPersonalContent = $this->conn->prepare($sql);
			if($TitleFilter != "") {
				$TitleFilter = "%$TitleFilter%";
				$GetPersonalContent->bindParam(":TitleFilter", $TitleFilter);
			}
			if($FiltersFilter != "") {
				$FiltersFilter = "%$FiltersFilter%";
				$GetPersonalContent->bindParam(":FiltersFilter", $FiltersFilter);
			}
			if($DescriptionFilter != "") {
				$DescriptionFilter = "%$DescriptionFilter%";
				$GetPersonalContent->bindParam(":DescriptionFilter", $DescriptionFilter);
			}
			$GetPersonalContent->bindParam(":Creator", $Username);
			$GetPersonalContent->bindParam(":ContentType", $ContentType);
			if(!$GetPersonalContent->execute()) return false;
			$Result['data'] = $GetPersonalContent->fetchAll(PDO::FETCH_ASSOC);
			if($Limit > 0) {
				$GetPersonalContentRowCount = $this->conn->prepare($sql);
				$GetPersonalContentRowCount->bindParam(":Creator", $Username);
				$GetPersonalContentRowCount->bindParam(":ContentType", $ContentType);
				if($TitleFilter != "") {
					$TitleFilter = "%$TitleFilter%";
					$GetPersonalContentRowCount->bindParam(":TitleFilter", $TitleFilter);
				}
				if($FiltersFilter != "") {
					$FiltersFilter = "%$FiltersFilter%";
					$GetPersonalContentRowCount->bindParam(":FiltersFilter", $FiltersFilter);
				}
				if($DescriptionFilter != "") {
					$DescriptionFilter = "%$DescriptionFilter%";
					$GetPersonalContentRowCount->bindParam(":DescriptionFilter", $DescriptionFilter);
				}
				if(!$GetPersonalContentRowCount->execute()) return false;
				$Result['RowCount'] = $GetPersonalContentRowCount->rowCount();
			}
			else $Result['RowCount'] = $GetPersonalContent->rowCount();
			return json_encode($Result);
		}
		catch(PDOException $e) {
			echo $sql;
			return false;
		}
	}
	function GetReadableContent($ContentType, $Username, $Limit, $Offset, $NonPersonal, $Creator, $TitleFilter, $FiltersFilter, $DescriptionFilter) {
		try {
			$Result = array();
			if(!$NonPersonal) $sql = "SELECT ID, Title, Description, Content, ContentType, QuestionType, Permissions, Creator, Filters FROM " . $this->ContentWrapper . " WHERE (Creator=:Creator OR Permissions='Public') AND ContentType=:ContentType";
			else $sql = "SELECT ID, Title, Description, Content, ContentType, QuestionType, Permissions, Creator, Filters FROM " . $this->ContentWrapper . " WHERE Permissions='Public' AND ContentType=:ContentType AND Creator!=:Creator";
			if($Creator != "") $sql .= " AND LOWER(Creator) LIKE LOWER(:ContentCreator)";
			if($TitleFilter != "") $sql .= " AND LOWER(Title) LIKE LOWER(:TitleFilter)";
			if($FiltersFilter != "") $sql .= " AND LOWER(Filters) LIKE LOWER(:FiltersFilter)";
			if($DescriptionFilter != "") $sql .= " AND LOWER(Description) LIKE LOWER(:DescriptionFilter)";

			$sql2 = "SELECT " . $this->ContentWrapper .".ID," . $this->ContentWrapper .".Title," . $this->ContentWrapper .".Description," . $this->ContentWrapper .".Content," . $this->ContentWrapper .".ContentType," . $this->ContentWrapper .".QuestionType," . $this->ContentWrapper .".Permissions," . $this->ContentWrapper .".Creator," . $this->ContentWrapper .".Filters " . "FROM " . $this->ContentWrapper . " INNER JOIN " . $this->WrapperPermissions . " ON " . $this->ContentWrapper . ".ID=" . $this->WrapperPermissions . ".ContentID WHERE " . $this->ContentWrapper . ".ContentType=:ContentType AND " . $this->WrapperPermissions . ".Username=:Creator";
			if($Creator != "") $sql2 .= " AND LOWER(Creator) LIKE LOWER(:ContentCreator)";
			if($TitleFilter != "") $sql2 .= " AND LOWER(Title) LIKE LOWER(:TitleFilter)";
			if($FiltersFilter != "") $sql2 .= " AND LOWER(Filters) LIKE LOWER(:FiltersFilter)";
			if($DescriptionFilter != "") $sql2 .= " AND LOWER(Description) LIKE LOWER(:DescriptionFilter)";

			$sql = $sql . " UNION ALL " . $sql2;
			$sql .= " ORDER BY Title ASC";
			if($Limit > 0) $GetReadableContent = $this->conn->prepare($sql . " LIMIT 20 OFFSET $Offset");
			else $GetReadableContent = $this->conn->prepare($sql);
			$GetReadableContent->bindParam(":Creator", $Username);
			$GetReadableContent->bindParam(":ContentType", $ContentType);
			if($Creator != "") {
				$Creator = "%" . $Creator . "%";
				$GetReadableContent->bindParam(":ContentCreator", $Creator);
			}
			if($TitleFilter != "") {
				$TitleFilter = "%$TitleFilter%";
				$GetReadableContent->bindParam(":TitleFilter", $TitleFilter);
			}
			if($FiltersFilter != "") {
				$FiltersFilter = "%$FiltersFilter%";
				$GetReadableContent->bindParam(":FiltersFilter", $FiltersFilter);
			}
			if($DescriptionFilter != "") {
				$DescriptionFilter = "%$DescriptionFilter%";
				$GetReadableContent->bindParam(":DescriptionFilter", $DescriptionFilter);
			}
			if(!$GetReadableContent->execute()) return false;
			$Result['data'] = $GetReadableContent->fetchAll(PDO::FETCH_ASSOC);
			if($Limit > 0) {
				$GetReadableContentRowCount = $this->conn->prepare($sql);
				$GetReadableContentRowCount->bindParam(":Creator", $Username);
				$GetReadableContentRowCount->bindParam(":ContentType", $ContentType);
				if($Creator != "") {
					$Creator = "%" . $Creator . "%";
					$GetReadableContentRowCount->bindParam(":ContentCreator", $Creator);
				}
				if($TitleFilter != "") {
					$TitleFilter = "%$TitleFilter%";
					$GetReadableContentRowCount->bindParam(":TitleFilter", $TitleFilter);
				}
				if($FiltersFilter != "") {
					$FiltersFilter = "%$FiltersFilter%";
					$GetReadableContentRowCount->bindParam(":FiltersFilter", $FiltersFilter);
				}
				if($DescriptionFilter != "") {
					$DescriptionFilter = "%$DescriptionFilter%";
					$GetReadableContentRowCount->bindParam(":DescriptionFilter", $DescriptionFilter);
				}
				if(!$GetReadableContentRowCount->execute()) return false;
				$Result["RowCount"] = $GetReadableContentRowCount->rowCount();
			}
			else $Result["RowCount"] = $GetReadableContent->rowCount();
			return json_encode($Result);
		}
		catch(PDOException $e) {
			echo $e->getMessage();
			return false;
		}
	}
	function GetChildContent($ID, $Username, $ContentType) {
		try{
			if($ContentType == "Note" || $ContentType == "Package") {
				$GetChildIDs = $this->conn->prepare("SELECT * FROM " . $this->WrapperRelations . " WHERE ParentID=:ID ORDER BY ChildOrder ASC");
				$GetChildIDs->bindParam(":ID", $ID);
				if(!$GetChildIDs->execute()) return false;
				$GetChildContent = $this->conn->prepare("SELECT * FROM " . $this->ContentWrapper . " WHERE ID=:ChildID");
				$ChildID = 0;
				$GetChildContent->bindParam(":ChildID", $ChildID);
				$Result = array();
				$Counter = 0;
				while($row = $GetChildIDs->fetch(PDO::FETCH_ASSOC)) {
					$ChildID = $row['ChildID'];
					$RelationID = $row['ID']; 
					if(!$GetChildContent->execute()) return false;
					$Entry = $GetChildContent->fetch(PDO::FETCH_ASSOC);
					$Entry['RelationID'] = $RelationID;
					array_push($Result, $Entry);
				}
				return json_encode($Result);
			}
			else if($ContentType == "Question") {
				$GetQuestionOptions = $this->conn->prepare("SELECT * FROM " . $this->QuestionOptions . " WHERE QuestionID=:ID");
				$GetQuestionOptions->bindParam(":ID", $ID);
				if(!$GetQuestionOptions->execute()) return false;
				return json_encode($GetQuestionOptions->fetchAll());
			}
		}
		catch (PDOException $e) {
			return false;
		}
	}
	function UnSubscribe($ID, $Username) {
		try{
			$Unsubscribe = $this->conn->prepare("DELETE FROM " . $this->Subscriptions . " WHERE Username=:Username AND ContentID=:ID");
			$Unsubscribe->bindParam(":Username", $Username);
			$Unsubscribe->bindParam(":ID", $ID);
			if(!$Unsubscribe->execute()) return false;
			else return true;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function Subscribe($ID, $Username) {
		try{
			$SubscriptionExists = $this->conn->prepare("SELECT * FROM " . $this->Subscriptions . " WHERE Username=:Username AND ContentID=:ID");
			$SubscriptionExists->bindParam(":Username", $Username);
			$SubscriptionExists->bindParam(":ID", $ID);
			if(!$SubscriptionExists->execute()) return false;
			if($SubscriptionExists->rowCount() > 0) return false;
			$Subscribe = $this->conn->prepare("INSERT INTO " . $this->Subscriptions . " (Username, ContentID) VALUES(:Username, :ID)");
			$Subscribe->bindParam(":Username", $Username);	
			$Subscribe->bindParam(":ID", $ID);
			if(!$Subscribe->execute()) return false;
			else return true;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function GetSubscriptions($ContentType, $Username, $Limit, $Offset, $TitleFilter, $FiltersFilter, $DescriptionFilter, $Creator) {
		try {
			$Result = array();
			$GetSubscriptionIDs = $this->conn->prepare("SELECT * FROM  " . $this->Subscriptions . " WHERE Username=:Creator");
			$GetSubscriptionIDs->bindParam(":Creator", $Username);
			if(!$GetSubscriptionIDs->execute()) return false;
			$SubscriptionIDs = array();
			while($row = $GetSubscriptionIDs->fetch(PDO::FETCH_ASSOC)) array_push($SubscriptionIDs, $row['ContentID']);
			$sql = "SELECT * FROM " . $this->ContentWrapper . " WHERE ContentType=:ContentType";
			if(count($SubscriptionIDs) > 0) {
				$sql .= " AND (";
				for($i = 0; $i < count($SubscriptionIDs); $i++) $sql .= "ID=:SubscriptionID$i OR ";
				$sql = substr($sql,0,strlen($sql) - 4);
				$sql .= ")";
			}
			else return array();
			if($Creator != "") $sql .= " AND LOWER(Creator) LIKE LOWER(:Creator)";
			if($TitleFilter != "") $sql .= " AND LOWER(Title) LIKE LOWER(:TitleFilter)";
			if($FiltersFilter != "") $sql .= " AND LOWER(Filters) LIKE LOWER(:FiltersFilter)";
			if($DescriptionFilter != "") $sql .= " AND LOWER(Description) LIKE LOWER(:DescriptionFilter)";
			$sql .= " ORDER BY Title ASC";
			if($Limit > 0) $GetSubscriptions = $this->conn->prepare($sql . " LIMIT $Limit OFFSET $Offset");
			else $GetSubscriptions = $this->conn->prepare($sql);
			if($Creator != "") {
				$Creator = "%$Creator%";
				$GetSubscriptions->bindParam(":Creator", $Creator);
			}
			if($TitleFilter != "") {
				$TitleFilter = "%$TitleFilter%";
				$GetSubscriptions->bindParam(":TitleFilter", $TitleFilter);
			}
			if($FiltersFilter != "") {
				$FiltersFilter = "%$FiltersFilter%";
				$GetSubscriptions->bindParam(":FiltersFilter", $FiltersFilter);
			}
			if($DescriptionFilter != "") {
				$DescriptionFilter = "%$DescriptionFilter%";
				$GetSubscriptions->bindParam(":DescriptionFilter", $DescriptionFilter);
			}
			if(count($SubscriptionIDs) > 0) for($i = 0; $i < count($SubscriptionIDs); $i++) $GetSubscriptions->bindParam(":SubscriptionID$i", $SubscriptionIDs[$i]);
			$GetSubscriptions->bindParam(":ContentType", $ContentType);
			if(!$GetSubscriptions->execute()) return false;
			$Result['data'] = $GetSubscriptions->fetchAll(PDO::FETCH_ASSOC);
			if($Limit > 0) {
				$GetSubscriptionsRowCount = $this->conn->prepare($sql);
				$GetSubscriptionsRowCount->bindParam(":ContentType", $ContentType);
				if($Creator != "") {
					$Creator = "%$Creator%";
					$GetSubscriptionsRowCount->bindParam(":Creator", $Creator);
				}
				if($TitleFilter != "") {
					$TitleFilter = "%$TitleFilter%";
					$GetSubscriptionsRowCount->bindParam(":TitleFilter", $TitleFilter);
				}
				if($FiltersFilter != "") {
					$FiltersFilter = "%$FiltersFilter%";
					$GetSubscriptionsRowCount->bindParam(":FiltersFilter", $FiltersFilter);
				}
				if($DescriptionFilter != "") {
					$DescriptionFilter = "%$DescriptionFilter%";
					$GetSubscriptionsRowCount->bindParam(":DescriptionFilter", $DescriptionFilter);
				}
				if(count($SubscriptionIDs) > 0) for($i = 0; $i < count($SubscriptionIDs); $i++) $GetSubscriptionsRowCount->bindParam(":SubscriptionID$i", $SubscriptionIDs[$i]);
				if(!$GetSubscriptionsRowCount->execute()) return false;
				$Result['RowCount'] = $GetSubscriptionsRowCount->rowCount();
			}
			else $Result['RowCount'] = $GetSubscriptions->rowCount();
			return json_encode($Result);
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function WritePermissions($Username, $ContentID, $UserList) {
		try{
			if($this->WriteContentPermitted($ContentID, $Username)) {
				$sql = "DELETE FROM " . $this->WrapperPermissions . " WHERE ContentID=:ContentID";
				for($i = 0; $i < count($UserList); $i++) {
					$sql .= " AND Username != '" . $UserList[$i]->Username . "'";
				}
				$Delete = $this->conn->prepare($sql);
				$Delete->bindParam(":ContentID", $ContentID);
				if(!$Delete->execute()) return false;
				$Insert = $this->conn->prepare("INSERT INTO " . $this->WrapperPermissions . " (Username, ContentID) VALUES (:Username, :ContentID)");
				$Exists = $this->conn->prepare("SELECT * FROM " . $this->WrapperPermissions . " WHERE ContentID=:ContentID AND Username=:Username");
				$PermittedUser = "";
				$Insert->bindParam(":ContentID", $ContentID);
				$Insert->bindParam(":Username", $PermittedUser);
				$Exists->bindParam(":ContentID", $ContentID);
				$Exists->bindParam(":Username", $PermittedUser);
				for($i = 0; $i < count($UserList); $i++) {
					if($UserList[$i]->ID == 0) {
						$PermittedUser = $UserList[$i]->Username;
						if(!$Exists->execute()) return false;
						if($Exists->rowCount() == 0) if(!$Insert->execute()) return false;
					}
				}
				return true;
			}
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function GetPermittedUsers($Username, $ContentID) {
		try{
			if($this->ReadContentPermitted($ContentID, $Username)) {
				$stmt = $this->conn->prepare("SELECT * FROM " . $this->WrapperPermissions . " WHERE ContentID=:ID");
				$stmt->bindParam(":ID", $ContentID);
				if(!$stmt->execute()) return false;
				return json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function SearchUsers($Text) {
		try{
			$stmt = $this->conn->prepare("SELECT Username FROM " . $this->UserDetails . " WHERE Username LIKE :text");
			$Text = "%$Text%";
			$stmt->bindParam(":text", $Text);
			if(!$stmt->execute()) return false;
			else return json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
}
?>
