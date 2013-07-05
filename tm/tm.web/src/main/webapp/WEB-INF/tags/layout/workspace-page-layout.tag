<%--

        This file is part of the Squashtest platform.
        Copyright (C) 2010 - 2013 Henix, henix.fr

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
<%@ tag body-content="empty" description="layout for a workspace page"%>
<%@ attribute name="head" fragment="true"%>
<%@ attribute name="footer" fragment="true"%>
<%@ attribute name="resourceName" required="true"  description="hyphened resource name of this workspace. eg: test-case"%>
<%@ attribute name="linkable" description="this help to render or not render search params for the requirement workspace, empty otherwise" %>

<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery"%>
<%@ taglib prefix="comp" tagdir="/WEB-INF/tags/component"%>
<%@ taglib prefix="pop" tagdir="/WEB-INF/tags/popup" %>
<%@ taglib prefix="treepopup" tagdir="/WEB-INF/tags/treepopups" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="treepopup" tagdir="/WEB-INF/tags/treepopups" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<layout:tree-page-layout titleKey="squashtm" highlightedWorkspace="${ resourceName }" linkable="${linkable}">
	
	<jsp:attribute name="head">
		
		<comp:rich-jeditable-header />		
		
		<jsp:invoke fragment="head" />
		
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.${ resourceName }.title" /></h2>	
	</jsp:attribute>

	<jsp:attribute name="tree">
		<tree:workspace-tree  workspaceType="${ resourceName }" rootModel="${ rootModel }"  id="tree" />
	</jsp:attribute>

	<jsp:attribute name="contextualContent">
		<%-- empty --%>
	</jsp:attribute>

	<jsp:attribute name="footer">
		
		<%-- 
		<treepopup:add-nameable-node-dialog resourceName="folder" treeNodeButton="squashtm.treemenu.create.buttons['create-folder']"/>
		<treepopup:add-nameable-node-dialog resourceName="${ resourceName }"  treeNodeButton="squashtm.treemenu.create.buttons['create-file']"/>
	
		<treepopup:rename-node-dialog treeSelector="#tree" treeNodeButton="squashtm.treeButtons['rename']"/>
		<treepopup:delete-node-dialog treeSelector="#tree" resourceName="${resourceName}" treeNodeButton="squashtm.treeButtons['delete']"/>

		<treepopup:copy-paste-node treeSelector="#tree" resourceName="${resourceName}" 
										treeNodeButtonCopy="squashtm.treeButtons['copy']" treeNodeButtonPaste="squashtm.treeButtons['paste']"/>

		<c:if test="${ resourceName == 'test-case' || resourceName == 'requirement' }">
			<treepopup:export-dialog treeSelector="#tree"  treeNodeButton="squashtm.treemenu.importer.buttons['export']" resourceName="${resourceName}"/>	
			<sec:authorize access="hasRole('ROLE_TM_PROJECT_MANAGER') or hasRole('ROLE_ADMIN')">
				<treepopup:import-excel-dialog treeSelector="#tree" treeNodeButton="squashtm.treemenu.importer.buttons['import-excel']" workspace="${resourceName}"  targetLibraries="${editableLibraries}"/>
				<treepopup:import-req-tc-links-excel-dialog treeSelector="#tree" 
				treeNodeButton="squashtm.treemenu.importer.buttons['import-links-excel']" />
			</sec:authorize>
		</c:if>
		
		 --%>

		<jsp:invoke fragment="footer" />		
	</jsp:attribute>
</layout:tree-page-layout>