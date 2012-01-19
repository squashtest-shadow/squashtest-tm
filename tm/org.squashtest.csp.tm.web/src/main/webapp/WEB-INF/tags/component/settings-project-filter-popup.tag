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

<%@ attribute name="openedBy" required="true" description="the id of the clickable widget that will open the popup" %>
<%@ attribute name="divId" required="true" description="the name you wish the popup to have"%>
<%@ attribute name="successCallback" required="false" description="if set, that handler will be called on completion"%>
<%@ attribute name="cancelCallback" required="false" description="if set, that handler will be called if aborted"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>


<c:url var="projectFilterUrl" value="/global-filter/filter"/>



<%-- 
	yeees that one is not contextual. Set it to true and the damn dialog will get wiped by any tree action. 	
--%>

<f:message var="selectallLabel" key="dialog.settings.filter.controls.selectall" />
<f:message var="deselectAllLabel" key="dialog.settings.filter.controls.deselectall" />
<f:message var="invertselectLabel" key="dialog.settings.filter.controls.invertselect" />



<pop:popup closeOnSuccess="false" titleKey="dialog.settings.filter.title" id="${divId}" openedBy="${openedBy}">
	<jsp:attribute name="buttons" >
		<f:message var="confirmLabel" key="dialog.button.confirm.label" />
		'${ confirmLabel }': function() {
			sendNewFilter();
			<c:if test="${not empty successCallback}">${successCallback}();</c:if>		
		},			
		<f:message var="abortlabel" key="dialog.button.cancel.label"/>
		'${ abortlabel }': function() {
			$( this ).dialog( 'close' );
			<c:if test="${not empty cancelCallback}">${cancelCallback}();</c:if>
		}
	</jsp:attribute>
	
	<jsp:attribute name="additionalSetup">
	width : 400,
	open : function(){
		loadFilterProject(); 
		
	}
	</jsp:attribute>
	
	<jsp:attribute name="body">
		<div class="project-item-template not-displayed">
			<div class="project-item ">
				<input type="checkbox" class="project-checkbox"/> <span class="project-name"></span>
			</div>
		</div>
		<div id="dialog-settings-filter-maincontent">			
			<div id="dialog-settings-filter-controls" class="project-filter-controls">
				<a id="dialog-settings-filter-selectall" href="#">${selectallLabel}</a>
				<a id="dialog-settings-filter-deselectall" href="#">${deselectAllLabel}</a>
				<a id="dialog-settings-filter-invertselect" href="#">${invertselectLabel}</a>				
			</div>	
			<hr/>
			<div id="dialog-settings-filter-projectlist" class="project-filter-list">
			
			</div>
		
			<div style="clear:both;display:hidden;"></div>
		</div>
	</jsp:attribute>

</pop:popup>




<%-- 
	code managing the initialization of the popup
 --%>
 
 <script type="text/javascript">
 
 	$(function(){
		
		$("#dialog-settings-filter-selectall").click(function(){
			selectAllProjects();
		});
		
		$("#dialog-settings-filter-deselectall").click(function(){
			deselectAllProjects();
		});
		
		$("#dialog-settings-filter-invertselect").click(function(){
			invertAllProjects();
		});
 	});
 	

 	
 	
 	function selectAllProjects(){
 		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");
 		
 		if (boxes.length==0) return;
 		
 		$(boxes).each(function(){
 			setCheckBox($(this), true);
 		});
 	}
 	
 	function deselectAllProjects(){
 		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");
 		
 		if (boxes.length==0) return;
 		
 		$(boxes).each(function(){
 			setCheckBox($(this), false);
 		});
 	}
 	
 	function invertAllProjects(){
 		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");
 		
 		if (boxes.length==0) return;
 		
 		$(boxes).each(function(){
 			setCheckBox($(this), ! $(this).is(":checked"));
 		});		
 	}
 
 
 </script>



<%-- 
	Code managing the loading phase of the popup. 
	It expects the server to send the data as a json object, see  tm.web.internal.model.jquery.FilterModel
	
	note : each project in the array is an array made of the following : { Long , String , Boolean )
--%>
<script type="text/javascript">
	
	function loadFilterProject(){
		
		clearFilterProject();
		
		$.get("${projectFilterUrl}",populateFilterProject,"json");
	}
	
	
	function clearFilterProject(){
		$("#dialog-settings-filter-projectlist").empty();
	}
	
	function populateFilterProject(jsonData){
		
		var cssClass="odd";
		var i=0;
		for (i=0;i<jsonData.projectData.length;i++){
			appendProjectItem("dialog-settings-filter-projectlist",jsonData.projectData[i], cssClass);
			cssClass=swapCssClass(cssClass);
		}
				
	}
	
	function swapCssClass(cssClass){
		if (cssClass=="odd") return "even";
		return "odd";
	}
	
	
	function setCheckBox(jqCheckbox, isEnabled){
		if (isEnabled){
			jqCheckbox.attr('checked','checked');
		}
		else{
			jqCheckbox.removeAttr('checked');
		}	
	}
	

	function appendProjectItem(containerId, projectItemData, cssClass){
		var jqNewItem = $("#${divId} .project-item-template .project-item").clone();
		jqNewItem.addClass(cssClass);
		
		var jqChkBx = jqNewItem.find(".project-checkbox");
		jqChkBx.attr('id','project-checkbox-'+parseInt(projectItemData[0]));
		
		var jqName = jqNewItem.find(".project-name");
		jqName.html(projectItemData[1]);
		
		setCheckBox(jqChkBx, projectItemData[2]);
		
		$("#"+containerId).append(jqNewItem);
	}
	
</script>




<%-- 
	code managing the data transmissions
 --%>
 
 <script type="text/javascript">
 	
 	function sendNewFilter(){
 		var isEnabled = $("#dialog-settings-isselected-checkbox").is(":checked");

 		var ids = getSelectedProjectIds("dialog-settings-filter-projectlist");	
 		$.post("${projectFilterUrl}",{projectIds : ids}, function(){
 			newFilterSuccess();
 		});
 		
 	}
 	
 	function getSelectedProjectIds(containerId){
 		var selectedBoxes = $("#"+containerId+" .project-checkbox:checked");
 		var zeids = new Array();
 		var i;
 		
 		for (i=0;i<selectedBoxes.length;i++){
 			var jqBox = $(selectedBoxes[i]);

 			zeids.push(extractId(jqBox.attr('id')));
 		}
 		
 		return zeids;
 	}
 	
 	function extractId(strDomId){
 		var idTemplate = "project-checkbox-";	
 		var templateLength = idTemplate.length;
 		var extractedId = strDomId.substring(templateLength) ;
 		return extractedId;
 	}
 	
 	function newFilterSuccess(){
 		$("#${divId}").dialog('close'); 		 		
 		<c:if test="${not empty successCallback}">${successCallback}();</c:if>
 		window.location.reload();
 	}
 	
 
 </script>

