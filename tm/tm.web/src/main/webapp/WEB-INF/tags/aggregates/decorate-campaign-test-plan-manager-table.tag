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
<%@ tag body-content="empty" description="jqueryfies a campaign test case table" %>
<%@ attribute name="campaignUrl" required="true" description="the url to the campaign that hold all of these test cases" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url  var="testCaseDetailsBaseUrl" value="/test-cases" /> 

<script type="text/javascript">
	$(function() {
		<%-- single test-case removal --%>
		$('#test-cases-table .delete-test-case-button').die('click');
		
		$('#test-cases-table .delete-test-case-button').live('click', function() {
			$.ajax({
				type : 'delete',
				url : '${ campaignUrl }/test-plan/' + trToItemId(this),
				dataType : 'json',
				success : refreshTestPlan		
			});
			return false; //return false to prevent navigation in page (# appears at the end of the URL)
		});
		<%-- selected test-case removal --%>
		$( '#${ batchRemoveButtonId }' ).click(function() {
			var table = $( '#test-cases-table' ).dataTable();
			var ids = getIdsOfSelectedTableRows(table, rowDataToItemId);
			
			if (ids.length > 0) {
				$.post('${ campaignUrl }/test-plan', { action: 'remove', itemIds: ids }, refreshTestPlan);
			}
		});
	});
	
	function refreshTestPlan() {
		var table = $('#test-cases-table').dataTable();
		saveTableSelection(table, rowDataToItemId);
		table.fnDraw(false);
	}
	
	function refreshTestPlanWithoutSelection(){
		var table = $('#test-cases-table').dataTable();
		table.fnDraw(false);
	}

	function testPlanDrawCallback() {
		<c:if test="${ editable }">
		decorateDeleteButtons($('.delete-test-case-button', this));
		</c:if>
		restoreTableSelection(this, rowDataToItemId);
	}

	function rowDataToItemId(rowData) {
		return rowData[0];	
	}

	function rowDataToTestCaseId(rowData) {
		return rowData[8];	
	}

	function addIdToTr(nRow, aData){
		$(nRow).attr("id", "test-plan-item:" + rowDataToItemId(aData));
	}
	
	function testPlanTableRowCallback(row, data, displayIndex) {
		addIdToTr(row, data);
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, rowDataToItemId(data), 'delete-test-case-button');
		</c:if>
		addClickHandlerToSelectHandle(row, $("#test-cases-table"));
		addHLinkToTestCaseName(row, data);
		return row;
	}
	
	function trToItemId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addHLinkToTestCaseName(row, data) {
		var url= '${ testCaseDetailsBaseUrl }/' + rowDataToTestCaseId(data) + '/info';			
		addHLinkToCellText($( 'td:eq(3)', row ), url);
	}	
</script>

<comp:decorate-ajax-table url="${ campaignUrl }/test-plan/manager/table" tableId="test-cases-table" paginate="true">
	<jsp:attribute name="drawCallback">testPlanDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">testPlanTableRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" />
		<dt:column-definition targets="1" sortable="false" cssClass="select-handle centered" width="2em"/>
		<dt:column-definition targets="2,3,4,5,6" sortable="false" />
		<dt:column-definition targets="7" sortable="false" width="2em" cssClass="centered"/>
		<dt:column-definition targets="8" visible="false" lastDef="true" />
	</jsp:attribute>
</comp:decorate-ajax-table>
