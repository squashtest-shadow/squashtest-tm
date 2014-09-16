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
<%@ tag language="java" pageEncoding="utf-8"%>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

<%@ attribute name="url" required="true" description="the url where to post the new password" %>
<%@ attribute name="id"  required="false" description="(optional) identifier of the said popup" %>
<%@ attribute name="openerId" required="true" description="the name of the control that will open that popup" %>
<%@ attribute name="successCallback" required="false" description="if defined, that function will be called after
																	the popup is closed." %>
<c:choose>
	<c:when test="${not empty id}">
		<c:set var="popupId" value="${id}" />
	</c:when>
	<c:otherwise>
		<c:set var="popupId" value="password-change-popup"/>
	</c:otherwise>

</c:choose>

<script type="text/javascript">
require( ["common"], function(){
	require( ["jquery"], function($){

	$(function(){
		$("#${popupId}").bind( "dialogclose", cleanUp);
		$("#${popupId}").data('confirm-handler', submitPassword);
	});


	function submitPassword(){
		
		if (! validatePassword()) return;		
			
		var oldPassword= $("#oldPassword").val();
		var newPassword = $("#newPassword").val();
		
		$.ajax({
			url : "${url}",
			type : "POST",
			dataType : "json",
			data : { "oldPassword" : oldPassword, "newPassword" : newPassword } ,
			success : userPasswordSuccess
		});		
						
		
	}

	<f:message var="oldPassError" key="user.account.oldpass.error" />
	<f:message var="newPassError" key="user.account.newpass.error"/>
	<f:message var="confirmPassError" key="user.account.confirmpass.error"/>
	<f:message var="samePassError" key="user.account.newpass.differ.error"/>	
	
	<%-- we validate the passwords only. Note that validation also occurs server side. --%>
	function validatePassword(){
		//first, clear error messages
		$("#user-account-password-panel span.error-message").html('');
		
		//has the user attempted to change his password ?
		
		var oldPassOkay=true;
		var newPassOkay=true;
		var confirmPassOkay=true;
		var samePassesOkay=true;
		

		if (! isFilled("#oldPassword")){
			$("span.error-message.oldPassword-error").html("${oldPassError}");
			oldPassOkay=false;
		}
		
		if (! isFilled("#newPassword")){
			$("span.error-message.newPassword-error").html("${newPassError}");
			newPassOkay=false;
		}

		if (! isFilled("#user-account-confirmpass")){
			$("span.error-message.user-account-confirmpass-error").html("${confirmPassError}");
			confirmPassOkay=false;
		}				
		
		if ((newPassOkay==true) && (confirmPassOkay==true)){
			var pass = $("#newPassword").val();
			var confirm = $("#user-account-confirmpass").val();
			
			if ( pass != confirm){
				$("span.error-message.newPassword-error").html("${samePassError}");
				samePassesOkay=false;
			}
		}

		
		return ( (oldPassOkay) && (newPassOkay) && (confirmPassOkay) &&(samePassesOkay) );
		
	}
	
	
	<%-- returns wether the field was filled or not --%>
	function isFilled(selector){
		var value = $(selector).val();
		if (value.length==0){
			return false;
		}else{
			return true;
		}
		
	}

	
	function hasPasswdChanged(){
		return (
			   (isFilled("#oldPassword"))
			|| (isFilled("#newPassword"))
			|| (isFilled("#user-account-confirmpass"))
		);
	}
	


	function userPasswordSuccess(){
		$("#${popupId}").dialog('close');
		<c:if test="${not empty successCallback}">
			${successCallback}();
		</c:if>
	}
	
	function cleanUp(){
		$("#oldPassword").val('');
		$("#newPassword").val('');
		$("#user-account-confirmpass").val('');	
		
	}
	});
});
</script>



<pop:popup  id="${popupId}" openedBy="${openerId}" closeOnSuccess="false" titleKey="user.account.password.label" isContextual="true" >

 	<jsp:attribute name="buttons"> 	
		<f:message var="label" key="label.Confirm" />
		'${ label }': function() {
				var handler = $($("#${popupId}")).data('confirm-handler');
				handler.call(this);	
		},			
		<pop:cancel-button />
 	</jsp:attribute> 

	<jsp:attribute name="additionalSetup">
		width: 420,
	</jsp:attribute> 	
 	
 	<jsp:attribute name="body"> 
		<div id="user-account-password-panel">
			
			<div >
				<label><f:message key="user.account.oldpass.label"/></label>
				<input type="password" id="oldPassword"/>
				<comp:error-message forField="oldPassword" />
			</div>
		
			<div>
				<label ><f:message key="user.account.newpass.label"/></label>
				<input type="password" id="newPassword"/>
				<comp:error-message forField="newPassword" />
			</div>
			
			<div>
				<label ><f:message key="user.account.confirmpass.label"/></label>
				<input type="password" id="user-account-confirmpass"/>
				<comp:error-message forField="user-account-confirmpass" />
			</div>			
			
		</div>
		
		<%-- the next comp:error is currently unused, however that might change later --%>
		<comp:error-message forField="user-account-changepass-status"/>
 		
 	</jsp:attribute>


</pop:popup>



