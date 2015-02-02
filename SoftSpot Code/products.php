<?php
session_start();
require_once("dbhelper.php");

if(isset($_POST["Login"]) || isset($_POST["Register"])) {
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

if(isset($_SESSION["User"])) $loggedin = "Logged in as " . $_SESSION["User"] . " | <a href='ViewCart.php'>View Cart</a> | <a href='logout.php'>Logout</a>";
else {
$loggedin = "";
if(isset($errmsg)) $loggedin = "<span class='errmsg' style='float: left; padding-left: 1.5vw;'>" . $errmsg . "</span>";
$loggedin .= "<form action='index.php' method='post'>Email: <input type='text' name='LoginEmail' class='formtextbox'> Password: <input type='password' name='LoginPassword' class='formtextbox'> <input type='submit' value='Login' name='Login' class='formbutton'> | <input type='submit' value='Register' name='Register' class='formbutton'></form>";
}

$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
if($con !== -1) {
	$CategoriesQuery = mysqli_query($con, "SELECT DISTINCT Category FROM $PRODUCTS_TABLE");
	if($CategoriesQuery) {
		$productcategories = "Category: <select id='productcategory' name='productcategory' class='formtextbox'><option value='All'>All</option>";
		while($row = mysqli_fetch_array($CategoriesQuery)) {
			$category = $row["Category"];
			$productcategories .= "<option value='$category'>$category</option>";
		}
		$productcategories .= "</select><br />";
	}
	mysqli_close($con);
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
<script>
$(document).ready(function() {
	$.post("FilterProducts.php",{Filter:"All"},function(result) {
		$("#productarea").html(result);
	});
	$("#productcategory").change(function(e){
		$.post("FilterProducts.php",{Filter:$("#"+e.target.id).val()},function(result) {
			$("#productarea").html(result);
		});
	});
});
$(document).on('click','.FullProductPage',function(e){
	$("#ProductID").val(e.target.id);
	$("#RedirectToProductPage").submit();
});
</script>
</head>
<body>
<div id="container">
	<div id="header">
		<?php if(isset($loggedin)) echo $loggedin;?>
	</div>
	
	<div id="main">
		<img src="images/maintitle.png" width=30% height=auto>
		<br />
		<div id="Announcements">
		<a href='index.php'><input type="button" value="Back" class="formbutton"></a><br />
		<h1>Products - <?php if(isset($productcategories)) echo $productcategories;?></h1>
		
		<span id='productarea'></span>
		</div>
		<form id='RedirectToProductPage' method='post' action='FullProductPage.php'><input type='hidden' name='ProductID' id='ProductID' value=''></form>
	</div>
</div>
</body>
</html>