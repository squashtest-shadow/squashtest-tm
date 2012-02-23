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
<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ execution }">
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
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<title>Exec #${execution.executionOrder + 1 } - ${execution.name} (${executionStep.executionStepOrder +1}/${totalSteps})</title>
	
	<layout:common-head />		
	<layout:_common-script-import />		

	<comp:rich-jeditable-header />	
	

<%-- cautious : below are used StepIndexes and StepIds. Dont get confused. --%>
<s:url var="executeNext" value="${ currentStepUrl }{stepIndex}">
	<s:param name="stepIndex" value="${executionStep.executionStepOrder+1}" />
</s:url>

<s:url var="executePrevious" value="${ currentStepUrl }{stepIndex}">
	<s:param name="stepIndex" value="${executionStep.executionStepOrder-1}" />
</s:url>

<s:url var="executeThis" value="${ currentStepUrl }{stepIndex}">
	<s:param name="stepIndex" value="${executionStep.executionStepOrder}" />
</s:url>

<s:url var="executeComment" value="${ currentStepUrl }{stepId}">
	<s:param name="stepId" value="${executionStep.id}" />
</s:url>

<s:url var="executeStatus" value="${ currentStepUrl }{stepId}">
	<s:param name="stepId" value="${executionStep.id}" />
</s:url>


	<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />	
</head>

<s:url var="btEntityUrl" value="/bugtracker/execution-step/{id}" >
	<s:param name="id" value="${executionStep.id}"/>
</s:url>

