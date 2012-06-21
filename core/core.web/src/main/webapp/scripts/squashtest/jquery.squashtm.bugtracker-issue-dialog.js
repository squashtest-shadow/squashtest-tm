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
		
		this.empty=true;
		
		this.changeCallback = function(){};
		

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
		
		- reportUrl : the url where to GET empty/POST filled bug reports
		- findUrl : the url where to GET remote issues
		
		- callback : any callback function. Can accept one argument : the json status of the operation.

	*/
	
	function init(settings){

		var self = this;
		
		this.model={};
		this.template=null;
		
		
		//urls
		this.reportUrl = settings.reportUrl;
		this.searchUrl = settings.searchUrl;
		
		
		//main panels of the popup
		this.pleaseWait = $(".pleasewait", this);
		this.content = $(".content", this);

		//the radio buttons
		this.attachRadio = $(".attach-radio", this);
		this.reportRadio = $(".report-radio", this);
				
		//the issue id (if any)
		this.idText = $(".id-text", this);
		
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
		this.postButton = $('.post-button', this.next());
		
		//search issue buttons. We also turn it into a jQuery button on the fly.
		this.searchButton = $('.attach-issue input[type="button"]', this).button();
		
		//a callback when the post is a success
		this.callback=settings.callback;
			
		//setting up the callbacks for the controls
		with(this){
			idText.keydown(function(){
				self.model.id = $(this).val();
			});
			
			prioritySelect.change(function(){
				self.model.priority = this.getSelected();
			});
			
			categorySelect.change(function(){
				self.model.category = this.getSelected();
			});
			
			versionSelect.change(function(){
				self.model.version = this.getSelected();
			});
			
			assigneeSelect.change(function(){
				self.model.assignee = this.getSelected();
			});	
			
			summaryText.keydown(function(){
				self.model.summary = $(this).val();
			});
			
			descriptionText.keydown=(function(){
				self.model.description = $(this).val();
			});
			
			commentText.keydown=(function(){
				self.model.comment = $(this).val();
			});

		}
	
	}

	$.fn.btIssueDialog = function(settings){

		var self = this;
		
		init.call(this, settings);
			

		
		/* ************* public popup state methods **************** */
		
		this.attachRadio.click(function(){
			toAttachMode();
		});
			
		this.reportRadio.click(function(){
			toReportMode();
		});	
		
		var toAttachMode = $.proxy(function(){
			flipToMain();
			flushSheet();
			enableIdSearch();
			disableControls();
			disablePost();
		}, self);
		
		var toReportMode = $.proxy(function(){
			flipToMain();
			flushSheet();
			disableIdSearch();
			enableControls();
			enablePost();
			resetModel();
		}, self);
		

		var flipToPleaseWait = $.proxy(function(){
			this.pleaseWait.show();
			this.content.hide();
		}, self);
		
		
		var flipToMain = $.proxy(function(){
			this.content.show();
			this.pleaseWait.hide();	
		}, self);
	
		var enablePost = $.proxy(function(){
			this.postButton.button('option', 'disabled', false);
		}, self);
		
		
		var disablePost = $.proxy(function(){
			this.postButton.button('option', 'disabled', true);
		}, self);

		
		var enableIdSearch = $.proxy(function(){
			with(this){
				idText.removeAttr('disabled');
				searchButton.button('option', 'disabled', false);
			}
		}, self);
		
		var disableIdSearch = $.proxy(function(){
			with(this){
				idText.attr('disabled', 'disabled');
				searchButton.button('option', 'disabled', true);
			}
		}, self);
		
		var enableControls = $.proxy(function(){
			with(this){
				prioritySelect.enable();
				categorySelect.enable();
				versionSelect.enable();
				assigneeSelect.enable();
				summaryText.removeAttr('disabled');
				descriptionText.removeAttr('disabled');
				commentText.removeAttr('disabled');
			}		
		}, self);
		
		var disableControls = $.proxy(function(){
			with(this){
				prioritySelect.disable();
				categorySelect.disable();
				versionSelect.disable();
				assigneeSelect.disable();
				summaryText.attr('disabled', 'disabled');
				descriptionText.attr('disabled', 'disabled');
				commentText.attr('disabled', 'disabled');
			}
		}, self);
	
	
		/* ********************** model management ************ */
			
		var setModel = $.proxy(function(newModel){
			
			this.model = newModel;
			
			with(this){
				
				idText.val(model.id);
				
				prioritySelect.populate(model.project.priorities);
				categorySelect.populate(model.project.categories);
				versionSelect.populate(model.project.versions);
				assigneeSelect.populate(model.project.users);
				
				prioritySelect.select(model.priority);
				categorySelect.select(model.category);
				versionSelect.select(model.version);
				assigneeSelect.select(model.assignee);
				
				summaryText.val(model.summary);
				descriptionText.val(model.description);
				commentText.val(model.comment);				
			}
		}, self);
			
			
		var resetModel = $.proxy(function(){
			getBugReportTemplate()
			.done(function(){
				var copy = JSON.parse(JSON.stringify(self.template));
				setModel(copy);	
			})
			.fail(bugReportError);
		}, self);
			
		var getBugReportTemplate = $.proxy(function(){
			var jobDone = $.Deferred();
				
			
			if (! this.template){
			
				
				flipToPleaseWait();		
				
				$.ajax({
					url : self.reportUrl,
					type : "GET",
					dataType : "json"			
				})
				.done(function(response){
					self.template = response;
					flipToMain();
					jobDone.resolve();
				})
				.fail(jobDone.reject)
				.then(flipToMain);
			}
			else{
				jobDone.resolve();
			}
			
			return jobDone.promise();
		}, self);



		
		//we let the usual error handling do its job here
		var bugReportError = $.proxy(function(jqXHR, textStatus, errorThrown){
			flipToMain();
		}, self);
		
		
		var flushSheet = $.proxy(function(){
			this.model={};
			
			this.idText.val('');
			this.summaryText.val('');
			this.descriptionText.val('');
			this.commentText.val('');
			
			this.prioritySelect.flush();
			this.categorySelect.flush();
			this.versionSelect.flush();
			this.assigneeSelect.flush();
		}, self);
		
		
		var fillReport = $.proxy(function(json){
			
			
		}, self);
		
		
		var submit = $.proxy(function(){

			
		}, self);
		
		
		var submitSuccess = $.proxy(function(json){

		}, self);
		
		
		var submitFails = $.proxy(function(){

		}, self);
		
		var submit = $.proxy(function(){

			
		}, self);	

		
		/* ************* public ************************ */
		
		this.submitIssue = function(){
			
		};
		
		/* ************* events ************************ */
		
		//the opening of the popup :
		this.bind("dialogopen", function(){
			self.attachRadio.click();
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
