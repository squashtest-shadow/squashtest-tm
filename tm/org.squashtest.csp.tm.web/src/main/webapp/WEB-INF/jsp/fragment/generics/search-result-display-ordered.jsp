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
<%-- 
	Winner of the Golden WTF Award here ! Whenever we have time for refractoring DUMP THIS along with 
		- search-result-display-by-requirement
		- search-result-display-ordered-by-requirement
		- search-result-display
 --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>
<c:url var="objectUrl" value="${workspace}" />

<br/>
<div id="search-result">
	<table id="search-result-datatable">
	<thead>
		<tr>
			<th> Id </th>
			<th><f:message key="${workspace}.header.title" />s</th>
			<c:if test="${ icon != 'Requirement' }"><th><f:message key="test-case.importance.combo.label"/></th></c:if>
			<c:if test="${ icon == 'Requirement' }">
			<th><f:message key="requirement.reference.label"/></th>
			<th><f:message key="search.criticality.label"/></th>
			</c:if>
			<th><f:message key="test-case.calling-test-cases.table.project.label"/></th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="object" items="${resultList}" varStatus="index" >
			<c:set var="currentProject" value="${object.project.name}"></c:set>
			
			<c:if test="${currentProject != oldProject}">
				<tr> 
					<td> ${object.id}</td>
					<c:if test="${ icon != 'Requirement' }"><c:set var="colSpan" value="3" /></c:if>
					<c:if test="${ icon == 'Requirement' }"><c:set var="colSpan" value="4" /></c:if>
					<td colspan="${colSpan}" id="searchnode-${object.class.simpleName}Library-${object.project.id}" class="searched-project non-tree" style="border: none;"> 
						<a style="color: white; text-decoration: none; border: none;" href="#">
							<img class="search-image" src="${servContext}/images/root.png">
							<span class="search-text">${object.project.name}</span>
						</a>
					</td> 
					<td></td>
					<td></td>
					<c:if test="${colSpan ==4}"><td></td></c:if>
					
				</tr>
				<c:set var="oldProject" value="${currentProject}"></c:set>
			</c:if>
		
			<c:choose>
				<c:when test="${object.class.simpleName== icon}">
				<c:choose>
					<c:when test="${ icon == 'Requirement' }">
						<tr  class=" requirement-${object.criticality}">
								<td class = "objectId"> ${object.id}</td>
								<td id="searchnode-${object.class.simpleName}-${object.id}" class="non-tree requirement-${object.criticality}" style="border: none;">
									<a href="#" style="text-decoration: none; border: none;">
										<img class="search-image" src="${servContext}/images/Icon_Tree_${icon}.png"/>
										<span class="search-text">${object.reference}-${object.name}</span>
									</a>
								</td>
								<td>
									${object.project.name }-${object.criticality}
								</td>
								<td>
									${object.project.name }-${object.reference}
								</td>
								<td>
									${object.project.name }-${object.name }
								</td>
						
						</tr>
					</c:when>
					<c:otherwise>
						<tr>
							<td class = "objectId"> ${object.id}</td>
							<td id="searchnode-${object.class.simpleName}-${object.id}" class="non-tree" style="border: none;">
								<a href="#" style="text-decoration: none; border: none;">
									<img class="search-image" src="${servContext}/images/Icon_Tree_${icon}.png"/>
									<span class="search-text">${object.name}</span>
								</a>
							</td>
							<td><f:message key="${object.importance.i18nKey}" /></td>
							<td>
								${object.project.name }-${object.name }
							</td>
						</tr>
					</c:otherwise>
				</c:choose>		
			</c:when>
			<c:when test="${object.class.simpleName== 'Iteration'}">
				<tr> 
					<td class = "objectId"> ${object.id}</td> 
					<td id="searchnode-${object.class.simpleName}-${object.id}" class="non-tree" style="border: none;"> 
						<a href="#" style="text-decoration: none; border: none;">
							<img class="search-image" src="${servContext}/images/Icon_Tree_Iteration.png">
							<span class="search-text">${object.name}</span>
						</a>
					</td>
					<td>---</td>
					<td>
						${object.project.name }-${object.name }
					</td>
				</tr>	
				</c:when>
				<c:otherwise>
					<tr> 
						<td class = "objectId"> ${object.id}</td>
						<td id="searchnode-${object.class.simpleName}-${object.id}" class="non-tree" style="border: none;"> 
							<a href="#" style="text-decoration: none; border: none;">
								<img class="search-image" src="${servContext}/images/Icon_Tree_Folder.png">
								<span class="search-text">${object.name}</span>
							</a>
						</td>
						<c:if test="${ icon != 'Requirement' }"><td>---</td></c:if>
						<c:if test="${ icon == 'Requirement' }">
						<td>${object.project.name }-</td>
						<td>${object.project.name }-</td>
						</c:if>
						<td>
							${object.project.name }-${object.name }
						</td>
					
					</tr>	
				</c:otherwise>
			</c:choose>
			
				
		</c:forEach>
	</tbody>
	</table>
</div>

<c:if test="${ icon == 'Requirement' }">
	<comp:decorate-ajax-search-table tableId="search-result-datatable" >
		<jsp:attribute name="initialSort">[[4,'asc']]</jsp:attribute>
		<jsp:attribute name="columnDefs">
			<dt:column-definition targets="0" sortable="false" visible="false" />
			<dt:column-definition targets="1" sortable="false" />
			<dt:column-definition targets="2, 3" sortable="true" visible="false" />
			<dt:column-definition targets="4" sortable="true" visible="false" lastDef="true"/>
		</jsp:attribute>
	</comp:decorate-ajax-search-table>
</c:if>
<c:if test="${ icon != 'Requirement' }">
	<comp:decorate-ajax-search-table tableId="search-result-datatable" >
		<jsp:attribute name="initialSort">[[2,'asc']]</jsp:attribute>
		<jsp:attribute name="columnDefs">
			<dt:column-definition targets="0" sortable="false" visible="false" />
			<dt:column-definition targets="1" sortable="false" />
			<dt:column-definition targets="2" sortable="false" />
			<dt:column-definition targets="3" sortable="false" visible="false" lastDef="true" />
		</jsp:attribute>
	</comp:decorate-ajax-search-table>
</c:if>