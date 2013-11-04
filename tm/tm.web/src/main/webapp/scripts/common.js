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
requirejs.config({
			packages : [
				"execution-processing",
				"contextual-content-handlers",
				"bugtracker",
				"file-upload",
				"squashtable",
				"issue-tables",
				"tree",
				"search",
				//cufs
				"custom-field-editor",
				"custom-field-binding",
				"custom-field-values",
				//entities management
				"teams-manager",
				"projects-manager",
				"project",
				"users-manager",
				"test-case-management",
				"test-case-folder-management",
				"test-case-library-management",
				"iteration-management",	
				"test-suite-management",
				"campaign-management",
				//workspaces
				"tc-workspace",
				"req-workspace",
				"camp-workspace"
			],
			/*
			 * rules for paths naming: 
			 * * third party lib: unversionned lib name 
			 * * non AMD squash lib: replace "squashtm" by "squash" in js file name and remove any unrequired "ext" suffix.
			 */
			paths : {
				/*
				 * CAVEAT: as we defined a "jquery" path, any module named
				 * "jquery/my.module" will be interpolated as
				 * "/lib/jquery/../my.module"
				 */
				"datatables" : "datatables/jquery.dataTables.min",
				//lib
				"domReady" : "lib/require/domReady",
				"jquery" : "lib/jquery/jquery-1.8.3.min",
				"jqueryui" : "lib/jquery/jquery-ui-1.9.0.custom.min",
				"handlebars" : "lib/handlebars/handlebars-1.0.0.rc3",
				"underscore" : "lib/underscore/underscore-1.4.4-min",
				"backbone" : "lib/backbone/backbone-1.0.0-min",
				"jqplot" : "lib/jqplot/jquery.jqplot.min",
				"jqplot-pie" : "lib/jqplot/plugins/jqplot.pieRenderer.min",
				"jqplot-dates" : "lib/jqplot/plugins/jqplot.dateAxisRenderer.min",
				"excanvas" : "lib/excanvas.min",
				//cke
				"ckeditor" : "ckeditor/ckeditor",
				"jquery.ckeditor" : "ckeditor/adapters/jquery",
				//jeditable
				"jeditable" : "jquery/jquery.jeditable.mini.authored",
				"jeditable.ckeditor" : "jquery/jquery.jeditable.ckeditor",
				"jeditable.datepicker" : "jquery/jquery.jeditable.datepicker",
				"jeditable.simpleJEditable" : "squashtest/classes/SimpleJEditable",
				"jeditable.selectJEditable" : "squashtest/classes/SelectJEditable",
				//jquery
				"jstree" : "jquery/jquery.jstree",
				"jform" : "jquery/jquery.form",
				"jquery.dialog-patch" : "jquery/jquery.dialog-patch",
				"jquery.throttle-debounce" : "jquery/jquery.ba-throttle-debounce.min",
				"jquery.generateId" : "jquery/jquery.generateId",
				"jquery.hotkeys" : "jquery/jquery.hotkeys-0.8",
				"jquery.timepicker" : "jquery/jquery-ui-timepicker-addon",
				"jquery.cookie" : "jquery/jquery.cookie",
				"jquery.tagit" : "jquery/tag-it.min",
				//squashtest
				"jquery.squash" : "squashtest/jquery.squash.plugin",
				"jquery.squash.rangedatepicker" : "squashtest/jquery.squash.rangedatepicker",
				"jquery.squash.togglepanel" : "squashtest/jquery.squash.togglepanels",
				"jquery.squash.messagedialog" : "squashtest/jquery.squash.messagedialog",
				"jquery.squash.confirmdialog" : "squashtest/jquery.squash.confirmdialog",
				"jquery.squash.oneshotdialog" : "squashtest/jquery.squash.oneshotdialog",
				"jquery.squash.squashbutton" : "squashtest/jquery.squash.squashbutton",
				"jquery.squash.jeditable" : "squashtest/jquery.squash.jeditable.ext",
				"squash.session-pinger" : "squashtest/jquery.squash.session-pinger",
				"jquery.squash.tagit" : "squashtest/jquery.squash.tagit",
				"jquery.squash.buttonmenu" : "squashtest/jquery.squash.buttonmenu",
				"jquery.squash.formdialog" : "squashtest/jquery.squash.formdialog",
				"jquery.switchButton" : "jquery/jquery.switchButton",
				"jquery.squash.add-attachment-popup" : "squashtest/add-attachment-popup",
				//squash
				"jquery.squash.projectpicker" : "squash/jquery.squash.projectpicker",
				"jquery.squash.fragmenttabs" : "squash/squash.fragmenttabs",
				"squash.cssloader" : "squash/squash.cssloader",
				"squash.translator" : "squash/squash.translator",
				"squash.resizer" : "squash/squash.tree-page-resizer",
				"squash.basicwidgets" : "squash/squash.basicwidgets",
				"squash.attributeparser" : "squash/squash.attributeparser",
				"squash.configmanager" : "squash/squash.configmanager",
				//workspace
				"workspace.tree-node-copier" : "workspace/workspace.tree-node-copier",
				"workspace.tree-event-handler" : "workspace/workspace.tree-event-handler",
				"workspace.permissions-rules-broker" : "workspace/workspace.permissions-rules-broker",
				"workspace.contextual-content" : "workspace/workspace.contextual-content",
				"workspace.event-bus" : "workspace/workspace.event-bus"
				
			},
			shim : {
				"ckeditor" : {
					exports : "CKEDITOR"
				},
				"jquery.ckeditor" : {
					deps : [ "jquery", "jqueryui", "jquery.dialog-patch" , "ckeditor"],
					exports : "jqueryCkeditor"
				},
				"jeditable" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "jeditable"
				},
				"jeditable.ckeditor" : {
					deps : [ "jeditable", "jquery.ckeditor",
							"jquery.generateId" ],
					exports : "jeditableCkeditor"
				},
				"jeditable.datepicker" : {
					deps : [ "jeditable" ],
					exports : "jeditableDatepicker"
				},
				"jeditable.simpleJEditable" : {
					deps : [ "jquery.squash.jeditable" ],
					exports : "SimpleJEditable"
				},
				"jeditable.selectJEditable" : {
					deps : [ "jquery.squash.jeditable" ],
					exports : "SelectJEditable"
				},
				"jstree" : {
					deps : [ "jquery", "jqueryui", "jquery.hotkeys",
							"jquery.cookie" ],
					exports : "jqueryui"
				},
				"jform" : [ "jquery" ],
				"jquery.throttle-debounce" : ["jquery"],
				"jqueryui" : [ "jquery" ],
				"datatables" : [ "jqueryui" ],
				"jquery.dialog-patch" : [ "jqueryui" ],
				"jquery.squash" : {
					deps : [ "jquery" ],
					exports : "squashtm.popup"
				},
				"jquery.squash.fragmenttabs" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "squashtm.fragmenttabs"
				},
				"jquery.squash.togglepanel" : {
					deps : [ "jquery", "jqueryui", "jquery.squash.squashbutton" ],
					exports : "jquerySquashtmTogglepanel"
				},
				"jquery.squash.messagedialog" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "jquerySquashMessageDialog"
				},
				"jquery.squash.confirmdialog" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "jquerySquashConfirmDialog"
				},
				"jquery.squash.oneshotdialog" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "jquerySquashOneShotConfirm"
				},
				"jquery.squash.jeditable" : {
					deps : [ "jquery", "jeditable", "jeditable.ckeditor" ],
					exports : "jquerySquashtmJeditable"
				},
				"jquery.squash.projectpicker" : {
					deps : [ "jquery", "jqueryui", "jquery.squash.confirmdialog" ],
					exports : "jquerySquashtmProjectPicker"
				},
				"jquery.squash.squashbutton" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "$.squash"
				},
				"jquery.cookie" : {
					deps : [ "jquery" ],
					exports : "jqueryCookie"
				},
				"handlebars" : {
					deps : [ "jquery" ],
					exports : "Handlebars"
				},
				"underscore" : {
					exports : "_"
				},
				"backbone" : {
					deps : [ "underscore", "jquery" ],
					exports : "Backbone"
				},
				"jquery.tagit" : {
					deps : ["jquery", "jqueryui"],
					exports : "tagit"
				},
				"jquery.switchButton" : ["jquery", "jqueryui"],

				"jqplot" : {
					deps : ["jquery"],
					exports : "jqplot"
				},
				"jqplot-pie" : {
					deps : ["jquery", "jqplot"],
					exports : "jqplot-pie"
				},
				
				"jqplot-dates" : {
					deps : ["jquery", "jqplot"],
					exports : "jqplot-dates"
				}
			}
		});