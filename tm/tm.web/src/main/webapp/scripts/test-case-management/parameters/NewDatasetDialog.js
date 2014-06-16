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
define([ "jquery", "backbone", "underscore", "app/lnf/Forms", "./NewDatasetModel", "jquery.squash.confirmdialog" ], function($,
		Backbone, _, Forms, NewDatasetModel) {
	var NewDatasetDialog = Backbone.View.extend({
		el : "#add-dataset-dialog",
		paramInputIdPrefix : "add-dataset-paramValue",
		paramRowClass : "parameterRow",
		inputClass : "paramValue",
		initialize : function() {
			this.settings = this.options.settings;
			var self = this;
			this.getAndAddParamterInputs = $.proxy(this._getAndAddParamterInputs, this);
			this.addParamterInputs = $.proxy(this._addParamterInputs, this);
			this.removeParameterInputs = $.proxy(this._removeParameterInputs, this);

			// add parameter value inputs
			this.getAndAddParamterInputs();

			// initialize popup
			this.$el.find("input:text").val("");
			$("span.error-message", $(self.el)).text("");

			this.$el.confirmDialog({
				autoOpen : true
			});

		},

		events : {
			"blur input:text.strprop" : "changeStrProp",
			"blur input:text.paramValue" : "changeParamProp",
			"confirmdialogcancel" : "cancel",
			"confirmdialogclose" : "cancel",
			"confirmdialogvalidate" : "validate",
			"confirmdialogconfirm" : "confirm"
		},

		changeStrProp : function(event) {
			var textbox = event.target;
			this.model.set(textbox.name, _.escape(textbox.value));
		},

		changeParamProp : function(event) {
			var self = this;
			var textbox = event.target;
			var nameLength = textbox.name.lenght;
			var id = parseInt(textbox.name.substring(self.inputClass.length, nameLength), 10);
			this.model.paramValueChanged(id, textbox.value);
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newDataset.cancel");
		},

		confirm : function(event) {
			this.cleanup();
			this.trigger("newDataset.confirm");
		},

		validate : function(event) {
			var res = true, validationErrors = this.model.validateAll();

			Forms.form(this.$el).clearState();

			if (validationErrors !== null) {
				for ( var key in validationErrors) {
					Forms.input(this.$("input[name='" + key + "']")).setState("error", validationErrors[key]);
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
		},

		_getAndAddParamterInputs : function() {
			var self = this;
			$.ajax({
				url : self.settings.basic.testCaseUrl + "/parameters",
				type : "get"
			}).done(self.addParamterInputs);

		},

		_addParamterInputs : function(json) {
			var self = this;
			var content = this.$("table.form-horizontal > tbody");
			// CREATE MODEL
			var paramValues = [];
			for ( var i = 0; i < json.length; i++) {
				paramValues.push([ json[i].id, "" ]);
			}
			this.model = new NewDatasetModel({
				name : "",
				paramValues : paramValues
			}, {
				url : self.settings.basic.testCaseDatasetsUrl + "/new"
			});

			// CREATE INPUTS
			var newTemplate = function(param) {
				var row = $("<tr/>", {
					'class' : 'control-group ' + self.paramRowClass
				});
				// label
				var labelCell = $("<td/>");
				var label = $("<label/>", {
					'class' : 'control-label',
					'for' : self.paramInputIdPrefix + param.id
				});
				label.text(param.name);
				labelCell.append(label);
				row.append(labelCell);
				// input
				var inputCell = $("<td/>", {
					'class' : 'controls'
				});
				var input = $("<input/>", {
					'type' : 'text',
					'class' : self.inputClass,
					'id' : self.paramInputIdPrefix + param.id,
					'name' : self.inputClass + param.id,
					'maxlength' : 255
				});
				input.attr("size", 50);
				inputCell.append(input);
				row.append(inputCell);
				content.append(row);
			};
			for ( var j = 0; j < json.length; j++) {
				var row = newTemplate(json[j]);
			}

		},

		_removeParameterInputs : function() {
			var selector = "tr." + this.paramRowClass;
			this.$(selector).remove();
		},
		cleanup : function() {
			this.$el.addClass("not-displayed");
			this.model = {
				name : ""
			};
			Forms.form(this.$el).clearState();
			this.removeParameterInputs();
			this.$el.confirmDialog("destroy");
		}

	});
	return NewDatasetDialog;
});