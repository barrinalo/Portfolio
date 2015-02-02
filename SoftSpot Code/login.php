<?php
session_start();
if(isset($_SESSION["User"])) unset($_SESSION["User"]);

if(isset($_POST["Login"]) || isset($_POST["Register"])) {
	require_once("dbhelper.php");
	$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
	if($con !== -1) {
		$Email = mysqli_real_escape_string($con, $_POST["LoginEmail"]);
		$Password = mysqli_real_escape_string($con, $_POST["LoginPassword"]);
		if(isset($_POST["Login"])) {
			$query = mysqli_query($con, "SELECT * FROM $CUSTOMER_TABLE WHERE Email='$Email' AND Password='$Password'");
			if(mysqli_num_rows($query) == 0) $errmsg = "Sorry this email/password combination is not registered.";
			else {
				$result = mysqli_fetch_array($query);
				$_SESSION["User"] = $result["Email"];
				header("Location: index.php");
			}
		}
		else if(isset($_POST["Register"])) {
			$query = mysqli_query($con, "SELECT * FROM $CUSTOMER_TABLE WHERE Email='$Email'");
			if(mysqli_num_rows($query) == 0) {
				if(strlen($Password) > 8 && filter_var($Email,FILTER_VALIDATE_EMAIL)) {
				$query = mysqli_query($con, "INSERT INTO $CUSTOMER_TABLE (Email,Password) VALUES('$Email','$Password')");
				if($query) $errmsg = "Thankyou for registering you may log in now.";
				else $errmsg = "Sorry there was an error in the registration, please try again.  If the problem persists please report it to the webmaster";
				}
				else $errmsg = "Sorry the email is invalid or the password is less than 8 characters.";
			}
			else $errmsg = "Sorry email already registered";
		}
	}
	mysqli_close($con);
}

?>
<!DOCTYPE html>
<html>
<head>
<title>Soft Spot - Fresh Bakes Daily</title>
<link rel="shortcut icon" href="favicon.ico">
<link rel="stylesheet" type="text/css" href="site-css/site-theme.css">
</head>
<body>
<img src="images/maintitle.png" style="max-width:30%;height:auto;"><br>
<center>
<table border=0px width=75%>
<tr>
<td class="nav-bar"><a href="index.php">Home</a></td>
<td class="nav-bar"><a href="products.php">Our Products</a></td>
<td class="nav-bar"><a href="order.php">Place an Order</a></td>
<td class="nav-bar"><a href="track.php">Track an Order</a></td>
<td class="nav-bar"><a href="http://www.softspotconfectionary.com/about.php">About</a></td>
</tr>
<tr>
<td colspan=5 class="main-content">
<br>
<form action="<?php echo $_SERVER['PHP_SELF'];?>" method="post">
Email: <input type="text" name="LoginEmail"><br />
Password: <input type="password" name="LoginPassword"><br />
<input type="submit" value="Log in" name="Login"> or <input type="submit" value="Register" name="Register"><br />
</form>
<?php if(isset($errmsg)) echo $errmsg;?><br /><br />
</td>
</tr>
<tr>
<td class="footer" colspan=5>
</td>
</tr>
</table>
</center>
</body>
</html>