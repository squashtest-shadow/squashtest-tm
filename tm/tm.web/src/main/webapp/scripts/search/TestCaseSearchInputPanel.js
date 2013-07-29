/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
define([ "jquery", "backbone", "handlebars", "squash.translator", "underscore",
		"app/util/StringUtil", "./SearchResultPage", , "jquery.squash",
		"jqueryui", "jquery.squash.togglepanel", "jquery.squash.datatables",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, translator, _,
		StringUtil, SearchResultPage) {

	var TestCaseSearchInputPanel = Backbone.View.extend({

		el : "#test-case-search-input-panel",

		initialize : function() {
			this.getInputInterfaceModel();
			this.makeGeneralInfoTogglePanel();
			this.makeCUFTogglePanel();
			this.makeImportanceTogglePanel();
			this.makePrerequisiteTogglePanel();
			this.makeAssociationTogglePanel();
			this.makeProjectTogglePanel();
			this.makeCreationTogglePanel();

		},

		events : {
			"click #test-case-search-button" : "showResults"
		},

		getInputInterfaceModel : function() {
			var self = this;
			var result = $.ajax({
				url : squashtm.app.contextRoot + "/advanced-search/input",
				data : "",
				dataType : "json"
			}).success(function(json) {
				$.each(json, function(key, value) {
					$.each(value, function(key, val){
						var source = $("#toggle-panel-template").html();
						var template = Handlebars.compile(source);
						var context = {"toggle-panel-id": val.id+"-panel-id", 
								       "toggle-panel-table-id": val.id+"-panel-table-id"};
						var html = template(context);
						$("#test-case-search-input-form-panel").append(html);
						self.makeTogglePanel(val.id+"-panel-id",val.name,true);
					});
				});
			});
		},

		showResults : function() {

			var results = $.ajax(
					{
						url : squashtm.app.contextRoot
								+ "/advanced-search/results?testcase"
					}).done(
					function(data) {
						$("#contextual-content").html(data);
						require([ "common" ], function(common) {
							require([ "jquery", "domReady",
									"./search/SearchResultPage" ], function($,
									domReady, TestCaseSearchInputPanel) {
								domReady(function() {

									new SearchResultPage();

								});
							});
						});
					});
		},

		makeTogglePanel : function(id, key, open) {
			var title = translator.get(key);
			
			var infoSettings = {
				initiallyOpen : open,
				title : title
			};
			this.$("#"+id).togglePanel(infoSettings);
		},
		
		makeCUFTogglePanel : function() {
			var title = translator.get("search.testcase.cuf.panel.title");

			var infoSettings = {
				initiallyOpen : false,
				title : title
			};
			this.$("#cuf-panel").togglePanel(infoSettings);
		},

		makeGeneralInfoTogglePanel : function() {
			var title = translator
					.get("search.testcase.generalinfos.panel.title");

			var infoSettings = {
				initiallyOpen : true,
				title : title
			};
			this.$("#general-information-panel").togglePanel(infoSettings);
		},

		makeImportanceTogglePanel : function() {
			var title = translator
					.get("search.testcase.importance.panel.title");
			var infoSettings = {
				initiallyOpen : false,
				title : title
			};
			this.$("#importance-panel").togglePanel(infoSettings);
		},

		makePrerequisiteTogglePanel : function() {
			var title = translator
					.get("search.testcase.prerequisite.panel.title");
			var infoSettings = {
				initiallyOpen : true,
				title : title
			};
			this.$("#prerequisite-panel").togglePanel(infoSettings);
		},

		makeAssociationTogglePanel : function() {
			var title = translator
					.get("search.testcase.association.panel.title");
			var infoSettings = {
				initiallyOpen : false,
				title : title
			};
			this.$("#association-panel").togglePanel(infoSettings);
		},

		makeProjectTogglePanel : function() {
			var title = translator.get("search.testcase.project.panel.title");
			var infoSettings = {
				initiallyOpen : false,
				title : title

			};
			this.$("#project-panel").togglePanel(infoSettings);
		},

		makeCreationTogglePanel : function() {
			var title = translator.get("search.testcase.creation.panel.title");
			var infoSettings = {
				initiallyOpen : true,
				title : title

			};
			this.$("#creation-panel").togglePanel(infoSettings);
		}

	});
	return TestCaseSearchInputPanel;
});