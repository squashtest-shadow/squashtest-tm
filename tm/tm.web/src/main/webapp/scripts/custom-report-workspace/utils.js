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
define(['jquery','underscore'], function($,_){
	
	return {
		btnconf : function(css){
			return {
				text : false,
				icons : {
					primary : css
				}
			};
		},
		/**
		 * Take all cufs from global vars, remove duplicate and return an array of all cufs
		 */
		extractCufsFromWorkspace : function () {
			//extracting all the cufs from the bindings
			var cufs = _.chain(squashtm.workspace.projects)
							.pluck("customFieldBindings")
							.map(function(entitiesBindings){
								return _.values(entitiesBindings);
							})
							.flatten()
							.pluck("customField")
							.uniq(function(customField){
								return customField.id;
							})
							.value();
			return cufs;							
		},

		getEmptyCufMap : function () {
			return {
				"REQUIREMENT_VERSION":[],
				"TEST_CASE":[],
				"CAMPAIGN":[],
				"ITERATION":[],
				"ITEM_TEST_PLAN":[],
				"EXECUTION":[]
			};
		},

		extractCufsMapFromWorkspace : function(){
			var cufMap = this.extractCufsBindingMapFromWorkspace();

			//extract cuf from bindings and remove duplicates (duplicates came from same cufs binded to same entity type on several projetct)
			return _.mapObject(cufMap,function(value){
				return _.chain(value)
							.pluck("customField")
							.uniq("id")
							.value();
				});
			},

			extractCufsBindingMapFromWorkspace : function() {
				var cufMap = this.getEmptyCufMap();
				var keys = _.keys(cufMap);
				//Exctracting all cufbindings and add them to cufmap by entity type
				_.chain(squashtm.workspace.projects)
					.pluck("customFieldBindings")
					.each(function(bindings){
						_.each(keys,function(key){
								var bindingsForEntityType = bindings[key] || [];
								cufMap[key] = cufMap[key].concat(bindingsForEntityType);
						});
					})
					.value();

				return cufMap;
			}
	};
});