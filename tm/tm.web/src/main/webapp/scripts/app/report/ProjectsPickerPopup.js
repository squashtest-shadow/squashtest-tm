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
 define([ "jquery",  "./ProjectsPickerModel", "underscore", "squashtable", "jqueryui", "jquery.squash.confirmdialog"],
		function($, ProjectFilterModel, _) {
	 //TODO mutualize what can be with ProjectFilterPopup and SingleProjectPickerPopup
	function eachCheckbox($domPicker, eachCallback) {
			var $boxes = $domPicker.find("table .project-checkbox");
			$boxes.each(eachCallback);
			return _.pluck($boxes, 'value');
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
				
			this.filterTable = $.proxy(this._filterTable, this);
			this.updateResult = $.proxy(this._updateResult, this);
			var self = this;			
			self.attributes.name = self.$el.attr("id");
			// process initial state
			self.attributes.allProjectIds = [];
			var projects = [];
			var ids =[];
			if(this.attributes.preferences){
				var projectsPrefs = this.attributes.preferences[self.attributes.name];
				var projectsPrefsSelected = _.filter(projectsPrefs, function(project){return project.selected;});
				ids = _.pluck(projectsPrefsSelected, "value");
			}
				this.$el.find("table tbody tr").each(function() {
					var $checkbox = $(this).find(".project-checkbox");
					var id = $checkbox.val();
					var checked = _.contains(ids, id);
					$checkbox.data("previous-checked", checked);
					$checkbox.checked = checked;
					self.attributes.allProjectIds.push(id);
				});
			
			// set model
			self.model = new ProjectFilterModel({projectIds : ids});
			
			// init confirm dialog
			this.$el.confirmDialog({
					width : 800});
			
			// init datatable
			self.table = this.$el.find("table").bind('filter', self.filterTable).squashTable({
					"sScrollY": "500px",
					"bFilter":true,
					"bPaginate" : false, 
					"bServerSide" : false,
					"bScrollCollapse": true,
					"bAutoWidth" : true,
					"bRetrieve" : false,
					"sDom" : '<"H"lfr>t',
				});
			
			this.updateResult();
			
			},
			
			open : function(){
				this.$el.confirmDialog("open");
				this.table.fnAdjustColumnSizing();
			},
			_filterTable : function(event){
				var warning = this.$el.find(".filter-warning")
				var filterText = this.$el.find('div.dataTables_filter input').val();
				if(filterText){
					warning.show();
				}else{
					warning.hide();
				}
			},
			confirm : function(){
				var self = this;
				this.table.fnFilter( '' );
				self.attributes.formState[self.attributes.name] =  _.map(self.attributes.allProjectIds, function(projectId) {
					var selected = _.contains(self.model.attributes.projectIds, projectId);
					return {
						value : projectId,
						selected : selected,
						type : "PROJECT_PICKER"
					};
				});
				self.updateResult();
			},
			
			_updateResult : function(){
				var self= this;
				if(self.model.attributes.projectIds.length > 1){
					self.attributes.$result.text(self.attributes.$result.data("multiple-value-text"));
				}else if (self.model.attributes.projectIds.length == 1){
					var projectId = self.model.attributes.projectIds[0];
					var projectName = this.$el.find('table td .project-checkbox[value='+projectId+']').parent().parent().find(".project-name").text();
					self.attributes.$result.text(projectName);
				}else{
					self.attributes.$result.text('');
				}
			},
			
			cancel : function(){
				this.table.fnFilter( '' );
				this.$el.find(".project-checkbox").each(function() {
					var previous = $(this).data("previous-checked");
					this.checked = previous;
				});
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
				this.model.deselect(deselectIds);
			},
			
			notifyModel : function(event){
				var checkbox = event.currentTarget;
				this.model.changeProjectState(checkbox.value, checkbox.checked);				
			}
		});
	 
		return ProjectFilterPopup;
});
