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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz" %>

<f:message var="squashlocale" key="squashtm.locale" />

<comp:rich-jeditable-header />
<comp:datepicker-manager locale="${squashlocale}"/>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<c:url var="campaignUrl" value="/campaigns/${campaign.id}" />
<s:url var="campaignInfoUrl" value="/campaigns/{campId}/general">
	<s:param name="campId" value="${campaign.id}" />
</s:url>
<s:url var="campaignPlanningUrl" value="/campaigns/{campId}/planning">
	<s:param name="campId" value="${campaign.id}" />
</s:url>
<s:url var="assignableUsersUrl" value="/campaigns/{campId}/assignable-user">
		<s:param name="campId" value="${campaign.id}" />
</s:url>

<s:url var="batchAssignableUsersUrl" value="/campaigns/{campId}/batch-assignable-user">
		<s:param name="campId" value="${campaign.id}" />
</s:url>

<s:url var="assignTestCasesUrl" value="/campaigns/{campId}/batch-assign-user">
		<s:param name="campId" value="${campaign.id}" />
</s:url>

<c:url var="testCaseManagerUrl" value="/campaigns/${ campaign.id }/test-plan/manager" />
<c:url var="testCaseDetailsBaseUrl" value="/test-cases" />

<c:url var="workspaceUrl" value="/campaign-workspace/#" />
<s:url var="simulateDeletionUrl" value="/campaign-browser/delete-nodes/simulate" />
<s:url var="confirmDeletionUrl" value="/campaign-browser/delete-nodes/confirm" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<c:set var="editable" value="${ false }" /> 
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE" domainObject="${ campaign }">
	<c:set var="editable" value="${ true }" /> 
</authz:authorized>

<script type="text/javascript">

	/* simple initialization for simple components */
	$(function(){
		$('#delete-campaign-button').button();
		$('#rename-campaign-button').button();	
	});
	
	/* display the campaign name. Used for extern calls (like from the page who will include this fragment)
	*  will refresh the general informations as well*/
	function nodeSetname(name){
		$('#campaign-name').html(name);		
	}
	
	/* renaming success handler */
	function renameCampaignSuccess(data){
		nodeSetname(data.newName);
		
		if (typeof renameSelectedNreeNode == 'function'){
			renameSelectedNreeNode(data.newName);
		}
		//change also the node name attribute
		if (typeof updateSelectedNodeName == 'function'){
			updateSelectedNodeName(data.newName);	
		}
						
		$( '#rename-campaign-dialog' ).dialog( 'close' );
	}
	
	/* renaming failure handler */
	function renameCampaignFailure(xhr){
		$('#rename-campaign-dialog .popup-label-error')
		.html(xhr.statusText);		
	}
	
	/* deletion success handler */
	function deleteCampaignSuccess(){
		<c:choose>
		<%-- case one : we were in a sub page context. We need to navigate back to the workspace. --%>
		<c:when test="${param.isInfoPage}" >		
		document.location.href="${workspaceUrl}" ;
		</c:when>
		<%-- case two : we were already in the workspace. we simply reload it (todo : make something better). --%>
		<c:otherwise>
		location.reload(true);
		</c:otherwise>
		</c:choose>		
	}
	
	/* deletion failure handler */
	function deleteCampaignFailure(xhr){
		alert(xhr.statusText);		
	}
	

	
</script>



