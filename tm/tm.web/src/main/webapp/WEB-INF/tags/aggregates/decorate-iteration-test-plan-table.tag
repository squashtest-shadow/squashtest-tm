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
<%@ attribute name="executable" type="java.lang.Boolean" 
	description="Permission to run the execution"%>
<%@ attribute name="assignableUsersUrl" required="true"
	description="URL to manipulate user of the test-plans"%>
<%@ attribute name="assignableStatusUrl" required="true"
	description="URL to manipulate status of the test-plan items"%>
<%@ attribute name="testCaseSingleRemovalPopupId" required="true"
	description="html id of the single test-case removal popup"%>
<%@ attribute name="testCaseMultipleRemovalPopupId" required="true"
	description="html id of the multiple test-case removal popup"%>
<%@ attribute name="baseIterationURL" description="the base iteration url" %>
<%@ attribute name="assignableUsers" type="java.lang.Object" description="a map of users paired by id -> login. The id must be a string."%>
<%@ attribute name="iteration" type="java.lang.Object" description="the instance of iteration" %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<s:url var="showExecutionUrl" value="/executions" />
<s:url var="dtMessagesUrl" value="/datatables/messages" />

<f:message var="cannotCreateExecutionException" key="squashtm.action.exception.cannotcreateexecution.label" />
<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message" />
<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel"  key="label.Cancel"/>

<c:if test="${editable}">
	<c:set var="deleteBtnClause" value=", delete-button=#iter-test-plan-delete-row-dialog"/>
</c:if>
<table id="test-plans-table" data-def="language=${dtMessagesUrl}, ajaxsource=${tableModelUrl}, hover"  >
	<thead>
		<tr>
			<th data-def="map=entity-index, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
			<th data-def="map=project-name, sortable"><f:message key="label.project" /></th>
			<th data-def="map=exec-mode, sortable, narrow, sClass=exec-mode">&nbsp;</th><%-- exec mode icon --%>
			<th data-def="map=reference, sortable"><f:message key="label.Reference"/></th>
			<th data-def="map=tc-name, sortable, sClass=toggle-row"><f:message key="iteration.executions.table.column-header.test-case.label" /></th>
			<th data-def="map=importance, sortable"><f:message key="iteration.executions.table.column-header.importance.label" /></th>
			<th data-def="map=dataset, sWidth=10%"><f:message key="label.Dataset" /></th>
			<th data-def="map=suite, sWidth=10%"><f:message key="iteration.executions.table.column-header.suite.label" /></th>
			<th data-def="map=status, sortable, sWidth=10%, sClass=has-status status-combo"><f:message key="iteration.executions.table.column-header.status.label" /></th>
			<th data-def="map=assignee-login, sortable, sWidth=10%, sClass=assignee-combo"><f:message key="iteration.executions.table.column-header.user.label" /></th>
			<th data-def="map=last-exec-on, sortable, sWidth=10%"><f:message key="iteration.executions.table.column-header.execution-date.label" /></th>
			<th data-def="map=empty-execute-holder, narrow, center, sClass=execute-button">&nbsp;</th>	
			<th data-def="map=empty-delete-holder, ${deleteBtnClause}">&nbsp;</th>				
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>



<div id="iter-test-plan-delete-row-dialog" class="not-displayed popup-dialog" title="<f:message key="test-case.verified_requirement_item.remove.button.label" />">
	<span style="font-weight:bold;"><f:message key="dialog.remove-testcase-association.message" /></span>
	<div class="popup-dialog-buttonpane"> 
		<input type="button" value="${confirmLabel}"/> 
		<input type="button" value="${cancelLabel}"/> 
	</div>
</div>

<div id="iter-test-plan-delete-execution-dialog" class="not-displayed popup-dialog" title="<f:message key="dialog.delete-execution.title" />">
	<span style="font-weight:bold;"><f:message key="dialog.delete-execution.message" /></span>
	<div class="popup-dialog-buttonpane"> 
		<input type="button" value="${confirmLabel}"/> 
		<input type="button" value="${cancelLabel}"/> 
	</div>
</div>

<%--
	TODO : that could use some refactoring too.
 --%>

