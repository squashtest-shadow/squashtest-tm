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
<%@ tag body-content="empty" description="jqueryfies a campaign test case table" %>

<%@ attribute name="testSuite" required="true" type="java.lang.Object"  description="the base iteration url" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="executable" type="java.lang.Boolean" description="Right to execute. Default to false." %>


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>


<%-- =============== URLs and other variables ======================== --%>

<c:set var="testCaseSingleRemovalPopupId" value="delete-test-suite-single-test-plan-dialog" />
<c:set var="testCaseMultipleRemovalPopupId" value="delete-test-suite-multiple-test-plan-dialog" />


<s:url var="tableModelUrl"	value="/test-suites/{testSuiteId}/test-plan/table">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="removeTestPlansUrl" value="/test-suites/{testSuiteId}/{iterationId}/test-plan">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var ="showExecutionUrl" value="/executions"/>

<s:url var="testPlanExecutionsUrl" value="/test-suites/{testSuiteId}/{iterationId}/test-case-executions/">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>


<s:url var="updateTestPlanUrl"	value="/test-suites/{testSuiteId}/test-plan">
	<s:param name="testSuiteId" value="${testSuite.id}" />
</s:url>

<s:url var="assignableStatusUrl" value="/iterations/{iterId}/assignable-statuses">
	<s:param name="iterId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="assignableUsersUrl" value="/test-suites/{testSuiteId}/{iterationId}/assignable-user">
	<s:param name="testSuiteId" value="${testSuite.id}" />
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<s:url var="baseIterationUrl" value="/iterations/{iterationId}">
	<s:param name="iterationId" value="${testSuite.iteration.id}" />
</s:url>

<c:url var="tableLanguageUrl" value='/datatables/messages' />

<%-- =============== /URLs and other variables ======================== --%>


<%-- ================== regionale and al. ============== --%>

<f:message var="statusUntestable" key="execution.execution-status.UNTESTABLE" />
<f:message var="statusBlocked" key="execution.execution-status.BLOCKED" />
<f:message var="statusFailure" key="execution.execution-status.FAILURE" />
<f:message var="statusSuccess" key="execution.execution-status.SUCCESS" />
<f:message var="statusRunning" key="execution.execution-status.RUNNING" />
<f:message var="statusReady" key="execution.execution-status.READY" />
<f:message var="statusError" key="execution.execution-status.ERROR" />
<f:message var="statusWarning" key="execution.execution-status.WARNING" />

<f:message var="cannotCreateExecutionException" key="squashtm.action.exception.cannotcreateexecution.label" />
<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />


<%-- ================== /regionale and al. ============== --%>



<table id="test-suite-test-plans-table" data-def="ajaxsource=${tableModelUrl}, language=${tableLanguageUrl}, hover, pagesize=10" >
	<thead>
		<tr>
			<th data-def="map=entity-index, sClass=drag-handle ui-state-default, select, center, narrow">#</th>
			<th data-def="map=project-name"><f:message key="label.project" /></th>
			<th data-def="map=exec-mode, narrow, sClass=exec-mode">&nbsp;</th><%-- exec mode icon --%>		
			<th data-def="map=reference"><f:message key="label.Reference"/></th>	
			<th data-def="map=tc-name, link=javascript:void(0), sClass=test-case-name-hlink"><f:message key="iteration.executions.table.column-header.test-case.label" /></th>
			<th data-def="map=importance"><f:message key="iteration.executions.table.column-header.importance.label" /></th>
      <th data-def="map=dataset"><f:message key="label.Dataset" /></th>
			<th data-def="map=status, sWidth=10%, sClass=has-status status-combo"><f:message key="iteration.executions.table.column-header.status.label" /></th>
			<th data-def="map=last-exec-by, sClass=executed-by"><f:message key="iteration.executions.table.column-header.user.label" /></th>
			<th data-def="map=last-exec-on"><f:message key="iteration.executions.table.column-header.execution-date.label" /></th>
			<th data-def="map=empty-execute-holder, center, narrow, sClass=execute-button">&nbsp;</th>		
			<th data-def="map=empty-delete-holder, center, narrow, sClass=delete-button, delete-button=#${testCaseSingleRemovalPopupId}">&nbsp;</th>			
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>

