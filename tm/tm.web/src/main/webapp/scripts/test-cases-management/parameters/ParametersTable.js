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

define([ "jquery", "backbone", "jeditable.simpleJEditable", "jquery.squash.confirmdialog",
		"jquery.squash.messagedialog", "squashtable" ], function($, Backbone, SimpleJEditable) {
	var ParametersTable = Backbone.View.extend({

		el : "#parameters-table",

		initialize : function() {
			this.settings = this.options.settings;
			this.removeRowParameter = $.proxy(this._removeRowParameter, this);
			this.parametersTableRowCallback = $.proxy(this._parametersTableRowCallback, this);
			this.confirmRemoveParameter = $.proxy(this._confirmRemoveParameter, this);

			this.refresh = $.proxy(this._refresh, this);
			this._configureTable.call(this);
			this._configureRemoveParametersDialogs.call(this);
		},

		events : {

		},

		_dataTableSettings : function(self) {
			return {
				// has Dom configuration
				"bPaginate" : false,
				"aaSorting" : [ [ 3, 'asc' ] ],
				"fnRowCallback" : self.parametersTableRowCallback
			};
		},

		_squashSettings : function(self) {

			var squashSettings = {};

			if (self.settings.permissions.isWritable) {
				squashSettings = {
					buttons : [ {
						tooltip : self.settings.language.remove,
						cssClass : "",
						tdSelector : "td.delete-button",
						uiIcon : "ui-icon-trash",
						onClick : this.removeRowParameter,
						condition : function(row, data) {
							return data["directly-associated"];
						}
					} ],

					richEditables : {
						conf : {
							ckeditor : {
								customConfig : self.settings.basic.ckeConfigUrl,
								language : self.settings.language.ckeLang
							},
							placeholder : self.settings.language.placeholder,
							submit : self.settings.language.submit,
							cancel : self.settings.language.cancellabel,
							indicator : self.settings.basic.indicatorUrl
						},

						targets : {
							'parameter-description' : self.settings.basic.parametersUrl + '/{entity-id}/description'
						}
					}
				};
			}

			return squashSettings;

		},

		_configureTable : function() {
			var self = this;
			$(this.el).squashTable(self._dataTableSettings(self), self._squashSettings(self));
			this.table = $(this.el).squashTable();
		},

		_parametersTableRowCallback : function(row, data, displayIndex) {
			if (this.settings.permissions.isWritable) {
				this.addSimpleJEditableToName(row, data);
			}

			return row;
		},

		_removeRowParameter : function(table, cell) {
			var row = cell.parentNode.parentNode;
			this.confirmRemoveParameter(row);
		},

		_confirmRemoveParameter : function(row) {
			var self = this;
			var paramId = self.table.getODataId(row);

			self._isUsed.call(self, paramId).done(function(isUsed) {
				if (isUsed) {
					self.cannotRemoveUsedParamDialog.openMessage();
				} else {
					self.toDeleteId = paramId;
					self.confirmRemoveParameterDialog.confirmDialog("open");
				}
			});
		},

		_isUsed : function(paramId) {
			var self = this;
			return $.ajax({
				url : self.settings.basic.parametersUrl + "/" + paramId + "/used",
				type : "get"
			});
		},

		_removeParameter : function() {
			var self = this;
			var id = this.toDeleteId;
			$.ajax({
				url : self.settings.basic.parametersUrl + '/' + id,
				type : 'delete'
			}).done(function() {
				self.refresh();
				self.trigger("parameterstable.removed");
			});
		},

		_configureRemoveParametersDialogs : function() {
			var self = this;
			this.confirmRemoveParameterDialog = $("#remove-parameter-confirm-dialog").confirmDialog();

			this.confirmRemoveParameterDialog.on("confirmdialogconfirm", $.proxy(self._removeParameter, self));
			this.confirmRemoveParameterDialog.on("close", $.proxy(function() {
				this.toDeleteId = null;
			}, this));

			this.cannotRemoveUsedParamDialog = $("#remove-parameter-used-dialog").messageDialog();
		},

		// =====================================================

		addSimpleJEditableToName : function(row, data) {
			var self = this;
			var urlPOST = self.settings.basic.parametersUrl + '/' + data["entity-id"] + "/name";
			var component = $('td.parameter-name', row);
			new SimpleJEditable({
				language : {
					richEditPlaceHolder : self.settings.language.placeholder,
					okLabel : self.settings.language.submit,
					cancelLabel : self.settings.language.cancellabel
				},
				targetUrl : urlPOST,
				component : component,
				jeditableSettings : {}
			});
		},

		_refresh : function() {
			this.table.fnDraw(false);
		}
	});

	return ParametersTable;

});
