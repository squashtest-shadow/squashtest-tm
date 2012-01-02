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
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<c:url var="treeBaseUrl" value="/test-case-browser"/>
<c:url var="backUrl" value="/requirement-workspace/" />
<c:url var="verifyingTestCasesTableUrl" value="/requirements/${requirement.id}/verifying-test-cases-table" />
<c:url var="verifyingTestCasesUrl" value="/requirements/${ requirement.id }/verifying-test-cases" />
<c:url var="nonVerifyingTestCasesUrl" value="/requirements/${ requirement.id }/non-verifying-test-cases" />
<c:url var="testCaseDetailsBaseUrl" value="/test-cases" />

<layout:tree-picker-layout removeLabelKey="association_interface.remove.button.label" 
							workspaceTitleKey="workspace.requirement.title" 
							addLabelKey="association_interface.add.button.label" 
							highlightedWorkspace="requirement"
							treeBaseUrl="${treeBaseUrl}" linkable="test-case" isSubPaged="true">
							
	<jsp:attribute name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />

		<aggr:decorate-verifying-test-cases-table tableModelUrl="${ verifyingTestCasesTableUrl }" batchRemoveButtonId="remove-items-button" 
			verifyingTestCasesUrl="${ verifyingTestCasesUrl }" nonVerifyingTestCasesUrl="${ nonVerifyingTestCasesUrl }"
			testCaseDetailsBaseUrl="${ testCaseDetailsBaseUrl }" />
		
		<script type="text/javascript">
			<%-- tree population callbacks --%>

			
			function getTestCasesIds(){
				var tab =  new Array();
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
				
				<%-- verifying test-case addition --%>
				$("#back").button().click(function(){
					document.location.href="${backUrl}";
				});
				
				$( '#add-items-button' ).click(function() {
					var tree = $( '#linkable-test-cases-tree' );
					var ids = new Array();
					
					ids = getTestCasesIds();
					
					if (ids.length > 0) {
						$.post('${ verifyingTestCasesUrl }', { testCasesIds: ids}, refreshVerifyingTestCases);
					}
					tree.jstree('deselect_all');
				});
			});
		</script>
	</jsp:attribute>
	
	<jsp:attribute name="tree">
		<tree:linkables-tree iconSet="testcase"  id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" folderContentUrlHandler="folderContentUrl" driveContentUrlHandler="libraryContentUrl"/>
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
		<aggr:verifying-test-cases-table />
	</jsp:attribute>


	<jsp:attribute name="subPageTitle">
		<h2>${requirement.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifying-test-cases.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>	
	
</layout:tree-picker-layout>

