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
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<c:url var="backUrl" value="/campaign-workspace/" />
<c:url var="treeBaseUrl" value="/test-case-browser/" />
<c:url var="testPlansTableUrl" value="${ baseURL }/test-cases/table" />
<c:url var="testPlanUrl" value="${ baseURL }/test-cases" />
<c:url var="removeTestPlanUrl" value="${ baseURL }/test-plan" />
<c:url var="nonBelongingTestPlansUrl" value="${ baseURL }/non-belonging-test-cases" />

<c:url var="testPlanDetailsBaseUrl" value="/test-cases" />

<c:url var="updateTestPlanUrl" value="${ baseURL }/test-case/" />

<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />
<%-- TODO : why is that no tree-picker-layout like the rest of association interface  ? --%>

<layout:tree-page-layout titleKey="squashtm"  highlightedWorkspace="campaign" isRequirementPaneSearchOn="true" linkable="test-case" isSubPaged="true">
	<jsp:attribute name="head">
	<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.purple.css" />

	<jq:execution-status-factory/> 
	<dt:datatables-header />

	<script type="text/javascript">
		selection = new Array();
		$(function(){
			navLinkHighlight("campaign-link");
			
			$( '#add-items-button' ).click(function() {
				var tree = $( '#linkable-test-cases-tree' );
				var ids = getTestCasesIds();
				if (ids.length > 0) {
					$.post('${ testPlanUrl }', { testCasesIds: ids}, refreshTestPlans);
				}
				tree.jstree('deselect_all'); //todo : each panel should define that method too.
				firstIndex = null;
				lastIndex = null;
			});
			
		});
		
		<%-- test-case addition --%>
		
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
	</script>	
		<%-- 
			tree js 
				
		--%>
	<script type="text/javascript">
		
		<%-- tree population callbacks --%>
		function libraryContentUrl(node) {
			return nodeContentUrl('${ treeBaseUrl }', node);
		}
		
		function folderContentUrl(node) {
			return nodeContentUrl('${ treeBaseUrl }', node);
		}
				
		
	</script>
		
		


			
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<f:message var="workspaceTitle" key="workspace.campaign.title" />		
		<h2>${ workspaceTitle }</h2>	
	</jsp:attribute>	
	
	<jsp:attribute name="subPageTitle">
		<h2>${iteration.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>		
	

	<jsp:attribute name="tree">
		<tree:linkables-tree workspaceType="test-case"  id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
	</jsp:attribute>
	
<jsp:attribute name="contextualContent">		
	<script type="text/javascript">
		$(function(){
			$("#back").button().click(function(){
				document.location.href="${backUrl}";
			});
		});
	</script>
	
	<div style="overflow:hidden;height:100%;">
	
		<div  id="tree-picker-actions-pane" class="centered">
			<div style="position:absolute;top:45%;margin-right:2em;">
				<div id="add-items-button" class="association-button" ></div>  
				<div id="remove-items-button" class="association-button" ></div>
			</div>
		</div>

		<div id="tree-picker-target-pane">
			<div class="ui-widget-header ui-corner-all fragment-header">
	
				<div style="float:left;height:100%;">			
					<h2>
						<f:message var="title" key="campaign.test-plan.panel.title"/>
						<span>${title}</span>
					</h2>
				</div>	
				<div style="clear:both;"></div>
	
			</div>
			<div class="fragment-body">
			<c:if test="${ useIterationTable }">
				<aggr:decorate-iteration-test-plan-manager-table tableModelUrl="${testPlansTableUrl}" testPlanDetailsBaseUrl="${testPlanDetailsBaseUrl}" 
					testPlansUrl="${removeTestPlanUrl}" batchRemoveButtonId="remove-items-button" 
					updateTestPlanUrl="${updateTestPlanUrl}" nonBelongingTestPlansUrl="${nonBelongingTestPlansUrl}" />
				<aggr:iteration-test-plan-manager-table/>
			</c:if>
			<c:if test="${ not useIterationTable }">
				<aggr:decorate-test-suite-test-plan-manager-table tableModelUrl="${testPlansTableUrl}" testPlanDetailsBaseUrl="${testPlanDetailsBaseUrl}" 
					testPlansUrl="${removeTestPlanUrl}" batchRemoveButtonId="remove-items-button"
					updateTestPlanUrl="${updateTestPlanUrl}" nonBelongingTestPlansUrl="${nonBelongingTestPlansUrl}" />
				<aggr:test-suite-test-plan-manager-table/>
			</c:if>
			</div>
		</div>
	</div>
</jsp:attribute>

<jsp:attribute name="foot">
		<f:message var ="addLabel" key="subpage.association.button.associate.label" />
		<f:message var ="removeLabel" key="subpage.association.button.disassociate.label" />
		<script type="text/javascript">
			$(function(){				
				$("#add-items-button").button({
					disabled : false,
					text : "${addLabel}",
					icons : {
						primary : "ui-icon-seek-next"
					}
				});		
				$("#remove-items-button").button({
					disabled : false,
					text : "${removeLabel}",
					icons : {
						primary : "ui-icon-seek-prev"
					}
				});	
			});
		</script>
	</jsp:attribute>

</layout:tree-page-layout>
