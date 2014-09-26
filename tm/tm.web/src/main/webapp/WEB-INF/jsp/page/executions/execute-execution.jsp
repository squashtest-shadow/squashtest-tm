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
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>

<c:url var="customFieldsValuesURL" value="/custom-fields/values" />
<c:url var="denormalizedFieldsValuesURL" value="/denormalized-fields/values" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>

<c:set var="editable" value="${ false }" />
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
	domainObject="${ execution }">
	<c:set var="editable" value="${ true }" />
</authz:authorized>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="execute-html">

<c:choose>
	<c:when test="${totalSteps == 0 }">
		<span><f:message key="execute.header.nostep.label" />
		</span>
	</c:when>
	<c:otherwise>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
			<title>Exec #${execution.executionOrder + 1 } -
				${execution.name} (${executionStep.executionStepOrder
				+1}/${totalSteps})</title>
			
			<layout:common-head />
			<layout:_common-script-import highlightedWorkspace=""/>
			
			<%-- cautious : below are used StepIndexes and StepIds. Dont get confused. --%>
			<s:url var="executeNext" value="${ currentStepsUrl }/index/{stepIndex}?optimized=${param.optimized}">
				<s:param name="stepIndex" value="${executionStep.executionStepOrder+1}" />
			</s:url>
			
            <c:if test="${executionStep.first}">
              <s:url var="executePrevious" value="${ currentStepsUrl }/prologue?optimized=${param.optimized}" />
            </c:if>
            <c:if test="${not executionStep.first}">
              <s:url var="executePrevious" value="${ currentStepsUrl }/index/{stepIndex}?optimized=${param.optimized}">
                <s:param name="stepIndex" value="${ executionStep.executionStepOrder - 1 }" />
              </s:url>
            </c:if>
			
			<s:url var="executeThis" value="${ currentStepsUrl }/index/{stepIndex}?optimized=${param.optimized}">
				<s:param name="stepIndex" value="${executionStep.executionStepOrder}" />
			</s:url>
			
			<s:url var="executeComment" value="${ currentStepsUrl }/{stepId}">
				<s:param name="stepId" value="${executionStep.id}" />
			</s:url>
			
			<s:url var="executeStatus" value="${ currentStepsUrl }/{stepId}">
				<s:param name="stepId" value="${executionStep.id}" />
			</s:url>
			
			<s:url var="executeInfos" value="${ currentStepsUrl }/index/{stepIndex}">
				<s:param name="stepIndex" value="${executionStep.executionStepOrder}" />
			</s:url>

			<comp:sq-css name="squash.purple.css" />
		</head>

		<s:url var="btEntityUrl" value="/bugtracker/execution-step/{id}">
			<s:param name="id" value="${executionStep.id}" />
		</s:url>
		<body class="execute-html-body">
			<f:message var="completedMessage" key="execute.alert.test.complete" />
			<f:message var="endTestSuiteMessage" key="squashtm.action.exception.testsuite.end" />

			<script type="text/javascript">						
			require(["common"], function() {
				require(['jquery', 'squash.basicwidgets', 
				         'iesupport/am-I-ie8',
				         'execution-processing/ie8-no-close-on-enter',
				         'page-components/step-information-panel', 'workspace.event-bus', 'app/util/ComponentUtil', 
				         "jquery.squash.oneshotdialog" , 'custom-field-values', 'jquery.squash'], 
				         function($, basicwidg, isIE, noCloseOnEnter, infopanel, eventBus, ComponentUtil, oneshot, cufValues ) {
			
				var isOer = ${ not empty hasNextTestCase };
				var hasNextTestCase = ${ (not empty hasNextTestCase) and hasNextTestCase };
				var hasNextStep = ${ (not empty hasNextStep) and hasNextStep };
	
				function refreshParent(){
					if (!!window.opener) {
                        if (window.opener.squashtm.execution){
							window.opener.squashtm.execution.refresh();
						}
						if (window.opener.progressWindow) {
							window.opener.progressWindow.close();
						}
                    }
				}
			
				function refreshExecStepInfos(){
					refreshParent();
					infopanel.refresh();
				}
			
				function testComplete() {	

					if (!isOer) {
						oneshot.show('popup.title.info',  "${ completedMessage }").done(function() {
							window.close();
						});
					} else if (hasNextTestCase) {
						$('#execute-next-test-case').click();
					} else { 
						// oer without next
						oneshot.show('popup.title.info',  "${ endTestSuiteMessage }").done(function() {
							window.close();
						});
					}					
					// parent refresh triggered through event
				}
				
				function navigateNext(){
					if (hasNextStep) {
						document.location.href="${ executeNext }";						
					} else {
						testComplete();
					}
				}
				
				function navigatePrevious(){
					document.location.href="${ executePrevious }";						
				}
				
				function initNextButton(){
					$("#execute-next-button").button({
						'text' : false,
						'disabled': ${ not hasNextStep },
						icons : {
							primary : 'ui-icon-triangle-1-e'
						}
					}).click( navigateNext );	
				}
				
				function initPreviousButton(){			
					$("#execute-previous-button").button({
						'text' : false,
						icons : {
							primary : 'ui-icon-triangle-1-w'
						}
					}).click( navigatePrevious );	
				}
				
				function initStopButton(){
					$("#execute-stop-button").button({
						'text': false, 
						'icons' : {
							'primary' : 'ui-icon-power'
						} 
					}).click(function(){
						window.close();
					});
					
				}
				
				function initFailButton(){
					$("#execute-fail-button").button({
						'text': false,
						'icons' :{
							'primary' : 'exec-status-failure'
						}
					}).click(function(){
						$.post('${ executeStatus }', {
							executionStatus : "FAILURE"
						},  setStatusFailure );
					});		
				}
				
				function initUntestableButton(){
					$("#execute-untestable-button").button({
						'text': false,
						'icons' :{
							'primary' : 'exec-status-untestable'
						}
					}).click(function(){
						$.post('${ executeStatus }', {
							executionStatus : "UNTESTABLE"
						},  setStatusUntestable );
					});		
				}
				
				function initBlockedButton(){
					$("#execute-blocked-button").button({
						'text': false,
						'icons' :{
							'primary' : 'exec-status-blocked'
						}
					}).click(function(){
						$.post('${ executeStatus }', {
							executionStatus : "BLOCKED"
						},  setStatusBlocked );
					});		
				}
				
				function initSuccessButton(){
					$("#execute-success-button").button({
						'text' : false,
						'icons' : {
							'primary' : 'exec-status-success'
						}
					}).click(function(){
						$.post('${ executeStatus }', {
							executionStatus : "SUCCESS"
						},  setStatusSuccess );
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
				
				function setStatusUntestable(){
					$("#execution-status-combo").val("UNTESTABLE");			
					statusComboChange();
					navigateNext();
				}
				
				function setStatusBlocked(){
					$("#execution-status-combo").val("BLOCKED");
					statusComboChange();
					navigateNext();
				}
				
				function statusComboSetIcon(){
					var cbox = $("#execution-status-combo");
					ComponentUtil.updateStatusCboxIcon(cbox);
				}
			
				function statusComboChange(){
					statusComboSetIcon();
					refreshExecStepInfos();
				}
				

				
				$(function(){
											
						basicwidg.init();
						
						initNextButton();
						initPreviousButton();
						initStopButton();
						<c:if test="${editable}">
						initUntestableButton();
						initBlockedButton();
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
						</c:if>
						$("#execute-next-test-case").button({
							'text': false,
							'disabled': !hasNextTestCase,
							icons: {
								primary : 'ui-icon-seek-next'
							}
						});
						
						<c:if test="${ not empty testPlanItemUrl }">
						$('#execute-next-test-case-panel').removeClass('not-displayed');		
						</c:if>
						
						$(window).unload( refreshParent );
						
						//issue #2069
						$.noBackspaceNavigation();	
						
						//issue #2195
						if (isIE){
							noCloseOnEnter();
						}
						
						//issue 3083 : propagate the information to the parent context
						eventBus.onContextual('context.bug-reported', function(event, json){
							window.opener.squashtm.workspace.eventBus.trigger(event, json )
						});
	                    <c:if test="${hasDenormFields}">
	                    $.getJSON("${denormalizedFieldsValuesURL}?denormalizedFieldHolderId=${executionStep.boundEntityId}&denormalizedFieldHolderType=${executionStep.boundEntityType}")
	                      .success(function(jsonDenorm){
	                       	 cufValues.infoSupport.init("#dfv-information-table", jsonDenorm, "jeditable");
	                      });
	                    </c:if>
	
	                    <c:if test="${hasCustomFields}">
	                    $.getJSON("${customFieldsValuesURL}?boundEntityId=${executionStep.boundEntityId}&boundEntityType=${executionStep.boundEntityType}")
	                      .success(function(jsonCufs){
	                        cufValues.infoSupport.init("#cuf-information-table", jsonCufs, 'jeditable');
	                      });
	                    </c:if>
				
					});	
				});
				
			});
			</script>

			<div id="execute-header">
				<table width="100%">
					<tr>
						<td class="centered"><button id="execute-stop-button">
								<f:message key="execute.header.button.stop.title" />
							</button>
						</td>
						<td class="centered">
							<button id="execute-previous-button">
								<f:message key="execute.header.button.previous.title" />
							</button> 
							<span id="execute-header-numbers-label">
								${executionStep.executionStepOrder+1} / ${totalSteps}
							</span>
							<button id="execute-next-button">
								<f:message key="execute.header.button.next.title" />
							</button>
						</td>
					<td class="centered not-displayed" id="execute-next-test-case-panel">
							<form action="<c:url value='${ testPlanItemUrl }/next-execution/runner?optimized=false' />" method="post">
								<f:message var="nextTestCaseTitle" key="execute.header.button.next-test-case.title" />
								<button id="execute-next-test-case" name="classic"  title="${ nextTestCaseTitle }">
									${ nextTestCaseTitle }
								</button>
							</form>
						</td>
						<td class="centered">
							<label id="evaluation-label-status">
								<f:message key="execute.header.status.label" />
							</label> 
							<c:choose>
							<c:when test="${editable }"><comp:execution-status-combo name="executionStatus" id="execution-status-combo" allowsUntestable="${allowsUntestable}" allowsSettled="${allowsSettled}"/>
							<c:if test="${allowsUntestable}">
							<button id="execute-untestable-button">
								<f:message key="execute.header.button.untestable.title" />
							</button>
							</c:if>
							<button id="execute-blocked-button">
								<f:message key="execute.header.button.blocked.title" />
							</button>							
							<button id="execute-fail-button">
								<f:message key="execute.header.button.failure.title" />
							</button>
							<button id="execute-success-button">
								<f:message key="execute.header.button.passed.title" />
							</button>
							</c:when>
							<c:otherwise>
							<jq:execution-status status="${executionStep.executionStatus.canonicalStatus}" /> 
							</c:otherwise>
							</c:choose>
						</td>
	
					</tr>
				</table>
			</div>

			<div id="execute-body" class="execute-fragment-body">
        <c:if test="${ hasCustomFields or hasDenormFields }">
          <comp:toggle-panel id="custom-fields-panel" titleKey="title.step.fields" open="true">
            <jsp:attribute name="body"> 
              <div id="dfv-information-table" class="display-table"></div>
              <div id="cuf-information-table" class="display-table"></div>
            </jsp:attribute>
          </comp:toggle-panel>
        </c:if>
				<comp:toggle-panel id="execution-action-panel"
					titleKey="execute.panel.action.title" 
					open="true">
					<jsp:attribute name="body">
						<div id="execution-action">${executionStep.action}</div>
					</jsp:attribute>
				</comp:toggle-panel>

				<comp:toggle-panel id="execution-expected-result-panel"
					titleKey="execute.panel.expected-result.title"
					open="true">
					<jsp:attribute name="body">
						<div id="execution-expected-result">${executionStep.expectedResult}</div>
					</jsp:attribute>
				</comp:toggle-panel>

				<div id="execute-evaluation">

					<div id="execute-evaluation-leftside">
						<c:if test="${editable}">
							<comp:rich-jeditable targetUrl="${executeComment}"
								componentId="execution-comment"/>
						</c:if>
						<comp:toggle-panel id="execution-comment-panel"
							titleKey="execute.panel.comment.title" 
							open="true">
							<jsp:attribute name="body">
								<div id="execution-comment">${executionStep.comment}</div>
							</jsp:attribute>
						</comp:toggle-panel>
					</div>

					<div id="execute-evaluation-rightside">
						<comp:step-information-panel auditableEntity="${executionStep}" entityUrl="${executeInfos}/general"/>
					</div>
					<div style="clear: both; visibility: hidden"></div>
				</div>

				<at:attachment-bloc attachListId="${executionStep.attachmentList.id}" workspaceName="campaign" editable="${ editable }" attachmentSet="${attachments}" />

				<%------------------------------ bugs section -------------------------------%>
				<%-- this section is loaded asynchronously. The bugtracker might be out of reach indeed. --%>
        		<div id="bugtracker-section-div">
        		</div>
        		 <script type="text/javascript">
        		 require(["common"], function() {
        			 require(["jquery", "app/ws/squashtm.notification"], function($, wtf) {
        		 		$("#bugtracker-section-div").load("${btEntityUrl}");
        		 		wtf.init({});
        			 });
        		 });
        		</script>
				<%------------------------------ /bugs section -------------------------------%>
	
				
			</div>
		</body>
	</c:otherwise>
</c:choose>
</html>