<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag body-content="empty" description="represents the whole test plan tab"%>

<%@ attribute name="campaign" required="true" type="java.lang.Object" description="the instance of the campaign"%>
<%@ attribute name="editable" type="java.lang.Boolean"       description="Right to edit content. Default to false."%>
<%@ attribute name="reorderable" type="java.lang.Boolean"    description="Right to reorder the test plan. Default to false."%>
<%@ attribute name="linkable" type="java.lang.Boolean"       description="Right to add test cases to the test plan. Default to false."%>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>



<c:url var="assignableUsersUrl"   value="/campaigns/${campaign.id}/assignable-users" />
<c:url var="testCaseUrl"          value="/test-cases/{tc-id}/info" />
<c:url var="dtMessagesUrl"        value="/datatables/messages" />
<c:url var="tablemodel"           value="/campaigns/${campaign.id}/test-plan/table" />


<f:message var="okLabel"          key="label.Ok" />
<f:message var="cancelLabel"      key="label.Cancel" />

<f:message var="assignLabel"      key="label.Assign" />
<f:message var="confirmLabel"     key="label.Confirm" />
<f:message var="cancelLabel"      key="label.Cancel" />
<f:message var="reorderLabel"     key="label.Reorder" />

<f:message var="tooltipSortmode"  key="tooltips.TestPlanSortMode" />
<f:message var="messageSortmode"  key="message.TestPlanSortMode" />
<f:message var="associateLabel"   key="label.Add" />
<f:message var="removeLabel"      key="label.Remove" />
<f:message var="assignLabel"      key="label.Assign" />
<f:message var="reorderLabel"     key="label.Reorder" />
<f:message var="filterLabel"      key="label.Filter" />
<f:message var="filterTooltip"    key="tooltips.FilterTestPlan" />
<f:message var="reorderTooltip"   key="tooltips.ReorderTestPlan" />
<f:message var="tooltipAddTPI"    key="tooltips.AddTPIToTP" />
<f:message var="tooltipRemoveTPI" key="tooltips.RemoveTPIFromTP" />
<f:message var="tooltipAssign"    key="tooltips.AssignUserToTPI" />


<%-- ================== the toolbar ==================== --%>
<div class="cf">


  <c:if test="${ editable }">
    <div class="left btn-toolbar">
      <div class="btn-group">
        <button id="filter-test-plan-button" class="sq-btn btn-sm" title="${filterTooltip}">
          <span class="ui-icon ui-icon-refresh"></span>${filterLabel}
        </button>
        <button id="reorder-test-plan-button" class="sq-btn btn-sm" title="${reorderTooltip}">
          <span class="ui-icon ui-icon-refresh"></span>${reorderLabel}
        </button>
        <span id="test-plan-sort-mode-message" class="not-displayed sort-mode-message small" title="${tooltipSortmode}">${messageSortmode}</span>
      </div>
    </div>
  </c:if>

  <c:if test="${ linkable or editable }">
    <div class="right btn-toolbar">
      <c:if test="${  editable }">
        <span class="btn-group">
          <button id="assign-users-button" class="sq-btn btn-sm" title="${tooltipAssign}">
            <span class="ui-icon ui-icon-person"></span>${assignLabel}
          </button>
        </span>
      </c:if>
      <c:if test="${ linkable }">
        <span class="btn-group">
          <button id="add-test-case-button" class="sq-btn btn-sm" title="${tooltipAddTPI}">
            <span class="ui-icon ui-icon-plusthick"></span>${associateLabel}
          </button>
          <button id="remove-test-case-button" class="sq-btn btn-sm" title="${tooltipRemoveTPI}">
            <span class="ui-icon ui-icon-trash"></span>${removeLabel}
          </button>
        </span>
      </c:if>
    </div>
  </c:if>
  
  
</div>


<%-- =================== the table =============================== --%>

