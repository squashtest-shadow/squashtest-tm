<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<s:url var="newExecutionUrl"
	value="/iterations/{iterId}/test-plan/{tpId}/executions/new">
	<s:param name="iterId" value="${iterationId}" />
	<s:param name="tpId" value="${testPlanItem.id}" />
</s:url>

<s:url var="showExecutionUrl" value="/executions" />

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ testSuite }">
	<c:set var="executable" value="${ true }" />
</authz:authorized>

<c:set var="textcolor" value="#555555" />


<td colspan="14">
	<table>
		<!-- -----------------------------------------------ROW OF EXECUTION -->
		<c:forEach items="${ executions }" var="execution" varStatus="status">
			<tr>
				<td style="color: ${textcolor}; font-style:italic; text-decoration: underline">
					<a href="${showExecutionUrl}/${execution.id}">
						<span style="font-weight:bold;">Exec. ${status.index + 1} :</span><span> ${ execution.name }</span>
					</a>
				</td>
				<td style="width: 7.5em;color: ${textcolor}; font-style:italic;"><f:message
						key="${ execution.executionMode.i18nKey }" />
				</td>
				<td style="width: 10em; color: ${textcolor} font-style:italic;"><f:message
						key="execution.execution-status.${execution.executionStatus}" />

				</td>
				<td style="width: 12em; color: ${textcolor}">
				<c:choose>
					<c:when test="${ execution.lastExecutedBy != null }">
						<span style="font-style:italic;">${ execution.lastExecutedBy }</span>
					</c:when>
					<c:otherwise>
						<span style="font-style:italic;"><f:message key="squashtm.nodata" /> </span>
					</c:otherwise>
				</c:choose>
				</td>
				<td style="width: 12em; color: ${textcolor}">
				<c:choose>
					<c:when test="${ execution.lastExecutedOn != null }">
						<f:message var="dateFormat" key="squashtm.dateformat" />
						<i><f:formatDate value="${ execution.lastExecutedOn }"
								pattern="${dateFormat}" /> </i>
					</c:when>
					<c:otherwise>
						<i><f:message key="squashtm.nodata" /> </i>
					</c:otherwise>
				</c:choose>
				</td>
				<td style="width: 2em;"><!-- todo : run the execution button --></td>
				<td style="width: 2em;" class="centered"><authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ execution }">
						<a id="delete-execution-table-button-${execution.id}" 
							class="delete-execution-table-button" title='<f:message key="label.removeExecution"/>' ></a>
					</authz:authorized>
				</td>
			</tr>
		</c:forEach>
		<!-- ------------------------------------------END ROW OF EXECUTION -->

		<!-- ---------------------------------------------ROW NEW EXECUTION -->
		<c:if test="${ executable && !testPlanItem.testCaseDeleted }">
			<tr>
				<td colspan="12" style="text-align: left;"> 
					<a id="new-exec-${testPlanItem.id}" style="font-size:0.8em;" class="button new-exec"  data-new-exec="${newExecutionUrl}">
						<f:message key="execution.iteration-test-plan-row.new" /> 
					</a> 
								
					<c:if test="${ testPlanItem.automated}"> 
							<a	class="button new-auto-exec" style="font-size:0.8em;" id="new-auto-exec-${testPlanItem.id}"	 data-tpi-id="${ testPlanItem.id }">
								<f:message	key="execution.iteration-test-plan-row.new.auto" />
							</a>
					</c:if>
				</td>
			</tr>
		</c:if>
		<!-- ---------------------------------------------END ROW NEW EXECUTION -->
	</table>
</td>
