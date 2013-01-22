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
			singleDelete :	tcUrl  + "/steps/{entity-id}",
			multiDelete : 	tcUrl  + "/steps",
			callTC : 		ctxUrl + "/test-cases/{called-tc-id}/info",
			pasteStep : 	tcUrl  + "/steps",
			editActionUrl : tcUrl  + "/steps/{step-id}/action",
			editResultUrl : tcUrl  + "/steps/{step-id}/result",
			stepcufBindingUrl: ctxUrl + "/custom-fields-binding?projectId="+conf.basic.projectId+"&bindableEntity=TEST_STEP&optional=false",
			ckeConfigUrl : ctxUrl + "/styles/ckeditor/ckeditor-config.js",
			indicatorUrl : ctxUrl + "/scripts/jquery/indixator.gif",
			callStepManagerUrl : tcUrl + "/call"
		}		
	}
	
	
	
	
	// ************************* table configuration functions ******************************
	
	function refresh(){
		$("#test-steps-table").squashTable().refresh();
	}
	
	function removeStepSuccess(){
		alert('I should throw an event there so that I can say I need to refresh the requirement table (refreshStepsAndImportance)')
	}
	
	function stepsTableDrawCallback() {
		var collapser = $("#test-steps-table").data('collapser');
		if(collapser){
			collapser.refreshTable();
		}
	}

	function isActionStep(rowData){
		return rowData['step-type']==="action";
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
		
		
		
	}
	
	
	
	// ************************************ toolbar utility functions *************************
	
	function decorateStepTableButton(selector, cssclass){
		$(selector).button({
			icons : {
				primary : cssclass
			}
		});
	}


	function initStepCopyPastaButtons(language, urls){
		
		var table = $("#test-steps-table").squashTable();
		
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
				if (idList.length==0){
					throw language.noStepSelected;
				}
			
				var position =  table.getSelectedIds();
		
				var data = {};
				data['copiedStepId']=idList;
		
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

		var table = $("#test-steps-table").squashTable();		
		var ids = table.getSelectedIds();
		
		if (ids.length==0){
			$.squash.openMessage(language.errorTitle, language.noStepSelected );
		}
		else{
			oneShotConfirm(language.deleteTitle, 
						   language.deleteConfirm, 
						   language.oklabel,
						   language.cancellabel
			).done(function(){
				$.ajax({
					url : urls.multiDelete+"/"+ids.join(',')
					type : 'DELETE',
					dataType : "json"
				});
			});
		}
	}
	
	function initCallStepButton(urls){
		$("#add-call-step-button").click(function(){			
			var url = document.URL;
			$.cookie('call-step-manager-referer', url, {path:'/'});
			document.location.href = urls.callStepManagerUrl;			
		});
	}
	
	// ******************************* toolbar initialization *********************************
	
	function initTableToolbar(language, urls){
		
		decorateStepTableButton("#copy-step", "ui-icon-clipboard" );
		decorateStepTableButton("#paste-step", "ui-icon-copy");
		decorateStepTableButton("#add-test-step-button", "ui-icon-plusthick");
		decorateStepTableButton("#add-call-step-button", "ui-icon-arrowthickstop-1-e");
		decorateStepTableButton("#delete-all-steps-button", "ui-icon-minusthick");
		decorateStepTableButton("#collapse-steps-button", "ui-icon-zoomout");
		
		//copy pasta buttons
		initStepCopyPastaButtons(language, urls);
		
		//delete all button
		initDeleteAllStepsButtons(language, urls);
		
		//call step button
		initCallStepButton(urls);
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
	
	function initCollapser(settings){
		
		var collapser;
		var language = settings.language;
		
		var collapseButton = $('#collapse-steps-button');		
		var table = $('#test-steps-table');
		
		//enrich the collapser prototype with more methods
		
		TableCollapser.prototype.oneCellIsInEditingState = oneCellIsInEditingState;
		TableCollapser.prototype.collapseCloseHandle = collapseCloseHandle;
		TableCollapser.prototype.openAllAndSetEditing = openAllAndSetEditing;
		TableCollapser.prototype.collapseOpenHandle = collapseOpenHandle;
		
		//begin
		
		var columns = [2,3];
		collapser = new TableCollapser(table, columns); 
		collapser.onClose.addHandler(collapser.collapseCloseHandle);
		collapser.onOpen.addHandler(collapser.collapseOpenHandle);	
		//collapser.bindButtonToTable(collapseButton);
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
		
		return collapser;
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
	
	function initAddStepDialog(language, urls){

		
		//main popup definition
		
		//TODO : the handlers
		var params = {
			selector : "#add-test-step-dialog",
			openedBy : "#add-test-step-button",
			title : language.title,
			isContextual : true,
			usesRichEdit : true,
			closeOnSuccess : false,
			ckEditor : {
				styleUrl : urls.ckeConfigUrl,
				lang : language.ckeLang
			},
			buttons : [
			   {
				   'text' : language.addStep
			   },
			   {
				   'text' : language.addAnother
			   },
			   {
				   'text' : language.cancellabel
			   }
			]
			
		};
		

		//cuf value support

		var dialog = $("#add-test-step-dialog");
		var cufTable = $("#add-test-step-custom-fields");
		var bindingsUrl = urls.stepcufBindingUrl;
		
		var cufValuesSupport = cufValuesManager.newCUFValuesCreator({url : bindingsUrl, table : cufTable});
		cufValuesSupport.reloadPanel();
		dialog.data('cuf-values-support', cufValuesSupport);
		
		dialog.on('dialogopen', function(){
			cufValuesSupport.reset();
		});
	}
	
	
	// ******************************* main *********************************
	
	function init(settings){
		
		
		// toolbar 
		
		
		// table collapser
		
		var collapserSettings = settings.collapser;
		var collapser = initCollapser(collapserSettings);
		
		
		// various event bindings
		
		$('#test-steps-table .delete-step-button').live('click', function() {
			$("#delete-step-dialog").data('opener', this).dialog('open');
		});
		
	}
	
	
	return {
		init : init
	}
	

})