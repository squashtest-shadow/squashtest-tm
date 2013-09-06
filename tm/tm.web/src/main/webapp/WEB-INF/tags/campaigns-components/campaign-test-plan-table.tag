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
<%@ tag body-content="empty" description="jqueryfies a campaign test case table" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="assignableUsersUrl" required="true" description="URL to manipulate user of the test-cases" %>
<%@ attribute name="campaignUrl" required="true" description="the url to the campaign that hold all of these test cases" %>
<%@ attribute name="testCaseMultipleRemovalPopupId" required="true" description="html id of the multiple test-case removal popup" %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:url var="testCaseDetailsBaseUrl" value="/test-cases" />

<table id="test-cases-table">
	<thead>
		<tr>
			<th>#</th>
			<th><f:message key="label.project" /></th>
			<th><f:message key="label.Reference"/></th>
			<th><f:message key="test-case.name.label" /></th>
			<th><f:message key="test-case.user.combo.label" /></th>
			<th><f:message key="test-case.importance.combo.label" /></th>
			<th><f:message key="label.Mode" /></th>
			<th>&nbsp;</th>				
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>
	<div id="test-case-row-buttons" class="not-displayed">
	<a id="delete-test-case-button" href="javascript:void(0)" class="delete-test-case-button"><f:message key="test-case.verified_requirement_item.remove.button.label" /></a>
</div> 


