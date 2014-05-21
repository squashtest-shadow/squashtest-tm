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
		'jqueryui', 'jquery.squash.formdialog' ], function($, Backbone, NewTestAutomationServerDialogView, NewTestAutomationServerModel, ButtonUtil) {
	var tasTable = squashtm.app.tasTable;
	/*
	 * Defines the controller for the test automation server table.
	 */
	var NewTestAutomationServersTableView = Backbone.View.extend({
		el : "#test-automation-server-table-pane",
		initialize : function() {
			var self = this;
			this.removeTestAutomationServer = $.proxy(this._removeTestAutomationServer, this);
			this.setConfirmRemoveDialogState = $.proxy(this._setConfirmRemoveDialogState, this);
			
			// DOM initialized table
			this.table = this.$("table");
			this.table.squashTable(squashtm.datatable.defaults, {});
			this.configureRemoveTASDialog();
			
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
			this.confirmRemoveTASDialog = $("#remove-test-automation-server-confirm-dialog").formDialog();
			this.confirmRemoveTASDialog.formDialog('setState','processing');
			this.confirmRemoveTASDialog.on("formdialogopen", this.setConfirmRemoveDialogState);
			this.confirmRemoveTASDialog.on("formdialogconfirm", $.proxy(this.removeTestAutomationServer, this));
			this.confirmRemoveTASDialog.on("formdialogcancel", $.proxy(function() {
				this.confirmRemoveTASDialog.formDialog('close');
			}, this));
			this.confirmRemoveTASDialog.on("formdialogclose", $.proxy(function() {
				this.toDeleteIds = [];
				this.table.deselectRows();
				this.confirmRemoveTASDialog.formDialog('setState','processing');
			}, this));
			
		},
		_setConfirmRemoveDialogState : function(event){
			var self = this;
			var table = self.table;
			var ids = table.getSelectedIds();
			if (ids.length !=1 ) {
				return ;
			}else {
				$.ajax({
					url : squashtm.app.contextRoot +"test-automation-servers/"+ids[0]+"/usage-status",
					type: "GET"
				}).then(function(status){
					if(!status.hasBoundProject && !status.hasExecutedTests){
						self.confirmRemoveTASDialog.formDialog('setState','case1');
					}else if (!status.hasExecutedTests){
						self.confirmRemoveTASDialog.formDialog('setState','case2');
					}else{
						self.confirmRemoveTASDialog.formDialog('setState','case3');
					}
				});
			}
			
		},
		_removeTestAutomationServer : function(event) {
			var table = this.table;
			var ids = table.getSelectedIds();
			if (ids.length === 0) {
				return;
			}
			$.ajax({
				url : squashtm.app.contextRoot + "test-automation-servers/" + ids.join(','),
				type : 'delete'
			}).then($.proxy(table.refresh, table)).fail(function(wtf){
				try {
					squashtm.notification.handleJsonResponseError(wtf);
				} catch (wtf) {
					squashtm.notification.handleGenericResponseError(wtf);
				}
			});
		},
	});
	return NewTestAutomationServersTableView;
});