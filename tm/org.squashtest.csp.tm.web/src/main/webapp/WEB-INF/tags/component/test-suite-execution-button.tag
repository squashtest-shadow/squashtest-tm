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
	<c:url var='runnerUrl'
		value='/test-suites/${ testSuiteId }/test-plan/execution/runner' />
	<script type="text/javascript">
		function classicExecution(data,  url) {

			var winDef = {
				name : "classic-execution-runner",
				features : "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised"
			};
			$.open(url, data, winDef);

		}
		var checkTestSuiteExecutionDoable = function(dataP, urlP) {
			return $.ajax({
				type : 'post',
				data : dataP,
				dataType : "json",
				url : urlP
			});
		}
		var testSuiteExecutionError = function(jqXHR) {
			var json = jQuery.parseJSON(jqXHR.responseText);

			if (json != null && json.actionValidationError != null) {
				var message = '<p><f:message key="squashtm.action.exception.testsuite.execution.error.first.words" />';
				message += '<ul>'
				if (json.actionValidationError.exception === "EmptyTestPlanException") {
					message += '<li> <f:message key="squashtm.action.exception.testsuite.testplan.empty" /></li>';
				}
				message += '</ul></p>'
				oneShotDialog('<f:message key="popup.title.error" />', message);
			}
		}
	</script>
	<c:if test="${ statisticsEntity.status == 'READY' }">
		<f:message var='startResumeLabel'
			key='test-suite.execution.start.label' />
	</c:if>
	<c:if test="${ statisticsEntity.status == 'RUNNING' }">
		<f:message var='startResumeLabel'
			key='test-suite.execution.resume.label' />
	</c:if>

	<c:if
		test="${ statisticsEntity.status == 'RUNNING' || statisticsEntity.status == 'READY'}">
		<a tabindex="0" href="#start" class="button" id="start-resume-button">${startResumeLabel}</a>
		<div id="start" style="display: none">
			<ul>
				<li><a class="start-suite-optimized" href="#"><f:message
							key="test-suite.execution.optimized.label" /> </a>
				</li>
				<li><a class="start-suite-classic" href="#"><f:message
							key='test-suite.execution.classic.label' /> </a>
				</li>
			</ul>
		</div>
		<form action="${ runnerUrl }" method="post"
			name="execute-test-suite-form" target="optimized-execution-runner"
			style="display: none;">
			<input type="submit" value='' name="optimized"
				id="start-optimized-button" /> <input type="hidden" name="mode"
				value="start-resume" />
		</form>
		<script>
			$(function() {
				$("#start-resume-button").menu({
					content : $('#start-resume-button').next().html(),
					showSpeed : 0,
					width : 130
				});

				var startmenu = allUIMenus[allUIMenus.length - 1];

				startmenu.chooseItem = function(item) {
					var url = "${ runnerUrl }";
					var data = {
						'classic' : '',
						'mode' : 'start-resume'
					};
					checkTestSuiteExecutionDoable(data, url).fail(
							testSuiteExecutionError).done(function(){
							launchClassicOrOptimizedStart(data, url, item);});
				}
			});

			function launchClassicOrOptimizedStart(data, url, item) {
				if ($(item).hasClass('start-suite-classic')) {
					classicExecution(data, url);
				} else if ($(item).hasClass('start-suite-optimized')) {
					$('#start-optimized-button').trigger('click');
				}
			}
		</script>
	</c:if>
	<c:if test="${ statisticsEntity.status != 'READY' }">
		<a tabindex="0" href="#restart" class="button" id="restart-button"><f:message
				key='test-suite.execution.restart.label' /> </a>
		<div id="restart" style="display: none">
			<ul>
				<li><a class="restart-suite-optimized" href="#"><f:message
							key="test-suite.execution.optimized.label" /> </a>
				</li>
				<li><a class="restart-suite-classic" href="#"><f:message
							key='test-suite.execution.classic.label' /> </a>
				</li>
			</ul>
		</div>
		<form action="${ runnerUrl }" method="post"
			name="execute-test-suite-form" target="optimized-execution-runner"
			style="display: none;">
			<input type="submit" value="" name="optimized"
				id="restart-optimized-button" /> <input type="hidden" name="mode"
				value="restart" />
		</form>
		<div id="confirm-restart-dialog" class="not-displayed popup-dialog"
			title="<f:message key='test-suite.execution.restart.title' />">
			<input id="restart-mode" type="hidden" value="classic" /> <span><f:message
					key="test-suite.execution.restart.warning-message" /> </span>
			<input:ok />
			<input:cancel />
		</div>
		<script>
			$(function() {
				$("#restart-button").menu({
					content : $('#restart-button').next().html(),
					showSpeed : 0,
					width : 130
				});

				var restartClassic = function() {
					restartDialog.confirmDialog('close');
					classicExecution('restart');
				};

				var restartOptimized = function() {
					restartDialog.confirmDialog('close');
					$('#restart-optimized-button').trigger('click');
				};

				var confirmRestartHandler = function() {
					if ($('#restart-mode').val() == 'classic') {
						restartClassic();
					} else {
						restartOptimized();
					}
				};

				var restartDialog = $("#confirm-restart-dialog");
				restartDialog.confirmDialog({
					confirm : confirmRestartHandler
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
				}
			});
		</script>

	</c:if>
</div>