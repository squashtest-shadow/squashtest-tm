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
	   			"squashtable",
	   			"jquery.squash.formdialog"], 
	   				function($, basic, contentHandlers, Frag, bugtracker, eventBus){

	$(function(){

		// buttons and toggle panels
		basic.init();
		
		//init the rename popup
		
		$("#rename-test-case-dialog").formDialog();
		
		$("#rename-test-case-button").on('click', function(){
			$( "#rename-test-case-dialog" ).formDialog('open');
		});
		
		$( "#rename-test-case-dialog" ).on( "formdialogopen", function(event, ui) {
			var name = $.trim($('#test-case-raw-name').text());
			$("#rename-test-case-input").val(name);			
		});
		
		$("#rename-test-case-dialog").on('formdialogconfirm', function(){
			
			var dialog = $("#rename-test-case-dialog");
			var newName = $("#rename-test-case-input").val();
			
			$.ajax({
				url : "${testCaseUrl}",
				type : "POST",
				dataType : "json",
				data : { 'newName' : newName}
			}).success(function(){
				eventBus.trigger('node.rename', { identity : identity, newName : newName});
				dialog.formDialog('close');
			});
		});
		
		$("#rename-test-case-dialog").on('formdialogcancel', function(){
			$("#rename-test-case-dialog").formDialog('close');
		});
		
		
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

