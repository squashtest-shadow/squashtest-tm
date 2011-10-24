<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU Lesser General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU Lesser General Public License for more details.

        You should have received a copy of the GNU Lesser General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%-- used for copy/paste of steps --%>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.cookie.js"></script>



<%------------------------------------- URLs ----------------------------------------------%>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="testCaseUrl" value="/test-cases/{tcId}">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="testCaseInfoUrl" value="/test-cases/{tcId}/general">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="getStepsUrl" value="/test-cases/{tcId}/steps-table">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="addStepUrl" value="/test-cases/{tcId}/steps/add">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<c:url var="verifiedRequirementsTableUrl"
	value="/test-cases/${testCase.id}/all-verified-requirements-table" />
<s:url var="updateStepUrl" value="/test-cases/{tcId}/steps/">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="verifiedReqsManagerUrl" value="/test-cases/${ testCase.id }/verified-requirements-manager" />

<c:url var="verifiedRequirementsUrl" value="/test-cases/${ testCase.id }/verified-requirements" />
<c:url var="nonVerifiedRequirementsUrl" value="/test-cases/${ testCase.id }/non-verified-requirements" />

<s:url var="callStepManagerUrl" value="/test-cases/${ testCase.id }/call" />

<s:url var="stepAttachmentManagerUrl" value="/attach-list/">
</s:url>

<s:url var="callingtestCasesTableUrl" value="/test-cases/${testCase.id}/calling-test-case-table" />

<c:url var="workspaceUrl" value="/test-case-workspace/#" />
<s:url var="simulateDeletionUrl" value="/test-case-browser/delete-nodes/simulate" />
<s:url var="confirmDeletionUrl" value="/test-case-browser/delete-nodes/confirm" />



<%-- ----------------------------------- Authorization ----------------------------------------------%>

<%-- 
	if no variable 'editable' was provided in the context, we'll set one according to the authorization the user
	was granted for that object. 
--%>
<c:if test="${empty editable}">
	<c:set var="editable" value="${ false }" /> 
	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ testCase }">
		<c:set var="editable" value="${ true }" /> 
	</authz:authorized>
</c:if>
 
<%-- ----------------------------------- Init ----------------------------------------------%>


