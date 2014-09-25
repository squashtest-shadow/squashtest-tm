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
require([ "common" ], function(common) {
	require([ "jquery", "search/test-case-search-result", "app/ws/squashtm.workspace", "domReady" ], function($,
			TestCaseSearchResultView, WS, domReady) {
		var goBack = function() {
			var associationType = $("#associationType").text();
			var associationId = $("#associationId").text();
			if(!!associationType){
				if("requirement" === associationType){
					document.location.href = squashtm.app.contextRoot + "requirement-versions/"+associationId+"/verifying-test-cases/manager";
				} else if("campaign" === associationType){
					document.location.href = squashtm.app.contextRoot + "campaigns/"+associationId+"/test-plan/manager";
				} else if("iteration" === associationType){
					document.location.href = squashtm.app.contextRoot + "iterations/"+associationId+"/test-plan-manager";
				} else if("testsuite" === associationType){
					document.location.href = squashtm.app.contextRoot + "test-suites/"+associationId+"/test-plan-manager";
				} 
			} else {
				document.location.href = squashtm.app.contextRoot + "test-case-workspace/";
			}
		};

		domReady(function() {
			WS.init();
			var view = new TestCaseSearchResultView();
			//$("#back").on("click", goBack);
		});

	});
});