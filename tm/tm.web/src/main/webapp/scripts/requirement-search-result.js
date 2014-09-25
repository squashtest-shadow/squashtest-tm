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
require([ "common" ], function(common) {
	require([ "jquery", "search/requirement-search-result", "app/ws/squashtm.workspace" ], function($,
			TestCaseSearchResultView, WS) {
		var goBack = function() {
			var associationType = $("#associationType").text();
			var associationId = $("#associationId").text();
			if(!!associationType){
				if("testcase" === associationType){
					document.location.href = squashtm.app.contextRoot + "test-cases/"+associationId+"/verified-requirement-versions/manager";
				}
				else if ("teststep" === associationType){
					document.location.href = squashtm.app.contextRoot + "test-steps/"+associationId+"/verified-requirement-versions/manager";
				}
			} else {
				document.location.href = squashtm.app.contextRoot + "requirement-workspace/";
			}
		};

		$(function() {
			WS.init();
			var view = new TestCaseSearchResultView();
			//$("#back").on("click", goBack);
		});

	});
});