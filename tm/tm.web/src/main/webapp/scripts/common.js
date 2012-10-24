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
requirejs.config({
	/*
	 * rules for paths naming : * third party lib : unversionned lib
	 * name * non AMD squash lib : replace "squashtm" by "squash" in js
	 * file name and remove any unrequired "ext" suffix.
	 */
	paths: {
		"domReady": "lib/require/domReady",
		/* CAVEAT : as we defined a "jquery" path, any module named "jquery/my.module" will be interpolated as "/lib/jquery/../my.module" */
		"jquery": "lib/jquery/jquery-1.8.2",
		"jqueryui": "lib/jquery/jquery-ui-1.9.0.custom",
		"datatables": "datatables/jquery.dataTables",
		"squash.datatables": "squashtest/jquery.squashtm.datatables",
		"jquery.squash.datatables": "squashtest/jquery.squashtm.datatables.v2",
		"handlebars": "lib/handlebars/handlebars-1.0.rc.1",
		"underscore": "lib/underscore/underscore-1.4.2",
		"backbone": "lib/backbone/backbone-0.9.2", 
		"ckeditor": "ckeditor/ckeditor.js", 
		"jquery.ckeditor": "adapters/jquery", 
		"jeditable": "jquery/jquery.jeditable", 
		"jeditable.ckeditor": "jquery/jquery.jeditable.ckeditor", 
		"jeditable.datepicker": "jquery/jquery.jeditable.datepicker", 
		"jstree": "jquery/jquery.jstree", 
		"jquery.generateId": "jquery/jquery.generateId",
		"jquery.hotkeys": "jquery/jquery.hotkeys-0.8", 
		"jquery.squash": "squashtest/jquery.squashtm.plugin", 
		"jquery.squash.togglepanel": "squashtest/jquery.squash.togglepanels", 
		"jquery.squash.messagedialog": "squashtest/jquery.squash.messagedialog", 
		"jquery.squash.confirmdialog": "squashtest/jquery.squash.confirmdialog",
		"jquery.squash.oneshotdialog": "squashtest/jquery.squashtm.oneshotdialog",
		"jquery.squash.contextual-content": "squashtest/jquery.squashtm.contextual-content", 
		"jquery.squash.fg.menu": "squashtest/jquery.squashtm.fg.menu", 
		"jquery.squash.squashbutton": "squashtest/jquery.squash.squashbutton", 
		"jquery.squash.jeditable": "squashtest/jquery.squashtm.jeditable.ext",
		"jquery.squash.jstree-node": "squashtest/jquery.squashtm.jstree-node", 
		"jquery.squash.jstree": "squashtest/jquery.squashtm.jstree.ext", 
		"jquery.squash.linkabletree": "squash/jquery.squashtm.linkabletree", 
		"jquery.squash.projectpicker": "squash/jquery.squashtm.projectpicker", 
		"jquery.cookie": "jquery/jquery.cookie"
	},
	shim : {
		"ckeditor": {
			exports: "CKEDITOR"
		},
		"jquery.ckeditor": {
			deps: [ "jquery", "ckeditor" ],
			exports: "jqueryCkeditor"
		},
		"jeditable": {
			deps: [ "jquery", "jqueryui" ], 
			exports: "jeditable"
		},
		"jeditable.ckeditor": {
			deps: [ "jeditable", "jquery.ckeditor", "jquery.generateId" ],
			exports: "jeditableCkeditor"
		},
		"jeditable.datepicker": {
			deps: [ "jeditable" ],
			exports: "jeditableDatepicker"
		},
		"jstree": {
			deps : [ "jquery", "jqueryui", "jquery.hotkeys", "jquery.cookie" ],
			exports : "jqueryui"
		},
		"jqueryui": {
			deps : [ "jquery" ],
			exports : "jqueryui"
		},
		"datatables": {
			deps : [ "jqueryui" ],
			exports : "datatables"
		},
		"squash.datatables": {
			deps : [ "datatables", "squashtest/jquery.squashtm.tableDnD.ext" ],
			exports : "squashtmDatatables"
		},
		"jquery.squash.datatables": {
			deps : [ "datatables", "squash.datatables", "squashtest/classes/KeyEventListener", "jquery.squash.oneshotdialog" ],
			exports : "squashtmDatatablesWidget"
		},
		"jquery.squash": {
			deps : [ "jquery" ],
			exports : "jquerySquashtm"
		},
		"jquery.squash.togglepanel": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashtmTogglepanel"
		},
		"jquery.squash.messagedialog": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashMessageDialog"
		},
		"jquery.squash.confirmdialog": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashConfirmDialog"
		},
		"jquery.squash.oneshotdialog": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashOneShotConfirm"
		},
		"jquery.squash.fg.menu": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashtmFgMenu"
		},
		"jquery.squash.contextual-content": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashContextualContent"
		},
		"jquery.squash.jeditable": {
			deps : [ "jquery", "jeditable", "jeditable.ckeditor" ],
			exports : "jquerySquashtmJeditable"
		},
		"squashtest/classes/TreeEventHandler": {
			deps : [ "squashtest/classes/Event" ],
			exports : "TreeEventHandler"
		},
		"squashtest/classes/TreeNodeCopier": {
			deps : [ "jquery" ],
			exports : "TreeNodeCopier"
		},
		"jquery.squash.jstree-node": {
			deps : [ "jquery", "jstree" ],
			exports : "jquerySquashtmJstreeNode"
		},
		"jquery.squash.jstree": {
			deps : [ "jquery", "jstree", "jquery.squash.jstree-node" ],
			exports : "jquerySquashtmJstree"
		},
		"jquery.squash.linkabletree": {
			deps : [ "jquery", "jquery.squash.jstree" ],
			exports : "jquerySquashtmLinkableTree"
		},
		"jquery.squash.projectpicker": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashtmProjectPicker"
		},
		"jquery.squash.squashbutton": {
			deps : [ "jquery", "jqueryui" ],
			exports : "jquerySquashSquashButton"
		},
		"jquery.cookie": {
			deps : [ "jquery" ],
			exports : "jqueryCookie"
		},
		"handlebars": {
			deps: [ "jquery" ],
			exports: "Handlebars"
		},
		"backbone": {
			deps : [ "underscore", "jquery" ],
			exports : "Backbone"
		}
	}
});