<script type="text/javascript">
	<%-- STEPS TABLE --%>
	$(function() {
		<%-- single step removal --%>
		$('#test-steps-table .delete-step-button').live('click', function() {
			$("#delete-step-dialog").data('opener', this).dialog('open');
		});

		$( "add-test-step-button" ).die('click');
	});
	
		
	
	function stepsTableRowCallback(row, data, displayIndex) {
		try{
		addIdToStepRow(row, data);
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getStepsTableRowId(data), 'delete-step-button');
		addClickHandlerToSelectHandle(row, $("#test-steps-table"));
		</c:if>
		addAttachmentButtonToRowDoV(row, getStepsTableAttchNumber(data), 'manage-attachment-button', 'manage-attachment-button-empty');
		addHLinkToCallStep(row, data);
		return row;
		}catch(wtf){
			alert(wtf);
		}
	}

	function stepsTableDrawCallback() {
		try{
		<c:if test="${ editable }">
		enableTableDragAndDrop('test-steps-table', getStepTableRowIndex, stepDropHandler);
		decorateDeleteButtons($('.delete-step-button', this));
		makeEditable(this);
		</c:if>
		restoreTableSelection(this, getStepsTableRowId);
		decorateAttachmentButtons($('.manage-attachment-button', this));
		decorateEmptyAttachmentButtons($('.manage-attachment-button-empty', this));
		}catch(wtf){
			alert(wtf);
		}
	}

	function addIdToStepRow(nRow, aData) {
		$(nRow).attr("id", "test-step:" + getStepsTableRowId(aData));
	}

	function parseStepId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function getStepsTableRowId(rowData) {
		return rowData[2];
	}
	
	function getStepTableRowIndex(rowData){
		return rowData[1];
	}
	
	function getStepsTableAttchNumber(rowData) {
		return rowData[8];
	}
	
	function isActionStep(rowData){
		return rowData[9]==="action";
	}
	
	function getCalledTCId(rowData){
		return rowData[10];
	}

	function stepDropHandler(rows, dropPosition) {
		var stepIds = $(rows).collect(function(elt){return elt.id.split(':')[1];});
		$.post('${ updateStepUrl }/move', { newIndex : dropPosition, stepIds : stepIds }, function() {
			refreshSteps();
		});
	}

	function refreshSteps() {
		
		var dataTable = $('#test-steps-table').dataTable();
		saveTableSelection(dataTable, getStepsTableRowId); 
		dataTable.fnDraw();
	}

	function objSteps(id, rank) {
		this.id = id;
		this.rank = rank;
	}
	
	
	function decorateStepTableButton(selector, cssclass){
		$(selector).button({
			text : false,
			icons : {
				primary : cssclass
			}
		});
	}
	


	$(function() {
		//overload the close handler
		$("#delete-all-steps-dialog").bind('dialogclose', function() {
			var answer = $("#delete-all-steps-dialog").data("answer");
			if (answer != "yes") {
				return;
			}

			var removeids = $("#delete-all-steps-dialog").data("IDList");


			$.ajax({
				type : 'POST',
				data : {
					removedStepIds : removeids
				},
				url : "${testCaseUrl}/removed-steps",
				success: refreshSteps 

			});

		});

		$("#delete-all-steps-button").click(function() {
			deleteSelectedSteps();
			return false;
		});

	});

	function deleteSelectedSteps(){
		
			var datatable = $("#test-steps-table").dataTable();
			var selectedIDs = getIdsOfSelectedTableRows(datatable,getStepsTableRowId);
			if (selectedIDs.length==0) return false;
			
			var removedStepIds = new Array();
			$("#delete-all-steps-dialog").data("answer","no");
			$("#delete-all-steps-dialog").data("IDList",selectedIDs);
				
			$("#delete-all-steps-dialog").dialog("open");
	}

		
	function makeEditable(dataTable){
		
		var rows = dataTable.fnGetNodes();
		/* check if the data is fed */
		if (rows.length==0) return;
		
		$(rows).each(function(){
			var data = dataTable.fnGetData(this);
			var tsId = data[2];	
			var columnAction = $("td.action-cell", this);
			var columnDescription = $("td.result-cell", this);
			
			if (isActionStep(data)){
				turnToEditable(columnAction,"action",tsId);
				turnToEditable(columnDescription,"result",tsId);
			}

			
		});

	}
	
	function turnToEditable(jqOColumn,strTarget,iid){

		var stUrl = "${ updateStepUrl }" + iid+"/"+strTarget; 
		
		var settings = {
			url : stUrl,
			ckeditor : { customConfig : '${ ckeConfigUrl }', language: '<f:message key="rich-edit.language.value" />' },		
			placeholder: '<f:message key="rich-edit.placeholder" />',
			submit: '<f:message key="rich-edit.button.ok.label" />',
			cancel: '<f:message key="rich-edit.button.cancel.label" />'	,
			indicator : '<img src="${ pageContext.servletContext.contextPath }/images/indicator.gif" alt="processing..." />' 
				
		};		
		jqOColumn.richEditable(settings);
	
	}
	
	function addHLinkToCallStep(row, data) {
		if (isActionStep(data)==false){
			var url= '${ pageContext.servletContext.contextPath }/test-cases/' + getCalledTCId(data) + '/info';			
			addHLinkToCellText($( 'td:eq(2)', row ), url);
		}
	}
	
	<%-- manage test step table toolbar buttons --%>
	$(function() {

		decorateStepTableButton("#copy-step", "ui-icon-clipboard" );
		decorateStepTableButton("#paste-step", "ui-icon-copy");
		decorateStepTableButton("#add-test-step-button", "ui-icon-plusthick");
		decorateStepTableButton("#add-call-step-button", "ui-icon-arrowthickstop-1-e");
		decorateStepTableButton("#delete-all-steps-button", "ui-icon-minusthick");
		
		
		<%-- note : until access to attachments manager is properly secured we'll forbid the access. --%>
		<c:if test="${editable}">
		$('#test-steps-table .has-attachment-cell a').live('click', function() {
			var listId = parseStepListId(this);
			document.location.href = "${stepAttachmentManagerUrl}" + listId + "/attachments/manager?workspace=test-case";
		});
		</c:if>
		
		<%-- copy/ paste button --%>
		
		$("#copy-step").bind('click', function(){
			var stepIds = getSelectedSteps();
			$.cookie('squash-test-step-ids', stepIds.toString(), {path:'/'} );			
		});
		
		
		$("#paste-step").bind('click', function(){
			var cookieIds = $.cookie('squash-test-step-ids');
			var stepIds = cookieIds.split(",");
				
			pasteSelectedSteps(stepIds);
		});
		
	});
	
	
	function pasteSelectedSteps(idList){
			
		if (idList.length==0){
			alert('<f:message key="test.case.steps.no.selection" />');
			return false;
		};

		var datatable = $("#test-steps-table").dataTable(); 
		
		var position =  getIdsOfSelectedTableRows(datatable,getStepsTableRowId);

		var data = new Object();
		data['copiedStepId']=idList;
		
		if (position.length>0){
			data['indexToCopy']=position[0];
		}
		
		$.ajax({
			type : 'POST',
			data : data,
			url : "${testCaseUrl}/steps/paste",
			success: refreshSteps 

		});
	}
	
	function getSelectedSteps(){
		var datatable = $("#test-steps-table").dataTable();
		var allSelectedIds = getIdsOfSelectedTableRows(datatable, getStepsTableRowId);
		
		if (allSelectedIds.length==0){
			alert('<f:message key="test.case.steps.no.selection" />');
			return false;
		}
		return allSelectedIds;
	}
	
	function parseStepListId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	<%-- /STEPS TABLE --%>
