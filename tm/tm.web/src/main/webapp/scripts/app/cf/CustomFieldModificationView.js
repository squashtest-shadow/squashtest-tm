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
		 "jeditable.selectJEditable", "jquery.squash.datatables", "jquery.squash.oneshotdialog", "jquery.squash.messagedialog" ], function($, Backbone, SimpleJEditable, SelectJEditable) {
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
			this.configureRenameOptionPopup();
			this.configureOptionTable();
			this.configureButtons();
			
		},
		events: {
			"click #cf-optional": "confirmOptional",
			"click .is-default>input:checkbox": "changeDefaultOption",
			"click .opt-label": "openRenameOptionPopup",
		}, 
		confirmOptional: function(event) {
			var self = this;
		var checked = event.target.checked;
			if(checked){
				self.sendOptional(checked);
			}else{
				var defaultValue = self.findDefaultValue();
				if(!defaultValue || defaultValue === "" || defaultValue === cfMod.richEditPlaceHolder){
					$.squash.openMessage(cfMod.popupErrorTitle, cfMod.mandatoryNeedsDefaultMessage,350);
					event.target.checked = true;
					return;
				}
				var message = cfMod.confirmMandatoryMessage;
				message = self.replacePlaceHolderByValue(0, message, defaultValue);
				oneShotConfirm(cfMod.confirmMandatoryTitle, message, cfMod.confirmLabel, cfMod.cancelLabel, 500)
				.done(function(){self.sendOptional(checked);})
				.fail(function(){event.target.checked = true;});
			}
		},
		findDefaultValue:function(){
			var defaultValueDiv = this.$('#cuf-default-value');
			if(defaultValueDiv && defaultValueDiv.length > 0){
				return defaultValueDiv[0].textContent;
			}else if(this.optionsTable){
				var checkedDefault = this.optionsTable.find('td.is-default input:checked');
				if(checkedDefault){
					return checkedDefault.val();
				}
			}
			return "";
		},
		replacePlaceHolderByValue: function(index, message, replaceValue){
			var pattern = /\{[\d,\w,\s]*\}/;
			var match = pattern.exec(message);
			var pHolder = match[index];
			return message.replace(pHolder, replaceValue);
		},
		sendOptional: function(optional){
			return $.ajax({
				url : cfMod.customFieldUrl+"/optional",
				type : "post",
				data : {'value' : optional},
				dataType : "json",
				});
		},
		
		changeDefaultOption: function(event) {
			var self = this;
			var checkbox = event.currentTarget;
			var	option = checkbox.value;
			var	defaultValue = checkbox.checked ? option : "";
			if(defaultValue === "" && !this.$("#cf-optional").prop("checked")){
				checkbox.checked = true;
				$.squash.openMessage(cfMod.popupErrorTitle, cfMod.defaultOptionMandatoryMessage);
				return;
			}
			var	uncheckSelector = ".is-default>input:checkbox" + (checkbox.checked ? "[value!='" + option + "']" : "");
						
			this.sendDefaultValue(defaultValue).done(function(){
				self.optionsTable.find(uncheckSelector).attr("checked", false);
			}).fail(function(){
				checkbox.checked ? checkbox.checked = false : checkbox.checked = true;
			});
			
		},
		sendDefaultValue: function(defaultValue){
			return $.ajax({
				url: cfMod.customFieldUrl+"/defaultValue",
				type: 'POST',
				data: {'value': defaultValue},
				dataType: 'json',
			});
		},
		openRenameOptionPopup: function(event){
			var self = this;
			var labelCell = event.currentTarget;
			var previousValue = labelCell.textContent;
			self.renameCufOptionPopup.find("#rename-cuf-option-previous").text(previousValue);
			self.renameCufOptionPopup.find("#rename-cuf-option-input").val(previousValue);
			self.renameCufOptionPopup.dialog("open");
		},
		renameOption: function(){
			var self = this;
			var previousValue = self.renameCufOptionPopup.find("#rename-cuf-option-previous").text();
			var newValue = self.renameCufOptionPopup.find("#rename-cuf-option-input").val();
			$.ajax({
				type : 'POST',
				data : {'value': newValue},
				dataType : "json",
				url : cfMod.optionsTable.ajaxSource+"/"+previousValue+"/label",
			}).done(function(data){
				self.optionsTable.refresh();
			});
			
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
			var self = this;
			this.makeSimpleJEditable("cuf-label");
			
			if($("#cuf-inputType").attr('value') == "PLAIN_TEXT"){
				new SimpleJEditable({
					language: {
						richEditPlaceHolder : cfMod.richEditPlaceHolder,
						okLabel: cfMod.okLabel,
						cancelLabel: cfMod.cancelLabel,
					},
					targetUrl : function(value, settings){if( self.changeDefaultValueText(value)){return value;}else{return this.revert;}},
					componentId : "cuf-default-value",
					jeditableSettings : {},
				});
			}else if($("#cuf-inputType").attr('value') == "CHECKBOX"){
				this.makeSelectJEditable("cuf-default-value", cfMod.checkboxJsonDefaultValues);
				
			}
		},
		changeDefaultValueText: function(value){
			if(value === ""){
				$.squash.openMessage(cfMod.popupErrorTitle, cfMod.defaultValueMandatoryMessage);
				return false;
			}else{
				this.sendDefaultValue(value);
				return true;
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
						          'click' : this.closePopup,
						        },
							],
			};
			squashtm.popup.create(params);
			$("#rename-cuf-popup").bind("dialogopen",function(event, ui) {
				var name = $.trim($('#cuf-name-header').text());
				$("#rename-cuf-input").val($.trim(name));
			});
			
		},
		closePopup : function() {$( this ).data("answer","cancel");
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
			var self = this;
			if($("#cuf-inputType").attr('value') != "DROPDOWN_LIST"){return;}
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
				"fnRowCallback": function() {
				},
				"aoColumnDefs": [ {'bSortable': false, 
					'sWidth': '2em', 
						'sClass': 'centered ui-state-default drag-handle select-handle', 
						'aTargets': [ 0 ], 
						'mDataProp' : 'entity-index'},
					{'bSortable': false, 
						"aTargets": [ 1 ],
						"sClass": "opt-label linkWise",
						"mDataProp": "opt-label"
					},
					{'bSortable': false, 
						'aTargets': [ 2 ], 
						'sClass': "is-default",
						'mDataProp' : 'opt-default'
					},
					{'bSortable': false,
						'sWidth': '2em', 
						'sClass': 'delete-button',
						'aTargets': [ 3 ],
						'mDataProp' : 'empty-delete-holder'} ]
			}, squashtm.datatable.defaults);
			
			var squashSettings = {
					enableHover : true,
					enableDnD : true,
					confirmPopup : {
						oklabel : cfMod.confirmLabel,
						cancellabel : cfMod.cancelLabel,
					},
					
					deleteButtons : {
						url : cfMod.optionsTable.ajaxSource+"/{opt-label}",
						popupmessage : cfMod.optionsTable.deleteConfirmMessage,
						tooltip : cfMod.optionsTable.deleteTooltip,
						success : function(data){self.optionsTable.refresh();},
						dataType : "json",
					},
					
					functions : {
						dropHandler : function(dropData){
							$.post(cfMod.optionsTable.ajaxSource+'/positions',dropData, function(){
								self.optionsTable.refresh();
							});
						},
						getODataId : function(arg){
							return this.fnGetData(arg)['opt-label'];
						}
					}
					
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
						          'click' : this.closePopup,
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
					selector : "#rename-cuf-option-popup",
					title : cfMod.optionsTable.renameCufOptionTitle,
					openedBy : "#rename-cuf-option-popup",
					isContextual : true,
					usesRichEdit : false,
					closeOnSuccess : true,
					buttons: [ { 'text' : cfMod.optionsTable.renameOptionLabel,
					        	  'click' : function(){self.renameOption.call(self);},
					        	},
						        { 'text' : cfMod.cancelLabel,
						          'click' : this.closePopup,
						        },
							],
			};
			squashtm.popup.create(params);
			this.renameCufOptionPopup =  $("#rename-cuf-option-popup");
		},
		
		
	});
	return CustomFieldModificationView;
});