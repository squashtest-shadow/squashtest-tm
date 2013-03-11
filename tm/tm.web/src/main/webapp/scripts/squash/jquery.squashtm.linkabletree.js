/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Creates preconfigured jstree which show "linkables" items. 
 * 
 * depends on : 
 * jquery
 * jquery ui
 * jquery.jstree.js
 * 
 * @author bsiri
 * @author Gregory Fouquet
 */
(function ($) {
	var defaultSettings = {
		contextPath: "", 
		jsonData: {}, 
		workspaceType: ""
	};
	
	function workspaceToIconToken(workspaceType) {
		var tokens = workspaceType.split("-");
		var capitalized = $.map(tokens, function (token) {
			return token.charAt(0).toUpperCase() + token.slice(1);
		});
		return capitalized.join("");
	}
	
	$.fn.extend({
		linkableTree: function (settings) {
			settings = $.extend(defaultSettings, settings);
			
			var contextPath = settings.contextPath;
			if (contextPath.charAt(contextPath.length -1) === "/") {
				settings.contextPath = contextPath.slice(0, -1);
			}
			
			var icons = {
				drive : settings.contextPath + "/images/root.png",
				folder : settings.contextPath + "/images/Icon_Tree_Folder.png",
				file : settings.contextPath + "/images/Icon_Tree_" + workspaceToIconToken(settings.workspaceType) + ".png",
				resource : settings.contextPath + "/images/Icon_Tree_Iteration.png",
				view : settings.contextPath + "/images/Icon_Tree_TestSuite.png" 
			};
			
			var themeUrl = settings.contextPath + "/styles/squashtree.css";
			
			var self = $(this);
			var tree = self.jstree({ 
				"plugins" : ["json_data", "sort", "themes", "types", "cookies", "ui", "squash", "treepicker"],
				"json_data" : { 
					"data" : settings.jsonData, 
					"ajax" : {
						"url": function (node) {
								if ((node.is(':library')) || (node.is(':folder'))) {
									return node.treeNode().getContentUrl();
								} else {
									return null;
								}
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
								"image" : icons.file
							}
						},
						"folder" : {
							"valid_children" : [ "file", "folder" ],
							"icon" : {
								"image" : icons.folder
							}
						},
						"drive" : {
							"valid_children" : [ "file", "folder" ],
							"icon" : {
								"image" : icons.drive
							}
						}
					}
				},
				"core" : { 
					"initially_open" : [ "${ selectedNode.attr['id'] }" ], 
					"animation" : 0
				}, 
				"ui": {
					select_multiple_modifier: "ctrl"
				},				
				"themes" : {
					"theme" : "squashtest",
					"dots" : true,
					"icons" : true,
					"url" : themeUrl				
				},
				"squash" : {
					rootUrl : settings.contextPath
				}			
				
			});
						
			return self;
		}
	});
})(jQuery);