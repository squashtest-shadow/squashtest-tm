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
			data.name = "axis";
			this._initialize(data, wizrouter);
			this.reloadData();

		},
		
	
		
		reloadData : function() {
			
			var operations = this.model.get("operations");
			
			_.each(operations, function (op){
				
				$("#operations-operation-select-"+ op.column.id).val(op.operation); 
				
			});
			
		},
		
		
		updateModel : function() {
			
			var ids = _.pluck($(".operations-operation-select"), "name");
			
			var operations = this.getVals(ids);
			
			this.model.set({operations : operations}); 
		},
		
		getVals : function (ids) {
		
			var self = this;
			
			return _.map(ids, function(id){
				return {column : self.findColumnById(id),
					operation : $("#operations-operation-select-" + id).val() ,
				};
			});
			
			
		},
		
		findColumnById : function (id){
			return _.find(_.reduce(this.model.get("columnPrototypes"), function(memo, val){ return memo.concat(val); }, []), function(col){return col.id == id; });
		},


		
		
	});

	return axisStepView;

});