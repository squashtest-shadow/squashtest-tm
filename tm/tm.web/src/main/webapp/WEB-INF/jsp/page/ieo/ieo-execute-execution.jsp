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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ execution }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="execute-html">

<c:choose>
<c:when test="${totalSteps == 0 }">
	<span><f:message key="execute.header.nostep.label"/></span>
</c:when>
<c:otherwise>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Exec #${execution.executionOrder + 1 } - ${execution.name} (${executionStep.executionStepOrder +1}/${totalSteps})</title>
	
	<layout:common-head />		
	<layout:_common-script-import highlightedWorkspace=""/>		

	<comp:rich-jeditable-header />
	

<%-- cautious : below are used StepIndexes and StepIds. Dont get confused. --%>
<s:url var="executeComment" value="/execute/{execId}/step/{stepId}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepId" value="${executionStep.id}" />
	<s:param name="ieo" value="true"/>
</s:url>

	<comp:sq-css name="squash.purple.css" />	
</head>

<s:url var="btEntityUrl" value="/bugtracker/execution-step/{id}?useDelegatePopup=true" >
	<s:param name="id" value="${executionStep.id}"/>
</s:url>


<body class="execute-html-body ieo-body">
	<f:message var="completedMessage" key="execute.alert.test.complete" />
	<c:if test="${ execution.project.bugtrackerConnected }">
	<c:choose>
	<c:when test="${ execution.bugTracker.iframeFriendly } }">
		<c:set var="bugLinkTarget" value="frameright" />
	</c:when>
	<c:otherwise>
		<c:set var="bugLinkTarget" value="_blank" />
		</c:otherwise>
	</c:choose>
	</c:if>
	<script type="text/javascript">
		
		$(function(){
			
			
			$("#execute-next-button").button({
				'text': false,
				'disabled': ${ not hasNextStep },
				icons: {
					primary : 'ui-icon-triangle-1-e'
				}
			}).click(function(){
				parent.squashtm.ieomanager.navigateNext();
			});	
		
			$("#execute-previous-button").button({
				'text' : false,
				icons : {
					primary : 'ui-icon-triangle-1-w'
				}
			}).click(function(){
				parent.squashtm.ieomanager.navigatePrevious();
			});
		
			$("#execute-stop-button").button({
				'text': false, 
				'icons' : {
					'primary' : 'ui-icon-power'
				} 
			}).click(function(){
				parent.squashtm.ieomanager.closeWindow();
			});			

			
			$("div.load-links-right-frame a").live('click', function(event){
				event.preventDefault();
				var url = $(this).attr('href'); 
				parent.squashtm.ieomanager.fillRightPane(url);
				return false;				
			});
			
			$("#bugtracker-section-div a").live('click', function(){
				$(this).attr('target', "${bugLinkTarget}");
			});			

			$("#execute-next-test-case").button({
				'text': false,
				'disabled': ${ (empty hasNextTestCase) or (not hasNextTestCase) or hasNextStep },
				icons: {
					primary : 'ui-icon-seek-next'
				}
			}).click(function(){
				parent.squashtm.ieomanager.navigateNextTestCase();
			});
			
			if (${ not empty testPlanItemUrl }) $('#execute-next-test-case-panel').removeClass('not-displayed');		
			if (${ (not empty testPlanItemUrl) and hasPreviousTestCase and (not hasPreviousStep) }) $('#new-test-case-label').removeClass('not-displayed');
		});
	</script> 
	<div id="execute-header" >
			<table style="width: 100%; table-layout: fixed; white-space: nowrap;">
			<tr>
				<td style="width:50px;" class="left-aligned"><button id="execute-stop-button" ><f:message key="execute.header.button.stop.title" /></button></td>
				<td style="padding-left: 20px; width: 120px" class="centered">
					<button id="execute-previous-button"><f:message key="execute.header.button.previous.title" /></button>
					<span id="execute-header-numbers-label">${executionStep.executionStepOrder +1} / ${totalSteps}</span>
					<button id="execute-next-button"><f:message key="execute.header.button.next.title" /></button>
				</td>
				<td style="width:50px" class="centered not-displayed" id="execute-next-test-case-panel">
					<f:message  var="nextTestCaseTitle" key="execute.header.button.next-test-case.title" />
					<button id="execute-next-test-case" name="optimized" class="button" title="${ nextTestCaseTitle }">${ nextTestCaseTitle }</button>
				</td>
				<td><h3 id="ieo-execution-title" class="ellipsis" >${ executionStep.execution.name }</h3></td>
			</tr>
			</table>
	</div>
	<div id="execute-body" class="execute-fragment-body">
	
		<div id="new-test-case-label" class="centered not-displayed">
			<font color=red><f:message
					key="execute.test.suite.next.test.case.label" />
			</font>
		</div>
	
		<div id="execute-evaluation-rightside">
			<div id="execution-information-fragment">
				<comp:step-information-panel auditableEntity="${executionStep}"/>
			</div>
		</div>
		
		<c:if test="${not empty denormalizedFieldValues }">
		<span id="denormalized-fields">
		<comp:toggle-panel id="denormalized-fields-panel" titleKey="title.step.fields" isContextual="true" open="true">
		<jsp:attribute name="body"> 
				<div class="display-table">
					<comp:denormalized-field-values-list denormalizedFieldValues="${ denormalizedFieldValues }" />
				</div>
			</jsp:attribute>
		</comp:toggle-panel>
		</span>
		</c:if>		
		
		<comp:toggle-panel id="execution-action-panel" titleKey="execute.panel.action.title" isContextual="true" open="true">
			<jsp:attribute name="body">
				<div id="execution-action" class="load-links-right-frame">${executionStep.action}</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		<comp:toggle-panel id="execution-expected-result-panel" titleKey="execute.panel.expected-result.title" isContextual="true" open="true">
			<jsp:attribute name="body">
				<div id="execution-expected-result" class="load-links-right-frame">${executionStep.expectedResult}</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		
		<div id="execute-evaluation">
		
			<div id="execute-evaluation-leftside">
	
				<comp:rich-jeditable targetUrl="${executeComment}" componentId="execution-comment" />
	
				<comp:toggle-panel id="execution-comment-panel" titleKey="execute.panel.comment.title" isContextual="true" open="true">
					<jsp:attribute name="body">
						<div id="execution-comment"  class="load-links-right-frame">${executionStep.comment}</div>
					</jsp:attribute>
				</comp:toggle-panel>
			</div>		
			<div style="clear:both;visibility:hidden"></div>
		</div>	
		
				
		<%------------------------------ Attachments bloc ---------------------------------------------%> 
		
		<at:attachment-bloc attachListId="${executionStep.attachmentList.id}" workspaceName="campaign" editable="${ editable }" attachmentSet="${attachments}" />
		
		<%------------------------------ /attachement ------------------------------%>
		
		<%------------------------------ bugs section -------------------------------%>
		<%--
			this section is loaded asynchronously. The bugtracker might be out of reach indeed.
		 --%>	
		 <script type="text/javascript">
		 	$(function(){
		 		$("#bugtracker-section-div").load("${btEntityUrl}");
		 	});
		 </script>
		<div id="bugtracker-section-div">
		</div>
		
		<%------------------------------ /bugs section -------------------------------%>
	</div>
	<comp:decorate-buttons />
</body>
</c:otherwise>
</c:choose>
</html>
