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
define(['jquery', 'workspace.event-bus', 'squash.translator', 'jqueryui', 'jquery.squash.confirmdialog', 'jquery.squash.formdialog' ], function($, eventBus, translator) {


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
	
	
	
	
	// ************************* call step dataset dialog 
	// *************************
	
	function _initCallStepDatasetDialog(conf){
		
		var dialog = $("#pick-call-step-dataset-dialog"),
			table = $("#test-steps-table-"+conf.testCaseId).squashTable();
		
		
		dialog.formDialog();
		
		dialog.on('formdialogopen', function(){
			var openerId = dialog.data('opener-id'),
				tblrow = table.getRowsByIds([openerId]);
				rowdata = table.fnGetData(tblrow.get(0)),
				stepInfo = rowdata['call-step-info'],
				thisTcName = $("#test-case-name").text(); // oooh that's ugly
			
			// display the content of pick-call-step-dataset-consequence
			var spanConsequence = $("#pick-call-step-dataset-consequence"),
				template = spanConsequence.data('template');
			
			var txtConsequence = template.replace('{0}', stepInfo.calledTcName)
										.replace('{1}', thisTcName);
			
			spanConsequence.text(txtConsequence);
			
			// now populate the combo
			var fetchDatasetsUrl = squashtm.app.contextRoot + '/test-cases/'+stepInfo.calledTcId+'/datasets';
			
			$.getJSON(fetchDatasetsUrl)
			.success(function(json){
				if (json.length == 0){
					$("#pick-call-step-dataset-nonavailable").show();
					$("#pick-call-step-dataset-select").hide();
				}
				else{
					var select = $("#pick-call-step-dataset-select");
					select.show();
					$("#pick-call-step-dataset-nonavailable").hide();
					
					var noneOption = $('<option value="0">'+translator.get('label.None')+'</option>')
					select.append(noneOption);
					
					$.each(json, function(idx, ds){
						var opt = $('<option value="'+ds.id+'">'+ds.name+'</option>');
						select.append(opt);
					});

					// TODO : now preselect the selected option
				}
			});
			
		});
		
		
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
				_initCallStepDatasetDialog(conf);
			}
		}
	};
	
});