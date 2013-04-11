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
<%@ tag description="Popup filling informations regarding issues"
	body-content="empty"%>

<%@ attribute name="id" required="true"
	description="the desired name for that popup"%>
<%@ attribute name="entityUrl" required="true"
	description="the url of the entity (bugtracker-wise)"%>
<%@ attribute name="interfaceDescriptor" type="java.lang.Object"
	required="true"
	description="an object holding the labels for the interface"%>
<%@ attribute name="successCallback" required="false"
	description="if set, that callback will be called on successfull completion. It must accept as a parameter a json object having an attribute named 'url'."%>
<%@ attribute name="bugTrackerId" required="true"
	description="id of the entity's project bug-tracker"%>
<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<f:message var="addIssueLabel" key="label.Add" />

<%-- 
The following urls aren't defined with a <c:url> but regular <c:set>. 
The reason for that is that the parameters are urls already.
--%>
<c:set var="bugReport" value="${entityUrl}/new-issue" />
<c:url var="remoteIssues" value="/bugtracker/find-issue/" />


<pop:popup id="${id}" openedBy="none" isContextual="true"
	titleKey="dialog.issue.report.title" closeOnSuccess="false"
	usesRichEdit="${interfaceDescriptor.supportsRichDescription}">
	<jsp:attribute name="buttonsArray">
		{
			'text' : '${ addIssueLabel }',
			'class' : 'post-button'
		}, 
		{
			'text' : "<f:message key='label.Cancel' />",
			'click' : function() {
				$( this )
					.data('answer', 'cancel')
					.dialog( 'close' );
			}
		}
	</jsp:attribute>
	<jsp:attribute name="additionalSetup">
 		height : 500,
 		width : 650
 	</jsp:attribute>
	<jsp:attribute name="body"> 
 	<div class="issue-report-dialog">
 		<div class="pleasewait" style="vertical-align: middle;">
 			<img
					src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" />
			<span style="font-size: 1.5em;"><f:message
						key="squashtm.processing" /></span>
 		</div>
		
	 	<div class="content">	
	
	 			<div class="issue-report-error">
	 				<comp:error-message forField="bugtracker" />
	 			</div>
	 				 	
	 	
	 		<form>
	 		
	 		
	 			<div class="attach-issue">
	 				<span class="issue-radio">
	 					<input type="radio" name="add-issue-mode" class="attach-radio"
							value="attach" />
	 					<span class="issue-radio-label"><f:message
									key="dialog.issue.radio.attach.label" /></span> <!--  I don't want a <label> here because of the default style -->
	 				</span>
	 				<label>${interfaceDescriptor.tableIssueIDHeader}</label>
	 				<input type="text" class="id-text" name="issue-key" value="" />
	 				<f:message var="searchIssueLabel" key="label.Search" />
	 				<input type="button" name="search-issue"
							value="${searchIssueLabel}" />
	 			</div>
	 		
	 	
	 			
	 			<div class="issue-report-break">
	 				
	 			</div>
	 			
	 			<span class="issue-radio">
	 				<input type="radio" class="report-radio" name="add-issue-mode"
						value="report" />
	 				<span class="issue-radio-label"><f:message
								key="dialog.issue.radio.new.label" /></span>
	 			 </span>
	 			 
	 			 <div class="issue-report-fields">
					<%-- populated by javascript --%>
				</div>
			</form>
		</div>
 	</div>
 	</jsp:attribute>
</pop:popup>



<%-- state manager code of the popup --%>
<script type="text/javascript">
	
	$(function(){
		require(["bugtracker"], function(){
			 
			var conf = {					
				reportUrl : "${bugReport}",
				searchUrl : "${remoteIssues}",
				bugTrackerId : "${bugTrackerId}",
				callback : ${successCallback},
				labels : ${ json:serialize(interfaceDescriptor) }
			};
			
			//TODO : label.RequiredFields and OptionalFields
			conf.labels.requiredFields = '<f:message key="label.RequiredFields" />';
			conf.labels.optionalFields = '<f:message key="label.OptionalFields" />';
			
			$("#${id}").btIssueDialog(conf);	
			
		});	
	});
</script>
