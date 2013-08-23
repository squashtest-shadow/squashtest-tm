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
define(['jquery', 'workspace.contextual-content', 'jqueryui', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog' ], function($, ctxt) {


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
				ctxt.trigger('context.content-updated', { newDates : data });
			});				
		});
	}
	
	
	function _initDeleteItemTestplan(conf){
		
		var deleteItemTestplanDialog = $("#iter-test-plan-delete-dialog");
		
		deleteItemTestplanDialog.formDialog();
		
		deleteItemTestplanDialog.on('formdialogopen', function(){
			var selIds = $("#iteration-test-plans-table").squashTable().getSelectedIds();
			
			switch (selIds.length){			
				case 0 : 	$(this).formDialog('setState','empty-selec'); break;
				case 1 : 	$(this).formDialog('setState','single-tp'); break;
				default : 	$(this).formDialog('setState','multiple-tp'); break;					
			}
			
		});
		
		deleteItemTestplanDialog.on('formdialogconfirm', function(){
			var table = $("#iteration-test-plans-table").squashTable();
			var ids = table.getSelectedIds();
			var url = conf.urls.testplanUrl + ids.join(',');
			
			$.ajax({
				url : url,
				type : 'delete',
				dataType : 'json'
			})
			.done(function(unauthorized){
				if (unauthorized){
					squashtm.notification.showInfo(conf.unauthorizedTestplanRemoval);
				}
				ctxt.trigger('context.content-modified');
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
			
			switch (selIds.length){			
				case 0 : 	$(this).formDialog('setState','empty-selec'); break;
				default : 	$(this).formDialog('setState','assign'); break;					
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
	
	
	return {
		init : function(conf){
			if (conf.permissions.linkable){
				_initDeleteItemTestplan(conf);
			}
			if (conf.permissions.editable){
				_initDeleteExecutionPopup(conf);
				_initBatchAssignUsers(conf);
			}
			
		}
	}
	
});