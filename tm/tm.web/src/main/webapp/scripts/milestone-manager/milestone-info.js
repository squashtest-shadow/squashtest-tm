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
	define([ 'module', "jquery", "squash.translator", "squash.basicwidgets", "jeditable.selectJEditable",
			"squash.configmanager", "workspace.routing", "jquery.squash.formdialog", "jeditable.datepicker", "squashtable", "jquery.squash.confirmdialog" ], function(
			module, $, translator, basic, SelectJEditable, confman, routing) {

		var config = module.config();

		function clickBugtackerBackButton() {
			document.location.href = config.urls.milestonesUrl;
		}

		function initRenameDialog() {
			var renameDialog = $("#rename-milestone-dialog");
			renameDialog.formDialog();

			renameDialog.on('formdialogopen', function() {
				var name = $.trim($('#milestone-name-header').text());
				$("#rename-milestone-input").val($.trim(name));
			});

			renameDialog.on('formdialogconfirm', function() {
				var params = {
					newName : $("#rename-milestone-input").val()
				};
				$.ajax({
					url : config.urls.milestoneUrl,
					type : 'POST',
					dataType : 'json',
					data : params
				}).success(function(data) {
					$('#milestone-name-header').html(data.newName);
					renameDialog.formDialog('close');
				});
			});

			renameDialog.on('formdialogcancel', function() {
				renameDialog.formDialog('close');
			});

			$("#rename-milestone-button").on('click', function() {
				renameDialog.formDialog('open');
			});

		}

		var postfn = function(value) {
			var localizedDate = value;
			var postDateFormat = $.datepicker.ATOM;
			var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
			var postDate = $.datepicker.formatDate(postDateFormat, date);

			return $.ajax({
				url : config.urls.milestoneUrl,
				type : 'POST',
				data : {
					newEndDate : postDate
				}
			}).done(function() {
				$("#milestone-end-date").text(value);
			});
		};

		var initJEditables = function() {
		var dateSettings = confman.getStdDatepicker();
		$("#milestone-end-date").editable(postfn, {
			type : 'datepicker',
			datepicker : dateSettings,
			name : "value"
		});

		var statusEditable = new SelectJEditable({
			target : config.urls.milestoneUrl,
			componentId : "milestone-status",
			jeditableSettings : {
				data : config.data.milestone.status
			},
		});
		
		var rangeEditable = new SelectJEditable({
			target : function(value) { changeRange(value, rangeEditable);},
			componentId : "milestone-range",
			jeditableSettings : {
				data : config.data.milestone.range
			},
		});
		
		var ownerEditable = createOwnerEditable();
		};
		
		
		function createOwnerEditable(){ 
			new SelectJEditable({
			target : config.urls.milestoneUrl,
			componentId : "milestone-owner",
			jeditableSettings : {
				data : config.data.userList
			}});
		}

			
		var changeRange = function changeRange(value, self){
		
			$.ajax({
				type : "POST",
				url : config.urls.milestoneUrl,
				data: {
					id: self.settings.componentId,
					value: value
				}
			}).then(function(value) {
				self.component.html(value);
				var data = JSON.parse(self.settings.jeditableSettings.data);
				var newRange;		
				for(var prop in data) {
                     if(data.hasOwnProperty(prop)){
                    	 if (data[prop] === value){
                    		 newRange = prop;
                    	 }
                     }
                       } 
				updateAfterRangeChange(newRange);
			});
		};
	
		function updateAfterRangeChange(newRange) {

			var ownerEditable = $("#milestone-owner-cell");
			//update the currentRange with the new value
			config.data.milestone.currentRange = newRange;
			//redraw the table so the project binding is editable or not depending on range
			$('#projects-table').squashTable().fnDraw();
			if (newRange === "GLOBAL"){
				//If new range is global, the owner is not editable and equal to <Admin>
				ownerEditable.html(translator.get("label.milestone.global.owner"));
			} else {
				//If new range is restricted, we must update the owner to the current user
				$.ajax({
					type : "POST",
					url : config.urls.milestoneUrl,
					data: {
						id: "milestone-owner",
						value: config.data.currentUser
					}
				}).then(function(value) {
					//recreate the editable
					ownerEditable.html('<span id="milestone-owner" >' + value + '</span>');
			        createOwnerEditable();
				});
			}
		}

		

		var drawCallBack = function editableBinding() {
//the bind to project is editable only if range is restricted
			if (config.data.milestone.currentRange === "RESTRICTED"){
			$("td.binded-to-project").editable('enable');
		} else {
			$("td.binded-to-project").editable('disable');

		}
				
			
		$("td.binded-to-project").editable(function(value, settings) {
			    var returned;
			    
			    var cell = this.parentElement;
			    var id = $("#projects-table").squashTable().getODataId(cell);
			    
				if (value === "yes"){
					bindProjectInPerimeter(id);
					returned = translator.get("squashtm.yesno.true");
				} else {
					unbindProjectInPerimeter (id);
					returned = translator.get("squashtm.yesno.false");
				}
			     return(returned);
			  }, {
				 data   : " {'yes':'" + translator.get("squashtm.yesno.true") + "', 'no' :'"  + translator.get("squashtm.yesno.false") +"'}",
				 type   : 'select',
			     submit  : translator.get("label.Confirm"),
			     cancel : translator.get("label.Cancel"),
			 });
		};
		
		function unbindProjectInPerimeter (id){
			var popup = $("#unbind-project-but-keep-in-perimeter-popup");
			popup.data('entity-id', id);
			popup.confirmDialog('open');
		}
		
		function bindProjectInPerimeter(id){
			var url = routing.buildURL('milestone.bind-projects-to-milestone',config.data.milestone.id); 
			$.ajax({
				url : url,
				type : 'POST',
				data : {Ids : [id]}
			}).success(function() {
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();
			});	
		}
		
		$(function() {
			
			var squashSettings;
			
			if (config.data.canEdit === true){
				initJEditables();
			} else {
				squashSettings = {
						functions:{					
							drawUnbindButton: function(template, cell){
								//do nothing so the unbind button are not displayed
							}	
						}
				};
				
			}
			$("#projects-table").squashTable({"bServerSide":false, fnDrawCallback : drawCallBack}, squashSettings);
			$("#bind-to-projects-table").squashTable({"bServerSide":false}, {});
			basic.init();
			$("#back").click(clickBugtackerBackButton);
			initRenameDialog();
	
		});

		var uncheck = function() {
			$("#bind-to-projects-table").find(":checkbox").prop('checked', false);
		};
		var checkAll = function() {
			$("#bind-to-projects-table").find(":checkbox").prop('checked', true);
		};

		var invertCheck = function() {
			var checked = $("#bind-to-projects-table").find(":checkbox").filter(":checked");
			var unchecked = $("#bind-to-projects-table").find(":checkbox").filter(":not(:checked)");
			checked.each(function() {
				$(this).prop('checked', false);
			});
			unchecked.each(function() {
				$(this).prop('checked', true);
			});
		};

		//unbind project but keep in perimeter 
		
		$("#unbind-project-but-keep-in-perimeter-popup").confirmDialog().on('confirmdialogconfirm', function(){	
			
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = routing.buildURL('milestone.bind-projects-to-milestone', config.data.milestone.id) + "/" + ids.join(',') + "/keep-in-perimeter";	
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();
			});
		});
		
		//Unbind project
		
		var bindedTable = $("#projects-table").squashTable();
		var bindableTable = $("#bind-to-projects-table").squashTable();
		
