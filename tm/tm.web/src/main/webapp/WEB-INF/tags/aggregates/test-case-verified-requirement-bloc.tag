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
<%@ tag body-content="empty" description="inserts the html table of verified resquirements" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ attribute name="verifiedRequirementsUrl" required="true"
	description="URL to manipulate the verified requirements" %>
<%@ attribute name="containerId" required="true" description="if of dom container that will hold the table events" %>
<%@ attribute name="verifiedRequirementsTableUrl" required="true"
	description="URL for the verified requirements table" %>
<%@ attribute name="linkable" required="true" description=" boolean that says if the concerned test case is viewed by a user who has LINK rights on this entity" %>

<s:url var="root" value="/" />
<script type="text/javascript" th:inline="javascript">
			if (!squashtm) {
				var squashtm = {};
			}
			if (!squashtm.app) {
				squashtm.app = {
					contextRoot : "${root}",
				};
			}
			squashtm.app.verifiedRequirementsBlocSettings = {
				containerId : "${containerId}",
				linkable : "${linkable}",
				oppened : true,
				title : "<f:message key='label.verifiedRequirements.test-cases' />",
				url :"${verifiedRequirementsUrl}",
			};
</script>
<div id="verified-requirements-bloc-frag">
	<div class="toggle-panel">	
		<span class="not-displayed toggle-panel-buttons"> 
			<c:if test="${ linkable }">					
					<f:message var="associateLabel"	key="label.associateRequirements" />
					<input id="add-verified-requirements-button" type="button" value="${associateLabel}" class="button" />
					
					<f:message var="removeLabel" key="label.removeRequirementsAssociation" />
					<input id="remove-verified-requirements-button" type="button" value="${ removeLabel }" class="button" />
			</c:if>
		</span>
		<div class="toggle-panel-main" id="verified-requirements-panel">
			<aggr:verified-requirements-table includeIndirectlyVerified="${ true }" linkable="${ linkable }" verifiedRequirementsTableUrl="${ verifiedRequirementsTableUrl }" verifiedRequirementsUrl="${verifiedRequirementsUrl }" containerId="contextual-content" />
		</div>
	</div>
</div>