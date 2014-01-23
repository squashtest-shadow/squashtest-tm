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
/*
 * settings : - selector : the jquery selector that will fetch the popup back - manager : an instance of the class
 * defined just above - listProjectsURL : the url where to send the GET request - bindProjectsURL : where to post the
 * new projects to bind
 * 
 */
define([ "jquery", "app/ws/squashtm.notification", "squashtest/jquery.squash.popuperror", "jqueryui" ], function($, WTF) {
	function TestAutomationAddProjectPopup(settings) {

		var self = this;

		var instance = $(settings.selector);
		var manager = settings.manager;
		var listProjectsURL = settings.listProjectsURL;
		var bindProjectsURL = settings.bindProjectsURL;

		var pleaseWaitPanel = instance.find(".ta-projectsadd-pleasewait");
		var mainPanel = instance.find(".ta-projectsadd-maindiv");

		// error panels
		var error = instance.find(".ta-projectsadd-error").popupError();
		var fatalError = instance.find(".ta-projectsadd-fatalerror").popupError();

		// additional behavior for fatal errors :
		fatalError.click(function() {
			instance.dialog('close');
		});

		// **************** properties enhancement ***********

		mainPanel.getListPanel = function() {
			return mainPanel.find(".ta-projectsadd-listdiv");
		};

		// **************** private ***************************

		var flipToPleaseWait = function() {
			pleaseWaitPanel.removeClass("not-displayed");
			mainPanel.addClass("not-displayed");
			fatalError.popupError('hide');
			error.popupError('hide');
		};

		var flipToMain = function() {
			pleaseWaitPanel.addClass("not-displayed");
			mainPanel.removeClass("not-displayed");
		};

		var handleCkboxChange = function() {
			$(this).parent().toggleClass('selected');
		};

		var projnameClick = function() {
			$(this).prev().click();
		};

		var newItem = function(jsonItem) {
			var item = $('<div class="listdiv-item"></div>');

			var projName = $('<span>' + jsonItem.name + '</span>');
			var chkbox = $('<input type="checkbox"/>');

			chkbox.data('project-name', jsonItem.name);
			chkbox.change(handleCkboxChange);

			projName.click(projnameClick);

			item.append(chkbox);
			item.append(projName);
			return item;
		};

		var populate = $.proxy(function(json) {
			var listPanel = mainPanel.getListPanel();
			listPanel.empty();

			var i = 0;
			var items = $();

			for (i = 0; i < json.length; i++) {
				var item = newItem(json[i]);
				items = items.add(item);
			}

			items.filter("div:odd").addClass("odd");
			items.filter("div:even").addClass("even");

			listPanel.append(items);

		}, this);

		var loadContent = $.proxy(function() {
			flipToPleaseWait();

			var getURL = manager.getServerBlock().getURL();
			var login = manager.getServerBlock().getLogin();
			var password = manager.getServerBlock().getPassword();

			$.ajax({
				url : listProjectsURL,
				type : 'GET',
				dataType : 'json',
				global : false,
				data : {
					url : getURL,
					login : login,
					password : password
				}
			}).done(function(json) {
				populate(json);
				flipToMain();
			}).fail(function(jsonError) {
				var message = squashtm.notification.getErrorMessage(jsonError);
				fatalError.find('span').text(message);
				fatalError.popupError('show');
			});

		}, this);

		var cancel = function() {
			instance.dialog('close');
		};

		var getServerData = function() {
			var serverBlock = manager.getServerBlock();

			var serverURL = serverBlock.getURL();
			var login = serverBlock.getLogin();
			var password = serverBlock.getPassword();

			var serverData = {
				serverBaseURL : serverURL,
				serverLogin : login,
				serverPassword : password
			};

			return serverData;
		};

		var getProjectNames = function() {
			var selected = mainPanel.getListPanel().find("input:checked");
			return selected.map(function(i, box) {
				return {
					projectName : $(box).data('project-name')
				};
			});
		};

		var submit = function() {

			flipToPleaseWait();

			var serverData = getServerData();
			var projectNames = getProjectNames();

			var formData = [];
			var i = 0;

			for (i = 0; i < projectNames.length; i++) {
				var formItem = $.extend({}, projectNames[i], serverData);
				formData.push(formItem);
			}

			$.ajax({
				url : bindProjectsURL,
				type : 'POST',
				contentType : 'application/json',
				dataType : 'json',
				global : false,
				data : JSON.stringify(formData)
			}).done(function() {
				instance.dialog('close');
				manager.getProjectsBlock().getTable().fnDraw();
			}).fail(function(jsonError) {
				flipToMain();
				var message = WTF.getErrorMessage(jsonError);
				error.find('span').text(message);
				error.popupError('show');
			});

		};

		// **************** events ******************************

		instance.bind("dialogopen", loadContent);
		var buttons = instance.dialog('option', 'buttons');

		buttons[0].click = submit;
		buttons[1].click = cancel;

	}

	return TestAutomationAddProjectPopup;
});