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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="aggr" tagdir="/WEB-INF/tags/aggregates"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="report" tagdir="/WEB-INF/tags/reportcriteria" %>
<?xml version="1.0" encoding="utf-8" ?>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>

<%-- that url should receive more GET parameters (typically more criterions for the request) --%>
<s:url var="baseReportUrl" value="/report-workspace/report/generate?report=${report.id}">
</s:url>

<s:url var="infoReportUrl" value="/report-workspace/report/info?report=${report.id}">
</s:url>

<s:url var="baseReportExportOptionUrl" value="/report-workspace/report/export-options?report=${report.id}">
</s:url>

<%-- 



 --%>
	
<script type="text/javascript">

	function serializeObject(obj){
		var result="";
		for (var ppt in obj){
			//case of a property of type array
			if (obj[ppt] instanceof Array){
				var values = obj[ppt];
				var i;
				for (i=0;i<values.length;i++){
					result+="&"+ppt+"[]="+values[i];
				}
			}
			//case of a scalar property
			else{
				result+="&"+ppt+"="+obj[ppt];				
			}
		}
		return result;
	}

	function addViewOption(){
		var viewOption = "&view="+$("#view-tabed-panel").data("selected");
		return viewOption;
	}
	
	//should be reworked later 
	function report_create_url(){
		var baseUrl = "${baseReportUrl}";
		
		//set view
		baseUrl+=addViewOption();
		
		var params=getReportCriteria();
		var serializedParams=serializeObject(params);
		var finalUrl=baseUrl+serializedParams;
		
		return finalUrl;
	}
	
	
	//that one too
	function report_create_exportoption_url(){
		var baseUrl = "${baseReportExportOptionUrl}";
		
		baseUrl+=addViewOption();
		
		return baseUrl;
	}	

	
	function loadReport(){
		var optionUrl=report_create_exportoption_url();		
		$.get(optionUrl,function(data){populateExportOptionSelect(data);},"json");
		
		var url = report_create_url();
		url+="&format=html";
		$("#view-content-panel").load(url);		
	}
	
	
	function populateExportOptionSelect(jsonOptions){
		$("#export-select").empty();
		var i=0;
		for (i=0;i<jsonOptions.length;i++){
			$("#export-select").append("<option value=\""+jsonOptions[i]+"\">"+jsonOptions[i]+"</option>");
		} 
	}
	
	function foldCriteriaPanel(){		
		var parent = $("#generate").parents(".toggle-panel").find("h3");
		parent.click(); //folds the criteria panel
				
	}
	
	function exportReport(){
		var url = report_create_url();
		url+="&format="+$("#export-select").val();
		//$.get(url);
		document.location.href=url;
	}

	$(function(){
		$("#generate").click(function(){
			
			//fold unused parts of the workspace
			if(typeof setReportWorkspaceExpandState == 'function'){
				setReportWorkspaceExpandState();
			}
			foldCriteriaPanel();
			
			//display the view
			$("#view-tabed-panel").removeClass("not-displayed");		 

			//fetch the view
			loadReport();
			
		});
		
		
		$("#export").click(function(){
			exportReport();
		});
		
	});
</script>

<c:if test="${! param.hasBackButton}">
<f:message var="toggleStateButtonNormal" key="report.workspace.togglebutton.normal.label" />
<f:message var="toggleStateButtonExpand" key="report.workspace.togglebutton.expand.label" />

<%-- this set of javascript manages the expanded or normal state of the category frame --%>
<script type="text/javascript">
	function setCategoryFrameNormalState(){
		$("#outer-category-frame").removeClass("expanded");
		$("#toggle-expand-category-frame-button").attr("value","${toggleStateButtonNormal}");
	}
	
	function setCategoryFrameExpandState(){
		$("#outer-category-frame").addClass("expanded");
		$("#toggle-expand-category-frame-button").attr("value","${toggleStateButtonExpand}");		
	}
	
	function toggleCategoryFrameState(){
		if ($("#outer-category-frame").hasClass("expanded")){
			setCategoryFrameNormalState();
		}else{
			setCategoryFrameExpandState();
		}
	}
	
	$(function(){
		$("#toggle-expand-category-frame-button").click(function(){
			toggleReportWorkspaceState();
		});
	});
	
</script>
</c:if>


<script type="text/javascript">
	$(function(){
		$("#back").button().click(function(){
			//document.location.href="${referer}";
			history.back();
		});
	});
</script>	


<div id="report-name-div" class="ui-widget-header ui-corner-all ui-state-default fragment-header">

<f:message var="reportName" key="${report.resourceKeyName}"/>
<f:message var="reportType" key="${report.reportType.resourceKeyName}"/>

<c:if test="${! param.hasBackButton}">
<%-- that button will manage the expand/normal state of the workspace --%>
<div id="category-frame-button">
	<input type="button" class="button" id="toggle-expand-category-frame-button" value="${toggleStateButtonNormal}"/>
</div> 
</c:if>

<div style="float: left; height: 100%;">
<h2><span><f:message key="report.header.title" />&nbsp;:&nbsp;</span><a href="${infoReportUrl}">${reportName} (${reportType})</a></h2>
</div>

<c:if test="${param.hasBackButton}">
	<div style="float: right;"><f:message var="back" key="fragment.edit.header.button.back" /> <input id="back"
		type="button" value="${ back }" /></div>
</c:if>


<div style="clear: both;"></div>
</div>



<div class="fragment-body">

	<comp:toggle-panel id="report-criteria-panel" isContextual="true" titleKey="report.criteria.panel.title" open="true">
		<jsp:attribute name="body">
		
		
		<%-- 
			TODO : make something smarter here, we need to load the correct tagfile corresponding to the current
			report.		
		 --%>
		 <c:choose>
		 <c:when test="${report.class.simpleName=='ReportExecutionProgressFollowUp'}">
			<report:execution-progress-followup-criteria/>
		</c:when>
		<c:when test="${report.class.simpleName=='ReportRequirementCoverageByTests'}">
			<report:requirement-coverage-criteria />
		</c:when>
		</c:choose>
			
		
			<f:message var="generateButtonLabel" key="report.criteria.panel.button.generate.label" />
			<input type="button" class="button" style="float:right;" id="generate" value="${generateButtonLabel}" />
			<div style="clear:both;"></div>
		</jsp:attribute>
		
	</comp:toggle-panel>
	
	
	<div id="view-tabed-panel" class="not-displayed">
		<ul>
			<c:forEach var="view" items="${report.viewCatalog.viewList}">
				<li><a href="#view-content-panel"><f:message key="${view.codeKey}"/></a></li>
			</c:forEach>
			
			<%--- forgive me w3c --%>
			<div id="export-option-div" style="float:right;">
				<label><f:message key="report.view.panel.label.export.label"/></label>
				<select id="export-select">
				</select>
			
				<f:message var="exportButtonLabel" key="report.view.panel.button.export.label" />
				<input type="button" class="button" id="export" value="${exportButtonLabel}" />
			
			</div>		
		</ul>
		
		<div id="view-content-panel" style="overflow:scroll;">
	
		</div>
	
	
	</div>
		
</div>


<comp:decorate-buttons />

<%-- post initialization javascript stuffs --%>

<script type="text/javascript">
	$(function(){
		var tabPanel=$("#view-tabed-panel");
		tabPanel.tabs({
				select: function(event, ui){
					tabPanel.data("selected",ui.index);
// 					loadReport();
				}
		});
		
		tabPanel.tabs("select",${report.viewCatalog.defaultViewIndex});
		tabPanel.data("selected",${report.viewCatalog.defaultViewIndex});

	});


</script>