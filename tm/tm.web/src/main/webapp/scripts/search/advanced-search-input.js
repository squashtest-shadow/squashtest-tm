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
    "./SearchTextfieldWidget", "./SearchTextareaWidget", 
		"./SearchMultiselectWidget", "./SearchDateWidget", "./SearchRangeWidget", 
		"./SearchExistsWidget", "./SearchCheckboxWidget", "./SearchComboMultiselectWidget", "./SearchRadioWidget", 
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel", "squashtable",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, translator, notification, _) {

	var TestCaseSearchInputPanel = Backbone.View.extend({

		el : "#advanced-search-input-panel",

		initialize : function() {
			this.model = {fields : []};
			// init templates cache
			this.templates = {};
			this.getInputInterfaceModel();
			
			// templates are no longer needed
			this.templates = {};
		},

		events : {
			"click #advanced-search-button" : "showResults"
		},

		getInputInterfaceModel : function() {
			var self = this;
			
			// compiles the panel template
			var source = self.$("#toggle-panel-template").html();
			
			if (!source) { // could this really happen without being a bug ?
				return;
			}
			
			var template = Handlebars.compile(source);
			
			// parses the search model if any
			var marshalledSearchModel = self.$("#searchModel").text();
			var searchModel = {};
			
			if(marshalledSearchModel){
				searchModel = JSON.parse(marshalledSearchModel).fields;
			}
			
			var searchDomain = self.$("#searchDomain").text();
			
			var result = $.ajax({
				url : squashtm.app.contextRoot + "/advanced-search/input?"+this.$("#searchDomain").text(),
				data : "nodata",
				dataType : "json"
			}).success(function(json) {
				
				$.each(json.panels || {}, function(index, panel) {
					var context = {"toggle-panel-id": panel.id+"-panel-id", "toggle-panel-table-id": panel.id+"-panel-table-id"};
					var tableid = panel.id+"-panel-table-id";
					var html = template(context);
					self.$("#advanced-search-input-form-panel-"+panel.location).append(html);
					self.$("#advanced-search-input-form-panel-"+panel.location).addClass(searchDomain);
					
					for (var i = 0, field; i < panel.fields.length; i++){
						field = panel.fields[i];

						if(field.inputType === "textfield"){
							self.makeTextField(tableid, field.id, field.title, searchModel[field.id], field.ignoreBridge);
							
						} else if (field.inputType === "textarea"){
							self.makeTextArea(tableid, field.id, field.title, searchModel[field.id]);
							
						} else if (field.inputType === "multiselect"){
							self.makeMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
							
						} else if (field.inputType === "combomultiselect"){
							self.makeComboMultiselect(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
							
						} else if (field.inputType === "range"){
							self.makeRangeField(tableid, field.id, field.title, searchModel[field.id]);
							
						} else if (field.inputType === "exists"){
							self.makeExistsField(tableid, field.id, field.title, field.possibleValues,searchModel[field.id]);
							
						} else if (field.inputType === "date"){
							self.makeDateField(tableid, field.id, field.title, searchModel[field.id]);
						} else if (field.inputType === "checkbox"){
							self.makeCheckboxField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id]);
							
						} else if (field.inputType === "radiobutton"){
							self.makeRadioField(tableid, field.id, field.title, field.possibleValues, searchModel[field.id], field.ignoreBridge);
							
						} 
					}
					self.makeTogglePanel(panel.id+"-panel-id",panel.title,panel.open,panel.cssClasses);
				});
			});
		},
		
		/**
		 * returns the html of a compiled template
		 * @param selector jq selector to find the template
		 * @param context the params given to the template 
		 * @returns
		 */
		_compileTemplate : function(selector, context) {
				var template = this.templates[selector];
				
				if (!template) {
					var source = this.$(selector).html();
					template = Handlebars.compile(source);
					this.templates[selector] = template;
				}
				
				return template(context);
		},

		
		_appendFieldDom : function(tableId, textFieldId, fieldHtml) {
			this.$("#"+tableId).append(fieldHtml);
			var escapedId = textFieldId.replace(/\./g, "\\.");
			return this.$("#" + escapedId);
		}, 
		
		makeRadioField : function(tableId, textFieldId, textFieldTitle, options, enteredValue, ignoreBridge) {
			var context = {"text-radio-id": textFieldId, "text-radio-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#radio-button-template", context));
			
			$fieldDom.searchRadioWidget({"ignoreBridge" : ignoreBridge});
			$fieldDom.searchRadioWidget("createDom", "F"+textFieldId, options);
			$fieldDom.searchRadioWidget("fieldvalue", enteredValue);
				
		},
		
		makeRangeField : function(tableId, textFieldId, textFieldTitle, enteredValue) {
			var context = {"text-range-id": textFieldId, "text-range-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#range-template", context));

			$fieldDom.searchRangeWidget();
			$fieldDom.searchRangeWidget("fieldvalue", enteredValue);
			
		},
		
		makeExistsField : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {
			var context = {"text-exists-id": textFieldId, "text-exists-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#exists-template", context));
			$fieldDom.searchExistsWidget();
			$fieldDom.searchExistsWidget("createDom", "F"+textFieldId, options);
			$fieldDom.searchExistsWidget("fieldvalue", enteredValue);
		},
			
		makeDateField : function(tableId, textFieldId, textFieldTitle, enteredValue) {
			var context = {"text-date-id": textFieldId, "text-date-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#date-template", context));
			$fieldDom.searchDateWidget();
			$fieldDom.searchDateWidget("createDom", "F"+textFieldId);
			$fieldDom.searchDateWidget("fieldvalue", enteredValue);
		},
			
		makeCheckboxField : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {
			var context = {"text-checkbox-id": textFieldId, "text-checkbox-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#checkbox-template", context));
			$fieldDom.searchCheckboxWidget();
			$fieldDom.searchCheckboxWidget("createDom", "F"+textFieldId, options);
			$fieldDom.searchCheckboxWidget("fieldvalue", enteredValue);
			
		},
			
		makeTextField : function(tableId, textFieldId, textFieldTitle, enteredValue, ignoreBridge) {
			var context = {"text-field-id": textFieldId, "text-field-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#textfield-template", context));
			$fieldDom.searchTextFieldWidget({"ignoreBridge" : ignoreBridge});
			$fieldDom.append($fieldDom.searchTextFieldWidget("createDom", "F"+textFieldId));
			$fieldDom.searchTextFieldWidget("fieldvalue", enteredValue);
		},
		
		makeTextArea : function(tableId, textFieldId, textFieldTitle, enteredValue) {
			var context = {"text-area-id": textFieldId, "text-area-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#textarea-template", context));
			$fieldDom.searchTextAreaWidget();
			$fieldDom.append($fieldDom.searchTextAreaWidget("createDom", "F"+textFieldId));
			$fieldDom.searchTextAreaWidget("fieldvalue", enteredValue);
		},
		
		makeMultiselect : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {
			var context = {"multiselect-id": textFieldId, "multiselect-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#multiselect-template", context));
			$fieldDom.searchMultiSelectWidget();
			$fieldDom.append($fieldDom.searchMultiSelectWidget("createDom", "F"+textFieldId, options));
			$fieldDom.searchMultiSelectWidget("fieldvalue", enteredValue);
		},
			
		makeComboMultiselect : function(tableId, textFieldId, textFieldTitle, options, enteredValue) {
			var context = {"combomultiselect-id": textFieldId, "combomultiselect-title": textFieldTitle};
			var $fieldDom = this._appendFieldDom(tableId, textFieldId, this._compileTemplate("#combomultiselect-template", context));
			$fieldDom.searchComboMultiSelectWidget();
			$fieldDom.searchComboMultiSelectWidget("createDom", "F"+textFieldId, options);
			$fieldDom.searchComboMultiSelectWidget("fieldvalue", enteredValue);
		},
		
		extractSearchModel : function(){
			var fields = self.$("div.search-input");
			
			var jsonVariable = {};

			for (var i = 0, $field; i < fields.length; i++) {
				$field = $(fields[i]);
				var type = $($field.children()[0]).attr("data-widgetname");
				var key = $field.attr("id");
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

			for ( var x in PARAMS) {
				var opt = document.createElement("textarea");
				opt.name = x;
				opt.value = PARAMS[x];
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
			
			var searchModel = JSON.stringify(this.model);
			var queryString = "searchModel=" + encodeURIComponent(searchModel);

			if(!!$("#associationType").length){				
				var associateResultWithType = $("#associationType").text();
				queryString += "&associateResultWithType=" + encodeURIComponent(associateResultWithType);

				var id = $("#associationId").text();
				queryString += "&id=" + encodeURIComponent(id);
				
			}
				
			document.location.href = squashtm.app.contextRoot + "advanced-search/results?"+$("#searchDomain").text() + "&" + queryString;
		},

		makeTogglePanel : function(id, key, open, css) {
			var title = key;
			
			var infoSettings = {
				initiallyOpen : open,
				title : title, 
				cssClasses : ""
			};
			$panel = this.$("#"+id);
			$panel.togglePanel(infoSettings);
			$("a", $panel.parent()).removeClass("tg-link").addClass(css.toString());
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
				} else if (field.values !== undefined && field.values !== null){
					hasCriteria = true;
				}
			});
			return !hasCriteria;
		}

	});
	return TestCaseSearchInputPanel;
});