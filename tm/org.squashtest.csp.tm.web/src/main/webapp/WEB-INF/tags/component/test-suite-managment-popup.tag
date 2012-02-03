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

<%@ attribute name="divId" required="true" description="the id of the current iteration" %>
<%@ attribute name="openerId" required="true" description="the id of the button opening this manager" %>
<%@ attribute name="suiteList" type="java.lang.Object" required="true" description="the list of the suites that exist already" %>
<%@ attribute name="baseUrl" required="true" description="url representing the current iteration" %>


<%@ taglib prefix="pop" 	tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" 		uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s"		uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" 	tagdir="/WEB-INF/tags/component" %>	

<%--
<c:set var="modelScriptUrl"   value="http://localhost/scripts/TestSuiteModel.js" />
<c:set var="managerScriptUrl" value="http://localhost/scripts/TestSuiteManager.js" /> 
 --%>
<s:url var="managerScriptUrl" value="/scripts/squashtest/classes/TestSuiteManager.js"  />  
<s:url var="modelScriptUrl" value="/scripts/squashtest/classes/TestSuiteModel.js"  />  

<%-- <link rel="stylesheet" type="text/css" href="http://localhost/css/suites.css" /> --%>

 
<s:url var="baseSuiteUrl" value="/test-suites" /> 
 
 
 
 <%-- ====================== POPUP STRUCTURE DEFINITION ========================= --%>
 
 
<pop:popup id="${divId}" isContextual="true"  openedBy="${openerId}" closeOnSuccess="false" titleKey="dialog.testsuites.title">

	<jsp:attribute name="buttons">
	<f:message var="closeLabel" key="dialog.testsuites.close" />
	'${ closeLabel }': function() {
		$( this ).dialog( 'close' );
	}		
	</jsp:attribute>
	
	<jsp:attribute name="additionalSetup">
		width : 400,
		open : function(){
			squashtm.testSuiteManagement.testSuiteManager.init();
		}
	</jsp:attribute>
	

	<jsp:attribute name="body">
	
	<div class="main-div-suites not-displayed">
	
		<div class="create-suites-section">
			<f:message var="createLabel" key="dialog.testsuites.create.add" />
			<input type="text" size="30"/><input type="button" class="button" value="${createLabel}" />
			<comp:error-message forField="name" />				
		</div>	
		
		<div class="display-suites-section">
		</div>
		
		<div class="rename-suites-section">
			<f:message var="renameLabel" key="dialog.testsuites.rename.label" />
			<input type="text" size="30"/><input type="button" class="button" value="${renameLabel}" />
		</div> 
		
		<div class="remove-suites-section">
			<f:message var="removeLabel" key="dialog.testsuites.remove.label" />
			<input type="button" class="button" value="${removeLabel}"/>
		</div>
	
	</div>
	</jsp:attribute>
</pop:popup>

 <%-- ====================== /POPUP STRUCTURE DEFINITION  ========================= --%>
 


<f:message var="defaultMessage" key="dialog.testsuites.defaultmessage" />

<script type="text/javascript">
	
	function loadTestSuiteModelScript(){
		return $.ajax({
			cache : true,
			type : 'GET',
			url : "${modelScriptUrl}",
			dataType : 'script'
		});
	}
	
	
	function loadTestSuiteManagerScript(){
		return $.ajax({
			cache : true,
			type : 'GET',
			url : "${managerScriptUrl}",
			dataType : 'script'
		});		
	}

	
	$(function(){		
		$.when(loadTestSuiteModelScript(), loadTestSuiteManagerScript()) 		
		.then(function(){				
			

			squashtm.testSuiteManagement= {};
			

			var data = [
					<c:forEach var="data" items="${suiteList}" varStatus="status" >
					{id : '${data.id}', name : '${data.name}'}<c:if test="${not status.last}">,</c:if>
					</c:forEach>
			];
			
			var modelSettings = {
				createUrl : "${baseUrl}/new",	
				baseUpdateUrl : "${baseSuiteUrl}",
				data : data
			};
			

			squashtm.testSuiteManagement.testSuiteModel = new TestSuiteModel(modelSettings);
			
			var managerSettings = {
				instance : $("#${divId} .main-div-suites"),
				model : squashtm.testSuiteManagement.testSuiteModel,
				defaultMessage : "${defaultMessage}"					
			};
			
			squashtm.testSuiteManagement.testSuiteManager = new TestSuiteManager(managerSettings);
			
			$("#${divId} .main-div-suites").removeClass("not-displayed");
		});
	});


</script>
