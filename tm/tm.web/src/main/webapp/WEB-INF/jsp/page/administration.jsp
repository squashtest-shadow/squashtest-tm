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
<c:url var="indexUrl" value="administration/indexes" />

<f:message var="dateFormat" key="squashtm.dateformat"/>

<layout:info-page-layout titleKey="label.administration">
	<jsp:attribute name="head">	
		<comp:sq-css name="squash.grey.css" />	
		<comp:sq-css name="squash.core.override.css" />
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2 class="admin">
			<f:message key="label.administration" />
		</h2>	
	</jsp:attribute>

	<jsp:attribute name="informationContent">
		<div id="admin-pane">
			<div id="admin-link-pane">
				<sec:authorize access=" hasRole('ROLE_ADMIN')">
					<a href="${ usersUrl }" class="unstyledLink">
						<span id="user-admin" class="admin-section-icon admin-user-icon"></span>
						<span class="admin-section-label"><f:message key="label.userManagement" /></span>
					</a>
				</sec:authorize>
				<a href="${ projectsUrl }" class="unstyledLink">
					<span id="project-admin" class="admin-section-icon admin-project-icon"></span>
					<span class="admin-section-label"><f:message key="label.projectManagement" /></span>
				</a>
				
<sec:authorize access=" hasRole('ROLE_ADMIN')">
				<a href="${ bugtrackerUrl }" class="unstyledLink">
					<span id="bug-tracker-admin" class="admin-section-icon admin-bugtracker-icon"></span>
					<span class="admin-section-label"><f:message key="label.bugtrackerManagement" /></span>
				</a>
				<a href="${ customFieldsUrl }" class="unstyledLink">
					<span id="custom-fields-admin" class="admin-section-icon admin-customfields-icon" ></span>
					<span class="admin-section-label"><f:message key="label.customFieldsManagement" /></span>
				</a>
				<a href="${ loginUrl }" class="unstyledLink ">
					<span id="login-message-admin" class="admin-section-icon admin-msglogin-icon"></span>
					<span class="admin-section-label"><f:message key="label.consultModifyLoginMessage" /></span>
				</a>
				<a href="${ welcomeUrl }" class="unstyledLink ">
					<span id="welcome-message-admin" class="admin-section-icon admin-msghome-icon"></span>
					<span class="admin-section-label"><f:message key="label.consultModifyWelcomeMessage" /></span>
				</a>
				<a href="" class="unstyledLink"></a>
				<a href="${ indexUrl }" class="unstyledLink">
					<span id="index-admin" class="admin-section-icon admin-index-icon"></span>
					<span class="admin-section-label"><f:message key="label.indexManagement" /></span>
				</a>
</sec:authorize>

			</div>
			
			<div id="admin-stats">
				<div class="admin-stats-table"><label ><f:message key="label.version"/></label><span >${initParam["projectVersion"]}</span></div>
				<div class="admin-stats-table"><label><f:message key="label.statistics"/></label>
					<div>
						<div><label><f:message key="label.projects"/></label><span>${adminStats.projectsNumber }</span></div>
						<div><label><f:message key="label.users"/></label><span>${adminStats.usersNumber }</span></div>
					</div>
					<div>
						<div><label><f:message key="label.requirements"/></label><span>${adminStats.requirementsNumber }</span></div>
						<div><label><f:message key="label.testCases"/></label><span>${adminStats.testCasesNumber }</span></div>
					</div>
					<div>
						<div><label><f:message key="label.campaigns"/></label><span>${adminStats.campaignsNumber }</span></div>
						<div><label><f:message key="label.iterations"/></label><span>${adminStats.iterationsNumber }</span></div>
						<div><label><f:message key="label.executions"/></label><span>${adminStats.executionsNumber }</span></div>
					</div>
				</div>
				<div class="admin-stats-table"><label><f:message key="label.lastIndexing"/></label>
					<div>
					<div><label><f:message key="label.requirements"/></label>
						<c:choose>
							<c:when test="${not empty adminStats.requirementIndexingDate}">
								<span id="last-modified-on">
									<f:formatDate value="${adminStats.requirementIndexingDate}" pattern="${dateFormat}" /> 
								</span>
							</c:when>
							<c:otherwise>
								<span>(<f:message key="label.lower.Never" />)</span>
							</c:otherwise>
						</c:choose>	
					</div>
					<div><label><f:message key="label.testCases"/></label>
						<c:choose>
							<c:when test="${not empty adminStats.testcaseIndexingDate}">
								<span id="last-modified-on">
									<f:formatDate value="${adminStats.testcaseIndexingDate}" pattern="${dateFormat}" /> 
								</span>
							</c:when>
							<c:otherwise>
								<span>(<f:message key="label.lower.Never" />)</span>
							</c:otherwise>
						</c:choose>	
					</div>
					<!-- <div><label><f:message key="label.campaigns"/></label>
						<c:choose>
							<c:when test="${not empty adminStats.campaignIndexingDate}">
								<span id="last-modified-on">
									<f:formatDate value="${adminStats.campaignIndexingDate}" pattern="${dateFormat}" /> 
								</span>
							</c:when>
							<c:otherwise>
								<span>(<f:message key="label.lower.Never" />)</span>
							</c:otherwise>
						</c:choose>	
					</div>-->
				</div>
				</div>
			</div>
		</div>
	</jsp:attribute>
</layout:info-page-layout>