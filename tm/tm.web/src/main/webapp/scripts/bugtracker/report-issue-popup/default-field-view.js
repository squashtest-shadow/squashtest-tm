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

define(["jquery", "backbone", "handlebars", "./BTEntity", "text!http://localhost:8080/squash/scripts/bugtracker/report-issue-popup/template.html!strip","jqueryui"], function($, Backbone, Handlebars, BTEntity, source){

	
	var ComboBox = Backbone.View.extend({
		
		initialize : function(){
			this.empty = (this.$el.find('option.issue-control-empty').length!=0);
			var evtname = 'change:'+this.options.attribute;
			
			this.model.on(evtname, this.updatecontrol, this);
			this.$el.on('change', $.proxy(this.updatemodel, this));
			
			if (!!this.empty){
				this.disable();
			}
		},
		
		getSelected : function(){
			var $el = this.$el;
			var id = $el.val();
			var name = $el.find("option:selected").text();
			return new BTEntity(id,name);
		},
		
		updatemodel : function(){
			var attribute = this.options.attribute;
			var selection = this.getSelected();
			var newValue = {};
			newValue[attribute] = selection;
			
			this.model.set(newValue);
		},
		
		updatecontrol : function(){
			var attribute = this.options.attribute;
			var value = this.model.get(attribute);
			if (! this.empty && !! value){
				this.$el.val(value.id);
			}
		},
		
		disable : function(){
			this.$el.attr('disabled', 'disabled');
		},
		
		enable : function(){
			if (! this.empty){
				this.$el.removeAttr('disabled');
			}
		}
		
	});
	
	var DefaultFieldView = Backbone.View.extend({
		
		
		// ****************** controls*******************
		
		remapControls : function(){
			
			var labels = this.options.labels;
			
			//the four selects
			this.prioritySelect = new ComboBox({
				el : this.$(".priority-select").get(0),
				model : this.model,
				attribute : 'priority'
			});
			
			this.categorySelect = new ComboBox({
				el : this.$(".category-select").get(0),
				model : this.model,
				attribute : 'category'
			});
			
			this.versionSelect = new ComboBox({
				el : this.$(".version-select").get(0),
				model : this.model,
				attribute : 'version'
			});
			
			this.assigneeSelect = new ComboBox({
				el : this.$(".assignee-select").get(0),
				model : this.model,
				attribute : 'assignee'
			});
			
			//the three text area
			this.summaryText = this.$(".summary-text");
			this.descriptionText = this.$(".description-text");
			this.commentText = this.$(".comment-text");			
		},
		
		enableControls : function(){			
			this.prioritySelect.enable();
			this.categorySelect.enable();
			this.versionSelect.enable();
			this.assigneeSelect.enable();
			this.summaryText.removeAttr('disabled');
			this.descriptionText.removeAttr('disabled');
			this.commentText.removeAttr('disabled');
				
		},
		
		disableControls : function(){			
			this.prioritySelect.disable();
			this.categorySelect.disable();
			this.versionSelect.disable();
			this.assigneeSelect.disable();
			this.summaryText.attr('disabled', 'disabled');
			this.descriptionText.attr('disabled', 'disabled');
			this.commentText.attr('disabled', 'disabled');		
		},
		

		//we must prevent keypress=enter event inside a textarea to bubble out and reach 
		//the submit button
		abortEnter : function(evt){
			if (evt.which == '13'){
				$.Event(evt).stopPropagation();
			}			
		},
		
		readModel : function(){
		
			var data = {
				'summary' : this.summaryText.val(),
				'description' : this.descriptionText.val(),
				'comment' : this.commentText.val()
			}
			
			this.model.set(data);		
	
		
		},
		// *********************** events *******************
		
		events : {
			"keypress .text-options" : "abortEnter"
		},
		
		
		// *********************** life cycle ***************

		
		initialize : function(){
			
			var template = Handlebars.compile(source);
			var data = {
				issue : this.model.attributes,
				labels : this.options.labels
			}
			
			var html = template(data);			
			
			this.$el.html(html);
			
			this.remapControls();
			this.reset();
			


			return this;

		}, 
		
		
		reset : function(){		
			var model = this.model;
			
			this.summaryText.val(model.get('summary'));
			this.descriptionText.val(model.get('description'));
			this.commentText.val(model.get('comment'));					
		}
		
	});
	
	return DefaultFieldView;
	
});