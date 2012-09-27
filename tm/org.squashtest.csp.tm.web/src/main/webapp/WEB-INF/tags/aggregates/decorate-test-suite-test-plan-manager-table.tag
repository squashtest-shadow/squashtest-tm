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
<%@ attribute name="tableModelUrl" required="true" description="URL to GET the model of the table" %>
<%@ attribute name="testPlansUrl" required="true" description="URL to manipulate (delete) the test-plans" %>
<%@ attribute name="nonBelongingTestPlansUrl" required="true" description="URL to manipulate the non belonging test cases" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="testPlanDetailsBaseUrl" required="true" description="base of the URL to get test case details" %> 
<%@ attribute name="updateTestPlanUrl" required="true" description="base of the url to update the test case url" %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags/input" %>

<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />
<f:message var="cyclicStepCallException" key="squashtm.action.exception.cyclicstepcallexception.label" />

<script type="text/javascript">
	
	<%-- 
		table js 
		Note : we duplicate part of the code of aggregate:decorate-iteration-test-plan-table, because we don't need 
		all the features that manage its executions	
	--%>
		
	$(function() {
		
		$( '#${ batchRemoveButtonId }' ).click(function() {
				var table = $( '#test-plan-table' ).dataTable();
				var ids = getIdsOfSelectedTableRows(table);
				
				if (ids.length > 0) {
					$.post('${ nonBelongingTestPlansUrl }/remove/detach', { testPlanIds: ids }, function(data){
						refreshTestPlans();
						checkForbiddenDeletion(data);
					});
				}
				
				$( 'tr.row_selected', table ).removeClass('row_selected');
			});
	
	
		<%-- single test-case removal --%>
		$('#test-plan-table .delete-test-case-button').live('click', function() {
			$.ajax({
				type : 'delete',
				url : '${ testPlansUrl }/remove/detach/' + parseTestPlanId(this),
				dataType : 'text',
				success : function(data){
					refreshTestPlans();
					checkForbiddenDeletion(data);
				}
			});
			return false; //return false to prevent navigation in page (# appears at the end of the URL)
		});

	});
		
	//This function checks the response and inform the user if a deletion was impossible
	function checkForbiddenDeletion(data){
		if(data=="true"){
			displayInformationNotification('${ unauthorizedDeletion }');
		}
	}
	
	//for drag and drop test case feature
	//row : selected row
	//dropPosition : the new position
	function testPlanDropHandler(row, dropPosition) {
		//first compose the url to update the order, then send a request attribute newIndex and call the refresh function
		$.post('${ updateTestPlanUrl }'+'/move', { itemIds: parseTestPlanIds(row), newIndex : dropPosition }, function(){
			refreshTestPlans();
		}) ;
	}
	
	function refreshTestPlans() {
		var table = $('#test-plan-table').dataTable();
		saveTableSelection(table, getTestPlansTableRowId);
		table.fnDraw(false);
	}
	
	function getTestPlansTableRowId(rowData) {
		return rowData[0];	
	}
	
	function getTestPlanTableRowIndex(rowData){
		return rowData[1];
	}
	
	function isTestCaseDeleted(rowData){
		return (rowData[7]=="true");
	}
	
	function getTestCaseId(rowData){
		return rowData[6];
	}
	
	
	function testPlanTableDrawCallback() {
		enableTableDragAndDrop('test-plan-table', getTestPlanTableRowIndex, testPlanDropHandler);
		decorateDeleteButtons($('.delete-test-case-button', this));
		restoreTableSelection(this, getTestPlansTableRowId);
	}
	
	
	
	function testPlanTableRowCallback(row, data, displayIndex) {
		addIdtoTestPlanRow(row, data);
		addDeleteButtonToRow(row, getTestPlansTableRowId(data), 'delete-test-case-button');
		addClickHandlerToSelectHandle(row, $("#test-plan-table"));
		addHLinkToTestPlanName(row, data);
		return row;
	}
	
	function addIdtoTestPlanRow(nRow, aData){
		$(nRow).attr("id", "test-case:" + getTestPlansTableRowId(aData));
	}
	
	function parseTestPlanIds(elements) {
		var ids = new Array();
		for(var i=0; i<elements.length; i++) {
			ids.push(parseTestPlanId(elements[i]));
		}
		return ids;
	}
	
	function parseTestPlanId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addHLinkToTestPlanName(row, data) {
		if (! isTestCaseDeleted(data) ){
			var url= '${ testPlanDetailsBaseUrl }/' + getTestCaseId(data) + '/info';		
			addHLinkToCellText($( 'td:eq(2)', row ), url);
		}
	}	


<%-- returns list of id of selected row --%>
	function getIdsOfSelectedTableRows(dataTable) {
		var rows = dataTable.fnGetNodes();
		var ids = new Array();
		
		$( rows ).each(function(index, row) {
			if ($( row ).attr('class').search('selected') != -1) {
				var data = dataTable.fnGetData(row);
				ids.push(data[0]);
			}
		});
		
		return ids;
	}
	
	
	$(function(){	

		/*
			could be optimized if we bind that in the datatableDrawCallback.		
		*/
		
		$('#test-plans-table tbody td a.test-case-name-hlink').die('click');
		
		<%-- binding the handler managing the collapse/expand test case icon--%>
		$('#test-plans-table tbody td a.test-case-name-hlink').live('click', function () {
			toggleExpandIcon(this);
		} );
		
	});
	
</script>

<%-- CONFIRM DELETION FROM ITERATION IF DISASSOCIATION FROM TEST SUITE IS ASKED --%>	
<f:message var="confirmDeletionFromTestSuiteAndIteration" key="dialog.testSuite.and.iteration.deletion.confirm.title" />	
<div id="confirm-deletion-from-test-suite-and-iteration-dialog" class="not-displayed popup-dialog" title="${ confirmTestSuiteAndIterationRemoval }">
	<strong><f:message key="dialog.testSuite.and.iteration.deletion.confirm.text" /></strong>
	<input:confirm />
	<input:cancel />
</div>

<comp:decorate-ajax-table url="${ tableModelUrl }" tableId="test-plan-table" paginate="true">
	<jsp:attribute name="drawCallback">testPlanTableDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">testPlanTableRowCallback</jsp:attribute>
		<jsp:attribute name="initialSort">[[2,'asc']]</jsp:attribute>
		<jsp:attribute name="columnDefs">
			<dt:column-definition targets="0" visible="false" />
			<dt:column-definition targets="1" sortable="false" cssClass="centered ui-state-default drag-handle select-handle" />
			<dt:column-definition targets="2, 3, 4, 5" sortable="false" />
			<dt:column-definition targets="6, 7" sortable="false" visible="false"/>
			<dt:column-definition targets="8" sortable="false" width="2em" lastDef="true" cssClass="centered"/>
		</jsp:attribute>
</comp:decorate-ajax-table>