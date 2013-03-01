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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>

<f:message var="squashlocale" key="squashtm.locale" />

<comp:rich-jeditable-header />
<comp:datepicker-manager locale="${squashlocale}" />

<c:url var="workspaceUrl" value="/campaign-workspace/#" />
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />

<s:url var="testSuiteUrl" value="/test-suites/{testSuiteId}">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="duplicateTestSuiteUrl"
	value="/iterations/{iterationId}/duplicateTestSuite/{testSuiteId}">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testSuiteInfoUrl" value="/test-suites/{testSuiteId}/general">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="baseIterationUrl" value="/iterations/{iterationId}">
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testSuiteStatisticsUrl"
	value="/test-suites/{testSuiteId}/statistics">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testSuiteExecButtonsUrl"
	value="/test-suites/{testSuiteId}/exec-button">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testSuiteTestPlanUrl"
	value="/test-suites/{testSuiteId}/test-plan/table">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="removeTestCaseUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/test-plan/remove">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="updateTestCaseUrl"
	value="/test-suites/{testSuiteId}/test-case/">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="assignableUsersUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/assignable-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="batchAssignableUsersUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/batch-assignable-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="assignTestCasesUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/batch-assign-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testPlanManagerUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/test-plan-manager">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="nonBelongingTestCasesUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/non-belonging-test-cases/remove">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testCaseExecutionsUrl"
	value="/test-suites/{testSuiteId}/{iterationId}/test-case-executions/">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<c:url var="testCaseDetailsBaseUrl"
	value="/test-case-libraries/1/test-cases" />

<s:url var="confirmDeletionUrl"
	value="/iterations/{iterationId}/test-suites/delete">
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>
<s:url var="btEntityUrl" value="/bugtracker/test-suite/{id}">
	<s:param name="id" value="${testSuite.id}" />
</s:url>


<c:url var="customFieldsValuesURL" value="/custom-fields/values" />


<f:message var='deleteMessageStart' key='dialog.label.delete-node.label.start'/>
<f:message var="deleteMessage" key="dialog.label.delete-nodes.test-suite.label" />
<f:message var='deleteMessageCantBeUndone' key='dialog.label.delete-node.label.cantbeundone'/>
<f:message var='deleteMessageConfirm' key='dialog.label.delete-node.label.confirm'/>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<%-- ----------------------------------- Authorization ----------------------------------------------%>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="SMALL_EDIT"
	domainObject="${ testSuite }">
	<c:set var="smallEditable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE"
	domainObject="${ testSuite }">
	<c:set var="deletable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE"
	domainObject="${ testSuite }">
	<c:set var="creatable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"
	domainObject="${ testSuite }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
	domainObject="${ testSuite }">
	<c:set var="executable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>



<script type="text/javascript">

	var identity = { obj_id : ${testSuite.id}, obj_restype : "test-suites"  };
	
	
	require(["domReady", "require"], function(domReady, require){
		domReady(function(){
			require(["jquery", "contextual-content-handlers"], function($, contentHandlers){
	
				var nameHandler = contentHandlers.getSimpleNameHandler();
				
				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#test-suite-name";
				
				squashtm.contextualContent.addListener(nameHandler);

				
			});
		});
	});
	
	
	function renameTestSuiteSuccess(data){
		var evt = new EventRename(identity, data.newName);
		squashtm.contextualContent.fire(null, evt);		
		refreshTestSuiteInfos();
	}
	
	
	/* Bind any changeable element to this handler to refresh the general informations */	
	function refreshTestSuiteInfos(){
		$('#general-informations-panel').load('${testSuiteInfoUrl}');	
	}

	
	/*post a request to duplicate the test suite*/
	function duplicateTestSuite(){
		return $.ajax({
			'url' : '${duplicateTestSuiteUrl}',
			type : 'POST',
			data : [],
			dataType : 'json'
		});
	};
	
	/*duplication sucess handler*/
	function duplicateTestSuiteSuccess(idOfDuplicate){
		<c:choose>
			<%-- if we were in a sub page context. We need to navigate back to the workspace. --%>
			<c:when test="${param.isInfoPage}" >	
			$.squash.openMessage('<f:message key="test-suite.duplicate.success.title" />', '<f:message key="test-suite.duplicate.success.message" />');
			</c:when>
			<c:otherwise>
				var destination = new SquashEventObject(${testSuite.iteration.id}, "iterations");
				var duplicate = new SquashEventObject( idOfDuplicate, "test-suites");
				var source = new SquashEventObject(${testSuite.id}, "test-suites");
				var evt = new EventDuplicate(destination, duplicate, source);
				squashtm.contextualContent.fire(null, evt);
			</c:otherwise>
		</c:choose>
		
	}


	/* deletion success handler */
	function deleteTestSuiteSuccess(){		
		<c:choose>
		<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
		<c:when test="${param.isInfoPage}" >		
			document.location.href="${workspaceUrl}" ;
		</c:when>
		<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
		<c:otherwise>
		location.reload(true);
		</c:otherwise>
		</c:choose>		
	}
	/* deletion failure handler */
	function deleteTestSuiteFailure(xhr){
		oneShotDialog("<f:message key='popup.title.error' />", xhr.statusText);
	}
