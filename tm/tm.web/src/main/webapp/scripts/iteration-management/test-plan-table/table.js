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
 * 		}
 * }
 * 
 */

define(['jquery', 'squash.translator', 'workspace.contextual-content', 'jquery.squash.datatables', 'jquery.squash.confirmdialog', 'jeditable'],function($, translator, ctxt) {


	
	function enhanceConfiguration(origconf){
		
		var conf = $.extend({}, origconf);
		
		var baseURL = squashtm.app.contextRoot;
		
		conf.messages = translator.get({
			automatedExecutionTooltip : "label.automatedExecution",
			executionStatus : {
				UNTESTABLE : "execution.execution-status.UNTESTABLE",
				BLOCKED : "execution.execution-status.BLOCKED",
				FAILURE : "execution.execution-status.FAILURE",
				SUCCESS : "execution.execution-status.SUCCESS",
				RUNNING : "execution.execution-status.RUNNING",
				READY  : "execution.execution-status.READY",
			},
			labelOk : "label.Ok",
			labelCancel : "label.Cancel"
		});
		
		conf.urls = {
			 testplanUrl : baseURL + '/iterations/'+conf.basic.iterationId+'/test-plan/',
			 executionsUrl : baseURL + '/executions/'
		};
		
		return conf;
	}
	
	
	
	// ****************** TABLE CONFIGURATION **************
	
	function createTableConfiguration(initconf){
		
		// data for the comboboxes
		
		var statuses = initconf.messages.executionStatus,
			jsonStatuses = JSON.stringify(statuses),
			assignableUsers = initconf.basic.assignableUsers,
			jsonAssignableUsers = JSON.stringify(assignableUsers),
			statusFactory = new squashtm.StatusFactory(statuses);	
		
		
		var submitStatusClbk = function(value) {
			var $span = $(this);
			$span.attr('class', 'common-status-label executions-status-'+value+'-icon');
			$span.text( statuses[value] );
		} 
		
		var submitAssigneeClbk = function(value) {
			$(this).text( assignableUsers[value] );
		} 
		
		// basic configuration. Much of it is in the DOM of the table.
		
		var tableSettings = {
		
			fnRowCallback : function(row, data, displayIndex){
				
				var $row = $(row);
				
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
					$exectd.addClass('exec-mode-automated').attr('title', initconf.messages.automatedExecutionTooltip);
				}
				
				// execution status (read)
				var status = data['status'],
					i18nstatus = statuses[status],
					$statustd = $row.find('.status-combo'),
					html = statusFactory.getHtmlFor(i18nstatus, status);
					
				$statustd.html(html);	// remember : this will insert a <span> in the process
				
				// execution status (edit)
				if (initconf.permissions.editable){
					var url = initconf.urls.testplanUrl + data['entity-id']; 
					$statustd.children().first().editable( url, {
						type : 'select',
						data : jsonStatuses,
						name : 'status',
						onblur : 'cancel',
						callback : submitStatusClbk
					});
					
				};

				// assignee (read) 
				var $assigneetd = $row.find('.assignee-combo');
				$assigneetd.wrapInner('<span/>');
								
				// assignee (edit)
				if (initconf.permissions.editable){
					var url = initconf.urls.testplanUrl + data['entity-id'] ;
						
					$assigneetd.children().first().editable(url,{
						type : 'select',
						data : jsonAssignableUsers,
						name : 'assignee', 
						onblur : 'cancel',
						callback : submitAssigneeClbk
					});
				};			
				
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
						jqnew.find('.new-exec', 'new-auto-exec').button();
						
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
					$("#test-plans-table").squashTable().refresh();
				});
			}
			
		};
	
		return {
			tconf : tableSettings,
			sconf : squashSettings
		}
	
	}
	
	
	
	// ****************** DELETE EXECUTION CONFIGURATION **************
	
	function initDeleteExecutionPopup(conf){
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
				ctxt.trigger('context.iteration-updated', data);
			});	
			
		});
	}
	
	function init(origconf){		
		
		var conf = enhanceConfiguration(origconf);
		
		//table init
		var tableconf = createTableConfiguration(conf);
		
		$("#test-plans-table").squashTable(tableconf.tconf, tableconf.sconf);
		
		// delete execution popup init
		initDeleteExecutionPopup(origconf);
		
	}
	
	
	return {
		init : init
	};
	
});