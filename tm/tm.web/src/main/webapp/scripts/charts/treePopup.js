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
define(["jquery", "backbone", "underscore", "handlebars", "tree", "jquery.squash.confirmdialog"],
	function ($, Backbone, _, Handlebars, tree) {
		"use strict";

	

		var View = Backbone.View.extend({
			el: "#tree-dialog",

			initialize: function (model) {
		
				this.model = model.model;
				this.nodes = model.nodes;
				this.render();
				
				$("#tree").on('reselect.jstree', function(event, data) {
		               data.inst.findNodes(model.nodes).select();
					});
				
				this.initTree(model.name);
				
				this.$el.confirmDialog({
					autoOpen: true
				});
		
			
			},

			render: function () {
				var treePopup = $("#tree-popup-tpl").html();
				this.treePopupTemplate = Handlebars.compile(treePopup);
				this.$el.append(this.treePopupTemplate());
				return this;
			},

			events: {
				"confirmdialogcancel": "cancel",
				"confirmdialogconfirm": "confirm"
			},

			cancel: function (event) {
				this.remove();

			},

			confirm: function (event) {
				var self = this;
				self.trigger("treePopup.confirm");
				this.remove();
			},
			
			

			initTree : function (workspaceName){
				

				var ids = _.pluck(this.model.get("scope"), "id");
				ids = ids.length > 0 ? ids : 0;
				
				
				
				$.ajax({
					url : squashtm.app.contextRoot + "/" + workspaceName + '-workspace/tree/' + ids,
					datatype : 'json' 
					
					
				}).done(function(model){
					
					var treeConfig = {
							model : model,
							treeselector: "#tree",
							workspace: workspaceName,	
							canSelectProject:true
					};
					tree.initLinkableTree(treeConfig);
				});

			},
			

			remove: function () {
				Backbone.View.prototype.remove.apply(this, arguments);
				$("#tree-dialog-container").html('<div id="tree-dialog" style="height: 200px!important" class="not-displayed popup-dialog search-minimal-height" th:title="#{report.form.tree-picker.dialog.title}" />');
			}
		});

		return View;
	});
