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
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib uri="http://www.springframework.org/security/tags"
	prefix="sec"%>

<c:url var="usersUrl" value="/administration/users/list" />
<c:url var="projectsUrl" value="/administration/projects" />
<c:url var="bugtrackerUrl" value="/administration/bugtrackers" />
<c:url var="loginUrl" value="/configuration/login-message" />
<c:url var="welcomeUrl" value="/configuration/welcome-message" />
<c:url var="customFieldsUrl" value="administration/custom-fields" />

<layout:info-page-layout titleKey="workspace.home.title">
	<jsp:attribute name="head">	
		<link rel="stylesheet" type="text/css"
			href="${ pageContext.servletContext.contextPath }/styles/master.grey.css" />	
			<link rel="stylesheet" type="text/css"
			href="${ pageContext.servletContext.contextPath }/styles/structure.override.css" />
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2>
			<f:message key="workspace.home.title" />
		</h2>	
	</jsp:attribute>

	<jsp:attribute name="informationContent">
		<div id="admin-pane">
			<div id="admin-link-pane">
				<sec:authorize access=" hasRole('ROLE_ADMIN')">
					<a href="${ usersUrl }" class="unstyledLink"><img id="user-admin"
						src="${ pageContext.servletContext.contextPath }/images/Button_User.png" /><br/><span><f:message
								key="label.userManagement" /></span></a>
				</sec:authorize>
				<a href="${ projectsUrl }" class="unstyledLink"><img id="project-admin"
				src="${ pageContext.servletContext.contextPath }/images/Button_Project.png" /><br/><span><f:message
						key="label.projectManagement" /></span></a>
				<sec:authorize access=" hasRole('ROLE_ADMIN')">
					<a href="${ bugtrackerUrl }" class="unstyledLink"><img id="bug-tracker-admin"
						src="${ pageContext.servletContext.contextPath }/images/Button_Bugtracker.png" /><br/><span><f:message
								key="label.bugtrackerManagement" /></span></a>
					<a href="${ customFieldsUrl }" class="unstyledLink"><img id="custom-fields-admin"
						src="${ pageContext.servletContext.contextPath }/images/Button_CUF.png" /><br/><span><f:message
								key="label.customFieldsManagement" /></span></a>
				</sec:authorize>
			</div>
			<div id="admin-link-msg-pane" class="display-table">
				<sec:authorize access=" hasRole('ROLE_ADMIN')">
					<a href="${ loginUrl }" class="unstyledLink display-table-row"><img id="login-message-admin"
						src="${ pageContext.servletContext.contextPath }/images/Button_MsgLogin.png" class="display-table-cell" /><span class="display-table-cell"><f:message
								key="label.consultModifyLoginMessage" /></span></a>
					<a href="${ welcomeUrl }" class="unstyledLink display-table-row"><img id="welcome-message-admin"
						src="${ pageContext.servletContext.contextPath }/images/Button_MsgHome.png" class="display-table-cell"/><span class="display-table-cell"><f:message
								key="label.consultModifyWelcomeMessage" /></span></a>
				</sec:authorize>
			</div>
		</div>
	</jsp:attribute>
</layout:info-page-layout>