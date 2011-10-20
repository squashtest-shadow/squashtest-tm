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
<%@ attribute name="rootModel" required="true" type="java.lang.Object" description="JSON serializable model of root of tree" %>
<%@ attribute name="driveContentUrlHandler" required="true" description="name of js function which computes the url to get content of a drive node" %>
<%@ attribute name="folderContentUrlHandler" required="true" description="name of js function which computes the url to get content of a drive node" %>
<%@ attribute name="fileContentUrlHandler" required="false" description="name of js function which computes the url to get content of a drive node" %>
<%@ attribute name="selectResourceHandler" required="false" %>
<%@ attribute name="selectFileHandler" required="true" %>
<%@ attribute name="selectFolderHandler" required="true" %>
<%@ attribute name="selectDriveHandler" required="true" %>
<%@ attribute name="browsableFiles" required="true" type="java.lang.Boolean" %>
<%@ attribute name="workspaceType" required="true" %>

<%@ taglib prefix="json" uri="http://org.squashtest.csp/taglib/json" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="su" uri="http://org.squashtest.csp/taglib/string-utils" %>
<%@ taglib prefix="s" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="tree" tagdir="/WEB-INF/tags/jstree" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsp/jstl/fmt"%>


<c:set var="browserUrlRoot" value="${ pageContext.servletContext.contextPath }/${ workspaceType }-browser" />

<s:url var="moveUrl" value="/${ workspaceType }-browser/move" />
<s:url var="copyUrl" value="/${ workspaceType }-browser/copy" />


<s:url var="deleteUrl" value="/${ workspaceType }s/">
</s:url>

<tree:_html-tree treeId="${ id }">
</tree:_html-tree>

