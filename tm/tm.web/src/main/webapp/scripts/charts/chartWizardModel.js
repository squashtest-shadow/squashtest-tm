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
define([ "jquery", "backbone", "underscore","app/util/StringUtil"], function($, Backbone, _,stringUtil) {
	"use strict";

return Backbone.Model.extend({

	initialize : function(data){

		var self = this;
		var chartDef = data.chartDef;

		if (chartDef !== null){

		this.set({
                    updateId : chartDef.id,
                    name : chartDef.name,
                    type : chartDef.type,
                    axis: chartDef.axis,
                    owner : chartDef.owner,
                    scope : _.map(chartDef.scope, function(val){ val.type = val.type.replace("LIBRARY", "LIBRARIE");return val;}),
                    projectsScope : chartDef.projectScope,
                    scopeEntity : self.getScopeEntity(chartDef.scope),
		    measures : chartDef.measures,
		    operations : self.getOperations(chartDef),
		    filters : self.getFilters(chartDef),
		    selectedEntity : self.getSelectedEntities(chartDef),
		    selectedAttributes : self.getSelectedAttributes(chartDef),
		    filtered : [true]
		});

		}
	},

	getOperations : function (chartDef){
		return _.chain(chartDef)
		.pick('measures', 'axis')
		.values()
		.flatten()
		.map(function(val){return _(val).pick('column', 'operation');})
		.value();

	},

	getScopeEntity : function (scope){

		var val = _.chain(scope)
		.first()
		.result("type")
		.value();

		val = val.split("_")[0];

		if (val == "PROJECT") {
			val = "default";
		} else if (val == "TEST"){
			val = "TEST_CASE";
		}

		return val;

	},

	getFilters : function (chartDef){

		return _.chain(chartDef.filters)
		.map(function(filter) {  filter.values = [filter.values] ; return filter;})
		.value();

	},

	getSelectedAttributes : function (chartDef){

		return _.chain(chartDef)
		.pick('filters', 'measures', 'axis')
		.values()
		.flatten()
		.pluck('column')
		.pluck('id')
		.uniq()
		.map(function(val) {return val.toString();})
		.value();

	},

	getSelectedEntities : function (chartDef) {

	return _.chain(chartDef)
	.pick('filters', 'measures', 'axis')
	.values()
	.flatten()
	.pluck('column')
	.pluck('specializedType')
	.pluck('entityType')
	.uniq()
	.value();
	},



		toJson : function(param) {
			return JSON.stringify ({
			id : this.get('updateId') || null,
			name : this.get("name") || param,
			type : this.get("type"),
			query : {
				axis: this.get("axis"),
				measures : this.get("measures"),
				filters : _.map(this.get("filters"), function(filter) {var newFilter= _.clone(filter); newFilter.values = _.flatten(filter.values); return newFilter;})
			},
			owner : this.get("owner") || null,
			projectScope : this.get("projectsScope"),
			scope : _.map(this.get("scope"), function(val) {var newVal = _.clone(val); newVal.type = val.type.replace("LIBRARIE", "LIBRARY"); return newVal;})
			});
		}


		});


});
