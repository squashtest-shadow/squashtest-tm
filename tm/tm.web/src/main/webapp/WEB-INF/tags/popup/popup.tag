<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2014 Henix, henix.fr

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
<%@ tag description="A dialog popup. The body of this tag contains the HTML body of the dialog. 
	Buttons and their bound actions are defined through a fragment passed as the 'buttons' attribute" body-content="empty" %>
<%@ attribute name="openedBy" description="id of the button which opens the dialog" %>
<%@ attribute name="id" required="true" description="id of the popup" %>
<%@ attribute name="titleKey" %>
<%@ attribute name="isContextual" required="false" description="if set, this popup will be added a class to show it belongs to the contextual panel"%>
<%@ attribute name="body" fragment="true" required="true" description="body of this dialog" %>
<%@ attribute name="buttons" fragment="true" required="false" description="buttons definitions of this dialog, defined as object" %>
<%@ attribute name="buttonsArray" fragment="true" required="false" description="buttons definitions of this dialog, defined as array. See jQuery documentation to understand the difference." %>
<%@ attribute name="closeOnSuccess" description="Closes the popup on ajax request success. Default is true." %>
<%@ attribute name="usesRichEdit" required="false"  type="java.lang.Boolean" description="a boolean telling whether textarea should be automagically turned to rich editors. Default is true." %>


<%@ attribute name="additionalSetup" required="false" fragment="true" 
	description="additional configuration at creation time. You must inline the attributes without brackets, 
	without comma before the first attribute, nor a comma after the last attribute.
	Except for handlers() that will stack on the default ones, you can safely redefine any attributes." %>


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"  %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<f:message var="cancelLabel" key="label.Cancel" />
<f:message var="ckeLang" key="rich-edit.language.value"/>
<f:message var="title" key="${ titleKey }"/>

<script type="text/javascript">
require(["common"], function() {
	require(["jquery", "jquery.squash"], function($, popup) {
		$(function() {
			var params = {
				selector : "#${id}",
				title : "${title}",
				<c:if test="${not empty openedBy}">
				openedBy : "#${openedBy}",
				</c:if>
				<c:if test="${not empty isContextual and isContextual}">
				isContextual : true,
				</c:if>
				<c:if test="${ not empty usesRichEdit and not usesRichEdit}">
				usesRichEdit : false,
				</c:if>
				<c:if test="${ not empty closeOnSuccess }">
				closeOnSuccess : ${closeOnSuccess},
				</c:if>
				ckeditor : {
					styleUrl : "${ckeConfigUrl}",
					lang : "${ckeLang}"
				},
				
				<%--  
					button settings using fragments. Buttons can also be defined as additionalSetup.
				 --%>
				<c:if test="${not empty buttons}">
				buttons: {
					<jsp:invoke fragment="buttons" />
				}
				</c:if>
				<c:if test="${not empty buttonsArray}">
				buttons : [
					<jsp:invoke fragment="buttonsArray" />           
				]
				</c:if>
				
				<c:if test="${not empty additionalSetup}">
				,
				<jsp:invoke fragment="additionalSetup"/>
				</c:if>
			}
			
			popup.create(params);
		});
	});
});
</script>


<div id="${ id }" class="popup-dialog not-displayed">
	<jsp:invoke fragment="body" />
</div>