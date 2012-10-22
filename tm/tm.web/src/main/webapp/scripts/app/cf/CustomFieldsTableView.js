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
define([ "jquery", "backbone", "app/cf/NewCustomFieldPanelView", "app/cf/NewCustomFieldModel", "squashtm.datatables", "jqueryui" ], function($, Backbone, NewCustomFieldPanelView, NewCustomFieldModel) {
		var cfTable = squashtm.app.cfTable;
		/*
		 * Defines the controller for the custom fields table.
		 */
		var CustomFieldTableView = Backbone.View.extend({
			el: "#cf-table-pane",
			initialize: function() {
				// this.el is decorated with an ajax sourced datatable
				var config = $.extend({
					"oLanguage": {
						"sUrl": cfTable.languageUrl
					},
					"bJQueryUI": true,
					"bAutoWidth": false,
					"bFilter": false,
					"bPaginate": true,
					"sPaginationType": "squash",
					"iDisplayLength": cfTable.displayLength,
					"bProcessing": true,
					"bServerSide": true,
					"sAjaxSource": cfTable.ajaxSource,
					"bDeferRender": true,
					"bRetrieve": true,
					"sDom": 't<"dataTables_footer"lirp>',
					"iDeferLoading": cfTable.deferLoading,
					"aaSorting": [ [ 10, "desc" ] ],
					"fnRowCallback": function() {
					},
					"aoColumnDefs": [ {
						"bVisible": false,
						"aTargets": [ 0 ],
						"sClass": "cf-id",
						"mDataProp": "cf-id"
					}, {
						"bSortable": true,
						"aTargets": [ 1 ],
						"mDataProp": "cf-name"
					}, {
						"bSortable": true,
						"aTargets": [ 2 ],
						"mDataProp": "cf-label"
					}, {
						"bVisible": false,
						"aTargets": [ 3 ],
						"sClass": "raw-input-type",
						"mDataProp": "raw-input-type"
					}, {
						"bSortable": true,
						"aTargets": [ 4 ],
						"mDataProp": "input-type"
					} ]
				}, squashtm.datatable.defaults);

				this.table = this.$("table");
				this.table.dataTable(config);
				
				this.$("input:button").button();
			}, 
			events: {
				"click #add-cf": "showNewCfPanel"
			}, 
			showNewCfPanel: function(event) {
				var self = this;
				 
				var discard = function() {
					self.newCfPanel.off("cancel confirm");
					self.newCfPanel.undelegateEvents();
					self.newCfPanel = null;				
					$(event.target).button("enable");
				};
				
				$(event.target).button("disable");
				self.newCfPanel = new NewCustomFieldPanelView({ model: new NewCustomFieldModel() });
				self.newCfPanel.on("cancel confirm", discard);
			}
		});
		return CustomFieldTableView;
});