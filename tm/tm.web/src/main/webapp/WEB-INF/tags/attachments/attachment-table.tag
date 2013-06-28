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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>

<%@ attribute name="entity" type="java.lang.Object"  description="the entity to which we bind those attachments. Either this, or 'attachListId', is required."%>
<%@ attribute name="attachListId" type="java.lang.Long" description="the id of the attachment list. Either this, or 'entity', is required." %>
<%@ attribute name="editable" type="java.lang.Boolean" description="List of attachments is editable. Defaults to false." required="true"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments" %>

<%------------------------------------- URLs --------------------------------------------------------%>
<c:choose>
<c:when test="${not empty entity }">
	<s:url var="baseURL" value="/attach-list/${entity.attachmentList.id}/attachments" />
</c:when>
<c:when test="${not empty attachListId }">
	<s:url var="baseURL" value="/attach-list/${attachListId}/attachments" />
</c:when>
</c:choose>


<c:set var="uploadAttachmentUrl"  value="${baseURL}/upload"/>
<c:set var="attachmentDetailsUrl" value="${baseURL}/details"/>

<c:url var="datatableLanguage" value="/datatables/messages" />

<f:message var="errorTitle" key="popup.title.error" />
<f:message var="nothingSelected" key="message.EmptyTableSelection"/>

<%------------------------------------- /URLs --------------------------------------------------------%>

<%---------------------------------Attachments table ------------------------------------------------%>


<%-- datatable conf --%>
<c:set var="tableDeleteConf" value=""/>
<c:if  test="${editable}"> <c:set var="tableDeleteConf" value=", delete-button=#delete-attachment-dialog"/></c:if>
	
<table id="attachment-detail-table" class="" data-def="ajaxsource=${attachmentDetailsUrl}, 
											  language=${datatableLanguage}, hover, pagesize=10, pre-sort=2">
	<thead>
		<tr>
			<th data-def="map=entity-index, select, narrow, center">#</th>
			<th data-def="map=hyphenated-name, sortable, center, link=${baseURL}/download/{entity-id}"><f:message key="label.Name"/></th>	
			<th data-def="map=size, center, sortable"><f:message key="label.SizeMb"/></th>
			<th data-def="map=added-on, center, sortable"><f:message key="label.AddedOn"/></th>
			<th data-def="map=empty-delete-holder ${tableDeleteConf}">&nbsp;</th> 
		</tr>
	</thead>
	<tbody>
		<%-- Will be populated through ajax --%>
	</tbody>
</table>
<%--------------------------------- /Attachments table ------------------------------------------------%>

			
<%------------------------------------------------- Dialogs ----------------------------------%>
<c:if test="${ editable }">


<f:message var="confirmLabel" key="label.Confirm"/>
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="deleteDialogTitle" key="title.RemoveAttachment"/>
<div id="delete-attachment-dialog" title="${deleteDialogTitle}">
	<span style="font-weight:bold"><f:message key="message.ConfirmRemoveAttachments" /></span>
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}"/>
		<input type="button" value="${cancelLabel}"/>
	</div>
</div>

<f:message var="renameDialogTitle" key="title.RenameAttachment" />
<div id="rename-attachment-dialog" title="${renameDialogTitle}">
	<label for="rename-attachment-input"><f:message key="dialog.rename.label" /></label>
	<input type="text" id="rename-attachment-input" size="50"/>
	<br />
	<comp:error-message forField="shortName" />
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}"/>
		<input type="button" value="${cancelLabel}"/>
	</div>	
</div>


<at:add-attachment-popup url="${uploadAttachmentUrl}" paramName="attachment" openedBy="add-attachment-button" />
</c:if>
<%------------------------------------------------- /Dialogs ----------------------------------%>
<%------------------------------------- scripts ------------------------------------------------------%>
<script type="text/javascript">

	//init function
	$(function() {
				
		require(["jquery", "jqueryui","jquery.squash.datatables", "jquery.squash.confirmdialog"], function($){

			function refreshAttachments() {
				$('#attachment-detail-table').squashTable().refresh();
			}
			
			//**********   table init ***********
			
			var table = $("#attachment-detail-table").squashTable({}, {});

			
			// ************* delete dialog init (if any) **************
			
			var deleteDialog = $("#delete-attachment-dialog");
			
			deleteDialog.confirmDialog();
			
			deleteDialog.on('confirmdialogconfirm', function(){
					
					var removedIds = table.getSelectedIds().join(',');
					var url = "${baseURL}"+"/"+removedIds;
					
					$.ajax({
						type : 'DELETE',
						url : url
					}).done(function(){
						deleteDialog.confirmDialog('close');
						refreshAttachments();		
					});
				});
			
			//************* rename dialog init (if any) ********
			
			var renameDialog = $("#rename-attachment-dialog");
			
			renameDialog.confirmDialog();
			
			renameDialog.on("confirmdialogconfirm", function(){
				
				var id = renameDialog.data("attachmentId");
				var url = "${baseURL}/"+id+"/name";
				var newName = $("#rename-attachment-input").val();
				
				$.ajax({
					url : url,
					type : 'POST',
					data : { name : newName }
				})
				.done(function(){
					renameDialog.confirmDialog('close');
					refreshAttachments();
				});
			});
			
	
			
			//************ buttons ***********
			
			var deleteButton = $("#delete-attachment-button");			
			var renameButton = $("#rename-attachment-button");
			var uploadButton = $("#add-attachment-button");
			
			deleteButton.squashButton();
			renameButton.squashButton();
			uploadButton.squashButton();
			
			deleteButton.click(function() {
				if  (table.getSelectedRows().size() > 0){
					deleteDialog.confirmDialog('open');
				}
				else{
					$.squash.openMessage("${errorTitle}", "${nothingSelected}");
				}
			});

			renameButton.click(function(){				
				var selectedIds = table.getSelectedIds();
				if (selectedIds.length!=1){
					<f:message var="renameAttachImpossible" key="message.CanRenameOnlyOneAttachment"/>
					$.squash.openMessage("<f:message key='popup.title.error' />", "${renameAttachImpossible}");
					event.stopPropagation();
					return false;
				}
				else{
					var id = selectedIds[0];
					var name = table.getDataById(id).name;
					
					var index = name.lastIndexOf(".");
					$("#rename-attachment-input").val(name.substring(0,index));
					renameDialog.data("attachmentId", id);
					renameDialog.confirmDialog('open');	
				}			
			});			
			
			//upload button is special and defined in comp:add-attachment-popup
		});
		
	});
	
</script>
<%------------------------------------- /scripts ------------------------------------------------------%>
