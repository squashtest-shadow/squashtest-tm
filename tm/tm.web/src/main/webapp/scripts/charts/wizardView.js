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
define([ "jquery", "backbone", "workspace.routing", "./entityStepView", "./scopeStepView", "./filterStepView", "./typeStepView", "./axisStepView", "./previewStepView", "./sideView" ], function($, Backbone,
		router, EntityStepView, ScopeStepView, FilterStepView, TypeStepView , AxisStepView, PreviewStepView, SideView) {

	"use strict";

	var wizardView = Backbone.View.extend({
		el : "#wizard",
		initialize : function(options) {
			this.model = options.model;
			this.steps = options.steps;
			this.model.set({
				steps:["entity", "scope", "filter", "type", "axis", "preview"]		
			});
		},
		
		showSideView : function(wizrouter){
			this.resetSideView();
			this.currentSideView = new SideView(this.model, wizrouter);
		},
		
		showNewStepView : function (wizrouter, View){	
			if (this.currentView !== undefined) {
			this.currentView.updateModel();
			}
			
			this.resetView();
			this.showSideView(wizrouter);
			this.currentView = new View(this.model, wizrouter);
		},
		
		showEntityStep : function(wizrouter) {			
			this.showNewStepView(wizrouter, EntityStepView);
		},
		showScopeStep : function(wizrouter) {
			this.showNewStepView(wizrouter, ScopeStepView);
		},

		showFilterStep : function(wizrouter) {
			this.showNewStepView(wizrouter, FilterStepView);
		},

		showTypeStep : function(wizrouter) {
			this.showNewStepView(wizrouter, TypeStepView);
		},
		
		showAxisStep : function(wizrouter) {
			this.showNewStepView(wizrouter, AxisStepView);
		},

		showPreviewStep :  function(wizrouter) {
			this.showNewStepView(wizrouter, PreviewStepView);
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
