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

(function($){


	//Adding maxlength attribute for text
	//thanks to http://blogpad-online.blogspot.com/2010/10/jeditable-maxlength_12.html
	$.editable.types.text.element = function(settings, original) {
        var input = $('<input />');
        if (settings.width  != 'none') { input.width(settings.width);  }
        if (settings.height != 'none') { input.height(settings.height); }
        if (settings.maxlength != 'none') { input.attr('maxlength', settings.maxlength); }
        input.attr('autocomplete','off');
        $(this).append(input);
        return(input);
	}
	


	/*
		custom rich jeditable for the type 'ckeditor'. The plugin jquery.jeditable.ckeditor.js must have been called beforehand. The purpose of it is that we hook the object with additional handlers that will enable or disable hyperlinks with respect to the state of the editable (edit-mode or display-mode).
		
		It accepts one object for argument, with the regular options of a jeditable.  : 
			- this : a dome element javascript object. Not part of the settings.
			- url : the url where to post.
			- ckeditor : the config for the nested  ckeditor instance 
			- placeholder : message displayed when the content is empty
			- submit : text for the submit button
			- cancel : text for the cancel button
	
	*/
	
	squashtm.jeditable = {
		bindLinks : function(){
			var elts = $('a', this);
			
			elts.unbind('click');
			elts.click(function(event){
				document.location.href=this.href;
				event.stopPropagation();
				return false;
			});
		},
		
		richEditable : function(customSettings){					
			//bind already existing links
			squashtm.jeditable.bindLinks.call(this);
		
			//settings
			var defSettings = {
				type : 'ckeditor',
				rows : 10,
				cols : 80,
				onblur : function(){},
				callback : function(result, settings){
					//'this' in this context is the div itself
					squashtm.jeditable.bindLinks.call(this);
				},
				
			};
			
			//merge the settings and create the object
			$.extend(true,defSettings, customSettings);
			$(this).editable(customSettings.url, defSettings);
			
			//hook the reset so that we bind links there too.
			var oldReset = this.reset;
			this.reset = function(){
				oldReset.call(this);
				squashtm.jeditable.bindLinks.call(this);		
			}		
		}		
	};
})(jQuery);
