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
<layout:info-page-layout titleKey="squashtm.project.title">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />	
		
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.project.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">	
		<c:url var="projectsUrl" value="/projects/list" />
		<c:url var="addProjectUrl" value="/projects/add" />
		<c:url var="projectDetailsBaseUrl" value="/projects" />
		
		<script type="text/javascript">
					$(function() {
							$( ".deactivated-form" ).submit(function() {
								return false;
							});
							$('#new-project-button').button();
					});
					
					function refreshProjects() {
						var table = $( '#projects-table' ).dataTable();
						table.fnDraw(false);
					}
					
		
					function tableDrawCallback() {
						addHoverHandler(this);
					}	
					
					function getProjectTableRowId(rowData) {
						return rowData[0];	
					}
					
					function addHLinkToProjectLogin(row, data) {
						var url= '${ projectDetailsBaseUrl }/' + getProjectTableRowId(data) + '/info';			
						addHLinkToCellText($( 'td:eq(1)', row ), url);
					}	
					
					function projectTableRowCallback(row, data, displayIndex) {
						addHLinkToProjectLogin(row, data);
						return row;
					}
					
					function addHoverHandler(dataTable){
						$( 'tbody tr', dataTable ).hover(
							function() {
								$( this ).addClass( 'ui-state-highlight' );
							}, 
							function() {
								$( this ).removeClass( 'ui-state-highlight' );
							} 
						);
					}		
					
					
		</script>
		
		<%----------------------------------- Projects Table -----------------------------------------------%>
		
<!-- 
	table structure (columns):
	
		* id (not shown)
		* selecthandle
		* name,
		* label
		* isActive,
		* created on
		* created by
		* modified on
		* modified by

 -->

<div class="fragment-body">
				<input style="float: right;" type="button" value='<f:message key="project.button.add.label" />' id="new-project-button"/>
				<div style="clear:both"></div>
				<comp:decorate-ajax-table url="${ projectsUrl }" tableId="projects-table" paginate="true">
					<jsp:attribute name="drawCallback">tableDrawCallback</jsp:attribute>
					<jsp:attribute name="initialSort">[[2,'asc']]</jsp:attribute>
					<jsp:attribute name="rowCallback">projectTableRowCallback</jsp:attribute>
					<jsp:attribute name="columnDefs">
						<dt:column-definition targets="0" visible="false" />
						<dt:column-definition targets="1" width="2em" cssClass="select-handle centered" sortable="false"/>
						<dt:column-definition targets="3, 4" sortable="false"/>
						<dt:column-definition targets="2, 5, 6, 7" sortable="true"/>
						<dt:column-definition targets="8" sortable="true" lastDef="true"/>
					</jsp:attribute>
				</comp:decorate-ajax-table>
				
				<table id="projects-table">
					<thead>
						<tr>
							<th>Id(not shown)</th> 
							<th>#</th>
							<th><f:message key="label.Name" /></th>
							<th><f:message key="project.workspace.table.header.label.label" /></th>
							<th><f:message key="project.workspace.table.header.active.label" /></th>
							<th><f:message key="label.CreatedOn" /></th>
							<th><f:message key="project.workspace.table.header.createdby.label" /></th>
							<th><f:message key="project.workspace.table.header.modifiedon.label" /></th>	
							<th><f:message key="project.workspace.table.header.modifiedby.label" /></th>		
						</tr>
					</thead>
					<tbody><%-- Will be populated through ajax --%></tbody>
				</table>


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
						<td><input id="add-project-name" type="text" size="50" />
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
						<td><input id="add-project-label" type="text" size="50"/></td>
					</tr>
				</table>
			</jsp:body>
</comp:popup>
</div>
</jsp:attribute>
</layout:info-page-layout>