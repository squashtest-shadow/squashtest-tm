/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define([ 'jquery', 'backbone', "underscore", './NewTestAutomationServerDialogView', './NewTestAutomationServerModel', 'app/util/ButtonUtil', 'squashtable',
		'jqueryui', 'jquery.squash.formdialog' ], function($, Backbone, _, NewTestAutomationServerDialogView, NewTestAutomationServerModel, ButtonUtil) {
	"use strict";

	var tasTable = squashtm.app.tasTable;
	/*
	 * Defines the controller for the test automation server table.
	 */
	var NewTestAutomationServersTableView = Backbone.View.extend({
		el : "#test-automation-server-table-pane",
		initialize : function() {
			var self = this;

			_.bindAll(this, "removeTestAutomationServer", "setConfirmRemoveDialogState");

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

			function refresh() {
				self.table.squashTable().fnDraw();
			}

			function discard() {
				self.stopListening(self.newTasDialog);
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

			self.listenTo(self.newTasDialog, "newtestautomationserver.cancel", discard);
			self.listenTo(self.newTasDialog, "newtestautomationserver.confirm", discardAndRefresh);
			self.listenTo(self.newTasDialog, "newtestautomationserver.confirm-carry-on", refresh);
		},

		configureRemoveTASDialog : function() {
			var self= this;

			var dialog = $("#remove-test-automation-server-confirm-dialog").formDialog();
			dialog.formDialog('setState','processing');
			dialog.on("formdialogopen", this.setConfirmRemoveDialogState);
			dialog.on("formdialogconfirm", function(evt){
				self.removeTestAutomationServer(evt);
				dialog.formDialog('close');
			});
			dialog.on("formdialogcancel", function() {
				dialog.formDialog('close');
			});
			dialog.on("formdialogclose", $.proxy(function() {
				this.toDeleteIds = [];
				this.table.deselectRows();
				dialog.formDialog('setState','processing');
			}, this));

			this.confirmRemoveTASDialog = dialog;

		},
		setConfirmRemoveDialogState : function(event){
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
		removeTestAutomationServer : function(event) {
			var self = this,
				table = this.table;
			var ids = table.getSelectedIds();
			if (ids.length === 0) {
				return;
			}
			$.ajax({
				url : squashtm.app.contextRoot + "test-automation-servers/" + ids.join(','),
				type : 'delete'
			})
			.then(function(){
				table.refresh();
				self.confirmRemoveTASDialog.formDialog('close');
			})
			.fail(function(wtf){
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