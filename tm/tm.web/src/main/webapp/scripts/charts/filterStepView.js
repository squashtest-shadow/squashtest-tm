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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView", "squash.configmanager", "jeditable.datepicker", "jquery.squash.togglepanel"],
	function($, backbone, _, Handlebars, AbstractStepView, confman) {
	"use strict";

	var filterStepView = AbstractStepView.extend({
		
		initialize : function(data, wizrouter) {
			this.tmpl = "#filter-step-tpl";
			this.model = data;
			data.nextStep = "type";
			data.prevStep = "scope";
			this._initialize(data, wizrouter);
			var pickerconf = confman.getStdDatepicker();
			$(".date-picker").datepicker(pickerconf);
			this.initOperationValues();
			
		},
	
		events : {
			"change .filter-operation-select" : "changeOperation",
		},
		
		initOperationValues : function (){
			
			var self = this;
			$(".filter-operation-select").each(function(indx, operation) {
				self.showFilterValues(operation.name , operation.value);
			});
			
		},
		
		updateModel : function() {
			//get ids of selecteds columns
			var ids = _.pluck($('[id^="filter-selection-"]').filter(":checked"), "name");
		
			var filters = ids.map(function (id){
				return { 
					columnId : this.findColumnById(id),
					operation : $("#filter-operation-select-" + id).val(),
					values : [$("#first-filter-value-" + id).val(), $("#second-filter-value-" + id).val()] };
				});
			
			this.model.set({ filters : filters });
	
		},
		
		findColumnById : function (id){
			return _.find(this.model.get("columnPrototypes")[this.model.get("selectedEntity")] , function(col){return col.id == id; });
		},
		
		changeOperation : function(event){				
			this.showFilterValues(event.target.name, event.target.value);
		},
		
		showFilterValues : function (id, val){
			
			var selector = $("#second-filter-value-" + id);
			if (val == "BETWEEN") {
				selector.show();
			} else {
				selector.hide();
				selector.val('');
			}
			
		}
		
	});

	return filterStepView;

});