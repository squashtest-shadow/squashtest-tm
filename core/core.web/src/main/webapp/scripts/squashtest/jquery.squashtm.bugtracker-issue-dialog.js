/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

function BTEntity(argId, argName){
	this.id = argId;
	this.name = argName;
	this.format= function(){
		return "id="+this.id+",name="+this.name;
	};
}

(function($){

	$.fn.btCbox = function(emptyMessage){
		
		var self = this;
		

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
			
			if ((entityArray.length == 1) && (entityArray[0].dummy)){
				var option = $("<option/>", { 
					'value' : entityArray[0].id, 
					'text' : emptyMessage 
				});
				this.append(option);	
				this.disable();
			}
			else{
				this.enable();
				var  i=0;			
				for (i=0;i<entityArray.length;i++){
					var entity = entityArray[i];
					var option = $("<option/>", { 
						'value' : entity.id, 
						'text' : entity.name 
					});
					this.append(option);			
				}
			}
		}
		
		this.getSelected = function(){
			var id = this.val();
			var name = this.find("option:selected").text();
			return new BTEntity(id, name);
		}
		
		return this;

	}



	/*
	  report-issue-dialog is the javascript object handling the behaviour of the popup that will post 
	  a new issue or attach an existing issue to the current entity.
	*/

	/*
		the settings object must provide :
		
		- labels : an object such as
			- emptyAssigneeLabel : label that should be displayed when the assignable user list is empty
			- emptyVersionLabel : same for version list
			- emptyCategoryLabel : same for category list
			- emptyPriorityLabel : same for priority
		- url : the url where gets and posts should use
		- callback : any callback function. Can accept one argument : the json status of the operation.

	*/

	$.fn.btIssueDialog = function(settings){

		var self = this;
		
		this.url = settings.url;
		
		//main panels of the popup
		this.pleaseWait = $(".pleasewait", this);
		this.content = $(".content", this);

		
		//the four selects
		this.prioritySelect = $(".priority-select", this.content).btCbox(settings.labels.emptyPriorityLabel);
		this.categorySelect = $(".category-select", this.content).btCbox("impossible - there cannot be no categories");
		this.versionSelect  = $(".version-select",  this.content).btCbox(settings.labels.emptyVersionLabel);
		this.assigneeSelect = $(".assignee-select", this.content).btCbox(settings.labels.emptyAssigneeLabel);


		//the three text area
		this.summaryText = $(".summary-text", this.content);
		this.descriptionText = $(".description-text", this.content);
		this.commentText = $(".comment-text", this.content);
		
		//the submit button
		this.postButton = $('.post-issue-button');
		
		//a variable where to hold the project entity
		this.projectEntity = null;
		this.callback=settings.callback;
		
		
		
		/* ************* private methods **************** */
		
		var flipToPleaseWait = $.proxy(function(){
			this.pleaseWait.show();
			this.content.hide();
		}, self);
		
		
		var flipToReport = $.proxy(function(){
			this.content.show();
			this.pleaseWait.hide();	
		}, self);

		
		var getBugReportData = $.proxy(function(){
			return $.ajax({
				url : self.url,
				type : "GET",
				dataType : "json"			
			});
		}, self);

		
		var enableButton = $.proxy(function(){
			this.postButton.button('option', 'disabled', false);
		}, self);
		
		
		var disableButton = $.proxy(function(){
			this.postButton.button('option', 'disabled', true);
		}, self);
		
		
		//we let the usual error handling do its job here
		var bugReportDataError = $.proxy(function(jqXHR, textStatus, errorThrown){
			flipToReport();
		}, self);
		
		
		var flushReport = $.proxy(function(){
			this.prioritySelect.flush();
			this.versionSelect.flush();
			this.assigneeSelect.flush(),
			this.categorySelect.flush();
		}, self);
		
		
		var fillReport = $.proxy(function(json){
			
			this.prioritySelect.populate(json.priorities);
			this.versionSelect.populate(json.versions);
			this.assigneeSelect.populate(json.users),
			this.categorySelect.populate(json.categories);		
		
			this.descriptionText.val(json.defaultDescription);
			this.projectEntity = new BTEntity(json.project.id, json.project.name);
			
		}, self);
		
		
		var submit = $.proxy(function(){
			
			issue.project = this.projectEntity.format();
			issue.priority = this.prioritySelect.getSelected().format();
			issue.version = this.versionSelect.getSelected().format();
			issue.assignee = this.assigneeSelect.getSelected().format();
			issue.category = this.categorySelect.getSelected().format();
			
			issue.summary=this.summaryText.val();
			issue.description=this.descriptionText.val();
			issue.comment=this.commentText.val();
			
			return $.ajax({
				url: this.url,
				type:"POST",
				dataType : "json",
				data : issue
			})
			
		}, self);
		
		
		var submitSuccess = $.proxy(function(json){
			this.dialog('close');
			disableButton();
			if (this.callback){
				this.callback(json);
			}
		}, self);
		
		
		var submitFails = $.proxy(function(){
			enableButton();
			flipToReport();
		}, self);
		
		var submit = $.proxy(function(){
			var issue ={};
			issue.project = this.projectEntity.format();
			issue.priority = this.prioritySelect.getSelected().format();
			issue.version = this.versionSelect.getSelected().format();
			issue.assignee = this.assigneeSelect.getSelected().format();
			issue.category = this.categorySelect.getSelected().format();
			
			issue.summary=this.summaryText.val();
			issue.description=this.descriptionText.val();
			issue.comment=this.commentText.val();
			
			return $.ajax({
				url: this.url,
				type:"POST",
				dataType : "json",
				data : issue
			})
			
		}, self);	

		
		/* ************* public ************************ */
		
		this.submitIssue = function(){
			
			disableButton();
			
			flipToPleaseWait();
			
			submit()
			.done(submitSuccess)
			.fail(submitFails);
		
		};
		
		/* ************* events ************************ */
		
		//the opening of the popup :
		this.bind("dialogopen", function(){
			
			disableButton();
			
			flipToPleaseWait();
			
			getBugReportData()
			.then(function(json)
			{		
				flushReport();
				fillReport(json);
				flipToReport();
				enableButton();
			})
			.fail(bugReportDataError);
		
		});
		
		//the action bound to click on the first button
		this.dialog('option').buttons[0].click=this.submitIssue;

		//we must prevent keypress=enter event inside a textarea to bubble out and reach 
		//the submit button
		$(".text-options", this.content).keypress(function(evt){
			if (event.which == '13'){
				$.Event(event).stopPropagation();
			}
		});
		
		return this;

	}

})(jQuery);
