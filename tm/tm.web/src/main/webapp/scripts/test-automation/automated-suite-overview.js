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
 * This module listens to the "reload.auto-suite-overview-popup" event and self initializes accordingly.
 *
 * Module api :
 * get() : returns the dialog widget instance.
 *
 */
define(["jquery", "handlebars", "../app/pubsub", "squash.statusfactory", "squash.translator",
        "jqueryui", "jquery.squash.formdialog"], function($, Handlebars, ps, statusfactory, translator) {
	"use strict";

	Handlebars.registerHelper("ballsyStatus", $.proxy(statusfactory.getHtmlFor, statusfactory));
	Handlebars.registerHelper("oddity", function(index) { return (index % 2 === 0) ? "even" : "odd"; });

	if ($.squash.autosuiteOverview === undefined) {

		$.widget("squash.autosuiteOverview", $.squash.formDialog, {

			// remember that some options are passed using dom conf, done in the super constructor
			options : {
				suite : null,
				intervalId : null
			},

			_create : function(){
				this._super();

				var self = this;

//				$(this.element).find("table").dataTable({
//					"oLanguage" : {
//						"sUrl" : squashtm.app.cfTable.languageUrl
//					},
//					"bAutoWidth" : false,
//					"bJQueryUI" : true,
//					"bFilter" : false,
//					"bPaginate" : false,
//					"bServerSide" : false,
//					"bDeferRender" : true,
//					"bRetrieve" : false,
//					"bSort" : false,
//					"aaSorting" : []
//				});

				this.options.executionRowTemplate = Handlebars.compile($("#exec-info-tpl").html());
				this.options.executionAutoInfos = $("#executions-auto-infos");

				// progressbar
				var executionProgressBar =  $("#execution-auto-progress-bar");
				executionProgressBar.progressbar({value : 0});
				executionProgressBar.find("div").addClass("ui-state-default");

				// events
				this.onOwnBtn("mainclose", function(){
					self.close();
				});

				this.onOwnBtn("warningok", function(){
					self.close();
				});

				this.onOwnBtn("warningcancel", function(){
					self.unclose();
				});
			},

			close : function(){

				// check : if the suite wasn't finished, we must let the user confirm
				var suite = this.options.suite;
				if (!! suite && suite.percentage < 100 && this.getState() !== "warning"){
					this.setState("warning");
					return false;
				}
				// else we actually close that dialog
				else{
					this._super();
					if (!! this.options.intervalId ){
						clearInterval(this.options.intervalId);
					}
					this._cleancontent();
				}

			},

			unclose : function (){
				this.setState("main");
			},


			_cleancontent : function(){

				var opts = this.options;

				clearInterval(opts.intervalId),
				opts.executionAutoInfos.empty();

				$("#execution-auto-progress-bar").progressbar("value", 0);
				$("#execution-auto-progress-amount").text(0 + "/" + 0);

				var table = $("table.test-plan-table");
				if (table.length > 0) {
					table.squashTable().refresh();
				}
				// TODO : replace the following function that doesn't exist anymore with an event published on the event bus
				//refreshStatistics();

			},

			watch : function(suite){
				this.options.suite = suite;
				this._initview();
				this.setState("main");
				this.open();

				var self = this;

				if (suite.percentage < 100) {
					this.options.intervalId = setInterval(function() {
						self.update();
					}, 5000);
				}
			},

			update : function(){
				var self = this,
					opts = this.options;

				$.ajax({
					type : "GET",
					url : opts.url + "/" +opts.suite.suiteId + "/executions",
					dataType : "json"
				}).done(function(json){
					self.options.suite = json;
					self._updateview();
					if (json.percentage == 100) {
						clearInterval(self.options.intervalId);
					}
				});
			},

			_initview : function(){
				var data = this.options.suite;
				var executions = data.executions,
					progress = data.percentage;

				// the progressbar
				var executionTerminated = progress / 100 * executions.length;
				$("#execution-auto-progress-bar").progressbar("value", progress);
				$("#execution-auto-progress-amount").text(executionTerminated + "/" + executions.length);

				// the "table"
				var htmlRows = this.options.executionRowTemplate({execs : executions});
				this.options.executionAutoInfos.html(htmlRows);
			},

			_updateview : function(){
				this._initview();
			}
		});

	}

	function init() {
		if (squashtm.context === undefined || squashtm.context.autosuiteOverview === undefined){
			squashtm.context = squashtm.context || {};

			var dialog = $("#execute-auto-dialog");
			dialog.autosuiteOverview();

			// note that we are storing the widget itself
			squashtm.context.autosuiteOverview = dialog.data("autosuiteOverview");
		}
	}

	ps.subscribe("reload.auto-suite-overview-popup", function() {
		init();
	});

	var module = {
			// should not be useful anymore
		//init : init,

		get : function(){
			if (squashtm.context === undefined || squashtm.context.autosuiteOverview === undefined){
				this.init();
			}
			return squashtm.context.autosuiteOverview;
		}
	};

	return module;
});

