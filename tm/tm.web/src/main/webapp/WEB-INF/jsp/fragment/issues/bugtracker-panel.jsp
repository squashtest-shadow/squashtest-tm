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
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="agg" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="is" tagdir="/WEB-INF/tags/issues"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>


<%--

	That jsp generates the Bug section of the GUI. It's made of two components : the Issue panel, that displays the bugs
	currently associated to the corresponding entity, and the bug report form, the dialog that will submit new ones.


	parameters : 
		- entity : the instance of the entity we need to display current bugs and possible add new ones.
		- entityType : a String being the REST name of that kind of resource, like in the regular workspaces.
		- interfaceDescriptor : an instance of BugTrackerInterfaceDescriptor that will provide the bug report dialog
			with the proper labels  
		- panelStyle : must be either string among : 'toggle', 'fragment-tab', or null or empty
		- bugTrackerStatus : a BugTrackerStatus that will set the initial status of that component :
				* UNDEFINED : never happens, since the controller is not supposed to return the view in the first place,
				* NEEDS_CREDENTIALS : a label will prompt the user for login,
				* READY : nothing special, the component is fully functional.
		- useParentContextPopup : if false, will create a popup as usual. If true, will use instead a popup accessible in the parent context 
								(that's how the ieo-execute-execution.jsp works)
 --%>
 
<%-- ------------------- variables ----------------- --%>

<c:set var="executable" value="${ false }" />
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE"
	domainObject="${ entity }">
	<c:set var="executable" value="${ true }" />
</authz:authorized>

<c:if
	test="${entityType == 'iteration'||entityType == 'test-suite'||entityType == 'campaign'||entityType == 'test-case'}">
	<c:set var="executable" value="${false}" />
</c:if>

<c:if test="${empty useParentContextPopup}"><c:set var="useParentContextPopup" value="${false}" /></c:if>


<%-- ------------------- /variables ----------------- --%>


<%-- ------------------- urls ----------------- --%>

<s:url var="bugTrackerServiceUrl" value="/bugtracker" />
<s:url var="bugTrackerStatusUrl" value="/bugtracker/status"/>
<s:url var="entityUrl" value="/bugtracker/{entityType}/{id}">
	<s:param name="entityType" value="${entityType}" />
	<s:param name="id" value="${entity.id}" />
</s:url>

<s:url var="credentialsUrl" value="/bugtracker/credentials" />

<s:url var="tableUrl" value="/bugtracker/{entityType}/{id}/known-issues">
	<s:param name="entityType" value="${entityType}" />
	<s:param name="id" value="${entity.id}" />
</s:url>

<%-- ------------------- /urls ----------------- --%>


<%-------------- a bug created successfully will be shown here -----------------------------%>

<script type="text/javascript">
	function displayNewIssue(json) {
		var issueUrl = json.url;
		var issueId = json.issueId;
		$("#issue-url").attr("href", issueUrl);
		$("#issue-url").attr("target", "_blank");
		$("#issue-url").html(issueUrl);
		setMessageIssueId(issueId);
		$("#issue-panel-bugcreated-area").fadeIn('slow').delay(20000).fadeOut(
				'slow');
	}

	//since IE8 doesn't support String.replace(), we're happily doing the job manually.
	function setMessageIssueId(newId) {
		var message = $("#issue-add-success-message").html();

		var beginIndex = message.indexOf("#", 0) + 1;
		var messageRemainder = message.substr(beginIndex);
		var endIndex = messageRemainder.indexOf(" ");

		var newMessage = message.substr(0, beginIndex) + newId
				+ messageRemainder.substr(endIndex);

		$("#issue-add-success-message").html(newMessage);

	}
</script>




<%--------------------------------- /login code ------------------------------------%>

<%--
	the two first functions are actually pointers to the callback we need to use when the login popup is invoked.
	Indeed, one can login when :
		- reporting an issue before being logged in,
		- spontaneously login in if the user wants to check the known issues.
		
	Those pointers will be set accordingly prior to open the popup.
	
	
	The rest is just standard functions.

 --%>

<script type="text/javascript">
	function loginSuccess() {
	};
	function loginFail() {
	};

	function invokeCredentialPopup(fnSuccessHandler, fnFailureHandler) {
		loginSuccess = fnSuccessHandler;
		loginFail = fnFailureHandler;
		$("#issue-dialog-credentials").dialog("open");
	}

	function refreshIssueTable() {
		var dataTable = $('#issue-table').dataTable();
		dataTable.fnDraw(false);
	}

	function enableIssueTable() {
		$("#issue-panel-knownissues-div").removeClass(
				"issue-panel-knownissues-displayed");
		$("#issue-panel-knownissues-div").addClass(
				"issue-panel-knownissues-notdisplayed");
		$("#issue-panel-known-issue-table-div").removeClass("not-displayed");
	}
</script>


<%-- internal logic for loging in when one want to check the issues. Main function : checkAndReloadIssue.
	due to the asynchronous nature of ajax calls the logic depends heavily on ajax callbacks.
	Depending on the success of a call, one of another function will be called and the logic 
	continues there.
 --%>

<script type="text/javascript">
	function loginSuccessCheckIssues() {
		enableIssueTable();
		refreshIssueTable();
	}

	function loginAbort() {
		return false;
	}

	function bugTrackerLogin() {
		invokeCredentialPopup(loginSuccessCheckIssues, loginAbort);
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
	function checkAndReportIssue(bugtrackerReportSettings) {

		//first step : check
		$.ajax({
			url : "${bugTrackerStatusUrl}",
			type : "GET",
			data : {"projectId": ${project.id} },
			dataType : "json",
			success : function(jsonCheck) {handleBugTrackerStatus(jsonCheck, bugtrackerReportSettings);}
		});
	}

	function handleBugTrackerStatus(jsonCheck, bugtrackerReportSettings) {
		if (jsonCheck.status == "bt_undefined") {
<%-- the bugtracker is undefined. Why the hell should we log in ?--%>
	return false;
		}

		if (jsonCheck.status == "needs_credentials") {
			invokeCredentialPopup(function() {loginSuccessOpenReport(bugtrackerReportSettings);}, abortReport);

			return false;
		}

		if (jsonCheck.status == "ready") {
			invokeBugReportPopup(bugtrackerReportSettings);
			return false;
		}

	}

	function abortReport() {
		return false;
	}

	function loginSuccessOpenReport(bugtrackerReportSettings) {
		enableIssueTable();
		refreshIssueTable();
		 invokeBugReportPopup(bugtrackerReportSettings);
	}

	function invokeBugReportPopup(bugtrackerReportSettings) {
		<c:choose>
		<c:when test="${useParentContextPopup}">
			parent.squashtm.bugReportPopup.open( bugtrackerReportSettings );
		</c:when>
		<c:otherwise>
			squashtm.bugReportPopup.open( bugtrackerReportSettings );
		</c:otherwise>
		</c:choose>			
	}
</script>




<div id="issue-panel-bugcreated-area"
	class="not-displayed ui-corner-all ">
	<span class="ui-icon ui-icon-info icon"></span><span
		id="issue-add-success-message"><f:message
			key="issue.add.success" />&nbsp;(<a href="" id="issue-url"></a>)</span>
</div>


<%-------------------------- displays the current bugs -------------------------%>


<%-- init section for issue-panel-knownissues-div --%>
<c:choose>
	<c:when test="${bugTrackerStatus == 'BUGTRACKER_NEEDS_CREDENTIALS'}">
		<c:set var="knownIssuesLabelInitCss"
			value="issue-panel-knownissues-displayed" />
		<c:set var="knownIssuesTableInitCss" value="class=\"not-displayed\"" />
	</c:when>
	<c:otherwise>
		<c:set var="knownIssuesLabelInitCss"
			value="issue-panel-knownissues-notdisplayed" />
		<c:set var="knownIssuesTableInitCss" value="" />
	</c:otherwise>
</c:choose>


<%-- /init section for issue-panel-knownissues-div --%>

<agg:structure-configurable-panel id="issue-panel"
	titleKey="issue.panel.title" isContextual="true" open="true"
	style="${panelStyle}">

	<jsp:attribute name="panelButtons">
		<c:if test="${ executable }">
				<f:message var="issueReportOpenButtonLabel"
				key="issue.button.opendialog.label" />
				<input type="button" class="button"
				id="issue-report-dialog-openbutton"
				value="${issueReportOpenButtonLabel}" />
		</c:if>
	</jsp:attribute>


	<jsp:attribute name="body">
	
		<div id="issue-panel-knownissues-div"
			class="${knownIssuesLabelInitCss}">
			<span><f:message key="issue.panel.needscredentials.label" />
			</span>			
			<f:message var="loginButtonLabel" key="label.LogIn" />
			<input type="button" class="button" id="issue-login-button"
				value="${loginButtonLabel}" />
		</div>
	<c:choose>
		<c:when test="${ bugTrackerStatus == 'BUGTRACKER_READY' }">
			<c:set var="freeSettings" value="iDeferLoading : null "/>
		</c:when>
		<c:otherwise>
			<c:set var="freeSettings" value="iDeferLoading : 0 "/>
		</c:otherwise>
	</c:choose>
		<div id="issue-panel-known-issue-table-div"${knownIssuesTableInitCss}>
			<c:choose>
				<c:when test="${entityType == 'execution-step'}">
					<is:issue-table-execstep dataUrl="${tableUrl}" bugTrackerUrl="${bugTrackerServiceUrl}" entityId="${entity.id}"
						interfaceDescriptor="${interfaceDescriptor}" freeSettings="${ freeSettings }" executable="${executable}"/>
				</c:when>
				<c:when test="${entityType == 'execution'}">
					<is:issue-table-exec dataUrl="${tableUrl}" bugTrackerUrl="${bugTrackerServiceUrl}" entityId="${entity.id}"
						interfaceDescriptor="${interfaceDescriptor}"  freeSettings="${ freeSettings }" executable="${ executable }"/>			
				</c:when>
				
				<c:when
					test="${entityType == 'iteration'||entityType == 'test-suite'||entityType == 'campaign'}">
					<is:issue-table-iter dataUrl="${tableUrl}" interfaceDescriptor="${interfaceDescriptor}"  freeSettings="${ freeSettings }" />
				</c:when>
				<c:when test="${entityType == 'test-case' }">
					<is:issue-table-tc dataUrl="${tableUrl}" interfaceDescriptor="${interfaceDescriptor}"  freeSettings="${ freeSettings }" />
				</c:when>
			</c:choose>
		</div>
	</jsp:attribute>
</agg:structure-configurable-panel>


<%-------------------------------- add issue popup code -----------------------------------%>
<c:if test="${executable and not useParentContextPopup}">
	<is:issue-add-popup id="issue-report-dialog"
		interfaceDescriptor="${interfaceDescriptor}"  bugTrackerId="${bugTracker.id}"/>
</c:if>
<%-------------------------------- /add issue popup code -----------------------------------%>


<%--------------------------------- login code ------------------------------------%>

<%-- 
note that the successCallback and failureCallback are in the present case two pointers to the actual callbacks, 
check that in the next <script></script> tags 
--%>
<is:issue-credentials-popup url="${credentialsUrl}"
	divId="issue-dialog-credentials" successCallback="loginSuccess"
	failureCallback="loginFail" bugTrackerId="${bugTracker.id}"/>


<script type="text/javascript">
require([ "common" ], function() {
  require([ "jquery", "squash.basicwidgets" ], function($, basicwidg) {
	$(function() {
		require(["squash.basicwidgets", "workspace.event-bus"], function(basicwidg, eventBus){
			basicwidg.init();
			<c:if test="${executable}">
			$("#issue-report-dialog-openbutton").squashButton().click(function() {
				$(this).removeClass("ui-state-focus ui-state-hover");
				checkAndReportIssue( {reportUrl:"${entityUrl}/new-issue" } );
			});
			</c:if>

			$("#issue-login-button").click(function() {
				$(this).removeClass("ui-state-focus ui-state-hover");
				bugTrackerLogin();
			});
			
			eventBus.onContextual('context.bug-reported', function(evt, json){
				displayNewIssue(json);
				refreshIssueTable();
			});
		});
	});
});
</script>