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
<%@ tag language="java" pageEncoding="ISO-8859-1"%>

<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>

<c:url var="treeBaseUrl" value="/campaign-browser/" />
<c:url var="rootModelUrl" value="/campaign-browser/campaign-tree" />


<tree:jstree-header />

<%--
	the following is to be considered as an API. Like the comp:datepicker, see this .tag as an object.
 --%>
<script type="text/javascript">
	var reportCriteria ;
	
	$(function(){
		reportCriteria=new Object();
		$("#campaign-status-combo").change(function(){comboStatusChange()});
	})
	
	function getReportCriteria(){
		return reportCriteria;
	}
	
	function updateCriteria(strKey,strValue){
		reportCriteria[strKey]=strValue;
	}
	
	
	<f:message var="campaignSelectionAll" key="report.criteria.panel.campaignlist.selection.all.label" />
	<f:message var="campaignSelectionSpecified" key="report.criteria.panel.campaignlist.selection.selected.label" />
	
	function updateCampaignSelectionSpan(){
		if ((typeof reportCriteria["campaignIds"]=='undefined')||(reportCriteria["campaignIds"].length==0)){
			$("#campaign-selection-status").html("${campaignSelectionAll}");
		}else{
			$("#campaign-selection-status").html("${campaignSelectionSpecified}");
		}
	}
	
	function comboStatusChange(){
		var selected = $("#campaign-status-combo").val();
		updateCriteria("campaignStatus",selected);
	}
	
	
</script>

<f:message var="squashlocale" key="squashtm.locale" />
<comp:datepicker-manager locale="${squashlocale}"/>
<div>
	<div class="datepicker-panel">
		<table class="datepicker-table">
			<tr >
				<td class="datepicker-table-col">
					<comp:datepicker fmtLabel="dialog.label.campaign.scheduled_start.label" 
						updateFunction="updateCriteria" 
						datePickerId="scheduled-start" 
						paramName="scheduledStart" isContextual="true"
						initialDate="${campaign.scheduledStartDate.time}" >	
					</comp:datepicker>
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker
						datePickerId="actual-start"
						updateFunction="updateCriteria"
						fmtLabel="dialog.label.campaign.actual_start.label"
						paramName="actualStart"
						initialDate="${campaign.actualStartDate.time}"
						isContextual="true">
					</comp:datepicker>
				</td>
			</tr>
			<tr>
				<td class="datepicker-table-col">
					<comp:datepicker fmtLabel="dialog.label.campaign.scheduled_end.label" 
						updateFunction="updateCriteria"
						datePickerId="scheduled-end" 
						paramName="scheduledEnd" isContextual="true"
						initialDate="${campaign.scheduledEndDate.time}" >	
					</comp:datepicker>				
				</td>
				<td class="datepicker-table-col">
					<comp:datepicker
						datePickerId="actual-end"
						fmtLabel="dialog.label.campaign.actual_end.label"
						paramName="actualEnd"
						updateFunction="updateCriteria"
						initialDate="${campaign.actualEndDate.time}"
						isContextual="true">
					</comp:datepicker>
				</td>
			</tr>
		</table>
	</div>
</div>

<div>
	<label><f:message key="report.criteria.panel.campaignstatus.label" /></label> 
	<select id="campaign-status-combo" name="campaignStatus">
		<option value="CAMPAIGN_ALL" ><f:message key="report.criteria.panel.campaignstatus.CAMPAIGN_ALL" /></option>
		<option value="CAMPAIGN_RUNNING" ><f:message key="report.criteria.panel.campaignstatus.CAMPAIGN_RUNNING" /></option>
		<option value="CAMPAIGN_OVER" ><f:message key="report.criteria.panel.campaignstatus.CAMPAIGN_OVER" /></option>
	</select >			

</div>



<div>
	<label><f:message key="report.criteria.panel.campaignlist.label" /></label>
	<span id="campaign-selection-status">${campaignSelectionAll}</span>
	<f:message var="selectCampaignButton" key="report.criteria.panel.campaignlist.button.label"/>
	<input type="button" class="button" id="report-select-campaign-button" name="campaignIdsButton" value="${selectCampaignButton}" style="display:inline-block;"/>

	<pop:popup id="report-select-campaign-popup" titleKey="dialog.report.executionprogress.selectcampaign.title" isContextual="true"
	openedBy="report-select-campaign-button" closeOnSuccess="false">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="dialog.button.confirm.label" />

		'${ label }': function() {
			var selectedIds = getSelectedCampaignIds();
			$("#report-select-campaign-popup").dialog("close");		
			
			updateCriteria("campaignIds",selectedIds);
			updateCampaignSelectionSpan();
		},			
		<pop:cancel-button />
	</jsp:attribute>
	<jsp:attribute name="body">
		<tree:linkables-tree workspaceType="campaign"  id="linkable-campaign-tree" />
	</jsp:attribute>
</pop:popup>


<%-- popup init code --%>
<script type="text/javascript">


		function initTree(jsondata){
			try{
				initLinkableTree(jsondata);
			}catch(e){
				alert(e.toString());
			}
		}

		$(function(){
			
			//resize the popup
			$("#report-select-campaign-popup").dialog("option","width",300);
			$("#report-select-campaign-popup").dialog("option","height",500);
			
			//init tree			
			$.get('${rootModelUrl}',function(data){initTree(data)},"json");			
			$("#linkable-campaign-tree").addClass("selection-tree");
		});
			
		<%-- tree population callbacks --%>
		function libraryContentUrl(node) {
			return nodeContentUrl('${ treeBaseUrl }', node);
		}
		
		function folderContentUrl(node) {
			return nodeContentUrl('${ treeBaseUrl }', node);
		}
		
		function getSelectedCampaignIds(){
			var tree = $( '#linkable-campaign-tree' );
			var ids = new Array();
			
			var nodes = tree.jstree('get_selected');
			
			if (nodes.length!=0){
				nodes.each(function(){
					if ($( this).attr('restype') == 'campaigns') {
						ids.push($( this).attr('resid'));
					}
				});
			}
			
			return ids;
			

		};
	
</script>

</div>