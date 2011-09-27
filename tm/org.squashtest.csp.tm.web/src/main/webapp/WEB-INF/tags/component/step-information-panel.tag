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
<%@ attribute name="auditableEntity" required="true" type="java.lang.Object" description="The entity which general information we want to show" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

	<f:message var="dateFormat" key="squashtm.dateformat" />

	<label for="last-executed-on" ><f:message key="auditable-entity.last-executed-on.label" /></label>
	<c:choose>
	<c:when test="${not empty auditableEntity.lastExecutedOn }">
	<span id="last-executed-on">
		<f:formatDate value="${ auditableEntity.lastExecutedOn }" pattern="${dateFormat}" /> 
	</span>
	<br/>
	<span id="last-executed-by">
		<f:message key="auditable-entity.last-executed-by.label" /> : ${ auditableEntity.lastExecutedBy }
	</span>
	</c:when>
	<c:otherwise>
	(<f:message key="auditable-entity.never-executed.label" />)
	</c:otherwise>
	</c:choose>
	<br />
