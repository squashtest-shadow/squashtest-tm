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
require([ "common" ], function() {
	"use strict";

	require([ "jquery", "underscore", "./app/pubsub", "squash.basicwidgets", "contextual-content-handlers",
			"jquery.squash.fragmenttabs", "bugtracker/bugtracker-panel", "workspace.event-bus", "iteration-management",
			"app/ws/squashtm.workspace" ], function($, _, ps, basicwidg, contentHandlers, Frag, bugtracker, eventBus,
			itermanagement, WS) {


		// *********** event handler ***************

		/* renaming success handler */
		var renameIterationSuccess = function(data) {
			squashtm.workspace.eventBus.trigger("node.rename", {
				identity : squashtm.page.identity,
				newName : data.newName
			});
		};

		squashtm = _.extend({}, squashtm);
		squashtm.handlers = _.extend({
			renameIterationSuccess : renameIterationSuccess
		}, squashtm.handlers);

		var refreshTestPlan = _.bind(function() {
			console && console.log && console.log("squashtm.execution.refresh");
			$("#iteration-test-plans-table").squashTable().refresh();
		}, window);

		squashtm.execution = _.extend({
			refresh : refreshTestPlan
		}, squashtm.execution);

		console.log(document.eventsQueue);

		// this is executed on each fragment load
		ps.subscribe("refresh.iteration", function() {
			var config = _.extend({}, squashtm.page);

			config = _.defaults(config, {
				isFullPage : false,
				hasBugtracker : false,
				hasFields : false
			});

			if (config.isFullPage) {
				WS.init();
			}

			basicwidg.init();

			var nameHandler = contentHandlers.getSimpleNameHandler();
			nameHandler.identity = squashtm.page.identity;
			nameHandler.nameDisplay = "#iteration-name";

			// todo : uniform the event handling.
			// rem : does it mean yet another half-assed refactoring ?
			itermanagement.initEvents();

			// ****** tabs configuration *******

			var fragConf = {
					active : 2,
					cookie : "iteration-tab-cookie",
					activate : function(event, ui) {
						if (ui.newPanel.is("#dashboard-iteration")) {
							eventBus.trigger("dashboard.appear");
						}
					}
			};
			Frag.init(fragConf);

			if (config.hasBugtracker) {
				bugtracker.load(config.bugtracker);
			}

			if (config.hasFields) {
				$("#iteration-custom-fields").load(config.customFields.url);
			}

			// ********** dashboard **************
			itermanagement.initDashboardPanel({
				master : "#dashboard-master",
				cacheKey : "it" + config.identity.resid
			});

			// ********** rename popup ***********
			$("#rename-iteration-dialog").bind("dialogopen", function(event, ui) {
				var name = $.trim($("#iteration-name").text());
				$("#rename-iteration-name").val(name);
			});

			console && console.log && console.log("iteration-page refresh.iteration");
		});
		console && console.log && console.log("iteration-page.js loaded");
	});
});
