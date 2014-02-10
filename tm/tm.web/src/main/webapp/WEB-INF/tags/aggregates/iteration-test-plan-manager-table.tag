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

<%@ attribute name="iteration" required="true" type="java.lang.Object" description="the iteration" %>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<f:message var="unauthorizedDeletion" key="dialog.remove-testcase-association.unauthorized-deletion.message"  />
<f:message var="cyclicStepCallException" key="squashtm.action.exception.cyclicstepcallexception.label" />


<s:url var="dtMessagesUrl" value="/datatables/messages" />
<s:url var="testplanUrl"  value="/iterations/${iteration.id}/test-plan" />
<s:url var="testcaseUrl"  value="/test-cases/{tc-id}/info" />

<table id="test-plans-table" data-def="ajaxsource=${testplanUrl}">
	<thead>
		<tr>
			<th data-def="map=entity-index, select, sClass=drag-handle, narrow">&nbsp;</th>
			<th data-def="map=project-name"><f:message key="label.project" /></th>
			<th title=<f:message key="label.Mode" /> class="no-user-select" data-def="map=exec-mode, sortable, narrow, center, visible=${iteration.project.testAutomationEnabled}, sClass=exec-mode">&nbsp;</th>
			<th data-def="map=reference"><f:message key="label.Reference"/></th>
			<th data-def="map=tc-name, link=${testcaseUrl}"><f:message key="iteration.executions.table.column-header.test-case.label" /></th>
			<th data-def="map=importance"><f:message key="iteration.executions.table.column-header.importance.label" /></th>
			<th data-def="map=dataset"><f:message key="label.Dataset" /></th>
			<th data-def="map=suite"><f:message key="iteration.executions.table.column-header.suite.label" /></th>
		</tr>
	</thead>
	<tbody><%-- Will be populated through ajax --%></tbody>
</table>



<script type="text/javascript">
require([ "common" ], function() {
	require(["jquery", "squashtable"], function($){
	var tableSettings = { 
		"fnRowCallback" : function(row, data, displayIndex) {
			
			var $row = $(row);
			
			var $exectd = $row.find('.exec-mode').text('');
			if (data['exec-mode'] === "M") {
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
					$("#test-plans-table").squashTable().refresh();
				});
			}
		}
	}
	
	
	$(function() {		
		
			
			$("#test-plans-table").squashTable(tableSettings, squashSettings);
			
			$( '#remove-items-button' ).click(function() {
				var table = $( '#test-plans-table' ).squashTable();
				var ids = table.getSelectedIds(),
					url = "${testplanUrl}/" + ids.join(',');
				
				if (ids.length > 0) {
					$.ajax({
						url : url,
						type : 'delete',
						dataType : 'json'
					})
					.done(function(data){
						if (data){
							squashtm.notification.showInfo('${ unauthorizedDeletion }');
						}
						table.refresh();
						table.deselectRows();
					});
				}
				
			});			
		});
	});
});
	
</script>
