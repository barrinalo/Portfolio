<?php
if(isset($_POST["Filter"])) {
	require_once("dbhelper.php");
	$con = dbconnect($HOST,$USERNAME,$PASSWORD,$DBNAME);
	if($con !== -1) {
		$Output = "<table border = 0>";
		$ColCounter = 0;
		if($_POST["Filter"] == "All") {
			$ProductQuery = mysqli_query($con,"SELECT * FROM $PRODUCTS_TABLE ORDER BY Category ASC");
		}
		else {
			$Filter = mysqli_real_escape_string($con,$_POST["Filter"]);
			$ProductQuery = mysqli_query($con, "SELECT * FROM $PRODUCTS_TABLE WHERE Category='$Filter'");
		}
		
		
		$ChangeCategory = "";
		while($row = mysqli_fetch_array($ProductQuery)) {
			$ProductCategory = $row["Category"];
			if($ChangeCategory != $ProductCategory) {
				if($ChangeCategory != "") {
					while($ColCounter != 4) {
						$Output .="<td></td>";
						$ColCounter++;
					}
					$ColCounter = 0;
				}
				$Output .= "<tr><th colspan='4' style='text-align: left;'><h1>$ProductCategory</h1></th></tr><tr>";
				$ChangeCategory = $ProductCategory;
			}
			$ProductName = $row["Name"];
			$ProductImage = $row["Picture_Name"];
			$ProductID = $row["Item_ID"];
			if(isset($_POST["LoggedIn"])) {
			
			}
			else {
				$Output .= "<td width='25%'><center><img src='images/$ProductImage' width='80%' height='80%' class='FullProductPage' id='$ProductID' style='cursor: pointer;'><br />$ProductName</center></td>";
				$ColCounter++;
				if($ColCounter == 4) {
					$Output.= "</tr><tr>";
					$ColCounter = 0;
				}
			}
		}
			
		while($ColCounter != 4) {
			$Output .="<td></td>";
			$ColCounter++;
		}
		$Output.= "</tr></table>";
		echo $Output;
	}
}
?>