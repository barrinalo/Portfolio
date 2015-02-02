<?php
session_start();
if(isset($_SESSION["Logged_In"])) unset($_SESSION["Logged_In"]);

if(isset($_POST["user"]) && isset($_POST["pass"])) {
	$username = $_POST["user"];
	$password = $_POST["pass"];
	if($username == "ssadmin" && $password=="lotsofcookies") {
		$_SESSION["Logged_In"] = true;
		header("Location: admin_index.php");
	}
}
?>

<!DOCTYPE html>
<html>
<head>
<title>Soft Spot - Fresh Bakes Daily</title>
<link rel="shortcut icon" href="favicon.ico">
<link href='http://fonts.googleapis.com/css?family=Courgette' rel='stylesheet' type='text/css'>
<link rel="stylesheet" type="text/css" href="site-css/site-theme.css">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
</head>
<body>
<br />
<img src="images/maintitle.png" style="max-width:30%;height:auto;"><br /><br />
<center>
<div id="adminlogin">
<form action="<?php echo $_SERVER['PHP_SELF'];?>" method="post">
Username: <input type="text" name="user" class="formtextbox"><br /><br />
Password: <input type="password" name="pass" class="formtextbox"><br /><br />
<input type="submit" value="Log in" class="formbutton">
</form>
</div>
</center>
</div>
</body>
</html>