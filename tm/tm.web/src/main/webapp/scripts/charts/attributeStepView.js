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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "./abstractStepView","../custom-report-workspace/utils","./customFieldPopup"],
	function($, backbone, _, Handlebars, AbstractStepView,chartUtils,CustomFieldPopup) {
	"use strict";

	var attributesStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#attributes-step-tpl";
			this.model = data;
			data.name = "attributes";
			this.model.set("cufMapByEntity",chartUtils.extractCufsMapFromWorkspace());
			this.model.set("computedColumnsPrototypes",this.computeColumnsPrototypes());
			this._initialize(data, wizrouter);
			//listen to changes in cuf selected attributes
			this.listenTo(this.model, 'change:selectedCufAttributes', this.updateSelectedAttributesWithCuf);
			this.initializeCufCheckBox();
		},
		
		events : {
			"click .wizard-cuf-btn" : "openCufPopup"
		},
	
		updateModel : function() {

			var self = this;
			var ids = _.pluck($('[id^="attributes-selection-"]').filter(":checked"), "name");
			var selectedAttributes = [];
			if (ids && ids.length > 0) {
				selectedAttributes.push(ids);
			}
			selectedAttributes.push(this.model.get);
			this.model.set({selectedAttributes : ids});

			//now retrieve the selected entities type to updated filter and operation view
			var selectedEntities = _.chain(ids)
				.map(function(id){
					var allProtos = _.chain(self.model.get("computedColumnsPrototypes")).values().flatten().value();
					return _.find(allProtos,function(proto){
						return proto.id === id || proto.id.toString() === id;
					});
				})
				.map(function(proto){
					return proto.specializedType.entityType;
				})
				.uniq()
				.value();

			this.model.set({"selectedEntity" : selectedEntities});
			
		},
		
		filterWithValidIds : function (col) {		
			var self = this;
			return _.chain(col)
			.filter(function(val){return _.contains(self.model.get("selectedAttributes"), val.column.id.toString());})
			.value();
			
		},
		
		//as in database we have not a real column prototype for each cu, we need to create synthetic prototype client side.
		//the prototypes for cuf in database are generic for one data type and one entity type. The cuf id isn't stored in prototype but directly in axis, filter or measure.
		computeColumnsPrototypes : function () {
			var initialColumnsPrototypes = this.model.get('columnPrototypes');

			//1 creating synthetics prototypes and merging with natural
			var mergedProto = this.mergeProtoypes(initialColumnsPrototypes);
			
			//2 reorder to follow the squashtm workspace order
			var orderedProtos = _.pick(mergedProto,["REQUIREMENT","REQUIREMENT_VERSION","TEST_CASE","CAMPAIGN","ITERATION","ITEM_TEST_PLAN","EXECUTION"]);

			return orderedProtos;
		},
		
		getSelectedProject : function () {
			var projectsScope = this.model.get('projectsScope');
			return _.filter(squashtm.workspace.projects,function (project) {
				return _.contains(projectsScope,project.id);
			});
		},

		//This function will return a map with synthetic column proto for cuf merged into the original map of prototypes
		mergeProtoypes : function (selectedEntitiesColumnsPrototypes) {
			var cufPrototypes = [];
			var mapOfNaturalPrototypes = {};

			//first we separate all generic cuf column prototypes from initial list of column prototype from the other one (attributes and calculated)
			_.each(selectedEntitiesColumnsPrototypes, function (prototypes, key) {
				//grouping by column type
				var groupedcolumnsPrototype = _.groupBy(prototypes,function(prototype){
					return prototype.columnType;
				});
				//extracting cuf prototype for this entity type and put in array of all cuf column proto
				var cufPrototypesForOneEntityType = groupedcolumnsPrototype["CUF"];
				if(cufPrototypesForOneEntityType){
					cufPrototypes = cufPrototypes.concat(cufPrototypesForOneEntityType);
				}
				//now inject into computedColumnsPrototypes all the natural column prototypes
				var naturalPrototypes = groupedcolumnsPrototype["ATTRIBUTE"];
				naturalPrototypes = naturalPrototypes.concat(groupedcolumnsPrototype["CALCULATED"]);
				mapOfNaturalPrototypes[key] = naturalPrototypes;
			});

			//now we create the map of synthetic column proto
			//first we create a map of all cuf binding for projects in perimeter
			var cufBindingMap = this.getCufProjectMap();
			//now we generate the synthetics columns prototypes
			var syntheticColumnPrototypes = this.getCufProtoForBindings(cufBindingMap,cufPrototypes);

			//finally we merge the the two maps and return the result
			var mergedPrototypes = chartUtils.getEmptyCufMap();
			_.each(mapOfNaturalPrototypes,function (values,key) {
				var syntheticColumnPrototypesForEntity = syntheticColumnPrototypes[key];
				if(syntheticColumnPrototypesForEntity && syntheticColumnPrototypesForEntity.length > 0){
					var allProto = values.concat(syntheticColumnPrototypesForEntity);
					mergedPrototypes[key] = allProto;
				}
				else{
					mergedPrototypes[key] = values;
				}
			});

			return mergedPrototypes;
		},

		//return a map with cuf bindings by entitity type : {"CAMPAIGN":[{cufBinding1},{cufBinding2}],"ITERATION":[{cufBinding1},{cufBinding2}]...}
		getCufProjectMap : function () {
			var selectedProjects = this.getSelectedProject();
			var scopeType = this.model.get('scopeType');
			var self = this;
			var cufMap = _.reduce(selectedProjects,function (memo, project) {
				_.each(project.customFieldBindings,function (values,key) {
					if(values.length > 0 && memo.hasOwnProperty(key)){
							memo[key] = memo[key].concat(values);
					}
				});
				return memo;
			},chartUtils.getEmptyCufMap());

			// if the perimeter type is default or selected project, we want only cuf in the project scope
			// but if the perimeter type is is custom, we want only the project scope for the specified entity and all cuf of the database for the others entities 
			// as we can't infer the joins that can be made between projects entities (eg a requirement can be linked to any TC so the cufs for TC must cover everything)
			// An alternative could be to make an ajax request to find all linked entities and adjust cuf but it will be too complex for a small gain, and will introduce issues for custom reports in workspaces
			if (scopeType === "CUSTOM") {
				this.appendAdditionnalCufBinding(cufMap);
			}
			//Now we filter out duplicates induced by selected several project with the same cuf binded to same entity type
			//we only want one instance of each cuf-entityType pair
			cufMap = _.mapObject(cufMap,function(bindings,entityType) {
				return _.uniq(bindings,function(binding) {
					return binding.customField.id;
				});
			});

			return cufMap;
		},

		appendAdditionnalCufBinding : function(cufBindingMap) {
			var entityPerimeter = this.model.get("scopeEntity");
			var emptyCufBindingsMap = this.getEmptycufBindingMapFilterd(entityPerimeter);
			var allcufBindingMap = chartUtils.extractCufsBindingMapFromWorkspace();
			_.each(emptyCufBindingsMap,function(value,entityType) {
				var additionnalCuf =  allcufBindingMap[entityType];
				if(cufBindingMap[entityType]){
					cufBindingMap[entityType] = cufBindingMap[entityType].concat(additionnalCuf);
				}
				else {
					cufBindingMap[entityType] = additionnalCuf;
				}
				
			});
		},

		getEmptyCufMap : function () {
			return {
				"REQUIREMENT_VERSION":[],
				"TEST_CASE":[],
				"CAMPAIGN":[],
				"ITERATION":[],
				"ITEM_TEST_PLAN":[],
				"EXECUTION":[]
			};
		},

		getEmptycufBindingMapFilterd :function(entityPerimeter) {
			switch (entityPerimeter) {
				case 'REQUIREMENT':
					return {
						"TEST_CASE":[],
						"CAMPAIGN":[],
						"ITERATION":[],
						"ITEM_TEST_PLAN":[],
						"EXECUTION":[]
					};
				case 'TEST_CASE':
					return {
						"REQUIREMENT_VERSION":[],
						"CAMPAIGN":[],
						"ITERATION":[],
						"ITEM_TEST_PLAN":[],
						"EXECUTION":[]
					};
				case 'CAMPAIGN':
					return {
						"REQUIREMENT_VERSION":[],
						"TEST_CASE":[]
					};
			}
		},

		getCufProtoForBindings : function (bindingMap,cufPrototypes) {
			var protoForCufBinding = chartUtils.getEmptyCufMap();
			var self = this;
			_.each(bindingMap,function (values,key) {
				var generatedPrototypes = _.map(values,function (cufBinding) {
					//1 find the proto name
					var protoLabel = key + "_" + self.getProtoSuffix(cufBinding);
					//2 find the prototype and upgrade it with cufCode and label
					var cufPrototype = _.find(cufPrototypes,function (proto) {
						return proto.label === protoLabel;
					});
					if (cufPrototype) {
						cufPrototype = _.clone(cufPrototype);
						cufPrototype.code = cufBinding.customField.code;
						cufPrototype.cufLabel = cufBinding.customField.label;
						cufPrototype.cufName = cufBinding.customField.name;
						cufPrototype.cufId = cufBinding.customField.id;
						cufPrototype.isCuf = true;
						cufPrototype.originalPrototypeId = cufPrototype.id;
						cufPrototype.id = cufPrototype.id + "-" + cufBinding.customField.id;
						cufPrototype.cufType = cufBinding.customField.inputType.enumName;
						cufPrototype.cufTypeFriendly = cufBinding.customField.inputType.friendlyName;
						if (cufPrototype.cufType === "DROPDOWN_LIST" || cufPrototype.cufType === "TAG") {
							cufPrototype.cufListOptions = cufBinding.customField.options;
						}
						return cufPrototype;
					}
				});
				if(generatedPrototypes){
					protoForCufBinding[key] = generatedPrototypes;
				}
			});
			return protoForCufBinding;
		},

		getProtoSuffix : function (value) {
			var suffix;
			switch (value.customField.inputType.enumName) {
				case "PLAIN_TEXT":
					suffix = "CUF_TEXT";
					break;
				case "CHECKBOX":
					suffix = "CUF_CHECKBOX";
					break;				
				case "DROPDOWN_LIST":
					suffix = "CUF_LIST";
					break;				
				case "DATE_PICKER":
					suffix = "CUF_DATE";
					break;				
				case "TAG":
					suffix = "CUF_TAG";
					break;
				case "NUMERIC":
					suffix = "CUF_NUMERIC";
					break;
			}
			return suffix;
		},

		openCufPopup : function(event) {
			var self = this;
			var entityType = event.target.getAttribute("data-entity");
			var cufToDisplay = _.mapObject(this.model.get("computedColumnsPrototypes"),function(prototypes,entityType) {
				return _.filter(prototypes,function(proto) {
					return proto.columnType === "CUF";
				});
			});
			this.model.set("cufToDisplay",cufToDisplay[entityType]);
			this.model.set("selectedCufEntity",entityType);
			var ids = _.pluck($('[id^="attributes-selection-"][data-cuf="true"]').filter(":checked"), "name");
			this.model.set("selectedCufAttributes",ids);
			var cufPopup = new CustomFieldPopup(this.model);
		},

		//callback executed when selected cuf changes
		updateSelectedAttributesWithCuf:function(model, newSelectedIds, options) {
			var self = this;
			var previousSelectedIds = model.previous("selectedCufAttributes");
			//damned, we have to play with lot's of manual change. A simple viewSate = fn(state) "a la react/redux" would be much cleaner...
			var idsToHide = _.difference(previousSelectedIds, newSelectedIds);
			_.each(idsToHide, function(id) {
				self.hideCufCheckBox(id);
			});

			var idsToShow = _.difference(newSelectedIds, previousSelectedIds);
			_.each(idsToShow, function(id) {
				self.showCufCheckBox(id);
			});
		},

		initializeCufCheckBox :function() {
			var ids = this.model.get("selectedCufAttributes") || [];
			var self = this;
			_.each(ids,function(id) {
				self.showCufCheckBox(id);
			});
		},

		showCufCheckBox : function(id) {
			var checkBoxSelector = '[id="attributes-selection-'+ id + '"]';
				var checkBox = this.$el.find(checkBoxSelector);
				checkBox.prop("checked",true);
				var checkBoxWrapperSelector = '[id="wrapper-attributes-selection-'+ id + '"]';
				var wrapper = this.$el.find(checkBoxWrapperSelector);
				wrapper.addClass("chart-wizard-visible");
				wrapper.removeClass("chart-wizard-hidden");
		},

		hideCufCheckBox : function(id) {
			var checkBoxSelector = '[id="attributes-selection-'+ id + '"]';
				var checkBox = this.$el.find(checkBoxSelector);
				checkBox.prop("checked",false);
				var checkBoxWrapperSelector = '[id="wrapper-attributes-selection-'+ id + '"]';
				var wrapper = this.$el.find(checkBoxWrapperSelector);
				wrapper.removeClass("chart-wizard-visible");
				wrapper.addClass("chart-wizard-hidden");
		}
	});
	return attributesStepView;
});