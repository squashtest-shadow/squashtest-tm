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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>


<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" />
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ execution }">
	<c:set var="editable" value="${ true }" />
</authz:authorized>


<%-- -----------------------------test automation ------------------------------ --%>

<c:set var="automated"	value="${ execution.executionMode == 'AUTOMATED' }" />
<c:set var="taDisassociated" value="${ automated and execution.automatedExecutionExtender.projectDisassociated}" />

<f:message var="taDisassociatedLabel" key="squashtm.itemdeleted"/>
<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="deleteExecutionButton" key="execution.execute.remove.button.label" />

<%-------------------------- urls ------------------------------%>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />

<c:url var="executionUrl" value="/executions/${execution.id}" />

<c:url var="baseExecuteUrl" value="/execute"/>

<s:url var="executionInfoUrl" value="/executions/{execId}/general">
	<s:param name="execId" value="${execution.id}" />
</s:url>
<c:choose>
	<c:when test="${automated}">
		<s:url var="executionStepsUrl" value="/executions/{execId}/auto-steps">
			<s:param name="execId" value="${execution.id}" />
		</s:url>
	</c:when>
	<c:otherwise>
		<s:url var="executionStepsUrl" value="/executions/{execId}/steps">
			<s:param name="execId" value="${execution.id}" />
		</s:url>
	</c:otherwise>
</c:choose>

<s:url var="stepAttachmentManagerUrl" value="/attach-list/" />

<s:url var="btEntityUrl" value="/bugtracker/execution/{id}">
	<s:param name="id" value="${execution.id}" />
</s:url>

<c:url var="customFieldsValuesURL" value="/custom-fields/values" />
<c:url var="denormalizedFieldsValuesURL" value="/denormalized-fields/values" />

<%-------------------------- /urls ------------------------------%>

<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header">

	<div style="float: left; height: 100%; width: 90%;">
		<h2>
					<a id="execution-name" href="${ executionUrl }">&#35;<c:out
						value="${executionRank} - ${ execution.name }" escapeXml="true" />
					</a>
		</h2>
	</div>

	<div class="snap-right">
		<f:message var="back" key="label.Back" />
		<input id="back" type="button" value="${ back }" class="sq-btn" />
	</div>


	<div class="unsnap"></div>
</div>

<div class="fragment-body">

	<div id="execution-toolbar" class="toolbar-class ui-corner-all ">
		<div class="toolbar-information-panel">
			<comp:execution-information-panel auditableEntity="${execution}" entityUrl="${executionUrl}"/>
		</div>
		<div class="toolbar-button-panel">
		<c:if test="${ editable }">
				<comp:execution-execute-buttons execution="${ execution }"/>
				<input type="button" class="sq-btn"
					value='${deleteExecutionButton}'
					id="delete-execution-button" />
			</c:if>
		</div>
		<div class="unsnap"></div>
    <c:if test="${ editable }">
      <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ executionUrl }" />
    </c:if>
	</div>

	<%----------------------------------- Information -----------------------------------------------%>
	
	<comp:toggle-panel id="execution-information-panel"
		titleKey="generics.information.title" 
		open="true">
		<jsp:attribute name="body">
		<div id="execution-information-table" class="display-table">
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-reference"><f:message key="test-case.reference.label" /></label>
				<div id="testcase-reference" class="display-table-cell">${ execution.reference }</div>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-description"><f:message key="label.Description" /></label>
				<div id="testcase-description" class="display-table-cell">${ execution.tcdescription }</div>
			</div>
		
		
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-status"><f:message key="test-case.status.label" /></label>
				<div  class="display-table-cell">
<span id="test-case-status-icon" class="test-case-status-${ execution.status }"> &nbsp </span> <span id="test-case-status"><comp:level-message level="${ execution.status }"/></span>
</div>
			</div>
