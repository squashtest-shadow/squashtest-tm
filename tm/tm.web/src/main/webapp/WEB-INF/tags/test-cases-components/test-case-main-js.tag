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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>


<%@ attribute name="testCase"   			required="true" type="java.lang.Object" 	description="the testcase" %>
<%@ attribute name="isInfoPage"				required="true" type="java.lang.Boolean" 	description="if this is displayed in an info page" %>
<%@ attribute name="callingTestCasesModel" 	required="true" type="java.lang.Object"		description="the model for the calling test cases datatable"%>


<c:url var="testCaseUrl" 	value="/test-cases/${testCase.id}"/>
<c:url var="getImportance" 	value="/test-cases/${testCase.id}/importance"/>
<c:url var="workspaceUrl" 	value="/test-case-workspace/#" />
<c:url var="btEntityUrl" 	value="/bugtracker/test-case/${testCase.id}"/>
<c:url var="customFieldsValuesURL" 			value="/custom-fields/values" />


<f:message key="tabs.label.issues" var="tabIssueLabel"/>
<f:message key="popup.title.error" var="popupErrorTitle" />

<%-- ----------------------------------------- Remaining of the javascript initialization ----------------------------- --%>

<script type="text/javascript">


var identity = { resid : ${testCase.id}, restype : "test-cases"  };

require([ "common" ], function() {
	require(["jquery", 
	   			"squash.basicwidgets", 
	   			"contextual-content-handlers", 
	   			"jquery.squash.fragmenttabs", 
	   			"bugtracker", 
	   			"workspace.event-bus", 
	   			"jqueryui", 
	   			"squashtable"], 
	   				function($, basic, contentHandlers, Frag, bugtracker, eventBus){
	function refreshTCImportance(){
		$.ajax({
			type : 'GET',
			data : {},
			dataType : "text",
			url : '${getImportance}'			
		})
		.success(function(importance){
			$("#test-case-importance").html(importance);	
		})
		.error(function(){
			$.squash.openMessage("${popupErrorTitle}", "fail to refresh importance");
		});
	}
	
	function renameTestCaseSuccess(data){
		eventBus.trigger('node.rename', { identity : identity, newName : data.newName});
	};	
	
	squashtm = squashtm || {};
	squashtm.testCase = squashtm.testCase || {};
	squashtm.testCase.renameTestCaseSuccess = renameTestCaseSuccess;
	
	$(function(){
		
		//init the rename popup
		$( "#rename-test-case-dialog" ).bind( "dialogopen", function(event, ui) {
			var name = $.trim($('#test-case-raw-name').text());
			$("#rename-test-case-input").val(name);
			
		});
		
		
		basic.init();
		
		//init the renaming listener
		
		var nameHandler = contentHandlers.getNameAndReferenceHandler();
		
		nameHandler.identity = identity;
		nameHandler.nameDisplay = "#test-case-name";
		nameHandler.nameHidden = "#test-case-raw-name";
		nameHandler.referenceHidden = "#test-case-raw-reference";
		
		
		
		//****** tabs configuration *******
		
		var fragConf = {
			beforeLoad : Frag.confHelper.fnCacheRequests,	
			cookie : "testcase-tab-cookie"
		};
		Frag.init(fragConf);
		
		// ******** calling test cases ***************************

		var callingTcConf = {
			'aaData' : ${json:serialize(callingTestCasesModel.aaData)}
		}
		
		var table = $("#calling-test-case-table").squashTable(callingTcConf, {});
		
		<c:if test="${testCase.project.bugtrackerConnected }">
		// ********* bugtracker ************
		bugtracker.btPanel.load({
			url : "${btEntityUrl}",
			label : "${tabIssueLabel}"
		});
		</c:if>
		
		
		// ***** other events from the contextual content ********			
		eventBus.onContextual('tc-req-links-updated', function(){
			$("#verified-requirements-table").squashTable().refresh();
			try{
				$("#test-steps-table").squashTable().refresh();
			}catch(notloadedyet){
				//no problems
			}			
		});
		
		//**** cuf sections ************
		
		<c:if test="${hasCUF}">
		//load the custom fields
		$.get("${customFieldsValuesURL}?boundEntityId=${testCase.boundEntityId}&boundEntityType=${testCase.boundEntityType}")
		.success(function(data){
			$("#test-case-description-table").append(data);
		});
		</c:if>
		
		
		//************** other *************
		
		$("#rename-test-case-button").squashButton();
		$("#print-test-case-button").squashButton();

		$("#print-test-case-button").click(function(){
			window.open("${testCaseUrl}?format=printable", "_blank");
		});
	});
	
	});
});
</script>

