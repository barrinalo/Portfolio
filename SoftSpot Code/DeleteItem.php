<?php
	if(isset($_POST["OrderItemID"])) {
		require_once("dbhelper.php");
		$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
		if($con !== -1) {
			$OrderItemID = mysqli_real_escape_string($con, $_POST["OrderItemID"]);
			mysqli_query($con, "DELETE FROM $ORDER_ITEMS WHERE Order_Item_ID=$OrderItemID");
		}
	}
	header("Location: ViewCart.php");
?>