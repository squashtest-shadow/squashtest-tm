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
define([ "jquery", "backbone", "squash.datatables", "jquery.squash.datatables", "jqueryui" ], function($, Backbone, SQDT) {
	function addHoverHandler(dataTable){
		$( 'tbody tr', dataTable ).hover(
			function() {
				$( this ).addClass( 'ui-state-highlight' );
			}, 
			function() {
				$( this ).removeClass( 'ui-state-highlight' );
			} 
		);
	}
	
	function getProjectTableRowId(rowData) {
		return rowData[0];	
	}

	function addHLinkToProjectLogin(row, data) {
		var url= squashtm.app.contextRoot + "/administration/projects" + getProjectTableRowId(data) + '/info';			
		SQDT.addHLinkToCellText($( 'td:eq(1)', row ), url);
	}

	var View = Backbone.View.extend({
		el: "#projects-table", 
		initialize : function() {
			var self = this, 
				tableConf = {
					"oLanguage": {
						"sUrl": squashtm.app.contextRoot + "/datatables/messages"
					},
					"sAjaxSource": squashtm.app.contextRoot + "/administration/projects/list", 
				/*	"bDeferRender" : true,
					"iDeferLoading" : 0,*/
					"fnRowCallback": this.projectTableRowCallback,
					"fnDrawCallback": this.tableDrawCallback, 
					"aaSorting": [ [ 2, "asc" ] ], 
					"aoColumnDefs": [ {
						"bVisible": false,
						"aTargets": [ 0 ],
						"sClass": "project-id"
//						"mDataProp" : "entity-id"
					}, {
						"aTargets": [ 1 ], 
						"bSortable": false
					}, {
						"aTargets": [ 2 ], 
						"bSortable": true
					}, {
						"aTargets": [ 3 ], 
						"bSortable": true
					}, {
						"aTargets": [ 2 ], 
						"bSortable": false, 
						"bVisible": false
					}, {
						"aTargets": [ 5 ], 
						"bSortable": true
					}, {
						"aTargets": [ 6 ], 
						"bSortable": true
					}, {
						"aTargets": [ 7 ], 
						"bSortable": true
					}, {
						"aTargets": [ 8 ], 
						"bSortable": true
					}]
			}, squashConf = {
					
			};
			
			this.$el.squashTable(tableConf, squashConf);
			this.dataTable = this.$el.squashTable();
			
		}, 
		refreshProjects: function() {
			table.fnDraw(false);
		}, 
		tableDrawCallback: function() {
			addHoverHandler(this);
		},	
		projectTableRowCallback: function(row, data, displayIndex) {
			addHLinkToProjectLogin(row, data);
			return row;
		}
	});

	return View;
});