<c:if test="${execution.automated}">
			<div class="display-table-row">
				<label class="display-table-cell" for="automated-script"><f:message key="test-case.testautomation.section.label" /></label>
				<div class="display-table-cell" id="automated-script" >${ taDisassociated ? taDisassociatedLabel : execution.automatedExecutionExtender.automatedTest.fullLabel }</div>
			</div>
</c:if>			
		</div>
	</jsp:attribute>
	</comp:toggle-panel>
	
	
		
	<%----------------------------------- Attribute -----------------------------------------------%>
	
	<comp:toggle-panel id="test-case-attribut-panel"
				titleKey="label.Attributes"
				   open="true">
				   
	<jsp:attribute name="body">
	<div id="test-case-attribut-table"  class="display-table">
<div class="display-table-row">
			<label for="test-case-importance" class="display-table-cell"><f:message key="test-case.importance.combo.label" /></label>
			<div class="display-table-cell">
			<span id="test-case-importance-icon" class="test-case-importance-${ execution.importance }">&nbsp</span>	<span id="test-case-importance"><comp:level-message level="${ execution.importance }"/></span>
			</div>
		</div>
		
		<div class="display-table-row">
				<label class="display-table-cell" for="testcase-nature"><f:message key="test-case.nature.label" /></label>
				<div id="testcase-nature" class="display-table-cell"><comp:level-message level="${ execution.nature }"/></div>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-type"><f:message key="test-case.type.label" /></label>
				<div id="testcase-type" class="display-table-cell"><comp:level-message level="${ execution.type }"/></div>
			</div>
		
		</div>
	</jsp:attribute>
