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
	paths : {
		"jquery" : "lib/jquery/jquery-1.8.2",
		"jqueryui" : "lib/jquery/jquery-ui-1.9.0.custom",
		"datatables" : "datatables/jquery.dataTables",
		"squashtm.datatables" : "squashtest/jquery.squashtm.datatables",
		"squashtm.datatables.widget" : "squashtest/jquery.squashtm.datatables.v2",
		"handlebars" : "lib/handlebars/handlebars-1.0.rc.1",
		"underscore" : "lib/underscore/underscore-1.4.2",
		"backbone" : "lib/backbone/backbone-0.9.2"
	},
	shim : {
		jqueryui : {
			deps : [ "jquery" ],
			exports : "jqueryui"
		},
		datatables : {
			deps : [ "jqueryui" ],
			exports : "datatables"
		},
		"squashtm.datatables": {
			deps : [ "datatables", "squashtest/jquery.squashtm.tableDnD.ext" ],
			exports : "squashtmDatatables"
		},
		"squashtm.datatables.widget": {
			deps : [ "datatables", "squashtm.datatables", "squashtest/classes/KeyEventListener" ],
			exports : "squashtmDatatablesWidget"
		},
		handlebars: {
			deps: [ "jquery" ],
			exports: "Handlebars"
		},
		backbone : {
			deps : [ "underscore", "jquery" ],
			exports : "Backbone"
		}
	}
});