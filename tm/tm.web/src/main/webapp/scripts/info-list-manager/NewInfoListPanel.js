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
define(
		[ "jquery", "app/BindView", "handlebars", "backbone.validation", "./InfoListOptionPanel", "./InfoListOptionModel", "./InfoListOptionCollection", "app/lnf/Forms", "squashtable/squashtable.options", "squash.configmanager", "jquery.squash.confirmdialog" ],
		function InfoListPanel($, BindView, Handlebars, Validation, InfoListOptionPanel, InfoListOptionModel, InfoListOptionCollection, Forms, SquashTable, confman) {
			"use strict";

			var validationOptions = {
				valid : function(view, prop) {
					view.boundControl(prop).setState("success");
				},
				invalid : function(view, prop, err) {
					console.log(view, prop, err);
					// find something better when there is more not-bound props
					if (prop === "options") {
						Forms.input(view.$("#options-table")).setState("error", err);
					} else {
						view.boundControl(prop).setState("error", err);
					}
				}
			};

			function initOptionsTable(view) {
				var radio = SquashTable.renderer($("#default-cell-tpl").html())(function(data, type, row) {
					return { checked: data, option: row.code };
				});

				var remove = SquashTable.renderer($("#remove-cell-tpl").html())(function(data, type, row) {
					return { value: row.code, name: "option-delete" };
				});

				var colDefs = SquashTable.colDefs()
					.std(0, "label")
					.std(1, "code")
					.radio({ targets: 2, render: radio, data: "isDefault" })
					.button({ targets: 3, render: remove, data: "" })
					.build();

				view.$optionsTable = view.$("#options-table");
				view.$optionsTable.DataTable({
					//"oLanguage" : {
					//	"sUrl" : squashtm.app.cfTable.languageUrl
					//},
					//"bAutoWidth" : false,
					jQueryUI : true,
					filter : false,
					paginate : false,
					columnDefs: colDefs,
					data: []
				});
			}


			/*
			 * Defines the controller for the new custom field panel.
			 *
			 * DESIGN NOTE : confimdialog would not correctly bubble its events when it is not the view's $el.
			 * On the other hand, this.$el is
			 *
			 */
			var NewInfoListPanel = BindView.extend({
				wrapper: "#new-item-pane",
				defaultWidth : 600,
				richWidth : 1000,

				initialize : function() {
					var self = this;
					this.apiRoot = $(this.wrapper).data("api-url");

					this.options = new InfoListOptionCollection();

					this.listenTo(squashtm.vent, "list-option:add", this.onAddListOption);
					this.listenTo(this.options, "add remove change", this.onOptionsChanged);

					Validation.bind(this, validationOptions);

					this.render();
					initOptionsTable(this);

					this.dialog = this.$el.confirmDialog({
						autoOpen : true,
						close : function() {
							self.cancel.call(self);
						}
					});

					this._resize();
				},

				render : function() {
					if (this.template === undefined) {
						var src = $("#new-item-pane-tpl").html();
						NewInfoListPanel.prototype.template = Handlebars.compile(src);
					}
					this.$el.append($(this.template({})));

					var $wrapper = $(this.wrapper);
					this.$el.attr("tile", $wrapper.attr("title"));
					this.$el.addClass($wrapper.attr("class"));
					var conf = confman.getStdCkeditor();
					this.$("#code").ckeditor(function(){}, conf);

					$wrapper.html(this.$el);

					return this.renderItemPanel();
				},

				renderItemPanel: function() {
					this.itemPanel = new InfoListOptionPanel({model: new InfoListOptionModel()});
					return this;
				},

				_resize : function(){
					if (this.$el.data().confirmDialog !== undefined){
						var type = this.model.get("inputType");
						var width = (type === "RICH_TEXT") ? this.richWidth : this.defaultWidth;
						this.$el.confirmDialog("option", "width", width);
					}
				},

				remove : function() {
					Validation.unbind(this);
					this.undelegateEvents();
					this.itemPanel.remove();
					BindView.prototype.remove.apply(this, arguments);
				},

				events : {
					"confirmdialogcancel" : "cancel",
					"confirmdialogvalidate" : "validate",
					"confirmdialogconfirm" : "confirm",
					"draw.dt": "onDrawOptionsTable",
					"change input:radio[name='option-default']": "onChangeDefaultOption",
					"click button[name='option-delete']": "onClickOptionDelete",
				},

				cancel : function(event) {
					console.log("confirmdialogcancel")
					window.squashtm.vent.trigger("newinfolist:cancelled", { model: this.model, view: this, source: event });
				},

				confirm : function(event) {
					console.log("confirmdialogconfirm")
					window.squashtm.vent.trigger("newinfolist:confirmed", { model: this.model, view: this, source: event });
				},

				validate : function(event) {
					var err = this.model.save(null, {

						url: this.apiRoot + "/new",
						async : false,
						error : function() {
							console.log("save error", arguments);
							res = false;
							event.preventDefault();
						}
					});

					return err;
				},

				onChangeDefaultOption: function(event) {
					console.log("onchangedefault")
					var tgt = event.target;
					var code = tgt.value;
					this.options.forEach(function(opt) {
						(opt.get("code") === code) ? opt.set("isDefault", true) : opt.set("isDefault", false);
					});
				},

				onClickOptionDelete: function(event) {
					var tgt = event.target;
					var code = tgt.dataset.value;
					var opt = this.options.findWhere({ code: code });
					if(!!this.options.remove(opt)) {
						this.$optionsTable.DataTable()
							.row($(tgt).closest("tr")).remove()
							.draw();
					}
				},

				onOptionsChanged: function(event) {
					this.model.set("options", this.options.toJSON());
				},

				onAddListOption: function(event) {
					this.options.add(event.model);
					this.$optionsTable.DataTable()
						.row.add(event.model.attributes)
						.draw();
					this.itemPanel.remove();
					this.renderItemPanel();
				},
			});

			return NewInfoListPanel;
		});