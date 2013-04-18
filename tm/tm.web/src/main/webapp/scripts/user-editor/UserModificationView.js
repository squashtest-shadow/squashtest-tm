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
		[ "jquery", "backbone", "underscore", "jeditable.simpleJEditable", "app/util/StringUtil", 
		  "./UserResetPasswordPopup","./UserPermissionsPanel", "./UserTeamsPanel",
				"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
				"jquery.squash.datatables", "jquery.squash.oneshotdialog",
				"jquery.squash.messagedialog", "jquery.squash.confirmdialog",
				, "jquery.squash.jeditable"],
		function($, Backbone, _, SimpleJEditable, StringUtil, UserResetPasswordPopup, UserPermissionsPanel, UserTeamsPanel ) {
			var UMod = squashtm.app.UMod;
			var UserModificationView = Backbone.View
					.extend({
						el : "#information-content",
						initialize : function() {

							this.configureTogglePanels();
							this.configureEditables();
							new UserResetPasswordPopup();
							new UserPermissionsPanel();
							new UserTeamsPanel();
							this.configureButtons();
						},

						events : {
							"click #delete-user-button" : "deleteUser",
							"change #user-group": "changeUserGroup",
						},
						
						changeUserGroup :function(event) {
							var url = UMod.user.url.changeGroup;
							$.ajax({
								type : 'POST',
								url : url,
								data : "groupId=" + $(event.target).val(),
								dataType : 'json'
							});
						},
						
						deleteUser : function(event) {
							var self = this;
							$.ajax({
								type : 'delete',
								url : UMod.user.url.admin,
								data : {},
								dataType : 'json',
								
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
								title : UMod.message.userInfoPanelTitle,
							};
							this.$("#user-info-panel").togglePanel(
									infoSettings);
						},

						configureEditables : function() {
							this.makeSimpleJEditable("user-login");
							this.makeSimpleJEditable("user-first-name");
							this.makeSimpleJEditable("user-last-name");
							this.makeSimpleJEditable("user-email");
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

					});
			return UserModificationView;
		});