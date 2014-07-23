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
<%@ tag body-content="empty" description="jqueryfies a campaign test case table" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="reorderable" type="java.lang.Boolean" description="Right to reorder the test plan. Default to false." %>
<%@ attribute name="assignableUsersUrl" required="true" description="URL to manipulate user of the test-cases" %>
<%@ attribute name="campaignUrl" required="true" description="the url to the campaign that hold all of these test cases" %>
<%@ attribute name="testCaseMultipleRemovalPopupId" required="true" description="html id of the multiple test-case removal popup" %>
<%@ attribute name="campaign" required="true" type="java.lang.Object" description="the instance of the campaign"%>
<%@ attribute name="assignableUsers" type="java.lang.Object" description="a map of users paired by id -> login. The id must be a string."%>
<%@ attribute name="weights" type="java.lang.Object" description="a map of weights paired by id -> internationalized text. The id must be a string."%>	
<%@ attribute name="modes" type="java.lang.Object" description="a map of modes paired by id -> internationalized text. The id must be a string."%>	


<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<c:url var="testCaseUrl" value="/test-cases/{tc-id}/info" />
<c:url var="dtMessagesUrl" value="/datatables/messages" />

<f:message var="assignLabel"	key="label.Assign"/>
<f:message var="confirmLabel"	key="label.Confirm"/>
<f:message var="cancelLabel"	key="label.Cancel"/>
<f:message var="reorderLabel"	key="label.Reorder" />

<%-- be careful that the variable below is a 'var', not a 'url'. It's so because 'campaignUrl' is already an URL. Just another detail to get straight one day... --%>
<c:set var="tablemodel" value="${campaignUrl}/test-plan/table" />

<c:if test="${editable}">
	<c:set var="deleteBtnClause" value=", delete-button=#delete-multiple-test-cases-dialog"/>
</c:if>
<table id="test-cases-table" data-def="ajaxsource=${tablemodel}" class="unstyled-table test-plan-table" data-entity-id="${campaign.id}" data-entity-type="campaign">
	<thead>
		<tr>
			<th class="no-user-select" data-def="map=entity-index, select, sortable, center, sClass=drag-handle, sWidth=2.5em">#</th>
			<th class="no-user-select tp-th-project-name" data-def="map=project-name, sortable"><f:message key="label.project" /></th>
			<th class="no-user-select tp-th-exec-mode" data-def="map=exec-mode, sortable, center, visible=${campaign.project.testAutomationEnabled}, sClass=exec-mode"><f:message key="label.Mode" /></th>
			<th class="no-user-select tp-th-reference" data-def="map=reference, sortable"><f:message key="label.Reference"/></th>
			<th class="no-user-select tp-th-name" data-def="map=tc-name, sortable, link=${testCaseUrl}"><f:message key="test-case.name.label" /></th>
			<th class="no-user-select tp-th-assignee" data-def="map=assigned-user, sortable, sWidth=10%, sClass=assignee-combo"><f:message key="test-case.user.combo.label" /></th>
			<th class="no-user-select tp-th-importance" data-def="map=importance, sortable"><f:message key="test-case.importance.combo.label" /></th>
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



<div id="camp-test-plan-batch-assign" class="not-displayed popup-dialog" title="<f:message key="label.AssignUser"/>">
	<div data-def="state=assign">
		<span><f:message key="message.AssignTestCaseToUser"/></span>
		<select class="batch-select">
			<c:forEach var="user" items="${assignableUsers}">
			<option value="${user.key}">${user.value}</option>		
			</c:forEach>
		</select>
	</div>	
	<span data-def="state=empty-selec"><f:message key="message.EmptyTableSelection"/></span>
	
	<div class="popup-dialog-buttonpane"> 
		<input type="button" value="${assignLabel}" data-def="state=assign, mainbtn=assign, evt=confirm"/>
		<input type="button" value="${cancelLabel}" data-def="mainbtn=empty-select, evt=cancel"/>
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

	
    require([ "common" ], function () {
    	  require([ "jquery", "domReady", 
    	            "campaign-management", 
    	            "jqueryui", "squashtable" ], function ($, domReady, manager) {

    
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
						campaignId : ${campaign.id},
						assignableUsers : ${ json:serialize(assignableUsers)},
						weights	: ${ json:serialize(weights)},
						modes : ${ json:serialize(modes)}
						
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