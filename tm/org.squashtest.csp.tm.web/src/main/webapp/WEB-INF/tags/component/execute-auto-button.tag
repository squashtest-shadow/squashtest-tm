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
<%@ tag
	description="general information panel for an auditable entity. Client can add more info in the body of this tag"
	pageEncoding="utf-8"%>
<%@ attribute name="testPlanTableId" required="true"%>
<%@ attribute name="url" required="true"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<s:url var="automatedSuitesUrl" value="/automated-suites">
</s:url>

<!-- *************************INITIALISATION*********************** -->
<script type="text/javascript">
		
		var autoUpdate;
		
		function updateExecutionInfo() {
			var suiteId = $("#executions-auto-infos").attr('suiteId');
			$.ajax({
				type : 'GET', 
				url : "${automatedSuitesUrl}/"+suiteId+"/executions", 
				dataType : "json"
			}).done(
				function(suiteView){
					var executions = suiteView.executions;
					for(i=0;i<executions.length;i++){
						var execution = executions[i];
						var execInfo = $("#execution-info-"+execution.id);
						var newExecStatus =  $("#execution-info-template .executionStatus").clone();
						var execStatus = execInfo.find(".executionStatus");
						newExecStatus.text(execution.localizedStatus);						
						newExecStatus.addClass('executions-status-'+execution.status.toLowerCase()+'-icon');
						execStatus.replaceWith(newExecStatus);
						
					}
					updateProgress(suiteView)
					if(suiteView.percentage == 100) {
						clearInterval(autoUpdate);
					}
			});
		}
		
	function executeAll() {
		var ids = [];
		executeAuto(ids);

	}

	function executeSelection() {
		var table = $('#${testPlanTableId}').dataTable();
		var ids = getIdsOfSelectedTableRows(table, getTestPlansTableRowId);
		if (ids.length == 0) {
			$.squash
					.openMessage("<f:message key='popup.title.error' />",
							"<f:message	key='dialog.assign-user.selection.empty.label'/>");
		} else {
			executeAuto(ids);
		}
	}
	function executeAuto(ids) {
		$.ajax({
			type : 'POST',
			url : "${url}",
			dataType : "json",
			data : {
				"id" : "execute-auto",
				"testPlanItemsIds" : ids
			}
		}).done(function(suiteView) {
			if(suiteView.executions.length == 0){
				$.squash
				.openMessage("<f:message key='popup.title.Info' />",
						"<f:message	key='dialog.execution.auto.overview.error.none'/>");
			}else{
			updateProgress(suiteView);
			openOverviewDialog(suiteView);
			}
		});
	}
	function updateProgress(suiteView) {
		var executions = suiteView.executions;
		var progress = suiteView.percentage;
		var executionTerminated = progress/100*executions.length
		$("#execution-auto-progress-bar").progressbar("value", progress);
		$("#execution-auto-progress-amount").text(executionTerminated+"/"+executions.length);
	}
	function openOverviewDialog(suiteView) {
		var executionAutoInfos = $("#executions-auto-infos");
		executionAutoInfos.attr('suiteId', suiteView.suiteId);
		var executions = suiteView.executions;
		var template = $("#execution-info-template .display-table-row").clone()
		for (i = 0; i < executions.length; i++) {
			var execution = executions[i];
			var executionHtml = template.clone();
			executionHtml.attr('id', "execution-info"+execution.id);
			executionHtml.find(".executionName").html(execution.name);
			var executionStatus = executionHtml.find(".executionStatus");
			executionStatus.html(execution.localizedStatus);
			executionStatus.addClass('executions-status-'+execution.status.toLowerCase()+'-icon');
			executionAutoInfos.append(executionHtml);
		}
		$("#execute-auto-dialog").dialog('open');
		$("#execute-auto-dialog").bind( "dialogclose", function(event, ui) {
			clearInterval(autoUpdate);
			executionAutoInfos.empty();
			$("#execution-auto-progress-bar").progressbar("value", 0);
			$("#execution-auto-progress-amount").text(0+"/"+0);
		});
		if(suiteView.percentage < 100){
			autoUpdate = setInterval(function() {
					updateExecutionInfo();
					}, 2000);
		}
	}
</script>

<div id="execution-info-template" style="display: hidden">
	<div class="display-table-row">
		<div class="executionName display-table-cell"></div>
		<div class="display-table-cell"><span class="executionStatus common-status-label"></span></div>
	</div>
</div>

<!-- *************************/INITIALISATION*********************** -->
<!-- *************************BUTTON*********************** -->
<div id="iteration-suite-auto-execution-button"
	style="display: inline-block;">


	<a tabindex="0" href="#execute-auto" class="button run-menu"
		id="execute-auto-button" class="button"><f:message
			key="iteration.suite.execution.auto.label" /> </a>
	<div id="execute-auto" style="display: none">
		<ul>
			<li><a class="execute-all" href="javascript:void(0)"><f:message
						key="iteration.suite.execution.auto.all.label" /> </a></li>
			<li><a class="execute-selection" href="javascript:void(0)"><f:message
						key='iteration.suite.execution.auto.selection.label' /> </a></li>
		</ul>
	</div>

	<script>
		$(function() {
			$("#execute-auto-button").fgmenu({
				content : $('#execute-auto-button').next().html(),
				showSpeed : 0,
				width : 130
			});

			var executeAutoMenu = allUIMenus[allUIMenus.length - 1];

			executeAutoMenu.chooseItem = function(item) {

				if ($(item).hasClass('execute-all')) {
					executeAll();
				} else {
					if ($(item).hasClass('execute-selection')) {
						executeSelection();
					}
				}
			};
		});
	</script>
</div>
<!-- *************************/BUTTON*********************** -->
<!-- *************************POPUP*********************** -->
<pop:popup id="execute-auto-dialog" titleKey="dialog.execute-auto.title"
	isContextual="true" closeOnSuccess="false">
	<jsp:attribute name="buttons">
			
				<f:message var="label" key="CLOSE" />
				'${ label }': function() {
					$( this ).dialog( 'close' );
				}		
				
			</jsp:attribute>
	<jsp:attribute name="body">
			<div>
				<div style="max-height: 200px; width: 100%; overflow-y: auto"
				id="executions-auto-infos" suiteId="0" class="display-table">
				</div>
				<div id="execution-auto-progress"
				style="width: 60%; margin: auto; margin-top: 40px">
					<div
					style="width: 70%; display: inline-block; vertical-align: middle">
					<div id="execution-auto-progress-bar"></div>
				</div>
	 				<div id="execution-auto-progress-amount"
					style="width: 20%; display: inline-block"></div>
				</div>
				<div class="popup-notification">
				<f:message key="dialog.execute-auto.close.note" />
				</div>
				</div>
				<script>
				$("#execution-auto-progress-bar").progressbar({
					value : 0
				});
				</script>
				
			</jsp:attribute>
</pop:popup>
<!-- *************************/POPUP*********************** -->