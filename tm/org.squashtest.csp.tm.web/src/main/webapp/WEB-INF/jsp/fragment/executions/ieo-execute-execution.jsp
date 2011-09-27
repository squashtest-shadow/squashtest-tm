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
<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >

<head>
	
	
	<layout:common-head />		
	<layout:_common-script-import />		
	<comp:decorate-toggle-panels />
	<comp:rich-jeditable-header />	
	<layout:_handle-ajax-errors />
	<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.cookie.js"></script>
	
	<script type="text/javascript">
	
	var urlRefreshStep = "/squash/execute/${execution.id}/step/";
	var stepNumber = parseInt("${executionStep.executionStepOrder}");
	var stepNumberPrevious = stepNumber - 1;
	var stepNumberNext = stepNumber + 1;
	var stepId = ${executionStep.id};
	var urlMenuPrevious = urlRefreshStep + "" + stepNumberPrevious +"/menu?ieo=true";
	var urlMenuNext = urlRefreshStep + "" + stepNumberNext +"/menu?ieo=true";
	
	var urlPrevious = urlRefreshStep + "" + stepNumberPrevious +"?ieo=true";
	var urlNext = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
	
	var urlCurrent = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
	
	var urlComment = urlRefreshStep + "" + stepId;
	var urlStatus = urlRefreshStep + "" + stepId;
	
	</script>
	
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


<body id="ieo-body">
	<script type="text/javascript">
	
	
	$(function(){

		$("#left-panel").resizable({
			helper: "ui-resizable-helper",
			alsoResize: "#right-panel",
			start: function(){ 
				$("#right-panel").css('visibility','hidden');
				$("#iframe-left").css('visibility','hidden');
			},
			stop: function(event, ui) {
				var body = document.getElementById("ieo-body");
				var bodyWidth = body.offsetWidth;
				var marginLeft = 5;
				var leftPanelSize = $(this).width();
				var rightPanelWidth = bodyWidth - (marginLeft + leftPanelSize);
				$("#right-panel").width(rightPanelWidth);
				$("#right-panel").css('visibility','visible');
				$("#iframe-left").css('visibility','visible');
			}
		});

		$("#right-panel").resizable();
		
	});
	
	function initStepValues(){
		
	}
	
	function refreshStepValues(urlStep){
		$.get(urlStep, function(rslt){
				stepNumber = parseInt(rslt.executionStepOrder);
				stepNumberPrevious = stepNumber - 1;
				stepNumberNext = stepNumber + 1;
				stepId = rslt.executionStepId;
				urlMenuPrevious = urlRefreshStep + "" + stepNumberPrevious +"/menu?ieo=true";
				urlMenuNext = urlRefreshStep + "" + stepNumberNext +"/menu?ieo=true";
				
				urlPrevious = urlRefreshStep + "" + stepNumberPrevious +"?ieo=true";
				urlNext = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
				
				urlCurrent = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
				
				urlComment = urlRefreshStep + "" + stepId;
				urlStatus = urlRefreshStep + "" + stepId;
			  }, 
			"json");
	}
	
	<%-- Reloading draggable menu with the right step --%>
	function refreshMenu(menuUrl, newNumber){
		$("#menu-space").load(menuUrl);
		refreshStepValues(urlRefreshStep + "" + newNumber + "/new-step-infos");
	}
	
	function refreshMenuNext(){
		refreshMenu(urlMenuNext, stepNumberNext);
	}
	
	function refreshMenuPrevious(){
		refreshMenu(urlMenuPrevious, stepNumberPrevious);
	}
	
	<%-- Navigate left panel to the right Step --%>
	function navigateNext(){
		<c:choose>
			<c:when test="${stepNumberNext == totalSteps-1}">
		testComplete();
			</c:when>
			<c:otherwise>
			parent.frameleft.document.location.href=urlNext;
			refreshMenuNext();
			</c:otherwise>
		</c:choose>
	}
	
	function navigateOther(value){
		var theUrl =  urlRefreshStep + ""+ value +"?ieo=true";
		var theMenuUrl =  urlRefreshStep + ""+ value +"/menu?ieo=true";
		parent.frameleft.document.location.href=theUrl;
		refreshMenu(theMenuUrl, value);
	}

	function navigatePrevious(){
		<c:choose>
			<c:when test="${stepNumberPrevious == 0}">
		testComplete();
			</c:when>
			<c:otherwise>
			parent.frameleft.document.location.href=urlPrevious;
			refreshMenuPrevious();
			</c:otherwise>
	</c:choose>			

	}
	<%-- fill the right panel with the content of entered url --%>
	function fillRightFrame(url){
		$('#iframe-right').attr("src", url);
	}
	
	</script>

	<div id="left-panel" style="z-index: 0;">
		<iframe id="iframe-left" name="frameleft" src="${executeThis}">
		</iframe>
	</div>

	
	<div id="right-panel" style="z-index: 0;">
		<iframe id="iframe-right" name="frameright">
		</iframe> 
	</div>
	
	<div id="menu-space" >
		<jsp:include page="step-information-menu.jsp" />
	</div>

	<comp:decorate-buttons />

</body>
</html>