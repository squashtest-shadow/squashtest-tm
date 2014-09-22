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
define(["jquery", "handlebars", "squash.translator", "jqueryui", "./lib/jquery.staticCustomfield", "./lib/jquery.jeditableCustomfield"],
		function($, handlebars, translator){
	"use strict";

	var fromTestCase = " ("+translator.get("label.fromTestCase")+") ";

	/*
	 * little helper thanks to stack overflow !
	 *
	 */

	handlebars.registerHelper('ifequals', function(cuftype, expected, options) {
		return (cuftype === expected) ? options.fn(this) : options.inverse(this);
	});

	handlebars.registerHelper('cuflabel', function(value) {
		var cuf = value.binding.customField, lbl = cuf.label;
		return (cuf.denormalized) ? lbl + fromTestCase : lbl;
	});

	handlebars.registerHelper('cufid', function(value) {
		var prefix = (value.binding.customField.denormalized) ? "denormalized-cuf-value-" : "cuf-value-";
		return prefix + value.id;
	});

	handlebars.registerHelper('cufclass', function(value) {
		return (value.binding.customField.denormalized) ? "denormalized-custom-field" : "custom-field";
	});



	var template = handlebars.compile(
		'{{#each this}}' +
		'<div class="display-table-row control-group">' +
			'<label class="display-table-cell">{{cuflabel this}}</label>' +
			'<div class="display-table-cell controls">' +
			'{{#ifequals binding.customField.inputType.enumName "RICH_TEXT"}}' +
				'<span id="{{cufid this}}" class="{{cufclass this}}" data-value-id="{{id}}">{{{value}}}</span>' +
			'{{else}}' +
				'<span id="{{cufid this}}" class="{{cufclass this}}" data-value-id="{{id}}">{{value}}</span>' +
			'{{/ifequals}}' +
			'<span class="help-inline not-displayed">&nbsp;</span>' +
			'</div>' +
		'</div>' +
		'{{/each}}'
		);



	return {

		init : function(containerSelector, cufValues, mode) {

		var html = template(cufValues);

		var container = $(containerSelector);

		container.append(html);

		var addEditableStyle = function(dom) {
			$(dom).find("p:first").addClass("editable");
		};

		for ( var idx in cufValues) {
			var cufValue = cufValues[idx], selector = (cufValue.binding.customField.denormalized) ? "#denormalized-cuf-value-" +
					cufValue.id
					: "#cuf-value-" + cufValue.id;

			var elt = container.find(selector);

			switch (mode) {
			case "static":
				elt.staticCustomfield(cufValue.binding.customField);
				break;

			case "editable":
				elt.editableCustomfield(cufValue.binding.customField);
				addEditableStyle(elt[0]);
				break;

			case "jeditable":
				elt.jeditableCustomfield(cufValue.binding.customField, cufValue.id);
				addEditableStyle(elt[0]);
				break;
			}
		}
	}
};

});