<script type="text/javascript">
	/*
	var testPlansUrl = "${testPlansUrl}";
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

	function bindMenuToExecutionShortCut(row, data){
		
		var tpId = data['entity-id'];
		var url = "${baseIterationURL}/test-plan/"+tpId+"/executions/new";


		//if the testcase is manual
		if(data['exec-mode'] === 'M'){
		
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
		
		var url = "${baseIterationURL}/test-plan/"+tpId+"/executions/new";
	
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
		
		var url = "${baseIterationURL}/test-plan/"+tpId+"/executions/new";

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
	function getCurrentStatus(data) {
		return data['status'];
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
		
		<c:if test="${executable}">
		addExecuteIconToTestPlan(row, data);
		</c:if>
		
		addStyleToDeletedTestCaseRows(row, data);
		addIterationTestPlanItemExecModeIcon(row, data);
		selectCurrentStatus(row,data);
		return row;
	}
	
	function testPlanDrawCallback(){
		
		var table = $("#test-plans-table").squashTable();
	
		<c:if test="${ editable }">
		addLoginListToTestPlan();
		addStatusListToTestPlan();		
		</c:if>		
		
		bindToggleExpandIcon(table);
	}
	
	function selectCurrentStatus(row,data) {
		
		var status = getCurrentStatus(data);
		$("#statuses option:contains('"+status+"')").attr("selected","selected");
		$(".status-list",row).parent().addClass("exec-status-"+$(".status-list",row).val());
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
	
	function addExecuteIconToTestPlan(row, data) {
		
		var tpId = data['entity-id'];
		
		if(!isTestCaseDeleted(data)){
			$('td:eq(11)', row)
				.prepend('<input type="image" class="shortcut-exec"  src="${pageContext.servletContext.contextPath}/images/execute.png"/><div id="shortcut-exec-man" style="display: none"><ul><li><a id="option1-'+tpId+'" href="#" onclick="launchClassicExe('+tpId+')"><f:message key="test-suite.execution.classic.label"/></a></li><li><a id="option2-'+tpId+'" href="#" onclick="launchOptimizedExe('+tpId+')"><f:message key="test-suite.execution.optimized.label"/></a></li></ul></div>');
		} else {
			$('td:eq(11)', row).prepend('<input type="image" class="disabled-shortcut-exec" src="${pageContext.servletContext.contextPath}/images/execute.png"/>');
			//TODO explain why this is done here and not in css file
			$('.disabled-shortcut-exec', row).css('opacity', 0.35);
		}

		bindMenuToExecutionShortCut(row, data);
	}
	
	<c:if test="${ editable }">
	
	function addStatusListToTestPlan(){
		
		var table = $("#test-plans-table").squashTable();
		
		$.get("${assignableStatusUrl}", "json")
		.success(function(json){
			table.data('status-list', json);
			table.$('td.status-combo').statusCombo();
		});
	}

	//because of IE8 naturally trimming text nodes we will trim
	//every string we must compare.
	$.fn.statusCombo = function(){
		
		if (this.length==0) return;
		var squashTable=$("#test-plans-table").squashTable();
		var assignableList = squashTable.data('status-list');
		if (! assignableList) return;
		
		//create the template
		var template=$('<select class="status-list"/>');
		for (var i=0;i<assignableList.length;i++){
			var opt = '<option class="exec-status-'+assignableList[i].name+'" value="'+assignableList[i].name+'">'+assignableList[i].internationalizedName+'</option>';
			template.append(opt);
		}
			
		template.change(function(){
			
			var self = $(this);
			$.ajax({
				type : 'POST',
				url : this.getAttribute('data-assign-url'),
				data : "statusName=" + this.value,
				dataType : 'json'
			}).done(function(data){
				self.parent().removeClass();
				self.parent().addClass("has-status status-combo exec-status-"+self.val());
			});
		});
			
		this.each(function(){
			
			var cloneSelect = template.clone(true);
			
			var jqTd = $(this);
			var row = this.parentNode;
			
			var status = $("td.status-combo span").html();
				
			//sets the change url
			var tpId = squashTable.getODataId(row);
			var dataUrl = "${baseIterationURL}/test-case/"+tpId+"/assign-status";
			
			cloneSelect.attr('data-assign-url', dataUrl);
					
			//append the content
			jqTd.empty().append(cloneSelect);
			$(".status-list option:contains('"+status+"')", row).attr("selected","selected");
			$(".status-list",row).parent().addClass("exec-status-"+$(".status-list",row).val());
		});	
	}
	
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
		var squashTable=$("#test-plans-table").squashTable();
		var assignableList = squashTable.data('assignable-list');
		if (! assignableList) return;
		
		this.each(function(){
			
			var jqTd = $(this);
			var row = this.parentNode;
			
			
			
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
			
			var cloneSelect = template.clone(true);
			
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
			
			// the row is closed - open it 
			var nTr = table.fnOpen(ltr, "      ", "");
			var url1 = "${testPlanExecutionsUrl}" + data['entity-id'];
			var jqnTr = $(nTr);
			
			var rowClass = ($(this).parent().parent().hasClass("odd")) ? "odd" : "even";
			jqnTr.addClass(rowClass);

			jqnTr.attr("style", "vertical-align:top;");
			image.attr("src", "${pageContext.servletContext.contextPath}/images/arrow_down.gif");
			

			jqnTr.load(url1, function(){				
				<c:if test="${ executable }">
				//apply the post processing on the content
				expandedRowCallback(jqnTr);
				</c:if>
			});

		} else {
			// This row is already open - close it 
			table.fnClose(ltr);
			image.attr("src",
							"${pageContext.servletContext.contextPath}/images/arrow_right.gif");
		};
		$(testPlanHyperlink).toggleClass("opened");
	}
	
	function bindToggleExpandIcon(table){
		$('tbody td a.test-case-name-hlink', table).bind('click', function() {
			toggleExpandIcon(this);
			return false; //return false to prevent navigation in page (# appears at the end of the URL)
		});		
	}
	*/
	
	/* ***************************** expanded line post processing *************** */

