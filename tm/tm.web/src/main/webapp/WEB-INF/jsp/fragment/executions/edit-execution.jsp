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
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>




<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" />
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ execution }">
	<c:set var="editable" value="${ true }" />
</authz:authorized>
<c:set var="automated"
	value="${ execution.executionMode == 'AUTOMATED' }" />


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

<%-------------------------- /urls ------------------------------%>
<script type="text/javascript">
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
	
	/* deletion success handler */
	function deleteExecutionSuccess(){
		$( '#delete-execution-confirm' ).dialog( 'close' );
// 		document.location.href="${ parentUrl }" ;
		history.back();
	}
	
	/* deletion failure handler */
	function deleteExecutionFailure(xhr){
		$.squash.openMessage("<f:message key='popup.title.error' />", xhr.statusText);		
	}

	/* simple initialization for simple components */
	$(function(){
		$('#delete-execution-button').button();

		$("#back").button().click(function(){
			history.back();
		});
		
		
	});

</script>

<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header">

	<div style="float: left; height: 100%; width: 90%;">
		<h2>
			<span><f:message key="execution.execute.header.title" />&nbsp;:&nbsp;</span>
					<a id="execution-name" href="${ executionUrl }">&#35;<c:out
						value="${executionRank} - ${ execution.name }" escapeXml="true" />
					</a>
		</h2>
	</div>

	<div class="snap-right">
		<f:message var="back" key="label.Back" />
		<input id="back" type="button" value="${ back }" class="button" />
	</div>


	<div style="clear: both;"></div>
</div>

