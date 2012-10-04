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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>

<%@ attribute name="entity" type="java.lang.Object"  description="the entity to which we bind those attachments" %>
<%@ attribute name="editable" type="java.lang.Boolean" description="List of attachments is editable. Defaults to false." %>
<%@ attribute name="tabId" description="id of the concerned tab" required="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

<%------------------------------------- URLs --------------------------------------------------------%>
<c:set var="rootUrl" value="attach-list/${entity.attachmentList.id}/attachments" />
<s:url var="prefixedRootUrl" value="/{rootUrl}">
	<s:param name="rootUrl" value="${rootUrl}"></s:param>
</s:url>
<s:url var="uploadAttachmentUrl" value="/{rootUrl}/upload">
	<s:param name="rootUrl" value="${rootUrl}"></s:param>
</s:url>
<s:url var="attachmentDetailsUrl" value="/{rootUrl}/details">
	<s:param name="rootUrl" value="${rootUrl}"></s:param>
</s:url>
<s:url var="attachmentRemoveUrl" value="/{rootUrl}">
	<s:param name="rootUrl" value="${rootUrl}"></s:param>
</s:url>
<s:url var="attachmentRemoveListUrl" value="/{rootUrl}/removed-attachments">
	<s:param name="rootUrl" value="${rootUrl}"></s:param>
</s:url>
<s:url var="renameAttachmentUrl" value="/{rootUrl}">
	<s:param name="rootUrl" value="${rootUrl}"></s:param>
</s:url>
<%------------------------------------- /URLs --------------------------------------------------------%>
<%------------------------------------- scripts ------------------------------------------------------%>
<script type="text/javascript">

	//init function
	$(function() {
		<%-- single verified requirement removal --%>
		$('#attachment-detail-table .delete-attachment-button').live('click', function() {
			$("#delete-attachment-dialog").data('opener', this).dialog('open');
		});
		
		<%-- selected verified requirements removal --%>
		//overload the close handler
		$("#delete-all-attachment-dialog").bind('dialogclose', function() {
			var answer = $("#delete-all-attachment-dialog").data("answer");
			if (answer != "yes") {
				return;
			}

			var removeids = $("#delete-all-attachment-dialog").data("IDList");


			$.ajax({
				type : 'POST',
				data : {
					attachmentIds : removeids
				},
				url : "${attachmentRemoveListUrl}",
				success : refreshAttachments

			});


		});

		$("#delete-all-attachment-button").click(function() {
			deleteSelectedAttachments();
			return false;
		});
		
		<%-- renaming button--%>
		
		$("#rename-attachment-button").click(function(){
			checkAndOpenRenameDialog();
		});
	});
	
	function refreshAttachments() {
		var table = $('#attachment-detail-table').dataTable();
		saveTableSelection(table, getAttachmentsTableRowId);
		table.fnDraw(false);
	}
	
	function attachmentsTableDrawCallback() {
		<c:if test="${ editable }">
		decorateDeleteButtons($('.delete-attachment-button', this));
		</c:if>
		restoreTableSelection(this, getAttachmentsTableRowId);
	}
	
	function getAttachmentsTableRowId(rowData) {
		return rowData[0];	
	}
	
	function attachmentsTableRowCallback(row, data, displayIndex) {
		<c:if test="${ editable }">
		addDeleteButtonToRow(row, getAttachmentsTableRowId(data), 'delete-attachment-button');
		</c:if>
		addClickHandlerToSelectHandle(row, $("#attachment-detail-table"));
		addHLinkToAttachmentName(row, data);
		return row;
	}
	
	function parseAttachmentId(element) {
		var elementId = element.id;
		return elementId.substr(elementId.indexOf(":") + 1);
	}
	
	function addHLinkToAttachmentName(row, data) {
		var url= '${prefixedRootUrl}/download/' + getAttachmentsTableRowId(data) ;			
		addHLinkToCellText($( 'td:eq(1)', row ), url);
	}	

	
	function deleteSelectedAttachments(){
		
		var datatable = $("#attachment-detail-table").dataTable();
		
		var selectedIDs = getIdsOfSelectedTableRows(datatable,getAttachmentsTableRowId);
		
		if (selectedIDs.length==0) {
			<f:message var="removeAttachImpossible" key="message.SelectAttachmentsToRemove"/>
			$.squash.openMessage("<f:message key='popup.title.error' />", "${removeAttachImpossible}");
			return false;
		}
		
		var removedStepIds = new Array();
		
		$("#delete-all-attachment-dialog").data("answer","no");
		$("#delete-all-attachment-dialog").data("IDList",selectedIDs);
			
		$("#delete-all-attachment-dialog").dialog("open");
	
}
	
	function getAttachmentShortName(name){
		var index = name.lastIndexOf(".");
		return name.substring(0,index);
	}
	
	function getAttachmentNameById(zeId){
		var datatable = $("#attachment-detail-table").dataTable();		
		var rows = datatable.fnGetNodes();
		if (rows.length==0) return false;
		
		var name;
		
		$(rows).each(function(index, row) {
			var data = datatable.fnGetData(row);
			if (data[0]==zeId){
				name=data[2];
			}
		});
		
		return name;
	}
	
	function checkAndOpenRenameDialog(){
		var datatable = $("#attachment-detail-table").dataTable();		
		var selectedIDs = getIdsOfSelectedTableRows(datatable,getAttachmentsTableRowId);		
		
		if (selectedIDs.length!=1){
			<f:message var="renameAttachImpossible" key="message.CanRenameOnlyOneAttachment"/>
			$.squash.openMessage("<f:message key='popup.title.error' />", "${renameAttachImpossible}");
		}		
		else{
			var zeId = selectedIDs[0];
			$("#rename-attachment-dialog").data("attachId",zeId);
			$("#rename-attachment-dialog").dialog("open");
		}
	}
	
