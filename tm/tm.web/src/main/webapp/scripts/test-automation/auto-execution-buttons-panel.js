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
/**
 * Controller for <code>execute-auto-button.tag</code>.
 *
 * <code>execute-auto-button.tag</code> issues a "reload.auto-exec-btns-panel" event through pubsub.
 * This modules initializes the buttons when the event is triggered. Client should only require this module.
 */
define([ "jquery", "squash.translator", "jquery.squash.buttonmenu" ], function($,
		autosuitedialog, messages) {
	// init message cache
	messages.load({
		errorDlg : {
			title : "popup.title.error",
			message : "message.EmptyTableSelection"
		},
		doneDlg : {
			title : "popup.title.Info",
			message : "dialog.execution.auto.overview.error.none"
		}
	});

	function executeAll() {
		console.log("execute all automated tests");
		var ids = [];
		executeAuto(ids);
	}

	function executeSelection() {
		var ids = $(".test-plan-table").squashTable().getSelectedIds();
		if (ids.length === 0) {
			$.squash.openMessage(message.get("popup.title.error"), message.get("message.EmptyTableSelection"));
		} else {
			executeAuto(ids);
		}
	}

	function executeAuto(ids) {
		var runUrl = $("#auto-exec-btns-panel").data("run-url");

		$.ajax({
			type : "POST",
			url : runUrl,
			dataType : "json",
			data : {
				"id" : "execute-auto",
				"testPlanItemsIds" : ids
			}
		}).done(function(suiteView) {
			if (suiteView.executions.length === 0) {
				$.squash.openMessage(messages.get("popup.title.Info"), messages.get("dialog.execution.auto.overview.error.none"));
			} else {
				/*
				 * I'm cheating here, I should write
				 *
				 * require(["test-automation/automated-suite-overview"], function(auto){
				 * auto.get().watch(suiteView); })
				 */
				squashtm.context.autosuiteOverview.watch(suiteView);
			}
		});
	}

	ps.subscribe("reload.auto-exec-btns-panel", function() {
		console.log("refreshing auto-exec-btns-panel");
		$("#execute-auto-button").buttonmenu();
		$("#execute-auto-execute-all").on("click", executeAll);
		$("#execute-auto-execute-selection").on("click", executeSelection);
	});
});