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
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<c:url var="backUrl" value="/campaign-workspace/" />
<c:url var="testPlanUrl" value="/test-suites/${testSuite.id}/test-plan/" />
<c:url var="testSuiteTestPlanUrl" value="/test-suites/${testSuite.id}/info" />


<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />
<%-- TODO : why is that no tree-picker-layout like the rest of association interface  ? --%>

<layout:tree-page-layout titleKey="squashtm"  highlightedWorkspace="campaign" isRequirementPaneSearchOn="true" linkable="test-case" isSubPaged="true">
	<jsp:attribute name="head">
	<comp:sq-css name="squash.purple.css" />

	<script type="text/javascript">
	require(["common"], function() {
		require(["jquery"], function($) {
			selection = [];
			$(function(){
	
				$( '#add-items-button' ).click(function() {
					var tree = $( '#linkable-test-cases-tree' );
					var ids = getTestCasesIds();
					if (ids.length > 0) {
						$.post('${ testPlanUrl }', { testCasesIds: ids})
						.done(function(){
							$("#test-plans-table").squashTable().refresh();
						})
					}
					tree.jstree('deselect_all'); //todo : each panel should define that method too.
					firstIndex = null;
					lastIndex = null;
				});
				
			});
		});
	});
		
		<%-- test-case addition --%>
		
		//todo : get that wtf thing straight. 
		//each panel (tree, search tc, search by req) should define a method getSelected()
		//the present function should only call the one belonging to the currently selected panel.
		function getTestCasesIds(){
			var tab =  [];
			var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
			var tree = $( '#linkable-test-cases-tree' );
			if (selected == 0){
				tab = tree.jstree('get_selected')
					  .not(':library')
					  .collect(function(elt){return $(elt).attr('resid');});
			}
			else{
				//that line is especially wtf, see seach-panel.tag
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
		<h2>${testSuite.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href='${backUrl}'"/>	
	</jsp:attribute>		
	

	<jsp:attribute name="tree">
		<tree:linkables-tree workspaceType="test-case"  elementType="testsuite" elementId="${testSuite.id}" id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
	</jsp:attribute>
	
<jsp:attribute name="contextualContent">		
	<script type="text/javascript">
	require(["common"], function() {
		require(["jquery", "jqueryui"], function($) {
			$(function(){
				$("#back").click(function(){
					document.location.href="${backUrl}";
				});
			});
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
						<f:message var="title" key="label.TestPlan"/>
						<span>${title}</span>
					</h2>
				</div>	
				<div class="unsnap"></div>
	
			</div>
			<div class="fragment-body">
				<c:url var="testSuiteUrl" value="/test-suites/${ testSuite.id }" />
				<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ testSuiteUrl }"/>
				<aggr:test-suite-test-plan-manager-table testSuite="${testSuite}" />
			</div>
		</div>
	</div>
</jsp:attribute>

<jsp:attribute name="foot">
		<f:message var ="addLabel" key="label.Add" />
		<f:message var ="removeLabel" key="subpage.association.button.disassociate.label" />
		<script type="text/javascript">
require([ "common" ], function() {
	require([ "jquery", "jqueryui" ], function($) {
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
	});
});
		</script>
	</jsp:attribute>

</layout:tree-page-layout>
