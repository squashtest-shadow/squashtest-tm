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
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>

<f:message var="squashlocale" key="squashtm.locale" />

<comp:datepicker-manager locale="${squashlocale}" />

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="iterationUrl" value="/iterations/{iterId}">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="iterationInfoUrl" value="/iterations/{iterId}/general">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="iterationPlanningUrl" value="/iterations/{iterId}/planning">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="iterationTestPlanUrl" value="/iterations/{iterId}/test-plan">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="iterationStatisticsUrl" value="/iterations/{iterId}/statistics">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="testPlanManagerUrl"
	value="/iterations/{iterId}/test-plan-manager">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="testCasesUrl" value="/iterations/{iterId}/test-plan">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="nonBelongingTestCasesUrl"
	value="/iterations/{iterId}/non-belonging-test-cases">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="assignableUsersUrl"
	value="/iterations/{iterId}/assignable-users">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="assignableStatusUrl"
	value="/iterations/{iterId}/assignable-statuses">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="assignTestCasesUrl"
	value="/iterations/{iterId}/batch-assign-user">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<c:url var="testCaseDetailsBaseUrl"
	value="/test-case-libraries/1/test-cases" /><%-- FIXME this url looks wrong but not used where it's passed --%>

<c:url var="workspaceUrl" value="/campaign-workspace/#" />

<s:url var="testCaseExecutionsUrl"
	value="/iterations/{iterId}/test-case-executions/">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="updateTestCaseUrl" value="/iterations/{iterId}/test-case/">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="simulateDeletionUrl"
	value="/campaign-browser/delete-iterations/simulate" />

<s:url var="confirmDeletionUrl"
	value="/campaign-browser/delete-iterations/confirm" />

<s:url var="testSuitesUrl" value="/iterations/{iterId}/test-suites">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>


<s:url var="btEntityUrl" value="/bugtracker/iteration/{id}">
	<s:param name="id" value="${iteration.id}" />
</s:url>


<c:url var="customFieldsValuesURL" value="/custom-fields/values" />

<f:message var='deleteMessageStart' key='dialog.label.delete-node.label.start'/>
<f:message var="deleteMessage" key="dialog.label.delete-nodes.iteration.label" />
<f:message var='deleteMessageCantBeUndone' key='dialog.label.delete-node.label.cantbeundone'/>
<f:message var='deleteMessageConfirm' key='dialog.label.delete-node.label.confirm'/>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE"
	domainObject="${ iteration }">
	<c:set var="writable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH"
	domainObject="${ iteration }">
	<c:set var="attachable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="SMALL_EDIT"
	domainObject="${ iteration }">
	<c:set var="smallEditable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE"
	domainObject="${ iteration }">
	<c:set var="deletable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE"
	domainObject="${ iteration }">
	<c:set var="creatable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"
	domainObject="${ iteration }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
	domainObject="${ iteration }">
	<c:set var="executable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>

<script type="text/javascript">
	function refreshIterationInfos() {
		$('#general-informations-panel').load('${iterationInfoUrl}');
	}
</script>

<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header">
	<div style="float: left; height: 100%;">
		<h2>
			<span><f:message key="iteration.header.title" />&nbsp;:&nbsp;</span><a
				id="iteration-name" href="${ iterationUrl }/info"><c:out
					value="${ iteration.name }" escapeXml="true" />
			</a>
		</h2>
	</div>

	<div style="clear: both;"></div>
	<c:if test="${ smallEditable }">
		<pop:popup id="rename-iteration-dialog"
			titleKey="dialog.rename-iteration.title" isContextual="true"
			openedBy="rename-iteration-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.rename-iteration.title" />
				'${ label }': function() {
					var url = "${ iterationUrl }";
					<jq:ajaxcall url="url" dataType="json" httpMethod="POST"
					useData="true" successHandler="renameIterationSuccess">				
						<jq:params-bindings newName="#rename-iteration-name" />
					</jq:ajaxcall>					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:attribute name="body">
				<script type="text/javascript">
					$("#rename-iteration-dialog").bind("dialogopen",
							function(event, ui) {
								var name = $.trim($('#iteration-name').text());
								$("#rename-iteration-name").val(name);

							});
				</script>			
				<label><f:message key="dialog.rename.label" />
				</label>
				<input type="text" id="rename-iteration-name" maxlength="255" size="50" />
				<br />
				<comp:error-message forField="name" />	
		
			</jsp:attribute>
		</pop:popup>
	</c:if>
</div>

<div id="iteration-toolbar" class="toolbar-class ui-corner-all ">
	<div class="toolbar-information-panel">
		<div id="general-informations-panel">
			<comp:general-information-panel auditableEntity="${iteration}" />
		</div>
	</div>
	<div class="toolbar-button-panel">
		<c:if test="${ executable && iteration.project.testAutomationEnabled }">
			<comp:execute-auto-button url="${ iterationUrl }" testPlanTableId="test-plans-table"/>
		
		</c:if>
		<c:if test="${ writable }">
			<input type="button"
				value=' <f:message key="iteration.test-plan.testsuite.manage.label"/>'
				id="manage-test-suites-button" class="button" />
		</c:if>
		<c:if test="${ smallEditable }">
			<input type="button"
				value='<f:message key="iteration.button.rename.label" />'
				id="rename-iteration-button" class="button" />
		</c:if>
		<c:if test="${ deletable }">
			<input type="button"
				value='<f:message key="iteration.button.remove.label" />'
				id="delete-iteration-button" class="button" />
		</c:if>
		
	</div>
	<div style="clear: both;"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ iterationUrl }" isContextual="${ ! param.isInfoPage }" />
	</c:if>
