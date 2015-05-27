/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
define([ "jquery", "squash.configmanager", "squash.translator", "moment",
		"datatables" ], function($, confman, translator, moment) {
	/*
	 * This module define a correct comparator for sorting date in squashtable where sorting is done client side
	 * The column should be tagged with a sType attribute in data-def, in the jsp file.
	 * sType = squashdateShort for short date
	 * sType = squashdateLong for long date
	 * Look in show-milestones.jsp for reference
	 * */

	//get the date format for moment.js and for the user's locale in message file
	var shortDateFormat = translator.get("squashtm.dateformatShort.js.moment");
	var longDateFormat = translator.get("squashtm.dateformat.js.moment");

	function formatDateForSort(dateString,dateFormat) {
		return moment(dateString, dateFormat);
	}

	squashdateShortAsc = function(dateStringA, dateStringB) {
		var dateA = formatDateForSort(dateStringA,shortDateFormat);
		var dateB = formatDateForSort(dateStringB,shortDateFormat);
		return moment(dateA).isAfter(dateB);
	};

	squashdateShortDesc = function(dateStringA, dateStringB) {
		var dateA = formatDateForSort(dateStringA,shortDateFormat);
		var dateB = formatDateForSort(dateStringB,shortDateFormat);
		return moment(dateA).isBefore(dateB);
	};
	
	squashdateLongAsc = function(dateStringA, dateStringB) {
		var dateA = formatDateForSort(dateStringA,longDateFormat);
		var dateB = formatDateForSort(dateStringB,longDateFormat);
		return moment(dateA).isAfter(dateB);
	};
	
	squashdateLongDesc = function(dateStringA, dateStringB) {
		var dateA = formatDateForSort(dateStringA,longDateFormat);
		var dateB = formatDateForSort(dateStringB,longDateFormat);
		return moment(dateA).isBefore(dateB);
	};

	//define the sort function in the squashtable object for the sType squashdateShort
	//naming convention is nameofstype-asc for ascending sort
	jQuery.fn.dataTableExt.oSort['squashdateShort-asc'] = squashdateShortAsc;
	jQuery.fn.dataTableExt.oSort['squashdateShort-desc'] = squashdateShortDesc;
	
	//define the sort function in the squashtable object for the sType squashdateLong
	jQuery.fn.dataTableExt.oSort['squashdateLong-asc'] = squashdateLongAsc;
	jQuery.fn.dataTableExt.oSort['squashdateLong-desc'] = squashdateLongDesc;

});