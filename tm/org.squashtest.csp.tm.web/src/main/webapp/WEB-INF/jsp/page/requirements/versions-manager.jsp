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
<%@ taglib tagdir="/WEB-INF/tags" prefix="sq" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="gr" tagdir="/WEB-INF/tags/aggregates"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib prefix="cmp" tagdir="/WEB-INF/tags/component" %>

<c:set var="displayedVersions" value="10" />
<layout:common-import-outer-frame-layout highlightedWorkspace="requirement" titleKey="squashtm.library.requirement.title">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />
		<%-- css override is needed in case of a sub page. --%>
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/structure.override.css" />
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/structure.subpageoverride.css" />
		<script type="text/javascript">
			$(function() {
				/* versions table decoration */
				var getRowId = function(data) {
					return data[0];
				};
				
				var table = $( "#versions-table" ); 
				
				table.dataTable({
					"oLanguage": {
						"sUrl": "<c:url value='/datatables/messages' />"
					},
					"bJQueryUI": true,
					"bAutoWidth": false,
					"bFilter": false,
					"bPaginate": true,
					"sPaginationType": "squash",
					"iDisplayLength": ${ displayedVersions },
					"bProcessing": true,
					"bServerSide": true,
					"sAjaxSource": "<c:url value='/requirements/${ requirement.id }/versions/table' />", 
					"bRetrieve": true,				
					"sDom": 't<"dataTables_footer"lirp>',
 					"iDeferLoading": ${ fn:length(versions) },
			        "aaSorting": [[ 1, "desc" ]],
			        "fnDrawCallback": function() { restoreTableSelection(this, getRowId); },
					"aoColumnDefs": [ 
						{ "bVisible": false, "aTargets": [0, 5] },
						{ "bSortable": true, "aTargets": [1], "sClass": "select-handle centered", "sWidth": "6em" }, 
						{ "bSortable": true, "aTargets": [2,3,4] } 
					] 
				});
				
				var showSelectedVersion = function(table) {
					var rows = table.fnGetNodes();
					var id;

					$( rows ).each(function(index, row) {
						if ($( row ).hasClass( 'ui-state-row-selected' )) {
							var data = table.fnGetData( row );
							id = getRowId( data );
							return false; // breaks the iteration
						}
					});

					var urlPattern = "<c:url value='/requirement-versions/selectedVersionId/editor-fragment' />";
					
					$( "#contextual-content" ).load(urlPattern.replace("selectedVersionId", id));
				}
				
				$(".select-handle", table).live('click', function() {
					var row = $( this.parentNode );
					
					if (!row.hasClass('ui-state-row-selected')) {
						row.addClass('ui-state-row-selected').removeClass('ui-state-highlight');
						row.parent().find('.ui-state-row-selected').not(row).removeClass( 'ui-state-row-selected');
						saveTableSelection(table.dataTable(), getRowId);
						showSelectedVersion(table);					
					}
				});
				
				/* refreshes table on ajax success */
				table.ajaxSuccess(function(event, xrh, settings) {
					if (settings.type == 'POST' && settings.url.match(/requirement-versions\/\d+$/g)) {
						var dataTable = $( this ).dataTable();
						saveTableSelection(dataTable, getRowId);
						dataTable.fnDraw(false);
					}
				});
			});
		</script>
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="squashtm.library.requirement.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="content">
		<div id="sub-page" class="sub-page" >
			<div id="sub-page-header" class="sub-page-header shadow ui-corner-all">
			
				<div id="sub-page-title" class="sub-page-title">
					<h2><f:message key="requirement-versions.manager.title" /></h2>
				</div>
				
				<div id="sub-page-buttons" class="sub-page-buttons">
					<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
					<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
				</div>
				
				<div class="unsnap"></div>
			</div>
			
			<div id="sub-page-list-panel" class="sub-page-list-panel shadow ui-corner-all ui-helper-reset ui-widget ui-widget-content">
				<table id="versions-table">
					<thead>
						<th>Id</th>
						<th><f:message key="requirement.versions.table.col-header.version-number" /></th>
						<th><f:message key="requirement.versions.table.col-header.reference" /></th>
						<th><f:message key="requirement.versions.table.col-header.name" /></th>
						<th><f:message key="requirement.versions.table.col-header.status" /></th>
						<th>Id</th>
					</thead>
					<tbody>
						<c:forEach var="version" items="${ versions }" end="${ displayedVersions - 1 }">
							<c:choose>
								<c:when test="${ version.id eq selectedVersion.id }">
									<c:set var="rowClass" value="ui-state-row-selected" />
								</c:when>
								<c:otherwise>
									<c:set var="rowClass" value="" />
								</c:otherwise>
							</c:choose>
							<tr class="${ rowClass }">
								<td class="select-handle centered">${ version.id }</td>
								<td>${ version.versionNumber }</td>
								<td>${ version.reference }</td>
								<td>${ version.name }</td>
								<td><cmp:level-message level="${ version.status }" /></td>
								<td>&nbsp;</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>	
			
			<div id="sub-page-selection-panel" class="sub-page-selection-panel shadow ui-corner-all ui-component">
				<div id="contextual-content">
					<gr:requirement-version-editor requirementVersion="${ selectedVersion }" jsonCriticalities="${ jsonCriticalities }" />
				</div>
			</div>	
		</div>
	
	</jsp:attribute>
</layout:common-import-outer-frame-layout>









