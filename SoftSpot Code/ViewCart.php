<?php
session_start();
if(!isset($_SESSION["User"])) header("Location: index.php");
else {
	require_once("dbhelper.php");
	$loggedin = "Logged in as " . $_SESSION["User"] . " | <a href='ViewCart.php'>View Cart</a> | <a href='logout.php'>Logout</a>";
	$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
	if($con !== -1) {
		$User = $_SESSION["User"];
		$Orders = "<center><table class='checkout'><tr><th class='checkout'>Remove</th><th width='40%' class='checkout'>Item</th><th width='20%' class='checkout'>Unit</th><th width='10%' class='checkout'>Quantity</th><th width='15%' class='checkout'>Price</th><th width='15%' class='checkout'>Sub Total</th></tr>";
		$OrderQuery = mysqli_query($con, "SELECT * FROM $ORDER_TABLE WHERE Email='$User' AND Status='Ordering'");
		$row = mysqli_fetch_array($OrderQuery);
		$OrderID = $row["Order_ID"];
		$AllCosts = 0;
		$OrderQuery = mysqli_query($con, "SElECT * FROM $ORDER_ITEMS WHERE OrderID=$OrderID");
		if($OrderQuery) {
		while($row = mysqli_fetch_array($OrderQuery)) {
			$OrderItemID = $row["Order_Item_ID"];
			$Unit = $row["Unit"];
			$Price = $row["Price"];
			$Quantity = $row["Quantity"];
			$Remarks = $row["Remarks"];
			$Total = $Price * $Quantity;
			$ItemID = $row["Item_ID"];
			$ItemQuery = mysqli_query($con, "SELECT * FROM $PRODUCTS_TABLE WHERE Item_ID=$ItemID");
			$row2 = mysqli_fetch_array($ItemQuery);
			$ItemName = $row2["Name"];
			$Orders .= "<tr><td class='checkout'><input type='button' value='Remove' class='deleteprice' id='$OrderItemID'></td><td width='40%' class='checkout'>$ItemName<br />Remarks: $Remarks</td><td width='20%' class='checkout'>$Unit</td><td width='10%' class='checkout'>$Quantity</td><td width='15%' class='checkout'>" . '$' .  number_format($Price,2) . "</td><td width='15%' class='checkout'>" . '$' . number_format($Total,2) . "</td></tr>";
			$AllCosts += $Total;
		}
		$Orders .= "<tr><td width='85%' colspan='5'><b>Total</b></td><td width='15%'>" . '$' . number_format($AllCosts,2) . "</td></tr>";
		}
		$Orders .= "</table>";
		$Orders .= "<form action='ProcessOrder.php' method='post'><input type='hidden' value='$OrderID' name='OrderID'><input type='submit' class='formbutton' name='Paypal' value='Checkout'></form><br />";
		$Orders .= "<br /><a href='index.php'><input type='button' class='formbutton' value='Home'></a> &nbsp; <a href='products.php'><input type='button' class='formbutton' value='Browse More Products'></a></center>";
		
		$History = "<h1>Past Orders</h1>";
		$History .= "<center><table class='checkout'><tr><th class='checkout'>Description</th><th class='checkout'>Amount</th><th class='checkout'>Status</th><th class='checkout'>Remarks</th></tr>";
		$HistoryQuery = mysqli_query($con, "SELECT * FROM $ORDER_TABLE WHERE Email='$User' AND Status!='Ordering'");
		while($row = mysqli_fetch_array($HistoryQuery)) {
			$TotalCost = 0;
			$OrderID = $row["Order_ID"];
			$Status = $row["Status"];
			$Comments = $row["Comments"];
			$History .= "<tr><td class='checkout'><ul>";
			$HistoryItemsQuery = mysqli_query($con, "SELECT * FROM $ORDER_ITEMS WHERE OrderID=$OrderID");
			while($row2 = mysqli_fetch_array($HistoryItemsQuery)) {
				$ItemID = $row2["Item_ID"];
				$Unit = $row2["Unit"];
				$Price = $row2["Price"];
				$Quantity = $row2["Quantity"];
				$Remarks = $row2["Remarks"];
				$Total = $Price * $Quantity;
				$ItemQuery = mysqli_query($con, "SELECT * FROM $PRODUCTS_TABLE WHERE Item_ID=$ItemID");
				$row3 = mysqli_fetch_array($ItemQuery);
				$ItemName = $row3["Name"];
				$History .= "<li>$ItemName ($Unit) - " . '$' . number_format($Price,2) . " x $Quantity = " . '$' . number_format($Total,2) . " Remarks: $Remarks</li>";
				$TotalCost += $Total;
			}
			$History .= "</li></td><td class='checkout'>" . '$' . number_format($TotalCost,2) . "</td><td class='checkout'>$Status</td><td class='checkout'>$Comments</td></tr>";
		}
		$History .= "</table>";
		mysqli_close($con);
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
	$(".deleteprice").click(function(e) {
		$("#OrderItemID").val(e.target.id);
		$("#DeleteItemForm").submit();
	});
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
			<?php if(isset($Orders)) echo $Orders;?>
			<form action='DeleteItem.php' method='post' id='DeleteItemForm'><input type='hidden' name='OrderItemID' id='OrderItemID' value=''></form>
			<?php if(isset($History)) echo $History;?>
		</div>
	</div>
</div>
</body>
</html>