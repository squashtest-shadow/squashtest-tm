/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define([ "jquery", "backbone", "handlebars", "app/util/StringUtil", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, StringUtil) {
	var UMod = squashtm.app.UMod;
	var UserResetPasswordPopup = Backbone.View.extend({
		initialize : function() {
			var self = this;

			var params = {
				selector : "#" + self.options.popupId,
				title : UMod.message.resetPasswordPopupTitle,
				openedBy : "#" + self.options.openerId,
				isContextual : true,
				closeOnSuccess : false,
				buttons : [ {
					'text' : UMod.message.confirmLabel,
					'click' : function() {
						self.submitPassword.call(self);
					}
				} ],
				width : 420
			};

			this.render();
			squashtm.popup.create(params);
			this.$dialog = $("#" + self.options.popupId); // dialog is removed from its original place afterwards
			this.$dialog.bind("dialogclose", self.dialogCleanUp);

		},

		render : function() {
			var source = $("#password-reset-popup-tpl").html();
			var template = Handlebars.compile(source);
			this.$el.html(template({
				popupId : this.options.popupId
			}));

			return this;
		},

		events : {},

		submitPassword : function() {
			var self = this;
			if (!self.validatePassword.call(self)) {
				return;
			}

			var newPassword = this.$dialog.find(".password").val();

			$.ajax({
				url : self.options.url,
				type : self.options.type,
				dataType : "json",
				data : {
					"password" : newPassword
				},
				success : function() {
					self.userPasswordSuccess.call(self);
				}
			});

		},

		// <%-- we validate the passwords only. Note that
		// validation also occurs server side. --%>
		validatePassword : function() {
			// first, clear error messages
			this.$dialog.find(".user-account-password-panel span.error-message").html('');

			// has the user attempted to change his password ?

			var newPassOkay = true;
			var confirmPassOkay = true;
			var samePassesOkay = true;

			if (!this.isFilled(".password")) {
				this.$dialog.find("span.error-message.password-error").html(UMod.message.newPassError);
				newPassOkay = false;
			}

			if (!this.isFilled(".user-account-confirmpass")) {
				this.$dialog.find("span.error-message.user-account-confirmpass-error").html(UMod.message.confirmPassError);
				confirmPassOkay = false;
			}

			if ((newPassOkay) && (confirmPassOkay)) {
				var pass = this.$dialog.find(".password").val();
				var confirm = this.$dialog.find(".user-account-confirmpass").val();

				if (pass != confirm) {
					this.$dialog.find("span.error-message.password-error").html(UMod.message.samePassError);
					samePassesOkay = false;
				}
			}

			return ((newPassOkay) && (confirmPassOkay) && (samePassesOkay));

		},

		isFilled : function(selector) {
			var value = this.$dialog.find(selector).val();
			if (!value.length) {
				return false;
			} else {
				return true;
			}

		},

		hasPasswdChanged : function() {
			return ((isFilled(".password")) || (isFilled(".user-account-confirmpass")));
		},

		userPasswordSuccess : function() {
			this.$dialog.dialog('close');
			squashtm.notification.showInfo(UMod.message.passSuccess);
			this.model.set("hasAuthentication", true);
		},

		/**
		 * context of this method should be the dialog
		 */
		dialogCleanUp : function() {
			var $this = $(this);
			$this.find(".password").val('');
			$this.find(".user-account-confirmpass").val('');
		}

	});
	return UserResetPasswordPopup;
});
