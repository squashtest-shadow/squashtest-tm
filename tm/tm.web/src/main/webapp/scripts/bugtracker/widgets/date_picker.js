/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
 * As of Squash TM 1.8 the date format can come in two flavor :
 * 1 - datepicker format : the legacy one. The property of the conf object is rendering.inputType.meta['date-format']. Now deprecated.
 * 2 - java format : the prefered one. The property of the conf object is rendering.inputType.meta['format'].
 *  
 */
define(["jquery", "../domain/FieldValue", "squash.configmanager", "squash.dateutils", "squash.translator", "jqueryui"], 
		function($, FieldValue, confman, dateutils, translator){


	return {
		
		options : {
			rendering : {
				inputType : {
					name : "date_picker",
					meta : { 'date-format' : "yy-mm-dd" }
				}
				
			}
		},
		
		_create : function(){
			
			this._super();
			
			var pickerconf = confman.getStdDatepicker();
			
			this.element.datepicker(pickerconf);

		},
		
		fieldvalue : function(fieldvalue){
			var date,strDate;
			if (fieldvalue===null || fieldvalue === undefined){
				
				date = this.element.datepicker('getDate');
				strDate = "";
				
				if (!! date){
					strDate = this.formatDate(date);					
				}
				
				var typename = this.options.rendering.inputType.dataType;
				return new FieldValue("--", typename, strDate);
			}
			else{
				strDate = fieldvalue.scalar;
				if (!!strDate){
					date = this.parseDate(strDate);
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
		},
		
		formatDate : function(date){

			var jsformat = this.options.rendering.inputType.meta['date-format'],		// the datepicker format (deprecated)
				javaformat = this.options.rendering.inputType.meta['format'];			// the java format
			
			if (!! javaformat){
				return dateutils.format(date, javaformat);
			}
			else{
				return $.datepicker.formatDate(jsformat, date);
			}
		},
		
		parseDate : function(strdate){
			var jsformat = this.options.rendering.inputType.meta['date-format'],		// the datepicker format (deprecated)
				javaformat = this.options.rendering.inputType.meta['format'];			// the java format
			
			if (!! javaformat ){
				return dateutils.parse(strdate, javaformat);
			}
			else{
				return $.datepicker.parseDate(jsformat, strdate);
			}
		},
		
		validate : function(){
			// first let's check the default behavior
			var messages = this._super();
			if (messages.length>0){
				return messages;
			}
			
			var txtdate = this.element.val();
			
			if ( txtdate !== ""){
				/* 
				 * warning : the format here must be the 'java' form of the dateformat returned by confman.getStdDatepicker()
				 * (the later returning the 'datepicker' form of this dateformat). (why people don't you agree on date formats).
				 * Here it is hardcoded to 'squashtm.dateformatShort'.
				 */
				var format = translator.get('squashtm.dateformatShort');	
				
				if (! dateutils.dateExists(txtdate, format)){
					messages.push('error.notadate');
				}
			}
			
			return messages;
		}
	};

});
