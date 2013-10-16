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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="ts" tagdir="/WEB-INF/tags/test-suites-components"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>


<f:message var="squashlocale" key="squashtm.locale" />

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

<s:url var="testSuiteStatisticsUrl"
	value="/test-suites/{testSuiteId}/statistics">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="btEntityUrl" value="/bugtracker/test-suite/{suiteId}">
	<s:param name="suiteId" value="${testSuite.id}" />
</s:url>

<s:url var="testSuiteExecButtonsUrl" value="/test-suites/{testSuiteId}/exec-button">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<c:url var="customFieldsValuesURL" value="/custom-fields/values" />


<f:message var='deleteMessageStart' key='dialog.label.delete-node.label.start'/>
<f:message var="deleteMessage" key="dialog.label.delete-nodes.test-suite.label" />
<f:message var='deleteMessageCantBeUndone' key='dialog.label.delete-node.label.cantbeundone'/>
<f:message var='deleteMessageConfirm' key='dialog.label.delete-node.label.confirm'/>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

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



<script type="text/javascript">

	function renameTestSuiteSuccess(data){
		var evt = new EventRename(identity, data.newName);
		squashtm.workspace.contextualContent.fire(null, evt);		
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
				squashtm.workspace.contextualContent.fire(null, evt);
			</c:otherwise>
		</c:choose>
		
	}

	
	function refreshExecButtons(){
		$('#test-suite-execution-button').load('${ testSuiteExecButtonsUrl }');
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
	<c:if test="${ writable }">
		<pop:popup id="rename-test-suite-dialog"
			titleKey="dialog.testsuites.rename.title" isContextual="true"
			openedBy="rename-test-suite-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.testsuites.rename.title" />
				'${ label }': function() {
					var url = "${ testSuiteUrl }";
					$.ajax({
						url : url, 
						dataType : 'json', 
						type : 'POST',
						data : { newName : $("#rename-test-suite-name").val() }
					});				
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:attribute name="body">
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
		
			</jsp:attribute>
		</pop:popup>
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
		<c:if test="${ writable }">
			<input type="button"
				value="<f:message key='test-suite.button.rename.label' />"
				id="rename-test-suite-button" class="button"
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


<csst:jq-tab>
<div class="fragment-tabs fragment-body">
	<ul class="tab-menu">
		<li><a href="#tabs-1"><f:message key="tabs.label.information" />
		</a>
		</li>
		<li><a href="#test-suite-test-plans-panel"><f:message key="tabs.label.test-plan" />
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
		<c:if test="${ writable }">
			<comp:rich-jeditable targetUrl="${ testSuiteUrl }"
				componentId="test-suite-description"
				submitCallback="refreshTestSuiteInfos" />
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
			titleKey="generics.customfieldvalues.title" 
			open="${hasCUF}">
			<jsp:attribute name="body">
				<div id="test-suite-custom-fields-content" class="display-table">
				<div class="waiting-loading full-size-hack minimal-height"></div>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
			

		<%-- ------------------ statistiques --------------------------- --%>
		<comp:statistics-panel statisticsEntity="${ statistics }" statisticsUrl="${ testSuiteStatisticsUrl }"/>
		
	</div>
	
		<%-- ------------------ test plan ------------------------------ --%>
	

	<ts:test-suite-test-plan-panel assignableUsers="${assignableUsers}" testSuite="${testSuite}"
									editable="${writable}" executable="${executable}" linkable="${linkable}" reorderable="${linkable}"	/>


		<%------------------------------ Attachments bloc ------------------------------------------- --%>
	
	<at:attachment-tab tabId="tabs-3" entity="${ testSuite }"	editable="${ executable }" tableModel="${attachmentsModel}"/>
	

</div>
</csst:jq-tab>
<%------------------------------------------automated suite overview --------------------------------------------%>
<c:if test="${ testSuite.iteration.project.testAutomationEnabled }">
	<comp:automated-suite-overview-popup />
</c:if>
<%------------------------------------------/automated suite overview --------------------------------------------%>



<%------------------------------ /bugs section -------------------------------%>

<c:if test="${ creatable }">
	<div id="confirm-duplicate-test-suite-dialog" class="not-displayed popup-dialog" title="<f:message key="title.DuplicateTestSuite" />">
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
	});
</script>

</c:if>
 <f:message key="tabs.label.issues" var="tabIssueLabel"/>
<script>

	var identity = { obj_id : ${testSuite.id}, obj_restype : "test-suites"  };

	require(["domReady", "require"], function(domReady, require){
		domReady(function(){	
			require(["jquery", "squash.basicwidgets", "workspace.contextual-content", "contextual-content-handlers", "jquery.squash.fragmenttabs", "bugtracker", "test-suite-management"], 
					function($, basicwidg, ctxt, contentHandlers, Frag, bugtracker, tsmanagement){
				
				basicwidg.init();
				
				var nameHandler = contentHandlers.getSimpleNameHandler();
				
				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#test-suite-name";
				
				ctxt.addListener(nameHandler);

				// todo : uniform the event handling.
				tsmanagement.initEvents();
				
				//****** tabs configuration *******
				
				var fragConf = {
					beforeLoad : Frag.confHelper.fnCacheRequests	
				};
				Frag.init(fragConf);
				
				<c:if test="${testSuite.iteration.project.bugtrackerConnected}">
				bugtracker.btPanel.load({
					url : "${btEntityUrl}",
					label : "${tabIssueLabel}"
				});
				</c:if>
				
				
				
				<c:if test="${hasCUF}">
				<%-- loading the custom field panel --%>
				$("#test-suite-custom-fields-content").load("${customFieldsValuesURL}?boundEntityId=${testSuite.boundEntityId}&boundEntityType=${testSuite.boundEntityType}"); 				
		    	</c:if>
		    	
		    	
			 	squashtm.execution = squashtm.execution || {};
			 	squashtm.execution.refresh = function(){
			 		ctxt.trigger('context.content-modified');
			 	};
			});
		});
	});
</script>