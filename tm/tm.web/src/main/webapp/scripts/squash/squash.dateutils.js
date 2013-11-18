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

define(["datejs-all", "squash.translator"], function(datelocales, translator) {
	
	

	function loadLocale(locale){
		var _loc = locale;
	
		if (_loc === null || _loc === undefined){
			_loc = translator.get('squashtm.locale');
		}
	
		Date.CultureInfo = datelocales[_loc] || datelocales['en'];
	}
	
	return {
		
		ISO_8601 : "yyyy-MM-ddTHH:mm:ss" ,
		
		/*
		* Accepts : 
		*	1/ format(Unknown value, string format) : returns the given date as string using the given format.
		*	The date can be a numeric timestamp, a Date instance or a string. If String, the ATOM (ISO 8601) format is assumed. 
		*	
		* 2/ format(string value, string toFormat, String fromFormat) : convert the date
		*		given as String, parsed using fromFormat, and converted to toFormat
		*/
		format : function(value, toFormat, fromFormat) {
		
			var _localDate = this.parse(value, fromFormat);
			
			if (!! toFormat){
				return _localDate.toString(toFormat);
			}
			else{
				return _localDate.toISOString();
			}
		},
		

		/*
		* @ params: 
		*  value : string value of the date, or numeric timestamp, or even a Date.
		*  format : string dateformat. if value is a string and the format is not specified, ATOM is assumed.
		* 
		*/
		parse : function(value, format) {
			
			loadLocale();
			
			var _localDate,
				type = typeof value;
		
			switch(type){
			case "number" : _localDate = new Date(value); 
							break;
			case "object" : _localDate = value; 
							break;
			case "string" : _localDate = (!! format) ? Date.parseExact(value, format) :
															Date.parse(value);	//ATOM is assumed
							break;
			default : throw "dateutils.format : cannot handle supplied argument";
			}
			
			return _localDate;
		}
		
		
	};
});