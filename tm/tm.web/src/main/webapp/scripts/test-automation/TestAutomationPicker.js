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
 settings : 
 - selector : an appropriate selector for the popup.
 - testAutomationURL : the url where to GET - POST things.
 - baseURL : the base url of the app.
 - successCallback : a callback if the test association succeeds. Will be given 1 argument, the path of the associated automated test.
 - messages : 
 - noTestSelected : message that must be displayed when nothing is selected
 */
define(["jquery", "squashtest/jquery.squash.popuperror"], function() {
	function TestAutomationPicker(settings) {
		var self = this;
	
		var instance = $(settings.selector);
		var testAutomationURL = settings.testAutomationURL;
		var baseURL = settings.baseURL;
		var successCallback = settings.successCallback;
	
		var pleaseWaitPanel = instance.find(".structure-pleasewait");
		var mainPanel = instance.find(".structure-treepanel");
		var tree = instance.find(".structure-tree");
	
		var error = instance.find(".structure-error");
		error.popupError();
	
		// cache
		this.modelCache = undefined;
	
		// ************ state handling ******************
	
		var flipToPleaseWait = function() {
			pleaseWaitPanel.removeClass("not-displayed");
			mainPanel.addClass("not-displayed");
			error.popup('hide');
		};
	
		var flipToMain = function() {
			pleaseWaitPanel.addClass("not-displayed");
			mainPanel.removeClass("not-displayed");
		};
	
		var getPostParams = function() {
	
			var node = tree.jstree('get_selected');
	
			if (node.length < 1) {
				throw "no-selection";
			}
			
	
			var nodePath = node.getPath();
			// let's strip the 'library' part
			var nodeName = nodePath.replace(/^[^\/]*\//, '');
	
			return {
				name : nodeName,
				projectId : node.getLibrary().getDomId()
			};
		};
	
		// ************ model *****************************
	
		var init = function() {
			return $.ajax({
				url : testAutomationURL,
				type : 'GET',
				dataType : 'json'
			}).done(function(json) {
				setCache(json);
				createTree();
				flipToMain();
			}).fail(function(jsonError) {
				handleAjaxError(jsonError);
			});
		};
	
		var setCache = function(model) {
			self.modelCache = model;
		};
	
		var createTree = function() {
	
			tree.jstree({
				"json_data" : {
					"data" : self.modelCache
				},
	
				"types" : {
					"types" : {
						"drive" : {
							"valid_children" : [ "ta-test", "folder" ],
							"select_node" : false
						},
						"ta-test" : {
							"valid_chidlren" : "none"
						},
						"folder" : {
							"valid_children" : [ "ta-test", "folder" ],
							"select_node" : false
						}
					}
				},
	
				"ui" : {
					select_multiple_modifier : false
				},
	
				"themes" : {
					"theme" : "squashtest",
					"dots" : true,
					"icons" : true,
					"url" : baseURL + "/styles/squash.tree.css"
				},
	
				"core" : {
					"animation" : 0
				},
	
				"plugins" : [ "json_data", "types", "ui", "themes", "squash" ]
	
			});
	
		};
	
		var reset = function() {
			if (tree.jstree('get_selected').length > 0) {
				tree.jstree('get_selected').deselect();
			}
		};
	
		// ****************** transaction ************
	
		var handleAjaxError = function(jsonError) {
			var message = squashtm.notification.getErrorMessage(jsonError);
			fatalError.find('span').text(message);
			fatalError.popupError('show');
		};
	
		var submit = function() {
	
			try {
	
				var params = getPostParams();
	
				$.ajax({
	
					url : testAutomationURL,
					type : 'POST',
					data : params,
					dataType : 'json'
	
				}).done(function() {
					instance.dialog('close');
					if (successCallback) {
						successCallback(tree.jstree('get_selected').getPath());
					}
				}).fail(function(jsonError) {
					handleAjaxError(jsonError);
				});
	
			} catch (exception) {
				if (exception == "no-selection") {
					error.find("span").text(settings.messages.noTestSelected);
				} else {
					error.find("span").text(exception);
				}
				error.popupError('show');
			}
	
		};
	
		// ************ events *********************
	
		instance.dialog('option').buttons[0].click = submit;
	
		instance.dialog('option').buttons[1].click = function() {
			instance.dialog('close');
		};
	
		instance.bind("dialogopen", function() {
			if (self.modelCache === undefined) {
				self.initAjax = init();
			} else {
				reset();
			}
		});
	
		instance.bind('dialogclose', function() {
			if (self.initAjax) {
				self.initAjax.abort();
			}
			
		});
	}
	
	return TestAutomationPicker;
});
