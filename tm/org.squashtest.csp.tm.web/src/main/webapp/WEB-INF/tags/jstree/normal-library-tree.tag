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
<%@ attribute name="newLeafLabelKey" required="true" description="Name of the message to be used as value of the 'new leaf' button" %>
<%@ attribute name="libraryNodeTitleKey" required="true" description="Key of the root node label" %>
<%@ attribute name="libraryUrl" required="true" description="URL of the library" %>
<%@ attribute name="rootModel" type="java.lang.Object" %>
<%@ attribute name="workspace" description="workspace we are in" %>
<%@ attribute name="selectedNode" type="java.lang.Object" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>

<c:set var="resourceUrlRoot" value="${ pageContext.servletContext.contextPath }" />
<tree:jstree-tree_element_menu newLeafButtonMessage="${ newLeafLabelKey }" workspace="${ workspace }"/>
<f:message var="libraryName" key="${ libraryNodeTitleKey }" />

<script type="text/javascript">
	

	function selectLibrary(node) {		
		var selResourceUrl = nodeResourceUrl('${ resourceUrlRoot }',  node);
		var selNodeContentUrl = nodeContentUrl('${ libraryUrl }', node);
		var selResourceId = node.attr('id');
	
		setTreeData(selResourceUrl, selNodeContentUrl, selResourceId);
		loadContextualContentIfRequired(node, selResourceUrl);
	}
	function selectFolder(node) {	
		
		var selResourceUrl = nodeResourceUrl('${ resourceUrlRoot }',  node);
		var selNodeContentUrl = nodeContentUrl('${ libraryUrl }', node);
		var selResourceId = node.attr('id');
		
		setTreeData(selResourceUrl, selNodeContentUrl, selResourceId);
		loadContextualContentIfRequired(node, selResourceUrl);
	}
	
	function selectFile(node) {		
		
		var selResourceUrl = nodeResourceUrl('${ resourceUrlRoot }',  node);
		var selNodeContentUrl = null;
		var selResourceId = node.attr('id');
		
		setTreeData(selResourceUrl, selNodeContentUrl, selResourceId);
		loadContextualContentIfRequired(node, selResourceUrl);
	}

	function setTreeData(selResourceUrl, selNodeContentUrl, selResourceId) {
		storeSelectedNodeUrls('tree', selResourceUrl, selNodeContentUrl, selResourceId);
	}

	function loadContextualContentIfRequired(node, selResourceUrl) {
		if(isCtrlClicked == true)return;
		
		clearContextualContent("#contextual-content");
		<jq:get-load urlExpression="selResourceUrl" targetSelector="#contextual-content" />;
		
	}
</script>

