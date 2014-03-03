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
 * projectPicker JQuery ui widget. Should be used with the appropriate dom
 * component (project-picker.frag.html)
 * 
 * Configuration : { url: "the url where to get the projects", required confirm :
 * function() on confirm, required cancel: function() on cancel, optional,
 * defaults to close loadOnce: true|false|"never", loads projects only once,
 * defaults to false, loads on each open, "never" never loads. }
 * 
 * It also forwards additional configuration to the internal popup dialog.
 * 
 * Methods : open, close
 */
 define([ "jquery",  "./ProjectFilterModel", "squashtable", "jqueryui", "jquery.squash.confirmdialog"],
		function($, ProjectFilterModel) {
	 
	function eachCheckbox($domPicker, eachCallback) {
			var $boxes = $domPicker.find("table .project-checkbox");
			$boxes.each(eachCallback);
			return $boxes.value;
	}
		
		function itemToDataMapper () {
			var item = $(this), jqCbx = item.find(".project-checkbox"), cbx = jqCbx.get()[0];
			var $name = item.find(".project-name").text();
	
			return {
				id : cbx.value,
				name : $name,
				selected : cbx.checked
			};
		}
		var ProjectFilterPopup = Backbone.View.extend({
		 
			events : {
				"confirmdialogcancel" : "cancel",
				"confirmdialogconfirm" : "confirm",
				"click .project-checkbox" : "notifyModel",
				"click .project-picker-selall" : "selectAllProjects",
				"click .project-picker-deselall" : "deselectAllProjects",
				"click .project-picker-invsel" : "invertAllProjects"
			},
		 
			initialize :function(){
			var self = this;			
			
			// process initial state
			var projects = [];
			var ids =[];
			this.$el.find("table tbody tr").each(function() {
				var $checkbox = $(this).find(".project-checkbox");
				var checked = $checkbox.checked;
				$checkbox.data("previous-checked", checked);
				var id = $checkbox.val();
				var name = $(this).find(".project-name").text();
				var project = {id :id, name : name , checked : checked};
				if(checked){
					ids.put(id);
				}
				projects.push(project);
			});
			// set model
			var url  = this.$el.data("url");
			self.model = new ProjectFilterModel({projectIds : ids},{url : url, initiallySelectedIds : ids , projectsState : projects});
			
			// init confirm dialog
			this.$el.confirmDialog({width : 600});
			
			// init datatable
			self.table = this.$el.find("table").squashTable({
					"sScrollY": "500px",
					"bFilter":true,
					"bPaginate" : false, 
					"bServerSide" : false,
					"bScrollCollapse": true,
					"bAutoWidth" : true,
					"bRetrieve" : false,
					"sDom" : '<"H"lfr>t',
				});
			
			},
			
			open : function(){
				this.$el.confirmDialog("open");
				this.table.fnAdjustColumnSizing();
			},
			
			confirm : function(){
				this.model.save(null,{success : function(){window.location.reload();}
				});
			},
			
			cancel : function(){
				this.table.fnFilter( '' );
				this.$el.find(".project-checkbox").each(function() {
					var previous = $(this).data("previous-checked");
					this.checked = previous;
				});
			},
			
			
	 
			dialogConfig : {
				autoOpen : false,
				resizable : false,
				modal : true,
				width : 600
			},
		
			selectAllProjects : function() {
				var ids = eachCheckbox(this.$el, function() {
					this.checked = true;
				});
				this.model.select(ids);
			},
		
			deselectAllProjects : function () {
				
				var ids = eachCheckbox(this.$el, function() {
					this.checked = false;
				});
				this.model.deselect(ids);
			},
		
			invertAllProjects : function () {
				var selectIds = [];
				var deselectIds = [];
				eachCheckbox(this.$el, function() {
					if(this.checked){
						this.checked = false;
						deselectIds.push(this.value);
					}else{
						this.checked = true;
						selectIds.push(this.value);
						
					}
					
				});
				this.model.select(selectIds);
				this.model.select(deselectIds);
			},
			
			notifyModel : function(event){
				var checkbox = event.currentTarget;
				this.model.changeProjectState(checkbox.value, checkbox.checked);				
			}
		});
	 
		return ProjectFilterPopup;
});
