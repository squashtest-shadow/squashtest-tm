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
define([ "jquery", "backbone", "underscore", "app/util/StringUtil",
		"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
		"jquery.squash.datatables", "jquery.squash.oneshotdialog",
		"jquery.squash.messagedialog", "jquery.squash.confirmdialog" ],
		function($, Backbone, _, StringUtil) {
			var UMod = squashtm.app.UMod;
			var UserTeamPanel = Backbone.View.extend({
				el : "#teams",
				initialize : function() {
					this.makeTogglePanel();
					this.configurePopups();
					this.configureTable();
					this.configureButtons();
				},
				events : {},
				makeTogglePanel : function(){
					var infoSettings = {
							initiallyOpen : true,
							title : UMod.message.teamsPanelTitle,
						};
						this.$("#teams-panel").togglePanel(
								infoSettings);
				},
				configurePopups :function(){
//					this.configureRemoveMemberDialog();
//					this.configureNoMemberSelectedDialog();
//					this.configureAddMemberDialog();
				},
				configureButtons : function() {
					//===============toogle buttons=================
					// this line below is here because toggle panel
					// buttons cannot be bound with the 'events'
					// property of Backbone.View.
					// my guess is that the event is bound to the button
					// before it is moved from it's "span.not-displayed"
					// to the toggle panel header.
					// TODO change our way to make toggle panels buttons
					//=============/toogle buttons===================
					
//					this.$("#remove-members-button").on('click',$.proxy(this.confirmRemoveMember,this));
//					this.$("#add-member-button").on('click',$.proxy(this.openAddMember, this));						
				
				},

//				configureTable : function(){
//					$("#members-table").squashTable({},{});		//let's try pure DOM conf							
//				},
//				confirmRemoveMember : function(event){
//					var hasMember = ($("#members-table").squashTable().getSelectedIds().length>0);
//					if (hasMember){
//						this.confirmRemoveMemberDialog.confirmDialog("open");
//					}
//					else{
//						this.noMemberSelectedDialog.messageDialog('open');
//					}
//				},
//				
//				openAddMember : function(){
//					this.addMemberDialog.confirmDialog('open');
//				},

//				removeMembers : function(event){
//					var table = $("#members-table").squashTable();
//					var ids = table.getSelectedIds();
//					if (ids.length === 0) return;
//					
//					$.ajax({
//						url : document.location.href+"/members/"+ids.join(','),
//						type : 'delete'
//					}).done($.proxy(table.refresh, table));
//					
//				},
				
//				addMember : function(event){
//					var dialog = this.addMemberDialog;
//					var login = dialog.find('#add-member-input').val();
//					
//					$.ajax({
//						url : document.location.href+"/members/"+login,
//						type : 'PUT'
//					}).success(function(){
//						dialog.confirmDialog('close');
//						$("#members-table").squashTable().refresh();
//					});
//				},
				
//				configureRemoveMemberDialog : function(){							
//				this.confirmRemoveMemberDialog = this.$("#remove-members-dialog").confirmDialog();
//				this.confirmRemoveMemberDialog.on("confirmdialogconfirm", $.proxy(this.removeMembers, this));													
//			},
//			
//			configureNoMemberSelectedDialog : function(){
//				this.noMemberSelectedDialog = this.$("#no-selected-users").messageDialog();														
//			},
			
//			configureAddMemberDialog : function(){
//				var addMemberDialog = this.$("#add-member-dialog").confirmDialog();
//				
//				addMemberDialog.on("confirmdialogvalidate", function(){
//					var login = addMemberDialog.find('#add-member-input').val();
//					if (login===null || login===undefined || login.length === 0){
//						dialog.activate('no-selected-users');
//						return false;
//					}
//					else{
//						return true;
//					}
//				});
//				
//				addMemberDialog.on("confirmdialogconfirm", $.proxy(this.addMember, this));	
//				
//				addMemberDialog.find('#add-member-input').autocomplete();
//				
//				addMemberDialog.on('confirmdialogopen', function(){
//					var dialog = addMemberDialog;
//					var input = dialog.find('#add-member-input');
//					dialog.activate('wait');
//					$.ajax({
//						url : document.location.href+"/non-members",
//						dataType : 'json'
//					}).success(function(json){
//						if (json.length>0){
//							var source = _.map(json, function(user){return user.login});
//							input.autocomplete("option", "source", source);
//							dialog.activate('main');
//						}
//						else{
//							dialog.activate('no-more-users');
//						}
//					});								
//				});
//				
//				addMemberDialog.activate = function(arg){
//					var cls = '.'+arg;
//					this.find('div').not('.popup-dialog-buttonpane')
//						.filter(cls).show().end()
//						.not(cls).hide();
//					if (arg!=='main'){
//						this.next().find('button:first').hide();
//					}
//					else{
//						this.next().find('button:first').show();
//					}
//				};
//				
//
//				
//				this.addMemberDialog = addMemberDialog;
//			},