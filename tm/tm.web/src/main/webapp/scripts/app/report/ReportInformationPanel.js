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
define(["is", "jquery", "backbone", "squash.configmanager", "workspace.routing", "./ReportCriteriaPanel", "jqueryui",
], function (is, $, backbone, confman, router, ReportCriteriaPanel) {
	"use strict";

	var reportInfomationPanel = Backbone.View.extend({

			// el: "#report-information-panel",

			initialize: function (attributes, config) {
				this.config = config;
				this.initText(config);

			},

			events: {
				"click #save": "save"
			},

			initText: function (config) {

				$("#report-description").ckeditor(function () {
				}, confman.getStdCkeditor());

				var reportDef = config.reportDef;

				if (reportDef !== null & reportDef !== undefined) {
					reportDef = JSON.parse(reportDef)
					$("#report-name").val(reportDef.name);
					$("#report-description").val(reportDef.description);
				}
			},

			getCookiePath: function () {
				var path = router.buildURL("custom-report-base");
				if (is.ie() || is.firefox()) {
					path = path + "/";
				}
				return path;
			},

			save: function () {
				if (this.model.hasBoundary()) {

					var path = this.getCookiePath();
					var data = {
						name: $("#report-name").val(),
						description: $("#report-description").val(),
						parameters: JSON.stringify(this.model.toJSON()),
					};
					$.ajax({
						type: "POST",
						url: this.config.reportUrl + "/panel/content/new-report/" + this.config.parentId,
						contentType: "application/json",
						data: JSON.stringify(data)
					}).done(function (id) {
						var nodeToSelect = "CustomReportReport-" + id;
						$.cookie("jstree_select", nodeToSelect, {path: path});
						window.location.href = router.buildURL("custom-report-report-redirect", id);
					})

				} else {

					var invalidPerimeterDialog = $("#invalid-perimeter").messageDialog();
					invalidPerimeterDialog.messageDialog("open");

				}


			}


		})
	;

	return reportInfomationPanel;

});
