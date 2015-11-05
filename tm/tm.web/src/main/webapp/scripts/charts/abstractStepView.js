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
define([ "jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "squash.translator" ], function($,
		backbone, _, Handlebars, translator) {
	"use strict";

	var steps = [ {
		name : "attributes",
		prevStep : "entity",
		nextStep : "filter",
		viewTitle : "chart.wizard.creation.step.attributes",
		stepNumber : 2,
		neededStep : ["entity"],
		buttons : ["previous", "next"]
	}, {
		name : "entity",
		prevStep : "",
		nextStep : "attributes",
		viewTitle : "chart.wizard.creation.step.entity",
		stepNumber : 1,
		buttons : ["next"]
	},{
		name : "axis",
		prevStep : "filter",
		nextStep : "type",
		viewTitle : "chart.wizard.creation.step.axis",
		stepNumber : 4,
		neededStep : ["entity", "attributes"],
		buttons : ["previous", "next"]
	},{
		name : "filter",
		prevStep  : "attributes",
	    nextStep : "axis",
		viewTitle : "chart.wizard.creation.step.filter",
		stepNumber : 3,
		neededStep : ["entity", "attributes"],
		buttons : ["previous", "next"]
	},{
		name : "preview",
		prevStep : "type",
		nextStep : "",
		viewTitle : "chart.wizard.creation.step.preview",
		stepNumber : 6,
		neededStep : ["entity", "attributes", "axis"],
		buttons : ["previous", "save"]
	},{
		name : "type",
		prevStep : "axis",
		nextStep : "preview",
		viewTitle : "chart.wizard.creation.step.type",
		stepNumber : 5,
		neededStep : ["entity", "attributes", "axis"],
		buttons : ["previous", "generate"]
	
	}
	];

	var validation = 
			[{
				name : "entity",
				validationParam : "selectedEntity"
			},{
				name :"attributes",
				validationParam : "selectedAttributes"
			},{
				name :"axis",
				validationParam : "operations"
			}];
	var abstractStepView = Backbone.View.extend({
		el : "#current-step",

		_initialize : function(data, wizrouter) {
			this.router = wizrouter;
			
			
			var currStep = _.findWhere(steps, {name : data.name});		
			this.next = currStep.nextStep;
			this.previous = currStep.prevStep;
			this.showViewTitle(currStep.viewTitle);
			this.initButtons(currStep.buttons);
			
			var missingStepNames = this.findMissingSteps(data, currStep.neededStep);
			
			if (_.isEmpty(missingStepNames)){
				this.render(data, $(this.tmpl));
			} else {
				
				var missingSteps = _.chain(steps)
				.filter(function(step){
					return _.contains(missingStepNames, step.name);
				})
				.sortBy("stepNumber")
				.value();

				var model = {steps : missingSteps, totalStep : steps.length};
				this.render(model, $("#missing-step-tpl"));
			}
			
			
		},
		
		findMissingSteps : function (data, neededStep) {
			
			return  _.filter(neededStep, function (step) {	
				var param = _.chain(validation)
				.find(function (val) {return val.name == step;})
				.result("validationParam")
				.value();	
				
				return _.isEmpty(_.result(data.attributes, param));
			});
			
			
		},
		initButtons : function (buttons){
			
			var allButtons = ["previous", "next", "save", "generate"];
			
			_.each(buttons, function(button) {
				var select = $("#" + button);
				select.show();
			});
			
			_.chain(allButtons).difference(buttons).each(function(button) {
				var select = $("#" + button);
				select.hide();
			}			
			);
	
		},

	

		navigateNext : function() {
			this.updateModel();
			this.router.navigate(this.next, {
				trigger : true
			});

		},

		updateModel : function() {
			// do in superclass
		},

		showViewTitle : function(title) {
			$("#step-title").text(translator.get(title));
		},

		navigatePrevious : function() {
			this.router.navigate(this.previous, {
				trigger : true
			});
		},

		render : function(data, tmpl) {
			var src = tmpl.html();
			this.template = Handlebars.compile(src);

			this.$el.append(this.template(data));

			return this;
		},

		destroy_view : function() {

			this.undelegateEvents();
			this.$el.removeData().unbind();
			this.remove();
			Backbone.View.prototype.remove.call(this);
		}

	});

	abstractStepView.extend = function(child) {
		var view = Backbone.View.extend.apply(this, arguments);
		view.prototype.events = _.extend({}, this.prototype.events, child.events);
		return view;
	};

	return abstractStepView;

});