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
<%@ tag body-content="empty" description="jqueryfies a verified reqs table" %>
<%@ attribute name="tableModelUrl" required="true" description="URL to GET the model of the table" %>
<%@ attribute name="verifyingTestCasesUrl" required="true" description="URL to manipulate the verifying test-cases" %>
<%@ attribute name="nonVerifyingTestCasesUrl" required="true" description="URL to manipulate the non verifying test cases" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script type="text/javascript">
	$(function() {
		<%-- single verifying test-case removal --%>
		$('#verifying-test-cases-table .delete-verifying-test-case-button').die('click');
		$('#verifying-test-cases-table .delete-verifying-test-case-button').live('click', function() {
			var savedThis = this;
			oneShotConfirm("<f:message key='label.Confirm' />", 
					"<f:message key='dialog.remove-testcase-requirement-association.message' />",
					"<f:message key='label.Confirm'/>",
					"<f:message key='label.Cancel'/>", '600px').done(	function(){deleteRequirementLink(savedThis);});
			
		});
		<%-- selected verifying test-case removal --%>
		$( '#${ batchRemoveButtonId }' ).click(function() {
			var table = $( '#verifying-test-cases-table' ).dataTable();
			var ids = getIdsOfSelectedTableRows(table, getTestCasesTableRowId);
			
			if (ids.length > 0) {
				oneShotConfirm("<f:message key='label.Confirm' />", 
						"<f:message key='dialog.remove-testcase-requirement-associations.message' />",
						"<f:message key='label.Confirm'/>",
						"<f:message key='label.Cancel'/>", '600px').done(function(){deleteRequirementsLinks(ids);	});
			}
		});
	});
	function deleteRequirementLink(savedThis){
		$.ajax({
			type : 'delete',
			url : '${ verifyingTestCasesUrl }/' + parseTestCaseId(savedThis),
			dataType : 'json',
			success : refreshVerifyingTestCases
			});
	}
	function deleteRequirementsLinks(ids){
		$.post(
				'${ nonVerifyingTestCasesUrl }',
				{ testCasesIds: ids },
				refreshVerifyingTestCases
			 );
	}
	function refreshVerifyingTestCases() {
		var table = $('#verifying-test-cases-table').dataTable();
		saveTableSelection(table, getTestCasesTableRowId);
		table.fnDraw(false);
	}

	function testCaseTableDrawCallback() {
		<c:if test="${ editable }">
		decorateDeleteButtons($('.delete-verifying-test-case-button', this));
		</c:if>
		restoreTableSelection(this, getTestCasesTableRowId);
	}

	function getTestCasesTableRowId(rowData) {
		return rowData[0];	
	}

	function testCaseTableRowCallback(row, data, displayIndex) {
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getTestCasesTableRowId(data), 'delete-verifying-test-case-button');
		</c:if>
		addClickHandlerToSelectHandle(row, $("#verifying-test-cases-table"));
		addHLinkToTestCaseName(row, data);
		return row;
	}

	function parseTestCaseId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addHLinkToTestCaseName(row, data) {
		var url= "<c:url value='/test-cases/' />" + getTestCasesTableRowId(data) + '/info';			
		addHLinkToCellText($( 'td:eq(3)', row ), url);
	}	
</script>
<comp:decorate-ajax-table url="${ tableModelUrl }" tableId="verifying-test-cases-table" paginate="true">
	<jsp:attribute name="initialSort">[[3,'asc']]</jsp:attribute>
	<jsp:attribute name="drawCallback">testCaseTableDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">testCaseTableRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" />
		<dt:column-definition targets="1" sortable="false" cssClass="select-handle centered" width="2em"/>
		<dt:column-definition targets="2, 3, 4, 5" sortable="true" />
		<dt:column-definition targets="6" sortable="false" width="2em" lastDef="true" cssClass="centered"/>
	</jsp:attribute>
</comp:decorate-ajax-table>
