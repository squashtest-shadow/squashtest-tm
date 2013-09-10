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
			self = this;
			this.configureModifyResultsDialog();
			this.getIdsOfSelectedTableRowList =  $.proxy(this._getIdsOfSelectedTableRowList, this);
			this.updateDisplayedImportance =  $.proxy(this._updateDisplayedImportance, this);
			var model = JSON.parse($("#searchModel").text());
			this.isAssociation = !!$("#associationType").length;
			if(this.isAssociation){
				this.associationType = $("#associationType").text();
				this.associationId = $("#associationId").text();
			}
			this.model = model;
			new TestCaseSearchResultTable(model, this.isAssociation, this.associationType, this.associationId);
		},

		events : {
			"click #export-search-result-button" : "exportResults",
			"click #modify-search-result-button" : "editResults",
			"click #new-search-button" : "newSearch",
			"click #modify-search-button" : "modifySearch",
			"click #associate-selection-button" : "associateSelection",
			"click #select-all-button" : "selectAllForAssocation",
			"click #associate-all-button" : "associateAll"
		},

		
		
		associateSelection : function(){
			var table = $('#test-case-search-result-table').dataTable();
			var rows = table.fnGetNodes();
			var associationId = this.associationId;
			var ids = [];
			$(rows).each(function(index, row) {
				if($("input[type=checkbox]", row).is(":checked")){
					//associate
					var id = $(".testcaseid", row).text();
					ids.push(id);
				}
			});
			
			if("requirement" === this.associationType){
				
				var st = "";
				var i;
				
				for(i=0; i<ids.length; i++){
					if(i+1 == ids.length){
						st = st+ids[i];
					} else {
						st = st+ids[i]+",";
					}
				}
				

				
				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/requirement-versions/" + associationId + "/verifying-test-cases/"+st
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/requirement-versions/" + associationId + "/verifying-test-cases/manager";
				});
				
			} else if ("campaign" === this.associationType){
				
				
				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/campaigns/" + associationId + "/test-plan",
					data : { "testCasesIds[]" : ids }
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/campaigns/" + associationId + "/test-plan/manager";				
				});
					
			} else if ("iteration" === this.associationType){

				
				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/iterations/" + associationId + "/test-plan",
					data : { "testCasesIds[]" : ids }
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/iterations/" + associationId + "/test-plan-manager";				
				});
				
			} else if ("testsuite" === this.associationType){

				
				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/test-suites/" + associationId + "/test-plan",
					data: { "testCasesIds[]" : ids }
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/test-suites/" + associationId + "/test-plan-manager";					
				});
				
			}
		},
		
		selectAllForAssocation : function(){
			var table = $('#test-case-search-result-table').dataTable();
			var rows = table.fnGetNodes();
			$(rows).each(function(index, row) {
				$("input[type=checkbox]", row).attr('checked', true);
			});
		},
		
		associateAll : function(){
			this.selectAllForAssocation();
			this.associateSelection();
		},
		
		modifySearch : function(){
			this.post(squashtm.app.contextRoot + "/advanced-search?testcase", {
				searchModel : JSON.stringify(this.model)
			});	
		},
		
		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
			for(var x in PARAMS) {
				var opt=document.createElement("textarea");
				opt.name=x;
				opt.value=PARAMS[x];
				temp.appendChild(opt);
			}
			document.body.appendChild(temp);
			temp.submit();
			return temp;
		},
		
		newSearch : function(){
			
			if(this.isAssociation){
				document.location.href= squashtm.app.contextRoot +"/advanced-search?testcase&id="+this.associationId+"&associateResultWithType="+this.associationType;
			} else {
				document.location.href= squashtm.app.contextRoot +"/advanced-search?testcase";
			}
		},
		
		exportResults : function(){
			document.location.href= squashtm.app.contextRoot +"/advanced-search?export=csv";
		},
		
		editResults : function(){
			this.addModifyResultDialog.confirmDialog("open");
		},
				
		_getIdsOfSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					ids.push(data["test-case-id"]);
				}
			});
			
			return ids;
		},
		
		_updateDisplayedImportance : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var value = $("#importance-combo").find('option:selected').text().split("-")[1];
					$(".editable_imp", row).text(value);
				}
			});
			
			return ids;
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
					$(option).html(value);
					$("select", cell).append(option);
				 });
			});
	
			addModifyResultDialog.on("confirmdialogvalidate",
					function() {
						
					});

			addModifyResultDialog.on("confirmdialogconfirm",
					function() {
						var table = $('#test-case-search-result-table').dataTable();
						var ids = self.getIdsOfSelectedTableRowList(table);
						self.updateDisplayedImportance(table);
						var value = $("#importance-combo").find('option:selected').val();
						var i;
						for(i=0; i<ids.length; i++){
							var urlPOST = squashtm.app.contextRoot + "/test-cases/" + ids[i];
							$.post(urlPOST, {
								value : value,
								id : "test-case-importance"	
							});
						}
					});
			
			addModifyResultDialog.on('confirmdialogopen',
					function() {
						var table = $('#test-case-search-result-table').dataTable();
						var ids = self.getIdsOfSelectedTableRowList(table);
						if(ids.length === 0) {
							var noLineSelectedDialog = $("#no-selected-lines").messageDialog();
							noLineSelectedDialog.messageDialog('open');
							$(this).confirmDialog('close');
						} else {
						}
					});

			addModifyResultDialog.activate = function(arg) {

			};

			this.addModifyResultDialog = addModifyResultDialog;
		}
		
	});
	return TestCaseSearchInputPanel;
});






