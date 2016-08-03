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
		"dashboard/SuperMasterView",/*"./summary",*/ "./bound-test-cases-pie",/* "./status-pie",*/ /*"./importance-pie",*/
		/*"./size-pie",*/ "squash.translator" ], function(require, StatModel, Timestamp, SuperMasterView, /*Summary,*/
		BoundTestCasePie, /*StatusPie,*/ /*ImportancePie,*/ /*SizePie,*/ translator) {

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

		addClickSearchEvent($("#dashboard-item-bound-tcs"), tcPie, "testcase");
		/* adds des autres graphiques */
		
		return [ tcPie /*+Autres Graphiques*/ ];
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
	
	/* Les autres functions pour les autres graphiques */
	
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
			/* Cases des autres graphiques */
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