<script type="text/javascript">
	
	//
	//prevent iteration copy, first step, check if ctrl was clicked. For the moment, could not find a simpler way...
	var isCtrlClicked = false;
	
	$(document).keydown(function(e){
		var keyCode = (window.event) ? e.which : e.keyCode;
		if(keyCode == '17'){
			isCtrlClicked = true;
		}
	});
	$(document).keyup(function(e){
		isCtrlClicked = false;
	});


	function liNode(node) {
		if ($(node).is("a")) {
			return $(node).parent();
		}	
		return $(node);
	}
	
	function isFolder(node){
		return node.is(":folder") ? 1 : 0;			
	}
	
	function isIteration(node){
		return node.is(":iteration") ? 1 : 0;			
	}
	
	function isRoot(node){
		return node.is(":library") ? 1 : 0;			
	}
	
	function getId(node) {
		var id = node.attr("resid");
		return id;
	}
	
	
	function getIds(tabNode, method) {
		var tabId = [];
		if (method == 1){
			tabNode.rslt.obj.each(function (i) {
				tabId[i] = $(this).attr("resid");
			});
		}
		if (method == 2){
			tabNode.rslt.o.each(function (i) {
				tabId[i] = $(this).attr("resid");
			});
		}
		if (method == 3){
			tabNode.each(function (i) {
				tabId[i] = $(this).attr("resid");
			});
		} 
		return tabId;
	}
	
	
	
	
	function updateTreebuttons(strOperations){
		for (var menu in squashtm.treemenu){
			for (var operation in squashtm.treemenu[menu].buttons){
				if (strOperations.match(operation)){
					squashtm.treemenu[menu].buttons[operation].enable();
				}
				else{
					squashtm.treemenu[menu].buttons[operation].disable();
				}
			}
		}
		
	}

	$(function () {
		var tree_icons = {
			drive_icon : "${ pageContext.servletContext.contextPath }/images/root.png",
			folder_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_Folder.png",
			file_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_${ su:hyphenedToCamelCase(workspaceType) }.png",
			resource_icon : "${ pageContext.servletContext.contextPath }/images/Icon_Tree_Iteration.png"
				
		};

		$("#${ id }")
		.bind("select_node.jstree", function(event, data){
			
			unselectNonSiblings(data.rslt.obj, $('#${id}'));
			operations = data.inst.allowedOperations();
			updateTreebuttons(operations);
			
			return true;
		})
		.bind("deselect_node.jstree", function(event, data){
			operations = data.inst.allowedOperations();
			updateTreebuttons(operations);
			return true;
		})		
		<%-- 
			the following should have been as a handler of before.jstree on call move_node.
			however many considerations lead to postprocess mode_node like now, rather than preprocess it. At least that event is triggered only once. 
		--%>
		.bind("move_node.jstree", function(event, data){			
			var moveObject = data.args[0];
			
			if (moveObject !==null 
				&& moveObject !== undefined 
				&& moveObject.cr !== undefined){
						
				if (isCtrlClicked){
					//we need to destroy the copies first, since we'll use our owns.
					destroyJTreeCopies(moveObject, data.inst);
					
					//now let's post.
					var newData = moveObjectToCopyData(data);
					copyNode(newData, "${copyUrl}")
					.fail(function(){
						data.inst.refresh();
					});
				}
				else{
					<f:message var="cannotMoveNodeLabel" key="squashtm.action.exception.cannotmovenode.label" />
					//check if we can move the object
					if(checkMoveIsAuthorized(data)){
						moveNode(data, "${moveUrl}")
						.fail(function(jqXHR){
							displayInformationNotification("${cannotMoveNodeLabel}");
							data.inst.refresh();
						});
					}
					else{
						displayInformationNotification("${cannotMoveNodeLabel}");
						data.inst.refresh();
					}
				}
			}			
		})	
		.jstree({ 
				<%-- cookie plugin should be defined after ui otherwise tree select state wont be restored --%>	
				"plugins" : ["json_data", "ui", "types", "sort", "crrm", "hotkeys", "dnd", "cookies", "themes", "squash" ], 			
				
				"json_data" : { 
					"data" : ${ json:serialize(rootModel) }, 
					"ajax" : {
						"url": function (node) {
							var nodeRel = node.attr("rel");
							var contentUrl;
							
							switch (nodeRel) {
							case "drive": 
								contentUrl = nodeContentUrl('${ browserUrlRoot }', node);
								break;
							case "folder":
								contentUrl = nodeContentUrl('${ browserUrlRoot }', node);
								break;
							case "file":
								<c:if test="${browsableFiles}">
								contentUrl = nodeContentUrl('${ browserUrlRoot }', node);
								</c:if>
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
					"start_drag" : false,
					"move_node" : true,
					"delete_node" : false,
					"remove" : false,
					"types" : {
						<c:if test="${ browsableFiles }">
						"resource" : {
							"valid_children" : "none",
							"select_node": function(node) {
								${ selectResourceHandler }(liNode(node));
								return true;
							},							
							"icon" : {
							"image" : tree_icons.resource_icon
							}
						},
						</c:if>
						<c:if test="${ not browsableFiles }">
						"file" : {
							"valid_children" : "none",
							"select_node": function(node) {
								${ selectFileHandler }(liNode(node));
								return true;
							},
							"icon" : {
								"image" : tree_icons.file_icon
							}
						},
						</c:if>
						<c:if test="${ browsableFiles }">
						"file" : {
							"valid_children" : [ "resource" ],
							"select_node": function(node) {
								${ selectFileHandler }(liNode(node));
								return true;
							},
							"icon" : {
								"image" : tree_icons.file_icon
							}
						},
						</c:if>
						"folder" : {
							"valid_children" : [ "file", "folder" ],
							"select_node": function(node, check, event) {
								${ selectFolderHandler }(liNode(node));
								return true;
							},
							"icon" : {
								"image" : tree_icons.folder_icon
							}
						},
						"drive" : {
							"valid_children" : [ "file", "folder" ],
							"select_node": function(node) {
								${ selectDriveHandler }(liNode(node));
								return true;
							},
							"icon" : {
								"image" : tree_icons.drive_icon
							}
						}
					}
				},
				
				"core" : { 
					<%-- do not uncomment otherwise tree is closed on page refresh --%>
					//"initially_open" : [ "${ selectedNode.attr['id'] }" ] 
					"animation" : 0
				},
				"crrm": {
					"move" : {
						"check_move" : treeCheckDnd
							
					} 
				}, 
				"dnd": {
					
	            	"drag_check" : function (data) {	            		
	                	return {
                    		after : true,
	                    	before : true,
	                    	inside : true
	                	};	                	
            		},
            		"drag_target" : false
				},
				
				"ui": {
					"disable_selecting_children" : true,
					"select_multiple_modifier" : "ctrl",
					"select_prev_on_delete" : false
				},
				
				"hotkeys" : {
					"del" : function(){
								<%-- requires that the delete-node-dialog popup exists somewhere. --%>
								$("#delete-node-dialog").dialog("open");
							},
					"f2" : function(){
								<%-- requires that the rename-node-dialog popup exists somewhere. --%>
								$('#rename-node-dialog').dialog('open');
							},
					"ctrl+c" : function(){
								<%-- requires that an instance of ButtonBasedTreeNodeCopier instance exists, see copy-paste-node.tag --%>
								if (squashtm.treemenu.treeNodeCopier){
									squashtm.treemenu.treeNodeCopier.copyNodesToCookie();
								}
							},
					"ctrl+v" : function(){
								if (squashtm.treemenu.treeNodeCopier){
									squashtm.treemenu.treeNodeCopier.pasteNodesFromCookie();
								}						
							},
							
					
					"up" : false, 
					"ctrl+up" : false, 
					"shift+up" : false, 
					"down" : false, 
					"ctrl+down" : false, 
					"shift+down" : false, 
					"left" : false, 
					"ctrl+left" : false, 
					"shift+left" : false, 
					"right" : false, 
					"ctrl+right" : false,
					"shift+right" : false, 
					"space" : false, 
					"ctrl+space" : false, 
					"shift+space" : false							
							
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
