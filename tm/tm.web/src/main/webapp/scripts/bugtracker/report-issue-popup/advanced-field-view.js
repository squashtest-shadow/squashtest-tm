/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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


define(["jquery", "backbone", "../widgets/widget-registry", "text!./advanced-view-template.html!strip", "jqueryui"], function($, Backbone, widgetRegistry, source){
	
	function FieldValue(id, value){
		this.id = id;
		this.scalar = null;
		this.composite = [];
		
		if (value instanceof Array){
			this.composite = value;
		}
		else{
			this.scalar = value;
		}
	}
	
	
	var AdvancedFieldView = Backbone.View.extend({
		
		initialize : function(){
			var template = Handlebars.compile(source);
			
			var data = {
				labels : this.options.labels
			};
			
			var html = template(data);
			
			this.$el.html(html);
			
		},
		
		readIn : function(){
			
		},
		
		readOut : function(){
			
		},
		
		enableControls : function(){
			
		},
		
		disableControls : function(){
			
		}
	});


});