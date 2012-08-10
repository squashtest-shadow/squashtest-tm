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
<%@ tag description="test automation panel (project level)" body-content="empty" %>

<%@ attribute name="project" type="java.lang.Object" required="true" description="the TM Project"%>
<%@ attribute name="taServer" type="java.lang.Object" required="true" description="the TA server"%>

<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>



<!--  ===================== DEV STYLESHEET, MOVE IT TO MAIN STYLESHEET WHEN READY ==================== -->

<LINK href="http://localhost/css/ta-admin-panel.css" rel="stylesheet" type="text/css">

<!--  ==================== /DEV STYLESHEET, MOVE IT TO MAIN STYLESHEET WHEN READY ==================== -->
 
 
<c:url var="listProjectsURL" value="/test-automation/servers/projects-list" />

<c:set var="initialChecked" value="checked=\"checked\""/>
<c:set var="initialDisabled" value="" />
<c:set var="initialCss" value="" />


<c:if test="${not project.testAutomationEnabled}">
	<c:set var="initialChecked"    value="" />
	<c:set var="initialDisabled"  value="disabled=\"disabled\"" />
	<c:set var="initialCss" value="ta-manager-disabled" />
</c:if>

<c:set var="inputSize" value="50" />

<comp:toggle-panel id="test-automation-management-panel" titleKey="project.testauto.panel.title" isContextual="true" open="true">

	<jsp:attribute name="body">
		<div class="ta-main-div">
		
			<div class="ta-maincheck-div ta-block">
				<label><f:message key="project.testauto.maincheckbox"/></label><input type="checkbox" id="test-auto-enabled-ckbox" ${initialChecked} />
			</div>
			
			<fieldset class="ta-server-block  ta-block  ${initialCss}">
				<legend><f:message key="project.testauto.serverblock.title"/></legend>
				<div class="ta-block-item">
					<div class="ta-block-item-unit"><label><f:message key="project.testauto.serverblock.url.label"/></label></div>
					<div class="ta-block-item-unit"><input type="text" class="ta-serverblock-url-input" value="${taServer.baseURL}" size="${inputSize}"/></div>
				</div>
				<div class="ta-block-item">
					<div class="ta-block-item-unit"><label><f:message key="project.testauto.serverblock.login.label"/></label></div>
					<div class="ta-block-item-unit"><input type="text" class="ta-serverblock-login-input" value="${taServer.login}" size="${inputSize}"/></div>
				</div>
				<div class="ta-block-item">
					<div class="ta-block-item-unit"><label><f:message key="project.testauto.serverblock.password.label"/></label></div>
					<div class="ta-block-item-unit"><input type="password" class="ta-serverblock-password-input" value="${taServer.password}" size="${inputSize}"/></div>
				</div>
			</fieldset> 
			
			<fieldset class="ta-projects-block  ta-block ${initialCss}">
				<legend><f:message key="project.testauto.projectsblock.title"/></legend>
				<!-- 
				<table class="ta-bound-projects-table">
					<
				
				
				</table>
				 -->
			</fieldset>
		
		
		</div>
		
	</jsp:attribute>
	

</comp:toggle-panel>

	
<script type="text/javascript">
	$(function(){
		
		if (! squashtm.testautomation){
			squashtm.testautomation = {};
		}
		
		var settings = {
			selector : "#test-automation-management-panel .ta-main-div",
			listProjectsURL : "${listProjectsURL}",
			initiallyEnabled : ${project.testAutomationEnabled}					
		};
		
		squashtm.testautomation.projectmanager = new TestAutomationProjectManager(settings);
	});

</script>