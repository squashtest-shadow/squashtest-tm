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
		 "jeditable.selectJEditable"  ], function($, Backbone, SimpleJEditable, SelectJEditable) {
	var cfMod = squashtm.app.cfMod;
	/*
	 * Defines the controller for the custom fields table.
	 */
	var CustomFieldModificationView = Backbone.View.extend({
		el : "#information-content",
		initialize : function() {
			this.configureButtons();
			this.configureTogglePanels();
			this.configureEditables();
			this.configureRenamePopup();
		},
		events: {
			"click #cf-optional": "sendOptional",
			"click #back": "goBack",			
		}, 
		sendOptional: function(event) {
		var checked = event.target.checked;
			$.ajax({
				url : cfMod.customFieldUrl,
				type : "post",
				data : {'isOptional' : checked},
				dataType : "json",
				});
		},
		goBack:function(){
			document.location.href = cfMod.backUrl;
		},
		openRenamePopup:function(){
			
		},
		configureButtons: function(){
			$("#back").button();
			$.squash.decorateButtons();
		},
		configureTogglePanels: function(){
			var settings = {
					initiallyOpen : true,
					title : cfMod.informationPanelLabel,
					cssClasses : "is-contextual",
				};
			this.$("#cuf-info-panel").togglePanel(settings);
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
					data : {'name': newNameVal},
					dataType : "json",
					url : cfMod.customFieldUrl,
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
					ckeditor : {
						styleUrl : squashtm.app.contextRoot+"/styles/ckeditor/ckeditor-config.js",
						lang : cfMod.ckeditorLang,
					},
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
		}
		
	});
	return CustomFieldModificationView;
});