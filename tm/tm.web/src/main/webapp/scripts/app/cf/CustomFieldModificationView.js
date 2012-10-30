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
define([ "jquery", "backbone", "jeditable.simpleJEditable", "jeditable.selectJEditable", "jquery.squash", "jqueryui", "jquery.squash.togglepanel",
		 "jeditable.selectJEditable", "jquery.squash.datatables" ], function($, Backbone, SimpleJEditable, SelectJEditable) {
	var cfMod = squashtm.app.cfMod;
	/*
	 * Defines the controller for the custom fields table.
	 */
	var CustomFieldModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {			
			this.configureTogglePanels();
			this.configureEditables();
			this.configureRenamePopup();
			this.configureAddOptionPopup();
			this.configureOptionTable();
			this.configureButtons();
			
		},
		events: {
			"click #cf-optional": "sendOptional",
			"click #back": "goBack",
			"click .is-default>input:checkbox": "changeDefaultOption",
			"click .opt-label": "changeOptionLabel",
		}, 
		sendOptional: function(event) {
		var checked = event.target.checked;
			$.ajax({
				url : cfMod.customFieldUrl+"/optional",
				type : "post",
				data : {'value' : checked},
				dataType : "json",
				});
		},
		goBack:function(){
			document.location.href = cfMod.backUrl;
		},
		changeDefaultOption: function(event) {
			var self = this;
			var checkbox = event.currentTarget;
			var	option = checkbox.value;
			var	defaultValue = checkbox.checked ? option : "";			
			var	uncheckSelector = ".is-default>input:checkbox" + (checkbox.checked ? "[value!='" + option + "']" : "");
						
			this.sendDefaultValue(defaultValue).done(function(){
				self.optionsTable.find(uncheckSelector).attr("checked", false);
			}).fail(function(){
				checkbox.checked ? checkbox.checked = false : checkbox.checked = true;
			});
			
		},
		sendDefaultValue: function(defaultValue){
			return $.ajax({
				url: cfMod.optionsTable.ajaxSource,
				type: 'POST',
				data: {'default': defaultValue},
				dataType: 'json',
			});
		},
		changeOptionLabel: function(event){
			var self = this;
			var labelCell = event.currentTarget;
			var previousValue = labelCell.value;
			self.renameCufOptionPopup.find("#rename-cuf-option-previous").text(previousValue);
			self.renameCufOptionPopup.find("#rename-cuf-option-input").val(previousValue);
			self.renameCufOptionPopup.dialog("open");
		},
		configureButtons: function(){
			$("#back").button();
			$.squash.decorateButtons();
		},
		configureTogglePanels: function(){
			var informationSettings = {
					initiallyOpen : true,
					title : cfMod.informationPanelLabel,
				};
			this.$("#cuf-info-panel").togglePanel(informationSettings);
			var optionSettings = {
					initiallyOpen : true,
					title : cfMod.optionsPanelLabel,
				};
			this.$("#cuf-options-panel").togglePanel(optionSettings);
		},
		configureEditables:function(){
			this.makeSimpleJEditable("cuf-label");
			
			if($("#cuf-inputType").attr('value') == "PLAIN_TEXT"){
				this.makeSimpleJEditable("cuf-default-value");
			}else if($("#cuf-inputType").attr('value') == "CHECKBOX"){
				this.makeSelectJEditable("cuf-default-value", cfMod.checkboxJsonDefaultValues);
				
			}
		},
		renameCuf: function(){
			var newNameVal = $("#rename-cuf-input").val();
				$.ajax({
					type : 'POST',
					data : {'value': newNameVal},
					dataType : "json",
					url : cfMod.customFieldUrl+"/name",
				}).done(function(data){
					$('#cuf-name-header').html(data.newName);
					$('#rename-cuf-popup').dialog('close');
				});
		},
		configureRenamePopup:function(){
			var params = {
					selector : "#rename-cuf-popup",
					title : cfMod.renameCufTitle,
					openedBy : "#rename-cuf-button",
					isContextual : true,
					usesRichEdit : false,
					closeOnSuccess : true,
					buttons: [ { 'text' : cfMod.renameLabel,
					        	  'click' : this.renameCuf,
					        	},
						        { 'text' : cfMod.cancelLabel,
						          'click' : this.closeRenamePopup,
						        },
							],
			};
			squashtm.popup.create(params);
			$("#rename-cuf-popup").bind("dialogopen",function(event, ui) {
				var name = $.trim($('#cuf-name-header').text());
				$("#rename-cuf-input").val($.trim(name));
			});
			
		},
		closeRenamePopup : function() {$( this ).data("answer","cancel");
											$( this ).dialog( 'close' );},
		makeSimpleJEditable : function(imputId){
			new SimpleJEditable({
				language: {
					richEditPlaceHolder : cfMod.richEditPlaceHolder,
					okLabel: cfMod.okLabel,
					cancelLabel: cfMod.cancelLabel,
				},
				targetUrl : cfMod.customFieldUrl,
				componentId : imputId,
				jeditableSettings : {},
			});
		},
		makeSelectJEditable : function(inputId, jsonData){
			new SelectJEditable({
				language: {
					richEditPlaceHolder : cfMod.richEditPlaceHolder,
					okLabel: cfMod.okLabel,
					cancelLabel: cfMod.cancelLabel,
				},
				targetUrl : cfMod.customFieldUrl,
				componentId : inputId,
				jeditableSettings : {data : JSON.stringify(jsonData)},
			});
		},
		configureOptionTable:function(){
			if($("#cuf-inputType").attr('value') == "PLAIN_TEXT"){return;}
			var config = $.extend({
				"oLanguage": {
					"sUrl": cfMod.optionsTable.languageUrl
				},
				"bJQueryUI": true,
				"bAutoWidth": false,
				"bFilter": false,
				"bPaginate": true,
				"sPaginationType": "squash",
				"iDisplayLength": cfMod.optionsTable.displayLength,
				"bProcessing": true,
				"bServerSide": true,
				"sAjaxSource": cfMod.optionsTable.ajaxSource,
				"bDeferRender": true,
				"bRetrieve": true,
				"sDom": 't<"dataTables_footer"lirp>',
				"iDeferLoading": 0,
				"aaSorting": [ [ 0, "asc" ] ],
				"fnRowCallback": function() {
				},
				"aoColumnDefs": [ {
					'bSortable': true, 
					"aTargets": [ 0 ],
					"class": "opt-label",
					"mDataProp": "opt-label"
					},
					{'bSortable': true, 
					'aTargets': [ 1 ], 
					'sClass': "is-default",
					'mDataProp' : 'opt-default'
					},
					{'bSortable': false,
					'sWidth': '2em', 
					'sClass': 'delete-button',
					'aTargets': [ 2 ],
					'mDataProp' : 'empty-delete-holder'} ]
			}, squashtm.datatable.defaults);
			
			var squashSettings = {
					enableHover : true,
					
					confirmPopup : {
						oklabel : cfMod.confirmLabel,
						cancellabel : cfMod.cancelLabel,
					},
					
					deleteButtons : {
						url : cfMod.optionsTable.ajaxSource+"/{entity-id}",
						popupmessage : cfMod.optionsTable.deleteConfirmMessage,
						tooltip : cfMod.optionsTable.deleteTooltip,
						success : function(data){self.table.refresh();}
					},
					
				};
				
			this.optionsTable = this.$("table");
			this.optionsTable.squashTable(config, squashSettings);
		},
		
		configureAddOptionPopup: function(){
			if($("#cuf-inputType").attr('value') != "DROPDOWN_LIST"){
				return;
			}
			var self = this;
			var params = {
					selector : "#add-cuf-option-popup",
					title : cfMod.optionsTable.addCufOptionTitle,
					openedBy : "#add-cuf-option-button",
					isContextual : true,
					usesRichEdit : false,
					closeOnSuccess : true,
					buttons: [ { 'text' : cfMod.optionsTable.addOptionLabel,
					        	  'click' : function(){self.addOption.call(self);},
					        	},
						        { 'text' : cfMod.cancelLabel,
						          'click' : this.closeRenamePopup,
						        },
							],
			};
			squashtm.popup.create(params);
		},
		addOption: function(){
			 if($("#cuf-inputType").attr('value') != "DROPDOWN_LIST"){
				 return;
			 }
			var self = this;
			var label = $("#new-cuf-option-label-input").val();
			return $.ajax({
				url: cfMod.optionsTable.newOptionUrl,
				type: 'POST',
				data: {'label': label},
				dataType: 'json',
			}).done(function(){
					self.optionsTable.refresh();
			});
		},
		configureRenameOptionPopup: function(){
			if($("#cuf-inputType").attr('value') != "DROPDOWN_LIST"){
				return;
			}
			var self = this;
			var params = {
					selector : "#add-cuf-option-popup",
					title : cfMod.optionsTable.addCufOptionTitle,
					openedBy : "#add-cuf-option-button",
					isContextual : true,
					usesRichEdit : false,
					closeOnSuccess : true,
					buttons: [ { 'text' : cfMod.optionsTable.addOptionLabel,
					        	  'click' : function(){self.addOption.call(self);},
					        	},
						        { 'text' : cfMod.cancelLabel,
						          'click' : this.closeRenamePopup,
						        },
							],
			};
			squashtm.popup.create(params);
		},
	});
	return CustomFieldModificationView;
});