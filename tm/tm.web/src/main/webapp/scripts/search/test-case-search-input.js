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
define([ "jquery", "backbone", "handlebars", "squash.translator", "underscore",
		"app/util/StringUtil", "./SearchTextfieldWidget", "./SearchTextareaWidget", 
		"./SearchMultiselectWidget", "./SearchDateWidget", "jquery.squash",
		"jqueryui", "jquery.squash.togglepanel", "jquery.squash.datatables",
		"jquery.squash.oneshotdialog", "jquery.squash.messagedialog",
		"jquery.squash.confirmdialog" ], function($, Backbone, Handlebars, translator, _,
		StringUtil, SearchTextfieldWidget, SearchTextareaWidget, SearchMultiselectWidget, SearchDateWidget) {

	var TestCaseSearchInputPanel = Backbone.View.extend({

		el : "#test-case-search-input-panel",

		initialize : function() {
			this.getInputInterfaceModel();
			this.model = {fields : []};
		},

		events : {
			"click #test-case-search-button" : "showResults"
		},

		getInputInterfaceModel : function() {
			var self = this;
			var result = $.ajax({
				url : squashtm.app.contextRoot + "/advanced-search/input",
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
							$("#test-case-search-input-form-panel").append(html);
							var i;
							for(i=0; i<val.fields.length; i++){
								if(val.fields[i].inputType == "textfield"){
									self.makeTextField(tableid, val.fields[i].id, val.fields[i].title, val.fields[i].internationalized);
								} else if (val.fields[i].inputType == "textarea"){
									self.makeTextArea(tableid, val.fields[i].id, val.fields[i].title, val.fields[i].internationalized);
								} else if (val.fields[i].inputType == "multiselect"){
									self.makeMultiselect(tableid, val.fields[i].id, val.fields[i].title, val.fields[i].internationalized, val.fields[i].possibleValues);
								}
							}
							self.makeTogglePanel(val.id+"-panel-id",val.title,val.open);
						}
					});
				});
			});
		},
				
		makeTextField : function(tableId, textFieldId, textFieldTitle, internationalized) {
			
			var title;
			if(internationalized){
				title = translator.get(textFieldTitle);
			} else {
				title = textFieldTitle;
			}
			 
			var source = $("#textfield-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-field-id": textFieldId, 
				           "text-field-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			$("#"+textFieldId).searchTextFieldWidget();
			$("#"+textFieldId).append($("#"+textFieldId).searchTextFieldWidget('createDom', "F"+textFieldId));
		},
		
		makeTextArea : function(tableId, textFieldId, textFieldTitle, internationalized) {

			var title;
			if(internationalized){
				title = translator.get(textFieldTitle);
			} else {
				title = textFieldTitle;
			}
			
			var source = $("#textarea-template").html();
			var template = Handlebars.compile(source);
			var context = {"text-area-id": textFieldId, 
				           "text-area-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			$("#"+textFieldId).searchTextAreaWidget();
			$("#"+textFieldId).append($("#"+textFieldId).searchTextAreaWidget('createDom', "F"+textFieldId));
		},
		
		makeMultiselect : function(tableId, textFieldId, textFieldTitle, internationalized, options) {
	
			var title;
			if(internationalized){
				title = translator.get(textFieldTitle);
			} else {
				title = textFieldTitle;
			}
			
			var source = $("#multiselect-template").html();
			var template = Handlebars.compile(source);
			var context = {"multiselect-id": textFieldId, 
				           "multiselect-title": title};
			var html = template(context);
			$("#"+tableId).append(html);
			$("#"+textFieldId).searchMultiSelectWidget();
			$("#"+textFieldId).append($("#"+textFieldId).searchMultiSelectWidget('createDom', "F"+textFieldId, options));
		},
			
		extractSearchModel : function(){
			var fields = $("div.search-input");
			
			var jsonVariable = {};
			var i;
			for(i=0; i<fields.length; i++){
				var type = $($(fields[i]).children()[0]).attr("data-widgetname");
				var key = $(fields[i]).attr("id");
				var value = $("#"+$(fields[i]).attr("id")).data("search"+type+"Widget").fieldvalue();
				var jsonKey  = key;
				jsonVariable[jsonKey] = value;
			}
			this.model = {fields : jsonVariable};
		},
		
		post : function (URL, PARAMS) {
			var temp=document.createElement("form");
			temp.action=URL;
			temp.method="POST";
			temp.style.display="none";
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
			
			this.post(squashtm.app.contextRoot + "/advanced-search/results?testcase", {
				searchModel : JSON.stringify(this.model)
			});	
		},

		makeTogglePanel : function(id, key, open) {
			var title = translator.get(key);
			
			var infoSettings = {
				initiallyOpen : open,
				title : title
			};
			this.$("#"+id).togglePanel(infoSettings);
		}

	});
	return TestCaseSearchInputPanel;
});