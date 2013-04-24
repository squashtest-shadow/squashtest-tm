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
        "squash.translator",
        "../widgets/widget-registry", 
        "text!./advanced-view-template.html!strip",
        //"text!http://localhost:8080/squash/scripts/bugtracker/report-issue-popup/template.html!strip",
        "jqueryui"], 
		function($, Backbone, translator, widgetRegistry, source){


	// *************** utilities ****************************


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
				logger.log("bugtracker ui : field (id : '"+field.id+"') : widget "+inputType.name+" not found, fallback to "+inputType.original);
				self.appendWidget(domelt, field, inputType.original);
			}
			
			//worst case scenario
			var allFailed = function(){
				if (field.rendering.required){
					logger.log("bugtracker ui : field (id : '"+field.id+"') is required, proceeding with default widget");
					widgetRegistry.loadWidget(widgetRegistry.defaultWidget, function(){
						self.appendWidget(domelt, field, widgetRegistry.defaultWidget);
					});
				}
				else{
					logger.log("bugtracker ui : field (id : '"+field.id+"') is optional, item removed and skipped");
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
			
			try{
				//if it's a scheme selector, append a special class to it
				if (field.rendering.inputType.fieldSchemeSelector){
					domelt.addClass('scheme-selector');
				}
				
				domelt.addClass('full-width issue-field-control');
				
				//append that element to the dom
				var enclosingSpan = fieldItem.getElementsByTagName('span')[0];
				domelt.appendTo(enclosingSpan);
				
				//create the widget
				domelt[widgetName](field);
				
				//map the widget in domelt.data as 'widget' for easier reference
				var instance = domelt.data(widgetName);
				domelt.data('widget', instance);
				
			}catch(problem){
				domelt.remove();
				throw problem;
			}
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
		fileUploads : [],
		
		events : {
			"click input.optional-fields-toggle" : "toggleOptionalFields",
			"change .scheme-selector" : "changeScheme",
			"keypress" : "abortEnter"
		},
		

		initialize : function(){

			//first, post process the source html and split into two templates
			this._initTemplates();
						
			//generate the main template (the 'frame')
			var data = {
				labels : this.options.labels
			};			

			
		},
		
		
		render : function(){

			//flush the panels
			this._flushPanels();
			
			//prepare a default scheme if none is set already	
			this._setDefaultScheme();
			
			//get the fields that must be displayed
			var schemes = this.model.get('project').schemes;			
			var allFields = schemes[this.model.get('currentScheme')].slice(0);
			
			//make sure the required fields are displayed first. Note : native javascript array .sort() would fail to preserve the order of two elements equally ranked so I'm doing it manually
			var requiredFields = $.grep(allFields, function(field){ return field.rendering.required;});
			var optionalFields = $.grep(allFields, function(field){ return ! field.rendering.required;});
			var fields = requiredFields.concat(optionalFields);
			
			fields.sort(function(fielda, fieldb){
				
				var requiredA = fielda.rendering.required ? 1 : 0;
				var requiredB = fieldb.rendering.required ? 1 : 0;
				
				return requiredB - requiredA;
			
			});
			
			//generate the frame
			var html = this.frameTpl(fields);			
			this.$el.html(html);		
			
			//generate the main content of the panel
			var panel = this.$el.find('div.issue-panel-container');
			
			//generate the widgets
			WidgetFactory.processPanel(panel, fields);
			
			//rebinds the view
			this.delegateEvents();
			
		},
		

		changeScheme : function(evt){
			
			//set the new currentScheme
			var widget = $(evt.target).data('widget');
			var fieldid = $(evt.target).data('fieldid');
			var value = widget.fieldvalue();
			
			var selector = ""+fieldid+":"+value.scalar;
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
		
		//we ignore the file uploads, they'll be stored to be processed elsewhere.
		//note about uploading files ajax style : read http://www.ajaxf1.com/tutorial/ajax-file-upload-tutorial.html
		readOut : function(){
			
			var allFileUploads = [];
			
			var newValues = {};
			var controls = this._getAllControls();
			this.model.unset('isInvalid');
			var self = this;
			
			controls.each(function(){
				
				var $this = $(this);
				
				var fieldid = $this.data('fieldid');
				$(".issue-field-message-holder", $this.parent().parent()).text("");
				var validation = $this.data('widget').validate();
				if(!!validation.length){
					
					for(var i=0; i<validation.length; i++){
						$(".issue-field-message-holder", $this.parent().parent()).append(translator.get(validation[i])+"<br/>");
					}
					$(".issue-field-message-holder", $this.parent().parent()).show();

					self.model.set('isInvalid', true);
					
				}
				var value = $this.data('widget').fieldvalue();
				
				//has any file upload ?
				var uploads = $this.find('input[type="file"]');
				if (uploads.length>0){
					allFileUploads = allFileUploads.concat(uploads.get());
				}
				
				newValues[fieldid] = value;
				
			});
			
			this.model.set('fieldValues', newValues);
			
			this.fileUploads = allFileUploads;
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
		
		//returns only the items that were set
		getFileUploads : function(){
			return $.grep(this.fileUploads, function(item){
				var content =  $(item).val();
				return ! (content == "" || typeof (content) == 'undefined' ); 
			});
		},
		
		//********************** the bowels ********************
		
		_getAllControls : function(){
			return this.$el.find(".issue-field-control");
		},
		
		_initTemplates : function(){
			this.frameTpl = Handlebars.compile(source);
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