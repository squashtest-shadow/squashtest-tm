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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>

<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<s:url var="newExecutionUrl" value="/iterations/{iterId}/test-plan/{tpId}/new-execution">
	<s:param name="iterId" value="${iterationId}" />
	<s:param name="tpId" value="${testPlanId}" />
</s:url>


<s:url var ="showExecutionUrl" value="/executions"/>

<c:set var="textcolor" value="#555555" />


<td>

</td>

<td>

</td>


<td >
	
		<c:forEach items="${ executions }" var="execution" varStatus="status" >
			<span style="display:block; margin-left: 10px; color: ${textcolor}; font-style:italic; text-decoration: underline"><a href="${showExecutionUrl}/${execution.id}">Exec. ${status.index + 1} :  ${ execution.name }</a></span>
		</c:forEach>
	
		<c:if test="${ editableIteration }">
			<span style="text-align:right;margin-right:10px;display:block">
				<small> 
					<a style="color:${textcolor}" id="new-exec-${testPlanId}" href="#" data-new-exec="${newExecutionUrl}" ><f:message key="execution.iteration-test-plan-row.new"/></a> 
				</small>
			</span>
		</c:if> 
</td>
<td>
</td>
<td>
		<c:forEach items="${ executions }" var="execution">
			<span style="display:block; color: ${textcolor}; font-style:italic;"><f:message key="${ execution.executionMode.i18nKey }"/></span>
		</c:forEach>
</td>


<td>

</td>

<td>
		<c:forEach items="${ executions }" var="execution">
			<span style="display:block; color: ${textcolor} font-style:italic;"><f:message key="execution.execution-status.${execution.executionStatus}"/></span>
		</c:forEach>
	
</td>
<td>
	
		<c:forEach items="${ executions }" var="execution">
			<span style="display:block; color: ${textcolor}">
				<c:choose>
					<c:when test="${ execution.lastExecutedBy != null }">
						<i>${ execution.lastExecutedBy }</i>
					</c:when>
					<c:otherwise>
						<i><f:message key="squashtm.nodata"/></i>
					</c:otherwise>
				</c:choose>
			</span>
		</c:forEach>

</td>	
<td>
		<c:forEach items="${ executions }" var="execution">
			<span style="display:block; color: ${textcolor}">
				<c:choose>
					<c:when test="${ execution.lastExecutedOn != null }">
						<f:message var="dateFormat" key="squashtm.dateformat" />
						<i><f:formatDate value="${ execution.lastExecutedOn }" pattern="${dateFormat}"/></i>
					</c:when>
					<c:otherwise>
						<i><f:message key="squashtm.nodata"/></i>
					</c:otherwise>
				</c:choose>
			</span>
		</c:forEach>

</td>
<td>

</td>