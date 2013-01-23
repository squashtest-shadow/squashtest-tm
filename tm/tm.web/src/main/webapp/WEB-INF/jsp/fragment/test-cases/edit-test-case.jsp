<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="ta" tagdir="/WEB-INF/tags/testautomation"%>


<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%-- used for copy/paste of steps --%>
<script type="text/javascript"
	src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.cookie.js"></script>

<%------------------------------------- URLs ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="testCaseUrl" value="/test-cases/{tcId}">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="testCaseInfoUrl" value="/test-cases/{tcId}/general">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>

<c:url var="verifiedRequirementsTableUrl"
	value="/test-cases/${testCase.id}/all-verified-requirements-table" />
<s:url var="updateStepUrl" value="/test-cases/{tcId}/steps/">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="btEntityUrl" value="/bugtracker/test-case/{id}">
	<s:param name="id" value="${testCase.id}" />
</s:url>
<s:url var="verifiedReqsManagerUrl"
	value="/test-cases/${ testCase.id }/verified-requirement-versions/manager" />
<c:url var="verifiedRequirementsUrl"
	value="/test-cases/${ testCase.id }/verified-requirement-versions" />
<c:url var="nonVerifiedRequirementsUrl"
	value="/test-cases/${ testCase.id }/non-verified-requirement-versions" />

<s:url var="callingtestCasesTableUrl"
	value="/test-cases/${testCase.id}/calling-test-case-table" />
<c:url var="workspaceUrl" value="/test-case-workspace/#" />
<s:url var="simulateDeletionUrl"
	value="/test-case-browser/delete-nodes/simulate" />
<s:url var="confirmDeletionUrl"
	value="/test-case-browser/delete-nodes/confirm" />
<s:url var="getImportance" value="/test-cases/{tcId}/importance">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="getNature" value="/test-cases/{tcId}/nature">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="getType" value="/test-cases/{tcId}/type">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="getStatus" value="/test-cases/{tcId}/status">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<s:url var="importanceAutoUrl" value="/test-cases/{tcId}/importanceAuto">
	<s:param name="tcId" value="${testCase.id}" />
</s:url>
<c:url var="executionsTabUrl"
	value='/test-cases/${testCase.id}/executions'>
	<c:param name="tab" value="" />
</c:url>

<c:url var="customFieldsValuesURL" value="/custom-fields/values" />
<c:url var="stepTabUrl" value="/test-cases/${testCase.id}/steps/panel" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<%-- 
	if no variable 'editable' was provided in the context, we'll set one according to the authorization the user
	was granted for that object. 
--%>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE"
	domainObject="${ testCase }">
	<c:set var="writable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH"
	domainObject="${ testCase }">
	<c:set var="attachable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="SMALL_EDIT"
	domainObject="${ testCase }">
	<c:set var="smallEditable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE"
	domainObject="${ testCase }">
	<c:set var="deletable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE"
	domainObject="${ testCase }">
	<c:set var="creatable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"
	domainObject="${ testCase }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>


<%---------------------------- Test Case Header ------------------------------%>

<div id="test-case-name-div" 
	 class="ui-widget-header ui-corner-all ui-state-default fragment-header">

	<div style="float: left; height: 100%;">
		<h2>
			<span>
				<f:message key="test-case.header.title" />&nbsp;:&nbsp;
			</span>
			
			<c:set var="completeTestCaseName" value="${ testCase.fullName }" />
			<a id="test-case-name" href="${ testCaseUrl }/info">
				<c:out value="${ completeTestCaseName }" escapeXml="true" /> 
			</a>
			
			<%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
			<span id="test-case-raw-reference" style="display: none">
				<c:out value="${ testCase.reference }" escapeXml="true" /> 
			</span> 
			
			<span id="test-case-raw-name" style="display: none">
				<c:out value="${ testCase.name }" escapeXml="true" /> 
			</span>
		</h2>
	</div>

	<div style="clear: both;"></div>
</div>

<%---------------------------- Rename test case popup ------------------------------%>
<c:if test="${ smallEditable }">
	<comp:popup id="rename-test-case-dialog"
		titleKey="dialog.rename-test-case.title" isContextual="true"
		openedBy="rename-test-case-button">
		
		<jsp:attribute name="buttons">
	
			<f:message var="label" key="dialog.rename-test-case.title" />
			
			'${ label }': function() {
				var newName = $("#rename-test-case-input").val();
				$.ajax({
					url : "${testCaseUrl}",
					type : "POST",
					dataType : "json",
					data : { 'newName' : newName}
				}).success(renameTestCaseSuccess);			
			},
			
			<pop:cancel-button />
			
		</jsp:attribute>
		
		<jsp:body>
				<label>
					<f:message key="dialog.rename.label" />
				</label>
				<input type="text" id="rename-test-case-input" 
					   maxlength="255"	size="50" />
				<br />
				<comp:error-message forField="name" />
		</jsp:body>

	</comp:popup>
