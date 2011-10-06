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
<%@ tag description="Popup allowing to upload files some files" body-content="empty" %>
	
<%@ attribute name="submitCallback" description="provides a callback for actions after submission"%>
<%@ attribute name="url" required="true" description="url to upload to"%>
<%@ attribute name="paramName" required="true" description="how the post parameter should be named with."%>
<%@ attribute name="openedBy" required="true" description="button opening the popup"%>


<%@ tag language="java" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
									
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.form.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/jquery/jquery.generateId.js"></script>
<script type="text/javascript" src="${ pageContext.servletContext.contextPath }/scripts/squashtest/add-attachment-popup.js"></script>

<%-- 
	that file groups three popup together. 
	 - file selection, 
	 - progres bar,
	 - summary
 --%>

<f:message var="uploadErrorMessage" key="dialog.attachment.summary.error" />
<f:message var="megaByteLabel" key="squashtm.megabyte.label" />			

			

<script type="text/javascript">

	<%--
	 * This section handles the transition between phase 1 and phase 2, ie all the operations between 
	 * the moment the user presses "submit" and the end of the download :
     *   - prelude to the upload : we'll need a 'ticket' for later use (see below),
	 *   - close the uploader popup, open the progress bar,
	 *   - start the uploading request (still stuck to HttpRequests, no XHR for this one)
	 *   - during the upload, poll the server for the upload using the ticket obtained in step 1
 	 *   - once it's done it will open the uploadSummary popup 
	 *
	 *  
	 * Notes : 
	 * 		- tickets are like sEcho for datatable : an identifier used by the server to know which upload to watch for.	 
	 * 		- we need the instruction flow to run as if the Ajax calls were synchronous. Since they aren't, we put the next steps of the 
	 *      code in the ajax success handlers.
	 --%>

	
	<%--
	 * This is the entry point : the user just clicked the "submit" button.
	 * 
	 * first let's warn the server we're going for an upload and request an upload ticket
	 * the server returns a string containing the number : that's the ticket.
	 *
	 --%>
	function setupAndBeginUpload(){
		$.post("${url}", function(data){ beginUpload(data)} );
	}

	
	<%-- once we got a ticket the real thing begins --%>
	function beginUpload(ticket){
		$("#attachment-progress-dialog").data("ticket",ticket);
		attachmentDisplayBar();
		attachmentSubmit(ticket);
	}


	<%--
	* the #attachment-progress-dialog dialog opening function is overloaded. See the javascript snippet at the bottom to see how.
	* It will mainly handle the logic regarging the progressbar and polling the server.
	--%>
	function attachmentDisplayBar(){
		$("#attachment-upload-dialog").dialog("close");
		$("#attachment-progress-dialog").dialog("open");
	}
	
	
	<%-- 
		.ajaxSubmit({}) will not treat server response status, will not fire error handlers, and returns a fake xhr object.
		It wont accept specific response data type so we must parse html, strip the tags and parse json ourselves.
		
		The only solution to process the response is to analyse the content to infer the status.	
	--%>
	function attachmentSubmit(ticket){
		$("#add-attachment-form").ajaxSubmit({
			url : "${url}?upload-ticket="+ticket,
			dataType : "text",
			success : function(){},
			error : function(){},
			complete : function(jqXHR){attachmentSubmitComplete(ticket, jqXHR);},
			target : '#dump'
		});	
	}
	
	function formatToMegabyte(lMaxSize){
		var mb = lMaxSize / 1048576;
		var strMb = mb.toString();
		var dotOffset = strMb.indexOf(".");
		
		if (dotOffset != -1){
			return strMb.substr(0, dotOffset+3);
		}else{
			return strMb;
		}
	}
	
	
	function unwrapJson(strText){
		var open = strText.indexOf("{");
		var close = strText.indexOf("}");
		
		return strText.substring(open, close+1);
		
	}

	<%-- see #attachmentSubmit for details regarding error handling --%>
	function attachmentSubmitComplete(ticket, jqXHR){

		$("#attachment-progress-dialog").dialog("close");
		
		<%-- 
		because some browsers find it clever to wrap the raw response inside html tags (no, it's not IE for once) 
		we need to 'unwrap' our nested json response.
		
		in our case, if the json response has an attribute maxSize, then we got an error.
		 --%>
		
		var text = jqXHR.responseText;		
		var refined = unwrapJson(text);
		var json = $.parseJSON(refined);
		
		if (json.maxSize === undefined){
			openUploadSummary(ticket);			
		}else{
			var maxSize = json.maxSize;		
			var message = "${uploadErrorMessage} (" + formatToMegabyte(maxSize) + " ${megaByteLabel})";
			
			displayInformationNotification(message);
			
			exitUpload();
		}
		
		
	}


	
</script>


<%-- 
	third section : the final popup giving a summary of the whole operation.
	
 --%>
<script type="text/javascript">

	//todo : init the popup with json that'll be loaded using the finalizeUpload on the controller
	
	function openUploadSummary(ticket){
		resetUploadSummary();
		
		$.ajax({
			url : "${url}?upload-ticket="+ticket,
			type : "DELETE",
			dataType : "json",
			success : populateAndOpenSummary
		});
		
		
	}
	
	function resetUploadSummary(){
		$("#attachment-upload-summary-body").html('');			
	}
	

	
	function populateAndOpenSummary(json){
		
		if (json!=null){
			if (! allTransferSuccessful(json)){
				populateSummary(json);			
				$("#attachment-upload-summary").dialog("open");
			}
			else{
				exitUpload();
			}
		}
	}

	
	function allTransferSuccessful(json){
		var summaries = json[0];
		var i=0;
		var allSuccess=true;
		for (i=0;i<summaries.length;i++){
			if (summaries[i].iStatus!=0){
				allSuccess=false;
				break;
			}
		}
		return allSuccess;		
	}

	
	function populateSummary(json){
		
		var summaries = json[0];
		var i=0;
		
		for (i=0;i<summaries.length;i++){
			$("#attachment-upload-summary-body").append(
					"<div class=\"display-table-row\" >"+
					"<div class=\"display-table-cell\" >"+
					"<label style=\"font-weight:bold;\">"+summaries[i].name+"</label>"+
					"</div>"+
					"<div class=\"display-table-cell\" >"+
					"<span>"+summaries[i].status+"</span>"+
					"</div>"+
					"</div>"					
			);
		}
				
	}

	
	function exitUpload(){
		<c:if test="${not empty submitCallback}">
		${submitCallback}();
		</c:if>		
	}


</script>



<%--  depends heavily on the jquery.form.js plugin, do not forget to include it  --%>
<pop:popup id="attachment-upload-dialog" openedBy="${openedBy}" 
 				isContextual="true" titleKey="dialog.attachment.upload.title">
 	<jsp:attribute name="buttons"> 		
 		<f:message var="attachSubmit" key="dialog.attachment.button.submit.label"/>
 		'${attachSubmit}' : function(){
 			setupAndBeginUpload();
 		},
 		
 		<pop:cancel-button />
 	</jsp:attribute> 
 	
 	<jsp:attribute name="additionalSetup">
 		width : 435,
 		open : function(){
 			var formInstance = $("#attachment-upload-dialog").data("formInstance");
 			formInstance.clear();
 		}
 	</jsp:attribute>
 	<jsp:attribute name="body"> 
 
 			
 		<%-- templates for cloning --%>
 		<div id="add-attachments-templates" style="display:none;">
	 		<f:message var="removeFileBrowser" key="dialog.attachment.add.button.remove.label" />
	 		<div class="attachment-item">
		 		<input type="file" name="${paramName}[]" size="40" />
		 		<input type="button" value="${removeFileBrowser}">
	 		</div>
 		</div>

 		
 		<div id="add-attachment-list" style="height:300px; margin:10 10 10 10;overflow-y:auto">
			<form id="add-attachment-form" action="${url}" method="POST" enctype="multipart/form-data">
 				 				 			
 			</form>
 		</div> 	
 		<div id="dump" style="display:none;"></div>
 	</jsp:attribute>
 </pop:popup>
 
 <%-- additional setup for the file selection popup --%>
 <script type="text/javascript">
 	$(function(){
 		var itemTemplate = $("#add-attachments-templates  .attachment-item");
 		var formInstance = $("#add-attachment-form").uploadPopup(itemTemplate);
 		$("#attachment-upload-dialog").data("formInstance", formInstance);
 	});
 </script>
 
 

 <pop:popup id="attachment-progress-dialog" openedBy="aezfsdfsfze" isContextual="true" 
 titleKey="dialog.attachment.pleasewait.title" closeOnSuccess="false">
 	<jsp:attribute name="buttons">
		 <f:message var="cancelLabel" key="dialog.button.cancel.label"/>
		'${cancelLabel}': function() {
			$( this ).dialog( 'close' );
			window.location.reload();
		}		
 	</jsp:attribute>
 	
 	<jsp:attribute name="body"> 

 		 	
 		<div style="text-align:center;">
 		<f:message var="pleaseWaitMessage" key="dialog.attachment.pleasewait.title" />
 			<h4 id="attachment-progress-message" style="text-align:center;"></h4>
 			<div id="attachment-progressbar"></div>
 			<span id="attachment-progress-percentage"></span>
 		</div>
 	</jsp:attribute>
 	
 	
 </pop:popup>
 
 
 <%-- more initiatisation code for the progress bar popup --%>
 <script type="text/javascript">
  
  
 	//popup additional init
	$(function(){
		//make it a progressbar
		$("#attachment-progressbar").progressbar({ value: 0 });
		
		//overload the open event of this dialog : reset the bar and init the poll loop;
		$("#attachment-progress-dialog").bind("dialogopen", function(){
 			
			uploadUpdateBar(0);
 			uploadDisplayPercentage(0);
 			uploadDisplayMessage("${pleaseWaitMessage}");			
			
			uploadIntervalId = setInterval ( "pollUploadStatus()", 1000 );

		});
		
		//overload the closing handler
		$("#attachment-progress-dialog").bind("dialogclose", function(){
			clearInterval ( uploadIntervalId );
		});

	}); 
  
 	<%--
 	 * the section below handle the polling routine.
 	 * it will periodically call the server for the upload status, with the upload ticket as a reference.
 	--%>
 	
 	var uploadIntervalId;

 	function updateProgressStatus(data){
 		
 		if (data.percentage<0){
 			uploadDisplayMessage("Warning : upload progress statistics are invalid");
 		}
 		
 		else{ 		
 			uploadUpdateBar(data.percentage);
 			uploadDisplayPercentage(data.percentage);
 			
	 		if (data.percentage==100){
	 			<f:message var="uploadCompleted" key="dialog.attachment.progressbar.complete" />
	 			uploadDisplayMessage("${uploadCompleted}");
	 		}
 		}
 	}
 
 	function pollUploadStatus(){
 		var ticket = $("#attachment-progress-dialog").data("ticket");
 		url = "${url}?upload-ticket="+ticket;
 		$.get(
 				url,
 				function(data){
 					updateProgressStatus(data);
 					},
 				"json"
 		);
 	}
 	
 	function uploadUpdateBar(percentage){
 		$("#attachment-progressbar").progressbar( "option", "value", percentage );
 	}
 	
 	function uploadDisplayPercentage(percentage){
 		$("#attachment-progress-percentage").html(percentage.toString()+" &#37;");
 	}
 	
 	function uploadDisplayMessage(message){
 		$("#attachment-progress-message").html(message);
 	}

 </script>
 
 
 <%-- popup for the summary --%>
 <pop:popup id="attachment-upload-summary"  titleKey="dialog.attachment.summary.label"  openedBy="nothing" isContextual="true" closeOnSuccess="false">
 	 <jsp:attribute name="buttons">
		 <f:message var="okLabel" key="dialog.attachment.add.button.acknowledge.label"/>
		'${okLabel}': function() {
			$(this).dialog('close');
		}		
 	</jsp:attribute>
 	
 	<jsp:attribute name="additionalSetup">
 		close : function(){
 			exitUpload();
 		}
 	</jsp:attribute>

 	
 	<jsp:attribute name="body">
 		
 		<div id="attachment-upload-summary-body" class="display-table">
 		
 		</div>
 		
 	
 	</jsp:attribute>
 
 </pop:popup>
 