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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
    
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>


<f:message var="userAccountPasswordLabel" key="label.password" />
<c:url var="userAccountUrl" value="/user-account/update" />

<layout:info-page-layout titleKey="dialog.settings.account.title" highlightedWorkspace="home">
	<jsp:attribute name="head">	
		<comp:sq-css name="squash.grey.css" />
	</jsp:attribute>
  
	<jsp:attribute name="titlePane">
		<h2><f:message key="dialog.settings.account.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">	
	
	<script type="text/javascript">
			$(function(){
				require(["squash.basicwidgets"], function(basic){
					basic.init();
					$("#back").click(function(){
						history.back();
					});
				});
			});
			function changePasswordCallback(){
				<f:message var="passSuccess" key="user.account.changepass.success" />
				squashtm.notification.showInfo("${passSuccess}");
			}
	</script>
	<div id="user-login-div" class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	
		<div style="float: left; height: 3em">
			<h2>
				<label for="user-login-header"><f:message key="user.header.title" /></label>
				<c:out value="${ user.login }" escapeXml="true" />
			</h2>
		</div>
		<div style="float: right;"><f:message var="back" key="label.Back" /> 
			<input id="back" type="button" value="${ back }" class="button"/>
		</div>

		<div style="clear: both;"></div>
	
	</div>
	
	<div class="fragment-body">

		<comp:simple-jeditable  targetUrl="${userAccountUrl}" componentId="user-account-email" width="200" />
	
		<comp:toggle-panel id="basic-info-panel" titleKey="user.account.basicinfo.label" open="true" >
			<jsp:attribute name="body">
				<div class="display-table">
					<div class="user-account-unmodifiable-field display-table-row">
						<label><f:message key="label.Name"/></label>
						<div class="display-table-cell"><span>${user.firstName } ${user.lastName}</span></div>
					</div>
					<div class="display-table-row">
						<label ><f:message key="label.Email"/></label>
						<div class="display-table-cell"><span id="user-account-email">${user.email}</span></div>
					</div>
					<div class="display-table-row">
						<label ><f:message key="label.Group"/></label>
						<div class="display-table-cell"><span><f:message key="user.account.group.${user.group.qualifiedName}.label" /></span></div>
					</div>
        <c:if test="${ authenticationProvider.managedPassword }">
        <div class="display-table-row">
          <label for="managed-pwd"><f:message key="label.password"/></label>
          <div class="display-table-cell">
            <span id="managed-pwd"><f:message key="message.managedPassword" /></span>
          </div>
        </div>
        </c:if>
				</div>
				<br/>
        <c:if test="${ not authenticationProvider.managedPassword }">
				<input type="button" id="change-password-button" value="${ userAccountPasswordLabel }" class="button" />									
        </c:if>
			</jsp:attribute>
		</comp:toggle-panel>	
		<comp:toggle-panel id="project-permission-panel" titleKey="user.project-rights.title.label" open="true">
			<jsp:attribute name="body">
				<table id="project-permission-table">
				<thead>
					<tr>
						<th><f:message key="label.project" /></th>
						<th><f:message key="label.Permission" /></th>
					</tr>
				</thead>
				<tbody>
					<c:forEach var="projectPermission" items="${ projectPermissions }">
					<tr><td>${ projectPermission.project.name }</td><td><f:message key="user.project-rights.${projectPermission.permissionGroup.simpleName}.label" /></td></tr>
					</c:forEach>
				</tbody>
			</table>
			</jsp:attribute>
		</comp:toggle-panel>		
		
	</div>
    <c:if test="${ not authenticationProvider.managedPassword }">
	<comp:user-account-password-popup openerId="change-password-button" url="${userAccountUrl}" successCallback="changePasswordCallback"/>
    </c:if>
	</jsp:attribute>
</layout:info-page-layout>

<comp:decorate-ajax-search-table tableId="project-permission-table" >
	<jsp:attribute name="columnDefs">
			<dt:column-definition targets="0" sortable="true" visible="true" />
			<dt:column-definition targets="1" sortable="true" visible="true" lastDef="true" />
	</jsp:attribute>
</comp:decorate-ajax-search-table>		