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
 * settings : {
 *		formats : [array of supported file extensions, that will be checked on validation ] 
 * 
 * }
 * 
 * -------------- API----------
 * 
 * the following MUST be implemented : 
 * 
 * {
 *		createSummary : function(json) : populate the summary panel using the xhr response.	
 * }
 * 
 * The following methods have a default implementation but could be considered for overriding : 
 * 
 * {
 * 		bindEvents : function() : event binding
 * 		getForm : function() : returns the form that must be uploaded.
 * }
 * 
 */
define(['jquery', 'jquery.squash.formdialog', 'jform'], function($){
	
	if (($.squash !== undefined) && ($.squash.importDialog !== undefined)){
		//plugin already loaded
		return ;
	}
	
	$.widget('squash.importDialog', $.squash.formDialog, {
		
		widgetEventPrefix : 'importdialog',
		
		options : {
			_ticket : 0,	//upload ticket (used internally, shouldn't be set by the user)
			formats : ['you forgot to configure that']
		},
		
	
		// ********************** abstrat *******************************
		
		createSummary : function(xhr){
			throw "importDialog : it seems this instance is an abstract instance : " +
					" it should have been subclassed and implement createSummary properly !";
		},
		
	
		_create : function(){
			this._super();
			this.bindEvents();
		},
		
		bindEvents : function(){
			var self = this;
			
			// ** buttons **
	
			this.onOwnBtn('import', function(){
				var validated = self.validate();
				if (validated){
					self.setState('confirm');
				} 
				else {
					self.setState('error-format');
				}
			});
			
			this.onOwnBtn('confirm', function(){
				self.submit();
			});
			
			this.onOwnBtn('ok', function(){
				self.close();
			});
			
			this.onOwnBtn('okerrsize', function(){
				self.close();
			});
			
			this.onOwnBtn('okerrformat', function(){
				self.setState('parametrization');
			});
			
			this.onOwnBtn('cancel-progression', function(){
				self.cancelUpload();
				self.close();
			});
			
			this.onOwnBtn('cancel', function(){
				self.close();
			});
						
		},
		
		open : function(){
			this._super();
			this.reset();
		},
		
		reset : function(){
			this.element.find('input').val('');
			this.setState('parametrization');
		},
		
		getForm : function(){
			return this.element.find('form');
		},
		
		validate : function(){
			
			var fileUploads = this.getForm().find("input[type='file']");

			var self = this;
			var validated = false;
			fileUploads.each(function(i, v) {
				var fileName = v.value;

				$.each(self.options.formats, function(i, v) {
					if (fileName.match("." + v + "$")) {
						validated = true;
					}
				});

			});
			
			return validated;
		},
		
		// ***************** request submission code *******************
		
		submit : function() {
			this.setState('progression');
			this.doSubmit();
		},
		
		doSubmit : function(){
			var self = this;
			
			var form = this.getForm();
			
			var url = form.attr('action');
			form.ajaxSubmit({
				url : url + '?upload-ticket=' + self.options._ticket,
				dataType : 'text/html',
				type : 'POST',
				success : function(){},
				error : function(){},
				complete : function(xhr){
					
					self.options.xhr = xhr;					
					var json = $.parseJSON($(xhr.responseText).text());
			
					if ('maxSize' in json){
						self.errMaxSize(json.maxSize);
						self.setState('error-size');
					}
					else{						
						self.createSummary(json);
						self.setState('summary');						
					}
				},
				target : self.element.find('.dump').attr('id')
			});
		},
		
		cancelUpload : function(){
			var state = this.getState();
			if (state === "progression"){
				this._cancelPoll();
				// we must also kill the submit itself, alas killing other pending
				// ajax requests.
				if (window.stop !== undefined) {
					window.stop();
				} else {
					/*
					 * IE-specific instruction document.execCommand("Stop"); wont prevent the file to be fully uploaded
					 * because it doesn't kill the socket, so we'll be even more blunt
					 */
					document.location.reload();
				}
			}
		},
		
		_startPoll : function(){
			// TODO
		},
		
		_cancelPoll : function(){
			// TODO
		},

		
		// ********************* errors *********************************
		
		errMaxSize : function(maxSize){
			var span = this.element.find('.error-size');
			var text = span.text();
			if (text.indexOf('{MAX-SIZE}') !== -1 ){
				text.replace('{MAX-SIZE}', maxSize);
				span.text( text );
			}
		}

	});
	
});