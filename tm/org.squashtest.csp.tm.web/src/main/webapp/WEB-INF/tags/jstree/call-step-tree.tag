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
		.bind("select_node.jstree", function(event, data){			
			var resourceUrl = $(data.rslt.obj).treeNode().getResourceUrl();
			squashtm.contextualContent.loadWith(resourceUrl);	
			
			return true;
		})
		.jstree({ 
			"plugins" : ["json_data", "sort", "themes", "types", "cookies", "ui", "squash"],
			"json_data" : { 
				"data" : ${ json:serialize(rootModel) }, 
				"ajax" : {
					"url": function (node) {
						return node.treeNode().getContentUrl();
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
				"animation" : 0
			}, 
			"ui" : {
				select_multiple_modifier: false
			},
			"themes" : {
				"theme" : "squashtest",
				"dots" : true,
				"icons" : true,
				"url" : "${ pageContext.servletContext.contextPath }/styles/squashtree.css"					
			},
			"squash" : {
				rootUrl : "${ pageContext.servletContext.contextPath }"
			}				
			
		});
	});
</script>		
	

<f:message var="libraryName" key="tree.node.test-case-library.title" />

<tree:_html-tree treeId="tree" />
		