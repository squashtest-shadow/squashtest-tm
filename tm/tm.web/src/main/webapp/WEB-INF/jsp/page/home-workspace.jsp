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
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<layout:info-page-layout titleKey="workspace.home.title" highlightedWorkspace="home" main="home-workspace">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.blue.css" />	
		<comp:sq-css name="squash.core.override.css" />	
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.home.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">	
		<div id="welcome-message" class="ui-widget ui-widget-content ui-corner-all" 
					style="margin: auto; margin-top: 50px; padding-left: 0.5em; padding-right: 0.5em; width: 80%">
					<p>${ welcomeMessage }</p>
		</div>
	</jsp:attribute>
</layout:info-page-layout>