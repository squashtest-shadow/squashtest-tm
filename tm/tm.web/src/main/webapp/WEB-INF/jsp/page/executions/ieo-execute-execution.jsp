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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="gr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" >

<head>
	<layout:common-head />		
	<layout:_common-script-import />		

	<comp:rich-jeditable-header />	
	<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.cookie.js"></script>
	
	<script type="text/javascript">
	var urlRefreshStep = "<c:url value='${ currentStepUrl }' />";
	var stepNumber = parseInt("${ executionStep.executionStepOrder }");
	var stepNumberPrevious = stepNumber - 1;
	var stepNumberNext = stepNumber + 1;
	var stepId = ${executionStep.id};
	var urlToolboxPrevious = urlRefreshStep + "" + stepNumberPrevious +"/menu?ieo=true";
	var urlToolboxNext = urlRefreshStep + "" + stepNumberNext +"/menu?ieo=true";
	
	var urlPrevious = urlRefreshStep + "" + stepNumberPrevious +"?ieo=true";
	var urlNext = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
	
	var urlCurrent = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
	
	var urlComment = urlRefreshStep + "" + stepId;
	var urlStatus = urlRefreshStep + "" + stepId;
	
	var urlIFrame = "${ urlIFrame }";
	
	</script>
	
	<%-- cautious : below are used StepIndexes and StepIds. Dont get confused. --%>	
	<c:url var="executeThis" value='${ currentStepUrl }/${ executionStep.executionStepOrder }'>
		<c:param name="ieo" />
	</c:url>
	<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />
</head>

