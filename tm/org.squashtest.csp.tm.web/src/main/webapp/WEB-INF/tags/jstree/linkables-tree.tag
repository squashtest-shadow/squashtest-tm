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
<%@ attribute name="id" required="true" description="id of the tree component" %>
<%@ attribute name="rootModel" required="false" type="java.lang.Object" description="JSON serializable model of root of tree. 
	If not set, the tree won't be initialized until initLinkableTree(json) is explicitely called with a valid json argument" %>
<%@ attribute name="driveContentUrlHandler" required="true" description="name of js function which computes the url to get content of a drive node" %>
<%@ attribute name="folderContentUrlHandler" required="true" description="name of js function which computes the url to get content of a drive node" %>
<%@ attribute name="iconSet" required="false" description="if set, will override the default icons"%>


<%@ taglib prefix="json" uri="http://org.squashtest.csp/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>


<tree:_html-tree treeId="${ id }">
</tree:_html-tree> 
<script type="text/javascript">
	
	<c:if test="${not empty rootModel}">
	$(function(){
		var jsondata = ${ json:serialize(rootModel) };
		if (jsondata!=null){
			initLinkableTree(jsondata);
		}
	});
	</c:if>
	
	function initLinkableTree(jsonData) {
		
		//default icons
		var tree_icons = {
			drive_icon : "${ pageContext.servletContext.contextPath }/images/root.png",
			folder_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_Folder.png",
			file_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_Iteration.png",
			resource_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_Iteration.png"
			
		};
		
		//override files if iconSet is provided
		<c:if test="${not empty iconSet}">
			<c:choose>
				<c:when test="${iconSet=='testcase'}">
					tree_icons.file_icon="${ pageContext.servletContext.contextPath }/images/Icon_Tree_TestCase.png"
				</c:when>
				<c:when test="${iconSet=='requirement'}">
					tree_icons.file_icon="${ pageContext.servletContext.contextPath }/images/Icon_Tree_Requirement.png"
				</c:when>
				<c:when test="${iconSet=='campaign'}">
					tree_icons.file_icon="${ pageContext.servletContext.contextPath }/images/Icon_Tree_Campaign.png"
				</c:when>
			</c:choose>
		</c:if>
		
		$("#${ id }")
		.jstree({ 
				"plugins" : ["json_data", "sort", "themes", "types", "cookies", "ui", "squash", "treepicker"],
				"json_data" : { 
					"data" : jsonData, 
					"ajax" : {
						"url": function (node) {
							var nodeRel = node.attr("rel");
							var contentUrl;
							
							switch (nodeRel) {
							case "drive": 
								contentUrl = ${ driveContentUrlHandler }(node);
								break;
							case "folder":
								contentUrl = ${ folderContentUrlHandler }(node);
								break;
							case "file":
								break;
							}
							
							return contentUrl;
						}, 
						"data": { component : 'jstree' }
					}
				},
				"types" : {
					"max_depth" : -2, // unlimited without check
					"max_children" : -2, // unlimited w/o check
					"valid_children" : [ "drive" ],
					"types" : {
						"file" : {
							"valid_children" : "none",
							"icon" : {
								"image" : tree_icons.file_icon
							}
						},
						"folder" : {
							"valid_children" : [ "file", "folder" ],
							"icon" : {
								"image" : tree_icons.folder_icon
							}
						},
						"drive" : {
							"valid_children" : [ "file", "folder" ],
							"icon" : {
								"image" : tree_icons.drive_icon
							}
						}
					}
				},
				"core" : { 
					"initially_open" : [ "${ selectedNode.attr['id'] }" ], 
					"animation" : 0
				}, 
				"ui": {
					select_multiple_modifier: "on"
				},				
				"themes" : {
					"theme" : "squashtest",
					"dots" : true,
					"icons" : true,
					"url" : "${ pageContext.servletContext.contextPath }/styles/squashtree.css"					
				},
				"squash" : {
					
				}			
				
			});
	};
</script>
