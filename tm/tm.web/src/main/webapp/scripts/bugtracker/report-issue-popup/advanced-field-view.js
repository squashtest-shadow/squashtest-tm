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


define(["jquery", "backbone", "../widgets/widget-registry", "text!http://localhost:8080/squash/scripts/bugtracker/report-issue-popup/template.html!strip", "jqueryui"], function($, Backbone, widgetRegistry, source){
	
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
		
		// properties set when init :
		
		currentScheme : undefined,
		fieldTemplate : undefined,
		
		events : {
			"click input.optional-fields-toggle" : "toggleOptionalFields",
			"change .scheme-selector" : "changeScheme"
		},
		
		initialize : function(){
					
			var $el = this.$el;
			
			//first, post process the source html and split into two templates
			var allHtml = $(source);
			
			var frameHtml = allHtml.filter(function(){return this.id==='containers-template'}).html();
			var frameTpl = Handlebars.compile(frameHtml);						
			
			var fieldHtml = allHtml.filter(function(){return this.id==='fields-templates'}).html();
			this.fieldTpl = Handlebars.compile(fieldHtml);	//that one is saved for later on
			
			
			//generate the main template (the 'frame')
			var data = {
				labels : this.options.labels
			};			
			var html = frameTpl(data);			
			$el.html(html);
					
						
			//prepare a default scheme.
			var project = this.model.get('project');
			//the following is weird but correct
			for (var schemeName in project.schemes){
				this.currentScheme = schemeName;
				break;
			}
			
			//render
			this.render();
		},
		
		
		render : function(){

			//get the fields that must be displayed
			var schemes = this.model.get('project').schemes;			
			var fields = schemes[this.currentScheme];
			
			this.renderFieldPanel(fields.slice(0), true);
			this.renderFieldPanel(fields.slice(0), false);
			
		},
		
		renderFieldPanel : function(fields, required){
			var panelclass = (required) ? "required-fields" : "optional-fields";
			$.grep(fields, function(field){return field.rendering.required === required})
			
			var html = this.fieldTpl(fields);
			this.$el.find('div.'+panelclass+' div.issue-panel-container').html(html);
			
		},
		
		changeScheme : function(evt){

		},
		
		toggleOptionalFields : function(){
			this.$el.find('div.optional-fields div.issue-panel-container').toggleClass('not-displayed');
			var btn = this.$el.find('input.optional-fields-toggle');
			var txt = btn.val();
			(txt==="+") ? btn.val('-') : btn.val('-');

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
	
	return AdvancedFieldView;


});