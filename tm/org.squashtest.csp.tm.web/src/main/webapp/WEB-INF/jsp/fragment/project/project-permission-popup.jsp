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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<table>
	<tr>
	<td> 
		<span> <f:message key="user.login.label" /></span>
	</td>
	<td> 
	<input id="user-input"/> 
	</td>
	</tr>
	
	<tr>
	<td> 
		<span> <f:message key="dialog.add-permission.permission.title" /> </span>
	</td>
	<td> 
	<select id="permission-input"> 
		<c:forEach items="${permissionList}" var="permission">
			<option value="${permission.qualifiedName}" id="${permission.simpleName}"><f:message
								key="user.project-rights.${permission.simpleName}.label" /></option>
		</c:forEach>
	</select>
	</td>
	</tr>
</table>
<script>
$(function() {
	var availableTags = [""
		<c:forEach items="${userList}" var="user">
			,"${user.login}"
		</c:forEach>
		];
	console.log(availableTags);
	$( "#user-input" ).autocomplete({
		source: availableTags
	});
});
</script>