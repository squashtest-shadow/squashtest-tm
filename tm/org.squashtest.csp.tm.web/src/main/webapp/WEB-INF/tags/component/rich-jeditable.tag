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
<%-- call rich-editable-init first --%>
<%@ tag language="java" pageEncoding="ISO-8859-1"%>

<%@ attribute name="targetUrl" required="true" %>
<%@ attribute name="componentId" required="true" %>
<%@ attribute name="submitCallback" required="false" %>
<%@ attribute name="rows" required="false" %>
<%@ attribute name="welcome" required="false" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:url var="ckeConfigUrl" value="/styles/ckeditor/ckeditor-config.js" />
<c:url var="ckeWelcomeMessageConfigUrl" value="/styles/ckeditor/welcome-message-ckeditor-config.js" />

<script type="text/javascript">

	$(function() {
		$( '#${componentId}' ).editable( '${ targetUrl }', {
			type: 'ckeditor',												//this input type isn't standard, refers to jquery.jeditable.ckeditor.js
			ckeditor : <c:choose> <c:when test="${ not empty welcome}"> { customConfig : '${ ckeWelcomeMessageConfigUrl }', language: '<f:message key="rich-edit.language.value" />' } </c:when>
			<c:otherwise> { customConfig : '${ ckeConfigUrl }', language: '<f:message key="rich-edit.language.value" />' } </c:otherwise> </c:choose>,				//same comment
			rows:10, 
			cols: 80,
			placeholder: '<f:message key="rich-edit.placeholder" />',
			submit: '<f:message key="rich-edit.button.ok.label" />',
			cancel: '<f:message key="rich-edit.button.cancel.label" />',
			onblur : function(){},											//this disable the onBlur handler, which would close the jeditable 
																			//when clicking in the rich editor (since it considers the click as 
																			//out of the editing zone)
			<c:if test="${ not empty submitCallback }" >callback : function(value, settings){${submitCallback}(value, settings);},</c:if>
			indicator : '<img src="${ pageContext.servletContext.contextPath }/images/indicator.gif" alt="processing..." />' 
			
		});
	})
	
</script>