</c:if>

<%---------------------------- Test Case Informations ------------------------------%>

<div id="test-case-toolbar" classes="toolbar-class ui-corner-all">
	
	<div class="toolbar-information-panel">
		<comp:general-information-panel auditableEntity="${ testCase }"	entityUrl="${ testCaseUrl }" />
	</div>

	<div class="toolbar-button-panel">
	<c:if test="${ smallEditable }">
		<input type="button" value="<f:message key='test-case.button.rename.label' />"
				id="rename-test-case-button" class="button" />
	</c:if>
	<c:if test="${ deletable }">
		<input type="button" value="<f:message key='test-case.button.remove.label' />"
				id="delete-test-case-button" class="button" />
	</c:if>
	</div>
	<div style="clear: both;"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }"
							objectUrl="${ testCaseUrl }" 
							isContextual="${ ! param.isInfoPage }" />
	</c:if>

</div>


<%-- --------------------------------------- Test Case body --------------------------------------- --%>

<div class="fragment-tabs fragment-body">

	<%--  ------------------ main tab panel --------------------------------- --%>
	<ul>
		<li>
			<a href="#tabs-1"><f:message key="tabs.label.information" /></a>
		</li>
		<li>
			<a href="${stepTabUrl}"><f:message key="tabs.label.steps" /></a>
		</li>
		<li>
			<a href="#tabs-3"><f:message key="label.Attachments" />
			<c:if test="${ testCase.attachmentList.notEmpty }">
				<span class="hasAttach">!</span>
			</c:if> 
			</a>
		</li>
		<li>
			<a href="${executionsTabUrl}"><f:message key="label.executions" /> </a>
		</li>
	</ul>
	
	
	<%-- ------------------------- Description Panel ------------------------- --%>
	
	<div id="tabs-1">
		
		<c:if test="${ smallEditable }">
		<comp:rich-jeditable   targetUrl="${ testCaseUrl }" 
							   componentId="test-case-description" />
		
		<comp:simple-jeditable targetUrl="${ testCaseUrl }"	
							   componentId="test-case-reference"
							   submitCallback="updateReferenceInTitle" 
							   maxLength="50" />

		<comp:select-jeditable componentId="test-case-importance"
							   jsonData="${ testCaseImportanceComboJson }"
							   targetUrl="${ testCaseUrl }" />

		<comp:select-jeditable componentId="test-case-nature"
							   jsonData="${ testCaseNatureComboJson }" 
							   targetUrl="${ testCaseUrl }" />

		<comp:select-jeditable componentId="test-case-type"
							   jsonData="${ testCaseTypeComboJson }" 
							   targetUrl="${ testCaseUrl }" />

		<comp:select-jeditable componentId="test-case-status"
								jsonData="${ testCaseStatusComboJson }" 
								targetUrl="${ testCaseUrl }" />
		</c:if>


		<comp:toggle-panel id="test-case-description-panel"
						   titleKey="label.Description" 
						   isContextual="true" 
						   open="true">
						   
			<jsp:attribute name="body">
			<div id="test-case-description-table"  class="display-table">
				
				<div class="display-table-row">
					<label class="display-table-cell" for="test-case-id">ID</label>
					<div class="display-table-cell" id="test-case-id">${ testCase.id }</div>
				</div>
				
				<div class="display-table-row">
					<label for="test-case-description" class="display-table-cell"><f:message key="label.Description" /></label>
					<div class="display-table-cell" id="test-case-description">${ testCase.description }</div>
				</div>
				
				<div class="display-table-row">
					<label class="display-table-cell" for="test-case-reference"><f:message key="test-case.reference.label" /></label>
					<div class="display-table-cell" id="test-case-reference">${ testCase.reference }</div>
				</div>
				
				<div class="display-table-row">
					<label for="test-case-importance" class="display-table-cell"><f:message key="test-case.importance.combo.label" /></label>
					<div class="display-table-cell">
						<span id="test-case-importance">${testCaseImportanceLabel}</span>
						<c:if test="${ smallEditable }">
						<comp:select-jeditable-auto
								associatedSelectJeditableId="test-case-importance"
								url="${ importanceAutoUrl }"
								isAuto="${ testCase.importanceAuto }"
								paramName="importanceAuto" />
						</c:if>
					</div>
				</div>
				
				<div class="display-table-row">
					<label for="test-case-nature" class="display-table-cell"><f:message key="test-case.nature.combo.label" /></label>
					<div class="display-table-cell">
						<span id="test-case-nature">${ testCaseNatureLabel }</span>
					</div>
				</div>
				
				<div class="display-table-row">
					<label for="test-case-type" class="display-table-cell">
						<f:message key="test-case.type.combo.label" />
					</label>
					<div class="display-table-cell">
						<span id="test-case-type">${ testCaseTypeLabel }</span>
					</div>
				</div>
				
				<div class="display-table-row">
					<label for="test-case-status" class="display-table-cell"><f:message key="test-case.status.combo.label" /></label>
					<div class="display-table-cell">
						<span id="test-case-status">${ testCaseStatusLabel }</span>
					</div>
				</div>
				
				
				<%-- Test Automation structure --%>
				<c:if test="${testCase.project.testAutomationEnabled}">
				<ta:testcase-script-elt-structure testCase="${testCase}"
												  canModify="${writable}" 
												  testCaseUrl="${testCaseUrl}" />	
				</c:if>			
				<%--/Test Automation structure --%>
				
			</div>
			</jsp:attribute>
		</comp:toggle-panel>
		

		<%----------------------------------- Prerequisites -----------------------------------------------%>

		<c:if test="${ writable }">
		<comp:rich-jeditable targetUrl="${ testCaseUrl }"
					   		 componentId="test-case-prerequisite" />
		</c:if>

		<comp:toggle-panel id="test-case-prerequisite-panel"
						   titleKey="generics.prerequisite.title" 
						   isContextual="true"
							open="${ not empty testCase.prerequisite }">
			<jsp:attribute name="body">
				<div id="test-case-prerequisite-table" class="display-table">
					<div class="display-table-row">
						<div class="display-table-cell" id="test-case-prerequisite">${ testCase.prerequisite }</div>
					</div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>


		<%--------------------------- Verified Requirements section ------------------------------------%>

		<comp:toggle-panel id="verified-requirements-panel"
						   titleKey="test-case.verified_requirements.panel.title"
						   isContextual="true" 
						   open="true">
			<jsp:attribute name="panelButtons">
			<c:if test="${ linkable }">
				<f:message var="associateLabel"	key="test-case.verified_requirements.manage.button.label" />
				<input id="verified-req-button" type="button" value="${associateLabel}" class="button" />
				
				<f:message var="removeLabel" key="test-case.verified_requirement_item.remove.button.label" />
				<input id="remove-verified-requirements-button" type="button" value="${ removeLabel }" class="button" />
			</c:if>
			</jsp:attribute>

			<jsp:attribute name="body">
			<aggr:decorate-verified-requirements-table
						tableModelUrl="${ verifiedRequirementsTableUrl }"
						verifiedRequirementsUrl="${ verifiedRequirementsUrl }"
						batchRemoveButtonId="remove-verified-requirements-button"
						nonVerifiedRequirementsUrl="${ nonVerifiedRequirementsUrl }"
						editable="${ linkable }"
						updateImportanceMethod="refreshTCImportance" />
			<aggr:verified-requirements-table />
			</jsp:attribute>
		</comp:toggle-panel>


		<%--------------------------- calling test case section ------------------------------------%>


		<comp:toggle-panel id="calling-test-case-panel"
						   titleKey="test-case.calling-test-cases.panel.title"
						   isContextual="true" 
						   open="true">


			<jsp:attribute name="body">
				<table id="calling-test-case-table">
					<thead>
						<tr>
							<th>Id(masked)</th>
							<th>#</th>
							<th><f:message key="label.project" /></th>
							<th><f:message key="test-case.reference.label" /></th>
							<th><f:message key="label.Name" /></th>
							<th><f:message key="test-case.calling-test-cases.table.execmode.label" /></th>				
						</tr>
					</thead>
					<tbody>
						<%-- loaded via ajax --%>
					</tbody>		
				</table>	
			</jsp:attribute>


		</comp:toggle-panel>

	</div>
	
	<%-- ------------------------- /Description Panel ------------------------- --%>

	<%------------------------------ Attachments bloc ---------------------------------------------%>
	
	<comp:attachment-tab tabId="tabs-3" 
						 entity="${ testCase }"
						 editable="${ attachable }" />


	<%------------------------------ /Attachments bloc ---------------------------------------------%>

	<%------------------------------ bugs section -------------------------------%>
	<c:if test="${testCase.project.bugtrackerConnected }">
		<comp:issues-tab btEntityUrl="${ btEntityUrl }" />
	</c:if>

	<%------------------------------ /bugs section -------------------------------%>

