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

/*
 * configuration an object as follow :
 * 
 * {
 *		permissions : {
 *			editable : boolean, is the table content editable ?
 *			executable : boolean, can the content be executed ?	
 * 		},
 * 		basic : {
 * 			iterationId : the id of the current iteration
 *			assignableUsers : [ { 'id' : id, 'login' : login } ]
 * 		},
 * 		messages : {
 * 			executionStatus : {
 * 				UNTESTABLE : i18n label,
 * 				BLOCKED : i18n label,
 *				FAILURE : i18n label,
 *				SUCCESS : i18n label,
 *				RUNNING : i18n label,
 *				READY : i18n label,
 * 			},
 *			automatedExecutionTooltip : i18n label,
 *			labelOk : i18n label,
 *			labelCancel : i18n label,
 *			titleInfo : i18n label,
 *			messageNoAutoexecFound : i18n label
 * 		},
 *		urls : {
 *			 testplanUrl : base urls for test plan items,
 *			 executionsUrl : base urls for executions
 *		}
 * }
 * 
 */

define(['jquery', 'squash.translator', './exec-runner', 
        'jquery.squash.datatables', 'jeditable', 'jquery.squash.buttonmenu'],
        function($, translator, execrunner) {

		
	
	
	// ****************** TABLE CONFIGURATION **************
	
	
	function _rowCallbackReadFeatures($row, data, _conf){
		
		// style for deleted test case rows
		if ( data['is-tc-deleted'] === "true" ){
			$row.addClass('test-case-deleted');
		}
		
		// execution mode icon
		var $exectd = $row.find('.exec-mode').text('');
		if ( data['exec-mode'] === "M"){
			$exectd.addClass('exec-mode-manual').attr('title', '');
		}
		else{
			$exectd.addClass('exec-mode-automated').attr('title', _conf.autoexecutionTooltip);
		}
		
		// execution status (read)
		var status = data['status'],
			i18nstatus = _conf.statuses[status],
			$statustd = $row.find('.status-combo'),
			html = _conf.statusFactory.getHtmlFor(i18nstatus, status);
			
		$statustd.html(html);	// remember : this will insert a <span> in the process
		
		
		// assignee (read) 
		var $assigneetd = $row.find('.assignee-combo');
		$assigneetd.wrapInner('<span/>');
	}
	

	function _rowCallbackWriteFeatures($row, data, _conf){
		
		// execution status (edit)
		var statusurl = _conf.testplanUrl + data['entity-id']; 
		$row.find('.status-combo').children().first().editable( statusurl, {
			type : 'select',
			data : _conf.jsonStatuses,
			name : 'status',
			onblur : 'cancel',
			callback : _conf.submitStatusClbk
		});
		
		// assignee (edit)
		var assigneeurl = _conf.testplanUrl + data['entity-id'];		
		$row.find('.assignee-combo').children().first().editable(assigneeurl,{
			type : 'select',
			data : _conf.jsonAssignableUsers,
			name : 'assignee', 
			onblur : 'cancel',
			callback : _conf.submitAssigneeClbk
		});
					
	}
	
	function _rowCallbackExecFeatures($row, data, _conf){

		//add the execute shortcut menu
		var isTcDel = data['is-tc-deleted'],
			isManual = (data['exec-mode'] === "M"); 
		
		var tpId = data['entity-id']
			$td = $row.find('.execute-button'),
			strmenu = $("#shortcut-exec-menu-template").html().replace(/{placeholder-tpid}/g, tpId),
		
		$td.empty();
		$td.append(strmenu);
		
		// if the test case is deleted : just disable the whole thing
		if (isTcDel){
			$td.find('.execute-arrow').addClass('disabled-transparent');
		} 
		
		//if the test case is manual : configure a button menu, althgouh we don't want it 
		//to be skinned as a regular jquery button
		else if (isManual){			
			$td.find('.buttonmenu').buttonmenu({preskinned : true, anchor : "right"});			
			$td.on('click', '.run-menu-item', _conf.manualHandler);
		} 
		
		//if the test case is automated : just configure the button
		else {
			$td.find('.execute-arrow').click(_conf.automatedHandler);
		}
		
	
	}	
	
	function createTableConfiguration(initconf){
		
		// conf objects for the row callbacks
		var _readFeaturesConf = {
			statuses : initconf.messages.executionStatus,
			autoexecutionTooltip : initconf.messages.automatedExecutionTooltip,
			statusFactory : new squashtm.StatusFactory(initconf.messages.executionStatus)	
		};
		
		var _writeFeaturesConf = {
		
			testplanUrl : initconf.urls.testplanUrl,
			
			jsonStatuses : JSON.stringify(initconf.messages.executionStatus),			
			submitStatusClbk : function(value, settings) {
				var $span = $(this),
					statuses = JSON.parse(settings.data);
				$span.attr('class', 'common-status-label executions-status-'+value+'-icon');
				$span.text( statuses[value] );
			}, 
			
			jsonAssignableUsers : JSON.stringify(initconf.basic.assignableUsers),
			submitAssigneeClbk : function(value, settings) {
				var assignableUsers = JSON.parse(settings.data);
				$(this).text( assignableUsers[value] );
			}
		};
		
		var _execFeaturesConf = {
			
			manualHandler : function(){
				
				var $this = $(this),
					tpid = $this.data('tpid'),
					ui = ($this.is('.run-popup')) ? "popup" : "oer",
					newurl = initconf.urls.testplanUrl + tpid + '/executions/new';
					
				$.post(newurl, {mode : 'manual'}, 'json')
				.done(function(execId){
					var execurl = initconf.urls.executionsUrl + execId +'/runner';
					if (ui === "popup"){
						execrunner.runInPopup(execurl);
					}
					else{
						execrunner.runInOER(execurl);
					}
					
				});				
			},
			
			automatedHandler : function(){
				var row = $(this).parents('tr').get(0),
					table = $("#iteration-test-plans-table").squashTable(),
					data = table.fnGetData(row),
					tpid = data['entity-id'],
					newurl = initconf.urls.testplanUrl + tpid + '/executions/new';
				
				$.post(newurl, {mode : 'auto'}, 'json')
				.done(function(suiteview){
					if (suiteview.executions.length == 0){
						$.squash.openMessage(initcon.messages.titleInfo, 
											initconf.messages.messageNoAutoexecFound);
					}
					else{
						squashtm.automatedSuiteOverviewDialog.open(suiteview);
					}
				});
				
			}
		};

		
		// basic table configuration. Much of it is in the DOM of the table.		
		var tableSettings = {
		
			fnRowCallback : function(row, data, displayIndex){
				
				var $row = $(row);
				
				//add read-only mode features (always applied)
				_rowCallbackReadFeatures($row, data, _readFeaturesConf);
				
				//add edit-mode features
				if (initconf.permissions.editable){
					_rowCallbackWriteFeatures($row, data, _writeFeaturesConf);
				}
				
				//add execute-mode features
				if (initconf.permissions.executable){
					_rowCallbackExecFeatures($row, data, _execFeaturesConf);
				}
				
				// done
				return row;
			},
				
			fnDrawCallback : function(){
				// make all <select> elements autosubmit on selection change.
				this.on('change', 'select', function(){
					$(this).submit();
				});
			}	
		};
		
		var squashSettings = {
			toggleRows : {				
				'td.toggle-row' : function(table, jqold, jqnew){
					
					var data = table.fnGetData(jqold.get(0)),
						url = initconf.urls.testplanUrl + data['entity-id'] + '/executions';
						
					jqnew.load(url, function(){	

						// styling 
						var newexecBtn = jqnew.find('.new-exec').squashButton(),
							newautoexecBtn = jqnew.find('.new-auto-exec').squashButton();
						
						// the delete buttons
						if (initconf.permissions.editable){
							jqnew.find('.delete-execution-table-button').button({
								text : false,
								icons : {
									primary : "ui-icon-minus"
								}
							})
							.on('click', function(){
								var dialog = $("#iter-test-plan-delete-execution-dialog");
								dialog.data('origin', this);
								dialog.confirmDialog('open');
							});
						};
						
						//the new execution buttons
						if (initconf.permissions.executable){
							
							newexecBtn.click(function(){
								var url = $(this).data('new-exec');
								$.post(url, {mode : 'manual'}, 'json')
								.done(function(id){
									document.location.href=initconf.urls.executionsUrl + id;
								});
								return false;
							});
							
							newautoexecBtn.click(function(){
								var url = $(this).data('new-exec');
								$.post(url, {mode : 'auto'}, 'json')
								.done(function(suiteview){
									if (suiteview.executions.length == 0){
										$.squash.openMessage(initcon.messages.titleInfo, 
															initconf.messages.messageNoAutoexecFound);
									}
									else{
										squashtm.automatedSuiteOverviewDialog.open(suiteview);
									}
								});
								return false;
							});
						}
					});
				}
			}
		};
		
		//more conf if editable
		
		if (initconf.permissions.editable){
			
			squashSettings.enableDnD = true;
			squashSettings.functions = {};
			squashSettings.functions.dropHandler = function(dropData){			
				var ids = dropData.itemIds.join(',');
				var url	= initconf.urls.testplanUrl + '/' + ids + '/position/' + dropData.newIndex;			
				$.post(url, function(){
					$("#iteration-test-plans-table").squashTable().refresh();
				});
			}
			
		};
	
		return {
			tconf : tableSettings,
			sconf : squashSettings
		}
	
	}
	

	// **************** MAIN ****************
	
	return {
		init : function(enhconf){			
			var tableconf = createTableConfiguration(enhconf);			
			$("#iteration-test-plans-table").squashTable(tableconf.tconf, tableconf.sconf);
		}
	};
	
});