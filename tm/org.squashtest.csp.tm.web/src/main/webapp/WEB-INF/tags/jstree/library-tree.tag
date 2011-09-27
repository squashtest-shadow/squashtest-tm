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
<%@ attribute name="libraryUrl" required="true" description="URL of the library" %>
<%@ attribute name="rootModel" type="java.lang.Object" %>
<%@ attribute name="selectedNode" type="java.lang.Object" %>
<%@ attribute name="workspace" required="false" description="will branch the configuation with respect to the workspace"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>

<c:set var="newLeafLabelKey" value="tree.button.new-${ workspace }.label"/>
<c:set var="libraryNodeTitleKey" value="tree.node.${ workspace }-library.title"/>

<%-- define the navigation functions --%>

<script type="text/javascript">

</script>


<c:choose>
	<c:when test="${workspace=='campaign'}">
		<tree:campaign-library-tree newLeafLabelKey="${newLeafLabelKey}" libraryUrl="${libraryUrl}" rootModel="${rootModel}" selectedNode="${selectedNode}" libraryNodeTitleKey="${libraryNodeTitleKey}" workspace="${ workspace }" />	
	
		<c:set var="folderUrlHandler" value="folderContentUrl"/>
		<c:set var="driveUrlHandler" value="rootNodeContentUrl"/>	
	
		<c:set var="browsable" value="true" />	
	
	</c:when>
	<c:otherwise>
		<tree:normal-library-tree newLeafLabelKey="${newLeafLabelKey}" libraryUrl="${libraryUrl}" rootModel="${rootModel}"  selectedNode="${selectedNode}" libraryNodeTitleKey="${libraryNodeTitleKey}" workspace="${ workspace }" />	
	
		<%-- needs to define stubs for fileContentUrl and selectResource so that the tag below wont fail --%>
		<script type="text/javascript">
			function fileContentUrl(){}
			function selectResource(){}
		</script>
		
		<c:set var="folderUrlHandler" value="nodeContentUrl"/>
		<c:set var="driveUrlHandler" value="rootNodeContentUrl"/>
	
		<c:set var="browsable" value="false" />	
	
	</c:otherwise>
</c:choose>

<tree:workspace-tree 
	id="tree" rootModel="${ rootModel }" browsableFiles="${browsable}"
	folderContentUrlHandler="${folderUrlHandler }" driveContentUrlHandler="${driveUrlHandler}" fileContentUrlHandler="fileContentUrl"
	selectDriveHandler="selectLibrary" selectFolderHandler="selectFolder" selectFileHandler="selectFile" selectResourceHandler="selectResource" workspaceType="${ workspace }"/>
