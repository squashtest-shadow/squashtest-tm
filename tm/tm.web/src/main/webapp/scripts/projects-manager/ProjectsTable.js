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
		return rowData["project-id"];	
	}

	function addHLinkToProjectName(row, data) {
		var url= squashtm.app.contextRoot + "/administration/projects/" + getProjectTableRowId(data) + "/info";			
		SQDT.addHLinkToCellText($( row ).find("td.name"), url);
	}

	function addTemplateIcon(row, data) {
		var type = data["raw-type"];
		$( row ).find(".type")
			.addClass("type-" + type)
			.attr("title", squashtm.app.projectsManager.tooltips[type]);
	}

	var View = Backbone.View.extend({
		el: "#projects-table", 
		initialize : function() {
			var self = this, 
				tableConf = {
					"oLanguage": {
						"sUrl": squashtm.app.contextRoot + "/datatables/messages"
					},
					"sAjaxSource": squashtm.app.contextRoot + "/generic-projects", 
					"bDeferRender": true,
					"iDeferLoading": squashtm.app.projectsManager.deferLoading, 
					"fnRowCallback": this.projectTableRowCallback,
					"fnDrawCallback": this.tableDrawCallback, 
					"aaSorting": [ [ 2, "asc" ] ], 
					"aoColumnDefs": [ {
						"bVisible": false,
						"aTargets": [ 0 ],
						"mDataProp": "project-id",
						"sClass": "project-id"
					}, {
						"aTargets": [ 1 ], 
						"mDataProp": "index",
						"bSortable": false, 
						"sClass": "select-handle centered"
					}, {
						"aTargets": [ 2 ], 
						"mDataProp": "name",
						"sClass": "name", 
						"bSortable": true
					}, {
						"aTargets": [ 3 ], 
						"mDataProp": "raw-type",
						"bSortable": false, 
						"bVisible": false
					}, {
						"aTargets": [ 4 ], 
						"mDataProp": "type",
						"sClass": "icon-cell type",
						"bSortable": false
					}, {
						"aTargets": [ 5 ], 
						"mDataProp": "label",
						"bSortable": true
					}, {
						"aTargets": [ 6 ], 
						"mDataProp": "active",
						"bSortable": false, 
						"bVisible": false
					}, {
						"aTargets": [ 7 ], 
						"mDataProp": "created-on",
						"bSortable": true
					}, {
						"aTargets": [ 8 ], 
						"mDataProp": "created-by",
						"bSortable": true
					}, {
						"aTargets": [ 9 ], 
						"mDataProp": "last-mod-on",
						"bSortable": true
					}, {
						"aTargets": [ 10 ], 
						"mDataProp": "last-mod-by",
						"bSortable": true
					}]
			}, squashConf = {
					
			};
			
			this.$el.squashTable(tableConf, squashConf);			
		}, 
		
		refresh: function() {
			this.$el.squashTable().fnDraw(false);
		}, 
		
		tableDrawCallback: function() {
			addHoverHandler(this);
		},	
		
		projectTableRowCallback: function(row, data, displayIndex) {
			addHLinkToProjectName(row, data);
			addTemplateIcon(row, data);
			return row;
		}
	});

	return View;
});