<body id="ieo-body">
	<f:message var="completedMessage" key="execute.alert.test.complete" />
	<f:message var="endTestSuiteMessage" key="squashtm.action.exception.testsuite.end" />
	<script type="text/javascript">
	$(function() {

		$("#left-panel").resizable({
			helper: "ui-resizable-helper",
			alsoResize: "#right-panel",
			start: function(){ 
				$(".resizable").addClass('not-visible');
			},
			stop: function(event, ui) {
				var body = document.getElementById("ieo-body");
				var bodyWidth = body.offsetWidth;
				var marginLeft = 5;
				var leftPanelSize = $(this).width();
				var rightPanelWidth = bodyWidth - (marginLeft + leftPanelSize);
				$("#right-panel").width(rightPanelWidth);
				$(".resizable").removeClass('not-visible');
			}
		});

		$("#right-panel").resizable();
		
		var toolbox = $("#toolbox-container");
		
		toolbox.delegate("#execute-next-step", "click", navigateNext);
		toolbox.delegate("#execute-previous-step", "click", navigatePrevious);
		toolbox.delegate('#stop-execution', 'click', function() {
			window.close();
		});
		toolbox.delegate('#step-status-combo', 'change', function(success) {
			var self = this;
			$.post(changeStatusUrl, {
				executionStatus : $(self).val()
			}, function() {
				statusComboChange(self);
			});
		});
		toolbox.delegate('#step-succeeded', 'click', function() {
			$.post(changeStatusUrl, {
				executionStatus : "SUCCESS"
			}).done( setStatusSuccess );				
		});
		toolbox.delegate('#step-failed', 'click', function(){
			$.post(changeStatusUrl, {
				executionStatus : "FAILURE"
			}).done( setStatusFailure );					
		});
		
		/* $(parent.frameleft).unload( refreshParent ); */// see "comment[1]"

	});
	
	function refreshParent(){
		window.opener.location.href = window.opener.location.href;
		if (window.opener.progressWindow) {
			window.opener.progressWindow.close();
		}
	}
	
	function statusComboSetIcon(combo){
		var cbox = $(this);
		//reset the classes
		cbox.attr("class","");
		
		cbox.addClass("execution-status-combo-class");
		
		//find and set the new class
		var selectedIndex = document.getElementById('step-status-combo').selectedIndex;
		var selector = "option:eq(" + selectedIndex + ")";
		
		var className = cbox.find(selector).attr("class");
		
		cbox.addClass(className);
	}
	
	function statusComboChange(combo){
		statusComboSetIcon(combo);
	}

	function setStatusSuccess(){
		var combo = $("#step-status-combo"); 
		combo.val("SUCCESS");			
		statusComboChange(combo);
		navigateNext();
	}
	
	function setStatusFailure(){
		var combo = $("#step-status-combo"); 
		combo.val("FAILURE");
		statusComboChange(combo);
		navigateNext();
	}
	
	function refreshStepValues(urlStep){
		$.get(urlStep, function(rslt){
				stepNumber = parseInt(rslt.executionStepOrder);
				stepNumberPrevious = stepNumber - 1;
				stepNumberNext = stepNumber + 1;
				stepId = rslt.executionStepId;
				urlToolboxPrevious = urlRefreshStep + "" + stepNumberPrevious +"/menu?ieo=true";
				urlToolboxNext = urlRefreshStep + "" + stepNumberNext +"/menu?ieo=true";
				
				urlPrevious = urlRefreshStep + "" + stepNumberPrevious +"?ieo=true";
				urlNext = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
				
				urlCurrent = urlRefreshStep + "" + stepNumberNext +"?ieo=true";
				
				urlComment = urlRefreshStep + "" + stepId;
				urlStatus = urlRefreshStep + "" + stepId;
			  }, 
			"json");
	}
	
	<%-- Reloading draggable menu with the right step --%>
	function refreshToolbox(toolboxUrl, newNumber) {
		$("#toolbox-container").load(toolboxUrl);
		refreshStepValues(urlRefreshStep + "" + newNumber + "/new-step-infos");
	}
	
	function refreshToolboxNext() {
		refreshToolbox(urlToolboxNext, stepNumberNext);
	}
	
	function refreshToolboxPrevious() {
		refreshToolbox(urlToolboxPrevious, stepNumberPrevious);
	}
	
	<%-- Navigate left panel to the right Step --%>
	function navigateNext(){
		/* refreshParent(); */
		if (hasNextStep) {
			showStepAt(urlNext);
			refreshToolboxNext();
		} else {
			testComplete();
		}
	}
	
	function navigateOther(value) {
		var theUrl =  urlRefreshStep + ""+ value +"?ieo=true";
		var theMenuUrl =  urlRefreshStep + ""+ value +"/menu?ieo=true";
		showStepAt(theUrl);
		refreshToolbox(theMenuUrl, value);
		/* refreshParent(); */
	}

	function navigatePrevious() {
		/* refreshParent(); */
		if (hasPreviousStep) {
			showStepAt(urlPrevious);
			refreshToolboxPrevious();
		} else {
			testComplete();
		}
	}
	
	function showStepAt(url) {
		parent.frameleft.document.location.href = url;
		/* $(parent.frameleft).unload( refreshParent ); */
		refreshParent();// see "comment[1]"
	}

	function testComplete(){
		if (!isSuite) {
			$.squash.openMessage("<f:message key='popup.title.info' />",  "${ completedMessage }" ).done(function() {
				refreshParent();// see "comment[1]"
				window.close();
			});
		} else if (hasNextTestCase) {
			$('#execute-next-test-case').click();
			refreshParent(); // see "comment[1]"
		} else { // suite without next
			$.squash.openMessage("<f:message key='popup.title.info' />","${ endTestSuiteMessage }").done(function() {
				refreshParent();// see "comment[1]"
				window.close();
 			});
		}					
	}
	
	<%-- fill the right panel with the content of entered url --%>
	function fillRightFrame(urlP){
		urlIFrame = urlP;
		$('#iframe-right').attr("src", urlP);
		$('#urlATransmettre').attr("value", urlP);
	}
	
	</script>

	<div id="left-panel" class="iframe-container resizable" style="z-index: 0;">
		<iframe id="iframe-left" name="frameleft" src="${executeThis}">
		</iframe>
	</div>

	
	<div id="right-panel" class="iframe-container" style="z-index: 0;">
		<iframe id="iframe-right" class="resizable" name="frameright" src="${ urlIFrame }">
		</iframe> 
	</div>
	
	<div id="toolbox-container" >
		<div id="ieo-toolbox" class="ui-state-active">
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
						<button class="button open-address-dialog-button"><f:message key="execution.IEO.address.go.to.button" /></button>
						<span class="step-paging"></span>
						<button class="button execute-previous-step"><f:message key="execute.header.button.previous.title" /></button>	
						<button class="button execute-next-step"><f:message key="execute.header.button.next.title" /></button>
					</td>
					<td class="centered not-displayed execute-next-test-case-panel">
						<form action="<c:url value='${ testPlanItemUrl }/next-execution/runner' />" method="post">
							<f:message  var="nextTestCaseTitle" key="execute.header.button.next-test-case.title" />
							<button name="optimized" class="button execute-next-test-case" title="${ nextTestCaseTitle }">${ nextTestCaseTitle }</button>
						</form>
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
					squashtm.ieomanager.fillRightFrame(url);
					$('#open-address-dialog').dialog('close');
					},			
				</jsp:attribute>
				<jsp:body>
					<label><f:message key="execution.execute.IEO.address.label" /></label>
					<input id="address-input" type="text" size="50" /><br/>
				</jsp:body>
		</comp:popup>	
		
	</div>

	<comp:decorate-buttons />
</body>
</html>

<!-- comment[1] [Issue 1126] Had to use directly "refreshParent" instead of "iframe.unload(refreshParent)" beacause the latest do not work with IE 8  -->