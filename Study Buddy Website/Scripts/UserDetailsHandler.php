<?php
class UserDetailsHandler extends DBConnection {
	private $UserDetails = "userdetails";

	function __construct() {
		parent::__construct();
	}
	function ResetPassword($Email) {
		$PasswordLength = mt_rand(20,50);
		$count = 0;
		$NewPassword = "";
		$KeySpace = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_";
		for($i = 0; $i < $PasswordLength; $i++) {
			$NewPassword .= substr($KeySpace, mt_rand(0,strlen($KeySpace)-1), 1);
		}
		$HashedPassword = password_hash($NewPassword, PASSWORD_DEFAULT);
		$ChangePassword = $this->conn->prepare("UPDATE " . $this->UserDetails . " SET AccountPassword=:NewPassword WHERE Email=:Email");
		$ChangePassword->bindParam(":NewPassword", $HashedPassword);
		$ChangePassword->bindParam(":Email", $Email);
		if(!$ChangePassword->execute()) return "Failed to reset password";
		if(@mail($Email,"Study Buddy - Password Reset", "Your new password is $NewPassword")) return "Password reset email sent";
		else return "Failed to send email";		
	}
	function ChangePassword($Username, $OldPassword, $NewPassword) {
		try{
			if($this->VerifyUser($Username, $OldPassword)) {
				$HashedPassword = password_hash($NewPassword, PASSWORD_DEFAULT);
				$ChangePassword = $this->conn->prepare("UPDATE " . $this->UserDetails . " SET AccountPassword=:NewPassword WHERE Username=:Username");
				$ChangePassword->bindParam(":NewPassword", $HashedPassword);
				$ChangePassword->bindParam(":Username", $Username);
				if($ChangePassword->execute()) return "<span class='alert alert-success'>Password changed</span>";
				else return "<span class='alert alert-danger'>Sorry unable to change password</span>";
			}
			else return "<span class='alert alert-warning'>Sorry old password does not match</span>";
		}
		catch(PDOException $e) {
			echo $e->GetMessage();
			return false;
		}
	}
	function VerifyUser($Username, $Password) {
		try {
			$VerifyUser = $this->conn->prepare("SELECT AccountPassword FROM " . $this->UserDetails . " WHERE Username=:Username");
			$VerifyUser->bindParam(":Username", $Username);
			$VerifyUser->execute();
			$VerifyUserResult = $VerifyUser->fetch(PDO::FETCH_ASSOC);
			if(password_verify($Password, $VerifyUserResult["AccountPassword"])) return true;
			else return false;
		}
		catch(PDOException $e) {
			return false;
		}
	}
	function RegisterNewUser($Username, $Email, $Password) {
		try {
			$RegisterUser = $this->conn->prepare("INSERT INTO " . $this->UserDetails . " (Username, AccountPassword, Email, AccountStatus) VALUES (:Username, :Password, :Email, :Status)");
			$AccountStatus = "Active";
			$HashedPassword = password_hash($Password, PASSWORD_DEFAULT);
			$RegisterUser->bindParam(":Username", $Username);
			$RegisterUser->bindParam(":Password", $HashedPassword);
			$RegisterUser->bindParam(":Email", $Email);
			$RegisterUser->bindParam(":Status", $AccountStatus);
			$RegisterUser->execute();
			mkdir("../Users/$Username");
			return true;
		}
		catch(PDOException $e)
    {
			return false;
   	}
	}
	function CheckUsernameExists($Username) {
		try {
			$Usernamecheck = $this->conn->prepare("SELECT COUNT(*) FROM " . $this->UserDetails . " WHERE Username=:Username");
			$Usernamecheck->bindParam(":Username", $Username);
			$Usernamecheck->execute();
			$Usernameresult = $Usernamecheck->fetchColumn();
			if($Usernameresult[0] >= 1) return true;
			else return false;
		}
		catch(PDOException $e)
    {
   		echo $e->getMessage();
   	}
	}
	function CheckEmailExists($Email) {
		if(!isset($this->connerror)) {
			try {
				$Emailcheck = $this->conn->prepare("SELECT COUNT(*) FROM " . $this->UserDetails . " WHERE Email=:Email");
				$Emailcheck->bindParam(":Email", $Email);
				$Emailcheck->execute();
				$Emailresult = $Emailcheck->fetchColumn();
				if($Emailresult[0] >= 1) return true;
				else return false;
			}
			catch(PDOException $e)
    	{
    		echo $e->getMessage();
    	}
		}
	}
}
?>
