<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

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
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<%--

	That jsp generates the Bug section of the GUI. It's made of two components : the Issue panel, that displays the bugs
	currently associated to the corresponding entity, and the bug report form, the dialog that will submit new ones.

	required in the surrounding context of that jsp : 
		- <comp:decorate-buttons />


	parameters : 
		- entity : the instance of the entity we need to display current bugs and possible add new ones.
		- entityType : a String being the REST name of that kind of resource, like in the regular workspaces.
		- projectName : the String name of the project that entity belongs to 
		- interfaceDescriptor : an instance of BugTrackerInterfaceDescriptor that will provide the bug report dialog
			with the proper labels  
		- bugTrackerStatus : a BugTrackerStatus that will set the initial status of that component :
				* UNDEFINED : never happens, since the controller is not supposed to return the view in the first place,
				* NEEDS_CREDENTIALS : a label will prompt the user for login,
				* READY : nothing special, the component is fully functional.
 --%>
<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ entity }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>

<s:url var="bugTrackerUrl" value="/bugtracker/"/>

<s:url var="entityUrl" value="/bugtracker/{entityType}/{id}" >
	<s:param  name="entityType" value="${entityType}"/>
	<s:param  name="id" value="${entity.id}"/>
</s:url>

<s:url var="credentialsUrl" value="/bugtracker/credentials" />

<s:url var="tableUrl" value="/bugtracker/{entityType}/{id}/known-issues" >
	<s:param name="entityType" value="${entityType}"/>
	<s:param  name="id" value="${entity.id}"/>
</s:url>
 

<%-------------- a bug created successfully will be shown here -----------------------------%>

<script type="text/javascript">
	function displayNewIssue(json){
		var issueUrl= json.url;
		var issueId= json.issueId;
		$("#issue-url").attr("href",issueUrl);
		$("#issue-url").html(issueUrl);
		setMessageIssueId(issueId); 
		$("#issue-panel-bugcreated-area").fadeIn('slow').delay(20000).fadeOut('slow');	
	}
	
	
	//since IE8 doesn't support String.replace(), we're happily doing the job manually.
	function setMessageIssueId(newId){
		var message = $("#issue-add-success-message").html();
		
		var beginIndex = message.indexOf("#",0)+1;
		var messageRemainder = message.substr(beginIndex);
		var endIndex = messageRemainder.indexOf(" ");
		
		var newMessage = message.substr(0,beginIndex)+newId+messageRemainder.substr(endIndex);
		
		$("#issue-add-success-message").html(newMessage);
	
	}
	

</script>



<div id="issue-panel-bugcreated-area" class="not-displayed ui-corner-all ">
	<span class="ui-icon ui-icon-info icon"></span><span id="issue-add-success-message"><f:message key="issue.add.success" />&nbsp;(<a href="" id="issue-url"></a>)</span>
</div>


<%-------------------------- displays the current bugs -------------------------%>


<%-- init section for issue-panel-knownissues-div --%>
<c:choose>
	<c:when test="${bugTrackerStatus == 'BUGTRACKER_NEEDS_CREDENTIALS'}">
		<c:set var="knownIssuesLabelInitCss" value="issue-panel-knownissues-displayed"/>
		<c:set var="knownIssuesTableInitCss" value="class=\"not-displayed\"" />	
	</c:when>
	<c:otherwise>
		<c:set var="knownIssuesLabelInitCss" value="issue-panel-knownissues-notdisplayed" />
		<c:set var="knownIssuesTableInitCss" value="" />	
	</c:otherwise>
</c:choose>


<%-- /init section for issue-panel-knownissues-div --%>

<comp:toggle-panel id="issue-panel" titleKey="issue.panel.title" isContextual="true" open="true"  >
	<jsp:attribute name="panelButtons">
	<c:if test="${ editable }">
		<f:message var="issueReportOpenButtonLabel" key="issue.button.opendialog.label" />
		<input type="button" class="button" id="issue-report-dialog-openbutton" value="${issueReportOpenButtonLabel}"/>
	</c:if>
	</jsp:attribute>
	
	<jsp:attribute name="body">
	
		<div id="issue-panel-knownissues-div" class="${knownIssuesLabelInitCss}">
			<span><f:message key="issue.panel.needscredentials.label"/></span>			
			<f:message var="loginButtonLabel" key="issue.panel.login.label" />
			<input type="button" class="button" id="issue-login-button" value="${loginButtonLabel}" />
		</div>
	
		<div id="issue-panel-known-issue-table-div" ${knownIssuesTableInitCss}>
			<c:choose>
				<c:when test="${entityType == 'execution-step'}">
					<comp:issue-table-execstep dataUrl="${tableUrl}" interfaceDescriptor="${interfaceDescriptor}"/>
				</c:when>
				<c:when test="${entityType == 'execution'}">
					<comp:issue-table-exec dataUrl="${tableUrl}" interfaceDescriptor="${interfaceDescriptor}" />			
				</c:when>
			</c:choose>
		</div>
	</jsp:attribute>
</comp:toggle-panel>


