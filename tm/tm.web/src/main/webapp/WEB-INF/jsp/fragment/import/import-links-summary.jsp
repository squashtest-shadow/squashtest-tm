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
<!-- The form.ajaxSubmit() fakes to ask for json while it needs text/html. Chrome and FF handle well a repsonse in json but not IE. -->
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<p>${'{' } "success" : ${ summary.success }, 
"total" : ${ summary.total }, 
"failures" : ${ summary.failures },
"criticalErrors" : ${summary.criticalErrors},

"obsolete" : "<c:forEach var="o" items="${summary.obsoletes }">${o},</c:forEach>",

"requirementAccessRejected" : "<c:forEach var="rar" items="${summary.requirementAccessRejected }">${rar}, </c:forEach>",

"requirementNotFound" : "<c:forEach var="rnf" items="${summary.requirementNotFound }">${rnf}, </c:forEach>",

"testCaseAccessRejected" : "<c:forEach var="tcar" items="${summary.testCaseAccessRejected }">${tcar}, </c:forEach>",

"testCaseNotFound" : "<c:forEach var="tcnf" items="${summary.testCaseNotFound }">${tcnf}, </c:forEach>",

"versionNotFound" : "<c:forEach var="vnf" items="${summary.versionNotFound }">${vnf}, </c:forEach>",

"linkAlreadyExist" : "<c:forEach var="lae" items="${summary.linkAlreadyExist }">${lae}, </c:forEach>",

"missingColumnHeaders" : "<c:forEach var="mch" items="${summary.missingColumnHeaders }">${mch}, </c:forEach>"

${'}'}</p>