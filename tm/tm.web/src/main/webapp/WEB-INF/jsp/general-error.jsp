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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
	<layout:common-head />
</head>

<body>

	<layout:navigation />

	<div id="workspace">

		<h1 /><fmt:message key="internalError"/></h1> 

		<p><fmt:message key="failedRequest"/> ${pageContext.errorData.requestURI}</p>
		<p><fmt:message key="statusCode"/> ${pageContext.errorData.statusCode}</p>
		<p><fmt:message key="exception"/> ${pageContext.errorData.throwable}</p>

		<c:set var="message">${pageContext.exception.message}</c:set>
		<c:if test="${message != null && fn:length(message)>0}">
			<p><fmt:message key="message"/> ${message}</p>
		</c:if>

		<c:set var="stacktrace" value="${pageContext.exception.stackTrace}"></c:set>
		<c:if test="${stacktrace != null && fn:length(stacktrace)>0}">
			<p><fmt:message key="stacktrace"/>:<br/>
			<c:forEach var="st" items="${stacktrace}">
     			${st}
     			<br />
			</c:forEach>
			</p>
		</c:if>

	</div>
	
	<squash:footer />
	
</body>

</html>