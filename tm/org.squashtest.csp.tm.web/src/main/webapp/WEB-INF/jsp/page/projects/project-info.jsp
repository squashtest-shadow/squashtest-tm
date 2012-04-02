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
<s:url var="administrationUrl" value="/administration" />

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
	
	
			<div>
				<comp:general-information-panel auditableEntity="${project}"
						entityUrl="${ projectUrl }" />
			</div>
	
			<%----------------------------------- User Infos -----------------------------------------------%>
			<br />
			<comp:rich-jeditable targetUrl="${ projectUrl }"
					componentId="project-label" />
			<comp:rich-jeditable targetUrl="${ projectUrl }"
					componentId="project-description" />
			
	
			<comp:toggle-panel id="project-info-panel"
					titleKey="project.info.panel.title" isContextual="true" open="true">
	
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
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
		</div>
	<comp:decorate-buttons />
	</jsp:attribute>
</layout:info-page-layout>

<script type="text/javascript">
			
				$(function() {
					
					$("#back").button().click(function(){
						document.location.href= "${administrationUrl}";
					});
				});
</script>