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
define(["jquery",  "jquery.squash.rangedatepicker", "squash.translator", "workspace.storage" ], 
		function($, rangedatepicker, translator, storage){
	
	"use strict";

	var tableSelector = '.test-plan-table';
	
	/*
	 * Prepare some default values 
	 */
	var _weights = translator.get({
		'VERY_HIGH' : 'test-case.importance.VERY_HIGH',
		'HIGH' : 'test-case.importance.HIGH',
		'MEDIUM' : 'test-case.importance.MEDIUM',
		'LOW' : 'test-case.importance.LOW'		
	});
	// add the level
	_weights['VERY_HIGH'] 	= '1-'+_weights['VERY_HIGH']; 
	_weights['HIGH'] 		= '2-'+_weights['HIGH']; 
	_weights['MEDIUM']		= '3-'+_weights['MEDIUM']; 
	_weights['LOW'] 		= '4-'+_weights['LOW']; 
	

	/*
	 * the FilterMode. No conf needed for now.
	 * 
	 */
	
	function FilterMode(){
		
		// ****** setup *******
		
		var table = $(tableSelector),
			entityId = table.data('entity-id'),
			entityType = table.data('entity-type');
		
		if (!entityId) {
			throw "sortmode : entity id absent from table data attributes";
		}
		if (!entityType) {
			throw "sortmode : entity type absent from table data attributes";
		}

		this.key = entityType + "-filter-" + entityId;
		
		this.isFiltering = false;
		
		
		// ****** private methods *******

		
		function hideFilterFields(_bNoredraw) {
			var squashtable = table.squashTable(),
				settings = squashtable.fnSettings();
			
			table.find(".th_input").hide();
	
			for (var i=0;i<settings.aoPreSearchCols.length; i++){
				settings.aoPreSearchCols[i].sSearch = '';
			}
	
			if ( _bNoredraw !== true){
				squashtable.refresh();
			}
		}
	
		function showFilterFields() {
			var squashtable = table.squashTable(),
				settings = squashtable.fnSettings();
	
			table.find(".th_input").show();
	
			$.each(settings.aoColumns, function(idx){
				var column = settings.aoColumns[idx];
				var $th = $(column.nTh);
				if (column.bVisible && $th.is('.tp-th-filter')){
					column.sSearch = $th.find('.filter_input').val();
					settings.aoPreSearchCols[idx].sSearch = column.sSearch;
				}
			});
	
			squashtable.refresh();
		}
		
		// ************** public methods *************
		
		this.toggleFilter = function(){
			if (this.isFiltering){
				this.isFiltering = false;
				hideFilterFields(false);
			}
			else{
				this.isFiltering = true;
				showFilterFields();
			}
			
			return this.isFiltering;
		}
	
		this.initializeFilterFields = function(initconf) {
	
			function _createCombo(th, id, content){
				if (!th || !content) {
					return;
				}
				// handlebars, dammit
				var combo = $("<select id='"+id+"' class='th_input filter_input'/>");
	
				var nullOption = new Option("", "");
				$(nullOption).html("");
	
				combo.append(nullOption);
	
				$.each(content, function(index, value) {
					var o = new Option(value, index);
					$(o).html(value);
					combo.append(o);
				});
	
				th.append(combo);
			}
	
			var tableId = table.attr("id");
			$( tableId + "_filter").hide();
	
			/*
			 * some of fields below can use some defaults values in case they were 
			 * not overriden in the conf 
			 */
			var users = initconf.basic.assignableUsers,
				statuses = initconf.messages.executionStatus,
				weights = initconf.basic.weights || _weights,	
				modes = initconf.basic.modes;
	
	
			table.find(".tp-th-project-name,.tp-th-reference,.tp-th-name,.tp-th-dataset,.tp-th-suite")
				 .append("<input class='th_input filter_input'/>");
	
	
	
			var execmodeTH = table.find("th.tp-th-exec-mode"),
				importanceTH = table.find(".tp-th-importance"),
				statusTH = table.find(".tp-th-status"),
				assigneeTH = table.find(".tp-th-assignee");
	
	
			_createCombo(execmodeTH, "#filter-mode-combo", modes);
			_createCombo(statusTH, "#filter-status-combo", statuses);
			_createCombo(assigneeTH, "#filter-user-combo", users);
			_createCombo(importanceTH, "#filter-weight-combo", weights);
	
			// use handlebars, dammit !
			table.find(".tp-th-exec-on").append("<div class='rangedatepicker th_input'>"
									+ "<input class='rangedatepicker-input' readonly='readonly'/>"
									+ "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
									+ "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
									+ "</div>");
	
	
			$(".th_input").click(function(event) {
				event.stopPropagation();
			}).keypress(function(event){
				if (event.which == 13 )
				{
					event.stopPropagation();
					event.preventDefault();
					event.target.blur();
					event.target.focus();
				}
			});
	
			table.find("th").hover(function(event) {
				event.stopPropagation();
			});
	
			$(".filter_input").change(function() {
				var sTable = table.squashTable(),
					settings = sTable.fnSettings(),
					api = settings.oApi,
					headers = table.find("th");
	
				var visiIndex =  headers.index($(this).parents("th:first")),
					realIndex = api._fnVisibleToColumnIndex( settings, visiIndex );
	
				sTable.fnFilter(this.value, realIndex);
			});
	
			rangedatepicker.init();
	
			hideFilterFields(true);
		}
	}

	return {
		newInst : function(){
			return new FilterMode();
		}
	}

});