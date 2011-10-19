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


<layout:tree-page-layout titleKey="squashtm" highlightedWorkspace="${ resourceName }" linkable="${linkable}">
	<jsp:attribute name="head">
		<comp:rich-jeditable-header />
		<script type="text/javascript">

			
			<c:if test="${ resourceName == 'requirement' }">
			function checkCrossProjectSelection(selectedNodes) {
				if ($(selectedNodes[0]).attr('rel') == "drive" ) {
					for (var num = 0; num < selectedNodes.length ; num++){
						if ($(selectedNodes[num]).attr('rel') != "drive" ){
							alert('<f:message key="dialog.cross-project.error.label"/>');
							return true;
						}
					}
				}
				else if ($(selectedNodes[0]).attr('rel') != "drive") {
					for (var num = 0; num < selectedNodes.length ; num++){
						if ($(selectedNodes[num]).attr('rel') == "drive" ){
							alert('<f:message key="dialog.cross-project.error.label"/>');
							return true;
						}
					}
				}
				return false;
			}
			</c:if>
	
			
			function renameSelectedNreeNode(name){
				var node = $('#tree').jstree("get_selected");
				$('#tree').jstree("set_text",node,name); //we don't want rename-node() since we use our own renaming interface
			}
			//update the selected node name attribute
			function updateSelectedNodeName(newName){
				var node = $('#tree').jstree("get_selected");
				node.attr('name', newName);
			}

	
			//success handler for a renaming operation
			function rename_from_tree_sucess(data) {
				//change the node name attribute
				updateSelectedNodeName(data.newName);
				//get the node
				var node = $('#tree').jstree("get_selected");
				//check if there's a prefix to the name
				var checkedName = getRealNodeName(data.newName, node);
				//performed if the node is displayed  in the contextual content
				updateFragmentPageData(data.newName, checkedName);
				
				//rename the node in the tree
				renameSelectedNreeNode(checkedName);

				//close the pop-up
				$('#rename-node-dialog' ).dialog( 'close' );
			}
			
	
			
			function rename_from_tree_failed(xhr){
				$('#rename-node-dialog .popup-label-error')
				.html(xhr.statusText);								
			}

			
			<c:choose>
				<c:when test="${ resourceName eq 'campaign' }">
					function getRealNodeName(name, node){
						var toReturn = name;
						//check if there's an index
						if(node.attr('iterationIndex') != null){
							toReturn = node.attr('iterationIndex') + " - " + name;
						}
						return toReturn;
					}
					//external calls ie fragment js functions
					function updateFragmentPageData(rawName, completeName){
						//declare a hook to rename the label
						if (typeof nodeSetname == 'function'){
							nodeSetname(rawName);
						}
					}
					//only for campaign : get the iteration index
					function getSelectedNodeIndex(){
						return $('#tree').jstree("get_selected").attr('iterationIndex');
					}
				</c:when>
				<c:when test="${ resourceName eq 'requirement' }">
					function getRealNodeName(name, node){
						var toReturn = name;
						//check if there's a reference
						if(node.attr('reference') != null){
							toReturn = node.attr('reference') + " - " + name;
						}
						return toReturn;
					}
					function updateFragmentPageData(rawName, completeName){
						if (typeof nodeSetname == 'function'){
							nodeSetname(completeName);
						}
						if (typeof updateRawNameHiddenField == 'function'){
							updateRawNameHiddenField(rawName);
						}
					}
				</c:when>
				<c:otherwise>
					function getRealNodeName(name, node){
						return name;
					}
					function updateFragmentPageData(rawName, completeName){
						if (typeof nodeSetname == 'function'){
							nodeSetname(rawName);
						}
					}
				</c:otherwise>
			</c:choose>
		</script>		

		
		<jsp:invoke fragment="head" />
	</jsp:attribute>

	<jsp:attribute name="titlePane">
		<h2><f:message key="workspace.${ resourceName }.title" /></h2>	
	</jsp:attribute>

	<jsp:attribute name="tree">
		<c:set var="browserUrlRoot" value="${ pageContext.servletContext.contextPath }/${ resourceName }-browser" />
		<f:message var="libraryName" key="tree.node.${ resourceName }-library.title" />
		<tree:library-tree  workspace="${ resourceName }" libraryUrl="${ browserUrlRoot }" rootModel="${ rootModel }" selectedNode="${ selectedNode }" />
	</jsp:attribute>

	<jsp:attribute name="contextualContent">
		<%-- empty --%>
	</jsp:attribute>

	<jsp:attribute name="footer">
		<treepopup:add-nameable-node-dialog resourceName="folder" treeNodeButton="squashtm.treemenu.create.buttons['folderButton']"/>
		<treepopup:add-nameable-node-dialog resourceName="${ resourceName }"  treeNodeButton="squashtm.treemenu.create.buttons['fileButton']"/>
		<treepopup:rename-node-dialog treeSelector="#tree" successCallback="rename_from_tree_sucess" treeNodeButton="squashtm.treemenu.create.buttons['renameButton']"/>
		<treepopup:delete-node-dialog treeSelector="#tree" resourceName="${resourceName}" treeNodeButton="squashtm.treemenu.create.buttons['deleteButton']"/>

		<treepopup:copy-paste-node 	  treeSelector="#tree" resourceName="${resourceName}" errorMessageKey="tree.button.copy-node.error" 
										treeNodeButtonCopy="squashtm.treemenu.create.buttons['copyButton']" treeNodeButtonPaste="squashtm.treemenu.create.buttons['pasteButton']"/>
		 
		<c:if test="${ resourceName == 'requirement' }">		
			<treepopup:export-requirement-dialog treeSelector="#tree"  />			
		</c:if>

		<jsp:invoke fragment="footer" />		
	</jsp:attribute>
</layout:tree-page-layout>