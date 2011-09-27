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
<%@ tag body-content="empty"
	description="javascript handling the copy and paste of nodes in the tree"%>
	

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>	
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
	
<%@ attribute name="copySelector" description="jquery selector of the copy button" %>
<%@ attribute name="pasteSelector" description="jquery selector of the paste button" %>
<%@ attribute name="treeSelector" description="jquery selector of the tree instance" %>
<%@ attribute name="errorMessageKey" description="message key for error popups" %>
<%@ attribute name="resourceName" description="name of the resource being processed (test-case etc)" %>

<s:url var="copyUrl" value="/{workspace}-browser/copy">
	<s:param name="workspace" value="${resourceName}" />
</s:url>

<f:message var="errorMessage" key="${errorMessageKey}"/>

<script type="text/javascript" >
	
	var buttonBasedTreeNodeCopier;

	$(function(){
		buttonBasedTreeNodeCopier = new ButtonBasedTreeNodeCopier({copySelector : "${copySelector}",
																   pasteSelector : "${pasteSelector}",	
																   treeSelector : "${treeSelector}",
																   errMessage : "${errorMessage}",
																   url : "${copyUrl}"
																  });		
	});

</script>	
