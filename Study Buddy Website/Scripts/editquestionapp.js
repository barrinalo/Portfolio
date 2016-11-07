var editquestionapp = angular.module("editquestionapp",[]);
editquestionapp.controller("editquestionctrl", function($scope, $http, $window) {
	$scope.ManualAnswerRegex = /(((?={(@*[^{}@]+[|]{2})+@*[^{}@|]+})(?!{[^{}@|]+}))|(?={@[^{}@]+}))/i;
	$scope.FilterRegex = /(?=^$)|(?=(#[^#\s]+)+$)/i;
	$scope.formData = {};
	$scope.FormFeedbackFail = "";
	//Form status stuff
	$scope.ProcessingForm = false;
	$scope.QuestionSaveSuccess = false;
	$scope.QuestionSaveFail = false;
	//Manual stuff
	$scope.ManualOptionID = 0;
	//Table stuff
	$scope.TableEntries = [];
	$scope.TableAspects = [];
	$scope.TopicIDCounter = 0;
	$scope.CurAspect = "";
	//Logical Stuff
	$scope.LogicalGates = [];
	//Graph Stuff
	$scope.GraphEntries = [];
	$scope.NewRelationshipFeedback = "";
	$scope.NewNodeFeedback = "";
	$scope.GraphNodes = [];
	//Equation Stuff
	$scope.EquationVariables = [];
	$scope.EquationStem = "";
	$scope.EquationExplanation = "";
	$scope.NewVariableFeedback = "";

	$scope.AddVariable = function() {
		if($.trim($scope.NewVariable) == "" || $scope.NewVariable === undefined) {
			$scope.NewVariableFeedback = "New Variable must have a name";
			return false;
		}
		if(/\s/gi.test($scope.NewVariable)) {
			$scope.NewVariableFeedback = "New Variable name cannot have whitespace";
			return false;
		}
		for(var i = 0; i < $scope.EquationVariables.length; i++) {
			if($.trim($scope.EquationVariables[i].Topic.toLowerCase()) == $.trim($scope.NewVariable.toLowerCase())) {
				$scope.NewVariableFeedback = "Variable with this name already exists";
				return false;
			}
		}
		$scope.NewVariableFeedback = "";
		$scope.EquationVariables.push({ID : 0, Topic : $.trim($scope.NewVariable), TableEntry : "", Stem : "", Explanation : "", Aspect : ""});
	}
	$scope.RemoveVariable = function(index) {
		$scope.EquationVariables.splice(index,1);
	}
	$scope.DrawGraph = function() {
		var canvas = document.getElementById("GraphCanvas");
		canvas.width = canvas.width;
		var ctx = canvas.getContext("2d");
		ctx.clearRect(0,0,canvas.width,canvas.height);
		var FontSize = 15;
		ctx.font = parseInt(FontSize) + "pt Arial";
		$scope.DrawNode($scope.CurrentNode, canvas.width/2, canvas.height/2, ctx, FontSize);
		var Relations = [];
		for(var i = 0; i < $scope.GraphEntries.length; i++) {
			if($scope.CurrentNode == $scope.GraphEntries[i].Node1 || $scope.CurrentNode == $scope.GraphEntries[i].Node2) {
				var bidir = false;
				for(var j = 0; j < Relations.length; j++) {
					if(Relations[j].Node1 == $scope.GraphEntries[i].Node2 && Relations[j].Node2 == $scope.GraphEntries[i].Node1 && Relations[j].Relationship == $scope.GraphEntries[i].Relationship) {
						Relations[j].Bidirectional = true;
						bidir = true;
					}
				}
				if(!bidir) Relations.push({Node1 : $scope.GraphEntries[i].Node1, Node2 : $scope.GraphEntries[i].Node2, Bidirectional : false, Relationship : $scope.GraphEntries[i].Relationship});
			}
		}
		var count = 0;
		for(var key in Relations) if(Relations.hasOwnProperty(key)) count++;
		var rotateamount = Math.PI * 2 / count;
		var count2 = 0;
		for(var key in Relations) {
			if(Relations.hasOwnProperty(key)) {
				var xpos = 300 * Math.cos(rotateamount * count2 + Math.PI/2) + canvas.width/2;
				var ypos = 300 * Math.sin(rotateamount * count2 + Math.PI/2) + 400;
				if($scope.CurrentNode == Relations[key].Node1) $scope.DrawNode(Relations[key].Node2, xpos, ypos, ctx, FontSize);
				else $scope.DrawNode(Relations[key].Node1, xpos, ypos, ctx, FontSize);
				if(Relations[key].Bidirectional) $scope.DrawRelationship(canvas.width/2, canvas.height/2, xpos, ypos, ctx, Relations[key].Relationship, true, FontSize);
				else {
					if($scope.CurrentNode == Relations[key].Node1) $scope.DrawRelationship(canvas.width/2, canvas.height/2, xpos, ypos, ctx, Relations[key].Relationship, false, FontSize);
					else $scope.DrawRelationship(xpos, ypos, canvas.width/2, canvas.height/2, ctx, Relations[key].Relationship, false, FontSize);
				}
				count2++;
			}
		}
	}
	$scope.DrawNode = function(Text, xpos, ypos, ctx, FontSize) {
		var dim = ctx.measureText(Text);
		ctx.fillText(Text, xpos - dim.width/2, ypos + FontSize/2);
	}
	$scope.DrawRelationship = function(x1,y1,x2,y2, ctx, Relationship, Bidirectional, FontSize) {
		var startRadians=Math.atan((y2-y1)/(x2-x1));
		startRadians+=((x2>=x1)?-90:90)*Math.PI/180;
		var endRadians=Math.atan((y2-y1)/(x2-x1));
		endRadians+=((x2>=x1)?90:-90)*Math.PI/180;
		var linelength = Math.sqrt(Math.pow(x2-x1,2) + Math.pow(y2-y1,2));

		ctx.moveTo(x1,y1);
		ctx.lineTo(x2,y2);
		ctx.stroke();
		
		$scope.DrawNode(Relationship, (x1 + x2)/2, (y1 + y2)/2, ctx, FontSize);

		ctx.save();
    ctx.beginPath();
    ctx.translate(x2,y2);
    ctx.rotate(endRadians);
    ctx.moveTo(0,0);
    ctx.lineTo(5,20);
    ctx.lineTo(-5,20);
    ctx.closePath();
    ctx.restore();
    ctx.fill();

		if(Bidirectional) {
			ctx.save();
    	ctx.beginPath();
    	ctx.translate(x1,y1);
    	ctx.rotate(startRadians);
    	ctx.moveTo(0,0);
    	ctx.lineTo(5,20);
    	ctx.lineTo(-5,20);
    	ctx.closePath();
    	ctx.restore();
			ctx.fill();
		}
	}
	$scope.RemoveNode = function() {
		for(var i = 0; i < $scope.GraphNodes.length; i++) {
			if($scope.GraphNodes[i] == $scope.CurrentNode) {
				for(var j = 0; j < $scope.GraphEntries.length; j++) {
					if($scope.GraphEntries[j].Node1 == $scope.CurrentNode || $scope.GraphEntries[j].Node2 == $scope.CurrentNode) {
						$scope.GraphEntries.splice(j,1);
						j--;
					}
				}
				$scope.GraphNodes.splice(i,1);
				break;
			}
		}
	}
	$scope.AddNode = function() {
		if($scope.NewNode !== undefined && $.trim($scope.NewNode) != "") {
			for(var i = 0; i < $scope.GraphNodes.length; i++) {
				if($scope.GraphNodes[i].toLowerCase() == $.trim($scope.NewNode.toLowerCase())) {
					$scope.NewNodeFeedback = "Node with this name already exists";
					return false;
				}
			}
			$scope.GraphNodes.push($.trim($scope.NewNode));
			$scope.CurrentNode = $.trim($scope.NewNode);
			$scope.GraphNodes.sort();
			$scope.NewNodeFeedback = "";
		}
		else $scope.NewNodeFeedback = "Node cannot be undefined";
	}
	$scope.ShowRelationship = function(GraphEntry) {
		if($scope.CurrentNode == GraphEntry.Node1 || $scope.CurrentNode == GraphEntry.Node2) return true;
		else return false;
	}
	$scope.AddRelationship = function() {
		if($scope.NewRelationship !== undefined && $.trim($scope.NewRelationship) !== "") {
			for(var i = 0; i < $scope.GraphEntries.length; i++) {
				if($scope.GraphEntries[i].Node1 == $scope.NewNode1 && $scope.GraphEntries[i].Relationship.toLowerCase() == $.trim($scope.NewRelationship.toLowerCase()) && $scope.GraphEntries[i].Node2 == $scope.NewNode2) {
					$scope.NewRelationshipFeedback = "Relationship already exists";
					return false;
				}
			}
			$scope.GraphEntries.push({ID : 0, Node1 : $scope.NewNode1, Relationship : $.trim($scope.NewRelationship), Node2 : $scope.NewNode2});
			$scope.CurrentNode = $scope.NewNode1;
			$scope.NewRelationshipFeedback = "";
		}
		else $scope.NewRelationshipFeedback = "Please fill in Relationship";
	}
	$scope.AddBidirectionalRelationship = function () {
		var node1tonode2 = false;
		var node2tonode1 = false;
		if($scope.NewRelationship !== undefined && $.trim($scope.NewRelationship) !== "") {
			for(var i = 0; i < $scope.GraphEntries.length; i++) {
				if($scope.GraphEntries[i].Node1 == $scope.NewNode1 && $scope.GraphEntries[i].Relationship.toLowerCase() == $.trim($scope.NewRelationship.toLowerCase()) && $scope.GraphEntries[i].Node2 == $scope.NewNode2) {
					node1tonode2 = true;
				}
				else if($scope.GraphEntries[i].Node1 == $scope.NewNode2 && $scope.GraphEntries[i].Relationship.toLowerCase() == $.trim($scope.NewRelationship.toLowerCase()) && $scope.GraphEntries[i].Node2 == $scope.NewNode1) {
					node2tonode1 = true;
				}
			}
			if(node1tonode2 && node2tonode1) {
				$scope.NewRelationshipFeedback = "Relationship already exists";
				return false;
			}
			if(!node1tonode2) {
				$scope.GraphEntries.push({ID : 0, Node1 : $scope.NewNode1, Relationship : $.trim($scope.NewRelationship), Node2 : $scope.NewNode2});
				$scope.CurrentNode = $scope.NewNode1;
			}
			if(!node2tonode1) {
				$scope.GraphEntries.push({ID : 0, Node1 : $scope.NewNode2, Relationship : $.trim($scope.NewRelationship), Node2 : $scope.NewNode1});
				$scope.CurrentNode = $scope.NewNode2;
			}
			$scope.NewRelationshipFeedback = "";
		}
		else $scope.NewRelationshipFeedback = "Please fill in Relationship";
	}
	$scope.RemoveRelationship = function(Index) {
		$scope.GraphEntries.splice(Index,1);
	}
	$scope.AddGate = function() {
		for(var i = 0; i < $scope.LogicalGates.length; i++) {
			if($.trim($scope.LogicalGates[i].Stem.toLowerCase()) == $.trim($scope.CreateGate.toLowerCase())) {
				alert("Gate with this name already exists");
				return false;
			}
		}
		if($.trim($scope.CreateGate) == "" || $scope.CreateGate == undefined) {
			alert("Gate name cannot be whitespace or empty");
			return false;
		}
		if(/[(){}\[\]&|!]/i.test($scope.CreateGate)) {
			alert("Gate name cannot contain any form of brackets or logical operators &, |, !");
			return false;
		}
		$scope.LogicalGates.push({ID : 0, QuestionID : $scope.formData.ContentID, Node1 : "", Node2 : "", Stem : $.trim($scope.CreateGate), Relationship : "", EquationError : ""});
	}
	$scope.RemoveGate = function(Index) {
		$scope.LogicalGates.splice(Index,1);
	}
	$scope.EvaluateEquation = function(Expression, Variables) {
		
		
	}
	$scope.CheckBooleanExpression = function(Expression,Index) {
		if($.trim(Expression) == "") {
			$scope.LogicalGates[Index].EquationError = "";
			return true;
		}
		if(/[{}\[\]]/gi.test(Expression)) {
			$scope.LogicalGates[Index].EquationError = "Sorry only () brackets are allowed";
			return false;
		}
		if(/(?=^[\s]*[&])|(?=[&][\s]*$)/gi.test(Expression)) {
			$scope.LogicalGates[Index].EquationError = "Error, missing operands for &";
			return false;
		}
		if(/(?=^[\s]*[|])|(?=[|][\s]*$)/gi.test(Expression)) {
			$scope.LogicalGates[Index].EquationError = "Error, missing operands for |";
			return false;
		}
		if(/[!][\s]*$/gi.test(Expression)) {
			$scope.LogicalGates[Index].EquationError = "Error, missing operand for !";
			return false;
		}
		var bracketstack = [];
		var offset = 0;
		var closebracket = Expression.indexOf(")", offset);
		var openbracket = Expression.indexOf("(", offset);
		while(closebracket != -1 || openbracket != -1) {
			if(closebracket == -1 && (openbracket != -1 || bracketstack.length != 0)) {
				$scope.LogicalGates[Index].EquationError = "Error, missing )";
				return false;
			}
			else if((closebracket < openbracket && bracketstack.length == 0) || (closebracket != -1 && openbracket == -1 && bracketstack.length == 0)) {
				$scope.LogicalGates[Index].EquationError = "Error, ) without (";
				return false;
			}
			else if(closebracket-openbracket == 1) {
				$scope.LogicalGates[Index].EquationError = "Error, empty brackets () detected";
				return false;
			}
			else if(openbracket < closebracket && openbracket != -1) {
				bracketstack.push(openbracket);
				offset = openbracket + 1;
			}
			else if(closebracket < openbracket && closebracket != -1) {
				bracketstack.pop();
				offset = closebracket + 1;
			}
			else if(openbracket < closebracket && openbracket == -1 && bracketstack.length != 0) {
				bracketstack.pop();
				offset = closebracket + 1;
			}
			closebracket = Expression.indexOf(")", offset);
			openbracket = Expression.indexOf("(", offset);
		}
		if(bracketstack.length != 0) {
			$scope.LogicalGates[Index].EquationError = "Error, missing )";
			return false;
		}
		Expression = Expression.replace(/[()&|]/g,"{");
		Expression = Expression.replace(/!/g,"");
		Terms = Expression.split("{");
		for(var i = 0; i < Terms.length; i++) {
			if($.trim(Terms[i].toLowerCase()) != "") {
				for(var j = 0; j < $scope.LogicalGates.length; j++) {
					if($.trim($scope.LogicalGates[j].Stem.toLowerCase()) == $.trim(Terms[i].toLowerCase())) {
						$scope.LogicalGates[Index].EquationError = "";
						return true;
					}
				}
				$scope.LogicalGates[Index].EquationError = "Error, " + Terms[i] + " does not match one of existing logic gates";
				return false;
			}
		}
	}
	$scope.AddAspect = function() {
		if($scope.TableAspects.indexOf($scope.NewAspect) == -1 && /\S/gi.test($scope.NewAspect) && $scope.NewAspect.length) {
			$scope.TableAspects.push($scope.NewAspect);
			var Topics = [];
			for(var i = 0; i < $scope.TableEntries.length; i++) {
				if(Topics.indexOf($scope.TableEntries[i].Topic) == -1) {
					Topics.push($scope.TableEntries[i].Topic);
					$scope.TableEntries.push({ID : 0, Topic : $scope.TableEntries[i].Topic, Aspect : $scope.NewAspect, TableEntry : "", TopicID : $scope.TableEntries[i].TopicID});
				}
			}
			$scope.CurAspect = $scope.NewAspect;
		}
		else $scope.CurAspect = $scope.NewAspect;
	}
	$scope.RemoveAspect = function() {
		for(var i = 0; i < $scope.TableEntries.length; i++) {
			if($scope.TableEntries[i].Aspect == $scope.CurAspect) {
				$scope.TableEntries.splice(i,1);
				i--;
			}
		}
		$scope.TableAspects.splice($scope.TableAspects.indexOf($scope.CurAspect),1);
	}
	$scope.AddTopic = function() {
		for(var i = 0; i < $scope.TableAspects.length; i++) {
			$scope.TableEntries.push({ID : 0, Topic : "", Aspect : $scope.TableAspects[i], TableEntry : "", TopicID : $scope.TopicIDCounter});
		}
		$scope.TopicIDCounter++;
	}
	$scope.RemoveTopic = function(TopicID) {
		for(var i = 0; i < $scope.TableEntries.length; i++) {
			if($scope.TableEntries[i].TopicID == TopicID) {
				$scope.TableEntries.splice(i,1);
				i--;
			}
		}
	}
	$scope.OnTopicChange = function(New, ID) {
		for(var i = 0; i < $scope.TableEntries.length; i++) {
			if($scope.TableEntries[i].TopicID == ID) $scope.TableEntries[i].Topic = New;
		}
	}		
	$scope.GetQuestionData = function() {
		$http.post("Scripts/ManageContent.php", {GetContent : $scope.formData.ContentType, Username : $scope.formData.Username, ContentID : $scope.formData.ContentID}).then(function(response) {
			$scope.formData.Title = response.data.Title;
			$scope.formData.Description = response.data.Description;
			$scope.formData.Filters = response.data.Filters;
			$scope.formData.Permissions = response.data.Permissions;
		});
		$http.post("Scripts/ManageContent.php", {GetChildContent : $scope.formData.ContentType, Username : $scope.formData.Username, ContentID : $scope.formData.ContentID}).then(function(response) {
			if($scope.formData.QuestionType == "Manual") {
				$scope.ManualExplanation = response.data[0].Explanation;
				$scope.ManualStem = response.data[0].Stem;
				$scope.ManualOptionID = response.data[0].ID;
			}
			else if($scope.formData.QuestionType == "Table") {
				$scope.TableEntries = [];
				$scope.TableAspects = [];
				$scope.TopicIDCounter = 0;
				$scope.CurAspect = "";
				for(var i = 0; i < response.data.length; i++) {
					var TableEntry = {};
					TableEntry['ID'] = response.data[i].ID;
					TableEntry['Topic'] = response.data[i].Topic;
					TableEntry['Aspect'] = response.data[i].Aspect;
					TableEntry['TableEntry'] = response.data[i].TableEntry;
					TableEntry['Explanation'] = response.data[i].Explanation;
					TableEntry['TopicID'] = response.data[i].Relationship;
					if($scope.TableAspects.indexOf(TableEntry['Aspect']) == -1) $scope.TableAspects.push(TableEntry['Aspect']);
					$scope.TableEntries.push(TableEntry);
					if($scope.TopicIDCounter < TableEntry['TopicID']) $scope.TopicIDCounter = TableEntry['TopicID'] + 1;
				}
			}
			else if($scope.formData.QuestionType == "Logical") {
				$scope.LogicalGates = [];
				for(var i = 0; i < response.data.length; i++) {
					var Gate = {};
					Gate['ID'] = response.data[i].ID;
					Gate['Stem'] = response.data[i].Stem;
					Gate['Node1'] = response.data[i].Node1;
					Gate['Node2'] = response.data[i].Node2;
					Gate['Relationship'] = response.data[i].Relationship;
					$scope.LogicalGates.push(Gate);
				}
			}
			else if($scope.formData.QuestionType == "Graph") {
				$scope.GraphEntries = [];
				$scope.NewRelationshipFeedback = "";
				$scope.NewNodeFeedback = "";
				$scope.GraphNodes = [];
				for(var i = 0; i < response.data.length; i++) {
					var GraphEntry = {};
					if($scope.GraphNodes.indexOf(response.data[i].Node1) == -1) $scope.GraphNodes.push(response.data[i].Node1);
					if($scope.GraphNodes.indexOf(response.data[i].Node2) == -1) $scope.GraphNodes.push(response.data[i].Node2);
					GraphEntry['ID'] = response.data[i].ID;
					GraphEntry['Node1'] = response.data[i].Node1;
					GraphEntry['Node2'] = response.data[i].Node2;
					GraphEntry['Relationship'] = response.data[i].Relationship;
					GraphEntry['TableEntry'] = response.data[i].TableEntry;
					if(GraphEntry['TableEntry'] == "DisableMultipleLinks") $scope.DisableMultipleLinks = true;
					$scope.GraphEntries.push(GraphEntry);
				}
				$scope.GraphNodes.sort();
			}
			else if($scope.formData.QuestionType == "Equation") {
				$scope.EquationVariables = [];
				$scope.EquationStem = "";
				$scope.EquationExplanation = "";
				$scope.NewVariableFeedback = "";
				for(var i = 0; i < response.data.length; i++) {
					var EquationVariable = {};
					EquationVariable['ID'] = response.data[i].ID;
					EquationVariable['TableEntry'] = response.data[i].TableEntry;
					EquationVariable['Stem'] = response.data[i].Stem;
					EquationVariable['Explanation'] = response.data[i].Explanation;
					EquationVariable['Aspect'] = response.data[i].Aspect;
					EquationVariable['Topic'] = response.data[i].Topic;
					$scope.EquationVariables.push(EquationVariable);
					$scope.EquationStem = response.data[i].Stem;
					$scope.EquationExplanation = response.data[i].Explanation;
				}
			}
		});
	}
	$scope.SubmitQuestionForm = function() {
		$scope.CommonQuestionForm.$setSubmitted();
		if($scope.CommonQuestionForm.$valid) {
			$scope.ProcessingForm = true;
			var Relations = [];
			if($scope.formData.QuestionType == "Manual") {
				Relations.push({ID : $scope.ManualOptionID, Topic : "", Aspect : "", TableEntry : "", Node1 : "", Node2 : "", Relationship : "", Stem : $scope.ManualStem, Explanation : $scope.ManualExplanation});
			}
			else if($scope.formData.QuestionType == "Table") {
				for(var i = 0; i < $scope.TableEntries.length; i++) {
					var NewEntry = true;
					for(var j = 0; j < Relations.length; j++) {
						if(Relations[j].Topic == $scope.TableEntries[i].Topic && Relations[j].Aspect == $scope.TableEntries[i].Aspect) {
							Relations[j].TableEntry += $scope.TableEntries[i].TableEntry;
							NewEntry = false;
							break;
						}
					}
					if(NewEntry) {
						Relations.push({ID : $scope.TableEntries[i].ID, Topic : $scope.TableEntries[i].Topic, Aspect : $scope.TableEntries[i].Aspect, TableEntry : $scope.TableEntries[i].TableEntry, Node1 : "", Node2 : "", Relationship : $scope.TableEntries[i].TopicID, Stem : "", Explanation : $scope.TableEntries[i].Explanation});
					}
				}
			}
			else if($scope.formData.QuestionType == "Graph") {
				for(var i = 0; i < $scope.GraphEntries.length; i++) {
					if($scope.DisableMultipleLinks) Relations.push({ID : $scope.GraphEntries[i].ID, Topic : "", Aspect : "", TableEntry : "DisableMultipleLinks", Node1 : $scope.GraphEntries[i].Node1, Node2 : $scope.GraphEntries[i].Node2, Relationship : $scope.GraphEntries[i].Relationship, Stem : "", Explanation : ""});
					else Relations.push({ID : $scope.GraphEntries[i].ID, Topic : "", Aspect : "", TableEntry : "AllowMultipleLinks", Node1 : $scope.GraphEntries[i].Node1, Node2 : $scope.GraphEntries[i].Node2, Relationship : $scope.GraphEntries[i].Relationship, Stem : "", Explanation : ""});
				}
			}
			if($scope.formData.QuestionType == "Logical") {
				var FormValid = true;
				for(var i = 0; i < $scope.LogicalGates.length; i++) {
					if($scope.LogicalGates[i].Node1 == undefined || $.trim($scope.LogicalGates[i].Node1) == "") {
						$scope.ProcessingForm = false;
						$scope.FormFeedbackFail = "True Terms cannot be left blank or white space";
						return false;
					}
					if($scope.LogicalGates[i].Node2 == undefined || $.trim($scope.LogicalGates[i].Node2) == "") {
						$scope.ProcessingForm = false;
						$scope.FormFeedbackFail = "False Terms cannot be left blank or white space";
						return false;
					}
					if(!$scope.CheckBooleanExpression($scope.LogicalGates[i].Relationship, i)) {
						$scope.ProcessingForm = false;
						$scope.FormFeedbackFail = "Error in boolean expression";
						FormValid = false;
					}
					Relations.push({ID : $scope.LogicalGates[i].ID, Topic : "", Aspect : "", TableEntry : "", Node1 : $scope.LogicalGates[i].Node1, Node2 : $scope.LogicalGates[i].Node2, Relationship : $scope.LogicalGates[i].Relationship, Stem : $scope.LogicalGates[i].Stem, Explanation : ""});
				}
				if(!FormValid) return false;
			}
			else if($scope.formData.QuestionType == "Equation") {
				var FormValid = true;
				
				for(var i = 0; i < $scope.EquationVariables.length; i++) {
					Relations.push({ID : $scope.EquationVariables[i].ID, Topic : $scope.EquationVariables[i].Topic, Aspect : $scope.EquationVariables[i].Aspect, TableEntry : $scope.EquationVariables[i].TableEntry, Node1 : "", Node2 : "", Relationship : "", Stem : $scope.EquationStem, Explanation : $scope.EquationExplanation});
				}
				if(!FormValid) return false;
			}
			$scope.formData['Relations'] = Relations;
			$scope.formData['WriteContent'] = true;
			$http.post("Scripts/ManageContent.php", $scope.formData).then(function(response) {
				if(response.data > 0) {
					$scope.formData.ContentID = response.data;
					$scope.GetQuestionData();
				}
				$scope.ProcessingForm = false;
				$scope.QuestionSaveSuccess = response.data;
				$scope.QuestionSaveFail = !response.data;
				$scope.FormFeedbackFail = "";
			});
		}
	}
	$scope.$watch('formData.ContentType', $scope.GetQuestionData);
});

editquestionapp.directive('stemValid', function() {
	return {
		require: 'ngModel',
		link: function(scope, elm, attr, ngModel) {
			ngModel.$validators.stemValid = function(value) {
				if(scope.formData.QuestionType == 'Manual' && scope.ManualAnswerRegex.test(value)) return true;
				else if(scope.formData.QuestionType != 'Manual') return true;
				else return false;
			}
		}
	}
});
editquestionapp.directive('ckEditor', function () {
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
