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
define(
		[ "jquery", "backbone", "handlebars", "app/lnf/SquashDatatablesLnF", "app/lnf/Forms", "squash.configmanager",
				"jquery.squash.formdialog", "datepicker/jquery.squash.datepicker-locales", "jquery.squash.tagit" ],
		function($, Backbone, Handlebars, SD, Forms, confman) {
			/*
			 * Defines the controller for the new custom field panel.
			 */
			var NewCustomFieldPanelView = Backbone.View
					.extend({
						el : "#new-cf-pane",
						defaultWidth : 600,
						richWidth : 1000,
						initialize : function() {
							var self = this;
							var model = this.model;
							$.datepicker.setDefaults($.datepicker.regional[squashtm.app.locale]);
							this.defaultValueField = this.$("input:text[name='defaultValue']");

							this.$("input:text.strprop").each(function() {
								var self = this;
								self.value = model.get(self.name);
							});
							this.$("input:checkbox[name='optional']").get()[0].checked = model.get("optional");
							this.$("select[name='inputType']").val(model.get("inputType"));

							this.$("input:button").button();
							
							this.$("#add-cf").button();
							
							// Icons don't work with an <input ... /> , but work with a <button />
							
				/*			$("input:button").button({
		            icons: {
		                primary: 'ui-icon-trash'
		            },
		            text: "toto"
		        });*/
						/*
							$("#add-cf").button({
									icons: {
										primary: 'ui-icon ui-icon-arrowthick-1-n'
									},
									label: "Ajouter "}) ;*/
							
							
							this.render();
							this.$el.formDialog({
								autoOpen : true,
								close : function() {
									self.cancel.call(self);
								}
							});
							this._resize();

						},

						render : function() {
							var inputType = this.model.get("inputType");
							var source = $("#" + inputType + "-default-tpl").html();
							var template = Handlebars.compile(source);
							this.$("#default-value-pane").html(template(this.model.toJSON()));
							
							// in case the input type was previously rich text.
							confman.destroyCkeditor("#defaultValue");
							
							switch (inputType) {
							case "DROPDOWN_LIST":
								this.renderOptionsTable();
								this.renderOptional(true);
								break;
							case "CHECKBOX":
								this.renderOptional(false);
								break;
							case "PLAIN_TEXT":
								this.renderOptional(true);
								break;
							case "DATE_PICKER":
								this.renderOptional(true);
								$("#defaultValue").datepicker({
									dateFormat : squashtm.app.localizedDateFormat
								});
								break;								
							case "RICH_TEXT" :
								this.renderOptional(true);
								var conf = confman.getStdCkeditor();
								$("#defaultValue").ckeditor(function(){}, conf);
								// the following reroute the blur event from the ckeditor and relocate it as thrown by the textarea 
								CKEDITOR.instances["defaultValue"].on('change', function(){
									$("#defaultValue").trigger('change');
								});
								break;
							case "TAG" :
								this.renderOptional(true);
								var tagconf = confman.getStdTagit();
								$.extend(true, tagconf, {
									validate :  function(label){
										if (label.indexOf("|") !== -1){
											$("#defaultValue").trigger('invalidtag');
											return false;
										}
										else{
											return true;
										}
									},									
									afterTagAdded:  function(event, ui){
										$("#defaultValue").trigger('change');
									},
									afterTagRemoved:  function(event, ui){
										$("#defaultValue").trigger('change');
									}
								});
								$("#defaultValue").squashTagit(tagconf);
								break;
							}
							this._resize();
							return this;
						},
						
						_resize : function(){
							if (this.$el.data().formDialog !== undefined){
								var type = this.model.get("inputType");
								var width = (type === "RICH_TEXT") ? this.richWidth : this.defaultWidth;
								this.$el.formDialog("option", "width", width);
							}
						},

						events : {
							// textboxes with class .strprop are bound to the
							// model prop which name matches the textbox name
							"blur input:text.strprop" : "changeStrProp",
							"change select.optprop" : "changeOptProp",
							"change input:text.dateprop" : "changeDateProp",
							"change textarea.richprop" : "changeRichProp",
							"change ul.tagprop" : "changeTagProp",
							"invalidtag ul.tagprop" : "invalidTag",
							"change select[name='inputType']" : "changeInputType",
							"click input:checkbox[name='optional']" : "changeOptional",
							"formdialogcancel" : "cancel",
							"formdialogvalidate" : "validate",
							"formdialogaddanother" : "addanother",
							"formdialogconfirm" : "confirm",
							"click .add-option" : "addOption",
							"click .remove-row>a" : "removeOption",
							"click .is-default>input:checkbox" : "changeDefaultOption"
						},

						changeStrProp : function(event) {
							var textbox = event.target;
							this.model.set(textbox.name, textbox.value);
						},
						changeDateProp : function(event) {
							var textbox = event.target;
							var date = $(textbox).datepicker("getDate");
							var dateToString = $.datepicker.formatDate($.datepicker.ATOM, date);
							this.model.set(textbox.name, dateToString);
						},
						changeOptProp : function(event) {
							var option = event.target;
							this.model.set(option.name, option.value);
						},
						changeRichProp : function(event){
							var area = $("#defaultValue");
							this.model.set(area.attr('id'), area.val());
						},
						changeTagProp : function(event){
							tags = $("#defaultValue").squashTagit("assignedTags").join("|");
							this.model.set("defaultValue", tags);
						},
						changeInputType : function(event) {
							var model = this.model;

							model.set("inputType", event.target.value);
							model.resetDefaultValue();

							this.render();
						},
						

						changeOptional : function(event) {
							this.model.set("optional", event.target.checked);
						},

						cancel : function(event) {
							this.cleanup();
							this.trigger("newcustomfield.cancel");
						},

						confirm : function(event) {
							this.cleanup();
							this.trigger("newcustomfield.confirm");
						},
						
						invalidTag : function(event) {
							var res = true, invalidTag = this.model.invalidTag();
							Forms.form(this.$el).clearState();
							Forms.input(this.$("ul[name='" + "defaultValue" + "']")).setState("error", invalidTag["tagCode"]);
							return false;
						},
						
						addanother : function(event) {
							var res = true, validationErrors = this.model.validateAll();

							Forms.form(this.$el).clearState();

							if (validationErrors !== null) {
								for ( var key in validationErrors) {
									Forms.input(this.$("input[name='" + key + "']")).setState("error",
											validationErrors[key]);
								}

								return false;
							}

							this.model.save(null, {
								async : false,
								error : function() {
									res = false;
									event.preventDefault();
								}
							});
							$('#cf-table').squashTable().refresh();
							this._resetForm();
							return res;
						},
						
						validate : function(event) {
							var res = true, validationErrors = this.model.validateAll();

							Forms.form(this.$el).clearState();

							if (validationErrors !== null) {
								for ( var key in validationErrors) {
									Forms.input(this.$("input[name='" + key + "']")).setState("error",
											validationErrors[key]);
								}

								return false;
							}

							this.model.save(null, {
								async : false,
								error : function() {
									res = false;
									event.preventDefault();
								}
							});
							this.$el.addClass("not-displayed");
							this.$el.formDialog("close");
							$('#cf-table').squashTable().refresh();
							return res;
						},

						cleanup : function() {
							this.$el.addClass("not-displayed");
							Forms.form(this.$el).clearState();
							this.$el.formDialog("destroy");
						},

						renderOptional : function(show) {
							var renderPane = this.$("#optional-pane");
							if (show) {
								renderPane.show();
							} else {
								renderPane.hide();
							}
						},
						renderOptionsTable : function() {
							this.optionsTable = this.$("#options-table");
							this.optionsTable.dataTable({
								"oLanguage" : {
									"sUrl" : squashtm.app.cfTable.languageUrl
								},
								"bAutoWidth" : false,
								"bJQueryUI" : true,
								"bFilter" : false,
								"bPaginate" : false,
								"bServerSide" : false,
								"bDeferRender" : true,
								"bRetrieve" : false,
								"bSort" : false,
								"aaSorting" : [],
								"fnRowCallback" : this.decorateOptionRow(this),
								"fnDrawCallback" : this.decorateOptionsTable,
								"aoColumnDefs" : [ {
									"aTargets" : [ 0 ],
									"sClass" : "option"
								}, {
									"aTargets" : [ 1 ],
									"sClass" : "code"
								}, {
									"aTargets" : [ 2 ],
									"sClass" : "is-default"
								}, {
									"aTargets" : [ 3 ],
									"sClass" : "remove-row"
								} ]
							});

							this.$("input:button").button();
							
							this.$("#add-cf").button();

						},

						_resetForm : function() {
							this.$textFields = this.$el.find("input:text");
							this.$textFields.val("");
							this.$textAreas.val("");
							this.$errorMessages.text("");
							Forms.form(this.$el).clearState();
						},
						
						addOption : function() {
							var optionLabelInput = Forms.input(this.$("input[name='new-option-label']"));
							var optionLabel = optionLabelInput.$el.val();

							var optionCodeInput = Forms.input(this.$("input[name='new-option-code']"));
							var optionCode = optionCodeInput.$el.val();

							try {
								this.model.addOption([ optionLabel, optionCode ]);

								this.optionsTable.dataTable().fnAddData([ optionLabel, optionCode, false, "" ]);

								optionCodeInput.clearState();
								optionCodeInput.$el.val("");
								optionLabelInput.clearState();
								optionLabelInput.$el.val("");

							} catch (ex) {
								if (ex.name === "ValidationException") {
									if (ex.validationErrors.optionLabel) {
										optionLabelInput.setState("error", ex.validationErrors.optionLabel);
									}
									if (ex.validationErrors.optionCode) {
										optionCodeInput.setState("error", ex.validationErrors.optionCode);
									}
								}
							}

						},

						removeOption : function(event) {
							// target of click event is a <span> inside of
							// <button>, so we use currentTarget
							var button = event.currentTarget, $button = $(button), option = $button.data("value"), row = $button
									.parents("tr")[0];

							this.model.removeOption(option);
							this.optionsTable.dataTable().fnDeleteRow(row);

						},

						changeDefaultOption : function(event) {
							var checkbox = event.currentTarget, option = checkbox.value, defaultValue = checkbox.checked ? option
									: "", uncheckSelector = ".is-default>input:checkbox" +
									(checkbox.checked ? "[value!='" + option + "']" : ""), optionsInput = Forms
									.input(this.$("input[name='options']"));

							optionsInput.clearState();

							if (this.model.get("optional") === false && checkbox.checked === false) {
								event.preventDefault();
								optionsInput.setState("warning", "message.defaultOptionMandatory");
								return;
							}

							this.model.set("defaultValue", defaultValue);
							this.optionsTable.find(uncheckSelector).attr("checked", false);

						},

						/**
						 * returns the function which should be used as a callback.
						 */
						decorateOptionRow : function(self) {
							return function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
								var row = $(nRow), 
									defaultCell = row.find(".is-default"), 
									removeCell = row.find(".remove-row"), 
									option = aData[0], 
									checked = option === self.model.get("defaultValue"),
									tplData = {
										option : option,
										checked : checked
									};

								var source = $("#remove-cell-tpl").html();
								var template = Handlebars.compile(source);
								removeCell.html(template(tplData));

								source = $("#default-cell-tpl").html();
								template = Handlebars.compile(source);
								defaultCell.html(template(tplData));
							};
						},

						decorateOptionsTable : function() {
							SD.deleteButton($(this).find(".remove-row>a"));
						}
					});

			return NewCustomFieldPanelView;
		});