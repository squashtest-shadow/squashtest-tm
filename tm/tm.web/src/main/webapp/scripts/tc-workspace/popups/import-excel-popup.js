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
	"use-strict";

	$.widget("squash.tcimportDialog", $.squash.importDialog, {

		createSummary : function(json) {
			this.template = this.template || Handlebars.compile($("#import-recap-tpl").html());

			var recap = $.extend({}, json);
			recap.failuresClass = recap.failures > 0 ? "span-red" : "";
			recap.createdOnly = (recap.renamed + recap.modified) === 0;
			recap.hasRenamed = recap.renamed > 0;
			recap.hasModified = recap.modified > 0;
			recap.hasRejects = recap.rejected > 0;

			this.element.find(".import-summary").html(this.template(recap));

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

			this.element.on("change", "input[name='importFormat']", function() {
				var value = $(this).val();
				$("#simulateButton").prop("disabled", value === "zip");
			});

			this.element.on("change", "select[name='projectId']", function() {
				var projectname = $(":selected", this).text();
				self.element.find(".confirm-project").text(projectname);
			});

			this.element.on("change", "input[type='file']", function() {
				var filename = /([^\\]+)$/.exec(this.value)[1];
				self.element.find(".confirm-file").text(filename);
			});
		}

	});

	function init() {
		$("#import-excel-dialog").tcimportDialog({
			formats : [ "xls", "xlsx", "zip" ]
		});

		// ******** additional processing ***********
//		$($("input[name='importFormat']")[0]).attr("checked", "checked");
//		$("#simulateButton").prop("disabled", false);
	}

	return {
		init : init
	};

});