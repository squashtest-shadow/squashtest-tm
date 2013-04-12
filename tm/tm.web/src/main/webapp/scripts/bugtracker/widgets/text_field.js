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


define(["jquery"], function($){

	return {
		
		options : {
			rendering : {
				inputType : {
					name : "text_field"
				}
				
			}
		},
		
		_create : function(){
			var field = this.options;
			var $this = this.element.eq(0);
			if (field.rendering.operations.length===0){
				$this.prop('disabled', true);
			}
		},
		
		fieldvalue : function(fieldvalue){
			if (fieldvalue===null || fieldvalue === undefined){
				var text = this.element.eq(0).val();
				return new FieldValue(text, text);
			}
			else{
				this.element.eq(0).val(fieldvalue.scalar);
			}
		}, 

		createDom : function(field){
			return $('<input />', {
				'type' : 'text',
				'data-btwidget' : 'text_field',
				'data-fieldid' : field.id
			});
		}
	}

})
