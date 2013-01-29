/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
 * 	 basic : {
 * 		testCaseId : the id of the test case,
 * 		projectId : the id of the project this test case belongs to
 * 		rootContext : the root url
 * 		testCaseUrl : the baseTestCaseUrl
 * 	 },
 * 
 * 
 * 	permissions : {
 * 		isWritable : says whether the table content or structure can be modified by the user
 * 		isAttachable : says if you can attach attachments to the steps 
 * 	},
 * 
 * 	language : {
 * 		errorTitle : the title of the error popup
 * 		noStepSelected : the message when no steps where selected although some were needed
 * 		oklabel : the ok label for any confirmation popup
 * 		cancellabel : the cancellabel for any confirmation popup
 * 		deleteConfirm : the message for confirmation of deletion of the popup
 * 		deleteTitle : the tooltip for the delete popup buttons
 *   	infoTitle : the title for the popup that says close your widgets in edit mode
 *   	popupMessage : the content of that popup
 *   	btnExpand : the label of the expand button
 *   	btnCollapse : the label of the collapse button  
 *      addStepTitle : title for the add step popup
 *      addStep : label for the add step button
 *      addAnotherStep :  label for the add another step button
 *      ckeLang : the language for ckEditor
 *      placeholder : the placeholder title
 *      submit : the submit button value
 * 	}
 * 
 * }
 * 
 * 
 * 
 */

