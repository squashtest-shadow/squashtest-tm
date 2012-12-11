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
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="org.squashtest.csp.tm.domain.project.*" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"  %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="pu" uri="http://org.squashtest.tm/taglib/project-utils" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<s:url var="administrationUrl" value="/administration" />
<layout:info-page-layout titleKey="squashtm.project.title" isSubPaged="true">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.grey.css" />
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>
	</jsp:attribute>
		<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.project.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}" onClick="document.location.href= '${administrationUrl}'"/>	
	</jsp:attribute>
	<jsp:attribute name="informationContent">
		
		<script type="text/javascript">
		squashtm.app.projectsManager = { 
				deferLoading: ${ fn:length(projects) }, 
				tooltips: {
					template: "<f:message key='label.projectTemplate' />",
					project: "<f:message key='label.project' />"
				},
				messages: {
					info : "<f:message key='popup.title.info'/>",
					noProjectTemplateMessage : "<f:message key='message.noProjectTemplateSource'/>"
					} 
				
		};
	  require([ "common" ], function() {
	    require([ "projects-manager" ]);
	  });
	  </script>		
		<%----------------------------------- Projects Table -----------------------------------------------%>
<div class="fragment-body unstyled">
  <sec:authorize access=" hasRole('ROLE_ADMIN')">
  <input id="new-project-button" class="snap-right" type="button" value="<f:message key='project.button.add.label' />" />
  <input id="new-project-from-template-button" class="snap-right" type="button" value="<f:message key='label.createFromATemplate'/>"/>
  </sec:authorize>
  <div style="clear:both"></div>
  <table id="projects-table">
  	<thead>
  		<tr>
  			<th>Id(not shown)</th> 
  			<th class="button-cell">#</th>
  			<th><f:message key="label.Name" /></th>
        <th>raw type (not shown)</th> 
        <th class="icon-cell">&nbsp;</th> 
  			<th><f:message key="label.tag" /></th>
  			<th><f:message key="label.active" /></th>
  			<th><f:message key="label.CreatedOn" /></th>
  			<th><f:message key="label.createdBy" /></th>
  			<th><f:message key="label.modifiedOn" /></th>	
  			<th><f:message key="label.modifiedBy" /></th>		
  		</tr>
  	</thead>
  	<tbody>
      <c:forEach var="project" items="${ projects }" varStatus="status">
      <tr>
        <td class="project-id">${ project.id }</td> 
        <td class="button-cell select-handle centered">${ status.index }</td>
        <td class="name">${ project.name }</td>
        <c:choose>
        <c:when test="${ pu:isTemplate(project) }">
        <td class="raw-type">template</td>
        <td class="type-template type" title="<f:message key='label.projectTemplate' />">&nbsp</td>
        </c:when>
        <c:otherwise>
        <td class="raw-type">project</td>
        <td class="icon-cell type-project" title="<f:message key='label.project' />">&nbsp</td>
        </c:otherwise>
        </c:choose>
        <td>${ project.label }</td>
        <td><f:message key="squashtm.yesno.${ project.active }" /></td>
        <td><comp:date value="${ project.createdOn }" /></td>
        <td><comp:user value="${ project.createdBy }" /></td>
        <td><comp:date value="${ project.lastModifiedOn }" /></td>
        <td><comp:user value="${ project.lastModifiedBy }" /></td>
      </tr>
      </c:forEach>
    </tbody>
  </table>

  <sec:authorize access=" hasRole('ROLE_ADMIN')">
  <!--   ===========================CREATE PROJECT DIALOG=======================================  -->
  <div id="add-project-dialog" class="not-displayed popup-dialog form-horizontal" title="<f:message key='title.addProject' />">
    <table class="form-horizontal">
    	<tr class="control-group">
    		<td>
          <label class="control-label" for="add-project-name">
            <f:message key="label.Name" />
          </label>
        </td>
    		<td class="controls">
          <input id="add-project-name" name="add-project-name" type="text" size="50" maxlength="255" />
    		  <span class="help-inline">&nbsp;</span>
        </td>
    	</tr>
      <tr class="control-group">
        <td>
          <label class="control-label" for="isTemplate"><f:message key="label.projectTemplate" /></label>
        </td>
        <td class="controls">
          <input name="isTemplate" type="checkbox" />
          <span class="help-inline">&nbsp;</span>
        </td>
      </tr>
    	<tr class="control-group">
    		<td>
          <label class="control-label" for="add-project-description"><f:message key="label.Description" /></label>
        </td>
    		<td class="controls">
          <textarea id="add-project-description" name="add-project-description"></textarea>
          <span class="help-inline">&nbsp;</span>
        </td>
    	</tr>
    	<tr class="control-group">
    		<td>
          <label class="control-label" for="add-project-label"><f:message key="label.tag" /></label>
        </td>
    		<td class="controls">
          <input id="add-project-label" name="add-project-label" type="text" size="50" maxlength="255" />
          <span class="help-inline">&nbsp;</span>
        </td>
    	</tr>

    </table>
    
    <div class="popup-dialog-buttonpane">
      <input class="confirm" type="button" value="<f:message key='label.Add' />" />
      <input class="cancel" type="button" value="<f:message key='label.Cancel' />" />
    </div>
  </div>
  <!--   ===========================/CREATE PROJECT DIALOG=======================================  -->
