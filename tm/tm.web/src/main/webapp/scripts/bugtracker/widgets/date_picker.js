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
			
			this._super();
			
			//parameterize the locale
			var localemeta = {
				format : 'squashtm.dateformatShort.js',
				locale : 'squashtm.locale'
			};
			
			var message = translator.get(localemeta);
			
			var language = regionale[message.locale] || regionale;
			
			var pickerconf = $.extend(true, {}, language, {dateFormat : message.format});
			
			this.element.datepicker(pickerconf);

		},
		
		fieldvalue : function(fieldvalue){
			var date,strDate;
			if (fieldvalue===null || fieldvalue === undefined){
				
				date = this.element.datepicker('getDate');
				var toFormat = this.options.rendering.inputType.meta['date-format'];
				strDate = $.datepicker.formatDate(toFormat, date);
				var typename = this.options.rendering.inputType.dataType;
				
				return new FieldValue("--", typename, strDate);
			}
			else{
				var fromFormat = this.options.rendering.inputType.meta['date-format'];
				strDate = fieldvalue.scalar;
				if (!!strDate){
					date = $.datepicker.parseDate(fromFormat, strDate);
					this.element.datepicker('setDate', date);
				}
			}
		}, 
		
		createDom : function(field){
			var input = $('<input/>', {
				'type' : 'text',
				'data-widgetname' : 'date_picker',
				'data-fieldid' : field.id
			});
			
			
			return input;
		}
	};

});