$("#unbind-project-popup").confirmDialog().on('confirmdialogconfirm', function(){
			
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = routing.buildURL('milestone.bind-projects-to-milestone', config.data.milestone.id) + "/" + ids.join(',');
			var selectedRow = bindedTable.getRowsByIds(ids);
		 
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();
			});
			
			
		});

		$("#unbind-project-button").on('click', function(){
			var ids = bindedTable.getSelectedIds();

			if (ids.length>0){
				var popup = $("#unbind-project-popup");
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
		
		
		
		$("#checkAll").on('click', checkAll);
		$("#uncheckAll").on('click', uncheck);
		$("#invertSelect").on('click', invertCheck);

		$("#bind-project-button").on('click', function() {
			bindProjectDialog.formDialog('open');
		});

		var bindProjectDialog = $("#bind-project-dialog");

		bindProjectDialog.formDialog();

		bindProjectDialog.on('formdialogcancel', function() {
			bindProjectDialog.formDialog('close');
		});
		

	    
		function getCheckedId() {
			$("#bind-to-projects-table").find(":checkbox:checked").parent("td").parent("tr").addClass(
					'ui-state-row-selected');
			var ids = $("#bind-to-projects-table").squashTable().getSelectedIds();
			$("#bind-to-projects-table").squashTable().deselectRows();
			return ids;
		}

 
		bindProjectDialog.on('formdialogconfirm', function() {

			var ids = getCheckedId();
			var url = routing.buildURL('milestone.bind-projects-to-milestone',config.data.milestone.id); 
			$.ajax({
				url : url,
				type : 'POST',
				data : {Ids : ids}
			}).success(function() {
				$('#projects-table').squashTable()._fnAjaxUpdate();
				$('#bind-to-projects-table').squashTable()._fnAjaxUpdate();

				bindProjectDialog.formDialog('close');
			});	
		});
	});
