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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView", "squash.translator",  "custom-report-workspace/charts/chartFactory", "jquery.squash"],
	function($, backbone, _, Handlebars, AbstractStepView, translator, chart) {
	"use strict";

	var typeStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#type-step-tpl";
			this.model = data;
		    data.name = "type";
			this._initialize(data, wizrouter);
			this.precalculateInfoListItemData();
			this.reloadData();
			this.initGraphExample();
		},
		
	events : {

			"change .axis-select" : "changeAxis",
			"change .chart-type" : "changeType"
			
		},
		
		generate : function(){
			
		
			
			var self = this;
			
			self.updateModel();
			
			$.ajax({
				'type' : 'POST',
				'dataType' : 'json',
				'contentType' : 'application/json',
				'url' : squashtm.app.contextRoot + '/charts/instance',
				'data' : this.model.toJson()
			})
			.success(function(json){
				self.model.set({chartData : json});
				self.navigateNext();
			});
	
		},
		changeType : function (event){
			this.showAxisByType(event.target.value);
			this.changeExampleGraph(event.target.value);
		},
		
		initGraphExample : function (){
			
			var type = this.model.get("type") || "PIE";
			this.changeExampleGraph(type);
			
		},
		changeExampleGraph : function (type){
			
			$("#chart-display-area").html('');
			var fakeData; 
			
			switch (type)  {
			case "PIE":
			case "BAR":	
			case "CUMULATIVE": fakeData = this.getFakeData1();
            break;
			case "TREND": 		
			case "COMPARATIVE": fakeData = this.getFakeData2();
			break;
				
			}
			
			
			fakeData.type = type;
			chart.buildChart("#chart-display-area", fakeData);
			
		},	
		
		getFakeData1 : function () {
			
			return {"name":"",
				"measures":[{"label":"","columnPrototype":{"label":"REQUIREMENT_ID","specializedEntityType":{"entityType":"REQUIREMENT","entityRole":null}},"operation":{"name":"COUNT"}}],
				"axes":[{"label":"","columnPrototype":{"label":"REQUIREMENT_VERSION_CREATED_ON","specializedEntityType":{"entityType":"REQUIREMENT_VERSION","entityRole":null}},"operation":{"name":"BY_MONTH"}}],
				"filters":[],
				"abscissa":[[201502],[201503],[201504],[201505],[201506],[201507],[201508],[201509],[201510],[201511]],
				"series":{"":[1,1,3,1,1,1,4,2,5,2]}};
			
		},
		getFakeData2 : function() {
			
			var val = translator.get("chart.wizard.example.value");
			var ser = translator.get("chart.wizard.example.serie");
			
			var value = function(number) {return val + " " + number;};
			var serie = function(number) {return ser + " " + number;};
			
			
			return {"name":"",
				"measures":[{"label":"","columnPrototype":{"label":"REQUIREMENT_ID","specializedEntityType":{"entityType":"REQUIREMENT","entityRole":null}},"operation":{"name":"COUNT"}}],
				"axes":[{"label":"","columnPrototype":{"label":"REQUIREMENT_VERSION_CATEGORY","specializedEntityType":{"entityType":"REQUIREMENT_VERSION","entityRole":null}},"operation":{"name":"NONE"}},{"label":"","columnPrototype":{"label":"REQUIREMENT_VERSION_CRITICALITY","specializedEntityType":{"entityType":"REQUIREMENT_VERSION","entityRole":null}},"operation":{"name":"NONE"}}],
				"abscissa":[[value(1),serie(1)],[value(2),serie(1)],[value(3),serie(1)],[value(4),serie(1)],[value(1),serie(2)],[value(2),serie(2)],[value(3),serie(2)],[value(4),serie(2)], [value(1),serie(3)],[value(2),serie(3)],[value(3),serie(3)],[value(4),serie(3)],[value(1),serie(4)],[value(2),serie(4)],[value(3),serie(4)],[value(4),serie(4)]],
				"series":{"":[1,1,3,1,1,1,4,2, 3, 1, 4, 1, 0, 0, 5, 0]}};
			
		},
		
		showAxisByType : function (type) {
			
			switch (type) {
			case "PIE": this.showPieAxis();
				break;
			case "BAR":	 
			case "CUMULATIVE": this.showOneAxis();
            break;
			case "TREND": 	
			case "COMPARATIVE": this.showMultiAxis();
			break;
			
			}
			
			
		},
		showMultiAxis : function (){
			this.setVisible(["axis-y", "axis-x2"]);
			
		},
		showPieAxis : function () {	
			this.setInvisible(["axis-y", "axis-x2"]);			
		},
		
		showOneAxis : function () {
			this.setVisible(["axis-y"]);
			this.setInvisible(["axis-x2"]);
		},
		
		
		
		showLineAxis : function () {
			this.setVisible(["axis-y"]);
			this.setInvisible(["axis-x2"]);
		}, 
		
		setVisible : function (values){		
			_.each(values, function (val) {$("#" + val).show();});
			
		},
		setInvisible : function (values){
			_.each(values, function (val) {$("#" + val).hide();});
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
			
			case "CUMULATIVE" :  
		    case "BAR" : axis2 = null;
			break;
			
		    case "PIE" : 
		    	var entityType = axis1.column.specializedType.entityType == "REQUIREMENT_VERSION" ?  "REQUIREMENT" : axis1.column.specializedType.entityType;
		    	measure = {};
		    	measure.operation = "COUNT";		    	
		    	measure.column = _.chain(this.model.get("columnPrototypes"))
		    	.pick(entityType)
		    	.values()
		    	.flatten()
		    	.find(function(col) {return col.attributeName == "id";})
		    	.value();
		    	measure.label = "";
		    	
		    	axis2 = null;
			break;

			}
			
					
			var axis = _.isEmpty(axis2)  ? _.isEmpty(axis1) ? [] :[axis1]:[axis1].concat(axis2);
			
			
			var measures = measure === undefined ? [] :[measure];
			
			this.model.set({type : type, measures : measures, axis : axis});  
	
		},
		
		find : function (name){
			
			var self = this;
			var columnLabel = $("#axis-axis-"+ name).val();
	
			var result;
			
			if (! _.isEmpty(columnLabel)){
				result = {
					column : self.findColumnByLabel(columnLabel),
					operation : self.findChoosenOperation(columnLabel),
					label : ""
				};
			}
			
			return result;
		},
		
		findChoosenOperation : function (label){
			return  _.chain(this.model.get("operations"))
			.find(function (obj) {return obj.column.label ==label;})
			.result("operation")
			.value();

		},
		
		findColumnByLabel : function (label){
			return _.chain(this.model.get("columnPrototypes"))
			.values()
			.flatten()
			.find(function(col){return col.label == label; })
			.value();
		},
		
		findFilterByColumnLabel : function (label){
			return _.chain(this.model.get("filters"))
			.find(function(obj){return obj.column.label == label; })
			.value();
		}

		
	});

	return typeStepView;

});