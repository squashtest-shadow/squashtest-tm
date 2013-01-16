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
define([ "jquery", "backbone", "./TestStepInfoModel", "jquery.squash",
		"jqueryui", "jquery.squash.togglepanel", "jquery.ckeditor",
		"jeditable", "ckeditor", "jeditable.ckeditor",
		"jquery.squash.jeditable", "jquery.squash.squashbutton",
		"datepicker/require.jquery.squash.datepicker-locales", ], function($,
		Backbone, TestStepInfoModel) {
	var editTCS = squashtm.app.editTCS;
	/*
	 * Defines the controller for the custom fields table.
	 */
	var TestStepModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {
			this.configureTogglePanels();
			this.configureCKEs();
			this.configureButtons();
			this.configureCUFs();

			this.initializeModel();

		},
		initializeModel : function() {
			var modelAttributes = {};
			this.fillModelAttributes(modelAttributes);
			this.model = new TestStepInfoModel(modelAttributes);
		},
		fillModelAttributes : function(modelAttributes) {
			var self = this;
			var fields = this.$(".test-step-attr");
			$.each(fields, function(index, value) {
				modelAttributes[$(value).attr("name")] = $(value).val();
			});

			var cufValuesValues = this.$(".cuf-value-control");
			if (cufValuesValues.length > 0) {
				modelAttributes.cufValues = {};
				$.each(cufValuesValues, function(index, value) {
					modelAttributes.cufValues[$(value).attr("name")] = self
							.getInputValue(value);
				});
			}
		},
		configureButtons : function() {
			this.$("#next-test-step-button").squashButton({
				disabled : editTCS.nextId == -1,
				text : false,
				icons : {
					primary : "ui-icon-triangle-1-e",
				}
			});

			this.$("#previous-test-step-button").squashButton({
				disabled : editTCS.previousId == -1,
				text : false,
				icons : {
					primary : "ui-icon-triangle-1-w",
				}
			});

			$.squash.decorateButtons();

		},

		configureTogglePanels : function() {
			var informationSettings = {
				initiallyOpen : true,
				title : editTCS.informationPanelLabel
			};
			this.$("#test-step-info-panel").togglePanel(informationSettings);
		},
		configureCUFs : function() {
			var dateSettings = {
				dateFormat : editTCS.localizedDateFormat
			};
			var widgets = this.$(".cuf-value-control");
			var count = widgets.length;
			for ( var i = 0; i < count; i++) {
				var widget = widgets.eq(i);
				var conf = editTCS.CUFsettings[i];

				if (conf.type === 'datepicker') {
					widget.datepicker(dateSettings);
				} else if (conf.type === 'select') {
					for ( var val in conf.data) {
						if (val === 'selected') {
						} else {
							var option = $('<option />', {
								value : val,
								text : conf.data[val]
							});
							if (conf.data[val] === conf.data.selected) {
								option.attr("selected", "");
							}
							option.appendTo(widget);
						}
					}
				}

			}
		},

		configureCKEs : function() {
			var self = this;
			var textareas = this.$el.find("textarea");
			textareas.each(function() {
				$(this).ckeditor(function() {
				}, squashtm.app.ckeditorSettings.ckeditor);
				
				CKEDITOR.instances[$(this).attr("id")].on('change',
						function(e) {
					self.updateCKEModelAttr.call(self, e);
				});
			});
		},

		events : {
			"click #previous-test-step-button" : "goPrevious",
			"click #next-test-step-button" : "goNext",
			// "change .test-step-attr" : "updateCKEModelAttr", 
			// did not work because of _CKE instances (cf method configureCKEs to see how manual binding is done.
			"change .cuf-value-control" : "updateModelCufAttr",
			"click #save-test-step-button" : "saveStep",
		},

		goPrevious : function(event) {
			document.location.href = squashtm.app.contextRoot + "test-steps/"
					+ editTCS.previousId;
		},
		goNext : function(event) {
			document.location.href = squashtm.app.contextRoot + "test-steps/"
					+ editTCS.nextId;

		},
		updateCKEModelAttr : function(event) {
			var attrInput = event.sender;
			var attrName = attrInput.element.$.getAttribute("name");
			var attrValue = attrInput.getData();
			this.model.set(attrName, attrValue);
		},
		updateModelCufAttr : function(event) {
			var input = event.target;
			var name = $(input).attr("name");
			var value = this.getInputValue(input);
			var cufValues = this.model.get("cufValues");
			cufValues[name] = value;
			this.model.set({
				'cufValues' : cufValues
			});
		},
		getInputValue : function(input) {
			if ($(input).hasClass("hasDatepicker")) {
				var date = $(input).datepicker("getDate");
				return $.datepicker.formatDate($.datepicker.ATOM, date);
			} else if ($(input).attr("type") === "checkbox") {
				return $(input).is("checked");
			} else {
				return $(input).val();
			}
		},
		saveStep : function(event) {
			this.model.save();
		},

	});
	return TestStepModificationView;
});