<div id="test-suite-test-plan-table-templates" style="display:none">
	
	<div class="shortcut-exec execute-arrow cursor-pointer" style="height : 20px; width : 20px;"></div>
	<div class="disabled-shortcut-exec execute-arrow cursor-pointer" style="height : 20px; width : 20px; opacity : 0.35"></div>
	
	<div id="shortcut-exec-man-template">
		<ul>
			<li><a data-tpid="{placeholder-tpid}" class="run-menu-item run-popup" href="javascript:void(0)"><f:message key="test-suite.execution.classic.label"/></a></li>
			<li><a data-tpid="{placeholder-tpid}" class="run-menu-item run-oer" href="javascript:void(0)"><f:message key="test-suite.execution.optimized.label"/></a></li>
		</ul>
	</div>
	
</div>
		


<script type="text/javascript">


$(function() {
	
	/* **************************************************************
		Library code.
		
		Should be moved to an external js when we have time for the plumbing
	
		Note : refreshTestSuiteView is defined in an outer context
	**************************************************************** */
	
	// ************************** start execution management *****************
	
	
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
			$("#test-suite-test-plans-table").squashTable().refresh();
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

	
	
	function runInPopup(){
		var url = runnerUrl;
		var data = {
			'optimized' : 'false'
		};
		var winDef = {
			name : "classicExecutionRunner",
			features : "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised"
		};
		$.open(url, data, winDef);		
	}
	
	function runInOER (){
		
		var url = runnerUrl;
		$('body form#start-optimized-form').remove();
		$('body').append('<form id="start-optimized-form" action="'+runnerUrl+'?optimized=true&suitemode=false" method="post" name="execute-test-case-form" target="optimized-execution-runner" class="not-displayed"> <input type="submit" value="true" name="optimized" id="start-optimized-button" /><input type="button" value="false" name="suitemode"  /></form>');
		
		$('#start-optimized-button').trigger('click');
	};
		
	function startManualExecution(tpId, executionUI){
		
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";

		var callback = (executionUI === "popup") ? runInPopup : runInOER;

		$.ajax({
			type : 'POST',
			url : url,
			data : {"mode":"manual"},
			dataType : "json"
		}).done(function(id){
			runnerUrl = "${showExecutionUrl}/"+id+"/runner";
			dryRunStart().done(callback);
		});
	}
	
	function execMenuManualClickHandler(){
		var jqThis = $(this);
		var id = jqThis.data('tpid');
		var ui = (jqThis.is('.run-popup') )? "popup" : "oer";
		
		startManualExecution(id,ui);
	}
	
	function execMenuAutoClickHandler(){
		
		var row = $(this).parents('tr');
		
		var table = $("#test-suite-test-plans-table").squashTable();
		var data = table.fnGetData(row);
		var tpId = data['entity-id'];
		var url = "${baseIterationUrl}/test-plan/"+tpId+"/executions/new";
		
		$.ajax({
			type : 'POST',
			url : url,
			data : {"mode":"auto"},
			dataType : "json"
		}).done(function(suiteView){
			 $("#test-suite-test-plans-table").squashTable().refresh();
			if(suiteView.executions.length == 0){
				$.squash.openMessage("<f:message key='popup.title.Info' />", "<f:message	key='dialog.execution.auto.overview.error.none'/>");
			}else{
				squashtm.automatedSuiteOverviewDialog.open(suiteView);
			}
		});
	}
	
	/* ************************** table rendering and binding ************** */
	
	
	function testPlanTableDrawCallback() {
		var table = $("#test-suite-test-plans-table").squashTable();
		bindToggleExpandIcon(table);
		<c:if test="${ editable }">
		addStatusListToTestPlan(table);	
		</c:if>
	}	

	function testPlanTableRowCallback(row, data, displayIndex) {
		<c:if test="${ editable }">
		addLoginListToTestPlan(row, data);
		selectCurrentStatus(row,data);
		</c:if>
		<c:if test="${executable}">
		addExecuteIconToTestPlan(row, data);
		</c:if>
		addTestSuiteTestPlanItemExecModeIcon(row, data);
		applyOtherStyles(row, data);
		return row;
	}

	

	function bindMenuToExecutionShortCut(row, data){
		
		var strtpid = data['entity-id']+"";	//cast as string
		
		//if the testcase is manual
		if(data['exec-mode'] === 'M'){
			
			var menu = $(".shortcut-exec",row);
		
			menu.fgmenu({
				content : $("#shortcut-exec-man-template").html().replace(/{placeholder-tpid}/g, strtpid),
				showSpeed : 0,
				width : 130
			});
			
			//the click handler below is a simple hack for css
			menu.click(function(){
				menu.removeClass('ui-state-active');
			});
			

			//bind the items. In the very strange fgmenu way.
			var instance = allUIMenus[allUIMenus.length - 1];
			instance.chooseItem = function(item){
				instance.kill();
				execMenuManualClickHandler.call(item);
			}
			
	
		//if the testcase is automated		
		} else {
			$(".shortcut-exec",row).click(execMenuAutoClickHandler);
		} 
	}
	
	function applyOtherStyles(row, data){		
		//deleted test cases 
		if (data["is-tc-deleted"]){
			$(row).addClass("test-case-deleted");
		}
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



	function addExecuteIconToTestPlan(row, data) {
		
		var tpId = data["entity-id"];
		var td=$('td.execute-button', row);
		
		var iconSelector = (data['is-tc-deleted']===false) ? ".shortcut-exec" : ".disabled-shortcut-exec";
		
		var icon = $("#test-suite-test-plan-table-templates "+iconSelector).clone()
		td.prepend(icon);		

		bindMenuToExecutionShortCut(row, data);
	}

	function addLoginListToTestPlan(row, data){
		if (! data['is-tc-deleted']){
			var id =data['entity-id'];
			$('td.executed-by', row).load("${assignableUsersUrl}" + "?testPlanId="+ id +"");
		}
	}
	
	function addStatusListToTestPlan(table){
		$.get("${assignableStatusUrl}", "json")
		.success(function(json){
			table.data('status-list', json);
			table.$('td.status-combo').statusCombo(table);
		});
	}
	
	$.fn.statusCombo = function(table){
		
		if (this.length==0) return;
		var squashTable=$("#test-suite-test-plan-table-templates").squashTable();
		var assignableList = table.data('status-list');
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
			var tpId = table.getODataId(row);
			var dataUrl = "${baseIterationUrl}/test-case/"+tpId+"/assign-status";
			
			cloneSelect.attr('data-assign-url', dataUrl);
					
			//append the content
			jqTd.empty().append(cloneSelect);
			$(".status-list option:contains('"+status+"')", row).attr("selected","selected");
			$(".status-list",row).parent().addClass("exec-status-"+$(".status-list",row).val());
		});	
	}
	
	function getCurrentStatus(data) {
		return data['status'];
	}
	
	function selectCurrentStatus(row,data) {
		
		var status = getCurrentStatus(data);
		$("#statuses option:contains('"+status+"')").attr("selected","selected");
		$(".status-list",row).parent().addClass("exec-status-"+$(".status-list",row).val());
	}
	
	function bindToggleExpandIcon(table){
		$("td.test-case-name-hlink")
			.prepend('<div style="display:inline-block" class="small-arrow small-right-arrow" />')
			.on('click', 'a', function() {
				toggleExpandIcon(this);
				return false; //return false to prevent navigation in page (# appears at the end of the URL)
			});		
	}

	
	/* ***************************** expanded line post processing *************** */

	function toggleExpandIcon(testPlanHyperlink){
		var table =  $('#test-suite-test-plans-table').squashTable();
		var jqHplk = $(testPlanHyperlink);
		var ltr = jqHplk.parents('tr').get(0);
		var image = jqHplk.prev();
		
		var data = table.fnGetData(ltr);
		
		if (! jqHplk.hasClass("opened"))
		{
			/* the row is closed - open it */
			var nTr = table.fnOpen(ltr, "      ", "");
			var url1 = "${testPlanExecutionsUrl}" + data["entity-id"];
			var jqnTr = $(nTr);
			
			var rowClass = ($(ltr).hasClass("odd")) ? "odd" : "even";
			jqnTr.addClass(rowClass);

			jqnTr.attr("style", "vertical-align:top;");
			image.removeClass('small-right-arrow').addClass('small-down-arrow');
			

			jqnTr.load(url1, function(){				
				<c:if test="${ executable }">
				//apply the post processing on the content
				expandedRowCallback(jqnTr);
				</c:if>
			});
			
		}
		else
		{
			/* This row is already open - close it */
			table.fnClose( ltr );
			image.removeClass('small-down-arrow').addClass('small-right-arrow');
		};
		jqHplk.toggleClass("opened");		
	}
	
	

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

			confirmeDeleteExecution(idExec);
		});

	}

	function confirmeDeleteExecution(idExec) {
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
					}).done(refreshTestSuiteView);
				});
	}

