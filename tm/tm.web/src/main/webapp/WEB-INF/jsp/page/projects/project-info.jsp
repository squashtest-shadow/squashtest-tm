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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="ta" tagdir="/WEB-INF/tags/testautomation"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags/input"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>

<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="projectUrl" value="/generic-projects/{projectId}">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>

<s:url var="projectsUrl" value="/administration/projects" />

<s:url var="permissionPopupUrl"
	value="/generic-projects/{projectId}/permission-popup">
	<s:param name="projectId" value="${adminproject.project.id}" />
</s:url>


<s:url var="customFieldManagerURL" 	value="/administration/projects/{projectId}/custom-fields-binding">
	<s:param name="projectId" 		value="${adminproject.project.id}"/>
</s:url>

<s:url var="wizardsManagerURL"      value="/administration/projects/{projectId}/wizards">
	<s:param name="projectId" 	    value="${adminproject.project.id}" />
</s:url>

<layout:info-page-layout titleKey="workspace.project.info.title" isSubPaged="true">
	<jsp:attribute name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/squash.grey.css" />	
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>	
	</jsp:attribute>
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.project.info.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href= '${projectsUrl}'"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">
	<c:choose>
	<c:when test="${ adminproject.template }">
		<f:message var="headerLabel" key="label.projectTemplate"/>
	</c:when><c:otherwise>
		<f:message var="headerLabel" key="label.Project"/>
	</c:otherwise>
	</c:choose>
		<div id="project-name-div"
			class="ui-widget-header ui-corner-all ui-state-default fragment-header">

			<div class="snap-left" style="height: 3em">
				<h2>
					<label for="project-name-header">
            <f:message key="${ adminproject.template ? 'label.projectTemplate' : 'label.project' }" />
					</label>
          <a id="project-name-header" href="javascript:void(0);">
            <c:out value="${ adminproject.project.name }" escapeXml="true" />
					</a>
				</h2>
			</div>

			<div class="unsnap"></div>

		</div>
	
		<%---INFO + Toolbar ---------------------%>
			<div id="project-toolbar" class="toolbar-class ui-corner-all">
				
				<div class="snap-left">
					<comp:general-information-panel auditableEntity="${adminproject.project}" entityUrl="${ projectUrl }" />
				</div>
				
				<div class="toolbar-button-panel">
					<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">
          <c:if test="${ adminproject.template }">
          <input type="button" value="<f:message key='label.coerceTemplateIntoProject' />" id="coerce" class="button" data-template-id="${ adminproject.id }" />
          <div id="coerce-warning-dialog" title="<f:message key="title.coerceTemplateIntoProject" />" class="alert not-displayed">
            <f:message key="message.coerceTemplateIntoProject" />
            <input:confirm />
            <input:cancel />
          </div>

