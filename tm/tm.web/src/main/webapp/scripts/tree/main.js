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



/*
 * settings : {
 * 	  workspace : one of ['test-case', 'requirement', 'campaign']
 *    treeselector : the tree selector,
 *    model : the data model for that tree.
 * }
 */

define([ "./workspace-tree-conf/conf-factory", "./plugins/plugin-factory"], function(wkspConf, pluginsFactory) {

	squashtm = squashtm || {};
	squashtm.tree = squashtm.tree || {};
	
	
	function initWorkspaceTree(settings){
		pluginsFactory.configure('workspace-tree');
		var conf = wkspConf.generate(settings);
		var instance = $(settings.treeselector).jstree(conf);
		squashtm.tree = instance;
	}
	
	
	return {
		initWorkspaceTree : initWorkspaceTree,
		initLinkableTree : undefined,
		initCallTestCaseTree : undefined
	}
	
	
});
