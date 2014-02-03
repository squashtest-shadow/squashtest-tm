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

define([ "jquery", "backbone", "underscore", "workspace.event-bus", "jeditable.simpleJEditable", "jeditable.selectJEditable", 
         "jeditable.selectJEditableAuto", "jquery.squash.jeditable"],
		function($, Backbone, _ , eventBus, SimpleJEditable, SelectJEditable, SelectJEditableAuto) {
	
			var GeneralInfosPanel = Backbone.View.extend({
				
				el : "#test-case-description-panel",
				
				initialize : function() {
					this.settings = this.options.settings;
					this.updateReferenceInTree = $.proxy(this._updateReferenceInTree, this);
					this.postImportance = $.proxy(this._postImportance, this);
					this.postStatus = $.proxy(this._postStatus, this);
					this.updateStatusInTree = $.proxy(this._updateStatusInTree, this);
					this.refreshImportanceIfAuto = $.proxy(this._refreshImportanceIfAuto,this);
					this.updateImportanceInTree = $.proxy(this._updateImportanceInTree, this);
					this.onRefreshImportance = $.proxy(this._onRefreshImportance, this);
					if(this.settings.writable){
						
						var richEditSettings = {
								url : this.settings.urls.testCaseUrl,
								ckeditor : {
									customConfig : squashtm.app.contextRoot + "styles/ckeditor/ckeditor-config.js",
									language : squashtm.message.cache['rich-edit.language.value']
								},
								placeholder : squashtm.message.placeholder,
								submit :  squashtm.message.cache['label.Ok'],
								cancel :  squashtm.message.cache['label.Cancel'],
								indicator : '<div class="processing-indicator"/>'

							};
						$('#test-case-description').richEditable(richEditSettings).addClass("editable");
						
						this.referenceEditable = new SimpleJEditable({
							targetUrl :this.settings.urls.testCaseUrl,
							componentId : "test-case-reference",
							submitCallback : this.updateReferenceInTree,
							jeditableSettings : {
								maxLength : 50
							}
						});	
					
						this.importanceEditable = new SelectJEditable({
							target : this.postImportance,
							getUrl : this.settings.urls.testCaseUrl+"/importance",
							componentId : "test-case-importance",
							jeditableSettings : {
								data : this.settings.testCaseImportanceComboJson
							}
						});
						
						this.natureEditable = new SelectJEditable({
							target : this.settings.urls.testCaseUrl,
							componentId : "test-case-nature",
							jeditableSettings : {
								data : this.settings.testCaseNatureComboJson
							}
						});
						
						this.typeEditable = new SelectJEditable({
							target : this.settings.urls.testCaseUrl,
							componentId : "test-case-type",
							jeditableSettings : {
								data : this.settings.testCaseTypeComboJson
							}
						});
						this.statusEditable = new SelectJEditable({
							target : this.postStatus,
							componentId : "test-case-status",
							jeditableSettings : {
								data : this.settings.testCaseStatusComboJson
							}
						});
					
						this.importanceEditableAuto = new SelectJEditableAuto({
								associatedSelectJeditableId:"test-case-importance",
								url: this.settings.urls.importanceAutoUrl,
								isAuto: this.settings.importanceAuto,
								paramName:"importanceAuto" ,
								autoCallBack: this.updateImportanceInTree
						});
						
						$(this.importanceEditable).on("selectJEditable.refresh", this.onRefreshImportance);
					}
					this.identity = { resid : this.settings.testCaseId, rel : "test-case"  };
					
				},
				
				events : {
					
				},
				_refreshImportanceIfAuto : function(){
					if(this.importanceEditableAuto.isAuto()){
						this.importanceEditable.refresh();
					}
				},
				
				_onRefreshImportance : function(){
					var option  = this.importanceEditable.getSelectedOption();
					this.updateImportanceInTree(option);
				},
					
				_postStatus : function (value, settings){
					var self = this;
					$.post(this.settings.urls.testCaseUrl, {id:"test-case-status", value : value})
					.done(function(response){
						self.updateStatusInTree(value);
					});
					
					// in the mean time, must return immediately
					return settings.data[value];
				},
				
				_postImportance : function (value, settings){
					var self = this;
					$.post(this.settings.urls.testCaseUrl, {id:"test-case-importance", value : value})
					.done(function(response){
						self.updateImportanceInTree(value);
					});
					
					// in the mean time, must return immediately
					return settings.data[value];
				},
				
				_updateStatusInTree : function(value){
					var self = this;
					var evt = new EventUpdateStatus(self.identity, value.toLowerCase());
					squashtm.workspace.eventBus.fire(null, evt);
				},
				
				_updateImportanceInTree : function(value){
					var self = this;
					var evt = new EventUpdateImportance(self.identity, value.toLowerCase());
					squashtm.workspace.eventBus.fire(null, evt);	
				},
				
				_updateReferenceInTree : function (newRef){
					eventBus.trigger('node.update-reference', {identity : self.identity}, newRef : newRef);		
				}

			});
						
			return GeneralInfosPanel;
});