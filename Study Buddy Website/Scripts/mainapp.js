var mainapp = angular.module("mainapp",['ngSanitize']);
mainapp.controller("mainctrl", function($scope, $http, $window) {
	$scope.CurrentDisplay = "Home";
	$scope.ContentManager = {};
	$scope.ContentManager['Question'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0};
	$scope.ContentManager['Note'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0};
	$scope.ContentManager['Package'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0};
	$scope.ContentManager['SubscriptionsNote'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0};
	$scope.ContentManager['SubscriptionsPackage'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0};
	$scope.ContentManager['AvailableSubscriptionsNote'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0};
	$scope.ContentManager['AvailableSubscriptionsPackage'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0};

	$scope.GetSubscriptions = function(ContentType) {
		var CreatorLike = "";
		if(ContentType == "SubscriptionsNote") CreatorLike = $scope.SubscriptionsNoteCreator;
		else if(ContentType == "SubscriptionsPackage") CreatorLike = $scope.SubscriptionsPackageCreator;
		$http.post("Scripts/ManageContent.php", {GetSubscriptions : ContentType.substr(13,ContentType.length), Username : $("#Username").html(), Offset : $scope.ContentManager[ContentType].Offset, Limit : $scope.ContentManager[ContentType].Limit, TitleFilter : $scope.ContentManager[ContentType].TitleFilter, FiltersFilter : $scope.ContentManager[ContentType].FiltersFilter, DescriptionFilter : $scope.ContentManager[ContentType].DescriptionFilter, Creator : CreatorLike}).then(
			function(response) {
				if(response.data) {
					$scope.ContentManager[ContentType].Content = response.data.data;
					$scope.ContentManager[ContentType].TotalRows = response.data.RowCount;
				}
		});
	}
	$scope.GetNonPersonalContent = function(ContentType) {
		var CreatorLike = "";
		if(ContentType == "AvailableSubscriptionsNote") CreatorLike = $scope.NoteCreator;
		else if(ContentType == "AvailableSubscriptionsPackage") CreatorLike = $scope.PackageCreator;
		$http.post("Scripts/ManageContent.php", {GetNonPersonalContent : ContentType.substr(22,ContentType.length), Username : $("#Username").html(), Offset : $scope.ContentManager[ContentType].Offset, Limit : $scope.ContentManager[ContentType].Limit, TitleFilter : $scope.ContentManager[ContentType].TitleFilter, FiltersFilter : $scope.ContentManager[ContentType].FiltersFilter, DescriptionFilter : $scope.ContentManager[ContentType].DescriptionFilter, Creator : CreatorLike}).then(
			function(response) {
				if(response.data) {
					$scope.ContentManager[ContentType].Content = response.data.data;
					$scope.ContentManager[ContentType].TotalRows = response.data.RowCount;
				}
		});
	}
	$scope.GetPersonalContent = function(ContentType) {
		$http.post("Scripts/ManageContent.php", {GetPersonalContent : ContentType, Username : $("#Username").html(), Offset : $scope.ContentManager[ContentType].Offset, Limit : $scope.ContentManager[ContentType].Limit, TitleFilter : $scope.ContentManager[ContentType].TitleFilter, FiltersFilter : $scope.ContentManager[ContentType].FiltersFilter, DescriptionFilter : $scope.ContentManager[ContentType].DescriptionFilter}).then(
			function(response) {
				if(response.data) {
					$scope.ContentManager[ContentType].Content = response.data.data;
					$scope.ContentManager[ContentType].TotalRows = response.data.RowCount;
				}
		});
	}
	$scope.HasNextContent = function(ContentType) {
		return (($scope.ContentManager[ContentType].Offset + $scope.ContentManager[ContentType].Limit) < $scope.ContentManager[ContentType].TotalRows);
	}
	$scope.HasPrevContent = function(ContentType) {
		return (($scope.ContentManager[ContentType].Offset - $scope.ContentManager[ContentType].Limit) >= 0);
	}
	$scope.NextContent = function(ContentType) {
		if($scope.HasNextContent(ContentType)) {
			$scope.ContentManager[ContentType].Offset += $scope.ContentManager[ContentType].Limit;
			if(ContentType.indexOf("Available") > -1) $scope.GetNonPersonalContent(ContentType);
			else if(ContentType.indexOf("Subscriptions") > -1) $scope.GetSubscriptions(ContentType);
			else $scope.GetPersonalContent(ContentType);
		}
	}
	$scope.PrevContent = function(ContentType) {
		if($scope.HasPrevContent(ContentType)) {
			$scope.ContentManager[ContentType].Offset -= $scope.ContentManager[ContentType].Limit;
			if(ContentType.indexOf("Available") > -1) $scope.GetNonPersonalContent(ContentType);
			else if(ContentType.indexOf("Subscriptions") > -1) $scope.GetSubscriptions(ContentType);
			else $scope.GetPersonalContent(ContentType);
		}
	}
	$scope.DeletePersonalContent = function(ContentType, ContentID) {
		$http.post("Scripts/ManageContent.php", {DeleteContent : ContentType, Username : $("#Username").html(), ContentID : ContentID}).then(
			function(response) {
				if(!response.data) alert("error");
				else {
					if(ContentType.indexOf("Available") > -1) $scope.GetNonPersonalContent(ContentType);
					else if(ContentType.indexOf("Subscriptions") > -1) $scope.GetSubscriptions(ContentType);
					else $scope.GetPersonalContent(response.data);
				}
		});
	}
	$scope.Subscribed = function(entry, ContentType) {
		for(var key in $scope.ContentManager[ContentType].Content) {
			if($scope.ContentManager[ContentType].Content.hasOwnProperty(key)) {
				if($scope.ContentManager[ContentType].Content[key].ID == entry.ID) return true;
			}
		}
		return false;
	}
	$scope.Subscribe = function(ID, ContentType) {
		$http.post("Scripts/ManageContent.php", {Subscribe : true, ContentID : ID, Username : $("#Username").html()}).then(
			function(response) {
				$scope.GetSubscriptions(ContentType);
			}
		);
	}
	$scope.Unsubscribe = function(ID, ContentType) {
		$http.post("Scripts/ManageContent.php", {Unsubscribe : true, ContentID : ID, Username : $("#Username").html()}).then(
			function(response) {
				$scope.GetSubscriptions(ContentType);
			}
		);
	}
	$scope.UpdatePassword = function() {
		if($scope.ChangePasswordForm.$valid) {
			$http.post("Scripts/signupapp.php", {ChangePassword : true, OldPassword : $scope.OldPassword, NewPassword : $scope.ChangePassword, Username : $("#Username").html()}).then(function(response) {$scope.ChangePasswordResult = response.data});
		}
	}
	$scope.GetPersonalContent("Question");
	$scope.GetPersonalContent("Note");
	$scope.GetPersonalContent("Package");
	$scope.GetNonPersonalContent("AvailableSubscriptionsNote");
	$scope.GetNonPersonalContent("AvailableSubscriptionsPackage");
	$scope.GetSubscriptions("SubscriptionsNote");
	$scope.GetSubscriptions("SubscriptionsPackage");
});

mainapp.directive("sameAs", function() {
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
