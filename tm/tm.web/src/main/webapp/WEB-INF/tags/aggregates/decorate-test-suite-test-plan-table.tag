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
<%@ attribute name="removeTestPlansUrl" required="true" description="URL to delete the selected test-case from the test-plan" %>
<%@ attribute name="nonBelongingTestPlansUrl" required="true" description="URL to manipulate the non belonging test cases" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="testPlanDetailsBaseUrl" required="true" description="base of the URL to get test case details" %>
<%@ attribute name="testPlanExecutionsUrl" required="true" description="base of the url to get the list of the executions for that test case"%> 
<%@ attribute name="updateTestPlanUrl" required="true" description="base of the url to update the test case url" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="executable" type="java.lang.Boolean" description="Right to execute. Default to false." %>
<%@ attribute name="assignableUsersUrl" required="true" description="URL to manipulate user of the test-plans" %>
<%@ attribute name="testCaseSingleRemovalPopupId" required="true" description="html id of the single test-case removal popup" %>
<%@ attribute name="testCaseMultipleRemovalPopupId" required="true" description="html id of the multiple test-case removal popup" %>
<%@ attribute name="testSuiteExecButtonsId" required="true" description="html id of the test suite execution buttons panel" %>
<%@ attribute name="testSuiteExecButtonsUrl" required="true" description="URL to refresh the labels on the execution buttons" %>
<%@ attribute name="baseIterationUrl" required="true" description="the base iteration url" %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>


<f:message var="statusUntestable" key="execution.execution-status.UNTESTABLE" />
<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
<f:message var="statusReady" key="execution.execution-status.READY" />
<f:message var="statusError" key="execution.execution-status.ERROR" />
<f:message var="statusWarning" key="execution.execution-status.WARNING" />

<s:url var ="showExecutionUrl" value="/executions"/>

<f:message var="cannotCreateExecutionException" key="squashtm.action.exception.cannotcreateexecution.label" />
<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />

