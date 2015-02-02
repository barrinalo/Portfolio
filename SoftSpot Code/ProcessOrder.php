<?php
session_start();
if(isset($_POST["Paypal"]) && isset($_SESSION["User"]) && isset($_POST["OrderID"])) {
	$User = $_SESSION["User"];
	$clientId = "AW-7NBA_WiRVSTEpfsAsKgIUQCMJv1HFdVfM-mLtLLg6NedKBAJh30gL569u";
	$clientSecret = "EE67sRB1cm-S0kj39Ncj8PkPyPy-IKvhVKUlvVy2KkDtd2tM6Wx31_qxfJy4";
	$TokenURL = "https://api.sandbox.paypal.com/v1/oauth2/token";
	$PaymentURL = "https://api.sandbox.paypal.com/v1/payments/payment";
	$curl_con = curl_init($TokenURL);
	
	curl_setopt($curl_con, CURLOPT_POST, true); 
	curl_setopt($curl_con, CURLOPT_SSL_VERIFYPEER, true);
	curl_setopt($curl_con, CURLOPT_USERPWD, $clientId . ":" . $clientSecret);
	curl_setopt($curl_con, CURLOPT_HEADER, false); 
	curl_setopt($curl_con, CURLOPT_RETURNTRANSFER, true); 
	curl_setopt($curl_con, CURLOPT_POSTFIELDS, "grant_type=client_credentials"); 
	
	$resp = curl_exec($curl_con);
	$resp = json_decode($resp);
	$Token = $resp->{'access_token'};
	require_once("dbhelper.php");
	$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
	if($con !== -1) {
		$OrderID = mysqli_real_escape_string($con, $_POST["OrderID"]);
		$OrderQuery = mysqli_query($con,"SELECT * FROM $ORDER_ITEMS WHERE OrderID=$OrderID");
		$OrderItems = array('items'=>array());
		$TotalCost = 0;
		while($row = mysqli_fetch_array($OrderQuery)) {
			$Price = $row["Price"];
			$Unit = $row["Unit"];
			$Quantity = $row["Quantity"];
			$Total = $Price * $Quantity;
			$ItemID = $row["Item_ID"];
			$ItemQuery = mysqli_query($con, "SELECT * FROM $PRODUCTS_TABLE WHERE Item_ID=$ItemID");
			$row2 = mysqli_fetch_array($ItemQuery);
			$ItemName = $row2["Name"];
			$ItemObj = array('quantity'=>"$Quantity",'name'=>"$ItemName",'price'=>number_format($Price,2),'currency'=>"SGD");
			array_push($OrderItems["items"],$ItemObj);
			$TotalCost += $Total;
		}
		$OrderData = array();
		$OrderData["intent"] = "sale";
		$OrderData["redirect_urls"] = array('return_url'=>"http://softspotconfectionary.com/ProcessOrder.php?OrderID=$OrderID",'cancel_url'=>"http://softspotconfectionary.com/ViewCart.php");
		$OrderData["payer"] = array('payment_method'=>"paypal");
		$OrderData["transactions"] = array(array('amount'=>array('total'=>number_format($TotalCost,2), 'currency'=>"SGD"),'description'=>"Thank you for ordering from us.  Collect at 36 Beo Crescent #01-43 S160036",'item_list'=>$OrderItems));
		$PaymentDetails = json_encode($OrderData);
		curl_close($curl_con);
		
		$curl = curl_init($PaymentURL); 
		curl_setopt($curl, CURLOPT_POST, true);
		curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, true);
		curl_setopt($curl, CURLOPT_HEADER, false);
		curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
		curl_setopt($curl, CURLOPT_HTTPHEADER, array(
				'Authorization: Bearer '.$Token,
				'Accept: application/json',
				'Content-Type: application/json'
				));
		curl_setopt($curl, CURLOPT_POSTFIELDS, $PaymentDetails); 
		
		$paymentresponse = curl_exec($curl);
		$paymentresponse = json_decode($paymentresponse);
		$redirectarray = $paymentresponse->{'links'}[1];
		$RedirectURL = $redirectarray->{'href'};
		$PaymentURL = $paymentresponse->{'id'};
		curl_close($curl);
		if(mysqli_num_rows(mysqli_query($con, "SELECT * FROM $PAYPAL_TABLE WHERE OrderID=$OrderID")) != 0)
		{
			mysqli_query($con, "UPDATE $PAYPAL_TABLE SET AccessToken='$Token', PaymentToken='$PaymentURL' WHERE OrderID=$OrderID");
		}
		else mysqli_query($con, "INSERT INTO $PAYPAL_TABLE (OrderID,AccessToken,PaymentToken) VALUES ($OrderID,'$Token','$PaymentURL')");
		mysqli_close($con);
		header("Location: $RedirectURL");
	}
}
else if(isset($_GET["PayerID"])) {
	require_once("dbhelper.php");
	$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
	if($con !== -1) {
	$OrderID= $_GET["OrderID"];
	$OrderQuery = mysqli_query($con, "SELECT * FROM $PAYPAL_TABLE WHERE OrderID=$OrderID");
	$row = mysqli_fetch_array($OrderQuery);
	
	$PayerID = $_GET["PayerID"];
	$PaymentID = $row["PaymentToken"];
	$Token = $row["AccessToken"];
	$data = json_encode(array(payer_id=>$PayerID));
	$PaymentURL = "https://api.sandbox.paypal.com/v1/payments/payment/$PaymentID/execute/";
	$curl = curl_init($PaymentURL); 
	curl_setopt($curl, CURLOPT_POST, true);
	curl_setopt($curl, CURLOPT_SSL_VERIFYPEER, true);
	curl_setopt($curl, CURLOPT_HEADER, false);
	curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
	curl_setopt($curl, CURLOPT_HTTPHEADER, array(
			'Authorization: Bearer '.$Token,
			'Accept: application/json',
			'Content-Type: application/json'
			));
	curl_setopt($curl, CURLOPT_POSTFIELDS, $data); 
	$resp = curl_exec($curl);
	curl_close($curl);
	
	if(strpos($resp,'"state":"completed"') !== false) {
		$User = $_SESSION["User"];
		require_once("dbhelper.php");
		$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
		if($con !== -1) {
			$OrderQuery = mysqli_query($con, "SELECT * FROM $ORDER_TABLE WHERE Email='$User' AND Status='Ordering'");
			$row = mysqli_fetch_array($OrderQuery);
			$OrderID = $row["Order_ID"];
			mysqli_query($con, "UPDATE $ORDER_TABLE SET Status='Ordered', Date_Ordered=CURDATE() WHERE Order_ID=$OrderID");
		}
	}
	mysqli_query($con, "DELETE FROM $PAYPAL_TABLE WHERE OrderID=$OrderID");
	header("Location: http://softspotconfectionary.com/ViewCart.php");
	mysqli_close($con);
	}
}
?>