/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
define([ "jquery", "backbone", "handlebars", "app/lnf/SquashDatatablesLnF" ], function($, Backbone, Handlebars, SD) {
	/*
	 * Defines the controller for the new custom field panel.
	 */
	var NewCustomFieldPanelView = Backbone.View.extend({
		el: "#new-cf-pane",
		initialize: function() {
			var model = this.model;

			this.defaultValueField = this.$("input:text[name='defaultValue']"); 

			this.$("input:text.strprop").each(function() {
				var self = this;				
				self.value = model.get(self.name);
			});
			this.$("input:checkbox[name='optional']").get()[0].checked = model.get("optional");
			this.$("select[name='inputType']").val(model.get("inputType"));

			this.$("input:button").button();

			this.render();
			this.$el.removeClass("not-displayed");
		}, 
		render: function() {
			var inputType = this.model.get("inputType");
			var source   = $("#" + this.model.get("inputType") + "-default-tpl").html();
			var template = Handlebars.compile(source);
			this.$("#default-value-pane").html(template(this.model.toJSON()));

			if (inputType === "DROPDOWN_LIST") {
				this.renderOptionsTable();
			}
			return this;
		},
		events: {
			// textboxes with class .strprop are bound to the model prop which name matches the textbox name
			"change input:text.strprop": "changeStrProp", 
			"change select.optprop": "changeOptProp", 
			"change select[name='inputType']": "changeInputType", 
			"click input:checkbox[name='optional']": "changeOptional",
			"click .cancel": "cancel",
			"click .confirm": "confirm", 
			"click .add-option": "addOption", 
			"click .remove-row>button": "removeOption", 
			"click .is-default>input:checkbox": "changeDefaultOption" 
		},
		changeStrProp: function(event) {
			var textbox = event.target;

			this.model.set(textbox.name, textbox.value);
		},
		changeOptProp: function(event) {
			var option = event.target;

			this.model.set(option.name, option.value);
		},
		changeInputType: function(event) {
			var model = this.model;

			model.set("inputType", event.target.value);
			model.resetDefaultValue();

			this.render();

		},
		changeOptional: function(event) {
			this.model.set("optional", event.target.checked);
		},

		cancel: function(event) {
			this.cleanup();
			this.trigger("cancel");
		},
		confirm: function(event) {
			console.log(this.model.save());
			this.cleanup();
			this.trigger("confirm");
		}, 
		cleanup: function() {
			this.$el.addClass("not-displayed");
		},
		renderOptionsTable: function() {
			this.optionsTable = this.$("#options-table");
			this.optionsTable.dataTable({
				"oLanguage": {
					"sUrl": squashtm.app.cfTable.languageUrl
				},
				"bJQueryUI": true,
				"bFilter": false,
				"bPaginate": false,
				"bProcessing": false,
				"bServerSide": false,
				"bDeferRender": true,
				"bRetrieve": false,
				"bSort": false,
				"aaSorting": [],
				"fnRowCallback": this.decorateOptionRow(this), 
				"fnDrawCallback": this.decorateOptionsTable,
				"aoColumnDefs": [ {
					"aTargets": [ 0 ], "sClass": "option"
				}, {
					"aTargets": [ 1 ], "sClass": "is-default", "sWidth": "2em"
				}, {
					"aTargets": [ 2 ], "sClass": "remove-row", "sWidth": "2em"
				} ]
			});

			this.$("input:button").button();

		}, 
		addOption: function() {
			var optionInput = this.$("input[name='new-option']"), 
				errorSpan = this.$("#new-option-error");
			
			var option = optionInput.val();

			if (this.model.addOption(option)) {
				this.optionsTable.dataTable().fnAddData([ option, false, "" ]);
				optionInput.val("");
				errorSpan.hide();
				
			} else {
				errorSpan.fadeIn("slow", function() { $(this).removeClass("not-displayed"); });
				
			}
		}, 
		removeOption: function(event) {
			// target of click event is a <span> inside of <button>, so we use currentTarget
			var button = event.currentTarget, 
				option = button.value, 
				row = $(button).parents("tr")[0];
			
			this.model.removeOption(option);
			this.optionsTable.dataTable().fnDeleteRow(row);
			
		},
		changeDefaultOption: function(event) {
			var checkbox = event.currentTarget, 
				option = checkbox.value,
				defaultValue = checkbox.checked ? option : "", 
				uncheckSelector = ".is-default>input:checkbox" + (checkbox.checked ? "[value!='" + option + "']" : "");
						
			this.model.set("defaultValue", defaultValue);				
			this.optionsTable.find(uncheckSelector).attr("checked", false);
			
		},
		/**
		 * returns the function which should be used as a callback.
		 */
		decorateOptionRow: function(self) { 
			return function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				var row = $(nRow), 
				defaultCell = row.find(".is-default"), 
				removeCell = row.find(".remove-row"),
				option = aData[0],
				checked = option === self.model.get("defaultValue"),
				tplData = { option: option, checked: checked };

				var source   = $("#remove-cell-tpl").html();
				var template = Handlebars.compile(source);
				removeCell.html(template(tplData));

				source   = $("#default-cell-tpl").html();
				template = Handlebars.compile(source);
				defaultCell.html(template(tplData));
			};
		}, 
		decorateOptionsTable: function() {
			SD.deleteButton($(this).find(".remove-row>button"));
		}
	});

	return NewCustomFieldPanelView;
});