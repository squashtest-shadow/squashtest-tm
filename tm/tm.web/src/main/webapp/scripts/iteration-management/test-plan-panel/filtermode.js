/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

define(["jquery",  "jquery.squash.rangedatepicker" ], function($, rangedatepicker){

	function _hideFilterFields(_bNoredraw) {
		var table = $("#iteration-test-plans-table").squashTable(),
			settings = table.fnSettings();
		table.find(".th_input").hide();
		
		var inputs = table.find('.filter_input');
		inputs.each(function(index){
			settings.aoPreSearchCols[index].sSearch = '';
		});
		
		if ( _bNoredraw !== true){
			table.refresh();
		}
	}

	function _showFilterFields() {
		var table = $("#iteration-test-plans-table").squashTable(),
			settings = table.fnSettings();
		
		table.find(".th_input").show();
		
		var inputs = table.find('.filter_input');
		inputs.each(function(index){
			settings.aoPreSearchCols[index].sSearch = this.value;
		});
		
		table.refresh();
	}
	


	function _initializeFilterFields(initconf) {
		
		function _populateCombo(id, content){
			var nullOption = new Option("", "");
			$(nullOption).html("");
			var combo = $(id);
			
			combo.append(nullOption);
			
			$.each(content, function(index, value) {
				var o = new Option(value, index);
				$(o).html(value);
				combo.append(o);
			});			
		}

		var users = initconf.basic.assignableUsers;
		var statuses = initconf.messages.executionStatus;
		var weights = initconf.basic.weights;
		var modes = initconf.basic.modes;
		var table = $("#iteration-test-plans-table");
		
		
		table.find(".tp-th-project-name").append("<input class='th_input filter_input'/>");		
		var execTH = table.find("th.tp-th-exec-mode");
		if(execTH.length>0){
			execTH.append("<select id='filter-mode-combo' class='th_input filter_input'/>");
		}
		
		table.find('.tp-th-reference,.tp-th-name,.tp-th-dataset,.tp-th-suite')
			 .append("<input class='th_input filter_input'/>");
		
		table.find('.tp-th-importance').append("<select id='filter-weight-combo' class='th_input filter_input'/>");
		table.find('.tp-th-status').append("<select id='filter-status-combo' class='th_input filter_input'/>");
		table.find('.tp-th-assignee').append("<select id='filter-user-combo' class='th_input filter_input'/>");
		
		table.find('.tp-th-exec-on').append("<div class='rangedatepicker th_input'>"
								+ "<input class='rangedatepicker-input' readonly='readonly'/>"
								+ "<div class='rangedatepicker-div' style='position:absolute;top:auto;left:auto;z-index:1;'></div>"
								+ "<input type='hidden' class='rangedatepicker-hidden-input filter_input'/>"
								+ "</div>");


		$("#iteration-test-plans-table_filter").hide();

		
		_populateCombo("#filter-status-combo", statuses);

		_populateCombo("#filter-user-combo", users);

		_populateCombo("#filter-weight-combo", weights);

		
		
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
			table.squashTable()
				.fnFilter(this.value, $(".filter_input").index(this));
		});

		rangedatepicker.init();
		
		_hideFilterFields(true);
	}
	
	return {
		initializeFilterFields : _initializeFilterFields,
		hideFilterFields : _hideFilterFields,
		showFilterFields : _showFilterFields
	}

});