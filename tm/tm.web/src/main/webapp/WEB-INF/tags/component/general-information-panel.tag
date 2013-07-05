<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

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
<%@ attribute name="entityUrl" description="REST url representing the entity. If set, this component will pull itself from entityUrl/general" %>
<%@ attribute name="withoutCreationInfo" type="java.lang.Boolean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<f:message var="dateFormat" key="squashtm.dateformat" />

<div id="general-information-panel" class="information-panel">
	<c:if test="${ not withoutCreationInfo }">
		<span ><f:message key="label.CreatedOn" />&nbsp;:&nbsp;</span>
		<span id="created-on">
			<f:formatDate value="${ auditableEntity.createdOn }" pattern="${dateFormat}"/> (${ auditableEntity.createdBy })
		</span>
		<br />
	</c:if>
	<span><f:message key="label.UpdatedOn" />&nbsp;:&nbsp;</span>
	<c:choose>
		<c:when test="${not empty auditableEntity.lastModifiedOn }">
			<span id="last-modified-on">
				<f:formatDate value="${ auditableEntity.lastModifiedOn }" pattern="${dateFormat}" /> (${ auditableEntity.lastModifiedBy })
			</span>
		</c:when>
		<c:otherwise>
			(<f:message key="label.lower.Never" />)
		</c:otherwise>
	</c:choose>
	<br />
</div>
<c:if test="${ not empty entityUrl }">
	<script type="text/javascript">
		$(function() {
			$("#general-information-panel").ajaxSuccess(function(event, xrh, settings) {
				if (settings.type == 'POST') {
					$( this ).load('${ entityUrl }/general');
				}
			});
		});
	</script>
</c:if>