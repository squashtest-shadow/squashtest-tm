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
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="ts" tagdir="/WEB-INF/tags/test-suites-components"%>
<%@ taglib prefix="csst"
	uri="http://org.squashtest.tm/taglib/css-transform"%>
<%@ taglib prefix="issues" tagdir="/WEB-INF/tags/issues"%>


<f:message var="squashlocale" key="squashtm.locale" />

<comp:datepicker-manager locale="${squashlocale}" />

<s:url var="testSuiteUrl" value="/test-suites/{testSuiteId}">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="duplicateTestSuiteUrl"
	value="/iterations/{iterationId}/duplicateTestSuite/{testSuiteId}">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="testSuiteStatisticsUrl"
	value="/test-suites/{testSuiteId}/statistics">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="btEntityUrl" value="/bugtracker/test-suite/{suiteId}">
	<s:param name="suiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testSuiteExecButtonsUrl"
	value="/test-suites/{testSuiteId}/exec-button">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="customFieldsValuesURL" value="/custom-fields/values">
	<s:param name="boundEntityId" value="${testSuite.boundEntityId}" />
	<s:param name="boundEntityType" value="${testSuite.boundEntityType}" />
</s:url>


<f:message var='deleteMessageStart'
	key='dialog.label.delete-node.label.start' />
<f:message var="deleteMessage"
	key="dialog.label.delete-nodes.test-suite.label" />
<f:message var='deleteMessageCantBeUndone'
	key='dialog.label.delete-node.label.cantbeundone' />
<f:message var='deleteMessageConfirm'
	key='dialog.label.delete-node.label.confirm' />
<f:message var="labelConfirm" key="label.Confirm" />
<f:message var="labelCancel" key="label.Cancel" />

<c:set var="servContext"
	value="${ pageContext.servletContext.contextPath }" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE"
	domainObject="${ testSuite }">
	<c:set var="writable" value="${ true }" />
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

<f:message key="tabs.label.issues" var="tabIssueLabel" />
<script type="text/javascript">
  squashtm = squashtm || {};
  squashtm.page = squashtm.page || {};
  var config = squashtm.page;
  config.isFullPage = ${ not empty param.isInfoPage and param.isInfoPage };
  config.hasFields = ${ hasCUF };
  config.hasBugtracker = ${ testSuite.iteration.project.bugtrackerConnected };
  config.identity = { resid : ${testSuite.id}, restype : "test-suites"  };
  config.parentIdentity = { resid : ${testSuite.iteration.id}, restype : "iteration" };
  config.bugtracker = { url: "${btEntityUrl}", style : "fragment-tab"  };
  config.customFields = { url: "${customFieldsValuesURL}" };
  config.testSuiteURL = "${testSuiteUrl}";
  config.api = {
		copy: "${duplicateTestSuiteUrl}"		
  };
  config.writable = ${not empty writable and writable};
</script>

<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header ctx-title">
	<div>
		<h2>
			<span><f:message key="test-suite.header.title" />&nbsp;:&nbsp;</span><a
				id="test-suite-name" href="${ testSuiteUrl }/info"><c:out
					value="${ testSuite.name }" escapeXml="true" /> </a>
		</h2>
	</div>

</div>

<div id="test-suite-toolbar" class="toolbar-class ui-corner-all cf">
	<div class="toolbar-information-panel">
		<div id="general-informations-panel">
			<comp:general-information-panel auditableEntity="${testSuite}" />
		</div>
	</div>
	<div class="toolbar-button-panel btn-toolbar right">
		<c:if test="${ executable }">
			<div id="test-suite-exec-btn-group" class="btn-group"
				data-content-url="${ testSuiteExecButtonsUrl }">
				<comp:test-suite-execution-button testSuiteId="${ testSuite.id }"
					statisticsEntity="${ statistics }" />
			</div>
			<c:if test="${ testSuite.iteration.project.testAutomationEnabled }">
				<comp:execute-auto-button url="${ testSuiteUrl }" />
			</c:if>
		</c:if>
		<c:if test="${ writable }">
			<input type="button"
				value="<f:message key='test-suite.button.rename.label' />"
				id="rename-test-suite-button" class="sq-btn" />
		</c:if>
		<c:if test="${ creatable }">
			<input type="button"
				value="<f:message key='test-suite.button.duplicate.label' />"
				id="duplicate-test-suite-button" class="sq-btn" />
		</c:if>
	</div>
	<div class="unsnap"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }"
			objectUrl="${ testSuiteUrl }" />
	</c:if>
</div>


