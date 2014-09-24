/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/* https://github.com/requirejs/example-multipage-shim */
({
	appDir : "${project.basedir}/src/main/webapp", // root of webapp files
	mainConfigFile : "${project.basedir}/src/main/webapp/scripts/common.js",
	baseUrl : "scripts", // where the js files are supposed to be inside appDir
	dir : "${rjs.outputDirectory}", // output of optimizer
	optimize : "${rjs.optimizer}",
	skipDirOptimize : true, // only want to minify the build layers specified in modules options and not the rest of the
							// JS files in the build output directory
	generateSourceMaps : true, // maps enables modern browsers to fetch pretty source files from ugly ones
	preserveLicenseComments : false,
	optimizeCss : "none", // already done by wro

	// add the modules to optimize below. rjs will pick up dependencies.
	// you should explicitly exclude the "common" module
	// you should explicitly include nested require directives (cf. the "login-page" example)
	modules : [
			{
				name : "common",
				// List common dependencies here. Only need to list
				// top level dependencies, "include" will find
				// nested dependencies.
				include : [
				// shimmed
				"jqueryui", "datatables", "handlebars", "underscore", "backbone", "ckeditor", "jquery.ckeditor",
						"jeditable", "jeditable.ckeditor", "jeditable.datepicker", "jeditable.simpleJEditable",
						"jeditable.selectJEditable", "jstree", "jform", "jquery.generateId", "jquery.hotkeys",
						"jquery.timepicker", "jquery.squash", "jquery.squash.fragmenttabs",
						"jquery.squash.togglepanel", "jquery.squash.messagedialog", "jquery.squash.confirmdialog",
						"jquery.squash.oneshotdialog", "jquery.squash.squashbutton", "jquery.squash.jeditable",
						"jquery.squash.projectpicker", "jquery.cookie", "jquery.tagit", "jquery.switchButton",
						"jqplot-core", "jqplot-pie", "squash.KeyEventListener",
						// AMD
						"domReady", "jquery", "squash.cssloader", "squash.translator", "squash.resizer",
						"squash.session-pinger", "jquery.squash.tagit",
						// TODO remove this one ?
						"workspace.contextual-content", "workspace.storage", "workspace.routing",
						"squash.translator"]
			},
			{
				name : "login-page",
				// we have to tell rjs which dependencies to include because
				// they are not "top level" dependencies, which prevents rjs to discover them.
				include : [ "jquery", "app/pubsub", "app/ws/squashtm.notification", "jqueryui",
						"jquery.squash.squashbutton" ],
				exclude : [ "common" ]
			},
			{
				name : "advanced-search-input",
				include : [ "search/advanced-search-input", "app/ws/squashtm.workspace" ],
				exclude : [ "common" ]
			},
			{
				name : "custom-field-manager",
				include : [ "custom-field-manager/CustomFieldsTableView", "app/ws/squashtm.workspace" ],
				exclude : [ "common" ]
			},
			{
				name : "custom-field-modification",
				include : [ "custom-field-editor/CustomFieldModificationView", "app/ws/squashtm.workspace" ],
				exclude : [ "common" ]
			},
			{
				name : "test-automation-servers-manager",
				include : [ "test-automation-servers-manager/TestAutomationServersTableView",
						"app/ws/squashtm.workspace" ],
				exclude : [ "common" ]
			},
			{
				name : "edit-test-step",
				include : [ "jquery", "squash.basicwidgets", "test-step-editor/TestStepModificationView",
						"app/ws/squashtm.workspace" ],
				exclude : [ "common" ]
			},
			{
				name : "home-workspace",
				include : [ "app/ws/squashtm.workspace" ],
				exclude : [ "common" ]
			},
			{
				name : "index-manager",
				include : [ "app/ws/squashtm.workspace", "search/index-administration-view" ],
				exclude : [ "common" ]
			},
			{
				name : "legacy-ws-page",
				include : [ "jquery", "app/ws/squashtm.workspace" ],
				exclude : [ "common" ]
			},
			{
				name : "print",
				include : [ "domReady", "page-components/general-information-panel" ],
				exclude : [ "common" ]
			},
			{
				name : "project-manager",
				include : [ "app/pubsub", "projects-manager/show-projects/ProjectsManager" ],
				exclude : [ "common" ]
			},
			{
				name : "project-page",
				include : [],
				exclude : [ "common" ]
			},
			{
				name : "report-workspace",
				include : [ "app/ws/squashtm.workspace", "app/report/squashtm.reportworkspace" ],
				exclude : [ "common" ]
			},
			{
				name : "user-page",
				include : [ "jquery", "user-editor/UserModificationView", "app/ws/squashtm.workspace", "app/pubsub" ],
				exclude : [ "common" ]
			},
			{
				name : "iteration-page",
				include : [ "app/pubsub", "squash.basicwidgets", "contextual-content-handlers", "jquery.squash.fragmenttabs",
				            "bugtracker/bugtracker-panel", "workspace.event-bus", "iteration-management", "app/ws/squashtm.workspace",
				            "test-automation/auto-execution-buttons-panel" ],
				exclude : [ "common" ]
			},
			{
				name : "test-suite-page",
				include : [ "app/pubsub", "squash.translator", "squash.basicwidgets", "workspace.event-bus", "app/ws/squashtm.workspace",
				            "contextual-content-handlers", "jquery.squash.fragmenttabs", "bugtracker/bugtracker-panel", "test-suite-management",
				            "jquery.cookie", "test-suite/execution-buttons-panel", "test-automation/auto-execution-buttons-panel" ],
				exclude : [ "common" ]
			}
			]
}) // DONT ADD NO SEMICOLON!
