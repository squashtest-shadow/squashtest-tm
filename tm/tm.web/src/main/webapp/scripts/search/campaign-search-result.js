/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil","workspace.routing","workspace.event-bus",
        "./CampaignSearchResultTable", "squash.translator", "app/ws/squashtm.notification",
        "workspace.projects", "./milestone-mass-modif-popup", 
        "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog", "jquery.squash.milestoneDialog" ], 
		function($, Backbone, _, StringUtil, routing, eventBus, CampaignSearchResultTable, 
				translator, notification, projects, milestoneMassModif) {
	
	var CampaignSearchResultPanel = Backbone.View.extend({

		expanded : false,
		el : "#sub-page",

		initialize : function() {
		//	this.configureModifyResultsDialog();
			this.getIdsOfSelectedTableRowList =  $.proxy(this._getIdsOfSelectedTableRowList, this);
			this.getVersionIdsOfSelectedTableRowList = $.proxy(this._getVersionIdsOfSelectedTableRowList, this); 
			this.getIdsOfEditableSelectedTableRowList = $.proxy(this._getIdsOfEditableSelectedTableRowList, this);
			this.updateDisplayedValueInColumn =  $.proxy(this._updateDisplayedValueInColumn, this);
			var model = JSON.parse($("#searchModel").text());
			this.isAssociation = !!$("#associationType").length;
			if(this.isAssociation){
				this.associationType = $("#associationType").text();
				this.associationId = $("#associationId").text();
			}
			this.model = model;
			new CampaignSearchResultTable(model, this.isAssociation, this.associationType, this.associationId);
			this.milestoneMassModif = new milestoneMassModif();
			this.initTableCallback();
			
		},

		events : {
			"click #select-all-button" : "selectAllForAssocation",
			"click #deselect-all-button" : "deselectAll",			
			"click #modify-search-result-button" : "editResults",			
			"click #new-search-button" : "newSearch",
			"click #modify-search-button" : "modifySearch",		
			"click #add-search-result-button" : "addExecution",
			"click #export-search-result-button" : "exportResults"
		},

	
		initTableCallback : function(){
			//little hack to select only previously selected campaigns (should be like requirements)
			var self = this;
			var table = $('#campaign-search-result-table').squashTable();
			table.drawcallbacks.push(function() {  
				table.deselectRows();
			self._restoreSelect();}
			);
		},
		
		selectAllForAssocation : function(){
			var table = $('#campaign-search-result-table').dataTable();
			var rows = table.fnGetNodes();
			var ids = [];
			$(rows).each(function(index, row) {
				ids.push(parseInt($(".element_id", row).text(),10));
			});
			
			// Should do that but it doesn't work, need to debug it
			// table.squashTable().selectRows(ids);
			
			$(rows).addClass('ui-state-row-selected');
			
		},
		
		deselectAll : function(){
			var table = $('#campaign-search-result-table').dataTable();
			table.squashTable().deselectRows();
		},
		
		modifySearch : function(){
			if(this.isAssociation){
				this.post(squashtm.app.contextRoot + "advanced-search?searchDomain=campaign&id="+this.associationId+"&associateResultWithType="+this.associationType, {
					searchModel : JSON.stringify(this.model)
				});	
			} else {
				this.post(squashtm.app.contextRoot + "/advanced-search?searchDomain=campaign", {
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
				document.location.href= squashtm.app.contextRoot +"/advanced-search?searchDomain=campaign&id="+this.associationId+"&associateResultWithType="+this.associationType;
			} else {
				document.location.href= squashtm.app.contextRoot +"/advanced-search?searchDomain=campaign";
			}
		},
		
		exportResults : function(){
			// document.location.href= squashtm.app.contextRoot +"/advanced-search?campaign&export=csv";
			var selectedIds = $("#campaign-search-result-table").squashTable().getSelectedIds();
			if (selectedIds.length === 0){
					notification.showError(translator.get('message.exportNoExecutionSelected'));
				return;
			} 
			else if (selectedIds.length > 1) {
				notification.showError(translator.get('message.exportMultipleExecutionSelected'));
				return;
			}
			else {
				// We have a campaign to export :)
				var table = $('#campaign-search-result-table').dataTable();
				var selectedRow = table.find(".ui-state-row-selected");
				
				document.location.href = window.squashtm.app.contextRoot + "/campaign-browser/export-campaign-by-execution/" + selectedIds.toString() + "?export=csv&exportType=S";
			}

		},
		 
		editResults : function(){
			this.addModifyResultDialog.confirmDialog("open");
		},
		
		_restoreSelect : function restoreSelect(){
			
			var selectedIds = this.selectedIds;
			var table = $('#campaign-search-result-table').squashTable();
			
			if ((selectedIds instanceof Array) && (selectedIds.length > 0)) {
				var rows = table.fnGetNodes();
				$(rows).filter(function() {		
					var rId = table.fnGetData(this)["execution-id"];
					return $.inArray(rId, selectedIds) != -1;
				}).addClass('ui-state-row-selected');
			}
			
		},
		
		_containsDuplicate : function containsDuplicate(arr) {arr.sort();
		var last = arr[0];
		for (var i=1; i<arr.length; i++) {
		   if (arr[i] == last) {return true;}
		   last = arr[i];
		}
		return false;
		},
		
		_getIdsOfSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					ids.push(data["execution-id"]);
				}
			});
			
			return ids;
		},
		
		_getVersionIdsOfSelectedTableRowList : function(dataTable){
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					ids.push(data["execution-id"]);
				}
			});
			
			return ids;		
		},
		
		_getIdsOfEditableSelectedTableRowList : function(dataTable) {
			var rows = dataTable.fnGetNodes();
			var ids = [];
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1) {
					var data = dataTable.fnGetData(row);
					if(data["editable"]){
						ids.push(data["execution-id"]);
					} 
				}
			});
			
			return ids;
		},
		
		
		// enableCategoryModification deleted

		
		_updateDisplayedValueInColumn : function(dataTable, column) {
			var rows = dataTable.fnGetNodes();
			
			$( rows ).each(function(index, row) {
				if ($( row ).attr('class').search('selected') != -1  && dataTable.fnGetData(row)["editable"]) {
					var value = $("#"+column+"-combo").find('option:selected').text();
					$(".editable_"+column, row).text(value);
				}
			});

		}
				
		// configureModifyResultsDialog  deleted
		
	});
	return CampaignSearchResultPanel;
});






