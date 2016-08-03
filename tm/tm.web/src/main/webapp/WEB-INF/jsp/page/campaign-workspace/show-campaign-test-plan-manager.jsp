<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2016 Henix, henix.fr

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
<%@ taglib prefix="camp" tagdir="/WEB-INF/tags/campaigns-components" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>


<c:url var="campaignUrl" value="/campaigns/${ campaign.id }" />
<c:url var="testPlanUrl" value="/campaigns/${campaign.id}/test-plan"/>


<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK" domainObject="${ campaign }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>

<layout:tree-picker-layout  workspaceTitleKey="workspace.campaign.title" 
							highlightedWorkspace="campaign" 
                            i18nLibraryTabTitle="squashtm.library.test-case.title"                             
							linkable="test-case" 
                            isSubPaged="true">
	<jsp:attribute name="head">
		<comp:sq-css name="squash.purple.css" />
	</jsp:attribute>
	
	<jsp:attribute name="subPageTitle">
		<h2>${campaign.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" id="back" class="sq-btn button" value="${backButtonLabel}" 
                onClick="document.location.href=squashtm.workspace.backurl;" /> 	
	</jsp:attribute>	
	
	
	<jsp:attribute name="tree">
		<tree:linkables-tree workspaceType="test-case"  elementType="campaign" elementId="${campaign.id}" id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
	</jsp:attribute>

	<jsp:attribute name="tableTitlePane">		
		<div class="snap-left" style="height:100%;">	
			<h2>
				<f:message var="title" key="label.TestPlan"/>
				<span>${title}</span>
			</h2>
		</div>	
		<div class="unsnap"></div>
	</jsp:attribute>	
	<jsp:attribute name="tablePane">
		<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ campaignUrl }" />
		
		<camp:campaign-test-plan-manager-table campaign="${campaign}" milestoneConf="${milestoneConf}"/>
		
	</jsp:attribute>
  
  
  <jsp:attribute name="foot">
    <script type="text/javascript">
        require(["common"], function(){
          require(["jquery", "tree", "workspace.event-bus", "squash.translator", "app/ws/squashtm.notification", "app/ws/squashtm.workspace"], function($, zetree, eventBus, msg, notification){
              
              $(function() {
            	  
                <%-- test-case addition --%>
                $( '#add-items-button' ).click(function() {
         					var tree = zetree.get('#linkable-test-cases-tree'); 
        					var ids =	[];
        					var nodes = 0;
        					if( tree.jstree('get_selected').length > 0 ) {
        						 nodes = tree.jstree('get_selected').not(':library').treeNode();
        						 ids = nodes.all('getResId');
        					}	


        					if (ids.length === 0) {
        						notification.showError(msg.get('message.emptySelectionTestCase'));
        						
        					}
        					
        					if (ids.length > 0) {
        						 $.post('${ testPlanUrl }', { testCasesIds: ids})
                  				   .done(function(){
                   				    eventBus.trigger('context.content-modified');
        							})
        					}
                      tree.jstree('deselect_all');  
                });
                
                $("#remove-items-button").on('click', function(){
                  
                  $("#remove-test-plan-button").click();
                });
                
                eventBus.onContextual("context.content-modified", function() {
                  $("#campaign-test-plans-table").squashTable().refresh();
                });
                    
              });
            });
          });
    </script>
  </jsp:attribute>
  
</layout:tree-picker-layout>