<%-------------------------------- add issue popup code -----------------------------------%>
<c:if test="${editable}">
<comp:issue-add-popup id="issue-report-dialog" entity="${executionStep}" 
					interfaceDescriptor="${interfaceDescriptor}"  
					url="${bugTrackerUrl}" entityUrl="${entityUrl}" 
					successCallback="issueReportSuccess"
					projectIdentifier="${projectName}" />
</c:if>
<%-------------------------------- /add issue popup code -----------------------------------%>


<%--------------------------------- login code ------------------------------------%>

<%-- 
note that the successCallback and failureCallback are in the present case two pointers to the actual callbacks, 
check that in the next <script></script> tags 
--%>
<comp:issue-credentials-popup url="${credentialsUrl}"  divId="issue-dialog-credentials"
							successCallback="loginSuccess" failureCallback="loginFail"	
								/>

<%--------------------------------- /login code ------------------------------------%>

<%--
	the two first functions are actually pointers to the callback we need to use when the login popup is invoked.
	Indeed, one can login when :
		- reporting an issue before being logged in,
		- spontaneously login in if the user wants to check the known issues.
		
	Those pointers will be set accordingly prior to open the popup.
	
	
	The rest is just standard functions.

 --%>
 
 <script type="text/javascript" >
 	function loginSuccess(){};
 	function loginFail(){}; 
 	
 	
	
	function invokeCredentialPopup(fnSuccessHandler, fnFailureHandler){
		loginSuccess=fnSuccessHandler;
		loginFail=fnFailureHandler;
		$("#issue-dialog-credentials").dialog("open");
	}
	 	
	
	function refreshIssueTable() {
		var dataTable = $('#issue-table').dataTable(); 
		dataTable.fnDraw();
	}
	
	
	function enableIssueTable(){
		$("#issue-panel-knownissues-div").removeClass("issue-panel-knownissues-displayed");	
		$("#issue-panel-knownissues-div").addClass("issue-panel-knownissues-notdisplayed");		
		$("#issue-panel-known-issue-table-div").removeClass("not-displayed");		
	}
	
 	
 </script>


<%-- internal logic for loging in when one want to check the issues. Main function : checkAndReloadIssue.
	due to the asynchronous nature of ajax calls the logic depends heavily on ajax callbacks.
	Depending on the success of a call, one of another function will be called and the logic 
	continues there.
 --%>
 
<script type="text/javascript" >
	
	function loginSuccessCheckIssues(){
		enableIssueTable();
		refreshIssueTable();
	}
	
	function loginAbort(){
		return false;
	}
	
	function bugTrackerLogin(){
		invokeCredentialPopup(loginSuccessCheckIssues,loginAbort);
	}
	

</script>



<%-- internal logic for reporting an issue. Main function : checkAndReportIssue
	due to the asynchronous nature of ajax calls the logic depends heavily on ajax callbacks.
	Depending on the success of a call, one of another function will be called and the logic 
	continues there.
 --%>
<script type="text/javascript">

	<%-- basic routine : if credentials are checked, proceed to bug report. If not, first hook into 
	set credential routine
	--%>
	function checkAndReportIssue(){
	
		//first step : check
		$.ajax({
			url : "${bugTrackerUrl}/check/",
			type : "GET",
			dataType : "json",
			success : handleBugTrackerStatus
		});
	}

	function handleBugTrackerStatus(jsonCheck){
		if (jsonCheck.status =="bt_undefined"){
			<%-- the bugtracker is undefined. Why the hell should we log in ?--%>
			return false;
		}
		
		if (jsonCheck.status == "needs_credentials"){
			invokeCredentialPopup(loginSuccessOpenReport,abortReport);
			
			return false;
		}
		
		if (jsonCheck.status == "ready"){
			invokeBugReportPopup();
			return false;
		}
		
	}
	
	function abortReport(){
		return false;
	}
	
	
	function issueReportSuccess(json){
		displayNewIssue(json);
		refreshIssueTable();
	}
	
	function loginSuccessOpenReport(){
		enableIssueTable();		
		refreshIssueTable();
		invokeBugReportPopup();
	}

	function invokeBugReportPopup(){
		$("#issue-report-dialog").dialog("open");
	}


		
</script>



<%-- init code, including copy pasta de decorate-button.tag, that will handle those two buttons only --%>
<script type="text/javascript">

	$(function() {
		
		<c:if test="${editable}">
		$( "#issue-report-dialog-openbutton" ).button();		
		$( "#issue-report-dialog-openbutton" ).click(function(){
			$(this).removeClass("ui-state-focus ui-state-hover");
		});
		</c:if>
		

		$( "#issue-login-button" ).button();		
		$( "#issue-login-button" ).click(function(){
			$(this).removeClass("ui-state-focus ui-state-hover");
		});
		
		
		<c:if test="${editable}">
		$("#issue-report-dialog-openbutton").click(checkAndReportIssue);
		</c:if>
		$("#issue-login-button").click(bugTrackerLogin);

																						
	});	
	
	
</script>