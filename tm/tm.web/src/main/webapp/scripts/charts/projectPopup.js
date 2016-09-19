/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define(["jquery", "backbone", "underscore", "handlebars", "tree", "squash.translator", "jquery.squash.confirmdialog"],
	function ($, Backbone, _, Handlebars, tree, translator) {
		"use strict";
        var View = Backbone.View.extend({
			el: "#project-perimeter-popup",

            initialize: function (options) {
                this.model = options.model;
                this.render();
                this.$el.confirmDialog({
					autoOpen: true
				});
                this.reloadProjectScope();
            },

            events: {
				"confirmdialogcancel": "cancel",
				"confirmdialogconfirm": "confirm"
			},

            render: function(){
                var src = $("#project-popup-tpl").html();
                this.template = Handlebars.compile(src);
                this.$el.append(this.template(this.model));
                return this;
            },

            remove: function () {
				Backbone.View.prototype.remove.apply(this, arguments);
				$("#project-perimeter-popup-container").html('<div id="project-perimeter-popup" style="height: 200px!important" class="not-displayed popup-dialog" title="' + translator.get('report.form.tree-picker.dialog.title') + '" />');
			},

			cancel: function (event) {
				this.remove();
			},

			confirm: function (event) {
                var selectedIds = $(".project-perimeter-checkbox:checked").map(function(index,object){
                    return $(this).val();
                }).toArray();
                this.model.set("scope", _.map(selectedIds,function(id){
                    return {id:id,type:"PROJECT"};
                }));
                 this.model.set("projectsScope", _.map(selectedIds,function(id){
                    return parseInt(id);
                }));
				this.remove();
			},

            reloadProjectScope: function () {
                var scope = this.model.get("scope");
                var self = this;
                var checkboxIds = _.chain(scope)
                                    .filter(function (entityRef) {
                                        return entityRef.type === "PROJECT";})
                                    .map(function (entityRef) {
                                        return "#project-perimeter-checkbox-" + entityRef.id;
                                    })
                                    .value();

                _.each(checkboxIds,function (id) {
                    self.$el.find(id).attr("checked","checked");
                });
            }
        
    });
        return View;
});