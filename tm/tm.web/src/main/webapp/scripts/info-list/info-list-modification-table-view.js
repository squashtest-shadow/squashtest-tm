/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
define(["jquery", "backbone", "underscore", "squash.basicwidgets", "jeditable.simpleJEditable",
		"workspace.routing", "./NewInfoListItemDialog", "./IconSelectDialog", "squash.translator", "jquery.squash.togglepanel", "squashtable", "jquery.squash.formdialog","jquery.squash", "jqueryui",  "jquery.squash.confirmdialog", "jquery.squash.messagedialog" ], function($, backbone, _, basic,
		SimpleJEditable, routing, NewInfoListItemDialog, IconSelectDialog, translator) {
	"use strict";

	var TableView = Backbone.View.extend({
		el : "#table-view",
		initialize : function(config) {
			this.config = config;
            this.initErrorPopup();
			this.tableInit();
			this.configureDeleteInfoListItemPopup();
			this.configureChangeLabelPopup();
			this.configureChangeCodePopup();
			this.$("#add-info-list-item-button").on("click", $.proxy(this.openAddItemPopup, this));
		},
		events : {
		"click .isDefault>input:checkbox" : "changeDefaultOption",
		"click td.opt-label" : "openChangeLabelPopup",
		"click td.opt-code" : "openChangeCodePopup",
		"click td.sq-icon" : "openChangeIconPopup",
		"click td.delete-button" : "openDeleteOptionPopup"
		},
		initErrorPopup : function(){
			this.errorPopup = $("#generic-error-dialog").messageDialog();
		},
		tableInit : function() {

			this.optionsTable = this.$("#info-list-item-table");
			var self = this;

			var squashSettings = {

					enableDnD : true,

					functions : {
						dropHandler : function(dropData) {
							var url = routing.buildURL('info-list.position', self.config.data.infoList.id);
							$.post(url,	dropData, function() {
								self.optionsTable._fnAjaxUpdate();
							});
						},
						drawIcon : function(value, cell){
							if (value !== "noicon"){
								cell.addClass("sq-icon");
								cell.html('<span class="sq-icon sq-icon-' + value + '"></span>');
								} else {
									//if there is no icon name display [None]
									cell.addClass("sq-icon");
									cell.text(translator.get("label.infoListItems.icon.none"));
								}
						}
					}

				};
			this.optionsTable.squashTable({"bServerSide" : false}, squashSettings);
		},

		changeDefaultOption : function(event) {

			var self = this;
			var checkbox = event.currentTarget;

			if (!checkbox.checked) {
				checkbox.checked = true;
				notification.showError("ERROR");
				return;
			}

			var cell = checkbox.parentElement;
			var row = cell.parentElement;
			var data = self.optionsTable.fnGetData(row);

			$.ajax({
				url : routing.buildURL("info-list-item.info", data['entity-id']),
				type : 'POST',
				data : {
					id:'info-list-item-default',
				}
			}).done(function() {
				self.optionsTable.find(".isDefault>input:checkbox").prop("checked", false);
				checkbox.checked = true;
			}).fail(function() {
				checkbox.checked = !checkbox.checked;
			});
		},




		openChangeLabelPopup : function(event) {
			var self = this;
			var labelCell = event.currentTarget;

			var row = labelCell.parentElement;
			var data = this.optionsTable.fnGetData(row);
			var id = data['entity-id'];
			var value = $(labelCell).text();

			self.ChangeLabelPopup.find("#rename-popup-info-list-item-label").val(value);
			self.ChangeLabelPopup.find("#rename-popup-info-list-item-id").val(id);
			self.ChangeLabelPopup.formDialog("open");
		},

		openChangeIconPopup : function(event){

			var self = this;

			var labelCell = event.currentTarget;

			var row = labelCell.parentElement;
			var data = self.optionsTable.fnGetData(row);
			var id = data['entity-id'];
			var iconName = data['iconName'];


			function discard() {
				self.newIconDialog.off("selectIcon.cancel selectIcon.confirm");
				self.newIconDialog.undelegateEvents();
				self.newIconDialog = null;
			}

			function discardAndRefresh(icon) {
				discard();
				$.ajax({
					url : routing.buildURL("info-list-item.info", id),
					type : 'POST',
					data : {
						id:'info-list-item-icon',
						value:icon
					}
				}).done(function() {
					self.optionsTable._fnAjaxUpdate();

				}).fail(function() {

				});
			}

			self.newIconDialog = new IconSelectDialog({ el: "#choose-item-icon-popup",
				model : {
					icon:"sq-icon-"+iconName
				}
			});

			self.newIconDialog.on("selectIcon.cancel", discard);
			self.newIconDialog.on("selectIcon.confirm", discardAndRefresh);
		},

		configureChangeLabelPopup : function() {
			var self = this;

			var dialog = $("#rename-info-list-item-popup");
			this.ChangeLabelPopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function(){
				self.changeLabel.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);

		},

		changeLabel : function() {

			var self = this;
			var id = self.ChangeLabelPopup.find("#rename-popup-info-list-item-id").val();
			var newValue = self.ChangeLabelPopup.find("#rename-popup-info-list-item-label").val();

			$.ajax({
				type : 'POST',
				data : {
					id:'info-list-item-label',
					'value' : newValue
				},
				url : routing.buildURL("info-list-item.info", id),
			}).done(function() {
				self.optionsTable._fnAjaxUpdate();
				self.ChangeLabelPopup.formDialog('close');
			});
		},

		openDeleteOptionPopup : function (event){
			var self = this;
			var cell = event.currentTarget;

			var row = cell.parentElement;
			var data = self.optionsTable.fnGetData(row);
			var id = data['entity-id'];

			$.ajax({
				type : 'GET',
			url : routing.buildURL('info-list.defaultItem',self.config.data.infoList.id)
			}).done(function(defaultItemId) {

				if (defaultItemId === id){
					self.errorPopup.find('.generic-error-main').html(translator.get("dialog.delete.info-list-item.isDefault"));
					self.errorPopup.messageDialog('open');
				} else {

					var message = $("#delete-info-list-item-warning");
					$.ajax({
						type : 'GET',
					url : routing.buildURL('info-list-item.isUsed',id)
					}).done(function(isUsed) {

						if (isUsed === true){
							message.text(translator.get("dialog.delete.info-list-item.used.message"));
						} else {
							message.text(translator.get("dialog.delete.info-list-item.unused.message"));
						}
						self.DeleteInfoListItemPopup.find("#delete-info-list-item-popup-info-list-item-id").val(id);
						self.DeleteInfoListItemPopup.formDialog("open");

					});
				}
			});
		},

		configureDeleteInfoListItemPopup : function(){
			var self = this;

			var dialog = $("#delete-info-list-item-popup");
			this.DeleteInfoListItemPopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function(){
				self.deleteInfoListItem.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);
		},
		deleteInfoListItem : function(){
			var self = this;
			var id = self.DeleteInfoListItemPopup.find("#delete-info-list-item-popup-info-list-item-id").val();

			$.ajax({
				type : 'DELETE',
				url : routing.buildURL("info-list-item.delete", self.config.data.infoList.id, id),
			}).done(function(data) {
				self.optionsTable._fnAjaxUpdate();
				self.DeleteInfoListItemPopup.formDialog('close');
			});


		},
		openChangeCodePopup : function(event) {
			var self = this;
			var codeCell = event.currentTarget;

			var row = codeCell.parentElement;
			var data = self.optionsTable.fnGetData(row);
			var id = data['entity-id'];
			var value = $(codeCell).text();

			self.ChangeCodePopup.find("#change-code-popup-info-list-item-code").val(value);
			self.ChangeCodePopup.find("#change-code-popup-info-list-item-id").val(id);
			self.ChangeCodePopup.formDialog("open");
		},

		configureChangeCodePopup : function() {
			var self = this;

			var dialog = $("#change-code-info-list-item-popup");
			this.ChangeCodePopup = dialog;

			dialog.formDialog();

			dialog.on('formdialogconfirm', function(){
				self.changeCode.call(self);
			});

			dialog.on('formdialogcancel', this.closePopup);

		},

		changeCode : function() {
			var self = this;
			var id = self.ChangeCodePopup.find("#change-code-popup-info-list-item-id").val();
			var newValue = self.ChangeCodePopup.find("#change-code-popup-info-list-item-code").val();

			$.ajax({
				type : 'POST',
				data : {
					id:'info-list-item-code',
					'value' : newValue
				},
				url : routing.buildURL("info-list-item.info", id),
			}).done(function() {
				self.optionsTable._fnAjaxUpdate();
				self.ChangeCodePopup.formDialog('close');
			});
		},
		closePopup : function() {
			$(this).formDialog('close');
		},

		openAddItemPopup : function() {

			var self = this;

			function discard() {
				self.newItemDialog.off("newOption.cancel newOption.confirm");
				self.newItemDialog.undelegateEvents();
				self.newItemDialog = null;
			}

			function discardAndRefresh() {
				discard();
				self.optionsTable._fnAjaxUpdate();
			}

			self.newItemDialog = new NewInfoListItemDialog({
				model : {
					"listId" : self.config.data.infoList.id
				}
			});

			self.newItemDialog.on("newOption.cancel", discard);
			self.newItemDialog.on("newOption.confirm", discardAndRefresh);

		}

	});
	return TableView;

});