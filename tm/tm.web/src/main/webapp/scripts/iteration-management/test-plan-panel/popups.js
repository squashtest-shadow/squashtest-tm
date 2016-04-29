/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define([ 'jquery', 'workspace.event-bus', 'app/util/ComponentUtil', 'squash.statusfactory', 'squash.translator',
		'squash.dateutils', 'app/ws/squashtm.notification', 'test-plan-management/DeleteExecutionDialog', 'jqueryui', 'jquery.squash.confirmdialog',
		'jquery.squash.formdialog' ], function($,
		eventBus, ComponentUtil, statusfactory, translator, dateutils, notification, DeleteExecutionDialog) {

	function _initDeleteExecutionPopup(conf) {
		new DeleteExecutionDialog({el: "#iter-test-plan-delete-execution-dialog", urlRoot: conf.urls.executionsUrl})

		// var deleteExecutionDialog = $("#iter-test-plan-delete-execution-dialog");
        //
		// deleteExecutionDialog.confirmDialog();
        //
		// deleteExecutionDialog.on('confirmdialogconfirm', function() {
		// 	var execId = $(this).data('origin').id.substr('delete-execution-table-button-'.length);
        //
		// 	$.ajax({
		// 		url : conf.urls.executionsUrl + execId,
		// 		type : 'DELETE',
		// 		dataType : 'json'
		// 	}).done(function(data) {
		// 		eventBus.trigger('context.content-modified', {
		// 			newDates : data
		// 		});
		// 	});
		// });
	}

	function _initDeleteItemTestplan(conf) {

		var deleteItemTestplanDialog = $("#iter-test-plan-delete-dialog");

		deleteItemTestplanDialog.formDialog();

		deleteItemTestplanDialog.on('formdialogopen', function() {

			var $this = $(this),
				$table = $("#iteration-test-plans-table").squashTable();

			var entityId = $this.data("entity-id");
			$this.data("entity-id", null);

			var selIds = [];

			if (!entityId) {
				selIds = $table.getSelectedIds();
			}

			if (!!entityId) {
				selIds.push(entityId);
			}

			switch (selIds.length) {
			case 0:
				$this.formDialog('close');
				notification.showError(translator.get('message.EmptyExecPlanSelection'));
				break;
			case 1:
				var row = $table.getRowsByIds(selIds)[0];
				var wasexecuted = (!! $table.fnGetData(row)['last-exec-on']);
				if (wasexecuted){
					$this.formDialog('setState', 'delete-single-tp');
				}
				else{
					$this.formDialog('setState', 'unbind-single-tp');
				}
				break;
			default:
				$this.formDialog('setState', 'multiple-tp');
				break;
			}

			this.selIds = selIds;
		});

		deleteItemTestplanDialog.on('formdialogconfirm', function() {
			var table = $("#iteration-test-plans-table").squashTable();
			var ids = this.selIds;
			var url = conf.urls.testplanUrl + ids.join(',');

			$.ajax({
				url : url,
				type : 'delete',
				dataType : 'json'
			}).done(function(partiallyUnauthorized) {
				/*
				 * When a user can delete a planned test case unless executed,
				 * and that a multiple selection encompassed both cases,
				 * the server performs the operation only on the item it is allowed to.
				 *
				 *  When this happens, the used must be notified.
				 */
				if (partiallyUnauthorized) {
					squashtm.notification.showWarning(conf.messages.unauthorizedTestplanRemoval);
				}
				eventBus.trigger('context.content-modified');
			});

			$(this).formDialog('close');
		});

		deleteItemTestplanDialog.on('formdialogcancel', function() {
			$(this).formDialog('close');
		});

	}

	function _initBatchAssignUsers(conf) {

		var batchAssignUsersDialog = $("#iter-test-plan-batch-assign");

		batchAssignUsersDialog.formDialog();

		batchAssignUsersDialog.on('formdialogopen', function() {
			var selIds = $("#iteration-test-plans-table").squashTable().getSelectedIds();

			if (selIds.length === 0) {
				$(this).formDialog('close');
				notification.showError(translator.get('message.EmptyExecPlanSelection'));
			} else {
				$(this).formDialog('setState', 'assign');
			}

		});

		batchAssignUsersDialog.on('formdialogconfirm', function() {

			var table = $("#iteration-test-plans-table").squashTable(), select = $('.batch-select', this);

			var rowIds = table.getSelectedIds(), assigneeId = select.val(), assigneeLogin = select.find(
					'option:selected').text();

			var url = conf.urls.testplanUrl + rowIds.join(',');

			$.post(url, {
				assignee : assigneeId
			}, function() {
				table.getSelectedRows().find('td.assignee-combo span').text(assigneeLogin);
			});

			$(this).formDialog('close');
		});

		batchAssignUsersDialog.on('formdialogcancel', function() {
			$(this).formDialog('close');
		});

	}
	function _initBatchEditStatus(conf) {

		var batchEditStatusDialog = $("#iter-test-plan-batch-edit-status");

		batchEditStatusDialog.formDialog();

		var cbox = batchEditStatusDialog.find(".execution-status-combo-class");
		ComponentUtil.updateStatusCboxIconOnChange(cbox);

		batchEditStatusDialog.on('formdialogopen', function() {
			var selIds = $("#iteration-test-plans-table").squashTable().getSelectedIds();
			var cbox = $(this).find(".execution-status-combo-class");
			ComponentUtil.updateStatusCboxIcon(cbox);
			if (selIds.length === 0) {
				$(this).formDialog('close');
				notification.showError(translator.get('message.EmptyExecPlanSelection'));
			} else {
				$(this).formDialog('setState', 'edit');
			}

		});

		batchEditStatusDialog.on('formdialogconfirm', function() {

			var table = $("#iteration-test-plans-table").squashTable(), select = $('.execution-status-combo-class',
					this);

			var rowIds = table.getSelectedIds(), statusCode = select.val(), statusName = select.find('option:selected')
					.text();

			var url = conf.urls.testplanUrl + rowIds.join(',');

			$.post(url, {
				status : statusCode
			}, function(itp) {

				// must update the execution status, the execution date and the assignee

				// 1/ the status
				var $statusspans = table.getSelectedRows().find('td.status-combo span');
				for ( var i = 0; i < $statusspans.length; i++) {
					var $statusspan = $($statusspans[i]);
					$statusspan.attr('class', 'cursor-arrow exec-status-label exec-status-' +
							itp.executionStatus.toLowerCase());
					$statusspan.html(statusfactory.translate(itp.executionStatus));

					// 2/ the date format
					var format = translator.get('squashtm.dateformat'), $execon = $statusspan.parents('tr:first').find(
							"td.exec-on");

					var newdate = dateutils.format(itp.lastExecutedOn, format);
					$execon.text(newdate);

					// 3/ user assigned
					$statusspan.parents('tr:first').find('td.assignee-combo').children().first().text(itp.assignee);
				}
			});

			$(this).formDialog('close');
		});

		batchEditStatusDialog.on('formdialogcancel', function() {
			$(this).formDialog('close');
		});

	}
	function _initReorderTestPlan(conf) {
		var dialog = $("#iter-test-plan-reorder-dialog");

		dialog.confirmDialog();

		dialog.on('confirmdialogconfirm', function() {
			var table = $("#iteration-test-plans-table").squashTable();
			var drawParameters = table.getAjaxParameters();

			var url = conf.urls.testplanUrl + '/order';
			$.post(url, drawParameters, 'json').success(function() {
				table.data('sortmode').resetTableOrder(table);
				eventBus.trigger('context.content-modified');
			});
		});

		dialog.on('confirmdialogcancel', function() {
			$(this).confirmDialog('close');
		});
	}

	return {
		init : function(conf) {
			if (conf.permissions.linkable) {
				_initDeleteItemTestplan(conf);
			}
			if (conf.permissions.editable) {
				_initBatchAssignUsers(conf);
				_initBatchEditStatus(conf);
			}
			if (conf.permissions.executable) {
				_initDeleteExecutionPopup(conf);
			}
			if (conf.permissions.reorderable) {
				_initReorderTestPlan(conf);
			}
		}
	};

});
