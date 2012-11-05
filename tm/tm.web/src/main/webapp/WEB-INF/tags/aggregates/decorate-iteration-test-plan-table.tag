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
<%@ attribute name="baseIterationURL" description="the base iteration url" %>

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

<f:message var="statusUntestable" key="execution.execution-status.UNTESTABLE" />
<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
<f:message var="statusReady" key="execution.execution-status.READY" />
<f:message var="statusError" key="execution.execution-status.ERROR" />
<f:message var="statusWarning" key="execution.execution-status.WARNING" />

<script type="text/javascript">
	
	var testPlansUrl = "${testPlansUrl}";
	var nonBelongingTestPlansUrl = "${nonBelongingTestPlansUrl}";

	//This function checks the response and inform the user if a deletion was impossible
	function checkForbiddenDeletion(data) {
		if (data == "true") {
				squashtm.notification.showInfo('${ unauthorizedDeletion }');
		}
	}


	function newExecutionClickHandler() {
		var url = $(this).attr('data-new-exec');
		$.ajax({
					type : 'POST',
					url : url,
					data : {"mode":"manual"},
					dataType : "json"
				})
				.done(function(id) {
					document.location.href = "${showExecutionUrl}/" + id;
				});
		return false; //return false to prevent navigation in page (# appears at the end of the URL)
	}
	
	function newAutoExecutionClickHandler() {
		var url = $(this).attr('data-new-exec');
		$.ajax({
					type : 'POST',
					url : url,
					data : {"mode":"auto"},
					dataType : "json"
				})
				.done(function(suiteView) {
					refreshTestPlans();
					if(suiteView.executions.length == 0){
						$.squash
						.openMessage("<f:message key='popup.title.Info' />",
								"<f:message	key='dialog.execution.auto.overview.error.none'/>");
					}else{
						squashtm.automatedSuiteOverviewDialog.open(suiteView);
					}
				});
		return false; //return false to prevent navigation in page (# appears at the end of the URL)
	}



	function refreshTestPlans() {
		$('#test-plans-table').squashTable().refresh();
	}

	function refreshTestPlansWithoutSelection() {
		var table = $('#test-plans-table').squashTable();
		table.refresh();
		table.deselectRows();
	}


	function getTestPlansTableRowId(rowData) {
		return rowData['entity-id'];
	}
	function getTestPlanTableRowIndex(rowData) {
		return rowData['entity-index'];
	}
	function isTestCaseDeleted(data) {
		return (data['is-tc-deleted'] == "true");
	}

	function addIterationTestPlanItemExecModeIcon(row, data) {
		var automationToolTips = {
			"M" : "",
			"A" : "<f:message key='label.automatedExecution' />"
		};
		var automationClass = {
			"M" : "manual",
			"A" : "automated"
		};

		var mode = data["exec-mode"];
		$(row).find("td.exec-mode")
			.text('')
			.addClass("exec-mode-" + automationClass[mode])
			.attr("title", automationToolTips[mode]);
	}			

	function testPlanTableRowCallback(row, data, displayIndex) {
		addHLinkToTestPlanName(row, data);
		addIconToTestPlanName(row, data);
		addStyleToDeletedTestCaseRows(row, data);
		addIterationTestPlanItemExecModeIcon(row, data);
		return row;
	}
	
	function testPlanDrawCallback(){
		<c:if test="${ editable }">
		addLoginListToTestPlan();
		</c:if>		
	}
	
	function addHLinkToTestPlanName(row, data) {
		var url = 'javascript:void(0)';
		addHLinkToCellText($('td:eq(4)', row), url);
		$('td:eq(4) a', row).addClass('test-case-name-hlink');
	}

	function addIconToTestPlanName(row, data) {
		$('td:eq(4)', row)
			.prepend('<img src="${pageContext.servletContext.contextPath}/images/arrow_right.gif"/>');
	}

	<c:if test="${ editable }">
	function addLoginListToTestPlan(){
			
		var table = $("#test-plans-table").squashTable();
		
		//look first at the cache
		var assignableList = table.data('assignable-list');
		
		if (assignableList!=null){
			table.$('td.assignable-combo').loginCombo();
		}
		
		$.get("${assignableUsersUrl}", "json")
		.success(function(json){
			table.data('assignable-list', json);
			table.$('td.assignable-combo').loginCombo();
		});

	}
	
	//because of IE8 naturally trimming text nodes we will trim
	//every string we must compare.
	$.fn.loginCombo = function(assignableList){
		
		if (this.length==0) return;
		var squashTable=this.eq(0).parents("table").squashTable();
		var assignableList = squashTable.data('assignable-list');
		if (! assignableList) return;
		
		//create the template
		var template=$('<select/>');
		for (var i=0;i<assignableList.length;i++){
			var opt = '<option value="'+assignableList[i].id+'">'+assignableList[i].login+'</option>';
			template.append(opt);
		}
		
		template.change(function(){
			$.ajax({
				type : 'POST',
				url : this.getAttribute('data-assign-url'),
				data : "userId=" + this.value,
				dataType : 'json'
			});
		});
			
		this.each(function(){
			
			var cloneSelect = template.clone(true);
			
			var jqTd = $(this);
			var row = this.parentNode;
			
			
			//sets the change url
			var tpId = squashTable.getODataId(row);
			var dataUrl = "${baseIterationURL}/test-case/"+tpId+"/assign-user";
			
			cloneSelect.attr('data-assign-url', dataUrl);
		
			//selects the assigned user
			var assignedTo = squashTable.fnGetData(row)['assigned-to'] || "0";
			cloneSelect.val(assignedTo);
			
			
			//append the content
			jqTd.empty().append(cloneSelect);
			
		});	
	}
	</c:if>
	
	function addStyleToDeletedTestCaseRows(row, data) {
		if (isTestCaseDeleted(data)) {
			$(row).addClass("test-case-deleted");
		}
	}


	function toggleExpandIcon(testPlanHyperlink) {

		var table = $('#test-plans-table').squashTable();
		var data = table.fnGetData(testPlanHyperlink.parentNode.parentNode);
		var image = $(testPlanHyperlink).parent().find("img");
		var ltr = testPlanHyperlink.parentNode.parentNode;

		if (!$(testPlanHyperlink).hasClass("opened")) {
			/* the row is closed - open it */
			var nTr = table.fnOpen(ltr, "      ", "");
			var url1 = "${testPlanExecutionsUrl}" + data['entity-id'];
			var jqnTr = $(nTr);
			
			jqnTr.load(url1);
			if ($(this).parent().parent().hasClass("odd")) {
				jqnTr.addClass("odd");
			} else {
				jqnTr.addClass("even");
			}
			jqnTr.attr("style", "vertical-align:top;");

			image.attr("src",
							"${pageContext.servletContext.contextPath}/images/arrow_down.gif");

		} else {
			/* This row is already open - close it */
			table.fnClose(ltr);
			image.attr("src",
							"${pageContext.servletContext.contextPath}/images/arrow_right.gif");
		};
		$(testPlanHyperlink).toggleClass("opened");
	}

	$(function() {
		
		/* ************************** various event handlers ******************* */

		<%--=========================--%>
		<%-- multiple test-plan removal --%>
		<%--=========================--%>
		//multiple deletion
		$("#${ testCaseMultipleRemovalPopupId }").bind(
				'dialogclose',
				function() {
					var answer = $("#${ testCaseMultipleRemovalPopupId }")
							.data("answer");
					if (answer != "yes") {
						return;
					}

					var table = $('#test-plans-table').squashTable();
					var ids = getIdsOfSelectedTableRows(table,
							getTestPlansTableRowId);

					if (ids.length > 0) {
						$.post( nonBelongingTestPlansUrl , {
							testPlanIds : ids
						}, function(data) {
							refreshTestPlans();
							checkForbiddenDeletion(data);
							refreshStatistics();
						});
					}

				});
	
		<%-- bind the new execution creation button to their event --%>
		var newExecButtons = $('a.new-exec');
		newExecButtons.die('click');
		newExecButtons.live('click', newExecutionClickHandler);
		var newExecAutoButtons = $('a.new-auto-exec');
		newExecAutoButtons.die('click');
		newExecAutoButtons.live('click', newAutoExecutionClickHandler);
		

		/* could be optimized if we bind that in the datatableDrawCallback.	*/
		$('#test-plans-table tbody td a.test-case-name-hlink').die('click');
		<%-- binding the handler managing the collapse/expand test case icon--%>
		
		$('#test-plans-table tbody td a.test-case-name-hlink').live('click', function() {
				toggleExpandIcon(this);
				return false; //return false to prevent navigation in page (# appears at the end of the URL)
		});

		
		
		/* ************************** datatable settings ********************* */
		
		var tableSettings = {
				"oLanguage": {
					"sUrl": "<c:url value='/datatables/messages' />"
				},
				"sAjaxSource" : "${tableModelUrl}", 
				"fnRowCallback" : testPlanTableRowCallback,
				"fnDrawCallback" : testPlanDrawCallback,
				"aoColumnDefs": [
					{'bSortable': false, 'bVisible': false, 'aTargets': [0], 'mDataProp' : 'entity-id'},
					{'bSortable': false, 'sClass': 'centered ui-state-default drag-handle select-handle', 'aTargets': [1], 'mDataProp' : 'entity-index'},
					{'bSortable': false, 'aTargets': [2], 'mDataProp' : 'project-name'},
					{'bSortable': false, 'aTargets': [3], 'mDataProp' : 'exec-mode', 'sWidth': '2em', 'sClass' : "exec-mode"},
					{'bSortable': false, 'aTargets': [4], 'mDataProp' : 'reference'},
					{'bSortable': false, 'aTargets': [5], 'mDataProp' : 'tc-name'},
					{'bSortable': false, 'aTargets': [6], 'mDataProp' : 'importance'},
					{'bSortable': false, 'sWidth': '10%', 'aTargets': [7], 'mDataProp' : 'type'},
					{'bSortable': false, 'sWidth': '10%', 'aTargets': [8], 'mDataProp' : 'suite'},
					{'bSortable': false, 'sWidth': '10%', 'sClass': 'has-status', 'aTargets': [9], 'mDataProp' : 'status'},
					{'bSortable': false, 'sWidth': '10%', 'sClass': 'assignable-combo', 'aTargets': [10], 'mDataProp' : 'last-exec-by'},
					{'bSortable': false, 'bVisible' : false, 'sWidth': '10%', 'aTargets': [11], 'mDataProp' : 'assigned-to'},
					{'bSortable': false, 'sWidth': '10%', 'aTargets': [12], 'mDataProp' : 'last-exec-on'},
					{'bSortable': false, 'bVisible': false, 'aTargets': [13], 'mDataProp' : 'is-tc-deleted'},
					{'bSortable': false, 'sWidth': '2em', 'sClass': 'centered delete-button', 'aTargets': [14], 'mDataProp' : 'empty-delete-holder'} 
				]
			};		
		
			var squashSettings = {
					
				enableHover : true,
				executionStatus : {
					untestable : "${statusUntestable}",
					blocked : "${statusBlocked}",
					failure : "${statusFailure}",
					success : "${statusSuccess}",
					running : "${statusRunning}",
					ready : "${statusReady}",
					error : "${statusError}",
					warning : "${statusWarning}",
				},
				confirmPopup : {
					oklabel : '<f:message key="label.Yes" />',
					cancellabel : '<f:message key="label.Cancel" />'
				},
				
			};
			
			<c:if test="${editable}">
			squashSettings.enableDnD = true;

			squashSettings.deleteButtons = {
				url : "${testPlansUrl}/{entity-id}",
				popupmessage : '<f:message key="dialog.remove-testcase-association.message" />',
				tooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />',
				success : function(data) {
					refreshTestPlans();
					checkForbiddenDeletion(data);	
					refreshStatistics();				
				}
				
			functions : {
					dropHandler : function(dropData){
						$.post('${ updateTestPlanUrl }/move',dropData, function(){
							$("#test-plans-table").squashTable().refresh();
						});
					}
				}
			};
			</c:if>
					
			$("#test-plans-table").squashTable(tableSettings, squashSettings);
	});
</script>
