/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
 * That widget is completely passive : it just provides a GUI, the manager handles the event and tells it what to do
 * next.
 */

define([ "jquery", "module", "jquery.cookie", "jqueryui" ], function($, module) {

	$.widget("squash.ieoControl", {

		options : {

		},

		_create : function() {

			var self = this;

			var positionLeft = $.cookie("ieo-toolbox-position-left");
			var positionTop = $.cookie("ieo-toolbox-position-top");

			if ((!!positionLeft) && (!!positionTop)) {
				this.element.offset({
					top : positionTop,
					left : positionLeft
				});
			}

			this.element.draggable({
				stop : function(event, ui) {
					var pos = $(this).offset();
					$.cookie("ieo-toolbox-position-left", pos.left);
					$.cookie("ieo-toolbox-position-top", pos.top);
				}
			});

			// ************* slider init
			// *****************

			this._initSlider();

			// ******** buttons init **********

			this.getNextStepButton().button({
				'text' : false,
				icons : {
					primary : 'ui-icon-triangle-1-e'
				}
			});

			this.getPreviousStepButton().button({
				'text' : false,
				icons : {
					primary : 'ui-icon-triangle-1-w'
				}
			});

			this.getStopButton().button({
				'text' : false,
				'icons' : {
					'primary' : 'ui-icon-power'
				}
			});

			this.getUntestableButton().button({
				'text' : false,
				'icons' : {
					'primary' : 'exec-status-untestable'
				}
			});

			this.getBlockedButton().button({
				'text' : false,
				'icons' : {
					'primary' : 'exec-status-blocked'
				}
			});

			this.getFailedButton().button({
				'text' : false,
				'icons' : {
					'primary' : 'exec-status-failure'
				}
			});

			this.getSuccessButton().button({
				'text' : false,
				'icons' : {
					'primary' : 'exec-status-success'
				}
			});

			this.getNextTestCaseButton().button({
				'text' : false,
				icons : {
					primary : 'ui-icon-seek-next'
				}
			});

			this.getStatusCombo().change(function() {
				self._updateComboIcon();
			});

		},

		// **************** setters *****************

		setManager : function(manager) {
			this.element.manager = manager;
			this._reset();
		},

		setStatus : function(status) {
			var cbox = this.getStatusCombo();
			cbox.val(status);
			this._updateComboIcon();
		},
		
		setUntestable : function(){
			this.setStatus('UNTESTABLE');
		},
		
		setBlocked : function(){
			this.setStatus('BLOCKED');
		},

		setSuccess : function() {
			this.setStatus("SUCCESS");
		},

		setFailure : function() {
			this.setStatus('FAILURE');
		},

		navigateNext : function() {
			this._refreshUI();
		},

		navigatePrevious : function() {
			this._refreshUI();
		},

		navigateRandom : function(stepIndex) {
			this._refreshUI();
		},

		navigateToNewTestCase : function() {
			this._reset();
			this._refreshUI();
		},

		// ********************** getters
		// ***************************

		getNextStepButton : function() {
			return this.element.find('.execute-next-step');
		},

		getPreviousStepButton : function() {
			return this.element.find('.execute-previous-step');
		},

		getStopButton : function() {
			return this.element.find('.stop-execution');
		},
		
		getUntestableButton : function(){
			return this.element.find('.step-untestable');
		},
		
		getBlockedButton : function(){
			return this.element.find('.step-blocked');
		},

		getFailedButton : function() {
			return this.element.find('.step-failed');
		},

		getSuccessButton : function() {
			return this.element.find('.step-succeeded');
		},

		getNextTestCaseButton : function() {
			return this.element.find('.execute-next-test-case');
		},

		getStatusCombo : function() {
			return this.element.find('.execution-status-combo-class');
		},

		getSlider : function() {
			return this.element.find('.slider');
		},

		_getState : function() {
			return this.element.manager.getState();
		},

		// ********************** predicates
		// ************************

		_canNavigateNextTestCase : function() {
			var state = this._getState();
			return ((state.testSuiteMode) && (!state.lastTestCase) && (this._isLastStep()));
		},

		_isLastStep : function() {
			var state = this._getState();
			return (state.currentStepIndex === state.lastStepIndex);
		},

		_isPrologue : function() {
			var state = this._getState();
			return (state.currentStepIndex === state.firstStepIndex);
		},

		// ************************ update methods
		// ********************

		_initSlider : function() {
			var self = this;

			var settings = (!!this.element.manager) ? this._getState() : {
				lastStepIndex : 0,
				currentStepIndex : 0
			};

			var slider = this.getSlider();

			try {
				slider.slider('destroy');
			} catch (ex) {
				// well okay, no big deal.
			}

			var sliderSettings = {
				range : "max",
				min : 0,
				max : settings.lastStepIndex,
				value : settings.currentStepIndex,
				stop : function(event, ui) {
					self.element.manager.navigateRandom(ui.value);
				}
			};

			slider.slider(sliderSettings);

		},

		_reset : function() {
			this._initSlider();
			this._refreshUI();
		},

		_updateComboIcon : function() {
			var cbox = this.getStatusCombo();

			// reset the classes
			cbox.attr("class", "");

			cbox.addClass("execution-status-combo-class");

			// find and set the new class
			var selectedIndex = cbox.get(0).selectedIndex;
			var selector = "option:eq(" + selectedIndex + ")";

			var className = cbox.find(selector).attr("class");

			cbox.addClass(className);
		},

		_updateCounter : function() {
			var label = this.element.find('.step-paging');
			var state = this._getState();
			var labelText = state.currentStepIndex + " / " + state.lastStepIndex;
			label.text(labelText);
		},

		_updateButtons : function() {

			var btnState = (this._isLastStep()) ? "disable" : "enable";
			this.getNextStepButton().button(btnState);

			btnState = (this._isPrologue()) ? "disable" : "enable";
			this.getPreviousStepButton().button(btnState);
			
			this.getUntestableButton().button(btnState);
			this.getBlockedButton().button(btnState);
			this.getSuccessButton().button(btnState);
			this.getFailedButton().button(btnState);

			if (this._getState().testSuiteMode) {
				this.element.find('.execute-next-test-case-panel').show();
			} else {
				this.element.find('.execute-next-test-case-panel').hide();
			}
			btnState = (this._canNavigateNextTestCase()) ? "enable" : "disable";
			this.getNextTestCaseButton().button(btnState);

		},

		_updateSlider : function() {
			this.getSlider().slider("option", "value", this._getState().currentStepIndex);
		},

		_updateComboEnable : function() {
			if (this._isPrologue()) {
				this.getStatusCombo().prop('disabled', true);
			} else {
				this.getStatusCombo().prop('disabled', false);
			}
		},

		_updateComboStatus : function() {
			var state = this._getState();
			this.getStatusCombo().val(state.currentStepStatus);
		},

		_updateCombo : function() {
			this._updateComboStatus();
			this._updateComboIcon();
			this._updateComboEnable();
		},

		_refreshUI : function() {
			this.element.removeClass('not-displayed');
			this._updateCounter();
			this._updateButtons();
			this._updateCombo();
			this._updateSlider();
		}

	});

});