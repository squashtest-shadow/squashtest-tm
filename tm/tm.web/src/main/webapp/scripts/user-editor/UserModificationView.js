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
define([ "jquery", "backbone", "underscore", "jeditable.simpleJEditable", "app/util/StringUtil",
		"./UserResetPasswordPopup", "./UserPermissionsPanel", "./UserTeamsPanel", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "squashtable", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog", "jquery.squash.jeditable" ], function($, Backbone, _,
		SimpleJEditable, StringUtil, UserResetPasswordPopup, UserPermissionsPanel, UserTeamsPanel) {
	var UMod = squashtm.app.UMod;
	var UserModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {

			this.configureTogglePanels();
			this.configureEditables();
			this.configureDeletionDialog();
			
			this.model = new Backbone.Model({
				hasAuthentication : UMod.user.hasAuthentication
			});
			
			this.confirmDeleteUserDialog = $("#delete-user-dialog").confirmDialog();

			if (this.model.get("hasAuthentication")) {
				this.resetPasswordPopup = this.createResetPasswordPopup();

			} else {
				this.createAuthPopup = new UserResetPasswordPopup({
					el : "#auth-pop-pane",
					popupId : "create-auth-popup",
					openerId : "create-auth-button",
					url: UMod.user.url.admin + "authentication", 
					type: "put",
					model : this.model
				});

			}

			new UserPermissionsPanel();
			new UserTeamsPanel();
			this.configureButtons();

			this.listenTo(this.model, "change:hasAuthentication", this.onChangeHasAuthentication);
			//apparently listenTo on the confirmDeleteUser has problems so we're using regular jquery.
			this.confirmDeleteUserDialog.on('confirmdialogconfirm', $.proxy(this.deleteUser, this));
		},

		events : {
			"click #delete-user-button" : "confirmUserDeletion",
			"change #user-group" : "changeUserGroup"
		},

		confirmUserDeletion : function(event) {
			this.confirmDeletionDialog.confirmDialog("open");
		},
		
		changeUserGroup : function(event) {
			var url = UMod.user.url.changeGroup;
			$.ajax({
				type : 'POST',
				url : url,
				data : "groupId=" + $(event.target).val(),
				dataType : 'json'
			});
		},
		
		confirmDeleteUser : function(evt){
			this.confirmDeleteUserDialog.confirmDialog('open');			
		},

		deleteUser : function(event) {
			var self = this;
			$.ajax({
				type : 'delete',
				url : UMod.user.url.admin,
				data : {},
				dataType : 'json'

			}).done(function() {
				self.trigger("user.delete");
			});

		},

		configureButtons : function() {
			$.squash.decorateButtons();
		},

		configureTogglePanels : function() {
			var infoSettings = {
				initiallyOpen : true,
				title : UMod.message.userInfoPanelTitle
			};
			this.$("#user-info-panel").togglePanel(infoSettings);
		},

		configureEditables : function() {
			this.makeSimpleJEditable("user-login");
			this.makeSimpleJEditable("user-first-name");
			this.makeSimpleJEditable("user-last-name");
			this.makeSimpleJEditable("user-email");
		},

		configureDeletionDialog : function() {
			this.confirmDeletionDialog = $("#delete-warning-pane").confirmDialog();
			this.confirmDeletionDialog.on("confirmdialogconfirm", $.proxy(this.deleteUser, this));
		},
		
		makeSimpleJEditable : function(imputId) {
			new SimpleJEditable({
				language : {
					richEditPlaceHolder : UMod.message.richEditPlaceHolder,
					okLabel : UMod.message.okLabel,
					cancelLabel : UMod.message.cancelLabel
				},
				targetUrl : UMod.user.url.admin,
				componentId : imputId,
				jeditableSettings : {}
			});
		},
		
		createResetPasswordPopup: function() {
			return new UserResetPasswordPopup({
				el: "#pass-pop-pane",
				popupId: "password-reset-popup",
				openerId: "reset-password-button",
				url: UMod.user.url.admin, 
				type: "post",
				model: this.model
			});
		},

		onChangeHasAuthentication : function() {
			if (this.model.get("hasAuthentication")) {
				this.resetPasswordPopup = this.resetPasswordPopup || this.createResetPasswordPopup();
				
				this.$("#reset-password-button").removeClass("not-displayed");
				this.$("#create-auth-button").addClass("not-displayed");

				if (this.createAuthPopup) {
					this.createAuthPopup.remove();
				}

			} // the other way is not possible
		}
	});
	return UserModificationView;
});