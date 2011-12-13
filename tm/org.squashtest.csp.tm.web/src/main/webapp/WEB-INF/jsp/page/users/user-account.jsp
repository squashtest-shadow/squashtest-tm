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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
    
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>


<f:message var="userAccountPasswordLabel" key='user.account.password.label'/>
<c:url var="userAccountUrl" value="/user-account/update" />

<layout:info-page-layout titleKey="dialog.settings.account.title" highlightedWorkspace="home">
	<jsp:attribute name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.green.css" />
		

	
	</jsp:attribute>
	<jsp:attribute name="titlePane">
		<h2><f:message key="dialog.settings.account.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">	
		<comp:decorate-buttons />
	
		<script type="text/javascript">

			$(function(){

				$("#back").button().click(function(){
					history.back();
				});

			});
			
			
			function changePasswordCallback(){
				<f:message var="passSuccess" key="user.account.changepass.success" />
				displayInformationNotification("${passSuccess}");
			}
				
		</script>
		

	
		<div id="user-login-div" class="ui-widget-header ui-corner-all ui-state-default fragment-header">
		
			<div style="float: left; height: 3em">
				<h2>
					<label for="user-login-header"><f:message key="user.header.title" /></label>
					<c:out value="${ user.login }" escapeXml="true" />
				</h2>
			</div>
			<div style="float: right;"><f:message var="back" key="fragment.edit.header.button.back" /> 
				<input id="back" type="button" value="${ back }" class="button"/>
			</div>
	
			<div style="clear: both;"></div>
		
		</div>
	
		<div class="fragment-body">

			<comp:simple-jeditable  targetUrl="${userAccountUrl}" componentId="user-account-email" />
	
			<comp:toggle-panel id="basic-info-panel" titleKey="user.account.basicinfo.label" open="true">
			
				<jsp:attribute name="body">
					
					<div class="user-account-unmodifiable-field">
						<label><f:message key="user.account.lastname.label"/></label>
						<span>${user.firstName } ${user.lastName}</span>
					</div>					
					
					<style type="text/css">
						#user-account-email input{
							width:200px ! important;
						}					
					</style>
					
					<div>
						<label ><f:message key="user.account.email.label"/></label>
						<span id="user-account-email">${user.email}</span>
					</div>
					
					<input type="button" id="change-password-button" value="${ userAccountPasswordLabel }" class="button" />									
													
				</jsp:attribute>
			</comp:toggle-panel>			
			
		</div>
		
	<comp:user-account-password-popup openerId="change-password-button" url="${userAccountUrl}" successCallback="changePasswordCallback"/>
		
				
	</jsp:attribute>

</layout:info-page-layout>