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

define(["jquery", "datepicker/require.jquery.squash.datepicker-locales", "jqueryui"], function($, regionale){
	
	return {
		/*
		 * Accepts : 
		 *	convert(long timestamp, string format) : convert the given numeric timestamp to a string of the given format
		 *	convert(string inputvalue, string toFormat, String fromFormat) : convert the date given as String, parsed using fromFormat, and converted to toFormat 
		 */
		convert : function(value, toFormat, fromFormat){
			
			var lang = translator.get('squashtm.locale');
			var reg = regionale[lang]	||	regionale;
			
			if (typeof value === "number"){
				// this case is when we have the timestamp as long
				return newDate(value).format(toFormat);
			}
			else{
				var date = $.datepicker.parseDate(fromFormat, value, reg);
				return $.datepicker.formatDate(toFormat, date, reg);
			}
		},
		
		/*
		 * @params :
		 * 	date : a Date object
		 * 	format : a String format
		 */
		format : function(date, format){
			return $.datepicker.formatDate(toFormat, date);		
		},
		
		parse : function(value, format){
			$.datepicker.parseDate(format, value);
		}
	}
});