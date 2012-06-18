/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
var squashtm = squashtm || {};
/**
 * Controller for the report panel
 * 
 * depends on : 
 * jquery
 * jquery ui
 * jquery.jeditable.js
 * jquery.jeditable.datepicker.js
 * 
 * @author Gregory Fouquet
 */
squashtm.report = (function ($) {
	var formState = {};

	function onSingleOptionChanged() {
		var option = this;
		var name = option.name;

		formState[name] = {
			value : option.value,
			selected : option.checked
		};
	}

	function onGroupedOptionChanged() {
		var option = this;
		var name = option.name;
		var value = option.value;

		var groupState = formState[name] || [];
		formState[name] = groupState;

		var res = groupState.filter(function (item) {
			return item.value === value;
		});

		if (res[0]) {
			res[0].selected = this.checked;
		} else {
			groupState.push({
				value : value,
				selected : option.checked
			});
		}
	}

	function onListItemSelected() {
		var dropdown = this;
		
		var state = $(dropdown.options).map(function (item) {
			return { value: item.value, selected: item.selected  };
		}); 
		
		formState[dropdown.name] = state;
	}
	
	function onTextBlurred() {
		formState[this.name] = this.value;
	}

	function onDatepickerChanged(value) {
		formState[this.id] = value;
		console.log(this.id);
		console.log(formState[this.id]);
	}

	function init() {
		var panel = $("#report-criteria-panel");
		var checkboxes = panel.find("input:checkbox");
		
		var groupedCheckboxes = checkboxes.filter(function (item) {
			return $(item).data('grouped');
		});
		groupedCheckboxes.change(onGroupedOptionChanged);
		groupedCheckboxes.change();
		
		var radios = panel.find("input:radio");
		radios.change(onGroupedOptionChanged);
		radios.change();
		
		var singleCheckboxes = checkboxes.filter(function (item) {
			return !$(item).data('grouped');
		});
		singleCheckboxes.change(onSingleOptionChanged);
		singleCheckboxes.change();
		
		var dropdowns = panel.find('select');
		dropdowns.change(onListItemSelected);
		dropdowns.change();
		
		var texts = panel.find("input:text");
		texts.blur(onTextBlurred);
		texts.blur();
		
		var datepickers = panel.find(".date-crit");
		datepickers.editable(function (value, settings) {
			var self = this;
			onDatepickerChanged.apply(self, [value]);
			
			return value;
		}, {
	        type      : 'datepicker',
	        tooltip   : "Click to edit..."
		});
		// initialiser la date !
		

		$('#generate').click(function () {
			console.log(formState);
		});
	}

	return {
		init : init
	};
})(jQuery);
