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
 * The initialization module takes settings, as you expect. Here is what the 
 * configuration object looks like :
 * 
 * {
 * 
 * 
 *  basic : {
 *      testCaseId : the id of the test case,
 *      projectId : the id of the project this test case belongs to
 *      rootContext : the root url
 *      testCaseUrl : the baseTestCaseUrl
 *  },
 * 
 * 
 *  permissions : {
 *      isWritable : says whether the table content or structure can be modified by the user
 *      isLinkable : says whether the access to the requirement/test-step association page is accessible
 *      isAttachable : says if you can attach attachments to the steps 
 *  },
 * 
 *  language : {
 *      errorTitle : the title of the error popup
 *      noStepSelected : the message when no steps where selected although some were needed
 *      oklabel : the ok label for any confirmation popup
 *      cancellabel : the cancellabel for any confirmation popup
 *      deleteConfirm : the message for confirmation of deletion of the popup
 *      deleteTitle : the tooltip for the delete popup buttons
 *      infoTitle : the title for the popup that says close your widgets in edit mode
 *      popupMessage : the content of that popup
 *      btnExpand : the label of the expand button
 *      btnCollapse : the label of the collapse button  
 *      addStepTitle : title for the add step popup
 *      addStep : label for the add step button
 *      addAnotherStep :  label for the add another step button
 *      ckeLang : the language for ckEditor
 *      placeholder : the placeholder title
 *      submit : the submit button value
 *  }
 * 
 * }
 * 
 * 
 * 
 */