</script>
<c:if test="${ editable }">
<%-- ------------------------------ Add Test Step Dialog ------------------------------------------------ --%>
<comp:popup id="add-test-step-dialog" titleKey="dialog.add-test_step.title" isContextual="true"
	openedBy="add-test-step-button">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="dialog.button.add.label" />
		'${ label }': function() {
			var url = "${ addStepUrl }";
			<jq:ajaxcall url="url" dataType="json" httpMethod="POST" useData="true" successHandler="refreshSteps">		
				<jq:params-bindings action="#add-test-step-action" expectedResult="#add-test-step-result" />
			</jq:ajaxcall>					
		},			
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:body>
		<div class="centered" style="text-align: center; margin-bottom: 2em;">
			<label style="font-weight: bold;" for="add-test-step-action"><f:message key="dialog.add-test_step.actions.label" /></label>
			<textarea id="add-test-step-action"></textarea>
			<comp:error-message forField="action" />	
		</div>
		<div class="centered">
			<label style="font-weight: bold;" for="add-test-step-result"><f:message key="dialog.add-test_step.expected-results.label" /></label>
			<textarea id="add-test-step-result"></textarea>
		</div>

	</jsp:body>
</comp:popup>

<%------------------------ Test Step deletion dialogs ----------------------------------%>

<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
<comp:popup id="delete-step-dialog" titleKey="dialog.delete-step.title" isContextual="true"
	openedBy="delete-step-button">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="dialog.button.delete-step.label" />

		'${ label }': function() {
		
			var bCaller = $.data(this,"opener");
			var url = "${ updateStepUrl }" + parseStepId(bCaller); 
			<jq:ajaxcall url="url" dataType='json' httpMethod="DELETE" successHandler="refreshSteps">					
			</jq:ajaxcall>					
		},			
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:body>
		<b><f:message key="dialog.delete-step.message" /></b>
		<br />				
	</jsp:body>
</comp:popup>

<%--- multiple deletions --%>

<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
<comp:popup id="delete-all-steps-dialog" titleKey="dialog.delete-selected-steps.title" isContextual="true"
	openedBy="delete-step-button">
	<jsp:attribute name="buttons">
				
				<f:message var="yesLabel" key="dialog.button.confirm" />
				
				'${ yesLabel }' : function(){
						$("#delete-all-steps-dialog").data("answer","yes");
						$("#delete-all-steps-dialog").dialog("close");
				},
				
				<pop:cancel-button />
						
	</jsp:attribute>
	<jsp:body>
		<b><f:message key="dialog.delete-selected-steps.message" /></b>
		<br />				
	</jsp:body>
