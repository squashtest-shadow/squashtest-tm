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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ attribute name="status"	description="execution status of the entity" %>

<f:message var="statusBloqued" key="execution.execution-status.BLOCKED" />
<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
<f:message var="statusReady" key="execution.execution-status.READY" />

<c:choose>
	<c:when test="${status == 'BLOCKED'}">
		<c:set var="entityStatus" value="${statusBloqued}" />
		<c:set var="statusIcon" value="${pageContext.servletContext.contextPath}/images/Icon_Yellow.png"/>
	</c:when>
	
	<c:when test="${status == 'FAILURE'}">
		<c:set var="entityStatus" value="${statusFailure}" />
		<c:set var="statusIcon" value="${pageContext.servletContext.contextPath}/images/Icon_Red.png"/>
	</c:when>
	
	<c:when test="${status == 'SUCCESS'}">
		<c:set var="entityStatus" value="${statusSuccess}" />
		<c:set var="statusIcon" value="${pageContext.servletContext.contextPath}/images/Icon_Green.png"/>
	</c:when>
	
	<c:when test="${status == 'RUNNING'}">
		<c:set var="entityStatus" value="${statusRunning}" />
		<c:set var="statusIcon" value="${pageContext.servletContext.contextPath}/images/Icon_Blue.png"/>
	</c:when>
	
	<c:when test="${status == 'READY'}">
		<c:set var="entityStatus" value="${statusReady}" />
		<c:set var="statusIcon" value="${pageContext.servletContext.contextPath}/images/Icon_Grey.png"/>
	</c:when>	

</c:choose>

<span style="white-space:nowrap; display:inline-block;" >
	<img src="${statusIcon}" style="margin-left:5px;vertical-align:bottom;" />
	<span style="margin-left:10px;">${entityStatus}</span>
</span>