<div class="fragment-body">

	<div id="execution-toolbar" class="toolbar-class ui-corner-all ">
		<div class="toolbar-information-panel">
			<div id="general-informations-panel">
				<comp:execution-information-panel auditableEntity="${execution}" />
			</div>
		</div>
		<div class="toolbar-button-panel">
		<c:if test="${ editable }">
				<comp:execution-execute-buttons execution="${ execution }"/>
				<input type="button"
					value='<f:message key="execution.execute.remove.button.label" />'
					id="delete-execution-button" />
			</c:if>
		</div>
		<div style="clear: both;"></div>
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
				<label class="display-table-cell" for="testcase-description"><f:message key="label.Description" /></label>
				<div id="testcase-description" class="display-table-cell">${ execution.tcdescription }</div>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-reference"><f:message key="test-case.reference.label" /></label>
				<div id="testcase-reference" class="display-table-cell">${ execution.reference }</div>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-importance"><f:message key="test-case.importance.label" /></label>
				<div id="testcase-importance" class="display-table-cell"><comp:level-message level="${ execution.importance }"/></div>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-nature"><f:message key="test-case.nature.label" /></label>
				<div id="testcase-nature" class="display-table-cell"><comp:level-message level="${ execution.nature }"/></div>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-type"><f:message key="test-case.type.label" /></label>
				<div id="testcase-type" class="display-table-cell"><comp:level-message level="${ execution.type }"/></div>
			</div>
			<div class="display-table-row">
				<label class="display-table-cell" for="testcase-status"><f:message key="test-case.status.label" /></label>
				<div id="testcase-status" class="display-table-cell"><comp:level-message level="${ execution.status }"/></div>
			</div>
			<comp:denormalized-field-values-list denormalizedFieldValues="${ denormalizedFieldValues }" />
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
		<table id="execution-execution-steps-table">
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
		<comp:rich-jeditable targetUrl="${ executionUrl }" componentId="execution-description" />
	</c:if>
	<f:message var="executionComment" key="execution.description.panel.title" />
	<comp:toggle-panel id="execution-description-panel" title="${executionComment}"  open="false">
		<jsp:attribute name="body">
		<div id="execution-description">${ execution.description }</div>
	</jsp:attribute>
	</comp:toggle-panel>

	<%------------------------------ Attachments bloc ---------------------------------------------%>

	<at:attachment-bloc  workspaceName="campaign" editable="${ editable }" 
						 attachListId="${execution.attachmentList.id}" attachmentSet="${attachmentSet}" />


	<%------------------------------ bugs section -------------------------------%>

	<div id="bugtracker-section-div"></div>

	<%------------------------------ /bugs section -------------------------------%>

	<%--------------------------- Deletion confirmation popup -------------------------------------%>


	<pop:popup id="delete-execution-confirm"
		titleKey="dialog.delete-execution.title" isContextual="true"
		openedBy="delete-execution-button">
		<jsp:attribute name="buttons">
		<f:message var="label" key="label.Confirm" />
		'${ label }': function() {
			var url = "${executionUrl}";
			<jq:ajaxcall url="url" httpMethod="DELETE"
				successHandler="deleteExecutionSuccess"
				errorHandler="deleteExecutionFailure">					
			</jq:ajaxcall>
		},			
		<pop:cancel-button />
	</jsp:attribute>
		<jsp:attribute name="body">
		<b><f:message key="dialog.delete-execution.message" />
			</b>
		<br />				
	</jsp:attribute>
	</pop:popup>

	<%--------------------------- /Deletion confirmation popup -------------------------------------%>
	<f:message var="statusUntestable" key="execution.execution-status.UNTESTABLE" />
	<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
	<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
	<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
	<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
	<f:message var="statusReady" key="execution.execution-status.READY" />

	<script type="text/javascript">
	
	$(function(){
		require(["squashtable", "jquery.squash.jeditable"], function(){
			
			
			// ************** execution table *********************
			var tableSettings = {
				"oLanguage":{
					"sLengthMenu": '<f:message key="generics.datatable.lengthMenu" />',
					"sZeroRecords": '<f:message key="generics.datatable.zeroRecords" />',
					"sInfo": '<f:message key="generics.datatable.info" />',
					"sInfoEmpty": '<f:message key="generics.datatable.infoEmpty" />',
					"sInfoFiltered": '<f:message key="generics.datatable.infoFiltered" />',
					"oPaginate":{
						"sFirst":    '<f:message key="generics.datatable.paginate.first" />',
						"sPrevious": '<f:message key="generics.datatable.paginate.previous" />',
						"sNext":     '<f:message key="generics.datatable.paginate.next" />',
						"sLast":     '<f:message key="generics.datatable.paginate.last" />'
					}
				},	
				"sAjaxSource": "${executionStepsUrl}", 
				"aoColumnDefs": ${stepsAoColumnDefs},
			};
			
			var squashSettings = {
					
				enableHover : true
				<c:if test="${ !automated }">
				,executionStatus : {
					untestable : "${statusUntestable}",
					blocked : "${statusBlocked}",
					failure : "${statusFailure}",
					success : "${statusSuccess}",
					running : "${statusRunning}",
					ready : "${statusReady}"
				}	</c:if>
			};
		
			
			
			<c:if test="${ editable }">
			squashSettings.richEditables = {
				conf : {
					ckeditor : { customConfig : '${ ckeConfigUrl }', language: '<f:message key="rich-edit.language.value" />' },
					placeholder: '<f:message key="rich-edit.placeholder" />',
					submit: '<f:message key="rich-edit.button.ok.label" />',
					cancel: '<f:message key="label.Cancel" />',
					indicator : '<div class="processing-indicator" />' 				
				},
				targets : {
					"rich-editable-comment" : "${ executionStepsUrl }/{entity-id}/comment"
				}
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
			
			$("#execution-execution-steps-table").squashTable(tableSettings, squashSettings);
			
			
			// ************** bugtracker section ******************************
		 	
		 	$("#bugtracker-section-div").load("${btEntityUrl}");
			
			
		 	// ************** handle for refershing the page (called by the execution popup) ******************
		 	
		 	squashtm.execution = squashtm.execution || {};
		 	squashtm.execution.refresh = $.proxy(function(){
		 		$("#execution-execution-steps-table").squashTable().refresh();	
		 		$("#general-informations-panel").load("${executionInfoUrl}");
		 	}, window);

		});
	});
	</script>

</div>

