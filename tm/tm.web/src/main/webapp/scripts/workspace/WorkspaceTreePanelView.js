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
/**
 * This is a template for a backbone module
 */
define(
		[ "jquery", "backbone", "handlebars", "workspace/WorkspaceWizardMenu",
				"jquery.squash.jstree" ],
		function($, Backbone, Handlebars, WorkspaceWizardMenu) {
			var View = Backbone.View
					.extend({
						el : "#tabbed-pane",

						initialize : function() {
							this.menu = new WorkspaceWizardMenu({
								collection : this.model.wizards
							});

							var tree = this.$("#tree");

							// apparently, jstree events dont bubble correctly,
							// backbone cant capture events
							// $().on changes this to emitter DOM -> proxy
							tree.on("select_node.jstree deselect_node.jstree deselect_all.jstree",
									$.proxy(this._onTreeSelectionChanged,this));

							// initialize menu state
							this.menu.refreshSelection(tree.jstree(
									"get_instance").get_selected());
						},

						events : {},

						_onTreeSelectionChanged : function(event, data) {
							this.menu
									.refreshSelection(data.inst.get_selected());
						}

					});

			return View;
		});