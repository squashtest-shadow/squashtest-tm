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
define(['jquery', 'squash.translator', 'app/ws/squashtm.notification', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog' ], 
		function($, translator, notification) {

	function _initBatchAssignUsers(conf){
		
		var batchAssignUsersDialog = $("#camp-test-plan-batch-assign");
		
		batchAssignUsersDialog.formDialog();
		
		batchAssignUsersDialog.on('formdialogopen', function(){
			var selIds = $("#campaign-test-plans-table").squashTable().getSelectedIds();
			
			if (selIds.length === 0){			
				$(this).formDialog('close');
				notification.showError(translator.get('campaign.test-plan.action.empty-selection.message'));
			}
			else{
				$(this).formDialog('setState','assign');				
			}
			
		});
		
		batchAssignUsersDialog.on('formdialogconfirm', function(){
			
			var table = $("#campaign-test-plans-table").squashTable(),
				select = $('.batch-select', this);
			
			var rowIds = table.getSelectedIds(),
				assigneeId = select.val(),
				assigneeLogin = select.find('option:selected').text();
			
			var url = conf.urls.testplanUrl + rowIds.join(',');
			
			$.post(url, {assignee : assigneeId}, function(){
				table.getSelectedRows().find('td.assignee-combo span').text(assigneeLogin);
			});
			
			$(this).formDialog('close');
		});
		
		batchAssignUsersDialog.on('formdialogcancel', function(){
			$(this).formDialog('close');
		});
		
	}
	
	function _initReorderTestPlan(conf){
		var dialog = $("#camp-test-plan-reorder-dialog");
		
		dialog.confirmDialog();
		
		dialog.on('confirmdialogconfirm', function(){
			var table = $("#campaign-test-plans-table").squashTable();
			var drawParameters = table.getAjaxParameters();
			
			var url = conf.urls.testplanUrl+'/order';
			$.post(url, drawParameters, 'json')
			.success(function(){
				table.data('sortmode').resetTableOrder(table);
				table.refresh();			
			});
		});
		
		dialog.on('confirmdialogcancel', function(){
			$(this).confirmDialog('close');
		});
	}
	
	
	function _initBatchRemove(conf){
		
		var dialog = $("#delete-multiple-test-cases-dialog");
		
		dialog.formDialog();
		
		dialog.on('formdialogopen', function(){
			
			// read the ids from the table selection
			var ids = $("#campaign-test-plans-table").squashTable().getSelectedIds();
			
			if (ids.length === 0){	
				// if empty, try to see if the delete buttons embedded in the table rows 
				// left something for us
				var _id = dialog.data('entity-id');
				dialog.data('entity-id', null);
				if (!! _id){
					ids = [ _id ];
				}
			}
			
			if (ids.length === 0){
				$(this).formDialog('close');
				notification.showError(translator.get('iteration.test-plan.action.empty-selection.message'));
			}
			else{
				this.selIds = ids;
				dialog.formDialog("setState", "confirm-deletion");
			}
			
		});
		
		
		dialog.on('formdialogconfirm', function(){
			var ids = this.selIds;
			
			var url = conf.urls.testplanUrl + ids.join(',');
			
			if (ids.length > 0){
				$.ajax({
					url : url, 
					type : 'DELETE',
					dataType : 'json'
				})
				.done(function(){
					$("#campaign-test-plans-table").squashTable().refresh();
					dialog.formDialog('close');
				});
			}
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});

		
	}
	
	return {
		init : function(conf){
			if (conf.features.editable){				
				_initBatchAssignUsers(conf);
			}
			if (conf.features.reorderable){
				_initReorderTestPlan(conf);
			}
			if (conf.features.linkable){
				_initBatchRemove(conf);
			}
		}
	};
	
});