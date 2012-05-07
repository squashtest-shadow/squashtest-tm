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
<%@ tag body-content="empty" description="Add script which handles json content of ajax errors and populates error-message tagsaccordingly" %>
<%@ attribute name="cssClass" description="additional css classes" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div id="ajax-processing-indicator" class="ui-corner-all ${cssClass} " style="display:inline-block">
	<img src="${ pageContext.servletContext.contextPath }/images/ajax-loader.gif" width="19px" height="19px"/>
	<span><f:message key="squashtm.processing"/></span>
</div>
<div id="generic-error-notification-area" class="ui-state-error ui-corner-all ${ cssClass } not-displayed ">
	<span class="ui-icon ui-icon-alert icon"></span><span><f:message key="error.generic.label" />&nbsp;(<a href="#" id="show-generic-error-details"><f:message key="error.generic.button.details.label" /></a>)</span>
</div>


<script type="text/javascript">


$(function() {
	<%-- Does not work with narrowed down selectors. see http://bugs.jquery.com/ticket/6161 --%>
	$( document ).ajaxError(function(event, request, settings, ex) {
		//Check if we get an Unauthorized access response, then redirect to login page 
		if(401 == request.status){
			window.parent.location.reload();
		}else{
			
			try {
				handleJsonResponseError(request);
			} catch(e) {
				handleGenericResponseError(request);
			}
		}
	});

	$("#ajax-processing-indicator").hide();
	$(document).ajaxStart( function(){
		$("#ajax-processing-indicator").show().css('display', 'inline-block');
	})		
	.ajaxStop( function(){
		$("#ajax-processing-indicator").hide();
	});

	
});
function handleJsonResponseError(request) {
	<%-- this pukes an exception if not valid json. there's no other jQuery way to tell --%>
	var json = jQuery.parseJSON(request.responseText);
	if(json != null){
		if ( json.actionValidationError != null){
			return $.squash.openMessage("<f:message key='popup.title.error'/>",json.actionValidationError.message);		
		}
		else{
			if ( json.fieldValidationErrors != null) {
				<%-- IE8 requires it a low tech code --%>
				var validationErrorList = json.fieldValidationErrors;
				if (validationErrorList.length>0){
					var counter=0;
					for (counter=0;counter<validationErrorList.length;counter++){
						var fve = validationErrorList[counter];
						var labelId = fve.fieldName + '-' + 'error';
						
						var label = $('span.error-message.'+labelId);
					
						if (label.length != 0) {
							label.html(fve.errorMessage);
						}else{
							throw 'exception';
						}
					}
				}
			}else{
				throw 'exception';
			}
		}
	}
}

function handleGenericResponseError(request) {
	$( '#show-generic-error-details' ).unbind('click').click(function() {
		var popup = window.open('about:blank','error_details','resizable=yes, scrollbars=yes, status=no, menubar=no, toolbar=no, dialog=yes, location=no')
		popup.document.write(request.responseText);
	});
	
	$( '#generic-error-notification-area').fadeIn('slow').delay(20000).fadeOut('slow');
}

function displayInformationNotification(message){
	$.squash.openMessage("<f:message key='popup.title.info' />", message);
}

</script>


