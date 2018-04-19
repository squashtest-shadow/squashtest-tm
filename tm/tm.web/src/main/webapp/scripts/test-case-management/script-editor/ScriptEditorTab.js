/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
define(["jquery", "backbone", "underscore", "ace/ace", "workspace.routing"], function ($, Backbone, _, ace, urlBuilder) {
	var ScriptEditorTab = Backbone.View.extend({

		el: "#tab-tc-script-editor",

		//This view is initialized in test-case.jsp, the server model is directly injected in the page.
		initialize: function (options) {
			var serverModel = options.settings;
			this._initializeEditor(serverModel);
			this._initializeModel(serverModel);
		},

		events: {
			"click #tc-script-save-button": "saveScript",
			"click #tc-script-toggle-option-panel": "toggleOptionPanel",
			"click #tc-script-activate-editor": "activateEditor",
			"click #tc-script-cancel": "cancel"
		},

		saveScript: function () {
			var that = this;
			var tcScript = this.editor.session.getValue();
			this.model.set('script', tcScript);
			this.model.save(null,{
				success: function (model, response) {
					that.originalScript = tcScript;
				}});
		},

		_initializeEditor: function (serverModel) {
			var that = this;
			//see https://github.com/ajaxorg/ace-builds/issues/35 to understand the next line...
			//so much headaches with the loading of ace extensions before finding this magic method...
			ace.config.loadModule("ace/ext/language_tools", function(langTools) {
				var editor = ace.edit("tc-script-editor");
				//disabling local auto completion as specified
				//must do it before using enableBasicAutoCompletion: true
				langTools.setCompleters([langTools.snippetCompleter]);
				that.editor = editor;
				that.originalScript = serverModel.scriptExender.script;

				editor.session.setValue(that.originalScript);
				that._initialize_editor_mode(editor);
				editor.setTheme("ace/theme/iplastic");
				editor.setOptions({
					// Has to set this one to true if i want snippets, but basic auto completion is disabled above
					enableBasicAutocompletion: true,
					// Soft tabs with two spaces by tabs like required by gherkin good practices
					tabSize: 2,
					useSoftTabs: true,
					enableSnippets: true,
					enableLiveAutocompletion: false,
					readOnly: true,
					highlightActiveLine: false,
					highlightGutterLine: false
				});

				// Adding convenient shortcut to save the script to server
				editor.commands.addCommand({
					name: 'saveGherkinScript',
					bindKey: {win: 'Ctrl-Alt-S',  mac: 'Command-Alt-S'},
					exec: function(editor) {
						console.log("saving script to server");
						that.saveScript();
					},
					readOnly: false
				});


			});
		},

		_initializeModel: function (options) {
			var ScriptedTestCseModel = Backbone.Model.extend({
				urlRoot: urlBuilder.buildURL("testcases.scripted", options.testCaseId)
			});
			this.model = new ScriptedTestCseModel();
		},

		_initialize_editor_mode: function (editor) {
			this.locale = this._findScriptLocale();

			var aceEditorMode;
			if (this.locale === "en") {
				aceEditorMode = "ace/mode/gherkin";
			} else {
				aceEditorMode = "ace/mode/gherkin-" + this.locale;
			}
			this.editor.session.setMode(aceEditorMode);
		},

		//copy pasta from ext-options.js as it is not exposed... sigh
		getThemes: function () {
			var themeList = ace.require("ace/ext/themelist");
			var themes = {Bright: [], Dark: []};
			themeList.themes.forEach(function (x) {
				themes[x.isDark ? "Dark" : "Bright"].push({caption: x.caption, value: x.theme});
			});
			return themes;
		},

		//do it in standard js mode, not in jquery, as done in ace editor demo and code
		_initOptionTab: function (editor) {
			var OptionPanel = ace.require("ace/ext/options").OptionPanel;
			var dom = ace.require("ace/lib/dom");
			var optionsPanel = new OptionPanel(editor);
			var themes = this.getThemes();


			var mainOptionGroup = {
				Theme: {
					path: "theme",
					type: "select",
					items: themes
				},

				"Font Size": {
					path: "fontSize",
					type: "number",
					defaultValue: 12,
					defaults: [
						{caption: "12px", value: 12},
						{caption: "24px", value: 24}
					]
				}
			};

			var moreOptionGroup = {
				"Show Invisibles": {
					path: "showInvisibles"
				},
				// "Show Indent Guides": {
				// 	path: "displayIndentGuides"
				// },
				"Show Gutter": {
					path: "showGutter"
				}
			};

			// brutal monkey patching... sorry for that but the authors of ace/ext-options.js
			// do not seems to have exposed a way to control witch items are shown in their nice control panel...
			optionsPanel.render = function () {
				optionsPanel.container.innerHTML = "";
				dom.buildDom(["table", {id: "controls"},
					optionsPanel.renderOptionGroup(mainOptionGroup),
					["tr", null, ["td", {colspan: 2},
						["table", {id: "more-controls"},
							optionsPanel.renderOptionGroup(moreOptionGroup)
						]
					]]
				], optionsPanel.container);
			};
			var optionsPanelContainer = document.getElementById("optionsPanel");
			optionsPanel.render();
			optionsPanelContainer.insertBefore(optionsPanel.container, optionsPanelContainer.firstChild);
		},

		_findScriptLocale: function () {
			var line0 = this.editor.session.getLine(0);
			var locale = "en";
			console.log(line0);
			if (line0 !== null) {
				line0 = line0.trim();
				if (line0.search("language:") !== -1) {
					var parsedLocale = line0.substring(line0.length - 2);
					console.log(parsedLocale);
					switch (parsedLocale) {
						case "fr" :
							locale = "fr";
							break;
						case "de" :
							locale = "de";
							break;
						case "es" :
							locale = "es";
							break;
						default:
							console.log("Unable to find locale from parsed locale : " + parsedLocale + ". Default to en.")
					}
				}
			}
			return locale;
		},

		toggleOptionPanel: function () {
			this.$el.find(".option-panel-wrapper").show();
			this.$el.find("#tc-script-editor").toggleClass("tc-script-editor-option-open tc-script-editor-option-closed");
		},

		activateEditor: function () {
			this.editor.setTheme("ace/theme/chrome");
			this.editor.setOptions({
				readOnly: false,
				highlightActiveLine: true,
				highlightGutterLine: true
			});
			this.$el.find("#tc-script-save-button").prop("disabled", false);
			this.$el.find("#tc-script-cancel").show();
			this.$el.find("#tc-script-activate-editor").hide();
		},

		cancel : function () {
			this.editor.setTheme("ace/theme/iplastic");
			this.editor.setOptions({
				readOnly: true,
				highlightActiveLine: false,
				highlightGutterLine: false
			});
			this.$el.find("#tc-script-save-button").prop("disabled", true);
			this.$el.find("#tc-script-cancel").hide();
			this.$el.find("#tc-script-activate-editor").show();
			this.editor.session.setValue(this.originalScript);
		}

	});

	return ScriptEditorTab;
});
