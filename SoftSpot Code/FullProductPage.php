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
	if(isset($_POST["ProductID"])) {
		$ProductID = mysqli_real_escape_string($con,$_POST["ProductID"]);
		$ProductQuery = mysqli_query($con, "SELECT * FROM $PRODUCTS_TABLE WHERE Item_ID=$ProductID");
		$ProductData = mysqli_fetch_array($ProductQuery);
		
		$ProductName = $ProductData["Name"];
		$ProductCategory = $ProductData["Category"];
		$ProductPicture = $ProductData["Picture_Name"];
		$ProductDescription = $ProductData["Description"];
		
		$Output = "<form method='post' action='Review.php'><a href='products.php'><input type='button' value='Back' class='formbutton'></a>&nbsp;<input type='hidden' value='$ProductID' name='ReviewProductID'><input type='submit' value='Write A Review' class='formbutton'></form>";
		$Output .= "<h1>$ProductCategory - $ProductName</h1>";
		$Output .= "<img src='images/$ProductPicture' width='33%' height='auto'>";
		$Output .= "<br /><p>$ProductDescription</p><br /><form method='post' action='AddToCart.php' id='ProductForm' name='ProductForm'><input type='hidden' name='ProductID' value='$ProductID'><span id='OrderArea'><span id='OrderItem1'>";
		
		$PricingQuery = mysqli_query($con, "SELECT * FROM $PRICE_TABLE WHERE Item_ID=$ProductID");
		if(isset($_SESSION["User"])) {
		$Output .= "Unit: <select class='pricecalculation' form='ProductForm' name='OrderUnit1' id='OrderUnit1'><option value=''></option>";
		while($row = mysqli_fetch_array($PricingQuery)) {
			$Unit = $row["Unit"];
			$Price = $row["Price"];
			$Output .= "<option value='$Price/$Unit'>" . '$' . "$Price/$Unit</option>";
		}
		$Output .= "</select> &nbsp; Quantity: <input type='text' id='OrderQuantity1' name='OrderQuantity1' value='' class='pricecalculation'> Remarks*: <input type='text' id='OrderRemarks1' value='' class='formtextbox' name='OrderRemarks1'></span>";
		$Output .= "</span><br /><input type='button' value='Add Row' id='AddRow' class='formbutton'>";
		$Output .= " &nbsp; <input type='button' value='Add To Cart' class='formbutton' id='AddToCart'></form>*Include need by date and other info in Remarks.";
		}
		else {
			while($row = mysqli_fetch_array($PricingQuery)) {
				$Unit = $row["Unit"];
				$Price = $row["Price"];
				$Output .= '$' . "$Price/$Unit<br />";
			}
			$Output .= "<br />Please log in to order.</form>";
		}
		$Output .= "<br /><span id='errmsg' style='color: red;'></span>";
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
	$("#AddRow").click(function() {
		var counter = 1;
		while($("#OrderItem" + counter.toString()).length) {
			counter++;
		}
		var txt = $("#OrderItem1").html();
		txt = txt.replace(/OrderUnit1/g, "OrderUnit" + counter.toString());
		txt = txt.replace(/OrderQuantity1/g,"OrderQuantity" + counter.toString());
		txt = txt.replace(/OrderRemarks1/g,"OrderQuantity" + counter.toString());
		
		$("#OrderArea").append("<span id='OrderItem" + counter.toString() + "'><br />" + txt + " &nbsp; <input type='button' value='Remove' id='Delete" + counter.toString() + "' class='deleteprice'></span>");
	});
	$("#AddToCart").click(function(e){
		var counter = 1;
		while($("#OrderItem" + counter.toString()).length) {
			if(!$.isNumeric($("#OrderQuantity" + counter.toString()).val())) {
				$("#errmsg").html("Please enter a valid quantity");
				return;
			}
			counter++;
		}
		
		$("#ProductForm").submit();
	});
});

$(document).on('click','.deleteprice',function(e) {
	var counter = e.target.id.slice(6,event.target.id.length);
	counter = Number(counter);
	$("#OrderItem" + counter.toString()).remove();
	counter++;
	while($("#OrderItem" + counter.toString()).length) {
		$("#OrderItem" + counter.toString()).attr("id","OrderItem" + Number(counter-1));
		$("#OrderUnit" + counter.toString()).attr("name","OrderUnit" + Number(counter-1));
		$("#OrderUnit" + counter.toString()).attr("id","OrderUnit" + Number(counter-1));
		$("#OrderQuantity" + counter.toString()).attr("name","OrderQuantity" + Number(counter-1));
		$("#OrderQuantity" + counter.toString()).attr("id","OrderQuantity" + Number(counter-1));
		$("#OrderRemarks" + counter.toString()).attr("name","OrderRemarks" + Number(counter-1));
		$("#OrderRemarks" + counter.toString()).attr("id","OrderRemarks" + Number(counter-1));
		$("#Delete" + counter.toString()).attr("id","Delete" + Number(counter-1));
		counter++;
	}
	
	counter = 1;
	var total = 0;
	while($("#OrderItem" + counter.toString()).length) {
		var Quantity = Number($("#OrderQuantity" + counter.toString()).val());
		var PriceUnit = $("#OrderUnit" + counter.toString()).val();
		var Price = Number(PriceUnit.slice(0,PriceUnit.indexOf("/")));
		if(!isNaN(Quantity) && !isNaN(Price)) {
			total += (Quantity * Price);
			
		}
		counter++;
	}
	$("#errmsg").html("Total: $" + total.toFixed(2).toString());
});
$(document).on('change','.pricecalculation',function(e) {
	var counter = 1;
	var total = 0;
	while($("#OrderItem" + counter.toString()).length) {
		var Quantity = Number($("#OrderQuantity" + counter.toString()).val());
		var PriceUnit = $("#OrderUnit" + counter.toString()).val();
		var Price = Number(PriceUnit.slice(0,PriceUnit.indexOf("/")));
		if(!isNaN(Quantity) && !isNaN(Price)) {
			total += (Quantity * Price);
			
		}
		counter++;
	}
	$("#errmsg").html("Total: $" + total.toFixed(2).toString());
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
		<?php if(isset($Output)) echo $Output;?>
		</div>
	</div>
</div>
</body>
</html>