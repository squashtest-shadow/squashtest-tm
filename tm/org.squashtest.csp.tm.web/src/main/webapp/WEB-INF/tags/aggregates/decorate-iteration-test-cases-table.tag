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
<%@ tag body-content="empty"
	description="jqueryfies a campaign test case table"%>
<%@ attribute name="tableModelUrl" required="true"
	description="URL to GET the model of the table"%>
<%@ attribute name="testPlansUrl" required="true"
	description="URL to manipulate the test-plans"%>
<%@ attribute name="nonBelongingTestPlansUrl" required="true"
	description="URL to manipulate the non belonging test cases"%>
<%@ attribute name="batchRemoveButtonId" required="true"
	description="html id of button for batch removal of test cases"%>
<%@ attribute name="testPlanDetailsBaseUrl" required="true"
	description="base of the URL to get test case details"%>
<%@ attribute name="testPlanExecutionsUrl" required="true"
	description="base of the url to get the list of the executions for that test case"%>
<%@ attribute name="updateTestPlanUrl" required="true"
	description="base of the url to update the test case url"%>
<%@ attribute name="editable" type="java.lang.Boolean"
	description="Right to edit content. Default to false."%>
	
<%@ attribute name="assignableUsersUrl" required="true"
	description="URL to manipulate user of the test-plans"%>
<%@ attribute name="testCaseSingleRemovalPopupId" required="true"
	description="html id of the single test-case removal popup"%>
<%@ attribute name="testCaseMultipleRemovalPopupId" required="true"
	description="html id of the multiple test-case removal popup"%>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>


<s:url var="showExecutionUrl" value="/executions" />

<f:message var="cannotCreateExecutionException"
	key="squashtm.action.exception.cannotcreateexecution.label" />
<f:message var="unauthorizedDeletion"
	key="dialog.remove-testcase-association.unauthorized-deletion.message" />


<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
<f:message var="statusReady" key="execution.execution-status.READY" />

