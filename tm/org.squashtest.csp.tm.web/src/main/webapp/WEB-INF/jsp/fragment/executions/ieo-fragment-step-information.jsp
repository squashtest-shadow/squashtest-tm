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
	<layout:_handle-ajax-errors />
	

<%-- cautious : below are used StepIndexes and StepIds. Dont get confused. --%>
<s:url var="executeNext" value="/execute/{execId}/step/{stepIndex}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepIndex" value="${executionStep.executionStepOrder+1}" />
	<s:param name="ieo" value="true"/>
</s:url>

<s:url var="executePrevious" value="/execute/{execId}/step/{stepIndex}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepIndex" value="${executionStep.executionStepOrder-1}" />
	<s:param name="ieo" value="true"/>
</s:url>

<s:url var="executeMenuNext" value="/execute/{execId}/step/{stepIndex}/menu">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepIndex" value="${executionStep.executionStepOrder+1}" />
	<s:param name="ieo" value="true"/>
</s:url>

<s:url var="executeMenuPrevious" value="/execute/{execId}/step/{stepIndex/menu">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepIndex" value="${executionStep.executionStepOrder-1}" />
	<s:param name="ieo" value="true"/>
</s:url>

<s:url var="executeThis" value="/execute/{execId}/step/{stepIndex}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepIndex" value="${executionStep.executionStepOrder}" />
	<s:param name="ieo" value="true"/>
</s:url>

<s:url var="executeComment" value="/execute/{execId}/step/{stepId}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepId" value="${executionStep.id}" />
	<s:param name="ieo" value="true"/>
</s:url>

<s:url var="executeStatus" value="/execute/{execId}/step/{stepId}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepId" value="${executionStep.id}" />
</s:url>


	<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />	
</head>

<s:url var="btEntityUrl" value="/bugtracker/execution-step/{id}" >
	<s:param name="id" value="${executionStep.id}"/>
</s:url>


<body class="execute-html-body ieo-body">



	
	<f:message var="completedMessage" key="execute.alert.test.complete" />
	
	<script type="text/javascript">
	
		function refreshExecStepInfos(){
			<%-- $("execute-html-body").parent().load("${executeThis}/ieo"); --%>
			
		}
	
		function testComplete(){
			alert( "${completedMessage}" );
			window.close();
		}
	
		function navigateNext(){
			parent.parent.navigateNext();
		}
		
		function navigatePrevious(){
			parent.parent.navigatePrevious();	

		}
		
		function initNextButton(){
			$("#execute-next-button").button({
				'text' : false,
				icons : {
					primary : 'ui-icon-triangle-1-e'
				}
			});
			$("#execute-next-button-2").button({
				'text' : false,
				icons : {
					primary : 'ui-icon-triangle-1-e'
				}
			});
			<c:choose>
				<c:when test="${executionStep.executionStepOrder == totalSteps-1}">
			//disable the next button since it's the last step
			$("#execute-next-button").button("option", "disabled", true);
			$("#execute-next-button-2").button("option", "disabled", true);
				</c:when>
				<c:otherwise>
			$("#execute-next-button").click(function(){
				navigateNext();
			});	
			$("#execute-next-button-2").click(function(){
				navigateNext();
			});	
				</c:otherwise>
			</c:choose>
		}
		
		function initPreviousButton(){			
		$("#execute-previous-button").button({
			'text' : false,
			icons : {
				primary : 'ui-icon-triangle-1-w'
			}
		});
		$("#execute-previous-button-2").button({
			'text' : false,
			icons : {
				primary : 'ui-icon-triangle-1-w'
			}
		});
		<c:choose>
			<c:when test="${executionStep.executionStepOrder == 0}">
		//disable the previous button since it's the first step
		$("#execute-previous-button").button("option", "disabled", true);
		$("#execute-previous-button-2").button("option", "disabled", true);
			</c:when>
			<c:otherwise>
		$("#execute-previous-button").click(function(){
			navigatePrevious();
		});
		$("#execute-previous-button-2").click(function(){
			navigatePrevious();
		});	
			</c:otherwise>
		</c:choose>
		}
		
		function initStopButton(){
			$("#execute-stop-button").button({
				'text': false, 
				'icons' : {
					'primary' : 'execute-stop'
				} 
			})
			.click(function(){
				parent.parent.window.close();
			});			
		}
		
		
		$(function(){
			initNextButton();
			initPreviousButton();
			initStopButton();
			$("#open-address-dialog-button").button();
			$("#execute-previous-button-2").button();
			$("#execute-next-button-2").button();
		});	
	</script>

	<script type="text/javascript">
	
		$(function() {	

			
			$("#execution-action a").live('click', function(){
				$(this).attr('target', "frameright");
			});
			
			$("#bugtracker-section-div a").live('click', function(){
				$(this).attr('target', "frameright");
			});			
		});
		
	</script> 
	<div id="execute-header">
		<!--  table layout ftw. -->	
		<table>
			<tr>
				<td style="text-align:left;"><button id="execute-stop-button" ><f:message key="execute.header.button.stop.title" /></button></td>
				<td style="text-align:left; padding-left: 20px;">
					<button id="execute-previous-button"><f:message key="execute.header.button.previous.title" /></button>
					<span id="execute-header-numbers-label">${executionStep.executionStepOrder +1} / ${totalSteps}</span>	
					<button id="execute-next-button"><f:message key="execute.header.button.next.title" /></button>
				</td>
			</tr>
		</table> 
			
	</div>
	<div id="execute-body" class="execute-fragment-body">
	
		<div id="execute-evaluation-rightside">
			<div id="execution-information-fragment">
				<comp:step-information-panel auditableEntity="${executionStep}"/>
			</div>
		</div>
		
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
			<div style="clear:both;visibility:hidden"></div>
		</div>	
		
				
		<%------------------------------ Attachments bloc ---------------------------------------------%> 
		<comp:attachment-bloc entity="${executionStep}" workspaceName="campaign" editable="${ editable }" />
		
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