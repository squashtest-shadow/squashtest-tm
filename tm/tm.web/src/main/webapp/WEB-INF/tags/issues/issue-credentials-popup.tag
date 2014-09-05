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
<%@ attribute name="url" required="true" description="the url where to post the credentials"%>
<%@ attribute name="divId" required="true" description="the name you wish the popup to have"%>
<%@ attribute name="successCallback" required="false" description="if set, that handler will be called on completion"%>
<%@ attribute name="failureCallback" required="false" description="if set, that handler will be called for abnormal terminations"%>
<%@ attribute name="bugTrackerId" %>

<%@ tag language="java" pageEncoding="utf-8"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib tagdir="/WEB-INF/tags/component" prefix="comp"%>


<pop:popup isContextual="true" closeOnSuccess="false" titleKey="dialog.issue.credentials.title" id="${divId}" openedBy="nothing">
	<jsp:attribute name="buttons" >
		<f:message var="loginlabel" key="label.LogIn" />
		'${ loginlabel }': function() {
			postIssueCredentials();
		},			
		<f:message var="abortlabel" key="label.Cancel"/>
		'${ abortlabel }': function() {
			$('#dialog-issue-login').val('');
			$('#dialog-issue-password').val('');
			$( this ).dialog( 'close' );
			<c:if test="${not empty failureCallback}">${failureCallback}();</c:if>
		}
	</jsp:attribute>
	
	<jsp:attribute name="additionalSetup">
	width : 300
	</jsp:attribute>
	
	<jsp:attribute name="body">
		<comp:error-message forField="bugtracker" />
		<div class="centered">
			<div class="display-table">
				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.issue.credentials.labels.username"/></label></div>
					<div class="display-table-cell"><input type="text" id="dialog-issue-login" /></div>
				</div>
				<div class="display-table-row">
					<div class="display-table-cell"><label><f:message key="dialog.issue.credentials.labels.password"/></label></div>
					<div class="display-table-cell"><input type="password"  id="dialog-issue-password"/></div>	
				</div>
			</div>
	
		</div>

	</jsp:attribute>

</pop:popup>

<script type="text/javascript">
	function postIssueCredentialsSuccess(){
		$("#${divId}").dialog('close');
		<c:if test="${not empty successCallback}">${successCallback}();</c:if>		
	}

	function postIssueCredentials(){
		var login = $("#dialog-issue-login").val();
		var password = $("#dialog-issue-password").val();
		$.post("${url}", {"login" : login, "password" : password, "bugTrackerId" : ${bugTrackerId}}, postIssueCredentialsSuccess,"json");
	}

</script>