<csst:jq-tab>
	<div class="fragment-tabs fragment-body">
		<ul class="tab-menu">
			<li><a href="#tabs-1"><f:message
						key="tabs.label.information" /> </a></li>
			<li><a href="#test-suite-test-plans-panel"><f:message
						key="tabs.label.test-plan" /> </a></li>
			<li><a href="#tabs-3"><f:message key="label.Attachments" />
					<c:if test="${ testSuite.attachmentList.notEmpty }">
						<span class="hasAttach">!</span>
					</c:if> </a></li>
<c:if test="${testSuite.project.bugtrackerConnected}">
        <li>
          <%-- div#bugtracker-section-main-div is declared in tagfile issues:bugtracker-panel.tag --%>
          <a href="#bugtracker-section-main-div"><f:message key="tabs.label.issues"/></a>
        </li>
</c:if>   					
		</ul>
		<div id="tabs-1">
			<c:if test="${ writable }">
				<comp:rich-jeditable targetUrl="${ testSuiteUrl }"
					componentId="test-suite-description" />
			</c:if>

			<comp:toggle-panel id="test-suite-description-panel"
				titleKey="label.Description"
				open="${ not empty testSuite.description }">
				<jsp:attribute name="body">
				<div id="test-suite-description">${ testSuite.description }</div>
			</jsp:attribute>
			</comp:toggle-panel>


			<%----------------------------------- Custom Fields -----------------------------------------------%>

			<comp:toggle-panel id="test-suite-custom-fields"
				titleKey="generics.customfieldvalues.title" open="${hasCUF}">
				<jsp:attribute name="body">
				<div id="test-suite-custom-fields-content">
                <c:if test="${hasCUF}">				
				  <comp:waiting-pane />
                </c:if>
				</div>
			</jsp:attribute>
			</comp:toggle-panel>



			<%-- ------------------ statistiques --------------------------- --%>
			<comp:statistics-panel statisticsEntity="${ statistics }"
				statisticsUrl="${ testSuiteStatisticsUrl }" />

		</div>

		<%-- ------------------ test plan ------------------------------ --%>


		<ts:test-suite-test-plan-panel assignableUsers="${assignableUsers}"
			testSuite="${testSuite}" weights="${weights}" modes="${modes}"
			statuses="${statuses}" editable="${writable}"
			executable="${executable}" linkable="${linkable}"
			reorderable="${linkable}" />


		<%------------------------------ Attachments bloc ------------------------------------------- --%>

		<at:attachment-tab tabId="tabs-3" entity="${ testSuite }"
			editable="${ executable }" tableModel="${attachmentsModel}" />

    <%-- ----------------------- bugtracker (if present)----------------------------------------%> 
<c:if test="${testSuite.project.bugtrackerConnected}">
        <issues:butracker-panel entity="${testSuite}"/>
</c:if>

    <%-- ----------------------- /bugtracker (if present)----------------------------------------%> 


	</div>
</csst:jq-tab>
<%------------------------------------------automated suite overview --------------------------------------------%>
<c:if test="${ testSuite.iteration.project.testAutomationEnabled }">
	<comp:automated-suite-overview-popup />
</c:if>
<%------------------------------------------/automated suite overview --------------------------------------------%>


<div class="not-displayed">
	<c:if test="${ writable }">
		<f:message var="renameDialogTitle"
			key="dialog.testsuites.rename.title" />
		<div id="rename-testsuite-dialog" title="${renameDialogTitle}"
			class="not-displayed popup-dialog">
			<div>
				<label><f:message key="dialog.rename.label" /></label> <input
					type="text" id="rename-test-suite-name" maxlength="255" size="50" />
				<br />
				<comp:error-message forField="name" />
			</div>

			<div class="popup-dialog-buttonpane">
				<input type="button" value="${labelConfirm}"
					data-def="evt=confirm, mainbtn" /> <input type="button"
					value="${labelCancel}" data-def="evt=cancel" />
			</div>
		</div>

	</c:if>
</div>

<%------------------------------ /bugs section -------------------------------%>
<div class="not-displayed">
	<c:if test="${ creatable }">
		<div id="confirm-duplicate-test-suite-dialog"
			class="not-displayed popup-dialog"
			title="<f:message key="title.DuplicateTestSuite" />">
			<strong><f:message key="message.DuplicateTestSuite" />
				"${testSuite.name}" ?</strong>
			<input:ok />
			<input:cancel />
		</div>

	</c:if>
</div>

<script type="text/javascript">
publish("reload.test-suite");
if (!squashtm.page.isFullPage) {
	require(["common"], function() {
		require(["test-suite-page"], function() {/*noop*/});
	});
}
</script>