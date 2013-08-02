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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>



<s:url var="statsUrl" value="/test-case-browser/statistics">
	<s:param name="libraries" value="${library.id}" />
	<s:param name="nodes" value=""/>
</s:url>

<%---------------------------- Test Case Header ------------------------------%>

<c:if test="${empty editable}">
	<c:set var="editable" value="${ false }" /> 
	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="SMALL_EDIT" domainObject="${ library }">
		<c:set var="editable" value="${ true }" /> 
	</authz:authorized>
</c:if>

<div class="ui-widget-header ui-corner-all ui-state-default fragment-header" >

<h2><span><f:message key="library.header.title" />&nbsp;:&nbsp;</span><a id="library-name" href="#"><c:out
	value="${ library.project.name }" escapeXml="true" /></a></h2>
</div>

<div class="fragment-body">

	<%-- statistics panel --%>	
	<dashboard:test-case-dashboard-panel listenTree="${false}" url="${statsUrl}"/>
	
	<%-- description panel --%>
	<comp:toggle-panel id="library-description-panel" titleKey="label.Description" isContextual="true" open="true">
		<jsp:attribute name="body">
			<div id="library-description" >${ library.project.description }</div>
		</jsp:attribute>
	</comp:toggle-panel> 
	
	<at:attachment-bloc editable="${ editable }" workspaceName="${ workspaceName }" attachListId="${ library.attachmentList.id}" attachmentSet="${attachments}"/>

	<comp:decorate-buttons />

</div>

<script type="text/javascript">
	$(function(){
		require(["dashboard"], function(dashboard){
			dashboard.init({
				master : '#dashboard-master',
				model : ${json:serialize(statistics)}
			});			
		});	
	});
</script>



