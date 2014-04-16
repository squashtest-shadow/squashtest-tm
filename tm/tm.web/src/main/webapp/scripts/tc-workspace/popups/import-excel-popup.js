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
define([ "jquery", "tree", "workspace/workspace.import-popup" ], function($, zetree) {
	"use strict";

	var recapBuilder = {};
	/**
	 * builds recap for zip import
	 * @param json
	 * @returns recap as an html string
	 */
	recapBuilder.zip = function(json) {
		this.template = this.template || Handlebars.compile($("#zip-import-recap-tpl").html()); // caching

		var recap = $.extend({}, json);
		recap.failuresClass = ((recap.failures > 0) ? "span-red" : "");
		recap.createdOnly = (recap.renamed + recap.modified) === 0;
		recap.hasRenamed = recap.renamed > 0;
		recap.hasModified = recap.modified > 0;
		recap.hasRejects = recap.rejected > 0;

		return this.template(recap);
	};

/**
 * builds recap for xls import
 * @param json
 * @returns recap as an html string
 */
	recapBuilder.xls = function(json) {
		this.template = this.template || Handlebars.compile($("#xls-import-recap-tpl").html()); // caching

		var recap = {
				testCaseSuccess: 10,
				testCaseWarning: 20,
				testCaseFailure: 30,
				testStepSuccess: 10,
				testStepWarning: 20,
				testStepFailure: 30,
				parameterSuccess: 10,
				parameterWarning: 20,
				parameterFailure: 30,
				datasetSuccess: 10,
				datasetWarning: 20,
				datasetFailure: 30,
				xlsRecapUrl: "http://foo.bar"
		};

		return this.template(recap);
	};

	$.widget("squash.tcimportDialog", $.squash.importDialog, {

		_create : function() {
			this._super();
			this.importType = this.element.find("#xls-import-opt").val();
		},

		createSummary : function(json) {
			this.element.find(".import-summary").html(recapBuilder[this.importType].call(this, json));
		},

		bindEvents : function() {
			this._super();
			var self = this;

			this.onOwnBtn("ok", function() {
				var tree = zetree.get();
				var projectId = self.element.find("select[name='projectId']").val();
				var lib = tree.jstree("findNodes", {
					rel : "drive",
					resid : projectId
				});
				if (lib.size() > 0) {
					tree.jstree("refresh", lib);
				}
			});

			this.onOwnBtn("simulate", function() {
				if(self.validate() === true) {
					self.simulate();
				} else {
					self.setState("error-format");
				}
			});

			this.element.on("change", "input[name='import-type']", function() {
				var value = $(this).val();
				self.importType = value;
				self.options.formats = self.options.typeFormats[value];
				$("#simulateButton").prop("disabled", value === "zip");
				$("#import-err-filetype").text(self.options.formats);
			});

			this.element.on("change", "select[name='projectId']", function() {
				var projectname = $(":selected", this).text();
				self.element.find(".confirm-project").text(projectname);
			});

			this.element.on("change", "input[type='file']", function() {
				var filename = /([^\\]+)$/.exec(this.value)[1];
				self.element.find(".confirm-file").text(filename);
			});
		},

		simulate: function() {
			this.setState("progression");
			this.doSubmit({ urlPostfix: "/" + this.importType , queryParams: {"dry-run": true}});
		},

		submit: function() {
			this.setState("progression");
			this.doSubmit({ urlPostfix: "/" + this.importType });
		}

	});


	function init() {
		$("#import-excel-dialog").tcimportDialog({
			formats : [ "xls", "xlsx" ],
			typeFormats: {
				zip: ["zip"],
				xls: ["xls", "xlsx"]
			}
		});
	}

	return {
		init : init
	};

});