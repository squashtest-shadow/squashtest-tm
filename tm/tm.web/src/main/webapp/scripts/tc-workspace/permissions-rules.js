/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

define(['jquery', 'tree-node-copier'], function($, nodecopier){


	
	return {
		buttonrules : {
			'new-folder-tree-button' : this.canCreateFolder,
			'new-test-case-button' : this.canCreateTestCase,
			'copy-node-tree-button' : this.canCopy,
			'paste-node-tree-button' : this.canPaste,
			'rename-node-tree-button' : this.canRename,
			'import-excel-tree-button' : this.canImport,
			'import-links-excel-tree-button' : this.canImport,
			'export-tree-button' : this.canExport
		},
		
		canCreateFolder : function(nodes){
			return nodes.filter([':creatable']).filter(':folder, :library').length === 1;
		},
		
		canCreateTestCase : function(nodes){
			return nodes.filter([':creatable']).filter(':folder, :test-case').length === 1;
		},
		
		canCopy : function(nodes){
			return nodes.not(':library').length > 0;
		},
		
		canPaste : function(nodes){
			return (nodecopier.mayPaste() === "OK");
		},
		
		whyCantPaste : function(nodes){
			return nodecopier.mayPaste();
		},
		
		canRename : function(nodes){
			return nodes.filter(':editable').not(':library').length === 1;
		},
		
		canImport : function(nodes){
			return nodes.tree.data('importable');
		},
		
		canExport : function(nodes){
			return true;
		}
		
	}
	
	
});