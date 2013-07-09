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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<f:message var="addFolderTitle"  		key="dialog.new-folder.title"/>		
<f:message var="addRequirementTitle"	key="dialog.new-requirement.title"/>	
<f:message var="addLabel"		 		key="label.Add"/>
<f:message var="addAnotherLabel"		key="label.addAnother"/>
<f:message var="addAnotherLabelFem"		key="label.fem.addAnother"/>
<f:message var="cancelLabel"			key="label.Cancel"/>
<f:message var="confirmLabel"			key="label.Confirm"/>
<f:message var="deleteNodeTitle"		key="dialog.delete-tree-node.title"/>

<f:message var="deleteMessagePrefix"	key="dialog.label.delete-node.label.start" />
<f:message var="deleteMessageVariable"  key="dialog.label.delete-nodes.test-cases.label"/>
<f:message var="deleteMessageSuffix"	key="dialog.label.delete-node.label.end"/>
<f:message var="deleteMessageNoUndo"	key="dialog.label.delete-node.label.cantbeundone" />
<f:message var="deleteMessageConfirm"	key="dialog.label.delete-node.label.confirm" />


<div id="treepopups-definition" class="not-displayed">

<div id="add-folder-dialog" class="popup-dialog not-displayed" title="${addFolderTitle}">
	<table class="add-node-attributes">
		<tr>
			<td><label for="add-folder-name"><f:message key="label.Name" /></label></td>
			<td><input id="add-folder-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
		<tr>
			<td><label for="add-foldder-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-folder-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabel}"   	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>


<div id="add-requirement-dialog" class="popup-dialog not-displayed" title="${addRequirementTitle}">
	<table class="add-node-attributes">
		
		<tr>
			<td><label for="add-requirement-name"><f:message key="label.Name" /></label></td>

			<td><input id="add-requirement-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
		
		<tr>
			<td><label for="add-requirement-reference"><f:message key="label.Reference" /></label></td>
			<td><input id="add-requirement-reference" type=text size="15" maxlength="20"/><br />
				<comp:error-message forField="reference" />	<td>
		</tr>
		
		<tr>
			<td><label for="add-requirement-criticality"><f:message key="requirement.criticality.combo.label" /></label></td>
			<td><div id="criticalityList"></div></td>
		</tr>
		
		<tr>
			<td><label for="add-requirement-category"><f:message key="requirement.category.combo.label" /></label></td>
			<td><div id="categoryList"></div></td>
		</tr>		
					
		<tr>
			<td><label for="add-requirement-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-requirement-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabelFem}"	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>



<div id="delete-node-dialog" class="popup-dialog not-displayed" title="${deleteNodeTitle}">
	
	<div id="delete-node-dialog-pleasewait" class="please-wait" data-def="xor-content=pleasewait"></div>
	
	<div id="delete-node-dialog-simulation" class="not-displayed" data-def="xor-content=confirm">
	
		<div class="display-table-row">
			<div class="display-table-cell" style="vertical-align:middle;">
				<img src='${servContext}/images/messagebox_confirm.png'/>
			</div>
			<div class="display-table-cell">
				<p>
					<c:out value="${deleteMessagePrefix}"/>
					<span class='red-warning-message'><c:out value="${deleteMessageVariable}" /></span> 
					<c:out value="${deleteMessageSuffix}" />
				</p>
				
				<p id="delete-node-dialog-details" class="not-displayed"></p>
				
				<p><c:out value="${deleteMessageNoUndo}"/></p>				
				
				<p class='bold-warning-message'><c:out value="${deleteMessageConfirm}"/></p>
				
			</div>
		</div>
	</div>
		
	<div id="delete-node-dialog-rejected"   class="not-displayed" data-def="xor-content=rejected">
		<f:message key="dialog.label.delete-node.rejected"/>
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn, xor-content=confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel"/>
	</div>
</div>


</div>