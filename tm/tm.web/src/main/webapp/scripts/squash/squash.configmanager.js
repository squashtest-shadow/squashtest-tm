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
define([ "jquery", "squash.translator", "datepicker/require.jquery.squash.datepicker-locales" ], function($, translator, regionale) {

	function stdCkeditor() {
		var lang = translator.get('rich-edit.language.value');

		return {
			customConfig : squashtm.app.contextRoot + '/styles/ckeditor/ckeditor-config.js',
			lang : lang
		};
	}
	
	function stdJeditable(){
		return {
			width : '100%',
			submit : squashtm.message.confirm,
			cancel : squashtm.message.cancel,
			maxlength : 255,
			cols : 80,
			max_size : 20,
			onblur : function() {
			},
			placeholder : squashtm.message.placeholder
			
		};
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
		getStdChkeditor : stdCkeditor,
		getStdJeditable : stdJeditable,
		getStdDatepicker : stdDatepicker
	};

});