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
<%@ tag description="Popup filling informations regarding issues" body-content="empty" %>
	
<%@ attribute name="id" required="true" description="the desired name for that popup" %>
<%@ attribute name="url" required="true" description="the base url pointing to the bugtracker controller"%>
<%@ attribute name="entity" type="java.lang.Object" required="true" description="the entity we need to attach a bug to"%>
<%@ attribute name="entityUrl" required="true" description="the url of the entity (bugtracker-wise)"%>
<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface"%>
<%@ attribute name="projectIdentifier" required="true" description="the String representation of the project identifier. The actual content depends on the choice of the controller." %>
<%@ attribute name="successCallback" required="false" description="if set, that callback will be called on successfull completion. It must accept as a parameter a json object having an attribute named 'url'."%>

<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>


<%-- 
The following urls aren't defined with a <c:url> but regular <c:set>. 
The reason for that is that the parameters are urls already.
--%>
<c:set var="bugReport" value="${entityUrl}/bug-report"/>

<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/bugtracker-dialog.js"></script>

<%-- state manager code of the popup --%>
<script type="text/javascript">
	function flipToPleaseWait(){
		$("#${id}-pleasewait").removeClass("not-displayed");
		$("#${id}-content").addClass("not-displayed");		
	}
	
	function flipToReport(){
		$("#${id}-pleasewait").addClass("not-displayed");
		$("#${id}-content").removeClass("not-displayed");			
	}

	

 	function toggleReportStyle(){
		$("#${id}-pleasewait").toggleClass("not-displayed");
		$("#${id}-content").toggleClass("not-displayed");	
 	}
 	
 	<%--  init code section --%>
	$(function(){
		$("#${id}").bind("dialogopen",function(){
			flipToPleaseWait();
	 		getBugReportData()
	 		.then(function(json){
				flushReport();
				fillReport(json);
	 		})
	 		.fail(bugReportDataError);
		});
		
	});

	function getBugReportData(){

		return $.ajax({
			url : "${bugReport}",
			type : "GET",
			dataType : "json"			
		});
	}
	
 	function flushReport(){
 		var jqPriority = $("#issue-report-select-priority");
 		var jqVersion = $("#issue-report-select-version");
 		var jqAssignee = $("#issue-report-select-assignee");
 		var jqCategory = $("#issue-report-select-category");
 		
 		flushSelect(jqPriority);
 		flushSelect(jqVersion);
 		flushSelect(jqAssignee);
 		flushSelect(jqCategory);
 	}
 	
 	function fillReport(jsonData){
 		var jqPriority = $("#issue-report-select-priority");
 		var jqVersion = $("#issue-report-select-version");
 		var jqAssignee = $("#issue-report-select-assignee");
 		var jqCategory = $("#issue-report-select-category");
 		
 		var priorities = jsonData.priorities;
 		var users = jsonData.users;
 		
 	 	<%-- those two next may represent empty lists so we handle them here --%>
 		
 		var categories = handleEmptyList(jsonData.categories,"${interfaceDescriptor.noCategoryLabel}");
 		var versions = handleEmptyList(jsonData.versions,"${interfaceDescriptor.noVersionLabel}");
 		
 		populateSelect(jqPriority,priorities);
 		populateSelect(jqVersion,versions);
 		populateSelect(jqAssignee,users);
 		populateSelect(jqCategory,categories);
 		
 		$("#issue-report-description").val(jsonData.defaultDescription);
 		
 		$("#issue-report-project").val(jsonData.projectId);
 		
 		flipToReport();
 	}
 	
 	
 	function bugReportDataError(jqXHR, textStatus, errorThrown){
 		flipToReport();
 	}

	<%-- posting code section --%>
	function prepareAndSubmit(){
		var issue = prepareIssueData();
		
		submitIssue(issue)
		.done(submitIssueSuccess)
		.fail(submitIssueFails);
	}
	
	function submitIssue(issue){
		flipToPleaseWait();
		
		return $.ajax({
			url: "${bugReport}",
			type:"POST",
			dataType : "json",
			data : issue
		});
	}
	
	function submitIssueSuccess(json){
		$("#${id}").dialog("close");
		<c:if test="${not empty successCallback}">${successCallback}(json);</c:if>
	}
	
	function submitIssueFails(){
		flipToReport();
	}

	function makeProject(){
		var id = $("#issue-report-project").val();
		var name = "${projectIdentifier}";
		return new btEntity(id, name);
	}

	function prepareIssueData(){
		<%-- would be beautyful if only we could... 
		return $("#issue-report-form").serializeArray();
		instead we go for the following : --%>
		
		var jqPriority = $("#issue-report-select-priority");
 		var jqVersion = $("#issue-report-select-version");
 		var jqAssignee = $("#issue-report-select-assignee");
 		var jqCategory = $("#issue-report-select-category");
 		
 		<%-- the following variables are of type btEntity, see squashtest/bugtracker-dialog.js--%>
 		var priority = extractSelectData(jqPriority);
 		var version = extractSelectData(jqVersion);
 		var assignee = extractSelectData(jqAssignee);
 		var category = extractSelectData(jqCategory);
 		var project = makeProject();
 			
 		<%-- setting the final data --%>
 		var issue = new Object();
 		
 		issue.project=project.format();
 		issue.priority=priority.format();
 		issue.version=version.format();
 		issue.assignee=assignee.format();
 		issue.category=category.format();
		issue.summary=$("#issue-report-summary").val();
		issue.description=$("#issue-report-description").val();
		issue.comment=$("#issue-report-comment").val();
		
		return issue;
	}