<script type="text/javascript">


	function refreshTestPlan() {
		$('#test-cases-table').squashTable().refresh();
	}
	
	function refreshTestPlanWithoutSelection(){
		var table = $('#test-cases-table').squashTable();
		table.refresh();
		table.deselectRows();
	}

	function rowDataToItemId(rowData) {
		return rowData['entity-id'];	
	}

	function rowDataToItemIndex(rowData){
		return rowData['entity-index'];
	}

	function rowDataToTestCaseId(rowData){
		return rowData['tc-id'];
	}

	function addIdtoTestCaseRow(nRow, aData){
		$(nRow).attr("id", "test-plan-item:" + rowDataToItemId(aData));
	}
	
	function testPlanRowCallback(row, data, displayIndex) {
		addIdtoTestCaseRow(row, data);
		addHLinkToTestCaseName(row, data);
		return row;
	}
	

	function testPlanDrawCallback() {
		<c:if test="${ editable }">
		addLoginListToTestPlan();
		</c:if>
	}
	
	<c:if test="${ editable }">
	function addLoginListToTestPlan(){
			
		var table = $("#test-cases-table").squashTable();
		
		//look first at the cache
		var assignableList = table.data('assignable-list');
		
		if (assignableList!=null){
			table.$('td.assignable-combo').loginCombo();
		}
		
		$.get("${assignableUsersUrl}", "json")
		.success(function(json){
			table.data('assignable-list', json);
			table.$('td.assignable-combo').loginCombo();
		});

	}
	</c:if>
	

	
	function addHLinkToTestCaseName(row, data) {
		var url= '${ testCaseDetailsBaseUrl }/' + rowDataToTestCaseId(data) + '/info';			
		addHLinkToCellText($( 'td:eq(3)', row ), url);
	}	
	
    require([ "common" ], function () {
    	  require([ "jquery", "domReady", "jqueryui", "jquery.squash.datatables" ], function ($, domReady) {
    	    <c:if test="${ editable }">
    	    $.fn.loginCombo = function(assignableList){
    	    	
    	    	if (this.length==0) return;
    	    	var squashTable=$("#test-cases-table").squashTable();
    	    	var assignableList = squashTable.data('assignable-list');
    	    	if (! assignableList) return;
    	    	
    	    	//create the template
    	    	var template=$('<select/>');
    	    	for (var i=0;i<assignableList.length;i++){
    	    		var opt = '<option value="'+assignableList[i].id+'">'+assignableList[i].login+'</option>';
    	    		template.append(opt);
    	    	}
    	    	
    	    	template.change(function(){
    	    		$.ajax({
    	    			type : 'POST',
    	    			url : this.getAttribute('data-assign-url'),
    	    			data : "userId=" + this.value,
    	    			dataType : 'json'
    	    		});
    	    	});
    	    		
    	    	this.each(function(){
    	    		
    	    		var cloneSelect = template.clone(true);
    	    		
    	    		var jqTd = $(this);
    	    		var row = this.parentNode;
    	    		
    	    		
    	    		//sets the change url
    	    		var tpId = squashTable.getODataId(row);
    	    		var dataUrl = "${campaignUrl}/test-plan/"+tpId+"/assign-user";
    	    		
    	    		cloneSelect.attr('data-assign-url', dataUrl);
    	    	
    	    		//selects the assigned user
    	    		var assignedTo = squashTable.fnGetData(row)['assigned-to'] || "0";
    	    		cloneSelect.val(assignedTo);
    	    		
    	    		
    	    		//append the content
    	    		jqTd.empty().append(cloneSelect);
    	    		
    	    	});	
    	    }
    	    </c:if>
    
    	    domReady(function() {
				//multiple deletion
				$( '#${ testCaseMultipleRemovalPopupId }' ).bind('dialogclose', function() {
					var answer = $("#${ testCaseMultipleRemovalPopupId }").data("answer");
					if (answer != "yes") {
						return;
					}
					
					var table = $( '#test-cases-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table, rowDataToItemId);
					
					if (ids.length > 0) {
						$.ajax({
							url : '${ campaignUrl }/test-plan',
							type : 'POST',
							data : { 'action' : 'remove', itemIds: ids }
						})
						.success(refreshTestPlan);
					}
				
				});
		
				
				/* **************************** datatable settings ******************* */
				
				
				var tableSettings = {
					"oLanguage":{
						"sLengthMenu": '<f:message key="generics.datatable.lengthMenu" />',
						"sZeroRecords": '<f:message key="generics.datatable.zeroRecords" />',
						"sInfo": '<f:message key="generics.datatable.info" />',
						"sInfoEmpty": '<f:message key="generics.datatable.infoEmpty" />',
						"sInfoFiltered": '<f:message key="generics.datatable.infoFiltered" />',
						"oPaginate":{
							"sFirst":    '<f:message key="generics.datatable.paginate.first" />',
							"sPrevious": '<f:message key="generics.datatable.paginate.previous" />',
							"sNext":     '<f:message key="generics.datatable.paginate.next" />',
							"sLast":     '<f:message key="generics.datatable.paginate.last" />'
						}
					},				
					"sAjaxSource" : "${ campaignUrl }/test-plan/table", 
					"aLengthMenu" : [[10, 25, 50, 100, -1], [10, 25, 50, 100, '<f:message key="label.All"/>']],
					"fnRowCallback" : testPlanRowCallback,
					"fnDrawCallback" : testPlanDrawCallback,
					"aoColumnDefs": [
						{'bSortable': true,  'aTargets': [0], 'mDataProp' : 'entity-index', 'sWidth' : '2.5em', 'sClass': 'centered ui-state-default drag-handle select-handle'},
						{'bSortable': true,  'aTargets': [1], 'mDataProp' : 'project-name'},
						{'bSortable': true,  'aTargets': [2], 'mDataProp' : 'reference'},
						{'bSortable': true,  'aTargets': [3], 'mDataProp' : 'tc-name'},
						{'bSortable': true,  'aTargets': [4], 'mDataProp' : 'assigned-user', 'sClass' : 'assignable-combo'},
						{'bSortable': true,  'aTargets': [5], 'mDataProp' : 'importance'},
						{'bSortable': true,  'aTargets': [6], 'mDataProp' : 'exec-mode'},
						{'bSortable': false, 'aTargets': [7], 'mDataProp' : 'empty-delete-holder', 'sWidth': '2em', 'sClass': 'centered delete-button'}
					]
				};		
			
				var squashSettings = {
						
					enableHover : true,
					confirmPopup : {
						oklabel : '<f:message key="label.Yes" />',
						cancellabel : '<f:message key="label.Cancel" />'
					},
					functions : {
						dropHandler : function(dropData){
							$.post('${ campaignUrl }/test-case/move',dropData, function(){
								$("#test-cases-table").squashTable().refresh();
							});
						}
					}
				};
				
				<c:if test="${editable}">
				squashSettings.enableDnD = true;
		
				squashSettings.deleteButtons = {
					
					url : "${campaignUrl}/test-plan/{entity-id}",
					popupmessage : '<f:message key="dialog.remove-testcase-association.message" />',
					tooltip : '<f:message key="test-case.verified_requirement_item.remove.button.label" />',
					success : function(data) {
						refreshTestPlan();				
					}
						
				};
				</c:if>
						
				$("#test-cases-table").squashTable(tableSettings, squashSettings);		
		
			});
    	  });
    });
</script>