<?php
session_start();
if(!isset($_SESSION["Logged_In"])) header("Location: admin.php");

require_once("dbhelper.php");
$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
if($con !== -1) {
	if(isset($_POST["AnnouncementID"]) && isset($_POST["DeleteAnnouncement"])) {
		$AnnouncementID = $_POST["AnnouncementID"];
		mysqli_query($con, "DELETE FROM $ANNOUNCEMENT_TABLE WHERE ID=$AnnouncementID");
		unset($AnnouncementID);
	}
	if(isset($_POST["newproduct"])) {
		mysqli_query($con, "INSERT INTO $PRODUCTS_TABLE (Name, Description, Picture_Name, Category, Featured) VALUES ('','','','','No')");
	}
	#Get Product Categories
	$CategoriesQuery = mysqli_query($con, "SELECT DISTINCT Category FROM $PRODUCTS_TABLE");
	if($CategoriesQuery) {
		$productcategories = "<select form='productform' name='productcategory' class='formtextbox'><option value=''></option>";
		while($row = mysqli_fetch_array($CategoriesQuery)) {
			$category = $row["Category"];
			$productcategories .= "<option value='$category'>$category</option>";
		}
		$productcategories .= "</select><br />New Category: <input type='text' class='formtextbox' name='newproductcategory'><br />";
	}
	#Get Past Announcements
	$PastAnnouncementQuery = mysqli_query($con, "SELECT * FROM $ANNOUNCEMENT_TABLE WHERE ID<>1");
	$PastAnnouncements = "<select name='PastAnnouncementID' form='updateform' class='formtextbox'><option value=''></option>";
	while($row = mysqli_fetch_array($PastAnnouncementQuery)) {
		$PastAnnouncementID = $row["ID"];
		$TempPastAnnouncementTitle = $row["Title"];
		$PastAnnouncements .= "<option value='$PastAnnouncementID'> $TempPastAnnouncementTitle </option>";
	}
	$PastAnnouncements .= "</select>";
	#Process forms
	if(isset($_POST["UpdateAbout"])) {
	$UpdateAboutText = mysqli_real_escape_string($con,$_POST["UpdateAboutText"]);
	mysqli_query($con, "UPDATE $ANNOUNCEMENT_TABLE SET Main_Text='$UpdateAboutText' WHERE ID=1");
	}
	if(isset($_POST["PublishAnnouncement"])) {
	$NewAnnouncement = mysqli_real_escape_string($con, $_POST["UpdateMainText"]);
	$NewAnnouncementTitle = mysqli_real_escape_string($con, $_POST["UpdateTitle"]);
	mysqli_query($con, "INSERT INTO $ANNOUNCEMENT_TABLE (Title, Main_Text, Date_Of_Announcement) VALUES ('$NewAnnouncementTitle','$NewAnnouncement',NOW())");
	}
	if(isset($_POST["ViewAnnouncement"])) {
		$AnnouncementID = $_POST["PastAnnouncementID"];
		$PastAnnouncementQuery = mysqli_query($con, "SELECT * FROM $ANNOUNCEMENT_TABLE WHERE ID=$AnnouncementID");
		if($PastAnnouncementQuery) {
			$result = mysqli_fetch_array($PastAnnouncementQuery);
			$PastAnnouncementTitle = $result["Title"];
			$PastAnnouncementText = $result["Main_Text"];
		}
	}
	if(isset($_POST["AnnouncementID"]) && isset($_POST["UpdateAnnouncement"])) {
		$NewAnnouncement = mysqli_real_escape_string($con, $_POST["UpdateMainText"]);
		$NewAnnouncementTitle = mysqli_real_escape_string($con, $_POST["UpdateTitle"]);
		$AnnouncementID = $_POST["AnnouncementID"];
		mysqli_query($con, "UPDATE $ANNOUNCEMENT_TABLE SET Main_Text='$NewAnnouncement', Title='$NewAnnouncementTitle' WHERE ID=$AnnouncementID");
		unset($AnnouncementID);
	}
	
	#Get About Text
	$AboutQuery = mysqli_query($con, "SELECT * FROM $ANNOUNCEMENT_TABLE WHERE Title='About Us'");
	if($AboutQuery) {
		$result = mysqli_fetch_array($AboutQuery);
		$AboutText = $result["Main_Text"];
	}
	#Get Products
	$ProductQuery = mysqli_query($con, "SELECT * FROM $PRODUCTS_TABLE");
	$products = "";
	while($row = mysqli_fetch_array($ProductQuery)) {
		$ProductID = $row["Item_ID"];
		$ProductName = $row["Name"];
		$products .= "<form action='EditProduct.php' method='post'>$ProductName - <input type='hidden' value='$ProductID' name='ProductID'><input type='submit' value='Edit' class='formbutton'></form>";
	}
	
	#Get Orders
	#$OrderQuery = mysqli_query($con, "SELECT * FROM $ORDER_ITEMS
}
mysqli_close($con)
?>

<!DOCTYPE html>
<html>
<head>
<title>Soft Spot - Fresh Bakes Daily</title>
<link rel="shortcut icon" href="favicon.ico">
<link href='http://fonts.googleapis.com/css?family=Courgette' rel='stylesheet' type='text/css'>
<link rel="stylesheet" type="text/css" href="site-css/site-theme.css">
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
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
		Make a new Announcement
		<form action="<?php echo $_SERVER['PHP_SELF'];?>" method="post" id="updateform">
		<input type="text" name="AnnouncementID" style="display: none;" value=<?php if(isset($AnnouncementID)) echo $AnnouncementID;?>>
		Title: <input type="text" name="UpdateTitle" class="formtextbox" value="<?php if(isset($PastAnnouncementTitle)) echo $PastAnnouncementTitle;?>"> Past Announcements: <?php echo $PastAnnouncements;?> <input type="submit" class="formbutton" value="View" name="ViewAnnouncement"><br />
		<textarea form="updateform" class="formtextbox" style="resize: none; width: 60vw; height: 10vh; margin-top: 1vh;" name="UpdateMainText"><?php if(isset($PastAnnouncementText)) echo $PastAnnouncementText;?></textarea><br />
		<input type="submit" class="formbutton" value="Delete" name="DeleteAnnouncement"> 
		<input type="submit" class="formbutton" value="Update Announcement" name="UpdateAnnouncement"> 
		<input type="submit" class="formbutton" value="Publish" name="PublishAnnouncement">
		</form>
		</div>
		<div id="Products">
		Products
		<form action="<?php echo $_SERVER['PHP_SELF'];?>" method="post" id="productform" enctype="multipart/form-data">
		<input type="submit" value="Add New Item" name="newproduct" class="formbutton"><br />
		</form>
		<br />
		<?php if(isset($products)) echo $products;?>
		<br />
		</div>
		<div id="About">
		<form action="<?php echo $_SERVER['PHP_SELF'];?>" method="post" id="aboutform">
		Title: <input type="text" name="About" value="About Us" readonly class="formtextbox"><br /><br />
		<textarea form="aboutform" class="formtextbox" style="resize: none; width: 25vw; height: 10vh;" name="UpdateAboutText"><?php if(isset($AboutText)) echo $AboutText;?></textarea>
		<br />
		<input type="submit" class="formbutton" value="Update" name="UpdateAbout">
		</form>
		</div>
		<div id="Orders" style="clear: both;">
		<?php if(isset($Orders)) echo $Orders;?>
		</div>
	</div>
</div>
</body>
</html>