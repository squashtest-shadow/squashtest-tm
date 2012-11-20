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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<layout:info-page-layout titleKey="squashtm.bugtrackers.title" isSubPaged="true">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.grey.css" />
		
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.bugtracker.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">
		<c:url var="bugtrackersUrl" value="/administration/bugtrackers/list" />
		<c:url var="addBugtrackerUrl" value="/administration/bugtrackers/add" />
		<c:url var="bugtrackerDetailsBaseUrl" value="/bugtracker" />
		
		<script type="text/javascript">
					$(function() {
							$('#new-bugtracker-button').button();
					});
					
					function refreshBugTrackers() {
						var table = $( '#bugtrackers-table' ).dataTable();
						table.fnDraw(false);
					}
													
					function tableDrawCallback() {
						addHoverHandler(this);
					}	
					
					function getBugtrackerTableRowId(rowData) {
						return rowData[0];	
					}
					function addHLinkToBugtrackerName(row, data) {
						var url= '${ bugtrackerDetailsBaseUrl }/' + getBugtrackerTableRowId(data) + '/info';			
						addHLinkToCellText($( 'td:eq(1)', row ), url);
					}	
					function bugtrackerTableRowCallback(row, data, displayIndex){
						addHLinkToBugtrackerName(row, data);
						return row;
					}
					
					function addHoverHandler(dataTable){
						$( 'tbody tr', dataTable ).hover(
							function() {
								$( this ).addClass( 'ui-state-highlight' );
							}, 
							function() {
								$( this ).removeClass( 'ui-state-highlight' );
							} 
						);
					}		
					
					
		</script>
		
		<%----------------------------------- BugTracker Table -----------------------------------------------%>
		
<!-- 
	table structure (columns):
	
		* id (not shown)
		* selecthandle
		* name,
		* kind,
		* url,
		* iframeFriendly

 -->

<div class="fragment-body">
				<input style="float: right;" type="button" value='<f:message key="label.AddBugtracker" />' id="new-bugtracker-button"/>
				<div style="clear:both"></div>
				<comp:decorate-ajax-table url="${ bugtrackersUrl }" tableId="bugtrackers-table" paginate="true">
					<jsp:attribute name="drawCallback">tableDrawCallback</jsp:attribute>
					<jsp:attribute name="initialSort">[[2,'asc']]</jsp:attribute>
					<jsp:attribute name="rowCallback">bugtrackerTableRowCallback</jsp:attribute>
					<jsp:attribute name="columnDefs">
						<dt:column-definition targets="0" visible="false" />
						<dt:column-definition targets="1" width="2em" cssClass="select-handle centered" sortable="false"/>
						<dt:column-definition targets="2, 3, 4" sortable="true"/>
						<dt:column-definition targets="5" sortable="false" lastDef="true"/>
					</jsp:attribute>
				</comp:decorate-ajax-table>
				
				<table id="bugtrackers-table">
					<thead>
						<tr>
							<th>Id(not shown)</th> 
							<th>#</th>
							<th><f:message key="label.Name" /></th>
							<th><f:message key="label.Kind" /></th>
							<th><f:message key="label.Url" /></th>
							<th><f:message key="label.lower.iframe" /></th>
						</tr>
					</thead>
					<tbody><%-- Will be populated through ajax --%></tbody>
				</table>


<comp:popup id="add-bugtracker-dialog" titleKey="dialog.new-bugtracker.title" openedBy="new-bugtracker-button">
	<jsp:attribute name="buttons">
		<f:message var="label1" key="label.Add" />
			'${ label1 }': function() {
					var url = "${ addBugtrackerUrl }";
					<jq:ajaxcall url="url"
					 dataType="json"
					 httpMethod="POST"
					 useData="true"
					 successHandler="refreshBugTrackers">
						{
						name: $( '#add-bugtracker-name' ).val(),
						url: $( '#add-bugtracker-url' ).val(),
						kind: $( '#add-bugtracker-kind' ).val(),
						iframeFriendly: $('#add-bugtracker-iframeFriendly').is(':checked')
						
						}

					</jq:ajaxcall>
				},							
		<pop:cancel-button />
	</jsp:attribute>
			<jsp:body>
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
			</jsp:body>
</comp:popup>

</div>
</jsp:attribute>
</layout:info-page-layout>