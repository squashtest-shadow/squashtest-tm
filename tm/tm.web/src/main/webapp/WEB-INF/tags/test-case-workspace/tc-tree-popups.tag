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


<f:message var="addFolderTitle"  	key="dialog.new-folder.title"/>	
<f:message var="addLabel"		 	key="label.Add"/>
<f:message var="addAnotherLabel"	key="label.addAnother"/>
<f:message var="cancelLabel"		key="label.Cancel"/>


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
		<input 	type="button" value="${addAnotherLabel}"    data-def="evt=add-another, mainbtn"/>
		<input 	type="button" value="${addLabel}" 			data-def="evt=add-close"/>
		<input  type="button" value="${cancelLabel}" 		data-def="evt=cancel"/>
	</div>
</div>


</div>