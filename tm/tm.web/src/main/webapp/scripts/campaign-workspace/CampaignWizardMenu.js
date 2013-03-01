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
 * This is a template for a backbone module
 */
define([ "jquery", "backbone", "handlebars", "jqueryui", "jquery.squash.squashbutton", "jquery.squash.jstree" ], function($, Backbone, Handlebars) {
	var View = Backbone.View.extend({
		el: "#wizard-tree-pane",
		initialize: function() {
			var enabled = this.collection && (this.collection.length > 0);
			this.menu = this.$("#wizard-tree-button").squashButton({ disabled: !enabled });
			
			this.render();
		}, 
		render: function() {
			if (this.collection.length > 0) {
				var source   = this.$("#wizard-tree-menu-template").html();
				var template = Handlebars.compile(source);
				
				var options = {
						html: template({ wizards: this.collection }), 
						params: {}
				};
				
				for (var wiz in this.collection) {
					options.params[wiz.name] = wiz.name;
				}
				
				this.menu.treeMenu(options);				
			}
			return this;
			
		},
		events: {
		}
	});
	
	return View;
});