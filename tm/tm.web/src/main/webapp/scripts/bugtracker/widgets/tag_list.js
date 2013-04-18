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
				readOnly : (this.options.rendering.operations.length===0),
				singleFieldNode : this.element
			}
			
			delegate.tagit(config);
			
			//now fix the css, the default isn't suitable enough
			//todo : move the css to a css file
			delegate.css(btTagitCss);
			delegate.removeClass('ui-corner-all');
	
			//this._super();
			
		},		
		
		_getDelegate : function(){
			return this.element.children('ul.bt-delegate');
		},
		
		_createDelegate : function(){
			var elt = $("<ul/>",{ 'class' : 'bt-delegate' });
			this.element.after(elt);			
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
		
		/*
		 * 
		 * enable() and disable() are scrapped from the source : see createTag() from tagit.js
		 * 
		 */
		disable : function(){
			//disable the tags
			var tagli = this._getDelegate().tagit('tagInput');
			tagli.removeClass('tagit-choice-editable');
			tagli.find('a').remove();
			tagli.addClass('tagit-choice-read-only');
			
			//disable the text input
			
		},
		
		enable : function(){
			
		},
		
		createDom : function(field){
			var elt = $('<input/>',{
				'data-widgetname' : 'tag_list',
				'data-fieldid' : field.id,
				'type' : 'text',
				'id' : "bttaglist-"+field.id,
				'class' : 'not-displayed'
			});
			
			
			return elt;
		}
		
		
	}
	
});