/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
define(
		[ "jquery", "backbone", "underscore", "app/util/StringUtil" ],
		function($, Backbone, _, StringUtil) {
			function isBlank(val) {
				return StringUtil.isBlank(val);
			}

			/*
			 * Defines the model for a new Dataset
			 */
			var NewDatasetModel = Backbone.Model
					.extend({
					// url : need to be passed to constructor with test case id
						defaults : {
							name : "",
							paramValues : [][2]
						},
						
						initialize :function(){
							this.paramValueChanged = $.proxy(this._paramValueChanged, this);
							this.findParamValue = $.proxy(this._findParamValue, this);
							this.defaults.paramValues = this.attributes.paramValues;
						},

						validateAll : function() {
							var attrs = this.attributes, errors = null;							
							if (isBlank(attrs.name)) {
								errors = errors || {};
								errors.name = "message.notBlank";
							}

							return errors;
						},
						
						_paramValueChanged : function(id, value){
							var paramValue = this.findParamValue(id);
							paramValue[1] = _.escape(value);
						},
						
						_findParamValue : function(id){
						var paramValues = this.attributes.paramValues;
							for(var i=0; i < paramValues.length; i++){
								if(paramValues[i][0] == id){
									return paramValues[i];
								}
							}
						}
					});
			return NewDatasetModel;
		});