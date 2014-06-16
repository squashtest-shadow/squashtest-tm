<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ iteration }">
	<c:set var="executable" value="${ true }" />
</authz:authorized>
<s:url var="newExecutionUrl"
	value="/iterations/{iterId}/test-plan/{tpId}/executions/new">
	<s:param name="iterId" value="${iterationId}" />
	<s:param name="tpId" value="${testPlanItem.id}" />
</s:url>

<s:url var="showExecutionUrl" value="/executions" />

<c:set var="textcolor" value="#555555" />

<td colspan="13">
	<table class="executions-table" id="item-test-plan-${testPlanItem.id}">
		<c:forEach items="${ executions }" var="execution" varStatus="status">
			<tr>
				<td colspan="5"
					style="text-align:left;color: ${textcolor}; font-style:italic; text-decoration: underline">
					<a href="${showExecutionUrl}/${execution.id}">
						<b>Exec. ${status.index + 1} :</b> ${ execution.name }
					</a>
				</td>
				<td style="width: 10%;">
					<span style="color: ${textcolor}">
					<c:choose>
						<c:when test="${  execution.testPlan != null &&  execution.testPlan.referencedDataset != null }">
							<i>${execution.testPlan.referencedDataset.name}</i>
						</c:when>
						<c:otherwise>
							<i><f:message key="squashtm.nodata" /> </i>
						</c:otherwise>
					</c:choose>
					</span>
				</td>
				<td style="width: 10%;"></td>
				<td style="width: 10%; color: ${textcolor} font-style:italic;"><f:message
						key="execution.execution-status.${execution.executionStatus}" />
				</td>
				<td style="width: 10%;">
					<span style="color: ${textcolor}">
					<c:choose>
						<c:when test="${ execution.lastExecutedBy != null }">
							<i>${ execution.lastExecutedBy }</i>
						</c:when>
						<c:otherwise>
							<i><f:message key="squashtm.nodata" /> </i>
						</c:otherwise>
					</c:choose>
					</span>
				</td>
				<td style="width: 10%;color: ${textcolor}">
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
				<td style="width: 2.5em;">
				</td>
				<td style="width: 1.5em;" class="centered">
					<authz:authorized hasRole="ROLE_ADMIN" hasPermission="EXECUTE" domainObject="${ execution }">
					<f:message var="labelRemoveExec" key="label.removeExecution"/>
					<a id="delete-execution-table-button-${execution.id}"  class="delete-execution-table-button" title="${labelRemoveExec}"></a>
					</authz:authorized>
				</td>
			</tr>
		</c:forEach>
		<c:if test="${ executable && !testPlanItem.testCaseDeleted }">
			<tr>
				<td colspan="13" style="text-align: left;">
					<strong>
						<a class="button new-exec" style="font-size:0.8em;" id="new-exec-${ testPlanItem.id }"  data-new-exec="${ newExecutionUrl }">
							<f:message key="execution.iteration-test-plan-row.new" />
						</a> 
						<c:if test="${testPlanItem.automated}"> 
						<a	class="button new-auto-exec" style="font-size:0.8em;" id="new-auto-exec-${ testPlanItem.id }"  data-tpi-id="${ testPlanItem.id }">
							<f:message key="execution.iteration-test-plan-row.new.auto" />
						</a>
						</c:if>
					</strong>
				</td>
				
			</tr>
			
		</c:if>
	</table>
</td>