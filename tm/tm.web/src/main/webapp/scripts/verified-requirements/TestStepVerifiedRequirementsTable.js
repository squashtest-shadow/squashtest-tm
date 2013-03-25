/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil" , "./VerifiedRequirementsTable" ,
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
		"jquery.squash.datatables", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ],
		function($, Backbone, _, StringUtil, VerifiedRequirementsTable) {
			var VRTS = squashtm.app.verifiedRequirementsTableSettings;
				var TestStepVerifiedRequirementsTable = VerifiedRequirementsTable.extend({
					
					initialize : function(options) {
						this.constructor.__super__.initialize.apply(this, [options]);
						this.detachSelectedRequirements = $.proxy(this._detachSelectedRequirements, this);
						this.detachRequirements = $.proxy(this._detachRequirements, this);
						this.confirmDetachRequirements = $.proxy(this._confirmDetachRequirements, this);
						this.configureDetachRequirementDialog.call(this);
					},
					
					events : {},
					
					
					
					_requirementsTableRowCallback: function (row, data, displayIndex) {
						if(VRTS.linkable && data["status"] !="OBSOLETE"){
							this.addSelectEditableToVersionNumber(row, data);
						}
						this.addLinkCheckboxToRow(row, data);
						return row;
					},

					addLinkCheckboxToRow : function(row, data, displayIndex){
						var id = data["entity-id"];
						var ajaxUrl= VRTS.stepUrl +'/'+ id ;
						var sendLinkedToStep = function(event){
							var checkbox = event.target;
							var linked = $(checkbox).is(":checked");
							var ajaxType = 'delete';
							if(linked){
								ajaxType = 'post';
							}
							$.ajax({
								url : ajaxUrl,
								type : ajaxType
							}).fail(function(){
								checkbox.checked = !checkbox.checked;
							});
						}
						var checked = data["verifiedByStep"] == "false" ? false : data["verifiedByStep"];
						var checkbox = $("<input/>", { 'data-version-id' : id  , 'type':'checkbox', 'name':'verified-by-step-checkbox', 'checked':checked});
						if (VRTS.linkable){
							checkbox.on("click", sendLinkedToStep);
						}else{
							checkbox.prop('disabled', true);
						}	
						$( 'td.link-checkbox', row ).append(checkbox);
						
					},
					
					_detachSelectedRequirements : function(){
						var rows = this.table.getSelectedRows();
						this.confirmDetachRequirements(rows);
					},
					
					
					_confirmDetachRequirements : function(rows){
						var self = this;
						this.toDetachIds = [];
						var rvIds = $(rows).collect(function(row){return self.table.getODataId(row);});
						var hasRequirement = (rvIds.length > 0);
						if (hasRequirement) {					
							this.toDetachIds = rvIds;
							this.confirmDetachRequirementDialog.confirmDialog("open");						
						} else {
							this.noRequirementSelectedDialog.messageDialog('open');
						}
					},
					
					_detachRequirements : function() {
						var self = this;
						var ids = 	this.toDetachIds;
						if (ids.length === 0){
							return;
						}
						$.ajax({
							url : VRTS.stepUrl +'/'+ ids.join(','),
							type : 'delete',
						}).done(self.refresh);
					
					},
					configureDetachRequirementDialog : function() {
						this.confirmDetachRequirementDialog = $("#remove-verified-requirement-version-from-step-dialog")
								.confirmDialog();
						this.confirmDetachRequirementDialog.width("600px");
						this.confirmDetachRequirementDialog.on("confirmdialogconfirm", $
								.proxy(this.detachRequirements, this));
						this.confirmDetachRequirementDialog.on("close", $.proxy(function(){
						this.toDetachIds = [];}, this));					
					},
					
				});
				return TestStepVerifiedRequirementsTable;
			});