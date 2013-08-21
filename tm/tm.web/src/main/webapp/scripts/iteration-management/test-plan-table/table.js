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
 *			assignableUsers : the json string for a map of { "id1 : "login1", "id2" : "login2" etc }
 * 		}
 * }
 * 
 */

define(['jquery', 'squash.translator', 'jquery.squash.datatables', 'jeditable'],function($, translator) {


	
	// ****************** INIT FUNCTIONS **************
	
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
			 executionsUrl : baseURL + '/executions'
		};
		
		return conf;
	}
	
	
	function createTableConfiguration(initconf){
		
		var statuses = initconf.messages.executionStatus;
		var assignableUsers = JSON.parse(initconf.basic.assignableUsers);
		
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
					html = squashtm.StatusFactory.getHtmlFor(i18nstatus, status);
					
				$statustd.html(html);	// remember : this will insert a <span> in the process
				
				// execution status (edit)
				if (initconf.permissions.editable){
					
					var comboconf = $.extend({}, statuses, { 'selected' : status } ),
						url = initconf.urls.testplanUrl + data['entity-id'],
						$statustd = $row.find('.status-combo');					
					
					$statustd.children().first().editable(url, {
						type : 'select',
						data : JSON.stringify(comboconf),
						name : 'status',
						onblur : 'submit';
					});
					
				};

				// assignee (read) 
				var $assigneetd = $row.find('.assignee-combo');
				$assigneetd.wrapInner('<span/>');
				
				// assignee (edit)
				if (initconf.permissions.editable){
					var assigneeId = data['assigned-to'] || "0",
						comboconf = $.exted({}, assignableUsers, { 'selected' : assigneeId }),
						url = initconf.urls.testplanUrl + data['entity-id'] ;
						
					$assigneetd.children().first().editable(url,{
						type : 'select',
						data : JSON.stringify(comboconf),
						name : 'assignee', 
						onblur : 'submit'
					});
				}
				
				
				// done
				return row;
			}
		};
		
		var squashSettings : {
			toggleRows : {				
				'td.toggle-row' : function(table, jqold, jqnew){
					
					var data = table.fnGetData(jqold.get(0)),
						url = initconf.urls.executionsUrl +'/' + data['entity-id'];
						
					jqnew.load(url, function(){				
						decorateRow(jqnew);
						if (initconf.permissions.executable){
							bindRowButtons(jqnew);
						}
					});
				}
			}
		};
		
		//more conf if editable
		
		if (conf.permissions.editable){
			
			squashSettings.enableDnD = true;
			
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
	
	
	function init(origconf){		
		
		var conf = enhanceConfiguration(origconf);
		var tableconf = createTableConfiguration(conf);
		
		$("#test-plans-table").squashTable(tableconf.tconf, tableconf.sconf);
	}
	
	
	return {
		init : init
	};
	
});