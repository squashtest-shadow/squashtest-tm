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


	function _initDeleteStep(conf){
		
		var deleteStepDialog = $("#delete-test-step-dialog");
		
		deleteStepDialog.formDialog();
		
		deleteStepDialog.on('formdialogopen', function(){

			var entityId = $("#delete-test-step-dialog").data("entity-id");
			$("#delete-test-step-dialog").data("entity-id", null);
			
			var selIds = [];
						
			if(!entityId){
				selIds = $("#test-steps-table-"+conf.testCaseId).squashTable().getSelectedIds();
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
		
		deleteStepDialog.on('formdialogconfirm', function(){
			var table = $("#test-steps-table-"+ conf.testCaseId).squashTable();
			var ids = this.selIds;
			var calledStepsDeleted	= stepIdsContainCalledSteps(table, ids);
			var url = conf.urls.testCaseStepsUrl +"/"+ ids.join(',');
			
			$.ajax({
				url : url,
				type : 'delete',
				dataType : 'json'
			})
			.done(function(testStepsSize){
				if(calledStepsDeleted){
					eventBus.trigger("testStepsTable.deletedCallSteps");
				}
				eventBus.trigger("testStepsTable.removedSteps");
				conf.stepsTablePanel.refreshTable();
				if(testStepsSize == "0"){
					eventBus.trigger("testStepsTable.noMoreSteps");
				}
			});
			
			$(this).formDialog('close');
		});
		
		deleteStepDialog.on('formdialogcancel', function(){
			$(this).formDialog('close');
		});
		
	}
	function stepIdsContainCalledSteps(table, ids){
		for(var i in ids){
			var calledId = table.getDataById(ids[i])["called-tc-id"];
			if(calledId){
				return true;
			}
		}
		return false;
	}
	
	/*
	 * needs :
	 * 
	 * conf.permissions.writable
	 * conf.urls.testCaseStepsUrl
	 * conf.testCaseId
	 * conf.stepsTablePanel
	 */
	return {
		init : function(conf){
			if (conf.permissions.writable){
				_initDeleteStep(conf);
			}
		}
	};
	
});