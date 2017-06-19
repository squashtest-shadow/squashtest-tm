/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
define([ 'module',  "jquery", "backbone", "underscore", "squash.basicwidgets", "jeditable.simpleJEditable",
		"workspace.routing", "squash.translator", "app/lnf/Forms", "app/util/StringUtil","jquery.squash.togglepanel", "jquery.squash.formdialog", "squashtable", "app/ws/squashtm.workspace"],
		function(module, $, backbone, _, basic, SimpleJEditable, routing, translator, Forms, StringUtils) {
	"use strict";

	var config = module.config();

	//translator.load([]);

	var reqLinkTypeManagerView = Backbone.View.extend({
		el : "#link-types-table-pane",
		initialize : function() {
			this.basicInit();
			this.config = config;

			this.initTable();

			this.configureNewLinkTypePopup();
			this.configureChangeRolePopup();
			this.configureChangeCodePopup();

			this.initErrorPopup();
			this.configureDeleteTypePopup();

		},
		basicInit : function() {
			basic.init();

		},

		initTable : function() {
			var squashSettings = {
    		searching : true,
    		buttons : [{
    			tooltip : translator.get("label.Delete"),
    			tdSelector : "td.delete-button",
    			uiIcon : "ui-icon-trash",
    			jquery : true
    		}],
    	};
			this.table = $("#requirement-link-types-table").squashTable(
				{
        	//"bServerSide" : false,
        	aaData : config.tableData.aaData
        },
        squashSettings
			);
		},

		events : {
			"click #add-link-type-btn" : "openAddLinkTypePopup",
			"click .isDefault>input:radio" : "changeDefaultType",
			"click td.opt-role1" : "openChangeRole1Popup",
			"click td.opt-role2" : "openChangeRole2Popup",
			"click td.opt-code1" : "openChangeCode1Popup",
			"click td.opt-code2" : "openChangeCode2Popup",
			"click td.delete-button" : "openDeleteTypePopup"
		},

		/* AddNewLinkType Popup functions */
		configureNewLinkTypePopup : function(){
      var self = this;

      var dialog = $("#add-link-type-popup");
      this.AddLinkTypePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.addLinkType.call(self);
      });

    	dialog.on('formdialogcancel', this.closePopup);

    },

		openAddLinkTypePopup : function(){
			var self = this;
			self.clearAddLinkErrorMessages();
			self.AddLinkTypePopup.formDialog("open");
		},

		clearAddLinkErrorMessages : function() {
			Forms.input($("#add-link-type-popup-role1")).clearState();
      Forms.input($("#add-link-type-popup-role1-code")).clearState();
      Forms.input($("#add-link-type-popup-role2")).clearState();
      Forms.input($("#add-link-type-popup-role2-code")).clearState();
		},

		addLinkType : function(){
    	var self = this;

    	// Clearing error messages
			self.clearAddLinkErrorMessages();

    	var newRole1 = self.AddLinkTypePopup.find("#add-link-type-popup-role1").val();
    	var newRole1Code = self.AddLinkTypePopup.find("#add-link-type-popup-role1-code").val();
    	var newRole2 = self.AddLinkTypePopup.find("#add-link-type-popup-role2").val();
    	var newRole2Code = self.AddLinkTypePopup.find("#add-link-type-popup-role2-code").val();

    	var params = {
    					"role1" : newRole1,
    					"role1Code" : newRole1Code,
    					"role2" : newRole2,
    					"role2Code" : newRole2Code
    	};
    	// Verification BLANK
    	var oneInputIsBlank = false;

    	if(StringUtils.isBlank(newRole1))	 {
    		oneInputIsBlank = true;
    		Forms.input($("#add-link-type-popup-role1")).setState("error", translator.get("message.notBlank"));
    	}
    	if(StringUtils.isBlank(newRole1Code))	 {
      	Forms.input($("#add-link-type-popup-role1-code")).setState("error", translator.get("message.notBlank"));
      	oneInputIsBlank = true;
      }
      if(StringUtils.isBlank(newRole2))	 {
      	Forms.input($("#add-link-type-popup-role2")).setState("error", translator.get("message.notBlank"));
      	oneInputIsBlank = true;
      }
      if(StringUtils.isBlank(newRole2Code))	 {
      	Forms.input($("#add-link-type-popup-role2-code")).setState("error", translator.get("message.notBlank"));
      	oneInputIsBlank = true;
      }
			if(!oneInputIsBlank) {
				// Verify if Codes already Exist
				$.ajax({
        	url : routing.buildURL("requirementLinkType.checkCodes"),
          type : 'GET',
          dataType: 'json',
          data : params,
        }).done(function(data) {
        	var oneCodeAlreadyExists = false;
        	if(data.code1Exists) {
          	Forms.input($("#add-link-type-popup-role1-code")).setState("error", translator.get("requirement-version.link.type.rejection.codeAlreadyExists"));
          	oneCodeAlreadyExists = true;
          }
          if(data.code2Exists) {
            Forms.input($("#add-link-type-popup-role2-code")).setState("error", translator.get("requirement-version.link.type.rejection.codeAlreadyExists"));
            oneCodeAlreadyExists = true;
          }
          if (!oneCodeAlreadyExists) {
						self.doAddNewLinkType(params);
          }
        });
			}

    },

		doAddNewLinkType : function(paramLinkType) {
			var self = this;
			$.ajax({
      	url : routing.buildURL("requirementLinkType"),
      	type : 'POST',
      	dataType: 'json',
      	data : paramLinkType,
      }).done(function() {
      		self.table.refresh();
      		self.AddLinkTypePopup.formDialog('close');
      });
		},

		closePopup : function() {
			$(this).formDialog('close');

		},

		/* Change Default Type functions */
		changeDefaultType : function(event) {
			var self = this;
			var radio = event.currentTarget;

			if(!radio.checked) {
				radio.checked = true;
			}

			var cell = radio.parentElement;
			var row = cell.parentElement;
			var data = self.table.fnGetData(row);

			// POST Modification
			$.ajax({
				url : routing.buildURL("requirement.link.type", data["type-id"]),
				type : 'POST',
				data : {
					id : 'requirement-link-type-default'
				}
				}).done(function() {
					self.table.find(".isDefault>input:radio").prop("checked", false);
      		radio.checked = true;
				}).fail(function() {
      		radio.checked = !radio.checked;
				});
		},

		/* Change  Role functions */

		configureChangeRolePopup : function() {
			var self = this;

      var dialog = $("#change-type-role-popup");
      this.ChangeRolePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.changeRole.call(self);
      });

      dialog.on('formdialogcancel', this.closePopup);
		},

		clearChangeRoleErrorMessage : function() {

    	Forms.input($("#change-type-role-popup-role")).clearState();
    },

		openChangeRolePopup : function(event, roleNumber) {
			var self = this;
			// clear error messages
			self.clearChangeRoleErrorMessage();
			// fill label input
			var roleCell = event.currentTarget;

			var row = roleCell.parentElement;
			var data = this.table.fnGetData(row);
			var typeId = data['type-id'];
			var currentRole = $(roleCell).text();

			self.ChangeRolePopup.data('typeId', typeId);
			self.ChangeRolePopup.data('roleNumber', roleNumber);
			self.ChangeRolePopup.formDialog('open');
			self.ChangeRolePopup.find("#change-type-role-popup-role").val(currentRole);
    },

		openChangeRole1Popup : function(event) {

			this.openChangeRolePopup(event, 1);
		},

		openChangeRole2Popup : function(event) {

    	this.openChangeRolePopup(event, 2);
    },

		changeRole : function() {
			var self = this;
			var typeId = self.ChangeRolePopup.data('typeId');
			var roleNumber = self.ChangeRolePopup.data('roleNumber');
			var newRole = self.ChangeRolePopup.find("#change-type-role-popup-role").val();

			// Verifications
			if(StringUtils.isBlank(newRole))	 {
      	Forms.input($("#change-type-role-popup-role")).setState("error", translator.get("message.notBlank"));
      } else {
				self.doChangeRole(typeId, roleNumber, newRole);
      }
		},

		doChangeRole : function(typeId, roleNumber, newRole) {
			var self = this;
			var requestId = 'requirement-link-type-role' + roleNumber;

			$.ajax({
				url : routing.buildURL("requirement.link.type", typeId),
				type : 'POST',
				data : {
					id : requestId,
					value : newRole
				}
			}).done(function() {
				self.table.refresh();
        self.ChangeRolePopup.formDialog('close');
			});

		},

		/* Change Code functions */

		configureChangeCodePopup : function() {
    	var self = this;

      var dialog = $("#change-type-code-popup");
      this.ChangeCodePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.changeCode.call(self);
   		});

      dialog.on('formdialogcancel', this.closePopup);
    },

    clearChangeCodeErrorMessage : function() {

    	Forms.input($("#change-type-code-popup-code")).clearState();
    },

		openChangeCode1Popup : function(event) {

    	this.openChangeCodePopup(event, 1);
    },

    openChangeCode2Popup : function(event) {

    	this.openChangeCodePopup(event, 2);
    },

		openChangeCodePopup : function(event, codeNumber) {
    	var self = this;
    	// clear error messages
    	self.clearChangeCodeErrorMessage();
    	// fill label input
    	var codeCell = event.currentTarget;

    	var row = codeCell.parentElement;
    	var data = this.table.fnGetData(row);
    	var typeId = data['type-id'];
    	var currentCode = $(codeCell).text();

    	self.ChangeCodePopup.data('typeId', typeId);
    	self.ChangeCodePopup.data('codeNumber', codeNumber);
    	self.ChangeCodePopup.formDialog('open');
    	self.ChangeCodePopup.find("#change-type-code-popup-code").val(currentCode);
    },

		changeCode : function() {
    	var self = this;
    	var typeId = self.ChangeCodePopup.data('typeId');
    	var codeNumber = self.ChangeCodePopup.data('codeNumber');
    	var newCode = self.ChangeCodePopup.find("#change-type-code-popup-code").val();

    	// Verifications
    	if(StringUtils.isBlank(newCode))	 {
         Forms.input($("#change-type-code-popup-code")).setState("error", translator.get("message.notBlank"));
      } else {
      	// Check code existence
				$.ajax({
					url : routing.buildURL("requirement.link.type", typeId),
					type : 'GET',
					data : {
						id : 'check-code',
						value: newCode
						}
					}).done(function(data) {
						if(data.codeExists) {
							Forms.input($("#change-type-code-popup-code")).setState("error", translator.get("requirement-version.link.type.rejection.codeAlreadyExists"));
						} else {
    					self.doChangeCode(typeId, codeNumber, newCode);
						}
					});
			}
    },

    doChangeCode : function(typeId, codeNumber, newCode) {
    	var self = this;
    	var requestId = 'requirement-link-type-code' + codeNumber;

    	$.ajax({
    		url : routing.buildURL("requirement.link.type", typeId),
    		type : 'POST',
    		data : {
    			id : requestId,
    			value : newCode
    		}
    	}).done(function() {
    		self.table.refresh();
        self.ChangeCodePopup.formDialog('close');
    	});

    },

		/* Delete Type functions */
		initErrorPopup : function() {

				this.ErrorPopup = $("#generic-error-dialog").messageDialog();
		},

		configureDeleteTypePopup : function() {

			var self = this;

      var dialog = $("#delete-link-type-popup");
      this.DeleteTypePopup = dialog;

      dialog.formDialog();

      dialog.on('formdialogconfirm', function(){
      	self.deleteType.call(self);
      });

      dialog.on('formdialogcancel', this.closePopup);
		},

		openDeleteTypePopup(event) {
			var self = this;
      var cell = event.currentTarget;

      var row = cell.parentElement;
      var data = self.table.fnGetData(row);
      var typeId = data['type-id'];

      $.ajax({
      	type: 'GET',
      	url: routing.buildURL('requirement.link.type', typeId),
      	data: {
      		id : 'isDefault'
      	}
      }).done(function(data) {
      	if(data.isTypeDefault) {
					self.ErrorPopup.find('.generic-error-main').html(translator.get("requirement-version.link.type.error.message.typeIsDefault"));
          self.ErrorPopup.messageDialog('open');
      	} else {
      		var deletePopupMessage = $("#delete-link-type-warning");
      		$.ajax({
      			type: 'GET',
      			url: routing.buildURL('requirement.link.type', typeId),
      			data: {
      				id: 'isUsed'
      			}
      		}).done(function(data) {
						if(data.isLinkTypeUsed) {
								deletePopupMessage.text(translator.get("requirement-version.link.type.delete.warning.linkTypeIsUsed"));
						} else {
								deletePopupMessage.text(translator.get("requirement-version.link.type.delete.warning.linkTypeIsUnused"));
						}
						self.DeleteTypePopup.data('typeId', typeId);
						self.DeleteTypePopup.formDialog('open');
      		});
      	}
      });
		},

		deleteType : function() {
			var self = this;
			var typeId = self.DeleteTypePopup.data('typeId');

			$.ajax({
				url: routing.buildURL('requirement.link.type', typeId),
				method: 'DELETE'
			}).done(function() {
				self.DeleteTypePopup.formDialog('close');
				self.table.refresh();
			});
		}



	});

	return reqLinkTypeManagerView;

});
