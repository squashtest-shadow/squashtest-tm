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
<%@ tag
	description="general information panel for an auditable entity. Client can add more info in the body of this tag"
	body-content="scriptless"%>
<%@ attribute name="statisticsEntity" required="true"
	type="java.lang.Object"
	description="The entity which general information we want to show"%>
<%@ attribute name="testSuiteId" required="true"
	description="The id of the test-suite"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags/input"%>

<div id="test-suite-execution-button" style="display: inline-block;">
	<c:url var='runnerUrl' value='/test-suites/${ testSuiteId }/test-plan/execution/runner' />
	<c:url var='testRunnerUrl' value='/test-suites/${ testSuiteId }/test-plan/execution/test-runner' />
	<c:url var='deleteOnRestartUrl' value='/test-suites/${ testSuiteId }/test-plan/executions' />
	<script type="text/javascript">
		function classicExecution(mode) {
			var url = "${ runnerUrl }";
			var data = {
				'optimized' : 'false',
				'mode' : mode
			};
			var winDef = {
				name : "classicExecutionRunner",
				features : "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised"
			};
			$.open(url, data, winDef);

		}
		function checkTestSuiteExecutionDoable() {
			return $.ajax({
				type : 'post',
				data : { 'mode' : 'start-resume' },
				dataType : "json",
				url : "${ testRunnerUrl }"
			});
		}
		
		function startResumeClassic(jqXHR) {
			//I shouldn't have to do this, I know, they made me .. :( 
			// seriously : don't know why but the ajax.done() method above is called even when the check fails (observed with FF 10)
			// therefore i have to check for jqXHR to be null to make sure the method is not called after a fail
			if (jqXHR == null) {
				classicExecution('start-resume');
			}

		}
		function startResumeOptimized(jqXHR) {
			//same as comment above
			if (jqXHR == null) {
				$('#start-optimized-button').trigger('click');
			}
		}
	</script>
	<c:if test="${ statisticsEntity.status == 'READY' }">
		<f:message var='startResumeLabel' key='test-suite.execution.start.label' />
	</c:if>
	<c:if test="${ statisticsEntity.status == 'RUNNING' }">
		<f:message var='startResumeLabel' key='test-suite.execution.resume.label' />
	</c:if>

	<c:if
		test="${ statisticsEntity.status == 'RUNNING' || statisticsEntity.status == 'READY'}">
		<input type="button" id="start-resume-button" class="button run-menu" value="${startResumeLabel}"/>
		<div id="start" style="display: none">
			<ul>
				<li>
					<a class="start-suite-optimized" href="javascript:void(0)"><f:message key="test-suite.execution.optimized.label" /> </a>
				</li>
				<li>
					<a class="start-suite-classic" href="javascript:void(0)"><f:message key='test-suite.execution.classic.label' /> </a>
				</li>
			</ul>
		</div>

		<script>
			$(function() {
				$("#start-resume-button").squashButton().fgmenu({
					content : $('#start-resume-button').next().html(),
					showSpeed : 0,
					width : 130
				});

				var startmenu = allUIMenus[allUIMenus.length - 1];

				startmenu.chooseItem = function(item) {
					
					var jqItem = $(item);
					if (jqItem.hasClass('start-suite-classic')) {
						checkTestSuiteExecutionDoable().done(
								startResumeClassic);
					} else {
						if (jqItem.hasClass('start-suite-optimized')) {
							checkTestSuiteExecutionDoable().done(
									startResumeOptimized);
						}
					}
					
					startmenu.kill();
				};
			});
		</script>
	</c:if>
	<form action="${ runnerUrl }" method="post" name="execute-test-suite-form" target="optimized-execution-runner" class="not-displayed">
		<input type="submit" value="true" name="optimized" id="start-optimized-button" />
		<input type="hidden" name="mode" value="start-resume" />
		<input type="hidden" name="suitemode" value="true" />
	</form>
	<c:if test="${ statisticsEntity.status != 'READY' }">
		<f:message var="restartSuiteButton" key="test-suite.execution.restart.label"/>
		<input type="button" id="restart-button" class="button run-menu" value="${restartSuiteButton}"/>
		<div id="restart" style="display: none">
			<ul>
				<li>
					<a class="restart-suite-optimized exec" href="javascript:void(0)"><f:message key="test-suite.execution.optimized.label" /> </a>
				</li>
				<li>
					<a class="restart-suite-classic" href="javascript:void(0)"><f:message key='test-suite.execution.classic.label' /> </a>
				</li>
			</ul>
		</div>
		<div id="confirm-restart-dialog" class="not-displayed popup-dialog"
			title="<f:message key='test-suite.execution.restart.title' />">
			<input id="restart-mode" type="hidden" value="classic" />
			<span><f:message key="test-suite.execution.restart.warning-message" /> </span>
			<input:ok />
			<input:cancel />
		</div>
		<script>
			$(function() {
				$("#restart-button").squashButton().fgmenu({
					content : $('#restart-button').next().html(),
					showSpeed : 0,
					width : 130
				});

				var restartDialog = $("#confirm-restart-dialog");
				restartDialog.confirmDialog({
					confirm : deleteExecAndStart
				});

				var startmenu = allUIMenus[allUIMenus.length - 1];

				startmenu.chooseItem = function(item) {
					var it = $(item);
					var restartMode = $('#restart-mode');

					if (it.hasClass('restart-suite-classic')) {
						restartMode.val('classic');
						restartDialog.confirmDialog('open');

					} else if (it.hasClass('restart-suite-optimized')) {
						restartMode.val('optimized');
						restartDialog.confirmDialog('open');
					}

					startmenu.kill();
				};
				
				function confirmRestartHandler (jqXHR) {
					//I shouldn't have to do this, I know, they made me .. :( 
					// seriously : don't know why but the ajax.done() method above is called even when the check fails (observed with FF 10)
					// therefore i have to check for jqXHR to be null to make sure the method is not called after a fail
					if (jqXHR == null) {
						restartDialog.confirmDialog('close');
						if ($('#restart-mode').val() == 'classic') {
							startResumeClassic();
						} else {
							startResumeOptimized();
						}
					}
				};
				function deleteExec (){
					return $.ajax({
						type : 'delete',
						url : "${ deleteOnRestartUrl }"
					});
				};
				function deleteExecAndStart (){
					deleteExec().then(checkStartAndHandleResultForRestart);
				};
				
			 	function checkStartAndHandleResultForRestart (){
					checkTestSuiteExecutionDoable().done(confirmRestartHandler);
				};
				
				
			});
		</script>

	</c:if>
</div>