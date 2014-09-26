<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="is" tagdir="/WEB-INF/tags/issues"%>

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
	<comp:sq-css name="squash.purple.css" />
	<layout:_common-script-import highlightedWorkspace="" />
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
					<comp:execution-status-combo name="executionStatus" id="step-status-combo" allowsUntestable="${config.allowsUntestable}" allowsSettled="${config.allowsSettled}"/>
					<c:if test="${config.allowsUntestable}">
						<button class="step-untestable"><f:message key="execute.header.button.untestable.title" /></button>
					</c:if>
					<button class="step-blocked"><f:message key="execute.header.button.blocked.title" /></button>
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
	<pop:popup id="open-address-dialog" openedBy="open-address-dialog-button" titleKey="execution.IEO.address.bar.label">
		<jsp:attribute name="buttons">
				<f:message var="label" key="execution.IEO.address.go.to.button" />
				'${ label }': function() {
				var url = $('#address-input').val();
				squashtm.ieomanager.fillRightPane(url);
				$('#open-address-dialog').dialog('close');
				},			
			</jsp:attribute>
			<jsp:attribute name="body">
				<label><f:message key="execution.execute.IEO.address.label" /></label>
				<input id="address-input" type="text" size="50" /><br/>
			</jsp:attribute>
	</pop:popup>
	
	<c:if test="${not empty bugTracker}">
	<is:issue-add-popup id="issue-report-dialog" interfaceDescriptor="${interfaceDescriptor}"  bugTrackerId="${bugTracker.id}"/>		
	</c:if>
	
	<f:message var="errorTitle" key="popup.title.error"/>
	<f:message var="okLabel" key="label.Ok"/>
	<div id="generic-error-dialog" class="not-displayed popup-dialog" title="${errorTitle}">
	  <div>
	     <div class="display-table-row">
	        <div class="display-table-cell warning-cell">
	          <div class="generic-error-signal"></div>
	        </div>
	        <div class="generic-error-main display-table-cell" style="padding-top:20px">
	        
	        </div>
	      </div>
	  </div>
	  <input type="button" value="${okLabel}"/>  
	</div>
	
	<script type="text/javascript">
	require(["common"], function() {
		require(["jquery", "domReady", "execution-processing", "jquery.squash.messagedialog"], 
				function($, domReady, execProcessing){
			requirejs.config({
				config : {
					'execution-processing/init-ieo' : ${json:serialize(config)}
				}
			});
			
			domReady(function(){
				$("#open-address-dialog-button").button();
				$("#generic-error-dialog").messageDialog();
				execProcessing.initIEO();
			});
			
			
		});
	});
	</script>
</body>
</html>
