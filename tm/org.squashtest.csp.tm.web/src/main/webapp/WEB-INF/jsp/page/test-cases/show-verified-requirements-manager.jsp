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
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>

<c:url var="treeBaseUrl" value="/requirement-browser/"/>
<c:url var="verifiedRequirementsTableUrl" value="/test-cases/${testCase.id}/verified-requirements-table" />
<c:url var="verifiedRequirementsUrl" value="/test-cases/${ testCase.id }/verified-requirements" />
<c:url var="nonVerifiedRequirementsUrl" value="/test-cases/${ testCase.id }/non-verified-requirements" />

<layout:tree-picker-layout removeLabelKey="association_interface.remove.button.label" 
						   workspaceTitleKey="workspace.test-case.title" 
						   addLabelKey="association_interface.add.button.label" 
						   highlightedWorkspace="test-case"
						   treeBaseUrl="${treeBaseUrl}" linkable="requirement" isSubPaged="true">
						   
	<jsp:attribute name="head">
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.green.css" />
		
		<aggr:decorate-verified-requirements-table tableModelUrl="${ verifiedRequirementsTableUrl }" verifiedRequirementsUrl="${ verifiedRequirementsUrl }" 
				nonVerifiedRequirementsUrl="${ nonVerifiedRequirementsUrl }" batchRemoveButtonId="remove-items-button" />
				
		<script type="text/javascript">

			function getRequirementsIds(){
				var tab =  new Array();
				var selected = $( "#tabbed-pane" ).tabs('option', 'selected');
				var tree = $( '#linkable-requirements-tree' );
				if (selected == 0){
					tree.jstree('get_selected').each(function(index, node){
						if ($( node ).attr('resType') == 'requirements') {
							tab.push($( node ).attr('resId'));
						}
					});
				}
				if (selected == 1){
					var table = $( '#search-result-datatable' ).dataTable();
					tab = getIdsOfSelectedAssociationTableRows(table, getRequirementsTableRowId);
				}
				
				return tab;
			}
		
			
			
			$(function() {

				
				<%-- verified requirements addition --%>
				$( '#add-items-button' ).click(function() {
					var tree = $( '#linkable-requirements-tree' );
					var ids = new Array();
					ids = getRequirementsIds()
			
					if (ids.length > 0) {
						$.post('${ verifiedRequirementsUrl }', { requirementsIds: ids}, refreshVerifiedRequirements);
					}
					tree.jstree('deselect_all');
				});				
			});				
		</script>
	</jsp:attribute>
	

	
	<jsp:attribute name="tree">
		<tree:linkables-tree iconSet="requirement" id="linkable-requirements-tree" rootModel="${ linkableLibrariesModel }" folderContentUrlHandler="folderContentUrl" driveContentUrlHandler="libraryContentUrl"/>
	</jsp:attribute>
	
	<jsp:attribute name="tableTitlePane">		
		<div class="snap-left" style="height:100%;">			
			<h2>
				<f:message var="title" key="test-case.verified_requirements.panel.title"/>
				<label>${title}</label>
			</h2>
		</div>						
		<div style="clear:both;"></div>
	</jsp:attribute>
	
	<jsp:attribute name="subPageTitle">
		<h2>${testCase.name}&nbsp;:&nbsp;<f:message key="squashtm.library.verifiedrequirements.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="fragment.edit.header.button.back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="history.back();"/>	
	</jsp:attribute>		
	
	<jsp:attribute name="tablePane">
		<aggr:verified-requirements-table/>
	</jsp:attribute>
</layout:tree-picker-layout>

