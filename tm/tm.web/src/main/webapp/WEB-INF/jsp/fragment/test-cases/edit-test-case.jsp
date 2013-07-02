<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

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
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="ta" tagdir="/WEB-INF/tags/testautomation"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components"%>


<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%-- used for copy/paste of steps --%>
<script type="text/javascript"	src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.cookie.js"></script>

<%------------------------------------- URLs ----------------------------------------------%>

<c:url var="workspaceUrl" 					value="/test-case-workspace/#" />

<c:url var="testCaseUrl" 					value="/test-cases/${testCase.id}"/>
<c:url var="verifiedRequirementsUrl" 		value="/test-cases/${testCase.id }/verified-requirement-versions"/>
<c:url var="verifiedRequirementsTableUrl"	value="/test-cases/${testCase.id}/verified-requirement-versions?includeCallSteps=true" />
<c:url var="btEntityUrl" 					value="/bugtracker/test-case/${testCase.id}"/>
<c:url var="getImportance" 					value="/test-cases/${testCase.id}/importance"/>
<c:url var="executionsTabUrl"				value="/test-cases/${testCase.id}/executions?tab="/>
<c:url var="customFieldsValuesURL" 			value="/custom-fields/values" />
<c:url var="stepTabUrl"						value="/test-cases/${testCase.id}/steps/panel" />

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

<tc:test-case-header testCase="${testCase}" />


<%---------------------------- Test Case Informations ------------------------------%>

<tc:test-case-toolbar testCase="${testCase}" isInfoPage="${param.isInfoPage}" otherViewers="${otherViewers}"   
					  moreThanReadOnly="${moreThanReadOnly}"  smallEditable="${smallEditable}" deletable="${deletable}" />

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
			<a href="${testCaseUrl}/parameters/panel"><f:message key="label.parameters" /></a>
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
		
		<tc:test-case-description 	testCase="${testCase}" 
									testCaseNatureComboJson="${testCaseNatureComboJson}"
									testCaseImportanceLabel="${testCaseImportanceLabel}"
									testCaseImportanceComboJson="${testCaseImportanceComboJson}" 
									testCaseTypeComboJson="${testCaseTypeComboJson}"
									testCaseTypeStatusJson="${testCaseTypeStatusJson}"
									writable="${writable}"
									smallEditable="${smallEditable}"/>
		

		<%----------------------------------- Prerequisites -----------------------------------------------%>

		<c:if test="${ writable }">
		<comp:rich-jeditable targetUrl="${ testCaseUrl }" componentId="test-case-prerequisite" />
		</c:if>

		<comp:toggle-panel id="test-case-prerequisite-panel" titleKey="generics.prerequisite.title" 
						   isContextual="true" open="${ not empty testCase.prerequisite }">
			<jsp:attribute name="body">
				<div id="test-case-prerequisite-table" class="display-table">
					<div class="display-table-row">
						<div class="display-table-cell" id="test-case-prerequisite">${ testCase.prerequisite }</div>
					</div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>


		<%--------------------------- Verified Requirements section ------------------------------------%>
		<tc:test-case-verified-requirement-bloc linkable="${ linkable }" verifiedRequirementsTableUrl="${ verifiedRequirementsTableUrl }" verifiedRequirementsUrl="${verifiedRequirementsUrl }" containerId="contextual-content"/>


		<%--------------------------- calling test case section ------------------------------------%>

		<tc:calling-test-cases-panel testCase="${testCase}" model="${callingTestCasesModel}"/>


	</div>
	
	<%-- ------------------------- /Description Panel ------------------------- --%>
	<%------------------------------ Attachments  ---------------------------------------------%>	
	
	<at:attachment-tab tabId="tabs-3"  entity="${ testCase }"  editable="${ attachable }" tableModel="${attachmentsModel}"/>
	
	<%------------------------------ /Attachments  ---------------------------------------------%>


</div>

<%-- ----------------------------------------- Remaining of the javascript initialization ----------------------------- --%>
	

<f:message key="tabs.label.issues" var="tabIssueLabel"/>
<script type="text/javascript">

	function refreshTCImportance(){
		$.ajax({
			type : 'GET',
			data : {},
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

	
	$(function(){
		
		//init the rename popup
		$( "#rename-test-case-dialog" ).bind( "dialogopen", function(event, ui) {
			var name = $.trim($('#test-case-raw-name').text());
			$("#rename-test-case-input").val(name);
			
		});
		
		$("#contextual-content").on("verifiedrequirementversions.refresh", refreshTCImportance);
		
		
	
		
		//init the renaming listener
		require(["jquery", "contextual-content-handlers", "jquery.squash.fragmenttabs", "bugtracker", "jqueryui"], function($, contentHandlers, Frag, bugtracker){
			
			var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };
			
			var nameHandler = contentHandlers.getNameAndReferenceHandler();
			
			nameHandler.identity = identity;
			nameHandler.nameDisplay = "#test-case-name";
			nameHandler.nameHidden = "#test-case-raw-name";
			nameHandler.referenceHidden = "#test-case-raw-reference";
			
			squashtm.contextualContent.addListener(nameHandler);
			
			//****** tabs configuration *******
			
			var fragConf = {
				beforeLoad : Frag.confHelper.fnCacheRequests	
			};
			Frag.init(fragConf);
			
			<c:if test="${testCase.project.bugtrackerConnected }">
			bugtracker.btPanel.load({
				url : "${btEntityUrl}",
				label : "${tabIssueLabel}"
			});
			</c:if>
			
		});
		
		//**** cuf sections ************
		
		<c:if test="${hasCUF}">
		//load the custom fields
		$.get("${customFieldsValuesURL}?boundEntityId=${testCase.boundEntityId}&boundEntityType=${testCase.boundEntityType}")
		.success(function(data){
			$("#test-case-description-table").append(data);
		});
		</c:if>
		
		
		//************** other *************
		
		$("#rename-test-case-button").squashButton();
		$("#delete-test-case-button").squashButton();
		$("#print-test-case-button").squashButton();

		$("#print-test-case-button").click(function(){
			window.open("${testCaseUrl}?format=printable", "_blank");
		});
	});

	
</script>

<%-- ===================================== popups =============================== --%>

<tc:test-case-popups testCase="${testCase}" smallEditable="${smallEditable}" deletable="${deletable}" />

<%-- ===================================== /popups =============================== --%>
		
<%-- Test Automation code --%>
<c:if test="${testCase.project.testAutomationEnabled}">
	<tc:testcase-script-elt-code testCase="${testCase}"
								 canModify="${writable}" 
								 testCaseUrl="${testCaseUrl}" />
</c:if>
<%-- /Test Automation code  --%>



