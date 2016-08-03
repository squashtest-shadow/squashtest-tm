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
define(["jquery", "backbone", "underscore", "app/squash.handlebars.helpers", "./abstractStepView"],
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

			var self = this;
			
			var ids = _.pluck($('[id^="attributes-selection-"]').filter(":checked"), "name");
			
			this.model.set({selectedAttributes : ids});
			
			var filtered = 	_(['filters', 'axis', 'measures', 'operations'])
			.reduce(function(memo, val){ 
				memo[val] = self.filterWithValidIds(self.model.get(val)); 
				return memo; }, {});
			
			this.model.set(filtered);	
			
		},
		
		filterWithValidIds : function (col) {		
			var self = this;
			return _.chain(col)
			.filter(function(val){return _.contains(self.model.get("selectedAttributes"), val.column.id.toString());})
			.value();
			
		},
		
		
		
		
	
		
		
	});

	return attributesStepView;

});