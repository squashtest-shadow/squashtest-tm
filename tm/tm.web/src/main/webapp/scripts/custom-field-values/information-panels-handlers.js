/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
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
define(["jquery", "handlebars", "squash.translator", "./lib/cuf-values-utils","jqueryui", "./lib/jquery.staticCustomfield", "./lib/jquery.jeditableCustomfield"], 
		function($, handlebars, translator, utils){
	
	var fromTestCase = " ("+translator.get("label.fromTestCase")+") ";
	
	utils.registerHandlebarHelpers(handlebars);
	
	var template = handlebars.compile(
		'{{#each this}}' +
		'<div class="display-table-row control-group">' +
			'<label class="display-table-cell">{{cuflabel this}}</label>' +
			'<div class="display-table-cell controls">' +
			
			'{{#ifequals binding.customField.inputType.enumName "RICH_TEXT"}}' +
			
				'<span id="{{cufid this}}" class="{{cufclass this}}" data-value-id="{{id}}">{{{value}}}</span>' +
			
			'{{else}} {{#ifequals binding.customField.inputType.enumName "TAG"}}' +
			
				'<ul id="{{cufid this}}" class="{{cufclass this}}" data-value-id="{{id}}">'+
				'{{#each optionValues}}' +
					'<li>{{this}}</li>' +
				'{{/each}}' +
				'</ul>' +
			
			'{{else}}' +
			
				'<span id="{{cufid this}}" class="{{cufclass this}}" data-value-id="{{id}}">{{value}}</span>' +
			
			'{{/ifequals}} {{/ifequals}}' +
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
	
			for (var idx in cufValues){
				
				var cufValue = cufValues[idx],
					selector = (cufValue.binding.customField.denormalized) ? 
							"#denormalized-cuf-value-"+cufValue.id : 
							"#cuf-value-"+cufValue.id;
				
				var elt = container.find(selector);
				
				switch(mode){
				case "static": 
					elt.staticCustomfield(cufValue.binding.customField);
					break;
				case "editable" : 
					elt.editableCustomfield(cufValue.binding.customField);
					break;
					
				case "jeditable" :
					elt.jeditableCustomfield(cufValue.binding.customField, cufValue.id);
					break;
				}
			}
			
			
			
		} 
		
		
	};
	
});
