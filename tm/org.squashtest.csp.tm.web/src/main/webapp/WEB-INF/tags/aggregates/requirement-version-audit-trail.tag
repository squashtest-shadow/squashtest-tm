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
<%@ tag body-content="empty" %>
<%@ attribute name="requirementVersion" required="true" type="java.lang.Object" rtexprvalue="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="cmp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%-- AUDIT TRAIL --%>
<script type="text/javascript">
	function getAuditTrailTableRowId(rowData) {
		return rowData[4];	
	}

	function auditTrailTableRowCallback(row, data, displayIndex) {
		if (data[3] == 'fat-prop') {
			var eventId = getAuditTrailTableRowId(data);
			
			var proto = $( '#show-audit-event-details-template' ).clone();
			proto.removeClass('not-displayed')
				.find( 'a' )
					.attr( 'id', 'show-audit-event-detail:' + eventId )
					.click(function() {
						showPropChangeEventDetails(eventId);
					});
			
			$( ':nth-child(3)', row ).append( proto ); //nth-child is 1-based !
		}

		return row;
	}
	
	function showPropChangeEventDetails(eventId) {
		var urlRoot = "${ pageContext.servletContext.contextPath }/audit-trail/requirement-versions/fat-prop-change-events/";
		
		$.getJSON( urlRoot + eventId, function(data, textStatus, xhr) {
			var dialog = $( "#audit-event-details-dialog" );
			$( "#audit-event-old-value", dialog ).html(data.oldValue);
			$( "#audit-event-new-value", dialog ).html(data.newValue);
			dialog.messageDialog("open");
		});
	}
</script>
<c:url var="requirementAuditTrailTableModelUrl" value="/audit-trail/requirement-versions/${ requirementVersion.id }/events-table" />
<cmp:toggle-panel id="requirement-audit-trail-panel" titleKey="audit-trail.requirement.panel.title" open="false">
	<jsp:attribute name="body">
		<cmp:decorate-ajax-table url="${ requirementAuditTrailTableModelUrl }" tableId="requirement-audit-trail-table" paginate="true" displayLength="10">
			<jsp:attribute name="rowCallback">auditTrailTableRowCallback</jsp:attribute>
			<jsp:attribute name="columnDefs">
				<dt:column-definition targets="0,1,2" visible="true" />
				<dt:column-definition targets="3" visible="false" />
				<dt:column-definition targets="4" visible="false" lastDef="true" />
			</jsp:attribute>
		</cmp:decorate-ajax-table>
		<div>
			<table id="requirement-audit-trail-table">
				<thead>
					<tr>
						<th><f:message key="audit-trail.requirement.table.col-header.date.label" /></th>
						<th><f:message key="audit-trail.requirement.table.col-header.author.label" /></th>
						<th><f:message key="audit-trail.requirement.table.col-header.event.label" /></th>
						<th>&nbsp;</th>
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody>
					<tr class="hidden">
						<td>date</td>
						<td>author</td>
						<td>message</td>
						<td>event type</td>
						<td>event id</td>
					</tr>
				</tbody>
			</table>
		</div> 
	</jsp:attribute>
</cmp:toggle-panel>

<span id="show-audit-event-details-template" class="not-displayed">&nbsp;<a id="show-audit-event-details" href="javascript:void(0)"><f:message key="audit-trail.requirement.property-change.show-details.label" /></a></span>

<script type="text/javascript">
	$(function() {
		$( "#requirement-audit-trail-table" ).ajaxSuccess(function(event, xrh, settings) {
			if (settings.type == 'POST' 
					&& !(settings.data && settings.data.match(/requirement-status/g))
					&& !settings.url.match(/versions\/new$/g)) {
				<%-- We refresh tble on POSTs which do not uptate requirement status or create a new version (these ones already refresh the whole page) --%>
				$( this ).dataTable().fnDraw(false);
			}
		});
	});
</script>
<%-- /AUDIT TRAIL --%>

<%-- AUDIT EVENT DETAILS --%>	
<f:message var="auditEventDetailsDialogTitle" key="audit-trail.requirement.property-change.show-details.title" />	
<div id="audit-event-details-dialog" class="not-displayed popup-dialog" title="${ auditEventDetailsDialogTitle }">
	<div class="display-table">
		<div>
			<label for="audit-event-old-value"><f:message key="audit-trail.requirement.property-change.old-value.label" /></label>
			<span id="audit-event-old-value">old value</span>
		</div>
		<div class="display-table-row">
			<label for="audit-event-new-value"><f:message key="audit-trail.requirement.property-change.new-value.label" /></label>
			<span id="audit-event-new-value">new value</span>
		</div>
		<input:ok />
	</div>
</div>
<script type="text/javascript">
	$(function() {
		$( "#audit-event-details-dialog" ).messageDialog();
	});
</script>
<%-- /AUDIT EVENT DETAILS --%>	
