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
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:url var="projectFilterUrl" value="/global-filter/filter" />

<script type="text/javascript">
	
	
	<f:message var="projectSelectionAll" key="report.criteria.panel.projectlist.selection.all.label" />
	<f:message var="projectSelectionSpecified" key="report.criteria.panel.projectlist.selection.selected.label" />
	<f:message var="selectallProjectLabel" key="report.criteria.panel.projectlist.selection.controls.selectall" />
	<f:message var="deselectAllProjectLabel" key="report.criteria.panel.projectlist.selection.controls.deselectall" />
	<f:message var="invertSelectProjectLabel" key="report.criteria.panel.projectlist.selection.controls.invertselect" />
	
	// for report criteria common code
	var reportCriteria;
	
	
	$(function(){
		reportCriteria=new Object();
	})
	
	function getReportCriteria(){
		return reportCriteria;
	}
	
	function updateCriteria(strKey,strValue){
		reportCriteria[strKey]=strValue;
	}
	//end for report criteria common code
	
	//Update the project selection 
	function refreshProjectSelection(){
		//update the ids list
		var selectedIds = getSelectedReportProjectIds();
		updateCriteria('projectIds', selectedIds);
		//update the span too
		updateProjectSelectionSpan();
	}
	
	//Change the text beside selection button
	function updateProjectSelectionSpan(){
		if ((typeof reportCriteria["projectIds"]=='undefined')||(reportCriteria["projectIds"].length==0)||(reportCriteria["projectIds"].length==numberOfProject)){
			$("#project-selection-status").html("${projectSelectionAll}");
		}else{
			$("#project-selection-status").html("${projectSelectionSpecified}");
		}
	}
	
	

</script>
<div>
	<label><f:message key="report.criteria.panel.projectlist.label" /></label>
	<span id="project-selection-status">${projectSelectionAll}</span>
	<f:message var="selectProjectButton" key="report.criteria.panel.projectlist.button.label"/>
	<input type="button" class="button" id="report-select-project-button" name="projectIdsButton" value="${selectProjectButton}" style="display:inline-block;"/>
	<pop:popup id="report-select-project-popup" openedBy="report-select-project-button" closeOnSuccess="false" isContextual="true" titleKey="dialog.report.requirementcoverage.selectproject.title">
		<jsp:attribute name="buttons">
			<f:message var="confirmLabel" key="dialog.button.confirm.label" />
			'${ confirmLabel }': function() {
				refreshProjectSelection();
				$("#report-select-project-popup").dialog("close");
			},
			<pop:cancel-button />
		</jsp:attribute>
		
		<jsp:attribute name="additionalSetup">
	width : 400,
	open : function(){
		loadProjectList(); 
		
	}
	</jsp:attribute>
		
		<jsp:attribute name="body">
		<div id="dialog-settings-project-selection-controls" class="project-filter-controls">
				<a id="dialog-settings-project-selectall" href="#">${selectallProjectLabel}</a>
				<a id="dialog-settings-project-deselectall" href="#">${deselectAllProjectLabel}</a>
				<a id="dialog-settings-project-invertselect" href="#">${invertSelectProjectLabel}</a>				
		</div>	
		<hr/>
		<div class="project-report-list-template not-displayed">
			<div class="project-item ">
				<input type="checkbox" class="project-report-checkbox" checked="checked"/> <span class="project-name"></span>
			</div>
		</div>
			<div id="report-project-list">
			</div>
		</jsp:attribute>
	</pop:popup>
</div>


<%-- 
	code managing the popup
 --%>

<script type="text/javascript">
	//We initiate the popup only once, this is the flag
	var isPopupFilled = false;
	//Total number of project
	var numberOfProject = 0;
	
	function loadProjectList(){
		//Check if the popup wasn't already filled
		if(!isPopupFilled){
			$.get("${projectFilterUrl}",populateReportProjectList,"json");
			isPopupFilled = true;
		}
	}
	
	//Fill the popup with project list
	function populateReportProjectList(jsonData){
		
		var cssClass="odd";
		var i=0;
		for (i=0;i<jsonData.projectData.length;i++){
			appendProjectReportItem("report-project-list",jsonData.projectData[i], cssClass);
			cssClass=swapCssClass(cssClass);
		}
		//total number of project
		numberOfProject = jsonData.projectData.length;
				
	}
	
	//Alternate class
	function swapCssClass(cssClass){
		if (cssClass=="odd") return "even";
		return "odd";
	}
	

	//Set the html and css project list attributes
	function appendProjectReportItem(containerId, projectItemData, cssClass){
		var jqNewItem = $("#report-select-project-popup .project-report-list-template .project-item").clone();
		jqNewItem.addClass(cssClass);
		
		var jqChkBx = jqNewItem.find(".project-report-checkbox");
		jqChkBx.attr('id','project-report-checkbox-'+parseInt(projectItemData[0]));
		
		var jqName = jqNewItem.find(".project-name");
		jqName.html(projectItemData[1]);
		
		$("#"+containerId).append(jqNewItem);
	}
	
	//Get selected ids
	function getSelectedReportProjectIds(){
 		var selectedBoxes = $("#report-project-list .project-report-checkbox:checked");
 		var zeids = new Array();
 		var i;
 		
 		for (i=0;i<selectedBoxes.length;i++){
 			var jqBox = $(selectedBoxes[i]);
 			zeids.push(extractProjectId(jqBox.attr('id')));
 		}
 		
 		return zeids;
 	}
	
	//Get the project id
	function extractProjectId(strDomId){
 		var idTemplate = "project-report-checkbox-";	
 		var templateLength = idTemplate.length;
 		var extractedId = parseInt(strDomId.substring(templateLength));
 		return extractedId;
 	}
	
	
	
	//Select all, deselect and invert selection
	$(function(){
		
		$("#dialog-settings-project-selectall").click(function(){
			selectAllReportProjects();
		});
		
		$("#dialog-settings-project-deselectall").click(function(){
			deselectAllReportProjects();
		});
		
		$("#dialog-settings-project-invertselect").click(function(){
			invertAllReportProjects();
		});
 	});
 	
	function selectAllReportProjects(){
		var boxes = $("#report-project-list .project-report-checkbox");
		
		if (boxes.length==0) return;
		
		$(boxes).each(function(){
			setCheckBox($(this), true);
		});
	}
	
	function deselectAllReportProjects(){
		var boxes = $("#report-project-list .project-report-checkbox");
		
		if (boxes.length==0) return;
		
		$(boxes).each(function(){
			setCheckBox($(this), false);
		});
	}
	
	function invertAllReportProjects(){
		var boxes = $("#report-project-list .project-report-checkbox");
		
		if (boxes.length==0) return;
		
		$(boxes).each(function(){
			setCheckBox($(this), ! $(this).is(":checked"));
		});		
	}
	
</script>
