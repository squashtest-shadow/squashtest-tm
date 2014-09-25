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
	require([ "jquery", "search/advanced-search-input", "app/ws/squashtm.workspace" ], function($,
			AdvancedSearchView, WS) {
		var goBack = function() {
			
			var searchDomain = $("#searchDomain").text();
			if("testcaseViaRequirement" === searchDomain){
				searchDomain = "testcase";
			}
			var associationType = $("#associationType").text();
			var associationId = $("#associationId").text();
			if(!!associationType){
				if("testcase" === searchDomain && "requirement" === associationType){
					document.location.href = squashtm.app.contextRoot + "requirement-versions/"+associationId+"/verifying-test-cases/manager";
				} else if("testcase" === searchDomain && "campaign" === associationType){
					document.location.href = squashtm.app.contextRoot + "campaigns/"+associationId+"/test-plan/manager";
				} else if("testcase" === searchDomain && "iteration" === associationType){
					document.location.href = squashtm.app.contextRoot + "iterations/"+associationId+"/test-plan-manager";
				} else if("testcase" === searchDomain && "testsuite" === associationType){
					document.location.href = squashtm.app.contextRoot + "test-suites/"+associationId+"/test-plan-manager";
				} else if("requirement" === searchDomain && "testcase" === associationType){
					document.location.href = squashtm.app.contextRoot + "test-cases/"+associationId+"/verified-requirement-versions/manager";
				} else if ("requirement" === searchDomain && "teststep" === associationType){
					document.location.href = squashtm.app.contextRoot + "test-steps/"+associationId+"/verified-requirement-versions/manager";
				}
			} else {
				if("testcase" === searchDomain){
					document.location.href = squashtm.app.contextRoot + "test-case-workspace/";
				} else if("requirement" === searchDomain){
					document.location.href = squashtm.app.contextRoot + "requirement-workspace/";
				}
			}
		};

		$(function() {
			WS.init();
			var view = new AdvancedSearchView();
			//$("#back").on("click", goBack);
		});

	});
});