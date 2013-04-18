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

/**
 * That widget is awkward because it delegates almost all of its behavior to $.squashTagit. Separating the btwidget and the squashTagit widget is 
 * a good way to prevent undesirable clashes with the type definition
 * 
 */

define(["jquery", "jqueryui", "http://localhost/scripts/scripts/squashtest/jquery.squash.tagit.js"], function($){

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
				singleFieldNode : this.element
			}
			
			if (tags.length>0){
				$.extend(config, {
					availableTags : tags,
					showAutocompleteOnFocus : true,
					constrained : true,
					'essspectacularrr !' : true
				});
			}
			
			delegate.squashTagit(config);
	
			if (! this.canEdit()){
				this.disable();
			}
			
		},		
		
		canEdit : function(){
			return (this.options.rendering.operations.length!==0);
		},
		
		_getDelegate : function(){
			return this.element.next('ul.bt-delegate');
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
			
			var delegate = this._getDelegate();
			
			if (fieldvalue===null || fieldvalue === undefined){
				var selected = delegate.squashTagit('assignedTags');
				
			}
			else{
				
			}
			
		},
		
		/*
		 * 
		 * enable() and disable() are scrapped from the source : see createTag() from tagit.js
		 * 
		 */
		disable : function(){
			this._getDelegate().squashTagit('disable');
		},
		
		enable : function(){
			this._getDelegate().squashTagit('enable');
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