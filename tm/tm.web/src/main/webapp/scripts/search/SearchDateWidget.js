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

define(["jquery",  "squash.configmanager", "jqueryui", "jeditable.datepicker"], 
		function($, confman){

	var searchwidget = $.widget("search.searchDateWidget", {
		
		options : {

		},
		
		_create : function(){
			this._super();
		},

		fieldvalue : function(value){
			
			if(!value){
			var checked = $($("input",$($(this.element.children()[0])))[0]).prop("checked");
			var startDate = $($("input",$($(this.element.children()[0])))[1]).datepicker('getDate');
			var endDate = $($("input",$($(this.element.children()[0])))[2]).datepicker('getDate');
			var id = $(this.element).attr("id");
			if(checked){
				
				var toFormat = "yy-mm-dd";
				var formattedStartDate = $.datepicker.formatDate(toFormat, startDate);
				var formattedEndDate = $.datepicker.formatDate(toFormat, endDate);
				
				return {"type" : "TIME_INTERVAL",
						"startDate" : formattedStartDate,
						"endDate" : formattedEndDate};
				} else {
					return null;
				}
			} else {
				$($("input",$($(this.element.children()[0])))[0]).attr('checked', 'checked');
				$($("input",$($(this.element.children()[0])))[1]).datepicker('setDate', new Date(value.startDate));
				$($("input",$($(this.element.children()[0])))[2]).datepicker('setDate', new Date(value.endDate));
			}
		}, 
		
		createDom : function(id){
			
			var pickerconf = confman.getStdDatepicker();
				
			$($("input",$($(this.element.children()[0])))[1]).datepicker(pickerconf);
			$($("input",$($(this.element.children()[0])))[2]).datepicker(pickerconf);	
		}
	 });
	return searchwidget;
});
