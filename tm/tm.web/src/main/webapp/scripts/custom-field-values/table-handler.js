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
define([ "jquery", "./lib/cuf-values-utils", "./lib/jquery.staticCustomfield", "./lib/jquery.jeditableCustomfield" ], 
		function($, utils) {
	/*********************************************************************************************************************
	 * 
	 * JS DATATABLE CONFIGURATION
	 * 
	 ********************************************************************************************************************/

	// ********************************** column definitions
	// ******************************
	function createColumnDefs(cufDefinitions) {

		var columns = [];
		var i = 0, total = cufDefinitions.length;

		for (i = 0; i < total; i++) {

			var code = cufDefinitions[i].code;
			var newColumn;
			
			if(cufDefinitions[i].denormalized){
				newColumn = {
						'bVisible' : true,
						'bSortable' : false,
						'mDataProp' : "denormalizedFields." + code + ".value",
						'sClass' : 'denormalized-field-value denormalized-field-' + code,
						'sWidth' : "5em",
						'aTargets' : [ 'denormalized-field-' + code ]
					};
			} else {
				newColumn = {
					'bVisible' : true,
					'bSortable' : false,
					'mDataProp' : "customFields." + code + ".value",
					'sClass' : 'custom-field-value custom-field-' + code,
					'sWidth' : "5em",
					'aTargets' : [ 'custom-field-' + code ]
				};
			}
			columns.push(newColumn);

		}

		return columns;

	}

	function mergeColumnDefs(regularColumnDefs, cufColumnDefs, insertionIndex) {
		/*
		 * update the aTargets of the existing columns if they use an index (instead of a classname) and if their index is
		 * above the insertionIndex
		 */
		var i = 0, regularLength = regularColumnDefs.length, cufLength = cufColumnDefs.length;

		for (i = 0; i < regularLength; i++) {
			var regDef = regularColumnDefs[i];
			var aTarget = regDef.aTargets[0];
			if ((typeof aTarget == "number") && (aTarget >= insertionIndex)) {
				regDef.aTargets[0] = aTarget + cufLength;
			}
		}

		// no we can merge the column defs
		var spliceArgs = [ insertionIndex, 0 ].concat(cufColumnDefs);
		Array.prototype.splice.apply(regularColumnDefs, spliceArgs);

		// return
		return regularColumnDefs;

	}

	// ************************************ table draw callback
	// *********************************

	function mapDefinitionsToCode(cufDefinitions) {

		var resultMap = {};

		var i = 0, length = cufDefinitions.length;

		for (i = 0; i < length; i++) {
			var currentDef = cufDefinitions[i];
			resultMap[currentDef.code] = currentDef;
		}

		return resultMap;
	}

	function makePostFunction(cufCode, table) {
		return function(value) {

			var row = $(this).parents('tr').get(0);
			var cufId = table.fnGetData(row).customFields[cufCode].id;

			var url = squashtm.app.contextRoot + "/custom-fields/values/" + cufId;

			return $.ajax({
				url : url,
				type : 'POST',
				data : {
					value : value
				}
			});
		};
	}
	
	function makeDenormalizedPostFunction(cufCode, table) {
		return function(value) {

			var row = $(this).parents('tr').get(0);
			var cufId = table.fnGetData(row).denormalizedFields[cufCode].id;

			var url = squashtm.app.contextRoot + "/denormalized-fields/values/" + cufId;

			return $.ajax({
				url : url,
				type : 'POST',
				data : {
					value : value
				}
			});
		};
	}

	function createCufValuesDrawCallback(cufDefinitions, editable) {

		var definitionMap = mapDefinitionsToCode(cufDefinitions);

		return function() {

			var table = this;
			var defMap = definitionMap;
			var isEditable = editable;

			// A cell holds a custom field value if it has the class
			// .custom-field-value, and if the data model is not empty
			// for that one.
			var cufCells = table.find('td.custom-field-value').filter(function() {
				return (table.fnGetData(this) !== null);
			});

			// now wrap the content with a span
			cufCells.wrapInner('<span/>');

			for ( var code in defMap) {

				var def = defMap[code];
				var spans = table.find('td.custom-field-' + code + '>span');

				if (isEditable) {
					var postFunction = makePostFunction(code, table);
					spans.jeditableCustomfield(def, postFunction);
				}
				else{
					spans.staticCustomfield(def);
				}

			}

		
			var dfvCells = table.find('td.denormalized-field-value').filter(function() {
				return (table.fnGetData(this) !== null);
			});

			// now wrap the content with a span
			dfvCells.wrapInner('<span/>');

			for ( var code1 in defMap) {

				var def1 = defMap[code1];
				var spans1 = table.find('td.denormalized-field-' + code1 + '>span');

				if (isEditable) {
					var postFunction1 = makeDenormalizedPostFunction(code1, table);
					spans1.jeditableCustomfield(def1, postFunction1);
				}
				else{
					spans1.staticCustomfield(def1);
				}

			}
		};
	}

	// ******************************************** datasource model
	// configuration **************************************

	function defaultFnServerDataImpl(sSource, aoData, fnCallback, oSettings) {
		oSettings.jqXHR = $.ajax({
			"dataType" : 'json',
			"type" : oSettings.sServerMethod,
			"url" : sSource,
			"data" : aoData,
			"success" : fnCallback
		});
	}

	function createDefaultDefinitions(cufDefinitions) {
		var i = 0, length = cufDefinitions.length, code, result = {};

		for (i = 0; i < length; i++) {
			code = cufDefinitions[i].code;
			result[code] = {
				id : null,
				value : null,
				code : null
			};
		}

		return result;
	}

	function fillMissingCustomFields(aaData, defaultDefinitions) {

		var length = aaData.length, i = 0;

		for (i = 0; i < length; i++) {

			var copyDefaults = $.extend({}, defaultDefinitions);

			// create the field 'customFields' if doesn't exist
			if (aaData[i].customFields === undefined) {
				aaData[i].customFields = copyDefaults;
			}
			// else we merge the defaults with the existing field
			else {
				aaData[i].customFields = $.extend(copyDefaults, aaData[i].customFields);
			}
		}

		return aaData;
	}

	// the goal here is to prevent faulty table redraw if some custom
	// fields aren't part of the model of a given row.
	// it may happen for tables mixing heterogeneous data, eg action
	// steps/call steps, or testcase from project A or project B.
	//
	// Hence we aim to fill the holes in the model, by decorating the
	// function fnCallback, then invoke the initial
	// decoratedFnServerData with it.
	function ajaxPostProcessorFactory(defaultDefinitions, decoratedFnServerData) {

		// now the decorated fnServerData function, that will invoke the
		// original fnServerData with the decorated callback
		return function(sSource, aoData, fnCallback, oSettings) {

			var decoratedCallback = function(json, xhr, statusText) {

				var ajaxProp = oSettings.sAjaxDataProp;

				var origData = (ajaxProp !== "") ? json[ajaxProp] : json;
				var fixedData = fillMissingCustomFields(origData, defaultDefinitions);

				var fixedJson;
				if (ajaxProp !== "") {
					fixedJson = json;
					fixedJson[ajaxProp] = fixedData;
				} else {
					fixedJson = fixedData;
				}

				fnCallback.call(this, fixedJson, xhr, statusText);
			};

			decoratedFnServerData.call(this, sSource, aoData, decoratedCallback, oSettings);
		};
	}

	// ************************ main decorator
	// ************************************

	function decorateTableSettings(tableSettings, cufDefinitions, index, isEditable) {

		var editable = (isEditable === undefined) ? false : isEditable;

		// decorate the column definitions
		var cufDefs = createColumnDefs(cufDefinitions);
		var origDef = tableSettings.aoColumnDefs;
		tableSettings.aoColumnDefs = mergeColumnDefs(origDef, cufDefs, index);

		// decorate the model and ajax processor
		var defaultDefinitions = createDefaultDefinitions(cufDefinitions);

		if (tableSettings.aaData !== undefined) {
			tableSettings.aaData = fillMissingCustomFields(tableSettings.aaData, defaultDefinitions);
		}

		var origFnServerData = tableSettings.fnServerData || defaultFnServerDataImpl;
		tableSettings.fnServerData = ajaxPostProcessorFactory(defaultDefinitions, origFnServerData);

		// decorate the table draw callback
		var oldDrawCallback = tableSettings.fnDrawCallback;

		var addendumCallback = createCufValuesDrawCallback(cufDefinitions, editable);

		tableSettings.fnDrawCallback = function() {
			if(!!oldDrawCallback){
				oldDrawCallback.apply(this, arguments);
			}
			addendumCallback.call(this);
		};

		return tableSettings;
	}

	/*********************************************************************************************************************
	 * 
	 * DOM TABLE CONFIGURATION
	 * 
	 ********************************************************************************************************************/

	function decorateDOMTable(zeTable, cufDefinitions, index) {
		var table = (zeTable instanceof jQuery) ? zeTable : $(zeTable);

		// create the new header columns
		var newTDSet = $();

		var i = 0, length = cufDefinitions.length;

		for (i = 0; i < length; i++) {
			var def = cufDefinitions[i];
			var newTD;
			if(cufDefinitions[i].denormalized){
				newTD = $('<th class="denormalized-field-' + def.code + '">' + def.label + '</th>'); 
			} else {
				newTD = $('<th class="custom-field-' + def.code + '">' + def.label + '</th>');
			}
			newTDSet = newTDSet.add(newTD);
		}

		// insert them
		var header = table.find('thead tr');
		var firstHeaders = header.find('th').slice(0, index);
		header.prepend(newTDSet);
		header.prepend(firstHeaders);

	}

	return {
		decorateTableSettings : decorateTableSettings,
		decorateDOMTable : decorateDOMTable
	};

});