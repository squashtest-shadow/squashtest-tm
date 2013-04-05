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
<%@ tag description="Table displaying the issues for an ExecutionStep" body-content="empty" %>

<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>	
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>

<%@ attribute name="interfaceDescriptor" type="java.lang.Object" required="true" description="an object holding the labels for the interface" %>
<%@ attribute name="dataUrl" required="true" description="where the table will fetch its data" %>
<%@ attribute name="bugTrackerUrl" required="true" description="where the delete buttons send the delete instruction" %>
<%@ attribute name="entityId" required="true" description="id of the current execution" %>
<%@ attribute name="freeSettings" required="true" description="added settings to issue table" %>
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
	
	function refreshTestPlan() {
		$('#issue-table').squashTable().refresh();
	}

	function getIssueTableRowUrl(rowData){
		return rowData[0];
	}

	function getIssueTableRowId(rowData) {
		return rowData[1];
	}
	
	function getIssueTableRowAssignee(rowData){
		return rowData[6];
	}

	function getIssueTableRowIssueId(rowData) {
		return rowData[8];
	}
	
	function issueTableRowCallback(row, data, displayIndex) {
		addHLinkToIdRow(row,data);
		checkEmptyValues(row, data);
		addIdAttributeToIdRow(row,data);
		return row;
	}
	
	
	function addHLinkToIdRow(row, data){
		var td = $(row).find("td:eq(0)");
		var url = getIssueTableRowUrl(data);
		addHLinkToCellText(td, url, true);
	}
	
	function addIdAttributeToIdRow(row,data){
		var td = $(row).find("td:eq(0)");
		var issueid = getIssueTableRowIssueId(data);
		td.attr('issueid',issueid);
	}
	
	function rowDataToTestCaseId(rowData){
		return rowData['tc-id'];
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

	
	/* ************************** datatable settings ********************* */


	$(function() {
		
		var tableSettings = {
				"oLanguage": {
					"sUrl": "<c:url value='/datatables/messages' />",
					"oPaginate":{
						"sFirst":    '<f:message key="generics.datatable.paginate.first" />',
						"sPrevious": '<f:message key="generics.datatable.paginate.previous" />',
						"sNext":     '<f:message key="generics.datatable.paginate.next" />',
						"sLast":     '<f:message key="generics.datatable.paginate.last" />'
					}
				},
				"sAjaxSource" : "${dataUrl}", 
				"aaSorting" : [[1,'desc']],
				"fnRowCallback" : issueTableRowCallback,
				"aoColumnDefs": [
					{'bSortable': false, 'bVisible': false, 'aTargets': [0], 'mDataProp' : 'issue-url'},
					{'bSortable': true,  'sClass': 'select-handle centered', 'aTargets': [1], 'mDataProp' : 'remote-id', 'sWidth' : '2em'},
					{'bSortable': false, 'aTargets': [2], 'mDataProp' : 'summary'},
					{'bSortable': false, 'aTargets': [3], 'sWidth': '2em', 'mDataProp' : 'priority'},
					{'bSortable': false, 'aTargets': [4], 'mDataProp' : 'status'},
					{'bSortable': false, 'aTargets': [5], 'mDataProp' : 'assignee'},
					{'bSortable': false, 'aTargets': [6], 'mDataProp' : 'owner'},
					{'bSortable': false, 'sWidth': '2em', 'aTargets': [7], 'sClass' : 'centered delete-button', 'mDataProp' : 'empty-placeholder'},
					{'bSortable': false, 'bVisible': false, 'aTargets': [8], 'mDataProp' : 'local-id'}
				]
			};		
		
			var squashSettings = {

			};
	
			
			squashSettings.enableDnD = false;
	
			squashSettings.deleteButtons = {
				url : '${bugTrackerUrl}issues/{local-id}',
				popupmessage : '<f:message key="dialog.remove-testcase-association.message" />',
				tooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />',
				success : function(data) {
					refreshTestPlan();
				}					
			};
			
			
			$("#issue-table").squashTable(tableSettings, squashSettings);
	});
	
</script>
	
<table id="issue-table">
	<thead>
		<tr>
			<th>URL(not displayed)</th>
			<th style="cursor:pointer">${interfaceDescriptor.tableIssueIDHeader}</th>
			<th>${interfaceDescriptor.tableSummaryHeader}</th>
			<th>${interfaceDescriptor.tablePriorityHeader}</th>
			<th>${interfaceDescriptor.tableStatusHeader}</th>
			<th>${interfaceDescriptor.tableAssigneeHeader}</th>
			<th>${interfaceDescriptor.tableReportedInHeader}</th>
			<th></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>



