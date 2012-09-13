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
<%@ tag description="general information panel for an auditable entity. Client can add more info in the body of this tag" body-content="scriptless" %>
<%@ attribute name="auditableEntity" required="true" type="java.lang.Object" description="The entity which general information we want to show" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>


	<f:message var="dateFormat" key="squashtm.dateformat" />
	
	
	<div style="display:inline-block; margin-right:2em;vertical-align:top">
		<label><f:message key="label.ExecutionMode" /></label>
		<span><f:message key="${ auditableEntity.executionMode.i18nKey }"/></span>
	</div>
	

	
	
	<div style="display:inline-block; margin-right:2em;vertical-align:top">
		<label><f:message key="label.Status" /></label>
		<jq:execution-status status="${auditableEntity.executionStatus.canonicalStatus}" /> 
		<c:if test="${ auditableEntity.automated }">
		<br>
		<label><f:message key="label.AutomatedTestStatus"/></label>
		<jq:execution-status status="${auditableEntity.executionStatus}" />
		</c:if>
	</div>
	
	<div style="display:inline-block; margin-right:2em;vertical-align:top">
		<label for="last-modified-on" ><f:message key="label.LastExecutionOn" /></label>
		<c:choose>
			<c:when test="${not empty auditableEntity.lastModifiedOn }">
				<span id="last-modified-on">
					<f:formatDate value="${ auditableEntity.lastModifiedOn }" pattern="${dateFormat}" /> 
					(${ auditableEntity.lastModifiedBy })
				</span>
			</c:when>
			<c:otherwise>
				(<f:message key="label.lower.Never" />)
			</c:otherwise>
		</c:choose>	
	</div>
	
	
	<c:if test="${auditableEntity.automated}">
	<f:message var="resultNotAvailable" key="url.resultNotAvailable" />
	<div style="display:inline-block; margin-right:2em;vertical-align:top">
		<label for="autoresult-url" ><f:message key="label.resultURL" /></label>
		<a id="autoresult-url" href="${execution.resultURL}"><c:out value="${execution.resultURL}" default="${resultNotAvailable}" /></a>
	</div>
	</c:if>