/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define(['tree', './tc-treemenu', './popups/init-all', './init-actions', 'squash/squash.tree-page-resizer'], 
		function(tree, treemenu, popups, actions, resizer) {

	
	function initResizer(){
		var conf = {
			leftSelector : "#tree-panel-left",
			rightSelector : "#contextual-content"
		};
		resizer.init(conf);
	}
	
	function init(settings){
		initResizer();
		tree.initWorkspaceTree(settings.tree);
		treemenu.init();	
		popups.init();
		actions.init();
	}
	
	
	return {
		init : init
	};
	
});
