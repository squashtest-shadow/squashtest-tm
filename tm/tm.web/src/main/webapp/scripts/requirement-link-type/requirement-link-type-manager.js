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
define([ 'module', "./requirement-link-type-table-view",  "jquery", "backbone", "underscore", "squash.basicwidgets", "jeditable.simpleJEditable",
		"workspace.routing", "squash.translator", "app/lnf/Forms", "app/util/StringUtil","jquery.squash.togglepanel", "squashtable", "app/ws/squashtm.workspace"],
		function(module, TableView  , $, backbone, _, basic, SimpleJEditable, routing, translator, Forms, StringUtils) {
	"use strict";

	var config = module.config();

	//translator.load([]);

	var reqLinkTypeManagerView = Backbone.View.extend({
		el : "#link-types-table-pane",
		initialize : function() {
			this.basicInit();
			this.config = config;
			this.configureNewLinkTypePopup();
			//this.configureDeleteInfoListPopup();
			this.initTable();

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
			"click #add-link-type-btn" : "openAddLinkTypePopup"
			/*"click #delete-info-list-button" : "deleteInfoListPopup"*/
			},

		openAddLinkTypePopup : function(){
			var self = this;
			self.clearAddLinkErrorMessages();
			self.AddLinkTypePopup.formDialog("open");
		},
		clearAddLinkErrorMessages() {
			Forms.input($("#add-link-type-popup-role1")).clearState();
      Forms.input($("#add-link-type-popup-role1-code")).clearState();
      Forms.input($("#add-link-type-popup-role2")).clearState();
      Forms.input($("#add-link-type-popup-role2-code")).clearState();
		},
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
    	// Vérification BLANK
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
			// Verify if the codes already exist
			if(!oneInputIsBlank) {
				// Verify if Codes Exist
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
		doAddNewLinkType(paramLinkType) {
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


	});
	return reqLinkTypeManagerView;

});
