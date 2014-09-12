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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil",
        "./TestCaseSearchResultTable","jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil, TestCaseSearchResultTable) {
	
	var TestCaseSearchInputPanel = Backbone.View.extend({

		expanded : false,
		el : "#sub-page",

		initialize : function() {
			this.configureModifyResultsDialog();
			this.getIdsOfSelectedTableRowList =  $.proxy(this._getIdsOfSelectedTableRowList, this);
			this.updateDisplayedValueInColumn =  $.proxy(this._updateDisplayedValueInColumn, this);
			var model = JSON.parse($("#searchModel").text());
			this.domain = $("#searchDomain").text();
			this.isAssociation = !!$("#associationType").length;
			if(this.isAssociation){
				this.associationType = $("#associationType").text();
				this.associationId = $("#associationId").text();
			}
			this.model = model;
			new TestCaseSearchResultTable(model, this.domain, this.isAssociation, this.associationType, this.associationId);
		},

		events : {
			"click #export-search-result-button" : "exportResults",
			"click #modify-search-result-button" : "editResults",
			"click #new-search-button" : "newSearch",
			"click #modify-search-button" : "modifySearch",
			"click #associate-selection-button" : "associateSelection",
			"click #select-all-button" : "selectAllForAssocation",
			"click #associate-all-button" : "associateAll",
			"click #deselect-all-button" : "deselectAll"
		},

		
		
		associateSelection : function(){
			var table = $('#test-case-search-result-table').dataTable();
			var ids = table.squashTable().getSelectedIds();
			if(ids.length === 0){
				var noLineSelectedDialog = $("#no-selected-lines").messageDialog();
				noLineSelectedDialog.messageDialog('open');
				return;
			}
			var id = this.associationId;
				
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
					url : squashtm.app.contextRoot + "/requirement-versions/" + id + "/verifying-test-cases/"+st
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/requirement-versions/" + id + "/verifying-test-cases/manager";
				});
				
			} else if ("campaign" === this.associationType){
				
				
				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/campaigns/" + id + "/test-plan",
					data : { "testCasesIds[]" : ids }
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/campaigns/" + id + "/test-plan/manager";				
				});
					
			} else if ("iteration" === this.associationType){

				
				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/iterations/" + id + "/test-plan",
					data : { "testCasesIds[]" : ids }
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/iterations/" + id + "/test-plan-manager";				
				});
				
			} else if ("testsuite" === this.associationType){

				
				$.ajax({
					type: "POST",
					url : squashtm.app.contextRoot + "/test-suites/" + id + "/test-plan",
					data: { "testCasesIds[]" : ids }
				}).done(function() {
					document.location.href = squashtm.app.contextRoot + "/test-suites/" + id + "/test-plan-manager";					
				});
				
			}
		},
		
		selectAllForAssocation : function(){
			var table = $('#test-case-search-result-table').dataTable();
			var rows = table.fnGetNodes();
			var ids = [];
			$(rows).each(function(index, row) {
				ids.push(parseInt($(".element_id", row).text(),10));
			});
			
			table.squashTable().selectRows(ids);
		},
		
		deselectAll : function(){
			var table = $('#test-case-search-result-table').dataTable();
			table.squashTable().deselectRows();
		},
		
		associateAll : function(){
			this.selectAllForAssocation();
			this.associateSelection();
		},
		
		modifySearch : function(){
			
			if(this.isAssociation){
				this.post(squashtm.app.contextRoot + "advanced-search?searchDomain="+this.domain+"&id="+this.associationId+"&associateResultWithType="+this.associationType, {
					searchModel : JSON.stringify(this.model)
				});	
			} else {
				this.post(squashtm.app.contextRoot + "advanced-search?searchDomain="+this.domain, {
					searchModel : JSON.stringify(this.model)
				});	
			}
		},
		
		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
			temp.acceptCharset="UTF-8";
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
				document.location.href= squashtm.app.contextRoot +"advanced-search?searchDomain="+this.domain+"&id="+this.associationId+"&associateResultWithType="+this.associationType;
			} else {
				document.location.href= squashtm.app.contextRoot +"advanced-search?searchDomain="+this.domain;
			}
		},
		
		exportResults : function(){
			document.location.href= squashtm.app.contextRoot +"/advanced-search?testcase&export=csv&searchModel="+JSON.stringify(this.model);
		},
		
		editResults : function(){
			this.addModifyResultDialog.confirmDialog("open");
		},
		
		
		validateSelection : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					if(!data["editable"]){
						var noWritingRightsDialog = $("#warning-no-writing-rights").messageDialog();
						noWritingRightsDialog.messageDialog('open');
					}
				}
			});
			
			return ids;
		},
		
		_getIdsOfSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					if(data["editable"]){
						ids.push(data["test-case-id"]);
					} 
				}
			});
			
			return ids;
		},
		
		_updateDisplayedValueInColumn : function(dataTable, column) {
			var rows = dataTable.fnGetNodes();
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1  && dataTable.fnGetData(row)["editable"]) {
					var value = $("#"+column+"-combo").find('option:selected').text();
					$(".editable_"+column, row).text(value);
				}
			});

		},
				
		configureModifyResultsDialog : function() {
			var self = this;
			var addModifyResultDialog = $("#modify-search-result-dialog").confirmDialog();
			
			$.ajax({
				url : squashtm.app.contextRoot + "/test-cases/importance-combo-data",
				dataType : 'json'
			}).success(function(json) {
				var importance_cell = $("#importance-combo");
				importance_cell.html("<select></select>");
				 $.each(json, function(key, value){ 
					var option = new Option(value, key);
					$(option).html(value);
					$("select", importance_cell).append(option);
				 });
			});
			


			$.ajax({
				url : squashtm.app.contextRoot + "/test-cases/status-combo-data",
				dataType : 'json'
			}).success(function(json) {
				status_cell = $("#status-combo");
				status_cell.html("<select></select>");
				 $.each(json, function(key, value){ 
					var option = new Option(value, key);
					$(option).html(value);
					$("select", status_cell).append(option);
				 });
			});
			

			
			$.ajax({
				url : squashtm.app.contextRoot + "/test-cases/type-combo-data",
				dataType : 'json'
			}).success(function(json) {
				type_cell = $("#type-combo");
				type_cell.html("<select></select>");
				 $.each(json, function(key, value){ 
					var option = new Option(value, key);
					$(option).html(value);
					$("select", type_cell).append(option);
				 });
			});
					
			$.ajax({
				url : squashtm.app.contextRoot + "/test-cases/nature-combo-data",
				dataType : 'json'
			}).success(function(json) {
				nature_cell = $("#nature-combo");
				nature_cell.html("<select></select>");
				 $.each(json, function(key, value){ 
					var option = new Option(value, key);
					$(option).html(value);
					$("select", nature_cell).append(option);
				 });
			});
			
			addModifyResultDialog.on("confirmdialogvalidate",
					function() {
						
					});

			addModifyResultDialog.on("confirmdialogconfirm",
					function() {
						var table = $('#test-case-search-result-table').dataTable();
						var ids = self.getIdsOfSelectedTableRowList(table);
						var columns = ["importance","status","type","nature"];
						var index = 0;
						
						for(index=0; index<columns.length; index++){
							if($("#"+columns[index]+"-checkbox").prop('checked')){
								self.updateDisplayedValueInColumn(table, columns[index]);
								var value = $("#"+columns[index]+"-combo").find('option:selected').val();
								for(var i=0; i<ids.length; i++){
									var urlPOST = squashtm.app.contextRoot + "/test-cases/" + ids[i];
									$.post(urlPOST, {
										value : value,
										id : "test-case-"+columns[index]	
									});
								}
							}
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
						}
						self.validateSelection(table);
					});

			addModifyResultDialog.activate = function(arg) {

			};

			this.addModifyResultDialog = addModifyResultDialog;
		}
		
	});
	return TestCaseSearchInputPanel;
});






