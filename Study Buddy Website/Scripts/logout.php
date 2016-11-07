<?php
	require_once("../../includes/dbhandler.php");
	$SecureSession = new SecureSessionHandler();
	session_set_save_handler($SecureSession, true);
	$SecureSession->start();
	$SecureSession->forget();
	header("Location: /index.php");
?>
