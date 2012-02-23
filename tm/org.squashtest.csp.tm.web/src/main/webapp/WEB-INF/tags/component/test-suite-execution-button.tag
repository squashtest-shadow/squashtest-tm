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
<%@ tag description="general information panel for an auditable entity. Client can add more info in the body of this tag" body-content="scriptless" %>
<%@ attribute name="statisticsEntity" required="true" type="java.lang.Object" description="The entity which general information we want to show" %>
<%@ attribute name="testSuiteId" required="true" description="The id of the test-suite" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div id="test-suite-execution-button" style="display:inline-block;">
	<c:url var='runnerUrl' value='/test-suites/${ testSuiteId }/test-plan/execution/runner' />
	<script type="text/javascript">
		$(function() {
			$("#start-classic-execution").button()
			.click(function() {
				var url = "${ runnerUrl }";
				var data = {'classic': '', 'mode': 'start-resume'};
				var winDef = {  name: "classic-execution-runner",  features: "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised" };
				$.open(url, data, winDef);
			});
			$("#restart-classic-execution").button()
			.click(function() {
				var url = "${ runnerUrl }";
				var data = {'classic': '', 'mode': 'restart'};
				var winDef = {  name: "classic-execution-runner",  features: "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised" };
				$.open(url, data, winDef);
			});
		});
	</script>
	<c:if test="${ statisticsEntity.status == 'READY' }">	
		<form action="${ runnerUrl }" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value="<f:message key='test-suite.execution.optimized.start.label' />" name="optimized" class="button execButton"/>
			<input type="hidden" name="mode" value="start-resume" />
		</form>
		<input id="start-classic-execution" type="button" value="<f:message key='test-suite.execution.classic.start.label' />" name="classic" class="button" />
	</c:if>
	<c:if test="${ statisticsEntity.status == 'RUNNING' }">	
		<form action="${ runnerUrl }" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value="<f:message key='test-suite.execution.optimized.resume.label' />" name="optimized" class="button execButton"/>
			<input type="hidden" name="mode" value="start-resume" />
		</form>
		<input id="start-classic-execution" type="button" value="<f:message key='test-suite.execution.classic.resume.label' />" name="classic" class="button" />
		<form action="${ runnerUrl }" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value="<f:message key='test-suite.execution.optimized.restart.label' />" name="optimized" class="button execButton" />
			<input type="hidden" name="mode" value="restart" />
		</form>
		<input id="restart-classic-execution" type="button" value="<f:message key='test-suite.execution.classic.restart.label' />" name="classic" class="button" />
	</c:if>
	<c:if test="${ statisticsEntity.status.terminatedStatus }">		
		<form action="${ runnerUrl }" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value="<f:message key='test-suite.execution.optimized.restart.label' />" name="optimized" class="button execButton" />
			<input type="hidden" name="mode" value="restart" />
		</form>
		<input id="restart-classic-execution" type="button" value="<f:message key='test-suite.execution.classic.restart.label' />" name="classic" class="button" />
	</c:if>
</div>