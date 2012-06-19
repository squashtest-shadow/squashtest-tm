<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<%@ attribute name="entityUrl" required="true" description="the url of the entity (bugtracker-wise)"%>
<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface"%>
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


<pop:popup id="${id}" openedBy="none" isContextual="true" 
		titleKey="dialog.issue.report.title" closeOnSuccess="false" usesRichEdit="${interfaceDescriptor.supportsRichDescription}">
	<jsp:attribute name="buttonsArray">
		{
			'text' : '${ addIssueLabel }',
			'class' : 'post-issue-button'
		}, 
		{
			'text' : "<f:message key='dialog.button.cancel.label'/>",
			'click' : function() {
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



<%-- state manager code of the popup --%>
<script type="text/javascript">
	$(function(){
		$.getScript("${ pageContext.servletContext.contextPath }/scripts/squashtest/jquery.squashtm.bugtracker-issue-dialog.js", function(){
			
			 $("#${id}").btIssueDialog({
				url : "${bugReport}",
				labels : {
					emptyAssigneeLabel : "${interfaceDescriptor.emptyAssigneeListLabel}",
					emptyCategoryLabel : "${interfaceDescriptor.emptyCategoryListLabel}",
					emptyVersionLabel : "${interfaceDescriptor.emptyVersionListLabel}"
				},
				callback : ${successCallback}
			});		
		});
	});
</script>



 