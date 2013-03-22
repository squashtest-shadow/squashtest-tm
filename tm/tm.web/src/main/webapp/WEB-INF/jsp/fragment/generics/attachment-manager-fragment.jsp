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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%-- 
	@params : voir page/attachments/attachment-manager.jsp 
--%>


<%------------------------------------- URLs --------------------------------------------------------%>
<s:url var="baseURL" value="/attach-list/{attach-list-id}/attachments" >
	<s:param name="attach-list-id" value="${attachListId}"/>
</s:url>

<c:set var="uploadAttachmentUrl"  value="${baseURL}/upload"/>
<c:set var="attachmentDetailsUrl" value="${baseURL}/details"/>

<c:url var="datatableLanguage" value="/datatables/messages" />


<%------------------------------------- /URLs --------------------------------------------------------%>

<%------------------------------------- scripts ------------------------------------------------------%>

<script type="text/javascript">

	
	function refreshAttachments() {
		$('#attachment-detail-table').squashTable().refresh();
	}
	
	//init function
	$(function() {
				
		require(["jquery", "jqueryui","jquery.squash.datatables", "jquery.squash.confirmdialog"], function($){
			
			// ******** the back button ***************
			
			$("#back").button().click(function(){
				//document.location.href="${referer}";
				history.back();
			});
			
			
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
				deleteDialog.confirmDialog('open');
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
			
			//upload button is special, see the tag included at the very bottom
		});
		
	});
		

</script>

<%------------------------------------- /scripts ------------------------------------------------------%>


<div id="attachment-manager-header" class="ui-widget-header ui-corner-all ui-state-default fragment-header">
	<div style="float: left; height: 100%;">
	<h2><span><f:message key="label.CurrentAttachments"/>&nbsp;:&nbsp;</span><a id="test-case-name" href="${ testCaseUrl }/info"><c:out
		value="${ testCase.name }" escapeXml="true" /></a></h2>
	</div>	
	<div style="float: right;">
		<f:message var="back" key="label.Back" /> 
		<input id="back" type="button" value="${ back }" class="button"/>
	</div>
	<div style="clear: both;"></div>
	
</div>




<div class="fragment-body">

	<div id="test-case-toolbar" class="toolbar-class ui-corner-all">
		<div class="toolbar-information-panel"></div>
		<div class="toolbar-button-panel">
			<f:message var="uploadAttachment" key="label.UploadAttachment" />
			<input id="add-attachment-button" type="button" value="${uploadAttachment}" class="button"/>
		</div>
		<div style="clear: both;"></div>
	</div>
	
	<%---------------------------------Attachments table ------------------------------------------------%>
	
	
	<comp:toggle-panel id="attachment-table-panel" titleKey="label.CurrentAttachments" isContextual="true" open="true" >
		<jsp:attribute name="panelButtons">	
			<f:message var="renameAttachment" key="label.Rename" />
			<input type="button" value="${renameAttachment}" id="rename-attachment-button" class="button" />
			<f:message var="removeAttachment" key="label.Remove" />
			<input type="button" value="${removeAttachment}" id="delete-attachment-button" class="button" />
		</jsp:attribute>
		<jsp:attribute name="body">
	
			<table id="attachment-detail-table" data-def="ajaxsource=${attachmentDetailsUrl}, 
														  language=${datatableLanguage}, hover, pagesize=10">
				<thead>
					<tr>
						<th data-def="map=entity-index, select, narrow, center">#</th>
						<th data-def="map=hyphenated-name, sortable, center, link=${baseURL}/download/{entity-id}"><f:message key="label.Name"/></th>	
						<th data-def="map=size, center, sortable"><f:message key="label.SizeMb"/></th>
						<th data-def="map=added-on, center, sortable"><f:message key="label.AddedOn"/></th>
						<th data-def="map=empty-delete-holder, delete-button=#delete-attachment-dialog">&nbsp;</th> 
					</tr>
				</thead>
				<tbody>
					<%-- Will be populated through ajax --%>
				</tbody>
			</table>
			
		</jsp:attribute>
	</comp:toggle-panel>
	
	
	<%-------------------------------- dialogs ----------------------------------%>
	
	
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
	
	
	<comp:add-attachment-popup url="${uploadAttachmentUrl}" paramName="attachment" openedBy="add-attachment-button" submitCallback="refreshAttachments" />

</div>

