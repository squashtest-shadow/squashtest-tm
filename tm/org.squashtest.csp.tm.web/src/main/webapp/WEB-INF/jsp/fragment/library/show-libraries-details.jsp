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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%---------------------------- Test Case Header ------------------------------%>

<div class="ui-widget-header ui-corner-all ui-state-default fragment-header" >

<h2><span><f:message key="library.header.title" />&nbsp;:&nbsp;</span><a id="library-name" href="#"><c:out
	value="${ library.project.name }" escapeXml="true" /></a></h2>

</div>

<div class="fragment-body">

<comp:toggle-panel id="library-description-panel" titleKey="label.Description" isContextual="true" open="true">

	<jsp:attribute name="body">
		<div id="library-description" >${ library.project.description }</div>
	</jsp:attribute>
</comp:toggle-panel> 
<comp:decorate-buttons />
</div>



