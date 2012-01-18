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

<p>A FeebackMultipartPopup accepts a single popup which contains three panels (divs) :
<ul>
	<li>the parametrization, typically where the file browser is</li>
	<li>the progression, which contains a "please wait" thing and a progress bar</li>
	<li>the summary, that proposes a view of the overall operation (status, warnings etc) after completion.</li>
</ul>
An three buttons : confirm, ok, cancel, having css classes set using the class constant defined at the end of the file.
</p>

<p>
	The parametrization panel must contain a form, enctype multipart/form-data and method POST.
</p>

<p>settings : </p>
<ul>
	<li>popup : the jQuery object that represent the dialog (not the widget itself).</li>
	<li>parametrizationPanel : a javascript object configuring the parametrization phase
		<ul>
			<li>selector : the selector that will give the jQuery object that represents the parametrisation panel</li>
			<li>submitUrl : the url where to upload the form</li>
		</ul>
	</li>
	<li>progressPanel : a javascript object configuring the progress upload phase.
		<ul>
			<li>selector : the selector that will give the jQuery object that represents the progress upload panel</li>
		</ul>	
	</li>
	<li>summaryPanel : a javascript object configuring the summary phase :
		<ul>
			<li>builder : a function accepting the result of the ajax request as a json object and returning the content of the summary panel as a jQuery object.</li>
		</ul>
	</li>
</ul>
*
*/
function FeedbackMultipartPopup(settings){

	this.jqueryfied=false;
	this.popup = settings.popup;
	this.parametrizationPanel = settings.parametrizationPanel;
	this.progressPanel = settings.progressPanel;
	this.summaryPanel = settings.summaryPanel;
	this.dumpPanel = settings.dumpPanel;
	
	//internal state
	this.ticket=0;
	this.state = undefined;
	
	/* ************* basic methods ************************ */
	
	this.jqueryfy = function(){
		if (! this.jqueryfied){
			this.parametrizationPanel.panel = $(this.parametrizationPanel.selector);
			this.progressPanel.panel = $(this.progressPanel.selector);
			this.summaryPanel.panel = $(this.summaryPanel.selector);
			this.dumpPanel.panel = $(this.dumpPanel.selector);
		}
	};
	

	
	this.getButtonPane = function(){
		return this.popup.eq(0).next()
	};
	
	this.getConfirmButton = function(){
		return this.getButtonPane().find("button."+FeedbackMultipartPopup.CONFIRM_CLASS);
	};
	
	this.getOkButton = function(){		
		return this.getButtonPane().find("button."+FeedbackMultipartPopup.OK_CLASS);
	};
	
	this.getCancelButton = function(){
		return this.getButtonPane().find("button."+FeedbackMultipartPopup.CANCEL_CLASS);
	};
	
	this.reset = function(){
	
		this.jqueryfy();
		
		//the following should be done once and for all :
		this.dumpPanel.panel.addClass("not-displayed");
	
		//init
		this.ticket=0;
		
		var paramPanel = this.parametrizationPanel.panel;
		paramPanel.find('input').val('');
		
		//do stuff
		
		this.showParametrization();
	};
	
	this.showParametrization = function(){
	
		this.parametrizationPanel.panel.show();
		this.progressPanel.panel.hide();
		this.summaryPanel.panel.hide();

		this.getConfirmButton().show();
		this.getCancelButton().show();			
		this.getOkButton().hide();
		
		// todo : kill the poll process associated to the progress panel
		this.state = "parametrization";
		
	};
	
	this.showProgress = function(){
	
		this.parametrizationPanel.panel.hide();
		this.progressPanel.panel.show();
		this.summaryPanel.panel.hide();	
		
		this.getConfirmButton().hide();
		this.getCancelButton().show();			
		this.getOkButton().hide();
		
		//todo : start the poll process associated to the progress panel
		
		this.state = "progress";
		
	};
	
	this.showSummary = function(){
	
		this.parametrizationPanel.panel.hide();
		this.progressPanel.panel.hide();
		this.summaryPanel.panel.show();	
		
		this.getConfirmButton().hide();
		this.getCancelButton().hide();			
		this.getOkButton().show();
		
		// todo : kill the poll process associated to the progress panel
	
		this.state = "summary";
		
	};

	
	/* ********************* main code : operations and transitions ****************************** */
	
	this.submit = function(){
		//todo and please use Deferred.done() to set the ticket and perform the following
		this.showProgress();
		this.doSubmit();
	};
	
	this.doSubmit = function(){
		var self =  this;
		var form = $("form", this.parametrizationPanel.panel);
		form.ajaxSubmit({
			url : this.parametrizationPanel.submitUrl+"?upload-ticket="+this.ticket,
			dataType : "text",
			success : function(){},
			error : function(){},
			complete : function(jqXHR){
				self.xhr = jqXHR; 
				self.displaySummary();
			},
			target : this.dumpPanel.panel.attr('id')
		});
	};
	
	
	var stripWrapper = function(xhr){
		var jqXHR = $(xhr.responseText);
		return jqXHR.html();
	}
	
	this.displaySummary = function(){
	
		var json = $.parseJSON(stripWrapper(this.xhr));
		
		var content = this.summaryPanel.builder(json);
		this.summaryPanel.panel.html('');
		this.summaryPanel.panel.prepend(content);
	
		this.showSummary();
		
	};
	
	this.cancel = function(){
		if (this.state == "progress"){
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
	
	return this;
}

/* ******************** Class constants *********************** */

FeedbackMultipartPopup.CONFIRM_CLASS = "confirm-button-class"
FeedbackMultipartPopup.CANCEL_CLASS = "cancel-button-class"
FeedbackMultipartPopup.OK_CLASS = "ok-button-class"