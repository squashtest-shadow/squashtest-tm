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
define([ "require", "iesupport/am-I-ie8", "./campaign-progression-view", "./test-inventory-table",
		"./nonexecuted-testcase-importance-pie", "./testcase-status-pie", "./success-rate-view", "../SuperMasterView" ],
		function(require, isIE8, ProgressionPlot, InventoryTable, ImportancePie, StatusPie, SuccessRateDonut,
				SuperMasterView) {

			function initCharts() {
				return [ new ProgressionPlot({
					el : this.$("#dashboard-cumulative-progression"),
					model : this.model
				}),

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
			}

			function doInit(settings) {
				new SuperMasterView({
					el : "#dashboard-master",
					settings : settings,
					initCharts : initCharts
				});
			}

			return {
				init : function(settings) {
					if (isIE8) {
						require([ "excanvas" ], function() {
							doInit(settings);
						});
					} else {
						doInit(settings);
					}
				}
			};

		});