<!--   ===========================CREATE FROM TEMPLATE DIALOG=======================================  -->
  <div id="add-project-from-template-dialog" class="not-displayed popup-dialog form-horizontal" title="<f:message key='title.addProjectFromTemplate' />">
    <table class="form-horizontal">
    	<tr class="control-group">
    		<td>
          <label class="control-label" for="add-project-from-template-name">
            <f:message key="label.Name" />
          </label>
        </td>
    		<td class="controls">
          <input id="add-project-from-template-name" name="add-project-from-template-name" type="text" size="50" maxlength="255" />
    		  <span class="help-inline">&nbsp;</span>
        </td>
    	</tr>
       	<tr class="control-group">
    		<td>
          <label class="control-label" for="add-project-from-template-description"><f:message key="label.Description" /></label>
        </td>
    		<td class="controls">
          <textarea id="add-project-from-template-description" name="add-project-from-template-description"></textarea>
          <span class="help-inline">&nbsp;</span>
        </td>
    	</tr>
    	<tr class="control-group">
    		<td>
          <label class="control-label" for="add-project-from-template-label"><f:message key="label.tag" /></label>
        </td>
    		<td class="controls">
          <input id="add-project-from-template-label" name="add-project-from-template-label"  type="text" size="50" maxlength="255" />
          <span class="help-inline">&nbsp;</span>
        </td>
    	</tr>
    	<!--     	TEMPLATE COMBO -->    	
    	<tr class="control-group">
    		<td>
          <label class="control-label" for="add-project-from-template-tempate"><f:message key="label.projectTemplate" /></label>
        </td>
    	<td class="controls">
          <div id="add-project-from-template-template" ></div>
         </td>
    	</tr>
    	<tr class="control-group">
    	<td>
          <label class="control-label" for="add-project-from-template-tempate"><f:message key="label.parametersFromTemplate" /></label>
        </td>
        <td>
    	<!--     	 CHECKBOXES -->
    	<input id="copyPermissions" name="copyPermissions" type="checkbox" />
          <label class=" afterDisabled" for="copyPermissions"><f:message key="label.copyPermissions" /></label>
      	 <br/>
      	 <input id="copyCUF"  name="copyCUF" type="checkbox" />
         <label class=" afterDisabled" for="copyCUF"><f:message key="label.copyCUF" /></label>
       	<br/>
          <input id="copyBugtrackerBinding" name="copyBugtrackerBinding" type="checkbox" />
         <label class=" afterDisabled" for="copyBugtrackerBinding"><f:message key="label.copyBugtrackerBinding" /></label>
      	<br/>
         <input id="copyAutomatedProjects" name="copyAutomatedProjects" type="checkbox" />
          <label class=" afterDisabled" for="copyAutomatedProjects"><f:message key="label.copyAutomatedProjects" /></label>
     	 </td>
      </table>
    
    <div class="popup-dialog-buttonpane">
      <input class="confirm" type="button" value="<f:message key='label.Add' />" />
      <input class="cancel" type="button" value="<f:message key='label.Cancel' />" />
    </div>
    
    <script id="templates-list-tpl" type="text/x-handlebars-template">
			<select>
        {{#each items}}
        <option value="{{this.id}}">{{this.name}}</option>
        {{/each}}
			</select>
     </script>
    
  </div>
  <!--   ===========================/CREATE FROM TEMPLATE DIALOG=======================================  -->
  </sec:authorize>
</div>
</jsp:attribute>
</layout:info-page-layout>