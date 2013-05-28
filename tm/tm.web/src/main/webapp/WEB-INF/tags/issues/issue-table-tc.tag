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
<%@ tag description="Table displaying the issues for a TestCase" body-content="empty" %>
	
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

<c:url var="executionUrl" value="/executions/"/>
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

<script type="text/javascript">
	
	function getIssueTableRowUrl(rowData){
		return rowData[0];
	}

	function getIssueTableRowId(rowData) {
		return rowData[1];
	}
	
	function getIssueTableRowAssignee(rowData){
		return rowData[6];
	}
	
	function getIssueTableRowExecId(rowData){
		return rowData[7];
	}

	function issueTableRowCallback(row, data, displayIndex) {
		addHLinkToIdRow(row,data);
		addHLinkToExecInfo(row, data);
		checkEmptyValues(row, data);
		return row;
	}
	
	
	function addHLinkToIdRow(row, data){
		var td = $(row).find("td:eq(0)");
		var url = getIssueTableRowUrl(data);
		addHLinkToCellText(td, url, true);
	}
	
	function addHLinkToExecInfo(row, data){
		var td = $(row).find("td:eq(5)");
		var execLabel = td.find(".issueExecLink");
		var url = "${executionUrl}"+getIssueTableRowExecId(data);
		addHLinkToCellText(execLabel, url, true);
	}

	
	<%-- we check the assignee only (for now) --%>
	function checkEmptyValues(row, data){
		var correctAssignee = handleEmptyValue(data, getIssueTableRowAssignee, "${interfaceDescriptor.tableNoAssigneeLabel}");
		var td=$(row).find("td:eq(6)");
		$(td).html(correctAssignee);
	}
	
	<%-- that method will take care of empty values if need be --%>
	function handleEmptyValue(data, fnGetData, strDefaultMessage){
		var value = fnGetData(data);
		if (value==""){
			return strDefaultMessage;
		}
		else{
			return value;
		}
	} 


	
</script>


<comp:decorate-ajax-table url="${dataUrl}" tableId="issue-table" paginate="true" >
	<jsp:attribute name="initialSort">[[1,'desc']]</jsp:attribute>
	<jsp:attribute name="rowCallback">issueTableRowCallback</jsp:attribute>
	<jsp:attribute name="freeSettings">${ freeSettings }</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" sortable="false" />
		<dt:column-definition targets="1" width="2.5em" cssClass="select-handle centered" sortable="true" visible="true"/>
		<dt:column-definition targets="2, 3, 4, 5" sortable="false" visible="true"/>
		<dt:column-definition targets="6" sortable="false" visible="true" />
		<dt:column-definition targets="7" sortable="false" visible="false" lastDef="true"/>
	</jsp:attribute>
</comp:decorate-ajax-table>


	
<table id="issue-table">
	<thead>
		<tr>
			<th>URL(not displayed)</th>
			<th style="cursor:pointer">${interfaceDescriptor.tableIssueIDHeader}</th>
			<th>${interfaceDescriptor.tableSummaryHeader}</th>
			<th>${interfaceDescriptor.tablePriorityHeader}</th>
			<th>${interfaceDescriptor.tableStatusHeader}</th>
			<th>${interfaceDescriptor.tableAssigneeHeader}</th>
			<th><f:message key="test-case.issues.table.column-header.reportedin.label" /></th>
			<th>Exec id</th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>



