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

	define([ 'module', "jquery", "squash.translator", "squash.basicwidgets", "jeditable.selectJEditable",
			"squash.configmanager", "workspace.routing", "jquery.squash.formdialog", "jeditable.datepicker", "squashtable" ], function(
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

		basic.init();
		$("#back").click(clickBugtackerBackButton);
		initRenameDialog();

		$(function() {
			$("#projects-table").squashTable({}, {});
			$("#bind-to-projects-table").squashTable({}, {});
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

		//Unbind project
$("#unbind-project-popup").confirmDialog().on('confirmdialogconfirm', function(){
			
			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = routing.buildURL('milestone.bind-projects-to-milestone', config.data.milestone.id) + "/" + ids.join(',');
			var table = $("#projects-table").squashTable();
			
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table.refresh();
			});
			
			
		});

		$("#unbind-project-button").on('click', function(){
			var ids = $("#projects-table").squashTable().getSelectedIds();

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
				$('#projects-table').squashTable().refresh();
				$('#bind-to-projects-table').squashTable().refresh();
				bindProjectDialog.formDialog('close');
			});	
		});
	});
