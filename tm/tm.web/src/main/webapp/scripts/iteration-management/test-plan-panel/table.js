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
 *			reorderable : boolean, can the user reorder the content ?
 *		},
 *		basic : {
 *			iterationId : the id of the current iteration
 *			assignableUsers : [ { 'id' : id, 'login' : login } ]
 *			weights : []
 *		},
 *		messages : {
 *			executionStatus : {
 *				UNTESTABLE : i18n label,
 *				BLOCKED : i18n label,
 *				FAILURE : i18n label,
 *				SUCCESS : i18n label,
 *				RUNNING : i18n label,
 *				READY : i18n label,
 *			},
 *			automatedExecutionTooltip : i18n label,
 *			labelOk : i18n label,
 *			labelCancel : i18n label,
 *			titleInfo : i18n label,
 *			messageNoAutoexecFound : i18n label
 *		},
 *		urls : {
 *			testplanUrl : base urls for test plan items,
 *			executionsUrl : base urls for executions
 *		}
 *	}
 * 
 */

define(
		[ 'jquery', 'squash.translator', './exec-runner', './sortmode', './filtermode',
		 'squash.dateutils', 'squash.statusfactory',
		  'test-automation/automated-suite-overview',
		  'squashtable', 'jeditable', 'jquery.squash.buttonmenu' ],
		function($, translator, execrunner, smode, filtermode, dateutils, statusfactory, autosuitedialog) {

			// ****************** TABLE CONFIGURATION **************

			function _rowCallbackReadFeatures($row, data, _conf) {

				// style for deleted test case rows
				if (data['is-tc-deleted'] === "true") {
					$row.addClass('test-case-deleted');
				}

				// execution mode icon
				var $exectd = $row.find('.exec-mode').text('');
				if (data['exec-mode'] === "A") {
					$exectd.append('<span class"exec-mode-icon exec-mode-manual"/>').attr('title', '');
				} else {
					$exectd.append('<span class="exec-mode-icon exec-mode-automated"/>').attr('title',
							_conf.autoexecutionTooltip);
				}
				
				// execution status (read)
				var status = data['status'], 
					$statustd = $row.find('.status-combo'), 
					html = statusfactory.getHtmlFor(status);

				$statustd.html(html); // remember : this will insert a <span>
										// in the process
				
				//execution date
				var date = data['last-exec-on'],
					format = translator.get('squashtm.dateformat');
				
				if(!!date){
					$row.find('.exec-on').text(dateutils.format(date, format));
				} else {
					$row.find('.exec-on').text('-');
				}

				// assignee (read)
				var $assigneetd = $row.find('.assignee-combo');
				$assigneetd.wrapInner('<span/>');
			}

			function _rowCallbackWriteFeatures($row, data, _conf) {

				// execution status (edit). Note : the children().first() thing
				// will return the span element.
				var statusurl = _conf.testplanUrl + data['entity-id'];
				var statusElt = $row.find('.status-combo').children().first();
				statusElt.addClass('cursor-arrow');
				statusElt.editable(
						statusurl, {
							type : 'select',
							data : _conf.jsonStatuses,
							name : 'status',
							onblur : 'cancel',
							callback : _conf.submitStatusClbk
						});

				// assignee (edit). Note : the children().first() thing will
				// return the span element.
				var assigneeurl = _conf.testplanUrl + data['entity-id'];
				var assigneeElt = $row.find('.assignee-combo').children().first();
				assigneeElt.addClass('cursor-arrow');
				assigneeElt.editable(
					assigneeurl, {
						type : 'select',
						data : _conf.jsonAssignableUsers,
						name : 'assignee',
						onblur : 'cancel',
						callback : _conf.submitAssigneeClbk
					});

			}

			function _rowCallbackExecFeatures($row, data, _conf) {

				// add the execute shortcut menu
				var isTcDel = data['is-tc-deleted'], 
					isManual = (data['exec-mode'] === "M");

				var tpId = data['entity-id'], $td = $row
						.find('.execute-button'), strmenu = $(
						"#shortcut-exec-menu-template").html().replace(
						/#placeholder-tpid#/g, tpId);

				$td.empty();
				$td.append(strmenu);

				// if the test case is deleted : just disable the whole thing
				if (isTcDel) {
					$td.find('.execute-arrow').addClass('disabled-transparent');
				}

				// if the test case is manual : configure a button menu,
				// althgouh we don't want it
				// to be skinned as a regular jquery button
				else if (isManual) {
					$td.find('.buttonmenu').buttonmenu({
						preskinned : true,
						anchor : "right"
					});
					$td.on('click', '.run-menu-item', _conf.manualHandler);
				}

				// if the test case is automated : just configure the button
				else {
					$td.find('.execute-arrow').click(_conf.automatedHandler);
				}

			}

			function createTableConfiguration(initconf) {

				// conf objects for the row callbacks
				var _readFeaturesConf = {
					statuses : initconf.messages.executionStatus,
					autoexecutionTooltip : initconf.messages.automatedExecutionTooltip
				};

				var _writeFeaturesConf = {

					testplanUrl : initconf.urls.testplanUrl,

					jsonStatuses : JSON.stringify(initconf.messages.executionStatus),
					
					submitStatusClbk : function(json, settings) {
						
						// must update the execution status, the execution date and the assignee
						var itp = JSON.parse(json);
						
						// 1/ the status
						var $statusspan = $(this), 
							statuses = JSON.parse(settings.data);
						
						$statusspan.attr('class', 'cursor-arrow exec-status-label exec-status-' + itp.executionStatus.toLowerCase());						
						$statusspan.text(statuses[itp.executionStatus]);
						
						// 2/ the date format
						var format = translator.get('squashtm.dateformat'),
							$execon= $statusspan.parents('tr:first').find("td.exec-on");
	
						var newdate = dateutils.format(itp.lastExecutedOn, format);
						$execon.text(newdate);
						
						// 3/ user assigned
						$statusspan.parents('tr:first')
									.find('td.assignee-combo')
									.children().first().
									text(itp.assignee);
					},

					jsonAssignableUsers : JSON.stringify(initconf.basic.assignableUsers),
					
					submitAssigneeClbk : function(value, settings) {
						var assignableUsers = JSON.parse(settings.data);
						$(this).text(assignableUsers[value]);
					}
				};

				var _execFeaturesConf = {

					manualHandler : function() {

						var $this = $(this), 
							tpid = $this.data('tpid'), 
							ui = ($this.is('.run-popup')) ? "popup" : "oer", newurl = initconf.urls.testplanUrl
								+ tpid + '/executions/new';

						$.post(newurl, {
							mode : 'manual'
						}, 'json').done(function(execId) {
							var execurl = initconf.urls.executionsUrl
									+ execId + '/runner';
							if (ui === "popup") {
								execrunner.runInPopup(execurl);
							} else {
								execrunner.runInOER(execurl);
							}

						});
					},

					automatedHandler : function() {
						var row = $(this).parents('tr').get(0), 
							table = $("#iteration-test-plans-table").squashTable(), 
							data = table.fnGetData(row), 
							tpid = data['entity-id'], 
							newurl = initconf.urls.testplanUrl+ tpid + '/executions/new';

						$.post(newurl, {
							mode : 'auto'
						}, 'json')
						.done(function(suiteview) {
							if (suiteview.executions.length === 0) {
								var _msg = initconf.messages;
								$.squash.openMessage(_msg.titleInfo,
												_msg.messageNoAutoexecFound);
							} else {
								autosuitedialog.get().watch(suiteview);
							}
						});

					}
				};

				// basic table configuration. Much of it is in the DOM of the
				// table.
				var tableSettings = {

					fnRowCallback : function(row, data, displayIndex) {

						var $row = $(row);

						// add read-only mode features (always applied)
						_rowCallbackReadFeatures($row, data, _readFeaturesConf);

						// add edit-mode features
						if (initconf.permissions.editable) {
							_rowCallbackWriteFeatures($row, data,
									_writeFeaturesConf);
						}

						// add execute-mode features
						if (initconf.permissions.executable) {
							_rowCallbackExecFeatures($row, data,
									_execFeaturesConf);
						}

						// done
						return row;
					},

					fnDrawCallback : function() {

						// make all <select> elements autosubmit on selection
						// change.
						this.on('change', 'select', function() {
							$(this).submit();
						});

						// update the sort mode
						var settings = this.fnSettings();
						var aaSorting = settings.aaSorting;

						this.data('sortmode').manage(aaSorting);
					}
				};

				var squashSettings = {

					toggleRows : {
						'td.toggle-row' : function(table, jqold, jqnew) {

							var data = table.fnGetData(jqold.get(0)), url = initconf.urls.testplanUrl
									+ data['entity-id'] + '/executions';

							jqnew.load(url, function() {

								// styling
								var newexecBtn = jqnew.find(
										'.new-exec')
										.squashButton(), newautoexecBtn = jqnew
										.find('.new-auto-exec')
										.squashButton();

								// the delete buttons
								if (initconf.permissions.executable) {
									jqnew.find('.delete-execution-table-button')
										.button({
											text : false,
											icons : {
												primary : "ui-icon-trash"
											}
										}).on('click', function() {
											var dialog = $("#iter-test-plan-delete-execution-dialog");
											dialog.data('origin',this);
											dialog.confirmDialog('open');
										});

									// the new execution buttons
									newexecBtn.click(function() {
										var url = $(this).data('new-exec');
										$.post(url,{
											mode : 'manual'
										},
										'json')
										.done(function(id) {
											document.location.href = initconf.urls.executionsUrl + id;
										});
										return false;
									});

									newautoexecBtn.click(function() {
										var url = $(this).data('new-exec');
										$.post(url,{
											mode : 'auto'
										},
										'json')
										.done(function(suiteview) {
											var _msg = initconf.messages;
											if (suiteview.executions.length === 0) {
												$.squash.openMessage(_msg.titleInfo,_msg.messageNoAutoexecFound);
											} else {
												autosuitedialog.get().watch(suiteview);
											}
										});
										return false;
									});
								}
							});
						}
					}
				};

				// more conf if editable

				if (initconf.permissions.reorderable) {

					squashSettings.enableDnD = true;
					squashSettings.functions = {};
					squashSettings.functions.dropHandler = function(dropData) {
						var ids = dropData.itemIds.join(',');
						var url = initconf.urls.testplanUrl + '/' + ids
								+ '/position/' + dropData.newIndex;
						$.post(url, function() {
							$("#iteration-test-plans-table").squashTable()
									.refresh();
						});
					};

				}

				return {
					tconf : tableSettings,
					sconf : squashSettings
				};

			}

			// **************** MAIN ****************

			return {
				init : function(enhconf) {

					var tableconf = createTableConfiguration(enhconf);

					var sortmode = smode.newInst(enhconf);
					tableconf.tconf.aaSorting = sortmode.loadaaSorting();

					var table = $("#iteration-test-plans-table").squashTable(
							tableconf.tconf, tableconf.sconf);
					table.data('sortmode', sortmode);
					this.lockSortMode = sortmode._lockSortMode;
					this.unlockSortMode = sortmode._unlockSortMode;
					
					this.hideFilterFields = filtermode.hideFilterFields;
					this.showFilterFields = filtermode.showFilterFields;
					filtermode.initializeFilterFields(enhconf);
				}
			};

		});