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

define(["jquery", "jqueryui"], function($){

	var searchwidget = $.widget("search.searchTextAreaWidget", {
		
		options : {

		},
		
		_create : function(){
			this._super();
		},
		
		fieldvalue : function(){
			var text = $(this.element.children()[0]).val();
			var id = $(this.element).attr("id");
			return {"type" : "TEXT",
					                 "value" : text};

		}, 
		
		createDom : function(id){
		
			return $('<textarea/>', {
				'data-widgetname' : 'TextArea',
				'data-fieldid' : id,
				'rows' : 5,
				'class' : "search-input"
			});
		}
	 });
	return searchwidget;
});
