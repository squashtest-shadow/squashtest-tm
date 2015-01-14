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
define([ "backbone", "underscore", "./ConciseFormModel", "app/util/ButtonUtil", "tree", 
         "./ProjectsPickerPopup", "./SingleProjectPickerPopup", "jeditable.datepicker",
         "jquery.squash.formdialog"],
function(Backbone, _, FormModel, ButtonUtil, treeBuilder, ProjectsPickerPopup, SingleProjectPickerPopup) {
	"use strict";

	var postDateFormat = $.datepicker.ATOM;
	var postNoDate = "--";

	function workspaceType(domTree) {
		var type = $(domTree).data("nodetype");
		return type.toLowerCase().replace(/_/g, "-");
	}


	function reformatDate(sourceFormat, targetFormat) {
		return function(date) {
			var tmp = $.datepicker.parseDate(sourceFormat, date);
			return $.datepicker.formatDate(targetFormat, tmp);
		};
	}

	function radioBinding(radio) {
		var binding = (radio.id).replace("-binder", "");

		if (_.isUndefined(binding) || binding === "none") {
			return undefined;
		}

		return binding;
	}

	/**
	 * View for the report criteria form
	 */
	var ReportCriteriaPanel = Backbone.View.extend({
		initialize: function(attributes, options) {
			// state init
			this.config = options.config;
			this.projectPickers = {};

			// methods fiddling
			_.bindAll(this, "setPickedNodes");
			// NOTE below : replaces handler builder by actual handler - kinda currifies onDatePicked(view, value) into onDatePicked(value)
			this.onDatePicked = this.onDatePicked(this);
			this.onNodesPicked = this.onNodesPicked(this);

			// model init
			this._initModel();
			if(options.formerState) {
				this.model.applyFormerState(options.formerState);
			}

			this.render();
		},

		render: function() {
			this._renderTexts();
			this._renderCheckboxes();
			this._renderCheckboxesGroups();
			this._renderDropdownLists();
			this._renderRadioGroups();
			this._renderDatePickers();
			this._renderTreePickers();
			this._renderProjectPickers();

			return this;
		},

		events: {
			"blur input:text.rpt-text-crit": "changeValuedInput",
			"change input:checkbox[data-grouped!='true']": "changeSingleCheckbox",
			"change input:checkbox[data-grouped='true']": "changeGroupedCheckbox",
			"change .rpt-drop select": "changeValuedInput",
			"change input:radio": "changeRadioButtonGroup",
			"click .rpt-tree-crit-open": "openTreePicker",
			"click .rpt-projects-crit-open": "openProjectPicker"
		},

		_renderTexts: function() {
			var self = this;

			this.$("input:text").each(function() {
				var dom = this;
				var attr = self.model.get(dom.name);
				if (!!attr && !_.isUndefined(attr.val)) {
					dom.value = attr.val;
				}
			});
		},

		_renderCheckboxes: function() {
			var self = this;

			this.$("input:checkbox[data-grouped!='true']").each(function() {
				var dom = this;
				var attr = self.model.get(dom.name);
				if (!!attr && _.isBoolean(attr.val)) {
					dom.checked = attr.val;
				}
			});
		},

		_renderCheckboxesGroups: function() {
			var self = this;

			this.$("input:checkbox[data-grouped='true']").each(function() {
				var dom = this;
				var attr = self.model.get(dom.name);
				dom.checked = !!attr && _.isArray(attr.val) && _.contains(attr.val, dom.value);
			});
		},

		_renderDropdownLists: function(selector) {
			var self = this;

			var optIterator = function(expectedValue) {
				return function() {
					var opt = this;
					opt.selected = opt.value === expectedValue;
				};
			};

			this.$(".rpt-drop select").each(function() {
				var dom = this;
				var attr = self.model.get(dom.name);
				if (!!attr && !_.isUndefined(attr.val)) {
					$(dom).find("option").each(optIterator(attr.val));
				}
			});
		},

		_renderRadioGroups: function() {
			var self = this;

			this.$("input:radio").each(function() {
				var dom = this;
				var attr = self.model.get(dom.name);
				dom.checked = !!attr && !_.isUndefined(attr.val) && dom.value === attr.val;
			}).each(function() {
				var dom = this;
				var binding = radioBinding(dom);
				if (!_.isUndefined(binding)) {
					var operation = dom.checked ? "enable" : "disable";
					self.setBoundButtonState(operation, binding);
				}
			});

		},

		_renderDatePickers: function() {
			var self = this;
			var $pickers = this.$(".rpt-date-crit");

			var settings = _.extend({}, $.datepicker.regional[$pickers.data("locale")]);
			_.defaults(settings, { dateFormat: "dd/mm/yy" });

			$pickers.each(function() {
				var dom = this;
				var attr = self.model.get(dom.id);
				if (_.isUndefined(attr.val) || attr.val === postNoDate) {
					dom.innerHTML = $(dom).data("nodate");
				} else {
					dom.innerHTML = reformatDate(postDateFormat, settings.dateFormat)(attr.val);
				}
			}).editable(this.onDatePicked, {
				type : "datepicker",
				tooltip : "Click to edit...",
				datepicker : settings
			});
		},

		_renderTreePickers: function() {
			var self = this;
			var config = self.config;

			this.$(".rpt-tree-crit").each(function(i, dom) {
				var type = workspaceType(dom);
				var url = config.contextPath + "/" + type + "-browser/drives";

				$.get(url, "linkables", "json").done(function(data) {
					var settings = _.clone(config);
					settings.workspace = type;
					settings.model = data;
					settings.treeselector = "#" + dom.id;
					treeBuilder.initLinkableTree(settings);
				});
			});

			this.$(".rpt-tree-crit-dialog").each(function() {
				var $dialog = $(this);
				
				$dialog.formDialog({height:500});
				
				$dialog.on('formdialogconfirm', self.onNodesPicked);
				
				$dialog.on('formdialogcancel', function(){$dialog.formDialog('close');});

			});
		},

		_renderProjectPickers: function() {
			var self = this;
			this.$(".project-picker").each(function(i, dom) {
				var pickerView;
				if ($(dom).data("multiselect") === "true") {
					pickerView = new ProjectsPickerPopup({ el : dom, model: self.model });
				} else {
					pickerView = new SingleProjectPickerPopup({ el : dom, model: self.model });
				}
				self.projectPickers[dom.id] = pickerView;
			});
		},

		_initModel: function() {
			this._initTexts();
			this._initCheckboxes();
			this._initCheckboxesGroups();
			this._initSingleOptionInputs(".rpt-drop select", "DROPDOWN_LIST");
			this._initSingleOptionInputs("input:radio:checked", "RADIO_BUTTONS_GROUP");
			this._initDatePickers();
			this._initPickers(".rpt-tree-crit", "TREE_PICKER");
			this._initPickers(".project-picker", "PROJECT_PICKER");
		},

		_initTexts: function() {
			var self = this;

			this.$("input:text").each(function() {
				var dom = this;
				self.model.set(dom.name, new FormModel.Input("TEXT", dom.value));
			});
		},

		_initCheckboxes: function() {
			var self = this;

			this.$("input:checkbox[data-grouped!='true']").each(function() {
				var dom = this;
				self.model.set(dom.name, new FormModel.Input("CHECKBOX", dom.checked));
			});
		},

		_initCheckboxesGroups: function() {
			var model = this.model;
			var $checked = this.$("input:checkbox[data-grouped='true']:checked");

			// extract control names
			var names = _.chain($checked).pluck("name").uniq().value();

			// init model with empty inputs
			_.each(names, function(name) {
				model.set(name, new FormModel.Input("CHECKBOXES_GROUP", []));
			});

			// pushes them input values
			_.each($checked, function(cbx) {
				model.get(cbx.name).val.push(cbx.value);
			});
		},

		_initSingleOptionInputs: function(selector, inputType) {
			var self = this;

			this.$(selector).each(function() {
				var dom = this;
				self.model.set(dom.name, new FormModel.Input(inputType, dom.value || ""));
			});
		},

		_initDatePickers: function() {
			var self = this;
			this.$(".rpt-date-crit").each(function(it, dom) {
				// TODO slap this ugly "reformat" stuff into a function ffs
				var date = postNoDate;

				try {
					date = reformatDate($(dom).data("locale") || "dd/mm/yy", postDateFormat)(dom.innerHTML);
				} catch (ex) { /*noop*/ }

				self.model.set(dom.id, new FormModel.Input("DATE", date === "" ? postNoDate : date));
			});
		},

		_initPickers: function(selector, pickerType) {
			var self = this;

			this.$(selector).each(function() {
				self.model.set(this.id, new FormModel.Input(pickerType, []));
			});
		},

		changeValuedInput: function(event) {
			var input = event.currentTarget;
			this.model.setVal(input.name, input.value);
		},

		changeSingleCheckbox: function(event) {
			var input = event.currentTarget;
			this.model.setVal(input.name, input.checked);
		},

		changeGroupedCheckbox: function(event) {
			var input = event.currentTarget;
			var $checked = this.$("input:checkbox[name='" + input.name + "']:checked");
			var values = _.map($checked, function(cbx) { return cbx.value; });
			this.model.setVal(input.name, values);
		},

		changeRadioButtonGroup: function(event) {
			var input = event.currentTarget;
			var $checked = this.$("input:radio[name='" + input.name + "']:checked");
			this.model.setVal(input.name, $checked.val());

			this.changeBoundButtonsState(event);
		},

		changeBoundButtonsState: function(event) {
			var self = this;
			var input = event.currentTarget;


			this.$("li [name=" + input.name + "]").each(function() {
				var dom = this;
				var binding = radioBinding(dom);

				if (!_.isUndefined(binding)) {
					var op = (input === dom && input.checked) ? "enable" : "disable";
					self.setBoundButtonState(op, binding);
				}
			});
		},

		setBoundButtonState: function(operation, binding) {
			ButtonUtil[operation].call(this, $("#" + binding + "-open"));
		},

		openTreePicker: function(event) {
			var target = event.currentTarget;
			var dialogId = $(target).data("idopened");
			$("#" + dialogId).formDialog("open"); // $() instead of this.$() because dialog was removed from its location
		},

		openProjectPicker: function(event) {
			var target = event.currentTarget;
			var dialogId = $(target).data("idopened");
			this.projectPickers[dialogId].open();
		},

		/**
		 * /!\ this is a jeditable handler builder
		 */
		onDatePicked: function(self) {
			/**
			 * /!\ this is a jeditable handler. "this" is the picker's original dom
			 */
			return function(localizedDate, settings) {
				var origDom = this;
				var postDate;

				if (localizedDate === "" || $(origDom).data("nodate") === localizedDate) {
					postDate = postNoDate;
				} else {
					postDate = reformatDate(settings.datepicker.dateFormat, postDateFormat)(localizedDate);
				}

				self.model.setVal(origDom.id, postDate);

				return localizedDate;
			};
		},

		/**
		 * /!\ this is a treee picker handler builder
		 */
		onNodesPicked: function(self) {
			/**
			 * This is a treee picker handler, "this" is bound to picker dialog.
			 */
			return function() {
				var $picker = $(this);
				$picker.formDialog("close");
				var $tree = $picker.find(".rpt-tree-crit");
				var nodes = $tree.jstree("get_selected");

				self.setPickedNodes($tree.attr("id"), nodes);
			};
		},

		/**
		 * "this" is bound to the view at init
		 * @param tree
		 * @param nodes
		 */
		setPickedNodes: function(propName, nodes) {
			var val = _.map(nodes || [], function(node) {
				var $node = $(node);
				return {resid: $node.attr("resid"), restype: $node.attr("restype")};
			});

			this.model.setVal(propName, val);
		}

	});

	return ReportCriteriaPanel;
});