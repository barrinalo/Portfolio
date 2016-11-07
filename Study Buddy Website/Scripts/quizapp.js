var quizapp = angular.module("quizapp",['ngSanitize']);
quizapp.controller("quizctrl", function($scope, $http, $window, $interval) {
	$scope.Math = Math;
	$scope.ManualAnswerRegex = /(((?={(@*[^{}@]+[|]{2})+@*[^{}@|]+})(?!{[^{}@|]+}))|(?={@[^{}@]+}))/i;
	$scope.CurQuestion = {};
	$scope.UserAnswers = {};
	$scope.PastQuestions = [];
	$scope.Submitted = false;
	$scope.FlipCard = false;
	$scope.Review = false;
	$scope.IsTest = false;
	$scope.TimeTaken = 0;
	$scope.TimeInterval;
	$scope.Override = "";
	//Test counter
	$scope.TestCurQuestion = 0;
	$scope.TimeTracker = 0;
	$scope.TestResult = 0;
	$scope.TestSubmitted = false;

	$scope.SubmitTest = function() {
		if($scope.CurQuestion.QuestionStyle != "FlashCard") {
			for(var i = 0; i < $scope.CurQuestion.DisplayOptions.length; i++) {
				$scope.CurQuestion.DisplayOptions[i]['UserAnswer'] = $scope.UserAnswers[$scope.CurQuestion.DisplayOptions[i].ID];
			}
		}
		$scope.Submitted = true;
		$scope.Review = true;
		$interval.cancel($scope.TimeInterval);
		if(!$scope.CurQuestion.TimeTaken) $scope.CurQuestion["TimeTaken"] = $scope.TimeTaken - $scope.TimeTracker;
		else $scope.CurQuestion["TimeTaken"] += $scope.TimeTaken - $scope.TimeTracker;
		for(var i = 0; i < $scope.PastQuestions.length; i++) {
			$scope.PastQuestions[i]['Username'] = $("#Username").html();
			$scope.PastQuestions[i]["TestID"] = 0;
			$scope.PastQuestions[i]["Settings"] = "";
			if($scope.PastQuestions[i].TimeTaken === undefined) $scope.PastQuestions[i]['TimeTaken'] = 0;
			else if($scope.PastQuestions[i].TimeTaken === 0) $scope.PastQuestions[i].TimeTaken = 1;
			if($scope.PastQuestions[i].QuestionStyle != "FlashCard") {
				var Result = 0;
				var count = 0;
				for(var j = 0; j < $scope.PastQuestions[i].DisplayOptions.length; j++) {
					if($scope.PastQuestions[i].DisplayOptions[j].IsAnswer === $scope.PastQuestions[i].DisplayOptions[j].UserAnswer) Result += 1;
					count += 1;
				}
				$scope.PastQuestions[i]['Result'] = Result/count;
			}
		}
	}
	$scope.RecordTest = function() {
		var testready = true;
		for(var i = 0; i < $scope.PastQuestions.length; i++) {
			if($scope.PastQuestions[i].Result === undefined) {
				testready = false;
				break;
			}
		}
		if(testready) {
			var count = 0;
			for(var i = 0; i < $scope.PastQuestions.length; i++) {
				$scope.TestResult += $scope.PastQuestions[i].Result;
				count++;
			}
			$scope.TestResult /= count;
			var TestRecord = {};
			TestRecord['Username'] = $("#Username").html();
			TestRecord['TimeTaken'] = $scope.TimeTaken;
			TestRecord['NoteID'] = $scope.ContentID;
			TestRecord['Result'] = $scope.TestResult;
			$http.post("Scripts/quizapp.php", {RecordTest : true, QuestionRecords : $scope.PastQuestions, TestRecord : TestRecord});
			$scope.TestSubmitted = true;
		}
		else alert("Please self mark all flash card questions");
	}
	$scope.SubmitAnswer = function() {
		$interval.cancel($scope.TimeInterval);
		$scope.CurQuestion['Username'] = $("#Username").html();
		if($scope.TimeTaken > 0) $scope.CurQuestion['TimeTaken'] = $scope.TimeTaken;
		else $scope.CurQuestion['TimeTaken'] = 1;
		$scope.Submitted = true;
		if($scope.CurQuestion.QuestionStyle != "FlashCard") {
			var Result = 0;
			var count = 0;
			for(var i = 0; i < $scope.CurQuestion.DisplayOptions.length; i++) {
				if($scope.CurQuestion.DisplayOptions[i].IsAnswer == $scope.UserAnswers[$scope.CurQuestion.DisplayOptions[i].ID]) Result += 1;
				count++;
			}
			$scope.CurQuestion['Result'] = Result/count;
			for(var i = 0; i < $scope.CurQuestion.DisplayOptions.length; i++) {
				$scope.CurQuestion.DisplayOptions[i]['UserAnswer'] = $scope.UserAnswers[$scope.CurQuestion.DisplayOptions[i].ID];
			}
		}
		$scope.PastQuestions.push($scope.CurQuestion);
		$scope.CurQuestion["TestID"] = 0;
		$scope.CurQuestion["Settings"] = "";
		$http.post("Scripts/quizapp.php", {RecordQuestion : true, QuestionRecord : $scope.CurQuestion});
	}
	$scope.PrepareQuestion = function () {
		if($scope.CurQuestion.QuestionType == "Manual") {
			var start = $scope.ManualAnswerRegex.exec($scope.CurQuestion.QuestionOptions[0].Stem).index;
			var end = $scope.CurQuestion.QuestionOptions[0].Stem.indexOf("}", start) + 1;
			var text = $scope.CurQuestion.QuestionOptions[0].Stem.substring(0,start) + "____" + $scope.CurQuestion.QuestionOptions[0].Stem.substring(end,$scope.CurQuestion.QuestionOptions[0].Stem.length);
			var options = $scope.CurQuestion.QuestionOptions[0].Stem.substring(start,end);
			options = options.replace(/{/gi,"");
			options = options.replace(/}/gi,"");
			options = options.split("||");
			var AnswerCount = 0;
			var curoptions = [];
			for(var i = 0; i < options.length; i++) {
				var questionoption = {}
				if(options[i].indexOf("@") != -1) {
					questionoption['IsAnswer'] = true;
					questionoption['Display'] = $scope.DecodeHTML(options[i].replace(/@/gi,""));
					$scope.CurQuestion['QuestionExplanation'] = "<b>Answer:</b> " + $scope.DecodeHTML(options[i].replace(/@/gi,""));
					AnswerCount++;
				}
				else {
					questionoption['IsAnswer'] = false;
					questionoption['Display'] = $scope.DecodeHTML(options[i]);
				}
				questionoption['ID'] = "Option_" + i;
				questionoption['UserAnswer'] = false;
				curoptions.push(questionoption);
			}
			$scope.CurQuestion['DisplayOptions'] = $scope.Shuffle(curoptions);
			if(AnswerCount == options.length && AnswerCount == 1) {
				$scope.CurQuestion['QuestionStyle'] = "FlashCard";
				$scope.CurQuestion['QuestionExplanation'] += "<br /><b>Explanation:</b> " + $scope.DecodeHTML($scope.CurQuestion.QuestionOptions[0].Explanation);
			}
			else if(options.length > 1) {
				$scope.CurQuestion['QuestionStyle'] = "MultipleChoice";
				$scope.CurQuestion['QuestionExplanation'] = $scope.DecodeHTML($scope.CurQuestion.QuestionOptions[0].Explanation);
			}
			$scope.CurQuestion['DisplayPrompt'] = $scope.DecodeHTML(text);
		}
		else if($scope.CurQuestion.QuestionType == "Logical") {
			if(Math.random() > 0.5) {
				var OptionNumbers = [];
				for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) OptionNumbers.push(i);
				var ChosenOption = Math.floor(Math.random() * OptionNumbers.length);
				var Terms = $scope.ExtractBooleanTerms($scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Relationship);
				while(Terms.length < 1) {
					OptionNumbers.splice(ChosenOption,1);
					if(OptionNumbers.length == 0) return false;
					ChosenOption = Math.floor(Math.random() * OptionNumbers.length);
					Terms = $scope.ExtractBooleanTerms($scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Relationship);
				}
				var Variables = [];
				for(var i = 0; i < Terms.length; i++) {
					var trueterm = "";
					var falseterm = "";
					for(var j = 0; j < $scope.CurQuestion.QuestionOptions.length; j++) {
						if($.trim($scope.CurQuestion.QuestionOptions[j].Stem.toLowerCase()) == $.trim(Terms[i].toLowerCase())) {
							trueterm = $scope.CurQuestion.QuestionOptions[j].Node1;
							falseterm = $scope.CurQuestion.QuestionOptions[j].Node2;
							break;
						}
					}
					Variables.push({Name : Terms[i], Value : false, TrueTerm : trueterm, FalseTerm : falseterm});
				}
				if(Math.random() > 0.5) var GateOutput = true;
				else var GateOutput = false;
				if(GateOutput) $scope.CurQuestion['DisplayPrompt'] = "Which of the following would result in the state of " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Stem + " being " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Node1 + "?";
				else $scope.CurQuestion['DisplayPrompt'] = "Which of the following would result in the state of " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Stem + " being " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Node2 + "?";
				$scope.CurQuestion['QuestionStyle'] = "MultipleChoice";
				$scope.CurQuestion['QuestionExplanation'] = "<b>Explanation: </b><br /><b>Boolean Expression:</b>" + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Relationship + "<br /><div class='table-responsive'><table class='table'><tr><th>Input Values</th><th>Reasoning</th></tr>";
				var curoptions = [];
				var permutations = $scope.ExhaustBooleanPermutations(Variables.length);
				var remainingoptions = [];
				for(var i = 0; i < permutations.length; i++) remainingoptions.push(i);
				if(remainingoptions.length > 5) {
					for(var i = 0; i < 5; i++) {
						var chosenquestionoption = Math.floor(Math.random() * remainingoptions.length);
						var questionoption = {};
						questionoption['ID'] = "Option_" + i;
						questionoption['UserAnswer'] = false;
						questionoption['Display'] = "";
						for(var j = 0; j < Variables.length; j++) {
							Variables[j].Value = permutations[remainingoptions[chosenquestionoption]][j];
							if(Variables[j].Value) questionoption['Display'] += "<br />State of " + Variables[j].Name + " is " + Variables[j].TrueTerm + " ";
							else questionoption['Display'] += "<br />State of " + Variables[j].Name + " is " + Variables[j].FalseTerm + " ";
						}			
						questionoption['Display'] = questionoption['Display'].substr(0,questionoption['Display'].length - 1);
						var BooleanEvaluation = $scope.EvaluateBooleanExpression($scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Relationship,Variables);
						if(BooleanEvaluation.TruthValue === GateOutput) questionoption['IsAnswer'] = true;
						else questionoption['IsAnswer'] = false;
						$scope.CurQuestion['QuestionExplanation'] += "<tr><td>" + questionoption['Display'] + "</td><td>" + BooleanEvaluation.Explanation + "<br />If the expression is true then " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Stem + " is " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Node1 + "<br />If the expression is false then " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Stem + " is " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Node1 + "</td></tr>";
						curoptions.push(questionoption);
						remainingoptions.splice(chosenquestionoption,1);
					}
				}
				else {
					for(var i = 0; i < remainingoptions.length; i++) {
						var chosenquestionoption = i;
						var questionoption = {};
						questionoption['ID'] = "Option_" + i;
						questionoption['UserAnswer'] = false;
						questionoption['Display'] = "";
						for(var j = 0; j < Variables.length; j++) {
							Variables[j].Value = permutations[remainingoptions[chosenquestionoption]][j];
							if(Variables[j].Value) questionoption['Display'] += "<br />State of " + Variables[j].Name + " is " + Variables[j].TrueTerm + " ";
							else questionoption['Display'] += "<br />State of " + Variables[j].Name + " is " + Variables[j].FalseTerm + " ";
						}			
						questionoption['Display'] = questionoption['Display'].substr(0,questionoption['Display'].length - 1);
						var BooleanEvaluation = $scope.EvaluateBooleanExpression($scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Relationship,Variables);
						if(BooleanEvaluation.TruthValue === GateOutput) questionoption['IsAnswer'] = true;
						else questionoption['IsAnswer'] = false;
						$scope.CurQuestion['QuestionExplanation'] += "<tr><td>" + questionoption['Display'] + "</td><td>" + BooleanEvaluation.Explanation + "<br />If the expression is true then " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Stem + " is " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Node1 + "<br />If the expression is false then " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Stem + " is " + $scope.CurQuestion.QuestionOptions[OptionNumbers[ChosenOption]].Node1 + "</td></tr>";
						curoptions.push(questionoption);
						remainingoptions.splice(chosenquestionoption,1);
					}
				}
				$scope.CurQuestion['DisplayOptions'] = $scope.Shuffle(curoptions);
			}
			else {
				$scope.CurQuestion['QuestionStyle'] = "MultipleChoice";
				$scope.CurQuestion['DisplayPrompt'] = "Given the following initial conditions : ";
				$scope.CurQuestion['QuestionExplanation'] = "<b>Explanation: </b><br /><div clas='table-responsive'><table class='table'><tr><th>Gate</th><th>Expression</th><th>Output</th></tr>";
				var Variables = [];
				var availableoptions = [];
				for(i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
					if($scope.CurQuestion.QuestionOptions[i].Relationship == "") {
						if(Math.random() > 0.5) {
							Variables.push({Name : $scope.CurQuestion.QuestionOptions[i].Stem, Value : true, TrueTerm : $scope.CurQuestion.QuestionOptions[i].Node1, FalseTerm : $scope.CurQuestion.QuestionOptions[i].Node2});
							$scope.CurQuestion['DisplayPrompt'] += "<br />" + $scope.CurQuestion.QuestionOptions[i].Stem + " is " + $scope.CurQuestion.QuestionOptions[i].Node1 + " ";
						}
						else {
							Variables.push({Name : $scope.CurQuestion.QuestionOptions[i].Stem, Value : false, TrueTerm : $scope.CurQuestion.QuestionOptions[i].Node1, FalseTerm : $scope.CurQuestion.QuestionOptions[i].Node2});
							$scope.CurQuestion['DisplayPrompt'] += "<br />" + $scope.CurQuestion.QuestionOptions[i].Stem + " is " + $scope.CurQuestion.QuestionOptions[i].Node2 + " ";
						}	
					}
				}
				$scope.CurQuestion['DisplayPrompt'] += "<br />Which of the following statements are correct?";
				var prevvarlength = 0;
				if(Variables.length == 0) return false;
				else {
					var evaluated = [];
					while(prevvarlength != Variables.length) {
						prevvarlength = Variables.length;
						for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
							var temp = $scope.EvaluateBooleanExpression($scope.CurQuestion.QuestionOptions[i].Relationship, Variables);
							if(temp.TruthValue !== undefined && evaluated.indexOf(i) == -1) {
								Variables.push({Name : $scope.CurQuestion.QuestionOptions[i].Stem, Value : temp.TruthValue, TrueTerm : $scope.CurQuestion.QuestionOptions[i].Node1, FalseTerm : $scope.CurQuestion.QuestionOptions[i].Node2});
								$scope.CurQuestion['QuestionExplanation'] += "<tr><td>" + $scope.CurQuestion.QuestionOptions[i].Stem + "</td><td><b>"  + $scope.CurQuestion.QuestionOptions[i].Relationship + "</b><br />" + temp.Explanation + "<br />If the expression is true " + $scope.CurQuestion.QuestionOptions[i].Stem + " is " + $scope.CurQuestion.QuestionOptions[i].Node1 + "<br />If the expression is false " + $scope.CurQuestion.QuestionOptions[i].Stem + " is " + $scope.CurQuestion.QuestionOptions[i].Node2 + "</td><td>";
								if(temp.TruthValue) $scope.CurQuestion['QuestionExplanation'] += $scope.CurQuestion.QuestionOptions[i].Node1 + "</td></tr>";
								else $scope.CurQuestion['QuestionExplanation'] += $scope.CurQuestion.QuestionOptions[i].Node2 + "</td></tr>";
								availableoptions.push(Variables.length - 1);
								evaluated.push(i);
							}
						}
					}
					$scope.CurQuestion['QuestionExplanation'] += "</table></div>";
					var curoptions = [];
					if(availableoptions.length > 5) {
						for(var i = 0; i < 5; i++) {
							var questionoption = {};
							var chosenoption = Math.floor(Math.random() * availableoptions.length);
							var questionoption = {};
							questionoption['UserAnswer'] = false;
							questionoption['ID'] = "Option_" + i;
							if(Variables[availableoptions[chosenoption]].Value) {
								if(Math.random() > 0.5) {
									questionoption['IsAnswer'] = true;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].TrueTerm;
								}
								else {
									questionoption['IsAnswer'] = false;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].FalseTerm;
								}
							}
							else {
								if(Math.random() > 0.5) {
									questionoption['IsAnswer'] = true;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].FalseTerm;
								}
								else {
									questionoption['IsAnswer'] = false;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].TrueTerm;
								}
							}
							curoptions.push(questionoption);
						}
					}
					else {
						for(var i = 0; i < availableoptions.length; i++) {
							var questionoption = {};
							questionoption['UserAnswer'] = false;
							questionoption['ID'] = "Option_" + i;
							if(Variables[availableoptions[i]].Value) {
								if(Math.random() > 0.5) {
									questionoption['IsAnswer'] = true;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].TrueTerm;
								}
								else {
									questionoption['IsAnswer'] = false;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].FalseTerm;
								}
							}
							else {
								if(Math.random() > 0.5) {
									questionoption['IsAnswer'] = true;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].FalseTerm;
								}
								else {
									questionoption['IsAnswer'] = false;
									questionoption['Display'] = Variables[availableoptions[i]].Name + " is " + Variables[availableoptions[i]].TrueTerm;
								}
							}
							curoptions.push(questionoption);
						}
						$scope.CurQuestion['DisplayOptions'] = $scope.Shuffle(curoptions);
					}
				}
			}
		}
		else if($scope.CurQuestion.QuestionType == "Graph") {
			if($scope.CurQuestion.QuestionOptions.length == 0) return false;
			$scope.CurQuestion['QuestionStyle'] = "MultipleChoice";
			var GraphRelationships = {};
			var GraphNodes = [];
			var NodesWithBothChildrenAndParents = [];
			var TypesOfRelationships = [];
			for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
				if(TypesOfRelationships.indexOf($scope.CurQuestion.QuestionOptions[i].Relationship) == -1) TypesOfRelationships.push($scope.CurQuestion.QuestionOptions[i].Relationship);
				if(GraphRelationships.hasOwnProperty($scope.CurQuestion.QuestionOptions[i].Node1)) {
					if(GraphRelationships[$scope.CurQuestion.QuestionOptions[i].Node1].hasOwnProperty($scope.CurQuestion.QuestionOptions[i].Relationship)) GraphRelationships[$scope.CurQuestion.QuestionOptions[i].Node1][$scope.CurQuestion.QuestionOptions[i].Relationship].push($scope.CurQuestion.QuestionOptions[i].Node2);
					else GraphRelationships[$scope.CurQuestion.QuestionOptions[i].Node1][$scope.CurQuestion.QuestionOptions[i].Relationship] = [$scope.CurQuestion.QuestionOptions[i].Node2];
				}
				else {
					GraphRelationships[$scope.CurQuestion.QuestionOptions[i].Node1] = {};
					GraphRelationships[$scope.CurQuestion.QuestionOptions[i].Node1][$scope.CurQuestion.QuestionOptions[i].Relationship] = [$scope.CurQuestion.QuestionOptions[i].Node2];
				}
			}
			if($scope.CurQuestion.QuestionOptions[0].TableEntry == "DisableMultipleLinks") var DisableMultipleLinking = true;
			else var DisableMultipleLinking = false;
			var ChosenRelationship = TypesOfRelationships[Math.floor(Math.random() * TypesOfRelationships.length)];
			var NodesWithChildren = [];
			var NodesWithParents = [];
			for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
				if($scope.CurQuestion.QuestionOptions[i].Relationship == ChosenRelationship) {
					if(NodesWithBothChildrenAndParents.indexOf($scope.CurQuestion.QuestionOptions[i].Node1) == -1) NodesWithBothChildrenAndParents.push($scope.CurQuestion.QuestionOptions[i].Node1);
					if(NodesWithBothChildrenAndParents.indexOf($scope.CurQuestion.QuestionOptions[i].Node2) == -1) NodesWithBothChildrenAndParents.push($scope.CurQuestion.QuestionOptions[i].Node2);
					if(NodesWithChildren.indexOf($scope.CurQuestion.QuestionOptions[i].Node1) == -1) NodesWithChildren.push($scope.CurQuestion.QuestionOptions[i].Node1);
					if(NodesWithParents.indexOf($scope.CurQuestion.QuestionOptions[i].Node2) == -1) NodesWithParents.push($scope.CurQuestion.QuestionOptions[i].Node2);
				}
			}
			var NodeNumber = Math.floor(Math.random() * NodesWithBothChildrenAndParents.length);
			var ChosenNode = NodesWithBothChildrenAndParents[NodeNumber];
			if(NodesWithChildren.indexOf(ChosenNode) != -1) var ChosenNodeHasChildren = true;
			else ChosenNodeHasChildren = false;
			if(NodesWithParents.indexOf(ChosenNode) != -1) var ChosenNodeHasParents = true;
			else var ChosenNodeHasParents = false;
			$scope.CurQuestion['DisplayPrompt'] = "Regarding " + ChosenNode + ", which of the following statements are correct?";
			$scope.CurQuestion['QuestionExplanation'] = "<b>Explanation:</b><br />";
			var curoptions = [];
			if(ChosenNodeHasParents && ChosenNodeHasChildren) GraphNodes = NodesWithBothChildrenAndParents;
			else if(ChosenNodeHasParents) GraphNodes = NodesWithChildren;
			else if(ChosenNodeHasChildren) GraphNodes = NodesWithParents;
			if(GraphNodes.length > 5) {
				for(var i = 0; i < 5; i++) {
					var questionoption = {};
					questionoption['ID'] = "Option_" + i;
					questionoption['UserAnswer'] = false;
					var chosengraphnodenum = Math.floor(Math.random() * GraphNodes.length);
					var chosengraphnode = GraphNodes[chosengraphnodenum];
					GraphNodes.splice(chosengraphnodenum,1);
					var randomnum = Math.random();
					if(DisableMultipleLinking) {
						if(ChosenNodeHasParents && ChosenNodeHasChildren) {
							if(randomnum < 0.5) {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + chosengraphnode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, chosengraphnode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else {
								questionoption['Display'] = chosengraphnode + " " + ChosenRelationship + " " + ChosenNode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, chosengraphnode, ChosenNode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
						else if(ChosenNodeHasParents) {
							questionoption['Display'] = chosengraphnode + " " + ChosenRelationship + " " + ChosenNode + " directly";
							var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, chosengraphnode, ChosenNode, 1);
							$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
							if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
							else questionoption['IsAnswer'] = false;
						}
						else if(ChosenNodeHasChildren) {
							questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + chosengraphnode + " directly";
							var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, chosengraphnode, 1);
							$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
							if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
							else questionoption['IsAnswer'] = false;
						}
					}
					else {
						if(ChosenNodeHasParents && ChosenNodeHasChildren) {
							if(randomnum < 0.25) {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + chosengraphnode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, chosengraphnode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else if (randomnum >= 0.25 && randomnum < 0.5) {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + chosengraphnode + " through one or more links";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, chosengraphnode, 0);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else if(randomnum >= 0.5 && randomnum < 0.75) {
								questionoption['Display'] = chosengraphnode + " " + ChosenRelationship + " " + ChosenNode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, chosengraphnode, ChosenNode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else if(randomnum >= 0.75) {
								questionoption['Display'] = chosengraphnode + " " + ChosenRelationship + " " + ChosenNode + " through one or more links";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, chosengraphnode, ChosenNode, 0);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
						else if(ChosenNodeHasParents) {
							if(randomnum > 0.5) {
								questionoption['Display'] = chosengraphnode + " " + ChosenRelationship + " " + ChosenNode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, chosengraphnode, ChosenNode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else {
								questionoption['Display'] = chosengraphnode + " " + ChosenRelationship + " " + ChosenNode + " through one or more links";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, chosengraphnode, ChosenNode, 0);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
						else if(ChosenNodeHasChildren) {
							if(randomnum > 0.5) {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + chosengraphnode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, chosengraphnode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + chosengraphnode + " through one or more links";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, chosengraphnode, 0);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
					}
					curoptions.push(questionoption);
				}
			}
			else {
				for(var i = 0; i < GraphNodes.length; i++) {
					var questionoption = {};
					questionoption['ID'] = "Option_" + i;
					questionoption['UserAnswer'] = false;
					var randomnum = Math.random();
					if(DisableMultipleLinking) {
						if(ChosenNodeHasParents && ChosenNodeHasChildren) {
							if(randomnum < 0.5) {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + GraphNodes[i] + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, GraphNodes[i], 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else {
								questionoption['Display'] = GraphNodes[i] + " " + ChosenRelationship + " " + ChosenNode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, GraphNodes[i], ChosenNode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
						else if(ChosenNodeHasParents) {
							questionoption['Display'] = GraphNodes[i] + " " + ChosenRelationship + " " + ChosenNode + " directly";
							var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, GraphNodes[i], ChosenNode, 1);
							$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
							if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
							else questionoption['IsAnswer'] = false;
						}
						else if(ChosenNodeHasChildren) {
							questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + GraphNodes[i] + " directly";
							var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, GraphNodes[i], 1);
							$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
							if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
							else questionoption['IsAnswer'] = false;
						}
					}
					else {
						if(ChosenNodeHasParents && ChosenNodeHasChildren) {
								if(randomnum < 0.25) {
									questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + GraphNodes[i] + " directly";
									var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, GraphNodes[i], 1);
									$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
									if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
									else questionoption['IsAnswer'] = false;
								}
								else if (randomnum >= 0.25 && randomnum < 0.5) {
									questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + GraphNodes[i] + " through one or more links";
									var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, GraphNodes[i], 0);
									$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
									if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
									else questionoption['IsAnswer'] = false;
								}
							else if(randomnum >= 0.5 && randomnum < 0.75) {
								questionoption['Display'] = GraphNodes[i] + " " + ChosenRelationship + " " + ChosenNode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, GraphNodes[i], ChosenNode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else if(randomnum >= 0.75) {
								questionoption['Display'] = GraphNodes[i] + " " + ChosenRelationship + " " + ChosenNode + " through one or more links";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, GraphNodes[i], ChosenNode, 0);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
						else if(ChosenNodeHasParents) {
							if(randomnum > 0.5) {
								questionoption['Display'] = GraphNodes[i] + " " + ChosenRelationship + " " + ChosenNode + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, GraphNodes[i], ChosenNode, 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else {
								questionoption['Display'] = GraphNodes[i] + " " + ChosenRelationship + " " + ChosenNode + " through one or more links";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, GraphNodes[i], ChosenNode, 0);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
						else if(ChosenNodeHasChildren) {
							if(randomnum > 0.5) {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + GraphNodes[i] + " directly";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, GraphNodes[i], 1);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
							else {
								questionoption['Display'] = ChosenNode + " " + ChosenRelationship + " " + GraphNodes[i] + " through one or more links";
								var questionresult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, ChosenNode, GraphNodes[i], 0);
								$scope.CurQuestion['QuestionExplanation'] += questionresult.Explanation;
								if(questionresult.TruthValue) questionoption['IsAnswer'] = true;
								else questionoption['IsAnswer'] = false;
							}
						}
					}
					curoptions.push(questionoption);
				}
			}
			$scope.CurQuestion['DisplayOptions'] = $scope.Shuffle(curoptions);
		}
		else if($scope.CurQuestion.QuestionType == "Table") {
			var Aspects = [];
			var Topics = [];
			var AspectUniques = {};
			var TopicUniques = {};
			for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
				if(Aspects.indexOf($scope.CurQuestion.QuestionOptions[i].Aspect) == -1) {
					Aspects.push($scope.CurQuestion.QuestionOptions[i].Aspect);
					AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect] = {};
					AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect]["NumberOfUniques"] = 1;
					AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect][$scope.CurQuestion.QuestionOptions[i].TableEntry] = 1;
				}
				else {
					var HasEntry = false;
					for(var key in AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect]) {
						if(AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect].hasOwnProperty(key)) {
							if(key == $scope.CurQuestion.QuestionOptions[i].TableEntry) {
								HasEntry = true;
								AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect][key] += 1;
							}
						}
					}
					if(!HasEntry) {
						AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect]["NumberOfUniques"] += 1;
						AspectUniques[$scope.CurQuestion.QuestionOptions[i].Aspect][$scope.CurQuestion.QuestionOptions[i].TableEntry] = 1;
					}
				}
				if(Topics.indexOf($scope.CurQuestion.QuestionOptions[i].Topic) == -1) {
					Topics.push($scope.CurQuestion.QuestionOptions[i].Topic);
					TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic] = {};
					TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic]["NumberOfUniques"] = 1;
					TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic][$scope.CurQuestion.QuestionOptions[i].TableEntry] = 1;
				}
				else {
					var HasEntry = false;
					for(var key in TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic]) {
						if(TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic].hasOwnProperty(key)) {
							if(key == $scope.CurQuestion.QuestionOptions[i].TableEntry) {
								HasEntry = true;
								TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic][key] += 1;
							}
						}
					}
					if(!HasEntry) {
						TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic]["NumberOfUniques"] += 1;
						TopicUniques[$scope.CurQuestion.QuestionOptions[i].Topic][$scope.CurQuestion.QuestionOptions[i].TableEntry] = 1;
					}
				}
			}
			if(Topics.length == 1 && Aspects.length == 1) {
				$scope.CurQuestion['QuestionStyle'] = "FlashCard";
				$scope.CurQuestion['DisplayPrompt'] = "What is the " + $scope.CurQuestion.QuestionOptions[0].Aspect + " of " + $scope.DecodeHTML($scope.CurQuestion.QuestionOptions[0].Topic);
				$scope.CurQuestion['QuestionExplanation'] = "<b>Answer:</b> " + $scope.DecodeHTML($scope.CurQuestion.QuestionOptions[0].TableEntry + "<b>Explanation:</b> " + $scope.CurQuestion.QuestionOptions[0].Explanation);
			}
			else {
				var OptionChoice = Math.floor(Math.random() * $scope.CurQuestion.QuestionOptions.length);
				var CurAspect = $scope.CurQuestion.QuestionOptions[OptionChoice].Aspect;
				var CurTopic = $scope.CurQuestion.QuestionOptions[OptionChoice].Topic;
				var CurTableEntry = $scope.CurQuestion.QuestionOptions[OptionChoice].TableEntry;
				$scope.CurQuestion['QuestionStyle'] = "MultipleChoice";
				$scope.CurQuestion['DisplayPrompt'] = "Regarding " + $scope.CurQuestion.QuestionOptions[OptionChoice].Aspect + " of " +$scope.DecodeHTML($scope.CurQuestion.QuestionOptions[OptionChoice].Topic) + " which of the following are correct?";
				var curoptions = [];
				var chosenquestions = [];
				var optionid = 0;
				if(Topics.length == 1 && Aspects.length > 1) {
					var NumberOfUniqueAspects = $scope.NumberOfProperties(TopicUniques[CurTopic]) - 1;
					var chosentableentries = [];
					var explanationtopics = [];
					var explanationaspects = [];
					if(NumberOfUniqueAspects > 5) {
						while(chosenquestions.length < 5) {
							var RandomOption = 1 + Math.floor(Math.random() * NumberOfUniqueAspects);
							while(chosenquestions.indexOf(RandomOption) != -1 || $scope.GetPropertyByIndex(TopicUniques[CurTopic], RandomOption)[0] == "NumberOfUniques") RandomOption = Math.floor(Math.random() * NumberOfUniqueAspects);
							chosenquestions.push(RandomOption);
							var CurOption = $scope.GetPropertyByIndex(TopicUniques[CurTopic], RandomOption);
							var questionoption = {};
							questionoption['UserAnswer'] = false;
							questionoption['IsAnswer'] = CurOption[0] == CurTableEntry;
							questionoption['Display'] = CurOption[0];
							questionoption['ID'] = "Option_" + optionid;
							curoptions.push(questionoption);
							chosentableentries.push(CurOption[0]);
							optionid++;
						}
					}
					else {
						for(var key in TopicUniques[CurTopic]) {
							if(TopicUniques[CurTopic].hasOwnProperty(key) && key != "NumberOfUniques") {
								var questionoption = {};
								questionoption['UserAnswer'] = false;
								questionoption['IsAnswer'] = key == CurTableEntry;
								questionoption['Display'] = key;
								questionoption['ID'] = "Option_" + optionid;
								curoptions.push(questionoption);
								chosentableentries.push(key);
								optionid++;
							}
						}
					}
					$scope.CurQuestion['QuestionExplanation'] = "<div class='table-responsive'><table class='table'><tr><th>Topic</th>";
					for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
						if(chosentableentries.indexOf($scope.CurQuestion.QuestionOptions[i].TableEntry) != -1) {
							if(explanationtopics.indexOf($scope.CurQuestion.QuestionOptions[i].Topic) == -1) explanationtopics.push($scope.CurQuestion.QuestionOptions[i].Topic);
							if(explanationaspects.indexOf($scope.CurQuestion.QuestionOptions[i].Aspect) == -1) explanationaspects.push($scope.CurQuestion.QuestionOptions[i].Aspect);
						}
					}
					for(var i = 0; i < explanationaspects.length; i++) $scope.CurQuestion['QuestionExplanation'] += "<th>" + explanationaspects[i] + "</th>";
					$scope.CurQuestion['QuestionExplanation'] += "</tr>";
					for(var i = 0; i < explanationtopics.length; i++) {
						$scope.CurQuestion['QuestionExplanation'] += "<tr><td>" + explanationtopics[i] + "</td>";
						for(var j = 0; j < $scope.CurQuestion.QuestionOptions.length; j++) {
							for(var k = 0; k < explanationaspects.length; k++) {
								if($scope.CurQuestion.QuestionOptions[j].Topic == explanationtopics[i] && $scope.CurQuestion.QuestionOptions[j].Aspect == explanationaspects[k]) {
									$scope.CurQuestion['QuestionExplanation'] += "<td><b>Answer:</b> " + $scope.CurQuestion.QuestionOptions[j].TableEntry + "<b>Explanation:</b> " + $scope.CurQuestion.QuestionOptions[j].Explanation + "</td>";
								}
							}
						}
						$scope.CurQuestion['QuestionExplanation'] += "</tr>";
					}
					$scope.CurQuestion['QuestionExplanation'] += "</table></div>";
				}
				else {
					if(Math.random() > 0.5) {
						var NumberOfUniqueTopics = $scope.NumberOfProperties(AspectUniques[CurAspect]) - 1;
						var chosentableentries = [];
						var explanationtopics = [];
						var explanationaspects = [];
						if(NumberOfUniqueTopics > 5) {
							while(chosenquestions.length < 5) {
								var RandomOption = Math.floor(Math.random() * NumberOfUniqueTopics);
								while(chosenquestions.indexOf(RandomOption) != -1 || $scope.GetPropertyByIndex(TopicUniques[CurTopic], RandomOption)[0] == "NumberOfUniques") RandomOption = Math.floor(Math.random() * NumberOfUniqueTopics);
								chosenquestions.push(RandomOption);
								var CurOption = $scope.GetPropertyByIndex(AspectUniques[CurAspect], RandomOption);
								var questionoption = {};
								questionoption['UserAnswer'] = false;
								questionoption['IsAnswer'] = CurOption[0] == CurTableEntry;
								questionoption['Display'] = CurOption[0];
								questionoption['ID'] = "Option_" + optionid;
								chosentableentries.push(CurOption[0]);
								curoptions.push(questionoption);
								optionid++;
							}
						}
						else {
							for(var key in AspectUniques[CurAspect]) {
								if(AspectUniques[CurAspect].hasOwnProperty(key) && key != "NumberOfUniques") {
									var questionoption = {};
									questionoption['UserAnswer'] = false;
									questionoption['IsAnswer'] = key == CurTableEntry;
									questionoption['Display'] = key;
									questionoption['ID'] = "Option_" + optionid;
									chosentableentries.push(key);
									curoptions.push(questionoption);
									optionid++;
								}
							}
						}
						$scope.CurQuestion['QuestionExplanation'] = "<div class='table-responsive'><table class='table'><tr><th>Topic</th>";
						for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
							if(chosentableentries.indexOf($scope.CurQuestion.QuestionOptions[i].TableEntry) != -1) {
								if(explanationtopics.indexOf($scope.CurQuestion.QuestionOptions[i].Topic) == -1) explanationtopics.push($scope.CurQuestion.QuestionOptions[i].Topic);
								if(explanationaspects.indexOf($scope.CurQuestion.QuestionOptions[i].Aspect) == -1) explanationaspects.push($scope.CurQuestion.QuestionOptions[i].Aspect);
							}
						}
						for(var i = 0; i < explanationaspects.length; i++) $scope.CurQuestion['QuestionExplanation'] += "<th>" + explanationaspects[i] + "</th>";
						$scope.CurQuestion['QuestionExplanation'] += "</tr>";
						for(var i = 0; i < explanationtopics.length; i++) {
							$scope.CurQuestion['QuestionExplanation'] += "<tr><td>" + explanationtopics[i] + "</td>";
							for(var j = 0; j < $scope.CurQuestion.QuestionOptions.length; j++) {
								for(var k = 0; k < explanationaspects.length; k++) {
									if($scope.CurQuestion.QuestionOptions[j].Topic == explanationtopics[i] && $scope.CurQuestion.QuestionOptions[j].Aspect == explanationaspects[k]) {
										$scope.CurQuestion['QuestionExplanation'] += "<td><b>Answer:</b> " + $scope.CurQuestion.QuestionOptions[j].TableEntry + "<b>Explanation:</b> " + $scope.CurQuestion.QuestionOptions[j].Explanation + "</td>";
									}
								}
							}
							$scope.CurQuestion['QuestionExplanation'] += "</tr>";
						}
						$scope.CurQuestion['QuestionExplanation'] += "</table></div>";
					}
					else {
						$scope.CurQuestion['DisplayPrompt'] = "Regarding " + $scope.CurQuestion.QuestionOptions[OptionChoice].Aspect + " : " + $scope.DecodeHTML(CurTableEntry) + " which of the following are correct?";
						$scope.CurQuestion['QuestionExplanation'] = "<div class='table-responsive'><table class='table'><tr><th>Topic</th><th>" + CurAspect + "</th></tr>";
						var availabletopics = [];
						for(var i = 0; i < $scope.CurQuestion.QuestionOptions.length; i++) {
							if($scope.CurQuestion.QuestionOptions[i].Aspect == CurAspect) availabletopics.push($scope.CurQuestion.QuestionOptions[i]);
						}
						if(availabletopics.length > 5) {
							for(var i = 0; i < 5; i++) {
								var questionoption = {};
								var temp = Math.floor(Math.random() * availabletopics.length);
								questionoption['UserAnswer'] = false;
								questionoption['IsAnswer'] = availabletopics[temp].TableEntry == CurTableEntry;
								questionoption['Display'] = availabletopics[temp].Topic;
								questionoption['ID'] = "Option_" + optionid;
								curoptions.push(questionoption);
								$scope.CurQuestion['QuestionExplanation'] += "<tr><td>" + availabletopics[temp].Topic + "</td><td><b>Answer:</b> " + availabletopics[temp].TableEntry + "<b>Explanation:</b> " + availabletopics[temp].Explanation + "</td></tr>";
								availabletopics.splice(temp,1);
								optionid++;
							}
						}
						else {
							for(var i = 0; i < availabletopics.length; i++) {
								var questionoption = {};
								questionoption['UserAnswer'] = false;
								questionoption['IsAnswer'] = availabletopics[i].TableEntry == CurTableEntry;
								questionoption['Display'] = availabletopics[i].Topic;
								questionoption['ID'] = "Option_" + optionid;
								$scope.CurQuestion['QuestionExplanation'] += "<tr><td>" + availabletopics[i].Topic + "</td><td><b>Answer:</b> " + availabletopics[i].TableEntry + "<b>Explanation:</b> " + availabletopics[i].Explanation + "</td></tr>";
								curoptions.push(questionoption);
								optionid++;
							}
						}
						$scope.CurQuestion['QuestionExplanation'] += "</table></div>";
					}
				}
				$scope.CurQuestion['DisplayOptions'] = $scope.Shuffle(curoptions);
			}
		}
	}
	$scope.GetQuestion = function() {
		$http.post("Scripts/quizapp.php", {GetQuestion : $scope.ContentType, ContentID : $scope.ContentID, Username : $scope.Username, Override : $scope.Override}).then(function(response) {
			$scope.CurQuestion = response.data;
			$scope.Submitted = false;
			$scope.FlipCard = false;
			$scope.Review = false;
			$scope.UserAnswers = {};
			$scope.PrepareQuestion();
			$scope.TimeTaken = 0;
			$scope.TimeInterval = $interval(function() {$scope.TimeTaken += 1;}, 1000);
		});
	}
	$scope.GetTestQuestions = function() {
		$http.post("Scripts/quizapp.php", {GetTestQuestions : true, ContentID : $scope.ContentID, Username : $scope.Username, Override : $scope.Override}).then(function(response) {
			$scope.PastQuestions = response.data;
			for(var i = 0; i < $scope.PastQuestions.length; i++) {
				$scope.CurQuestion = $scope.PastQuestions[i];
				$scope.PrepareQuestion();
			}
			$scope.CurQuestion = $scope.PastQuestions[$scope.TestCurQuestion];
			$scope.Submitted = false;
			$scope.FlipCard = false;
			$scope.Review = false;
			$scope.UserAnswers = {};
			$scope.TimeTaken = 0;
			$scope.TimeTracker = 0;
			$scope.TimeInterval = $interval(function() {$scope.TimeTaken += 1;}, 1000);
		});
	}
	$scope.DecodeHTML = function(text) {
		var elem = document.createElement('textarea');
		elem.innerHTML = text;
		return elem.value;
	}
	$scope.Shuffle = function shuffle(array) {
  	var currentIndex = array.length, temporaryValue, randomIndex;
  	// While there remain elements to shuffle...
  	while (0 !== currentIndex) {
    	// Pick a remaining element...
    	randomIndex = Math.floor(Math.random() * currentIndex);
    	currentIndex -= 1;
    	// And swap it with the current element.
    	temporaryValue = array[currentIndex];
    	array[currentIndex] = array[randomIndex];
    	array[randomIndex] = temporaryValue;
  	}
  	return array;
	}
	$scope.OptionCorrect = function(ID) {	
		for(var i = 0; i < $scope.CurQuestion.DisplayOptions.length; i++) {
			if($scope.CurQuestion.DisplayOptions[i].ID == ID) {
				if(($scope.CurQuestion.DisplayOptions[i].IsAnswer && $scope.UserAnswers[String(ID)]) || (!$scope.CurQuestion.DisplayOptions[i].IsAnswer && !$scope.UserAnswers[String(ID)])) return true;
				else return false;
			}
		}
	}
	$scope.NumberOfProperties = function(obj) {
		var count = 0;
		for(var key in obj) {
			if(obj.hasOwnProperty(key)) count++;
		}
		return count;
	}
	$scope.GetPropertyByIndex = function(obj, index) {
		var count = 0;
		for(var key in obj) {
			if(obj.hasOwnProperty(key)) {
				if(index == count) return [key, obj[key]];
				count++;
			}
		}
	}
	$scope.ReviewQuestion = function(Question) {
		$scope.CurQuestion = Question;
		if($scope.CurQuestion != Question) $scope.UserAnswers = {};
		$scope.Review = false;
	}
	$scope.NextQuestion = function() {
		if($scope.TestCurQuestion < ($scope.PastQuestions.length - 1)) {
			if($scope.CurQuestion.QuestionStyle != "FlashCard") {
				for(var i = 0; i < $scope.CurQuestion.DisplayOptions.length; i++) {
					$scope.CurQuestion.DisplayOptions[i]['UserAnswer'] = $scope.UserAnswers[$scope.CurQuestion.DisplayOptions[i].ID];
				}
			}
			if(!$scope.CurQuestion.TimeTaken) $scope.CurQuestion["TimeTaken"] = $scope.TimeTaken - $scope.TimeTracker;
			else $scope.CurQuestion["TimeTaken"] += $scope.TimeTaken - $scope.TimeTracker;
			$scope.TestCurQuestion++;
			$scope.CurQuestion = $scope.PastQuestions[$scope.TestCurQuestion];
			$scope.UserAnswers = {};
			$scope.TimeTracker = $scope.TimeTaken;
		}
	}
	$scope.PrevQuestion = function() {
		if($scope.TestCurQuestion > 0) {
			if($scope.CurQuestion.QuestionStyle != "FlashCard") {
				for(var i = 0; i < $scope.CurQuestion.DisplayOptions.length; i++) {
					$scope.CurQuestion.DisplayOptions[i]['UserAnswer'] = $scope.UserAnswers[$scope.CurQuestion.DisplayOptions[i].ID];
				}
			}
			if(!$scope.CurQuestion.TimeTaken) $scope.CurQuestion["TimeTaken"] = $scope.TimeTaken - $scope.TimeTracker;
			else $scope.CurQuestion["TimeTaken"] += $scope.TimeTaken - $scope.TimeTracker;
			$scope.TestCurQuestion--;
			$scope.CurQuestion = $scope.PastQuestions[$scope.TestCurQuestion];		
			$scope.UserAnswers = {};	
			$scope.TimeTracker = $scope.TimeTaken;
		}
	}
	$scope.MarkFlashCard = function(val, ID) {
		for(var i = 0; i < $scope.PastQuestions.length; i++) {
			if($scope.PastQuestions[i].ID == ID) {
				$scope.PastQuestions[i].Result = val;
				break;
			}
		}
	}
	$scope.SubmitFlashCard = function(val) {
		$scope.CurQuestion['Result'] = val;
		$scope.SubmitAnswer();
	}
	$scope.HasParentRelation = function(GraphRelationships, ChosenRelationship, ChildNode) {
		for(var key in GraphRelationships) {
			if(GraphRelationships.hasOwnProperty(key)) {
				if($scope.IsChildRelation(GraphRelationships, ChosenRelationship, key, ChildNode, 0).TruthValue) return true;
			}
		}
		return false;
	}
	$scope.IsChildRelation = function(GraphRelationships, ChosenRelationship, ParentNode, ChildNode, Depth) {
		var Result = {TruthValue : false, Explanation : ParentNode + " " + ChosenRelationship + ":<ul>"};
		if(GraphRelationships.hasOwnProperty(ParentNode)) {
			if(GraphRelationships[ParentNode].hasOwnProperty(ChosenRelationship)) {
				if(Depth == 1) {
					for(var i = 0; i < GraphRelationships[ParentNode][ChosenRelationship].length; i++) {
						Result['Explanation'] += "<li>" + GraphRelationships[ParentNode][ChosenRelationship][i];
						if(GraphRelationships[ParentNode][ChosenRelationship][i] == ChildNode) {
							Result['TruthValue'] = true;
							Result['Explanation'] = ParentNode + " " + ChosenRelationship + ":<ul>";
							for(var j = 0; j < GraphRelationships[ParentNode][ChosenRelationship].length; j++) {
								if(GraphRelationships[ParentNode][ChosenRelationship][j] == ChildNode) Result['Explanation'] += "<li><font color='red'>" + GraphRelationships[ParentNode][ChosenRelationship][j] + "</font></li>";
								else Result['Explanation'] += "<li>" + GraphRelationships[ParentNode][ChosenRelationship][j] + "</li>";
							}
							Result['Explanation'] += "</ul>";
							return Result;
						}
					}
					Result['Explanation'] += "</ul>";
					Result['Explanation'] += "<font color='red'>" + ChildNode + " is not in the above list.</font><br />";
					return Result;
				}
				else {
					if(GraphRelationships[ParentNode][ChosenRelationship].length == 0) return false;
					else {
						for(var i = 0; i < GraphRelationships[ParentNode][ChosenRelationship].length; i++) {
							
							if(GraphRelationships[ParentNode][ChosenRelationship][i] == ChildNode) {
								Result['TruthValue'] = true;
								Result['Explanation'] = ParentNode + " " + ChosenRelationship + ":<ul>";
								for(var j = 0; j < GraphRelationships[ParentNode][ChosenRelationship].length; j++) {
									if(GraphRelationships[ParentNode][ChosenRelationship][j] == ChildNode) Result['Explanation'] += "<li><font color='red'>" + GraphRelationships[ParentNode][ChosenRelationship][j] + "</font></li>";
									else Result['Explanation'] += "<li>" + GraphRelationships[ParentNode][ChosenRelationship][j] + "</li>";
								}
								Result['Explanation'] += "</ul>";
								return Result;
							}
							else {
								var ChildResult = $scope.IsChildRelation(GraphRelationships, ChosenRelationship, GraphRelationships[ParentNode][ChosenRelationship][i], ChildNode, 0);
								if(ChildResult.TruthValue) {
									Result['Explanation'] += "<li>" + ChildResult.Explanation + "</li></ul>";
									Result['TruthValue'] = true;
									return Result;
								}
								else Result['Explanation'] += "<li>" + GraphRelationships[ParentNode][ChosenRelationship][i] + "</li>";
							}
						}
						Result['Explanation'] += "</ul>";
						Result['Explanation'] += "<font color='red'>" + ChildNode + " is not in the above list.</font><br />";
						return Result;
					}
				}
			}
			else {
				Result['Explanation'] = "<font color='red'>" + ParentNode + " does not " + ChosenRelationship + " anything</font><br />";
				return Result;
			}
		}
		else {
			Result['Explanation'] = "<font color='red'>" + ParentNode + " does not " + ChosenRelationship + " anything</font><br />";
			return Result;
		}
	}
	$scope.ExhaustBooleanPermutations = function(NumberOfTerms) {
		var Result = [];
		if(NumberOfTerms == 1) {
			Result.push([true]);
			Result.push([false]);
			return Result;
		}
		else {
			var subsequentterms = $scope.ExhaustBooleanPermutations(NumberOfTerms - 1);
			for(var i = 0; i < subsequentterms.length; i++) {
				Result.push(subsequentterms[i].concat(true));
				Result.push(subsequentterms[i].concat(false));
			}
			return Result;
		}
	}
	$scope.ExtractBooleanTerms = function(Expression) {
		Expression = $.trim(Expression);
		Expression = Expression.replace(/[()&|]/g,"{");
		Expression = Expression.replace(/!/g,"");
		var Terms = Expression.split("{");
		for(var i = 0; i < Terms.length; i++) {
			Terms[i] = $.trim(Terms[i]);
			if(Terms[i] == "") {
				Terms.splice(i,1);
				i--;
			}
		}
		return Terms;
	}
	$scope.EvaluateBooleanExpression = function(Expression, Variables) {
		var Result = {TruthValue : false, Explanation : ""}
		Expression = $.trim(Expression);
		var FirstBracket = Expression.indexOf("(");
		if(FirstBracket == -1) {
			if(Expression.indexOf("&") == -1 && Expression.indexOf("|") == -1) {
				Expression = $.trim(Expression);
				if(Expression[0] == "!") var negate = true;
				else var negate = false;
				Expression = Expression.replace(/!/i,"");
				for(var i = 0; i < Variables.length; i++) {
					if($.trim(Expression.toLowerCase()) == $.trim(Variables[i].Name.toLowerCase())) {
						if(negate) {
							Result.TruthValue = !Variables[i].Value;
							if(Variables[i].Value) Result.Explanation = Variables[i].Name + " was " + Variables[i].TrueTerm + ", Expression was NOT " + Variables[i].Name + ", therefore <font color='red'>" + Variables[i].Name + " is " + Variables[i].FalseTerm + "</font>";
							else Result.Explanation = Variables[i].Name + " was " + Variables[i].FalseTerm + ", Expression was NOT " + Variables[i].Name + ", therefore <font color='green'>" + Variables[i].Name + " is " + Variables[i].TrueTerm + "</font>";
							return Result;
						}
						else {
							Result.TruthValue = Variables[i].Value;
							if(Variables[i].Value) Result.Explanation = Variables[i].Name + " was " + Variables[i].TrueTerm + ", Expression was " + Variables[i].Name + ", therefore <font color='green'>" + Variables[i].Name + " is " + Variables[i].TrueTerm + "</font>";
							else Result.Explanation = Variables[i].Name + " was " + Variables[i].FalseTerm + ", Expression was " + Variables[i].Name + ", therefore <font color='red'>" + Variables[i].Name + " is " + Variables[i].FalseTerm + "</font>";
							return Result;
						}
					}
				}
				Result.TruthValue = undefined;
				Result.Explanation = "The value of <font color='blue'>" + Expression + " was undefined</font>";
				return Result;
			}
			else if(Expression.indexOf("&") != -1) {
				var Terms = Expression.split("&");
				Result.Explanation = "If any of these terms are false or undefined, then the whole Expression is false or undefined respectively<ul>";
				var hasfalse = false;
				var hasundefined = false;
				for(var i = 0; i < Terms.length; i++) {
					var temp = $scope.EvaluateBooleanExpression(Terms[i], Variables);
					if(temp.TruthValue === false) hasfalse = true;
					else if(temp.TruthValue === undefined) hasundefined = true;
					Result.Explanation += "<li>" + temp.Explanation; + "</li>";
				}
				if(hasfalse) {
					Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
					Result.Truthvalue = false;
					return Result;
				}
				if(hasundefined) {
					Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
					Result.Truthvalue = undefined;
					return Result;
				}
				Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
				Result.TruthValue = true;
				return Result;
			}
			else if(Expression.indexOf("|") != -1) {
				var Terms = Expression.split("|");
				Result.Explanation = "If any of these terms are true, then the whole Expression is true<ul>";
				var hasundefined = false;
				var hastrue = false;
				for(var i = 0; i < Terms.length; i++) {
					var temp = $scope.EvaluateBooleanExpression(Terms[i], Variables);
					if(temp.TruthValue === true) hastrue = true;
					else if(temp.TruthValue === undefined) hasundefined = true;
					Result.Explanation += "<li>" + temp.Explanation + "</li>";
				}
				if(hastrue) {
					Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
					Result.TruthValue = true;
					return Result;
				}
				if(hasundefined) {
					Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
					Result.Truthvalue = undefined;
					return Result;
				}
				Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
				Result.Truthvalue = false;
				return Result;
			}
		}
		else {
			var offset = FirstBracket;
			var bracketcount = 1;
			while(bracketcount > 0) {
				var openbracket = Expression.indexOf("(",offset + 1);
				var closebracket = Expression.indexOf(")", offset + 1);
				if(closebracket < openbracket && closebracket != -1) {
					bracketcount--;
					offset = closebracket;
				}
				else if(closebracket == -1) {
					Result.TruthValue = undefined;
					Result.Explanation = "Missing )";
					return Result;
				}
				else if(openbracket < closebracket && openbracket != -1) {
					bracketcount++;
					offset = openbracket;
				}
				else if(closebracket != -1 && openbracket == -1) {
					bracketcount--;
					offset = closebracket;
				}
			}
			var negate = undefined;
			var prioroperator = undefined;
			var priorterm = undefined;
			var priortermresult = undefined;
			var postoperator = undefined;
			var postterm = undefined;
			var posttermresult = undefined;
			var bracketterm = $.trim(Expression.substr(FirstBracket + 1, offset - FirstBracket - 1));
			var brackettermresult = $scope.EvaluateBooleanExpression(bracketterm, Variables);
			if(bracketterm === "") {
				Result.TruthValue = undefined;
				Result.Explanation = "The Expression was empty, therefore it is undefined";
				return Result;
			}
			if(FirstBracket > 0) {
				var count = 1;
				while(count <= FirstBracket) {
					if(Expression[FirstBracket-count] == "!" && negate === undefined) negate = true;
					else if(Expression[FirstBracket-count] == "!" && negate !== undefined) negate = !negate;
					else if(Expression[FirstBracket-count] == "&" || Expression[FirstBracket-count] == "|") {
						if(count == FirstBracket) {
							Result.TruthValue = undefined;
							Result.Explanation = "Was expecting an Expression, but none was found";
							return Result;
						}
						prioroperator = Expression[FirstBracket-count];
						break;
					}
					else if(/[^\s]/i.test(Expression[FirstBracket-count])) {
						Result.TruthValue = undefined;
						Result.Explanation = "Was expecting an Operator, but an Expression was found";
						return Result;
					}
					count++;
				}
				if(prioroperator !== undefined && count <= FirstBracket) priorterm = $.trim(Expression.substr(0,FirstBracket-count));
				if(priorterm == "" && prioroperator !== undefined) {
					Result.TruthValue = undefined;
					Result.Explanation = "Was expecting an Expression, but none was found";
					return Result;
				}
				if(negate && brackettermresult.TruthValue !== undefined) brackettermresult.TruthValue = !brackettermresult.TruthValue;
			}
			if(offset < Expression.length - 1) {
				var count = 1;
				while(count < (Expression.length - offset)) {
					if(Expression[offset + count] == "&" || Expression[offset + count] == "|") {
						if((offset + count) == (Expression.length - 1)) {
							Result.TruthValue = undefined;
							Result.Explanation = "Was expecting an Expression, but none was found";
							return Result;
						}
						postoperator = Expression[offset + count];
						break;
					}
					else if(/[^\s]/i.test(Expression[offset + count])) {
						Result.TruthValue = undefined;
						Result.Explanation = "Was expecting an Operator, but an Expression was found";
						return Result;
					}
					count++;
				}
				if(postoperator !== undefined && (offset + count) < (Expression.length - 1)) postterm = $.trim(Expression.substr(offset + count + 1));
				if(postterm == "" && postoperator !== undefined) {
					Result.TruthValue = undefined;
					Result.Explanation = "Was expecting an Expression, but none was found";
					return Result;
				}
			}
			if(postterm !== undefined) posttermresult = $scope.EvaluateBooleanExpression(postterm, Variables);
			if(priorterm !== undefined) priortermresult = $scope.EvaluateBooleanExpression(priorterm, Variables);
			if(prioroperator != undefined && postoperator != undefined) {
				if(prioroperator == "&" && postoperator == "&") {
					Result.Explanation += "If any of these terms are false or undefined, then the Expression is false or undefined respectively<ul>";
					if(posttermresult.TruthValue === undefined || priortermresult.TruthValue === undefined || brackettermresult.TruthValue === undefined) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
					else {
						if(priortermresult.TruthValue && brackettermresult.TruthValue && posttermresult.TruthValue) {
							if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
							else Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
							Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
							Result.TruthValue = true;
							return Result;
						}
						else {
							if(negate && brackettermresult.TruthValue !== undefined)Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
							else Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
							Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
							Result.TruthValue = false;
							return Result;
						}
					}
				}
				else if(prioroperator == "|" && postoperator == "|") {
					Result.Explanation += "If any of these terms are true, then the Expression is true<ul>";
					if(priortermresult.TruthValue || brackettermresult.TruthValue || posttermresult.TruthValue) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
						Result.TruthValue = true;
						return Result;
					}
					else if(posttermresult.TruthValue === undefined || priortermresult.TruthValue === undefined || brackettermresult.TruthValue === undefined) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
					else {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>" + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
						Result.TruthValue = false;
						return Result;
					}
				}
				else if(prioroperator == "|" && postoperator == "&") {
					if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "If either " + priorterm + " OR (<b>NOT</b>" + bracketterm + " AND " + postterm + ") is true, then the Expression is true<ul>";
					else Result.Explanation += "If either " + priorterm + " OR (" + bracketterm + " AND " + postterm + ") is true, then the Expression is true<ul>";
					if(priortermresult.TruthValue === true || (posttermresult.TruthValue === true && brackettermresult.TruthValue === true)) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
						Result.TruthValue = true;
						return Result;
					}
					else if(priortermresult.TruthValue === false && (brackettermresult.TruthValue === false || posttermresult.TruthValue === false)) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
						Result.TruthValue = false;
						return Result;
					}
					else {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
				}
				else if(prioroperator == "&" && postoperator == "|") {
					if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "If either (" + priorterm + " AND <b>NOT</b>" + bracketterm + ") OR " + postterm + ") is true, then the Expression is true<ul>";
					else Result.Explanation += "If either (" + priorterm + " AND " + bracketterm + ") OR " + postterm + ") is true, then the Expression is true<ul>";
					if(posttermresult.TruthValue === true || (priortermresult.TruthValue === true && brackettermresult.TruthValue === true)) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li></li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
						Result.TruthValue = true;
						return Result;
					}
					else if(posttermresult.TruthValue === false && (brackettermresult.TruthValue === false || priortermresult.TruthValue === false)) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
						Result.TruthValue = false;
						return Result;
					}
					else {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li><li>" + posttermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
				}
			}
			else if(prioroperator !== undefined && postoperator === undefined) {
				if(prioroperator == "&") {
					if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "If " + priorterm + " AND <b>NOT</b>(" + bracketterm + ") are true, then the Expression is true<ul>";
					else Result.Explanation += "If " + priorterm + " AND (" + bracketterm + ") are true, then the Expression is true<ul>";
					if(priortermresult.TruthValue === true && brackettermresult.TruthValue === true) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
						Result.TruthValue = true;
						return Result;
					}
					else if(brackettermresult.TruthValue === false || priortermresult.TruthValue === false) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
						Result.TruthValue = false;
						return Result;
					}
					else {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
				}
				else if(prioroperator == "|") {
					if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "If " + priorterm + " OR <b>NOT</b>(" + bracketterm + ") are true, then the Expression is true<ul>";
					else Result.Explanation += "If " + priorterm + " OR (" + bracketterm + ") are true, then the Expression is true<ul>";
					if(priortermresult.TruthValue === true || brackettermresult.TruthValue === true) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
						Result.TruthValue = true;
						return Result;
					}
					else if(brackettermresult.TruthValue === false && priortermresult.TruthValue === false) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
						Result.TruthValue = false;
						return Result;
					}
					else {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + priortermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
				}
			}
			else if(prioroperator === undefined && postoperator !== undefined) {
				if(postoperator == "&") {
					if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "If " + postterm + " AND <b>NOT</b>(" + bracketterm + ") are true, then the Expression is true<ul>";
					else Result.Explanation += "If " + postterm + " AND (" + bracketterm + ") are true, then the Expression is true<ul>";
					if(posttermresult.TruthValue === true && brackettermresult.TruthValue === true) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
						Result.TruthValue = true;
						return Result;
					}
					else if(brackettermresult.TruthValue === false || posttermresult.TruthValue === false) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
						Result.TruthValue = false;
						return Result;
					}
					else {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
				}
				else if(postoperator == "|") {
					if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "If " + postterm + " OR <b>NOT</b>(" + bracketterm + ") are true, then the Expression is true<ul>";
					else Result.Explanation += "If " + postterm + " OR (" + bracketterm + ") are true, then the Expression is true<ul>";
					if(posttermresult.TruthValue === true || brackettermresult.TruthValue === true) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='green'><b>Therefore this Expression is true</b></font>";
						Result.TruthValue = true;
						return Result;
					}
					else if(brackettermresult.TruthValue === false && posttermresult.TruthValue === false) {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='red'><b>Therefore this Expression is false</b></font>";
						Result.TruthValue = false;
						return Result;
					}
					else {
						if(negate && brackettermresult.TruthValue !== undefined) Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li><b>NOT</b>" + brackettermresult.Explanation + "</li>";
						else Result.Explanation += "<li>Since: " + posttermresult.Explanation + "</li><li>" + brackettermresult.Explanation + "</li>";
						Result.Explanation += "</ul><font color='blue'><b>Therefore this Expression is undefined</b></font>";
						Result.TruthValue = undefined;
						return Result;
					}
				}
			}
			else if(prioroperator === undefined && postoperator === undefined) {
				if(negate && brackettermresult.TruthValue !== undefined) {
					Result.Explanation += "<ul><li><b>NOT</b>"+ brackettermresult.Explanation + "</li></ul>";
					Result.TruthValue = brackettermresult.TruthValue;
					return Result;
				}
				else {
					Result.Explanation += "<ul><li>" + brackettermresult.Explanation + "</li></ul>";
					Result.TruthValue = brackettermresult.TruthValue;
					return Result;
				}
			}
		}	
	}
	$scope.$watch('ContentType', function() {
		if(!$scope.IsTest) $scope.GetQuestion();
		else $scope.GetTestQuestions();
	});
});

