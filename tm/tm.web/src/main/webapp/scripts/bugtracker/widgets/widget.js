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

/*
 * This is not an actual widget, it's a documentation on what a widget should be.
 * 
 * A Widget is a jQuery widget, see below for the methods that should be accessible. The domain of that widget must be 'squashbt'.
 * See the various documentation on creating widget for details.
 * 
 * The constructor of the widget will be invoked using an argument of type 'field', see below. The widget is editable only if 
 * field.rendering.operations[] is not empty. This array might contain "set", "add", "remove", that you may use if you like to. see method 
 * fieldvalue(fieldvalue) below.    
 * 
 * The widget must also define a method 'createDom', that is not part of the widget itself, but rather an attribute of the widget constructor.
 * see below.
 * 
 * 
 * --------------
 * 
 * Possible arguments : 
 * - field : see 'options' below, or also org.squashtest.tm.bugtracker.advanceddomain.Field
 * - fieldvalue : see ../domain/FieldValue, or also org.squashtest.tm.bugtracker.advanceddomain.FieldValue
 */
 
define(["jquery", "jqueryui"], function($){
	
	$.widget("squashbt.somewidget", {
		
		options : {
			//defaults value for the field
			id : null,
			label : null,
			possibleValues : [],
			rendering : {
				operations : [],
				inputType : {
					name : "unknown",
					original : "unknown",
					fieldSchemeSelector : false
				},
				required : false
			}
		},
		
		_create : function(){
			//whatever you need
		},
		
		fieldvalue : function(fieldvalue){
			//if fieldvalue is null or undefined, acts as a getter. Else, it's a setter.
		}
		
		
	});
	
	$.squashbt.somewidget.createDom = function(field){
		/*
		 * create the dom element that best fits this field. This dom element is returned as a jquery object.
		 * The following attributes MUST be set : 
		 * - data-btwidget : the name of this widget
		 * - data-fieldid : the id of this field, ie field.id
		 */
	}
	
});