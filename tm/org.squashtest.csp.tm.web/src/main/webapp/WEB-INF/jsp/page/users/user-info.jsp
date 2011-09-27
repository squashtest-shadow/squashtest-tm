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
<link rel="stylesheet" type="text/css"
	href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib tagdir="/WEB-INF/tags/jquery" prefix="jq"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="dt" tagdir="/WEB-INF/tags/datatables" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%------------------------------------- URLs et back button ----------------------------------------------%>
<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<s:url var="userUrl" value="/users/{userId}">
	<s:param name="userId" value="${user.id}" />
</s:url>
<c:url var="usersUrl" value="/users/list" />

<s:url var="permissionTableUrl" value="/users/{userId}/permission-table">
	<s:param name="userId" value="${user.id}" />
</s:url>

<s:url var="permissionPopupUrl" value="/users/{userId}/permission-popup">
	<s:param name="userId" value="${user.id}" />
</s:url>

<s:url var="addPermissionUrl" value="/users/{userId}/add-permission">
	<s:param name="userId" value="${user.id}" />
</s:url>

<s:url var="removePermissionUrl" value="/users/{userId}/remove-permission">
	<s:param name="userId" value="${user.id}" />
</s:url>

<s:url var="changeUserGroupUrl" value="/users/{userId}/change-group">
	<s:param name="userId" value="${user.id}" />
</s:url>

<f:message key="dialog.add-permission.empty.label" var="emptyMessage" />

