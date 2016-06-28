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
		"workspace.projects", "workspace.routing", "../charts/chartFactory"],
	function (_, Backbone, translator, Handlebars, dateutils, projects, urlBuilder, chartFactory) {
		"use strict";

		var View = Backbone.View.extend({

			el: "#contextual-content-wrapper",
			tpl: "#tpl-show-chart",
			entityFiltersTpl: "#tpl-show-entity-filters",
			filterTpl: "#tpl-show-one-filter",
			entityOperationsTpl: "#tpl-show-entity-operations",
			operationTpl: "#tpl-show-one-operation",
			valuesI18nColumnPrototypeLabel: ["REQUIREMENT_VERSION_CRITICALITY", "REQUIREMENT_VERSION_STATUS",
				"TEST_CASE_IMPORTANCE", "TEST_CASE_STATUS"],
			infolistI18nColumnPrototypeLabel: ["TEST_CASE_NATURE", "TEST_CASE_TYPE", "REQUIREMENT_VERSION_CATEGORY"],
			infolistTestCaseType: ["TYP_COMPLIANCE_TESTING", "TYP_CORRECTION_TESTING", "TYP_END_TO_END_TESTING",
				"TYP_EVOLUTION_TESTING", "TYP_PARTNER_TESTING", "TYP_REGRESSION_TESTING", "TYP_UNDEFINED"],
			infolistTestCaseNature: ["NAT_ATDD", "NAT_BUSINESS_TESTING", "NAT_FUNCTIONAL_TESTING", "NAT_NON_FUNCTIONAL_TESTING",
				"NAT_PERFORMANCE_TESTING", "NAT_SECURITY_TESTING", "NAT_UNDEFINED", "NAT_USER_TESTING"],
			infolistRequirementCategory: ["CAT_BUSINESS", "CAT_ERGONOMIC", "CAT_FUNCTIONAL", "CAT_NON_FUNCTIONAL", "CAT_PERFORMANCE", "CAT_SECURITY",
				"CAT_TECHNICAL", "CAT_TEST_REQUIREMENT", "CAT_UNDEFINED", "CAT_UNKNOWN", "CAT_USER_STORY", "CAT_USE_CASE"],

			initialize: function (options) {
				this.options = options;

				this.i18nString = translator.get({
					"dateFormat": "squashtm.dateformat",
					"dateFormatShort": "squashtm.dateformatShort"
				});
				_.bindAll(this, "render", "redraw");
				this.render();
				//this.initListenerOnWindowResize();
			},

			events: {
				"click #refresh-btn": "refresh",
				"click #modify-chart-button": "modifyChart",
				"click #rename-chart-button": "rename",
				"click #export-chart-button": "export"
			},

			render: function () {
				$(window).unbind('resize.chart');
				this.$el.html("");
				var self = this;
				var url = urlBuilder.buildURL('custom-report-chart-server', this.model.get('id'));

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
					self.setPerimeterMessage(json);
					self.template();
					self.activeChart = chartFactory.buildChart("#chart-display-area", json, self.getVueConf(), self.model.get("entityOperation"));
					$(window).bind('resize.chart', self.redraw);
				});
			},

			refresh: function () {
				this.activeChart.remove();
				this.render();
			},

			/**
			 * Set a boolean in model so Handlebar can handle fancy specification rules on perimeter
			 */
			setPerimeterMessage: function (json) {
				var projectScope = json.projectScope,
					scope = json.scope;

				// extract the type of perimeter
				var extractor = new RegExp('(PROJECT|TEST_CASE|REQUIREMENT|CAMPAIGN)');
				var etype = extractor.exec(scope[0].entityType)[0],
					entityNames = translator.get('wizard.perimeter.' + etype);

				// now build the message
				var msg = "";
				var projectName;

				// type 1 : the default perimeter
				if (etype === "PROJECT") {
					projectName = projects.findProject(projectScope[0]).name;
					msg = translator.get('label.project').toLowerCase() + " " + projectName;
				}
				// type 2 : custom perimeter with all nodes from the same project
				else if (projectScope.length === 1) {
					projectName = projects.findProject(projectScope[0]).name;
					msg = translator.get('wizard.perimeter.msg.custom.singleproject', entityNames, projectName);
				}
				// type 3 : multiple selection from multiple projects
				else {
					msg = translator.get('wizard.perimeter.msg.custom.multiproject', entityNames);
				}

				this.model.set('perimeterMessage', msg);
			},

			initListenerOnWindowResize: function () {
				var self = this;
				$(window).bind('resize.chart', self.redraw);
			},

			redraw: function () {
				this.activeChart.render();
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
				this.model.set("projectName", json.scope[0].name);//for now we have just default project as perimeter
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
				this.loadFilters();
				this.loadOperations();
				this.getAllI18n();
			},

			loadFilters: function () {
				var self = this;
				var entityFilters = _.chain(this.model.get("filters"))
					.map(function (filter) {
						var formatedFilter = {
							entityType: self.addPrefix(filter.columnPrototype.specializedEntityType.entityType, "chart.entityType."),
							columnLabel: self.addPrefix(filter.columnPrototype.label, "chart.column."),
							values: self.getI18nKeyForFilterValues(filter.columnPrototype.label, filter.values),
							hasI18nValues: self.filterHasI18nValues(filter.columnPrototype.label, filter.values)
						};
						return formatedFilter;
					})
					.groupBy("entityType")
					.values()
					.value();
				this.model.set("entityFilters", entityFilters);
			},

			loadOperations: function () {
				var self = this;
				var operations = _.union(this.model.get("axes"), this.model.get("measures"));
				var formatedOperations = _.chain(operations)
					.map(function (operation) {
						return {
							entityType: self.addPrefix(operation.columnPrototype.specializedEntityType.entityType, "chart.entityType."),
							columnLabel: self.addPrefix(operation.columnPrototype.label, "chart.column."),
							operationLabel: self.addPrefix(operation.operation.name, "chart.operation.")
						};
					})
					.groupBy("entityType")
					.values()
					.value();
				this.model.set("entityOperation", formatedOperations);
			},

			addPrefix: function (obj, prefix) {
				return prefix + obj;
			},

			getAllI18n: function () {
				var keys = [];
				var self = this;
				//get all keys from operations
				var operations = this.model.get("entityOperation");
				_.each(operations, function (operationsByType) {
					_.each(operationsByType, function (op) {
						keys.push(_.values(op));
					});
				});

				//get all keys from filters
				var filters = this.model.get("entityFilters");
				_.each(filters, function (filtersByType) {
					_.each(filtersByType, function (filter) {
						keys.push(filter.entityType);
						keys.push(filter.columnLabel);
						if (filter.hasI18nValues) {
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
						op.columnLabel = self.getI18n(op.columnLabel);
						op.operationLabel = self.getI18n(op.operationLabel);
					});
				});

				_.each(filters, function (filtersByType) {
					_.each(filtersByType, function (filter) {
						filter.entityType = self.getI18n(filter.entityType);
						filter.columnLabel = self.getI18n(filter.columnLabel);
						if (filter.hasI18nValues) {
							_.each(filter.values, function (value, index) {
								filter.values[index] = self.getI18n(value);
							});
						}
					});
				});

			},

			filterHasI18nValues: function (columnPrototypeLabel, values) {
				var hasI18nValues = false;
				if (_.contains(this.valuesI18nColumnPrototypeLabel, columnPrototypeLabel)) {
					hasI18nValues = true;
				}
				//if we have an infolist comlumn, it's a little bit tricky because we must check if vales are defaults (and must be translated) or custom values...
				if (_.contains(this.infolistI18nColumnPrototypeLabel, columnPrototypeLabel)) {
					var firstValue = values[0] || "";//values shouldn't be empty because a filter must have values...
					hasI18nValues = this.isDefaultInfolist(columnPrototypeLabel, firstValue);
				}
				return hasI18nValues;
			},

			getI18nKeyForFilterValues: function (columnPrototypeLabel, values) {
				var self = this;
				if (this.filterHasI18nValues(columnPrototypeLabel, values)) {
					return _.map(values, function (value) {
						return self.getI18nKeyForFilterValue(columnPrototypeLabel, value);
					});
				}
				return values;
			},

			getI18nKeyForFilterValue: function (columnPrototypeLabel, value) {
				switch (columnPrototypeLabel) {
					case "TEST_CASE_IMPORTANCE":
						return this.addPrefix(value, "test-case.importance.");
					case "TEST_CASE_STATUS":
						return this.addPrefix(value, "test-case.status.");
					case "TEST_CASE_NATURE":
						return this.addPrefix(value, "test-case.nature.");
					case "TEST_CASE_TYPE":
						return this.addPrefix(value, "test-case.type.");
					case "REQUIREMENT_VERSION_CRITICALITY":
						return this.addPrefix(value, "requirement.criticality.");
					case "REQUIREMENT_VERSION_STATUS":
						return this.addPrefix(value, "requirement.status.");
					case "REQUIREMENT_VERSION_CATEGORY":
						return this.addPrefix(value, "requirement.category.");
					default:
						return value;
				}
			},

			isDefaultInfolist: function (columnPrototypeLabel, value) {
				var isDefault = false;
				switch (columnPrototypeLabel) {
					case "TEST_CASE_NATURE":
						isDefault = _.contains(this.infolistTestCaseNature, value);
						break;
					case "TEST_CASE_TYPE":
						isDefault = _.contains(this.infolistTestCaseType, value);
						break;
					case "REQUIREMENT_VERSION_CATEGORY":
						isDefault = _.contains(this.infolistRequirementCategory, value);
						break;
					default:
				}
				return isDefault;
			},

			getI18n: function (key) {
				return " " + translator.get(key);
			},

			modifyChart: function () {
				var nodeId = this.model.get('id');
				var url = urlBuilder.buildURL("chart.wizard", nodeId);
				document.location.href = url;
			},

			remove: function () {
				$(window).unbind('resize.chart');
				if (this.activeChart) {
					this.activeChart.remove();
				}
				Backbone.View.prototype.remove.call(this);
			},

			rename: function () {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("renameNode");
			},

			export: function () {
				var wreqr = squashtm.app.wreqr;
				wreqr.trigger("exportChart");
			},

			getVueConf: function () {
				return {
					animate: true
				};
			}

		});

		return View;
	});
