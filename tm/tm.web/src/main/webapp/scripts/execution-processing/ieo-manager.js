/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * conf : must be available as config for the main module.
 * 
 * {
 * 	completeTitle : "title for the completion message dialog",
 *  completeTestMessage : "content for the completion message dialog"
 *  completeSuiteMessage : "content for the completion of the whole suite message dialog"
 * 
 * }
 * 
 * + the rest described as in the 'state' variable below
 * 
 * comment[1] [Issue 1126] Had to use directly "refreshParent" instead of "iframe.unload(refreshParent)" beacause the latest do not work with IE 8  
 * 
 */



define(["jquery", "module", "jquery.squash.messagedialog"], function($,module){
	
	var settings = module.config();
	
	
	/* this is a constructor */
	return function() {

		// ***************** init function **********************
		
		this.state = $.extend({
				
				isOptimized : undefined,
				isLastTestCase : undefined,
				testSuiteMode : undefined,
				isPrologue : undefined,
				
				baseStepUrl : undefined,
				nextTestCaseUrl : undefined,
				
				currentExecutionId : undefined,
				currentStepId : undefined,
				
				firstStepIndex : undefined,
				lastStepIndex : undefined,
				currentStepIndex : undefined,
				
				currentStepStatus : undefined
				
				
		}, settings);
		


		_updateState(settings);

		// ***************** private stuffs ****************
		
		var _updateState = function(newState){
			this.state = newState;
		};
		
		
		var getJson = function(url){
			return $.get(url, null, null, "json")			
		};
		

		var refreshParent = function(){
			window.opener.location.href = window.opener.location.href;
			if (window.opener.progressWindow) {
				window.opener.progressWindow.close();
			}
		};
		
		
		var testComplete = function(){
			if (! this.state.testSuiteMode){
				$.squash.openMessage(settings.completeTitle, settings.completeTestMessage ).done(function() {
					refreshParent();// see "comment[1]"
					window.close();
				});
			} else if (canNavigateNextTestCase()){
				this.navigateNextTestCase();	
				refreshParent();
			}
			else{
				$.squash.openMessage(settings.completeTitle, settings.completeSuiteMessage).done(function() {
					refreshParent();// see "comment[1]"
					window.close();
	 			});				
			}
			
		};
		
		var navigateLeftPanel = function(url){
			parent.frameleft.document.location.href = url;
		};
		
		//************ public functions ****************
		
		this.fillRightFrame = function(url){
			this.rightFrame.attr('src', url);
		};
		
		this.navigateNext = function(){
			var state = this.state;
			
			if (! isLastStep()){
				var nextStep = state.currentStepIndex + 1;
				this.navigateRandom(nextStep);
			}
			else{
				testComplete();
			}
		};
		
		this.navigatePrevious = function(){
			var state = this.state;
			
			if (! isPrologue()){
				var prevStep = state.currentStepIndex - 1;
				this.navigateRandom(prevStep);
			}			
		};
		
		this.navigatePrologue = function(){
			var state = this.state;
			var url = state.baseStepUrl+"prologue?optimized=true&suitemode="+state.testSuiteMode;
			navigateLeftPanel(url);
			this.toolbox._refreshUI();		
		};
		
		
		this.navigateRandom = function(newStepIndex){
			var state = this.state;
			
			if (newStepIndex === 0){
				this.navigatePrologue();
			}
			else{
				getJson(nextUrl)
				.success(function(json){
					
					state.currentStepStatus = json.currentStepStatus;
					state.currentStepId = json.currentStepId;
					
					var frameLeftUrl = state.baseStepUrl+newStepIndex+"?optimized=true&suitemode="+state.testSuiteMode;
					navigateLeftPanel(frameLeftUrl);	
					
					this.toolbox.optimizedToolbox("navigateRandom");				
				});				
			}
			state.currentStepIndex = newStepIndex;

		};
		
		this.navigateNextTestCase = function(){			
			getJson(nextTestCaseUrl)
			.success(function(json){
				_updateState(json);
				this.toolbox.optimizedToolbox("navigateToNewTestCase");
			});
		};
		
		this.closeWindow = function(){
			window.close();
		};


		this.getState = function(){
			return state;
		};
		
		// ********************** predicates ************************
		
		var canNavigateNextTestCase = function(){
			var state = this.state;
			return ((state.testSuiteMode) &&  (! state.isLastTestCase) && (this._isLastStep()));			
		};
		
		var isLastStep = function(){
			return (this.currentStepIndex===this.state.lastStepIndex);			
		};
		
		var isPrologue = function(){
			return (this.state.currentStepIndex===this.state.firstStepIndex);
		};
		
		
		// *********** setters etc *********************

		
		this.setToolbox = function(toolbox){
			
			var self = this;
			
			this.toolbox = toolbox;			
			toolbox.setManager(this);
			
			var nextButton = toolbox.optimizedToolbox("getNextStepButton");
			var prevButton = toolbox.optimizedToolbox("getPreviousStepButton");
			var stopButton = toolbox.optimizedToolbox("getStopButton");
			var succButton = toolbox.optimizedToolbox("getSuccessButton");
			var failButton = toolbox.optimizedToolbox("getFailureButton");
			var mvTCButton = toolbox.optimizedToolbox("getNextTestCaseButton");
			var statusCombo = toolbox.optimizedToolbox("getStatusCombo");
			
			nextButton.click(function(){
				self.navigateNext();
			});
			
			prevButton.click(function(){
				self.navigatePrevious();
			});
			
			mvTCButton.click(function(){
				self.navigateNextTestCase();		
			});	
			
			stopButton.click(function(){
				window.close();
			});
			
			statusCombo.change(function(){
				var self = this;
				$.post(state.statusUrl, {
					executionStatus : $(self).val()
				});
			});
			
			succButton.click(function(){
				$.post(state.statusUrl,{
					executionStatus : "SUCCESS"
				})
				.success(function(){
					toolbox.optimizedToolbox("setSuccess");
				});
			});
			
			failButton.click(function(){
				$.post(state.statusUrl,{
					executionStatus : "FAILURE"
				})
				.success(function(){
					toolbox.optimizedToolbox("setFailure");
				});			
			});
		
		};
		
		this.setRightFrame = function(rightFrame){
			this.rightFrame = rightFrame;
		};
		
	}
	
});