</script>
<%------------------------------------- /scripts ------------------------------------------------------%>
<div id="${tabId}" class="table-tab">

<div class="toolbar" >
<c:if test="${ editable }">
		<f:message var="uploadAttachment" key="label.UploadAttachment" />
		<input id="add-attachment" type="button" value="${uploadAttachment}" class="button"/>
		<f:message var="renameAttachment" key="label.Rename" />
		<input type="button" value="${renameAttachment}" id="rename-attachment-button" class="button" />
		<f:message var="removeAttachment" key="label.Remove" />
		<input type="button" value="${removeAttachment}" id="delete-all-attachment-button" class="button" />
</c:if>
</div>
<%---------------------------------Attachments table ------------------------------------------------%>


<div class="table-tab-wrap" >
	<comp:decorate-ajax-table url="${attachmentDetailsUrl}" tableId="attachment-detail-table" paginate="true">
		<jsp:attribute name="rowCallback">attachmentsTableRowCallback</jsp:attribute>
		<jsp:attribute name="drawCallback">attachmentsTableDrawCallback</jsp:attribute>
		<jsp:attribute name="columnDefs">
			<dt:column-definition targets="0" visible="false" />
			<dt:column-definition targets="1" visible="true" cssClass="select-handle" width="2em"/>
			<dt:column-definition targets="2" visible="false" />
		    <dt:column-definition targets="3,4,5" visible="true" cssClass="centered" sortable="true"/>
			<dt:column-definition targets="6" visible="true" width="2em" lastDef="true" cssClass="centered"/>
		</jsp:attribute>					
	</comp:decorate-ajax-table>
	<table id="attachment-detail-table">
		<thead>
			<tr>
				<th>Id</th>
				<th>#</th>
				<th>(notdisplayed)</th>	
				<th><f:message key="label.Name"/></th>	
				<th><f:message key="label.SizeMb"/></th>
				<th><f:message key="label.AddedOn"/></th>
				<th>&nbsp;</th> 
			</tr>
		</thead>
		<tbody>
			<%-- Will be populated through ajax --%>
		</tbody>
	</table>
	<div id="attachment-row-buttons" class="not-displayed">
		<a id="delete-attachment-button" class="delete-attachment-button" >
		<f:message key="label.Remove" /></a>
	</div>
</div>
<%--------------------------------- /Attachments table ------------------------------------------------%>

<comp:decorate-buttons />
</div>
			
<%------------------------------------------------- Dialogs ----------------------------------%>
<c:if test="${ editable }">
<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
<pop:popup id="delete-attachment-dialog" titleKey="title.RemoveAttachment" isContextual="true" openedBy="delete-attachment-button">
	<jsp:attribute name="buttons">
		<f:message var="label" key="label.Confirm" />

		'${ label }': function() {
		
			var bCaller = $.data(this,"opener");
			var url = "${ attachmentRemoveUrl }/" + parseAttachmentId(bCaller); 
			<jq:ajaxcall url="url" dataType='json' httpMethod="DELETE" successHandler="refreshAttachments">					
			</jq:ajaxcall>					
		},			
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="body">
		<b><f:message key="message.ConfirmRemoveAttachment" /></b>
		<br />				
	</jsp:attribute>
</pop:popup>


<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
<pop:popup id="delete-all-attachment-dialog" titleKey="title.RemoveAttachment" isContextual="true"
	openedBy="delete-attachment-button">
	<jsp:attribute name="buttons">
			<f:message var="label" key="label.Yes" />
				'${ label }' : function(){
						$("#delete-all-attachment-dialog").data("answer","yes");
						$("#delete-all-attachment-dialog").dialog("close");
				},
				
				<pop:cancel-button />
						
	</jsp:attribute>
	<jsp:attribute name="body">
		<b><f:message key="message.ConfirmRemoveAttachments" /></b>
		<br />				
	</jsp:attribute>
</pop:popup>


<%-- 
we need a hook before opening the dialog, hence we do not bind rename-attachment-button directly here.
check the init function in the javascript above to find the real binding.
 --%>
<comp:popup id="rename-attachment-dialog" titleKey="title.RenameAttachment" isContextual="true"
	openedBy="delete-attachment-button">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="label.Rename" />
		'${ label }': function() {
			var id = $("#rename-attachment-dialog").data("attachId");
			var url = "${ renameAttachmentUrl }/"+id;
			<jq:ajaxcall url="url" dataType="json" httpMethod="POST" useData="true" successHandler="refreshAttachments">					
				<jq:params-bindings newName="#rename-attachment-input" />
			</jq:ajaxcall>					
		},			
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:body>
		<script type="text/javascript">
	$("#rename-attachment-dialog").bind("dialogopen", function(event, ui) {
		var id = $("#rename-attachment-dialog").data("attachId");
		var name = getAttachmentNameById(id);
		var rename = getAttachmentShortName(name);
		$("#rename-attachment-input").val(rename);

	});
	</script>
		<label><f:message key="dialog.rename.label" /></label>
		<input type="text" id="rename-attachment-input" size="40"/>
		<br />
		<comp:error-message forField="shortName" />	

	</jsp:body>
</comp:popup>

<comp:add-attachment-popup url="${uploadAttachmentUrl}" paramName="attachment" openedBy="add-attachment" submitCallback="refreshAttachments" />
</c:if>
<%------------------------------------------------- /Dialogs ----------------------------------%>
