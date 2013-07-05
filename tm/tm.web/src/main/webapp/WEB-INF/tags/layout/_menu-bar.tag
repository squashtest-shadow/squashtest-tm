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
<%@ tag description="the main menu bar, the one displayed on the top right" %>


<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
 
 <%-- 
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squash/jquery.squashtm.projectpicker.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squash/squashtm.menubar.js"></script>
--%>

<c:url var="projectFilterStatusUrl" value="/global-filter/filter-status"/>
<c:url var="administrationUrl" value="/administration"/>
<c:url var="userAccountUrl" value="/user-account" />

<div>
	<input type="checkbox" id="menu-toggle-filter-ckbox"></input>
	<a id="menu-project-filter-link" href="#" ></a> 
</div>
<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">
<div><a id="menu-administration-link" href="${ administrationUrl }" ><f:message key="workspace.menubar.administration.label"/></a></div>
</sec:authorize>

<div>
<a id="menu-account-link" href="${userAccountUrl}">
<f:message key="workspace.menubar.account.label"/>&nbsp;(<c:out value="${sessionScope['SPRING_SECURITY_CONTEXT'].authentication.name}"/>)
</a>
</div>

<sec:authorize access="isAuthenticated()">
	<c:url var="logoutUrl" value="/logout" />
</sec:authorize>
<div><a id="menu-logout-link" href="${ logoutUrl }" ><f:message key="workspace.menubar.logout.label"/></a></div>


<comp:settings-project-filter-popup divId="project-filter-popup" openedBy="menu-project-filter-link"/>