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
define(["jquery", "jquery.squash" ], function($) {
	"use strict";

	function _dryRunStart(url){
		return $.ajax({
			url : url,
			method : "get",
			dataType : "json",
			data : {
				"dry-run" : ""
			}
		});
	}

	function _runInPopup(url){
		var data = {
			"optimized" : "false"
		};
		var winDef = {
			name : "classicExecutionRunner",
			features : "height=690, width=810, resizable, scrollbars, dialog, alwaysRaised"
		};
		$.open(url, data, winDef);
	}

	function _runInOER(url){

		$("body form#start-optimized-form").remove();
		$("body").append("<form id=\"start-optimized-form\" action=\""+url+"?optimized=true&suitemode=false\" method=\"post\" name=\"execute-test-case-form\" target=\"optimized-execution-runner\" class=\"not-displayed\"> <input type=\"submit\" value=\"true\" name=\"optimized\" id=\"start-optimized-button\" /><input type=\"button\" value=\"false\" name=\"suitemode\"  /></form>");

		$("#start-optimized-button").trigger("click");
	}

	return {

		runInPopup : function(url){
			_dryRunStart(url)
			.done(function(){
				_runInPopup(url);
			});
		},

		runInOER : function(url){
			_dryRunStart(url)
			.done(function(){
				_runInOER(url);
			});
		}
	};

});