<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership.

        This is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        this software is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this software.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<f:message var="addFolderTitle"  		key="dialog.new-folder.title"/>		
<f:message var="addCampaignTitle"		key="dialog.new-campaign.title"/>
<f:message var="addIterationTitle"		key="dialog.new-iteration.title"/>	
<f:message var="renameNodeTitle"		key="dialog.rename-tree-node.title" />
<f:message var="deleteNodeTitle"		key="dialog.delete-tree-node.title"/>	
<f:message var="addLabel"		 		key="label.Add"/>
<f:message var="addAnotherLabel"		key="label.addAnother"/>
<f:message var="addAnotherLabelFem"		key="label.fem.addAnother"/>
<f:message var="cancelLabel"			key="label.Cancel"/>
<f:message var="confirmLabel"			key="label.Confirm"/>


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


<div id="add-campaign-dialog" class="popup-dialog not-displayed" title="${addCampaignTitle}">
	<table class="add-node-attributes">
		
		<tr>
			<td><label for="add-campaign-name"><f:message key="label.Name" /></label></td>

			<td><input id="add-campaign-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
					
		<tr>
			<td><label for="add-campaign-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-campaign-description" data-def="isrich"></textarea></td>
		</tr>
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabelFem}"	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>

<div id="add-iteration-dialog" class="popup-dialog not-displayed" title="${addIterationTitle}">
	<table class="add-node-attributes">
		
		<tr>
			<td><label for="add-iteration-name"><f:message key="label.Name" /></label></td>

			<td><input id="add-iteration-name" type="text" size="50" maxlength="255" /><br />
				<comp:error-message forField="name" />
			</td>
		</tr>
					
		<tr>
			<td><label for="add-iteration-description"><f:message key="label.Description" /></label></td>
			<td><textarea id="add-iteration-description" data-def="isrich"></textarea></td>
		</tr>
		
		<tr>
			<td><!-- empty placeholder : there is no label for that checbkox--></td>
			<td>
			<input id="copy-test-plan-box" name="copy-test-plan-box" type="checkbox"/>
			<label class="afterDisabled" for="copy-test-plan-box"><f:message key="dialog.new-iteration.copy"/></label>
			</td>
		</tr>		
		
	</table>	
	<div class="popup-dialog-buttonpane">
		<input 	type="button" value="${addAnotherLabelFem}"	data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>




<div id="rename-node-dialog" class="popup-dialog not-displayed" title="${renameNodeTitle}" >

	<span data-def="state=denied">
		<f:message key="dialog.label.rename-node.rejected" />		
	</span>
	
	<div data-def="state=confirm">
		<label for="rename-tree-node-text"><f:message key="dialog.rename.label" /></label>
		<input id="rename-tree-node-text" type="text" size="50" /> <br />
		<comp:error-message forField="name" />
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" 		data-def="evt=confirm, mainbtn=confirm, state=confirm"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel, mainbtn"/>
	</div>	

</div>


<div id="delete-node-dialog" class="popup-dialog not-displayed" title="${deleteNodeTitle}">
	
	<div data-def="state=pleasewait">	
 		<comp:waiting-pane/>	
	</div>
	
	<div class="not-displayed" data-def="state=confirm">
	
		<div class="display-table-row">
			<div class="display-table-cell warning-cell">
				<div class="delete-node-dialog-warning"></div>
			</div>
			<div class="display-table-cell">
				<p>
					<f:message key="dialog.label.delete-node.label.start.campaigns" />
					<span class='red-warning-message'>
						<f:message key="dialog.label.delete-nodes.campaigns.label"/>
					</span> 
					<f:message key="dialog.label.delete-node.label.end"/>
				</p>
				
				<div class="not-displayed delete-node-dialog-details">
					<p><f:message key="dialog.delete-tree-node.details"/></p>
					<ul>
					</ul>				
				</div>
				
				<p>
					<span>
						<f:message key="dialog.label.delete-node.label.cantbeundone" />
					</span>				
					<span class='bold-warning-message'>
						<f:message key="dialog.label.delete-node.label.confirm" />
					</span>				
				</p>				
				
			</div>
		</div>
	</div>
		
	<div class="not-displayed" data-def="state=rejected">
		<f:message key="dialog.label.delete-node.rejected"/>
	</div>
	
	<div class="popup-dialog-buttonpane">
		<input type="button" value="${confirmLabel}" data-def="evt=confirm, mainbtn=confirm, state=confirm"/>
		<input type="button" value="${cancelLabel}"  data-def="evt=cancel,  mainbtn=rejected"/>
	</div>
</div>



</div>