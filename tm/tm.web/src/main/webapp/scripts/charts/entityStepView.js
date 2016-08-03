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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "workspace.projects", "./abstractStepView", "tree", "squash.translator", "./treePopup", "jquery.squash.confirmdialog", "jquery.squash.buttonmenu"],
	function ($, backbone, _, Handlebars, projects, AbstractStepView, tree, translator, TreePopup) {
		"use strict";

		translator.load({
			msgdefault: 'wizard.perimeter.msg.default',
			msgcustomroot: 'wizard.perimeter.msg.custom.root',
			msgcustomsingle: 'wizard.perimeter.msg.custom.singleproject',
			msgcustommulti: 'wizard.perimeter.msg.custom.multiproject'
		});

		var entityStepView = AbstractStepView.extend({

			initialize: function (data, wizrouter) {
				this.tmpl = "#entity-step-tpl";
				this.model = data;
				data.name = "entity";
				this._initialize(data, wizrouter);
				$("#change-perimeter-button").buttonmenu();
				var treePopup = $("#tree-popup-tpl").html();
				this.treePopupTemplate = Handlebars.compile(treePopup);
				this.initPerimeter();


			},

			events: {
				"click .perimeter-select": "openPerimeterPopup",
				"click #repopen-perim": "reopenPerimeter",
				"click #reset-perimeter": "resetPerimeter"

			},

			initPerimeter: function () {
				var scope = this.model.get("scopeEntity") || "default";
				if (scope === "default") {
					this.writeDefaultPerimeter();
				} else {
					this.writePerimeter(scope);
				}

			},

			writeDefaultPerimeter: function () {

				var defaultId = this.model.get("defaultProject");
				var projectName = projects.findProject(defaultId).name;

				var mainmsg = translator.get("wizard.perimeter.msg.default");
				var perimmsg = " " + translator.get('label.project').toLowerCase() + " " + projectName;
				$("#selected-perim-msg").text(mainmsg);
				$("#selected-perim").text(perimmsg);

				this.model.set({scope: [{type: "PROJECT", id: defaultId}]});
				this.model.set({projectsScope: [defaultId]});
				this.model.set({scopeEntity: "default"});
			},

			writePerimeter: function (name) {

				var rootmsg = translator.get('wizard.perimeter.msg.custom.root');
				var entitynames = translator.get("wizard.perimeter." + name);

				var projScope = this.model.get('projectsScope'),
					suffixmsg = null;

				if (projScope.length === 1) {
					var projectId = projects.findProject(projScope[0]).name;
					suffixmsg = translator.get('wizard.perimeter.msg.custom.singleproject', entitynames, projectId);
				} else {
					suffixmsg = translator.get('wizard.perimeter.msg.custom.multiproject', entitynames);
				}

				$("#selected-perim-msg").text(rootmsg);
				var link = "<a id='repopen-perim' style='cursor:pointer' name= '" + name + "'>" + suffixmsg + "</a>";
				$("#selected-perim").html(link);

			},

			resetPerimeter: function () {
				this.writeDefaultPerimeter();
			},

			reopenPerimeter: function (event) {


				var self = this;

				var nodes = _.map(this.model.get("scope"), function (obj) {
					return {
						restype: obj.type.split("_").join("-").toLowerCase() + "s", //yeah that quite fucked up...change back the _ to -, lower case and add a "s"
						resid: obj.id
					};
				});


				var treePopup = new TreePopup({
					model: self.model,
					name: event.target.name,
					nodes: nodes

				});
				self.addTreePopupConfirmEvent(treePopup, self, event.target.name);

			},
			openPerimeterPopup: function (event) {

				var self = this;

				var treePopup = new TreePopup({
					model: self.model,
					name: event.target.name,
					nodes: []
				});

				self.addTreePopupConfirmEvent(treePopup, self, event.target.name);


			},

			addTreePopupConfirmEvent: function (popup, self, name) {

				popup.on('treePopup.confirm', function () {

					var scope = _.map($("#tree").jstree('get_selected'), function (sel) {
						return {
							type: $(sel).attr("restype").split("-").join("_").slice(0, -1).toUpperCase(),
							id: $(sel).attr("resid")
						};
					});
					self.model.set({scope: scope});
					self.model.set({
						projectsScope: _.uniq(_.map($("#tree").jstree('get_selected'), function (obj) {
							return $(obj).closest("[project]").attr("project");
						}))
					});
					self.writePerimeter(name);
					self.model.set({scopeEntity: name});
					self.removeInfoListFilter();
				});

			},


			removeInfoListFilter: function () {
				this.model.set({
					filters: _.chain(this.model.get("filters"))
						.filter(function (val) {
							return val.column.dataType != "INFO_LIST_ITEM";
						})
						.value()
				});
			},

			updateModel: function () {

				var self = this;

				var entity = _.map($("input[name='entity']:checked"), function (a) {
					return $(a).val();
				});

				this.model.set({selectedEntity: entity});

				this.model.set({
					selectedAttributes: _.filter(this.model.get("selectedAttributes"), function (val) {
						return _.contains(self.getIdsOfValidColumn(), val);
					})
				});

				var filtered = _(['filters', 'axis', 'measures', 'operations'])
					.reduce(function (memo, val) {
						memo[val] = self.filterWithValidIds(self.model.get(val));
						return memo;
					}, {});

				this.model.set(filtered);

			},

			filterWithValidIds: function (col) {
				var self = this;

				return _.chain(col)
					.filter(function (val) {
						return _.contains(self.getIdsOfValidColumn(), val.column.id.toString());
					})
					.value();

			},

			getIdsOfValidColumn: function () {
				return _.chain(this.model.get("columnPrototypes"))
					.pick(this.model.get("selectedEntity"))
					.values()
					.flatten()
					.pluck("id")
					.map(function (val) {
						return val.toString();
					})
					.value();
			}
		});

		return entityStepView;

	});
