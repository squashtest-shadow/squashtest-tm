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
<f:message var="addIssueLabel" key="dialog.button.add.label" />

<%-- 
The following urls aren't defined with a <c:url> but regular <c:set>. 
The reason for that is that the parameters are urls already.
--%>
<c:set var="bugReport" value="${entityUrl}/bug-report"/>


<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/bugtracker-dialog.js"></script>

 <!-- 
<script type="text/javascript" src="http://localhost/scripts/report-issue-dialog.js"></script>
 -->

<%-- state manager code of the popup --%>
<script type="text/javascript">
	function flipToPleaseWait(){
		$("#${ id } .pleasewait").removeClass("not-displayed");
		$("#${ id } .content").addClass("not-displayed");		
	}
	
	function flipToReport(){
		$("#${ id } .pleasewait").addClass("not-displayed");
		$("#${ id } .content").removeClass("not-displayed");			
	}

	

 	function toggleReportStyle(){
		$("#${ id } .pleasewait").toggleClass("not-displayed");
		$("#${ id } .content").toggleClass("not-displayed");	
 	}
 	
 	<%--  init code section --%>
	$(function(){
		$("#${ id }").bind("dialogopen",function(){
			$( '.post-issue-button' ).button('option', 'disabled', true);		
			flipToPleaseWait();
	 		getBugReportData()
	 		.then(function(json){
				flushReport();
				fillReport(json);
				$( '.post-issue-button' ).button('option', 'disabled', false);		
	 		})
	 		.fail(bugReportDataError);
		});
		
	});

	function getBugReportData(){

		return $.ajax({
			url : "${ bugReport }",
			type : "GET",
			dataType : "json"			
		});
	}
	
 	function flushReport(){
 		var jqPriority = $("#${id} .priority-select");
 		var jqVersion = $("#${id} .version-select");
 		var jqAssignee = $("#${id} .assignee-select");
 		var jqCategory = $("#${id} .category-select");
 		
 		flushSelect(jqPriority);
 		flushSelect(jqVersion);
 		flushSelect(jqAssignee);
 		flushSelect(jqCategory);
 	}
 	
 	function fillReport(jsonData){
 		var jqPriority = $("#${id} .priority-select");
 		var jqVersion = $("#${id} .version-select");
 		var jqAssignee = $("#${id} .assignee-select");
 		var jqCategory = $("#${id} .category-select");
 		
 		var priorities = jsonData.priorities;
 		
 		
 	 	<%-- those two next may represent empty lists so we handle them here --%>
 	 	var users = handleEmptyList(jsonData.users,"${ interfaceDescriptor.emptyAssigneeListLabel }");
 		var categories = handleEmptyList(jsonData.categories,"${ interfaceDescriptor.emptyCategoryListLabel }");
 		var versions = handleEmptyList(jsonData.versions,"${ interfaceDescriptor.emptyVersionListLabel }");
 		
 		populateSelect(jqPriority,priorities);
 		populateSelect(jqVersion,versions);
 		populateSelect(jqAssignee,users);
 		populateSelect(jqCategory,categories);
 		
 		$("#${id} .description-text").val(jsonData.defaultDescription);
 		
 		$("#${id} .project-id").val(jsonData.projectId);
 		
 		flipToReport();
 	}
 	
 	
 	function bugReportDataError(jqXHR, textStatus, errorThrown){
 		flipToReport();
 	}

	<%-- posting code section --%>
	function prepareAndSubmit(){		
		$( '.post-issue-button' ).button('option', 'disabled', true);		
		var issue = prepareIssueData();
		
		submitIssue(issue)
		.done(submitIssueSuccess)
		.fail(submitIssueFails);
	}
	
	function submitIssue(issue){
		flipToPleaseWait();
		
		return $.ajax({
			url: "${ bugReport }",
			type:"POST",
			dataType : "json",
			data : issue
		});
	}
	
	function submitIssueSuccess(json){
		$("#${ id }").dialog("close");
		$( '.post-issue-button' ).button('option', 'disabled', false);		
		<c:if test="${not empty successCallback}">${successCallback}(json);</c:if>
	}
	
	function submitIssueFails(){
		$( '.post-issue-button' ).button('option', 'disabled', false);		
		flipToReport();
	}

	function makeProject(){
		var id = $("#${id} .project-id").val();
		var name = "${projectIdentifier}";
		return new btEntity(id, name);
	}

	function prepareIssueData(){
		
 		var jqPriority = $("#${id} .priority-select");
 		var jqVersion = $("#${id} .version-select");
 		var jqAssignee = $("#${id} .assignee-select");
 		var jqCategory = $("#${id} .category-select");
 		
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
		
 		issue.summary=$("#${id} .summary-text").val();
		issue.description=$("#${id} .description-text").val();
		issue.comment=$("#${id} .comment-text").val();
		
		return issue;
	}
