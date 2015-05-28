<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
<%@ tag body-content="empty"
	description="test case toolbar and messages"%>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="milestoneConf" required="true" type="java.lang.Object" description="an instance of MilestoneFeatureConfiguration"%>
<%@ attribute name="nodeType" required="false" type="java.lang.String" description="selected element type"%>
<%-- Node type define the type of selected node. Values :
campaign, iteration, testsuite, execution, requirement, testcase
 --%>

<c:if test="${milestoneConf.messagesEnabled && (milestoneConf.multipleBindings || milestoneConf.locked)}">
	<div data-milestones="${milestoneConf.totalMilestones}" class="milestone-count-notifier entity-edit-general-warning">
	<%-- If element is associated with at least one locked milestone -> Show locked message even with app not in milestone mode (3613-R2.08) --%>
	<c:choose>
		<c:when test="${milestoneConf.locked}">
			<c:choose>
				<c:when test="${nodeType == 'campaign' || nodeType == 'iteration' || nodeType=='testsuite' || nodeType == 'execution'}">
					<f:message var="warningMsg" key="message.CannotModifyBecauseMilestoneLocking.campaign" />
				</c:when>
				<c:otherwise>
					<f:message var="warningMsg" key="message.CannotModifyBecauseMilestoneLocking" />
				</c:otherwise>
			</c:choose>
		</c:when>
	<%-- If element is associated with several milestones AND isn't associated with any locked milestone AND the app is in milestone mode -> Show message by type --%>
		<c:when test="${milestoneConf.multipleBindings && milestoneConf.userEnabled && !milestoneConf.locked}">
			<c:choose>
				<c:when test="${nodeType == 'requirement'}">
					<f:message var="warningMsg" key="messages.boundToMultipleMilestones.requirement" />
				</c:when>	
				<c:when test="${nodeType == 'testcase'}">
					<f:message var="warningMsg" key="messages.boundToMultipleMilestones.testcase" />
				</c:when>	
			</c:choose>
		</c:when>
	</c:choose>
			<span>${warningMsg}</span>
	</div>
</c:if>