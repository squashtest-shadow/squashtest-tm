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
<%@ tag body-content="empty" description="jqueryfies a campaign test case table" %>
<%@ attribute name="campaignUrl" required="true" description="the url to the campaign that hold all of these test cases" %>
<%@ attribute name="testCasesUrl" required="true" description="URL to manipulate the test-cases" %>
<%@ attribute name="nonBelongingTestCasesUrl" required="true" description="URL to manipulate the non belonging test cases" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="testCaseDetailsBaseUrl" required="true" description="base of the URL to get test case details" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script type="text/javascript">
	$(function() {
		<%-- single test-case removal --%>
		$('#test-cases-table .delete-test-case-button').die('click');
		
		$('#test-cases-table .delete-test-case-button').live('click', function() {
			$.ajax({
				type : 'delete',
				url : '${ testCasesUrl }/' + parseTestCaseId(this),
				dataType : 'json',
				success : refreshTestCases
			});
		});
		<%-- selected test-case removal --%>
		$( '#${ batchRemoveButtonId }' ).click(function() {
			var table = $( '#test-cases-table' ).dataTable();
			var ids = getIdsOfSelectedTableRows(table, getTestCasesTableRowId);
			
			if (ids.length > 0) {
				$.post('${ nonBelongingTestCasesUrl }', { testCasesIds: ids }, refreshTestCases);
			}
		});
	});
	
	function refreshTestCases() {
		var table = $('#test-cases-table').dataTable();
		saveTableSelection(table, getTestCasesTableRowId);
		table.fnDraw(false);
	}
	
	function refreshTestCasesWithoutSelection(){
		var table = $('#test-cases-table').dataTable();
		table.fnDraw(false);
	}

	function testCaseTableDrawCallback() {
		<c:if test="${ editable }">
		decorateDeleteButtons($('.delete-test-case-button', this));
		</c:if>
		restoreTableSelection(this, getTestCasesTableRowId);
	}

	function getTestCasesTableRowId(rowData) {
		return rowData[0];	
	}

	function addIdtoTestCaseRow(nRow, aData){
		$(nRow).attr("id", "test-case:" + getTestCasesTableRowId(aData));
	}
	
	function testCaseTableRowCallback(row, data, displayIndex) {
		addIdtoTestCaseRow(row, data);
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getTestCasesTableRowId(data), 'delete-test-case-button');
		</c:if>
		addClickHandlerToSelectHandle(row, $("#test-cases-table"));
		addHLinkToTestCaseName(row, data);
		return row;
	}
	
	function parseTestCaseId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addHLinkToTestCaseName(row, data) {
		var url= '${ testCaseDetailsBaseUrl }/' + getTestCasesTableRowId(data) + '/info';			
		addHLinkToCellText($( 'td:eq(2)', row ), url);
	}	
</script>

<comp:decorate-ajax-table url="${ campaignUrl }/test-plan/manager/table" tableId="test-cases-table" paginate="true">
	<jsp:attribute name="drawCallback">testCaseTableDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">testCaseTableRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" />
		<dt:column-definition targets="1" sortable="false" cssClass="select-handle centered" width="2em"/>
		<dt:column-definition targets="2,3,4,5" sortable="false" />
		<dt:column-definition targets="6" sortable="false" width="2em" lastDef="true" cssClass="centered"/>
	</jsp:attribute>
</comp:decorate-ajax-table>
