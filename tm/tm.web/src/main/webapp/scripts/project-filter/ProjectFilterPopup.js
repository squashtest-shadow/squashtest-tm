/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
 define([ "jquery",  "./ProjectFilterModel","underscore","app/squash.handlebars.helpers", "squashtable", "jqueryui", "jquery.squash.confirmdialog",],
		function($, ProjectFilterModel, _, Handlebars) {
	//TODO mutualize what can be with app/report/ProjectsPickerPopup and SingleProjectPickerPopup
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
		 
			initialize :function(options){
				var self = this;
				this.options = options;
				//if we are in a SPA environnement like in custom reports, perform render before init datatable and component
				//you must also provide the model as options.initialProjectModel
				//please don't overide the selection model declared below as this.model !
				if(this.options && this.options.frontTemplating){
					this.render().doInitialize();
				}
				else {
					this.doInitialize();
				}	
			},

			doInitialize : function() {
				var self = this;
				this.filterTable = $.proxy(this._filterTable, this);
				// process initial state
				var ids =[];
				this.$el.find("table tbody tr").each(function() {
					var $checkbox = $(this).find(".project-checkbox");
					var checked = $checkbox.is(":checked");
					$checkbox.data("previous-checked", checked);
					var id = $checkbox.val();
					if(checked){
						ids.push(id);
					}
				});
				// set model 
				var url  = this.$el.data("url");
				self.model = new ProjectFilterModel({projectIds : ids} );
				self.model.url = url;
				
				// init confirm dialog
				this.$el.confirmDialog({
						width : 800});
				
				// init datatable
				// change filter by search.dt
				self.table = this.$el.find("table").bind('search.dt', self.filterTable).squashTable({
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

			render : function() {
				var templateSelector = this.options.templateSelector;
				if (templateSelector){
					var source = $(templateSelector).html();
					var template = Handlebars.compile(source);
					this.$el.append(template(this.options.initialProjectModel));
				} else {
					throw "you must specify a template selector to render this dialog with hanlebar, client side";
				}
				return this;
			},
			
			open : function(){
				this.$el.confirmDialog("open");
				this.table.fnAdjustColumnSizing();
			},

			_filterTable : function(event){
				var warning = this.$el.find(".filter-warning");
				var filterText = this.$el.find('div.dataTables_filter input').val();
				if(filterText){
					warning.show();
				}else{
					warning.hide();
				}
			},
			confirm : function(){
				//in case of use in SPA environnement, we don't want to post project ProjectFilter
				//And by mercy we don't want to reload page and loose our precious client state...
				if(this.options && this.options.preventServerFilterUpdate){
					this.trigger("projectPopup.confirm");
				}
				else {
					this.model.save(null,{
					success : function(){window.location.reload();}
				});
				}
				
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
				modal : true
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
