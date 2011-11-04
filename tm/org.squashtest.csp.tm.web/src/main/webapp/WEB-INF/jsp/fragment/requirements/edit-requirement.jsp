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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="requirementUrl" value="/requirements/{reqId}">
	<s:param name="reqId" value="${requirement.id}" />
</s:url>
<s:url var="getVerifyingTestCaseUrl" value="/requirements/${requirement.id}/verifying-test-cases-table" />

<s:url var="verifyingTCManagerUrl" value="/requirements/${ requirement.id }/verifying-test-cases-manager" /> 

<c:url var="verifyingTestCasesUrl" value="/requirements/${ requirement.id }/verifying-test-cases" />
<c:url var="nonVerifyingTestCasesUrl" value="/requirements/${ requirement.id }/non-verifying-test-cases" />
<c:url var="testCaseDetailsBaseUrl" value="/test-cases" />


<c:url var="workspaceUrl" value="/requirement-workspace/#" />
<s:url var="simulateDeletionUrl" value="/requirement-browser/delete-nodes/simulate" />
<s:url var="confirmDeletionUrl" value="/requirement-browser/delete-nodes/confirm" />



<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>
<%-- ----------------------------------- Init ----------------------------------------------%>



<div class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float:left;height:100%;">	
		<h2>
			<span><f:message key="requirement.header.title" />&nbsp;:&nbsp;</span>
			<c:set var="completeRequirementName" value="${ requirement.name }" />
			<c:if test="${not empty requirement.reference && fn:length(requirement.reference) > 0}" >
				<c:set var="completeRequirementName" value='${ requirement.reference } - ${ requirement.name }' />
			</c:if>
			<a id="requirement-name" href="${ requirementUrl }/info"><c:out value="${ completeRequirementName }" escapeXml="true"/></a>
			<%-- raw reference and name because we need to get the name and only the name for modification, and then re-compose the title with the reference  --%>
			<span id="requirement-raw-reference" style="display:none"><c:out value="${ requirement.reference }" escapeXml="true"/></span>
			<span id="requirement-raw-name" style="display:none"><c:out value="${ requirement.name }" escapeXml="true"/></span>
		</h2>
	</div>

	
	<div style="clear:both;"></div>	

	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
		<comp:popup id="rename-requirement-dialog" titleKey="dialog.rename-requirement.title" 
			isContextual="true" openedBy="rename-requirement-button">
			<jsp:attribute name="buttons">
				<f:message var="label" key="dialog.rename-requirement.title" />
				'${ label }': function() {
					var url = "${ requirementUrl }";
					<jq:ajaxcall 
						url="url"
						dataType="json"
						httpMethod="POST"
						useData="true"
						successHandler="renameRequirementSuccess"		>		
						<jq:params-bindings newName="#rename-requirement-input" />
					</jq:ajaxcall>					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<script type="text/javascript">
				$( "#rename-requirement-dialog" ).bind( "dialogopen", function(event, ui) {
					var name = $('#requirement-raw-name').text();
					$("#rename-requirement-input").val(name);
					
				});
				</script>
				<label><f:message key="dialog.rename.label" /></label>
				<input type="text" id="rename-requirement-input" /><br/>
				<comp:error-message forField="name"/>
			</jsp:body>
		</comp:popup>
	</authz:authorized>		
</div>

<div class="fragment-body">
	<div id="requirement-toolbar" class="toolbar-class ui-corner-all" >
		<div  class="toolbar-information-panel">
			<comp:general-information-panel auditableEntity="${requirement}" entityUrl="${ requirementUrl }" />
		</div>

		<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
			<div class="toolbar-button-panel">
				<input type="button" value='<f:message key="requirement.button.rename.label" />' id="rename-requirement-button" class="button"/> 
				<input type="button" value='<f:message key="requirement.button.remove.label" />' id="delete-requirement-button" class="button"/>		
			</div>	
		</authz:authorized>

		<div style="clear:both;"></div>			
	</div>

	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
		<comp:rich-jeditable targetUrl="${ requirementUrl }" componentId="requirement-description" />
		<%-- make requirement-reference editable --%>
		<%-- TODO put at end of page, maybe componentize --%>
		<comp:simple-jeditable targetUrl="${ requirementUrl }" componentId="requirement-reference" submitCallback="updateReferenceInTitle" maxLength="20" />
	</authz:authorized>

	<comp:toggle-panel id="requirement-information-panel" titleKey="requirement.panel.general-informations.title" isContextual="true" open="true" >
		<jsp:attribute name="body">
			<div id="edit-requirement-table" class="display-table">
				<div class="display-table-row">
					<label for="requirement-description" class="display-table-cell"><f:message key="requirement.description.label" /></label>
					<div class="display-table-cell" id="requirement-description">${ requirement.description }</div>
				</div>
				<div class="display-table-row">
					<label class="display-table-cell"  for="requirement-reference"><f:message key="requirement.reference.label" /></label>
					<div class="display-table-cell"  id="requirement-reference">${ requirement.reference }</div>
				</div>
				<div class="display-table-row">
					<label for="requirement-criticality" class="display-table-cell"><f:message key="requirement.criticality.combo.label" /></label>
					<div class="display-table-cell">
							<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
							<div id="requirement-criticality"><s:message code="requirement.criticality.${ requirement.criticality }" /></div>
							<comp:select-jeditable componentId="requirement-criticality" jsonData="${criticalityList}" targetUrl="${requirementUrl}" />
							</authz:authorized>
							<authz:notAuthorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
								<s:message code="requirement.criticality.${ requirement.criticality }" />
							</authz:notAuthorized>
						</div>
				</div>
			</div>
		</jsp:attribute>
	</comp:toggle-panel>

	<%--------------------------- verifying TestCase section ------------------------------------%>
	<script type="text/javascript">
		$(function(){
			$("#verifying-test-case-button").button().click(function(){
				document.location.href="${verifyingTCManagerUrl}" ;	
			});
		});
	</script>

	<comp:toggle-panel id="verifying-requirement-panel" titleKey="requirement.verifying_test-case.panel.title" open="true">
		<jsp:attribute name="panelButtons">
			<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
				<f:message var="associateLabel" key="requirement.verifying_test-case.manage.button.label"/>
				<f:message var="removeLabel" key="test-case.verified_requirement_item.remove.button.label"/>
				
				<input id="verifying-test-case-button" type="button" value="${associateLabel}"/>
				<input id="remove-verifying-test-case-button" type="button" class="button" value="${removeLabel}"/>
			</authz:authorized>
		</jsp:attribute>
	
		<jsp:attribute name="body">
			<aggr:decorate-verifying-test-cases-table nonVerifyingTestCasesUrl="${ nonVerifyingTestCasesUrl }" tableModelUrl="${ getVerifyingTestCaseUrl }" 
				verifyingTestCasesUrl="${ verifyingTestCasesUrl }" batchRemoveButtonId="remove-verifying-test-case-button"
				testCaseDetailsBaseUrl="${ testCaseDetailsBaseUrl }" editable="${ editable }" />
			<aggr:verifying-test-cases-table />
		</jsp:attribute>
	</comp:toggle-panel>

<%------------------------------ Attachments bloc ---------------------------------------------%> 

	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
		<c:set var="editable" value="${ true }" />
	</authz:authorized>
	<comp:attachment-bloc entity="${requirement}" workspaceName="requirement" editable="${ editable }" />
	
	<%--------------------------- Deletion confirmation popup -------------------------------------%>
	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
	
	<comp:delete-contextual-node-dialog simulationUrl="${simulateDeletionUrl}" confirmationUrl="${confirmDeletionUrl}" 
			itemId="${requirement.id}" successCallback="deleteRequirementSuccess" openedBy="delete-requirement-button" titleKey="dialog.delete-requirement.title"/>
	

	</authz:authorized>
</div>

<comp:decorate-buttons />

<script type="text/javascript">
	/* display the requirement name. Used for extern calls (like from the page who will include this fragment)
	*  will refresh the general informations as well*/
	function nodeSetname(name){
		$('#requirement-name').html(name);		
	}
	
	function updateRawNameHiddenField(name){
		$('#requirement-raw-name').html(name);
	}
	
	function composeRequirementName(rawName)
	{
		var toReturn = rawName;
		if($('#requirement-raw-reference').text().length > 0){
			toReturn = $('#requirement-raw-reference').text() + " - " + rawName;
		}
		return toReturn;
	}

	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ requirement }">
		/* renaming success handler */
		function renameRequirementSuccess(data){
			//Compose the real name
			var checkedName = composeRequirementName(data.newName);
			//update name in panel
			nodeSetname(checkedName);
			//update name in tree
			updateTreeDisplayedName(checkedName);
			//change also the node name attribute
			if (typeof updateSelectedNodeName == 'function'){
				updateSelectedNodeName(data.newName);	
			}
			//and the hidden raw name
			updateRawNameHiddenField(data.newName);
			$( '#rename-requirement-dialog' ).dialog( 'close' );
		}
		
		/*update only the displayed node name*/
		function updateTreeDisplayedName(newName){
			if (typeof renameSelectedNreeNode == 'function'){
				renameSelectedNreeNode(newName);
			}
		}
		
		/* renaming after reference update */
		function updateReferenceInTitle(reference){
			//update hidden reference
			var jqRawRef = $('#requirement-raw-reference');
			jqRawRef.html(reference);
			var newName = "";
			if(reference.length > 0)
				{
					newName += reference + " - ";
				}
			newName += $('#requirement-raw-name').text();
			//update name
			nodeSetname(newName);
			//update tree
			updateTreeDisplayedName(newName);
		}
		
		/* renaming failure handler */
		function renameRequirementFailure(xhr){
			$('#rename-requirement-dialog .popup-label-error')
			.html(xhr.statusText);		
		}
		
		/* deletion success handler */
		function deleteRequirementSuccess(){		
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
	</authz:authorized>
</script>
