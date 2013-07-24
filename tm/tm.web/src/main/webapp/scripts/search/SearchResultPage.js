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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil","jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "jquery.squash.datatables",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil) {
	
	var TestCaseSearchInputPanel = Backbone.View.extend({

		expanded : false,
		el : "#test-case-search-results",

		initialize : function() {
	
		},

		events : {
			"click #toggle-expand-search-result-frame-button" : "toggleTree"
		},

		toggleTree : function(){
			
			if(expanded){
				$("#tree-panel-left").show();
				$("#contextual-content").removeAttr("style");
			} else {
				$("#tree-panel-left").hide();
				$("#contextual-content").css("left",0);
			}
		}
		
	});
	return TestCaseSearchInputPanel;
});