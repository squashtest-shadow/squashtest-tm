<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<%@ tag body-content="empty"
	description="jqueryfies a verified reqs table"%>
<%@ attribute name="tableModelUrl" required="true"
	description="URL to GET the model of the table"%>
<%@ attribute name="verifiedRequirementsUrl" required="true"
	description="URL to manipulate the verified requirements"%>
<%@ attribute name="nonVerifiedRequirementsUrl" required="true"
	description="URL to manipulate the non verified requirements"%>
<%@ attribute name="batchRemoveButtonId" required="true"
	description="html id of button for batch removal of requirements"%>
<%@ attribute name="editable" type="java.lang.Boolean"
	description="Right to edit content. Default to false."%>
<%@ attribute name="updateImportanceMethod" required="false"
	description="name of the method used to update the importance of the test case when deleting requirement associations"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="input" tagdir="/WEB-INF/tags/input"%>

<script type="text/javascript">



	function findRowStatus(dataTable, rowId, getRowId, getRowStatus) {
		var rows = dataTable.fnGetNodes();
		var status="";
		
		$(rows).each(function(index, row) {
			var data = dataTable.fnGetData(row);
			var id = getRowId(data)
			if (id == rowId) {
				status = getRowStatus(data);
			}
		});
	
		return status;
	}


	function getObsoleteStatusesOfSelectedTableRows(dataTable, getRowStatus) {
		var rows = dataTable.fnGetNodes();
		var obsoleteStatuses = new Array();
		
		$(rows).each(function(index, row) {
			if ($(row).hasClass('ui-state-row-selected')) {
				var data = dataTable.fnGetData(row);
				var status = getRowStatus(data)
				if (status == "OBSOLETE") {
					obsoleteStatuses.push(status);
				}
			}
		});

		return obsoleteStatuses;
	}

	
	$(function() {
		<%-- single verified requirement removal --%>
		$('#verified-requirements-table .delete-verified-requirement-button').die('click');

		$('#verified-requirements-table .delete-verified-requirement-button').live('click', function() {
			var savedThis = this;
			var table = $( '#verified-requirements-table' ).dataTable();
			
			var id = parseRequirementId(savedThis);
			var status = findRowStatus(table, id, getRequirementsTableRowId, getRequirementsTableRowStatus);
			
			if (status == "OBSOLETE") {
				oneShotConfirm("<f:message	key='dialog.obsolete.requirement.version.removal.confirm.title' />", 
						"<f:message key='dialog.obsolete.requirement.version.removal.confirm.text' />",
						"<f:message key='dialog.button.confirm.label'/>",
						"<f:message key='dialog.button.cancel.label'/>", '600px').done(function(){deleteVerifiedRequirement(id);});
			} else {
				oneShotConfirm("<f:message key='popup.title.confirm' />", 
						"<f:message key='dialog.remove-requirement-version-association.message' />",
						"<f:message key='dialog.button.confirm.label'/>",
						"<f:message key='dialog.button.cancel.label'/>", '600px').done(function(){deleteVerifiedRequirement(id);});
			}
		});
		
		<%-- 
			selected verified requirements removal. If some of them are indirect verifications (and thus cannot be deleted) warn
			 the user about that. 
		--%>
		$( '#${ batchRemoveButtonId }' ).click(function() {
			var table = $( '#verified-requirements-table' ).dataTable();
			var ids = getIdsOfSelectedTableRows(table, getRequirementsTableRowId);
			var obsoleteStatuses = getObsoleteStatusesOfSelectedTableRows(table, getRequirementsTableRowStatus);
			var indirects = $("tr.requirement-indirect-verification.ui-state-row-selected", table);
			if (indirects.length >0){
				$.squash.openMessage("<f:message key='popup.title.error' />", '<f:message key="verified-requirements.table.indirectverifiedrequirements.removalattemptsforbidden.label"/>');
			}
			if (obsoleteStatuses.length > 0){
				oneShotConfirm("<f:message key='dialog.multiple.obsolete.requirement.versions.removal.confirm.title' />", 
						"<f:message key='dialog.multiple.obsolete.requirement.versions.removal.confirm.text' />",
						"<f:message key='dialog.button.confirm.label'/>",
						"<f:message key='dialog.button.cancel.label'/>", '600px').done(function(){deleteVerifiedRequirements(ids);});
			} else {
				oneShotConfirm("<f:message key='popup.title.confirm' />", 
						"<f:message key='dialog.remove-requirement-version-associations.message' />",
						"<f:message key='dialog.button.confirm.label'/>",
						"<f:message key='dialog.button.cancel.label'/>", '600px').done(function(){deleteVerifiedRequirements(ids);});
			}
			
		});
		
	});
	
	function deleteVerifiedRequirement(id){
		$.ajax({
			type : 'delete',
			url : '${ verifiedRequirementsUrl }/' + id,
			dataType : 'json',
			success : function(){
				refreshVerifiedRequirements();
				<c:if test="${ not empty updateImportanceMethod }" >${ updateImportanceMethod }();</c:if>
			   }
		});
	}
	
	function deleteVerifiedRequirements(ids){
		if (ids.length > 0) {
			$.post('${ nonVerifiedRequirementsUrl }', { requirementVersionsIds: ids }, refreshVerifiedRequirements)
			<c:if test="${ not empty updateImportanceMethod }" >.success(function(){${ updateImportanceMethod }();})</c:if>
			;
		}
	}
	
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
	
	function getRequirementsTableRowStatus(rowData) {
		return rowData[8];	
	}

	
	function requirementsTableRowCallback(row, data, displayIndex) {
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getRequirementsTableRowId(data), 'delete-verified-requirement-button');
		</c:if>
		addClickHandlerToSelectHandle(row, $("#verified-requirements-table"));
		addHLinkToRequirementName(row, data);
		<c:if test="${ editable }">
		addSelectEditableToVersionNumber(row, data);
		</c:if>
		return row;
	}

	
	function parseRequirementId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	 
	function addHLinkToRequirementName(row, data) {
		var url='${ pageContext.servletContext.contextPath }/requirement-versions/' + getRequirementsTableRowId(data) + '/info';			
		addHLinkToCellText($( 'td:eq(3)', row ), url);
	}	
	
	function addSelectEditableToVersionNumber(row, data) {
		var urlPOST='${ verifiedRequirementsUrl }/' + getRequirementsTableRowId(data);
		var urlGET='${ pageContext.servletContext.contextPath }/requirements/' + getRequirementsTableRowId(data) + '/versions/version-number';
		var table = $('#verified-requirements-table').dataTable();
		if (data[9]!="false"){
			<%-- the table needs to be redrawn after each return of the POST so we implement the posting workflow --%>
			$( 'td:eq(4)', row ).editable(function(value, settings) {
					var innerPOSTData;
					$.post(urlPOST, {
						value : value
					}, function (data){
						innerPOSTData = data;
						table.fnDraw(false);
					});
					return(innerPOSTData);
				}, {
				type: 'select',	
				<%-- placeholder: '<f:message key="rich-edit.placeholder" />', --%>
				submit: '<f:message key="rich-edit.button.ok.label" />',
				cancel: '<f:message key="rich-edit.button.cancel.label" />',	
				onblur : function() {}, <%-- prevents the widget to return to unediting state on blur event --%> 
				loadurl : urlGET,
				onsubmit : function() {} <%-- do nothing for now --%>
			});
		}
	}
	
	function discriminateDirectVerifications(dataTable){
		var rows = dataTable.fnGetNodes();
		var ids = new Array();

		$(rows).each(function(index, row) {
			var data = dataTable.fnGetData(row);
			if (data[9]=="false"){
				$(row).addClass("requirement-indirect-verification");
				$('td:last', row).html(''); //remove the delete button
			}else{
				$(row).addClass("requirement-direct-verification");
			}
			
		});		
	}
	
</script>



<comp:decorate-ajax-table url="${ tableModelUrl }"
	tableId="verified-requirements-table" paginate="true">
	<jsp:attribute name="initialSort">[[4,'asc']]</jsp:attribute>
	<jsp:attribute name="drawCallback">requirementsTableDrawCallback</jsp:attribute>
	<jsp:attribute name="rowCallback">requirementsTableRowCallback</jsp:attribute>
	<jsp:attribute name="columnDefs">
		<dt:column-definition targets="0" visible="false" />
		<dt:column-definition targets="1" sortable="false"
			cssClass="select-handle centered" width="2em" />
		<dt:column-definition targets="2, 3, 4, 5, 6" sortable="true" />
		<dt:column-definition targets="7" sortable="false" width="2em"
			cssClass="centered" />
		<dt:column-definition targets="8" sortable="false" visible="false" />
		<dt:column-definition targets="9" sortable="false" visible="false"
			lastDef="true" />

	</jsp:attribute>
</comp:decorate-ajax-table>