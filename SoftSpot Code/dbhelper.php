<?php
$HOST = "localhost";
$USERNAME = "softspot_admin";
$PASSWORD = "2,3,4,5,6-Pentahydroxyhexanal";
$DBNAME = "softspot_database";
$PRODUCTS_TABLE = "catalog";
$PRICE_TABLE = "price_catalog";
$REVIEW_TABLE = "reviews";
$ORDER_TABLE = "order_log";
$ORDER_ITEMS = "order_items";
$PAYPAL_TABLE = "paypal_keys";
$CUSTOMER_TABLE = "customers";
$ANNOUNCEMENT_TABLE = "announcements";

function dbconnect($host, $user, $password, $database) {
	$con = mysqli_connect($host,$user,$password,$database);
	if (mysqli_connect_errno()) {
		return -1;
	}
	else {
		return $con;
	}
}
?>