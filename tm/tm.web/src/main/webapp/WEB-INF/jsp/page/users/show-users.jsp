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

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="json" uri="http://org.squashtest.tm/taglib/json" %>

<s:url var="rootContext" value="/" />
<s:url var="backUrl" value="/administration" />
<s:url var="baseUrl" value="/administration/users"/>


<layout:info-page-layout titleKey="squashtm.users.title" isSubPaged="true">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.grey.css" />	
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
	<div class="fragment-body">
		<div id="users-table-pane">
			<div style="float: right;">
				<a id="add-user-button" href="#" class="add-user-button"><f:message key="user.add.label" /></a>
			</div>
			<div style="clear:both"></div>
				
			<table id="users-list-table">
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
	<%-- ------------------------------ Add User Dialog ------------------------------------------------ --%>
		<comp:popup id="add-user-dialog" titleKey="title.AddUser" isContextual="true"
			openedBy="add-user-button" width="400">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="label.Add" />
				'${ label }': function() {
					var handler = $("#add-user-dialog").data('confirm-handler');
					handler.call(this);	
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<table id="add-user-table">
					<tr> <td>
						<label style="font-weight: bold;" for="add-user-login"><f:message key="label.Login" /></label>
						</td>
						<td>
						<input type="text" id="add-user-login" size="30"/></td>
						</tr>
						<tr>
						<td> <comp:error-message forField="user-login" /> </td>
						 </tr>
					<tr> <td>
						<label style="font-weight: bold;" for="add-user-firstName"><f:message key="label.FirstName" /></label>
						</td>
						<td>
						<input type="text" id="add-user-firstName" size="30"/></td> 
						</tr>
						<tr>
						<td><comp:error-message forField="user-firstName" /></td>
						</tr> 
					<tr> <td>
						<label style="font-weight: bold;" for="add-user-lastName"><f:message key="label.LastName" /></label>
						</td>
						<td>
						<input type="text" id="add-user-lastName" size="30"/>
					</td>
					</tr>
						<tr>
					<td><comp:error-message forField="user-lastName" /></td>
					 </tr>
					<tr> <td>
						<label style="font-weight: bold;" for="add-user-email"><f:message key="label.Email" /></label>
						</td>
						<td>
						<input type="email" id="add-user-email" size="30"/>
					</td>
					</tr>
						<tr>
					<td><comp:error-message forField="user-email" /></td>
					 </tr>
					<tr> <td>
						<label style="font-weight: bold;" for="add-user-group"><f:message key="label.Group" /></label>
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
					<tr> <td>
						<label  style="font-weight: bold;" for="add-user-password"><f:message key="user.account.newpass.label"/></label>
						</td>
						<td>
						<input type="password" id="add-user-password" size="30"/>
					</td>
					</tr>
					<tr>
					<td><comp:error-message forField="password" /></td>
					 </tr>	
					<tr> <td>
						<label style="font-weight: bold;" for="new-user-confirmpass"><f:message key="user.account.confirmpass.label"/></label>				
						</td>
						<td>
						<input type="password" id="new-user-confirmpass" size="30"/>
					</td>
					</tr>
						<tr>
					<td><comp:error-message forField="confirmpass" /></td>
					 </tr>
				</table>
			</jsp:body>
		</comp:popup>
	</div>
	
	
	<f:message var="missingNewPassword" key="user.account.newpass.error"/>
	<f:message var="missingConfirmPassword" key="user.account.confirmpass.error"/>
	<f:message var="differentConfirmation" key="user.account.newpass.differ.error"/>
	<f:message var="deleteMessage" key="dialog.delete-user.message"/>
	<f:message var="deleteTooltip" key="tooltips.delete-user"/>
	<f:message var="ok" key="label.Confirm"/>
	<f:message var="cancel" key="label.Cancel"/>
		
	<script type="text/javascript">
	
		$(function(){
			require(['users-manager'], function(userAdmin){
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
					}
				}
				
				userAdmin.initUserListPage(settings);
			});
		});
		
	</script>	
	<comp:decorate-buttons />
	</jsp:attribute>

</layout:info-page-layout>

