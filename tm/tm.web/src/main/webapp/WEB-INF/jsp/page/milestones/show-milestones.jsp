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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<s:url var="administrationUrl" value="/administration" />

<f:message var="addLabel"       key="label.Add"/>
<f:message var="confirmLabel"   key="label.Confirm"/>
<f:message var="cancelLabel"    key="label.Cancel"/>
<jsp:useBean id="now" class="java.util.Date"  />   
<f:message var="dateFormat" key="squashtm.dateformatShort" />


<layout:info-page-layout titleKey="squashtm.milestone.title" isSubPaged="true" main="milestone-manager/milestone-manager.js">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.grey.css" />
		
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.milestone.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="sq-btn" value="${backButtonLabel}" onClick="document.location.href= '${administrationUrl}'"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">
		<c:url var="milestonesUrl" value="/administration/milestones/list" />
		<c:url var="addMilestoneUrl" value="/administration/milestones" />
		<c:url var="milestoneDetailsBaseUrl" value="/milestone" />
		<c:url var="dtMessagesUrl" value="/datatables/messages" />

		
		<%----------------------------------- Milestp,e Table -----------------------------------------------%>

<div class="fragment-body">
	<input class="snap-right sq-btn" type="button" value='<f:message key="label.AddMilestone" />'   id="new-milestone-button"/>
	<input class="snap-right sq-btn" type="button" value='<f:message key="label.deleteMilestone" />' id="delete-milestone-button"/>
	<div style="clear:both"></div>
	
	
	<table id="milestones-table" class="unstyled-table" data-def="ajaxsource=${milestonesUrl}, hover, filter, pre-sort=1-asc">
		<thead>
			<tr>
				<th data-def="map=index, select">#</th>
				<th data-def="map=label, sortable, link=${milestoneDetailsBaseUrl}/{entity-id}/info"  class="datatable-filterable"><f:message key="label.Milestone" /></th>
				<th data-def="map=status, sortable" class="datatable-filterable"><f:message key="label.Status"   /></th>
				<th data-def="map=endDate, sortable"><f:message key="label.EndDate"/></th>
		       	<th data-def="map=nbOfProjects, sortable"><f:message key="label.projects"/></th>
		        <th data-def="map=range, sortable"><f:message key="label.Range" /></th>
				 <th data-def="map=owner, sortable"><f:message key="label.Owner" /></th>
				<th data-def="map=description, sortable"><f:message key="label.Description" /></th>
			    <th data-def="map=created-on, sortable"><f:message key="label.CreatedOn" /></th>
                <th data-def="map=created-by, sortable" ><f:message key="label.createdBy" /></th>
                <th data-def="map=last-mod-on, sortable"><f:message key="label.modifiedOn" /></th>  
                <th data-def="map=last-mod-by, sortable"><f:message key="label.modifiedBy" /></th> 
				<th data-def="map=delete, delete-button=#delete-milestone-popup"></th>				
			</tr>
		</thead>
		<tbody>	
		</tbody>
	</table>

<!--
<table id="milestones-table" class="unstyled-table" data-def="filter, pre-sort=1-asc">
		<thead>
			<tr>
			    <th data-def="map=entity-id, invisible"> </th>
				<th data-def="map=index, select">#</th>
				<th data-def="map=label ,sortable"><f:message key="label.Milestone" /></th>
				<th data-def="map=status, sortable"><f:message key="label.Status"   /></th>
				<th data-def="map=endDate,sortable"><f:message key="label.EndDate"/></th>
		       	<th data-def="map=nbOfBindedProject,sortable"><f:message key="label.projects"/></th>
		        <th data-def="map=range,sortable"><f:message key="label.Range" /></th>
				 <th data-def="map=owner,sortable"><f:message key="label.Owner" /></th>
				<th data-def="map=description, sortable"><f:message key="label.Description" /></th>
			    <th data-def="map=createdOn, sortable"><f:message key="label.CreatedOn" /></th>
                <th data-def="map=createdBy, sortable"><f:message key="label.createdBy" /></th>
                <th data-def="map=lastModifiedOn, sortable"><f:message key="label.modifiedOn" /></th>  
                <th data-def="map=lastModifiedBy,sortable"><f:message key="label.modifiedBy" /></th> 
				<th data-def="map=placeholder, delete-button=#delete-milestone-popup"></th>				
			</tr>
		</thead>
		<tbody>	
		 <c:forEach items="${milestones}" var="milestone" varStatus="milestoneIndex"> 
		 <tr>
		 <td>${ milestone.id} </td>
		 <td>${milestoneIndex.index +1}</td>
		 <td><a href="${milestoneDetailsBaseUrl}/${milestone.id}/info"> ${milestone.label}</a></td>
		 <td><f:message key="${milestone.status.i18nKey}"/></td>
		 <td><f:formatDate value="${milestone.endDate}" pattern="${dateFormat}"/></td>
		 <td>${milestone.nbOfBindedProject}</td>
		 <td><f:message key="${milestone.range.i18nKey}"/></td>
		 <td>${milestone.owner.name}</td>
		 <td>${milestone.description}</td>
		 <td><f:formatDate value="${milestone.createdOn}" pattern="${dateFormat}"/></td>
		 <td>${milestone.createdBy}</td>
		 <td><f:formatDate value="${milestone.lastModifiedOn}" pattern="${dateFormat}"/></td>
		 <td>${milestone.lastModifiedBy}</td>
		 <td></td>
		 </tr>
		 </c:forEach>
		</tbody>
	</table>
-->




	<f:message var="deleteMilestoneTitle" key="dialog.delete-milestone.title" />
	<div id="delete-milestone-popup" class="popup-dialog not-displayed" title="${deleteMilestoneTitle}">
		
		<div class="display-table-row">
            <div class="display-table-cell warning-cell">
                <div class="delete-node-dialog-warning"></div>
            </div>
            <div id="warning-delete" class="display-table-cell">
			</div>
		</div>
		<div class="popup-dialog-buttonpane">
		    <input class="confirm" type="button" value="${confirmLabel}" />
		    <input class="cancel" type="button" value="${cancelLabel}" />				
		</div>
	
	</div>	

    <f:message var="addMilestoneTitle" key="dialog.new-milestone.title"/>
    <div id="add-milestone-dialog" class="not-displayed popup-dialog" 
          title="${addMilestoneTitle}" />
          
        <table>
          <tr>
            <td><label for="add-milestone-label"><f:message
              key="label.Label" /></label></td>
            <td><input id="add-milestone-label" type="text" size="30" maxlength="30"/>
            <comp:error-message forField="label" /></td>
          </tr>
        
            <td><label for="add-milestone-status"><f:message
              key="label.Status" /></label></td>
            <td>
		<select id="add-milestone-status" class="combobox">           
            <c:forEach items="${milestoneStatus}" var="status" > 
            <option value = "${status.key}" >${status.value} </option>
            </c:forEach>
            </select>
    
        </td>
        
         <tr>
       
            <td><label><f:message key="label.EndDate" /></td>    
            <td><span id="add-milestone-end-date"></span>
        <comp:error-message forField="endDate" /></td>
         </tr>  

          <tr>
            <td>
               <label for="add-milestone-description">
                   <f:message key="label.Description" />
                </label>
            </td>
            <td>
                <textarea id="add-milestone-description" name="add-milestone-description"></textarea>
            <comp:error-message forField="description" /></td>
          </tr>     
        </table>
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${addLabel}" data-def="mainbtn, evt=confirm"/>
        <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
      </div>     
</div>
</jsp:attribute>
</layout:info-page-layout>