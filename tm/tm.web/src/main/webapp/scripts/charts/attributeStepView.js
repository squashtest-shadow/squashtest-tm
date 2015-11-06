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

	var attributesStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#attributes-step-tpl";
			this.model = data;
			data.name = "attributes";
			this._initialize(data, wizrouter);
			

		},
		
		events : {

		},
		
		

		
		updateModel : function() {

			var ids = _.pluck($('[id^="attributes-selection-"]').filter(":checked"), "name");
			
			this.model.set({selectedAttributes : ids});
			
		}
		
	
		
		
	});

	return attributesStepView;

});