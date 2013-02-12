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
				"jquery.squash.messagedialog", "jquery.squash.confirmdialog",
				, "jquery.squash.jeditable" ],
		function($, Backbone, _, StringUtil) {
			var teamMod = squashtm.app.teamMod;
			var TeamModificationView = Backbone.View
					.extend({
						el : "#information-content",
						initialize : function() {

							this.configureTogglePanels();
							this.configureEditables();
							this.configureRenamePopup();
							this.configureButtons();
							//===============toogle buttons=================
							// this line below is here because toggle panel
							// buttons cannot be bound with the 'events'
							// property of Backbone.View.
							// my guess is that the event is bound to the button
							// before it is moved from it's "span.not-displayed"
							// to the toggle panel header.
							// TODO change our way to make toggle panels buttons
							// this.$("#add-user-button").on("click",
							// $.proxy(this.openAddUserPopup, this));
							//=============/toogle buttons===================
							// dialog is moved from DOM when widgetized => we
							// need to store it
							this.confirmDeletionDialog = this.$(
									"#delete-warning-pane").confirmDialog();
							// ...and we cannot use the events hash
							this.confirmDeletionDialog.on(
									"confirmdialogconfirm", $.proxy(
											this.deleteTeam, this));
						},

						events : {

							"click #delete-team-button" : "confirmTeamDeletion"
						},

						confirmTeamDeletion : function(event) {
							this.confirmDeletionDialog.confirmDialog("open");
						},

						deleteTeam : function(event) {
							var self = this;

							$.ajax({
								type : "delete",
								url : document.location.href
							}).done(function() {
								self.trigger("team.delete");
							});

						},

						replacePlaceHolderByValue : function(index, message,
								replaceValue) {
							var pattern = /\{[\d,\w,\s]*\}/;
							var match = pattern.exec(message);
							var pHolder = match[index];
							return message.replace(pHolder, replaceValue);
						},

						configureButtons : function() {
							$.squash.decorateButtons();
						},

						configureTogglePanels : function() {
							var descSettings = {
								initiallyOpen : true,
								title : teamMod.descriptionPanelLabel
							};
							this.$("#team-description-panel").togglePanel(
									descSettings);
							
							var usersSettings = {
								initiallyOpen : true,
								title : teamMod.membersPanelLabel
							};
							this.$("#members-panel").togglePanel(usersSettings);

						},

						configureEditables : function() {
							var settings = {
								url : teamMod.teamUrl,
								ckeditor : {
									customConfig : squashtm.app.contextRoot
											+ "/styles/ckeditor/ckeditor-config.js",
									language : teamMod.richEditLanguageValue
								},
								placeholder : teamMod.richEditPlaceHolder,
								submit : teamMod.richEditsubmitLabel,
								cancel : teamMod.cancelLabel,
								indicator : '<img src="'
										+ squashtm.app.contextRoot
										+ '/scripts/jquery/indicator.gif" alt="processing..." />'

							};

							$('#team-description').richEditable(settings)
									.addClass("editable");
						},

						renameTeam : function() {
							var newNameVal = $("#rename-team-input").val();
							$.ajax({
								type : 'POST',
								data : {
									'value' : newNameVal
								},
								dataType : "json",
								url : teamMod.teamUrl + "/name"

							}).done(function(data) {
								$('#team-name-header').html(data.newName);
								$('#rename-team-popup').dialog('close');
							});
						},

						configureRenamePopup : function() {
							var params = {
								selector : "#rename-team-popup",
								title : teamMod.renameTeamTitle,
								openedBy : "#rename-team-button",
								isContextual : true,
								usesRichEdit : false,
								closeOnSuccess : true,
								buttons : [ {
									'text' : teamMod.renameLabel,
									'click' : this.renameTeam
								}, {
									'text' : teamMod.cancelLabel,
									'click' : this.closePopup
								} ]
							};
							squashtm.popup.create(params);
							$("#rename-team-popup").bind(
									"dialogopen",
									function(event, ui) {
										var name = $.trim($('#team-name-header')
												.text());
										$("#rename-team-input")
												.val($.trim(name));
									});

						},

						closePopup : function() {
							$(this).data("answer", "cancel");
							$(this).dialog('close');
						},

					});
			return TeamModificationView;
		});