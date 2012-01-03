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

<p>A FeebackMultipartPopup accepts a single popup which contains three panels (divs) : </p>
<ul>
	<li>the parametrization, typically where the file browser is</li>
	<li>the progression, which contains a "please wait" thing and a progress bar</li>
	<li>the summary, that proposes a view of the overall operation (status, warnings etc) after completion.</li>
</ul>

<p>
	The parametrization panel must contain a form, enctype multipart/form-data and method POST.
</p>

<p>settings : </p>
<ul>
	<li>popup : the jQuery object that represent the dialog (not the widget itself).</li>
	<li>parametrizationPanel : a javascript object configuring the parametrization phase
		<ul>
			<li>selector : the selector that will give the jQuery object that represents the parametrisation panel</li>
		</ul>
	</li>
	<li>progressPanel : a javascript object configuring the progress upload phase.</li>
	//TODO : complete this
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
	this.ticket=0;
	
	/* ************* basic methods ************************ */
	
	this.jqueryfy = function(){
		if (! this.jqueryfied){
			this.parametrizationPanel.panel = $(this.parametrizationPanel.selector);
			this.progressPanel.panel = $(this.progressPanel.selector);
			this.summaryPanel.panel = $(this.summaryPanel.selector);
			this.dumpPanel.panel = $(this.dumpPanel.selector);
		}
	}
	
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
		this.parametrizationPanel.panel.removeClass("not-displayed");
		this.progressPanel.panel.addClass("not-displayed");
		this.summaryPanel.panel.addClass("not-displayed");
		
		// todo : kill the poll process associated to the progress panel
	};
	
	this.showProgress = function(){
		this.parametrizationPanel.panel.addClass("not-displayed");
		this.progressPanel.panel.removeClass("not-displayed");
		this.summaryPanel.panel.addClass("not-displayed");	
		
		//todo : start the poll process associated to the progress panel
	};
	
	this.showSummary = function(){
		this.parametrizationPanel.panel.addClass("not-displayed");
		this.progressPanel.panel.addClass("not-displayed");
		this.summaryPanel.panel.removeClass("not-displayed");	
		
		// todo : kill the poll process associated to the progress panel
	};

	
	/* ********************* main code : operations and transitions ****************************** */
	
	this.initSubmission = function(){
		//todo and please use Deferred.done() to set the ticket and perform the following
		this.showProgress();
		this.submit();
	};
	
	this.submit = function(){
		var self =  this;
		var form = $("form", this.parametrizationPanel.panel);
		form.ajaxSubmit({
			url : this.parametrizationPanel.submitUrl+"?upload-ticket="+this.ticket,
			dataType : "text",
			success : function(){},
			error : function(){},
			complete : function(jqXHR){this.xhr = jqXHR; self.displaySummary();},
			target : this.dumpPanel.panel.attr('id')
		});
	};
	
	this.displaySummary = function(){
		this.showSummary();
		
		//todo : build the summary using the xhr response, possibly by calling a client-defined callback.
	}
	
	return this;
}