</comp:popup>
</c:if>


<%---------------------------- Test Case Header ------------------------------%>



<div id="test-case-name-div" class="ui-widget-header ui-corner-all ui-state-default fragment-header">

<div style="float: left; height: 100%;">
<h2><span><f:message key="test-case.header.title" />&nbsp;:&nbsp;</span><a id="test-case-name" href="${ testCaseUrl }/info"><c:out
	value="${ testCase.name }" escapeXml="true" /></a></h2>
</div>



<div style="clear: both;"></div>
</div>

<%---------------------------- Rename test case popup ------------------------------%>
<c:if test="${ editable }">
<comp:popup id="rename-test-case-dialog" titleKey="dialog.rename-test-case.title" isContextual="true"
	openedBy="rename-test-case-button">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="dialog.button.rename-tree-node.label" />
		'${ label }': function() {
			var url = "${ testCaseUrl }";
			<jq:ajaxcall url="url" dataType="json" httpMethod="POST" useData="true" successHandler="renameTestCaseSuccess">					
				<jq:params-bindings newName="#rename-test-case-input" />
			</jq:ajaxcall>					
		},			
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:body>
		<script type="text/javascript">
	$("#rename-test-case-dialog").bind("dialogopen", function(event, ui) {
		var name = $('#test-case-name').text();
		$("#rename-test-case-input").val(name);

	});
</script>
		<label><f:message key="dialog.rename.label" /></label>
		<input type="text" id="rename-test-case-input" />
		<br />
		<comp:error-message forField="name" />	

	</jsp:body>
</comp:popup>
</c:if>

<%---------------------------- Test Case Informations ------------------------------%>

<div class="fragment-body">

<div id="test-case-toolbar" class="toolbar-class ui-corner-all">
<div class="toolbar-information-panel">
<comp:general-information-panel auditableEntity="${testCase}" entityUrl="${ testCaseUrl }" />


<%-- There used to be an execution mode combobox right here. Search SCM if needed again --%>
</div>

<div class="toolbar-button-panel">
<c:if test="${ editable }">
	<input type="button" value='<f:message key="test-case.button.rename.label" />' id="rename-test-case-button" class="button" />
	<input type="button" value='<f:message key="test-case.button.remove.label" />' id="delete-test-case-button" class="button" />
</c:if>
	</div>
<div style="clear: both;"></div>
</div>

<%----------------------------------- Description -----------------------------------------------%>
<c:if test="${ editable }">
<comp:rich-jeditable
	targetUrl="${ testCaseUrl }" componentId="test-case-description" />
</c:if>
<comp:toggle-panel id="test-case-description-panel" titleKey="generics.description.title" isContextual="true" open="true">
	<jsp:attribute name="body">
		<div id="test-case-description" >${ testCase.description }</div>
	</jsp:attribute>
</comp:toggle-panel> 

<%----------------------------------- Test Step Table -----------------------------------------------%> 

<script type="text/javascript">
	$(function(){
		$("#add-call-step-button").click(function(){			
			var url = document.URL;
			$.cookie('call-step-manager-referer', url, {path:'/'});
			document.location.href = "${callStepManagerUrl}";			
		});
	});

</script>


