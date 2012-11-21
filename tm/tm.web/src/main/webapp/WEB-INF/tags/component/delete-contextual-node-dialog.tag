<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2012 Henix, henix.fr

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
<%@ tag body-content="empty" description="popup for item deletion in the contextual content. Made to remove a single item when displayed in the contextual content of the interface."%>

<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>

<%@ attribute name="openedBy" description="id of the widget that will open the popup"%>
<%@ attribute name="titleKey" description="resource key for the title of the popup." %>
<%@ attribute name="successCallback" description="javascript callback in case of success."%>
<%@ attribute name="simulationUrl" required="true" description="the url where to post a simulation of the deletion before actually performing it."%>
<%@ attribute name="confirmationUrl" required="true" description="the url where to post to confirm the deletion."%>
<%@ attribute name="itemId" required="true" description="the id of the item to be deleted"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
	<c:when test="${'delete-requirement-button' == openedBy}">
		<f:message var="deleteMessage" key="dialog.label.delete-nodes.requirement.label" />
	</c:when>
	<c:when test="${'delete-test-case-button' == openedBy}">
		<f:message var="deleteMessage" key="dialog.label.delete-nodes.test-case.label" />
	</c:when>
	<c:when test="${'delete-campaign-button' == openedBy}">
		<f:message var="deleteMessage" key="dialog.label.delete-nodes.campaign.label" />
	</c:when>
	<c:when test="${'delete-iteration-button' == openedBy}">
		<f:message var="deleteMessage" key="dialog.label.delete-nodes.iteration.label" />
	</c:when>
	<c:when test="${'delete-test-suite-button' == openedBy}">
		<f:message var="deleteMessage" key="dialog.label.delete-nodes.test-suite.label" />
	</c:when>
	<c:otherwise>
		<f:message var="deleteMessage" key="dialog.label.delete-nodes.label" />
	</c:otherwise>
</c:choose>

<f:message var='deleteMessageStart' key='dialog.label.delete-node.label.start'/>
<f:message var='deleteMessageCantBeUndone' key='dialog.label.delete-node.label.cantbeundone'/>
<f:message var='deleteMessageConfirm' key='dialog.label.delete-node.label.confirm'/>

<c:set var="servContext" value="${ pageContext.servletContext.contextPath }"/>

<%-- onLoad code --%>
<script type="text/javascript">

$(function(){
	$( "#delete-contextual-node-dialog_${openedBy}" ).bind( "dialogopen", function(event, ui) {
		
		var jqThis = $(this);
		
		sendContextualDeletionSimulationRequest(jqThis);
	
	});
});	
</script>

<%-- preamble --%>

<script type="text/javascript">

function sendContextualDeletionSimulationRequest(jqDialog){
	
	var url = "${simulationUrl}";	

	$.ajax({
		url :url,
		data :{"nodeIds[]": [${itemId}] },
		type : 'post',
		dataType : 'json'
	})			
	.success(function(data){
		//var message = data.message + "<b>${deleteMessage}</b>";
		//jqDialog.html(message);
		jqDialog.html("<table><tr><td><img src='${servContext}/images/messagebox_confirm.png'/></td><td><table><tr><td><span>${deleteMessageStart}<span class='warning-message'> <span class='red-warning-message'>${deleteMessage}</span> </span></span></td></tr><tr><td>${deleteMessageCantBeUndone}</td></tr><tr><td><span class='black-warning-message'>${deleteMessageConfirm}</span></td></tr></table></td></tr></table>");
		
	})
	.fail(function(){
		jqDialog.dialog("close"); <%-- the standard failure handler should kick in, no need for further treatment here. --%>
	});
				
}

</script>

<%-- confirmation code --%>
<script type="text/javascript">

function confirmDeletion(){
	
	var jqDialog = $( "#delete-contextual-node-dialog_${openedBy}" );
	var url = "${confirmationUrl}";

	$.ajax({
		url : url+"?nodeIds[]="+${itemId},
		dataType : "json",
		type : 'DELETE'
	})
	.success(function(list){			
		jqDialog.dialog("close");
		<c:if test="${not empty successCallback}">
		${successCallback}();
		</c:if>
	})
	.fail();
}
	
</script>



<comp:popup id="delete-contextual-node-dialog_${openedBy}"  titleKey="${titleKey}" closeOnSuccess="false" openedBy="${openedBy}" isContextual="true">
	<jsp:attribute name="buttons">
	
		<f:message var="label" key="tree.button.delete-node.label" />
	
			'${ label }': confirmDeletion,			
		<pop:cancel-button />
	</jsp:attribute>
	
	<jsp:body>
		
		<span id="delete-contextual-node-dialog-label"></span>
		<br />				
	</jsp:body>
</comp:popup>