</script>

<pop:popup id="${id}" openedBy="none" isContextual="true" 
		titleKey="dialog.issue.report.title" closeOnSuccess="false">
	<jsp:attribute name="buttonsArray">
		{
			text: '${ addIssueLabel }',
			click: prepareAndSubmit, 
			'class': 'post-issue-button'
		}, {
			text: "<f:message key='dialog.button.cancel.label'/>",
			click: function() {
				$( this )
					.data('answer', 'cancel')
					.dialog( 'close' );
			}
		}
	</jsp:attribute>	
 	<jsp:attribute name="additionalSetup">
 		height : 400,
 		width : 550
 	</jsp:attribute>
 	<jsp:attribute name="body"> 
 	<div class="issue-report-dialog">
 		<div class="pleasewait" style="vertical-align:middle;">
 			<img src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" />
			<span style="font-size:1.5em;"><f:message key="squashtm.processing"/></span>
 		</div>
		
	 	<div class="content" >	
	 		<form>
	 			<input type="hidden" class="project-id" name="projectId"/>
	 			<comp:error-message forField="bugtracker" /><br/><br/>
	 			
	 			<div class="combo-options">
		 			<div class="display-table">
		 				<div>
		 					<div>
								<label class="priority-label">${interfaceDescriptor.reportPriorityLabel}</label>
							</div>
							<div>
								<select class="priority-select" name="priorityId" style="width:100%;"></select>
							</div>
							<div>
								<label class="category-label">${interfaceDescriptor.reportCategoryLabel}</label>
							</div>
							<div>
								<select class="category-select" name="categoryId" style="width:100%;"></select>
							</div>	
						</div>
								
		 				<div>
	 						<div>
								<label class="version-label">${interfaceDescriptor.reportVersionLabel}</label>
							</div>
							<div>	
								<select class="version-select" name="versionId" style="width:100%;"></select>
							</div>
							<div>
								<label class="assignee-label">${interfaceDescriptor.reportAssigneeLabel}</label>
							</div>
							<div>	
								<select class="assignee-select" name="assigneeId" style="width:100%;"></select>
								
							</div>
						</div>
					</div>
				</div>
				
				<f:message var="summarySize" key="dialog.issue.report.summary.size"/>
				
				<div class="text-options">
					<div>
						<label>${interfaceDescriptor.reportSummaryLabel}</label>
						<input type="text" class="summary-text" name="summary" maxlength="${summarySize}" style="width:100%"/> 
					</div>
					<div>
						<label>${interfaceDescriptor.reportDescriptionLabel}</label>
						<textarea class="description-text" name="description"></textarea>
					</div>
					<div>
						<label>${interfaceDescriptor.reportCommentLabel}</label>
						<textarea class="comment-text" name="comment"></textarea>
					</div>
				</div>
			</form>
		</div>
 	</div>
 	</jsp:attribute>
 </pop:popup>




 