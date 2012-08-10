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

<%@ attribute name="projectEntity" required="java.lang.Object" description="the TM Project"%>
<%@ attribute name="taServer" required="java.lang.Object" description="the TA server"%>

<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>





<c:set var="initialChecked" value="checked=\"checked\""/>
<c:set var="initialDisabled" value="" />

<c:if test="${not project.testAutomationEnabled}">
	<c:set var="initialChecked"    value="" />
	<c:set var="initialDisabled"  value="disabled=\"disabled\"" />
</c:if>

<comp:toggle-panel id="test-automation-management-panel" titleKey="project.testauto.panel.title" isContextual="true" open="true">

	<jsp:attribute name="body">
		<div class="test-automation-management-maincheck">
			<label><f:message key="project.testauto.maincheckbox"/></label><input type="checkbox" id="test-auto-enabled-ckbox" ${initialChecked} />
		</div>
		
		<fieldset id="test-automation-server-block">
			<legend><f:message key="project.testauto.serverblock.title"/></legend>
			<div class="ta-serverblock-item">
				<label><f:message key="project.testauto.serverblock.url.label"/></label>
				<input type="text" class="ta-serverblock-url-input" value="${taServer.baseURL}"/>
			</div>
			<div class="ta-serverblock-item">
				<label><f:message key="project.testauto.serverblock.login.label"/></label>
				<input type="text" class="ta-serverblock-login-input" value="${taServer.login}"/>
			</div>
			<div class="ta-serverblock-item">
				<label><f:message key="project.testauto.serverblock.password.label"/></label>
				<input type="password" class="ta-serverblock-password-input" value="${taServer.password}"/>
			</div>
		</fieldset> 
		
		<fieldset id="test-automation-projects-block">
			<legend><f:message key="project.testauto.projectsblock.title"/></legend>
		
		</fieldset>
		
		
	</jsp:attribute>

</comp:toggle-panel>