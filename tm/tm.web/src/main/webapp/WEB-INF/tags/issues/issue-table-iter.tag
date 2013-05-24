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
<%@ tag description="Table displaying the issues for an Iteration" body-content="empty" %>
	
<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>	
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>

<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface"%>
<%@ attribute name="dataUrl" required="true" description="where the table will fetch its data" %>
<%@ attribute name="freeSettings" required="true" description="added settings to issue table" %>

<c:url var="tableLanguageUrl" value="/datatables/messages" />

<%-- 
	columns are :
	
		- URL  (not shown)
		- ID
		- owner
		- Priority
		- Summary
		- Status
		- Assignation

 --%>

	
<table id="issue-table" data-def="hover, datakeys-id=issue-id, ajaxsource=${dataUrl}, language=${tableLanguageUrl}, pre-sort=1-desc">
	<thead>
		<tr>
			<th data-def="select, map=issue-id, link={issue-url}, sWidth=2.5em, sortable">${interfaceDescriptor.tableIssueIDHeader}</th>
			<th data-def="map=issue-summary">${interfaceDescriptor.tableSummaryHeader}</th>
			<th data-def="map=issue-priority">${interfaceDescriptor.tablePriorityHeader}</th>
			<th data-def="map=issue-status">${interfaceDescriptor.tableStatusHeader}</th>
			<th data-def="map=issue-assignee">${interfaceDescriptor.tableAssigneeHeader}</th>
			<th data-def="map=issue-owner, link=${pageContext.servletContext.contextPath}{issue-owner-url}"><f:message key="iteration.issues.table.column-header.reportedin.label" /></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>

<script type="text/javascript">

	$(function(){
		

		var issueTableRowCallback = function(row, data, displayIndex) {
			var correctAssignee = (data["issue-assignee"]!=="") ? data["issue-assignee"] : "${interfaceDescriptor.tableNoAssigneeLabel}";
			var td=$(row).find("td:eq(4)");
			$(td).html(correctAssignee);
			return row;
		};
		
		require(["jquery.squash.datatables"], function(datatable){
			$("#issue-table").squashTable(
				{
					'fnRowCallback' : issueTableRowCallback,
					${freeSettings}
				},
				{}
			);
		});
	});
</script>

