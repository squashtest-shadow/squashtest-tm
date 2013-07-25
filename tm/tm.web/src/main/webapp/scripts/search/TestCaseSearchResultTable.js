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
define([ "jquery", "backbone", "squash.datatables", "jquery.squash.datatables", "jqueryui" ], function($, Backbone,
		SQDT) {

	var TestCaseSearchResultTable = Backbone.View.extend({
		el : "#test-case-search-result-table",
		initialize : function() {
			var self = this, tableConf = {
				"oLanguage" : {
					"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
				},
				"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
				"bDeferRender" : true,
				"bFilter" : false,
				"fnRowCallback" : this.teamTableRowCallback,
				"fnDrawCallback" : this.tableDrawCallback,
				"aaSorting" : [ [ 2, "asc" ] ],
				"aoColumnDefs" : [ {
					"bSortable" : false,
					"aTargets" : [ 0 ],
					"mDataProp" : "entity-index",
					"sClass" : "select-handle centered"
				}, {
					"aTargets" : [ 1 ],
					"mDataProp" : "project-name",
					"bSortable" : false,
				}, {		
					"aTargets" : [ 2 ],
					"mDataProp" : "test-case-id",
					"bSortable" : false,
				}, {
					"aTargets" : [ 3 ],
					"mDataProp" : "test-case-ref",
					"bSortable" : true,
					"sClass" : "editable_ref"
				}, {
					"aTargets" : [ 4 ],
					"mDataProp" : "test-case-label",
					"bSortable" : false,
					"sClass" : "editable_label"
				}, {
					"aTargets" : [ 5 ],
					"mDataProp" : "test-case-weight",
					"bSortable" : true,
					"sClass" : "editable_imp"
				}, {
					"aTargets" : [ 6 ],
					"mDataProp" : "test-case-requirement-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 7 ],
					"mDataProp" : "test-case-teststep-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 8 ],
					"mDataProp" : "test-case-iteration-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 9 ],
					"mDataProp" : "test-case-attachment-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 10 ],
					"mDataProp" : "test-case-created-by",
					"bSortable" : true
				}, {
					"aTargets" : [ 11 ],
					"mDataProp" : "test-case-modified-by",
					"bSortable" : true
				}, {
					"aTargets" : [ 12 ],
					"mDataProp" : "empty-openinterface2-holder",
					"sClass" : "centered delete-button",
					"sWidth" : "2em",
					"bSortable" : false
				}, {
					"aTargets" : [ 13 ],
					"mDataProp" : "empty-opentree-holder",
					"sClass" : "centered delete-button",
					"sWidth" : "2em",
					"bSortable" : false
				}, {
					"aTargets" : [ 14 ],
					"mDataProp" : "editable",
					"bVisible" : false,
					"bSortable" : false
				} ],
				"sDom" : 'ft<"dataTables_footer"lirp>'
			}, squashConf = {
				enableHover : true/*,
				bindLinks : {
					list : [ {
						target : 2,
						url : squashtm.app.contextRoot + "/administration/teams/{entity-id}",
						isOpenInTab : false
					} ]
				},
				deleteButtons : {
					url : squashtm.app.contextRoot + "/administration/teams/{entity-id}",
					popupmessage : squashtm.app.teamsManager.table.deleteButtons.popupmessage,
					tooltip : squashtm.app.teamsManager.table.deleteButtons.tooltip,
					success : function() {
						self.refresh();
					},
					fail : function() {
					}
				}*/

			};

			this.$el.squashTable(tableConf, squashConf);
		},

		
		
		teamTableRowCallback : function(row, data, displayIndex) {
			if(data["editable"]){
				var content = $(".editable_ref", row).html();
				$(".editable_ref", row).html("<input id='reference-input'></input>")
				$("#reference-input",row).val(content);
			}
			return row;
		},

		tableDrawCallback : function() {

		},
		
		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return TestCaseSearchResultTable;
});