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
<%@ tag body-content="empty" description="the calling test case table" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>


<%@ attribute name="testCase" required="true" type="java.lang.Object"  description="the testcase" %>
<%@ attribute name="writable"  required="true" type="java.lang.Boolean"  description="if the user has write permission on this test case" %>
<%@ attribute name="deletable"  required="true" type="java.lang.Boolean"  description="if the user has deletion permission on this test case" %>
<%@ attribute name="moreThanReadOnly"  required="true" type="java.lang.Boolean"  description="if the user has more than read only permission on this test case" %>
<%@ attribute name="isInfoPage"  required="true" type="java.lang.Boolean"  description="a parameter set in the context when this template is to be displayed in an info page." %>
<%@ attribute name="otherViewers"  required="true" type="java.lang.Object"  description="an object saying if that test case is being browsed by someone else right now" %>
<%@ attribute name="milestoneConf" required="true" type="java.lang.Object" description="an instance of MilestoneFeatureConfiguration"%>


<c:url var="testCaseUrl" value="/test-cases/${testCase.id}"/>

<f:message var="createNewVersionLabel" key="label.createNewVersion"/>
<f:message var="renameLabel" key="test-case.button.rename.label"/>
<f:message var="printLabel" key="label.print"/>

<%---------------------------- Test Case Informations ------------------------------%>

<div id="test-case-toolbar" class="toolbar-class ui-corner-all">
	
	<div class="toolbar-information-panel">
		<comp:general-information-panel auditableEntity="${ testCase }"	entityUrl="${ testCaseUrl }" />
	</div>

	<div class="toolbar-button-panel">
	<c:if test="${ writable }">
		<input type="button" value="${renameLabel}"
				id="rename-test-case-button" class="sq-btn" />
	</c:if>
    <c:if test="${milestoneConf.messagesEnabled}">
        <input type="button" value="${createNewVersionLabel}" id="create-test-case-version-button" class="sq-btn"/>
    </c:if>
		<input type="button" value="${printLabel}" id="print-test-case-button" class="sq-btn"/>
	</div>
	
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }"
							objectUrl="${ testCaseUrl }" />
	</c:if>
  
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
  
	<div class="unsnap"></div>
 
</div>


