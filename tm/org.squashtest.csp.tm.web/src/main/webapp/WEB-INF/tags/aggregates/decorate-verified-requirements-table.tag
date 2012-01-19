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
<%@ tag body-content="empty" description="jqueryfies a verified reqs table"%>
<%@ attribute name="tableModelUrl" required="true" description="URL to GET the model of the table"%>
<%@ attribute name="verifiedRequirementsUrl" required="true" description="URL to manipulate the verified requirements"%>
<%@ attribute name="nonVerifiedRequirementsUrl" required="true"
	description="URL to manipulate the non verified requirements"%>
<%@ attribute name="batchRemoveButtonId" required="true"
	description="html id of button for batch removal of requirements"%>
<%@ attribute name="editable" type="java.lang.Boolean" description="Right to edit content. Default to false." %>
<%@ attribute name="updateImportanceMethod" required="false" description="name of the method used to update the importance of the test case when deleting requirement associations" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
	
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<script type="text/javascript">
	$(function() {
		<%-- single verified requirement removal --%>
		
		$('#verified-requirements-table .delete-verified-requirement-button').die('click');

		$('#verified-requirements-table .delete-verified-requirement-button').live('click', function() {
			$.ajax({
				type : 'delete',
				url : '${ verifiedRequirementsUrl }/' + parseRequirementId(this),
				dataType : 'json',
				success : function(){
					refreshVerifiedRequirements();
					<c:if test="${ not empty updateImportanceMethod }" >${ updateImportanceMethod }();</c:if>
				   }
			});
		});
		
		<%-- 
			selected verified requirements removal. If some of them are indirect verifications (and thus cannot be deleted) warn
			 the user about that. 
		--%>
		$( '#${ batchRemoveButtonId }' ).click(function() {
			var table = $( '#verified-requirements-table' ).dataTable();
			var ids = getIdsOfSelectedTableRows(table, getRequirementsTableRowId);
			var indirects = $("tr.requirement-indirect-verification", table);
			if (indirects.length >0){
				alert('<f:message key="verified-requirements.table.indirectverifiedrequirements.removalattemptsforbidden.label"/>');
			}
			
			if (ids.length > 0) {
				$.post('${ nonVerifiedRequirementsUrl }', { requirementsIds: ids }, refreshVerifiedRequirements)
				<c:if test="${ not empty updateImportanceMethod }" >.success(function(){${ updateImportanceMethod }();})</c:if>
				;
			}
		});
		
		
	});
	
	
	function refreshVerifiedRequirements() {
		var table = $('#verified-requirements-table').dataTable();
		saveTableSelection(table, getRequirementsTableRowId);
		table.fnDraw(false);
	}

	
	function requirementsTableDrawCallback() {
		decorateDeleteButtons($('.delete-verified-requirement-button', this));
		restoreTableSelection(this, getRequirementsTableRowId);
		discriminateDirectVerifications(this);
	}

	
	function getRequirementsTableRowId(rowData) {
		return rowData[0];	
	}

	
	function requirementsTableRowCallback(row, data, displayIndex) {
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getRequirementsTableRowId(data), 'delete-verified-requirement-button');
		</c:if>
		addClickHandlerToSelectHandle(row, $("#verified-requirements-table"));
		addHLinkToRequirementName(row, data);
		return row;
	}

	
	function parseRequirementId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	
	function addHLinkToRequirementName(row, data) {
		var url= '${ pageContext.servletContext.contextPath }/requirements/' + getRequirementsTableRowId(data) + '/info';			
		addHLinkToCellText($( 'td:eq(3)', row ), url);
	}	
	
	
	function discriminateDirectVerifications(dataTable){
		var rows = dataTable.fnGetNodes();
		var ids = new Array();

		$(rows).each(function(index, row) {
			var data = dataTable.fnGetData(row);
			if (data[7]=="false"){
				$(row).addClass("requirement-indirect-verification");
				$('td:last', row).html(''); //remove the delete button
			}else{
				$(row).addClass("requirement-direct-verification");
			}
			
		});		
	}
	
</script>
<comp:decorate-ajax-table url="${ tableModelUrl }" tableId="verified-requirements-table" paginate="true">
	<jsp:attribute name="initialSort">[[4,'asc']]</jsp:attribute>
	<jsp:attribute name="drawCallback">requirementsTableDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">requirementsTableRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" />
		<dt:column-definition targets="1" sortable="false" cssClass="select-handle centered" width="2em"/>
		<dt:column-definition targets="2, 3, 4, 5" sortable="true" />
		<dt:column-definition targets="6" sortable="false" width="2em" cssClass="centered"/>
		<dt:column-definition targets="7" sortable="false" visible="false" lastDef="true"  />

	</jsp:attribute>
</comp:decorate-ajax-table>