<script type="text/javascript">
	var removeTestPlansUrl = "${removeTestPlansUrl}";
	var nonBelongingTestPlansUrl = "${nonBelongingTestPlansUrl}";
	var runnerUrl = ""; //URL used to run an execution. will be created dynamically
	
	var dryRunStart = function() {
		return $.ajax({
			url : runnerUrl,
			method : 'get',
			dataType : 'json',
			data : {
				'dry-run' : ''
			}
		});
	};
	
	
	$(function() {
		<%--=========================--%>
		<%-- single test-plan removal --%>
		<%--=========================--%>
		$('#test-suite-test-plans-table .delete-test-suite-test-plan-button').die('click');
		
		//single deletion buttons
		$('#test-suite-test-plans-table .delete-test-suite-test-plan-button').live('click', function() {
			$("#${ testCaseSingleRemovalPopupId }").data('opener', this).dialog('open');
			return false;
		});
		
		$("#${ testCaseSingleRemovalPopupId }").bind('dialogclose', function() {
			var answer = $("#${ testCaseSingleRemovalPopupId }").data("answer");
			if ( (answer != "delete") && (answer != "detach") ) {
				return;
			}
			var bCaller = $.data(this,"opener");
			
			if (answer == "delete") {
				$.ajax({
					type : 'delete',
					url : removeTestPlansUrl+'/delete/' + parseTestPlanId(bCaller),
					dataType : 'text',
					success : function (data){
						refreshTestPlans();
						checkForbiddenDeletion(data);
						refreshStatistics();
						refreshExecButtons();
					}
				});
			}
			
			if (answer == "detach") {
				$.ajax({
					type : 'delete',
					url : removeTestPlansUrl+'/detach/' + parseTestPlanId(bCaller),
					dataType : 'text',
					success : function (data){
						refreshTestPlans();
						checkForbiddenDeletion(data);
						refreshStatistics();
						refreshExecButtons();
					}
				});
			}
		});
		
		//This function checks the response and inform the user if a deletion was impossible
		function checkForbiddenDeletion(data){
			if(data=="true"){
				squashtm.notification.showInfo('${ unauthorizedDeletion }');
			}
		}
		
		<%--=========================--%>
		<%-- multiple test-plan removal --%>
		<%--=========================--%>
		//multiple deletion
		$("#${ testCaseMultipleRemovalPopupId }").bind('dialogclose', function() {
			var answer = $("#${ testCaseMultipleRemovalPopupId }").data("answer");
			if ( (answer != "delete") && (answer != "detach") ) {
				return;
			}
			
			var table = $( '#test-suite-test-plans-table' ).squashTable();
			var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
			
			if (answer == "delete") {
				if (ids.length > 0) {
					$.post(nonBelongingTestPlansUrl+'/delete', { testPlanIds: ids }, function(data){
						refreshTestPlans();
						checkForbiddenDeletion(data);
						refreshStatistics();
						refreshExecButtons();
						});
				}
			}
			if (answer == "detach") {
				if (ids.length > 0) {
					$.post(nonBelongingTestPlansUrl+'/detach', { testPlanIds: ids }, function(data){
						refreshTestPlans();
						checkForbiddenDeletion(data);
						refreshStatistics();
						refreshExecButtons();
						});
				}
			}
		
		});
	});


	<%--=========================--%>
	<%-- new execution Buttons --%>
	<%--=========================--%>
	$(function() {	
		<%-- bind the new execution creation button to their event --%>
		var newExecButtons = $('a.new-exec');
		newExecButtons.die('click');
		newExecButtons.live('click', newExecutionClickHandler);
		var newExecAutoButtons = $('a.new-auto-exec');
		newExecAutoButtons.die('click');
		newExecAutoButtons.live('click', newAutoExecutionClickHandler);
	});
	
	
	function newExecutionClickHandler(){
		var url = $(this).attr('data-new-exec');
		
		$.ajax({type : 'POST', url : url, dataType : "json", data:{"mode":"manual"}})
		.success(function(id){
			document.location.href="${showExecutionUrl}/"+id;
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
	
function bindMenuToExecutionShortCut(row, data){
		
		var tpId = data[0];
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";


		//if the testcase is manual
		if(data[3] === 'M'){
		
			$(".shortcut-exec",row).fgmenu({
				content : $("#shortcut-exec-man",row).html(),
				showSpeed : 0,
				width : 130
			});
	
		//if the testcase is automated		
		} else {
			$(".shortcut-exec",row).click(function(){
			$.ajax({
				type : 'POST',
				url : url,
				data : {"mode":"auto"},
				dataType : "json"
			}).done(function(suiteView){
				refreshTestPlans();
				if(suiteView.executions.length == 0){
					$.squash
					.openMessage("<f:message key='popup.title.Info' />",
							"<f:message	key='dialog.execution.auto.overview.error.none'/>");
				}else{
					squashtm.automatedSuiteOverviewDialog.open(suiteView);
				}
			});
			});
		} 
	}
	
	function launchClassicExe(tpId){
		
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";
	
		var startResumeClassic = function() {
			var url = runnerUrl;
			var data = {
				'optimized' : 'false'
			};
			var winDef = {
				name : "classicExecutionRunner",
				features : "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised"
			};
			$.open(url, data, winDef);
		};

		$.ajax({
			type : 'POST',
			url : url,
			data : {"mode":"manual"},
			dataType : "json"
		}).done(function(id){
			runnerUrl = "${showExecutionUrl}/"+id+"/runner";
			dryRunStart().done(startResumeClassic);
		});
	}
	
	function launchOptimizedExe(tpId){
		
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";

		var startResumeOptimized = function() {
			
			var url = runnerUrl;
			$('body form#start-optimized-form').remove();
			$('body').append('<form id="start-optimized-form" action="'+runnerUrl+'?optimized=true&suitemode=false" method="post" name="execute-test-case-form" target="optimized-execution-runner" class="not-displayed"> <input type="submit" value="true" name="optimized" id="start-optimized-button" /><input type="button" value="false" name="suitemode"  /></form>');
			
			$('#start-optimized-button').trigger('click');
		};
		
		$.ajax({
			type : 'POST',
			url : url,
			data : {"mode":"manual"},
			dataType : "json"
		}).done(function(id){
			runnerUrl = "${showExecutionUrl}/"+id+"/runner";
			dryRunStart().done(startResumeOptimized);
		});
	}
	
	<%--=========================--%>
	<%-- Drag and Drop --%>
	<%--=========================--%>
	//for drag and drop test case feature
	//row : selected row
	//dropPosition : the new position
	function testPlanDropHandler(rows, dropPosition) {
		var itemIds = $(rows).collect(function(elt){return elt.id.split(':')[1];});
		$.post('${ updateTestPlanUrl }/move', { newIndex : dropPosition, itemIds : itemIds }, function() {
			refreshTestPlans();
		});		
	}

	<%--=========================--%>
	<%-- Refresh methods --%>
	<%--=========================--%>
	function refreshTestPlans() {
		var table = $('#test-suite-test-plans-table').squashTable();
		saveTableSelection(table, getTestPlansTableRowId);
		table.refresh();
	}
	
	
	function refreshExecButtons(){
		$('#${ testSuiteExecButtonsId }').load('${ testSuiteExecButtonsUrl }');
	}
	
	function refreshTestPlansWithoutSelection() {
		var table = $('#test-suite-test-plans-table').squashTable();
		table.refresh();
	}


	<%--=========================--%>
	<%-- Table methods  --%>
	<%--=========================--%>
	function testPlanTableDrawCallback() {
		<c:if test="${ editable }">
		enableTableDragAndDrop('test-suite-test-plans-table', getTestPlanTableRowIndex, testPlanDropHandler);
		decorateDeleteButtons($('.delete-test-suite-test-plan-button', this));
		</c:if>
		restoreTableSelection(this, getTestPlansTableRowId);
		convertExecutionStatus(this);
	}

	function getTestPlansTableRowId(rowData) {
		return rowData[0];	
	}
	function getTestPlanTableRowIndex(rowData){
		return rowData[1];
	}
	function isTestCaseDeleted(data){
		return (data[11]=="true");
	}
	
	function addTestSuiteTestPlanItemExecModeIcon(row, data) {
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
		addIdtoTestPlanRow(row, data);
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getTestPlansTableRowId(data), 'delete-test-suite-test-plan-button');
		addClickHandlerToSelectHandle(row, $("#test-suite-test-plans-table"));
		addLoginListToTestPlan(row, data);
		</c:if>
		addHLinkToTestPlanName(row, data);
		addIconToTestPlanName(row, data);
		<c:if test="${executable}">
		addExecuteIconToTestPlan(row, data);
		</c:if>
		addStyleToDeletedTestCaseRows(row, data);
		addTestSuiteTestPlanItemExecModeIcon(row, data);
		return row;
	}
	
	function addIdtoTestPlanRow(nRow, aData){
		$(nRow).attr("id", "test-plan:" + getTestPlansTableRowId(aData));
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
		var url= 'javascript:void(0)';			
		addHLinkToCellText($( 'td:eq(4)', row ), url);
		$('td:eq(4) a', row).addClass('test-case-name-hlink');
	}
	
	function addIconToTestPlanName(row, data){
		$('td:eq(4)', row).prepend('<img src="${pageContext.servletContext.contextPath}/images/arrow_right.gif"/>');	
	}	

	function addExecuteIconToTestPlan(row, data) {
		
		var tpId = data[0];
		
		if(!isTestCaseDeleted(data)){
			$('td:eq(10)', row)
				.prepend('<input type="image" class="shortcut-exec" src="${pageContext.servletContext.contextPath}/images/execute.png"/><div id="shortcut-exec-man" style="display: none"><ul><li><a id="option1-'+tpId+'" href="#" onclick="launchClassicExe('+tpId+')"><f:message key="test-suite.execution.classic.label"/></a></li><li><a id="option2-'+tpId+'" href="#" onclick="launchOptimizedExe('+tpId+')"><f:message key="test-suite.execution.optimized.label"/></a></li></ul></div>');
		} else {
			$('td:eq(10)', row).prepend('<input type="image" class="disabled-shortcut-exec" src="${pageContext.servletContext.contextPath}/images/execute.png"/>');
			//TODO explain why this is done here and not in css file
			$('.disabled-shortcut-exec', row).css('opacity', 0.35);
		}

		bindMenuToExecutionShortCut(row, data);
	}

	function addLoginListToTestPlan(row, data){
		if (! isTestCaseDeleted(data)){
			var id = getTestPlansTableRowId(data);
			$('td:eq(8)', row).load("${assignableUsersUrl}" + "?testPlanId="+ id +"");
		}
	}

	
	function addStyleToDeletedTestCaseRows(row, data){
		if (isTestCaseDeleted(data)){
			$(row).addClass("test-case-deleted");
		}		
	}
	
	
	
	function convertExecutionStatus(dataTable){
		var factory = new squashtm.StatusFactory({
			untestable : "${statusUntestable}",
			blocked : "${statusBlocked}",
			failure : "${statusFailure}",
			success : "${statusSuccess}",
			running : "${statusRunning}",
			ready : "${statusReady}",
			error : "${statusError}",
			warning : "${statusWarning}"
		});
		
		var rows=dataTable.fnGetNodes();
		if (rows.length==0) return;
		
		$(rows).each(function(){
			var col=$("td:eq(7)", this);
			var oldContent=col.html();
			
			var newContent = factory.getHtmlFor(oldContent);	
			
			col.html(newContent);
			
		});		
	}
	

	function toggleExpandIcon(testPlanHyperlink){
		
	
		var table =  $('#test-suite-test-plans-table').squashTable();
		var donnees = table.fnGetData(testPlanHyperlink.parentNode.parentNode);
		var image = $(testPlanHyperlink).parent().find("img");
		var ltr = testPlanHyperlink.parentNode.parentNode;
		
		if (!$(testPlanHyperlink).hasClass("opened"))
		{
			/* the row is closed - open it */
			var nTr = table.fnOpen(ltr, "      ", "");
			var url1="${testPlanExecutionsUrl}" + donnees[0];
			
			
			$(nTr).load(url1);
			if ($(this).parent().parent().hasClass("odd")){
				$(nTr).addClass("odd");
			}
			else {
				$(nTr).addClass("even");
			}
			$(nTr).attr("style","vertical-align:top;");
			
			image.attr("src","${pageContext.servletContext.contextPath}/images/arrow_down.gif");
			
		}
		else
		{
			/* This row is already open - close it */
			table.fnClose( ltr );
			image.attr("src","${pageContext.servletContext.contextPath}/images/arrow_right.gif");
		};
		$(testPlanHyperlink).toggleClass("opened");		
	}
	
	
	$(function(){	

		/*
			could be optimized if we bind that in the datatableDrawCallback.		
		*/
		
		$('#test-suite-test-plans-table tbody td a.test-case-name-hlink').die('click');
		
		<%-- binding the handler managing the collapse/expand test case icon--%>
		$('#test-suite-test-plans-table tbody td a.test-case-name-hlink').live('click', function () {
			toggleExpandIcon(this);
			return false; //return false to prevent navigation in page (# appears at the end of the URL)
		} );
		
	
	});
	
	
</script>

<comp:decorate-ajax-table url="${ tableModelUrl }" tableId="test-suite-test-plans-table" paginate="true" isSquashtable="${true}">
	<jsp:attribute name="drawCallback">testPlanTableDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">testPlanTableRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" />
		<dt:column-definition targets="1" sortable="false" cssClass="centered ui-state-default drag-handle select-handle" />
		<dt:column-definition targets="2" sortable="false" />
		<dt:column-definition targets="3" sortable="false" cssClass="exec-mode" width="2em" />
		<dt:column-definition targets="4, 5, 6, 7" sortable="false" />
		<dt:column-definition targets="8" sortable="false" cssClass="has-status"/>
		<dt:column-definition targets="9, 10" sortable="false" width="12em"/>
		<dt:column-definition targets="11" sortable="false" visible="false" />
		<dt:column-definition targets="12" sortable="false" width="2em" cssClass="centered"/>
		<dt:column-definition targets="13" sortable="false" width="2em" lastDef="true" cssClass="centered"/>
	</jsp:attribute>
</comp:decorate-ajax-table>