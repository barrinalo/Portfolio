<?php
	require_once("../../../includes/dbhandler.php");
	$SecureSession = new SecureSessionHandler();
	session_set_save_handler($SecureSession, true);
	$SecureSession->start();
	if(!$SecureSession->isValid(5) || $SecureSession->get("CurrentUser") == null) {
		$SecureSession->forget();
		header("Location: index.php");
	}
	$Dir = "Users/" . $SecureSession->get("CurrentUser");
	$Files = scandir("../../" . $Dir);
	$Output = "";
	foreach($Files as $File) {
		if(!is_dir($File)) {
			$Output .= "<tr><td><img src='../../$Dir/$File' height=200 width=auto></td><td>$File</td><td><button class='btn btn-default' onclick=" . '"' . "ChooseImage('http://studybuddy.x10host.com/$Dir/$File')" . '"' . ">Choose</button></td><td><button class='btn btn-default' onclick=" . '"' . "Delete('$File')" . '"' . ">Delete</button></td></tr>";
		}
	}
?>
<!DOCTYPE html>
<html lang="en">
	<head>
	  <meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
		<link rel="stylesheet" href="site-theme.css">
		<script>
			function getUrlParam( paramName ) {
            var reParam = new RegExp( '(?:[\?&]|&)' + paramName + '=([^&]+)', 'i' );
            var match = window.location.search.match( reParam );

            return ( match && match.length > 1 ) ? match[1] : null;
        }
			var ChooseImage = function(URL) {
				var funcNum = getUrlParam( 'CKEditorFuncNum' );
				window.opener.CKEDITOR.tools.callFunction( funcNum, URL);
				window.close();
			}
			var Delete = function(URL) {
				$.post("Delete.php", {FileToDelete : URL});
				location.reload();
			}
		</script>
		<link rel="stylesheet" href="site-theme.css">
	</head>
	<body>
		<div class='container-fluid'>
			<div class='table-responsive'>
				<table class="table">
					<tr><th>Preview</th><th>File Name</th><th>Choose</th><th>Delete</th></tr>
			<?php echo $Output;?>
				</table>
			</div>
		</div>
	</body>
</html>
