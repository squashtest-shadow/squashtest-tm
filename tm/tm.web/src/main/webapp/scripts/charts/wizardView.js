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
define([ "jquery", "backbone", "workspace.routing", "squash.translator", "./entityStepView", "./filterStepView", "./typeStepView", "./axisStepView", "./previewStepView", "./sideView", "./attributeStepView", "jquery.squash.togglepanel" ], function($, Backbone,
		router, translator, EntityStepView, FilterStepView, TypeStepView , AxisStepView, PreviewStepView, SideView, AttributeStepView) {

	"use strict";

	var wizardView = Backbone.View.extend({
		el : "#wizard",
		initialize : function(options) {
		
			this.model = options.model;
			this.model.set({
				steps:["entity", "attributes", "filter", "axis", "type", "preview"]	,
			   perimSelect :[{text:"label.testCase" , name:"TEST_CASE"}, {text:"label.campaigns" , name:"CAMPAIGN"}, {text:"label.requirements" , name:"REQUIREMENT"}]
			});
			this.loadI18n();
		},
		
		events : {
			"click #next" : "navigateNext",
			"click #previous" : "navigatePrevious",
		    "click #generate" : "generate",
			"click #save" : "save"
		},
		
		navigateNext : function (){
			this.currentView.navigateNext();
		},
		
		navigatePrevious : function (){
			this.currentView.navigatePrevious();
		},
		
		generate : function (){
			this.currentView.generate();
		},
		save : function() {
			this.currentView.save();
		},
		
		loadI18n : function (){
			
			var chartTypes = this.addPrefix(this.model.get("chartTypes"), "chartType.");
			var entityTypes = this.addPrefix(_.keys(this.model.get("entityTypes")), "entityType.");
			var operation = this.addPrefix(_.uniq(this.flatten(this.model.get("dataTypes"))), "operation.");
			var column = this.addPrefix(_.pluck(this.flatten(this.model.get("columnPrototypes")), "label") ,"column.");
			
			var keys = chartTypes.concat(entityTypes, operation, column);
			
			var result = this.addPrefix(keys, "chart.");
			
			translator.load(result);
			
		},
		
		flatten : function (col) {		
			return _.reduce(col, function(memo, val) {return memo.concat(val);}, []);			
		},
		
		addPrefix : function(col, prefix){
			return _.map(col, function (obj){
				return prefix + obj;
			});
			
		},
		
		showSideView : function(){
			this.resetSideView();
			this.currentSideView = new SideView(this.model);
		},
		
		showNewStepView : function (View, wizrouter){	
			if (this.currentView !== undefined) {
			this.currentView.updateModel();
			}
			
			this.resetView();
			this.showSideView();
			this.currentView = new View(this.model, wizrouter);

		},
		
		showEntityStep : function(wizrouter) {			
			this.showNewStepView(EntityStepView, wizrouter);
		},
		
		showFilterStep : function(wizrouter) {
			this.showNewStepView(FilterStepView, wizrouter);
		},

		showTypeStep : function(wizrouter) {
			this.showNewStepView(TypeStepView, wizrouter);
		},
		
		showAxisStep : function(wizrouter) {
			this.showNewStepView(AxisStepView, wizrouter);
		},

		showPreviewStep :  function(wizrouter) {
			this.showNewStepView(PreviewStepView, wizrouter);
		},
		
		showAttributesStep : function(wizrouter){
			this.showNewStepView(AttributeStepView, wizrouter);
		},
		resetView : function() {
			console.log(this.model);
			if (this.currentView !== undefined) {
				this.currentView.destroy_view();
				$("#current-step-container").html('<span id="current-step" />');
			}

		},
		
		resetSideView : function() {
		
			if (this.currentSideView !== undefined) {
				this.currentSideView.destroy_view();
				$("#current-side-view-container").html('<span id="side-view" />');
			}
		}

	});

	return wizardView;

});
