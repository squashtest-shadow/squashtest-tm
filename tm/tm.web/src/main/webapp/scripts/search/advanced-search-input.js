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
define([ "jquery", "backbone", "handlebars", "squash.translator", "app/ws/squashtm.notification", "underscore",
		"app/util/StringUtil", "./SearchTextfieldWidget", "./SearchTextareaWidget", 
		"./SearchMultiselectWidget", "./SearchDateWidget", "./SearchRangeWidget", 
		"./SearchExistsWidget", "./SearchCheckboxWidget", "./SearchComboMultiselectWidget", "./SearchRadioWidget", 
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, translator, notification, _,
		StringUtil, SearchTextfieldWidget, SearchTextareaWidget, SearchMultiselectWidget, 
		SearchDateWidget, SearchRangeWidget, SearchExistsWidget, SearchCheckboxWidget, SearchComboMultiselectWidget, SearchRadioWidget) {

	var TestCaseSearchInputPanel = Backbone.View.extend({

		el : "#advanced-search-input-panel",

		initialize : function() {
			this.getInputInterfaceModel();
			this.model = {fields : []};
		},

		events : {
			"click #advanced-search-button" : "showResults"
		},

		getInputInterfaceModel : function() {
			var self = this;
			var result = $.ajax({
				url : squashtm.app.contextRoot + "/advanced-search/input?"+$("#searchDomain").text(),
				data : "nodata",
				dataType : "json"
			}).success(function(json) {
				$.each(json, function(key, value) {
					$.each(value, function(key, val){
						var source = $("#toggle-panel-template").html();
						if(source){
							var template = Handlebars.compile(source);
							var context = {"toggle-panel-id": val.id+"-panel-id", 
									       "toggle-panel-table-id": val.id+"-panel-table-id"};
							var tableid = val.id+"-panel-table-id";
							var html = template(context);
							$("#advanced-search-input-form-panel-"+val.location).append(html);
							$("#advanced-search-input-form-panel-"+val.location).addClass($("#searchDomain").text());
							var i;
							var searchModel = {}; 
							if($("#searchModel").text()){
								searchModel = JSON.parse($("#searchModel").text()).fields;
							}
							
							var field ;
							for(i=0; i<val.fields.length; i++){
								field = val.fields[i];
								if(field.inputType == "textfield"){
									self.makeTextField(tableid, field.id, field.title, searchModel[field.id], field.ignoreBridge);
								} else if (field.inputType == "textarea"){
									self.makeTextArea(tableid, field.id, field.title, searchModel[field.id]);
								} else if (field.inputType == "multiselect"){
									self.makeMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								} else if (field.inputType == "combomultiselect"){
									self.makeComboMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								} else if (field.inputType == "range"){
									self.makeRangeField(tableid, field.id, field.title, searchModel[field.id]);
								} else if (field.inputType == "exists"){
									self.makeExistsField(tableid, field.id, field.title, field.possibleValues,searchModel[field.id]);
								} else if (field.inputType == "date"){
									self.makeDateField(tableid, field.id, field.title, searchModel[field.id]);
								} else if (field.inputType == "checkbox"){
									self.makeCheckboxField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
								} else if (field.inputType == "radiobutton"){
									self.makeRadioField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id], field.ignoreBridge);
								} 
							}
							self.makeTogglePanel(val.id+"-panel-id",val.title,val.open,val.cssClasses);
						}
					});
				});
			});
		},

		makeRadioField : function(tableId, textFieldId, textFieldTitle, options, enteredValue, ignoreBridge) {
			
			var title = textFieldTitle;
			
			var source = $("#radio-button-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-radio-id": textFieldId, 
				           "text-radio-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchRadioWidget({"ignoreBridge" : ignoreBridge});
			$("#"+escapedId).searchRadioWidget('createDom', "F"+textFieldId, options);
			$("#"+escapedId).searchRadioWidget('fieldvalue', enteredValue);
			
		},
		
		makeRangeField : function(tableId, textFieldId, textFieldTitle, enteredValue) {
			
			var title = textFieldTitle;
			
			var source = $("#range-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-range-id": textFieldId, 
				           "text-range-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchRangeWidget();
			$("#"+escapedId).searchRangeWidget('fieldvalue', enteredValue);
			
		},
		
		makeExistsField : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {
			
			var title = textFieldTitle;
			var source = $("#exists-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-exists-id": textFieldId, 
				           "text-exists-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchExistsWidget();
			$("#"+escapedId).searchExistsWidget('createDom', "F"+textFieldId, options);
			$("#"+escapedId).searchExistsWidget('fieldvalue', enteredValue);
		},
			
		makeDateField : function(tableId, textFieldId, textFieldTitle, enteredValue) {
			
			var title = textFieldTitle;
			var source = $("#date-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-date-id": textFieldId, 
				           "text-date-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchDateWidget();
			$("#"+escapedId).searchDateWidget('createDom', "F"+textFieldId);
			$("#"+escapedId).searchDateWidget('fieldvalue', enteredValue);
		},
			
		makeCheckboxField : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {

			var title = textFieldTitle;
			var source = $("#checkbox-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-checkbox-id": textFieldId, 
				           "text-checkbox-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchCheckboxWidget();
			$("#"+escapedId).searchCheckboxWidget('createDom', "F"+textFieldId, options);
			$("#"+escapedId).searchCheckboxWidget('fieldvalue', enteredValue);
			
		},
			
		makeTextField : function(tableId, textFieldId, textFieldTitle, enteredValue, ignoreBridge) {
			
			var title = textFieldTitle;
			var source = $("#textfield-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-field-id": textFieldId, 
				           "text-field-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchTextFieldWidget({"ignoreBridge" : ignoreBridge});
			$("#"+escapedId).append($("#"+escapedId).searchTextFieldWidget('createDom', "F"+textFieldId));
			$("#"+escapedId).searchTextFieldWidget('fieldvalue', enteredValue);
		},
		
		makeTextArea : function(tableId, textFieldId, textFieldTitle, enteredValue) {

			var title = textFieldTitle;
			var source = $("#textarea-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-area-id": textFieldId, 
				           "text-area-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchTextAreaWidget();
			$("#"+escapedId).append($("#"+escapedId).searchTextAreaWidget('createDom', "F"+textFieldId));
			$("#"+escapedId).searchTextAreaWidget('fieldvalue', enteredValue);
		},
		
		makeMultiselect : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {
	
			var title = textFieldTitle;
			var source = $("#multiselect-template").html();
			var template = Handlebars.compile(source);
			var context = {"multiselect-id": textFieldId, 
				           "multiselect-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchMultiSelectWidget();
			$("#"+escapedId).append($("#"+escapedId).searchMultiSelectWidget('createDom', "F"+textFieldId, options));
			$("#"+escapedId).searchMultiSelectWidget('fieldvalue', enteredValue);
		},
			
		makeComboMultiselect : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {
			
			var title = textFieldTitle;
			var source = $("#combomultiselect-template").html();
			var template = Handlebars.compile(source);
			var context = {"combomultiselect-id": textFieldId, 
				           "combomultiselect-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			$("#"+escapedId).searchComboMultiSelectWidget();
			$("#"+escapedId).searchComboMultiSelectWidget('createDom', "F"+textFieldId, options);
			$("#"+escapedId).searchComboMultiSelectWidget('fieldvalue', enteredValue);
		},
		
		extractSearchModel : function(){
			var fields = $("div.search-input");
			
			var jsonVariable = {};
			var i;
			for(i=0; i<fields.length; i++){
				var type = $($(fields[i]).children()[0]).attr("data-widgetname");
				var key = $(fields[i]).attr("id");
				var escapedKey = key.replace(/\./g, "\\.");
				var field = $("#"+escapedKey).data("search"+type+"Widget");
				if(field && !!field.fieldvalue()){
					var value = field.fieldvalue();
					var jsonKey  = key;
					jsonVariable[jsonKey] = value;
				}
			}
			this.model = {fields : jsonVariable};
		},
		
		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
			temp.acceptCharset="UTF-8";
			for(var x in PARAMS) {
				var opt=document.createElement("textarea");
				opt.name=x;
				opt.value=PARAMS[x];
				temp.appendChild(opt);
			}
			document.body.appendChild(temp);
			temp.submit();
			return temp;
		},
		
		showResults : function() {
			
			this.extractSearchModel();
			
			if (this.emptyCriteria()){
				var message = translator.get('search.validate.empty.label');
				notification.showInfo(message);
				return;
			}

			if(!!$("#associationType").length){
				
				var associateResultWithType = $("#associationType").text();
				var id = $("#associationId").text();
				
				this.post(squashtm.app.contextRoot + "/advanced-search/results?"+$("#searchDomain").text(), {
					searchModel : JSON.stringify(this.model),
					associateResultWithType : associateResultWithType,
					id : id
				});	
				
				
			} else {
				this.post(squashtm.app.contextRoot + "advanced-search/results?"+$("#searchDomain").text(), {
					searchModel : JSON.stringify(this.model)
				});	
			}
		},

		makeTogglePanel : function(id, key, open, css) {
			var title = key;
			
			var infoSettings = {
				initiallyOpen : open,
				title : title, 
				cssClasses : ""
			};
			this.$("#"+id).togglePanel(infoSettings);
			$("a", $("#"+id).parent()).removeClass("tg-link").addClass(css.toString());
		},
		
		emptyCriteria : function(){
			var hasCriteria = false;
			$.each(this.model.fields, function(namename,field){ 
				// we must distinguish singlevalued and multivalued fields
				// singlevalued fields define a property 'value', while multivalued fields define a property 'values'.
				// a singlevalued field is empty if the property 'value' is empty, 
				// a multivalued field is empty if the property 'values' is null.
				// 
				if ( field.value !== undefined && field.value !== "" ){
					hasCriteria = true;
				}
				else if (field.values !== undefined && field.values !== null){
					hasCriteria = true;
				}
			});
			return !hasCriteria;
		}

	});
	return TestCaseSearchInputPanel;
});