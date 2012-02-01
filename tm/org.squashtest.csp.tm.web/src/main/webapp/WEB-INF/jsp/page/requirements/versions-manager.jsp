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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="sq" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="gr" tagdir="/WEB-INF/tags/aggregates"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>

<layout:info-page-layout titleKey="squashtm.library.requirement.title" highlightedWorkspace="requirement" isSubPaged="true">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="squashtm.library.requirement.title" /></h2>	
	</jsp:attribute>
	
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="requirement-versions.manager.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">
		<script type="text/javascript">
			$(function() {
				$("#versions-table").dataTable({
					"oLanguage": {
						"sUrl": "<c:url value='/datatables/messages' />"
					},
					"bJQueryUI": true,
					"bAutoWidth": false,
					"bFilter": false,
					"bPaginate": true,
					"sPaginationType": "squash",
					"iDisplayLength": 20,
					"bProcessing": true,
					"bServerSide": true,
					"sAjaxSource": "<c:url value='/requirements/${ requirement.id }/versions/table' />", 
					"bRetrieve": true,				
					"sDom": 't<"dataTables_footer"lirp>',
 					/*"iDeferLoading": ${ fn:length(versions) },*/
					/* "bScrollInfinite": true, */
			        /* "bScrollCollapse": true, */
			        /* "sScrolly": "200px", */
			        /* "bSaveState": true, */ 
					"aoColumnDefs": [ 
						{ "bVisible": false, "aTargets": [0] },
						{ "bSortable": false, "aTargets": [1,2,3,4] } 
					] 
				});
			});
		</script>
		<div id="versions-table-panel">
			<table id="versions-table">
				<thead>
					<th>Id</th>
					<th><f:message key="requirement.requirement-versions.table.version-number.col-header" /></th>
					<th><f:message key="requirement.requirement-versions.table.reference.col-header" /></th>
					<th><f:message key="requirement.requirement-versions.table.name.col-header" /></th>
					<th><f:message key="requirement.requirement-versions.table.status.col-header" /></th>
				</thead>
				<tbody>
					<c:forEach var="version" items="${ versions }">
						<c:choose>
							<c:when test="version.id eq selectedVersion.id">
								<c:set var="rowClass" value="selected" />
							</c:when>
							<c:otherwise>
								<c:set var="rowClass" value="" />
							</c:otherwise>
						</c:choose>
						<tr class="${ rowClass }">
							<td>${ version.id }</td>
							<td>${ version.versionNumber }</td>
							<td>${ version.reference }</td>
							<td>${ version.name }</td>
							<td>${ version.status }</td>
						</tr>
					</c:forEach>
				</tbody>
			</table>
		</div>	
		
		<div id="selected-version-editor">
			<gr:requirement-version-editor requirementVersion="${ selectedVersion }" jsonCriticalities="${ jsonCriticalities }" />
		</div>
	</jsp:attribute>
</layout:info-page-layout>




²









