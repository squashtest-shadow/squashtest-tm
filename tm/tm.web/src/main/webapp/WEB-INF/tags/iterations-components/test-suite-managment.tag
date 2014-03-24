<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ attribute name="popupId" required="true" description="the id of the managment popup. Just supply the name and it will be generated." %>
<%@ attribute name="popupOpener" required="true" description="the id of the button that will open the popup. Must exist prior to the call to this tag." %>
<%@ attribute name="menuId" required="true" description="the id of the button opening the menu. Must exists." %>
<%@ attribute name="suiteList" type="java.lang.Object" required="true" description="the list of the suites that exist already" %>
<%@ attribute name="testSuitesUrl" required="true" description="url representing the current iteration" %>
<%@ attribute name="datatableId" required="true" description="the id of the test plan datatable"%>
<%@ attribute name="emptySelectionMessageId" required="true" description="the id of the div representing the message shown when nothing is selected"%>
<%@ attribute name="creatable" required="true" type="java.lang.Boolean" description="if the user has creation rights on the iteration" %>
<%@ attribute name="deletable" required="true" type="java.lang.Boolean" description="if the user has deletion rights on the iteration" %>

<%@ taglib prefix="pop" 	tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" 		uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" 		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s"		uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" 	tagdir="/WEB-INF/tags/component" %>	
<%@ taglib prefix="fn"		uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

 
<s:url var="baseSuiteUrl" value="/test-suites" /> 
 
 <%-- ====================== POPUP STRUCTURE DEFINITION ========================= --%>
 
<pop:popup id="${popupId}" isContextual="true"  openedBy="manage-test-suites-button" closeOnSuccess="false" titleKey="dialog.testsuites.title">

	<jsp:attribute name="buttons">
	<f:message var="closeLabel" key="label.Close" />
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
	<c:if test="${ creatable }">
		<div class="create-suites-section">
            <f:message var="defaultMessage" key="dialog.testsuites.defaultmessage" />
			<f:message var="createLabel" key="label.Add"/>
			<input type="text" size="30" placeholder="${defaultMessage}"/><input type="button" class="button" value="${createLabel}"/><br/>
			<comp:error-message forField="name" />			
		</div>	
		</c:if>
		<div class="display-suites-section">
		</div>
		
		<div class="rename-suites-section">
			<f:message var="renameLabel" key="dialog.testsuites.rename.label" />
			<input type="text" size="30"/><input type="button" class="button" value="${renameLabel}" />
		</div> 
		<c:if test="${ deletable }">
		<div class="remove-suites-section">
			<f:message var="removeLabel" key="dialog.testsuites.remove.label" />
			<input type="button" class="button" value="${removeLabel}"/>
		</div>
	</c:if>
	</div>
	</jsp:attribute>
</pop:popup>

<div id="suite-menu-empty-selection-popup" class="not-visible"
				title="<f:message key='title.suite.menu.emptySelection' />">
				<div>
					<f:message key="message.suite.menu.emptySelection" />
				</div>
			</div>
<%-- ====================== /POPUP STRUCTURE DEFINITION  ========================= --%>


<f:message var="deleteMessage" key="dialog.delete-test-suite.message" />
<f:message var="deleteTitle" key="dialog.delete-test-suite.title" />

<script type="text/javascript">
require( ["common"], function(){
	
	require(["jquery","iteration-management"], function($,main){
$(function(){	
		
		var initData = [
						<c:forEach var="suite" items="${suiteList}" varStatus="status">
							{ id : '${suite.id}', name : '${fn:replace(suite.name, "'", "\\'")}' }<c:if test="${not status.last}">,</c:if>
						</c:forEach>
					];
	
		var tableListener = {
				redraw : function(evt_name){
					//"add" is none of our business.
					if ((evt_name===undefined) || (evt_name=="node.remove") || (evt_name=="node.rename") || (evt_name =="node.bind")){
						$('#iteration-test-plans-table').squashTable().refreshRestore();	
					}
				}
			};
		
		var modelSettings = {
				createUrl : "${testSuitesUrl}/new",	
				baseUpdateUrl : "${baseSuiteUrl}",
				getUrl : "${testSuitesUrl}",
				removeUrl : "${testSuitesUrl}/delete",
				initData : initData
			};
		
		var managerSettings = {
				instance : $("#${popupId} .main-div-suites"),
				deleteConfirmMessage : "${deleteMessage}",
				deleteConfirmTitle : "${deleteTitle}"
			};
		
		var menuSettings = {
				instanceSelector : "#${menuId}",
				datatableSelector : "#${datatableId}",
				emptySelectionMessageSelector: "#${ emptySelectionMessageId }",
				emptySuiteSelectionMessageSelector: "#suite-menu-empty-selection-popup",
			};
		
		var config = {
				modelSettings : modelSettings,
				managerSettings: managerSettings,
				menuSettings : menuSettings,
				tableListener : tableListener
			};
		
		main.initTestSuiteMenu(config);

		//now we can make reappear
		$("#${popupId} .main-div-suites").removeClass("not-displayed");
	});
});
});
</script>
