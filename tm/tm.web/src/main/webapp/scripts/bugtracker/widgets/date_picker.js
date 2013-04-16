/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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


define(["jquery", "../domain/FieldValue", "squash.translator", "datepicker/require.jquery.squash.datepicker-locales", "jqueryui"], function($, FieldValue, translator, regionale){

	function convertStrDate(fromFormat, toFormat, strFromValue){
		var date = $.datepicker.parseDate(fromFormat, strFromValue);
		return $.datepicker.formatDate(toFormat, date);		
	}
	
	return {
		
		options : {
			rendering : {
				inputType : {
					name : "date_picker",
					meta : { 'date-format' : "yyyy-mm-dd" }
				}
				
			}
		},
		
		_create : function(){
			
			//parameterize the locale
			var localemeta = {
				format : 'squashtm.dateformatShort.js',
				locale : 'squashtm.locale'
			}
			
			var message = translator.get(localemeta);
			this.options.message = message;
			
			var language = regionale[message.locale] || regionale;
			
			var pickerconf = $.extend(true, {}, language, {dateFormat : message.format});
			
			//TODO : handle proper configuration
			this.element.datepicker(pickerconf);
			
			if (this.options.rendering.operations.length===0){
				this.element.prop('disabled', true);
			};
		},
		
		fieldvalue : function(fieldvalue){
			if (fieldvalue===null || fieldvalue === undefined){
				var date = this.element.datepicker('getDate');
				var toFormat = this.options.rendering.inputType.meta['date-format'];
				var strDate = $.datepicker.formatDate(toFormat, date);
				return new FieldValue("", strDate);
			}
			else{
				var fromFormat = this.options.rendering.inputType.meta['date-format'];
				var strDate = fieldvalue.scalar;
				if (!!strDate){
					var date = $.datepicker.parseDate(fromFormat, strDate)
					this.element.datepicker('setDate', date);
				}
			}
		}, 
		
		disable : function(){
			this.element.prop('disabled', true);
		},
		
		enable : function(){
			if (this.options.rendering.operations.length!=0){
				this.element.prop('disabled', false);
			}
		},

		createDom : function(field){
			var input = $('<input/>', {
				'type' : 'text',
				'data-widgetname' : 'date_picker',
				'data-fieldid' : field.id
			});
			
			input.attr('size', 60);
			
			return input;
		}
	}

})
