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
 define([ "jquery",  "squashtable", "jqueryui", "jquery.squash.confirmdialog"],
		function($) {
	 
	
		
	//TODO mutualize what can be with ProjectFilterPopup and ProjectsPickerPopup
		var ProjectFilterPopup = Backbone.View.extend({
		 
			events : {
				"confirmdialogcancel" : "cancel",
				"confirmdialogconfirm" : "confirm",
				"click .project-checkbox" : "setSelected",
			
			},
			
			
			initialize :function(){
			var self = this;			
			self.attributes.name = self.$el.attr("id");
			// process initial state
			self.attributes.allProjectIds = [];
			this.updateResult = $.proxy(this._updateResult, this);
			this.filterTable = $.proxy(this._filterTable, this);
			this.updateChecked = $.proxy(this._updateChecked, this);
			if(this.attributes.preferences){
				self.attributes.selectedId =  _.findWhere(this.attributes.preferences[self.attributes.name], {selected:true}).value;
			}
			
				this.$el.find("table tbody tr").each(function() {
					var $checkbox = $(this).find(".project-checkbox");
					var id = $checkbox.val();
					var checked = self.attributes.selectedId == id;
					$checkbox.data("previous-checked", checked);
					$checkbox.prop("checked", checked);
					self.attributes.allProjectIds.push(id);
				});
			
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
					"fnDrawCallback" : self.updateChecked
				});
			self.updateResult();
			},
			
			open : function(){
				this.$el.confirmDialog("open");
				this.table.fnAdjustColumnSizing();
			},
			_filterTable : function(event){
				var self = this;
				var warning = this.$el.find(".filter-warning");
				var filterText = this.$el.find('div.dataTables_filter input').val();
				if(filterText){
					warning.show();
				}else{
					warning.hide();
				}
				
			},
			_updateChecked : function(){
				var self = this;
				//To fix problem when check another radio button while previously checked was hidden.
				this.$el.find("table tbody tr").each(function() {
					var $checkbox = $(this).find(".project-checkbox");
					var id = $checkbox.val();
					var checked = self.attributes.selectedId == id;					
					$checkbox.prop("checked", checked);
				});
			},
			confirm : function(){
				var self = this;
				this.table.fnFilter( '' );
				self.attributes.formState[self.attributes.name] =  _.map(self.attributes.allProjectIds, function(projectId) {
					var selected = self.attributes.selectedId == projectId;
					return {
						value : projectId,
						selected : selected,
						type : "PROJECT_PICKER"
					};
				});
				this.updateResult();
			},
			
			_updateResult : function(){
				var self = this;
				if(self.attributes.selectedId){
					var projectId = self.attributes.selectedId;
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
				 
			setSelected : function(event){
				var radio = event.currentTarget;
				this.attributes.selectedId = radio.value ;
			}
		});
	 
		return ProjectFilterPopup;
});
