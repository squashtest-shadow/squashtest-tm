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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView"],
	function($, backbone, _, Handlebars, AbstractStepView) {
	"use strict";

	var axisStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#axis-step-tpl";
			this.model = data;
			data.nextStep = "preview";
			data.prevStep = "type";		
			var src = $("#measure-operation-tpl").html();
			this.measureTemplate = 	Handlebars.compile(src);
			this._initialize(data, wizrouter);
			this.populateOperation("MEASURE");
			this.populateOperation("AXIS");
		},
		
		events : {
			"change #MEASURE" : "changeMeasure",
			"change #AXIS" : "changeAxis",	
		},
		
		updateModel : function() {

		},
		
		changeMeasure : function(event) {
			this.populateOperation("MEASURE");
		}, 

		changeAxis : function(event) {
			this.populateOperation("AXIS");
			
		}, 	
		populateOperation : function(role){
			var data = this.model.attributes;
			var selectedCol = _.find(data.columnPrototypes[data.selectedEntity] , function(col){return col.label == $("#" + role).val();});
			var operationsAllowedByType = data.dataTypes[selectedCol.dataType];
			var operationsAllowedByRole = data.columRoles[role];
			var permitedOperations = _.intersection(operationsAllowedByType, operationsAllowedByRole);	
			var operationSelector = this.measureTemplate({operations : permitedOperations});
			$("#" + role + "-operation-container").html(operationSelector);	
		}
		
		
	});

	return axisStepView;

});