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
<%-- 
	Winner of the Golden WTF Award here ! Whenever we have time for refractoring DUMP THIS along with 
		- search-result-display-by-requirement
		- search-result-display-ordered-by-requirement
		- search-result-display
		
	13/10/04 : that page is still there and I still hate it
	
	14/01/24 : pruned lot of dead code and waiting for the day I will dump the remainder
	
 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<?xml version="1.0" encoding="utf-8" ?>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
	
<c:set var="servContext" value="${ pageContext.servletContext.contextPath }" />
<c:url var="objectUrl" value="${workspace}" />

<div id="search-result">
	<table id="search-result-datatable">

		<tbody>
			<c:forEach var="object" items="${resultList}" varStatus="index">
				
				<c:set var="currentProject" value="${object.project.name}"></c:set>
				
				<c:if test="${currentProject != oldProject}">
					<c:set var="oldProject" value="${currentProject}"></c:set>
					<tr>
						<td>${object.id}</td>
						<td id="searchnode-CampaignLibrary-${object.project.id}"
							class="searched-project non-tree" style="border: none;">
								<span class="icon-entity icon-root"/>
								<span class="search-text">${object.project.name}</span>
							
						</td>
						<td>${object.project.name}</td>
					</tr>

				</c:if>
				
				<c:set var="icon-class" value="${ (object.class.simpleName == 'Campaign') ? 'icon-campaign' : 
												  (object.class.simpleName == 'Iteration') ? 'icon-iteration' : 
												  'icon-folder' }"/>
				<tr>
					<td class="objectId">${object.id}</td>
					<td id="searchnode-${object.class.simpleName}-${object.id}" class="non-tree" style="border: none;">
						<span class="icon-entity ${icon-class}"></span> 
						<span class="search-text">${object.name}</span> 					
					</td>
					<td>${object.project.name }-${object.name }</td>
				</tr>

			</c:forEach>
		</tbody>
	</table>
</div>

<comp:decorate-ajax-search-table tableId="search-result-datatable">
		<jsp:attribute name="initialSort">[[2,'asc']]</jsp:attribute>
		<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" sortable="false" visible="false" />
		<dt:column-definition targets="1" sortable="false" />
		<dt:column-definition targets="2" sortable="true" visible="false"
				lastDef="true" />
	</jsp:attribute>
</comp:decorate-ajax-search-table>