define(["jquery", "squash.table-collapser", "custom-field-values"], function($, TableCollapser, cufValuesManager){
	
	// ************************* configuration functions ************************************
	
	function makeTableUrls(conf){
		var tcUrl = conf.basic.testCaseUrl;
		var ctxUrl = conf.basic.rootContext;
		return {
			dropUrl :  		tcUrl  + "/steps/move",
			attachments : 	ctxUrl + "/attach-list/{attach-list-id}/attachments/manager?workspace=test-case",
			singleDelete :	tcUrl  + "/steps/{step-id}",
			multiDelete : 	tcUrl  + "/steps",
			callTC : 		ctxUrl + "/test-cases/{called-tc-id}/info",
			pasteStep : 	tcUrl  + "/steps",
			addStep : 		tcUrl  + "/steps/add",
			editActionUrl : tcUrl  + "/steps/{step-id}/action",
			editResultUrl : tcUrl  + "/steps/{step-id}/result",
			stepcufBindingUrl: ctxUrl + "/custom-fields-binding?projectId="+conf.basic.projectId+"&bindableEntity=TEST_STEP&optional=false",
			ckeConfigUrl : ctxUrl + "styles/ckeditor/ckeditor-config.js",
			indicatorUrl : ctxUrl + "/scripts/jquery/indicator.gif",
			callStepManagerUrl : tcUrl + "/call",
			tableLanguageUrl : ctxUrl + "/datatables/messages",
			tableAjaxUrl : tcUrl + "/steps-table"
		};		
	}
		
	
	// ************************* table configuration functions ******************************
	
	function refresh(){
		$("#test-steps-table").squashTable().refresh();
	}
	
	function removeStepSuccess(){
		alert('I should throw an event there so that I can say I need to refresh the requirement table (refreshStepsAndImportance)');
	}
	
	
	function stepsTableCreatedRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull){
		nRow.className += (aData['step-type']==="action") ?  " action-step-row" : " call-step-row";
	}
	
	function specializeCellClasses(table){
		
		var actionRows = table.find('tr.action-step-row');
		var callRows = table.find('tr.call-step-row');
		
		//remove useless classes for action step rows
		actionRows.find('td.called-tc-cell').removeClass('called-tc-cell');
		
		//remove useless classes for call step rows
		callRows.find('td.rich-edit-action').removeClass('rich-edit-action');
		callRows.find('td.rich-edit-result').removeClass('rich-edit-result');
		callRows.find('td.has-attachment-cell').removeClass('has-attachment-cell');
		callRows.find('td.custom-field-value').removeClass();	//remove all the classes
		callRows.find('td.called-tc-cell').next().remove().end().attr('colspan', 2);
	}
	
	function stepsTableDrawCallback() {

		//rework the td css classes to inhibit some post processing on them when not relevant
		specializeCellClasses(this);
		
		//collapser
		var collapser = this.data('collapser');
		if(collapser){
			collapser.refreshTable();
		}
		
	}


	function stepDropHandlerFactory(dropUrl){
		return function stepDropHandler(dropData) {
			$.post(dropUrl ,dropData, function(){
				refresh();
			});
		}
	}


	// ************************************ table initialization *****************************
	
	
	function initTable(settings){
		
		var language = settings.language,
			urls = makeTableUrls(settings),
			permissions = settings.permissions;
		
		
		var cufTableHandler = cufValuesManager.cufTableSupport;
		
		// first we must process the DOM table for cufs
		cufTableHandler.decorateDOMTable($("#test-steps-table"), settings.basic.cufDefinitions, 2);
			
		
		//now let's move to the datatable configuration
		//in order to enable/disable some features regarding the permissions, one have to tune the css classes of some columns.
		var editActionClass ="", editResultClass="", deleteClass="", dragClass="";
		

		if (permissions.isWritable){
			editActionClass="rich-edit-action";
			editResultClass="rich-edit-result";
			deleteClass="delete-button";
			dragClass="drag-handle";
		}
		
		//create the settings
		var datatableSettings = {
			oLanguage : {
				sUrl : urls.tableLanguageUrl
			},
			aaData : settings.basic.tableData,
			sAjaxSource : urls.tableAjaxUrl,
			fnDrawCallback : stepsTableDrawCallback,
			fnCreatedRow  : stepsTableCreatedRowCallback,
			iDeferLoading : settings.basic.initialRows,
			aoColumnDefs : [
			  {'bVisible':false, 'bSortable':false, 'aTargets':[0], 'mDataProp':'step-id'},
			  {'bVisible':true,  'bSortable':false, 'aTargets':[1], 'mDataProp':'step-index', 'sClass':'select-handle centered '+dragClass, 'sWidth':'2em'},
			  {'bVisible':true,  'bSortable':false, 'aTargets':[2], 'mDataProp':'attach-list-id', 'sClass':'centered has-attachment-cell', 'sWidth':'2em'},
			  {'bVisible':true,  'bSortable':false, 'aTargets':[3], 'mDataProp':'step-action', 'sClass':'called-tc-cell '+editActionClass},
			  {'bVisible':true,  'bSortable':false, 'aTargets':[4], 'mDataProp':'step-result', 'sClass': editResultClass},
			  {'bVisible':false, 'bSortable':false, 'aTargets':[5], 'mDataProp':'nb-attachments'},
			  {'bVisible':false, 'bSortable':false, 'aTargets':[6], 'mDataProp':'step-type'},
			  {'bVisible':false, 'bSortable':false, 'aTargets':[7], 'mDataProp':'called-tc-id'},
			  {'bVisible':true,  'bSortable':false, 'aTargets':[8], 'mDataProp':'empty-delete-holder', 'sClass':'centered '+deleteClass, 'sWidth':'2em'}
			]
			
		};
		
		//decorate the settings with the cuf values support
		datatableSettings = cufTableHandler.decorateTableSettings(datatableSettings, settings.basic.cufDefinitions, 2);
		
		
		var squashSettings = {
			
			dataKeys : {
				entityId : 'step-id' ,
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
				list : [
				   {
					   targetClass : 'called-tc-cell',
					   url : urls.callTC
				   }
				]
			}
		};
		
		if (permissions.isWritable){
			
			var moreSettings = {
				
				enableDnD : true,
				
				deleteButtons : {
					url : urls.singleDelete,
					popupmessage : language.deleteSingleConfirm,
					tooltip : language.deleteTitle,
					success : refresh
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
			
		};
		
		if (permissions.isAttachable){
			squashSettings.attachments = {
					url : urls.attachments
			}
		}
		
		$("#test-steps-table").squashTable(datatableSettings, squashSettings);
		
	}
	
	
	// ************************************ toolbar utility functions *************************
	
	function decorateStepTableButton(selector, cssclass){
		$(selector).button({
			icons : {
				primary : cssclass
			}
		});
	}

	
	
	
	// *************************** add step popup ***************************
	
	function addTestStepSuccess(){
		var dialog = $("#add-test-step-dialog");
		if (dialog.dialog("isOpen")==true){
			dialog.dialog('close');
		}
		refresh();
	}


	function addTestStepSuccessAnother(){
		CKEDITOR.instances["add-test-step-action"].setData('');
		CKEDITOR.instances["add-test-step-result"].setData('');
		
		var dialog = $("#add-test-step-dialog");
		dialog.data('cuf-values-support').reset();
		
		refresh();
	}

	function readAddStepParams(){
		
		var cufSupport = $("#add-test-step-dialog").data('cuf-values-support');
		
		var params = {};
		params.action = $("#add-test-step-action").val();
		params.expectedResult = $("#add-test-step-result").val();	
		$.extend(params,cufSupport.readValues());
		
		return params;
		
	}
	
	function initAddTestStepDialog(language, urls){

		decorateStepTableButton("#add-test-step-button", "ui-icon-plusthick");
		
		
		function postStep(data){
			return $.ajax({
				url : urls.addStep,
				type : 'POST',
				data : data,
				dataType : 'json'
			});
		}
		
		//main popup definition
		
		//TODO : the handlers
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
			buttons : [
			   {
				   'text' : language.addStep,
				   'click' : function(){
					   var data = readAddStepParams();
					   postStep(data).success(addTestStepSuccess);
				   }
			   },
			   {
				   'text' : language.addAnotherStep,
				   'click' : function(){
					   var data = readAddStepParams();
					   postStep(data).success(addTestStepSuccessAnother);
				   }
			   },
			   {
				   'text' : language.cancellabel,
				   'click' : function(){
					   $("#add-test-step-dialog").dialog('close');
				   }
			   }
			]
			
		};
		
		squashtm.popup.create(params);
		
		//cuf value support

		var dialog = $("#add-test-step-dialog");
		var cufTable = $("#add-test-step-custom-fields");
		var bindingsUrl = urls.stepcufBindingUrl;
		
		var cufValuesSupport = cufValuesManager.newCreationPopupCUFHandler({url : bindingsUrl, table : cufTable});
		cufValuesSupport.reloadPanel();
		dialog.data('cuf-values-support', cufValuesSupport);
		
		dialog.on('dialogopen', function(){
			cufValuesSupport.reset();
		});
	}
	
	
	// ************************* other buttons code **********************************

	function initStepCopyPastaButtons(language, urls){
		
		var table = $("#test-steps-table").squashTable();
		
		decorateStepTableButton("#copy-step", "ui-icon-clipboard" );
		decorateStepTableButton("#paste-step", "ui-icon-copy");
		
		$("#copy-step").bind('click', function(){
			var stepIds = table.getSelectedIds();
			if (stepIds.length==0){
				$.squash.openMessage( language.errorTitle, language.noStepSelected );
			}
			else{
				$.cookie('squash-test-step-ids', stepIds.toString(), {path:'/'} );
			}
			
		});
		
		
		$("#paste-step").bind('click', function(){
			var cookieIds = $.cookie('squash-test-step-ids');
			var stepIds = cookieIds.split(",");
			
			try{
				if (stepIds.length==0){
					throw language.noStepSelected;
				}
			
				var position =  table.getSelectedIds();
		
				var data = {};
				data['copiedStepId']=stepIds;
		
				var pasteUrl = urls.pasteStep;
				
				if (position.length>0){
					data['indexToCopy']=position[0];
					pasteUrl = pasteUrl + "/paste";
				} else {
					pasteUrl = pasteUrl + "/paste-last-index";
				}
				
				$.ajax({
					type : 'POST',
					data : data,
					url : pasteUrl,
					dataType : "json", 
					success: refresh
				});
				
				$("#paste-step").removeClass('ui-state-focus');
			}
			catch(damn){
				$.squash.openMessage( language.errorTitle, damn );
			}
		});		
	}
	
	
	function initDeleteAllStepsButtons(language, urls){

		decorateStepTableButton("#delete-all-steps-button", "ui-icon-minusthick");
		
		$("#delete-all-steps-button").bind('click', function(){
			var table = $("#test-steps-table").squashTable();		
			var ids = table.getSelectedIds();
			
			if (ids.length==0){
				$.squash.openMessage(language.errorTitle, language.noStepSelected );
			}
			else{
				oneShotConfirm(language.deleteTitle, 
							   language.deleteMultipleConfirm, 
							   language.oklabel,
							   language.cancellabel
				).done(function(){
					$.ajax({
						url : urls.multiDelete+"/"+ids.join(','),
						type : 'DELETE',
						dataType : "json"
					}).success(refresh);
				});
			}						
		});
	}
	
	function initCallStepButton(urls){

		decorateStepTableButton("#add-call-step-button", "ui-icon-arrowthickstop-1-e");
		
		$("#add-call-step-button").click(function(){			
			var url = document.URL;
			$.cookie('call-step-manager-referer', url, {path:'/'});
			document.location.href = urls.callStepManagerUrl;			
		});
	}
	
	// ******************************* toolbar initialization *********************************
	
	function initTableToolbar(language, urls){
		
		
		//copy pasta buttons
		initStepCopyPastaButtons(language, urls);
		
		//delete all button
		initDeleteAllStepsButtons(language, urls);
		
		//call step button
		initCallStepButton(urls);
		
		//add test step
		initAddTestStepDialog(language, urls);
		
	};
	
	
	// ************************************* table collapser code ****************************
	
	function oneCellIsInEditingState(){
		var collapsibleCells = this.collapsibleCells;
		for(var k = 0; k < collapsibleCells.length ; k++){
			if(collapsibleCells[k].editing){
				return  true;
			}
		}		
		return false;
	}
	
	function collapseCloseHandle(){
		var collapsibleCells = $(this.collapsibleCells);
		collapsibleCells.editable('disable');
		collapsibleCells.removeClass('editable');
		collapsibleCells.bind("click", this.openAllAndSetEditing);
	}
	
	function openAllAndSetEditing(eventObject){
		this.openAll();
		setTimeout(function() {
			$(eventObject.target).click();
		 }, 500);
	}
	
	function collapseOpenHandle(){
		var collapsibleCells = $(this.collapsibleCells);
		collapsibleCells.editable('enable');
		collapsibleCells.addClass('editable');
		collapsibleCells.unbind("click", this.openAllAndSetEditing);
	}
	
	function initCollapser(language, urls){
		

		decorateStepTableButton("#collapse-steps-button", "ui-icon-zoomout");
		
		var collapser;
		
		var collapseButton = $('#collapse-steps-button');		
		var table = $('#test-steps-table');
		
		//enrich the collapser prototype with more methods
		
		TableCollapser.prototype.oneCellIsInEditingState = oneCellIsInEditingState;
		TableCollapser.prototype.collapseCloseHandle = collapseCloseHandle;
		TableCollapser.prototype.openAllAndSetEditing = openAllAndSetEditing;
		TableCollapser.prototype.collapseOpenHandle = collapseOpenHandle;
		
		//begin
		
		var cellSelector = function(row){
			return $(row).find('td.rich-edit-action').add('td.rich-edit-result', row).get();
		}
		
		collapser = new TableCollapser(table, cellSelector); 
		collapser.onClose.addHandler(collapser.collapseCloseHandle);
		collapser.onOpen.addHandler(collapser.collapseOpenHandle);	
		
		collapseButton.click(function(){
			if(collapser.isOpen){
				if(collapser.oneCellIsInEditingState()){
					$.squash.openMessage(language.infoTitle, language.collapseMessage);
				}else{
					collapser.closeAll();
					decorateStepTableButton("#collapse-steps-button", "ui-icon-zoomin");
					$("#collapse-steps-button").attr('title', language.btnExpand);
					$("#collapse-steps-button").button({label: language.btnExpand});
				}
			}else{
				collapser.openAll();
				decorateStepTableButton("#collapse-steps-button", "ui-icon-zoomout");
				$("#collapse-steps-button").attr('title', language.btnCollapse);
				$("#collapse-steps-button").button({label:language.btnCollapse});
			}
		});
		
		//end
		table.data('collapser', collapser);
		
	 }	

	
	// ******************************* main *********************************
	
	function init(settings){

		var language = settings.language;
		var urls = makeTableUrls(settings);
		
		// the js table
		initTable(settings);
		
		// toolbar 
		if (settings.permissions.isWritable){
			initTableToolbar(language, urls);
		}
		
		// table collapser		
		initCollapser(language, urls);
		
	};
	
	
	return {
		init : init
	};
	

})