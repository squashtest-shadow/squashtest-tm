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
<%-- 
  As of Squash TM 1.11 the content of this tag was wiped then replaced by a fork of 
  tags/iteration-components/iteration-test-plan-panel.tag

  Some features were then removed. See comments in the js initialization bloc at the end of this file.
  
 --%>
<%@ tag body-content="empty" description="the test plan panel of a campaign when displayed in the test plan manager" %>

<%@ attribute name="campaign" type="java.lang.Object" description="the instance of campaign"%>


<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<s:url var="dtMessagesUrl" value="/datatables/messages" />
<s:url var="tableModelUrl" value="/campaigns/{campId}/test-plan">
  <s:param name="campId" value="${campaign.id}" />
</s:url>
<s:url var="testcaseUrl"  value="/test-cases/{tc-id}/info" />



<div id="campaign-test-plans-panel" class="table-tab">

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
  <%--
    Because the filtering/sorting system might not like that a column may be defined or not,
    the column must always be present. It may, however, be displayed or not.
   --%>
 <c:set var="milestoneVisibility" value="${(not empty cookie['milestones']) ? '' : ', invisible'}"/>

  <div class="std-margin-top">

    <table id="campaign-test-plans-table" class="test-plan-table unstyled-table"
      data-def="ajaxsource=${tableModelUrl}"  data-entity-id="${campaign.id}" data-entity-type="campaign">
      <thead>
        <tr>
          <th class="no-user-select"
            data-def="map=entity-index, select, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
          <th class="no-user-select tp-th-filter tp-th-project-name" data-def="map=project-name, sortable">
            <f:message key="label.project" />
          </th>
          <th class="no-user-select" data-def="sortable, map=milestone-dates ${milestoneVisibility}">
            <f:message key="label.Milestone"/>
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
          <th class="no-user-select" data-def="map=empty-delete-holder, unbind-button=#unbind-test-case-dialog">&nbsp;</th>
        </tr>
      </thead>
      <tbody>
        <%-- Will be populated through ajax --%>
      </tbody>
    </table>


    <%-- ============================== THE DIALOGS ========================= --%>


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

    <div class="popup-dialog-buttonpane">
      <input type="button" value="${assignLabel}" data-def="state=assign, mainbtn=assign, evt=confirm" />
      <input type="button" value="${cancelLabel}" data-def="evt=cancel" />
    </div>
  </div>
  
  <script id="delete-dialog-tpl" type="text/x-handlebars-template">
  <div id="{{dialogId}}" class="not-displayed popup-dialog" title="<f:message key='dialog.remove-testcase-associations.title'/>">
    <div data-def="state=confirm-deletion">
      <span><f:message key="dialog.remove-testcase-associations.message"/></span>
    </div>
    <div data-def="state=multiple-tp">
      <span><f:message key="dialog.remove-testcase-associations.message.multiple"/></span>
      <span><f:message key="message.permissions.confirm"/></span>
    </div>
    <div class="popup-dialog-buttonpane">
      <input type="button" class="button" value="${okLabel}" data-def="evt=confirm, mainbtn"/>
      <input type="button" class="button" value="${cancelLabel}" data-def="evt=cancel"/>
    </div>
  </div>
  </script>

  </div>
</div>
<!-- /test plan panel end -->

<script type="text/javascript">
  require(["common"], function(){
    require(["domReady", "campaign-management"], function(domReady, campInit){
      
    <%--
      Note about module 'campaign-management' :
      
      This module is usually used for the test plan of an campaign in the context of 
      the view on that campaign. There are much less features for this table in
      the context of the test plan manager. The javascript is all 
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
        	
        	// (also remember that the campaigns have a conf object that differs 
        	// slightly from the one for the iterations. That's why we have here 
        	// 'features' instead of 'permissions' and 'data' instead of 'basic')
        	//  
            features : {
              linkable : true,
              editable : true,
              executable : false,
              reorderable : true
            },
            data : {
              campaignId : ${campaign.id}
            }
          };
          
        campInit.initTestPlanPanel(conf);
      });
      
    });
  });


</script>


