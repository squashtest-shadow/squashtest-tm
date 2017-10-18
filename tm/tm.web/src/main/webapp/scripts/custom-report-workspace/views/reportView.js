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
define(["underscore", "backbone", "squash.translator", "handlebars", "squash.dateutils",
		"workspace.projects", "workspace.routing"],
	function (_, Backbone, translator, Handlebars, dateutils, projects, urlBuilder, chartUtils) {
		"use strict";

		var View = Backbone.View.extend({

			el: "#contextual-content-wrapper",
			tpl: "#tpl-show-report",

			initialize: function (options) {
				this.options = options;

				this.i18nString = translator.get({
					"dateFormat": "squashtm.dateformat",
					"dateFormatShort": "squashtm.dateformatShort"
				});
				_.bindAll(this, "render", "redraw");
				this.render();
			},

			events: {
				"click #refresh-btn": "refresh",
				"click #modify-report-button": "modifyChart",
				"click #rename-report-button": "rename",
				"click #export-report-button": "export"
			},

			render: function () {
				$(window).unbind('resize.report');
				this.$el.html("");
				var self = this;
				var url = urlBuilder.buildURL('custom-report-report-server', this.model.get('id'));

				this.options.acls.fetch({})
					.then(function () {
						return $.ajax({
							'type': 'get',
							'dataType': 'json',
							'contentType': 'application/json',
							'url': url
						});

					}).then(function (json) {
					self.setBaseModelAttributes(json);
					self.loadI18n();
					self.template();
					$(window).bind('resize.report', self.redraw);
				});
			},

			refresh: function () {
				this.activeChart.remove();
				this.render();
			},

			initListenerOnWindowResize: function () {
				var self = this;
				$(window).bind('resize.report', self.redraw);
			},

			redraw: function () {
				this.activeReport.render();
			},

			template: function () {
				// TODO maybe template could be compiled only once -> store it someplace
				var source = $("#tpl-show-chart").html();
				var template = Handlebars.compile(source);
				Handlebars.registerPartial("entityFiltersTpl", $(this.entityFiltersTpl).html());
				Handlebars.registerPartial("filterTpl", $(this.filterTpl).html());
				Handlebars.registerPartial("entityOperationsTpl", $(this.entityOperationsTpl).html());
				Handlebars.registerPartial("operationTpl", $(this.operationTpl).html());

				var props = this.model.toJSON();
				props.acls = this.options.acls.toJSON();

				this.$el.append(template(props));
			},

			setBaseModelAttributes: function (json) {
				this.model.set("name", json.name);
				this.model.set("createdBy", json.createdBy);
				this.model.set("createdOn", (this.i18nFormatDate(json.createdOn) + " " + this.i18nFormatHour(json.createdOn)));
				if (json.lastModifiedBy) {
					this.model.set("lastModifiedBy", json.lastModifiedBy);
					this.model.set("lastModifiedOn", (this.i18nFormatDate(json.lastModifiedOn) + " " + this.i18nFormatHour(json.lastModifiedOn)));
				}
				this.model.set("axes", json.axes);
				this.model.set("filters", json.filters);
				this.model.set("measures", json.measures);
				this.model.set("generatedDate", this.i18nFormatDate(new Date()));
				this.model.set("generatedHour", this.i18nFormatHour(new Date()));
			},

			i18nFormatDate: function (date) {
				return dateutils.format(date, this.i18nString.dateFormatShort);
			},

			i18nFormatHour: function (date) {
				return dateutils.format(date, "HH:mm");
			},

			loadI18n: function () {
				this.getAllI18n();
			},

			getAllI18n: function () {
				var keys = [];
				var self = this;
				//get all keys from operations
				var operations = this.model.get("entityOperation");
				_.each(operations, function (operationsByType) {
					_.each(operationsByType, function (op) {
						keys.push(op.entityType);
						keys.push(op.operationLabel);
						if(!op.isCuf){
							keys.push(op.columnLabel);
						}
					});
				});

				//get all keys from filters
				var filters = this.model.get("entityFilters");
				_.each(filters, function (filtersByType) {
					_.each(filtersByType, function (filter) {
						keys.push(filter.entityType);
						if(!filter.isCuf){
							keys.push(filter.columnLabel);
						}
						if (filter.hasI18nValues && !filter.isCuf) {
							_.each(filter.values, function (value) {
								keys.push(value);
							});
						}
					});
				});

				keys = _.chain(keys)
					.flatten()
					.uniq()
					.value();

				//retrieve alls strings from server and caching into local storage. using translator.get() to make synchrone request
				translator.get(keys);

				//now translate the operations and filters
				_.each(operations, function (operationsByType) {
					_.each(operationsByType, function (op) {
						op.entityType = self.getI18n(op.entityType);
						if(!op.isCuf){
							op.columnLabel = self.getI18n(op.columnLabel);
						}
						op.operationLabel = self.getI18n(op.operationLabel);
					});
				});

				_.each(filters, function (filtersByType) {
					_.each(filtersByType, function (filter) {
						filter.entityType = self.getI18n(filter.entityType);
						filter.operationLabel = self.getI18n(filter.operationLabel);
						if(!filter.isCuf){
							filter.columnLabel = self.getI18n(filter.columnLabel);
						}
						if (filter.hasI18nValues && !filter.isCuf) {
							_.each(filter.values, function (value, index) {
								filter.values[index] = self.getI18n(value);
							});
						}
					});
				});

			},

			getI18n: function (key) {
				return " " + translator.get(key);
			},

			modifyReport: function () {
				var nodeId = this.model.get('id');
				// var url = urlBuilder.buildURL("chart.wizard", nodeId);
				// document.location.href = url;
			},

			remove: function () {
				$(window).unbind('resize.report');
				if (this.activeReport) {
					this.activeReport.remove();
				}
				Backbone.View.prototype.remove.call(this);
			},

			rename: function () {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("renameNode");
			},

			export: function () {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("exportReport");
			},

		});

		return View;
	});
