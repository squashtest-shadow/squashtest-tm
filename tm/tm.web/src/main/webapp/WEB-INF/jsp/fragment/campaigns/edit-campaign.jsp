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
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="camp" tagdir="/WEB-INF/tags/campaigns-components"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup"%>
<%@ taglib prefix="authz" tagdir="/WEB-INF/tags/authz"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="at" tagdir="/WEB-INF/tags/attachments"%>
<%@ taglib prefix="csst" uri="http://org.squashtest.tm/taglib/css-transform" %>
<%@ taglib prefix="dashboard" tagdir="/WEB-INF/tags/dashboard" %>

<f:message var="squashlocale" key="squashtm.locale" />	
<f:message var="iterationPlanningTitle" key="campaigns.planning.iterations.scheduled_dates"/>	
<f:message var="iterationPlanningButton" key="campaigns.planning.iterations.button" /> 
<f:message var="buttonOK" key="label.Ok"/>
<f:message var="buttonCancel" key="label.Cancel"/>
<f:message var="dateformat" key="squashtm.dateformatShort" />

<comp:datepicker-manager locale="${squashlocale}" />

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<c:url var="campaignUrl" value="/campaigns/${campaign.id}" />
<c:url var="campaignInfoUrl" value="/campaigns/${campaign.id}/general" />
<c:url var="campaignPlanningUrl" value="/campaigns/${campaign.id}/planning"/>
<c:url var="assignableUsersUrl" value="/campaigns/${campaign.id}/assignable-users" />
<c:url var="campaignStatisticsUrl" value="/campaigns/${campaign.id}/dashboard-statistics" />
<c:url var="campaignInfoStatisticsUrl" value="/campaigns/${campaign.id}/statistics"/>
<c:url var="campaignStatisticsPrintUrl" value="/campaigns/${campaign.id}/dashboard"/>
<c:url var="testCaseManagerUrl"	value="/campaigns/${campaign.id}/test-plan/manager" />
<c:url var="workspaceUrl" value="/campaign-workspace/#" />
<c:url var="btEntityUrl" value="/bugtracker/campaign/${campaign.id}" />
<c:url var="customFieldsValuesURL" value="/custom-fields/values" />

<%-- ----------------------------------- Authorization ----------------------------------------------%>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="WRITE"
	domainObject="${ campaign }">
	<c:set var="writable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="ATTACH" domainObject="${ campaign }">
	<c:set var="attachable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>

<authz:authorized hasRole="ROLE_ADMIN" hasPermission="DELETE" domainObject="${ campaign }">
	<c:set var="deletable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="CREATE" domainObject="${ campaign }">
	<c:set var="creatable" value="${true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"	domainObject="${ campaign }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>
<authz:authorized hasRole="ROLE_ADMIN" hasPermission="LINK"	domainObject="${ campaign }">
	<c:set var="linkable" value="${ true }" />
	<c:set var="moreThanReadOnly" value="${ true }" />
</authz:authorized>



