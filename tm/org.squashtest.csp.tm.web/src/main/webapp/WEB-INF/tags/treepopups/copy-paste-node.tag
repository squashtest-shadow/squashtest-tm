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
	
<%@ attribute name="treeSelector" description="jquery selector of the tree instance" %>
<%@ attribute name="resourceName" description="name of the resource being processed (test-case etc)" %>
<%@ attribute name="treeNodeButtonCopy" required="true" description="the javascript button that will trigger a copy" %>
<%@ attribute name="treeNodeButtonPaste" required="true" description="the javascript button that will trigger the paste" %>

<s:url var="copyUrl" value="/{workspace}-browser/copy">
	<s:param name="workspace" value="${resourceName}" />
</s:url>
<s:url var="copyIterationUrl" value="/{workspace}-browser/copyIteration">
	<s:param name="workspace" value="${resourceName}" />
</s:url>

<f:message var="errorMessage" key="tree.button.copy-node.error"/>
<f:message var="pasteNotHere" key="tree.button.copy-node.error.pastenothere"/>
<f:message var="pasteIterationNotHere" key="tree.button.copy-node.error.pasteiterationnothere"/>
<f:message var="pasteNotSameProject" key="tree.button.copy-node.error.pastenotsameproject"/>
<f:message var="notOneEditable" key="tree.button.copy-node.error.notOneEditable"/>
<script type="text/javascript" >

	$(function(){
		squashtm.treemenu.treeNodeCopier = new TreeNodeCopier({treeSelector : "${treeSelector}",
											 errMessage : "${errorMessage}",
											 pasteNotSameProject : "${pasteNotSameProject}",											 pasteNotHere :"${pasteNotHere}",
											 pasteIterationNotHere :"${pasteIterationNotHere}",
											 pasteNotHere :"${pasteNotHere}",
											 notOneEditable :"${notOneEditable}",
											 url : "${copyUrl}",
											 urlIteration : "${copyIterationUrl}"
										});		
		
		${treeNodeButtonCopy}.click(function(){
			squashtm.treemenu.treeNodeCopier.copyNodesToCookie();
		});
		
		${treeNodeButtonPaste}.click(function(){
			squashtm.treemenu.treeNodeCopier.pasteNodesFromCookie();
		});		
	});

</script>	