define([ "jquery", "squash.table-collapser", "custom-field-values", "squash.translator" ], function($, TableCollapser,
		cufValuesManager, translator) {

	// ************************* configuration functions
	// ************************************

	function makeTableUrls(conf) {
		var tcUrl = conf.basic.testCaseUrl;
		var ctxUrl = conf.basic.rootContext;
		return {
			dropUrl : tcUrl + "/steps/move",
			attachments : ctxUrl + "/attach-list/{attach-list-id}/attachments/manager?workspace=test-case",
			singleDelete : tcUrl + "/steps/{step-id}",
			steps : ctxUrl + "test-steps/",
			multiDelete : tcUrl + "/steps",
			callTC : ctxUrl + "/test-cases/{called-tc-id}/info",
			pasteStep : tcUrl + "/steps",
			addStep : tcUrl + "/steps/add",
			editActionUrl : tcUrl + "/steps/{step-id}/action",
			editResultUrl : tcUrl + "/steps/{step-id}/result",
			stepcufBindingUrl : ctxUrl + "/custom-fields-binding?projectId=" + conf.basic.projectId +
					"&bindableEntity=TEST_STEP&optional=false",
			ckeConfigUrl : ctxUrl + "styles/ckeditor/ckeditor-config.js",
			indicatorUrl : ctxUrl + "/scripts/jquery/indicator.gif",
			callStepManagerUrl : tcUrl + "/call",
			tableLanguageUrl : ctxUrl + "/datatables/messages",
			tableAjaxUrl : tcUrl + "/steps",
			projectId : conf.basic.projectId
		// yes, that's no url. Uh.
		};
	}

	// ************************* table configuration functions
	// ******************************

	function refresh() {
		$("#test-steps-table").squashTable().refresh();
	}

	function removeStepSuccess() {
		refresh();
		$("#contextual-content").trigger("testStepsTable.removedSteps");
	}

	function stepsTableCreatedRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
		nRow.className += (aData['step-type'] === "action") ? " action-step-row" : " call-step-row";
	}

	function specializeCellClasses(table) {

		var actionRows = table.find('tr.action-step-row');
		var callRows = table.find('tr.call-step-row');

		// remove useless classes for action step rows
		actionRows.find('td.called-tc-cell').removeClass('called-tc-cell');

		// remove useless classes for call step rows
		callRows.find('td.rich-edit-action').removeClass('rich-edit-action');
		callRows.find('td.rich-edit-result').removeClass('rich-edit-result');
		callRows.find('td.has-attachment-cell').removeClass('has-attachment-cell');
		callRows.find('td.custom-field-value').removeClass(); // remove
		// all
		// the
		// classes
		callRows.find('td.called-tc-cell').next().remove().end().attr('colspan', 2);
	}

	function stepsTableDrawCallback() {

		// rework the td css classes to inhibit some post processing on
		// them when not relevant
		specializeCellClasses(this);

		// collapser
		var collapser = this.data('collapser');
		if (collapser) {
			collapser.refreshTable();
		}

	}

	function stepDropHandlerFactory(dropUrl) {
		return function stepDropHandler(dropData) {
			$.post(dropUrl, dropData, function() {
				refresh();
			});
		};
	}

	// ************************************ table initialization
	// *****************************

	function initTable(settings) {
		var cufColumnPosition = 4;
		var language = settings.language, urls = makeTableUrls(settings), permissions = settings.permissions;

		var cufTableHandler = cufValuesManager.cufTableSupport;

		// first we must process the DOM table for cufs
		cufTableHandler.decorateDOMTable($("#test-steps-table"), settings.basic.cufDefinitions, cufColumnPosition);

		// now let's move to the datatable configuration
		// in order to enable/disable some features regarding the
		// permissions, one have to tune the css classes of some
		// columns.
		var editActionClass = "", editResultClass = "", deleteClass = "", dragClass = "", linkButtonClass = "", attachButtonClass = "";
		
		if (permissions.isWritable) {
			editActionClass = "rich-edit-action";
			editResultClass = "rich-edit-result";
			deleteClass = "delete-button";
			dragClass = "drag-handle";
		}
		if(!permissions.isLinkable){
			linkButtonClass = "default-cursor";
		}
		if(!permissions.isAttachable){
			attachButtonClass = "default-cursor";
		}

		// create the settings
		var datatableSettings = {
			oLanguage : {
				sUrl : urls.tableLanguageUrl
			},
			aaData : settings.basic.tableData,
			sAjaxSource : urls.tableAjaxUrl,
			fnDrawCallback : stepsTableDrawCallback,
			fnCreatedRow : stepsTableCreatedRowCallback,
			iDeferLoading : settings.basic.totalRows,
			iDisplayLength : 50,
			aoColumnDefs : [ {
				'bVisible' : false,
				'bSortable' : false,
				'aTargets' : [ 0 ],
				'mDataProp' : 'step-id'
			}, {
				'bVisible' : true,
				'bSortable' : false,
				'aTargets' : [ 1 ],
				'mDataProp' : 'step-index',
				'sClass' : 'select-handle centered ' + dragClass,
				'sWidth' : '2em'
			}, {
				'bVisible' : true,
				'bSortable' : false,
				'aTargets' : [ 2 ],
				'mDataProp' : 'attach-list-id',
				'sClass' : 'centered has-attachment-cell '+ attachButtonClass,
				'sWidth' : '2em'
			}, {
				'bVisible' : true,
				'bSortable' : false,
				'aTargets' : [ 3 ],
				'mDataProp' : 'empty-requirements-holder',
				'sClass' : 'centered requirements-button '+ linkButtonClass,
				'sWidth' : '2em'
			}, {
				'bVisible' : true,
				'bSortable' : false,
				'aTargets' : [ 4 ],
				'mDataProp' : 'step-action',
				'sClass' : 'called-tc-cell collapsible ' + editActionClass
			}, {
				'bVisible' : true,
				'bSortable' : false,
				'aTargets' : [ 5 ],
				'mDataProp' : 'step-result',
				'sClass' : 'collapsible ' + editResultClass
			}, {
				'bVisible' : false,
				'bSortable' : false,
				'aTargets' : [ 6 ],
				'mDataProp' : 'nb-attachments'
			}, {
				'bVisible' : false,
				'bSortable' : false,
				'aTargets' : [ 7 ],
				'mDataProp' : 'step-type'
			}, {
				'bVisible' : false,
				'bSortable' : false,
				'aTargets' : [ 8 ],
				'mDataProp' : 'called-tc-id'
			}, {
				'bVisible' : true,
				'bSortable' : false,
				'aTargets' : [ 9 ],
				'mDataProp' : 'empty-browse-holder',
				'sClass' : 'centered browse-button',
				'sWidth' : '2em'
			}, {
				'bVisible' : true,
				'bSortable' : false,
				'aTargets' : [ 10 ],
				'mDataProp' : 'empty-delete-holder',
				'sClass' : 'centered ' + deleteClass,
				'sWidth' : '2em'
			}, {
				'bVisible' : false,
				'bSortable' : false,
				'aTargets' : [ 11 ],
				'mDataProp' : 'has-requirements'
			} ]

		};

		// decorate the settings with the cuf values support
		datatableSettings = cufTableHandler.decorateTableSettings(datatableSettings, settings.basic.cufDefinitions,
				cufColumnPosition, permissions.isWritable);

		var squashSettings = {

			dataKeys : {
				entityId : 'step-id',
				entityIndex : 'step-index'
			},

			enableHover : true,

			confirmPopup : {
				oklabel : language.oklabel,
				cancellabel : language.cancellabel
			},

			attachments : {
				url : "#"
			},

			bindLinks : {
				list : [ {
					targetClass : 'called-tc-cell',
					url : urls.callTC
				} ]
			},

			buttons : [ {
				tooltip : language.edit,
				cssClass : "",
				tdSelector : "td.browse-button",
				image : "/squash/images/pencil.png",
				onClick : function(table, cell) {
					var row = cell.parentNode.parentNode;
					var stepId = table.getODataId(row);
					var url = urls.steps + stepId;
					window.open(url, '_blank');
					window.focus();
				}
			}, {
				tooltip : language.requirements,
				cssClass : "",
				tdSelector : "td.requirements-button",
				image : function(row, data) {
					if (data["has-requirements"]) {
						return "/squash/images/Icon_Tree_Requirement.png";
					}
					return "/squash/images/Icon_Tree_Requirement_off.png";
				},
				condition : function(row, data) {
					return data["step-type"] == "action";
				},
				onClick : function(table, cell) {
					if (permissions.isLinkable){
						var row = cell.parentNode.parentNode;
						var stepId = table.getODataId(row);
						var url = urls.steps + stepId + "/verified-requirement-versions/manager";
						document.location.href = url;
					}
				}
			} ]

		};

		if (permissions.isWritable) {

			var moreSettings = {

				enableDnD : true,

				deleteButtons : {
					url : urls.singleDelete,
					popupmessage : language.deleteSingleConfirm,
					tooltip : language.deleteTitle,
					success : removeStepSuccess
				},

				richEditables : {
					conf : {
						ckeditor : {
							customConfig : urls.ckeConfigUrl,
							language : language.ckeLang
						},
						placeholder : language.placeholder,
						submit : language.submit,
						cancel : language.cancellabel,
						indicator : language.indicatorUrl
					},

					targets : {
						'rich-edit-action' : urls.editActionUrl,
						'rich-edit-result' : urls.editResultUrl
					}
				},

				functions : {
					dropHandler : stepDropHandlerFactory(urls.dropUrl)
				}
			};

			$.extend(squashSettings, moreSettings);

		}

		if (permissions.isAttachable) {
			squashSettings.attachments = {
				url : urls.attachments
			};
		}

		$("#test-steps-table").squashTable(datatableSettings, squashSettings);

	}

	// ************************************ toolbar utility functions
	// *************************

	
	// *************************** add step popup
	// ***************************

	function addTestStepSuccess() {
		var dialog = $("#add-test-step-dialog");
		if (dialog.dialog("isOpen")) {
			dialog.dialog('close');
		}
		refresh();
	}

	function addTestStepSuccessAnother() {
		CKEDITOR.instances["add-test-step-action"].setData('');
		CKEDITOR.instances["add-test-step-result"].setData('');

		var dialog = $("#add-test-step-dialog");
		dialog.data('cuf-values-support').reset();

		refresh();
	}

	function readAddStepParams() {

		var cufSupport = $("#add-test-step-dialog").data('cuf-values-support');

		var params = {};
		params.action = $("#add-test-step-action").val();
		params.expectedResult = $("#add-test-step-result").val();
		$.extend(params, cufSupport.readValues());

		return params;

	}

	function initAddTestStepDialog(language, urls) {

		
		function postStep(data) {
			return $.ajax({
				url : urls.addStep,
				type : 'POST',
				data : data,
				dataType : 'json'
			});
		}

		// main popup definition

		// TODO : the handlers
		var params = {
			selector : "#add-test-step-dialog",
			openedBy : "#add-test-step-button",
			title : language.addStepTitle,
			isContextual : true,
			usesRichEdit : true,
			closeOnSuccess : false,
			ckeditor : {
				styleUrl : urls.ckeConfigUrl,
				lang : language.ckeLang
			},
			buttons : [ {
				'text' : language.addAnotherStep,
				'click' : function() {
					var data = readAddStepParams();
					postStep(data).success(addTestStepSuccessAnother);
				}
			}, {
				'text' : language.addStep,
				'click' : function() {
					var data = readAddStepParams();
					postStep(data).success(addTestStepSuccess);
				}
			}, {
				'text' : language.cancellabel,
				'click' : function() {
					$("#add-test-step-dialog").dialog('close');
				}
			} ]

		};

		squashtm.popup.create(params);

		// cuf value support

		var dialog = $("#add-test-step-dialog");
		var cufTable = $("#add-test-step-custom-fields");
		var bindingsUrl = urls.stepcufBindingUrl;

		var cufValuesSupport = cufValuesManager.newCreationPopupCUFHandler({
			url : bindingsUrl,
			table : cufTable
		});
		cufValuesSupport.reloadPanel();
		dialog.data('cuf-values-support', cufValuesSupport);

		dialog.on('dialogopen', function() {
			cufValuesSupport.reset();
		});
	}

	// ************************* other buttons code
	// **********************************

	function initStepCopyPastaButtons(language, urls) {

		var table = $("#test-steps-table").squashTable();

		
		$("#copy-step").bind('click', function() {
			var stepIds = table.getSelectedIds();
			if (!stepIds.length) {
				$.squash.openMessage(language.errorTitle, language.noStepSelected);
			} else {
				var oPath = {
					path : '/'
				};
				$.cookie('squash-test-step-ids', stepIds.toString(), oPath);
				$.cookie('squash-test-step-project', urls.projectId, oPath);
			}

		});

		$("#paste-step").bind(
				'click',
				function() {

					var cookieIds = $.cookie('squash-test-step-ids');
					var cookieProject = $.cookie('squash-test-step-project');
					var currentProject = urls.projectId;

					if (parseInt(cookieProject, 10) !== currentProject) {
						oneShotConfirm(language.infoTitle, language.warnCopy, language.confirmlabel,
								language.cancellabel).then(function() {
							performPaste(cookieIds); // see definition below
						});
					} else {
						performPaste(cookieIds); // see definition below
					}
				});

		function performPaste(rawIds) {
			var stepIds = rawIds.split(",");

			try {
				if (!stepIds.length) {
					throw language.noStepSelected;
				}

				var position = table.getSelectedIds();

				var data = {};
				data.copiedStepId = stepIds;

				var pasteUrl = urls.pasteStep;

				if (position.length > 0) {
					data.indexToCopy = position[0];
					pasteUrl = pasteUrl + "/paste";
				} else {
					pasteUrl = pasteUrl + "/paste-last-index";
				}

				$.ajax({
					type : 'POST',
					data : data,
					url : pasteUrl,
					dataType : "json",
					success : refresh
				});

				$("#paste-step").removeClass('ui-state-focus');
			} catch (damn) {
				$.squash.openMessage(language.errorTitle, damn);
			}
		}
	}

	function initDeleteAllStepsButtons(language, urls) {

	
		$("#delete-all-steps-button").bind(
				'click',
				function() {

					var table = $("#test-steps-table").squashTable();
					var ids = table.getSelectedIds();

					if (!ids.length) {
						$.squash.openMessage(language.errorTitle, language.noStepSelected);
					} else {
						var promise = oneShotConfirm(language.deleteTitle, language.deleteMultipleConfirm,
								language.oklabel, language.cancellabel);

						promise.done(function() {
							$.ajax({
								url : urls.multiDelete + "/" + ids.join(','),
								type : 'DELETE',
								dataType : "json"
							}).success(refresh);
						});
					}
				});
	}

	function initCallStepButton(urls) {

	
		$("#add-call-step-button").click(function() {
			var url = document.URL;
			$.cookie('call-step-manager-referer', url, {
				path : '/'
			});
			document.location.href = urls.callStepManagerUrl;
		});
	}

	// ******************************* toolbar initialization
	// *********************************

	function initTableToolbar(language, urls) {

		// copy pasta buttons
		initStepCopyPastaButtons(language, urls);

		// delete all button
		initDeleteAllStepsButtons(language, urls);

		// call step button
		initCallStepButton(urls);

		// add test step
		initAddTestStepDialog(language, urls);

	}

	// ************************************* table collapser code
	// ****************************

	function isEditing(collapser) {
		var collapsibleCells = collapser.collapsibleCells;
		for ( var k = 0; k < collapsibleCells.length; k++) {
			if (collapsibleCells[k].editing) {
				return true;
			}
		}
		return false;
	}

	function makeCollapsibleCellsHandlers(collapser) {

		var openEdit = $.proxy(function(eventObject) {
			this.openAll();
			$(eventObject.target).click();
		}, collapser);

		return {
			open : function(collapser) {
				var collapsibleCells = $(collapser.collapsibleCells);
				collapsibleCells.addClass('editable').off("click", openEdit).editable('enable');
			},

			close : function(collapser) {
				var collapsibleCells = $(collapser.collapsibleCells);
				collapsibleCells.removeClass('editable').on("click", openEdit).editable('disable');
			}
		};
	}

	function initCollapser(language, urls, isWritable) {

	
		var collapser;

		var collapseButton = $('#collapse-steps-button');

		var table = $('#test-steps-table');

		// begin

		var cellSelector = function(row) {
			return $(row).find('td.collapsible').not('called-tc-id').get();
		};

		collapser = new TableCollapser(table, cellSelector);

		// button handlers

		var buttonOpenHandler = $.proxy(function() {
			this.squashButton('option', 'icons', {
				primary : 'ui-icon-zoomout'
			});
			this.attr('title', language.btnCollapse);
			this.squashButton('option', 'label', language.btnCollapse);
		}, collapseButton);

		var buttonCloseHandler = $.proxy(function() {
			$this.squashButton('option', 'icons', {
				primary : 'ui-icon-zoomin'
			});
			$this.attr('title', language.btnExpand);
			$this.squashButton('option', 'label', language.btnExpand);

		}, collapseButton);

		collapser.onOpen(buttonOpenHandler);
		collapser.onClose(buttonCloseHandler);

		// writable handlers

		if (isWritable) {
			var handlers = makeCollapsibleCellsHandlers(collapser);
			collapser.onOpen(handlers.open);
			collapser.onClose(handlers.close);
		}

		collapseButton.click(function() {
			$this = $(this);
			if (collapser.isOpen) {
				if (isEditing(collapser)) {
					$.squash.openMessage(language.infoTitle, language.collapseMessage);
				} else {
					collapser.closeAll();
				}
			} else {
				collapser.openAll();
			}
		});

		// end
		table.data('collapser', collapser);

	}

	// ******************************* main
	// *********************************

	function init(settings) {
		$.squash.decorateButtons();
		
		var language = settings.language;
		var urls = makeTableUrls(settings);
		var permissions = settings.permissions;

		// the js table
		initTable(settings);

		// toolbar
		if (permissions.isWritable) {
			initTableToolbar(language, urls);
		}

		// table collapser
		initCollapser(language, urls, permissions.isWritable);

	}

	return {
		init : init
	};

});