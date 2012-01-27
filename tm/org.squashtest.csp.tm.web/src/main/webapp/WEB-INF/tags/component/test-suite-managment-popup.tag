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
<%@ tag description="managment of iteration test suites" body-content="empty" %>
<%@ tag language="java" pageEncoding="ISO-8859-1"%>

<%@ attribute name="baseUrl" required="true" description="url representing the current iteration" %>
<%@ attribute name="divId" required="true" description="the id of the current iteration" %>
<%@ attribute name="openerId" required="true" description="the id of the button opening this manager" %>
<%@ attribute name="suiteList" type="java.lang.Object" required="true" description="the list of already existing suites" %>


<%@ taglib prefix="pop" 	tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" 		uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s"		uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" 	tagdir="/WEB-INF/tags/component" %>	


<script type="text/javascript" src="http://localhost/scripts/TestSuiteManager.js"></script>
 
<pop:popup id="${divId}" isContextual="true" openedBy="${openedId}" closeOnSuccess="false" >
	<jsp:attribute name="buttons">
		<pop:cancel-button/>
	</jsp:attribute>

	<jsp:attribute name="body">
	
	<div class="create-suites-section">
		<f:message var="createLabel" key="dialog.testsuites.create.label" />
		<input type="text" id="new-test-suite-name"/><input type="button" class="button" value="${createLabel}" />
		<comp:error-message forField="name" />
	</div>	
	
	<div class="display-suites-section">
	<c:forEach items="${suiteList}" var="item">
	<div class="suite-div ui-corner-all">
		<span data-suite-id="${item.id}"><c:out value="${item.name}" /></span>
	</div>
	</c:forEach>
	</div>
	
	<div class="rename-suites-section">
		<f:message var="renameLabel" key="dialog.testsuites.rename.label" />
		<input type="text" /><input type="button" class="button" value="${renameLabel}" />
	</div> 
	
	<div class="remove-suites-section">
		<f:message var="removeLabel" key="dialog.testsuites.remove.label" />
		<input type="button" class="button" value="${removeLabel}"/>
	</div>
	
	</jsp:attribute>
</pop:popup>

<script type="text/javascript">
	
	var testSuiteManager = null;
	
	$(function(){
		var settings = {
			instance : $("#${divId}"),
			url : "${baseUrl}"
		};
		
		testSuiteManager = new TestSuiteManager(settings);
			
	});


</script>