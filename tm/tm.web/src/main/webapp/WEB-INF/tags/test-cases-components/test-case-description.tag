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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags/test-cases-components"%>


<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>
<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>
<%@ attribute name="testCaseImportanceComboJson"  required="true" type="java.lang.Object"  description="the json formatted list of importances" %>
<%@ attribute name="testCaseNatureComboJson"  required="true" type="java.lang.Object"  description="the json formatted list of natures" %>
<%@ attribute name="testCaseTypeComboJson"  required="true" type="java.lang.Object"  description="the json formatted list of types" %>
<%@ attribute name="testCaseTypeStatusJson"  required="true" type="java.lang.Object"  description="the json formatted list of status" %>
<%@ attribute name="testCaseImportanceLabel"  required="true" type="java.lang.String"  description="a label related to test case importance, not sure to remember what." %>


<c:url var="testCaseUrl" 					value="/test-cases/${testCase.id}"/>
<c:url var="importanceAutoUrl" 				value="/test-cases/${testCase.id}/importanceAuto"/>


<c:if test="${ writable }">
<script type="text/javascript">
	var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };

	//for technical reasons we handle directly the ajax operation when choosing a status.
	function postUpdateStatus(value, settings){
		$.post("${testCaseUrl}", {id:"test-case-status", value : value})
		.done(function(response){
			var evt = new EventUpdateStatus(identity, value.toLowerCase());
			squashtm.workspace.eventBus.fire(null, evt);
		});
		
		//in the mean time, must return immediately
		var data = JSON.parse(settings.data);
		return data[value];
	}

	//for technical reasons we handle directly the ajax operation when choosing an importance.
	function postUpdateImportance(value, settings){
		$.post("${testCaseUrl}", {id:"test-case-importance", value : value})
		.done(function(response){
			var evt = new EventUpdateImportance(identity, value.toLowerCase());
			squashtm.workspace.eventBus.fire(null, evt);
		});
		
		//in the mean time, must return immediately
		var data = JSON.parse(settings.data);
		return data[value];
	}

</script>
<comp:rich-jeditable   targetUrl="${ testCaseUrl }" 
					   componentId="test-case-description" />

<comp:simple-jeditable targetUrl="${ testCaseUrl }"	
					   componentId="test-case-reference"
					   submitCallback="updateReferenceInTitle" 
					   maxLength="50" />

<comp:select-jeditable componentId="test-case-importance"
					   jsonData="${ testCaseImportanceComboJson }"
					   targetFunction="postUpdateImportance" />

<comp:select-jeditable componentId="test-case-nature"
					   jsonData="${ testCaseNatureComboJson }" 
					   targetUrl="${ testCaseUrl }" />

<comp:select-jeditable componentId="test-case-type"
					   jsonData="${ testCaseTypeComboJson }" 
					   targetUrl="${ testCaseUrl }" />

<comp:select-jeditable componentId="test-case-status"
						jsonData="${ testCaseStatusComboJson }" 
						targetFunction="postUpdateStatus" />
</c:if>


<comp:toggle-panel id="test-case-description-panel"
				   titleKey="label.Description" 
				   open="true">
				   
	<jsp:attribute name="body">
	<div id="test-case-description-table"  class="display-table">
		
		<div class="display-table-row">
			<label class="display-table-cell" for="test-case-id">ID</label>
			<div class="display-table-cell" id="test-case-id">${ testCase.id }</div>
		</div>
		
		<div class="display-table-row">
			<label for="test-case-description" class="display-table-cell"><f:message key="label.Description" /></label>
			<div class="display-table-cell" id="test-case-description">${ testCase.description }</div>
		</div>
		
		<div class="display-table-row">
			<label class="display-table-cell" for="test-case-reference"><f:message key="test-case.reference.label" /></label>
			<div class="display-table-cell" id="test-case-reference">${ testCase.reference }</div>
		</div>
		
		<div class="display-table-row">
			<label for="test-case-importance" class="display-table-cell"><f:message key="test-case.importance.combo.label" /></label>
			<div class="display-table-cell">
				<span id="test-case-importance">${testCaseImportanceLabel}</span>
				<c:if test="${ writable }">
				<comp:select-jeditable-auto
						associatedSelectJeditableId="test-case-importance"
						url="${ importanceAutoUrl }"
						isAuto="${ testCase.importanceAuto }"
						paramName="importanceAuto" />
				</c:if>
			</div>
		</div>
		
		<div class="display-table-row">
			<label for="test-case-nature" class="display-table-cell"><f:message key="test-case.nature.combo.label" /></label>
			<div class="display-table-cell">
				<span id="test-case-nature">${ testCaseNatureLabel }</span>
			</div>
		</div>
		
		<div class="display-table-row">
			<label for="test-case-type" class="display-table-cell">
				<f:message key="test-case.type.combo.label" />
			</label>
			<div class="display-table-cell">
				<span id="test-case-type">${ testCaseTypeLabel }</span>
			</div>
		</div>
		
		<div class="display-table-row">
			<label for="test-case-status" class="display-table-cell"><f:message key="test-case.status.combo.label" /></label>
			<div class="display-table-cell">
				<span id="test-case-status">${ testCaseStatusLabel }</span>
			</div>
		</div>
		
		
		<%-- Test Automation structure --%>
		<c:if test="${testCase.project.testAutomationEnabled}">
		<tc:testcase-script-elt-structure testCase="${testCase}"
										  canModify="${writable}" 
										  testCaseUrl="${testCaseUrl}" />	
		</c:if>			
		<%--/Test Automation structure --%>
		
	</div>
	</jsp:attribute>
</comp:toggle-panel>