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
<%@ tag description="Popup allowing to upload files some files" body-content="empty" %>
	
<%@ attribute name="url" required="true" description="url to upload to"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<f:message var="attachSubmit" key="label.Upload"/>
<f:message var="okLabel" key="label.Ok"/>
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="removeFileBrowser" key="dialog.attachment.add.button.remove.label" />

<div id="add-attachments-dialog" class="popup-dialog not-displayed" data-def="url=${url}">

	<%-- templates for cloning --%>
	<div class="add-attachments-templates not-displayed" style="display:none;">
		<div class="attachment-item">
			<input type="file" name="attachment[]" size="40" />
			<input type="button" value="${removeFileBrowser}">
		</div>
	</div>

	<div data-def="state=selection">
		<form class="attachment-upload-form">
		
		</form>	
	</div>

	<div data-def="state=uploading" class="attachment-upload-uploading centered">
	 	<h4 	class="attachment-progress-message centered"><f:message key="title.UploadingPleaseWait" /></h4>
 		<div 	class="attachment-progressbar"></div>
 		<span 	class="attachment-progress-percentage"></span>
 	</div>
	
	<div data-def="state=summary" class="attachment-upload-summary display-table">
	
	</div>
	
	<div data-def="state=error" >
		<span class="attachment-upload-error-message">
			<f:message key="message.AttachmentUploadSizeExceeded"/>
		</span>
	</div>

	<div class="popup-dialog-buttonpane">
		<input type="button" value="${attachSubmit}" data-def="evt=submit, state=selection, mainbtn=selection"/>
		<input type="button" value="${cancelLabel}" data-def="evt=cancel, state=selection uploading, mainbtn=selection uploading"/>
		<input type="button" value="${okLabel}" data-def="evt=done, state=summary, mainbtn=summary"/>
	</div>

</div>

<div id="dump" class="not-displayed"></div>
 

 