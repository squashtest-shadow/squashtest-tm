/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
define([ 'jquery', 'backbone', './NewTestAutomationServerDialogView', './NewTestAutomationServerModel', 'app/util/ButtonUtil', 'squashtable',
		'jqueryui' ], function($, Backbone, NewTestAutomationServerDialogView, NewTestAutomationServerModel, ButtonUtil) {
	var tasTable = squashtm.app.tasTable;
	/*
	 * Defines the controller for the test automation server table.
	 */
	var NewTestAutomationServersTableView = Backbone.View.extend({
		el : "#test-automation-server-table-pane",
		initialize : function() {
			var self = this;
			
			// DOM initialized table
			this.table = this.$("table");
			this.table.squashTable(squashtm.datatable.defaults, {});
			
			
		},

		events : {
			"click #add-test-automation-server" : "showNewTestAutomationServerDialog"
		},

		showNewTestAutomationServerDialog : function(event) {
			var self = this, showButton = event.target;

			function discard() {
				self.newTasDialog.off("newtestautomationserver.cancel newtestautomationserver.confirm");
				self.newTasDialog.undelegateEvents();
				self.newTasDialog = null;
				ButtonUtil.enable($(showButton));
				self.table.squashTable().fnDraw();
			}

			function discardAndRefresh() {
				discard();
				self.table.squashTable().fnDraw();
			}

			ButtonUtil.disable($(event.target));
			self.newTasDialog = new NewTestAutomationServerDialogView({
				model : new NewTestAutomationServerModel()
			});

			self.newTasDialog.on("newtestautomationserver.cancel", discard);
			self.newTasDialog.on("newtestautomationserver.confirm", discardAndRefresh);
		},
		
		configureRemoveTASDialog : function() {
			// confirmRemoveRequirementDialog
			this.confirmRemoveTASDialog = $("#remove-test-automation-server-dialog").confirmDialog();
			this.confirmRemoveTASDialog.width("600px");
			this.confirmRemoveTASDialog.on("confirmdialogconfirm", $.proxy(this.removeTestAutomationServer, this));
			this.confirmRemoveTASDialog.on("close", $.proxy(function() {
				this.toDeleteIds = [];
			}, this));
			
		},
		
		removeTestAutomationServer : function(){
				var self = this;
				var ids = this.toDeleteIds;
				if (ids.length === 0) {
					return;
				}
				$.ajax({
					url : VRTS.url + '/' + ids.join(','),
					type : 'delete'
				}).done(self.refresh);

		}
	});
	return NewTestAutomationServersTableView;
});