</div>

<%--------------------------- Deletion confirmation popup -------------------------------------%>

<c:if test="${ deletable }">


	<comp:delete-contextual-node-dialog
		simulationUrl="${simulateDeletionUrl}"
		confirmationUrl="${confirmDeletionUrl}" itemId="${testCase.id}"
		successCallback="deleteTestCaseSuccess"
		openedBy="delete-test-case-button"
		titleKey="dialog.delete-test-case.title" />

</c:if>

<%-- ----------------------------------------- Remaining of the javascript initialization ----------------------------- --%>
	

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
	
	
	function refreshTCImportance(){
		$.ajax({
			type : 'GET',
			data : {},
			success : function(importance){refreshTCImportanceSuccess(importance);},
			error : function(){refreshTCImportanceFail();},
			dataType : "text",
			url : '${getImportance}'			
		})
		.success(function(importance){
			$("#test-case-importance").html(importance);	
		})
		.error(function(){
			$.squash.openMessage("<f:message key='popup.title.error' />", "fail to refresh importance");
		});
	}


	
	function deleteTestCaseSuccess() {
		<c:choose>
		<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
		<c:when test="${param['isInfoPage']}" >		
		document.location.href="${workspaceUrl}" ;
		</c:when>
		<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
		<c:otherwise>
		location.reload(true);
		</c:otherwise>
		</c:choose>		
	}
	


	function renameTestCaseSuccess(data){
		var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };
		var evt = new EventRename(identity, data.newName);
		squashtm.contextualContent.fire(null, evt);		
	};	
	
	function updateReferenceInTitle(newRef){
		var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };
		var evt = new EventUpdateReference(identity, newRef);
		squashtm.contextualContent.fire(null, evt);		
	};
	
	
	function beforeLoadTab(event, ui){
		if (document.getElementById("test-steps-tabs-panel") !== null){
			event.preventDefault();
			return false;
		}
	};

	
	$(function(){
		
		//init the rename popup
		$( "#rename-test-case-dialog" ).bind( "dialogopen", function(event, ui) {
			var name = $.trim($('#test-case-raw-name').text());
			$("#rename-test-case-input").val(name);
			
		});
		
		
		<c:if test="${hasCUF}">
		//load the custom fields
		$.get("${customFieldsValuesURL}?boundEntityId=${testCase.boundEntityId}&boundEntityType=${testCase.boundEntityType}")
		.success(function(data){
			$("#test-case-description-table").append(data);
		});
		</c:if>
		
		
		//init the requirements manager button
		$("#verified-req-button").button().click(function() {
			document.location.href = "${verifiedReqsManagerUrl}";
		});
		
		
		//init the renaming listener
		require(["jquery", "contextual-content-handlers"], function($, contentHandlers){
			
			var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };
			
			var nameHandler = contentHandlers.getNameAndReferenceHandler();
			
			nameHandler.identity = identity;
			nameHandler.nameDisplay = "#test-case-name";
			nameHandler.nameHidden = "#test-case-raw-name";
			nameHandler.referenceHidden = "#test-case-raw-reference";
			
			squashtm.contextualContent.addListener(nameHandler);
			
		});
		
	});

	
</script>


<comp:decorate-ajax-table url="${ callingtestCasesTableUrl }"
			tableId="calling-test-case-table" paginate="true">		
	<jsp:attribute name="initialSort">[[4,'asc']]</jsp:attribute>
	<jsp:attribute name="rowCallback">callingTestCasesTableRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false"  sortable="false" />
		<dt:column-definition targets="1" sortable="false" cssClass="centered select-handle" width="2em" />
		<dt:column-definition targets="2" sortable="true" />
		<dt:column-definition targets="3" sortable="true" width="15em" />
		<dt:column-definition targets="4" sortable="true" />
		<dt:column-definition targets="5" sortable="true" visible="true" lastDef="true" />
	</jsp:attribute>
</comp:decorate-ajax-table>		

		
<%-- Test Automation code --%>
<c:if test="${testCase.project.testAutomationEnabled}">
	<ta:testcase-script-elt-code testCase="${testCase}"
								 canModify="${writable}" 
								 testCaseUrl="${testCaseUrl}" />
</c:if>
<%-- /Test Automation code  --%>


<comp:fragment-tabs beforeLoad="beforeLoadTab" />

