<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2015 Henix, henix.fr

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
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>

<s:url var="administrationUrl" value="/administration" />

<f:message var="confirmLabel" key="label.Confirm" />
<f:message var="addLabel" key="label.Add" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="addClientTitle" key="label.addClientTitle" />
<f:message var="deleteClientTitle" key="label.deleteClientTitle" />

<layout:info-page-layout titleKey="label.ModifyConfig" isSubPaged="true">
	<jsp:attribute name="head">
		<comp:sq-css name="squash.grey.css" />
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2 class="admin">
			<f:message key="label.administration" />
		</h2>	
	</jsp:attribute>

	<jsp:attribute name="subPageTitle">
		<h2>
			<f:message key="label.ModifyConfig" />
		</h2>
	</jsp:attribute>

	<jsp:attribute name="subPageButtons">
		<f:message var="backButtonLabel" key="label.Back" />
		<input type="button" class="button" value="${backButtonLabel}"
			onClick="document.location.href= '${administrationUrl}'" />	
	</jsp:attribute>

	<jsp:attribute name="footer">	
		
	</jsp:attribute>

	<jsp:attribute name="informationContent">
		
		
		<c:url var="clientsUrl" value="/administration/config/clients/list" />
		<c:url var="addClientUrl" value="/administration/config/clients" />

			
		<div id="config-page-content" class="admin-message-page-content">   
             
             		<comp:toggle-panel id="config-info-panel"
				titleKey="label.ModifyConfig" open="true">
             <jsp:attribute name="body">
               	<div id="config-table" class="display-table">
					
						<div class="display-table-row">
							<label for="whiteList" class="display-table-cell">
							<f:message key="label.whiteList" />
							</label>				
							<div class="display-table-cell editable text-editable"
								id="whiteList">${whiteList}</div>	
						</div>
				</div>
                
                  	<div id="config-table" class="display-table">
					
						<div class="display-table-row">
							<label for="uploadSizeLimit" class="display-table-cell">
							<f:message key="label.uploadSizeLimit" />
							</label>
								<div class="display-table-cell editable text-editable"
								id="uploadSizeLimit">  ${uploadSizeLimit} </div>
						</div>
				</div>
                
                  	<div id="config-table" class="display-table">
					
						<div class="display-table-row">
							<label for="uploadImportSizeLimit" class="display-table-cell">
							<f:message key="label.uploadImportSizeLimit" />
							</label>
								<div class="display-table-cell editable text-editable"
								id="uploadImportSizeLimit"> ${uploadImportSizeLimit} </div>
						</div>
				</div>

              </jsp:attribute>
				     </comp:toggle-panel>
	
						<div id="client-config-panel" 
				class="sq-tg expand">
                          <div class="tg-head">
                            <h3><f:message key="label.ModifyClientConfig" /></h3>
                            <div class="tg-toolbar">
                              <button id="new-client-button" title="${addClientTitle}"
							class="sq-icon-btn btn-sm" type="submit">
                                <span class="ui-icon ui-icon-plus squared-icons">+</span>
                              </button>
                              <button id="delete-client-button" title="${deleteClientTitle}"
							class="sq-icon-btn btn-sm" type="submit">
                                <span class="ui-icon ui-icon-minus squared-icons">-</span>
                              </button>                       
                            </div>
                          </div>
                          <div class="tg-body">
                          	<table id="client-table"
						class="unstyled-table"
						data-def="ajaxsource=${clientsUrl}, hover, filter, pre-sort=1-asc">
		<thead>
			<tr>
				<th data-def="map=entity-id, invisible"> </th>
				<th data-def="map=index, select">#</th>
				<th data-def="map=name, sortable" class="datatable-filterable"><f:message
										key="label.Name" /></th>
				<th data-def="map=secret, sortable"><f:message
										key="label.secret" /></th> 
				<th data-def="map=redirect_uri, sortable"><f:message
										key="label.redirect_uri" /></th> 
				<th data-def="map=delete, delete-button=#delete-client-popup"></th>				
			</tr>
		</thead>
		<tbody>	
		</tbody>
	</table>
	

	<div id="delete-client-popup" class="popup-dialog not-displayed" title="${deleteClientTitle}">
		
		<div class="display-table-row">
            <comp:notification-pane type="error">
              <jsp:attribute name="htmlcontent">
                  <div class="display-table-cell">
                    <span><f:message key="message.client.remove.first"/></span>
                    <span class="red-warning-message"><f:message key="message.client.remove.second"/></span>
                    <span><f:message key="message.client.remove.third"/></span>
                    <span class="bold-warning-message"><f:message key="message.client.remove.fourth"/></span>
                  </div>              
              </jsp:attribute>
            </comp:notification-pane>

            <div id="warning-delete" class="display-table-cell">
			</div>
		</div>
		<div class="popup-dialog-buttonpane">
		    <input class="confirm" type="button" value="${confirmLabel}" />
		    <input class="cancel" type="button" value="${cancelLabel}" />				
		</div>
	
	</div>	
	
    <div id="add-client-dialog" class="not-displayed popup-dialog" 
          title="${addClientTitle}" />
          
        <table>
          <tr>
            <td><label for="add-client-name"><f:message
              key="label.Name" /></label></td>
            <td><input id="add-client-name" type="text" size="30" maxlength="30"/>
            <comp:error-message forField="label" /></td>
          </tr>
          <tr>
            <td><label for="add-client-secret"><f:message
              key="label.secret" /></label></td>
            <td><input id="add-client-secret" type="text" size="30" maxlength="30"/>
            <comp:error-message forField="label" /></td>
          </tr>        
          <tr>
            <td><label for="add-redirect_uri"><f:message
              key="label.redirect_uri" /></label></td>
            <td><input id="add-client-uri" type="text" size="30"/>
            <comp:error-message forField="label" /></td>
          </tr>  
        </table>
      <div class="popup-dialog-buttonpane">
        <input type="button" value="${confirmLabel}" data-def="mainbtn, evt=confirm"/>
        <input type="button" value="${cancelLabel}" data-def="evt=cancel"/>
      </div>     
</div>
                         </div>			     
			</div>
		</div>
	</jsp:attribute>
</layout:info-page-layout>

<script type="text/javascript">
	require([ "common" ], function() {
		require([ "client-manager/client-manager" ], function() {
		});
	});
</script>



