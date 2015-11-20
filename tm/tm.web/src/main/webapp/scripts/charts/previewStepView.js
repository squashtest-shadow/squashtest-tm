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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView", "workspace.routing", "custom-report-workspace/charts/chartFactory"],
	function($, backbone, _, Handlebars, AbstractStepView, router, chart) {
	"use strict";

	var previewStepView = AbstractStepView.extend({

		initialize : function(data, wizrouter) {
			this.tmpl = "#preview-step-tpl";
			this.model = data;
			data.name = "preview";
			this._initialize(data, wizrouter);
			this.initChart();
			this.initName();

		},


		initName : function (){
			var name = this.model.get('name') || "graph" ;
			 $("#chart-name").val(name);
		},
		initChart : function (){
			var data = this.model.get("chartData");
			chart.buildChart("#chart-display-area", data);

		},
		save : function () {
			var parentId = this.model.get("parentId");
			this.updateModel();
			
			var targetUrl;
			
			if (this.model.get("chartDef") === null){
				targetUrl = router.buildURL("chart.new", parentId);
			} else {
				targetUrl = router.buildURL("chart.update", parentId);
			}
			
			$.ajax({
				method : "POST",
				contentType: "application/json",
				url : targetUrl,
				data : this.model.toJson()

			}).done(function(url){
				 window.location.href = squashtm.app.contextRoot + url;
			});	
			

		},
		

		updateModel : function() {

		    var name = $("#chart-name").val();
		    this.model.set({name : name });
		}

	});

	return previewStepView;

});
