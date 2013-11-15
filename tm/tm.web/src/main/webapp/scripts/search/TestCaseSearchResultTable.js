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
define([ "jquery", "backbone", "squash.translator", "squashtable", "jqueryui", "jquery.squash.jeditable" ], function($, Backbone, translator) {

	var TestCaseSearchResultTable = Backbone.View.extend({
		el : "#test-case-search-result-table",
		initialize : function(model, isAssociation, associateType, associateId) {
			this.model = model;
			this.isAssociation = isAssociation;
			this.associateType = associateType;
			this.associateId = associateId;
			this.addSelectEditableToImportance = $.proxy(this._addSelectEditableToImportance, this);
			this.addSelectEditableToNature = $.proxy(this._addSelectEditableToNature, this);
			this.addSelectEditableToType = $.proxy(this._addSelectEditableToType, this);
			this.addSelectEditableToStatus = $.proxy(this._addSelectEditableToStatus, this);
			this.addSimpleEditableToReference = $.proxy(this._addSimpleEditableToReference, this);
			this.addSimpleEditableToLabel = $.proxy(this._addSimpleEditableToLabel, this);
			this.addInterfaceLevel2Link = $.proxy(this._addInterfaceLevel2Link, this);
			this.addIconToAssociatedToColumn = $.proxy(this._addIconToAssociatedToColumn, this);
			this.addTreeLink = $.proxy(this._addTreeLink, this);
			this.getTableRowId = $.proxy(this._getTableRowId, this);
			this.tableRowCallback = $.proxy(this._tableRowCallback, this);
			this.addAssociationCheckboxes  = $.proxy(this._addAssociationCheckboxes, this);
			
			var self;
			
			if(isAssociation){
				
				self = this, tableConf = {
						"oLanguage" : {
							"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
						},
					    "bServerSide": true,  
						"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
						 "fnServerParams": function ( aoData )   
						    {  
							 aoData.push( { "name": "testcase", "value": "testcase" } );  
						        aoData.push( { "name": "model", "value": JSON.stringify(model) } );  
						        aoData.push( { "name": "associateResultWithType", "value": associateType } );  
						        aoData.push( { "name": "id", "value":  associateId } );  
						    }, 
						"sServerMethod": "POST",
						"bDeferRender" : true,
						"bFilter" : false,
						"fnRowCallback" : this.tableRowCallback,
						"fnDrawCallback" : this.tableDrawCallback,
						"aaSorting" : [ [ 2, "asc" ], [4, "asc"], [6, "asc"], [5, "asc"] ],
						"aoColumnDefs" : [ {
							"bSortable" : false,
							"aTargets" : [ 0 ],
							"mDataProp" : "entity-index",
							"sClass" : "select-handle centered"
						}, {
							"aTargets" : [ 1 ],
							"mDataProp" : "empty-is-associated-holder",
							"bSortable" : false,
							"sWidth" : "2em",
							"sClass" : "is-associated centered"
						}, {
							"aTargets" : [ 2 ],
							"mDataProp" : "project-name",
							"bSortable" : true
						}, {		
							"aTargets" : [ 3 ],
							"mDataProp" : "test-case-id",
							"bSortable" : true,
							"sClass" : "centered element_id"
						}, {
							"aTargets" : [ 4 ],
							"mDataProp" : "test-case-ref",
							"bSortable" : true,
							"sClass" : "editable_ref"
						}, {
							"aTargets" : [ 5 ],
							"mDataProp" : "test-case-label",
							"bSortable" : true,
							"sClass" : "editable_label"
						}, {
							"aTargets" : [ 6 ],
							"mDataProp" : "test-case-weight",
							"bSortable" : true,
							"sClass" : "editable_importance"
						}, {
							"aTargets" : [ 7 ],
							"mDataProp" : "test-case-nature",
							"bSortable" : true,
							"sClass" : "editable_nature"
						}, {
							"aTargets" : [ 8 ],
							"mDataProp" : "test-case-type",
							"bSortable" : true,
							"sClass" : "editable_type"
						}, {
							"aTargets" : [ 9 ],
							"mDataProp" : "test-case-status",
							"bSortable" : true,
							"sClass" : "editable_status"
						}, {
							"aTargets" : [ 10 ],
							"mDataProp" : "test-case-requirement-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 11 ],
							"mDataProp" : "test-case-teststep-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 12 ],
							"mDataProp" : "test-case-iteration-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 13 ],
							"mDataProp" : "test-case-attachment-nb",
							"bSortable" : true
						}, {
							"aTargets" : [ 14 ],
							"mDataProp" : "test-case-created-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 15 ],
							"mDataProp" : "test-case-modified-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 16 ],
							"mDataProp" : "empty-openinterface2-holder",
							"sClass" : "centered search-open-interface2-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 17 ],
							"mDataProp" : "editable",
							"bVisible" : false,
							"bSortable" : false
						} ],
						"sDom" : 'ft<"dataTables_footer"lip>'
					}, squashConf = {
						enableHover : true
					};
				
				this.$el.squashTable(tableConf, squashConf);
			} else {
				self = this, tableConf = {
						"oLanguage" : {
							"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
						},
					    "bServerSide": true,  
						"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
						 "fnServerParams": function ( aoData )   
						    {  
							 aoData.push( { "name": "testcase", "value": "testcase" } );  
							 aoData.push( { "name": "model", "value": JSON.stringify(model) } );  
						    }, 
						"sServerMethod": "POST",
						"bDeferRender" : true,
						"bFilter" : false,
						"fnRowCallback" : this.tableRowCallback,
						"fnDrawCallback" : this.tableDrawCallback,
						"aaSorting" : [ [ 1, "asc" ],[3, "asc"],[5, "asc"],[4, "asc"] ],
						"aoColumnDefs" : [ {
							"bSortable" : false,
							"aTargets" : [ 0 ],
							"mDataProp" : "entity-index",
							"sClass" : "select-handle centered"
						}, {
							"aTargets" : [ 1 ],
							"mDataProp" : "project-name",
							"bSortable" : true
						}, {		
							"aTargets" : [ 2 ],
							"mDataProp" : "test-case-id",
							"bSortable" : true,
							"sClass" : "centered element_id"
						}, {
							"aTargets" : [ 3 ],
							"mDataProp" : "test-case-ref",
							"bSortable" : true,
							"sClass" : "editable_ref"
						}, {
							"aTargets" : [ 4 ],
							"mDataProp" : "test-case-label",
							"bSortable" : true,
							"sClass" : "editable_label"
						}, {
							"aTargets" : [ 5 ],
							"mDataProp" : "test-case-weight",
							"bSortable" : true,
							"sClass" : "editable_importance"
						}, {
							"aTargets" : [ 6 ],
							"mDataProp" : "test-case-nature",
							"bSortable" : true,
							"sClass" : "editable_nature"
						}, {
							"aTargets" : [ 7 ],
							"mDataProp" : "test-case-type",
							"bSortable" : true,
							"sClass" : "editable_type"
						}, {
							"aTargets" : [ 8 ],
							"mDataProp" : "test-case-status",
							"bSortable" : true,
							"sClass" : "editable_status"
						}, {
							"aTargets" : [ 9 ],
							"mDataProp" : "test-case-requirement-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 10 ],
							"mDataProp" : "test-case-teststep-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 11 ],
							"mDataProp" : "test-case-iteration-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 12 ],
							"mDataProp" : "test-case-attachment-nb",
							"bSortable" : true,
							"sClass" : "centered"
						}, {
							"aTargets" : [ 13 ],
							"mDataProp" : "test-case-created-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 14 ],
							"mDataProp" : "test-case-modified-by",
							"bSortable" : true
						}, {
							"aTargets" : [ 15 ],
							"mDataProp" : "empty-openinterface2-holder",
							"sClass" : "centered search-open-interface2-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 16 ],
							"mDataProp" : "empty-opentree-holder",
							"sClass" : "centered search-open-tree-holder",
							"sWidth" : "2em",
							"bSortable" : false
						}, {
							"aTargets" : [ 17 ],
							"mDataProp" : "editable",
							"bVisible" : false,
							"bSortable" : false
						} ],
						"sDom" : 'ft<"dataTables_footer"lip>'
					}, squashConf = {
						enableHover : true
					};
				
				this.$el.squashTable(tableConf, squashConf);
			}
			

		},

		_getTableRowId : function(rowData) {
			return rowData[2];	
		},

		_addSelectEditableToImportance : function(row, data) {
			var self = this;
			var urlPOST = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			var urlGET = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"] + "/importance-combo-data";
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			$('.editable_importance', row).editable(urlPOST, {
						type : 'select',
						submit : ok,
						cancel : cancel,
						loadurl : urlGET,
						"submitdata" : function(value, settings) {
							return {"id": "test-case-importance"};
						}
					});
		},
		
		_addSelectEditableToNature : function(row, data) {
			var self = this;
			var urlPOST = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			var urlGET = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"] + "/nature-combo-data";
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			$('.editable_nature', row).editable(urlPOST, {
				type : 'select',
				submit : ok,
				cancel : cancel,
				loadurl : urlGET,
				"submitdata" : function(value, settings) {
					return {"id": "test-case-nature"};
				}
			});
		},
		
		_addSelectEditableToType : function(row, data) {
			var self = this;
			var urlPOST = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			var urlGET = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"] + "/type-combo-data";
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			$('.editable_type', row).editable(urlPOST, {
				type : 'select',
				submit : ok,
				cancel : cancel,
				loadurl : urlGET,
				"submitdata" : function(value, settings) {
					return {"id": "test-case-type"};
				}
			});
		},
		
		_addSelectEditableToStatus : function(row, data) {
			var self = this;
			var urlPOST = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			var urlGET = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"] + "/status-combo-data";
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			$('.editable_status', row).editable(urlPOST, {
				type : 'select',
				submit : ok,
				cancel : cancel,
				loadurl : urlGET,
				"submitdata" : function(value, settings) {
					return {"id": "test-case-status"};
				}
			});
		},
		
		_addSimpleEditableToReference : function(row, data) {
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			var placeholder = translator.get("rich-edit.placeholder");
			var url = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			
			$(".editable_ref", row).editable(url,{
				"placeholder" : placeholder,
				"submit" : ok,
				"cancel" : cancel,
				"submitdata" : function(value, settings) {
					return {"id": "test-case-reference"};
				}
			});
		},

		_addAssociationCheckboxes : function(row, data) {
			$(".association-checkbox", row).html("<input type='checkbox'/>");
		},
		
		_addSimpleEditableToLabel : function(row, data) {
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			var placeholder = translator.get("rich-edit.placeholder");
			var url = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			$(".editable_label", row).editable(url,{
				"placeholder" : placeholder,
				"submit" : ok,
				"cancel" : cancel,	
				"submitdata" : function(value, settings) {
					return {"id": "test-case-newname"};
				}
			});
		},
	
		_tableRowCallback : function(row, data, displayIndex) {
			if(data["editable"]){
				this.addSimpleEditableToReference(row,data);
				this.addSimpleEditableToLabel(row,data);
				this.addSelectEditableToImportance(row,data);
				this.addSelectEditableToNature(row,data);
				this.addSelectEditableToStatus(row,data);
				this.addSelectEditableToType(row,data);
			}

			this.addInterfaceLevel2Link(row,data);
			this.addTreeLink(row,data);
	
			if(this.isAssociation){
				this.addIconToAssociatedToColumn(row,data);
			}
		},

		_addInterfaceLevel2Link : function(row, data) {
			var id = data["test-case-id"];
			var $cell = $(".search-open-interface2-holder",row);
			$cell.append('<span class="search-open-interface2"></span>')
			.click(function(){
		        window.location = squashtm.app.contextRoot + "/test-cases/" + id + "/info";
		    });
		},
		
		_addIconToAssociatedToColumn : function(row, data) {
			
			var associatedTo = data["is-associated"];
			
			if(associatedTo){
				if(this.associateType == "requirement"){
					$(".is-associated",row).append('<span class="associated-icon-requirement"></span>');
				} else {
					$(".is-associated",row).append('<span class="associated-icon-campaign"></span>');
				}
			}
		},
		
		_addTreeLink : function(row, data){
			var self = this;
			var id = data["test-case-id"];
			var $cell = $(".search-open-tree-holder", row);
			$cell.append('<span class="search-open-tree"></span>')
				.click(function(){
				window.location = squashtm.app.contextRoot + "/test-case-workspace/?element_id="+id;
			});
		},
				
		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return TestCaseSearchResultTable;
});