</c:if>
	
	
	
	
	/* ************************** various event handlers ******************* */
	
	//This function checks the response and inform the user if a deletion was impossible
	function checkForbiddenDeletion(data){
		if(data=="true"){
			squashtm.notification.showInfo('${ unauthorizedDeletion }');
		}
	}


	function postItemRemoval(removalType){
		var selectedIds = $("#test-suite-test-plans-table").squashTable().getSelectedIds().join(',');
		var url = "${removeTestPlansUrl}/"+selectedIds+"/"+removalType;
		
		$.ajax({
			type : 'POST',
			url : url,
			dataType : 'text'
		}).success(function(data){
			checkForbiddenDeletion(data);
			refreshTestSuiteView();
		});
	} 
	
	// *********************** deletion popups ***********************
	
	$("#${ testCaseSingleRemovalPopupId }").bind('dialogclose', function() {
			var answer = $("#${ testCaseSingleRemovalPopupId }").data("answer");
			if ( (answer != "delete") && (answer != "detach") ) {
				return; //should throw an exception instead. Should not happen anyway.
			}
			
			postItemRemoval(answer);
			
		});
	
	//multiple deletion
	$("#${ testCaseMultipleRemovalPopupId }").bind('dialogclose', function() {
			var answer = $("#${ testCaseMultipleRemovalPopupId }").data("answer");
			if ( (answer != "delete") && (answer != "detach") ) {
				return;
			}

			postItemRemoval(answer);
		
		});

	/* ************************** datatable settings ********************* */
	
	var tableSettings = {
			"fnRowCallback" : testPlanTableRowCallback,
			"fnDrawCallback" : testPlanTableDrawCallback
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
			}
			
		};
		
		<c:if test="${editable}">
		squashSettings.enableDnD = true;
			
		squashSettings.functions = {
			dropHandler : function(dropData){
				var itemIds = dropData.itemIds;
				var newIndex = dropData.newIndex;
				var url = "${updateTestPlanUrl}/"+itemIds.join(',')+"/position/"+newIndex;
				$.post(url,function(){
					$("#test-suite-test-plans-table").squashTable().refresh();
				});
			}
		};
		


		</c:if>
				
		$("#test-suite-test-plans-table").squashTable(tableSettings, squashSettings);
});

</script>