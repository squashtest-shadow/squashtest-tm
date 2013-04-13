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


define(["jquery", 
        "backbone", 
        "../widgets/widget-registry", 
        "../domain/FieldValue", 
        "text!http://localhost:8080/squash/scripts/bugtracker/report-issue-popup/template.html!strip", 
        "jqueryui"], 
		function($, Backbone, widgetRegistry, FieldValue, source){


	// *************** utilities ****************************
	
	function WidgetNotFound(name,original){
		this.name=name;
		this.original=original;
		this.toString = function(){
			return "widget { name : '"+this.name+"', original : '"+this.original+"'} not found.";
		} 
	}
	
	var logger = {
		log : function(message){
			if (console && console.log){
				console.log(message);
			}
		}
	}
	
	// ***************** widget helper **********************
	
	/*
	 * Note : part of this job is made asynchronously so the code might seem convoluted
	 * 
	 */
	var WidgetFactory = {
		
		registry : widgetRegistry,

		findFieldById : function(fields, id){
			for (var i=0, len = fields.length ; i<len ; i++){
				if (fields[i].id === id){
					return fields[i];
				}
			}
			return null;	//should never happen, right ?
		},
		
		/*
		 * The widget must first be loaded. But there are chances that the widget can't be found because there is counterpart in Squash to the widget 
		 * defined on the remote bugtracker.
		 * 
		 * Therefore fallback policy is : 
		 * - load and execute the expected widget (inputType.name).
		 * - if fails, load and execute the widget under the name the remote bugtracker knows it (inputType.original)
		 * - if it fails again : 
		 * 	 	-if the field is required, try with the default widget
		 *  	-else discard the field entirely.
		 */
		createWidget : function(domelt, field){

			var self = this;
			var inputType = field.rendering.inputType;

			
			//the case where all runs fine
			var allFine =  function(){
				self.appendWidget(domelt, field, inputType.name);
			};
			
			//fallback to inputType.original
			var fallback = function(){
				logger.log("field (id : '"+field.id+"') : widget "+inputType.name+" not found, fallback to "+inputType.original);
				self.appendWidget(domelt, field, inputType.original);
			}
			
			//worst case scenario
			var allFailed = function(){
				if (field.rendering.required){
					logger.log("field (id : '"+field.id+"') is required, proceeding with default widget");
					widgetRegistry.loadWidget(widgetRegistry.defaultWidget, function(){
						self.appendWidget(domelt, field, widgetRegistry.defaultWidget);
					});
				}
				else{
					logger.log("field (id : '"+field.id+"') is optional, item removed and skipped");
					$(domelt).remove();
				}				
			}
			
			//now let's run it
			widgetRegistry.loadWidget(inputType.name, allFine, function(){
				widgetRegistry.loadWidget(inputType.original, fallback, allFailed);
			});

		},
		
		appendWidget : function(fieldItem, field, widgetName){
			
			//create the element
			var domelt = $.squashbt[widgetName].createDom(field);
			
			//if it's a scheme selector, append a special class to it
			if (field.rendering.inputType.fieldSchemeSelector){
				domelt.addClass('scheme-selector');
			}
			
			//append that element to the dom
			var enclosingSpan = fieldItem.getElementsByTagName('span')[0];
			domelt.appendTo(enclosingSpan);
			
			//create the widget
			domelt[widgetName](field);
			
			//map the widget in domelt.data as 'widget' for easier reference
			var instance = domelt.data(widgetName);
			domelt.data('widget', instance);
		},
			
		processPanel : function(panel, fields){
			
			var items = panel.find('div.issue-field');
			var self=this;
			
			items.each(function(){
				var item = this;
				var id = item.getAttribute('data-fieldid');
				var field = self.findFieldById(fields, id);

				self.createWidget(item,field);
				
			});
		}
			
			
	}
	
	
	
	// ***************** main view ********************************
	
	var AdvancedFieldView = Backbone.View.extend({
		
		// properties set when init :
		fieldTpl : undefined,
		frameTpl : undefined,
		
		events : {
			"click input.optional-fields-toggle" : "toggleOptionalFields",
			"change .scheme-selector" : "changeScheme",
			"keypress" : "abortEnter"
		},
		

		initialize : function(){
					
			var $el = this.$el;
			
			//first, post process the source html and split into two templates
			this._initTemplates();
						
			//generate the main template (the 'frame')
			var data = {
				labels : this.options.labels
			};			
			var html = this.frameTpl(data);			
			$el.html(html);					
						

			//now we can render
			this.render();
			
		},
		
		
		render : function(){

			//flush the panels
			this._flushPanels();
			
			//prepare a default scheme if none is set already	
			this._setDefaultScheme();
			
			//get the fields that must be displayed
			var schemes = this.model.get('project').schemes;			
			var fields = schemes[this.model.get('currentScheme')];
			
			this.renderFieldPanel(fields, true);
			this.renderFieldPanel(fields, false);
			
			//rebinds the view
			this.delegateEvents();
			
		},
		

		changeScheme : function(evt){
			
			//set the new currentScheme
			var widget = $(evt.target).data('widget');
			var fieldid = $(evt.target).data('fieldid');
			var value = widget.fieldvalue();
			
			var selector = ""+fieldid+":"+value.id;
			this.model.set('currentScheme', selector);
			
			//checkout then checkin again to update the view
			this.readOut();
			this.readIn();
			
		},
		
		toggleOptionalFields : function(){
			this.$el.find('div.optional-fields div.issue-panel-container').toggleClass('not-displayed');
			var btn = this.$el.find('input.optional-fields-toggle');
			var txt = btn.val();
			(txt==="+") ? btn.val('-') : btn.val('+');
		},
		
		readIn : function(){
			
			//first, create the fields
			this.render();
			
			//now we can fill them
			var fieldValues = this.model.get('fieldValues');
			var allControls = this._getAllControls();
			
			for (var fieldId in fieldValues){
				var value 	= fieldValues[fieldId];
				var control = allControls.filter('[data-fieldid="'+fieldId+'"]');
				
				if (control.length>0){
					control.data('widget').fieldvalue(value);
				}
				
			}
			
			
		},
		
		readOut : function(){
			var newValues = {};
			var controls = this._getAllControls();
			controls.each(function(){
				var $this = $(this);
				var fieldid = $this.data('fieldid');
				var value = $this.data('widget').fieldvalue();
				
				newValues[fieldid] = value;
			});
			
			this.model.set('fieldValues', newValues);
		},
		
		enableControls : function(){
			var allControls = this._getAllControls();
			allControls.each(function(){
				$(this).data('widget').enable();
			});
		},
		
		disableControls : function(){
			var allControls = this._getAllControls();
			allControls.each(function(){
				$(this).data('widget').disable();
			});
			
		},
		
		
		//********************** the bowels ********************
		
		_getAllControls : function(){
			return this.$el.find("span.issue-field-control-holder").children();
		},
		
		_initTemplates : function(){
			var allHtml = $(source);
			
			var frameHtml = allHtml.filter(function(){return this.id==='containers-template'}).html();
			this.frameTpl = Handlebars.compile(frameHtml);						
			
			var fieldHtml = allHtml.filter(function(){return this.id==='fields-templates'}).html();
			this.fieldTpl = Handlebars.compile(fieldHtml);	//that one is saved for later on			
		},
		
		_setDefaultScheme : function(){
			var scheme = this.model.get('currentScheme'); 
			var project = this.model.get('project');
			if (scheme === null || scheme === undefined){
				//the following is weird but correct
				for (var schemeName in project.schemes){
					this.model.set('currentScheme', schemeName, {silent : true});
					break;
				}
			}						
		},
		
		renderFieldPanel : function(allFields, required){
			
			//some decisions to make
			var panelclass = (required) ? "required-fields" : "optional-fields";
			var fields = $.grep(allFields, function(field){return field.rendering.required === required})
			
			//generate the main content of the panel
			var panel = this.$el.find('div.'+panelclass+' div.issue-panel-container');
			
			var html = this.fieldTpl(fields);			
			panel.html(html);
			
			//generate the widgets
			WidgetFactory.processPanel(panel, fields);
			
		},
		
		_flushPanels : function(){
			$("div.issue-panel-container").empty();
		},
		
		//we must prevent keypress=enter event inside a textarea to bubble out and reach 
		//the submit button
		abortEnter : function(evt){
			if (evt.which == '13'){
				$.Event(evt).stopPropagation();
			}			
		}
		
		
	});
	
	return AdvancedFieldView;


});