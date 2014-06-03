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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="it" tagdir="/WEB-INF/tags/iterations-components"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard" %>

<f:message var="squashlocale" key="squashtm.locale" />

<comp:datepicker-manager locale="${squashlocale}" />

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="iterationUrl" value="/iterations/{iterId}">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="iterationPlanningUrl" value="/iterations/{iterId}/planning">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>
<s:url var="iterationTestPlanUrl" value="/iterations/{iterId}/test-plan">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<s:url var="iterationDashboardStatisticsUrl" value="/iterations/{iterId}/dashboard-statistics">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<c:url var="iterationStatisticsPrintUrl" value="/iterations/${iteration.id}/dashboard"/>

<s:url var="testPlanManagerUrl"
	value="/iterations/{iterId}/test-plan-manager">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

<c:url var="testCaseDetailsBaseUrl"
	value="/test-case-libraries/1/test-cases" /><%-- FIXME this url looks wrong but not used where it's passed --%>

<c:url var="workspaceUrl" value="/campaign-workspace/#" />

<s:url var="updateTestCaseUrl" value="/iterations/{iterId}/test-plan/">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

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

	<div class="unsnap"></div>
	<c:if test="${ writable }">
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
			<comp:general-information-panel auditableEntity="${iteration}" entityUrl="${iterationUrl}"/>
		</div>
	</div>
	<div class="toolbar-button-panel">
		<c:if test="${ executable && iteration.project.testAutomationEnabled }">
			<comp:execute-auto-button url="${ iterationUrl }" testPlanTableId="iteration-test-plans-table"/>
		
		</c:if>
		<c:if test="${ writable }">
			<input type="button"
				value=' <f:message key="iteration.test-plan.testsuite.manage.label"/>'
				id="manage-test-suites-button" class="sq-btn" />
		</c:if>
		<c:if test="${ writable }">
			<input type="button"
				value='<f:message key="iteration.button.rename.label" />'
				id="rename-iteration-button" class="sq-btn" />
		</c:if>
		
	</div>
	<div class="unsnap"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ iterationUrl }"/>
	</c:if>
</div>

