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
define(["jquery",  "jquery.squash.rangedatepicker" ], function($, rangedatepicker){
	"use strict";

	var tableSelector = ".test-plan-table";

	function _hideFilterFields(_bNoredraw) {
		var table = $(tableSelector).squashTable(),
			settings = table.fnSettings();
		table.find(".th_input").hide();

		var inputs = table.find(".filter_input");
		inputs.each(function(index){
			settings.aoPreSearchCols[index].sSearch = '';
		});

		if ( _bNoredraw !== true){
			table.refresh();
		}
	}

	function _showFilterFields() {
		var table = $(tableSelector).squashTable(),
			settings = table.fnSettings();

		table.find(".th_input").show();

		var inputs = table.find(".filter_input");
		inputs.each(function(index){
			settings.aoPreSearchCols[index].sSearch = this.value;
		});

		table.refresh();
	}



	function _initializeFilterFields(initconf) {

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


		var table = $(tableSelector);
		$(table.attr("id") + "_filter").hide();

		var users = initconf.basic.assignableUsers,
			statuses = initconf.messages.executionStatus,
			weights = initconf.basic.weights,
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

		_hideFilterFields(true);
	}

	return {
		initializeFilterFields : _initializeFilterFields,
		hideFilterFields : _hideFilterFields,
		showFilterFields : _showFilterFields
	};

});