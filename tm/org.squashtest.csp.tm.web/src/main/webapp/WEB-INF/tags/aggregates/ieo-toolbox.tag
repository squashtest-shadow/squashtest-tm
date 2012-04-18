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
<%@ tag body-content="empty" %>
<%@ attribute name="execution" required="true" type="java.lang.Object" %>
<%@ attribute name="executionStep" required="true" type="java.lang.Object" %>
<%@ attribute name="hasPreviousStep" required="true" type="java.lang.Boolean" %>
<%@ attribute name="hasNextStep" required="true" type="java.lang.Boolean" %>
<%@ attribute name="totalSteps" required="true" type="java.lang.Integer" %>
<%@ attribute name="hasNextTestCase" required="false" type="java.lang.Boolean" %>
<%@ attribute name="isSuite" required="false" type="java.lang.Boolean" %>
<%@ attribute name="testPlanItemUrl" required="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<s:url var="executeStatus" value="/execute/{execId}/step/{stepId}">
	<s:param name="execId" value="${execution.id}" />
	<s:param name="stepId" value="${executionStep.id}" />
</s:url>

<script type="text/javascript">
	var changeStatusUrl = '${ executeStatus }';
	var hasPreviousStep = ${ (not empty hasPreviousStep) and hasPreviousStep }; 
	var hasNextStep = ${ (not empty hasNextStep) and hasNextStep };
	var hasNextTestCase = ${ (not empty hasNextTestCase) and hasNextTestCase };
	var isSuite = ${ (not empty isSuite) and isSuite }

	$(function() {
		var positionLeft = $.cookie("ieo-toolbox-position-left");
		var positionTop = $.cookie("ieo-toolbox-position-top");
		
		if ( positionLeft != null && positionTop != null ) {
			$("#draggable-menu").offset({top : positionTop, left: positionLeft});
		}
		
		$("#draggable-menu").draggable({
			start: function(event, ui){
				$(".iframe-container").addClass('not-visible');
			},	
			stop: function(event, ui){
				$(".iframe-container").removeClass('not-visible');
				var pos = $("#draggable-menu").offset();
				$.cookie("ieo-toolbox-position-left", pos.left);
				$.cookie("ieo-toolbox-position-top", pos.top);
			}	
		});
		
		<%-- Accessing the parent method see ieo-execute-execution.jsp --%>
		$( "#slider" ).slider({
			range: "max",
			min: 1,
			max: ${totalSteps},
			value: ${executionStep.executionStepOrder +1 },
			stop: function( event, ui ) {
				parent.parent.navigateOther(ui.value - 1);
			}
		});
		
		$("#execute-next-step").button({
			'text': false,
			'disabled': !hasNextStep,
			icons: {
				primary : 'ui-icon-triangle-1-e'
			}
		});
		
		$("#execute-previous-step").button({
			'text' : false,
			'disabled': !hasPreviousStep,
			icons : {
				primary : 'ui-icon-triangle-1-w'
			}
		});
	
		$("#stop-execution").button({
			'text': false, 
			'icons' : {
				'primary' : 'ui-icon-power'
			} 
		});

		$("#step-failed").button({
			'text': false,
			'icons' :{
				'primary' : 'execute-failure'
			}
		});

		$("#step-succeeded").button({
			'text' : false,
			'icons' : {
				'primary' : 'execute-success'
			}
		});

		$("#execute-next-test-case").button({
			'text': false,
			'disabled': !hasNextTestCase || hasNextStep,
			icons: {
				primary : 'ui-icon-seek-next'
			}
		});
		
		if (${ not empty testPlanItemUrl }) $('#execute-next-test-case-panel').removeClass('not-displayed');

		$("#draggable-menu .button").button();
		
		$("#step-status-combo").val("${executionStep.executionStatus}");
		
		statusComboSetIcon($("#step-status-combo"));		
		
		$('#urlATransmettre').attr("value", $('#iframe-right').attr("src"));
	});	
</script>


<div id="draggable-menu" class="ui-state-active">
	<table >
		<tr>
			<td class="left-aligned"><button id="stop-execution" ><f:message key="execute.header.button.stop.title" /></button></td>
			<td class="right-aligned">
				<label id="evaluation-label-status"><f:message key="execute.header.status.label" /></label>
				<comp:execution-status-combo name="executionStatus" id="step-status-combo" />
				<button id="step-failed"><f:message key="execute.header.button.failure.title" /></button>
				<button id="step-succeeded"><f:message key="execute.header.button.passed.title" /></button>
			</td>
			<td class="centered">
				<button id="open-address-dialog-button" class="button"><f:message key="execution.IEO.address.go.to.button" /></button>
				<span id="step-paging">${executionStep.executionStepOrder +1} / ${totalSteps}</span>
				<button id="execute-previous-step" class="button"><f:message key="execute.header.button.previous.title" /></button>	
				<button id="execute-next-step" class="button"><f:message key="execute.header.button.next.title" /></button>
			</td>
			<td class="centered not-displayed" id="execute-next-test-case-panel">
				<form action="<c:url value='${ testPlanItemUrl }/next-execution/runner' />" method="post">
					<input id="urlATransmettre" type="hidden" name="ieoIFrameUrl" value="" >
					<f:message  var="nextTestCaseTitle" key="execute.header.button.next-test-case.title" />
					<button id="execute-next-test-case" name="optimized" class="button" title="${ nextTestCaseTitle }">${ nextTestCaseTitle }</button>
				</form>
			</td>
		</tr>
		<tr>
			<td class="centered" colspan="4">
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