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
define([ "jquery", "backbone", "workspace.routing", "./entityStepView", "./scopeStepView", "./filterStepView", "./typeStepView", "./axisStepView", "./previewStepView" ], function($, Backbone,
		router, EntityStepView, ScopeStepView, FilterStepView, TypeStepView , AxisStepView, PreviewStepView) {

	"use strict";

	var wizardView = Backbone.View.extend({
		el : "#wizard",
		initialize : function(options) {
			this.model = options.model;
			this.steps = options.steps;
		},

		showEntityStep : function(wizrouter) {
			this.resetView();
			this.currentView = new EntityStepView(this.model, wizrouter);


		},
		showScopeStep : function(wizrouter) {
			this.resetView();
			this.currentView = new ScopeStepView(this.model, wizrouter);
		},

		showFilterStep : function(wizrouter) {
			this.resetView();
			this.currentView = new FilterStepView(this.model, wizrouter);

		},

		showTypeStep : function(wizrouter) {
			this.resetView();
			this.currentView = new TypeStepView(this.model, wizrouter);
		},
		
		showAxisStep : function(wizrouter) {
			this.resetView();
			this.currentView = new AxisStepView(this.model, wizrouter);
		},

		showPreviewStep :  function(wizrouter) {
			this.resetView();
			this.currentView = new PreviewStepView(this.model, wizrouter);
		},
		
		resetView : function() {
			console.log(this.model);
			if (this.currentView !== undefined) {
				this.currentView.destroy_view();
				$("#current-step-container").html('<span id="current-step" />');
			}

		}

	});

	return wizardView;

});
