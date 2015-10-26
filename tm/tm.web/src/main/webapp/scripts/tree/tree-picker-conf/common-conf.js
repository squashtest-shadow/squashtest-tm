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
define(function() {

	var baseURL = squashtm.app.contextRoot;

	return {
		generate : function(settings) {

			return {

				plugins : [ "json_data", "sort", "themes", "types", "cookies", "ui", "squash", "treepicker",
						'conditionalselect' ],

				json_data : {
					ajax : {
						url : function(n) {
							var nodes = n.treeNode();
							if (nodes.canContainNodes()) {
								return nodes.getContentUrl();
							} else {
								return null;
							}
						}
					},
					data : settings.model
				},

				core : {
					animation : 0
				},

				ui : {
					select_multiple_modifier : "ctrl",
					select_range_modifier : "shift"
				},

				themes : {
					theme : "squashtest",
					dots : true,
					icons : true,
					url : squashtm.app.contextRoot + "/styles/squash.tree.css"
				},

				squash : {
					rootUrl : squashtm.app.contextRoot,
					opened : (!!settings.selectedNode) ? [ settings.selectedNode ] : []
				},
				conditionalselect : function(node) {
					
					if (settings.canSelectProject){
						return true;
					}
					
					if($(node).is("[rel='drive']") ){
						return false ;
					}
					return true;
				}
			};
		}
	};

});