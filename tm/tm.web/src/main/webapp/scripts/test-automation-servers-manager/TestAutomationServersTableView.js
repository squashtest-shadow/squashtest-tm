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
			// this.el is decorated with an ajax sourced
			// datatable
			var config = $.extend({
			// "oLanguage" : {
			// "sUrl" : tasTable.languageUrl
			// },
			// "bJQueryUI" : true,
			// "bAutoWidth" : false,
			// "bFilter" : false,
			// "bPaginate" : true,
			// "sPaginationType" : "squash",
			// "iDisplayLength" : tasTable.displayLength,
			// "bServerSide" : true,
			// "sAjaxSource" : tasTable.ajaxSource,
			// "bDeferRender" : true,
			// "bRetrieve" : true,
			// "sDom" : 't<"dataTables_footer"lp>',
			// "iDeferLoading" : 0,
			// "aaSorting" : [ [ 2, "asc" ] ],
			// "fnRowCallback" : function() {
			// },
			// "aoColumnDefs" : [
			// {
			// "bVisible" : false,
			// "aTargets" : [ 0 ],
			// "sClass" : "cf-id",
			// "mDataProp" : "entity-id"
			// },
			// {
			// 'bSortable' : false,
			// 'sClass' : 'centered ui-state-default drag-handle select-handle',
			// 'aTargets' : [ 1 ],
			// 'mDataProp' : 'entity-index'
			// },
			// {
			// "bSortable" : true,
			// "aTargets" : [ 2 ],
			// "mDataProp" : "name"
			// },
			// {
			// "bSortable" : true,
			// "aTargets" : [ 3 ],
			// "mDataProp" : "label"
			// },
			// {
			// "bVisible" : false,
			// "aTargets" : [ 4 ],
			// "sClass" : "raw-input-type",
			// "mDataProp" : "raw-input-type"
			// },
			// {
			// "bSortable" : true,
			// "aTargets" : [ 5 ],
			// "mDataProp" : "input-type"
			// },
			// {
			// 'bSortable' : false,
			// 'sWidth' : '2em',
			// 'sClass' : 'delete-button',
			// 'aTargets' : [ 6 ],
			// 'mDataProp' : 'empty-delete-holder'
			// } ]
			}, squashtm.datatable.defaults);

			var squashSettings = {
			// enableHover : true,
			//
			// confirmPopup : {
			// oklabel : tasTable.confirmLabel,
			// cancellabel : tasTable.cancelLabel
			// },
			//
			// deleteButtons : {
			// url : tasTable.ajaxSource + "/{entity-id}",
			// popupmessage : "<div class='display-table-row'><div class='display-table-cell warning-cell'><div
			// class='delete-node-dialog-warning'></div></div><div
			// class='display-table-cell'>"+tasTable.deleteConfirmMessageFirst+"<span class='red-warning-message'>
			// "+tasTable.deleteConfirmMessageSecond+"</span>"+tasTable.deleteConfirmMessageThird+"<span
			// class='bold-warning-message'> "+tasTable.deleteConfirmMessageFourth+"</span></div></div>",
			// tooltip : tasTable.deleteTooltip,
			// success : function(data) {
			// self.table.refresh();
			// }
			// },
			//
			// bindLinks : {
			// list : [ {
			// url : tasTable.customFieldUrl + "/{entity-id}",
			// target : 2,
			// isOpenInTab : false
			// } ]
			// }
			};
			// TODO make dom initialized table
			this.table = this.$("table");
			this.table.squashTable(config, squashSettings);
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
		}
	});
	return NewTestAutomationServersTableView;
});