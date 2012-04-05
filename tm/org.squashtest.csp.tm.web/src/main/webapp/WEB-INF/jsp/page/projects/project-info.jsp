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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="projectUrl" value="/projects/{projectId}">
	<s:param name="projectId" value="${project.id}" />
</s:url>
<s:url var="projectsUrl" value="/projects" />

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
							key="project.header.title" />
					</label><a id="project-name-header" href="javascript:void(0);"><c:out
							value="${ project.name }" escapeXml="true" />
					</a>
				</h2>
			</div>

			<div style="float: right;">
				<f:message var="back" key="fragment.edit.header.button.back" />
				<input id="back" type="button" value="${ back }" />
				
			</div>

			<div style="clear: both;"></div>

		</div>
	
		<div class="fragment-body">
			<%------------------------------------------------ BODY -----------------------------------------------%>
	
			<div id="project-toolbar" classes="toolbar-class ui-corner-all"><%---INFO + Toolbar ---------------------%>
			<div>
				<comp:general-information-panel auditableEntity="${project}"
						entityUrl="${ projectUrl }" />
			</div>
				
			<div class="toolbar-button-panel">
				<f:message var="rename" key="project.button.rename.label" />
				<input type="button" value="${ rename }" id="rename-project-button"
					class="button" />
					<f:message var="delete" key='project.button.delete.label' />
					<input type="button" value="${ delete }" id="delete-project-button" class="button" />
			</div>
			</div><%-------------------------------------------------------------END INFO + Toolbar ---------------%>
			<%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
			<br />
			<comp:rich-jeditable targetUrl="${ projectUrl }"
				componentId="project-label" />
			<comp:rich-jeditable targetUrl="${ projectUrl }"
				componentId="project-description" />
			
	
			<comp:toggle-panel id="project-info-panel"
				titleKey="project.info.panel.title" isContextual="true" open="true"
				classes="information-panel">
	
				<jsp:attribute name="body">
					<div id="project-description-table" class="display-table">
						<div class="display-table-row">
							<label for="project-label" class="display-table-cell">
							<f:message key="project.label.label" />
							</label>
							<div class="display-table-cell" id="project-label">${ project.label }</div>
						</div>
						<div class="display-table-row">
							<label for="project-description" class="display-table-cell">
							<f:message key="project.description.label" />
							</label>
							<div class="display-table-cell" id="project-description">${ project.description }</div>
						</div>
						<div class="display-table-row">
						<f:message var="active" key="project.active.label" />
						<f:message var="activate" key="project.activate.label" />
						<f:message var="inactive" key="project.inactive.label" />
						<f:message var="inactivate" key="project.inactivate.label" />
						<label for="project-active" class="display-table-cell "><f:message key="project.state.label"/></label>
							<div class="display-table-cell" id="project-active">
								<c:if test="${project.active}">		
								<span class="projectActive">${active} </span>
										<a id="activateProject" href="javascript:void(0);">[${inactivate}]</a>
								</c:if>
								<c:if test="${!project.active}">
								<span class="projectInactive">${inactive} </span>
										<a id="activateProject" href="javascript:void(0);">[${activate}]</a>
								</c:if>
							</div>
						</div>
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
			<%-----------------------------------END INFORMATION PANEL -----------------------------------------------%>
		</div>
		<%---------------------------------------------------------------END  BODY -----------------------------------------------%>
	<comp:decorate-buttons />
	</jsp:attribute>
</layout:info-page-layout>

<script type="text/javascript">
	var changeActive = ${!project.active};
	
	$(function() {

		$("#back").button().click(clickProjectBackButton);

		$('#activateProject').click(function() {
			changeActiveProject(changeActive);
		});
		
		$('#delete-project-button').button().click(deleteProject);
	});
	
	function clickProjectBackButton(){
			document.location.href = "${projectsUrl}";
	}
	function changeActiveProject(active) {

		requestProjectActivation(active).done(function(data) {
			refreshProjectActivationSuccess(data);
		});
	}

	function requestProjectActivation(active) {
		return $.ajax({
			type : 'post',
			data : {
				'isActive' : active
			},
			dataType : "json",
			url : "${ projectUrl }"
		});
	}

	function refreshProjectActivationSuccess(data) {
		var isNowActive = data.active;
		if (isNowActive) {
			var labelInactive = $('#project-description-table .projectInactive');
			labelInactive.removeClass('projectInactive');
			labelInactive.addClass('projectActive');
			labelInactive.text("${active}");

			var linkActivate = $('#project-description-table a#activateProject');
			linkActivate.text("[${inactivate}]");
			changeActive = !isNowActive;
		} else {
			var labelInactive = $('#project-description-table .projectActive');
			labelInactive.removeClass('projectActive');
			labelInactive.addClass('projectInactive');
			labelInactive.text("${inactive}");

			var linkActivate = $('#project-description-table a#activateProject');
			linkActivate.text("[${activate}]");
			changeActive = !isNowActive;
		}
	}
	
	function deleteProject(){
		oneShotConfirm("<f:message key='dialog.delete-project.title'/>",
		"<f:message key='dialog.delete-project.message'/>",
		"<f:message key='dialog.button.confirm.label'/>",
		"<f:message key='dialog.button.cancel.label'/>").done(function(){
			requestProjectDeletion().done(deleteProjectSuccess).fail(deleteProjectError);
			});
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
	function deleteProjectError(jqXHR, textStatus, errorThrown){
		var json = jQuery.parseJSON(jqXHR.responseText);
		
		if (json != null && json.actionValidationError != null){
			if (json.actionValidationError.exception === "CannotDeleteProjectException"){						
				oneShotDialog("<f:message key='popup.title.error'/>","<f:message key='project.delete.cannot.exception'/>");		
			}
		}
	}
</script>

<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->
<comp:popup id="rename-project-dialog"
	titleKey="dialog.rename-project.title" isContextual="true"
	openedBy="rename-project-button">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="dialog.button.rename-tree-node.label" />
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
		var name = $('#project-name-header').text();
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
		<input type="text" id="rename-project-input" maxlength="255" />
		<br />
		<comp:error-message forField="name" />
	</jsp:body>
</comp:popup>
<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->