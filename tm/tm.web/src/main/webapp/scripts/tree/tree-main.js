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
/*
 * settings : {
 * workspace : one of ['test-case', 'requirement', 'campaign']
 * treeselector : the tree selector,
 * model : the data model for that tree.
 * selectedNode : 
 * }
 */

define([ "jquery", 
         "./simple-tree-conf/conf-factory",
		"./workspace-tree-conf/conf-factory",
		"./tree-picker-conf/conf-factory", 
		"./plugins/plugin-factory",
		"workspace.contextual-content", 
		"jstree" ], function($, simpleConf, wkspConf,
		pickerConf, pluginsFactory, ctxtcontent) {

	squashtm = squashtm || {};
	squashtm.tree = squashtm.tree || undefined;

	return {
		initWorkspaceTree : function(settings) {
			pluginsFactory.configure("workspace-tree", settings);
			var conf = wkspConf.generate(settings);
			var treeDiv = $(settings.treeselector);
			//trick for [Issue 2886]
			treeDiv.bind("loaded.jstree", function(event, data){
					treeDiv.jstree("save_cookie", "open_node");
				});
			var instance = treeDiv.jstree(conf);
			squashtm.tree = instance;
		},

		initLinkableTree : function(settings) {
			pluginsFactory.configure("tree-picker");
			var conf = pickerConf.generate(settings);
			var instance = $(settings.treeselector).jstree(conf);
			squashtm.tree = instance;
		},

		initSimpleTree : function(settings) {
			pluginsFactory.configure("simple-tree");
			var conf = simpleConf.generate(settings);
			var instance = $(settings.treeselector).jstree(conf);
			squashtm.tree = instance;
		},

		initCallStepTree : function(settings) {
			pluginsFactory.configure("simple-tree");
			var conf = simpleConf.generate(settings);
			var instance = $(settings.treeselector).jstree(conf);

			instance.on("select_node.jstree", function(event, data) {
				var resourceUrl = $(data.rslt.obj).treeNode().getResourceUrl();
				ctxtcontent.loadWith(resourceUrl);
				return true;
			});

			squashtm.tree = instance;
		},

		get : function() {
			return squashtm.tree;
		}
	};

});
