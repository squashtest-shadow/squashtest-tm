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
<%@ attribute name="campaignUrl" required="true" description="the url to the campaign that hold all of these test cases" %>
<%@ attribute name="batchRemoveButtonId" required="true" description="html id of button for batch removal of test cases" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="campaign" type="java.lang.Object" description="The campaign." %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:url var="testCaseUrl" value="/test-cases/{tc-id}/info" />
<c:url var="testplanUrl"  value="/campaigns/${campaign.id}/test-plan" />
<c:url var="dtMessagesUrl" value="/datatables/messages" />

<%-- be careful that the variable below is a 'var', not a 'url'. It's so because 'campaignUrl' is already an URL. Just another detail to get straight one day... --%>
<c:set var="tablemodel" value="${campaignUrl}/test-plan/table" />

<table id="test-cases-table" data-def="ajaxsource=${tablemodel}">
	<thead>
		<tr>
			<th data-def="map=entity-index, select,center, sClass=drag-handle">#</th>
			<th data-def="map=project-name"><f:message key="label.project" /></th>
			<th data-def="map=exec-mode, center, visible=${iteration.project.testAutomationEnabled}, sClass=exec-mode"><f:message key="label.Mode" /></th>
			<th data-def="map=reference"><f:message key="label.Reference"/></th>
			<th data-def="map=tc-name, link=${testCaseUrl}"><f:message key="test-case.name.label" /></th>
			<th data-def="map=assigned-user, sWidth=10%"><f:message key="test-case.user.combo.label" /></th>
			<th data-def="map=importance"><f:message key="test-case.importance.combo.label" /></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>
<div id="test-case-row-buttons" class="not-displayed">
	<a id="delete-test-case-button"  class="delete-test-case-button"><f:message key="test-case.verified_requirement_item.remove.button.label" /></a>
</div> 
<script type="text/javascript">
require(["common"], function(){
require(["jquery","squashtable"], function($){
	var tableSettings = { 
		"fnRowCallback" : function(row, data, displayIndex) {
			
			var $row = $(row);
			
			var $exectd = $row.find('.exec-mode').text('');
			if (data['exec-mode'] === "A") {
				$exectd.append('<span class"exec-mode-icon exec-mode-manual"/>').attr('title', '');
			} else {
				var label =  "<f:message key="label.automatedExecution"/>";
				$exectd.append('<span class="exec-mode-icon exec-mode-automated"/>').attr('title',
						label);
			}
			
		}
	};	
	
	var squashSettings = {
		enableDnD : true,
		functions : {
			dropHandler : function(dropData){
				var ids = dropData.itemIds.join(',');
				var url	= "${testplanUrl}/" + ids + '/position/' + dropData.newIndex;		
				$.post(url, function(){
					$("#test-cases-table").squashTable().refresh();
				});
			}
		}
	}

	$(function() {

		
			$("#test-cases-table").squashTable(tableSettings, squashSettings);
			
			<%-- selected test-case removal --%>
			$( '#${ batchRemoveButtonId }' ).click(function() {
				var table = $( '#test-cases-table' ).squashTable();
				var ids = table.getSelectedIds();
				
				if (ids.length > 0) {
					$.post('${ campaignUrl }/test-plan', { action: 'remove', itemIds: ids }).done(function(){
						table.refresh();
					});
				}
			});
			
		});
	});
});
	
</script>

