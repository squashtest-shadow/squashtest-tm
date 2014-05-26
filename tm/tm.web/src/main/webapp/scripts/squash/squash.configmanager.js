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
define([ "jquery", "squash.translator", "datepicker/jquery.squash.datepicker-locales" ], function($, translator, regionale) {

	function stdCkeditor() {
		var lang = translator.get('rich-edit.language.value');

		return {
			customConfig : squashtm.app.contextRoot + '/styles/ckeditor/ckeditor-config.js',
			lang : lang
		};
	}
	
	function stdJeditable(){
		
		var lang = translator.get({
			submit : "label.Confirm",
			cancel : "label.Cancel",
			placeholder : "rich-edit.placeholder"
		});
		
		return $.extend(lang, {
			width : 200,
			maxlength : 255,
			indicator : '<div class="processing-indicator"/>',
			onblur : function() {
			},
			// abort edit if clicked on a hyperlink (being the tag itself or its content)
			onedit : function(settings, editable, evt){
				var $target = $(evt.target);
				return ! ( $target.is('a') || $target.parents('a').length > 0);  
			}
		});

	}
	
	function jeditableSelect(){
		var lang = translator.get({
			submit : "label.Confirm",
			cancel : "label.Cancel",
			placeholder : "rich-edit.placeholder"
		});
		
		return $.extend(lang, {
			type : 'select',
			width : '100%',
			maxlength : 255,
			indicator : '<div class="processing-indicator"/>',
			onblur : function() {
			},
			// abort edit if clicked on a hyperlink (being the tag itself or its content)
			onedit : function(settings, editable, evt){
				var $target = $(evt.target);
				return ! ( $target.is('a') || $target.parents('a').length > 0);  
			},
			callback : function(value, settings){
				$(this).text(settings.data[value]);
			}
		});
	}
	
	function jeditableCkeditor(){
		var ckconf = stdCkeditor(),
			jedconf = stdJeditable();
		
		return $.extend(true, 
			jedconf, 
			{
				cols : 80,
				rows : 10,
				type : 'ckeditor',
				ckeditor : ckconf
			}
		);
	}
	
	/*
	 * @params (optionals)
	 *	format : a string date format that datepicker understands
	 *	locale : a locale, used for datepicker internationalization
	 */
	function stdDatepicker(format, locale){
		
		// fetch the optional parameters if unspecified
		var fetchmeta = {},
			conf = {};
		
		if (!! format) { conf.format = format; } else { fetchmeta.format = 'squashtm.dateformatShort.datepicker'; }
		if (!! locale) { conf.locale = locale; } else { fetchmeta.locale = 'squashtm.locale'; }
				
		var translated = translator.get(fetchmeta);
		$.extend(conf, translated);
		
		// now configure the datepicker
		var language = regionale[conf.locale] || regionale;
		
		return $.extend(true, {}, {dateFormat : conf.format}, language);	
	}

	return {
		getStdCkeditor : stdCkeditor,
		getStdJeditable : stdJeditable,
		getStdDatepicker : stdDatepicker,
		getJeditableCkeditor : jeditableCkeditor,
		getJeditableSelect : jeditableSelect
	};

});