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


/*
 * Configuration : 
 * 
 * {
 * data : {
 *  campaignId : the campaign id,
 *  campaignUrl : the campaignUrl,
 *  bugtrackerUrl : the url to access the bugtracker for that entity,
 *  testCaseManagerUrl : the url to access the test case manager,
 *  cufValuesUrl : the url where to load the custom fields 
 * }
 * 
 * features :{
 *  isWritable : boolean, says if the campaign can be modified,
 *  hasBugtracker : boolean, says if the campaign can access the bugtracker,
 *  hasCUF : boolean, says if the campaign has CUFs to load 
 * }
 * }
 */

define([ 
        "./core/main", './test-plan-panel/main', "dashboard/campaigns-dashboard/main", "./planning/main"], 
		function(core, testPlanPanel, dashboard, planning) {
	
	return {
		
		// this one should init all, but you still can invoke the methods below
		init : function(conf){
			
			var reconf = $.extend(true, {}, conf, {
				data : {
					identity : {
						resid : conf.data.campaignId,
						restype : "campaigns"
					}
				}
			});
			
			core.init(reconf);
		},		
		
		initTestPlanPanel : function(conf){
			testPlanPanel.init(conf);
		},
		
		initDashboardPanel : function(conf){
			dashboard.init(conf);
		}
	};
});