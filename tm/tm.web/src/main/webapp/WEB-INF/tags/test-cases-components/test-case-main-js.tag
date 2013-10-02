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
	
		
	function deleteTestCaseSuccess() {
		<c:choose>
			<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
			<c:when test="${isInfoPage}" >		
				document.location.href="${workspaceUrl}" ;
			</c:when>
			<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
			<c:otherwise>
				location.reload(true);
			</c:otherwise>
		</c:choose>		
	}
	


	function renameTestCaseSuccess(data){
		var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };
		var evt = new EventRename(identity, data.newName);
		squashtm.workspace.contextualContent.fire(null, evt);		
	};	
	
	function updateReferenceInTitle(newRef){
		var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };
		var evt = new EventUpdateReference(identity, newRef);
		squashtm.workspace.contextualContent.fire(null, evt);		
	};

	
	$(function(){
		
		//init the rename popup
		$( "#rename-test-case-dialog" ).bind( "dialogopen", function(event, ui) {
			var name = $.trim($('#test-case-raw-name').text());
			$("#rename-test-case-input").val(name);
			
		});
		
					
		//init the renaming listener
		require(["jquery", "contextual-content-handlers", "jquery.squash.fragmenttabs", "bugtracker", "workspace.contextual-content", "jqueryui", "squashtable"], 
				function($, contentHandlers, Frag, bugtracker, contextualContent){
			
			var identity = { obj_id : ${testCase.id}, obj_restype : "test-cases"  };
			
			var nameHandler = contentHandlers.getNameAndReferenceHandler();
			
			nameHandler.identity = identity;
			nameHandler.nameDisplay = "#test-case-name";
			nameHandler.nameHidden = "#test-case-raw-name";
			nameHandler.referenceHidden = "#test-case-raw-reference";
			
			contextualContent.addListener(nameHandler);
			
			//****** tabs configuration *******
			
			var fragConf = {
				beforeLoad : Frag.confHelper.fnCacheRequests	
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
			contextualContent.addListener({
				update : function(evt){
					if (evt.evt_name === "tc-req-links-updated"){
						$("#verified-requirements-table").squashTable().refresh();
						try{
							$("#test-steps-table").squashTable().refresh();
						}catch(notloadedyet){
							//no problems
						}
					}
				}
			});
			
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
		$("#delete-test-case-button").squashButton();
		$("#print-test-case-button").squashButton();

		$("#print-test-case-button").click(function(){
			window.open("${testCaseUrl}?format=printable", "_blank");
		});
		
		
	});

	
</script>

