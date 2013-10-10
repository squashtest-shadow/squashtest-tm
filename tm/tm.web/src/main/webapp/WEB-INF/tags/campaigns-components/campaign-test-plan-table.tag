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
<%@ attribute name="reorderable" type="java.lang.Boolean" description="Right to reorder the test plan. Default to false." %>
<%@ attribute name="assignableUsersUrl" required="true" description="URL to manipulate user of the test-cases" %>
<%@ attribute name="campaignUrl" required="true" description="the url to the campaign that hold all of these test cases" %>
<%@ attribute name="testCaseMultipleRemovalPopupId" required="true" description="html id of the multiple test-case removal popup" %>
<%@ attribute name="campaign" required="true" type="java.lang.Object" description="the instance of the campaign"%>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:url var="testCaseUrl" value="/test-cases/{tc-id}/info" />
<c:url var="dtMessagesUrl" value="/datatables/messages" />

<f:message var="confirmLabel"	key="label.Confirm"/>
<f:message var="cancelLabel"	key="label.Cancel"/>
<f:message var="reorderLabel"	key="label.Reorder" />

<%-- be careful that the variable below is a 'var', not a 'url'. It's so because 'campaignUrl' is already an URL. Just another detail to get straight one day... --%>
<c:set var="tablemodel" value="${campaignUrl}/test-plan/table" />

<c:if test="${editable}">
	<c:set var="deleteBtnClause" value=", delete-button=#delete-multiple-test-cases-dialog"/>
</c:if>
<table id="test-cases-table" data-def="ajaxsource=${tablemodel}">
	<thead>
		<tr>
			<th class="no-user-select" data-def="map=entity-index, select, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
			<th class="no-user-select" data-def="map=project-name, sortable"><f:message key="label.project" /></th>
			<th class="no-user-select" data-def="map=reference, sortable"><f:message key="label.Reference"/></th>
			<th class="no-user-select" data-def="map=tc-name, sortable, link=${testCaseUrl}"><f:message key="test-case.name.label" /></th>
			<th class="no-user-select" data-def="map=assigned-user, sortable, sWidth=10%"><f:message key="test-case.user.combo.label" /></th>
			<th class="no-user-select" data-def="map=importance, sortable"><f:message key="test-case.importance.combo.label" /></th>
			<th class="no-user-select" data-def="map=exec-mode, sortable"><f:message key="label.Mode" /></th>
			<th class="no-user-select" data-def="map=empty-delete-holder${deleteBtnClause}">&nbsp;</th>				
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>
<div id="test-case-row-buttons" class="not-displayed">
	<a id="delete-test-case-button"  class="delete-test-case-button"><f:message key="test-case.verified_requirement_item.remove.button.label" /></a>
</div> 


<div id="camp-test-plan-reorder-dialog" class="not-displayed popup-dialog" title="${reorderLabel}" >
	<span><f:message key="message.ReorderTestPlan"/></span>
	<div class="popup-dialog-buttonpane"> 
		<input type="button" value="${confirmLabel}"/> 
		<input type="button" value="${cancelLabel}"/> 
	</div>
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

	
    require([ "common" ], function () {
    	  require([ "jquery", "domReady", 
    	            "campaign-management", 
    	            "jqueryui", "squashtable" ], function ($, domReady, manager) {
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
					
					var ids = this.selIds;
					
					if (ids.length > 0) {
						$.ajax({
							url : '${ campaignUrl }/test-plan',
							type : 'POST',
							data : { 'action' : 'remove', itemIds: ids }
						})
						.success(refreshTestPlan);
					}
				
				});
		
				
				/* **************************** partial main stub ******************* */

				var mainconf = {
					basic :{
						campaignId : ${campaign.id}	
					},
					permissions : {
						editable : ${editable},
						reorderable : ${reorderable},
					},
					messages : {
						allLabel : '<f:message key="label.All"/>'
					}
				};
				
				manager.initTestPlanPanel(mainconf);
				
		
			});
    	  });
    });
</script>