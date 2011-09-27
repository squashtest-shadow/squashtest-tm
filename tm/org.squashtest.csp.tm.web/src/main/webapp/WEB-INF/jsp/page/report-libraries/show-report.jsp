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
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="sq"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<layout:info-page-layout titleKey="squashtm.library.requirement.title" highlightedWorkspace="report">
	<jsp:attribute name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />
	</jsp:attribute>
	<jsp:attribute name="titlePane">
		<h2><f:message key="squashtm.library.test-case.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">	
		<jsp:include page="/WEB-INF/jsp/fragment/reports/edit-report.jsp" >
			<jsp:param name="hasBackButton" value="true" />
		</jsp:include>
	</jsp:attribute>

</layout:info-page-layout>
