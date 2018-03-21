/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
require(["common"], function () {
	require(["app/squash.wreqr.init", "jquery","squash.translator", "custom-report-workspace/charts/chartFactory"],
		function (squashtm, $, translator, chart) {
			"use strict";
			var data = squashtm.statisticalAnalysis.data;

			var getFakeData1 = function () {
				return {"name":translator.get('chart.wizard.example.title'),
					"measures":[{"label":"","columnPrototype":{"label":"REQUIREMENT_ID","specializedEntityType":{"entityType":"REQUIREMENT","entityRole":null}},"operation":{"name":"COUNT"}}],
					"axes":[{"label":"","columnPrototype":{"label":"REQUIREMENT_VERSION_CREATED_ON","specializedEntityType":{"entityType":"REQUIREMENT_VERSION","entityRole":null},"dataType":"DATE"},"operation":{"name":"BY_MONTH"}}],
					"filters":[],
					"abscissa":[["201502"],["201503"],["201504"],["201505"],["201506"],["201507"],["201508"],["201509"],["201510"],["201511"]],
					"series":{"":[1,1,3,1,1,1,4,2,5,2]}};
			};

			$("#chart-display-area").html('');
			var fakeData = getFakeData1();

			fakeData.type = 'BAR';
			chart.buildChart("#chart-display-area", fakeData);



			console.log(data.statsHistory.length);

		});
});
