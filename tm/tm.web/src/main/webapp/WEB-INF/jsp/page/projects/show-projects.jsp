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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<layout:info-page-layout titleKey="squashtm.project.title" isSubPaged="true">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.grey.css" />
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.project.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">
		<c:url var="projectsUrl" value="/administration/projects/list" />
		<c:url var="addProjectUrl" value="/administration/projects/add" />
		<c:url var="projectDetailsBaseUrl" value="/administration/projects" />
		
		<script type="text/javascript">
	  require([ "common" ], function() {
	    require([ "domReady", "projects-manager" ], function(domReady, projects) {
	    	domReady(function() {
	        projects.init();
	    	});
	    });
	  });
	  </script>		
		<%----------------------------------- Projects Table -----------------------------------------------%>
<div class="fragment-body">
				<sec:authorize access=" hasRole('ROLE_ADMIN')">
				<input style="float: right;" type="button" value='<f:message key="project.button.add.label" />' id="new-project-button"/>
				</sec:authorize>
				<div style="clear:both"></div>
				<table id="projects-table">
					<thead>
						<tr>
							<th class="not-displayed">Id(not shown)</th> 
							<th style="width: 2em;">#</th>
							<th><f:message key="label.Name" /></th>
							<th><f:message key="label.tag" /></th>
							<th class="not-displayed"><f:message key="label.active" /></th>
							<th><f:message key="label.CreatedOn" /></th>
							<th><f:message key="label.createdBy" /></th>
							<th><f:message key="label.modifiedOn" /></th>	
							<th><f:message key="label.modifiedBy" /></th>		
						</tr>
					</thead>
					<tbody><%-- Will be populated through ajax --%></tbody>
				</table>

<sec:authorize access=" hasRole('ROLE_ADMIN')">
<comp:popup id="add-project-dialog" titleKey="dialog.new-project.title" openedBy="new-project-button">
	<jsp:attribute name="buttons">
		<f:message var="label1" key="label.Add" />
			'${ label1 }': function() {
					var url = "${ addProjectUrl }";
					<jq:ajaxcall url="url"
					 dataType="json"
					 httpMethod="POST"
					 useData="true" 
					 successHandler="refreshProjects">		
						<jq:params-bindings name="#add-project-name" description="#add-project-description" label="#add-project-label" />
					</jq:ajaxcall>
				},							
		<pop:cancel-button />
	</jsp:attribute>
			<jsp:body>
				<table>
					<tr>
						<td><label for="add-project-name"><f:message
							key="label.Name" /></label></td>
						<td><input id="add-project-name" type="text" size="50" maxlength="255" />
						<comp:error-message forField="name" /></td>
					</tr>
					<tr>
						<td><label for="add-project-description"><f:message
							key="label.Description" /></label></td>
						<td><textarea id="add-project-description"></textarea></td>
					</tr>
					<tr>
						<td><label for="add-project-label"><f:message
							key="dialog.new-project.label.label" /></label></td>
						<td><input id="add-project-label" type="text" size="50" maxlength="255" /></td>
					</tr>
				</table>
			</jsp:body>
</comp:popup>
</sec:authorize>
</div>
</jsp:attribute>
</layout:info-page-layout>