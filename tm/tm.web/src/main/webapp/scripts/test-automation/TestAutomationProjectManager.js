/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
/*
 settings :
 - selector  : a css selectorthat matches the root element of the manager
 - enableTAURL : the url where to post that test automation is enabled or disabled
 */
define(["jquery", "squashtable"], function($) {
	function TestAutomationProjectManager(settings) {

		var instance = $(settings.selector);
		var initiallyEnabled = settings.initiallyEnabled;
		var enableTAURL = settings.enableTAURL;

		var self = this;

		// ************* graphic components ******************

		var mainCheckBox = instance.find(".ta-maincheck-div>input");

		var serverBlock = instance.find(".ta-server-block");

		var projectsBlock = instance.find(".ta-projects-block");

		var inputs = serverBlock.find("input");

		// ************** enhance the attributes ************

		projectsBlock.getTable = function() {
			// return projectsBlock.find(".ta-projects-table").squashTable(); //doesn't work anymore because this selector is
			// not the same than the one userd to create the squashTable instance
			return $("#ta-projects-table").squashTable();
		};

		serverBlock.getURL = function() {
			return serverBlock.find(".ta-serverblock-url-input").val();
		};

		serverBlock.getLogin = function() {
			return serverBlock.find(".ta-serverblock-login-input").val();
		};

		serverBlock.getPassword = function() {
			return serverBlock.find(".ta-serverblock-password-input").val();
		};

		// ************** private ****************************

		var disableManager = function() {
			inputs.attr('disabled', 'disabled');
			serverBlock.addClass("ta-manager-disabled");
		};

		var enableManager = function() {
			inputs.removeAttr('disabled', 'disabled');
			serverBlock.removeClass("ta-manager-disabled");
		};

		var updateUIState = function() {
			if (mainCheckBox.attr('checked')) {
				enableManager();
			} else {
				disableManager();
			}
		};

		var switchTestAutomationOnOff = function() {
			var enabled = mainCheckBox.is(':checked');
			$.post(enableTAURL, {
				enabled : enabled
			});
		};

		// ************** public getters **********************

		this.getProjectsBlock = function() {
			return projectsBlock;
		};

		this.getServerBlock = function() {
			return serverBlock;
		};

		// ************* event handlers ***********************

		mainCheckBox.change(function() {
			switchTestAutomationOnOff();
			updateUIState();
		});

		// ************* rest of the init code ***************

		updateUIState();

	}
	
	return TestAutomationProjectManager;
});