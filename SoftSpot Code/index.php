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
	$UpdateQuery = mysqli_query($con, "SELECT * FROM $ANNOUNCEMENT_TABLE WHERE ID<>1 ORDER BY Date_Of_Announcement DESC LIMIT 5");
	if($UpdateQuery) {
		$Updates = "";
		while($row = mysqli_fetch_array($UpdateQuery)) {
			$ATitle = $row["Title"];
			$AText = $row["Main_Text"];
			$ADate = $row["Date_Of_Announcement"];
			$Updates .= "<h1>$ADate - $ATitle</h1><p class='Announcement'>$AText</p><br />";
		}
	}
	$AboutQuery = mysqli_query($con, "SELECT * FROM $ANNOUNCEMENT_TABLE WHERE ID=1");
	if($AboutQuery) {
		$row = mysqli_fetch_array($AboutQuery);
		$AboutTitle = $row["Title"];
		$AboutText = $row["Main_Text"];
		$About = "<h1>$AboutTitle</h1>$AboutText<br />";
	}
	$FeaturedQuery = mysqli_query($con, "SELECT * FROM $PRODUCTS_TABLE WHERE Featured='Yes'");
	if($FeaturedQuery) {
		$FeaturedProducts = "";
		$Counter = 1;
		while($row = mysqli_fetch_array($FeaturedQuery)) {
			$FeaturedPicture = $row["Picture_Name"];
			$FeaturedProducts .= "<img src='images/$FeaturedPicture' width='80%' height='80%' class='SlideShow' id='Picture$Counter' style='cursor: pointer'>";
			$Counter++;
		}
		$FeaturedProducts .= "<form action='FullProductPage.php' method='post' id='PromoForm'><input type='hidden' name='ProductID' id='ProductID' value=''></form>";
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
<script>
$(document).ready(function() {
	function ChangeSlide() {
		if(!$(".CurrentSlide").length) {
			$("#Picture1").show();
			$("#Picture1").attr("class","CurrentSlide");
			var imagename = $("#Picture1").attr("src");
			var promoid = imagename.slice(imagename.indexOf("Product") + 7,imagename.indexOf("."));
			$("#ProductID").val(promoid);
		}
		else {
			var cur = $(".CurrentSlide");
			var CurID = $(".CurrentSlide").attr("id");
			CurID = CurID.slice(7,CurID.length);
			CurID = Number(CurID);
			CurID = CurID + 1;
			
			var next;
			if(!$("#Picture" + CurID.toString()).length) next = $("#Picture1").attr("class","CurrentSlide");
			else next = $("#Picture" + CurID.toString()).attr("class","CurrentSlide");
			
			cur.attr("class","SlideShow");
			var imagename = next.attr("src");
			var promoid = imagename.slice(imagename.indexOf("Product") + 7,imagename.indexOf("."));
			cur.fadeOut("slow",function(){next.fadeIn("slow")});
			$("#ProductID").val(promoid);
		}
	}
	$(".SlideShow").hide();
	ChangeSlide();
	
	setInterval(ChangeSlide,5000);
});
$(document).on("click",".CurrentSlide", function() {
	$("#PromoForm").submit();
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
		<?php if(isset($Updates)) echo $Updates;?>
		</div>
		<div id="Products">
		<h1>Products</h1>
		<?php if(isset($FeaturedProducts)) echo $FeaturedProducts;?>
		<br /><a href='products.php'>View All</a>
		</div>
		<div id="About">
		<?php if(isset($About)) echo $About;?>
		<br />
		Contact: 9793 9109<br />
		Email: soft.spot.confectionary@gmail.com<br />
		Address: 36 Beo Crescent #01-43 Singapore 160036
		<iframe
  		width=350vw;
  		height=300vh;
  		frameborder="0" style="border:0"
  		src="https://www.google.com/maps/embed/v1/place?key=AIzaSyDMCTIuya_tdNitgF2clEELg18Or1Hmgs0
    		&q=36+Beo+Crescent+Singapore">
		</iframe>
		</div>
	</div>
</div>
</body>
</html>