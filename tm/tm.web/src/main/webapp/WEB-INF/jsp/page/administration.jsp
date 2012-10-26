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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<c:url var="usersUrl" value="/administration/users/list"/>
<c:url var="projectsUrl" value="/administration/projects"/>
<c:url var="bugtrackerUrl" value="/administration/bugtrackers"/>
<c:url var="loginUrl" value="/configuration/login-message"/>
<c:url var="welcomeUrl" value="/configuration/welcome-message"/>
<c:url var="customFieldsUrl" value="administration/custom-fields"/>

<layout:info-page-layout titleKey="workspace.home.title">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />	
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.home.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">	
		<div id="admin-link-pane">
			<table>
			<sec:authorize access=" hasRole('ROLE_ADMIN')">
				<tr><td><a href="${ usersUrl }"><b><f:message key="label.userManagement" /></b></a></td></tr>
				</sec:authorize>
				<tr><td><a href="${ projectsUrl }"><b><f:message key="label.projectManagement" /></b></a></td></tr>
				<sec:authorize access=" hasRole('ROLE_ADMIN')">
				<tr><td><a href="${ bugtrackerUrl }"><b><f:message key="label.bugtrackerManagement" /></b></a></td></tr>
				<tr><td><a href="${ customFieldsUrl }"><b><f:message key="label.customFieldsManagement" /></b></a></td></tr>
				<tr><td><a href="${ loginUrl }"><b><f:message key="label.consultModifyLoginMessage" /></b></a></td></tr>
				<tr><td><a href="${ welcomeUrl }"><b><f:message key="label.consultModifyWelcomeMessage" /></b></a></td></tr>
				</sec:authorize>
			</table>
		</div>	
	</jsp:attribute>
</layout:info-page-layout>