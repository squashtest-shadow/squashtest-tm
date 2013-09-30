<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

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
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="reportType" type="java.lang.Object"  required="true" description="get the path to the image for a given
																						ReportType" %>
																						
<c:choose>
	<c:when test="${reportType.resourceKeyName =='squashtest.report.reporttype.progressfollowup.name'}">
		<c:set var="imgPath" value="${ pageContext.servletContext.contextPath }/images/report_spreadsheet.png" />
	</c:when>
</c:choose>

<img src="${imgPath}" />