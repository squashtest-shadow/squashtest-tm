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

define(["jquery", "backbone", "handlebars", "./BTEntity", "text!http://localhost:8080/squash/scripts/bugtracker/report-issue-popup/default-view-template.html!strip","jqueryui"], function($, Backbone, Handlebars, BTEntity, source){
	

	$.fn.btCbox = function(emptyMessage){
		
		var self = this;		
		
		this.empty=true;
		
	
		this.flush = function(){
			this.find("option").remove();
		}
		
		this.disable = function(){
			this.attr('disabled', 'disabled');
		}
		
		this.enable = function(){
			this.removeAttr('disabled');
		}
		
		this.populate = function(entityArray){
			
			this.flush();
			
			if ((entityArray.length == 1) && (entityArray[0].dummy)){
				var option = $("<option/>", { 
					'value' : entityArray[0].id, 
					'text' : emptyMessage 
				});
				this.append(option);	
				this.disable();
				this.empty=true;
			}
			else{
				//this.enable(); only the master control says if it's enabled
				var  i=0;			
				for (i=0;i<entityArray.length;i++){
					var entity = entityArray[i];
					var option = $("<option/>", { 
						'value' : entity.id, 
						'text' : entity.name 
					});
					this.append(option);			
				}
				this.empty=false;
			}
		}

		
		this.getSelected = function(){
			var id = this.val();
			var name = this.find("option:selected").text();
			return new BTEntity(id, name);
		}
		
		this.select = function(btEntity){
			if (
				this.isEmpty()	||
				(arguments.length ==0) ||
				(! btEntity)				
			){
				return;
			}else{
				this.val(btEntity.id);
			}
		}
		
		this.isEmpty = function(){
			return this.empty;
		};

		
		return this;

	}

	
	var DefaultFieldView = Backbone.View.extend({
		
		
		// ****************** controls*******************
		
		remapControls : function(){
			
			var options = this.options;
			
			//the four selects
			this.prioritySelect = this.$(".priority-select").btCbox(options.labels.emptyPriorityLabel);
			this.categorySelect = this.$(".category-select").btCbox("impossible - there cannot be no categories");
			this.versionSelect  = this.$(".version-select").btCbox(options.labels.emptyVersionLabel);
			this.assigneeSelect = this.$(".assignee-select").btCbox(options.labels.emptyAssigneeLabel);
			
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
		
		populateControls : function(){
			var project = this.model.get('project');
			this.prioritySelect.populate(project.priorities);
			this.categorySelect.populate(project.categories);
			this.versionSelect.populate(project.versions);
			this.assigneeSelect.populate(project.users);		
		},
		
		abortEnter : function(evt){
			if (evt.which == '13'){
				$.Event(evt).stopPropagation();
			}			
		},
		
		readModel : function(){
		
			var data = {
				'priority' : this.prioritySelect.getSelected(),
				'category' : this.categorySelect.getSelected(),
				'version' : this.versionSelect.getSelected(),
				'assignee' : this.assigneeSelect.getSelected(),
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
			var html = template();			
			
			this.$el.html(html);
			
			this.remapControls();
			this.populateControls();			
			this.reset();
			

			//we must prevent keypress=enter event inside a textarea to bubble out and reach 
			//the submit button
			/*this.$(".text-options").keypress(function(evt){
				if (evt.which == '13'){
					$.Event(evt).stopPropagation();
				}
			});*/
			
			return this;

		}, 
		
		
		reset : function(){		
			var model = this.model;
			this.prioritySelect.select(model.get('priority'));
			this.categorySelect.select(model.get('category'));
			this.versionSelect.select(model.get('version'));
			this.assigneeSelect.select(model.get('assignee'));
			
			this.summaryText.val(model.get('summary'));
			this.descriptionText.val(model.get('description'));
			this.commentText.val(model.get('comment'));					
		}
		
	});
	
	return DefaultFieldView;
	
});