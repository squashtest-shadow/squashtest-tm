<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

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

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="agg" tagdir="/WEB-INF/tags/aggregates" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<s:url var="rootContext" value="/" />
<s:url var="backUrl" value="/administration" />
<s:url var="baseUrl" value="/administration/users"/>


<layout:info-page-layout titleKey="squashtm.users.title" isSubPaged="true">
	<jsp:attribute  name="head">	
		<comp:sq-css name="squash.grey.css" />	
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2 class="admin"><f:message key="label.administration" /></h2>	
	</jsp:attribute>
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.user.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
			<f:message var="back" key="label.Back" /> 
				<input id="back" type="button" value="${ back }" />
	</jsp:attribute>
	<jsp:attribute name="informationContent">	
			<div class="fragment-tabs fragment-body">
			<ul>
				<li><a href="#users-table-pane"><f:message key="label.users"/></a></li>
				<li><a href="#team-table-pane"><f:message key="label.teams"/></a></li>
			</ul>
			<div id="users-table-pane" class="table-tab">
					<div class="toolbar">
				<a id="add-user-button" href="#" class="add-user-button"><f:message key="user.add.label" /></a>
			</div>
					<div class="table-tab-wrap">
			<table id="users-list-table"  class="unstyled-table">
				<thead>
					<tr>
						<th class="user-id">Id</th>
						<th class="user-index">#</th>
						<th class="user-login datatable-filterable"><f:message key="label.Login" /></th>
						<th class="user-group"><f:message key="label.Group" /></th>
						<th class="user-firstname datatable-filterable"><f:message key="label.FirstName" /></th>
						<th class="user-lastname datatable-filterable"><f:message key="label.LastName" /></th>
						<th class="user-email datatable-filterable"><f:message key="label.Email" /></th>
						<th class="user-created-on"><f:message key="label.CreatedOn" /></th>
						<th class="user-created-by datatable-filterable"><f:message key="label.createdBy" /></th>
						<th class="user-modified-on"><f:message key="label.modifiedOn" /></th>	
						<th class="user-modified-by datatable-filterable"><f:message key="label.modifiedBy" /></th>
						<th class="empty-delete-holder"></th>
					</tr>
				</thead>
				<tbody>
					<%-- Will be populated through ajax --%>
				</tbody>
			</table>

	
		</div>
	</div><%-- /div#users-table-pane --%>
	
	<agg:teams-table-tab />
	
	
	</div><%-- /div.fragment-body.fragment-tabs --%>
	<%-- ------------------------------ Add User Dialog ------------------------------------------------ --%>
		<pop:popup id="add-user-dialog" titleKey="title.AddUser" isContextual="true"
			openedBy="add-user-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="label.Add" />
				'${ label }': function() {
					var handler = $("#add-user-dialog").data('confirm-handler');
					handler.call(this);	
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:attribute name="additionalSetup">
				width : 600
			</jsp:attribute>
			<jsp:attribute name="body">
				<table id="add-user-table">
					<tr> <td>
						<label  for="add-user-login"><f:message key="label.Login" /></label>
						</td>
						<td>
						<input type="text" id="add-user-login" size="30"/></td>
						</tr>
						<tr>
						<td> <comp:error-message forField="user-login" /> </td>
						 </tr>
					<tr> <td>
						<label  for="add-user-firstName"><f:message key="label.FirstName" /></label>
						</td>
						<td>
						<input type="text" id="add-user-firstName" size="30"/></td> 
						</tr>
						<tr>
						<td><comp:error-message forField="user-firstName" /></td>
						</tr> 
					<tr> <td>
						<label  for="add-user-lastName"><f:message key="label.LastName" /></label>
						</td>
						<td>
						<input type="text" id="add-user-lastName" size="30"/>
					</td>
					</tr>
						<tr>
					<td><comp:error-message forField="user-lastName" /></td>
					 </tr>
					<tr> <td>
						<label  for="add-user-email"><f:message key="label.Email" /></label>
						</td>
						<td>
						<input type="email" id="add-user-email" size="30"/>
					</td>
					</tr>
						<tr>
					<td><comp:error-message forField="user-email" /></td>
					 </tr>
					<tr> <td>
						<label  for="add-user-group"><f:message key="label.Group" /></label>
						</td>
						<td>
						<select id="add-user-group">
							<c:forEach var="group" items="${ usersGroupList }">
								<c:choose>
									<c:when test="${ group.simpleName == 'User' }">
										<option id="${group.qualifiedName}" value="${group.id}" selected="selected"><f:message key="user.account.group.${group.qualifiedName}.label" /></option>
									</c:when>
									<c:otherwise>
										<option id="${group.qualifiedName}" value="${group.id}"><f:message key="user.account.group.${group.qualifiedName}.label" /></option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>
					</td> </tr>
          <c:if test="${ not authenticationProvider.managedPassword }">
          <tr>
            <td>
              <label for="add-user-password"><f:message key="user.account.newpass.label" /></label>
            </td>
            <td>
              <input type="password" id="add-user-password" size="30" />
            </td>
          </tr>
          <tr>
            <td><comp:error-message forField="password" /></td>
          </tr>	
          <tr>
            <td>
              <label for="new-user-confirmpass"><f:message key="user.account.confirmpass.label"/></label>				
            </td>
            <td>
              <input type="password" id="new-user-confirmpass" size="30"/>
            </td>
          </tr>
          <tr>
            <td><comp:error-message forField="confirmpass" /></td>
          </tr>
          </c:if>
          <c:if test="${ authenticationProvider.managedPassword }">
          <tr>
            <td><label><f:message key="label.password" /></label></td>
            <td><span><f:message key="message.managedPassword" /></span></td>
          </tr>
          </c:if>
				</table>
			</jsp:attribute>
		</pop:popup>
	
	<f:message var="missingNewPassword" key="user.account.newpass.error"/>
	<f:message var="missingConfirmPassword" key="user.account.confirmpass.error"/>
	<f:message var="differentConfirmation" key="user.account.newpass.differ.error"/>
	<f:message var="deleteMessage" key="dialog.delete-user.message"/>
	<f:message var="deleteTooltip" key="tooltips.delete-user"/>
	<f:message var="ok" key="label.Confirm"/>
	<f:message var="cancel" key="label.Cancel"/>
		
	<comp:decorate-buttons />

	<script type="text/javascript">
	//<![CDATA[
		squashtm.app.teamsManager = {
			table : {
				deleteButtons : {
					popupmessage : "<f:message key='message.confirmDeleteTeam' />",
					tooltip : "<f:message key='label.deleteTeam' />"
				}
			}
		}
		require([ "common" ], function() {
    		require(["users-manager", "jquery"], function(userAdmin, $){
    			var settings = {
    				data : {
    					tableData : ${json:serialize(userList)}
    				},
    				urls : {
    					rootContext : "${rootContext}",
    					backUrl : "${backUrl}",
    					baseUrl : "${baseUrl}"
    				},
    				language : {
    					missingNewPassword : "${missingNewPassword}",
    					missingConfirmPassword : "${missingConfirmPassword}",
    					differentConfirmation : "${differentConfirmation}",
    					deleteMessage :"${deleteMessage}",
    					deleteTooltip : "${deleteTooltip}",
    					ok : "${ok}",
    					cancel :"${cancel}"
    				},
    				managedPassword: ${ authenticationProvider.managedPassword }
    			}
    			
  				$(function() {
    				userAdmin.initUserListPage(settings);
  				});
    		});
			require([ "teams-manager" ]);
		});
	//]]>
	</script>	
	</jsp:attribute>
</layout:info-page-layout>
<comp:fragment-tabs/>