</comp:toggle-panel>
	
	<%----------------------------------- Prerequisites -----------------------------------------------%>

	<comp:toggle-panel id="execution-prerequisite-panel"
		titleKey="generics.prerequisite.title" 
		open="${ not empty execution.prerequisite }">
		<jsp:attribute name="body">
		<div id="execution-prerequisite-table" class="display-table">
			<div class="display-table-row">
				<div class="display-table-cell">${ execution.prerequisite }</div>
			</div>
		</div>
	</jsp:attribute>
	</comp:toggle-panel>
	
	

	<%----------------------------------- result summary -----------------------------------------------%>

	<c:if test="${execution.automated}">
	<comp:toggle-panel id="auto-execution-result-summary-panel"
		titleKey="label.resultSummary" 
		open="${ not empty execution.resultSummary }">
		<jsp:attribute name="body">
			<span>${execution.resultSummary}</span>
		</jsp:attribute>
	</comp:toggle-panel>
	</c:if>

	<%---------------------------- execution step summary status --------------------------------------%>

	<comp:toggle-panel id="execution-steps-panel"
		titleKey="executions.execution-steps-summary.panel.title"
		open="true">
		<jsp:attribute name="body">
		<table id="execution-execution-steps-table" class="unstyled-table">
			<thead>
				<tr>
					<th>Id(masked)</th>
					<th><f:message
								key="executions.steps.table.column-header.rank.label" />
						</th>
					<c:forEach var="label" items="${stepsDfvsLabels}">
						<th>${label}</th>
					</c:forEach>
					<th><f:message
								key="executions.steps.table.column-header.action.label" />
						</th>		
					<th><f:message
								key="executions.steps.table.column-header.expected-result.label" />
						</th>
					<th><f:message
								key="executions.steps.table.column-header.status.label" />
						</th>
					<th><f:message
								key="executions.steps.table.column-header.last-execution.label" />
						</th>
					<th><f:message
								key="executions.steps.table.column-header.user.label" />
						</th>		
					<th><f:message
								key="executions.steps.table.column-header.comment.label" />
						</th>
					<th>bug list (masked)</th>
					<th><f:message
								key="executions.steps.table.column-header.bugged.label" />
						</th>
					<th>numberOfAttch(masked)</th>
					<th><f:message
								key="executions.steps.table.column-header.attachment.label" />
					</th>
					<th><f:message		key="label.short.execute" /></th>
				</tr>
			</thead>
			<tbody>
				<%-- Will be populated through ajax --%>
			</tbody>
		</table>
		<br />
	</jsp:attribute>
	</comp:toggle-panel>

	<%-------------------------------------- Comment --------------------------------------------------%>

	<c:if test="${ editable }">
        <c:set var="descrRicheditAttributes" value="class='editable rich-editable' data-def='url=${executionUrl}'"/>
	</c:if>
	<f:message var="executionComment" key="execution.description.panel.title" />
	<comp:toggle-panel id="execution-description-panel" title="${executionComment}"  open="false">
		<jsp:attribute name="body">
		<div id="execution-description" ${descrRicheditAttributes} >${ execution.description }</div>
	</jsp:attribute>
	</comp:toggle-panel>

	<%------------------------------ Attachments bloc ---------------------------------------------%>

	<at:attachment-bloc  workspaceName="campaign" editable="${ editable }" 
						 attachListId="${execution.attachmentList.id}" attachmentSet="${attachmentSet}" />


	<%------------------------------ bugs section -------------------------------%>

	<div id="bugtracker-section-div"></div>

	<%------------------------------ /bugs section -------------------------------%>
	<%--------------------------- Deletion confirmation popup -------------------------------------%>
  
    <f:message var="deletionDialogTitle" key="dialog.delete-execution.title" />
    <div id="delete-execution-dialog" class="popup-dialog not-displayed" title="${deletionDialogTitle}">
    
        <span style="font-weight:bold;"><f:message key="dialog.delete-execution.message" /></span>
        
        <div class="popup-dialog-buttonpane">
          <input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn"/>
          <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>        
        </div>    
    </div>


	<%--------------------------- /Deletion confirmation popup -------------------------------------%>
	<f:message var="statusSettled" key="execution.execution-status.SETTLED" />
	<f:message var="statusUntestable" key="execution.execution-status.UNTESTABLE" />
	<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
	<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
	<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
	<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
	<f:message var="statusReady" key="execution.execution-status.READY" />

	<script type="text/javascript">
	var squashtm = squashtm || {};
	
	require(["common"], function() {
		require(["jquery", "page-components/execution-information-panel", "custom-field-values", 
		         "app/ws/squashtm.notification", 
		         "squashtable", "jquery.squash.formdialog",
		         "jquery.squash.jeditable"], function($, infopanel, cufValuesManager, notification) {	
			
			<%--
			(scoped) variable that will publish useful functions to various stakeholders (use the 
			search function if you need to see where, I'm not documenting it because 
			that should be refactored anyway)
            --%>
			squashtm.execution = squashtm.execution || {}
			
			// --------- renaming handler ------------
			
			/* display the execution name. Used for extern calls (like from the page who will include this fragment)
			*  will refresh the general informations as well*/
			function nodeSetName(name){
				var fullname = name;
				$('#execution-name').html(fullname);		
			}
			
			/* renaming success handler */
			function renameExecutionSuccess(data){
				nodeSetName(data.newName);								
				$( '#rename-execution-dialog' ).dialog( 'close' );
			}
			
			
			// ============== deletion dialog ================
			
			var deldialog = $("#delete-execution-dialog");
			deldialog.formDialog();
			
			deldialog.on('formdialogconfirm', function(){
				$.ajax({
	              url : "${executionUrl}", 
	              type : 'DELETE',
				}).success(function(){
					deldialog.formDialog('close');
					history.back();
				}).error(function(xhr){
					notification.showError(xhr.statusText);
				});	          
			});
			
			deldialog.on('formdialogcancel', function(){
				deldialog.formDialog('close');
			});

			$('#delete-execution-button').on('click', function(){
				deldialog.formDialog('open');
			});
			
			
			// ========= history =======

			$("#back").on('click', function(){history.back();});

			
			// ==== execution table ====
			var tableSettings = {
				"sAjaxSource": "${executionStepsUrl}", 
				"aoColumnDefs": ${stepsAoColumnDefs}, 
				"cufDefinitions": ${ json:marshall(stepsCufDefinitions) }
			};
			
			var squashSettings = {
				enableHover : true
			};			
			
			<c:if test="${ editable }">
			squashSettings.richEditables = {
				"rich-editable-comment" : "${ executionStepsUrl }/{entity-id}/comment"				
			};
			squashSettings.attachments = { 
				url : "${stepAttachmentManagerUrl}/{attach-list-id}/attachments/manager?workspace=campaign"
			}
			
			</c:if>
			squashSettings.buttons = [
					{ tooltip : "<f:message key='label.run'/>",
						tdSelector : "td.run-step-button",
						uiIcon : "execute-arrow",
						onClick : function(table, cell){
							var executionId = "${execution.id}";
							var row = cell.parentNode.parentNode; // hopefully, that's the
							// 'tr' one
							var executionStepId = table.getODataId(row);
								var url = "${baseExecuteUrl}/"+executionId+"/step/"+executionStepId;
								var data = {
									'optimized' : 'false',
								};
								var winDef = {
									name : "classicExecutionRunner",
									features : "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised"
								};
								$.open(url, data, winDef);
						}
					},
					{ tooltip : "<f:message key='issue.button.opendialog.label' />",
						tdSelector : "td.bug-button",
						uiIcon : function(row, data){
							return (data["bug-list"].length>0)? "has-bugs" : "table-cell-add";
						},
						onClick : function(table, btnElt){			
							console.log('there');
							var row = btnElt.parentNode.parentNode; // hopefully, that's the
							// 'tr' one
							var executionStepId = table.getODataId(row);
							checkAndReportIssue( {
								reportUrl:squashtm.app.contextRoot+"/bugtracker/execution-step/"+executionStepId+"/new-issue", 
								callback:function(json){
									var btn = $(btnElt);
									btn.removeClass('table-cell-add')
											.addClass('has-bugs');
									issueReportSuccess(json);
								}
							} );
							
						}
					}
				];
			
			
		
			
			var cufColumnPosition = 2;
			var cufTableHandler = cufValuesManager.cufTableSupport;
			cufTableHandler.decorateDOMTable($("#execution-execution-steps-table"), tableSettings.cufDefinitions, cufColumnPosition);

			datatableSettings = cufTableHandler.decorateTableSettings(tableSettings, tableSettings.cufDefinitions,
					cufColumnPosition, true);
			
			$("#execution-execution-steps-table").squashTable(tableSettings, squashSettings);
			
			
			//==== cuf sections (if any)====

			<c:set var="cufdisplaymode" value="${editable ? 'jeditable' : 'static'}"/>
			
			<c:if test="${not empty executionDenormalizedValues}">
			var denoCufs = ${json:marshall(executionDenormalizedValues)};
			cufValuesManager.infoSupport.init("#execution-information-table", denoCufs, "${cufdisplaymode}");
			</c:if>
			
			<c:if test="${not empty executionCufValues}">
			var cufs = ${json:marshall(executionCufValues)};
			cufValuesManager.infoSupport.init("#execution-information-table", cufs, "${cufdisplaymode}");	
			</c:if>
			
			// ==== bugtracker section ====
		 	
		 	$("#bugtracker-section-div").load("${btEntityUrl}");
			
			
		 	// ==== handle for refershing the page (called by the execution popup) ====
		 	
		 	squashtm.execution.refresh = $.proxy(function(){
		 		$("#execution-execution-steps-table").squashTable().refresh();
		 		infopanel.refresh();
		 		//see execution-execute-button.tag	
       			 squashtm.execution.updateBtnlabelFromTable();
		 	}, window);
		});
	});
	</script>

</div>

