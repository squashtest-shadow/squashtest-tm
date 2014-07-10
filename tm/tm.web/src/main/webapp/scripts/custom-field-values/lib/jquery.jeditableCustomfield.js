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
define(
		[ "jquery", "./cuf-values-utils", "squash.configmanager", "jqueryui",
				"jquery.squash.jeditable", "jeditable.datepicker",
				"datepicker/jquery.squash.datepicker-locales" ],
		function($, utils, confman) {

			function buildPostFunction(idOrURLOrPostfunction, postProcess) {

				var postProcessFn = postProcess || function(value) {
					return value;
				};

				var postFunction;

				if (typeof idOrURLOrPostfunction === "function") {
					postFunction = idOrURLOrPostfunction;
				} else if (typeof idOrURLOrPostfunction === "string") {
					postFunction = function(value) {
						return $.ajax({
							url : idOrURLOrPostfunction,
							data : {
								'value' : value
							},
							type : 'POST'
						});
					};
				} else if (typeof idOrURLOrPostfunction === undefined) {
					postFunction = function(value) {
						var id = $(this).data('value-id');
						var url = squashtm.app.contextRoot	+ "/custom-fields/values/" + id;
						return $.ajax({
							url : url,
							data : {
								'value' : value
							},
							type : 'POST'
						});
					};
				} else {
					// assumed to be an integer
					postFunction = function(value) {
						var url = squashtm.app.contextRoot	+ "/custom-fields/values/"	+ idOrURLOrPostfunction;
						return $.ajax({
							url : url,
							data : {
								'value' : value
							},
							type : 'POST'
						});
					};
				}
				

				return function(value, settings) {
					var data = postProcessFn(value, settings);
					postFunction.call(this, data);
					return value;
				};

			}

			function getBasicConf() {
				return confman.getStdJeditable();
			}

			function initAsDatePicker(elts, cufDefinition,
					idOrURLOrPostfunction) {

				var conf = getBasicConf();

				var format = cufDefinition.format;
				var locale = cufDefinition.locale;

				conf.type = 'datepicker';
				conf.datepicker = $.extend({
					dateFormat : format
				}, $.datepicker.regional[locale]);

				var postProcess = function(value, settings) {
					return utils.convertStrDate(format, $.datepicker.ATOM,
							value);
				};

				var postFunction = buildPostFunction(idOrURLOrPostfunction,
						postProcess);

				elts.editable(postFunction, conf);

			}

			function initAsList(elts, cufDefinitions, idOrURLOrPostfunction) {
				if (elts.length === 0){
					return;
				}

				var prepareSelectData = function(options, selected) {

					var i = 0, length = options.length;
					var result = {};

					var opt;
					for (i = 0; i < length; i++) {
						opt = options[i].label;
						result[opt] = opt;
					}

					result.selected = selected;
					return result;

				};

				elts.each(function() {

					var jqThis = $(this);
					var selected = jqThis.text();

					var conf = getBasicConf();
					conf.type = 'select';
					conf.data = prepareSelectData(
							cufDefinitions.options, selected);

					var postFunction = buildPostFunction(idOrURLOrPostfunction);

					jqThis.editable(postFunction, conf);

				});
			}

			function initAsPlainText(elts, cufDefinition, idOrURLOrPostfunction) {

				var conf = getBasicConf();
				conf.type = 'text';

				var postFunction = buildPostFunction(idOrURLOrPostfunction);

				elts.editable(postFunction, conf);

			}

			function initAsCheckbox(elts, cufDefinition, idOrURLOrPostfunction) {

				if (elts.length === 0){
					return;
				}
				
				var postFunction = buildPostFunction(idOrURLOrPostfunction);

				var clickFn = function() {
					var jqThis = $(this);
					var checked = jqThis.prop('checked');
					postFunction.call(jqThis, checked);
				};

				elts.each(function() {

					var jqThis = $(this);
					var chkbx;

					if (jqThis.is('input[type="checkbox"]')) {
						chkbx = jqThis;
					} else if (jqThis.find('input[type="checkbox"]').length > 0) {
						chkbx = jqThis.find('input[type="checkbox"]');
					} else {
						chkbx = utils.appendCheckbox(jqThis);
					}

					chkbx.enable(true);
					chkbx.click(clickFn);

				});

			}

			
			$.fn.customField = function(cufDefinition, idOrURLOrPostfunction) {

				var type = cufDefinition.inputType.enumName;

				if (type === "DATE_PICKER") {
					initAsDatePicker(this, cufDefinition, idOrURLOrPostfunction);
				} else if (type === "DROPDOWN_LIST") {
					initAsList(this, cufDefinition, idOrURLOrPostfunction);
				} else if (type === "PLAIN_TEXT") {
					initAsPlainText(this, cufDefinition, idOrURLOrPostfunction);
				} else {
					initAsCheckbox(this, cufDefinition, idOrURLOrPostfunction);
				}

			};

		});