<comp:toggle-panel id="test-case-steps-panel"
	titleKey="test-case.steps.table.title" open="true" isContextual="true">
	<jsp:attribute name="panelButtons">
	<c:if test="${ editable }">		
		<a id="add-test-step-button" class="button ui-icon test-step-toolbar-button" href="#"><f:message key="test-case.step.button.add.label" /></a>
		<a id="delete-all-steps-button" class="button ui-icon test-step-toolbar-button" href="#"><f:message key="test-case.step.button.remove.label" /></a>
		<a id="add-call-step-button" class="button ui-icon test-step-toolbar-button" href="#"><f:message key="test-case.step.button.call.label" /></a>
		
		<a id="copy-step"  class="button ui-icon test-step-toolbar-button" href="#"><f:message key="test-case.step.button.copy.label" /></a>
		<a id="paste-step" class="button ui-icon test-step-toolbar-button" href="#"><f:message key="test-case.step.button.paste.label" /></a>

	</c:if>
	</jsp:attribute>
	<jsp:attribute name="body">
		<comp:decorate-ajax-table url="${ getStepsUrl }" tableId="test-steps-table" paginate="true">		
			<jsp:attribute name="drawCallback">stepsTableDrawCallback</jsp:attribute>
			<jsp:attribute name="rowCallback">stepsTableRowCallback</jsp:attribute>
			<jsp:attribute name="columnDefs">

				<dt:column-definition targets="0, 2, 6" visible="false" sortable="false" />
				<dt:column-definition targets="1" sortable="false" cssClass="centered ui-state-default drag-handle select-handle"
					width="2em" />
				<dt:column-definition targets="3" sortable="false" width="2em" cssClass="centered has-attachment-cell" />
				<dt:column-definition targets="4" sortable="false" cssClass="action-cell" />
				<dt:column-definition targets="5" sortable="false" cssClass="result-cell" />
				<dt:column-definition targets="7" sortable="false" cssClass="centered" width="2em" />
				<dt:column-definition targets="8" sortable="false" visible="false"/>
				<dt:column-definition targets="9" sortable="false" visible="false" />
				<dt:column-definition targets="10" sortable="false" visible="false" lastDef="true" />
			</jsp:attribute>
		</comp:decorate-ajax-table>
		
		<table id="test-steps-table">
			<thead>
				<tr>
					<th>S</th>
					<th>#</th>
					<th>stepId(masked)</th>
					<th><f:message key="table.column-header.has-attachment.label" /></th>
					<th><f:message key="test-case.steps.table.column-header.actions.label" /></th>
					<th><f:message key="test-case.steps.table.column-header.expected-results.label" /></th>
					<th>M</th>
					<th>&nbsp;</th>
					<th>nbAttach(masked)</th>
					<th>stepNature(masked)</th>
					<th>calledStepId(masked)</th>	
				</tr>
			</thead>
			<tbody>
				<%-- Will be populated by ajax --%>
			</tbody>
		</table>
		
	 	<div id="test-step-row-buttons" class="not-displayed">
			<a id="delete-step-button" href="#" class="delete-step-button"><f:message key="test-case.step.delete.label" /></a>
			<a id="manage-attachment-button" href="#" class="manage-attachment-button"><f:message key="test-case.step.manage-attachment.label" /></a>
			<a id="manage-attachment-button-empty" href="#" class="manage-attachment-button-empty"><f:message key="test-case.step.add-attachment.label" /></a>
		</div>
	
	</jsp:attribute>
</comp:toggle-panel> 

<%--------------------------- Verified Requirements section ------------------------------------%> 

<script
	type="text/javascript">
	$(function() {
		$("#verified-req-button").button().click(function() {
			document.location.href = "${verifiedReqsManagerUrl}";
		});
	});
</script> 

<comp:toggle-panel id="verified-requirements-panel" titleKey="test-case.verified_requirements.panel.title" isContextual="true" open="true">
	<jsp:attribute name="panelButtons">
	<c:if test="${ editable }">
		<f:message var="associateLabel" key="test-case.verified_requirements.manage.button.label" />
		<input id="verified-req-button" type="button" value="${associateLabel}" class="button" />
		<f:message var="removeLabel" key="test-case.verified_requirement_item.remove.button.label" />
		<input id="remove-verified-requirements-button" type="button" value="${ removeLabel }" class="button" />
	</c:if>
	</jsp:attribute>

	<jsp:attribute name="body">
		<aggr:decorate-verified-requirements-table tableModelUrl="${ verifiedRequirementsTableUrl }"
			verifiedRequirementsUrl="${ verifiedRequirementsUrl }" batchRemoveButtonId="remove-verified-requirements-button"
			nonVerifiedRequirementsUrl="${ nonVerifiedRequirementsUrl }" editable="${ editable }" />
		<aggr:verified-requirements-table />
	</jsp:attribute>