<csst:jq-tab activeContentIndex="2">
<div class="fragment-tabs fragment-body">
	<ul class="tab-menu">
		<li><a href="#dashboard-iteration"><f:message key="title.Dashboard"/>
		</a>
		</li>
		<li><a href="#tabs-1"><f:message key="tabs.label.information" />
		</a>
		</li>
		<li><a href="#iteration-test-plans-panel"><f:message key="tabs.label.test-plan" />
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

		<c:if test="${ writable }">
			<comp:rich-jeditable targetUrl="${ iterationUrl }"
				componentId="iteration-description"/>
		</c:if>

		<comp:toggle-panel id="iteration-description-panel"
			titleKey="label.Description"
			open="true">
			<jsp:attribute name="body">
			<div id="iteration-description">${ iteration.description }</div>
			</jsp:attribute>
		</comp:toggle-panel>


		
		<%----------------------------------- Custom Fields -----------------------------------------------%>

      <comp:toggle-panel id="iteration-custom-fields" titleKey="generics.customfieldvalues.title" open="${hasCUF}">
        <jsp:attribute name="body">
				<div id="iteration-custom-fields-content" class="display-table">
                <c:if test="${hasCUF}">
			     	<comp:waiting-pane />
                </c:if>
				</div>
			</jsp:attribute>
      </comp:toggle-panel>




      <%--------------------------- Planning section ------------------------------------%>

		<comp:toggle-panel id="datepicker-panel"
			titleKey="label.Planning"
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
									initialDate="${iteration.scheduledStartDate.time}"
									editable="${ writable }">	
					</comp:datepicker>
				</td>
				<td class="datepicker-table-col">
	
					<comp:datepicker-auto datePickerId="actual-start"
									url="${iterationPlanningUrl}"
									fmtLabel="dialog.label.iteration.actual_start.label"
									paramName="actualStart" autosetParamName="setActualStartAuto"
									isAuto="${iteration.actualStartAuto}"
									initialDate="${iteration.actualStartDate.time}"
									isContextual="true" editable="${ writable }"
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
									initialDate="${iteration.scheduledEndDate.time}"
									editable="${ writable }">	
					</comp:datepicker>				
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto datePickerId="actual-end"
									url="${iterationPlanningUrl}"
									fmtLabel="dialog.label.iteration.actual_end.label"
									paramName="actualEnd" autosetParamName="setActualEndAuto"
									isAuto="${iteration.actualEndAuto}"
									initialDate="${iteration.actualEndDate.time}"
									isContextual="true" editable="${ writable }"
									jsVarName="actualEnd">
					</comp:datepicker-auto>
				</td>
			</tr>
			</table>
			</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
	</div>
	
	<%-- ------------------ test plan ------------------------------ --%>
	
	<it:iteration-test-plan-panel iteration="${iteration}" assignableUsers="${assignableUsers}" weights="${weights}" modes="${modes}" statuses="${statuses}"
								  linkable="${linkable}"   editable="${writable}"  executable="${executable}" reorderable="${linkable}"/>
	
	<%-- ------------------ /test plan ----------------------------- --%>

	<%------------------------------- Dashboard ---------------------------------------------------%>
	<div id="dashboard-iteration">
		<dashboard:iteration-dashboard-panel url="${iterationDashboardStatisticsUrl}" printUrl="${iterationStatisticsPrintUrl}" allowsSettled="${allowsSettled}" allowsUntestable="${allowsUntestable}" />
	</div>


	<%------------------------------ Attachments bloc ------------------------------------------- --%>
	
	<at:attachment-tab tabId="tabs-3" entity="${ iteration }"	editable="${ attachable }"  tableModel="${attachmentsModel}"/>



		
	<%-- ----------------------------------- Test Suite Management -------------------------------------------------- --%>
	<c:if test="${ writable }">
		<!-- here the deletable attribute concern the iteration because it has the same impact so far on the appearance the deletion button for a test suite. -->
		<!-- it is unlikely but for more specific right management we will have to check the right of the user on the selected test suites in the popup -->
		<it:test-suite-managment suiteList="${iteration.testSuites}"
			popupOpener="manage-test-suites-button" creatable="${ creatable }"
			deletable="${ deletable }" popupId="manage-test-suites-popup"
			menuId="manage-test-suites-buttonmenu" testSuitesUrl="${testSuitesUrl}"
			datatableId="iteration-test-plans-table"
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
</csst:jq-tab>
<%------------------------------------------automated suite overview --------------------------------------------%>
<c:if test="${ executable && iteration.project.testAutomationEnabled }">		
	<comp:automated-suite-overview-popup />
	</c:if>
	<%------------------------------------------/automated suite overview --------------------------------------------%>
	

 <f:message key="tabs.label.issues" var="tabIssueLabel"/>
<script type="text/javascript">

	var identity = { resid : ${iteration.id}, restype : "iterations"  };
	
	
	require(["common"], function(){
			require(["jquery", "squash.basicwidgets", "contextual-content-handlers", 
			         "jquery.squash.fragmenttabs", "bugtracker", "workspace.event-bus", 
			         "iteration-management", "app/ws/squashtm.workspace" ], 
					function($, basicwidg, contentHandlers, Frag, bugtracker, eventBus, itermanagement, WS){
		$(function(){
                 WS.init();
				basicwidg.init();
				
				// *********** event handler ***************
				
				var nameHandler = contentHandlers.getSimpleNameHandler();
				
				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#iteration-name";

				
				// todo : uniform the event handling.
				itermanagement.initEvents();
				
				//****** tabs configuration *******
				
				var fragConf = {
					active : 2,
					cookie : "iteration-tab-cookie",
					activate : function(event, ui){
						if (ui.newPanel.is('#dashboard-iteration')){
							eventBus.trigger('dashboard.appear');
						}
					}
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
			 		$("#iteration-test-plans-table").squashTable().refresh();
			 	}, window);
			 	
			 	// ********** rename popup ***********
			 	
				$("#rename-iteration-dialog").bind("dialogopen",
					function(event, ui) {
						var name = $.trim($('#iteration-name').text());
						$("#rename-iteration-name").val(name);

				});
		
				// ********** dashboard **************
				
				itermanagement.initDashboardPanel({
					master : '#dashboard-master',
					cacheKey : 'it${iteration.id}'
				});	
				
			});
		});
	});



	/* renaming success handler */
	function renameIterationSuccess(data) {
		squashtm.workspace.eventBus.trigger('node.rename', { identity : identity, newName : data.newName});		
	}
	
</script>


