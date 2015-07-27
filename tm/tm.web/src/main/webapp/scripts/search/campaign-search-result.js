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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil","workspace.routing","workspace.event-bus", 'tree', './execution-treemenu',
        "./CampaignSearchResultTable", "squash.translator", "app/ws/squashtm.notification",
        "workspace.projects", "./milestone-mass-modif-popup", 
        "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog",
		"jquery.squash.formdialog", "jquery.squash.milestoneDialog" ], 
		function($, Backbone, _, StringUtil, routing, eventBus, tree, treemenu, CampaignSearchResultTable, 
				translator, notification, projects, milestoneMassModif) {
	
	var CampaignSearchResultPanel = Backbone.View.extend({

		expanded : false,
		el : "#sub-page",

		initialize : function() {
		  this.configureModifyResultsDialog();
		  this.configureExecutionDialog();
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
		
		addExecution : function(){
			this.addExecutionDialog.formDialog("open");
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
		
		
		// enableCategoryModification 
		enableCategoryModification : function(dialog, table){
			
			var rows = table.getSelectedRows();
			
			if (rows.length === 0){
				return;
			}

			// reset the controls
			$("#modify-search-result-dialog-project-conf-warning").hide();
			$(".mass-change-forbidden").hide();
			$(".mass-change-allowed").show();
			$(".mass-change-infolist-combo").prop('disabled', false);
			
			// find the selected projects unique ids
			var selectedProjects = [];
			rows.each(function(indx, row){
				selectedProjects.push(table.fnGetData(row)['project-id']);
			});
			selectedProjects = _.uniq(selectedProjects);
			
			// check for conflicts
			var difCat = projects.haveDifferentInfolists(selectedProjects, ["category"]);
			
			function populateCombo(select, infolistName){
				var p = projects.findProject(selectedProjects[0]);
				select.empty();
				
				for (var i=0;i<p[infolistName].items.length; i++){
					var item = p[infolistName].items[i];
					var opt = $('<option/>', {
						value : item.code,
						html : item.friendlyLabel
					});
					select.append(opt);
				}				
			}
			
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

			
			function loadCombos(comboname){
				$.ajax({
					url : squashtm.app.contextRoot + "/executions/"+ comboname +"-data",
					dataType : 'json'
				})
				.success(function(json) {
					var combo = $("<select/>"),
						comboCell = $("#"+comboname);
						
					 $.each(json, function(key, value){ 
						var option = $("<option/>",{
							value : key,
							html : value
						});
						combo.append(option);
					 });
					 comboCell.append(combo);
				});
			}
			

			/*
			 * configure the comboboxes. Note that the category combos cannot 
			 * be initialized before we know which requirements were selected.  
			 */
			loadCombos("assignment-combo");
			loadCombos("status-combo");
			
			addModifyResultDialog.on('change', ':checkbox', function(evt){
				var cbx = $(evt.currentTarget),
					state = cbx.prop('checked'),
					select = cbx.parent().siblings().last().find('select');
				
				select.prop('disabled', !state);
			});
			
			addModifyResultDialog.on("confirmdialogvalidate",function() {});

			addModifyResultDialog.on("confirmdialogconfirm",function() {
				var table = $('#campaign-search-result-table').dataTable();
				var ids = self.getVersionIdsOfSelectedTableRowList(table);
				var editableIds = self.getIdsOfEditableSelectedTableRowList(table);
				var columns = ["assignment","status"];
				var index = 0;
				
				for(index=0; index<columns.length; index++){
					if($("#"+columns[index]+"-checkbox").prop('checked')){
						self.updateDisplayedValueInColumn(table, columns[index]);
						var value = $("#"+columns[index]+"-combo").find('option:selected').val();
						for(var i=0; i<editableIds.length; i++){
							var urlPOST = routing.buildURL('executions', editableIds[i]);
							$.post(urlPOST, {
								value : value,
								id : "execution-"+columns[index]	
							}).success(function(){
								$('#campaign-search-result-table').squashTable()._fnAjaxUpdate();
							});
						}
					}
				}
			});
			
			addModifyResultDialog.on('confirmdialogopen',function() {
				addModifyResultDialog.find(':checkbox').prop('checked', false);
				addModifyResultDialog.find('select').prop('disabled', true);
			
				var table = $('#campaign-search-result-table').squashTable();
				var ids = self.getVersionIdsOfSelectedTableRowList(table);
				var editableIds = self.getIdsOfEditableSelectedTableRowList(table);
				if(ids.length === 0) {							
					notification.showError(translator.get('message.noLinesSelected'));
					$(this).confirmDialog('close');
				}else if (editableIds.length === 0){
					notification.showError(translator.get('message.search.modify.noLineWithWritingRightsOrWrongStatus'));
					$(this).confirmDialog('close');
				}else if (editableIds.length < ids.length){							
					notification.showError(translator.get('message.search.modify.noWritingRightsOrWrongStatus'));
				}
				
				self.enableCategoryModification(addModifyResultDialog, table);

			});

			addModifyResultDialog.activate = function(arg) {};

			this.addModifyResultDialog = addModifyResultDialog;
		},
		
		
		configureExecutionDialog : function() {
			var self = this;
			var addExecutionDialog = $("#add-new-execution-dialog").formDialog();

			function loadTree(){
				$.ajax({
					url : squashtm.app.contextRoot + "/executions/getTree",
					datatype : 'json' 
				})
				.success(function(json) {
				 // Add tree in the dialog > rootModel is supposed to be given thanks to the controller
					squashtm.app.campaignWorkspaceConf.tree.model = json;
					tree.initWorkspaceTree(squashtm.app.campaignWorkspaceConf.tree);
				});
			}

 
			addExecutionDialog.activate = function(arg) {};

			addExecutionDialog.on('formdialogopen',function() {
				addExecutionDialog.formDialog('open');
				var selectedIds = $("#campaign-search-result-table").squashTable().getSelectedIds();
				if (selectedIds.length === 0){
					addExecutionDialog.formDialog('close');
				  notification.showError(translator.get('message.NoExecutionSelected'));
					return; 
				} 
				/* Get more Ids
				else if (selectedIds.length > 1) {
					addExecutionDialog.formDialog('close');
				  notification.showError(translator.get('message.MultipleExecutionSelected'));
					return;
				}
				*/
				else {
				// get the execution id, give it to the controller which gives back the rootmodel for the tree
				loadTree();
				}
				
			});
			
			addExecutionDialog.on('formdialogconfirm',function() {
				
				// Get all executions we want to add
				var selectedIds = $("#campaign-search-result-table").squashTable().getSelectedIds();

				var arraySelectedIds = new Array();
				for (var j = 0  ; j < selectedIds.length ; j++)
				 { arraySelectedIds.push(selectedIds[j]);
				 }
				
				// Get the place where we want to put the executions 
				var nodes = $("#tree").jstree('get_selected');
				
				// Node must be an iteration (and only one for now)
				if (nodes.getResType() !== "iterations") {
					 notification.showError(translator.get('message.SelectIteration'));
				}
				else if(nodes.length > 1){
					 notification.showError(translator.get('message.SelectOneIteration'));
				}
				else {
						$.ajax({
							url : squashtm.app.contextRoot + "/executions/add-execution/"  + nodes.getResId() ,
							type : 'POST',
							data : {
										executionIds : arraySelectedIds 
							}
						})
						.success(function(json) {
							// not need to refresh yet
						});
				}

				
				
				addExecutionDialog.formDialog('close');
			});
			
			addExecutionDialog.on('formdialogadd',function() {
				
				
				/* copypasta from this file (look it up) for the moment to check if it works */
				function loadTree(){
					$.ajax({
						url : squashtm.app.contextRoot + "/executions/getTree",
						datatype : 'json' 
					})
					.success(function(json) {
					 // Add tree in the dialog > rootModel is supposed to be given thanks to the controller
						squashtm.app.campaignWorkspaceConf.tree.model = json;
						tree.initWorkspaceTree(squashtm.app.campaignWorkspaceConf.tree);
					});
				}
				/* end copypasta*/
				
				// Get the place where we want to add the iteration
				var nodes = $("#tree").jstree('get_selected');

				// Node must be a campaign (and only one for now)
				if (nodes.getResType() !== "campaigns") {
					 notification.showError(translator.get('message.SelectCampaign'));
				}
				else if(nodes.length > 1){
					 notification.showError(translator.get('message.SelectOneCampaign'));
				}
				else {
						$.ajax({
							url : squashtm.app.contextRoot + "/executions/add-iteration/"  + nodes.getResId() ,
							type : 'POST',
							})
						.success(function() {
						 // refresh tree
							console.log("success");
							loadTree();
						});
				}
				
				
				
				
				
				
				
				
				
				
				
				console.log("coucou");
			});
			
			
			
			addExecutionDialog.on('formdialogcancel',function() {
				addExecutionDialog.formDialog('close');
			});
			
			addExecutionDialog.on('formdialogclose',function() {
				addExecutionDialog.formDialog('close');
			});
			
			
			this.addExecutionDialog = addExecutionDialog; 
			
		}
		
	});
	return CampaignSearchResultPanel;
});






