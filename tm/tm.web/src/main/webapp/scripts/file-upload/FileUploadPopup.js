/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
 * 		- tickets are like sEcho for datatable : an identifier used by the server to know which upload to watch for.	 
 * 		- we need the instruction flow to run as if the Ajax calls were synchronous. Since they aren't, we put the next steps of the 
 *      code in the ajax success handlers.
 */

require(["jquery", "squash.attributeparser", "jquery.squash.formdialog", 
         "./jquery.squash.multi-fileupload"], function($, attrparser){



 	$.widget("squash.attachmentsDialog", $.squash.formDialog,{
 		
 		options : {
 			width : 435,
 			url : undefined //also read via data-def thanks to native formDialog init
 		},
 		
 		_create : function(){
 			
 			this._super();
 			
 			// fetch the upload size exceeded error message
 			var errSpan = this.element.find('.attachment-upload-error-message');
 			this.options._sizeexceeded = errSpan.text();
 			errSpan.text('');
 			 			
 			// create the multiFileupload
 			var template = this.element.find('.add-attachments-templates > .attachment-item');
 			this.options._form = this.element.find('.attachment-upload-form').multiFileupload(template);
 			
 			// create the progressbar
 			this.options.bar = this.element.find('.attachment-progressbar').progressbar({ value: 0 });
 			this.option.percent = this.element.find('.attachment-progress-percentage');
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
			if (this.options._xhr){
				this.stopPolling();
				this.options._xhr.abort();
			}
  		}
  		
  		// ****************** files submission ***********************
  		
  		submitAttachments : function(){
  			var self = this;
  			var url = this.options.url;
  			
  			$.post(url)
  			.done(function(ticket){
  				self.options.ticket = ticket;
  				self.setState('uploading');
  				self.startPolling();
  				
  				self.options._xhr = self.options._form.ajaxSubmit({
  					url : url+"?upload-ticket="+ticket,
  					dataType : "text/html",
  					success : function(){},
  					error : function(){},
  					complete : function(json){
  						self.stopPolling();
  						self.submitComplete(json),
  					},
  					target : "#dump"
  					
  				});
  				
  			});
  		},
  		
  		
		/*
		because some browsers find it clever to wrap the raw response inside html tags (no, it's not IE for once) 
		we need to 'unwrap' our nested json response.
		
		in our case, if the json response has an attribute maxSize, then we got an error.
		*/
  		submitComplete : function(){
  			var xhr = this.options._xhr;
  			var text = $(xhr.responseText).text();
  			var json = $.parseJSON(text);
  			if (json.maxSize === undefined){
  				this.displaySummary();
  			}
  			else{
  				this.displayError(json.maxSize);
  			}
  		},
  		

  		// ********************* upload progress ******************
  		
  		startPolling : function(){
  			this.refreshBar(0);
  			this.options.pollid = setInterval($.proxy(self.poll, self), this), 1000);
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
  		}
  		
  		refreshBar : function(percentage){
  			this.options.bar.progressbar('option', 'value', percentage);
  			this.options.percent.text(percentage.toString+'&#37;');
  			
  		}
  		
  		
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
  				if (json !== null && self.allSuccessful(json)) {
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
  				if (summaries[i].iStatus!=0){
  					return false;
  				}
  			}
  			return true;	 			
  		},
  		
  		populateSummary : function(json){
  			var summaries = json[0],
  			var i=0,
  				summarydiv = this.find('.attachment-upload-summary'),
  				summaryItemTpl ='<div class="display-table-row" >'+
								'<label class="display-table-cell" style="font-weight:bold;">#summaryname#</label>'+
								'<span class="display-table-cell">#summarystatus#</span>'+
								'</div>';	
  			
  			for (i=0;i<summaries.length;i++){
  				var item = summaries[i];
  				var line = summaryItemTpl.replace('#summaryname#', item.name)
  										.replace('#summarystatus#', item.status);
  				summarydiv.append(line);
  			}
  				 			
  		},
  		
  		
  		// ***************** errors ***********************
  		
  		displayError : function(size){
  			var errMessage = this.options._sizeexceeded.replace('#size#', size);
  			this.element.find('.attachment-upload-error-message').text(errMessage);
  			this.setState('error');
  		}
  		
 		
 		
 	});

 
	
	return $.squash.attachmentsDialog;
});