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
define([ "require", "./campaign-progression-view", "./test-inventory-table",
		"./nonexecuted-testcase-importance-pie", "./testcase-status-pie", "./success-rate-view", "../SuperMasterView" ],
		function(require, ProgressionPlot, InventoryTable, ImportancePie, StatusPie, SuccessRateDonut,
				SuperMasterView) {

			function initCharts() {
				
				 var res = [

				new ImportancePie({
					el : this.$("#dashboard-nonexecuted-testcase-importance"),
					model : this.model
				}),

				new StatusPie({
					el : this.$("#dashboard-testcase-status"),
					model : this.model
				}),

				new SuccessRateDonut({
					el : this.$("#dashboard-success-rate"),
					model : this.model
				}),

				new InventoryTable({
					el : this.$("#dashboard-test-inventory"),
					model : this.model
				}) ];
				 
				 if ( this.$("#dashboard-cumulative-progression").length > 0 ) {
					 res.push(
							 new ProgressionPlot({
									el : this.$("#dashboard-cumulative-progression"),
									model : this.model
								}));
				 }
				 
				 return res;
			}

			function doInit(settings) {
				new SuperMasterView({
					el : "#dashboard-master",
					modelSettings : settings,
					initCharts : initCharts
				});
			}

			return {
				init : function(settings) {
						doInit(settings);
				}
			};

		});