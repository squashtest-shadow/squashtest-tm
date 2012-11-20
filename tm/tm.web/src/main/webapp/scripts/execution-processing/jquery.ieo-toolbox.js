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
 * That widget is completely passive : it just 
 * provides a GUI, the manager handles the event
 * and tells it what to do next. 
 */

define(["jquery", "module", "jquery.cookie", "jqueryui"], function($,module){
	
	$.widget("squash.optimizedToolbox", $.ui.draggable, {
		
		options : {},
		
		_create : function(){
			
			var self=this;
			
			
			var positionLeft = $.cookie("ieo-toolbox-position-left");
			var positionTop = $.cookie("ieo-toolbox-position-top");
			
			if ( positionLeft != null && positionTop != null ) {
				this.offset({top : positionTop, left: positionLeft});
			}	
			
			$.ui.dialog.prototype._create.call(this,{
				start: function(event, ui){
					$(".iframe-container").addClass('not-visible');
				},	
				stop: function(event, ui){
					$(".iframe-container").removeClass('not-visible');
					var pos = this.offset();
					$.cookie("ieo-toolbox-position-left", pos.left);
					$.cookie("ieo-toolbox-position-top", pos.top);
				}					
			});
	
			
			// ************* slider init *****************
			
			this._initSlider();

			
			
			// ******** buttons init **********
			
			this.find(".execute-next-step").button({
				'text': false,
				icons: {
					primary : 'ui-icon-triangle-1-e'
				}
			});
			
			this.find(".execute-previous-step").button({
				'text' : false,
				icons : {
					primary : 'ui-icon-triangle-1-w'
				}
			});
		
			this.find(".stop-execution").button({
				'text': false, 
				'icons' : {
					'primary' : 'ui-icon-power'
				} 
			});

			this.find(".step-failed").button({
				'text': false,
				'icons' :{
					'primary' : 'execute-failure'
				}
			});

			this.find(".step-succeeded").button({
				'text' : false,
				'icons' : {
					'primary' : 'execute-success'
				}
			});

			this.find(".execute-next-test-case").button({
				'text': false,
				icons: {
					primary : 'ui-icon-seek-next'
				}
			});
			
			
			this.find('.step-status-combo').change(function(){
				self._updateComboIcon();
			});

			
		},
		
		
		// **************** setters *****************

		setManager : function(manager){
			this.manager = manager;
			this._refreshUI();
		},

		
		setStatus : function(status){
			var cbox = this.find('.step-status-combo');
			cbox.val(status);
			this._updateComboIcon();
		},
		
		setSuccess : function(){
			this.setStatus("SUCCESS");
		},
		
		setFailure : function(){
			this.setStatus('FAILURE');
		},
		
		
		navigateNext : function(){
			this._refreshUI();
		},
		
		navigatePrevious : function(){
			this._refreshUI();
		},
		
		navigateRandom : function(stepIndex){
			this._refreshUI();
		},
		
		navigateToNewTestCase : function(){
			this._reset();
			this._refreshUI();
		},
		
		// ********************** getters ***************************

		getNextStepButton : function(){
			return this.find('.execute-next-step');
		},
		
		getPreviousStepButton : function(){
			return this.find('.execute-previous-step');
		},
		
		getStopButton : function(){
			return this.find('.stop-execution');
		},
		
		getFailedButton : function(){
			return this.find('.step-failed');
		},
		
		getSuccessButton : function(){
			return this.find('.step-succeeded');
		},
		
		getNextTestCaseButton : function(){
			return this.find('.execute-next-test-case');
		},
		
		getStatusCombo : function(){
			return this.find('.step-status-combo');
		},
		
		getSlider : function(){
			return this.find('.slider');
		},
		
		_getState : function(){
			return this.manager.getState();
		},
		
		// ********************** predicates ************************
		
		var _canNavigateNextTestCase : function(){
			var state = this._getState();
			return ((state.testSuiteMode) &&  (! state.isLastTestCase) && (this._isLastStep()));				
		},
		
		var _isLastStep : function(){
			var state = this._getState();
			return (state.currentStepIndex===state.lastStepIndex);			
		},
		
		var _isPrologue : function(){
			var state = this._getState();
			return (state.currentStepIndex===state.firstStepIndex);
		},

		
		// ************************ update methods ********************
		
		_initSlider : function(){
			var self = this;
			
			var settings = (!!this.manager) ? this._getState() : { lastStepIndex : 0, currentStepIndex : 0 };
			
			var slider = this.getSlider();
			
			slider.slider('destroy');
			
			var sliderSettings = {
				range: "max",
				min: 0,
				max: settings.lastStepIndex,
				value: settings.currentStepIndex,
				stop: function( event, ui ) {
					self.manager.navigateRandom(ui.value);
				}
			};

			slider.slider(sliderSettings});
			
		},
		
		_reset : function(){
			this._initSlider();
		},
		
		_updateComboIcon : function(){
			var cbox = this.getSatusCombo();
			
			//reset the classes
			cbox.attr("class","");
			
			cbox.addClass("execution-status-combo-class");
			
			//find and set the new class
			var selectedIndex = cbox.get(0).selectedIndex;
			var selector = "option:eq(" + selectedIndex + ")";
			
			var className = cbox.find(selector).attr("class");
			
			cbox.addClass(className);			
		},
		
		_updateCounter : function(){
			var label = this.find('step-paging');
			var state = this._getState();
			var labelText = state.currentStepIndex+" / ("+state.lastStepIndex+")";
			label.text(labelText);
		},
		
		_updateButtons : function(){
			
			var btnState = (this._isLastStep()) ? "disable" : "enable";
			this.getNextButton().button(btnState);
			
			btnState = (this._isPrologue()) ? "disable" : "enable";
			this.getPreviousButton().button(btnState);
			
			if (this._getState().testSuiteMode){
				this.find('.execute-next-test-case-panel').show();
			}
			else{
				this.find('.execute-next-test-case-panel').hide();
			}
			btnstate = (this._canNavigateNextTestCase()) ? "enable" : "disable";
			this.getNextTestCaseButton().button(btnState);
			
		},
		
		_updateSlider : function(){
			this.getSlider().slider("option", "value", this.options.currentStepIndex);
		},
		
		_updateComboEnable : function(){
			if (this._isPrologue()){
				this.getStatusCombo().prop('disabled', true);
			}
			else{
				this.getStatusCombo().prop('disabled', false);
			}
		},
		
		_updateComboStatus : function(){
			var state = this._getState();
			this.getStatusCombo().val(state.currentStepStatus);
		},
		
		_updateCombo : function(){
			_updateComboStatus();
			_updateComboIcon();
			_updateComboEnable();
		},
		
		_refreshUI : function(){
			this._updateCounter();
			this._updateButtons();
			this._updateCombo();
			this._updateSlider();
			this._updateNextTCButton();
		}
		
		
	});
	
	
})