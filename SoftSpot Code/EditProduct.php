<?php
session_start();
if(!isset($_SESSION["Logged_In"])) header("Location: admin.php");
if(!isset($_POST["ProductID"])) header("Location: admin_index.php");
else {
	require_once("dbhelper.php");
	$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
	if($con !== -1) {
		if(isset($_POST["UpdateProduct"])) {
			$ProductID = mysqli_real_escape_string($con,$_POST["ProductID"]);
			$ProductName = mysqli_real_escape_string($con,$_POST["productname"]);
			$ProductDescription = mysqli_real_escape_string($con,$_POST["productdescription"]);
			if(isset($_POST["Featured"])) $ProductFeatured = "Yes";
			else $ProductFeatured = "No";
			if($_POST["productcategory"] == "") $ProductCategory = mysqli_real_escape_string($con,$_POST["newproductcategory"]);
			else $ProductCategory = $_POST["productcategory"];
			if($_FILES["productpic"]["size"] < 524288 && getimagesize($_FILES["productpic"]["tmp_name"]) != 0) {
				$NewProductPic= "Product$ProductID." . explode(".", $_FILES["productpic"]["name"])[1];
				if(!file_exists("images/$NewProductPic")) {
					move_uploaded_file($_FILES["productpic"]["tmp_name"], "images/$NewProductPic");
				}
			}
			else $NewProductPic="";
			if($NewProductPic != "") {
				$ProductQuery = mysqli_query($con,"SELECT * FROM $PRODUCTS_TABLE WHERE Item_ID=$ProductID");
				$row = mysqli_fetch_array($ProductQuery);
				$OldProductPic = $row["Picture_Name"];
				if($OldProductPic != "") unlink("images/$OldProductPic");
				if($OldProductPic == $NewProductPic) {
					move_uploaded_file($_FILES["productpic"]["tmp_name"], "images/$NewProductPic");
				}
			}
			if($NewProductPic != "") mysqli_query($con,"UPDATE $PRODUCTS_TABLE SET Name='$ProductName', Description='$ProductDescription', Category='$ProductCategory', Picture_Name='$NewProductPic', Featured='$ProductFeatured' WHERE Item_ID=$ProductID");
			else mysqli_query($con,"UPDATE $PRODUCTS_TABLE SET Name='$ProductName', Description='$ProductDescription', Category='$ProductCategory', Featured='$ProductFeatured' WHERE Item_ID=$ProductID");
			
			mysqli_query($con, "DELETE FROM $PRICE_TABLE WHERE Item_ID=$ProductID");
			$PricingInfoCounter = 1;
			while(isset($_POST["unittb$PricingInfoCounter"])) {
				if($_POST["unittb$PricingInfoCounter"] != "" && $_POST["pricetb$PricingInfoCounter"] != "") {
					$unit = mysqli_real_escape_string($con, $_POST["unittb$PricingInfoCounter"]);
					$price = mysqli_real_escape_string($con, $_POST["pricetb$PricingInfoCounter"]);
					mysqli_query($con, "INSERT INTO $PRICE_TABLE (Item_ID,Unit,Price) VALUES ($ProductID,'$unit',$price)");
				}
				$PricingInfoCounter++;
			}
		}
		else if(isset($_POST["DeleteProduct"])) {
			$ProductID = mysqli_real_escape_string($con,$_POST["ProductID"]);
			$ProductQuery = mysqli_query($con,"SELECT * FROM $PRODUCTS_TABLE WHERE Item_ID=$ProductID");
			$row = mysqli_fetch_array($ProductQuery);
			$OldProductPic = $row["Picture_Name"];
			if($OldProductPic != "") unlink("images/$OldProductPic");
			mysqli_query($con, "DELETE FROM $PRODUCTS_TABLE WHERE Item_ID=$ProductID");
			mysqli_query($con, "DELETE FROM $PRICE_TABLE WHERE Item_ID=$ProductID");
			header("Location: admin_index.php");
		}
		$ProductID = mysqli_real_escape_string($con,$_POST["ProductID"]);
		$ProductQuery = mysqli_query($con,"SELECT * FROM $PRODUCTS_TABLE WHERE Item_ID=$ProductID");
		if($ProductQuery) {
			$row = mysqli_fetch_array($ProductQuery);
			$ProductForm = "<form action='EditProduct.php' method='post' id='UpdateProduct' enctype='multipart/form-data'><input type='hidden' name='ProductID' value='$ProductID'>";
			$ProductID = $row["Item_ID"];
			$ProductName = $row["Name"];
			$ProductImage = $row["Picture_Name"];
			$ProductDescription = $row["Description"];
			$ProductCategory = $row["Category"];
			$ProductFeatured = $row["Featured"];
			$ProductForm .= "Product Name: <input type='text' name='productname' value='$ProductName' class='formtextbox'><br />";
			if(file_exists("images/$ProductImage") && $ProductImage != "") $ProductForm .= "<img src='images/$ProductImage' width='25%' height=auto><br />Change Picture: <input type='file' name='productpic' class='formbutton'><br />";
			else $ProductForm .= "Upload Picture: <input type='file' name='productpic' class='formbutton'><br />";
			$CategoriesQuery = mysqli_query($con, "SELECT DISTINCT Category FROM $PRODUCTS_TABLE");
			if($CategoriesQuery) {
				$productcategories = "Category: <select form='UpdateProduct' name='productcategory' class='formtextbox'><option value=''></option>";
				while($row = mysqli_fetch_array($CategoriesQuery)) {
					$category = $row["Category"];
					if($category == $ProductCategory) $productcategories .= "<option value='$category' selected='selected'>$category</option>";
					else $productcategories .= "<option value='$category'>$category</option>";
				}
				$productcategories .= "</select> &nbsp; New Category: <input type='text' class='formtextbox' name='newproductcategory'><br />";
				if($ProductFeatured == "Yes") $productcategories .= "Feature: <input type='checkbox' name='Featured' value='Featured' checked><br />";
				else $productcategories .= "Feature: <input type='checkbox' name='Featured' value='Featured'><br />";
				$ProductForm .= $productcategories;
			}
			
			$ProductForm .= "Description:<br /><textarea form='UpdateProduct' name='productdescription' style='resize: none; width: 60vw; height: 10vh; margin-top: 1vh;' class='formtextbox'>$ProductDescription</textarea><br />";
			
			#Get pricing info
			$PricingQuery = mysqli_query($con,"SELECT * FROM $PRICE_TABLE WHERE Item_ID=$ProductID");
			$ProductForm .= "<span id='pricearea'>";
			$PricingCounter = 1;
			while($row = mysqli_fetch_array($PricingQuery)) {
				$unit = $row["Unit"];
				$price = $row["Price"];
				$ProductForm .= "<span id='PriceAndUnit$PricingCounter'>Unit: <input type='text' class='formtextbox' value='$unit' name='unittb$PricingCounter' id='unittb$PricingCounter'> &nbsp; Price: <input type='text' class='formtextbox' value='$price' name='pricetb$PricingCounter' id='pricetb$PricingCounter'> &nbsp; <input type='button' class = 'deleteprice' value='Remove' id='delete$PricingCounter'><br /></span>";
				$PricingCounter++;
			}
			$ProductForm .= "</span><br />";
			$ProductForm .= "<input type='button' class='formbutton' id='addpayment' value='Add Price Information'><br />";
			$ProductForm .= "<input type='submit' name='UpdateProduct' value='Update' class='formbutton'> <input type='submit' name='DeleteProduct' value='Delete' class='formbutton'>";
			$ProductForm .= "</form>";
		}
		else $ProductForm = "Sorry unable to find product, please click <a href='admin_index.php'>here</a> to return to the main page.";
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
	$("#addpayment").click(function() {
		var counter = 1;
		while($("#PriceAndUnit" + counter.toString()).length) {
			counter++;
		}
		$("#pricearea").append("<span id='PriceAndUnit" + counter.toString() + "'>Unit: <input type='text' class='formtextbox' value='' id='unittb" + counter.toString() + "' name='unittb" + counter.toString() + "'> &nbsp; Price: <input type='text' class='formtextbox' value='' id='pricetb" + counter.toString() + "' name='pricetb" + counter.toString() + "'> &nbsp; <input type='button' class='deleteprice' value='Remove' id='delete" + counter.toString() + "'><br /></span>");
	});
});

