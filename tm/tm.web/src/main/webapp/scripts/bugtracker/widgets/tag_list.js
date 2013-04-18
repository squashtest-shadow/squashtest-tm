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

define(["jquery", "jqueryui", "jquery.tagit"], function($){
	
	var btTagitCss = {
		'display' : 'inline-block',
		'width' : '98%',
		'line-height' : '1em',
		'border-color' : 'lightgray'
	};
	
	return {
		
		options : {
			
			id : null,
			possibleValues : [],
			rendering : {
				operations : [],
				inputType : {
					name : "tag_list"
				},
				required : false
			}
			
		},
		
		_create : function(){
			
			var delegate = this._createDelegate();
			
			var tags = this._createTags();
			
			var config = {
					
				autocomplete : {
					source : tags
				},
				
				showAutocompleteOnFocus : true,
				allowDuplicates : false,
				readOnly : (this.options.rendering.operations.length===0),
				singleField : true
			}
			
			delegate.tagit(config);
			
			//now fix the css, the default isn't suitable enough
			delegate.next().css(btTagitCss);
			
		},		
		
		_getDelegate : function(){
			return this.element.children('input.bt-delegate');
		},
		
		_createDelegate : function(){
			var elt = $("<input/>",{'type' : 'text', 'class' : 'bt-delegate'});
			this.element.append(elt);
			
			return elt;
		},
		
		_createTags : function(){

			var possibleValues = this.options.possibleValues;
			
			//build the autocomplete source
			var tags = [];
			for (var i=0, len = possibleValues.length ; i < len; i++){
				tags.push(possibleValues[i].scalar);
			}			
			
			return tags;
		},
		
		fieldvalue : function(fieldvalue){
			
			//todo
		},
		
		disable : function(){
			//if doesn't exist, you need to declare and implement it
			this._getDelegate().tagit('option', 'disabled', true);
		},
		
		enable : function(){
			//same remark here
			if (this.options.rendering.operations.length!=0){
				this._getDelegate().tagit('option', 'disabled', false);
			}
		},
		
		createDom : function(field){
			var elt = $('<div/>',{
				'data-widgetname' : 'tag_list',
				'data-fieldid' : field.id
			});
			
			
			return elt;
		}
		
		
	}
	
});