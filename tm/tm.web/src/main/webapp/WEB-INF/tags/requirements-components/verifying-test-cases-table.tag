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
<%@ tag body-content="empty" description="jqueryfies a verified reqs table" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="requirementVersion" type="java.lang.Object" required="true" description="The RequirementVersion instance for which we render the verifying testcases" %>
<%@ attribute name="model" type="java.lang.Object" required="true" description="the initial rows of the table"%>
<%@ attribute name="autoJsInit" type="java.lang.Boolean" required="false" 
              description="TRANSITIONAL. If set to true, will insert the javascript initialization block. Defaults is true."%>


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<%-- ======================== VARIABLES & URLS ============================ --%>


<s:url var="tableModelUrl" value="/requirement-versions/${requirementVersion.id}/verifying-test-cases/table" />
<c:url var="verifyingTestCasesUrl" value="/requirement-versions/${ requirementVersion.id }/verifying-test-cases" />
<c:url var="tableLanguageUrl" value="/datatables/messages"/>
<c:url var="testCaseUrl" value="/test-cases"/>

<f:message var="emptyMessage" key="message.EmptyTableSelection" />
<f:message var="labelConfirm" key="label.Confirm"/>
<f:message var="labelCancel"  key="label.Cancel"/>
<f:message var="removeAssoc"  key="dialog.remove-testcase-requirement-association.message" />
<f:message var="titleError"   key="popup.title.error" />

<c:set var="tblRemoveBtnClause" value=""/>
<c:if test="${editable}" >
<c:set var="tblRemoveBtnClause" value=", delete-button=#remove-verifying-test-case-dialog" />
</c:if>

<%-- ======================== /VARIABLES & URLS ============================ --%>

        
<table id="verifying-test-cases-table" class="unstyled-table" data-def="ajaxsource=${tableModelUrl}, deferloading=${model.iTotalRecords}, 
  datakeys-id=tc-id, pre-sort=2-asc, pagesize=50 ">
  <thead>
    <tr>
      <th data-def="map=tc-index, select">#</th>
      <th data-def="map=project-name, sortable"><f:message key="label.project" /></th>
      <th data-def="map=tc-reference, sortable"><f:message key="test-case.reference.label" /></th>
      <th data-def="map=tc-name, sortable, link=${testCaseUrl}/{tc-id}/info"><f:message key="test-case.name.label" /></th>
      <th data-def="map=tc-type, sortable"><f:message key="verifying-test-cases.table.column-header.type.label"/></th>
      <th data-def="map=empty-delete-holder${tblRemoveBtnClause}">&nbsp;</th>        
    </tr>
  </thead>
  <tbody>
  </tbody>
</table>

<div id="remove-verifying-test-case-dialog" class="popup-dialog not-displayed" title="${labelConfirm}">
  <div><c:out value="${removeAssoc}"/></div>
  <div class="popup-dialog-buttonpane">
    <input class="confirm" type="button" value="${labelConfirm}" />
     <input class="cancel" type="button" value="${labelCancel}" />
  </div>
</div>

<c:if test="${empty autoJsInit or autoJsInit}" >
<script type="text/javascript">
  require([ "common" ], function() {
      require(['jquery', 'workspace.event-bus', 'squashtable', 'jquery.squash.confirmdialog'], function($, eventBus){
      $(function() {
          var table = $("#verifying-test-cases-table").squashTable({
            'aaData' : ${json:serialize(model.aaData)}
          }, {});
          
          <c:if test="${editable}">
          var removeDialog = $("#remove-verifying-test-case-dialog").confirmDialog();
          
          $( '#${batchRemoveButtonId}' ).click(function() {
            var table = $( '#verifying-test-cases-table' ).squashTable();
            var ids = table.getSelectedIds();
            
            if (ids.length > 0) {
              removeDialog.confirmDialog('open');
            } else {
              $.squash.openMessage("${titleError}","${emptyMessage}");
            }
          });
          
          removeDialog.on('confirmdialogconfirm', function(){
            var ids = table.getSelectedIds();
            $.ajax({
              url : "${verifyingTestCasesUrl}/"+ids.join(','),
              type : 'DELETE',
              dataType : 'json'
            }).success(function() {
              table.refresh();
              eventBus.trigger("node.update-reqCoverage", {targetIds : ids});
            })
          });
          </c:if>
        });
    });
  });
</script>
</c:if>
