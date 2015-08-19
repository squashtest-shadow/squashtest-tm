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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>


<c:url var="backUrl" value="/requirement-workspace/" />
<c:url var="requirementUrl" value="/requirements/${ requirement.id }" />
<c:url var="verifyingTestCasesUrl" value="/requirement-versions/${ requirementVersion.id }/verifying-test-cases" />

<layout:tree-picker-layout  workspaceTitleKey="workspace.requirement.title" 
              highlightedWorkspace="requirement"
              linkable="test-case" 
              isSubPaged="true">
              
  <jsp:attribute name="head">
    <comp:sq-css name="squash.blue.css" />
  </jsp:attribute>
  
  <jsp:attribute name="tree">
    <tree:linkables-tree workspaceType="test-case" elementType="requirement" elementId="${requirementVersion.id}" id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
  </jsp:attribute>
  
  <jsp:attribute name="tableTitlePane">    
      <div class="snap-left" style="height:100%;">      
        <h2>
          <f:message var="title" key="requirement.verifying_test-case.panel.title"/>
          <span>${title}</span>
        </h2>
      </div>  
      <div class="unsnap"></div>
  </jsp:attribute>
  <jsp:attribute name="tablePane">
    <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ requirementUrl }" />
    
    <aggr:decorate-verifying-test-cases-table editable="true" model="${verifyingTestCaseModel}" requirementVersion="${requirementVersion}" batchRemoveButtonId="none"/>
        
    <div id="add-summary-dialog" class="not-displayed" title="<f:message key='requirement-version.verifying-test-case.add-summary-dialog.title' />">
      <ul><li>summary message here</li></ul>
    </div>
  </jsp:attribute>

  <jsp:attribute name="subPageTitle">
    <h2>${requirementVersion.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
  </jsp:attribute>
  
  <jsp:attribute name="subPageButtons">
    <f:message var="backButtonLabel" key="label.Back" />
    <input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href='${backUrl}'"/>  
  </jsp:attribute>  
  
  
  <jsp:attribute name="foot">

    <script type="text/javascript">
require([ "common" ], function() {
  require([ "jquery","workspace.event-bus", "workspace.tree-event-handler", "jqueryui", "jquery.squash.messagedialog", "squashtable" ], function($, eventBus, treehandler) {
    $(function() {
      //the case 'get ids from the research tab' is disabled here, waiting for refactoring. 
      function getTestCasesIds(){
        var ids =  [];
        var nodes = $( '#linkable-test-cases-tree' ).jstree('get_selected').not(':library').treeNode();
        if (nodes.length>0){
          ids = nodes.all('getResId');
        }
      
        return $.map(ids, function(id){ return parseInt(id);});
      }
      
      $( "#add-summary-dialog" ).messageDialog();

      var summaryMessages = {
        alreadyVerifiedRejections: "<f:message key='requirement-version.verifying-test-case.already-verified-rejection' />",
        notLinkableRejections: "<f:message key='requirement-version.verifying-test-case.not-linkable-rejection' />"
      };

      var showAddSummary = function(summary) {
        if (summary) {
          var summaryRoot = $( "#add-summary-dialog > ul" );
          summaryRoot.empty();
          
          for(var rejectionType in summary) {
            var message = summaryMessages[rejectionType];
            
            if (message) {
              summaryRoot.append('<li>' + message + '</li>');
            }
          }
          
            if (summaryRoot.children().length > 0) {
              $( "#add-summary-dialog" ).messageDialog("open");
            }
        }
          
      };
      
      $( '#add-items-button' ).click(function() {
        var tree = $('#linkable-test-cases-tree');
        var table = $("#verifying-test-cases-table").squashTable();
        var ids = getTestCasesIds();
        
        if (ids.length > 0) {
          $.ajax({
            url : '${ verifyingTestCasesUrl }/'+ids.join(','),
            type : 'POST', 
            dataType :'json'
          })
          .success(function(data){
            showAddSummary(data);
            table.refresh();
            sendUpdateTree(data.linkedIds);  
          });
        }
        tree.jstree('deselect_all');
      });
      
      $("#remove-items-button").click(function(){
        var table = $("#verifying-test-cases-table").squashTable();
        var ids = table.getSelectedIds();
        $.ajax({
          url : '${verifyingTestCasesUrl}/'+ids.join(','),
          type : 'DELETE',
          dataType : 'json'
        }).success(function(){
          table.refresh();
          sendUpdateTree(ids);  
        });
      });
      
      function sendUpdateTree(ids){
        eventBus.trigger("node.update-reqCoverage", {targetIds : ids});
      }
    });
  });
});
    </script>  
  </jsp:attribute>
</layout:tree-picker-layout>

