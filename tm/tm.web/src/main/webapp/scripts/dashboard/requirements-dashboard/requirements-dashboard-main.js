/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

define([ "require", "dashboard/basic-objects/model", "dashboard/basic-objects/timestamp-label",
		"dashboard/SuperMasterView","./summary", "./bound-test-cases-pie", "./status-pie", "./criticality-pie",
		"./bound-description-pie", "squash.translator" ], function(require, StatModel, Timestamp, SuperMasterView, Summary,
		BoundTestCasePie, StatusPie, CriticalityPie, BoundDescriptionPie, translator) {

	function doInit(settings) {
		new SuperMasterView({
			el : "#dashboard-master",
			modelSettings : settings,
			initCharts : initCharts
		});
	}

	function initCharts() {
		
		var tcPie = new BoundTestCasePie({
			el : this.$("#dashboard-item-bound-tcs"),
			model : this.model
		});
		
		var statPie = new StatusPie({
			el : this.$("#dashboard-item-requirements-status"),
			model : this.model
		});
		
		var critPie = new CriticalityPie({
			el : this.$("#dashboard-item-requirements-criticality"),
			model : this.model
		});
		
		var descPie = new BoundDescriptionPie({
			el : this.$("#dashboard-item-bound-desc"),
			model : this.model
		});
		
		var summary = new Summary({
			el : this.$(".dashboard-summary"),
			model : this.model
		});

		addClickSearchEvent($("#dashboard-item-bound-tcs"), tcPie, "testcase");
		addClickSearchEvent($("#dashboard-item-requirements-status"), statPie, "status");
		addClickSearchEvent($("#dashboard-item-requirements-criticality"), critPie, "criticality");
		addClickSearchEvent($("#dashboard-item-bound-desc"), critPie, "description");
		
		return [ summary, tcPie, statPie, critPie, descPie ];
	}
	
	function addTestCasesToSearch(search, pointIndex) {
		search.fields.testcases = {};
		search.fields.testcases.type = "RANGE";
		switch (pointIndex) {
		case 0:
			search.fields.testcases.minValue = "0";
			search.fields.testcases.maxValue = "0";
			break;
		case 1:
			search.fields.testcases.minValue = "1";
			search.fields.testcases.maxValue = "1";
			break;

		case 2:
			search.fields.testcases.minValue = "2";
			search.fields.testcases.maxValue = "";
			break;
		}
	}
	
	function addStatusToSearch(search, pointIndex) {
		search.fields.status = {};
		search.fields.status.type = "LIST";
		switch (pointIndex) {
		case 0:
			search.fields.status.values = [ "1-WORK_IN_PROGRESS" ];
			break;
		case 1:
			search.fields.status.values = [ "2-UNDER_REVIEW" ];
			break;
		case 2:
			search.fields.status.values = [ "3-APPROVED" ];
			break;
		case 3:
			search.fields.status.values = [ "4-OBSOLETE" ];
			break;
		}
	}
	
	function addCriticalityToSearch(search, pointIndex) {
		search.fields.criticality = {};
		search.fields.criticality.type = "LIST";
		switch (pointIndex) {
		case 0:
			search.fields.criticality.values = [ "3-UNDEFINED" ];
			break;
		case 1:
			search.fields.criticality.values = [ "2-MINOR" ];
			break;
		case 2:
			search.fields.criticality.values = [ "1-MAJOR" ];
			break;
		case 3:
			search.fields.criticality.values = [ "0-CRITICAL" ];
			break;
		}
	}
	
	function addDescriptionToSearch(search, pointIndex) {
		search.fields.hasDescription = {};
		search.fields.hasDescription.type = "RANGE";
		switch (pointIndex) {
		case 0:
			search.fields.hasDescription.minValue = "";
			search.fields.hasDescription.maxValue = "0";
			break;
		case 1:
			search.fields.hasDescription.minValue = "1";
			search.fields.hasDescription.maxValue = "";
			break;
		}
	}
	
	function addClickSearchEvent(item, pie, type) {

		item.bind('jqplotDataHighlight', function(ev, seriesIndex, pointIndex, data) {
			var $this = $(this);
			// TODO: The message shouldn't be associated with 'test-cases'
			$this.attr('title', translator.get("dashboard.test-cases.search"));
			//add pointer because IE don't support  zoom-in. Put pointer before zoom-in, so zoom-in is used if the brower support it
			$this.css('cursor', 'pointer');
			$this.css('cursor', 'zoom-in');

		});

		item.bind('jqplotDataUnhighlight', function(ev, seriesIndex, pointIndex, data) {
			var $this = $(this);
			$this.attr('title', "");
			$this.css('cursor', 'auto');
		});

		item.bind('jqplotDataClick', function(ev, seriesIndex, pointIndex, data) {
			var ids = pie.model.get('selectedIds');

			var search = {
				fields : {
					id : {
						type : "LIST",
						values : ""
					}
				}
			};

			search.fields.id.values = ids.toString().split(",");

			switch (type) {
			case "testcase":
				addTestCasesToSearch(search, pointIndex);
				break;
			case "status":
				addStatusToSearch(search, pointIndex);
				break;
			case "criticality":
				addCriticalityToSearch(search, pointIndex);
				break;
			case "description":
				addDescriptionToSearch(search, pointIndex);
				break;
			}

			var queryString = "searchModel=" + encodeURIComponent(JSON.stringify(search));
			document.location.href = squashtm.app.contextRoot + "/advanced-search/results?requirement&" + queryString;

		});
	}

	return {
		init : function(settings) {
			doInit(settings);
		}
	};

});