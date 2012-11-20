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
<%@ taglib prefix="sq" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

<s:url var="userListTableUrl" value="/administration/users/table" />
<s:url var="userDetailsBaseUrl" value="/administration/users" />
<s:url var="addUserUrl" value="/administration/users/add" />
<s:url var="administrationUrl" value="/administration" />

<layout:info-page-layout titleKey="squashtm.users.title" isSubPaged="true">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.grey.css" />	
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="label.administration" /></h2>	
	</jsp:attribute>
	<jsp:attribute name="subPageTitle">
		<h2><f:message key="workspace.user.title" /></h2>
	</jsp:attribute>
	
	<jsp:attribute name="subPageButtons">
			<f:message var="back" key="label.Back" /> 
				<input id="back" type="button" value="${ back }" />
	</jsp:attribute>
	<jsp:attribute name="informationContent">	
			<script type="text/javascript">
			
				$(function() {
					$('#add-user-button').button();
					
					$("#back").button().click(function(){
						document.location.href= "${administrationUrl}";
					});
				});
			
				function refreshUsers() {
					var dataTable = $('#users-list-table').dataTable();
					dataTable.fnDraw();
				}
				
				function getUserTableRowId(rowData) {
					return rowData[0];	
				}
				
				function addHLinkToUserLogin(row, data) {
					var url= '${ userDetailsBaseUrl }/' + getUserTableRowId(data) + '/info';			
					addHLinkToCellText($( 'td:eq(1)', row ), url);
				}	
				
				function userTableRowCallback(row, data, displayIndex) {
					addHLinkToUserLogin(row, data);
					var template = '<a href="javascript:void(0)">'+'</a>';
					var cells = $('td.delete-button', row);
					cells.html(template);
					cells.find('a').button({
						text : false,
						icons : {
							primary : "ui-icon-minus"
						}
					});
					bindDeleteButton(row,data);
					return row;
				}
				
				function bindDeleteButton(row,data) {
					
					var button = $('td.delete-button > a', row);
					var id = getUserTableRowId(data);
					
					button.click(function(){
						$.ajax({
							  type: 'POST',
							  url: "${userDetailsBaseUrl}/"+id+"/remove",
							  data : {userId : id},
							  dataType: 'json',
							  success: function(){
								  refreshUsers();
							  }
						});
					});
				}

				<f:message var="newPassError" key="user.account.newpass.error"/>
				<f:message var="confirmPassError" key="user.account.confirmpass.error"/>
				<f:message var="samePassError" key="user.account.newpass.differ.error"/>	

				<%-- we validate the passwords only. Note that validation also occurs server side. --%>
				function validatePassword(){
					//first, clear error messages
					$("#add-user-table span.error-message").html('');

					var newPassOkay=true;
					var confirmPassOkay=true;
					var samePassesOkay=true;
					
					if (! isFilled("#add-user-password")){
						$("span.error-message.password-error").html("${newPassError}");
						newPassOkay=false;
					}

					if (! isFilled("#new-user-confirmpass")){
						$("span.error-message.confirmpass-error").html("${confirmPassError}");
						confirmPassOkay=false;
					}				
					
					if ((newPassOkay==true) && (confirmPassOkay==true)){
						var pass = $("#add-user-password").val();
						var confirm = $("#new-user-confirmpass").val();
						
						if ( pass != confirm){
							$("span.error-message.password-error").html("${samePassError}");
							samePassesOkay=false;
						}
					}
					
					return ( (newPassOkay) && (confirmPassOkay) &&(samePassesOkay) );
					
				}

				<%-- returns wether the field was filled or not --%>
				//note : I don't trust hasOwnProperty due to its cross-browser issues. We'll
				//do it low tech once again.
				function isFilled(selector){
					var value = $(selector).val();
					if (value.length==0){
						return false;
					}else{
						return true;
					}
					
				}
				
			</script>
			
			<div class="fragment-body">
			
			<div id="users-table-pane">
	
				
				<div style="float: right;">
				<a id="add-user-button" href="#" class="add-user-button"><f:message key="user.add.label" /></a>
			</div>
				<div style="clear:both"></div>
				
					<table id="users-list-table">
						<thead>
							<tr>
								<th>Id</th>
								<th>#</th>
								<th><f:message key="label.Login" /></th>
 								<th><f:message key="label.Group" /></th>
								<th><f:message key="label.FirstName" /></th>
								<th><f:message key="label.LastName" /></th>
								<th><f:message key="label.Email" /></th>
								<th><f:message key="label.CreatedOn" /></th>
								<th><f:message key="project.workspace.table.header.createdby.label" /></th>
								<th><f:message key="project.workspace.table.header.modifiedon.label" /></th>	
								<th><f:message key="project.workspace.table.header.modifiedby.label" /></th>
								<th></th>
							</tr>
						</thead>
						<tbody>
							<%-- Will be populated through ajax --%>
						</tbody>
					</table>
				
				
				<comp:decorate-ajax-table url="${ userListTableUrl }" tableId="users-list-table" paginate="true">
					<jsp:attribute name="rowCallback">userTableRowCallback</jsp:attribute>
					<jsp:attribute name="columnDefs">
						<dt:column-definition targets="0" visible="false" />
						<dt:column-definition targets="1" sortable="false" cssClass="select-handle centered" width="2em"/>
						<dt:column-definition targets="2, 3, 4, 5, 6, 7, 8, 9" sortable="true" />
						<dt:column-definition targets="10" sortable="true" width="2em"/>
						<dt:column-definition targets="11" sortable="false" width="2em" lastDef="true" cssClass="centered delete-button"/>
					</jsp:attribute>
				</comp:decorate-ajax-table>	
				
	
	</div>
	<%-- ------------------------------ Add User Dialog ------------------------------------------------ --%>
	<comp:popup id="add-user-dialog" titleKey="title.AddUser" isContextual="true"
		openedBy="add-user-button" width="400">
		<jsp:attribute name="buttons">
		
			<f:message var="label" key="label.Add" />
			'${ label }': function() {
				if (! validatePassword()) return;
				var url = "${ addUserUrl }";
				<jq:ajaxcall url="url" httpMethod="POST" useData="true" successHandler="refreshUsers" dataType="json">		
					<jq:params-bindings login="#add-user-login" firstName="#add-user-firstName" lastName="#add-user-lastName"
					 email="#add-user-email" groupId="#add-user-group" password="#add-user-password" />
				</jq:ajaxcall>		
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
	<comp:decorate-buttons />
	</jsp:attribute>
</layout:info-page-layout>

<script type="text/javascript">

	$(function(){
		$("#add-user-dialog").bind( "dialogclose", cleanUp);
	});
	
	function cleanUp(){
		$("#add-user-password").val('');
		$("#new-user-confirmpass").val('');		
	}
	
</script>