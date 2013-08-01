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
define([ "jquery", "backbone", "squash.translator", "squash.datatables", "jquery.squash.datatables", "jqueryui" ], function($, Backbone, translator,
		SQDT) {

	var TestCaseSearchResultTable = Backbone.View.extend({
		el : "#test-case-search-result-table",
		initialize : function(model) {
			this.model = model;
			this.addSelectEditableToImportance = $.proxy(this._addSelectEditableToImportance, this);
			this.addSimpleEditableToReference = $.proxy(this._addSimpleEditableToReference, this);
			this.addSimpleEditableToLabel = $.proxy(this._addSimpleEditableToLabel, this);
			this.addInterfaceLevel2Link = $.proxy(this._addInterfaceLevel2Link, this);
			this.addTreeLink = $.proxy(this._addTreeLink, this);
			this.tableDrawCallback = $.proxy(this._tableDrawCallback, this);
			this.openBreadCrumb =  $.proxy(this._openBreadCrumb, this);
			this.openFoldersUntillEnd = $.proxy(this._openFoldersUntillEnd, this);
			this.findTreeBreadcrumbToNode =  $.proxy(this._findTreeBreadcrumbToNode, this);
			this.getTableRowId = $.proxy(this._getTableRowId, this);
			this.tableRowCallback = $.proxy(this._tableRowCallback, this);
			var self = this, tableConf = {
				"oLanguage" : {
					"sUrl" : squashtm.app.contextRoot + "/datatables/messages"
				},
				"sAjaxSource" : squashtm.app.contextRoot + "/advanced-search/table",
				"bDeferRender" : true,
				"bFilter" : false,
				"fnRowCallback" : this.tableRowCallback,
				"fnDrawCallback" : this.tableDrawCallback,
				"aaSorting" : [ [ 2, "asc" ] ],
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
					"bSortable" : true
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
					"sClass" : "editable_imp"
				}, {
					"aTargets" : [ 6 ],
					"mDataProp" : "test-case-requirement-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 7 ],
					"mDataProp" : "test-case-teststep-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 8 ],
					"mDataProp" : "test-case-iteration-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 9 ],
					"mDataProp" : "test-case-attachment-nb",
					"bSortable" : true
				}, {
					"aTargets" : [ 10 ],
					"mDataProp" : "test-case-created-by",
					"bSortable" : true
				}, {
					"aTargets" : [ 11 ],
					"mDataProp" : "test-case-modified-by",
					"bSortable" : true
				}, {
					"aTargets" : [ 12 ],
					"mDataProp" : "empty-openinterface2-holder",
					"sClass" : "centered search-open-interface2",
					"sWidth" : "2em",
					"bSortable" : false
				}, {
					"aTargets" : [ 13 ],
					"mDataProp" : "empty-opentree-holder",
					"sClass" : "centered search-open-tree",
					"sWidth" : "2em",
					"bSortable" : false
				}, {
					"aTargets" : [ 14 ],
					"mDataProp" : "editable",
					"bVisible" : false,
					"bSortable" : false
				} ],
				"sDom" : 'ft<"dataTables_footer"lirp>'
			}, squashConf = {
				enableHover : true
			};

			this.$el.squashTable(tableConf, squashConf);
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
			$('.editable_imp', row).editable(
					function(value, settings) {
						var innerPOSTData;
						$.post(urlPOST, {
							value : value,
							id : "test-case-importance"	
						}, function(data) {
							innerPOSTData = data;
							self.refresh();
						});
						return (innerPOSTData);
					}, {
						type : 'select',
						submit : ok,
						cancel : cancel,
						onblur : function() {
						},
						loadurl : urlGET,
						onsubmit : function() {
						}
					});
		},
		
		_addSimpleEditableToReference : function(row, data) {
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			var url = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			$(".editable_ref", row).editable(url,{
				"submit" : ok,
				"cancel" : cancel,
				"submitdata" : function(value, settings) {
					return {"id": "test-case-reference"};
				}
			});
		},
		
		_addSimpleEditableToLabel : function(row, data) {
			var ok = translator.get("rich-edit.button.ok.label");
			var cancel = translator.get("label.Cancel");
			var url = squashtm.app.contextRoot + "/test-cases/" + data["test-case-id"];
			$(".editable_label", row).editable(url,{
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
			}
			
			this.addInterfaceLevel2Link(row,data);
			this.addTreeLink(row,data);
		},

		_tableDrawCallback : function() {

		},

		_addInterfaceLevel2Link : function(row, data) {
			var id = data["test-case-id"];
		    $(".search-open-interface2",row).click(function(){
		        window.location = squashtm.app.contextRoot + "/test-cases/" + id + "/info";
		    });
		},
		

		_addTreeLink : function(row, data){
			var self = this;
			var id = data["test-case-id"];
			$(".search-open-tree", row)
					.click(
							function() {
								$("#tree-panel-left").show();
								$("#contextual-content").removeAttr("style"); 
								$("#tabbed-pane").tabs('select', 0);
								var jqTree=$("#tree");
								var treeNodeName = "TestCase-" + id;
								self.findTreeBreadcrumbToNode(treeNodeName, squashtm.app.contextRoot + "/search/test-cases/breadcrumb" ).done(function(data){self.openBreadCrumb(data, jqTree);});
							});
		},
			
		_openBreadCrumb : function(treeNodesIds, jqTree){
			var self = this;
			var breadCrumbLength = treeNodesIds.length;
			var libraryName = treeNodesIds[breadCrumbLength - 1];
			jqTree.jstree("deselect_all");
			var librayNode = jqTree.find("li[id=\'"+libraryName+"\']");
			  var start = breadCrumbLength -2;
			  jqTree.jstree("open_node",librayNode, function(){self.openFoldersUntillEnd(treeNodesIds,  jqTree, start);});
		},
		
		_openFoldersUntillEnd : function(treeNodesIds,  jqTree, i){
			var self = this; 
			var treeNodeName;
			var treeNode;
			if ( i >= 1 ) {  
				  treeNodeName = treeNodesIds[i];
				  treeNode = jqTree.find("li[id=\'"+treeNodeName+"\']");
				  i--;
				  jqTree.jstree("open_node",treeNode, function(){self.openFoldersUntillEnd(treeNodesIds,  jqTree, i);});
			}else{
				  treeNodeName = treeNodesIds[i];
				  treeNode = jqTree.find("li[id=\'"+treeNodeName+"\']");
				  jqTree.jstree("deselect_all");
				  jqTree.jstree("select_node",treeNode);
			  }
		},
			
		_findTreeBreadcrumbToNode : function(treeNodeName, url){
			var dataB = {
					'nodeName' : treeNodeName
				};
			return $.ajax({
				'url' : url,
				type : 'POST',
				data : dataB,
				dataType : 'json'
			});
		},
		
		refresh : function() {
			this.$el.squashTable().fnDraw(false);
		}
	});

	return TestCaseSearchResultTable;
});