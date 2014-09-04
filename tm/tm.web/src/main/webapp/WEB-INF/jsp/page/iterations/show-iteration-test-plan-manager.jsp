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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="it" tagdir="/WEB-INF/tags/iterations-components"%>

<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<c:url var="backUrl" value="/campaign-workspace/" />
<c:url var="testPlanUrl" value="/iterations/${iteration.id}/test-plan/" />
<c:url var="iterationUrl" value="/iterations/${iteration.id}/" />
<c:url var="iterationTestPlanUrl" value="/iterations/${iteration.id}/info" />

<layout:tree-picker-layout  workspaceTitleKey="workspace.campaign.title"
                            i18nLibraryTabTitle="squashtm.library.test-case.title" 
                            highlightedWorkspace="campaign" 
                            linkable="test-case" 
                            isSubPaged="true">
                            
	<jsp:attribute name="head">
    	<comp:sq-css name="squash.purple.css" />
    
    	<script type="text/javascript">
    	require(["common"], function() {
    		require(["jquery", "tree", "workspace.event-bus"], function($, zetree, eventBus) {
            	$(function(){
            		
            		$( '#add-items-button' ).on('click', function() {
            			var tree = zetree.get();
            			var ids = tree.jstree('get_selected').all('getResId');
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
            		
    				$("#back").click(function(){
    					document.location.href="${backUrl}";
    				});
    				
    				
            		
            	});
    		});
    	});
    	</script>	
	</jsp:attribute>
    
      
  <jsp:attribute name="subPageTitle">
    <h2>${iteration.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
  </jsp:attribute>
    
    
  <jsp:attribute name="subPageButtons">
    <f:message var="backButtonLabel" key="label.Back" />
    <input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href='${backUrl}'"/>  
  </jsp:attribute>    
  
  <jsp:attribute name="tree">
    <tree:linkables-tree workspaceType="test-case"  elementType="iteration" elementId="${iteration.id}" id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
  </jsp:attribute>

    <jsp:attribute name="tableTitlePane">		
      <div class="snap-left" style="height:100%;">			
        <h2>      	
      	   <span><f:message key="label.TestPlan"/></span>
      	</h2>
      </div>	
      <div class="unsnap"></div>
    </jsp:attribute>
	
  
  <jsp:attribute name="tablePane">
    <comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ iterationUrl }" />
    <aggr:iteration-test-plan-manager-table iteration="${iteration}"/>      
    <it:test-suite-managment  iteration="${iteration}"/>  
  </jsp:attribute>


  <jsp:attribute name="foot">
  	<script type="text/javascript">
          require([ "common" ], function() {
          	require(["jquery", "iteration-management"], function($, iterManager) {
          		$(function(){				
          			iterManager.initEvents({});
          		});
          	});
          });
  	</script>
  </jsp:attribute>

</layout:tree-picker-layout>
