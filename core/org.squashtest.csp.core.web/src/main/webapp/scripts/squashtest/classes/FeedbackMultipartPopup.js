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

/**


<p>A FeedbackMultipartPopup is an object complementary to the regular jQuery.dialog. Its purpose is to enrich the submission of
multipart form data, which can be long to upload, and of which the user might appreciate to be informed of the progression.</p>

<p>popup layout definition :

the bulk of the popup consists of several divs : parametrization, confirm and summary. Each of them
will be identified as such if they use the corresponding css classes : 'parametrization', 'confirm' and 'summary' classes.
</p>

<p>configuration : the object setting is made of :

<ul>
	<li>popup : the jQuery object representing the dialog (not the widget)</li>
	<li>errorHandler : a javascript function accepting a javascript object (the json response). Must return null if no error were parsed, or the mssage to display if an error occured.</li>
	<li> parametrization : an object defined as follow : 
		<ul>
		<li>submitUrl : the url where to submit.</li>
		<li>extensions : the autorized extensions for file uploads.</li>	
		<li>errorMessage : the error message to display if validation fails.</li>
		</ul>
	</li>
	<li>summary : an object defined as follow : 
		<ul>
		<li>builder : a javascript function accepting a javascript object (the json response of the form sumbission) to help with the construction of the summary before it is displayed;</li>
		</ul>
	</li>
	
</ul>



*
*/
function FeedbackMultipartPopup(settings){

	/* ***************** attributes ******************************* */
	
	this.popup = settings.popup;
	
	this.parametrization = settings.parametrization;
	this.summary = settings.summary;
	this.errorHandler = settings.errorHandler;
	
	
	//internal state
	this.ticket=0;
	this.state = undefined;
	
		
	/* *****************private methods *************************** */

	var self=this;
	var getButtonPane = $.proxy(function(){
						return this.popup.eq(0).next().find(".ui-dialog-buttonset");
					}, self);
	
	var getButtons = $.proxy(function(className){
						var buttons = getButtonPane().find("button."+className);
						return buttons;
					}, self);
	
	var showButtons = $.proxy(function(className){
						var allButtons = getButtonPane().find("button");
						var selectedButtons = allButtons.filter("."+className);
						
						allButtons.hide();
						selectedButtons.show();
						
					}, self);

	var findMainPanel = $.proxy(function(name){
						return this[name].panel;
					}, self);
	
	var showPanel = $.proxy(function(name){						
						$.each(this.allPanels, function(i,v){
							v.hide(); 
						});
						findMainPanel(name).show();
					}, self);
	
	var displayError = $.proxy(function(message){
						var erPanel = findMainPanel(FeedbackMultipartPopup.ERROR);
						var spMessage = $("<span/>", { "text" : message });
						erPanel.empty();
						erPanel.append(spMessage);
					}, self);
	
	/* ********************** public methods ************************* */
	
	this.reset = function(){
	
		var paramPanel = this.parametrization.panel;
		paramPanel.find('input').val('');
		
		this.setState(FeedbackMultipartPopup.PARAMETRIZATION);
	};
	
	
	
	this.setState = function(stateName){
		showPanel(stateName);
		showButtons(stateName);
		this.state = stateName;		
	};
	
	
	this.validate = function(){
	
		var fileUploads = $("."+FeedbackMultipartPopup.PARAMETRIZATION+" input[type='file']", this.popup);
		
		var self = this;
		var validated = false;
		
		fileUploads.each(function(i,v){			
			var fileName = v.value;
			
			$.each(self.parametrization.extensions, function(i,v){
				if (fileName.match("."+v+"$")){
					validated=true;
				}
			});
			
		});
		
		if (validated){
			this.setState(FeedbackMultipartPopup.CONFIRM);
		}else{
			displayError(this.parametrization.errorMessage);
			this.setState(FeedbackMultipartPopup.ERROR);
		}
	
	}
	
	this.submit = function(){
		//todo and please use Deferred.done() to set the ticket and perform the following
		this.setState(FeedbackMultipartPopup.PROGRESSION);
		this.doSubmit();
	};
	
	this.doSubmit = function(){
		var localSelf =  this;
		var form = $("form", this.parametrization.panel);
		form.ajaxSubmit({
			url : this.parametrization.submitUrl+"?upload-ticket="+this.ticket,
			dataType : "json",
			success : function(){},
			error : function(){},
			complete : function(jqXHR){
				localSelf.xhr = jqXHR; 
				localSelf.displaySummary();
			},
			target : this.dump.panel.attr('id')
		});
	};
	

	
	this.displaySummary = function(){
	
		var json = $.parseJSON(this.xhr.responseText);		
		
		var errorMessage = this.errorHandler(json);
		
		if (errorMessage===null){	
			this.summary.builder(json);		
			this.setState(FeedbackMultipartPopup.SUMMARY);
		}else{
			displayError(errorMessage);
			this.setState(FeedbackMultipartPopup.ERROR);			
		}
			
		
	};
	
	this.cancel = function(){
		if (this.state == "progression"){
			this.cancelPoll();
			//we must also kill the submit itself, alas killing other pending ajax requests.
			if (window.stop !== undefined){
				window.stop();	
			}else{
				/*
					IE-specific instruction document.execCommand("Stop"); wont prevent the file to be fully uploaded because it doesn't kill
					the socket, so we'll be even more blunt
				*/
				document.location.reload();
			}
		}
		this.popup.dialog("close");
	};
	
	this.cancelPoll = function(){
		//todo
	};
	
	this.startPoll = function(){
		//todo
	};
	
	
/* **************************************************************
						CONSTRUCTION					
************************************************************** */

	
	var buildErrorPanel = $.proxy(function(){
				var localSelf = this;
			
				this.error = {};
				this.error.panel = $("<div/>", {'class' : 'error-display'} );
				
				var errorButton = $("<button type='button' class='button'>");
				errorButton.addClass(FeedbackMultipartPopup.ERROR);
				getButtonPane().prepend(errorButton);		
				errorButton.button({'label' : 'Ok'}).click(function(){
					localSelf.setState(FeedbackMultipartPopup.PARAMETRIZATION);}
				);
				
				this.popup.append(this.error.panel);		
				this.allPanels.push(this.error.panel);
			}, self);
	
	
	var buildDumpPanel = $.proxy(function(){	
				this.dump = {};
				this.dump.panel = $("<div/>", { 'id' : 'excel-dump', 'class' : 'dump'} );
				this.dump.panel.hide();		
				
				this.popup.append(this.dump.panel);
			}, self);
	
	//the actual construction takes place now
	
	this.allPanels = [];
	
	this.confirm = {};	
	this.progression = {};
	
	this.parametrization.panel = $("."+FeedbackMultipartPopup.PARAMETRIZATION, this.popup);
	this.confirm.panel = $("."+FeedbackMultipartPopup.CONFIRM, this.popup);
	this.progression.panel = $("."+FeedbackMultipartPopup.PROGRESSION, this.popup);
	this.summary.panel =  $("."+FeedbackMultipartPopup.SUMMARY, this.popup);
	
	this.allPanels.push(this.parametrization.panel);
	this.allPanels.push(this.summary.panel);
	this.allPanels.push(this.progression.panel);
	this.allPanels.push(this.confirm.panel);
	
	buildErrorPanel();
	buildDumpPanel();
		
	return this;
}

/* ******************** Class constants *********************** */

FeedbackMultipartPopup.PARAMETRIZATION = "parametrization"
FeedbackMultipartPopup.CONFIRM = "confirm"
FeedbackMultipartPopup.ERROR = "error"
FeedbackMultipartPopup.PROGRESSION = "progression"
FeedbackMultipartPopup.SUMMARY = "summary"