<!-- NON ACTIVE BUTTON : WAIT FOR FEATURE TO BE REQUESTED -->
<%-- 					<c:if test="${ adminproject.template }"> --%>
<%-- 					<f:message var="createFromTemplate" key="label.createFromTemplate" /> --%>
<%-- 					<input type="button" value="${ createFromTemplate }" id="createFromTemplate-project-button" --%>
<!-- 								class="button" /> -->
 					</c:if>
					<f:message var="rename" key="project.button.rename.label" />
					<input type="button" value="${ rename }" id="rename-project-button"
								class="button" />
					</sec:authorize>
					<sec:authorize access="hasRole('ROLE_ADMIN')">
						<f:message var="delete" key='project.button.delete.label' />
						<input type="button" value="${ delete }" id="delete-project-button"
								class="button" />
					</sec:authorize>						
				</div>
				<div class="unsnap"></div>
			</div>
			<%-------------------------------------------------------------END INFO + Toolbar ---------------%>
			
			<%------------------------------------------------ BODY -----------------------------------------------%>
		
			<div class="fragment-tabs fragment-body">
			<ul>
				<li><a href="#main-informations"><f:message key="tabs.label.mainpanel"/></a></li>
				<li><a href="${customFieldManagerURL}"><f:message key="tabs.label.cufbinding"/></a></li>
				<li><a href="${wizardsManagerURL}"><f:message key="tabs.label.wizards"/></a></li>
			</ul>
		
			<%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
			<div id="main-informations">
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
								<script>
								function projectBugTrackerCallBack (value, settings) {
									squashtm.bugtrackerMenu.updateBugTrackerMenu(false);
									<c:if test="${ ! adminproject.template }">
										  if(value != "<f:message key='project.bugtracker.name.undefined'/>"){								        	 
								        	 $("#project-bugtracker-project-name-row").show();
												refreshBugTrackerProjectName();
									     }else{
								        	 $("#project-bugtracker-project-name-row").hide();								        	 
								         }
								      </c:if>
								}
								</script>
								<comp:select-jeditable componentId="project-bugtracker"
										jsonData="${bugtrackersList}" targetUrl="${projectUrl}"
										submitCallback="projectBugTrackerCallBack" />
								
							</div>
						</div>
						<c:if test="${ ! adminproject.template }">
						<div class="display-table-row"	id="project-bugtracker-project-name-row"
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
						</c:if>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
		</c:if>
		<%-----------------------------------END BUGTRACKER PANEL -----------------------------------------------%>
				<%----------------------------------- USER PANEL -----------------------------------------------%>
			<f:message key="title.AddPermission" var="addButtonTitle" />
			<comp:toggle-panel id="project-users-panel"
				titleKey="label.Permissions" isContextual="true" open="true"
				classes="users-panel">
	
				<jsp:attribute name="panelButtons">
					<input id="add-permission-button" title="${addButtonTitle}"	type="button" value="+" class="button" />
				</jsp:attribute>
				
				<jsp:attribute name="body">
					<table id="user-permissions-table">
						<thead>
							<tr>
								<th class="party-index">#</th>
								<th class="party-id"></th>
								<th class="party-name datatable-filterable"><f:message key="party.header.title" /></th>
								<th class="user-permission"><f:message key="project.permission.table.profile.label" /></th>
								<th class="party-type"><f:message key="party.type" /></th>
								<th class="empty-delete-holder"> </th>
							</tr>
						</thead>
					</table>
					<div class="not-displayed permission-select-template">
						<select>
						<c:forEach var="perm" items="${availablePermissions}">
						<option value="${perm.qualifiedName}"><f:message key="user.project-rights.${perm.simpleName}.label"/></option>
						</c:forEach>
						</select>
					</div>
					<tbody>
					</tbody>
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
			<%----------------------------- ATTACHMENT -------------------------------------------%>
			
			<comp:attachment-bloc editable="${ true }" entity="${ adminproject.project }" workspaceName=""/>
			<%----------------------------- /ATTACHMENT -------------------------------------------%>
			
			</div> <%-- /div#main-informations --%>		
				
			</div>	<%-- /div#project-administration-content --%>

		<%---------------------------------------------------------------END  BODY -----------------------------------------------%>
		
			<%----------------------------------- add User Popup-----------------------------------------------%>
		<f:message var="noUserSelectedError" key="error.permissions.noUserSelected" />
		<pop:popup id="add-permission-dialog"
				titleKey="title.AddPermission" isContextual="true"
				openedBy="add-permission-button">
			<jsp:attribute name="buttons">
			
				<%--- the slash at the end of the url below cannot be removed because it would cause the last part of the
				permission to be interpreted as a file extension by spring MVC --%>
				<f:message var="label" key="label.Add" />
				'${ label }': function() {
					var partyId = $("#party-id").val();
					
					if (partyId == "" || partyId === null || partyId === undefined){
						squashtm.notification.showInfo("${noUserSelectedError}");
					}
					else{					
						var permission = $("#permission-input").val();
						var url = squashtm.app.contextRoot+"/generic-projects/${adminproject.project.id}/parties/"+partyId+"/permissions/"+permission+"/";
						$.ajax({
							url : url,
							type : 'PUT',
						}).success(refreshTableAndPopup);	
					}				
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:attribute name="body">
				<div id="permission-popup">
				</div>
			</jsp:attribute>
		</pop:popup>
		<%----------------------------------- /add User Popup-----------------------------------------------%>
	</jsp:attribute>
</layout:info-page-layout>

<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->
<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">
	<pop:popup id="rename-project-dialog"
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
		<jsp:attribute name="body">
		<label><f:message key="dialog.rename.label" />
		</label>
		<input type="text" id="rename-project-input" maxlength="255" size="50" />
		<br />
		<comp:error-message forField="name" />
	</jsp:attribute>
	</pop:popup>
</sec:authorize>

<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->
<script type="text/javascript">

	
	/* popup renaming success handler */
	function renameProjectSuccess(data) {
		$('#project-name-header').html(data.newName);
		$('#rename-project-dialog').dialog('close');
	}
		
	
	function clickProjectBackButton(){
		document.location.href = "${projectsUrl}";
	}
	
	function refreshBugTrackerProjectName() {
		$.ajax({
			type: 'GET',
			 url: "${projectUrl}/bugtracker/projectName",
		}).done(function(data){
			$( "#project-bugtracker-project-name").text(data);
		});
		
	}
	
	function reloadPermissionPopup(){
		var permPopup = $("#permission-popup");
		permPopup.empty();
		permPopup.load("${permissionPopupUrl}");		
	}
	
	function refreshTableAndPopup(){
		reloadPermissionPopup();		
		$("#user-permissions-table").squashTable().refresh();		
	}
	


	$(function() {

		require(["common"], function(){
		 	require(["projects-manager", "jquery.squash.fragmenttabs", "project"], function(projectsManager, Frag){
		 		init(projectsManager, Frag);	
		 	});			
		});
	});
	
	function init(projectsManager, Frag){
		
		
		// back button
		$("#back").button().click(clickProjectBackButton);
		
		// permission mgt
		$("#add-permission-button").button();
		
		// rename popup
		$("#rename-project-dialog").bind("dialogopen", function(event, ui) {
			var name = $.trim($('#project-name-header').text());
			$("#rename-project-input").val(name);
	
		});
		
		// permissions popup
		reloadPermissionPopup();
		$("#add-permission-dialog").on('dialogopen', function(){$("#party-id").val('');});

		//user permissions table
		var permSettings = {
			basic : {
				projectId : ${adminproject.project.id},
				userPermissions : ${json:serialize(userPermissions)}
			},
			language : {
				ok : '<f:message key="label.Confirm"/>',
				cancel : '<f:message key="label.Cancel"/>',
				deleteMessage : '<f:message key="message.permissions.remove"/>',
				deleteTooltip : '<f:message key="tooltips.permissions.remove"/>'
			}
		};
		
		projectsManager.projectInfo.initUserPermissions(permSettings);
				

		Frag.init({
			beforeLoad : Frag.confHelper.fnCacheRequests
		});								
	}
	
	
	<sec:authorize access=" hasRole('ROLE_ADMIN')">
	$(function() {
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
		
		$('#delete-project-button').button().click(deleteProject);		
	});
	</sec:authorize>

</script>