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
				"custom-field-editor",
				"custom-field-binding",
				"custom-field-values",
				"execution-processing",
				"projects-manager",
				"teams-manager",
				"project",
				"contextual-content-handlers",
				"test-cases-management",
				"users-manager",
				"bugtracker",
				"file-upload",
				"iteration-management",
				"issue-tables",
				//'tree',
				{
					name : 'tree',
					main : 'main',
					location : "http://localhost/scripts/scripts/tree"
				},
				'tc-workspace',
				//'req-workspace',
				{
					name : 'req-workspace',
					main : 'main',
					location : 'http://localhost/scripts/scripts/req-workspace'
				},
				'camp-workspace'			
			],
			/*
			 * rules for paths naming: * third party lib: unversionned lib name *
			 * non AMD squash lib: replace "squashtm" by "squash" in js file
			 * name and remove any unrequired "ext" suffix.
			 */
			paths : {
				"domReady" : "lib/require/domReady",
				/*
				 * CAVEAT: as we defined a "jquery" path, any module named
				 * "jquery/my.module" will be interpolated as
				 * "/lib/jquery/../my.module"
				 */
				"jquery" : "lib/jquery/jquery-1.8.3.min",
				"jqueryui" : "lib/jquery/jquery-ui-1.9.0.custom.min",
				"jquery.dialog-patch" : "jquery/jquery.dialog-patch",
				"datatables" : "datatables/jquery.dataTables.min",
				"squash.datatables" : "squashtest/jquery.squashtm.datatables",
				"jquery.squash.datatables" : "squashtest/jquery.squashtm.datatables.v2",
				"handlebars" : "lib/handlebars/handlebars-1.0.0.rc3",
				"underscore" : "lib/underscore/underscore-1.4.4-min",
				"backbone" : "lib/backbone/backbone-1.0.0-min",
				"ckeditor" : "ckeditor/ckeditor",
				"jquery.ckeditor" : "ckeditor/adapters/jquery",
				"jeditable" : "jquery/jquery.jeditable.mini",
				"jeditable.ckeditor" : "jquery/jquery.jeditable.ckeditor",
				"jeditable.datepicker" : "jquery/jquery.jeditable.datepicker",
				"jeditable.simpleJEditable" : "squashtest/classes/SimpleJEditable",
				"jeditable.selectJEditable" : "squashtest/classes/SelectJEditable",
				"jstree" : "jquery/jquery.jstree",
				"jform" : "jquery/jquery.form",
				"jquery.generateId" : "jquery/jquery.generateId",
				"jquery.hotkeys" : "jquery/jquery.hotkeys-0.8",
				"jquery.timepicker" : "jquery/jquery-ui-timepicker-addon",
				"jquery.squash" : "squashtest/jquery.squashtm.plugin",
				"jquery.squash.fragmenttabs" : "squash/squashtm.fragmenttabs",
				"jquery.squash.togglepanel" : "squashtest/jquery.squash.togglepanels",
				"jquery.squash.messagedialog" : "squashtest/jquery.squash.messagedialog",
				"jquery.squash.confirmdialog" : "squashtest/jquery.squash.confirmdialog",
				"jquery.squash.oneshotdialog" : "squashtest/jquery.squashtm.oneshotdialog",
				"workspace.contextual-content" : "workspace/workspace.contextual-content",	
				"jquery.squash.fg.menu" : "squashtest/jquery.squashtm.fg.menu",
				"jquery.squash.squashbutton" : "squashtest/jquery.squash.squashbutton",
				"jquery.squash.jeditable" : "squashtest/jquery.squashtm.jeditable.ext",
				"jquery.squash.projectpicker" : "squash/jquery.squashtm.projectpicker",
				"jquery.cookie" : "jquery/jquery.cookie",
				"squash.bugtrackerMenu" : "squashtest/classes/BugTrackerMenu",
				"squash.cssloader" : "squash/squash.cssloader",
				"squash.translator" : "squash/squash.translator",
				"squash.resizer" : "squash/squashtm.tree-page-resizer",
				"squash.table-collapser" : "squashtest/jquery.squash.table-collapser",
				"squash.session-pinger" : "squashtest/jquery.squash.session-pinger",
				"jquery.tagit" : "jquery/tag-it.min",		
				"jquery.squash.tagit" : "squashtest/jquery.squash.tagit",
				"jquery.squash.add-attachment-popup" : "squashtest/add-attachment-popup",
				"jquery.squash.buttonmenu" : "squashtest/jquery.squash.buttonmenu",
				"jquery.squash.formdialog" : "squashtest/jquery.squash.formdialog",
				"squash.attributeparser" : 'squash/squash.attributeparser',
				"squash.configmanager" : 'squash/squash.configmanager',
				"workspace.tree-node-copier" : "workspace/workspace.tree-node-copier",
				"workspace.tree-event-handler" : "workspace/workspace.tree-event-handler",
				"workspace.permissions-rules-broker" : "workspace/workspace.permissions-rules-broker"
			},
			shim : {
				"ckeditor" : {
					exports : "CKEDITOR"
				},
				"jquery.ckeditor" : {
					deps : [ "jquery", "ckeditor", "jquery.dialog-patch" ],
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
				"jqueryui" : [ "jquery" ],
				"datatables" : [ "jqueryui" ],
				"squash.datatables" : {
					deps : [ "datatables",
							"squashtest/jquery.squashtm.tableDnD.ext" ],
					exports : "squashtm.datatables"
				},
				"jquery.squash.datatables" : {
					deps : [ "datatables", "squash.datatables",
							"squashtest/classes/KeyEventListener",
							"jquery.squash.oneshotdialog" ],
					exports : "squashtmDatatablesWidget"
				},
				"jquery.squash" : {
					deps : [ "jquery" ],
					exports : "jquerySquashtm"
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
				"jquery.squash.fg.menu" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "jquerySquashtmFgMenu"
				},
				"jquery.squash.jeditable" : {
					deps : [ "jquery", "jeditable", "jeditable.ckeditor" ],
					exports : "jquerySquashtmJeditable"
				},
				"jquery.squash.projectpicker" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "jquerySquashtmProjectPicker"
				},
				"jquery.squash.squashbutton" : {
					deps : [ "jquery", "jqueryui" ],
					exports : "jquerySquashSquashButton"
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
				"jquery.squash.add-attachment-popup" : {
					deps : ["jquery", "jquery.generateId"],
					exports : "squash.add-attachment-popup"
				}
			}
		});