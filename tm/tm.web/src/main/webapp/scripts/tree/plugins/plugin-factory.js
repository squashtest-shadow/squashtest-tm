/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

//the 'tree-node' plugin will be automatically applied when required
define(['jquery', './jstree-dnd-override','./squash-plugin', './workspace-tree-plugin', './tree-picker-plugin' , './tree-node', 'jstree'], function($, applyDndOverride, applySquashPlugin, applyWorkspacePlugin, applyTreePickerPlugin){

	return {
		
		configure : function(type, settings){
			switch(type){
			
			case 'workspace-tree' : 
				applyDndOverride(settings);
				applySquashPlugin();
				applyWorkspacePlugin();
				break;
				
			case 'tree-picker' : 
				applySquashPlugin();
				applyTreePickerPlugin();
				break;
				
			case 'simple-tree' : 
				applySquashPlugin();
				break;
				
			default :
				throw "'"+type+"' is not a valid tree profile";
			}
		}
		
	};
	
});