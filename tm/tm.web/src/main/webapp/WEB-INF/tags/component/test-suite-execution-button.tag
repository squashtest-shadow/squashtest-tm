<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
<c:url var='deleteOnRestartUrl' value='/test-suites/${ testSuiteId }/test-plan/executions' />


<c:if test="${ statisticsEntity.status == 'READY' }">
	<f:message var='startResumeLabel' key='test-suite.execution.start.label' />
</c:if>
<c:if test="${ statisticsEntity.status == 'RUNNING' }">
	<f:message var='startResumeLabel' key='test-suite.execution.resume.label' />
</c:if>


<div id="test-suite-execution-button" class="btn-group" data-runner-url="${ runnerUrl }" data-execs-url="${ deleteOnRestartUrl }">

	<c:if test="${ statisticsEntity.status == 'RUNNING' || statisticsEntity.status == 'READY'}">
		<input type="button" id="start-resume-button" class="sq-btn run-menu" value="${startResumeLabel}"/>		
		<ul class="not-displayed">
			<li class="cursor-pointer">
				<a id="start-suite-classic-button" ><f:message key='test-suite.execution.classic.label' /> </a>
			</li>		
			<li class="cursor-pointer">
				<a id="start-suite-optimized-button" ><f:message key="test-suite.execution.optimized.label" /> </a>
			</li>
		</ul>
	

	</c:if>
	
	<c:if test="${ statisticsEntity.status != 'READY' }">
	
		<f:message var="restartSuiteButton" key="test-suite.execution.restart.label"/>
		<input type="button" id="restart-button" class="sq-btn run-menu" value="${restartSuiteButton}"/>		
		<ul class="not-displayed">
			<li class="cursor-pointer">
				<a id="restart-suite-classic-button" ><f:message key='test-suite.execution.classic.label' /> </a>
			</li>	
			<li class="cursor-pointer">
				<a id="restart-suite-optimized-button" class="exec" ><f:message key="test-suite.execution.optimized.label" /> </a>
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
	publish("reload.exec-btns-panel");
	</script>

</div>