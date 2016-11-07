<?php
require_once("../../includes/dbhandler.php");
require_once("ContentWrapperHandler.php");
class APIHandler extends ContentWrapperHandler {
	protected $APIKeys = "apikeys";
	function __construct() {
		parent::__construct();
	}
	function GenerateAccessToken($Username, $Password) {
		try {
			$VerifyUser = $this->conn->prepare("SELECT AccountPassword FROM " . $this->UserDetails . " WHERE Username=:Username");
			$VerifyUser->bindParam(":Username", $Username);
			$VerifyUser->execute();
			$VerifyUserResult = $VerifyUser->fetch(PDO::FETCH_ASSOC);
			if(password_verify($Password, $VerifyUserResult["AccountPassword"])) {
				$Keyspace = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_";
				$max = strlen($Keyspace);
				$min = 0;
				$Token = "";
				$TokenLength = mt_rand(50,100);
				for($i = 0; $i < $TokenLength; $i++) {
					$Token .= substr($Keyspace, mt_rand($min,$max)-1, 1);
				}
				$UpdateToken = $this->conn->prepare("DELETE FROM " . $this->APIKeys . " WHERE Username=:Username");
				$UpdateToken->bindParam(":Username", $Username);
				if(!$UpdateToken->execute()) return false;
				$UpdateToken = $this->conn->prepare("INSERT INTO " . $this->APIKeys . " (TempKey, Username, TimeCreated) VALUES (:Token, :Username, NOW())");
				$UpdateToken->bindParam(":Username", $Username);
				$UpdateToken->bindParam(":Token", $Token);
				if(!$UpdateToken->execute()) return false;
				return $Token;
			}
			else return false;
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function VerifyToken($Username, $Token) {
		try{
			$GetToken = $this->conn->prepare("SELECT * FROM " . $this->APIKeys . " WHERE Username=:Username AND TempKey=:Token");
			$GetToken->bindParam(":Username", $Username);
			$GetToken->bindParam(":Token", $Token);
			if(!$GetToken->execute()) return false;
			if($GetToken->rowCount() == 0) return false;
			else {
				$Curtime = time();
				$row = $GetToken->fetch(PDO::FETCH_ASSOC);
				$TokenTime = strtotime($row['TimeCreated']);
				if($TokenTime === false) return false;
				$diff = $Curtime - $TokenTime;
				if($diff > 60*60*24) {
					$UpdateToken = $this->conn->prepare("DELETE FROM " . $this->APIKeys . " WHERE Username=:Username");
					$UpdateToken->bindParam(":Username", $Username);
					if(!$UpdateToken->execute()) return false;
					return false;
				}
				else return true;
			}
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
}
?>
