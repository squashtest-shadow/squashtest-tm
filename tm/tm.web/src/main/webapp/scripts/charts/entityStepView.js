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
define(["jquery", "backbone", "handlebars", "./abstractStepView", "tree", "squash.translator", "./treePopup", "jquery.squash.confirmdialog", "jquery.squash.buttonmenu"],
	function($, backbone, Handlebars, AbstractStepView, tree, translator, TreePopup) {
	"use strict";

	var entityStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#entity-step-tpl";
			this.model = data;
			data.name = "entity";
			this._initialize(data, wizrouter);
			$("#change-perimeter-button").buttonmenu();
			var treePopup = $("#tree-popup-tpl").html();
			this.treePopupTemplate = Handlebars.compile(treePopup);
			this.initPerimeter();

			
		},
		
		events : {
			"click .perimeter-select" : "openPerimeterPopup",
			"click #repen-perim" : "reopenPerimeter",
			"click #reset-perimeter" : "resetPerimeter"
			
		},
		
		initPerimeter : function (){
			var scope = this.model.get("scopeEntity") || "default";
			if ( scope == "default"){
				this.writeDefaultPerimeter();
			} else {
				this.writePerimeter(scope);
			}
			
		},
		
		writeDefaultPerimeter : function (){
			$("#selected-perim").text(translator.get("wizard.perimeter.default"));	
		},
		writePerimeter : function (name){
			var link = "<a id='repen-perim' style='text-decoration: underline;' name= '" + name + "'>" + translator.get("wizard.perimeter." + name) + "</a>" ;
			var message = translator.get("wizard.perimeter.selection").split("{0}").join(link);
			$("#selected-perim").html(message);
			
		},
		resetPerimeter : function () {
			var defaultId = this.model.get("defaultProject");
			this.model.set({scope : {type : "PROJECT", id : defaultId} });
			this.model.set({projectsScope : [defaultId]});
			this.model.set({scopeEntity : "default"});
			this.writeDefaultPerimeter();
			
			
		},
		
		reopenPerimeter : function (event){
			
			
			var self = this;
			
			var nodes = _.map(this.model.get("scope"), function(obj) {
				return {
					restype:obj.type.split("_").join("-").toLowerCase() + "s", //yeah that quite fucked up...change back the _ to -, lower case and add a "s" 
					resid:obj.id};});
			
			
			var treePopup = new TreePopup({
				model : self.model,
				name : event.target.name,
				nodes : nodes
				
			});
			self.addTreePopupConfirmEvent(treePopup, self, event.target.name);
	
		},
		openPerimeterPopup : function (event){
			
			var self = this;
			
			var treePopup = new TreePopup({
				model : self.model,
				name : event.target.name,
				nodes : []
			});
			
			self.addTreePopupConfirmEvent(treePopup, self, event.target.name);
			
			
		},
		
		addTreePopupConfirmEvent : function(popup, self, name){
		
			popup.on('treePopup.confirm', function(){
				
				var scope = _.map($("#tree").jstree('get_selected'), function (sel) { return {type : $(sel).attr("restype").split("-").join("_").slice(0,-1).toUpperCase(), id:$(sel).attr("resid")}; } );
				self.model.set({scope : scope});
				self.model.set({projectsScope : _.uniq(_.map($("#tree").jstree('get_selected'), function(obj){return $(obj.closest("[project]")).attr("project"); }))});				
				self.writePerimeter(name);
				self.model.set({scopeEntity : name});
				
			});
			
		},
		
		
		updateModel : function() {
		    var entity = _.map($("input[name='entity']:checked"), function(a) {return $(a).val();});

			this.model.set({selectedEntity : entity, name : "graph" });  

		}		
	});

	return entityStepView;

});