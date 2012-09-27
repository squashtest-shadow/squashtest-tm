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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="ta" tagdir="/WEB-INF/tags/testautomation"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="projectUrl" value="/administration/projects/{projectId}">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>
<s:url var="projectsUrl" value="/administration/projects" />


<s:url var="permissionTableUrl"
	value="/administration/projects/{projectId}/permission-table">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>

<s:url var="permissionPopupUrl"
	value="/administration/projects/{projectId}/permission-popup">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>

<s:url var="addPermissionUrl"
	value="/administration/projects/{projectId}/add-permission">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>

<s:url var="removePermissionUrl"
	value="/administration/projects/{projectId}/remove-permission">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>

<layout:info-page-layout titleKey="workspace.project.info.title">
	<jsp:attribute name="head">	
		<link rel="stylesheet" type="text/css"
			href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />	
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2>
			<f:message key="workspace.project.info.title" />
		</h2>	
	</jsp:attribute>

	<jsp:attribute name="informationContent">

		<div id="project-name-div"
			class="ui-widget-header ui-corner-all ui-state-default fragment-header">

			<div style="float: left; height: 3em">
				<h2>
					<label for="project-name-header"><f:message
							key="label.Project" />
					</label><a id="project-name-header" href="javascript:void(0);"><c:out
							value="${ adminproject.project.name }" escapeXml="true" />
					</a>
				</h2>
			</div>

			<div style="float: right;">
				<f:message var="back" key="label.Back" />
				<input id="back" type="button" value="${ back }" />
				
			</div>

			<div style="clear: both;"></div>

		</div>
	
		<div class="fragment-body">
			<%------------------------------------------------ BODY -----------------------------------------------%>
	
			<div id="project-toolbar" classes="toolbar-class ui-corner-all">
				<%---INFO + Toolbar ---------------------%>
			<div>
				<comp:general-information-panel
						auditableEntity="${adminproject.project}"
						entityUrl="${ projectUrl }" />
			</div>
				
			<div class="toolbar-button-panel">
				<sec:authorize access=" hasRole('ROLE_ADMIN')">
				<f:message var="rename" key="project.button.rename.label" />
				<input type="button" value="${ rename }" id="rename-project-button"
							class="button" />
					<f:message var="delete" key='project.button.delete.label' />
					<input type="button" value="${ delete }" id="delete-project-button"
							class="button" />
					</sec:authorize>
			</div>
			</div>
			<%-------------------------------------------------------------END INFO + Toolbar ---------------%>
		
			<%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
			<br />
			<comp:simple-jeditable targetUrl="${ projectUrl }" componentId="project-label" maxLength="255" />
			<comp:rich-jeditable targetUrl="${ projectUrl }" componentId="project-description" />
			
			<comp:toggle-panel id="project-info-panel"
				titleKey="project.info.panel.title" isContextual="true" open="true"
				classes="information-panel">
	
				<jsp:attribute name="body">
					<div id="project-description-table" class="display-table">
						<div class="display-table-row">
							<label for="project-label" class="display-table-cell">
							<f:message key="project.label.label" />
							</label>
							<div class="display-table-cell" id="project-label">${ adminproject.project.label }</div>
						</div>
						<div class="display-table-row">
							<label for="project-description" class="display-table-cell">
							<f:message key="label.Description" />
							</label>
							<div class="display-table-cell" id="project-description">${ adminproject.project.description }</div>
						</div>
