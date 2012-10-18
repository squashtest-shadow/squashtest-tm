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
<%@ tag description="test automation panel (test case level)" body-content="empty"%>


<%@ attribute name="testCase" required="true" type="java.lang.Object" description="the test case"%>
<%@ attribute name="canModify" required="no" type="java.lang.Boolean" description="whether the script name link is editable (or not). Default is false."%>
<%@ attribute name="testCaseUrl" required="yes" description="the url where to reach the test case"%>


<%@ tag language="java" pageEncoding="ISO-8859-1" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"  %>

 			
<div class="display-table-row">
	
	<label class="display-table-cell"><f:message key="test-case.testautomation.section.label"/></label>
	
	<div class="display-table-cell">
	<c:choose >
		<c:when test="${testCase.automatedTest != null}">
			<a id="ta-picker-link" href="javascript:void(0);"><c:out value="${testCase.automatedTest.fullName}"/></a>
			<c:if test="${ canModify }"><a id="remove-ta-link"  class="actionLink" href="javascript:void(0);">[<f:message key="label.Remove"/>]</a></c:if>
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${ canModify }">
					<a id="ta-picker-link" href="javascript:void(0);"><f:message key="label.dot.pick"/></a>
					<a id="remove-ta-link" class="actionLink" href="javascript:void(0);" style="display:none">[<f:message key="label.Remove"/>]</a>
				</c:when>
				<c:otherwise>
					<f:message key="label.none"/>
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>	
	</div>
</div>

			