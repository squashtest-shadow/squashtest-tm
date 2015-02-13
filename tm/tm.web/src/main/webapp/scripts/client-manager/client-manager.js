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
	define(['module', "jquery", "squash.translator", "workspace.routing","squash.configmanager","app/ws/squashtm.notification", "squashtable", 
	         "app/ws/squashtm.workspace", 
	         "jquery.squash.formdialog", "jquery.squash.confirmdialog"], 
			function(module, $, translator, routing, confman, notification){					
		
		var config = module.config();

		$(function() {			
			
			var squashSettings = {
					functions:{					
						drawDeleteButton: function(template, cells){
				
							$.each(cells, function(index, cell) {
								var row = cell.parentNode; // should be the tr
								var id = clientTable.getODataId(row);
								var $cell = $(cell);
								
									$cell.html(template);
									$cell.find('a').button({
										text : false,
										icons : {
											primary : "ui-icon-trash"
										}
									});		
													
							});
						}	
					}
			};
			
			var clientTable = $("#client-table").squashTable({"bServerSide":false},squashSettings);			
			$('#new-client-button').button();	

			/* The button gets CSS we don't want to keep a clean CSS and also put a span with text only after*/
			$("#new-client-button").removeClass("ui-button-text-only").addClass("ui-button-text-icon-primary");
			$("#new-client-button > span").removeClass("ui-button-text");
		});	

		$("#delete-client-popup").confirmDialog().on('confirmdialogconfirm', function(event){
			
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = squashtm.app.contextRoot+'/administration/config/clients/'+ ids.join(",");
			var table = $("#client-table").squashTable();
			var selectedRow = table.getRowsByIds(ids);
			
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table._fnAjaxUpdate();
			});
			
			
		});

		$("#delete-client-button").on('click', function(){

			var ids = $("#client-table").squashTable().getSelectedIds();
	
			if (ids.length>0){
				var popup = $("#delete-client-popup");
				popup.data('entity-id', ids);
				popup.confirmDialog('open');
			}
			else{
				displayNothingSelected();
			}
		});
		
		function displayNothingSelected(){
			var warn = translator.get({
				errorTitle : 'popup.title.Info',
				errorMessage : 'message.EmptyTableSelection'
			});
			$.squash.openMessage(warn.errorTitle, warn.errorMessage);
		}
		
		
	
	var addClientDialog = $("#add-client-dialog");
		
	addClientDialog.formDialog();

	addClientDialog.on('formdialogconfirm', function(){
		var url = squashtm.app.contextRoot+'/administration/config/clients/';
		var params = {
			clientId: $( '#add-client-name' ).val(),
			clientSecret: $( '#add-client-secret' ).val(),
		};
		$.ajax({
			url : url,
			type : 'POST',
			dataType : 'json',
			data : params				
		}).success(function(data){
			$('#client-table').squashTable()._fnAjaxUpdate();
			addClientDialog.formDialog('close');
		});
	
	});
	
	addClientDialog.on('formdialogcancel', function(){
		addClientDialog.formDialog('close');
	});
		
	$('#new-client-button').on('click', function(){
		addClientDialog.formDialog('open');
	});
	
	});			
	