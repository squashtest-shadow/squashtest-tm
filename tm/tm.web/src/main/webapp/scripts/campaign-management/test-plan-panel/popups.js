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
define(['jquery', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog' ], function($) {

	function _initBatchAssignUsers(conf){
		
		var batchAssignUsersDialog = $("#camp-test-plan-batch-assign");
		
		batchAssignUsersDialog.formDialog();
		
		batchAssignUsersDialog.on('formdialogopen', function(){
			var selIds = $("#test-cases-table").squashTable().getSelectedIds();
			
			if (selIds.length === 0){			
				$(this).formDialog('setState','empty-selec');
			}
			else{
				$(this).formDialog('setState','assign');				
			}
			
		});
		
		batchAssignUsersDialog.on('formdialogconfirm', function(){
			
			var table = $("#test-cases-table").squashTable(),
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
			var table = $("#test-cases-table").squashTable();
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
	
	
	return {
		init : function(conf){
			if (conf.permissions.editable){				
				_initBatchAssignUsers(conf);
			}
			if (conf.permissions.reorderable){
				_initReorderTestPlan(conf);
			}
		}
	};
	
});