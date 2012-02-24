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
	<c:if test="${ statisticsEntity.status == 'READY' }">	
		<form action="<c:url value="/test-suites/${testSuiteId}/test-plan/start-resume/runner" />" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value='<f:message key="test-suite.execution.start.label" />' name="optimized" class="button execButton"/>
		</form>
	</c:if>
	<c:if test="${ statisticsEntity.status == 'RUNNING' }">	
		<form action="<c:url value="/test-suites/${testSuiteId}/test-plan/start-resume/runner" />" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value='<f:message key="test-suite.execution.resume.label" />' name="optimized" class="button execButton"/>
		</form>
		<form action="<c:url value="/test-suites/${testSuiteId}/test-plan/restart/runner" />" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value='<f:message key="test-suite.execution.restart.label" />' name="optimized" class="button execButton"/>
		</form>
	</c:if>
	<c:if test="${ statisticsEntity.status.terminatedStatus }">		
		<form action="<c:url value="/test-suites/${testSuiteId}/test-plan/restart/runner" />" method="post" name="execute-test-suite-form" target="optimized-execution-runner" style="display:inline-block;">
			<input type="submit" value='<f:message key="test-suite.execution.restart.label" />' name="optimized" class="button execButton"/>
		</form>
	</c:if>
</div>