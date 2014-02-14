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
 *	settings : {
 *		master : a css selector that identifies the whole section that need initialization,
 *		workspace : one of "test-case", "campaign", "requirement" (can be read from dom)
 *		rendering : one of "toggle-panel", "plain". This is a hint that tells how to render the dashboard container (can be read from dom),
 *		model : a javascript object, workspace-dependent, containing the data that will be plotted (optional, may be undefined)
 *		cacheKey : if defined, will use the model cache using the specified key.
 *		listeTree : if true, the model will listen to tree selection.
 *	}
 * 
 */

define([ "require", "iesupport/am-I-ie8", "dashboard/basic-objects/model", 
		"dashboard/basic-objects/timestamp-label", "dashboard/SuperMasterView", "./summary", "./bound-requirements-pie",
		"./status-pie", "./importance-pie", "./size-pie" ], function(require, isIE8, StatModel, Timestamp,
		SuperMasterView, Summary, BoundReqPie, StatusPie, ImportancePie, SizePie) {

	var dependencies = [ "squash.attributeparser" ];

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
		return [ new Summary({
			el : this.$(".dashboard-summary"),
			model : this.model
		}),

		new BoundReqPie({
			el : this.$("#dashboard-item-bound-reqs"),
			model : this.model
		}),

		new StatusPie({
			el : this.$("#dashboard-item-test-case-status"),
			model : this.model
		}),

		new ImportancePie({
			el : this.$("#dashboard-item-test-case-importance"),
			model : this.model
		}),

		new SizePie({
			el : this.$("#dashboard-item-test-case-size"),
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