</comp:toggle-panel> 


<%--------------------------- calling test case section ------------------------------------%> 

<%-- javascript for the calling test case table --%>
<script type="text/javascript">
	function addHLinkToCallingTestCasesName(row, data) {
		var url= '${ pageContext.servletContext.contextPath }/test-cases/' + data[0] + '/info';			
		addHLinkToCellText($( 'td:eq(2)', row ), url);
	}	
	
	function callingTestCasesTableRowCallback(row, data, displayIndex) {
		addClickHandlerToSelectHandle(row, $("#calling-test-case-table"));
		addHLinkToCallingTestCasesName(row, data);
		return row;
	}

</script>

<comp:toggle-panel id="calling-test-case-panel" titleKey="test-case.calling-test-cases.panel.title" isContextual="true" open="true">

	
	<jsp:attribute name="body">
		<%--<jsp:attribute name="drawCallback">todo</jsp:attribute>--%>
		<comp:decorate-ajax-table url="${ callingtestCasesTableUrl }" tableId="calling-test-case-table" paginate="true">		
			<jsp:attribute name="initialSort">[[3,'asc']]</jsp:attribute>
			<jsp:attribute name="rowCallback">callingTestCasesTableRowCallback</jsp:attribute>
			<jsp:attribute name="columnDefs">
				<dt:column-definition targets="0" visible="false" sortable="false" />
				<dt:column-definition targets="1" sortable="false" cssClass="centered select-handle" width="2em" />
				<dt:column-definition targets="2, 3" sortable="true" />
				<dt:column-definition targets="4" sortable="true" visible="true" lastDef="true" />
			</jsp:attribute>
		</comp:decorate-ajax-table>	
	
	
		<table id="calling-test-case-table">
			<thead>
				<tr>
					<th>Id(masked)</th>
					<th>#</th>
					<th><f:message key="test-case.calling-test-cases.table.project.label"/></th>
					<th><f:message key="test-case.calling-test-cases.table.name.label"/></th>
					<th><f:message key="test-case.calling-test-cases.table.execmode.label"/></th>				
				</tr>
			</thead>
			<tbody>
			
			</tbody>		
		</table>	
	</jsp:attribute>


</comp:toggle-panel>

<%------------------------------ Attachments bloc ---------------------------------------------%> 


<comp:attachment-bloc entity="${testCase}" workspaceName="test-case" editable="${ editable }" />


<%--------------------------- Deletion confirmation popup -------------------------------------%> 

<c:if test="${ editable }">


	<comp:delete-contextual-node-dialog simulationUrl="${simulateDeletionUrl}" confirmationUrl="${confirmDeletionUrl}" 
			itemId="${testCase.id}" successCallback="deleteTestCaseSuccess" openedBy="delete-test-case-button" titleKey="dialog.delete-test-case.title"/>

</c:if>


</div>

<comp:decorate-buttons />

<script type="text/javascript">
	/* display the test case name. Used for extern calls (like from the page who will include this fragment)
	 *  will refresh the general informations as well*/
	function nodeSetname(name) {
		$('#test-case-name').html(name);
	}

	/* renaming success handler */
	function renameTestCaseSuccess(data) {
		nodeSetname(data.newName);

		if (typeof renameSelectedNreeNode == 'function') {
			renameSelectedNreeNode(data.newName);
		}
		//change also the node name attribute
		if (typeof updateSelectedNodeName == 'function'){
			updateSelectedNodeName(data.newName);	
		}

		$('#rename-test-case-dialog').dialog('close');
	}

	/* renaming failure handler */
	function renameTestCaseFailure(xhr) {
		$('#rename-test-case-dialog .popup-label-error').html(xhr.statusText);
	}

	/* deletion success handler */
	function deleteTestCaseSuccess() {
		<c:choose>
		<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
		<c:when test="${param.isInfoPage}" >		
		document.location.href="${workspaceUrl}" ;
		</c:when>
		<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
		<c:otherwise>
		location.reload(true);
		</c:otherwise>
		</c:choose>		
	}

</script>



