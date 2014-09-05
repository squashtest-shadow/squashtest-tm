<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%-- 
  As of Squash TM 1.11 the content of this file has been wiped and replaced by tags/test-suites-components/test-suite-test-plan-panel.tag
  (Just like for iteration but with less features).

  Some features were then removed. See comments in the js initialization bloc at the end of this file.
  
 --%>
<%@ tag body-content="empty" description="the test plan panel of an iteration when displayed in the test plan manager" %>

<%@ attribute name="testSuite" type="java.lang.Object" description="the instance of test suite"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<s:url var="dtMessagesUrl" value="/datatables/messages" />
<s:url var="tableModelUrl" value="/test-suites/{suiteId}/test-plan">
  <s:param name="suiteId" value="${testSuite.id}" />
</s:url>
<s:url var="testcaseUrl"  value="/test-cases/{tc-id}/info" />



<div id="iteration-test-plans-panel" class="table-tab">

  <%-- ==================== THE TOOLBAR ==================== --%>

  <div class="cf">

    <f:message var="tooltipSortmode" key="tooltips.TestPlanSortMode" />
    <f:message var="messageSortmode" key="message.TestPlanSortMode" />
    <f:message var="reorderLabel" key="label.Reorder" />
    <f:message var="filterLabel" key="label.Filter" />
    <f:message var="filterTooltip" key="tooltips.FilterTestPlan" />
    <f:message var="reorderTooltip" key="tooltips.ReorderTestPlan" />
    <f:message var="removeLabel" key="label.Remove" />
    <f:message var="manageTS" key='menu.test-suites.button.main' />
    <f:message var="removeEverywhereLabel" key="label.RemoveTSAndIT" />
    <f:message var="tooltipAddSuite" key="tooltips.AddTSToTPI" />
    <f:message var="confirmLabel" key="label.Confirm" />
    <f:message var="cancelLabel" key="label.Cancel" />
    <f:message var="assignLabel" key="label.Assign" />
    <f:message var="okLabel" key="label.Ok" />


    
    <div class="left btn-toolbar">
      <span class="btn-group">
        <button id="filter-test-plan-button" class="sq-btn btn-sm" title="${filterTooltip}">
          <span class="ui-icon ui-icon-refresh"></span>
          ${filterLabel}
        </button>
        <button id="reorder-test-plan-button" class="sq-btn btn-sm" title="${reorderTooltip}">
          <span class="ui-icon ui-icon-refresh"></span>
          ${reorderLabel}
        </button>
        <span id="test-plan-sort-mode-message" class="not-displayed sort-mode-message small"
          title="${tooltipSortmode}">${messageSortmode}</span>
      </span>
    </div>

    <div class="right btn-toolbar">

        <span class="btn-group">

          <button id="remove-test-plan-button" class="sq-btn btn-sm" title="${tooltipRemoveTPI}">
            <span class="ui-icon ui-icon-trash"></span>
            ${removeLabel}
          </button>
        </span>

    </div>
    
  </div>

  <%-- ===================== THE TABLE ===================== --%>

  <div class="table-tab-wrap">

    <table id="test-suite-test-plans-table" class="test-plan-table unstyled-table"
      data-def="ajaxsource=${tableModelUrl}"  data-entity-id="${testSuite.id}" data-entity-type="testSuite">
      <thead>
        <tr>
          <th class="no-user-select"
            data-def="map=entity-index, select, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
          <th class="no-user-select tp-th-filter tp-th-project-name" data-def="map=project-name, sortable">
            <f:message key="label.project" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-reference" data-def="map=reference, sortable">
            <f:message key="label.Reference" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-name" data-def="map=tc-name, sortable, link=${testcaseUrl}">
            <f:message key="iteration.executions.table.column-header.test-case.label" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-importance" data-def="map=importance, sortable">
            <f:message key="iteration.executions.table.column-header.importance.label" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-dataset" data-def="map=dataset.selected.name, sortable, sWidth=10%, sClass=dataset-combo">
            <f:message key="label.Dataset" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-suite" data-def="map=suite, tooltip-target=suitesTot, sortable, sWidth=10%">
            <f:message key="iteration.executions.table.column-header.suite.label" />
          </th>
          <th class="no-user-select tp-th-filter tp-th-status" data-def="map=status, sortable, sWidth=10%, sClass=status-display">
            <f:message key="iteration.executions.table.column-header.status.label" />
          </th>
          <th class="no-user-select" data-def="map=empty-delete-holder, delete-button=#ts-test-plan-delete-dialog">&nbsp;</th>
        </tr>
      </thead>
      <tbody>
        <%-- Will be populated through ajax --%>
      </tbody>
    </table>


    <%-- ============================== THE DIALOGS ========================= --%>


    <div id="ts-test-plan-delete-dialog" class="not-displayed popup-dialog"
      title="<f:message key="dialog.remove-testcase-testsuite-associations.title" />">
      <span data-def="state=single-tp" style="font-weight: bold;">
        <f:message key="dialog.remove-testcase-testsuite-association.message" />
      </span>
      <span data-def="state=multiple-tp" style="font-weight: bold;">
        <f:message key="dialog.remove-testcase-testsuite-associations.message" />
      </span>

      <div class="popup-dialog-buttonpane">
        <input type="button" value="${removeLabel}"
          data-def="state=single-tp multiple-tp, mainbtn=single-tp multiple-tp, evt=confirm" />
        <input type="button" value="${removeEverywhereLabel}" data-def="state=single-tp multiple-tp, evt=confirmall" />
        <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
      </div>
    </div>

    <div id="ts-test-plan-reorder-dialog" class="not-displayed popup-dialog" title="${reorderLabel}">
      <span>
        <f:message key="message.ReorderTestPlan" />
      </span>
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" />
        <input type="button" value="${cancelLabel}" />
      </div>
    </div>

  </div>
</div>
<!-- /test plan panel end -->

<script type="text/javascript">
  require(["common"], function(){
    require(["domReady", "test-suite-management"], function(domReady, tsInit){
      
    <%--
      Note about module 'testsuite-management' :
      
      This module is usually used for the test plan of an iteration in the context of 
      the view on that iteration. There are much less features for this table in
      the context of the test plan manager. For instance one could potentially unroll the 
      list of execution, or execute them, or assign users etc : the javascript is all 
      there and are all executed.
      
      The only thing preventing those features to appear is the lack of valid targets :
      some columns in the table are missing, or doesn't have the correct css classes.
      Still, remember that the javascript here is not tailormade, nor configured with 
      specific flags, it just happens to work as is.   
      
      So, your guess : Is it cool, or risky ?
    --%>
    	
      domReady(function(){
        var conf = {
        	// permissions are hard coded because a user accessing that page 
        	// should have this following profile
            permissions : {
              linkable : true,
              editable : true,
              executable : false,
              reorderable : true
            },
            basic : {
              testsuiteId : ${testSuite.id}
            }
          };
          
        tsInit.initTestPlanPanel(conf);
      });
      
    });
  });


</script>

