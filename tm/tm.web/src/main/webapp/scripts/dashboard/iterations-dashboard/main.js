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
define([ "require", "iesupport/am-I-ie8", "./nonexecuted-testcase-importance-pie", "./testcase-status-pie",
		"./success-rate-view", "./test-inventory-table", "dashboard/SuperMasterView" ], function(require, isIE8,
		ImportancePie, StatusPie, SuccessRateDonut, InventoryTable, SuperMasterView) {

	var dependencies = ["squash.attributeparser"];

	if (isIE8) {
		dependencies.push("excanvas");
	}

	function doInit(settings) {
		new SuperMasterView({
			el : "#dashboard-master",
			settings : settings, 
			initCharts : initCharts
		});
	}

	function initCharts() {
		return [

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

	return {
		init : function(settings) {
			require(dependencies, function(attrparser, IterDashboardView) {
				doInit(settings);
			});
		}
	};

});