</div>

<div class="fragment-tabs fragment-body">
	<ul>
		<li><a href="#tabs-1"><f:message key="tabs.label.information" />
		</a>
		</li>
		<li><a href="#tabs-2"><f:message key="tabs.label.test-plan" />
		</a>
		</li>
		<li><a href="#tabs-3"><f:message key="label.Attachments" />
				<c:if test="${ iteration.attachmentList.notEmpty }">
					<span class="hasAttach">!</span>
				</c:if>
		</a>
		</li>
	</ul>
	<div id="tabs-1">

		<c:if test="${ smallEditable }">
			<comp:rich-jeditable targetUrl="${ iterationUrl }"
				componentId="iteration-description"
				submitCallback="refreshIterationInfos" />
		</c:if>

		<comp:toggle-panel id="iteration-description-panel"
			classes="information-panel" titleKey="label.Description"
			isContextual="true" open="true">
			<jsp:attribute name="body">
			<div id="iteration-description">${ iteration.description }</div>
			</jsp:attribute>
		</comp:toggle-panel>


		
		<%----------------------------------- Custom Fields -----------------------------------------------%>
		
		<comp:toggle-panel id="iteration-custom-fields" 
			titleKey="generics.customfieldvalues.title" isContextual="true"
			open="${hasCUF}">
			<jsp:attribute name="body">
				<div id="iteration-custom-fields-content" class="display-table">
				<div class="waiting-loading minimal-height"></div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		


		<%--------------------------- Planning section ------------------------------------%>

		<comp:toggle-panel id="datepicker-panel"
			titleKey="label.Planning" isContextual="true"
			open="true">
			<jsp:attribute name="body">
			<div class="datepicker-panel">
			<table class="datepicker-table">
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker
									fmtLabel="dialog.label.iteration.scheduled_start.label"
									url="${iterationPlanningUrl}" datePickerId="scheduled-start"
									paramName="scheduledStart" isContextual="true"
									postCallback="refreshIterationInfos"
									initialDate="${iteration.scheduledStartDate.time}"
									editable="${ smallEditable }">	
					</comp:datepicker>
				</td>
				<td class="datepicker-table-col">
	
					<comp:datepicker-auto datePickerId="actual-start"
									url="${iterationPlanningUrl}"
									fmtLabel="dialog.label.iteration.actual_start.label"
									paramName="actualStart" autosetParamName="setActualStartAuto"
									isAuto="${iteration.actualStartAuto}"
									postCallback="refreshIterationInfos"
									initialDate="${iteration.actualStartDate.time}"
									isContextual="true" editable="${ smallEditable }"
									jsVarName="actualStart">
					</comp:datepicker-auto>
				</td>
			</tr>
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker
									fmtLabel="dialog.label.iteration.scheduled_end.label"
									url="${iterationPlanningUrl}" datePickerId="scheduled-end"
									paramName="scheduledEnd" isContextual="true"
									postCallback="refreshIterationInfos"
									initialDate="${iteration.scheduledEndDate.time}"
									editable="${ smallEditable }">	
					</comp:datepicker>				
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto datePickerId="actual-end"
									url="${iterationPlanningUrl}"
									fmtLabel="dialog.label.iteration.actual_end.label"
									paramName="actualEnd" autosetParamName="setActualEndAuto"
									isAuto="${iteration.actualEndAuto}"
									postCallback="refreshIterationInfos"
									initialDate="${iteration.actualEndDate.time}"
									isContextual="true" editable="${ smallEditable }"
									jsVarName="actualEnd">
					</comp:datepicker-auto>
				</td>
			</tr>
			</table>
			</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		<%-- ------------------ statistiques --------------------------- --%>
		<comp:statistics-panel statisticsEntity="${ statistics }" statisticsUrl="${ iterationStatisticsUrl }"/>
	</div>
	<div id="tabs-2" class="table-tab">

		<%-- ------------------ test plan ------------------------------ --%>


		<div class="toolbar">
			<c:if test="${ linkable }">
				<f:message var="associateLabel"
					key="label.Add" />
				<f:message var="removeLabel"
					key="label.Remove" />
				<f:message var="assignLabel"
					key="label.Assign" />
				<f:message var="manageTS" key='menu.test-suites.button.main' />
				<input id="test-case-button" type="button" value="${associateLabel}"
					class="button" />
				<input id="remove-test-case-button" type="button"
					value="${removeLabel}" class="button" />
				<input id="assign-test-case-button" type="button"
					value="${assignLabel}" class="button" />
				<input id="manage-test-suites-menu" type="button"
					value="${manageTS}" class="button" />
			</c:if>
		</div>

		<div class="table-tab-wrap">


			<aggr:decorate-iteration-test-plan-table
				tableModelUrl="${iterationTestPlanUrl}"
				testPlanDetailsBaseUrl="${testCaseDetailsBaseUrl}"
				testPlansUrl="${testCasesUrl}"
				batchRemoveButtonId="remove-test-case-button"
				updateTestPlanUrl="${updateTestCaseUrl}"
				assignableUsersUrl="${assignableUsersUrl}"
				assignableStatusUrl="${assignableStatusUrl}"
				nonBelongingTestPlansUrl="${nonBelongingTestCasesUrl}"
				testPlanExecutionsUrl="${testCaseExecutionsUrl}"
				editable="${ linkable }"
				executable="${ executable }"
				testCaseMultipleRemovalPopupId="delete-multiple-test-plan-dialog"
				baseIterationURL="${iterationUrl}"
				testCaseSingleRemovalPopupId="delete-single-test-plan-dialog" />
			<aggr:iteration-test-plan-table />
		</div>

		<%--------------------------- Deletion confirmation pup for Test plan section ------------------------------------%>
		<c:if test="${ linkable }">
			<pop:popup id="delete-multiple-test-plan-dialog"
				openedBy="remove-test-case-button" isContextual="true" 
				titleKey="dialog.remove-testcase-associations.title">
				<jsp:attribute name="buttons">
		<f:message var="label" key="label.Yes" />
				'${ label }' : function(){
					$("#delete-multiple-test-plan-dialog").data("answer","yes");
					$("#delete-multiple-test-plan-dialog").dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
				<jsp:attribute name="body">
	<f:message var="emptyMessage"
						key="message.EmptyTableSelection" />			
	<script type="text/javascript">
		$("#delete-multiple-test-plan-dialog").bind(
				"dialogopen",
				function(event, ui) {
					var table = $('#test-plans-table').squashTable();
					var ids = table.getSelectedIds();

					if (ids.length == 0) {
						$.squash.openMessage(
								"<f:message key='popup.title.error' />",
								"${emptyMessage}");
						$(this).dialog('close');
					}

				});
	</script>
		<f:message key="dialog.remove-testcase-associations.message" />
	</jsp:attribute>
			</pop:popup>

		</c:if>
		<%-- ------------------------- /Deletion confirmation popup for Test plan section --------------------------------- --%>

		<%-- ----------------------------------- Test Suite Management -------------------------------------------------- --%>
		<c:if test="${ writable }">
			<!-- here the deletable attribute concern the iteration because it has the same impact so far on the appearance the deletion button for a test suite. -->
			<!-- it is unlikely but for more specific right management we will have to check the right of the user on the selected test suites in the popup -->
			<comp:test-suite-managment suiteList="${iteration.testSuites}"
				popupOpener="manage-test-suites-button" creatable="${ creatable }"
				deletable="${ deletable }" popupId="manage-test-suites-popup"
				menuId="manage-test-suites-menu" testSuitesUrl="${testSuitesUrl}"
				datatableId="test-plans-table"
				emptySelectionMessageId="test-plan-empty-sel-msg" />

			<div id="test-plan-empty-sel-msg" class="not-visible"
				title="<f:message key='iteration.test-plan.action.title' />">
				<div>
					<f:message key="iteration.test-plan.action.empty-selection.message" />
				</div>
			</div>
		</c:if>
		<%-- ----------------------------------- /Test Suite Management -------------------------------------------------- --%>
	</div>

	<%------------------------------ Attachments bloc ------------------------------------------- --%>
	
	<comp:attachment-tab tabId="tabs-3" entity="${ iteration }"	editable="${ attachable }" />




	<%-- ---------------------deletion popup------------------------------ --%>
	<c:if test="${ deletable }">
	
		<comp:delete-contextual-node-dialog
		simulationUrl="${simulateDeletionUrl}"
		confirmationUrl="${confirmDeletionUrl}" itemId="${iteration.id}"
		successCallback="deleteIterationSuccess"
		openedBy="delete-iteration-button"
		titleKey="dialog.delete-iteration.title" />
	
	</c:if>

	<%--------------------------- Assign User popup -------------------------------------%>
	<c:if test="${writable}">
		<pop:popup id="batch-assign-test-case"
			titleKey="label.AssignUser" isContextual="true"
			openedBy="assign-test-case-button" closeOnSuccess="false">

			<jsp:attribute name="buttons">
		
			<f:message var="label" key="label.Assign" />
			'${ label }': function() {
				var url = "${assignTestCasesUrl}";
				var table = $( '#test-plans-table' ).squashTable();
				var ids = table.getSelectedIds();
		
				var user = $(".batch-select", this).val();
			
				$.post(url, { testPlanIds: ids, userId: user}, function(){
					refreshTestPlansWithoutSelection();
					$("#batch-assign-test-case").dialog('close');
				});
				
			},			
			<pop:cancel-button />
		</jsp:attribute>
			<jsp:attribute name="body">
			<script type="text/javascript">
				$("#batch-assign-test-case").bind("dialogopen",function(event, ui) {
					
					var table = $('#test-plans-table').squashTable();
					var ids = table.getSelectedIds();

					if (ids.length > 0) {
						var pop = this;
						$.get("${assignableUsersUrl}","json")
						.success(function(jsonList) {
							var select = $(".batch-select", pop);
							select.empty();
							for ( var i = 0; i < jsonList.length; i++) {
								select.append('<option value="'+jsonList[i].id+'">'
												+ jsonList[i].login
												+ '</option>');
							}
						});
					} else {
						$.squash.openMessage("<f:message key='popup.title.error' />", "${emptyMessage}");
						$(this).dialog('close');
					}

				});
			</script>
			<span><f:message key="message.AssignTestCaseToUser" />
				</span>
			<select class="batch-select">
			</select>
		</jsp:attribute>
		</pop:popup>
	</c:if>
</div>
<%------------------------------------------automated suite overview --------------------------------------------%>
<c:if test="${ executable && iteration.project.testAutomationEnabled }">		
	<comp:automated-suite-overview-popup />
	</c:if>
	<%------------------------------------------/automated suite overview --------------------------------------------%>
	

<comp:decorate-buttons />

 <f:message key="tabs.label.issues" var="tabIssueLabel"/>
<script type="text/javascript">

	var identity = { obj_id : ${iteration.id}, obj_restype : "iterations"  };
	
	
	require(["domReady", "require"], function(domReady, require){
		domReady(function(){
			require(["jquery", "contextual-content-handlers", "jquery.squash.fragmenttabs", "bugtracker"], function($, contentHandlers, Frag, bugtracker){

				var nameHandler = contentHandlers.getSimpleNameHandler();
				
				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#iteration-name";
				
				squashtm.contextualContent.addListener(nameHandler);
				
				<c:if test="${linkable}">
				$('#test-case-button').click(function() {
					document.location.href = "${testPlanManagerUrl}";
				});
				</c:if>			

				
				//****** tabs configuration *******
				
				var fragConf = {
					beforeLoad : Frag.confHelper.fnCacheRequests	
				};
				Frag.init(fragConf);
				
				<c:if test="${iteration.project.bugtrackerConnected}">
				bugtracker.btPanel.load({
					url : "${btEntityUrl}",
					label : "${tabIssueLabel}"
				});
				</c:if>
				
				
				
				<c:if test="${hasCUF}">
				<%-- loading the custom field panel --%>
				$("#iteration-custom-fields").load("${customFieldsValuesURL}?boundEntityId=${iteration.boundEntityId}&boundEntityType=${iteration.boundEntityType}"); 				
				</c:if>	
				
			 	squashtm.execution = squashtm.execution || {};
			 	squashtm.execution.refresh = $.proxy(function(){
			 		$("#test-plans-table").squashTable().refresh();
			 		$('#general-informations-panel').load('${iterationInfoUrl}');
			 	}, window);
				
			});
		});
	});



	/* renaming success handler */
	function renameIterationSuccess(data) {
		var evt = new EventRename(identity, data.newName);
		squashtm.contextualContent.fire(null, evt);		
		refreshIterationInfos();
	}



	/* deletion success handler */
	function deleteIterationSuccess() {
		<c:choose>
		<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
		<c:when test="${param.isInfoPage}" >
		document.location.href = "${workspaceUrl}";
		</c:when>
		<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
		<c:otherwise>
		location.reload(true);
		</c:otherwise>
		</c:choose>
	}

	/* deletion failure handler */
	function deleteIterationFailure(xhr) {
		$.squash.openMessage("<f:message key='popup.title.error' />",xhr.statusText);
	}
		
</script>


