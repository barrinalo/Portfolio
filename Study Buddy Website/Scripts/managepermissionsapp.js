var managepermissionsapp = angular.module("managepermissionsapp",[]);
managepermissionsapp.controller("managepermissionsctrl", function($scope, $http, $window) {
	$scope.PermittedUsers = [];
	$scope.PotentialUsers = [];
	$scope.Pending = false;
	$scope.SearchUsers = function() {
		if($scope.UserSearch != "") {
			$http.post("Scripts/ManageContent.php", {SearchUsers : true, SearchText : $scope.UserSearch}).then(function(response) {
				$scope.PotentialUsers = response.data;
			});
		}
	}
	$scope.IsPermitted = function(Username) {
		for(var i = 0; i < $scope.PermittedUsers.length; i++) if($scope.PermittedUsers[i].Username == Username) return true;
		return false;
	}
	$scope.Add = function(Username) {
		if(!$scope.IsPermitted(Username)) {
			$scope.PermittedUsers.push({Username : Username, ID : 0, ContentID : $scope.ContentID});
		}
	}
	$scope.Remove = function(Username) {
		for(var i = 0; i < $scope.PermittedUsers.length; i++) {
			if($scope.PermittedUsers[i].Username == Username) {
				$scope.PermittedUsers.splice(i,1);
				break;
			}
		}
	}
	$scope.WritePermissions = function() {
		$scope.Pending = true;
		$http.post("Scripts/ManageContent.php", {WritePermissions : true, Username : $("#Username").html(), ContentID : $scope.ContentID, UserList : $scope.PermittedUsers}).then(function(response) {
			$scope.Success = response.data;
			$scope.Pending = false;
		});
	}
	$scope.$watch('ContentID', function() {
		$http.post("Scripts/ManageContent.php", {GetPermittedUsers : true, Username : $("#Username").html(), ContentID : $scope.ContentID}).then(function(response) {
			$scope.PermittedUsers = response.data;
		});
	});
});
