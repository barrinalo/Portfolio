<?php
session_start();
if(isset($_SESSION["User"])) $loggedin = "Logged in as " . $_SESSION["User"] . ", <a href='logout.php'>Logout</a>";
else $loggedin = "<a href='login.php'>Login</a>";
?>
<!DOCTYPE html>
<html>
<head>
<title>Soft Spot - Fresh Bakes Daily</title>
<link rel="shortcut icon" href="favicon.ico">
<link rel="stylesheet" type="text/css" href="site-css/site-theme.css">
</head>
<body>
<img src="images/maintitle.png" style="max-width:30%;height:auto;"><br>
<center>
<table border=0px width=75%>
<tr>
<td class="nav-bar"><a href="index.php">Home</a></td>
<td class="nav-bar"><a href="products.php">Our Products</a></td>
<td class="nav-bar"><a href="order.php">Place an Order</a></td>
<td class="nav-bar"><a href="track.php">Track an Order</a></td>
<td class="nav-bar"><a href="http://www.softspotconfectionary.com/about.php">About</a></td>
</tr>
<tr>
<td colspan=5 class="main-content">
<br>
Contact: 9793 9109<br>
Email: soft.spot.confectionary@gmail.com (Preferred)<br>
Location: 36 Beo Crescent #01-43 S160036<br>
<iframe
  width=50%
  height=320vh
  frameborder="0" style="border:0"
  src="https://www.google.com/maps/embed/v1/place?key=AIzaSyDMCTIuya_tdNitgF2clEELg18Or1Hmgs0
    &q=36+Beo+Crescent+Singapore">
</iframe>
</td>
</tr>
<tr>
<td class="footer" colspan=5>
<?php if(isset($loggedin)) echo $loggedin;?>
</td>
</tr>
</table>
</center>
</body>
</html>