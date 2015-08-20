/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
/*
 * Returns the workspace-independant part of the configuration of a "workspace" tree.
 * 
 * 
 * conf : {
 *  model : model object for that tree
 * }
 * 
 */
define(function(){
	
	var baseURL = squashtm.app.contextRoot;
	
	return {
		generate : function(settings){
	
			var workspace = settings.workspace;
			var treepref = localStorage[workspace+"-tree-pref"];
			
			
			var cfg = { 
				"plugins" : ["json_data", "ui", "types", "hotkeys", "dnd", "cookies", "themes", "squash", "workspace_tree", 'conditionalselect' ],
				
				"json_data" : { 
					"data" : settings.model, 
					"ajax" : {
						"url": function (node) {
							return node.treeNode().getContentUrl();
						} 
					}
				},
				
				"core" : { 
					"animation" : 0
				},
				
				"ui": {
					"disable_selecting_children" : true,
					"select_multiple_modifier" : "ctrl",
					"select_range_modifier" : "shift",
					"select_prev_on_delete" : false
				},
				
				"hotkeys" : {
					"del" : function(){
								this.get_container().trigger('suppr.squashtree');
							},
					"f2" : function(){
								this.get_container().trigger('rename.squashtree');
							},
					"ctrl+c" : function(){
								this.get_container().trigger('copy.squashtree');
							},
					"ctrl+v" : function(){
								this.get_container().trigger('paste.squashtree');
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
					"url" : baseURL+"/styles/squash.tree.css"
				},
				
				"squash" : {
					rootUrl : baseURL,
					opened : (!!settings.selectedNode) ? [settings.selectedNode] : []
				},
				conditionalselect : function(node) {
					if($(node).attr("milestones-dont-allow-click")){
						return false ;
					}
					return true;
				}
				
			};
			
			
			if(treepref != 1) {
				cfg.plugins.push("sort"); 
			}
			
			return cfg;
		}
	};
});