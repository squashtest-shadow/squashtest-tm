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
		[ "jquery", "backbone","jeditable.simpleJEditable", "jquery.squash.confirmdialog",
				"jquery.squash.messagedialog", "jquery.squash.datatables" ],
				function($, Backbone, SimpleJEditable) {
					var DatasetsTable = Backbone.View.extend({
						
						el : "#datasets-table",
						
						initialize : function() {
						this.settings = this.options.settings;
							this.removeRowDataset = $.proxy(
									this.removeRowDataset, this);											
							this.confirmRemoveDataset = $.proxy(
									this._confirmRemoveDataset, this);
							
							this.refresh = $.proxy(this._refresh, this);
							this.configureTable.call(this);
							this.configureRemoveDatasetDialog.call(this);
						},
						
						events : {
							
						},
						
						
						dataTableSettings : function(self) {
							return {
								"oLanguage": {sUrl:self.settings.basic.dataTableLanguageUrl},	
								"sAjaxSource": self.settings.basic.testCaseDatasetsUrl, 
								"bPaginate": false,
								"aaSorting" : [ [ 3, 'asc' ] ],
								"aoColumnDefs": JSON.parse( self.settings.datasetsAoColumnDefs),
							};
						},

						squashSettings : function(self) {

							var squashSettings = {};

							if (self.settings.permissions.isWritable) {
								squashSettings = {
									buttons : [ {
										tooltip : self.settings.language.remove,
										cssClass : "",
										tdSelector : "td.delete-button",
										uiIcon : "ui-icon-minus",
										onClick : this.removeRowDataset
									} ]
								};
							}

							return squashSettings;

						},

						configureTable : function() {
							var self = this;
							$(this.el).squashTable(
									self.dataTableSettings(self),
									self.squashSettings(self));
							this.table = $(this.el).squashTable();
						},

					
						//call removeRowDataset instead//
						_removeRowDataset : function(table, cell) {
							var row = cell.parentNode.parentNode;
							this.confirmRemoveDataset(row);
						},

						
						//call confirmRemoveDataset instead//
						_confirmRemoveDataset: function(row) {
							var self = this;
							var paramId = self.table.getODataId(row);
							self.toDeleteId = paramId;
							self.confirmRemoveParameterDialog.confirmDialog("open");
						},
						

						removeDataset : function() {
							var self = this;
							var id = this.toDeleteId;					
							$.ajax({
								url : self.settings.basic.datasetsUrl + '/' + id,
								type : 'delete'
							}).done(self.refresh);
						},

						configureRemoveDatasetDialog : function() {
							var self = this;
							this.confirmRemoveDatasetDialog = $(
									"#remove-dataset-confirm-dialog")
									.confirmDialog();
							
							this.confirmRemoveDatasetDialog.on(
									"confirmdialogconfirm", $.proxy(
											self._removeDataset, self));
							this.confirmRemoveDatasetDialog.on("close", $
									.proxy(function() {
										this.toDeleteId = null;
									}, this));
							
						},

						refresh : function() {
							this.table.fnDraw(false);
							this.options.parentTab.trigger("datasets.table.refresh");
						}
					});
						
					return DatasetsTable;

		});