<c:if test="${ executable }">
	
	function expandedRowCallback(jqnTr) {
		initDeleteButtonsToFunctions(jqnTr);
		initNewExecButtons(jqnTr);
	};
	
	
	
	function initNewExecButtons(jqnTr){		
		var newExecButton = $('a.new-exec', jqnTr);
		newExecButton.button().on('click', newExecutionClickHandler);
		var newExecAutoButton = $('a.new-auto-exec', jqnTr);
		newExecAutoButton.button().on('click', newAutoExecutionClickHandler);		
	}
	

	function initDeleteButtonsToFunctions(jqnTr) {
		
		decorateDeleteButtons($(".delete-execution-table-button", jqnTr));
		
		var execOffset = "delete-execution-table-button-";
		
		$(".delete-execution-table-button", jqnTr)
		.click(function() {
			//console.log("delete execution #"+idExec);
			var execId = $(this).attr("id");
			var idExec = execId.substring(execOffset.length);
			var execRow = $(this).closest("tr");
			var testPlanHyperlink = $(this).closest("tr")
										   .closest("tr")
										   .prev()
										   .find("a.test-case-name-hlink");

			confirmeDeleteExecution(idExec, testPlanHyperlink, execRow);
		});

	}

	function confirmeDeleteExecution(idExec, testPlanHyperlink, execRow) {
		oneShotConfirm("<f:message key='dialog.delete-execution.title'/>",
				"<f:message key='dialog.delete-execution.message'/>",
				"<f:message key='label.Confirm'/>",
				"<f:message key='label.Cancel'/>").done(
				function() {
					$.ajax({
						'url' : "${showExecutionUrl}/" + idExec,
						type : 'DELETE',
						data : [],
						dataType : "json"
					}).done(function(data){
						refreshTable(testPlanHyperlink, execRow, data);
					});
				});
	}

	
	function refreshTable(testPlanHyperlink, execRow, data) {
		refreshTestPlans();		
		refreshIterationInfos();
		refreshStatistics();
		actualStart.refreshAutoDate(data.newStartDate);
		actualEnd.refreshAutoDate(data.newEndDate);

	}
	
</c:if>

	$(function() {
		
		/* ************************** various event handlers ******************* */

		<%--=========================--%>
		<%-- multiple test-plan removal --%>
		<%--=========================--%>
		//multiple deletion
		$("#${ testCaseMultipleRemovalPopupId }").bind('dialogclose',function() {
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

		/* ************************** datatable settings ********************* */
		
		require(['iteration-management'], function(iterInit){
			var conf = {
				permissions : {
					editable : ${editable},
					executable : ${executable}
				},
				basic : {
					iterationId : ${iteration.id},
					assignableUsers : ${ json:serialize(assignableUsers) }
				}
			};
			
			iterInit.initTestPlanTable(conf);
		});
					
			
	});
</script>

