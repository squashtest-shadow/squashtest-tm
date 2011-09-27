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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>



	<div style="display:inline-block; margin-right:2em;">
		<label><f:message key="auditable-entity.execution-mode.label" /></label>
		<span><f:message key="${ auditableEntity.executionMode.i18nKey }"/></span>
	</div>

	
		
	<div style="display:inline-block; margin-right:2em;">
		<label><f:message key="auditable-entity.execution-status.label" /></label>
		<jq:execution-status status="{auditableEntity.executionStatus}" />
	</div>
	
	<div style="display:inline-block; margin-right:2em;">
		<label for="last-executed-on" ><f:message key="auditable-entity.last-executed-on.label" /></label>
		<c:choose>
			<c:when test="${not empty auditableEntity.lastExecutedOn }">
				<span id="last-executed-on">
					<f:message var="dateFormat" key="squashtm.dateformat"/>
					<f:formatDate value="${ auditableEntity.lastExecutedOn }" pattern="${dateFormat}" /> 
					(${ auditableEntity.lastExecutedBy })
				</span>
			</c:when>
			<c:otherwise>
				(<f:message key="auditable-entity.never-executed.label" />)
			</c:otherwise>
		</c:choose>	
	</div>
	

