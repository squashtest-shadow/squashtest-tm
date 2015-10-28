/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
/*
 * This section handles the transition between phase 1 and phase 2, ie all the operations between 
 * the moment the user presses "submit" and the end of the download :
 *   - prelude to the upload : we'll need a 'ticket' for later use (see below),
 *   - close the uploader popup, open the progress bar,
 *   - start the uploading request (still stuck to HttpRequests, no XHR for this one)
 *   - during the upload, poll the server for the upload using the ticket obtained in step 1
 *   - once it's done it will open the uploadSummary popup 
 *
 *  
 * Notes : 
 *		- tickets are like sEcho for datatable : an identifier used by the server to know which upload to watch for.	 
 *		- we need the instruction flow to run as if the Ajax calls were synchronous. Since they aren't, we put the next steps of the 
 *		code in the ajax success handlers.
 */

define(["jquery", "squash.attributeparser", "handlebars", "jquery.squash.formdialog", "./jquery.squash.multi-fileupload"], 
		function($, attrparser, Handlebars){

	if (($.squash !== undefined) && ($.squash.attachmentsDialog !== undefined)) {
		// plugin already loaded
		return;
	}

	$.widget("squash.attachmentsDialog", $.squash.formDialog,{

		options : {
			width : 400,
			url : undefined 
		},

		_create : function(){

			this._super();
			
			// main form init
			var template = this.element.find('.add-attachments-templates > .attachment-item');
			this.options._form = this.element.find('.attachment-upload-form').multiFileupload(template);

			// progressbar init
			this.options.bar = this.element.find('.attachment-progressbar').progressbar({ value: 0 });
			this.options.percent = this.element.find('.attachment-progress-percentage');

			// summary init
			var summaryItemTpl ='<div class="display-table-row" >'+
				'<label class="display-table-cell" style="font-weight:bold;">{{name}}</label>'+
				'<span class="display-table-cell">{{status}}</span>'+
				'</div>';	
			this.options.summaryitem = Handlebars.compile(summaryItemTpl);

			// error init
			var errSpan = this.element.find('.attachment-upload-error-message');
			this.options._sizeexceeded = errSpan.text();
			errSpan.text('');

			this._bindEvents();
		},

		_bindEvents : function(){
			var self = this;

			this.onOwnBtn('cancel', function(){
				self.close();
			});

			this.onOwnBtn('done', function(){
				self.close();
			});

			this.onOwnBtn('submit', function(){
				self.submitAttachments();
			});
		},

		open : function(){
			this._super();
			this.options._form.clear();
			this.setState('selection');
		},

		close : function(){
			this._super();
			if (this.options._xhr && this.options.preventAbortion !== true){
				this.stopPolling();
				this.options._xhr.abort();
			}
		},

		// ****************** files submission ***********************

		submitAttachments : function(){
			var self = this;
			var url = this.options.url;

			$.post(url)
			.done(function(ticket){
				self.options.ticket = ticket;
				self.setState('uploading');
				self.startPolling();

				self.options._form.ajaxSubmit({
					url : url+"?upload-ticket="+ticket,
					dataType : "text/html",
					beforeSend : function(xhr){
						self.options._xhr = xhr;
					},
					success : function(){},
					error : function(){},
					complete : function(json){
						self.stopPolling();
						self.submitComplete();
					},
					target : "#dump"
					
				});
				
			});
		},
		
		
		/*
		because some browsers find it clever to wrap the raw response inside html tags (no, it's not IE for once) 
		we need to 'unwrap' our nested json response.
		
		in our case, if the json response exists and has an attribute maxSize, then we got an error.
		*/
		submitComplete : function(){
			var xhr = this.options._xhr;
			// Kind of a hack to prevent the thisDialog.close hook to try and abort the xhr,
			// which leads to an infinite loop in IE.
			// TODO Gotta find a more elegant way
			this.options.preventAbortion = true;

			var text = $(xhr.responseText).text();
			
			try{	// try json
				var json = $.parseJSON(text);
	
				if (json == null ||json.maxUploadError.maxSize === undefined){
					this.displaySummary();
				}
				else{
					this.displayError(json.maxUploadError.maxSize);
				}
			}
			catch(ex){ // try html
				this.displayError(text);
			}
		},
		

		// ********************* upload progress ******************

		startPolling : function(){
			this.refreshBar(0);
			var fnpoll = $.proxy(this.poll, this);
			this.options.pollid = setInterval(fnpoll,  1000);
		},
		
		poll : function(){
			var self = this,
				url = this.options.url +"?upload-ticket="+this.options.ticket;
			$.getJSON(url).done(function(json){
				self.refreshBar(json.percentage);
			});
		},
		
		stopPolling : function(){
			clearInterval(this.options.pollid);
		},
		
		refreshBar : function(percentage){
			this.options.bar.progressbar('option', 'value', percentage);
			this.options.percent.text(percentage.toString()+'%');
			
		},
		
		
		// ******************** upload summary ********************

		
		displaySummary : function(){
			var ticket = this.options.ticket,
				url = this.options.url,
				self = this;
			
			$.ajax({
				url : url+'?upload-ticket='+ticket,
				type : 'DELETE',
				dataType : 'json'
			})
			.done(function(json){
				if (json !== null && ! self.allSuccessful(json)) {
					self.populateSummary(json);
					self.setState('summary');
				}else{
					self.close();
					self._trigger('done');
				}
			});
			
		},
		
		allSuccessful : function(json){
			var summaries = json[0];
			var i=0;
			for (i=0;i<summaries.length;i++){
				if (summaries[i].iStatus!==0){
					return false;
				}
			}
			return true;
		},
		
		populateSummary : function(json){
			var summaries = json[0],
				i=0,
				summarydiv = this.element.find('.attachment-upload-summary'),
				summaryItemTpl = this.options.summaryitem;
			
			summarydiv.empty();
			for (i=0;i<summaries.length;i++){
				var item = summaries[i];
				var line = summaryItemTpl(item);
				summarydiv.append(line);
			}

		},
		
		
		// ***************** errors ***********************
		
		displayError : function(sizeOrMsg){
			
			if (typeof sizeOrMsg === "string"){
				this.element.find('.attachment-upload-error-message').text(sizeOrMsg);
			}
			else{
				var s = (sizeOrMsg / 1048576).toFixed(3);
				
				var errMessage = this.options._sizeexceeded.replace('#size#', s);
				this.element.find('.attachment-upload-error-message').text(errMessage);
			}
			this.setState('error');
		}
		
		
		
	});

});