<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org

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
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<c:url var="treeBaseUrl" value="/test-case-browser"/>
<c:url var="campaignUrl" value="/campaigns/${ campaign.id }" />


<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ campaign }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>

<layout:tree-picker-layout  workspaceTitleKey="workspace.campaign.title" 
							highlightedWorkspace="campaign"
							treeBaseUrl="${treeBaseUrl}"
							isRequirementPaneSearchOn="true" linkable="test-case" isSubPaged="true">
	<jsp:attribute name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />

		<aggr:decorate-campaign-test-plan-manager-table campaignUrl="${ campaignUrl }" 
			batchRemoveButtonId="remove-items-button" editable="${editable}"/>
		
		<script type="text/javascript">
			
			//todo : get that wtf thing straight. 
			//each panel (tree, search tc, search by req) should define a method getSelected()
			//the present function should only call the one belonging to the currently selected panel.
			function getTestCasesIds(){
				var tab =  new Array();
				var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
				var tree = $( '#linkable-test-cases-tree' );
				if (selected == 0){
					tab = tree.jstree('get_selected')
						  .not(':library')
						  .collect(function(elt){return $(elt).attr('resid');});
				}
				else{
					//that line is especially wtf, see seach-panel.tag and search-panel-by-requirement.tag
					//to understand what I mean.
					tab = getIdSelection();
				}
				return tab;
			}
			

			
			$(function() {

				<%-- back button --%>
				
				$("#back").button().click(function(){
					history.back();
				});
				
				<%-- test-case addition --%>
				$( '#add-items-button' ).click(function() {
					
					var tree = $( '#linkable-test-cases-tree' );
					var ids = getTestCasesIds();
					if (ids.length > 0) {
						$.post('<c:url value="/campaigns/${ campaign.id }/test-plan" />', { testCasesIds: ids }, refreshTestPlan);
					}
					tree.jstree('deselect_all'); //todo : each panel should define that method too.
					firstIndex = null;
					lastIndex = null;
				});
			});
		</script>
	</jsp:attribute>
	
		<jsp:attribute name="subPageTitle">
		<h2>${campaign.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>	
	
	
	<jsp:attribute name="tree">
		<tree:linkables-tree workspaceType="test-case"  id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
	</jsp:attribute>

	<jsp:attribute name="tableTitlePane">		
		<div class="snap-left" style="height:100%;">	
			<h2>
				<f:message var="title" key="campaign.test-plan.panel.title"/>
				<span>${title}</span>
			</h2>
		</div>	
		<div style="clear:both;"></div>
	</jsp:attribute>	
	<jsp:attribute name="tablePane">
		<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ campaignUrl }" isContextual="false"/>
		<aggr:campaign-test-plan-manager-table />
	</jsp:attribute>
</layout:tree-picker-layout>

