<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<s:url var="administrationUrl" value="/administration" />

<layout:info-page-layout titleKey="squashtm.bugtrackers.title" isSubPaged="true">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.grey.css" />
		
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.bugtracker.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href= '${administrationUrl}'"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">
		<c:url var="bugtrackersUrl" value="/administration/bugtrackers/list" />
		<c:url var="addBugtrackerUrl" value="/administration/bugtrackers/add" />
		<c:url var="bugtrackerDetailsBaseUrl" value="/bugtracker" />
		<c:url var="dtMessagesUrl" value="/datatables/messages" />

		
		<%----------------------------------- BugTracker Table -----------------------------------------------%>


<div class="fragment-body">
	<input class="snap-right" type="button" value='<f:message key="label.AddBugtracker" />' id="new-bugtracker-button"/>
	<div style="clear:both"></div>
	
	<table id="bugtrackers-table" class="unstyled-table" data-def="ajaxsource=${bugtrackersUrl}, hover, pre-sort=1-asc">
		<thead>
			<tr>
				<th data-def="map=index, select">#</th>
				<th data-def="map=name, sortable, link=${bugtrackerDetailsBaseUrl}/{id}/info"><f:message key="label.Name" /></th>
				<th data-def="map=kind, sortable"><f:message key="label.Kind" /></th>
				<th data-def="map=url, sortable, link={url}"><f:message key="label.Url" /></th>
				<th data-def="map=iframe-friendly"><f:message key="label.lower.iframe" /></th>
			</tr>
		</thead>
		<tbody><%-- Will be populated through ajax --%></tbody>
	</table>


<pop:popup id="add-bugtracker-dialog" titleKey="dialog.new-bugtracker.title" openedBy="new-bugtracker-button">
	<jsp:attribute name="buttons">
		<f:message var="label1" key="label.Add" />
			'${ label1 }': function() {
					$.ajax({
						url : "${addBugtrackerUrl}",
						type : 'POST', 
						dataType : 'json',
						data : {
							name: $( '#add-bugtracker-name' ).val(),
							url: $( '#add-bugtracker-url' ).val(),
							kind: $( '#add-bugtracker-kind' ).val(),
							iframeFriendly: $('#add-bugtracker-iframeFriendly').is(':checked')							
						}
					}).done(function(){
						$('#bugtrackers-table').squashTable().refresh();
					});
				},							
		<pop:cancel-button />
	</jsp:attribute>
			<jsp:attribute name="body">
				<table>
					<tr>
						<td><label for="add-bugtracker-name"><f:message
							key="label.Name" /></label></td>
						<td><input id="add-bugtracker-name" type="text" size="50" />
						<comp:error-message forField="name" /></td>
					</tr>
					<tr>
						<td><label for="add-bugtracker-kind"><f:message
							key="label.Kind" /></label></td>
						<td><select id="add-bugtracker-kind" class="combobox">
						<c:forEach items="${ bugtrackerKinds }" var="kind" > 
						<option value = "${kind}" >${kind}</option>
						</c:forEach>
						</select>
						
						<comp:error-message forField="kind" /></td>
					</tr>
					<tr>
						<td><label for="add-bugtracker-url"><f:message
							key="dialog.new-bugtracker.url.label" /></label></td>
						<td><input id="add-bugtracker-url" type="text" size="50"/>
						<comp:error-message forField="url" /></td>
					</tr>
					<tr>
						<td><label for="add-bugtracker-iframeFriendly"><f:message
							key="label.DisplaysInIframe" /></label></td>
						<td><input id="add-bugtracker-iframeFriendly" type="checkbox" />
						<comp:error-message forField="iframeFriendly" /></td>
					</tr>
				</table>
			</jsp:attribute>
</pop:popup>

		
	<script type="text/javascript">
require(["common"], function() {
	require(["jquery", "squashtable"], function($){					
		$(function() {		
			$('#new-bugtracker-button').button();				
			$("#bugtrackers-table").squashTable({},{});						
		});				
	});				
});				
	</script>
</div>
</jsp:attribute>
</layout:info-page-layout>