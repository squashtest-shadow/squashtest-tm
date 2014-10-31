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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>

<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="milestoneUrl" value="/milestone/{milestoneId}">
	<s:param name="milestoneId" value="${milestone.id}" />
</s:url>
<s:url var="milestonesUrl" value="/administration/milestones" />

<f:message var="renameLabel" key="label.Rename" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="dateFormat" key="squashtm.dateformatShort" />
<f:formatDate value="${ milestone.endDate }" var="formatedEndDate" pattern="${dateFormat}"/>


<layout:info-page-layout titleKey="workspace.milestone.info.title" isSubPaged="true">
	<jsp:attribute name="head">	
		<comp:sq-css name="squash.grey.css" />	
	</jsp:attribute>

	<jsp:attribute name="titlePane"><h2 class="admin"><f:message key="label.administration" /></h2></jsp:attribute>
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.milestone.info.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href= '${milestonesUrl}'"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">

		<div id="milestone-name-div"
			class="ui-widget-header ui-corner-all ui-state-default fragment-header">

			<div style="float: left; height: 3em">
				<h2>
					<label for="milestone-name-header"><f:message
							key="label.Milestone" />
					</label><a id="milestone-name-header" ><c:out
							value="${ milestone.label }" escapeXml="true" />
					</a>
				</h2>
			</div>
			<div class="unsnap"></div>

		</div>
	
		<div class="fragment-body">
			<%------------------------------------------------ BODY -----------------------------------------------%>
	
			<div id="milestone-toolbar" classes="toolbar-class ui-corner-all">
				<%--- Toolbar ---------------------%>
				
			<div class="toolbar-button-panel">
				<f:message var="rename" key="rename" />
				<input type="button" value="${ rename }" id="rename-milestone-button"
							class="sq-btn" />
			</div>
			</div>
			<%--------End Toolbar ---------------%>
		
			<%----------------------------------- INFORMATION PANEL -----------------------------------------------%>
			<br />
			<br />
			<comp:toggle-panel id="milestone-info-panel"
				titleKey="label.MilestoneInformations" open="true">
	
				<jsp:attribute name="body">
					<div id="milestone-description-table" class="display-table">
					
						<div class="display-table-row">
							<label for="milestone-end-date" class="display-table-cell">
							<f:message key="label.EndDate" />
							</label>
							<div class="display-table-cell" ><span id="milestone-end-date" >${formatedEndDate}</span></div>
						</div>
					
					<div class="display-table-row">
							<label for="milestone-status" class="display-table-cell">
							<f:message key="label.Status" />
							</label>
							<div class="display-table-cell" ><span id="milestone-status" >	${ milestoneStatusLabel } </span></div>
						</div>
						
						<div class="display-table-row">
							<label for="milestone-description" class="display-table-cell">
							<f:message key="label.Description" />
							</label>
							<div class="display-table-cell editable rich-editable" data-def="url=${milestoneUrl}" id="milestone-description">${ milestone.description }</div>
						</div>
					
					</div>
				</jsp:attribute>
			</comp:toggle-panel>
			
			
			<%-----------------------------------END INFORMATION PANEL -----------------------------------------------%>
			</div>
		<%---------------------------------------------------------------END  BODY -----------------------------------------------%>
	</jsp:attribute>
</layout:info-page-layout>


<!-- --------------------------------RENAME POPUP--------------------------------------------------------- -->
  
    <f:message var="renameBTTitle" key="dialog.rename-bugtracker.title" />
    <div id="rename-milestone-dialog" class="not-displayed popup-dialog"
        title="${renameBTTitle}">
  
        <label><f:message key="dialog.rename.label" /></label>
        <input type="text" id="rename-milestone-input" maxlength="255" size="50" />
        <br />
        <comp:error-message forField="name" />
  
        
        <div class="popup-dialog-buttonpane">
          <input type="button" value="${renameLabel}" data-def="mainbtn, evt=confirm"/>
          <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
        </div>        
    </div>


  
<!-- ------------------------------------END RENAME POPUP------------------------------------------------------- -->

<script type="text/javascript">

  //*****************Back button  
  
  function clickBugtackerBackButton(){
    document.location.href = "${milestonesUrl}";
  }
 
  
  function initRenameDialog(){
    var renameDialog = $("#rename-milestone-dialog");
    renameDialog.formDialog();
    
    renameDialog.on('formdialogopen', function(){
          var name = $.trim($('#milestone-name-header').text());
          $("#rename-milestone-input").val($.trim(name));    
    });
    
    renameDialog.on('formdialogconfirm', function(){
      var params = { newName : $("#rename-milestone-input").val() };
      $.ajax({
        url : "${ milestoneUrl }",
        type : 'POST',
        dataType : 'json',
        data : params
      }).success(function(data){
  	    $('#milestone-name-header').html(data.newName);
  	    renameDialog.formDialog('close');   	  
      });
    });
    
    renameDialog.on('formdialogcancel', function(){
    	renameDialog.formDialog('close');
    });
    
    $("#rename-milestone-button").on('click', function(){
    	renameDialog.formDialog('open');
    });
    
  }
  
  
  require(["common"], function(){
	  require(["jquery","squash.translator", "squash.basicwidgets","jeditable.selectJEditable", "squash.configmanager", "jquery.squash.formdialog", "jeditable.datepicker"], function(jquery, translator, basic, SelectJEditable, confman){

		  $(function(){
	    	
			    
		    	var settings = {
			    	    urls : {
			    	           milestoneUrl : "${milestoneUrl}"
			    		    	},
			    	     data :{
			    	           milestoneStatus : ${milestoneStatus}
			    			}
			    	};
			  
				var postfn = function(value){
					var localizedDate = value;
					var postDateFormat = $.datepicker.ATOM;
					var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
					var postDate = $.datepicker.formatDate(postDateFormat, date);
				
					return $.ajax({
						url : settings.urls.milestoneUrl,
						type : 'POST',
						data : { newEndDate : postDate }
					})
					.done(function(){
						$("#milestone-end-date").text(value);
					});
				};
		    	
		    	var dateSettings = confman.getStdDatepicker(); 
				$("#milestone-end-date").editable(postfn, {
					type : 'datepicker',
					datepicker : dateSettings,
					name : "value"
				});
		    	
				var statusEditable = new SelectJEditable({
				target : settings.urls.milestoneUrl,
                componentId : "milestone-status",
				jeditableSettings : {
					data : settings.data.milestoneStatus
				},
			});	
	    	
	      basic.init();
	      $("#back").click(clickBugtackerBackButton);
	      initRenameDialog();
	    });
	  });	  
  });


</script>