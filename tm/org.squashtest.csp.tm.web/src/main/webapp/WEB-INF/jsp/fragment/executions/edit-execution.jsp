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


<comp:rich-jeditable-header />
<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" />
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
	domainObject="${ execution }">
	<c:set var="editable" value="${ true }" />
</authz:authorized>
<c:set var="automated"
	value="${ execution.executionMode == 'AUTOMATED' }" />

<script type="text/javascript"
	src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.dateformat.js"></script>

<%-------------------------- urls ------------------------------%>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />

<c:url var="executionUrl" value="/executions/${execution.id}" />


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
			<span><f:message key="execution.execute.header.title" />&nbsp;:&nbsp;</span><a
				id="execution-name" href="${ executionUrl }/info">&#35;<c:out
					value="${executionRank} - ${ execution.name }" escapeXml="true" />
			</a>
		</h2>
	</div>

	<div style="float: right;">
		<f:message var="back" key="label.Back" />
		<input id="back" type="button" value="${ back }" class="button" />
	</div>


	<div style="clear: both;"></div>
	<c:if test="${ editable }">
		<comp:opened-object otherViewers="${ otherViewers }"
			objectUrl="${ executionUrl }" isContextual="false" />
	</c:if>
</div>

<div class="fragment-body">

	<div id="execution-toolbar" class="toolbar-class ui-corner-all ">
		<div class="toolbar-information-panel">
			<div id="general-informations-panel">
				<comp:execution-information-panel auditableEntity="${execution}" />
			</div>
		</div>
		<div class="toolbar-button-panel">
		<c:if test="${ editable && not execution.automated }">
				<comp:execution-execute-buttons execution="${ execution }"/>
				<input type="button"
					value='<f:message key="execution.execute.remove.button.label" />'
					id="delete-execution-button" />
			</c:if>
		</div>
		<div style="clear: both;"></div>
	</div>


	<%----------------------------------- Prerequisites -----------------------------------------------%>

	<comp:toggle-panel id="execution-prerequisite-panel"
		titleKey="generics.prerequisite.title" isContextual="true"
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
		titleKey="label.resultSummary" isContextual="true"
		open="${ not empty execution.resultSummary }">
		<jsp:attribute name="body">
			<span>${execution.resultSummary}</span>
		</jsp:attribute>
	</comp:toggle-panel>
	</c:if>

	<%---------------------------- execution step summary status --------------------------------------%>



	<comp:toggle-panel id="execution-steps-panel"
		titleKey="executions.execution-steps-summary.panel.title"
		isContextual="true" open="true">
		<jsp:attribute name="body">
		<table id="execution-execution-steps-table">
			<thead>
				<tr>
					<th>Id</th>
					<th><f:message
								key="executions.steps.table.column-header.rank.label" />
						</th>
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
					<th><f:message
								key="executions.steps.table.column-header.bugged.label" />
						</th>
					<th>numberOfAttch(masked)</th>
					<th><f:message
								key="executions.steps.table.column-header.attachment.label" />
						</th>
				</tr>
			</thead>
			<tbody>
				<%-- Will be populated through ajax --%>
			</tbody>
		</table>
		<br />
	</jsp:attribute>
	</comp:toggle-panel>



	<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
	<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
	<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
	<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
	<f:message var="statusReady" key="execution.execution-status.READY" />

	<script type="text/javascript">


	$(function(){
		
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
			"aoColumnDefs": [
			{'bVisible': false, 'bSortable': false, 'sWidth': '2em', 'aTargets': [0], 'mDataProp' : 'entity-id'},
			{'bVisible': true, 'bSortable': false, 'sWidth': '2em', 'sClass': 'select-handle centered', 'aTargets': [1], 'mDataProp' : 'entity-index'},
			{'bVisible': true, 'bSortable': false, 'aTargets': [2], 'mDataProp' : 'action'},
			{'bVisible': true, 'bSortable': false, 'aTargets': [3], 'mDataProp' : 'expected'},
			{'bVisible': true, 'bSortable': false, 'aTargets': [4], 'mDataProp' : 'status', 'sClass' : 'has-status'},
			{'bVisible': true, 'bSortable': false, 'aTargets': [5], 'mDataProp' : 'last-exec-on'},
			{'bVisible': true, 'bSortable': false, 'aTargets': [6], 'mDataProp' : 'last-exec-by'},
			{'bVisible': true, 'bSortable': false, 'aTargets': [7], 'sClass' : 'smallfonts rich-editable-comment', 'mDataProp' : 'comment'},
			{'bVisible': true, 'bSortable': false, 'sWidth': '2em', 'sClass': 'centered bugged-cell', 'aTargets': [8], 'mDataProp' : 'bugged'},
			{'bVisible': false, 'bSortable': false, 'aTargets': [9], 'mDataProp' : 'nb-attachments'},
			{'bVisible': ${editable}, 'bSortable': false, 'sWidth': '2em', 'sClass': 'centered has-attachment-cell', 'aTargets': [10], 'mDataProp' : 'attach-list-id'}
			]
		};
		
			var squashSettings = {
					
				enableHover : true
				<c:if test="${ !automated }">
				,executionStatus : {
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
				indicator : '<img src="${ pageContext.servletContext.contextPath }/scripts/jquery/indicator.gif" alt="processing..." />' 				
			},
			targets : {
				"rich-editable-comment" : "${ executionStepsUrl }/{entity-id}/comment"
			}
		};
		squashSettings.attachments = { 
			url : "${stepAttachmentManagerUrl}/{attach-list-id}/attachments/manager?workspace=campaign"
		}
		
		</c:if>
		
		
		$("#execution-execution-steps-table").squashTable(tableSettings, squashSettings);
	});
</script>

	<%-------------------------------------- Comment --------------------------------------------------%>

	<c:if test="${ editable }">
		<comp:rich-jeditable targetUrl="${ executionUrl }"
			componentId="execution-description" />
	</c:if>
	<f:message var="executionComment"
		key="execution.description.panel.title" />
	<comp:toggle-panel id="execution-description-panel"
		title="${executionComment}" isContextual="true" open="false">
		<jsp:attribute name="body">
		<div id="execution-description">${ execution.description }</div>
	</jsp:attribute>
	</comp:toggle-panel>

	<%------------------------------ Attachments bloc ---------------------------------------------%>

	<comp:attachment-bloc entity="${execution}" workspaceName="campaign"
		editable="${ editable }" />


	<%------------------------------ bugs section -------------------------------%>
	<%--
	this section is loaded asynchronously. The bugtracker might be out of reach indeed.
 --%>
	<script type="text/javascript">
 	$(function(){
 		$("#bugtracker-section-div").load("${btEntityUrl}");
 	});
 </script>
	<div id="bugtracker-section-div"></div>

	<%------------------------------ /bugs section -------------------------------%>


	<comp:decorate-buttons />

	<%--------------------------- Deletion confirmation popup -------------------------------------%>


	<comp:popup id="delete-execution-confirm"
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
		<jsp:body>
		<b><f:message key="dialog.delete-execution.message" />
			</b>
		<br />				
	</jsp:body>
	</comp:popup>

	<%--------------------------- /Deletion confirmation popup -------------------------------------%>

</div>







