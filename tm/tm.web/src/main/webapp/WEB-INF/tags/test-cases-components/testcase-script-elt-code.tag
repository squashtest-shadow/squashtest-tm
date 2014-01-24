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
<%@ tag description="test automation panel (test case level)" body-content="empty"%>


<%@ attribute name="testCase" type="java.lang.Object" required="true"
	description="the test case"%>
<%@ attribute name="canModify" required="no" type="java.lang.Boolean" 
	description="whether the script name link is editable (or not). Default is false."%>
<%@ attribute name="testCaseUrl" required="yes" description="the url where to reach the test case"%>


<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>


<c:if test="${canModify == true}">
	<pop:popup id="ta-picker-popup" closeOnSuccess="false" openedBy="ta-picker-link"
			   titleKey="test-case.testautomation.popup.title" 
			   usesRichEdit="false" isContextual="true" >
	
	<jsp:attribute name="buttonsArray">
		{
			'text'	: '<f:message key="test-case.testautomation.popup.ok"/>',
			'class' : 'ta-picker-confirm'
		},
		{
			'text' : '<f:message key="label.Cancel" />',
			'class': 'ta-picker-cancel'
		}
	</jsp:attribute>	
	
	<jsp:attribute name="additionalSetup">
		height : 500	
	</jsp:attribute>
	
	<jsp:attribute name="body">
	<div class="ta-picker-structure-maindiv">
	
	 	<div class="structure-pleasewait">
 			<comp:waiting-pane/>		
 		</div>
	
		<div class="structure-error">
			<span> </span>
		</div>
	
		<div class="structure-treepanel has-standard-margin">
			<div class="structure-tree"></div>		
		</div>
		
	</div>
	</jsp:attribute>
	
	</pop:popup>
	
<f:message var="deleteAutoTitle" key='title.confirmDeleteAutomatedTestLink'/>
<div id="test-automation-removal-confirm-dialog" class="not-displayed popup-dialog" title="${ deleteAutoTitle }">
<strong><f:message key='message.confirmDeleteAutomatedTestLink'/></strong>
</div>
<script>


</script>
	<script type="text/javascript">
	require(["common"], function() {
		require(["jquery"], function($) {
			
		$(function(){

			var pickerSettings = {
				selector : "#ta-picker-popup",
				testAutomationURL : "${testCaseUrl}/test-automation/tests",
				baseURL : "${pageContext.servletContext.contextPath}",
				successCallback : function(newName){ $("#ta-picker-link").text(newName); 
				$("#remove-ta-link").show();},
				messages : {
					noTestSelected : '<f:message key="test-case.testautomation.popup.error.noselect"/>'
				}
			};
			
			new TestAutomationPicker(pickerSettings);
			
			var removerSettings = {
				linkSelector : "#remove-ta-link",
				automatedTestRemovalUrl : "${testCaseUrl}/test-automation",
				successCallback : function(newName){ $("#ta-picker-link").text("<f:message key='label.dot.pick'/>");
				$("#remove-ta-link").hide();},
				confirmPopupSelector : "#test-automation-removal-confirm-dialog"
				
			};
			
			new TestAutomationRemover(removerSettings);
			
			
		});

		});
	});
	</script>
</c:if>
			