<div class="ui-widget-header ui-state-default ui-corner-all fragment-header">

	<div style="float:left;height:100%;">
		<h2>
			<span ><f:message key="campaign.header.title" />&nbsp;:&nbsp;</span><a id="campaign-name" href="${ campaignUrl }/info"><c:out value="${ campaign.name }" escapeXml="true"/></a>
		</h2>
	</div>
	
	<div style="clear:both;"></div>
	<c:if test="${ editable }">
		<comp:popup id="rename-campaign-dialog" titleKey="dialog.rename-campaign.title" 
		            isContextual="true"   openedBy="rename-campaign-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.rename-campaign.title" />
				'${ label }': function() {
					var url = "${ campaignUrl }";
					<jq:ajaxcall 
						url="url"
						dataType="json"
						httpMethod="POST"
						useData="true"
						successHandler="renameCampaignSuccess">				
						<jq:params-bindings newName="#rename-campaign-name" />
					</jq:ajaxcall>					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<script type="text/javascript">
				$( "#rename-campaign-dialog" ).bind( "dialogopen", function(event, ui) {
					var name = $('#campaign-name').text();
					$("#rename-campaign-name").val(name);
					
				});
				</script>			
				<label><f:message key="dialog.rename.label" /></label>
				<input type="text" id="rename-campaign-name" /><br/>
				<comp:error-message forField="name"/>	
		
			</jsp:body>
		</comp:popup>
	</c:if>	
</div>

<div class="fragment-body">

<div id="campaign-toolbar" class="toolbar-class ui-corner-all " >
	<div  class="toolbar-information-panel">
		<comp:general-information-panel auditableEntity="${campaign}"/>
	</div>
	<div class="toolbar-button-panel">
	<c:if test="${ editable }">
		<input type="button" value='<f:message key="campaign.button.rename.label" />' id="rename-campaign-button" /> 
		<input type="button" value='<f:message key="campaign.button.remove.label" />' id="delete-campaign-button" />
	</c:if>
	</div>	
	<div style="clear:both;"></div>	
</div>



<c:if test="${ editable }">
	<comp:rich-jeditable targetUrl="${ campaignUrl }" componentId="campaign-description" />
</c:if>

<script type="text/javascript">
	$(function(){
		$("#campaign-reset-description").button().click(function(){$("#campaign-description").html('');return false;});
	});
</script>

<comp:toggle-panel id="campaign-description-panel" classes="information-panel" titleKey="generics.description.title" isContextual="true" open="true">
	<jsp:attribute name="body">
		<div id="campaign-description" >${ campaign.description }</div>
	</jsp:attribute>
</comp:toggle-panel>


<%--------------------------- Planning section ------------------------------------%>
<comp:toggle-panel id="datepicker-panel" titleKey="campaign.planning.panel.title" isContextual="true" open="true">
	<jsp:attribute name="body">
	<div class="datepicker-panel">
		<table class="datepicker-table">
			<tr >
				<td class="datepicker-table-col">
					<comp:datepicker fmtLabel="dialog.label.campaign.scheduled_start.label" 
						url="${campaignPlanningUrl}" datePickerId="scheduled-start" 
						paramName="scheduledStart" isContextual="true"
						initialDate="${campaign.scheduledStartDate.time}" editable="${ editable }" >	
					</comp:datepicker>
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto
						datePickerId="actual-start"
						url="${campaignPlanningUrl}"
						fmtLabel="dialog.label.campaign.actual_start.label"
						paramName="actualStart"
						autosetParamName="setActualStartAuto"
						isAuto="${campaign.actualStartAuto}"
						initialDate="${campaign.actualStartDate.time}"
						isContextual="true"
						editable="${ editable }" >
					</comp:datepicker-auto>
				</td>
			</tr>
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker fmtLabel="dialog.label.campaign.scheduled_end.label" 
						url="${campaignPlanningUrl}" datePickerId="scheduled-end" 
						paramName="scheduledEnd" isContextual="true"
						initialDate="${campaign.scheduledEndDate.time}" 
						editable="${ editable }"
						>	
					</comp:datepicker>				
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto
						datePickerId="actual-end"
						url="${campaignPlanningUrl}"
						fmtLabel="dialog.label.campaign.actual_end.label"
						paramName="actualEnd"
						autosetParamName="setActualEndAuto"
						isAuto="${campaign.actualEndAuto}"
						initialDate="${campaign.actualEndDate.time}"
						isContextual="true"
						editable="${ editable }"
						>
					</comp:datepicker-auto>
				</td>
			</tr>
		</table>
	</div>
	</jsp:attribute>
</comp:toggle-panel>
<%--------------------------- /Planning section ------------------------------------%>





<%--------------------------- Test plan section ------------------------------------%>
<script type="text/javascript">
	$(function(){
		$("#test-case-button").button().click(function(){
			document.location.href="${testCaseManagerUrl}";	
		});
		$("#remove-test-case-button").button();
	});
</script>

<comp:toggle-panel id="test-plan-panel" titleKey="campaign.test-plan.panel.title" open="true">
	<jsp:attribute name="panelButtons">
		<c:if test="${ editable }">
			<f:message var="associateLabel" key="campaign.test-plan.manage.button.label"/>
			<f:message var="removeLabel" key="campaign.test-plan.remove.button.label"/>
			<f:message var="assignLabel" key="campaign.test-plan.assign.button.label"/>
			<input id="test-case-button" type="button" value="${associateLabel}" class="button"/>
			<input id="remove-test-case-button" type="button" value="${removeLabel}" class="button"/>
			<input id="assign-test-case-button" type="button" value="${assignLabel}" class="button"/>
		</c:if>
	</jsp:attribute>
	
	<jsp:attribute name="body">
		<aggr:decorate-campaign-test-plan-table testCaseDetailsBaseUrl="${testCaseDetailsBaseUrl}" 
			batchRemoveButtonId="remove-test-case-button" editable="${ editable }" assignableUsersUrl="${assignableUsersUrl}" 
			campaignUrl="${ campaignUrl }" testCaseMultipleRemovalPopupId="delete-multiple-test-cases-dialog" testCaseSingleRemovalPopupId="delete-single-test-case-dialog" />
		<aggr:campaign-test-cases-table/>
	</jsp:attribute>
</comp:toggle-panel>


<%--------------------------- Deletion confirmation pup for Test plan section ------------------------------------%>

<pop:popup id="delete-multiple-test-cases-dialog" openedBy="remove-test-case-button" titleKey="dialog.remove-testcase-associations.title">
	<jsp:attribute name="buttons">
		<f:message var="label" key="attachment.button.delete.label" />
				'${ label }' : function(){
						$("#delete-multiple-test-cases-dialog").data("answer","yes");
						$("#delete-multiple-test-cases-dialog").dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="body">
		<f:message key="dialog.remove-testcase-associations.message" />
	</jsp:attribute>
</pop:popup>

<%--- the openedBy attribute here is irrelevant and is just a dummy --%>
<pop:popup id="delete-single-test-case-dialog" openedBy="test-cases-table .delete-test-case-button" titleKey="dialog.remove-testcase-association.title">
	<jsp:attribute name="buttons">
		<f:message var="label" key="attachment.button.delete.label" />
				'${ label }' : function(){
						$("#delete-single-test-case-dialog").data("answer","yes");
						$("#delete-single-test-case-dialog").dialog("close");
				},
				
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="body">
		<f:message key="dialog.remove-testcase-association.message" />
	</jsp:attribute>
</pop:popup>


<%------------------------------ Attachments bloc ---------------------------------------------%> 

<comp:attachment-bloc entity="${campaign}" workspaceName="campaign" editable="${ editable }" />


<%--------------------------- Deletion confirmation popup -------------------------------------%>
<c:if test="${ editable }">


	<comp:delete-contextual-node-dialog simulationUrl="${simulateDeletionUrl}" confirmationUrl="${confirmDeletionUrl}" 
			itemId="${campaign.id}" successCallback="deleteCampaignSuccess" openedBy="delete-campaign-button" titleKey="dialog.delete-campaign.title"/>

</c:if>


<%--------------------------- Assign User popup -------------------------------------%>


<comp:popup id="batch-assign-test-case" titleKey="dialog.assign-test-case.title" 
	
	isContextual="true" openedBy="assign-test-case-button" closeOnSuccess="false">
		<jsp:attribute name="buttons">
		
			<f:message var="label" key="dialog.assign-test-case.title" />
			'${ label }': function() {
				var url = "${assignTestCasesUrl}";
				var table = $( '#test-cases-table' ).dataTable();
				var ids = getIdsOfSelectedTableRows(table, getTestPlanTableRowId);
				var user = $(".comboLogin").val();
			
				$.post(url, { testCasesIds: ids, userId: user}, function(){
					refreshTestCasesWithoutSelection();
					$("#batch-assign-test-case").dialog('close');
				});
				
			},			
			<pop:cancel-button />
		</jsp:attribute>
		<jsp:body>
			<f:message var="emptyMessage" key="dialog.assign-user.selection.empty.label" />
			<f:message var="confirmMessage" key="dialog.assign-test-case.confirm.label" />
			<script type="text/javascript">
				$("#batch-assign-test-case").bind( "dialogopen", function(event, ui){
					var table = $( '#test-cases-table' ).dataTable();
					var ids = getIdsOfSelectedTableRows(table, getTestPlanTableRowId);
					if (ids.length > 0) {
						var comboBox = $.get("${batchAssignableUsersUrl}", false, function(){
							$("#comboBox-div").html("${confirmMessage}");
							$("#comboBox-div").append(comboBox.responseText);
							$("#comboBox-div").show();
						});
					}
					else {
						alert("${emptyMessage}");
						$(this).dialog('close');
					}
					
				});
			</script>
			<div id="comboBox-div">
			</div>
		</jsp:body>
</comp:popup>
<comp:decorate-buttons />
</div>






