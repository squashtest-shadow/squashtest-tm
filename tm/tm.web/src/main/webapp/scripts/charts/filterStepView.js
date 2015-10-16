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
define(["jquery", "backbone", "underscore", "handlebars", "./abstractStepView", "squash.configmanager", "squash.translator", "jeditable.datepicker", "jquery.squash.togglepanel",],
	function($, backbone, _, Handlebars, AbstractStepView, confman, translator) {
	"use strict";

	var filterStepView = AbstractStepView.extend({
		
		initialize : function(data) {
			this.dateISOFormat = $.datepicker.ISO_8601;
			this.datePickerFormat = translator.get("squashtm.dateformatShort.datepicker");
			this.tmpl = "#filter-step-tpl";
			this.model = data;
			this._initialize(data);
			var pickerconf = confman.getStdDatepicker();
			$(".date-picker").datepicker(pickerconf);
			this.reloadPreviousValues();
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
		
		reloadPreviousValues : function (){
			
			var self = this;
			var filters = this.model.get("filters");
			
			if (filters !== undefined){
				
				_.each(filters, function(filter){				
					self.applyPreviousValues(filter);
				});	
			}
			
		},
		applyPreviousValues : function (filter){
			var self = this;
			var id = filter.column.id;
						
			$("#filter-selection-" + id).attr("checked", "true");
			$("#filter-operation-select-" + id).val(filter.operation);	
			$("#first-filter-value-" + id).val(self.getValueFromFilter(filter, 0));
			$("#second-filter-value-" + id).val(self.getValueFromFilter(filter, 1));
		},
		getValueFromFilter : function (filter, pos){
			var self = this;
			var datatype = filter.column.dataType;
			
			var result = filter.values[pos];
			
			if (datatype == "DATE" && result !== undefined){
			var date = $.datepicker.parseDate(self.dateISOFormat, result);
			result = $.datepicker.formatDate(self.datePickerFormat, date);
			}
			
			return  result;	
		},
		
		updateModel : function() {
			//get ids of selecteds columns
			var ids = _.pluck($('[id^="filter-selection-"]').filter(":checked"), "name");
			var self = this;
			var filters = ids.map(function (id){
				return { 
					column : self.findColumnById(id),
					operation : $("#filter-operation-select-" + id).val(),
					values : self.getFilterValues(id) };
				});
			
			this.model.set({ filters : filters });
	
		},
		getFilterValues : function (id){
			var self = this;
			var datatype = self.findColumnById(id)["dataType"];
			var result = [$("#first-filter-value-" + id).val(), $("#second-filter-value-" + id).val()];
			
			
			if (datatype == "DATE"){
				result = _.map(result, function(elem){				
					var date = $.datepicker.parseDate(self.datePickerFormat, elem);
					var result = $.datepicker.formatDate(self.dateISOFormat, date);
					return result;
				});	
			}
			
			return self.removeEmpty(result);
			
		},
		removeEmpty : function(tab){
			return _.filter(tab, function(elem){return elem !== undefined && elem !== "";});
			
		},
		findColumnById : function (id){
			return _.find(_.reduce(this.model.get("columnPrototypes"), function(memo, val){ return memo.concat(val); }, []), function(col){return col.id == id; });
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