<script type="text/javascript">
	
	var testPlansUrl = "${testPlansUrl}";
	var nonBelongingTestPlansUrl = "${nonBelongingTestPlansUrl}";
	
	$(function() {


		//This function checks the response and inform the user if a deletion was impossible
		function checkForbiddenDeletion(data) {
			if (data == "true") {
				displayInformationNotification('${ unauthorizedDeletion }');
			}
		}
<%-- selected test-plan removal --%>
	//multiple deletion
		$("#${ testCaseMultipleRemovalPopupId }").bind(
				'dialogclose',
				function() {
					var answer = $("#${ testCaseMultipleRemovalPopupId }")
							.data("answer");
					if (answer != "yes") {
						return;
					}

					var table = $('#test-plans-table').dataTable();
					var ids = getIdsOfSelectedTableRows(table,
							getTestPlansTableRowId);

					if (ids.length > 0) {
						$.post( nonBelongingTestPlansUrl , {
							testPlanIds : ids
						}, function(data) {
							refreshTestPlans();
							checkForbiddenDeletion(data);
						});
					}

				});
<%-- bind the new execution creation button to their event --%>
	$('a[id|="new-exec"]').die('click');
		$('a[id|="new-exec"]').live('click', newExecutionClickHandler);

	});

	function changeUserLogin(cbox) {
		var jqBox = $(cbox);
		var tr = jqBox.parents("tr");
		var uncut = $(tr).attr("id");
		var id = uncut.substring(uncut.indexOf(":") + 1);
		var url = jqBox.attr('data-assign-url');
		$.ajax({
			type : 'POST',
			url : url,
			data : "userId=" + jqBox.val(),
			dataType : 'json'
		});

	}

	function newExecutionClickHandler() {
		var url = $(this).attr('data-new-exec');
		$.ajax({
					type : 'POST',
					url : url,
					dataType : "json"
				})
				.success(function(id) {
					document.location.href = "${showExecutionUrl}/" + id;
				});
		return false; //return false to prevent navigation in page (# appears at the end of the URL)
	}



	function refreshTestPlans() {
		var table = $('#test-plans-table').dataTable();
		saveTableSelection(table, getTestPlansTableRowId);
		table.fnDraw(false);
	}

	function refreshTestPlansWithoutSelection() {
		var table = $('#test-plans-table').dataTable();
		table.fnDraw(false);
	}

	function testPlanTableDrawCallback() {
		restoreTableSelection(this, getTestPlansTableRowId);
	}

	function getTestPlansTableRowId(rowData) {
		return rowData[0];
	}
	function getTestPlanTableRowIndex(rowData) {
		return rowData[1];
	}
	function isTestCaseDeleted(data) {
		return (data[10] == "true");
	}

	function testPlanTableRowCallback(row, data, displayIndex) {
		addIdtoTestPlanRow(row, data);
		<c:if test="${ editable }">
		addLoginListToTestPlan(row, data);
		</c:if>
		addHLinkToTestPlanName(row, data);
		addIconToTestPlanName(row, data);
		addStyleToDeletedTestCaseRows(row, data);
		return row;
	}

	function addIdtoTestPlanRow(nRow, aData) {
		$(nRow).attr("id", "test-plan:" + getTestPlansTableRowId(aData));
	}

	function parseTestPlanId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}

	function addHLinkToTestPlanName(row, data) {
		var url = 'javascript:void(0)';
		addHLinkToCellText($('td:eq(2)', row), url);
		$('td:eq(2) a', row).addClass('test-case-name-hlink');
	}

	function addIconToTestPlanName(row, data) {
		$('td:eq(2)', row)
				.prepend(
						'<img src="${pageContext.servletContext.contextPath}/images/arrow_right.gif"/>');
	}

	function addLoginListToTestPlan(row, data) {
		if (!isTestCaseDeleted(data)) {
			var id = getTestPlansTableRowId(data);
			$('td:eq(7)', row).load(
					"${assignableUsersUrl}" + "?testPlanId=" + id + "");
		}
	}

	function addStyleToDeletedTestCaseRows(row, data) {
		if (isTestCaseDeleted(data)) {
			$(row).addClass("test-case-deleted");
		}
	}


	function toggleExpandIcon(testPlanHyperlink) {

		var table = $('#test-plans-table').dataTable();
		var donnees = table.fnGetData(testPlanHyperlink.parentNode.parentNode);
		var image = $(testPlanHyperlink).parent().find("img");
		var ltr = testPlanHyperlink.parentNode.parentNode;

		if (!$(testPlanHyperlink).hasClass("opened")) {
			/* the row is closed - open it */
			var nTr = table.fnOpen(ltr, "      ", "");
			var url1 = "${testPlanExecutionsUrl}" + donnees[0];

			$(nTr).load(url1);
			if ($(this).parent().parent().hasClass("odd")) {
				$(nTr).addClass("odd");
			} else {
				$(nTr).addClass("even");
			}
			$(nTr).attr("style", "vertical-align:top;");

			image
					.attr("src",
							"${pageContext.servletContext.contextPath}/images/arrow_down.gif");

		} else {
			/* This row is already open - close it */
			table.fnClose(ltr);
			image
					.attr("src",
							"${pageContext.servletContext.contextPath}/images/arrow_right.gif");
		}
		;
		$(testPlanHyperlink).toggleClass("opened");
	}

	$(function() {
		/* could be optimized if we bind that in the datatableDrawCallback.	*/
		$('#test-plans-table tbody td a.test-case-name-hlink').die('click');
		<%-- binding the handler managing the collapse/expand test case icon--%>
		
		$('#test-plans-table tbody td a.test-case-name-hlink').live('click', function() {
				toggleExpandIcon(this);
				return false; //return false to prevent navigation in page (# appears at the end of the URL)
		});

		var tableSettings = {
				"oLanguage":{
					"sLengthMenu": '<f:message key="generics.datatable.lengthMenu" />',
					"sZeroRecords": '<f:message key="generics.datatable.zeroRecords" />',
					"sInfo": '<f:message key="generics.datatable.info" />',
					"sInfoEmpty": '<f:message key="generics.datatable.infoEmpty" />',
					"sInfoFiltered": '<f:message key="generics.datatable.infoFiltered" />',
					"oPaginate":{
						"sFirst":    '<f:message key="generics.datatable.paginate.first" />',
						"sPrevious": '<f:message key="generics.datatable.paginate.previous" />',
						"sNext":     '<f:message key="generics.datatable.paginate.next" />',
						"sLast":     '<f:message key="generics.datatable.paginate.last" />'
					}
				},				
				"sAjaxSource" : "${tableModelUrl}", 
				"fnRowCallback" : testPlanTableDrawCallback,
				"fnDrawCallback" : testPlanTableRowCallback,
				"aoColumnDefs": [
					{'bSortable': false, 'bVisible': false, 'aTargets': [0], 'mDataProp' : 'entity-id'},
					{'bSortable': false, 'sClass': 'centered ui-state-default drag-handle select-handle', 'aTargets': [1], 'mDataProp' : 'entity-index'},
					{'bSortable': false, 'aTargets': [2], 'mDataProp' : 'project-name'},
					{'bSortable': false, 'aTargets': [3], 'mDataProp' : 'tc-name'},
					{'bSortable': false, 'aTargets': [4], 'mDataProp' : 'importance'},
					{'bSortable': false, 'sWidth': '10%', 'aTargets': [5], 'mDataProp' : 'type'},
					{'bSortable': false, 'sWidth': '10%', 'aTargets': [6], 'mDataProp' : 'suite'},
					{'bSortable': false, 'sWidth': '10%', 'sClass': 'has-status', 'aTargets': [7], 'mDataProp' : 'status'},
					{'bSortable': false, 'sWidth': '10%', 'aTargets': [8], 'mDataProp' : 'last-exec-by'},
					{'bSortable': false, 'sWidth': '10%', 'aTargets': [9], 'mDataProp' : 'last-exec-on'},
					{'bVisible': false, 'bSortable': false, 'aTargets': [10], 'mDataProp' : 'is-tc-deleted'},
					{'bSortable': false, 'sWidth': '2em', 'sClass': 'centered delete-button', 'aTargets': [11], 'mDataProp' : 'empty-delete-holder'} 
				]
			};		
		
			var squashSettings = {
					
				enableHover : true,
				executionStatus : {
					blocked : "${statusBlocked}",
					failure : "${statusFailure}",
					success : "${statusSuccess}",
					running : "${statusRunning}",
					ready : "${statusReady}"
				},
				confirmDialog : {
					oklabel : '<f:message key="attachment.button.delete.label" />',
					cancellabel : '<f:message key="dialog.button.cancel.label" />'
				},
				functions : {
					dropHandler : function(dropData){
						$.post('${ updateTestPlanUrl }/move',dropData, function(){
							refreshTestPlans();
						});
					}
				}
			};
			
			<c:if test="${editable}">
			squashSettings.enableDnD = true;

			squashSettings.deleteButton = {
				
				url : "${testPlansUrl}/{entity-id}",
				popupmessage : '<f:message key="dialog.remove-testcase-association.message" />',
				tooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />',
				success : function(data) {
					refreshTestPlans();
					checkForbiddenDeletion(data);					
				}
					
			};
			</c:if>
					
			$("#execution-execution-steps-table").squashTable(tableSettings, squashSettings);
	});
</script>
