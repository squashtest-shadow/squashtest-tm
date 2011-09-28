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
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />

<s:url var="executeComment" value="/execute/{execId}/step/{stepId}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepId" value="${executionStep.id}" />
	<s:param name="ieo" value="true"/>
</s:url>
	
<s:url var="executeStatus" value="/execute/{execId}/step/{stepId}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepId" value="${executionStep.id}" />
</s:url>

<script type="text/javascript">

	$(function(){
		var positionLeft = $.cookie("menuPositionLeft");
		var positionTop = $.cookie("menuPositionTop");
		if ( positionLeft != null && positionTop != null){
			$("#draggable-menu").offset({top : positionTop, left: positionLeft});
		}
		$("#draggable-menu").draggable({
			start: function(event, ui){
				$("#right-panel").css('visibility','hidden');
				$("#left-panel").css('visibility','hidden');
			},	
			stop: function(event, ui){
				$("#right-panel").css('visibility','visible');
				$("#left-panel").css('visibility','visible');
				var pos = $("#draggable-menu").offset();
				$.cookie("menuPositionLeft", pos.left);
				$.cookie("menuPositionTop", pos.top);
			}	
		});
		
		<%-- Accessing the parent method see ieo-execute-execution.jsp --%>
		$( "#slider" ).slider({
			range: "max",
			min: 1,
			max: ${totalSteps},
			value: ${executionStep.executionStepOrder +1},
			stop: function( event, ui ) {
				parent.parent.navigateOther(ui.value-1);
			}
		});
	});
	
	function refreshExecStepInfos(url){
	}
	
	function testComplete(){
		alert( "${ completedMessage }" )
		window.close();
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
		$("#execute-stop-button-2").button({
			'text': false, 
			'icons' : {
				'primary' : 'execute-stop'
			} 
		})
		.click(function(){
			window.close();
		});
		
	}
	
	function initFailButton(){
		$("#execute-fail-button").button({
			'text': false,
			'icons' :{
				'primary' : 'execute-failure'
			}
		})
		.click(function(){
			$.post('${ executeStatus }', {
				executionStatus : "FAILURE"
			}, setStatusFailure());					
		});		
	}
	
	
	function initSuccessButton(){
		$("#execute-success-button").button({
			'text' : false,
			'icons' : {
				'primary' : 'execute-success'
			}
		})
		.click(function(){
			$.post('${ executeStatus }', {
				executionStatus : "SUCCESS"
			}, setStatusSuccess());				
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
		$("#open-address-dialog-button").button();
		$("#execute-previous-button-2").button();
		$("#execute-next-button-2").button();
		
		$("#execution-status-combo").val("${executionStep.executionStatus}");
		statusComboSetIcon();
		
		$('#execution-status-combo').change(function(success) {
			$.post('${ executeStatus }', {
				executionStatus : $(this).val()
			},
			statusComboChange
			);
		});
	});	
</script>


<div id="draggable-menu" class="ui-state-active">
	<table >
		<tr>
			<td style="text-align:left;"><button id="execute-stop-button-2" ><f:message key="execute.header.button.stop.title" /></button></td>
			<td style="text-align:right;">
				<label id="evaluation-label-status"><f:message key="execute.header.status.label" /></label>
				<select id="execution-status-combo" name="executionStatus" class="execution-status-combo-class">
					<option value="BLOQUED" class="executions-status-bloqued-icon"><f:message key="execution.combo.BLOQUED.label" /></option>
					<option value="FAILURE" class="executions-status-failure-icon"><f:message key="execution.combo.FAILURE.label" /></option>
					<option value="SUCCESS" class="executions-status-success-icon"><f:message key="execution.combo.SUCCESS.label" /></option>
			<!-- <option value="RUNNING" class="executions-status-running-icon"><f:message key="execution.combo.RUNNING.label" /></option>  -->		
					<option value="READY" class="executions-status-ready-icon"><f:message key="execution.combo.READY.label" /></option>
				</select >				
				<button id="execute-fail-button"><f:message key="execute.header.button.failure.title" /></button>
				<button id="execute-success-button"><f:message key="execute.header.button.passed.title" /></button>
			</td>
			<td style="text-align:center;">
				<button id="open-address-dialog-button"><f:message key="execution.IEO.address.go.to.button" /></button>
				<span id="execute-header-numbers-label-2">${executionStep.executionStepOrder +1} / ${totalSteps}</span>
				<button id="execute-previous-button-2"><f:message key="execute.header.button.previous.title" /></button>	
				<button id="execute-next-button-2"><f:message key="execute.header.button.next.title" /></button>
			</td>
		</tr>
		<tr>
			<td>
				<div id="slider"></div>
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
			parent.parent.fillRightFrame(url);
			$('#open-address-dialog').dialog('close');
			},			
		</jsp:attribute>
		<jsp:body>
			<label><f:message key="execution.execute.IEO.address.label" /></label>
			<input id="address-input" type="text" size="50" /><br/>
		</jsp:body>
</comp:popup>	