</script>

<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header">
	<div style="float: left; height: 100%;">
		<h2>
			<span><f:message key="test-suite.header.title" />&nbsp;:&nbsp;</span><a
				id="test-suite-name" href="${ testSuiteUrl }/info"><c:out
					value="${ testSuite.name }" escapeXml="true" /> </a>
		</h2>
	</div>

	<div style="clear: both;"></div>
	<c:if test="${ smallEditable }">
		<comp:popup id="rename-test-suite-dialog"
			titleKey="dialog.testsuites.rename.title" isContextual="true"
			openedBy="rename-test-suite-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.testsuites.rename.title" />
				'${ label }': function() {
					var url = "${ testSuiteUrl }";
					<jq:ajaxcall url="url" dataType="json" httpMethod="POST"
					useData="true" successHandler="renameTestSuiteSuccess">				
						<jq:params-bindings newName="#rename-test-suite-name" />
					</jq:ajaxcall>					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<script type="text/javascript">
				$( "#rename-test-suite-dialog" ).bind( "dialogopen", function(event, ui) {
					var name = $.trim($('#test-suite-name').text());
					$("#rename-test-suite-name").val(name);
					
				});
				</script>			
				<label><f:message key="dialog.rename.label" />
				</label>
				<input type="text" id="rename-test-suite-name" maxlength="255" size="50" />
				<br />
				<comp:error-message forField="name" />	
		
			</jsp:body>
		</comp:popup>
	</c:if>
</div>

<div id="test-suite-toolbar" class="toolbar-class ui-corner-all ">
	<div class="toolbar-information-panel">
		<div id="general-informations-panel">
			<comp:general-information-panel auditableEntity="${testSuite}" />
		</div>
	</div>
	<div class="toolbar-button-panel">
		<c:if test="${ executable }">
			<div id="test-suite-execution-button" style="display: inline-block;">
				<comp:test-suite-execution-button testSuiteId="${ testSuite.id }"
					statisticsEntity="${ statistics }" />
			</div>
			<c:if test="${ testSuite.iteration.project.testAutomationEnabled }">
			<comp:execute-auto-button url="${ testSuiteUrl }" testPlanTableId="test-suite-test-plans-table"/>
			</c:if>
		</c:if>
		<c:if test="${ smallEditable }">
			<input type="button"
				value="<f:message key='test-suite.button.rename.label' />"
				id="rename-test-suite-button" class="button"
				style="display: inline-block;" />
		</c:if>
		<c:if test="${ deletable }">
			<input type="button"
				value="<f:message key='test-suite.button.remove.label' />"
				id="delete-test-suite-button" class="button"
				style="display: inline-block;" />
		</c:if>
		<c:if test="${ creatable }">
			<input type="button"
				value="<f:message key='test-suite.button.duplicate.label' />"
				id="duplicate-test-suite-button" class="button"
				style="display: inline-block;" />
		</c:if>
	</div>
	<div style="clear: both;"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }"
			objectUrl="${ testSuiteUrl }" isContextual="${ ! param.isInfoPage }" />
	</c:if>
