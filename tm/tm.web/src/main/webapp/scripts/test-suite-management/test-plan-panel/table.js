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
 *		},
 *		basic : {
 *			testsuiteId : the id of the current testSuite
 *			assignableUsers : [ { 'id' : id, 'login' : login } ]
 *			weights [{ }]
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
		[ 'jquery', 'squash.translator', './exec-runner', './sortmode',
				'datepicker/require.jquery.squash.datepicker-locales',
				'jeditable.datepicker', 'squashtable', 'jeditable',
				'jquery.squash.buttonmenu' ],
		function($, translator, execrunner, smode, regionale) {

			// ****************** TABLE CONFIGURATION **************

			function _rowCallbackReadFeatures($row, data, _conf) {

				// style for deleted test case rows
				if (data['is-tc-deleted'] === "true") {
					$row.addClass('test-case-deleted');
				}

				// execution mode icon
				var $exectd = $row.find('.exec-mode').text('');
				if (data['exec-mode'] === "M") {
					$exectd.append(
							'<span class"exec-mode-icon exec-mode-manual"/>')
							.attr('title', '');
				} else {
					$exectd
							.append(
									'<span class="exec-mode-icon exec-mode-automated"/>')
							.attr('title', _conf.autoexecutionTooltip);
				}

				// execution status (read)
				var status = data['status'], i18nstatus = _conf.statuses[status], $statustd = $row
						.find('.status-combo'), html = _conf.statusFactory
						.getHtmlFor(i18nstatus, status);

				$statustd.html(html); // remember : this will insert a <span>
										// in the process

				// assignee (read)
				var $assigneetd = $row.find('.assignee-combo');
				$assigneetd.wrapInner('<span/>');
			}

			function _rowCallbackWriteFeatures($row, data, _conf) {

				// execution status (edit)
				var statusurl = _conf.testplanUrl + data['entity-id'];
				$row.find('.status-combo').children().first().editable(
						statusurl, {
							type : 'select',
							data : _conf.jsonStatuses,
							name : 'status',
							onblur : 'cancel',
							callback : _conf.submitStatusClbk
						});

				// assignee (edit)
				var assigneeurl = _conf.testplanUrl + data['entity-id'];
				$row.find('.assignee-combo').children().first().editable(
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
				var isTcDel = data['is-tc-deleted'], isManual = (data['exec-mode'] === "M");

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

			function _hideFilterFields() {
				$(".th_input", $("#test-suite-test-plans-table")).hide();
				$(".filter_input", $("#test-suite-test-plans-table")).each(function(){
					$("#test-suite-test-plans-table").squashTable().fnFilter("", $(".filter_input").index(this));
				});				
			}

			function _showFilterFields() {
				$(".th_input", $("#test-suite-test-plans-table")).show();
				$(".filter_input", $("#test-suite-test-plans-table")).each(function(){
					$("#test-suite-test-plans-table").squashTable().fnFilter(this.value, $(".filter_input").index(this));
				});
			}

			function _initializeFilterFields(initconf) {

				var users = initconf.basic.assignableUsers;
				var statuses = initconf.messages.executionStatus;
				var weights = initconf.basic.weights;

				$($("th", $("#test-suite-test-plans-table"))[1]).append(
						"<input class='th_input filter_input'/>");
				$($("th", $("#test-suite-test-plans-table"))[2]).append(
						"<input class='th_input filter_input'/>");
				$($("th", $("#test-suite-test-plans-table"))[3]).append(
						"<input class='th_input filter_input'/>");
				$($("th", $("#test-suite-test-plans-table"))[4])
						.append(
								"<select id='filter-weight-combo' class='th_input filter_input'/>");
				$($("th", $("#test-suite-test-plans-table"))[5]).append(
						"<input class='th_input filter_input'/>");
				$($("th", $("#test-suite-test-plans-table"))[6])
						.append(
								"<select id='filter-status-combo' class='th_input filter_input'/>");
				$($("th", $("#test-suite-test-plans-table"))[7])
						.append(
								"<select id='filter-user-combo' class='th_input filter_input'/>");
				$($("th", $("#test-suite-test-plans-table"))[8])
						.append(
								"<div class='datepicker th_input'>"
										+ "<input id='datepicker-input' readonly='readonly'/>"
										+ "<div id='datepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
										+ "<input type='hidden' id='datepicker-hidden-input' class='filter_input'/>"
										+ "</div>");

				$("#test-suite-test-plans-table_filter").hide();
	
				var nullOption = new Option("", "");
				$(nullOption).html("");
				$("#filter-status-combo", $("#test-suite-test-plans-table")).append(nullOption);
				
				$.each(statuses,
						function(index, value) {
							var o = new Option(value, index);
							$(o).html(value);
							$("#filter-status-combo",
									$("#test-suite-test-plans-table"))
									.append(o);
						});

				nullOption = new Option("", "");
				$(nullOption).html("");
				$("#filter-user-combo", $("#test-suite-test-plans-table")).append(nullOption);
				
				$.each(users, function(index, value) {
					var o = new Option(value, index);
					$(o).html(value);
					$("#filter-user-combo", $("#test-suite-test-plans-table"))
							.append(o);
				});

				nullOption = new Option("", "");
				$(nullOption).html("");
				$("#filter-weight-combo", $("#test-suite-test-plans-table")).append(nullOption);
				
				$.each(weights,
						function(index, value) {
							var o = new Option(value, index);
							$(o).html(value);
							$("#filter-weight-combo",
									$("#test-suite-test-plans-table"))
									.append(o);
						});

				$(".th_input").click(function(event) {
					event.stopPropagation();
				});

				$("#test-suite-test-plans-table th").hover(function(event) {
					event.stopPropagation();
				});
				
				$("#datepicker-div td").hover(function(event) {
					event.stopPropagation();
				});
				
				$(".filter_input").change(
						function() {
							$("#test-suite-test-plans-table").squashTable()
									.fnFilter(this.value,
											$(".filter_input").index(this));
						});

				var localemeta = {
						format : 'squashtm.dateformatShort.js',
						locale : 'squashtm.locale'
					};
					
				var message = translator.get(localemeta);

				var language = regionale[message.locale] || regionale;
					
				$.datepicker._defaults.onAfterUpdate = null;
				 $.datepicker.setDefaults(language);
				
				var datepicker__updateDatepicker = $.datepicker._updateDatepicker;
				
				$.datepicker._updateDatepicker = function(inst) {
					
					datepicker__updateDatepicker.call(this, inst);

					var onAfterUpdate = this._get(inst, 'onAfterUpdate');
					if (onAfterUpdate) {
						onAfterUpdate.apply(
								(inst.input ? inst.input[0] : null), [
										(inst.input ? inst.input.val() : ''),
										inst ]);
					}
				};

				var cur = -1, prv = -1;
				$('.datepicker div')
						.datepicker(
								{
									dateFormat : message.format,
									changeMonth : true,
									changeYear : true,
									showButtonPanel : true,

									beforeShowDay : function(date) {
										return [
												true,
												((date.getTime() >= Math.min(
														prv, cur) && date
														.getTime() <= Math.max(
														prv, cur)) ? 'date-range-selected'
														: '') ];
									},

									onSelect : function(dateText, inst) {
										var d1, d2;

										prv = cur;
										cur = (new Date(inst.selectedYear,
												inst.selectedMonth,
												inst.selectedDay)).getTime();
										if (prv == -1) {
											prv = cur;
											d1 = $.datepicker.formatDate(
													message.format, new Date(cur),
													{});
											df1 = $.datepicker.formatDate(
													'dd/mm/yy', new Date(cur),
													{});
											$('#datepicker-input').val(d1);
											$('#datepicker-hidden-input').val(
													df1);
											$('#datepicker-hidden-input')
													.change();
										} else if (prv == cur) {
											d1 = $.datepicker.formatDate(
													message.format, new Date(cur),
													{});
											df1 = $.datepicker.formatDate(
													'dd/mm/yy', new Date(cur),
													{});
											$('#datepicker-input').val(d1);
											$('#datepicker-hidden-input').val(
													df1);
											$('#datepicker-hidden-input')
													.change();
										} else {
											d1 = $.datepicker
													.formatDate(message.format,
															new Date(Math.min(
																	prv, cur)),
															{});
											d2 = $.datepicker
													.formatDate(message.format,
															new Date(Math.max(
																	prv, cur)),
															{});
											df1 = $.datepicker
													.formatDate('dd/mm/yy',
															new Date(Math.min(
																	prv, cur)),
															{});
											df2 = $.datepicker
													.formatDate('dd/mm/yy',
															new Date(Math.max(
																	prv, cur)),
															{});
											$('#datepicker-input').val(
													d1 + ' - ' + d2);
											$('#datepicker-hidden-input').val(
													df1 + ' - ' + df2);
											$('#datepicker-hidden-input')
													.change();
										}
									},

									onChangeMonthYear : function(year, month,
											inst) {
										// prv = cur = -1;
									},

									onAfterUpdate : function(inst) {
										
										if(!$("#okbutton", ".datepicker div .ui-datepicker-buttonpane").length){
										
											
								
											$(
												'<button type="button" id="okbutton" class="ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all" data-handler="hide" data-event="click">Ok</button>')
												.appendTo(
														$('.datepicker div .ui-datepicker-buttonpane'))
												.on(
														'click',
														function() {
															$('.datepicker div')
																	.hide();
														});
										
											$(
											'<button type="button" id="resetbutton" class="ui-datepicker-close ui-state-default ui-priority-primary ui-corner-all" data-handler="hide" data-event="click">reset</button>')
											.appendTo(
													$('.datepicker div .ui-datepicker-buttonpane'))
											.on(
													'click',
													function() {
														$('#datepicker-input').val("");
														$('#datepicker-hidden-input').val("");
														$('#datepicker-hidden-input').change();
													});
											
									}
										}
								}).hide();

				$('#datepicker-input').on(
						'focus',
						function(e) {
							var v = this.value, d;

							try {
								if (v.indexOf(' - ') > -1) {
									d = v.split(' - ');

									prv = $.datepicker.parseDate(message.format,
											d[0]).getTime();
									cur = $.datepicker.parseDate(message.format,
											d[1]).getTime();

								} else if (v.length > 0) {
									prv = cur = $.datepicker.parseDate(
											message.format, v).getTime();
								}
							} catch (e) {
								cur = prv = -1;
							}

							if (cur > -1) {
								$('.datepicker div').datepicker('setDate',
										new Date(cur));
							}

							$('.datepicker div').datepicker('refresh').show();
						});

				_hideFilterFields();
			}

			function createTableConfiguration(initconf) {

				// conf objects for the row callbacks
				var _readFeaturesConf = {
					statuses : initconf.messages.executionStatus,
					autoexecutionTooltip : initconf.messages.automatedExecutionTooltip,
					statusFactory : new squashtm.StatusFactory(
							initconf.messages.executionStatus)
				};

				var _writeFeaturesConf = {

					testplanUrl : initconf.urls.testplanUrl,

					jsonStatuses : JSON
							.stringify(initconf.messages.executionStatus),
					submitStatusClbk : function(value, settings) {
						var $span = $(this), statuses = JSON
								.parse(settings.data);
						$span.attr('class', 'exec-status-label exec-status-'
								+ value.toLowerCase());
						$span.text(statuses[value]);
					},

					jsonAssignableUsers : JSON
							.stringify(initconf.basic.assignableUsers),
					submitAssigneeClbk : function(value, settings) {
						var assignableUsers = JSON.parse(settings.data);
						$(this).text(assignableUsers[value]);
					}
				};

				var _execFeaturesConf = {

					manualHandler : function() {

						var $this = $(this), tpid = $this.data('tpid'), ui = ($this
								.is('.run-popup')) ? "popup" : "oer", newurl = initconf.urls.testplanUrl
								+ tpid + '/executions/new';

						$.post(newurl, {
							mode : 'manual'
						}, 'json').done(
								function(execId) {
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
						var row = $(this).parents('tr').get(0), table = $(
								"#test-suite-test-plans-table").squashTable(), data = table
								.fnGetData(row), tpid = data['entity-id'], newurl = initconf.urls.testplanUrl
								+ tpid + '/executions/new';

						$
								.post(newurl, {
									mode : 'auto'
								}, 'json')
								.done(
										function(suiteview) {
											if (suiteview.executions.length === 0) {
												$.squash
														.openMessage(
																initcon.messages.titleInfo,
																initconf.messages.messageNoAutoexecFound);
											} else {
												squashtm.automatedSuiteOverviewDialog
														.open(suiteview);
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

							jqnew
									.load(
											url,
											function() {

												// styling
												var newexecBtn = jqnew.find(
														'.new-exec')
														.squashButton(), newautoexecBtn = jqnew
														.find('.new-auto-exec')
														.squashButton();

												// the delete buttons
												if (initconf.permissions.executable) {
													jqnew
															.find(
																	'.delete-execution-table-button')
															.button(
																	{
																		text : false,
																		icons : {
																			primary : "ui-icon-trash"
																		}
																	})
															.on(
																	'click',
																	function() {
																		var dialog = $("#ts-test-plan-delete-execution-dialog");
																		dialog
																				.data(
																						'origin',
																						this);
																		dialog
																				.confirmDialog('open');
																	});

													// the new execution buttons
													newexecBtn
															.click(function() {
																var url = $(
																		this)
																		.data(
																				'new-exec');
																$
																		.post(
																				url,
																				{
																					mode : 'manual'
																				},
																				'json')
																		.done(
																				function(
																						id) {
																					document.location.href = initconf.urls.executionsUrl
																							+ id;
																				});
																return false;
															});

													newautoexecBtn
															.click(function() {
																var url = $(
																		this)
																		.data(
																				'new-exec');
																$
																		.post(
																				url,
																				{
																					mode : 'auto'
																				},
																				'json')
																		.done(
																				function(
																						suiteview) {
																					if (suiteview.executions.length === 0) {
																						$.squash
																								.openMessage(
																										initcon.messages.titleInfo,
																										initconf.messages.messageNoAutoexecFound);
																					} else {
																						squashtm.automatedSuiteOverviewDialog
																								.open(suiteview);
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
							$("#test-suite-test-plans-table").squashTable()
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

					var table = $("#test-suite-test-plans-table").squashTable(
							tableconf.tconf, tableconf.sconf);
					table.data('sortmode', sortmode);
					this.lockSortMode = sortmode._lockSortMode;
					this.unlockSortMode = sortmode._unlockSortMode;
					this.hideFilterFields = _hideFilterFields;
					this.showFilterFields = _showFilterFields;
					_initializeFilterFields(enhconf);
				}
			};

		});