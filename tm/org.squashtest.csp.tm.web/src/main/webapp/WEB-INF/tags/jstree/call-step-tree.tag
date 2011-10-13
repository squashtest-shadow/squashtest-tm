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


<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="jq" tagdir="/WEB-INF/tags/jquery" %>
<%@ taglib prefix="su" uri="http://org.squashtest.csp/taglib/string-utils" %>
<%@ taglib prefix="json" uri="http://org.squashtest.csp/taglib/json" %>

<%@ attribute name="rootModel" required="true" type="java.lang.Object"  description="the json-formatted initial nodes loaded in the tree (typically, libraries)" %>

<c:set var="resourceUrlRoot" value="${ pageContext.servletContext.contextPath }" />
<c:set var="resourceName" value="test-case" />
<c:set var="libraryUrl" value="${ pageContext.servletContext.contextPath }/${ resourceName }-browser" />

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
		
		var selResourceUrl = nodeResourceUrl('${ resourceUrlRoot }',  node)+"?edit-mode=0";
		var selNodeContentUrl = null;
		var selResourceId = node.attr('id');
	
		setTreeData(selResourceUrl, selNodeContentUrl, selResourceId);
		loadContextualContentIfRequired(node, selResourceUrl);
	}

	function setTreeData(selResourceUrl, selNodeContentUrl, selResourceId) {
		storeSelectedNodeUrls('tree', selResourceUrl, selNodeContentUrl, selResourceId);
	}

	function loadContextualContentIfRequired(node, selResourceUrl) {
		clearContextualContent("#contextual-content");
		<jq:get-load urlExpression="selResourceUrl" targetSelector="#contextual-content" />;
	}		

	
	function liNode(node) {
		if ($(node).is("a")) {
			return $(node).parent();
		}	
		return $(node);
	}
	

	function getId(node) {
		var id = node.attr("resid");
		return id;
	}


	$(function () {
		var tree_icons = {
				drive_icon : "${ pageContext.servletContext.contextPath }/images/root.png",
				folder_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_Folder.png",
				file_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_${ su:hyphenedToCamelCase(resourceName) }.png",
				resource_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_Iteration.png"
				
		};

		$("#tree")
		.bind('click.jstree', function(event, data) {
			cancelMultipleClickEvent(event);
		})
		.bind("select_node.jstree", function(event, data){
			unselectNonSiblings(data.rslt.obj, $('#tree'));
			return true;
		})
		.bind("dblclick.jstree", function(event) {
			toggleEventTargetIfNode(event, $(this));
		})
		.jstree({ 
			"plugins" : ["json_data", "sort", "themes", "types", "cookies", "ui", "squash"],
			"json_data" : { 
				"data" : ${ json:serialize(rootModel) }, 
				"ajax" : {
					"url": function (node) {
						var nodeRel = node.attr("rel");
						var contentUrl;
						
						switch (nodeRel) {
						case "drive": 
							contentUrl = nodeContentUrl('${ libraryUrl }', node);
							break;
						case "folder":
							contentUrl = nodeContentUrl('${ libraryUrl }', node);
							break;
						}
						
						return contentUrl;
					} 
				}
			},
			"types" : {
				"max_depth" : -2, // unlimited without check
				"max_children" : -2, // unlimited w/o check
				"valid_children" : [ "drive" ],
				"types" : {
					
					"file" : {
						"valid_children" : "none",
						"select_node": function(node) {
							selectFile(liNode(node));
							return true;
						},
						"icon" : {
							"image" : tree_icons.file_icon
						}
					},

					"folder" : {
						"valid_children" : [ "file", "folder" ],
						"select_node": function(node, check, event) {
							selectFolder(liNode(node));
							return true;
						},
						"icon" : {
							"image" : tree_icons.folder_icon
						}
					},
					"core" : { 
						"animation" : 0
					}, 
					"ui" : {
						select_multiple_modifier: false
					},
					"drive" : {
						"valid_children" : [ "file", "folder" ],
						"icon" : {
							"image" : tree_icons.drive_icon
						}
					}
				}
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
	});
</script>		
	

<f:message var="libraryName" key="tree.node.test-case-library.title" />

<tree:_html-tree treeId="tree" />
		