<div class="table-tab-wrap">
  <c:if test="${editable}">
    <c:set var="deleteBtnClause" value=", delete-button=#delete-multiple-test-cases-dialog" />
  </c:if>
  <table id="test-cases-table" data-def="ajaxsource=${tablemodel}" class="unstyled-table test-plan-table"
    data-entity-id="${campaign.id}" data-entity-type="campaign">
    <thead>
      <tr>
        <th class="no-user-select"
          data-def="map=entity-index, select, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
        <th class="no-user-select tp-th-project-name" data-def="map=project-name, sortable">
          <f:message key="label.project" />
        </th>
        <th class="no-user-select tp-th-exec-mode"
          data-def="map=exec-mode, sortable, center, visible=${campaign.project.testAutomationEnabled}, sClass=exec-mode">
          <f:message key="label.Mode" />
        </th>
        <th class="no-user-select tp-th-reference" data-def="map=reference, sortable">
          <f:message key="label.Reference" />
        </th>
        <th class="no-user-select tp-th-name" data-def="map=tc-name, sortable, link=${testCaseUrl}">
          <f:message key="test-case.name.label" />
        </th>
        <th class="no-user-select tp-th-assignee"
          data-def="map=assigned-user, sortable, sWidth=10%, sClass=assignee-combo">
          <f:message key="test-case.user.combo.label" />
        </th>
        <th class="no-user-select tp-th-importance" data-def="map=importance, sortable">
          <f:message key="test-case.importance.combo.label" />
        </th>
        <th class="no-user-select" data-def="map=empty-delete-holder${deleteBtnClause}">&nbsp;</th>
      </tr>
    </thead>
    <tbody>
      <%-- Will be populated through ajax --%>
    </tbody>
  </table>
  <div id="test-case-row-buttons" class="not-displayed">
    <a id="delete-test-case-button" class="delete-test-case-button"><f:message
        key="test-case.verified_requirement_item.remove.button.label" /></a>
  </div>



  <%-- ================== the popup ======================== --%>

  <div id="camp-test-plan-reorder-dialog" class="not-displayed popup-dialog" title="${reorderLabel}">
    <span><f:message key="message.ReorderTestPlan" /></span>
    <div class="popup-dialog-buttonpane">
      <input type="button" value="${confirmLabel}" />
      <input type="button" value="${cancelLabel}" />
    </div>
  </div>



  <div id="camp-test-plan-batch-assign" class="not-displayed popup-dialog" title="<f:message key="label.AssignUser"/>">
    <div data-def="state=assign">
      <span><f:message key="message.AssignTestCaseToUser" /></span>
      <select class="batch-select">
        <c:forEach var="user" items="${assignableUsers}">
          <option value="${user.key}">${user.value}</option>
        </c:forEach>
      </select>
    </div>
    <span data-def="state=empty-selec"><f:message key="message.EmptyTableSelection" /></span>

    <div class="popup-dialog-buttonpane">
      <input type="button" value="${assignLabel}" data-def="state=assign, mainbtn=assign, evt=confirm" />
      <input type="button" value="${cancelLabel}" data-def="mainbtn=empty-select, evt=cancel" />
    </div>
  </div>
  
  
  <div id="delete-multiple-test-cases-dialog" class="not-displayed popup-dialog" title="<f:message key='dialog.remove-testcase-associations.title'/>">
  
    <div data-def="state=confirm-deletion">
      <span><f:message key="dialog.remove-testcase-associations.message"/></span>
    </div>
    
    <div data-def="state=empty-selec">
      <span><f:message key="message.EmptyTableSelection" /></span>
    </div>
    
    <div class="popup-dialog-buttonpane">
      <input type="button" class="button" value="${okLabel}" data-def="state=confirm-deletion, evt=confirm, mainbtn=confirm-deletion"/>
      <input type="button" class="button" value="${cancelLabel}" data-def="evt=cancel, mainbtn=empty-select"/>
    </div>
  
  </div>

</div>

<%-- the init code must now be in the main jsp (edit-campaign or the test plan manager jsp) --%>

