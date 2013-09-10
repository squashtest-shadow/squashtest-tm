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


<c:url var='runnerUrl' value='/test-suites/${ testSuiteId }/test-plan/execution/runner' />
<c:url var='testRunnerUrl' value='/test-suites/${ testSuiteId }/test-plan/execution/test-runner' />
<c:url var='deleteOnRestartUrl' value='/test-suites/${ testSuiteId }/test-plan/executions' />


<c:if test="${ statisticsEntity.status == 'READY' }">
	<f:message var='startResumeLabel' key='test-suite.execution.start.label' />
</c:if>
<c:if test="${ statisticsEntity.status == 'RUNNING' }">
	<f:message var='startResumeLabel' key='test-suite.execution.resume.label' />
</c:if>


<div id="test-suite-execution-button" style="display: inline-block;">

	<c:if test="${ statisticsEntity.status == 'RUNNING' || statisticsEntity.status == 'READY'}">
		<input type="button" id="start-resume-button" class="button run-menu" value="${startResumeLabel}"/>		
		<ul class="not-displayed">
			<li>
				<a id="start-suite-optimized-button" href="javascript:void(0)"><f:message key="test-suite.execution.optimized.label" /> </a>
			</li>
			<li>
				<a id="start-suite-classic-button" href="javascript:void(0)"><f:message key='test-suite.execution.classic.label' /> </a>
			</li>
		</ul>
	

	</c:if>
	<form action="${ runnerUrl }" method="post" name="execute-test-suite-form" target="optimized-execution-runner" class="not-displayed">
		<input type="submit" value="true" name="optimized" id="start-optimized-button" />
		<input type="hidden" name="mode" value="start-resume" />
		<input type="hidden" name="suitemode" value="true" />
	</form>
	
	<c:if test="${ statisticsEntity.status != 'READY' }">
	
		<f:message var="restartSuiteButton" key="test-suite.execution.restart.label"/>
		<input type="button" id="restart-button" class="button run-menu" value="${restartSuiteButton}"/>		
		<ul class="not-displayed">
			<li>
				<a id="restart-suite-optimized-button" class="exec" href="javascript:void(0)"><f:message key="test-suite.execution.optimized.label" /> </a>
			</li>
			<li>
				<a id="restart-suite-classic-button" href="javascript:void(0)"><f:message key='test-suite.execution.classic.label' /> </a>
			</li>
		</ul>
		
		<f:message var="confirmRestartTitle" key='test-suite.execution.restart.title' />
		<div id="confirm-restart-dialog" class="not-displayed popup-dialog"	title="${confirmRestartTitle}">
			<span><f:message key="test-suite.execution.restart.warning-message" /> </span>
			<div class="popup-dialog-buttonpane">
				<input:ok />
				<input:cancel />
			</div>
		</div>
	
	</c:if>
				
	<script>
		function checkTestSuiteExecutionDoable() {
			return $.ajax({
				type : 'post',
				data : { 'mode' : 'start-resume' },
				dataType : "json",
				url : "${ testRunnerUrl }"
			});
		}	
		
		function classicExecution() {
			var url = "${ runnerUrl }";
			var data = {
				'optimized' : 'false',
				'mode' : 'start-resume'
			};
			var winDef = {
				name : "classicExecutionRunner",
				features : "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised"
			};
			$.open(url, data, winDef);
	
		}
		
		function optimizedExecution() {
			$('#start-optimized-button').trigger('click');
		}
		
		$(function() {
		
			require(['jquery', 'jquery.squash.buttonmenu'], function($){				
				
				// ****** start-resume menu ********
				var startResumeBtn = $("#start-resume-button");
				if (startResumeBtn.length>0){
					$("#start-resume-button").buttonmenu();
					
					$("#start-suite-optimized-button").on('click', function(){
						checkTestSuiteExecutionDoable().done(optimizedExecution);					
					});
					
					$("#start-suite-classic-button").on('click', function(){
						checkTestSuiteExecutionDoable().done(classicExecution);
					});
				}
				
				
				// ******* restart menu *********
				var restartBtn = $("#restart-button");
				if (restartBtn.length>0){
					restartBtn.buttonmenu();
		
					var restartDialog = $("#confirm-restart-dialog");
					
					$("#restart-suite-optimized-button").on("click", function(){
						restartDialog.data('restart-mode', 'optimized');
						restartDialog.confirmDialog('open');
					});
					
					$("#restart-suite-classic-button").on("click", function(){
						restartDialog.data('restart-mode', 'classic');
						restartDialog.confirmDialog('open');
					});	
					
					restartDialog.confirmDialog({
						confirm : function (){
							$.ajax({
								type : 'delete',
								url : "${ deleteOnRestartUrl }"
							})
							.then(function(){
								return checkTestSuiteExecutionDoable();
							})
							.done(function(){
								restartDialog.confirmDialog('close');
								var mode = restartDialog.data('restart-mode');
								if (mode === 'classic'){
									classicExecution();
								} else {
									optimizedExecution();
								}
							});
						}
					});				
				}			
			});

			
		});
	</script>

</div>