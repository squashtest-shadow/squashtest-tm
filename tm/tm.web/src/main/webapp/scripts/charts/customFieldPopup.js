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
			el: "#cuf-popup",

            initialize: function (options) {
                this.model = options;
                this.render();
                this.$el.confirmDialog({
					autoOpen: true
				});
                var protoForEntityType = this.model.get("computedColumnsPrototypes");
                protoForEntityType = protoForEntityType[this.model.get("selectedCufEntity")];
                this.cufProtosforThisEntityType = _.filter(protoForEntityType,function(proto){
                    return proto && proto.columnType === "CUF";
                });
            },

            events: {
				"confirmdialogcancel": "cancel",
				"confirmdialogconfirm": "confirm",
				"confirmdialogvalidate" : "validate"
			},

            render: function(){
                var src = $("#cuf-popup-tpl").html();
                this.template = Handlebars.compile(src);
                this.$el.append(this.template(this.model));
                return this;
            },

            remove: function () {
				Backbone.View.prototype.remove.apply(this, arguments);
				$("#cuf-popup-container").html('<div id="cuf-popup" style="height: 200px!important" class="not-displayed popup-dialog" title="' + translator.get('report.form.tree-picker.dialog.title') + '" />');
			},

			cancel: function (event) {
				this.remove();
			},

			confirm: function (event) {
                var selectedCufAttributes = this.model.get("selectedCufAttributes") || [];
                var checkedCufAttributes = this.convertCufIdToPrototypeId();
                //removing all cuf proto id for this entity type before adding only checked one
                //so we can keep a clean list of ids if the popup is openened several times
                var cufIds = _.pluck(this.cufProtosforThisEntityType,"id");
                selectedCufAttributes = _.reject(selectedCufAttributes,function(id) {
                    return _.contains(cufIds,id);
                });
                if(checkedCufAttributes.length > 0){
                    selectedCufAttributes = selectedCufAttributes.concat(checkedCufAttributes);
                }

                this.model.set("selectedCufAttributes", selectedCufAttributes);
                this.trigger("cufPopup.confirm");
				this.remove();
			},

            convertCufIdToPrototypeId: function() {
                var self = this;
                var cufIds = this.getSelectedCheckbox();
                return _.map(cufIds, function(cufId) {
                    var cufPrototype = _.find(self.cufProtosforThisEntityType, function(proto) {
                        return proto.cufId === parseInt(cufId);
                    });
                    return cufPrototype.id;
                });
            },

            getSelectedCheckbox : function() {
                return $(".cuf-checkbox:checked").map(function(index,object){
                    return $(this).val();
                }).toArray();
            }
        
    });
        return View;
});