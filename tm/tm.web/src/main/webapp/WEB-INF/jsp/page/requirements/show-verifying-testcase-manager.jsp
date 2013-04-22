<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

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
<c:url var="treeBaseUrl" value="/test-case-browser"/>
<c:url var="backUrl" value="/requirement-workspace/" />
<c:url var="requirementUrl" value="/requirements/${ requirement.id }" />
<c:url var="verifyingTestCasesTableUrl" value="/requirement-versions/${ requirementVersion.id }/verifying-test-cases/table" />
<c:url var="verifyingTestCasesUrl" value="/requirement-versions/${ requirementVersion.id }/verifying-test-cases" />
<c:url var="nonVerifyingTestCasesUrl" value="/requirement-versions/${ requirementVersion.id }/non-verifying-test-cases" />

<layout:tree-picker-layout  workspaceTitleKey="workspace.requirement.title" 
							highlightedWorkspace="requirement"
							treeBaseUrl="${treeBaseUrl}" linkable="test-case" isSubPaged="true">
							
	<jsp:attribute name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />

		<aggr:decorate-verifying-test-cases-table tableModelUrl="${ verifyingTestCasesTableUrl }" batchRemoveButtonId="remove-items-button" 
			verifyingTestCasesUrl="${ verifyingTestCasesUrl }" nonVerifyingTestCasesUrl="${ nonVerifyingTestCasesUrl }" />
		
		<script type="text/javascript">
			<%-- tree population callbacks --%>
			
			function getTestCasesIds(){
				var tab =  [];
				var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
				var tree = $( '#linkable-test-cases-tree' );
				if (selected == 0){
					tab = tree.jstree('get_selected')
						  .not(':library')
						  .collect(function(elt){return $(elt).attr('resid');});
				}
				if (selected == 1){
					var table = $( '#search-result-datatable' ).dataTable();
					tab = getIdsOfSelectedAssociationTableRows(table, getTestCasesTableRowId);
				}
				return tab;
			}
			
			$(function() {
				$( "#add-summary-dialog" ).messageDialog();
				
				<%-- verifying test-case addition --%>
				$("#back").button().click(function() {
					document.location.href="${backUrl}";
				});

				var summaryMessages = {
					alreadyVerifiedRejections: "<f:message key='requirement-version.verifying-test-case.already-verified-rejection' />",
					notLinkableRejections: "<f:message key='requirement-version.verifying-test-case.not-linkable-rejection' />"
				};
				
				var showAddSummary = function(summary) {
					if (summary) {
						var summaryRoot = $( "#add-summary-dialog > ul" );
						summaryRoot.empty();
						
						for(rejectionType in summary) {
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
				
				var addHandler = function(data) {
					showAddSummary(data);
					refreshVerifyingTestCases();
				};
				
				$( '#add-items-button' ).click(function() {
					var tree = $( '#linkable-test-cases-tree' );
					var ids = [];
					
					ids = getTestCasesIds();
					
					if (ids.length > 0) {
						$.post('${ verifyingTestCasesUrl }', { testCasesIds: ids}, addHandler);
					}
					tree.jstree('deselect_all');
				});
			});
		</script>
	</jsp:attribute>
	
	<jsp:attribute name="tree">
		<tree:linkables-tree workspaceType="test-case"  id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" />
	</jsp:attribute>
	
	<jsp:attribute name="tableTitlePane">		
			<div class="snap-left" style="height:100%;">			
				<h2>
					<f:message var="title" key="requirement.verifying_test-case.panel.title"/>
					<span>${title}</span>
				</h2>
			</div>	
			<div style="clear:both;"></div>
	</jsp:attribute>
	<jsp:attribute name="tablePane">
		<comp:opened-object otherViewers="${ otherViewers }" objectUrl="${ requirementUrl }" isContextual="false"/>
		<aggr:verifying-test-cases-table />
		<div id="add-summary-dialog" class="not-displayed" title="<f:message key='requirement-version.verifying-test-case.add-summary-dialog.title' />">
			<ul><li>summary message here</li></ul>
		</div>
	</jsp:attribute>

	<jsp:attribute name="subPageTitle">
		<h2>${requirementVersion.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>	
	
</layout:tree-picker-layout>

