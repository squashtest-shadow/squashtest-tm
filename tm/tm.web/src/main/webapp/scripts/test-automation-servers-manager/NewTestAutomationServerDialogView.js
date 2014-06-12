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
define([ "jquery", "backbone", "handlebars", "app/lnf/Forms", "jquery.squash.confirmdialog", "jquery.ckeditor",
		"datepicker/jquery.squash.datepicker-locales" ], function($, Backbone, Handlebars, Forms) {
	/*
	 * Defines the controller for the new test automation panel.
	 */
	var NewTestAutomationServerDialogView = Backbone.View.extend({
		el : "#new-test-automation-server-popup",
		initialize : function() {
			var self = this;
			var model = this.model;
			this.configureCKEs();
			this.$("input:text.strprop").each(function() {
				var self = this;
				self.value = model.get(self.name);
			});
			this.$("input:checkbox[name='manualSlaveSelection']")[0].checked = model.get("manualSlaveSelection");

			this.$el.confirmDialog({
				autoOpen : true,
				close : function() {
					self.cancel.call(self);
				}
			});

		},

		events : {
			// textboxes with class .strprop are bound to the
			// model prop which name matches the textbox name
			"blur input:text.strprop" : "changeStrProp",
			"blur input:password.strprop" : "changeStrProp",
			// "change textarea" : "updateCKEModelAttr",
			// did not work because of _CKE instances (cf method
			// configureCKEs to see how manual binding is done.
			"click input:checkbox[name='manualSlaveSelection']" : "changeManualSlaveSelection",
			"confirmdialogcancel" : "cancel",
			"confirmdialogvalidate" : "validate",
			"confirmdialogconfirm" : "confirm",
		},

		changeStrProp : function(event) {
			var textbox = event.target;
			this.model.set(textbox.name, textbox.value);
		},

		changeManualSlaveSelection : function(event) {
			this.model.set("manualSlaveSelection", event.target.checked);
		},

		cancel : function(event) {
			this.cleanup();
			this.trigger("newtestautomationserver.cancel");
		},

		confirm : function(event) {
			this.cleanup();
			this.trigger("newtestautomationserver.confirm");
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

			return res;
		},

		cleanup : function() {
			this.$el.addClass("not-displayed");
			Forms.form(this.$el).clearState();
			this.$el.confirmDialog("destroy");
		},

		configureCKEs : function() {
			var self = this;
			var textareas = this.$el.find("textarea");
			textareas.each(function() {
				$(this).ckeditor(function() {
				}, squashtm.app.ckeditorSettings.ckeditor);

				CKEDITOR.instances[$(this).attr("id")].on('change', function(e) {
					self.updateCKEModelAttr.call(self, e);
				});
			});
		},
		updateCKEModelAttr : function(event) {
			var attrInput = event.sender;
			var attrName = attrInput.element.$.getAttribute("name");
			var attrValue = attrInput.getData();
			this.model.set(attrName, attrValue);
		}

	});

	return NewTestAutomationServerDialogView;
});