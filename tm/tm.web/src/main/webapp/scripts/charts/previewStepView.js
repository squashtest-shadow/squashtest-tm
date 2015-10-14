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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView", "workspace.routing"],
	function($, backbone, _, Handlebars, AbstractStepView, router) {
	"use strict";

	var previewStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#preview-step-tpl";
			this.model = data;
			data.nextStep = "";
			data.prevStep = "axis";
			this._initialize(data, wizrouter);
		},
		
		events : {
			"click #preview" : "preview",
			"click #save" : "save"
			
		},
		
		preview : function(){
			
		},
		
		save : function () {
			
			$.ajax({
				method : "POST",
				contentType: "application/json",
				url : router.buildURL("chart.new"),
				data : this.model.toJson()
				
			});
			
			
		},
		
		updateModel : function() {

		}
		
	});

	return previewStepView;

});