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
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >

<c:if test="${ config.prologue }">
  <c:set var="executeThis" value="${config.baseStepUrl}/prologue?optimized=true" />
</c:if>
<c:if test="${ not config.prologue }">
  <c:set var="executeThis" value="${config.baseStepUrl}/index/${config.currentStepIndex -1}?optimized=true" />
</c:if>


<head>

	
	<layout:common-head />		
	<layout:_common-script-import highlightedWorkspace=""/>
	<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/squash.purple.css" />

	<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/common.js"></script>
		
</head>

<body id="ieo-body">

	<div id="ieo-left-panel" >
		<iframe id="iframe-left" name="frameleft" class="ieo-frame" src="${executeThis}">
		</iframe>
	</div>

	
	<div id="ieo-right-panel">
		<iframe id="iframe-right" name="frameright" class="ieo-frame" >
		</iframe>
	</div>
	
	
	<%-- structure of the toolbox --%>
	<div id="ieo-control" class="ui-state-active not-displayed">
		<table >
			<tr>
				<td class="left-aligned"><button class="stop-execution"><f:message key="execute.header.button.stop.title" /></button></td>
				<td class="right-aligned">
					<label class="evaluation-label-status"><f:message key="execute.header.status.label" /></label>
					<comp:execution-status-combo name="executionStatus" id="step-status-combo" />
					<button class="step-failed"><f:message key="execute.header.button.failure.title" /></button>
					<button class="step-succeeded"><f:message key="execute.header.button.passed.title" /></button>
				</td>
				<td class="centered">
					<button id="open-address-dialog-button" class="button "><f:message key="execution.IEO.address.go.to.button" /></button>
					<span class="step-paging"></span>
					<button class="button execute-previous-step"><f:message key="execute.header.button.previous.title" /></button>	
					<button class="button execute-next-step"><f:message key="execute.header.button.next.title" /></button>
				</td>
				<td class="centered not-displayed execute-next-test-case-panel">
					<f:message  var="nextTestCaseTitle" key="execute.header.button.next-test-case.title" />
					<button class="button execute-next-test-case" title="${ nextTestCaseTitle }">${ nextTestCaseTitle }</button>
				</td>
			</tr>
			<tr>
				<td class="centered" colspan="4">
					<div class="slider"></div>
				</td>
			</tr>
		</table>
	</div>
	
	
	<%-- Popup to enter the url we want the right panel to be filled with --%>
	<comp:popup id="open-address-dialog" openedBy="open-address-dialog-button" titleKey="execution.IEO.address.bar.label">
		<jsp:attribute name="buttons">
				<f:message var="label" key="execution.IEO.address.go.to.button" />
				'${ label }': function() {
				var url = $('#address-input').val();
				squashtm.ieomanager.fillRightPane(url);
				$('#open-address-dialog').dialog('close');
				},			
			</jsp:attribute>
			<jsp:body>
				<label><f:message key="execution.execute.IEO.address.label" /></label>
				<input id="address-input" type="text" size="50" /><br/>
			</jsp:body>
	</comp:popup>	

	
	<script type="text/javascript">
		require(["domReady", "execution-processing"], function(domReady, execProcessing){
			domReady(function(){
				
				$("#open-address-dialog-button").button();
				
				requirejs.config({
					config : {
						'execution-processing/init-ieo' : ${json:serialize(config)}
					}
				
				});
				execProcessing.initIEO();
			});
		});
	
	</script>
</body>
</html>
