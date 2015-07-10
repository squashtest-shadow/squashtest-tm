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
define([ "jquery", "backbone", "squash.translator","jeditable.simpleJEditable", "workspace.projects",
         "squash.configmanager", "workspace.routing", "app/ws/squashtm.notification", "squashtable",
         "jqueryui", "jquery.squash.jeditable", "jquery.cookie" ],
         function($, Backbone, translator, SimpleJEditable, projects, confman, routing, notification) {

	var CampaignSearchResultTable = Backbone.View.extend({
		el : "#campaign-search-result-table",
		initialize : function(model, isAssociation, associateType, associateId) {
			this.model = model;
			this.isAssociation = isAssociation;
			this.associateType = associateType;
			this.associateId = associateId;
			this.addSelectEditableToCriticality = $.proxy(this._addSelectEditableToCriticality, this);
			this.addSelectEditableToCategory = $.proxy(this._addSelectEditableToCategory, this);
			this.addSelectEditableToStatus = $.proxy(this._addSelectEditableToStatus, this);
			this.addSimpleEditableToReference = $.proxy(this._addSimpleEditableToReference, this);
			this.addSimpleEditableToLabel = $.proxy(this._addSimpleEditableToLabel, this);
			this.addInterfaceLevel2Link = $.proxy(this._addInterfaceLevel2Link, this);
			this.addIconToAssociatedToColumn = $.proxy(this._addIconToAssociatedToColumn, this);
			this.addTreeLink = $.proxy(this._addTreeLink, this);
			this.getTableRowId = $.proxy(this._getTableRowId, this);
			this.tableRowCallback = $.proxy(this._tableRowCallback, this);

			var self = this;
			var tableConf ;
			var squashConf;
			if(isAssociation){

				tableConf = {
						"oLanguage" : {
							"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
						},
						"bServerSide": true,
						"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
						"fnServerParams": function ( aoData )
							{
								aoData.push( { "name": "model", "value": JSON.stringify(model) } );
								aoData.push( { "name": "associateResultWithType", "value": associateType } );
								aoData.push( { "name": "id", "value":  associateId } );
								aoData.push( { "name": "campaign", "value": "campaign" } );
							},
						"sServerMethod": "POST",
						"bDeferRender" : true,
						"bFilter" : false,
						"fnRowCallback" : this.tableRowCallback,
						"fnDrawCallback" : this.tableDrawCallback,
						"aaSorting" : [ [ 2, "asc" ],  [4, "asc"], [6, "asc"], [7, "asc"], [8, "asc"], [5, "asc"] ],
						"aoColumnDefs" : [ {
							"bSortable" : false,
							"aTargets" : [ 0 ],
							"mDataProp" : "entity-index",
							"sClass" : "select-handle centered"
						}, {
							"aTargets" : [ 1 ],
							"mDataProp" : "empty-is-associated-holder",
							"bSortable" : false,
							"sWidth" : "2em",
							"sClass" : "is-associated centered"
						}, {
							"aTargets" : [ 2 ],
							"mDataProp" : "project-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 3 ],
							"mDataProp" : "campaign-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 4 ],
							"mDataProp" : "iteration-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 5 ],
							"mDataProp" : "execution-id",
							"bSortable" : true,
							"sClass" : "element_id"
						}, { 
							"aTargets" : [ 6 ],
							"mDataProp" : "execution-mode",
							"bSortable" : true
						}, {
							"aTargets" : [ 7 ],
							"mDataProp" : "execution-milestone-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 8 ],
							"mDataProp" : "testsuite-execution",
							"bSortable" : true
						}, {
							"aTargets" : [ 9 ],
							"mDataProp" : "execution-status",
							"bSortable" : true,
							"sClass" : "centered"
						},{
							"aTargets" : [ 10 ],
							"mDataProp" : "execution-executed-by",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 11 ],
							"mDataProp" : "execution-executed-on",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 12 ],
							"mDataProp" : "execution-datasets",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 13 ],
							"mDataProp" : "empty-openinterface2-holder",
							"sClass" : "centered search-open-interface2-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 14 ],
							"mDataProp" : "editable",
							"bVisible" : false,
							"bSortable" : false
						} ],
						"sDom" : 'ft<"dataTables_footer"lip>'
					};

				squashConf = {
						enableHover : true
					};

				this.$el.squashTable(tableConf, squashConf);
			} else {
				tableConf = {
						"oLanguage" : {
							"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
						},
						"bServerSide": true,
						"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
						"fnServerParams": function ( aoData )
							{
								aoData.push( { "name": "model", "value": JSON.stringify(model) } );
								aoData.push( { "name": "campaign", "value": "campaign" } );
							},
						"sServerMethod": "POST",
						"bDeferRender" : true,
						"bFilter" : false,
						"fnRowCallback" : this.tableRowCallback,
						"fnDrawCallback" : this.tableDrawCallback,
						"aaSorting" : [ [ 1, "asc" ], [ 3, "asc" ], [ 5, "asc" ], [ 6, "asc" ], [ 7, "asc" ], [ 4, "asc" ] ],
						"aoColumnDefs" : [ {
							"bSortable" : false,
							"aTargets" : [ 0 ],
							"mDataProp" : "entity-index",
							"sClass" : "select-handle centered"
						}, {
							"aTargets" : [ 1 ],
							"mDataProp" : "project-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 2 ],
							"mDataProp" : "campaign-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 3 ],
							"mDataProp" : "iteration-name",
							"bSortable" : true
						}, {
							"aTargets" : [ 4 ],
							"mDataProp" : "execution-id",
							"bSortable" : true,
							"sClass" : "element_id"
						}, {
							"aTargets" : [ 5 ],
							"mDataProp" : "execution-mode",
							"bSortable" : true
						}, {
							"aTargets" : [ 6 ],
							"mDataProp" : "execution-milestone-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 7 ],
							"mDataProp" : "testsuite-execution",
							"bSortable" : true
						}, {
							"aTargets" : [ 8 ],
							"mDataProp" : "execution-status",
							"bSortable" : true,
							"sClass" : "centered"
						},{
							"aTargets" : [ 9 ],
							"mDataProp" : "execution-executed-by",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 10 ],
							"mDataProp" : "execution-executed-on",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 11 ],
							"mDataProp" : "execution-datasets",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 12 ],
							"mDataProp" : "empty-openinterface2-holder",
							"sClass" : "centered search-open-interface2-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 13 ],
							"mDataProp" : "empty-opentree-holder",
							"sClass" : "centered search-open-tree-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 14 ],
							"mDataProp" : "editable",
							"bVisible" : false,
							"bSortable" : false
						} ],
						"sDom" : 'ft<"dataTables_footer"lip>'
					};

				squashConf = {
					enableHover : true
				};

				this.$el.squashTable(tableConf, squashConf);
			}


		},

		_getTableRowId : function(rowData) {
			return rowData[2];
		},

		// _addSelectEditableToCriticality 

	  //	_addSelectEditableToCategory 

		// _addSelectEditableToStatus 

		// _addSimpleEditableToReference 

		// _addSimpleEditableToLabel 

		_tableRowCallback : function(row, data, displayIndex) {
		/*	if(data.editable){
				this.addSimpleEditableToReference(row,data);
				this.addSimpleEditableToLabel(row,data);
				this.addSelectEditableToCriticality(row,data);
				this.addSelectEditableToCategory(row,data);
				this.addSelectEditableToStatus(row,data);
			}else{*/
				$(row).addClass("nonEditable");
			//	$(row).attr('title', squashtm.app.campaignSearchResultConf.messages.nonEditableTooltip);
			//}
			this.addInterfaceLevel2Link(row,data);
			this.addTreeLink(row,data);

			if(this.isAssociation){
				this.addIconToAssociatedToColumn(row,data);
			}
		},

		_addInterfaceLevel2Link : function(row, data) {
			var id = data["execution-id"];
			var $cell = $(".search-open-interface2-holder",row);
			$cell.append('<span class="ui-icon ui-icon-pencil"></span>')
			.click(function(){
				
				// TODO : get the exact url
				window.location = squashtm.app.contextRoot + "/campaigns/" + id + "/info";
			});
		},

		_addIconToAssociatedToColumn : function(row, data) {

			var associatedTo = data["is-associated"];

			if(associatedTo){
				if(this.associateType == "campaign"){
					$(".is-associated",row).append('<span class="associated-icon-requirement"></span>');
				} else if(this.associateType == "testcase"){
					$(".is-associated",row).append('<span class="associated-icon-testcase" title="'+translator.get('search.associatedwith.testcase.image.tooltip')+'"></span>');
				} else {
					$(".is-associated",row).append('<span class="associated-icon-campaign"></span>');
				}
			}
		},

		_addTreeLink : function(row, data){
			var self = this;
			var id = data["execution-id"];
			var $cell = $(".search-open-tree-holder", row);
			$cell.append('<span class="search-open-tree"></span>')
				.click(function(){
					$.cookie("workspace-prefs", id, {path : "/"});
					window.location = squashtm.app.contextRoot + "campaign-workspace/";
			});
		},

		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return CampaignSearchResultTable;
});