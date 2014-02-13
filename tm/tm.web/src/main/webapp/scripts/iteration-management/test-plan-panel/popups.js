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
define(['jquery', 'workspace.event-bus', 'jqueryui', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog' ], function($, eventBus) {


	function _initDeleteExecutionPopup(conf){
		
		var deleteExecutionDialog = $("#iter-test-plan-delete-execution-dialog");
		
		deleteExecutionDialog.confirmDialog();
		
		deleteExecutionDialog.on('confirmdialogconfirm', function(){
			var execId = $(this).data('origin')
								.id
								.substr('delete-execution-table-button-'.length);
			
			$.ajax({
				url : conf.urls.executionsUrl + execId,
				type : 'DELETE',
				dataType : 'json'
			}).done(function(data){
				eventBus.trigger('context.content-modified', { newDates : data });
			});				
		});
	}
	
	
	function _initDeleteItemTestplan(conf){
		
		var deleteItemTestplanDialog = $("#iter-test-plan-delete-dialog");
		
		deleteItemTestplanDialog.formDialog();
		
		deleteItemTestplanDialog.on('formdialogopen', function(){

			var entityId = $("#iter-test-plan-delete-dialog").data("entity-id");
			$("#iter-test-plan-delete-dialog").data("entity-id", null);
			
			var selIds = [];
						
			if(!entityId){
				selIds = $("#iteration-test-plans-table").squashTable().getSelectedIds();
			} 
						
			if(!!entityId){
				selIds.push(entityId);
			}
			
			switch (selIds.length){			
				case 0 : $(this).formDialog('setState','empty-selec'); break;
				case 1 : $(this).formDialog('setState','single-tp'); break;
				default : $(this).formDialog('setState','multiple-tp'); break;					
			}
			
			this.selIds = selIds;
		});
		
		deleteItemTestplanDialog.on('formdialogconfirm', function(){
			var table = $("#iteration-test-plans-table").squashTable();
			var ids = this.selIds;
			var url = conf.urls.testplanUrl + ids.join(',');
			
			$.ajax({
				url : url,
				type : 'delete',
				dataType : 'json'
			})
			.done(function(unauthorized){
				if (unauthorized){
					squashtm.notification.showInfo(conf.messages.unauthorizedTestplanRemoval);
				}
				eventBus.trigger('context.content-modified');
			});
			
			$(this).formDialog('close');
		});
		
		deleteItemTestplanDialog.on('formdialogcancel', function(){
			$(this).formDialog('close');
		});
		
	}
	
	function _initBatchAssignUsers(conf){
		
		var batchAssignUsersDialog = $("#iter-test-plan-batch-assign");
		
		batchAssignUsersDialog.formDialog();
		
		batchAssignUsersDialog.on('formdialogopen', function(){
			var selIds = $("#iteration-test-plans-table").squashTable().getSelectedIds();
			
			if (selIds.length === 0){			
				$(this).formDialog('setState','empty-selec');
			}
			else{
				$(this).formDialog('setState','assign');				
			}
			
		});
		
		batchAssignUsersDialog.on('formdialogconfirm', function(){
			
			var table = $("#iteration-test-plans-table").squashTable(),
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
		var dialog = $("#iter-test-plan-reorder-dialog");
		
		dialog.confirmDialog();
		
		dialog.on('confirmdialogconfirm', function(){
			var table = $("#iteration-test-plans-table").squashTable();
			var drawParameters = table.getAjaxParameters();
			
			var url = conf.urls.testplanUrl+'/order';
			$.post(url, drawParameters, 'json')
			.success(function(){
				table.data('sortmode').resetTableOrder(table);
				eventBus.trigger('context.content-modified');				
			});
		});
		
		dialog.on('confirmdialogcancel', function(){
			$(this).confirmDialog('close');
		});
	}
	
	
	return {
		init : function(conf){
			if (conf.permissions.linkable){
				_initDeleteItemTestplan(conf);
			}
			if (conf.permissions.editable){				
				_initBatchAssignUsers(conf);
			}
			if(conf.permissions.executable){
				_initDeleteExecutionPopup(conf);
			}
			if (conf.permissions.reorderable){
				_initReorderTestPlan(conf);
			}
		}
	};
	
});