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
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>


<c:url var="backUrl" value="/campaign-workspace/" />
<c:url var="treeBaseUrl" value="/test-case-browser/" />
<c:url var="testPlansTableUrl" value="/iterations/${iteration.id}/test-cases" />
<c:url var="testCasesUrl" value="/iterations/${ iteration.id }/test-cases" />
<c:url var="removeTestPlanUrl" value="/iterations/${ iteration.id }/test-plan" />
<c:url var="nonBelongingTestPlansUrl" value="/iterations/${ iteration.id }/non-belonging-test-cases" />

<c:url var="testPlanDetailsBaseUrl" value="/test-cases" />

<s:url var="updateTestPlanUrl" value="/iterations/{iterId}/test-case/">
	<s:param name="iterId" value="${iteration.id}" />
</s:url>

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

			storeSelection();
			
		});
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
		
		function storeSelection(){
			$('a.ui-squashtest-tree-inactive').live('click', function(){
				node = $(this).parent("li");
				if ($(node).attr('rel') != "drive")
				{
					if($(this).hasClass("jstree-clicked")){
						if ($(node).attr('rel') == "file"){
							selection.push($(node).attr('resid'));
						}else{
							selection.push(-$(node).attr('resid'))
						}
					}else{
						for(var i = 0; i <selection.length; i++){
							if(selection[i] == $(node).attr('resid') || selection[i] == -$(node).attr('resid')){
								for(var j = i; j <selection.length; j++){
									selection[j] = selection[j + 1];
								}
								selection.pop();
							}
						}
					}
				}
			});
		}
		
		
		
		
	</script>
		
		
		<%-- 
			table js 
			Note : we duplicate part of the code of aggregate:decorate-iteration-test-plan-table, because we don't need 
			all the features that manage its executions	
		--%>
		<script type="text/javascript">
			
		$(function() {
			
			$( '#remove-test-case-button' ).click(function() {
					var table = $( '#test-plan-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table);
					
					if (ids.length > 0) {
						$.post('${ nonBelongingTestPlansUrl }', { testPlanIds: ids }, function(data){
							refreshTestPlans();
							checkForbiddenDeletion(data);
						});
					}
					
					$( 'tr.row_selected', table ).removeClass('row_selected');
				});
				
				<%-- enable table row selection --%>
				$( '#test-plan-table tbody tr:not(.delete-test-case-button)' ).live('click', function() {
					 $( this ).toggleClass('row_selected');
				});		
		
		
			<%-- single test-case removal --%>
			$('#test-plan-table .delete-test-case-button').live('click', function() {
				$.ajax({
					type : 'delete',
					url : '${ removeTestPlanUrl }/' + parseTestPlanId(this),
					dataType : 'text',
					success : function(data){
						refreshTestPlans();
						checkForbiddenDeletion(data);
					}
				});
			});
			
			<%-- test-case addition --%>
				$( '#add-test-case-button' ).click(function() {
					<%-- var tree = $( '#linkable-test-cases-tree' );
					var ids = new Array();
					tree.jstree('get_selected').each(function(index, node){
						if ($( node ).attr('resType') == 'test-cases') {
							ids.push($( node ).attr('resId'));
						}
						// TODO : manage folder case
					});
					--%>
					var tree = $( '#linkable-test-cases-tree' );
					//tree selection
					var ids = selection;
					//tabs selection
					if(selectedTab != 0){
						ids = getIdSelection();
					}
					
					if (ids.length > 0) {
						$.post('${ testCasesUrl }', { testCasesIds: ids}, refreshTestPlans);
					}
					tree.jstree('deselect_all');
					//reset the multiple selection fields
					firstIndex = null;
					lastIndex = null;
					selection = [];
				});
			
		});
			
		//This function checks the response and inform the user if a deletion was impossible
		function checkForbiddenDeletion(data){
			if(data=="true"){
				displayInformationNotification('${ unauthorizedDeletion }');
			}
		}

		//for drag and drop test case feature
		//row : selected row
		//dropPosition : the new position
		function testPlanDropHandler(row, dropPosition) {
			//first compose the url to update the order, then send a request attribute newIndex and call the refresh function
			$.post('${ updateTestPlanUrl }' + parseTestPlanId(row), { newIndex : dropPosition }, function(){
				refreshTestPlans();
			}) ;
		}
		
		function refreshTestPlans() {
			var table = $('#test-plan-table').dataTable();
			saveTableSelection(table, getTestPlansTableRowId);
			table.fnDraw(false);
		}
		
		function getTestPlansTableRowId(rowData) {
			return rowData[0];	
		}
		
		function getTestPlanTableRowIndex(rowData){
			return rowData[1];
		}
		
		function isTestCaseDeleted(rowData){
			return (rowData[6]=="true");
		}
		
		function getTestCaseId(rowData){
			return rowData[5];
		}
		

		function testPlanTableDrawCallback() {
			enableTableDragAndDrop('test-plan-table', getTestPlanTableRowIndex, testPlanDropHandler);
			decorateDeleteButtons($('.delete-test-case-button', this));
			restoreTableSelection(this, getTestPlansTableRowId);
		}



		function testPlanTableRowCallback(row, data, displayIndex) {
			addIdtoTestPlanRow(row, data);
			addDeleteButtonToRow(row, getTestPlansTableRowId(data), 'delete-test-case-button');
			addClickHandlerToSelectHandle(row, $("#test-plan-table"));
			addHLinkToTestPlanName(row, data);
			return row;
		}
		
		function addIdtoTestPlanRow(nRow, aData){
			$(nRow).attr("id", "test-case:" + getTestPlansTableRowId(aData));
		}

		function parseTestPlanId(element) {
			var elementId = element.id;
			return elementId.substr(elementId.indexOf(":") + 1);
		}
		
		function addHLinkToTestPlanName(row, data) {
			if (! isTestCaseDeleted(data) ){
				var url= '${ testPlanDetailsBaseUrl }/' + getTestCaseId(data) + '/info';		
				addHLinkToCellText($( 'td:eq(2)', row ), url);
			}
		}	
		
		
		<%-- returns list of id of selected row --%>
			function getIdsOfSelectedTableRows(dataTable) {
				var rows = dataTable.fnGetNodes();
				var ids = new Array();
				
				$( rows ).each(function(index, row) {
					if ($( row ).attr('class').search('selected') != -1) {
						var data = dataTable.fnGetData(row);
						ids.push(data[0]);
					}
				});
				
				return ids;
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
		<tree:linkables-tree iconSet="testcase"  id="linkable-test-cases-tree" rootModel="${ linkableLibrariesModel }" folderContentUrlHandler="folderContentUrl" driveContentUrlHandler="libraryContentUrl"/>
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
					<f:message var="addLabel" key="association_interface.add.button.label" />
					<input id="add-test-case-button" type="button" value="${ addLabel }" class="button" 
					style="margin-bottom:15px;width:30px;"/>  

				
					<f:message var="removeLabel" key="association_interface.remove.button.label" />
					<input id="remove-test-case-button" type="button" value="${ removeLabel }" class="button" 					
					style="margin-top:15px;width:30px;"/>
				</div>
			</div>

			<div id="tree-picker-target-pane">
				<div class="ui-widget-header ui-corner-all fragment-header">
		
					<div style="float:left;height:100%;">			
						<h2>
							<f:message var="title" key="campaign.test-plan.panel.title"/>
							<label>${title}</label>
						</h2>
					</div>	
					<div style="clear:both;"></div>
		
				</div>
				
				
				<comp:decorate-ajax-table url="${ testPlansTableUrl }" tableId="test-plan-table" paginate="true">
					<jsp:attribute name="drawCallback">testPlanTableDrawCallback</jsp:attribute>
					<jsp:attribute name="rowCallback">testPlanTableRowCallback</jsp:attribute>
						<jsp:attribute name="initialSort">[[2,'asc']]</jsp:attribute>
						<jsp:attribute name="columnDefs">
							<dt:column-definition targets="0" visible="false" />
							<dt:column-definition targets="1" sortable="false" cssClass="centered ui-state-default drag-handle select-handle" />
							<dt:column-definition targets="2, 3, 4" sortable="true" />
							<dt:column-definition targets="5, 6" sortable="false" visible="false"/>
							<dt:column-definition targets="7" sortable="false" width="2em" lastDef="true" cssClass="centered"/>
						</jsp:attribute>
				</comp:decorate-ajax-table>
				<div class="fragment-body">
					<table id="test-plan-table">
						<thead>
							<tr>
								<th>test plan Id</th>
								<th>&nbsp;</th>
								<th><f:message key="iteration.executions.table.column-header.project.label" /></th>
								<th><f:message key="iteration.executions.table.column-header.test-case.label" /></th>
								<th><f:message key="iteration.executions.table.column-header.type.label" /></th>
								<th>test case id</th>
								<th>is deleted</th>
								<th>&nbsp;</th>				
							</tr>
						</thead>
						<tbody><%-- Will be populated through ajax --%></tbody>
					</table>
				 	<div id="test-case-row-buttons" class="not-displayed">
						<a id="delete-test-case-button" href="#" class="delete-test-case-button"><f:message key="test-case.verified_requirement_item.remove.button.label" /></a>
					</div> 
				</div>
			</div>
		</div>
	</jsp:attribute>

</layout:tree-page-layout>
