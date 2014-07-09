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
 * 
 * The CUFValuesCreator handles CustomFieldValues for entities that are being created.
 * They differ from the standard handling of the custom field values because, since they are
 * being created, they don't have an id yet. That's why we have a separate manager here.
 *  
 */

define(
		[ "jquery", "./cuf-values-utils", "squash.configmanager", "jqueryui",
				"jquery.squash.jeditable", "jeditable.datepicker"],
		function($, utils, confman) {

			function noPostFn(value) {
				return value;
			}

			function initDatepicker(input) {

				var conf = {
					type : 'datepicker',
					datepicker : confman.getStdDatepicker()
				};

				input.editable(noPostFn, conf);

			}
			
			function initCkeditor(input){
				var conf = confman.getStdCkeditor();
				input.ckeditor(function(){}, conf);
			}

			/*
			 * settings : - url : the url where to fetch the creator panel -
			 * table : the <table/> element that (will) hold the elements, as a
			 * jQuery object (but not as a jQuery.DataTable)
			 */
			function CUFValuesCreator(settings) {

				this.table = settings.table;

				if (this.table === undefined || !this.table.is('table')) {
					throw "illegal argument : the settings must provide an attribute 'table' referencing " + 
					"a jquery table";
				}

				this.url = settings.url;

				/*
				 * loads the custom field values into the table using the given
				 * url also stores that url for future reference
				 */
				this.loadPanel = function(url) {
					var pleaseWait = $('<tr class="cuf-wait" style="line-height:10px;"><td colspan="2" class="waiting-loading"></td></tr>');
					var table = this.table;

					// cleanup of the previous calls (if any)
					table.find('.create-node-custom-field-row').remove(); 

					table.append(pleaseWait);

					var self = this;

					$.get(url, null, null, "html").success(
							function(html) {
								table.find(".cuf-wait").remove();

								// because it wouldn't work otherwise, we must
								// strip the result of the license header
								var fixed = $.trim(html.replace(
										/<\!--[\s\S]*--\>/, ''));
								table.append(fixed);
								self.init(table);

								this.url = url;
							});
				};

				/* reload the custom field values using the last used url */
				this.reloadPanel = function() {
					this.loadPanel(this.url);
				};

				/* init the widgets used by the custom field values */
				this.init = function() {
					var table = this.table;
					
					var bindings = table.find(".create-node-custom-field");
					if (bindings.length > 0) {
						bindings.each(function() {

							var input = $(this);
							var defValue = input.data('default-value');
							var inputType = input.data('input-type');

							if (inputType === "DATE_PICKER") {
								initDatepicker(input);
							}
							else if (inputType === "RICH_TEXT"){
								initCkeditor(input);
							}
						});

						this.reset(table);
					}
					
				};

				/*
				 * reset the values of the custom field values. Will not
				 * reinitialise the widgets themselves.
				 */
				this.reset = function() {
					var table = this.table;
					var bindings = table.find(".create-node-custom-field");
					if (bindings.length > 0) {
						bindings.each(function() {
							var input = $(this);
							var defValue = input.data('default-value');
							var inputType = input.data('input-type');

							if (inputType === "CHECKBOX") {
								input.prop('checked', (defValue === true));
							} else if (inputType === "DATE_PICKER") {
								var format = input.data('format');
								var displayedDate = utils.convertStrDate(
										$.datepicker.ATOM, format, defValue);
								input.text(displayedDate);
							} else {
								input.val(defValue);
							}
						});
					}
				};
				
				this.destroy = function(){
					var table = this.table;
					
					var bindings = table.find(".create-node-custom-field");
					if (bindings.length > 0) {
						bindings.each(function() {

							var input = $(this);
							var inputType = input.data('input-type');

							if (inputType === "DATE_PICKER") {
								input.editable("destroy");
							}
							else if (inputType === "RICH_TEXT"){
								var id = input.attr('id');
								var inst = CKEDITOR.instances[id];
								if (!!inst){
									inst.destroy(true);
								}
							}
						});
					}			
				};

				/*
				 * returns : a map of { id, value }, suitable for posting with
				 * the rest of the entity model
				 */
				this.readValues = function() {
					var result = {};
					var table = this.table;

					var cufs = table.find(".create-node-custom-field");
					if (cufs.length > 0) {
						cufs.each(function() {
							var input = $(this);
							var inputType = input.data('input-type');
							var value = null;
							if (inputType === "CHECKBOX") {
								value = input.prop('checked');
							} else if (inputType === "DATE_PICKER") {
								var format = input.data('format');
								value = utils.convertStrDate(format,
										$.datepicker.ATOM, input.text());
							} else {
								value = input.val();
							}
							result[this.id] = value;
						});
					}
					return result;
				};

			}
			

			return CUFValuesCreator;

		});