<layout:info-page-layout titleKey="workspace.user.title">
	<jsp:attribute  name="head">	
		<link rel="stylesheet" type="text/css" href="${ pageContext.servletContext.contextPath }/styles/master.blue.css" />	
	</jsp:attribute>
	
	<jsp:attribute name="titlePane">
		<h2><f:message key="user.table.title" /></h2>	
	</jsp:attribute>
	
	<jsp:attribute name="informationContent">
		<div id="user-info-pane">
		<%---------------------------- Test Case Header ------------------------------%>

		<script type="text/javascript">
		
		<%---
		
		unused yet
		
		$("#user-active").change(function(){
			var url = '${userUrl}?active='+ $("#user-active").is(':checked');
			$.ajax({
				  type: 'POST',
				  url: url,
				  dataType: 'json'
				});
		});	
		--%>	
		
		
		
			$(function(){
				refreshTableAndPopup()
				setActiveValue();
				$("#add-permission-button").button();
				$("#back").button().click(function(){
					document.location.href= "${usersUrl}";
				});
				
				$("#add-permission-dialog").bind("dialogopen", function(event, ui) {
					if ($("#project-input option:last-child").html() == null){
						$(this).dialog('close');	
						alert('${emptyMessage}');
					}
				});

				
				$(".select-class").live('change', function(){
					var url = "${addPermissionUrl}";
					var tr = $(this).parents("tr");
					var projectId = $(tr).attr("id");

					$.ajax({
						  type: 'POST',
						  url: url,
						  data: "project="+projectId+"&permission="+$(this).val(),
						  dataType: 'json',
						  success: function(){
							  refreshTableAndPopup();
						  }
					});
				});
				
				$(".delete-permission-button").live('click', function(){
					var url = "${removePermissionUrl}";
					var tr = $(this).parents("tr");
					var projectId = $(tr).attr("id");
					
					$.ajax({
						  type: 'POST',
						  url: url,
						  data: "project="+projectId,
						  success: function(){
							refreshTableAndPopup();
						  }
					});
				});
				
				$("#user-group").change(function(){
					var url = "${changeUserGroupUrl}";
					$.ajax({
						  type: 'POST',
						  url: url,
						  data: "groupId="+$(this).val(),
						  dataType: 'json'
					});
				});
				
				
				
			});
			
			function setActiveValue(){
				if (${user.active} == true){
					$("#user-active").attr('checked', true);
				}
			}
			
			function getPermissionTableRowId(rowData) {
				return rowData[0];	
			}
			
			function addDeleteButtonCallBack(row, data, displayIndex){
				var id = getPermissionTableRowId(data);
				addDeleteButtonToRow(row, id, 'delete-permission-button');
				return row;
			}
			
			function refreshTableCallBack(){
				decorateDeleteButtons($('.delete-permission-button', this));
			}
			
			function refreshTableAndPopup(){
				$("#permission-table").empty();
				$("#permission-table").load("${permissionTableUrl}");
				$("#permission-popup").empty();
				$("#permission-popup").load("${permissionPopupUrl}");
			}
		</script>

	
		<div id="user-login-div"
			class="ui-widget-header ui-corner-all ui-state-default fragment-header">

			<div style="float: left; height: 3em">
				<h2>
					<label for="user-login-header"><f:message
							key="user.header.title" />
					</label><a id="user-login-header" href="${userUrl }/info"><c:out
							value="${ user.login }" escapeXml="true" />
					</a>
				</h2>
			</div>

			<div style="float: right;">
				<f:message var="back" key="fragment.edit.header.button.back" />
				<input id="back" type="button" value="${ back }" />
			</div>

			<div style="clear: both;"></div>

		</div>
		<div>
			<comp:general-information-panel auditableEntity="${user}" />
		</div>

		<%----------------------------------- User Infos -----------------------------------------------%>
		<br />
		<comp:simple-jeditable targetUrl="${ userUrl }"
			componentId="user-login" />
		<comp:simple-jeditable targetUrl="${ userUrl }"
			componentId="user-first-name" />
		<comp:simple-jeditable targetUrl="${ userUrl }"
			componentId="user-last-name" />
		<comp:simple-jeditable targetUrl="${ userUrl }"
			componentId="user-email" />

		<comp:toggle-panel titleKey="user.info.panel.title" isContextual="true" open="true">

			<jsp:attribute name="body">
				<table id="user-infos-table">
					<tr>
						<td> <b><f:message key="dialog.add-user.login.label" /> : </b> </td>
						<td>  <div id="user-login">${ user.login }</div> </td>
					</tr>
					<tr>
						<td> <b><f:message key="dialog.add-user.firstname.label" /> : </b> </td>
						<td> <div id="user-first-name">${ user.firstName }</div> </td>
					</tr>
					<tr>
						<td> <b><f:message key="dialog.add-user.lastname.label" /> : </b> </td>
						<td> <div id="user-last-name">${ user.lastName }</div> </td>
					</tr>
					<tr>
						<td> <b><f:message key="dialog.add-user.email.label" /> : </b> </td> 
						<td> <div id="user-email">${ user.email }</div> </td>
					</tr>
					
					
					<tr>
						<td> <b><f:message key="dialog.add-user.users.group.label" /> : </b> </td> 
						<td>
							<select id="user-group">
								<c:forEach var="group" items="${ usersGroupList }">
									<c:choose>
										<c:when test="${ group.simpleName == user.group.simpleName }">
											<option id="${group.qualifiedName}" value="${group.id}" selected="selected"><f:message key="user.account.group.${group.simpleName}.label" /></option>
										</c:when>
										<c:otherwise>
											<option id="${group.qualifiedName}" value="${group.id}"><f:message key="user.account.group.${group.simpleName}.label" /></option>
										</c:otherwise>
									</c:choose>
								</c:forEach>
							</select>
						</td>
					</tr>
				</table>
				<br />
				<%--
				<div>
					<input id="user-active" type="checkbox" /> <f:message
						key="user.active.label" />
				</div>
				 --%>
			</jsp:attribute>

		</comp:toggle-panel>

		<br />
		<f:message key="dialog.add-permission.title" var="addButtonTitle" />
		
		<comp:toggle-panel titleKey="user.project-rights.title.label" isContextual="true" open="true">
			<jsp:attribute name="panelButtons">
				<input id="add-permission-button" title="${addButtonTitle}" type="button" value="+"/>
			</jsp:attribute>
			
			<jsp:attribute name="body">
				<div id="permission-table">
				</div>
				<div id="permission-row-buttons" class="not-displayed">
					<a id="delete-permission-button" href="#" class="delete-permission-button">
						<f:message key="tree.button.delete.label" />
					</a>
				</div> 
			</jsp:attribute>
		</comp:toggle-panel>
		
				
		<comp:popup id="add-permission-dialog"
			titleKey="dialog.add-permission.title" isContextual="true"
			openedBy="add-permission-button">
			<jsp:attribute name="buttons">
			
				<f:message var="label" key="dialog.add-permission.button.label" />
				'${ label }': function() {
					var url = "${ addPermissionUrl }";
					
					<jq:ajaxcall url="url" dataType="json" httpMethod="POST"
					useData="true" successHandler="refreshTableAndPopup">					
						<jq:params-bindings project="#project-input" permission="#permission-input" />
					</jq:ajaxcall>	
					
					
				},			
				<pop:cancel-button />
			</jsp:attribute>
			<jsp:body>
				<div id="permission-popup">
				</div>
			</jsp:body>
		</comp:popup>
	<comp:decorate-buttons />
	<comp:decorate-toggle-panels/>
	</jsp:attribute>
</layout:info-page-layout>