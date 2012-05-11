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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
	<comp:decorate-ajax-search-table tableId="user-permission-table" >
		<jsp:attribute name="rowCallback">addDeleteButtonCallBack</jsp:attribute>
		<jsp:attribute name="drawCallback">refreshTableCallBack</jsp:attribute>
		<jsp:attribute name="columnDefs">
			<dt:column-definition targets="0" sortable="false" visible="false" />
			<dt:column-definition targets="1, 2" sortable="true" />
			<dt:column-definition targets="3" sortable="true" width="2em" lastDef="true"/>
		</jsp:attribute>
	</comp:decorate-ajax-search-table>
	<table id="user-permission-table">
		<thead>
			<tr>
				<th> Id </th>
				<th> <f:message key="user.header.title" /></th>
				<th> <f:message key="project.permission.table.profile.label" /> </th>
				<th></th>
			</tr>
		</thead>
		<tbody>
			<%-- faire la condition si permissionProjectList est mull --%>
			<c:forEach items="${userPermissionList}" var="permissionUser">
				<tr id="${permissionUser.user.id}">
					<td id="id">${permissionUser.user.id}</td>
					<td>
						<span title="${permissionUser.user.firstName}&nbsp;${permissionUser.user.lastName}">${permissionUser.user.login}</span>
					</td>
					<td>
						<select class="select-class">
							<c:forEach items="${permissionList}" var="permission">
								<option value="${permission.qualifiedName}"
									id="${permission.simpleName}"  
									<c:if test="${permissionUser.permissionGroup.simpleName == permission.simpleName}">
										selected="selected"
									</c:if>
									>
									<f:message
										key="user.project-rights.${permission.simpleName}.label" />
								</option>
							</c:forEach>
						</select>
					</td>
					<td>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>