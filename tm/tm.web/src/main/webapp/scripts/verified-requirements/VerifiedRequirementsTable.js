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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "jquery.squash.datatables", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ], function($, Backbone, _, StringUtil) {
	var VRTS = squashtm.app.verifiedRequirementsTableSettings;
	var VerifiedRequirementsTable = Backbone.View.extend({

		el : "#verified-requirements-table",

		initialize : function() {
			VRTS = squashtm.app.verifiedRequirementsTableSettings;
			this.removeRequirements = $.proxy(this._removeRequirements, this);
			this.removeRowRequirementVersion = $.proxy(this._removeRowRequirementVersion, this);
			this.requirementsTableDrawCallback = $.proxy(this._requirementsTableDrawCallback, this);
			this.requirementsTableRowCallback = $.proxy(this._requirementsTableRowCallback, this);
			this.removeSelectedRequirements = $.proxy(this._removeSelectedRequirements, this);
			this.confirmRemoveRequirements = $.proxy(this._confirmRemoveRequirements, this);
			this.addSelectEditableToVersionNumber = $.proxy(this._addSelectEditableToVersionNumber, this);
			this.refresh = $.proxy(this._refresh, this);
			this.configureTable.call(this);
			this.configurePopups.call(this);
		},

		events : {},

		configurePopups : function() {
			this.configureRemoveRequirementDialogs.call(this);
			this.configureNoRequirementSelectedDialog.call(this);
		},
		dataTableSettings : function(self) {
			return {
				// has Dom configuration
				"aaSorting" : [ [ 4, 'asc' ] ],
				"fnRowCallback" : this.requirementsTableRowCallback,
				"fnDrawCallback" : this.requirementsTableDrawCallback
			};
		},

		squashSettings : function(self) {

			var settings = {};

			if (VRTS.linkable) {
				settings.buttons = [ {
					tooltip : VRTS.messages.remove,
					cssClass : "",
					tdSelector : "td.delete-button",
					uiIcon : "ui-icon-minus",
					onClick : this.removeRowRequirementVersion
				} ];
			}

			return settings;

		},

		configureTable : function() {
			var self = this;
			this.table = this.$el.squashTable(self.dataTableSettings(self), self.squashSettings(self));
		},

		_requirementsTableDrawCallback : function() {
			if (this.table) {// We do not restore table
				// selection for first drawing
				// on pre-filled tables.
				restoreTableSelection(this.table, function(data) {
					return data["entity-id"];
				});
			}
		},

		_requirementsTableRowCallback : function(row, data, displayIndex) {
			if (VRTS.linkable && data.status != "OBSOLETE") {
				this.addSelectEditableToVersionNumber(row, data);
			}
			return row;
		},

		_removeRowRequirementVersion : function(table, cell) {
			var row = cell.parentNode.parentNode;
			this.confirmRemoveRequirements([ row ]);
		},

		_removeSelectedRequirements : function() {
			var rows = this.table.getSelectedRows();
			this.confirmRemoveRequirements(rows);
		},

		_confirmRemoveRequirements : function(rows) {
			var self = this;
			this.toDeleteIds = [];
			var rvIds = $(rows).collect(function(row) {
				return self.table.getODataId(row);
			});
			var hasRequirement = (rvIds.length > 0);
			if (hasRequirement) {
				this.toDeleteIds = rvIds;
				var obsoleteStatuses = $(rows).not(function(index, row) {
					var data = self.table.fnGetData(row);
					return data.status != "OBSOLETE";
				});
				if (obsoleteStatuses.length > 0) {
					this.confirmRemoveObsoleteRequirementDialog.confirmDialog("open");
				} else {
					this.confirmRemoveRequirementDialog.confirmDialog("open");
				}
			} else {
				this.noRequirementSelectedDialog.messageDialog('open');
			}

		},

		_removeRequirements : function() {
			var self = this;
			var ids = this.toDeleteIds;
			if (ids.length === 0) {
				return;
			}
			$.ajax({
				url : VRTS.url + '/' + ids.join(','),
				type : 'delete'
			}).done(self.refresh);

		},

		configureRemoveRequirementDialogs : function() {
			// confirmRemoveRequirementDialog
			this.confirmRemoveRequirementDialog = $("#remove-verified-requirement-version-dialog").confirmDialog();
			this.confirmRemoveRequirementDialog.width("600px");
			this.confirmRemoveRequirementDialog.on("confirmdialogconfirm", $.proxy(this.removeRequirements, this));
			this.confirmRemoveRequirementDialog.on("close", $.proxy(function() {
				this.toDeleteIds = [];
			}, this));
			// confirmRemoveObsoleteRequirementDialog
			this.confirmRemoveObsoleteRequirementDialog = $("#remove-obsolete-verified-requirement-version-dialog")
					.confirmDialog();
			this.confirmRemoveObsoleteRequirementDialog.width("600px");
			this.confirmRemoveObsoleteRequirementDialog.on("confirmdialogconfirm", $.proxy(this.removeRequirements,
					this));
			this.confirmRemoveObsoleteRequirementDialog.on("close", $.proxy(function() {
				this.toDeleteIds = [];
			}, this));
		},

		configureNoRequirementSelectedDialog : function() {
			this.noRequirementSelectedDialog = $("#no-selected-requirement-dialog").messageDialog();
		},

		// =====================================================

		_addSelectEditableToVersionNumber : function(row, data) {
			var self = this;
			var urlPOST = VRTS.url + '/' + data["entity-id"];
			var urlGET = squashtm.app.contextRoot + '/requirement-versions/' + data["entity-id"] + '/version-numbers';

			// the table needs to be redrawn after each return
			// of the POST so we implement the posting workflow
			$('td.versionNumber', row).editable(function(value, settings) {
				var innerPOSTData;
				$.post(urlPOST, {
					value : value
				}, function(data) {
					innerPOSTData = data;
					self.refresh();
				});
				return (innerPOSTData);
			}, {
				type : 'select',
				submit : VRTS.messages.ok,
				cancel : VRTS.messages.cancel,
				onblur : function() {
				}, // prevents the widget to return to
				// unediting state on blur event
				// --%>
				loadurl : urlGET,
				onsubmit : function() {
				} // - do nothing for now
			});

		},

		_refresh : function() {
			var self = this;
			saveTableSelection(self.table, function(data) {
				return data["entity-id"];
			});
			this.table.fnDraw(false);
			$("#" + VRTS.containerId).trigger("verifiedrequirementversions.refresh");
		}

	});
	return VerifiedRequirementsTable;
});