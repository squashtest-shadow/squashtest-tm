/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
 * accepts as parameter (in that order) :
 * - a jquery selector to append to, 
 * - an array of CustomFieldValueModel, 
 * - a mode : "static", "editable", "jeditable"
 * can edit or not 
 * 
 */
define(["jquery", "handlebars", "jqueryui", "./lib/jquery.staticCustomfield", "./lib/jquery.jeditableCustomfield"], 
		function($, handlebars){
	
	/*
	 * little helper thanks to stack overflow !
	 * 
	 */
	
	handlebars.registerHelper('ifequals', function(cuftype, expected, options) {
	  return (cuftype === expected) ? options.fn(this) : options.inverse(this);
	});
	
	var template = handlebars.compile(
		'{{#each this}}' +
		'<div class="display-table-row control-group">' +
			'<label class="display-table-cell">{{binding.customField.label}}</label>' +
			'<div class="display-table-cell controls">' +
			'{{#ifequals binding.customField.inputType.enumName "RICH_TEXT"}}' +
				'<span id="cuf-value-{{id}}" class="custom-field" data-value-id="{{id}}">{{{value}}}</span>' +
			'{{else}}' +
				'<span id="cuf-value-{{id}}" class="custom-field" data-value-id="{{id}}">{{value}}</span>' +
			'{{/ifequals}}' +
			'<span class="help-inline not-displayed">&nbsp;</span>' +
			'</div>' +			
		'</div>' +
		'{{/each}}'
		);
	
	
	
	return {
		
		init : function(containerSelector, cufValues, mode){
			
			var html = template(cufValues);
			
			var container = $(containerSelector);
						
			container.append(html);
			
			var allFields = container.find('.custom-field');
			if (allFields.length > 0){
				allFields.each(function(idx, elt){
					var cufValue = cufValues[idx];
					switch(mode){
					case "static": 
						$(elt).staticCustomfield(cufValue.binding.customField);
						break;
					case "editable" : 
						$(elt).editableCustomfield(cufValue.binding.customField);
						break;
						
					case "jeditable" :
						$(elt).jeditableCustomfield(cufValue.binding.customField, cufValue.id);
						break;
					}
				});
			}
			
		} 
		
		
	};
	
});
