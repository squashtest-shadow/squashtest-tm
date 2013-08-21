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

define(["jquery", "squash.translator", "datepicker/require.jquery.squash.datepicker-locales", "jqueryui", "jeditable.datepicker"], 
		function($, translator, regionale){

	var searchwidget = $.widget("search.searchDateWidget", {
		
		options : {

		},
		
		_create : function(){
			this._super();
		},

		fieldvalue : function(){
			var checked = $($(this.element.children()[0]).children()[0]).prop('checked');
			var startDate = $($(this.element.children()[0]).children()[2]).datepicker('getDate');
			var endDate = $($(this.element.children()[0]).children()[4]).datepicker('getDate');
			var id = $(this.element).attr("id");
			if(checked){
				
				var toFormat = "yy-mm-dd";
				var formattedStartDate = $.datepicker.formatDate(toFormat, startDate);
				var formattedEndDate = $.datepicker.formatDate(toFormat, endDate);
				
				return {"type" : "TIME_INTERVAL",
						"startDate" : formattedStartDate,
						"endDate" : formattedEndDate};
				} else {
					return {"type" : "TIME_INTERVAL",
						"startDate" : null,
						"endDate" : null};
				}
		}, 
		
		createDom : function(id){
			
			var localemeta = {
					format : 'squashtm.dateformatShort.js',
					locale : 'squashtm.locale'
				};
				
			var message = translator.get(localemeta);
			this.options.message = message;
				
			var language = regionale[message.locale] || regionale;
				
			var pickerconf = $.extend(true, {}, language, {dateFormat : message.format});
				
			$($(this.element.children()[0]).children()[2]).datepicker(pickerconf);
			$($(this.element.children()[0]).children()[4]).datepicker(pickerconf);	
		}
	 });
	return searchwidget;
});