<%-- 	Waiting for implementation of deactivation	<comp:project-active adminproject="${ adminproject }"/> --%>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
			<%-----------------------------------END INFORMATION PANEL -----------------------------------------------%>
					<%----------------------------------- BUGTRACKER PANEL -----------------------------------------------%>
			
		<c:if test="${!bugtrackersListEmpty}">
			<comp:toggle-panel id="project-bugtracker-panel"
					titleKey="label.Bugtracker" isContextual="true"
					open="true" classes="bugtacker-panel">
				<jsp:attribute name="body">
			
					<div id="project-bugtracker-table" class="display-table">
						
						<div class="display-table-row">
							<label for="project-bugtracker" class="display-table-cell">
								<f:message key="label.Bugtracker" />
							</label>
							<div class="display-table-cell">
								<div id="project-bugtracker">
									<c:choose>
										<c:when test="${ !adminproject.project.bugtrackerConnected }">
											<f:message key="project.bugtracker.name.undefined" />
										</c:when>
										<c:otherwise>
											${ adminproject.project.bugtrackerBinding.bugtracker.name }						
										</c:otherwise>
									</c:choose>
								</div>
									<script> function projectBugTrackerCallBack (value, settings) {
										updateBugTrackerMenu(false);
								         if(value != "<f:message key='project.bugtracker.name.undefined'/>"){								        	 
								        	 $("#project-bugtracker-project-name-row").show();
												refreshBugTrackerProjectName();
									     }else{
								        	 $("#project-bugtracker-project-name-row").hide();								        	 
								         }
								         }</script>
								<comp:select-jeditable componentId="project-bugtracker"
										jsonData="${bugtrackersList}" targetUrl="${projectUrl}"
										submitCallback="projectBugTrackerCallBack" />
								
							</div>
						</div>
						<div class="display-table-row"
								id="project-bugtracker-project-name-row"
								<c:if test="${ !adminproject.project.bugtrackerConnected }">style="display:none"</c:if>>
							<label for="project-bugtracker-project-name"
									class="display-table-cell">
								<f:message key="project.bugtracker.project.name.label" />
							</label>
							<comp:simple-jeditable targetUrl="${ projectUrl }"
									componentId="project-bugtracker-project-name" width="200"/>
							<div class="display-table-cell"
									id="project-bugtracker-project-name">
								<c:choose>
									<c:when test="${ adminproject.project.bugtrackerConnected }">${ adminproject.project.bugtrackerBinding.projectName }</c:when>
									<c:otherwise>${ adminproject.project.name }</c:otherwise>
								</c:choose>
							</div>
						</div>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
		</c:if>
		<%-----------------------------------END BUGTRACKER PANEL -----------------------------------------------%>
				<%----------------------------------- USER PANEL -----------------------------------------------%>
			<f:message key="title.AddPermission" var="addButtonTitle" />
			<comp:toggle-panel id="project-users-panel"
				titleKey="project.users.panel.title" isContextual="true" open="true"
				classes="users-panel">
	
				<jsp:attribute name="panelButtons">
					<input id="add-permission-button" title="${addButtonTitle}"	type="button" value="+" class="button" />
				</jsp:attribute>
				
				<jsp:attribute name="body">
					<div id="permission-table"></div>
					<div id="permission-row-buttons" class="not-displayed">
						<a id="delete-permission-button" href="#"
							class="delete-permission-button">
							<f:message key="tree.button.delete.label" />
						</a>
					</div> 
				</jsp:attribute>
			</comp:toggle-panel>
			<%-----------------------------------END USERS PANEL -----------------------------------------------%>
			
			
			<%------------------------------ TEST AUTOMATION PROJECT -------------------------------------------%>

			<ta:ta-admin-panel  
				project="${adminproject.project}" 
				taServer="${taServer}" 
				boundProjects="${boundTAProjects}"
								
			/>
			
			<%----------------------------- /TEST AUTOMATION PROJECT -------------------------------------------%>					
					
			<%----------------------------------- add User Popup-----------------------------------------------%>
		<comp:popup id="add-permission-dialog"
				titleKey="title.AddPermission" isContextual="true"
				openedBy="add-permission-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="label.Add" />
				'${ label }': function() {
					var url = "${ addPermissionUrl }";
					
					<jq:ajaxcall url="url" dataType="json" httpMethod="POST"
						useData="true" successHandler="refreshTableAndPopup">					
						<jq:params-bindings userLogin="#user-input"
							permission="#permission-input" />
					</jq:ajaxcall>	
					
					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<div id="permission-popup">
				</div>
			</jsp:body>
		</comp:popup>
		<%----------------------------------- /add User Popup-----------------------------------------------%>

		</div>
		<%---------------------------------------------------------------END  BODY -----------------------------------------------%>
	
<comp:decorate-buttons />
	</jsp:attribute>
</layout:info-page-layout>
<script type="text/javascript">

//*********************************************************************NON ADMIN SCRIPT 
//*****************Back button
	$(function() {		
		$("#back").button().click(clickProjectBackButton);
	});
	
	function clickProjectBackButton(){
		document.location.href = "${projectsUrl}";
	}
//****************End Back button
	function refreshBugTrackerProjectName() {
		$.ajax({
				type: 'GET',
				 url: "${projectUrl}/bugtracker/projectName",
		}  	
		).done(function(data){
			$( "#project-bugtracker-project-name").text(data);
		});
		
	}
    
//***************Permission management
	$(function(){
		refreshTableAndPopup();
		$("#add-permission-button").button();
		
		$("#add-permission-dialog").bind("dialogopen", function(event, ui) {
//  			if ($("#user-input option:last-child").html() == null){
//  				$(this).dialog('close');
//  				$.squash.openMessage("<f:message key='popup.title.error' />", "<f:message key='message.AllUsersAlreadyLinkedToProject' />");
//  			}
		});
		
		$(".select-class").live('change', function(){
			var url = "${addPermissionUrl}";
			var tr = $(this).parents("tr");
			var userId = $(tr).attr("id");

			$.ajax({
				  type: 'POST',
				  url: url,
				  data: "user="+userId+"&permission="+$(this).val(),
				  dataType: 'json',
				  success: function(){
					  refreshTableAndPopup();
				  }
			});
		});
		
		$(".delete-permission-button").live('click', function(){
			var url = "${removePermissionUrl}";
			var tr = $(this).parents("tr");
			var userId = $(tr).attr("id");
			
			$.ajax({
				  type: 'POST',
				  url: url,
				  data: "user="+userId,
				  success: function(){
					refreshTableAndPopup();
				  }
			});
		});
		
		
	});
	
	function getPermissionTableRowId(rowData) {
		return rowData[0];	
	}
	
	function addDeleteButtonCallBack(row, data, displayIndex){
		var id = getPermissionTableRowId(data);
		addDeleteButtonToRow(row, id, 'delete-permission-button');
		return row;
	}
	
	function refreshTableCallBack(){
		decorateDeleteButtons($('.delete-permission-button', this));
	}
	
	function refreshTableAndPopup(){
		$("#permission-table").empty();
		$("#permission-table").load("${permissionTableUrl}");
		$("#permission-popup").empty();
		$("#permission-popup").load("${permissionPopupUrl}");
	}
//************************** End Permission Management
// *****************************************************************************END NON ADMIN SCRIPT 


<sec:authorize access=" hasRole('ROLE_ADMIN')">//**********************************ADMIN SCRIPT 
	
	$(function() {
		$('#delete-project-button').button().click(deleteProject);
		
	});
	
	function deleteProject(){
	<c:if test="${adminproject.deletable}">	
		oneShotConfirm("<f:message key='dialog.delete-project.title'/>",
		"<f:message key='dialog.delete-project.message'/>",
		"<f:message key='label.Confirm'/>",
		"<f:message key='label.Cancel'/>").done(function(){
			requestProjectDeletion().done(deleteProjectSuccess);
			});
		</c:if>
		<c:if test="${!adminproject.deletable}">	
			$.squash.openMessage("<f:message key='popup.title.info'/>","<f:message key='project.delete.cannot.exception'/>");
		</c:if>
	}
	
	function requestProjectDeletion(){
		return $.ajax({
			type : 'delete',
			dataType : "json",
			url : "${ projectUrl }"
		});
	}
	function deleteProjectSuccess(data){
		clickProjectBackButton();
	}
	</sec:authorize>//**********************************************************************END ADMIN SCRIPT 
</script>

<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->
<sec:authorize access=" hasRole('ROLE_ADMIN')">
	<comp:popup id="rename-project-dialog"
		titleKey="dialog.rename-project.title" isContextual="true"
		openedBy="rename-project-button">
		<jsp:attribute name="buttons">
	
		<f:message var="label" key="label.Rename" />
		'${ label }': function() {
			var url = "${ projectUrl }";
			<jq:ajaxcall url="url" dataType="json" httpMethod="POST"
				useData="true" successHandler="renameProjectSuccess">					
				<jq:params-bindings newName="#rename-project-input" />
			</jq:ajaxcall>					
		},			
		<pop:cancel-button />
	</jsp:attribute>
		<jsp:body>
<script type="text/javascript">
	$("#rename-project-dialog").bind("dialogopen", function(event, ui) {
		var name = $.trim($('#project-name-header').text());
		$("#rename-project-input").val(name);

	});
	/* renaming success handler */
	function renameProjectSuccess(data) {
		$('#project-name-header').html(data.newName);
		$('#rename-project-dialog').dialog('close');
	}
</script>
		<label><f:message key="dialog.rename.label" />
		</label>
		<input type="text" id="rename-project-input" maxlength="255" size="50" />
		<br />
		<comp:error-message forField="name" />
	</jsp:body>
	</comp:popup>
</sec:authorize>
<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->