/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
define([ "jquery", "backbone", "underscore"], function($, Backbone, _) {


	var IndexAdministrationView = Backbone.View.extend({

		el : "#index-administration-content",

		initialize : function() {

		},

		events : {
			"click #index-all-button" : "indexAll",
			"click #requirement-index-button" : "indexRequirements",
			"click #testcase-index-button" : "indexTestcases",
			"click #campaign-index-button" : "indexCampaigns",
			"click #refresh-index-button" : "refreshPage"
		},
		
		indexAll : function(){
			$.ajax({
				  type: "POST",
				  url: squashtm.app.contextRoot + "advanced-search/index-all",
				  data: "nodata"
			});
		},
		
		indexRequirements : function(){
			$.ajax({
				  type: "POST",
				  url: squashtm.app.contextRoot + "advanced-search/index-requirements",
				  data: "nodata"
			});
		},

		indexTestcases : function(){
			$.ajax({
				  type: "POST",
				  url: squashtm.app.contextRoot + "advanced-search/index-testcases",
				  data: "nodata"
			});
		},
		
		indexCampaigns : function(){
			$.ajax({
				  type: "POST",
				  url: squashtm.app.contextRoot + "advanced-search/index-campaigns",
				  data: "nodata"
			});
		},
		
		refreshPage : function(){
			$.ajax({
				  type: "GET",
				  url: squashtm.app.contextRoot + "advanced-search/refresh",
				  data: "nodata"
			}).success(function(val){
				$("#monitor-percentage").html(val);
				$("#monitor-percentage").removeClass("not-displayed");
				$("#monitor-message").removeClass("not-displayed");
			});
		}
	});
	
	return IndexAdministrationView;
});