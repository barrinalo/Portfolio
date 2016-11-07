var signupapp = angular.module("signupapp",[]);
signupapp.controller("signupctrl", function($scope, $http, $window) {
	$scope.usernameregex = /^[a-zA-Z0-9_-]+$/i;
	$scope.emailregex = /^[^@\0\n\f\r\v\t ]+@[^@.\0\n\f\r\v\t ]+([.][^@.\0\n\f\r\v\t ]+)+$/i;
	$scope.processingform = false;
	$scope.registersuccess = false;
	$scope.registerfail = false;
	$scope.RegisterNewUser = function(isValid) {
		if(isValid) {
			$scope.processingform = true;
			$http.post("Scripts/signupapp.php",{SignupUsername : $scope.SignupUsername, SignupEmail : $scope.SignupEmail, SignupPassword : $scope.SignupPassword}).then(function(response) {
				$scope.processingform = false;
				$scope.registersuccess = response.data;
				$scope.registerfail = !response.data;
			}, function(error, status) {
				$scope.processingform = false;
				$scope.registersuccess = response.data;
				$scope.registerfail = !response.data;
			});
		}
		else {
			$scope.SignupForm.SignupUsername.$setDirty();
			$scope.SignupForm.SignupEmail.$setDirty();
			$scope.SignupForm.SignupPassword.$setDirty();
			$scope.SignupForm.SignupPasswordConfirm.$setDirty();
		}
	}
	$scope.processinglogin = false;
	$scope.loginsuccess = false;
	$scope.loginfail = false;
	$scope.Login = function(IsValid) {
		if(IsValid) {
			$scope.processinglogin = true;
			$http.post("Scripts/loginapp.php",{LoginUsername : $scope.LoginUsername, LoginPassword : $scope.LoginPassword}).then(function(response){
				$scope.processinglogin = false;
				$scope.loginsuccess = response.data;
				$scope.loginfail = !response.data;
				if(response.data) $window.location.href = "main.php";
			}, function(error, status) {
				$scope.processinglogin = false;
				$scope.loginsuccess = response.data;
				$scope.loginfail = !response.data;
			});
		}
		else {
			$scope.LoginForm.LoginUsername.$setDirty();
			$scope.LoginForm.LoginPassword.$setDirty();
		}
	}
	$scope.ResetPassword = function() {
		$http.post("Scripts/signupapp.php", {ResetPassword : $scope.ForgottenEmail}).then(function(response) {$scope.PasswordResetResult = response.data});
	}
});
signupapp.directive("sameAs", function() {
	return {
		require: "ngModel",
		link: function(scope, element, attrs, ngModel) {
			ngModel.$validators.sameAs = function(value) {
				return value === $("#" + attrs.sameAs).val();
			};
			scope.$watch(attrs.sameAs, function() {
				ngModel.$validate();
			});
		}
	}
});
signupapp.directive("usernameAvailable", function($q, $http) {
	return {
		require: "ngModel",
		link: function(scope, element, attrs, ngModel) {
			ngModel.$asyncValidators.usernameAvailable = function(username) {
				var deferred = $q.defer();
				$http.post("Scripts/signupapp.php", {SignupUsername : username}).then(function(response) {
					if(response.data) deferred.reject();
					else deferred.resolve();
				}, function(error, status) {deferred.reject();});
				return deferred.promise;
			};
		}
	}
});

signupapp.directive("emailAvailable", function($q, $http) {
	return {
		require: "ngModel",
		link: function(scope, element, attrs, ngModel) {
			ngModel.$asyncValidators.emailAvailable = function(email) {
				var deferred = $q.defer();
				$http.post("Scripts/signupapp.php", {SignupEmail : email}).then(function(response){
					if(response.data) deferred.reject();
					else deferred.resolve();
				}, function(error, status) {deferred.reject();});
				return deferred.promise;
			};
		}
	}
});
signupapp.directive("emailExists", function($q, $http) {
	return {
		require: "ngModel",
		link: function(scope, element, attrs, ngModel) {
			ngModel.$asyncValidators.emailExists = function(email) {
				var deferred = $q.defer();
				$http.post("Scripts/signupapp.php", {EmailExists : email}).then(function(response){
					if(response.data) deferred.reject();
					else deferred.resolve();
				}, function(error, status) {deferred.reject();});
				return deferred.promise;
			};
		}
	}
});
