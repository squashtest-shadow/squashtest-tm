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
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<?xml version="1.0" encoding="utf-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title><f:message key="page.authentication.title"/></title>
		<layout:common-head />		
        <script type="text/javascript">
        var require = require || {};
        require.baseUrl = "${pageContext.servletContext.contextPath}/scripts";
        	var squashtm = {};
        	squashtm.app = {
        		contextRoot : "${pageContext.servletContext.contextPath}",
            	notificationConf: {
          			infoTitle: "<f:message key='popup.title.info' />", 
          			errorTitle: "<f:message key='popup.title.error' />"
          		}
        	};
        </script>
        <script src="<c:url value='/scripts/require-min.js' />"></script>
        <script type="text/javascript">
          require([ "common" ], function() {
            require([ "jquery", "app/ws/squashtm.notification", "domReady", "jqueryui", "jquery.squash.squashbutton" ], function($, WTF, domReady) {
            	domReady(function() {
              		WTF.init(squashtm.app.notificationConf);
              		
    				$('auth-error').fadeIn('slow');
    				
					$('body').keydown(function(event){
						var e;
						if (event.which !="") { e = event.which; }
						else if (event.charCode != "") { e = event.charCode; }
						else if (event.keyCode != "") { e = event.keyCode; }
						
						if (e==13){
							$('#login-form-button-set input').click();
						}
					});
					
					$.squash.decorateButtons();
					$("#j_username").focus();
            	});
            });
          });
        </script>
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />
	</head>
	<body class="nav-up-layout">
		<div id="nav-bar" class="ui-helper-clearfix">
			<div class="snap-left">
				<span id="nav-up-version">Squash TM v${initParam["squashTMVersion"]} </span>			
			</div>
			<div class="snap-right">
				<div class="unstyled-notification-pane">
							<layout:_ajax-notifications  cssClass="snap-right"/>
				<img src="${ pageContext.servletContext.contextPath }/images/logo_squash30h.png" />
				</div>
			</div>
		</div>
		
		<div id="main-pane">
			<s:message var="dialogTitle" code="dialog.authentication.title" />
			<div id="login-fom" class="ui-dialog ui-widget ui-widget-content ui-corner-all"
				style="margin-left: 40%; margin-top: 5%; width: 20%; position: relative; height: auto;">
				<sf:form action="j_spring_security_check" method="POST">
					<div id="login-form-body" class="ui-dialog-content ui-widget-content">
						<table class="ui-widget">
							<tr>
								<td>
									<label for="j_username"><s:message code="dialog.authentication.login.label" /></label>
								</td>
								<td>
									<input id="j_username" name="j_username" size="20" maxlength="50" type="text" value="${ sessionScope.SPRING_SECURITY_LAST_USERNAME }" />
								</td>
							</tr>
							<tr>
								<td>
									<label for="j_password"><s:message code="dialog.authentication.password.label" /></label>
								</td>
								<td>
									<input id="j_password" name="j_password" size="20" maxlength="50" type="password" />
								</td>
							</tr>
							<tr>
								<td colspan="2">
									<c:if test="${ not empty sessionScope.SPRING_SECURITY_LAST_EXCEPTION }">
										<div id="auth-error" class="ui-state-error ui-corner-all">
											<span class="ui-icon ui-icon-info" style="float: left; margin-right: 0.3em;"></span><span><f:message key="page.authentication.error.message" /></span>
										</div>
									</c:if>
								</td>
							</tr>
						</table>
					</div>
				
					<div id="login-form-button-pane" class="ui-dialog-buttonpane  ui-widget-content ui-helper-clearfix">
						<div id="login-form-button-set" >
							<s:message var="submitLabel" code="dialog.authentication.button.submit.label" />
							<input type="submit" value="${ submitLabel }" class="button" />
						</div>
					</div>
				</sf:form>
			</div>
			
			<c:if test="${ not empty welcomeMessage }">
				<div id="welcome-message" class="ui-widget ui-widget-content ui-corner-all" 
					style="margin: auto; margin-top: 2%; padding-left: 0.5em; padding-right: 0.5em; width: 80%">
					<p>${ welcomeMessage }</p>
				</div>
			</c:if>
		</div>		
	</body>
</html>
