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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ attribute name="execution" required="true" type="java.lang.Object"
	description="The execution"%>
<c:if test="${ execution.executionMode == 'MANUAL' }" >
<c:url var="runnerUrl" value="/executions/${execution.id}/runner" />

<c:choose>
	<c:when test="${execution.executionStatus == 'READY'}">
		<f:message var="executeBtnLabel"
			key="execution.execute.start.button.label" />
	</c:when>
	<c:otherwise>
		<f:message var="executeBtnLabel"
			key="execution.execute.resume.button.label" />
	</c:otherwise>
</c:choose>

<input type="button"
	value="<f:message key="execution.execute.IEO.button.label" />"
	id="ieo-execution-button" />
<form action="${ runnerUrl }" method="post"
	name="execute-test-case-form" target="optimized-execution-runner"
	class="not-displayed">
	<input type="submit" value='' name="optimized"
		id="start-optimized-button" />
</form>
<input type="button" value="${executeBtnLabel}"
	id="execute-execution-button" />

<script>
	$(function() {
		var dryRunStart = function() {
			return $.ajax({
				url : '${ runnerUrl }',
				method : 'get',
				dataType : 'json',
				data : {
					'dry-run' : ''
				}
			});
		};

		var startResumeClassic = function() {
			var url = "${ runnerUrl }";
			var data = {
				'classic' : ''
			};
			var winDef = {
				name : "classicExecutionRunner",
				features : "height=500, width=600, resizable, scrollbars, dialog, alwaysRaised"
			};
			$.open(url, data, winDef);
		};

		var startResumeOptimized = function() {
			$('#start-optimized-button').trigger('click');
		};

		$("#execute-execution-button").button().click(function() {
			dryRunStart().done(startResumeClassic);
		});

		$("#ieo-execution-button").button().click(function() {
			dryRunStart().done(startResumeOptimized);
		});
	});
</script>
</c:if>