<div
	class="ui-widget-header ui-state-default ui-corner-all fragment-header">

	<div style="float: left; height: 100%;">
		<h2>
			<span><f:message key="label.Campaign" />&nbsp;:&nbsp;</span><a
				id="campaign-name" href="${ campaignUrl }/info"><c:out
					value="${ campaign.name }" escapeXml="true" />
			</a>
		</h2>
	</div>

	<div class="unsnap"></div>
	<c:if test="${writable}">
		<pop:popup id="rename-campaign-dialog"
			titleKey="dialog.rename-campaign.title" isContextual="true"
			openedBy="rename-campaign-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.rename-campaign.title" />
				'${ label }': function() {
					var url = "${ campaignUrl }";
					$.ajax({
						url : url,
						dataType : 'json', 
						type : 'post', 
						data : { newName : $("#rename-campaign-name").val() }
					}).done(renameCampaignSuccess);
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:attribute name="body">
				<script type="text/javascript">
				require(["common"], function() {
					require(["jquery"], function($) {
					$("#rename-campaign-dialog").bind("dialogopen",
						function(event, ui) {
							var name = $.trim($('#campaign-name').text());
							$("#rename-campaign-name").val(name);

						}
					);
					});
				});
				</script>			
				<label><f:message key="dialog.rename.label" />
				</label>
				<input type="text" id="rename-campaign-name" maxlength="255" size="50" />
				<br />
				<comp:error-message forField="name" />	
			</jsp:attribute>
		</pop:popup>
	</c:if>
</div>

<div id="campaign-toolbar" class="toolbar-class ui-corner-all ">
	<div class="toolbar-information-panel">
		<comp:general-information-panel auditableEntity="${campaign}" entityUrl="${campaignUrl}"/>
	</div>
	<div class="toolbar-button-panel">
	
		
		<c:if test="${ writable }">
		
			<input type="button" class="sq-btn"
				value='<f:message key="label.Rename" />'
				id="rename-campaign-button" />
		</c:if>

	</div>
	<div class="unsnap"></div>
	<c:if test="${ moreThanReadOnly }">
		<comp:opened-object otherViewers="${ otherViewers }"
			objectUrl="${ campaignUrl }" />
	</c:if>
</div>


<csst:jq-tab>
<div class="fragment-tabs fragment-body">
	<ul class="tab-menu">
		<li><a href="#campaign-dashboard"><f:message key="title.Dashboard"/>
		</a>
		</li>
		<li><a href="#tabs-1"><f:message key="tabs.label.information" />
		</a>
		</li>
		<li><a href="#tabs-2"><f:message key="tabs.label.test-plan" />
		</a>
		</li>
		<li><a href="#tabs-3"><f:message key="label.Attachments" />
				<c:if test="${ campaign.attachmentList.notEmpty }">
					<span class="hasAttach">!</span>
				</c:if>
		</a>
		</li>
	</ul>
	<div id="tabs-1">
		<c:if test="${ writable }">
			<comp:rich-jeditable targetUrl="${ campaignUrl }"
				componentId="campaign-description" />
		</c:if>

		<script type="text/javascript">
		require([ "common" ], function () {
			require([ "jquery", "domReady", "jqueryui" ], function ($, domReady) {
				/* simple initialization for simple components */
				domReady(function() {
					$("#").button().click(function() {
						$("#campaign-description").html('');
						return false;
					});
				});
			});
		});
		</script>

		<comp:toggle-panel id="campaign-description-panel"
			titleKey="label.Description"
			open="true">
			<jsp:attribute name="body">
				<div id="campaign-description">${ campaign.description }</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		
		<%----------------------------------- Custom Fields -----------------------------------------------%>
		
		<comp:toggle-panel id="campaign-custom-fields" 
			titleKey="generics.customfieldvalues.title"	open="${hasCUF}">
			<jsp:attribute name="body">
				<div id="campaign-custom-fields-content" class="display-table">
<c:if test="${hasCUF}">
				<comp:waiting-pane/>
</c:if>
				</div>
			</jsp:attribute>
		</comp:toggle-panel>
		
		


		<%--------------------------- Planning section ------------------------------------%>
		<comp:toggle-panel id="datepicker-panel" titleKey="label.Planning"	open="true">
			<jsp:attribute name="panelButtons">
				<c:if test="${writable}">
				<input id="iteration-planning-button" class="sq-btn" type="button" role="button" value="${iterationPlanningButton}"/>
				</c:if>
			</jsp:attribute>
			<jsp:attribute name="body">
	<div class="datepicker-panel">
		<table class="datepicker-table">
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker
									fmtLabel="dialog.label.campaign.scheduled_start.label"
									url="${campaignPlanningUrl}" datePickerId="scheduled-start"
									paramName="scheduledStart" isContextual="true"
									initialDate="${campaign.scheduledStartDate.time}"
									editable="${ writable }">	
					</comp:datepicker>
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto datePickerId="actual-start"
									url="${campaignPlanningUrl}"
									fmtLabel="dialog.label.campaign.actual_start.label"
									paramName="actualStart" autosetParamName="setActualStartAuto"
									isAuto="${campaign.actualStartAuto}"
									initialDate="${campaign.actualStartDate.time}"
									isContextual="true" editable="${ writable }">
					</comp:datepicker-auto>
				</td>
			</tr>
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker
									fmtLabel="dialog.label.campaign.scheduled_end.label"
									url="${campaignPlanningUrl}" datePickerId="scheduled-end"
									paramName="scheduledEnd" isContextual="true"
									initialDate="${campaign.scheduledEndDate.time}"
									editable="${ writable }">	
					</comp:datepicker>				
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker-auto datePickerId="actual-end"
									url="${campaignPlanningUrl}"
									fmtLabel="dialog.label.campaign.actual_end.label"
									paramName="actualEnd" autosetParamName="setActualEndAuto"
									isAuto="${campaign.actualEndAuto}"
									initialDate="${campaign.actualEndDate.time}"
									isContextual="true" editable="${ writable }">
					</comp:datepicker-auto>
				</td>
			</tr>
		</table>
	</div>
	</jsp:attribute>
	</comp:toggle-panel>
	
				

	<div id="iteration-planning-popup" class="popup-dialog not-displayed" 
		title="${iterationPlanningTitle}" data-def="dateformat=${dateformat}, campaignId=${campaign.id}">						
		
		<div data-def="state=edit">
			<table class="iteration-planning-content" >
				<thead>
					<tr>
						<th><f:message key="label.Name"/></th>
						<th><f:message key="campaigns.planning.iterations.scheduledstart"/></th>
						<th><f:message key="campaigns.planning.iterations.scheduledend"/></th>
					</tr>
				</thead>
				<tbody>
				
				</tbody>						
			</table>
		</div>
		
		<div data-def="state=loading" >		
 				<comp:waiting-pane />
		</div>
		
		<div class="popup-dialog-buttonpane">
			<input type="button" value="${buttonOK}" data-def="evt=confirm, mainbtn=edit"/>
			<input type="button" value="${buttonCancel}" data-def="evt=cancel, mainbtn" />
		</div>					
		
	</div>
	
		<%--------------------------- /Planning section ------------------------------------%>
		<%-- ------------------ statistiques --------------------------- --%>
		<comp:statistics-panel statisticsEntity="${ statistics }" statisticsUrl="${ campaignInfoStatisticsUrl }"/>
		<%-- ------------------ /statistiques --------------------------- --%>
	</div>
	<div id="tabs-2" class="table-tab">

		<%--------------------------- Test plan section ------------------------------------%>
		<script type="text/javascript">
		require([ "common" ], function () {
			require([ "jquery", "domReady", "jqueryui" ], function ($, domReady) {
				/* simple initialization for simple components */
				domReady(function() {
					
					$("#test-case-button").bind("click", function(){
						document.location.href = "${testCaseManagerUrl}";						
					});
					
					
					<c:if test="${hasCUF}">
					<%-- loading the custom field panel --%>
					$("#campaign-custom-fields-content").load("${customFieldsValuesURL}?boundEntityId=${campaign.boundEntityId}&boundEntityType=${campaign.boundEntityType}");
					</c:if>			    	
				});
			});
		});
		</script>

		<div class="cf">
			<f:message var="tooltipSortmode" key="tooltips.TestPlanSortMode" />
			<f:message var="messageSortmode" key="message.TestPlanSortMode" />
			<f:message var="associateLabel" key="label.Add" />
			<f:message var="removeLabel" key="label.Remove" />
			<f:message var="assignLabel" key="label.Assign" />
			<f:message var="reorderLabel" key="label.Reorder" />
			<f:message var="filterLabel" key="label.Filter" />
            <f:message var="filterTooltip" key="tooltips.FilterTestPlan" />
			<f:message var="reorderTooltip" key="tooltips.ReorderTestPlan" />
			<f:message var="tooltipAddTPI" key="tooltips.AddTPIToTP" />
			<f:message var="tooltipRemoveTPI" key="tooltips.RemoveTPIFromTP" />
			<f:message var="tooltipAssign" key="tooltips.AssignUserToTPI" />

			<c:if test="${ writable }">
      <div class="left btn-toolbar">
        <div class="btn-group">
          <button id="filter-test-plan-button" class="sq-btn btn-sm" title="${filterTooltip}">
            <span class="ui-icon ui-icon-refresh"></span>${filterLabel}
          </button>
          <button id="reorder-test-plan-button" class="sq-btn btn-sm" title="${reorderTooltip}">
            <span class="ui-icon ui-icon-refresh"></span>${reorderLabel}
          </button>
          <span id="test-plan-sort-mode-message" class="not-displayed sort-mode-message small" title="${tooltipSortmode}">${messageSortmode}</span>
        </div>
      </div>
			</c:if>
			
			<c:if test="${ linkable or writable }">
      <div class="right btn-toolbar">
      <c:if test="${  writable }">
        <span class="btn-group">
          <button id="assign-users-button" class="sq-btn btn-sm" title="${tooltipAssign}" >
            <span class="ui-icon ui-icon-person"></span>${assignLabel}
          </button>
        </span>
        </c:if>
         <c:if test="${ linkable }">
        <span class="btn-group">
          <button id="test-case-button" class="sq-btn btn-sm" title="${tooltipAddTPI}">
            <span class="ui-icon ui-icon-plusthick"></span>${associateLabel}
          </button>
          <button id="remove-test-case-button" class="sq-btn btn-sm" title="${tooltipRemoveTPI}">
            <span class="ui-icon ui-icon-trash"></span>${removeLabel}
          </button>
        </span>
        </c:if>
      </div>
			</c:if>
		</div>
		<div class="table-tab-wrap">
			<camp:campaign-test-plan-table
				assignableUsers="${assignableUsers}" 
				modes="${modes}"
				weights="${weights}"
				batchRemoveButtonId="remove-test-case-button"
				editable="${ linkable }" assignableUsersUrl="${assignableUsersUrl}"
				reorderable="${linkable}"
				campaignUrl="${ campaignUrl }"
				testCaseMultipleRemovalPopupId="delete-multiple-test-cases-dialog" 
				campaign="${campaign}"/>
		</div>


		<%--------------------------- Deletion confirmation popup for Test plan section ------------------------------------%>

		<pop:popup id="delete-multiple-test-cases-dialog"
			openedBy="remove-test-case-button"
			titleKey="dialog.remove-testcase-associations.title">
			<jsp:attribute name="buttons">
		<f:message var="label" key="label.Yes" />
				'${ label }' : function(){
						$("#delete-multiple-test-cases-dialog").data("answer","yes");
						$("#delete-multiple-test-cases-dialog").dialog("close");
				},
				
		<pop:cancel-button /> 
	</jsp:attribute>
			<jsp:attribute name="body">
			<f:message var="emptyMessage"
					key="message.EmptyTableSelection" />			
		<script type="text/javascript">
		require(["common"], function() {
			require(["jquery"], function($) {
				$("#delete-multiple-test-cases-dialog").bind( "dialogopen", function(event, ui){
					var _id =  $("#delete-multiple-test-cases-dialog").data("entity-id");
					
					var ids = [];
					
					if(!_id){
						ids = $( '#test-cases-table' ).squashTable().getSelectedIds();
					} else {
						ids.push(_id);
					}
				
					$("#delete-multiple-test-cases-dialog").data("entity-id", null);
					
					if (ids.length == 0) {
						$.squash.openMessage("<f:message key='popup.title.error' />", "${emptyMessage}");
						$(this).dialog('close');
					}
					
					 this.selIds = ids;
				});
				
			});
		});
			</script>
		<f:message key="dialog.remove-testcase-associations.message" />
	</jsp:attribute>
		</pop:popup>


	</div>

	<%------------------------------ Attachments bloc ---------------------------------------------%>

	<at:attachment-tab tabId="tabs-3" entity="${ campaign }" editable="${ attachable }" tableModel="${attachmentsModel}"/>
	
	
	<%------------------------------- Dashboard ---------------------------------------------------%>
	<div id="campaign-dashboard">
		<dashboard:campaign-dashboard-panel url="${campaignStatisticsUrl}" printUrl="${campaignStatisticsPrintUrl}" allowsSettled="${allowsSettled}" allowsUntestable="${allowsUntestable}" />
	</div>

</div>
</csst:jq-tab>



 <f:message key="tabs.label.issues" var="tabIssueLabel"/>
<script type="text/javascript">

	var identity = { resid : ${campaign.id}, restype : "campaigns"  };

	
	require(["common"], function(){
			require(["jquery", "squash.basicwidgets", "contextual-content-handlers", "jquery.squash.fragmenttabs", 
			         "bugtracker/bugtracker-panel", 'workspace.event-bus', "campaign-management",
			         "jqueryui"], 
					function($, basicwidg, contentHandlers, Frag, bugtrackerPanel, eventBus, campmanager){
		$(function(){
				
				basicwidg.init();
				
				var nameHandler = contentHandlers.getSimpleNameHandler();
				
				nameHandler.identity = identity;
				nameHandler.nameDisplay = "#campaign-name";
				
								
				//****** tabs configuration ***********
				
				var fragConf = {
					beforeLoad : Frag.confHelper.fnCacheRequests,
					cookie : "iteration-tab-cookie",
					activate : function(event, ui){
						if (ui.newPanel.is('#campaign-dashboard')){
							eventBus.trigger('dashboard.appear');
						}
					}
				};
				Frag.init(fragConf);
				
				<c:if test="${campaign.project.bugtrackerConnected}">
				bugtrackerPanel.load({
					url : "${btEntityUrl}",
					label : "${tabIssueLabel}"
				});
				</c:if>
				
				// ********** planning **************
				
				<c:if test="${writable}">
				campmanager.initPlanning({
					campaignId : ${campaign.id}
				});
				</c:if>
				
				// ********** dashboard **************
				
				campmanager.initDashboardPanel({
					master : '#dashboard-master',
					cacheKey : 'camp${campaign.id}'
				});		
				
			});
		});
	});
	
	function renameCampaignSuccess(data){
		squashtm.workspace.eventBus.trigger('node.rename', { identity : identity, newName : data.newName});		
	};					
	
</script>

