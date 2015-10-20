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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView", "tree"],
	function($, backbone, _, Handlebars, AbstractStepView, tree) {
	"use strict";

	var scopeStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#scope-step-tpl";
			this.model = data;
			data.nextStep = "filter";
			data.prevStep = "entity";
			this._initialize(data, wizrouter);
			
			var nodes = _.map(this.model.get("scope"), function(obj) {
				return {
					restype:obj.type.split("_").join("-").toLowerCase() + "s", //yeah that quite fucked up...change back the _ to -, lower case and add a "s" 
					resid:obj.id};});
			
			
			
			$("#tree").on('reselect.jstree', function(event, data) {
               data.inst.findNodes(nodes).select();
			});	
			
		
			
			this.initTree();
		
		},
		
		updateModel : function() {
			
			var scope = _.map($("#tree").jstree('get_selected'), function (sel) { return {type : $(sel).attr("restype").split("-").join("_").slice(0,-1).toUpperCase(), id:$(sel).attr("resid")}; } );
			
			this.model.set({scope : scope});
		},
		
		
		initTree : function (){
		

			var workspaceName;

			var ids = _.pluck(this.model.get("scope"), "id");
			ids = ids.length > 0 ? ids : 0;
			
			switch (this.model.get("selectedEntity")){
			
			case "REQUIREMENT": 
			case "REQUIREMENT_VERSION":  workspaceName = "requirement";
				break;
				
			case "TEST_CASE" : workspaceName = "test-case";
				break;
				
			case "CAMPAIGN" :
			case "ITERATION" :
			case "EXECUTION" :
			case "ITEM_TEST_PLAN" : workspaceName = "campaign";
			
			}
			
			$.ajax({
				url : squashtm.app.contextRoot + "/" + workspaceName + '-workspace/tree/' + ids,
				datatype : 'json' 
				
				
			}).done(function(model){
				
				var treeConfig = {
						model : model,
						treeselector: "#tree",
						workspace:"campaign",	
						selectedNode :''
				};
				tree.initLinkableTree(treeConfig);
					
			});

		}
	});

	return scopeStepView;

});