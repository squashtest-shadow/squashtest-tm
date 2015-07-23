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
define([ "jquery", "backbone", "squash.translator", '../test-plan-panel/exec-runner',"jeditable.simpleJEditable", "workspace.projects",
         "squash.configmanager", "workspace.routing", "app/ws/squashtm.notification", "squashtable",
         "jqueryui", "jquery.squash.jeditable", "jquery.cookie" ],
         function($, Backbone, translator, execrunner, SimpleJEditable, projects, confman, routing, notification) {

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
						"aaSorting" : [ [ 1, "asc" ], [ 2, "asc" ], [ 3, "asc" ], [ 4, "asc" ], [ 5, "asc" ], [ 6, "asc" ], [ 7, "asc" ], [ 8, "asc" ], [ 9, "asc" ], [ 10, "asc" ]],
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
							"mDataProp" : "testsuite-execution",
							"bSortable" : true
						}, {
							"aTargets" : [ 7 ],
							"mDataProp" : "execution-status",
							"bSortable" : true
						},{
							"aTargets" : [ 8 ],
							"mDataProp" : "execution-executed-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 9 ],
							"mDataProp" : "execution-executed-on",
							"bSortable" : true
						}, {
							"aTargets" : [ 10 ],
							"mDataProp" : "execution-datasets",
							"bSortable" : true
						}, {
							"aTargets" : [ 11 ],
							"mDataProp" : "empty-opentree-holder",
							"sClass" : "centered search-open-tree-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 12 ],
							"mDataProp" : "empty-openinterface2-holder",
							"sClass" : "centered search-open-interface2-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 13 ],
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

		manualHandler : function() {

			var $this = $(this),
				tpid = $this.data('tpid'),
				suiteId = $this.data('suiteid'),
				
				ui = ($this.is('.run-popup')) ? "popup" : "oer", newurl = squashtm.app.contextRoot + '/test-suites/' + tpid + '/test-plan/' + suiteId + '/executions/new';

			$.post(newurl, {
				mode : 'manual'
			}, 'json').done(function(execId) {
				var execurl = squashtm.app.contextRoot + "/executions/" + execId + '/runner'; 
				if (ui === "popup") {
					execrunner.runInPopup(execurl);
				} else {
					execrunner.runInOER(execurl);
				}

			});
		},

		automatedHandler : function() {
			var row = $(this).parents('tr').get(0);
			var	table = $("#iteration-test-plans-table").squashTable();
			var	data = table.fnGetData(row);

			var tpiIds =  [];
			var tpiId = data['entity-id'];
			tpiIds.push(tpiId);

			var url = squashtm.app.contextRoot + "/automated-suites/new";

			var formParams = {};
			var ent =  squashtm.page.identity.restype === "iterations" ? "iterationId" : "testSuiteId";
			formParams[ent] = squashtm.page.identity.resid;
			formParams.testPlanItemsIds = tpiIds;

			$.ajax({
				url : url,
				dataType:'json',
				type : 'post',
				data : formParams,
				contentType : "application/x-www-form-urlencoded;charset=UTF-8"
			}).done(function(suite) {
				squashtm.context.autosuiteOverview.start(suite);
			});
			return false;
 
		},
		
		_tableRowCallback : function(row, data, displayIndex) {
			 
			// add the execute shortcut menu
			
			// Instead of "Manuel", get the i18n thing TODO !
			var manual = translator.get("test-case.execution-mode.MANUAL");
			var isTcDel = data['is-tc-deleted'], isManual = (data['execution-mode'] === manual);
			
			// There tpId is wrong
			var tpId = data['execution-id'], suiteId = data['execution-suiteId'], $td = $(row).find('.search-open-interface2-holder'); 
			strmenu1 = $("#shortcut-exec-menu-template").html().replace(/#placeholder-tpid#/g, tpId);
			strmenu = strmenu1.replace(/#placeholder-suiteid#/g, suiteId);

			$td.empty();  
			$td.append(strmenu);
			
			// if the test case is deleted : just disable the whole thing
			// Plot twist  : Launch button has to be greyed
			if (isTcDel) {
				$td.find('.execute-arrow').addClass('disabled-transparent');
				$("#test-suite-execution-button").addClass('disabled-transparent');
			}

			// if the test case is manual : configure a button menu,
			// althgouh we don't want it
			// to be skinned as a regular jquery button
			else if (isManual) {
				$td.find('.buttonmenu').buttonmenu({
					anchor : "right"
				}); 
				$td.on('click', '.run-menu-item', this.manualHandler);
			}

			// if the test case is automated : just configure the button
			else {
				$td.find('.execute-arrow').click(this.automatedHandler);
			}
			
			
			// Add another stuff
			
			
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
			this.addTreeLink(row,data);

			if(this.isAssociation){
				this.addIconToAssociatedToColumn(row,data);
			}
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
			// Get id of execution to put in the link when clicked
			var id = data["execution-id"];
			
			var $cell = $(".search-open-tree-holder", row);
			$cell.append('<span class="search-open-tree"></span>')
				.click(function(){
					$.cookie("workspace-prefs", id, {path : "/"});
					window.location = squashtm.app.contextRoot + "executions/" + id ;
			});
		},

		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return CampaignSearchResultTable;
});