<body class="execute-html-body">
	<f:message var="completedMessage" key="execute.alert.test.complete" />
		<script type="text/javascript">
		
			window.onunload = test;
		
			function test(){
				refreshParent();
			}
			
			function refreshParent(){
				window.opener.location.href = window.opener.location.href;
				if (window.opener.progressWindow)
				{
					window.opener.progressWindow.close();
				}
			}
		
			function refreshExecStepInfos(){
				$("#execution-information-fragment").load("${executeThis}/general");
			}
		
			function testComplete() {
				alert( "${ completedMessage }" );
				refreshParent();
				window.close();
			}
		
			function navigateNext(){
				<c:choose>
					<c:when test="${ not hasNextStep }">
				testComplete();
					</c:when>
					<c:otherwise>
				document.location.href="${ executeNext }";						
					</c:otherwise>
				</c:choose>
			}
			
			function navigatePrevious(){
				<c:choose>
					<c:when test="${ not hasPreviousStep }">
				testComplete();
					</c:when>
					<c:otherwise>
				document.location.href="${ executePrevious }";						
					</c:otherwise>
				</c:choose>
			}
			
			function initNextButton(){
				$("#execute-next-button").button({
					'text' : false,
					'disabled': ${ not hasNextStep },
					icons : {
						primary : 'ui-icon-triangle-1-e'
					}
				}).click(function(){
					navigateNext();
				});	
			}
			
			function initPreviousButton(){			
				$("#execute-previous-button").button({
					'text' : false,
					'disabled': ${ not hasPreviousStep },
					icons : {
						primary : 'ui-icon-triangle-1-w'
					}
				}).click(function(){
					navigatePrevious();
				});	
			}
			
			function initStopButton(){
				$("#execute-stop-button").button({
					'text': false, 
					'icons' : {
						'primary' : 'execute-stop'
					} 
				}).click(function(){
					refreshParent();
					window.close();
				});
				
			}
			
			function initFailButton(){
				$("#execute-fail-button").button({
					'text': false,
					'icons' :{
						'primary' : 'execute-failure'
					}
				}).click(function(){
					$.post('${ executeStatus }', {
						executionStatus : "FAILURE"
					}, function(){
						setStatusFailure();
						}
					);
				});		
			}
			
			
			function initSuccessButton(){
				$("#execute-success-button").button({
					'text' : false,
					'icons' : {
						'primary' : 'execute-success'
					}
				}).click(function(){
					$.post('${ executeStatus }', {
						executionStatus : "SUCCESS"
					}, function(){
						setStatusSuccess();
						}
					);
				});
				
			}
			
	
			function setStatusSuccess(){
				$("#execution-status-combo").val("SUCCESS");			
				statusComboChange();
				navigateNext();
			}
			
			function setStatusFailure(){
				$("#execution-status-combo").val("FAILURE");
				statusComboChange();
				navigateNext();
			}
			
			function statusComboSetIcon(){
				var cbox = $("#execution-status-combo");
				//reset the classes
				cbox.attr("class","");
				
				cbox.addClass("execution-status-combo-class");
				
				//find and set the new class
				var selectedIndex=document.getElementById('execution-status-combo').selectedIndex;
				var selector = "option:eq("+selectedIndex+")";
				
				var className = cbox.find(selector).attr("class");
				
				cbox.addClass(className);
			}
		
			function statusComboChange(){
				statusComboSetIcon();
				refreshExecStepInfos();
			}
			
			$(function(){
				initNextButton();
				initPreviousButton();
				initStopButton();
				initFailButton();
				initSuccessButton();

				$("#execution-status-combo").val("${executionStep.executionStatus}");
				statusComboSetIcon();
				
				$('#execution-status-combo').change(function(success) {
					$.post('${ executeStatus }', {
						executionStatus : $(this).val()
					},
					statusComboChange
					);
				});
				
				$("#execute-next-test-case").button({
					'text': false,
					'disabled': ${ (empty hasNextTestCase) or (not hasNextTestCase) or hasNextStep },
					icons: {
						primary : 'ui-icon-seek-next'
					}
				});
				
				if (${ not empty testPlanItemUrl }) $('#execute-next-test-case-panel').removeClass('not-displayed');		
			});	
		</script>
	
	<div id="execute-header">
		<%--  I know, table as a layout. But damn. --%>
		<table>
			<tr>
				<td class="left-align"><button id="execute-stop-button" ><f:message key="execute.header.button.stop.title" /></button></td>
				<td class="centered">
					<button id="execute-previous-button"><f:message key="execute.header.button.previous.title" /></button>
					<span id="execute-header-numbers-label">${executionStep.executionStepOrder +1} / ${totalSteps}</span>	
					<button id="execute-next-button"><f:message key="execute.header.button.next.title" /></button>
				</td>
				<td class="right-align">
					<label id="evaluation-label-status"><f:message key="execute.header.status.label" /></label>
					<comp:execution-status-combo name="executionStatus" id="execution-status-combo" />
					<button id="execute-fail-button"><f:message key="execute.header.button.failure.title" /></button>
					<button id="execute-success-button"><f:message key="execute.header.button.passed.title" /></button>
				</td>
				<td class="centered not-displayed" id="execute-next-test-case-panel">
					<form action="<c:url value='${ testPlanItemUrl }/next-execution/runner' />" method="post">
						<f:message  var="nextTestCaseTitle" key="execute.header.button.next-test-case.title" />
						<button id="execute-next-test-case" name="classic" class="button" title="${ nextTestCaseTitle }">${ nextTestCaseTitle }</button>
					</form>
				</td>
			</tr>
		</table>
		
	</div>
	<div id="execute-body" class="execute-fragment-body">

		<comp:toggle-panel id="execution-action-panel" titleKey="execute.panel.action.title" isContextual="true" open="true">
			<jsp:attribute name="body">
				<div id="execution-action" >${executionStep.action}</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		<comp:toggle-panel id="execution-expected-result-panel" titleKey="execute.panel.expected-result.title" isContextual="true" open="true">
			<jsp:attribute name="body">
				<div id="execution-expected-result" >${executionStep.expectedResult}</div>
			</jsp:attribute>
		</comp:toggle-panel>

		<div id="execute-evaluation">
		
			<div id="execute-evaluation-leftside">
	
				<comp:rich-jeditable targetUrl="${executeComment}" componentId="execution-comment" 
				submitCallback="refreshExecStepInfos"/>
	
				<comp:toggle-panel id="execution-comment-panel" titleKey="execute.panel.comment.title" isContextual="true" open="true">
					<jsp:attribute name="body">
						<div id="execution-comment" >${executionStep.comment}</div>
					</jsp:attribute>
				</comp:toggle-panel>
			</div>		
			
			<div id="execute-evaluation-rightside">
				<div id="execution-information-fragment">
					<comp:step-information-panel auditableEntity="${executionStep}"/>
				</div>
			</div>
			<div style="clear:both;visibility:hidden"></div>
		</div>	
		
		<comp:attachment-bloc entity="${executionStep}" workspaceName="campaign" editable="${ editable }" />
		
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