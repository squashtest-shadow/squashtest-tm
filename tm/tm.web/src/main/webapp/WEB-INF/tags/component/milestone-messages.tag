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
<%@ tag body-content="empty" description="test case toolbar and messages" %>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<%@ attribute name="milestoneConf" required="true" type="java.lang.Object" description="an instance of MilestoneFeatureConfiguration"%>

<c:if test="${milestoneConf.messagesEnabled}">
    <div data-milestones="${milestoneConf.totalMilestones}" class="milestone-count-notifier entity-edit-general-warning 
          ${(milestoneConf.multipleBindings) ? '' : 'not-displayed'}">
      <p><f:message key="messages.boundToMultipleMilestones"/></p>
    </div>
    <c:if test="${milestoneConf.locked}">
    <div class="entity-edit-general-warning">
      <p><f:message key="message.CannotModifyBecauseMilestoneLocking"/></p>
    </div>        
    </c:if>
</c:if>