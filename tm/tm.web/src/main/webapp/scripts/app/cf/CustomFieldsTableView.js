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
define(
		[ "jquery", "backbone", "app/cf/NewCustomFieldPanelView",
				"app/cf/NewCustomFieldModel", "jquery.squash.datatables",
				"jqueryui" ],
		function($, Backbone, NewCustomFieldPanelView, NewCustomFieldModel) {
			var cfTable = squashtm.app.cfTable;
			/*
			 * Defines the controller for the custom fields table.
			 */
			var CustomFieldTableView = Backbone.View
					.extend({
						el : "#cf-table-pane",
						initialize : function() {
							var self = this;
							$(".button").button();
							// this.el is decorated with an ajax sourced
							// datatable
							var config = $.extend({
								"oLanguage" : {
									"sUrl" : cfTable.languageUrl
								},
								"bJQueryUI" : true,
								"bAutoWidth" : false,
								"bFilter" : false,
								"bPaginate" : true,
								"sPaginationType" : "squash",
								"iDisplayLength" : cfTable.displayLength,
								"bServerSide" : true,
								"sAjaxSource" : cfTable.ajaxSource,
								"bDeferRender" : true,
								"bRetrieve" : true,
								"sDom" : 't<"dataTables_footer"lp>',
								"iDeferLoading" : 0,
								"aaSorting" : [ [ 2, "asc" ] ],
								"fnRowCallback" : function() {
								},
								"aoColumnDefs" : [
									{
										"bVisible" : false,
										"aTargets" : [ 0 ],
										"sClass" : "cf-id",
										"mDataProp" : "entity-id"
									},
									{
										'bSortable' : false,
										'sClass' : 'centered ui-state-default drag-handle select-handle',
										'aTargets' : [ 1 ],
										'mDataProp' : 'entity-index'
									},
									{
										"bSortable" : true,
										"aTargets" : [ 2 ],
										"mDataProp" : "name"
									},
									{
										"bSortable" : true,
										"aTargets" : [ 3 ],
										"mDataProp" : "label"
									},
									{
										"bVisible" : false,
										"aTargets" : [ 4 ],
										"sClass" : "raw-input-type",
										"mDataProp" : "raw-input-type"
									},
									{
										"bSortable" : true,
										"aTargets" : [ 5 ],
										"mDataProp" : "input-type"
									},
									{
										'bSortable' : false,
										'sWidth' : '2em',
										'sClass' : 'delete-button',
										'aTargets' : [ 6 ],
										'mDataProp' : 'empty-delete-holder'
									} ]
							}, squashtm.datatable.defaults);

							var squashSettings = {
								enableHover : true,

								confirmPopup : {
									oklabel : cfTable.confirmLabel,
									cancellabel : cfTable.cancelLabel
								},

								deleteButtons : {
									url : cfTable.ajaxSource + "/{entity-id}",
									popupmessage : cfTable.deleteConfirmMessage,
									tooltip : cfTable.deleteTooltip,
									success : function(data) {
										self.table.refresh();
									}
								},

								bindLinks : {
									list : [ {
										url : cfTable.customFieldUrl + "/{entity-id}",
										target : 2,
										isOpenInTab : false
									} ]
								}
							};

							this.table = this.$("table");
							this.table.squashTable(config, squashSettings);

							this.$("input:button").button();
						},

						events : {
							"click #add-cf" : "showNewCfPanel"
						},

						showNewCfPanel : function(event) {
							var self = this, showButton = event.target;

							function discard() {
								self.newCfPanel
										.off("newcustomfield.cancel newcustomfield.confirm");
								self.newCfPanel.undelegateEvents();
								self.newCfPanel = null;
								$(showButton).button("enable");
								self.table.squashTable().fnDraw();
							}

							function discardAndRefresh() {
								discard();
								self.table.squashTable().fnDraw();
							}

							$(event.target).button("disable");
							self.newCfPanel = new NewCustomFieldPanelView({
								model : new NewCustomFieldModel()
							});

							self.newCfPanel
									.on("newcustomfield.cancel", discard);
							self.newCfPanel.on("newcustomfield.confirm",
									discardAndRefresh);
						}
					});
			return CustomFieldTableView;
		});