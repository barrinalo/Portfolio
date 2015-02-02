<?php
session_start();
	if(isset($_SESSION["User"])) {
		require_once("dbhelper.php");
		$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
		if($con !== -1) {
			$User = $_SESSION["User"];
			$OrderQuery = mysqli_query($con, "SELECT * FROM $ORDER_TABLE WHERE Email='$User' AND Status='Ordering'");
			if(mysqli_num_rows($OrderQuery) == 1) {
				$row = mysqli_fetch_array($OrderQuery);
				$OrderID = $row["Order_ID"];
			}
			else {
				mysqli_query($con, "INSERT INTO $ORDER_TABLE (Email,Status,Comments) VALUES('$User','Ordering','')");
				$OrderQuery = mysqli_query($con, "SELECT * FROM $ORDER_TABLE WHERE Email='$User' AND Status='Ordering'");
				if($OrderQuery) {
					$row = mysqli_fetch_array($OrderQuery);
					$OrderID = $row["Order_ID"];
				}
			}
			
			$counter = 1;
			$ProductID = mysqli_real_escape_string($con, $_POST["ProductID"]);
			
			while(isset($_POST["OrderUnit$counter"])) {
				if($_POST["OrderUnit$counter"] != "" && $_POST["OrderQuantity$counter"] != "") {
					$PriceUnit = mysqli_real_escape_string($con, $_POST["OrderUnit$counter"]);
					$Price = explode("/",$PriceUnit)[0];
					$Unit = explode("/",$PriceUnit)[1];
					$Quantity = mysqli_real_escape_string($con, $_POST["OrderQuantity$counter"]);
					$Remarks = mysqli_real_escape_string($con, $_POST["OrderRemarks$counter"]);
					mysqli_query($con, "INSERT INTO $ORDER_ITEMS (OrderID,Unit,Price,Quantity,Item_ID,Remarks) VALUES ($OrderID,'$Unit',$Price,$Quantity,$ProductID,'$Remarks')");
				}
				$counter++;
			}
			mysqli_close($con);
		}
		header("Location: products.php");
	}
	
?>