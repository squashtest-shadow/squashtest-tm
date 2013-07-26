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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil",
        "./TestCaseSearchResultTable","jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "jquery.squash.datatables",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil, TestCaseSearchResultTable) {
	
	var TestCaseSearchInputPanel = Backbone.View.extend({

		expanded : false,
		el : "#test-case-search-results",

		initialize : function() {
			this.expanded = false;
			this.toggleTree();
			this.configureModifyResultsDialog();
			new TestCaseSearchResultTable();
		},

		events : {
			"click #toggle-expand-search-result-frame-button" : "toggleTree",
			"click #export-search-result-button" : "exportResults",
			"click #modify-search-result-button" : "editResults"
		},

		exportResults : function(){
			var f = 10;
		},
		
		editResults : function(){
			this.addModifyResultDialog.confirmDialog("open");
		},
		
		toggleTree : function(){
			
			if(this.expanded){
				$("#tree-panel-left").show();
				$("#contextual-content").removeAttr("style");
				this.expanded = false;
				$("#toggle-expand-search-result-frame-button").val("<<");
			} else {
				$("#tree-panel-left").hide();
				$("#contextual-content").css("left",0);
				this.expanded = true;
				$("#toggle-expand-search-result-frame-button").val(">>");
			}
		},
		
		configureModifyResultsDialog : function() {
			var addModifyResultDialog = $("#modify-search-result-dialog").confirmDialog();

			var cell = $("#importance-combo");
			cell.html("<select></select>");
			
			$.ajax({
				url : squashtm.app.contextRoot + "/test-cases/importance-combo-data",
				dataType : 'json'
			}).success(function(json) {
				 $.each(json, function(key, value){ 
					var option = new Option(value, key);
					$("select", cell).append(option);
				 });
			});
			
			addModifyResultDialog.on("confirmdialogvalidate",
					function() {

					});

			addModifyResultDialog.on("confirmdialogconfirm",
					function() {

					});

			addModifyResultDialog.on('confirmdialogopen',
					function() {
						
					});

			addModifyResultDialog.activate = function(arg) {

			};

			this.addModifyResultDialog = addModifyResultDialog;
		}
		
	});
	return TestCaseSearchInputPanel;
});