</script>

<pop:popup id="${id}" openedBy="none" isContextual="true" 
		titleKey="dialog.issue.report.title" closeOnSuccess="false">
		
 	<jsp:attribute name="buttons"> 	
		<f:message var="label" key="dialog.button.add.label" />
		'${ label }': prepareAndSubmit,			
		<pop:cancel-button />
 	</jsp:attribute> 
 	<jsp:attribute name="additionalSetup">
 		height : 400,
 		width : 550
 	</jsp:attribute>
 	<jsp:attribute name="body"> 
 		<div id="${id}-pleasewait" style="vertical-align:middle;">
 			<img src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" />
			<span style="font-size:1.5em;"><f:message key="squashtm.processing"/></span>
 		</div>
		<span id="${id}-pleasewait" class="not-displayed please-wait-message" ><f:message key="dialog.issue.pleasewait"/></span>
		
	 	<div id="${id}-content" >	
	 		<form id="issue-report-form" >
	 			<input type="hidden" id="issue-report-project" name="projectId"/>
	 			<comp:error-message forField="bugtracker" /><br/><br/>
	 			
	 			<div class="display-table">
	 				<div class="display-table-row">
	 					<div class="display-table-cell">
							<label>${interfaceDescriptor.priorityLabel}</label>
						</div>
						<div class="display-table-cell">
							<select id="issue-report-select-priority" name="priorityId" style="width:100%;"></select>
						</div>
						<div class="display-table-cell">
							<label>${interfaceDescriptor.categoryLabel}</label>
						</div>
						<div class="display-table-cell">
							<select id="issue-report-select-category" name="categoryId" style="width:100%;"></select>
						</div>	
					</div>
							
	 				<div class="display-table-row">
 						<div class="display-table-cell">
							<label>${interfaceDescriptor.versionLabel}</label>
						</div>
						<div class="display-table-cell">	
							<select id="issue-report-select-version" name="versionId" style="width:100%;"></select>
						</div>
						<div class="display-table-cell">
							<label>${interfaceDescriptor.assigneeLabel}</label>
						</div>
						<div  class="display-table-cell">	
							<select id="issue-report-select-assignee" name="assigneeId" style="width:100%;"></select>
							
						</div>
					</div>
				</div>
				<br/>
				
				<f:message var="summarySize" key="dialog.issue.report.summary.size"/>
				<label>${interfaceDescriptor.summaryLabel}</label><input type="text" id="issue-report-summary" name="summary" maxlength="${summarySize}" style="width:100%"/> <br/>
				<br/>
				
				<label>${interfaceDescriptor.descriptionLabel}</label> <br/>
				<textarea id="issue-report-description" name="description"></textarea> <br/>
				
				<label>${interfaceDescriptor.commentLabel}</label><br/>
				<textarea id="issue-report-comment" name="comment"></textarea>
			</form>
		</div>
 		
 	</jsp:attribute>
 </pop:popup>




 