</div>
<comp:fragment-tabs />
<div class="fragment-tabs fragment-body">
	<ul>
		<li><a href="#tabs-1"><f:message key="tabs.label.information" />
		</a>
		</li>
		<li><a href="#tabs-2"><f:message key="tabs.label.test-plan" />
		</a>
		</li>
		<li><a href="#tabs-3"><f:message key="label.Attachments" />
				<c:if test="${ testSuite.attachmentList.notEmpty }">
					<span class="hasAttach">!</span>
				</c:if>
		</a>
		</li>
	</ul>
	<div id="tabs-1">
		<c:if test="${ smallEditable }">
			<comp:rich-jeditable targetUrl="${ testSuiteUrl }"
				componentId="test-suite-description"
				submitCallback="refreshTestSuiteInfos" />
		</c:if>

		<comp:toggle-panel id="test-suite-description-panel"
			titleKey="label.Description" isContextual="true"
			open="${ not empty testSuite.description }">
			<jsp:attribute name="body">
				<div id="test-suite-description">${ testSuite.description }</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		
		<%----------------------------------- Custom Fields -----------------------------------------------%>
		
		<comp:toggle-panel id="test-suite-custom-fields"
			titleKey="generics.customfieldvalues.title" isContextual="true"
			open="${hasCUF}">
			<jsp:attribute name="body">
				<div id="test-suite-custom-fields-content" class="display-table">
				<div class="waiting-loading minimal-height"></div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
			

		<%-- ------------------ statistiques --------------------------- --%>
		<comp:statistics-panel statisticsEntity="${ statistics }" statisticsUrl="${ testSuiteStatisticsUrl }"/>
		
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
				<input id="test-case-button" type="button" value="${associateLabel}"
					class="button" />
				<input id="remove-test-suite-test-case-button" type="button"
					value="${removeLabel}" class="button" />
				<input id="assign-test-case-button" type="button"
					value="${assignLabel}" class="button" />

			</c:if>
		</div>

		<div class="table-tab-wrap">

			<aggr:decorate-test-suite-test-plan-table
				tableModelUrl="${testSuiteTestPlanUrl}"
				testPlanDetailsBaseUrl="${testCaseDetailsBaseUrl}"
				removeTestPlansUrl="${removeTestCaseUrl}"
				batchRemoveButtonId="remove-test-suite-test-case-button"
				updateTestPlanUrl="${updateTestCaseUrl}"
				assignableUsersUrl="${assignableUsersUrl}"
				baseIterationUrl="${baseIterationUrl}"
				nonBelongingTestPlansUrl="${nonBelongingTestCasesUrl}"
				testPlanExecutionsUrl="${testCaseExecutionsUrl}"
				editable="${ linkable }"
				testCaseMultipleRemovalPopupId="delete-test-suite-multiple-test-plan-dialog"
				testCaseSingleRemovalPopupId="delete-test-suite-single-test-plan-dialog"
				testSuiteExecButtonsId="test-suite-execution-button"
				testSuiteExecButtonsUrl="${ testSuiteExecButtonsUrl }" />
			<aggr:test-suite-test-plan-table />
		</div>


		<%--------------------------- Deletion confirmation popup for Test plan section ------------------------------------%>

		<pop:popup id="delete-test-suite-multiple-test-plan-dialog"
			openedBy="remove-test-suite-test-case-button"
			titleKey="dialog.remove-testcase-testsuite-associations.title" isContextual="true"  >
			<jsp:attribute name="buttons">
		<f:message var="labelDelete" key="label.Yes" />
				'${ labelDelete }' : function() {
            $this = $(this);						
						$this.data("answer","delete");
						$this.dialog("close");
				},
				
		<f:message var="labelDetach" key="label.No" />
				'${ labelDetach }' : function(){
            $this = $(this);            
						$this.data("answer","detach");
						$this.dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
			<jsp:attribute name="body">
		<f:message var="emptyMessage"
					key="message.EmptyTableSelection" />			
		<script type="text/javascript">
				$("#delete-test-suite-multiple-test-plan-dialog").bind( "dialogopen", function(event, ui){
					var table = $( '#test-suite-test-plans-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
	
					if (ids.length == 0) {
						$.squash.openMessage("<f:message key='popup.title.error' />", "${emptyMessage}");
						$(this).dialog('close');
					}
					
				});
			</script>
		<f:message key="dialog.remove-testcase-testsuite-associations.message" />
	</jsp:attribute>
		</pop:popup>

		<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
		<pop:popup id="delete-test-suite-single-test-plan-dialog"
			openedBy="test-suite-test-plans-table .delete-test-suite-test-plan-button"
			titleKey="dialog.remove-testcase-testsuite-association.title" isContextual="true">
			<jsp:attribute name="buttons">
		<f:message var="labelDelete" key="label.Yes" />
				'${ labelDelete }' : function(){
            $this = $(this);
						$this.data("answer","delete");
						$this.dialog("close");
				},
				
		<f:message var="labelDetach" key="label.No" />
				'${ labelDetach }' : function(){
            $this = $(this);
						$this.data("answer","detach");
						$this.dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
			<jsp:attribute name="body">
		<f:message key="dialog.remove-testcase-testsuite-association.message" />
	</jsp:attribute>
		</pop:popup>

		<%-- ------------------------- /Deletion confirmation popup for Test plan section --------------------------------- --%>
	</div>

	<%------------------------------ Attachments bloc ------------------------------------------- --%>
	<comp:attachment-tab tabId="tabs-3" entity="${ testSuite }"
		editable="${ executable }" />
	<%-- ---------------------deletion popup------------------------------ --%>
	<c:if test="${ deletable }">
		<script>
		var testSuiteId = ${testSuite.id};
		$(function(){
			$('#delete-test-suite-button').click(function(){
				oneShotConfirm("<f:message key='dialog.delete-test-suite.title' />", 
						"<table><tr><td><img src='${servContext}/images/messagebox_confirm.png'/></td><td><table><tr><td><span>${deleteMessageStart} <span class='red-warning-message'>${deleteMessage}</span> ${deleteMessageEnd}</span></td></tr><tr><td>${deleteMessageCantBeUndone}</td></tr><tr><td class='bold-warning-message'>${deleteMessageConfirm}</td></tr></table></td></tr></table>",
						"<f:message key='label.Confirm' />",  
						"<f:message key='label.Cancel' />",
						'500px').done(function(){confirmTestSuiteDeletion()
							.done(deleteTestSuiteSuccess)
							.fail(deleteTestSuiteFailure)});
			});
		});
		
		function confirmTestSuiteDeletion(){
			return $.ajax({
				'url' : '${confirmDeletionUrl}',
				type : 'POST',
				data : {"ids[]":[testSuiteId]},
				dataType : 'json'
			});
		}
		
		</script>
	</c:if>

	<%--------------------------- Assign User popup -------------------------------------%>
	<comp:popup id="batch-assign-test-case"
		titleKey="label.AssignUser" isContextual="true"
		openedBy="assign-test-case-button" closeOnSuccess="false">

		<jsp:attribute name="buttons">
		
			<f:message var="label" key="label.Assign" />
			'${ label }': function() {
				var url = "${assignTestCasesUrl}";
				var table = $( '#test-suite-test-plans-table' ).dataTable();
				var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
		
				var user = $(".comboLogin").val();
			
				$.post(url, { testPlanIds: ids, userId: user}, function(){
					refreshTestPlansWithoutSelection();
					$("#batch-assign-test-case").dialog('close');
				});
				
			},
			<pop:cancel-button />
		</jsp:attribute>
		<jsp:body>
			<f:message var="confirmMessage"
				key="message.AssignTestCaseToUser" />
			<script type="text/javascript">
				$("#batch-assign-test-case").bind( "dialogopen", function(event, ui){
					var table = $( '#test-suite-test-plans-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);

					if (ids.length > 0) {
						var comboBox = $.get("${batchAssignableUsersUrl}", false, function(){
							$("#comboBox-div").html("${confirmMessage}");
							$("#comboBox-div").append(comboBox.responseText);
							$("#comboBox-div").show();
						});
					}
					else {
						$.squash.openMessage("<f:message key='popup.title.error' />", "${emptyMessage}");
						$(this).dialog('close');
					}
					
				});
			</script>
			<div id="comboBox-div">
			</div>
		</jsp:body>
	</comp:popup>
</div>
<script type="text/javascript">
	$(function(){
		$('#test-case-button').click(function(){ document.location.href = "${testPlanManagerUrl}" ; });	
	});
</script>
<%------------------------------------------automated suite overview --------------------------------------------%>
<c:if test="${ testSuite.iteration.project.testAutomationEnabled }">
	<comp:automated-suite-overview-popup />
</c:if>
<%------------------------------------------/automated suite overview --------------------------------------------%>
<%------------------------------ bugs section -------------------------------%>
<c:if test="${testSuite.iteration.project.bugtrackerConnected }">
	<comp:issues-tab btEntityUrl="${ btEntityUrl }" />
</c:if>
<%------------------------------ /bugs section -------------------------------%>

<c:if test="${ creatable }">
	<div id="confirm-duplicate-test-suite-dialog" class="not-displayed popup-dialog" title="<f:message key="title.DupliateTestSuite" />">
		<strong><f:message key="message.DuplicateTestSuite" /> "${testSuite.name}" ?</strong>
		<input:ok />
		<input:cancel />
	</div>
<script>
	$(function(){
		var confirmHandler = function() {
			dialog.confirmDialog("close");
			duplicateTestSuite().done(function(json){
				duplicateTestSuiteSuccess(json);
			});
		};
		var dialog = $( "#confirm-duplicate-test-suite-dialog" );
		dialog.confirmDialog({confirm: confirmHandler});
		$('#duplicate-test-suite-button').click(function(){
			dialog.confirmDialog( "open" );
			return false;
		});
		
		<c:if test="${hasCUF}">
		<%-- loading the custom field panel --%>
		$("#test-suite-custom-fields-content").load("${customFieldsValuesURL}?boundEntityId=${testSuite.boundEntityId}&boundEntityType=${testSuite.boundEntityType}"); 				
    	</c:if>
	});
</script>
</c:if>
<comp:decorate-buttons />