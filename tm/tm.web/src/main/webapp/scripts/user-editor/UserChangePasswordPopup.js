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
define(
		[ "jquery", "backbone", "underscore", "app/util/StringUtil",
				"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
				"jquery.squash.datatables", "jquery.squash.oneshotdialog",
				"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ],
		function($, Backbone, _, StringUtil) {
			var UMod = squashtm.app.UMod;
			var UserChangePasswordPopup = Backbone.View
					.extend({
						el : "#password-change-popup",
						initialize : function() {
							self = this;
							var params = {
								selector : "#password-change-popup",
								title : UMod.message.changePasswordPopupTitle,
								openedBy : "#reset-password-button",
								isContextual : true,
								closeOnSuccess : false,
								buttons : [ {
									'text' : UMod.message.confirmLabel,
									'click' : self.submitPassword
								} ],
								width : 420,
							};

							squashtm.popup.create(params);
							$("#password-change-popup").bind("dialogclose",
									self.cleanUp);

						},
						events : {},

						submitPassword : function() {
							self = this;
							if (!self.validatePassword())
								return;

							var oldPassword = $("#oldPassword").val();
							var newPassword = $("#newPassword").val();

							$.ajax({
								url : "${url}",
								type : "POST",
								dataType : "json",
								data : {
									"oldPassword" : oldPassword,
									"newPassword" : newPassword
								},
								success : self.userPasswordSuccess
							});

						},

						// <%-- we validate the passwords only. Note that
						// validation also occurs server side. --%>
						validatePassword : function() {
							// first, clear error messages
							$("#user-account-password-panel span.error-message")
									.html('');

							// has the user attempted to change his password ?

							var oldPassOkay = true;
							var newPassOkay = true;
							var confirmPassOkay = true;
							var samePassesOkay = true;

							if (!isFilled("#oldPassword")) {
								$("span.error-message.oldPassword-error").html(
										UMod.message.oldPassError);
								oldPassOkay = false;
							}

							if (!isFilled("#newPassword")) {
								$("span.error-message.newPassword-error").html(
										UMod.message.newPassError);
								newPassOkay = false;
							}

							if (!isFilled("#user-account-confirmpass")) {
								$(
										"span.error-message.user-account-confirmpass-error")
										.html(UMod.message.confirmPassError);
								confirmPassOkay = false;
							}

							if ((newPassOkay == true)
									&& (confirmPassOkay == true)) {
								var pass = $("#newPassword").val();
								var confirm = $("#user-account-confirmpass")
										.val();

								if (pass != confirm) {
									$("span.error-message.newPassword-error")
											.html(UMod.message.samePassError);
									samePassesOkay = false;
								}
							}

							return ((oldPassOkay) && (newPassOkay)
									&& (confirmPassOkay) && (samePassesOkay));

						},

						isFilled : function(selector) {
							var value = $(selector).val();
							if (value.length == 0) {
								return false;
							} else {
								return true;
							}

						},

						hasPasswdChanged : function() {
							return ((this.isFilled("#oldPassword"))
									|| (this.isFilled("#newPassword")) || (this
									.isFilled("#user-account-confirmpass")));
						},

						userPasswordSuccess : function() {
							this.dialog('close');
							squashtm.notification
									.showInfo(UMod.message.passSuccess);
						},

						cleanUp : function() {
							$("#oldPassword").val('');
							$("#newPassword").val('');
							$("#user-account-confirmpass").val('');

						},
					});
			return UserChangePasswordPopup;
		});