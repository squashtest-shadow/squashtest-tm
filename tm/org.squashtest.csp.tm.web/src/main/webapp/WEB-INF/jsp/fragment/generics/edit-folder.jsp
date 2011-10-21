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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<s:url var="folderUrl" value="/${ updateUrl }/{folderId}">
	<s:param name="folderId" value="${folder.id}" />
</s:url>


<c:if test="${empty editable}">
	<c:set var="editable" value="${ false }" /> 
	<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ folder }">
		<c:set var="editable" value="${ true }" /> 
	</authz:authorized>
</c:if>

<script type="text/javascript">
	function nodeSetname(name){
		$('#folder-name').html(name);	
	}
</script>


<div class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<h2>
		<span><f:message key="folder.title" />&nbsp;:&nbsp;</span><span id="folder-name"><c:out value="${ folder.name }" escapeXml="true"/></span>
	</h2>
</div>
<c:if test="${ editable }">
<comp:rich-jeditable
	targetUrl="${ folderUrl }" componentId="folder-description" />
</c:if>
<div class="fragment-body">
	<comp:toggle-panel id="folder-description-panel" titleKey="folder.description.label" isContextual="true" open="true">
		<jsp:attribute name="body">
			<div id="folder-description">${ folder.description }</div>
		</jsp:attribute>
	</comp:toggle-panel>
</div>

