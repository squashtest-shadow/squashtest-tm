/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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

define(['jquery', 'squash.configmanager', 'squash.attributeparser', "jeditable", "jeditable.ckeditor"], 
		function($, confman, attrparser){
	

	// Adding maxlength attribute for text
	// thanks to
	// http://blogpad-online.blogspot.com/2010/10/jeditable-maxlength_12.html
	$.editable.types.text.element = function(settings, original) {
		var input = $('<input />');
		if (settings.width != 'none') {
			input.width(settings.width);
		}
		if (settings.height != 'none') {
			input.height(settings.height);
		}
		if (settings.maxlength != 'none') {
			input.attr('maxlength', settings.maxlength);
		}
		input.attr('autocomplete', 'off');
		$(this).append(input);
		return (input);
	};

	/**
	 * custom rich jeditable for the type 'ckeditor'. The plugin
	 * jquery.jeditable.ckeditor.js must have been called beforehand. The
	 * purpose of it is that we hook the object with additional handlers that
	 * will enable or disable hyperlinks with respect to the state of the
	 * editable (edit-mode or display-mode).
	 * 
	 * It accepts one object for argument, with the regular options of a
	 * jeditable. : - this : a dom element javascript object. Not part of the
	 * settings. - url : the url where to post. - ckeditor : the config for the
	 * nested ckeditor instance - placeholder : message displayed when the
	 * content is empty - submit : text for the submit button - cancel : text
	 * for the cancel button
	 * 
	 * Also accepts (simple) options passed as 'data-def' on the dom element. 
	 * Note : options 'cols' and 'rows' can be set to 'auto', such dimensions 
	 * will then be unbounded. 
	 * 
	 */

	$.widget('squash.richEditable', {

		options : confman.getJeditableCkeditor(),

		_init : function() {
			var defoptions = this.options;
			
			this.element.each(function(){
				var $this = $(this);
				var stropt = $this.data('def');
				var options = (!! stropt) ? attrparser.parse(stropt) : {};
				
				var finaloptions = $.extend(true, {}, defoptions, options);
				
				if (options.cols === "auto"){
					delete finaloptions.cols;
				}
				if (options.rows === "auto"){
					delete finaloptions.rows;
				}
				
				$this.editable(finaloptions.url, finaloptions);				
			});
		}

	});	
	
});

