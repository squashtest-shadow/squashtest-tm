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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView", "squash.translator", "jquery.squash"],
	function($, backbone, _, Handlebars, AbstractStepView, translator) {
	"use strict";

	var typeStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#type-step-tpl";
			this.model = data;
		    data.name = "type";
			this._initialize(data, wizrouter);
			this.precalculateInfoListItemData();
			this.reloadData();
		},
		
	events : {
			
			"change .axis-select" : "changeAxis",
			"change .chart-type" : "changeType"
			
		},
		
		changeType : function (event){
			this.showAxisByType(event.target.value);
			
		},
		
		showAxisByType : function (type) {
			
			switch (type) {
			
			case "LINE" : this.showLineAxis();
				break;
			case "BAR" : this.showBarAxis();
				break;
			case "PIE" : this.showPieAxis();
				break;
			case "TABLE" : //no table atm
				break;
			}
			
			
		},
		
		showPieAxis : function () {	
			this.setInvisible(["axis-y", "axis-x2"]);			
		},
		
		showBarAxis : function () {
			this.setVisible(["axis-y", "axis-x2"]);
		},
		
		showLineAxis : function () {
			this.setVisible(["axis-y"]);
			this.setInvisible(["axis-x2"]);
		}, 
		
		setVisible : function (values){		
			_.each(values, function (val) {$("#" + val).visible();});
			
		},
		setInvisible : function (values){
			_.each(values, function (val) {$("#" + val).invisible();});
		},

		precalculateInfoListItemData : function () {
			
			var getInfoListItems = function (infoList){
				return _.chain(infoList).reduce(function(memo, val){ 
					return memo.concat(_.map(_.values(val), function(list) { 
						return list.items; }));
					}, [])
				.flatten()
				.value();
			};
			
			var infoLists = this.model.get("projectInfoList");
			
			this.systemInfoListItems = getInfoListItems(_(infoLists).pick("default"));
			this.allInfoListItems = getInfoListItems(infoLists);
			
		},
		
		changeAxis :  function (event){
			
			this.loadOperation($(event.target).val(), event.target.name);
			this.loadFilter($(event.target).val(), event.target.name);
		},
		
		loadOperation : function (colName, axis){
           var operation = this.findChoosenOperation(colName)  || "NONE";		
			$("#operation-" + axis).text(this.i18nOperation(operation));
			
		},
		
		i18nOperation: function(operation) {
			return translator.get("chart.operation." + operation);
		},
		
		reloadAxis : function (val, axisName){
			var label = _.chain(val).result("column").result("label").value();
			$("#axis-axis-" + axisName).val(label);
		},
		
		reloadData : function () {
			
			var self = this;

			var type = $(".chart-type").filter(":checked").attr("value");
			this.showAxisByType(type);
			
			
			_.each(this.model.get("measures"), function (val) {
				var axisName = "y";
				self.reloadAxis(val, axisName);
			});
			_.each(this.model.get("axis"), function (val, indx) {
				var  axisName = "x" + (indx + 1);
				self.reloadAxis(val, axisName);
			});

			
			_.chain(["y", "x1", "x2"])
			.map(function(val){return "axis-" + val;})
			.each(function (val){
				var label = $("#axis-" + val).val();
				self.loadOperation(label, val);	
				self.loadFilter(label, val);
			});
		
		
		},
		
		findInfoListItem : function(liste, value) { 
			return _.chain(liste)
		.find(function(val){return val.code == value; });
		},
		
       isSystem : function(value) {
			return ! this.findInfoListItem(this.systemInfoListItems, value)
			.isUndefined()
		    .value();
			},
		
		loadFilter : function (colName, axis) {
			var filter = this.findFilterByColumnLabel(colName);
			
			var self = this;

			if (filter){
				
				var operation = self.i18nOperation(filter.operation);
				
				var type = filter.column.dataType;
				
				var columnLabel = filter.column.label;
					
				var values = filter.values;
				
				switch (type){
				
				case "INFO_LIST_ITEM":
				
					values = _.chain(values)
					.flatten()
					.map(function(val){		
						if (!self.isSystem(val)){
							return self.findInfoListItem(self.allInfoListItems, val).result("label").value();
						} else {
							return translator.get(self.findInfoListItem(self.systemInfoListItems, val).result("label").value());
						}
					})
					.value();
					
					
					break;
				
				case "LEVEL_ENUM":
					
					var levelEnum = _.chain(self.model.get("levelEnums"))
					.pick(columnLabel) 
					.reduce(function(memo, val){ return memo.concat(val);}, [])
					.value();
					
					values = self.translateEnum (values, levelEnum);
					
					break;
					
				case "EXECUTION_STATUS":

					var execStatus = _(self.model.get("executionStatus"))
					.flatten();
					
					values = self.translateEnum (values, execStatus);
			
					break;

					
				default :
					//nothing to do
					
				}
				
				var text = translator.get("chart.wizard.label.filter") + " " +operation +" " + values.join(" ; ");
				
				$("#filter-" + axis).text(text);

				
			}
			
		},
		
		translateEnum : function (values, myEnum){
			
			return _.chain(values)
			.flatten()
			.map(function(val){ 
				return _.chain(myEnum)
				.find(function(item) {return item.name == val;})
				.result("i18nkey")
				.value();	
			}).map(function(i18n){
				return translator.get(i18n);
			})
			.value();
			
		},
		
		updateModel : function() {
			var type = $("input[name='chart-type']:checked").val();
			
			
			var measure = this.find("y");
			var axis1 = this.find("x1");
			var axis2  = this.find("x2");	
			
			switch (type) {
			
			case "LINE" :  axis2 = null;
			break;
			
		    case "BAR" : 
			break;
			
		    case "PIE" : 
		    	measure = _.clone(axis1);
		    	measure.operation = "COUNT";
		    	axis2 = null;
			break;
			
		    case "TABLE" : //no table atm
			break;
			}
			
					
			var axis = axis2 === null ? [axis1]:[axis1].concat(axis2);
			
			this.model.set({type : type, measures : [measure], axis : axis});  
	
		},
		
		find : function (name){
			
			var self = this;
			var columnLabel = $("#axis-axis-"+ name).val();
	
			return {
				column : self.findColumnByLabel(columnLabel),
				operation : self.findChoosenOperation(columnLabel),
				label : ""
			};
		},
		
		findChoosenOperation : function (label){
			return  _.result( _.find(this.model.get("operations"), function (obj) {return obj.column.label ==label;}), "operation");
		},
		
		findColumnByLabel : function (label){
			return _.find(_.reduce(this.model.get("columnPrototypes"), function(memo, val){ return memo.concat(val); }, []), function(col){return col.label == label; });
		},
		
		findFilterByColumnLabel : function (label){
			return _.find(this.model.get("filters"), function(col){return col.column.label == label; });
		}
		
	});

	return typeStepView;

});