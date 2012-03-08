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
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="testCaseDetailsBaseUrl" required="true" description="base of the URL to get test case details" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="assignableUsersUrl" required="true" description="URL to manipulate user of the test-cases" %>
<%@ attribute name="campaignUrl" required="true" description="the url to the campaign that hold all of these test cases" %>
<%@ attribute name="testCaseSingleRemovalPopupId" required="true" description="html id of the single test-case removal popup" %>
<%@ attribute name="testCaseMultipleRemovalPopupId" required="true" description="html id of the multiple test-case removal popup" %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>


<script type="text/javascript">
	$(function() {
		//multiple deletion
		$( '#${ testCaseMultipleRemovalPopupId }' ).bind('dialogclose', function() {
			var answer = $("#${ testCaseMultipleRemovalPopupId }").data("answer");
			if (answer != "yes") {
				return;
			}
			
			var table = $( '#test-cases-table' ).dataTable();
			var ids = getIdsOfSelectedTableRows(table, getTestPlanTableRowId);
			
			if (ids.length > 0) {
				$.post('${ campaignUrl }/test-plan', { action: 'remove', itemsIds: ids }, refreshTestPlan);
			}
		
		});
		<%-- single test-case removal --%>
		$('#test-cases-table .delete-test-case-button').die('click');
		
		//single deletion buttons
		$('#test-cases-table .delete-test-case-button').live('click', function() {
			$("#${ testCaseSingleRemovalPopupId }").data('opener', this).dialog('open');
		});
		
		$("#${ testCaseSingleRemovalPopupId }").bind('dialogclose', function() {
			var answer = $("#${ testCaseSingleRemovalPopupId }").data("answer");
			if (answer != "yes") {
				return;
			}
			var bCaller = $.data(this,"opener");
		
			$.ajax({
				type : 'delete',
				url : '${ campaignUrl }/test-plan/' + parseItemId(bCaller),
				dataType : 'json',
				success : refreshTestPlan
			});
		});
	});
	
	function changeUserLogin(cbox){
		var jqBox = $(cbox);
		var tr = jqBox.parents("tr");
		var uncut = $(tr).attr("id");
		var id = uncut.substring(uncut.indexOf(":") + 1);
		var url = jqBox.attr('data-assign-url');
		$.ajax({
			  type: 'POST',
			  url: url,
			  data: "userId="+jqBox.val(),
			  dataType: 'json'
		});

	}

	function testPlanDropHandler(rows, dropPosition) {
		var itemsIds = $(rows).collect(function(row) { return parseItemId(row); });
		$.post('${ campaignUrl }/test-plan/index/' + dropPosition, { action : 'move', itemsIds : itemsIds }, refreshTestPlan);
	}
	
	function refreshTestPlan() {
		var table = $('#test-cases-table').dataTable();
		saveTableSelection(table, getTestPlanTableRowId);
		table.fnDraw(false);
	}
	
	function refreshTestPlanWithoutSelection(){
		var table = $('#test-cases-table').dataTable();
		saveTableSelection(table, getTestPlanTableRowId);
		table.fnDraw(false);
	}

	function testPlanDrawCallback() {
		<c:if test="${ editable }">
		enableTableDragAndDrop('test-cases-table', getTestPlanTableRowIndex, testPlanDropHandler);
		decorateDeleteButtons($('.delete-test-case-button', this));
		</c:if>
		restoreTableSelection(this, getTestPlanTableRowId);
	}

	function getTestPlanTableRowId(rowData) {
		return rowData[0];	
	}

	function getTestPlanTableRowIndex(rowData){
		return rowData[1];
	}

	function addIdtoTestCaseRow(nRow, aData){
		$(nRow).attr("id", "test-plan-item:" + getTestPlanTableRowId(aData));
	}
	
	function testPlanRowCallback(row, data, displayIndex) {
		addIdtoTestCaseRow(row, data);
		<c:if test="${ editable }">
		<c:if test='${assignableUsersUrl != " " }'>
		addLoginListToTestCase(row, data);
		</c:if>
		addDeleteButtonToRow(row, getTestPlanTableRowId(data), 'delete-test-case-button');
		</c:if>
		addClickHandlerToSelectHandle(row, $("#test-cases-table"));
		addHLinkToTestCaseName(row, data);
		return row;
	}
	
	function addLoginListToTestCase(row, data) {
		var id = getTestPlanTableRowId(data);
		$('td:eq(3)', row).load("${assignableUsersUrl}" + "?testCaseId="+ id +"");
	}
	
	function parseItemId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addHLinkToTestCaseName(row, data) {
		var url= '${ testCaseDetailsBaseUrl }/' + getTestPlanTableRowId(data) + '/info';			
		addHLinkToCellText($( 'td:eq(2)', row ), url);
	}	
</script>

<comp:decorate-ajax-table url="${ campaignUrl }/test-plan/table" tableId="test-cases-table" paginate="true">
	<jsp:attribute name="initialSort">[[3,'asc']]</jsp:attribute>
	<jsp:attribute name="drawCallback">testPlanDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">testPlanRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" />
		<dt:column-definition targets="1" sortable="false" cssClass="select-handle drag-handle centered" width="2em" />
		<dt:column-definition targets="2,3,4,5,6" sortable="false" />
		<dt:column-definition targets="7" sortable="false" width="2em" lastDef="true" cssClass="centered" />
	</jsp:attribute>
</comp:decorate-ajax-table>
