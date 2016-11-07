var editnonquestioncontentapp = angular.module("editnonquestioncontentapp",[]);
editnonquestioncontentapp.controller("editnonquestioncontentctrl", function($scope, $http, $window) {
	$scope.FilterRegex = /(?=^$)|(?=(#[^#\s]+)+$)/i;
	$scope.formData = {};
	$scope.ProcessingForm = false;
	$scope.ContentSaveSuccess = false;
	$scope.ContentSaveFail = false;
	$scope.ContentManager = {};
	$scope.AttachedContentType = "";
	$scope.ContentManager['Question'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0, AttachedContent : [], AttachedContentSize : 0};
	$scope.ContentManager['Note'] = {Content : {}, TitleFilter : "", FiltersFilter : "", DescriptionFilter : "", Offset : 0, Limit : 20, TotalRows : 0, AttachedContent : [], AttachedContentSize : 0};
	$scope.GetChildContent = function(ContentType) {
		$http.post("Scripts/ManageContent.php", {GetChildContent : $scope.formData.ContentType, Username : $scope.formData.Username, ContentID : $scope.formData.ContentID}).then(
			function(response) {
				if(response.data) {
					$scope.ContentManager[ContentType].AttachedContent = response.data;
					for(var obj in $scope.ContentManager[ContentType].AttachedContent) {
						if($scope.ContentManager[ContentType].AttachedContent.hasOwnProperty(obj)) $scope.ContentManager[ContentType].AttachedContentSize++;
					}
				}
			}
		);
	}
	$scope.GetReadableContent = function(ContentType) {
		$http.post("Scripts/ManageContent.php", {GetReadableContent : ContentType, Username : $scope.formData.Username, Offset : $scope.ContentManager[ContentType].Offset, Limit : $scope.ContentManager[ContentType].Limit, TitleFilter : $scope.ContentManager[ContentType].TitleFilter, FiltersFilter : $scope.ContentManager[ContentType].FiltersFilter, DescriptionFilter : $scope.ContentManager[ContentType].DescriptionFilter, Creator : $scope.ContentCreator}).then(
			function(response) {
				if(response.data) {
					$scope.ContentManager[ContentType].Content = response.data.data;
					$scope.ContentManager[ContentType].TotalRows = response.data.RowCount;
				}
		});
	}
	$scope.IsAttached = function(ID, ContentType) {
		for(var i = 0; i < $scope.ContentManager[ContentType].AttachedContent.length; i++) {
			if($scope.ContentManager[ContentType].AttachedContent[i].ID == ID) return true;
		}
		return false;
	}
	$scope.NotFirst = function(entry, ContentType) {
		if($scope.ContentManager[ContentType].AttachedContentSize > 0) {
			if($scope.ContentManager[ContentType].AttachedContent[0].ID == entry.ID) return false;
			else return true;
		}
		else return true;
	}
	$scope.NotLast = function(entry, ContentType) {
		if($scope.ContentManager[ContentType].AttachedContentSize > 0) {
			if($scope.ContentManager[ContentType].AttachedContent[$scope.ContentManager[ContentType].AttachedContentSize-1].ID == entry.ID) return false;
			else return true;
		}
		else return true;
	}
	$scope.AddToContent = function(entry, ContentType) {
		$scope.ContentManager[ContentType].AttachedContent.push(entry)
		$scope.ContentManager[ContentType].AttachedContentSize += 1;
	}
	$scope.MoveUp = function(entry, ContentType) {
		for(var obj in $scope.ContentManager[ContentType].AttachedContent) {
			if($scope.ContentManager[ContentType].AttachedContent[obj].ID == entry.ID) {
				var index = $scope.ContentManager[ContentType].AttachedContent.indexOf($scope.ContentManager[ContentType].AttachedContent[obj]);
				if(index > 0) {
					var temp = $scope.ContentManager[ContentType].AttachedContent[index-1];
					$scope.ContentManager[ContentType].AttachedContent[index-1] = $scope.ContentManager[ContentType].AttachedContent[index];
					$scope.ContentManager[ContentType].AttachedContent[index] = temp;
					return true;
				}
			}
		}
	}
	$scope.MoveDown = function(entry, ContentType) {
		for(var obj in $scope.ContentManager[ContentType].AttachedContent) {
			if($scope.ContentManager[ContentType].AttachedContent[obj].ID == entry.ID) {
				var index = $scope.ContentManager[ContentType].AttachedContent.indexOf($scope.ContentManager[ContentType].AttachedContent[obj]);
				if(index < $scope.ContentManager[ContentType].AttachedContentSize-1) {
					var temp = $scope.ContentManager[ContentType].AttachedContent[index+1];
					$scope.ContentManager[ContentType].AttachedContent[index+1] = $scope.ContentManager[ContentType].AttachedContent[index];
					$scope.ContentManager[ContentType].AttachedContent[index] = temp;
					return true;
				}
			}
		}
	}
	$scope.RemoveFromContent = function(entry, ContentType) {
		for(var obj in $scope.ContentManager[ContentType].AttachedContent) {
			if($scope.ContentManager[ContentType].AttachedContent[obj].ID == entry.ID) {
				var index = $scope.ContentManager[ContentType].AttachedContent.indexOf($scope.ContentManager[ContentType].AttachedContent[obj]);
				$scope.ContentManager[ContentType].AttachedContent.splice(index,1);
				$scope.ContentManager[ContentType].AttachedContentSize -= 1;
				return true;
			}
		}
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
			$scope.GetReadableContent(ContentType);
		}
	}
	$scope.PrevContent = function(ContentType) {
		if($scope.HasPrevContent(ContentType)) {
			$scope.ContentManager[ContentType].Offset -= $scope.ContentManager[ContentType].Limit;
			$scope.GetReadableContent(ContentType);
		}
	}
	$scope.CopyToClipboard = function(text) {
		window.prompt("Copy and paste this text into your note to insert content", text);
	}
	$scope.SubmitContentForm = function(ContentType) {
		$scope.ContentForm.$setSubmitted();
		if($scope.ContentForm.$valid) {
			var Children = [];
			for(var obj in $scope.ContentManager[ContentType].AttachedContent) {
				if($scope.ContentManager[ContentType].AttachedContent.hasOwnProperty(obj)) {
					var CurChildOrder = $scope.ContentManager[ContentType].AttachedContent.indexOf($scope.ContentManager[ContentType].AttachedContent[obj]) + 1;
					Children.push({ID : $scope.ContentManager[ContentType].AttachedContent[obj].RelationID, ChildID : $scope.ContentManager[ContentType].AttachedContent[obj].ID, ChildOrder : CurChildOrder});
					
				}
			}
			$scope.ProcessingForm = true;
			$scope.formData['Relations'] = Children;
			$scope.formData['WriteContent'] = true;
			$http.post("Scripts/ManageContent.php", $scope.formData).then(function(response) {
				if(response.data > 0) $scope.formData.ContentID = response.data;
				$scope.ProcessingForm = false;
				$scope.ContentSaveSuccess = response.data;
				$scope.ContentSaveFail = !response.data;
			});
		}
	}
	$scope.$watch('formData.ContentType', function() {
		if($scope.formData.ContentType == 'Note') $scope.AttachedContentType = "Question";
		else if($scope.formData.ContentType == 'Package') $scope.AttachedContentType = "Note";
		$scope.GetReadableContent($scope.AttachedContentType);
		$scope.GetChildContent($scope.AttachedContentType);
	});
});

editnonquestioncontentapp.directive('ckEditor', function () {
    return {
        require: 'ngModel',
        link: function (scope, elm, attr, ngModel) {
            var ck = CKEDITOR.replace(elm[0]);
            if (!ngModel) return;
            ck.on('instanceReady', function () {
                ck.setData(ngModel.$viewValue);
            });
            function updateModel() {
                scope.$apply(function () {
                ngModel.$setViewValue(ck.getData());
            });
        }
        ck.on('change', updateModel);
        ck.on('key', updateModel);
        ck.on('dataReady', updateModel);

        ngModel.$render = function (value) {
            ck.setData(ngModel.$viewValue);
        };
    }
};
});
