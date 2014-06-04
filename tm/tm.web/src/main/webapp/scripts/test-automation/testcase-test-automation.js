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
 - canModify : a boolean telling if the associated script can be changed or not 
 - testAutomationURL : the url where to GET - POST - DELETE things.
 */
define([ "jquery", "workspace.event-bus", "squash.translator", "squash.configmanager", "tree/plugins/plugin-factory",
		"jquery.squash.formdialog", "squashtest/jquery.squash.popuperror", "jeditable" ], function($, eventBus, translator, confman, 
		treefactory) {

	
	// ************* specific jeditable plugin ***************
	
	/*
	 * We need a specific plugin because we need the buttons panel 
	 * to have a third button.
	 * 
	 * We base our plugin on the 'text' builtin plugin.
	 * 
	 */
	
	var edObj = $.extend(true, {},$.editable.types.text);
	var edFnButtons = $.editable.types.defaults.buttons;
	
	edObj.buttons = function(settings, original){
		//var form = this;
		//first apply the original function
		edFnButtons.call(this, settings, original);
		
		// now add our own button
		var btn = $("<button/>",{
			'text' : translator.get('label.dot.pick'),
			'id' : 'ta-script-picker-button'
		});
		
		this.append(btn);
	}
	
	$.editable.addInputType('ta-picker', edObj );
	
	
	// ****************** init function ********************
	
	function init(settings){
		
		
		// simple case first
		if (! settings.canModify){
			return;
		}
		
		// else we must init the special edit in place and the popups
		_initEditable(settings);
		_initPopup(settings);
		
	}
	
	function _initEditable(settings){
		
		var elt = $("#ta-script-picker-span");

		var conf = confman.getStdJeditable();
		conf.type = 'ta-picker';
		conf.name = 'path';
		
		
		// now make it editable
		elt.editable(settings.testAutomationURL, conf);
		
		// more events
		elt.on('click', '#ta-script-picker-button', function(){
			$("#ta-picker-popup").formDialog('open');
			return false;//for some reason jeditable would trigger 'submit' if we let go
		});

		
	}
	
	function _initPopup(settings){
		
		var dialog = $("#ta-picker-popup");
		
		var tree = dialog.find(".structure-tree");

		var error = dialog.find(".structure-error");
		error.popupError();

		// init

		dialog.formDialog({
			height : 500
		});

		// cache
		dialog.data('model-cache', undefined);



		// ************ model loading *************************
		
		var initDialogCache = function() {
			
			dialog.formDialog('setState', 'pleasewait');
			
			return $.ajax({
				url : settings.testAutomationURL,
				type : 'GET',
				dataType : 'json'
			})
			.done(function(json) {
				dialog.data('model-cache', json);
				createTree();
				dialog.formDialog('setState', 'main');
			})
			.fail(function(jsonError) {
				dialog.formDialog('close');
			});
		};

		var createTree = function() {

			treefactory.configure('simple-tree'); // will add the 'squash' plugin if doesn't exist yet
			tree.jstree({
				"json_data" : {
					"data" : dialog.data('model-cache')
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
					"url" : squashtm.app.contextRoot + "/styles/squash.tree.css"
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


		var submit = function() {

			try {

				var node = tree.jstree('get_selected');

				if (node.length < 1) {
					throw "no-selection";
				}

				var nodePath = node.getPath();

				$("#ta-script-picker-span").find('form input[name="path"]').val(nodePath);
				dialog.formDialog('close');

			} catch (exception) {
				if (exception == "no-selection") {
					error.find("span").text(translator.get('test-case.testautomation.popup.error.noselect'));
				}
				error.popupError('show');
			}

		};

		// ************ events *********************

		dialog.on('formdialogconfirm', submit);

		dialog.on('formdialogcancel', function() {
			dialog.formDialog('close');
		});

		dialog.on("formdialogopen", function() {
			if (dialog.data('model-cache') === undefined) {
				dialog.initAjax = initDialogCache();
			} else {
				reset();
			}
		});

		dialog.on('formdialogclose', function() {
			if (dialog.initAjax) {
				dialog.initAjax.abort();
			}

		});
	}
	
	return {
		init : init
	};
});