$(document).on('click','.deleteprice',function(e) {
	var counter = e.target.id.slice(6,event.target.id.length);
	counter = Number(counter);
	$("#PriceAndUnit" + counter.toString()).remove();
	counter++;
	while($("#PriceAndUnit" + counter.toString()).length) {
		$("#PriceAndUnit" + counter.toString()).attr("id","PriceAndUnit" + Number(counter-1));
		$("#unittb" + counter.toString()).attr("name","unittb" + Number(counter-1));
		$("#unittb" + counter.toString()).attr("id","unittb" + Number(counter-1));
		$("#pricetb" + counter.toString()).attr("name","pricetb" + Number(counter-1));
		$("#pricetb" + counter.toString()).attr("id","pricetb" + Number(counter-1));
		$("#delete" + counter.toString()).attr("id","delete" + Number(counter-1));
		counter++;
	}
});
</script>
</head>
<body>
<div id="container">
	<div id="header">
		Admin | <a href="logout.php">Logout</a>
	</div>
	
	<div id="main">
		<img src="images/maintitle.png" width=30% height=auto>
		<br />
		<div id="Announcements">
		<a href="admin_index.php"><input type='button' value='Back' class='formbutton'></a><br />
		<?php if(isset($ProductForm)) echo $ProductForm;?>
		</div>
	</div>
</div>
</body>
</html>