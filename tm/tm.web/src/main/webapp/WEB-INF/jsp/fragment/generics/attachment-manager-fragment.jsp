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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>

<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>


<div id="attachment-manager-header" class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float: left; height: 100%;">
	<h2><span><f:message key="label.CurrentAttachments"/>&nbsp;:&nbsp;</span></h2>
	</div>	
	<div style="float: right;">
		<f:message var="back" key="label.Back" /> 
		<input id="back" type="button" value="${ back }" class="button" onClick="history.back();"/>
	</div>
	<div style="clear: both;"></div>
	
</div>


<div class="fragment-body">

	<div id="test-case-toolbar" class="toolbar-class ui-corner-all">
		<div class="toolbar-information-panel"></div>
		<div class="toolbar-button-panel">
			<f:message var="uploadAttachment" key="label.UploadAttachment" />
			<input id="add-attachment-button" type="button" value="${uploadAttachment}" class="button"/>
		</div>
		<div style="clear: both;"></div>
	</div>
	
	<%---------------------------------Attachments table ------------------------------------------------%>
	
	
	<comp:toggle-panel id="attachment-table-panel" titleKey="label.CurrentAttachments"  open="true" >
		<jsp:attribute name="panelButtons">	
			<f:message var="renameAttachment" key="label.Rename" />
			<input type="button" value="${renameAttachment}" id="rename-attachment-button" class="button" />
			<f:message var="removeAttachment" key="label.Delete" />
			<input type="button" value="${removeAttachment}" id="delete-attachment-button" class="button" />
		</jsp:attribute>
		<jsp:attribute name="body">
			<at:attachment-table editable="${true}" attachListId="${attachListId}" model="${attachmentsModel}"/>					
		</jsp:attribute>
	</comp:toggle-panel>

</div>

