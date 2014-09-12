<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>

<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<c:url value="${dataAssignUrl}" var="url" />

<select id="${selectIdentifier}" class="${selectClass}"
	data-assign-url="${url}" onchange="changeUserLogin(this)">
	<option value="0" class="not-affected">
		<f:message key="label.Unassigned" />
	</option>

	<c:forEach var="user" items="${usersList}">
		<c:choose>
			<c:when test="${testCaseAssignedLogin == user.login}">
				<option value="${user.id}" selected="selected">${user.login}</option>
			</c:when>
			<c:otherwise>
				<option value="${user.id}">${user.login}</option>
			</c:otherwise>
		</c:choose>

	</c:forEach>
</select>
<script>

	function changeUserLogin(cbox){
		<c:if test="${ not empty dataAssignUrl }">
			var jqBox = $(cbox);
			var url = jqBox.attr('data-assign-url');
			$.ajax({
					  type: 'POST',
					  url: url,
					  data: "userId="+jqBox.val(),
					  dataType: 'json'
			});
		</c:if>
	}

</script>