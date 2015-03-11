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
require(["common"], function(){
	require(["app/pubsub", "backbone.wreqr", 'module', "jquery", "squash.translator", "workspace.routing","squash.configmanager","squash.dateutils", "milestone-manager/MilestoneFeatureSwitch",
	         "jeditable.datepicker",  "squashtable", "app/ws/squashtm.workspace", "jquery.squash.formdialog", "jquery.squash.confirmdialog"],
			function(ps, Wreqr, module, $, translator, routing, confman, dateutils, MilestoneFeatureSwitch){
		"use strict";

		squashtm = squashtm || {};
		squashtm.vent = squashtm.vent || new Wreqr.EventAggregator();

		var config = module.config();

		function getPostDate(localizedDate) {
			try {
				var postDateFormat = $.datepicker.ATOM;
				var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
				var postDate = $.datepicker.formatDate(postDateFormat, date);
				return postDate;

			} catch (err) {
				return null;
			}
		}

		function setActionsEnabled(enabled) {
			$(".milestone-dep").prop("disabled", !enabled);
		}

	ps.subscribe("loaded.milestoneFeatureSwitch", function() {
		console.log("loaded.milestoneFeatureSwitch");
		new MilestoneFeatureSwitch();
	});

	squashtm.vent.on("milestonefeatureswitch:activating milestonefeatureswitch:deactivating", function(event) {
		// prevents performing ops on milestones while the feature state is being changed
		setActionsEnabled(false);
	});

	squashtm.vent.on("milestonefeatureswitch:activated", function(event) {
		setActionsEnabled(true);
	});
	squashtm.vent.on("milestonefeatureswitch:deactivated", function(event) {
		setActionsEnabled(false);
		$table()._fnAjaxUpdate();
	});

	function $table() {
		return $("#milestones-table").squashTable();
	}

	$(function() {

		var squashSettings = {
					functions:{
						drawDeleteButton: function(template, cells){

							$.each(cells, function(index, cell) {
								var row = cell.parentNode; // should be the tr
								var id = milestoneTable.getODataId(row);
								var $cell = $(cell);

								if (_.contains(config.data.editableMilestoneIds, id)){
									$cell.html(template);
									$cell.find('a').button({
										text : false,
										icons : {
											primary : "ui-icon-trash"
										}
									});
								}
							});
						}
					}
			};

			var milestoneTable = $("#milestones-table").squashTable({"bServerSide":false},squashSettings);
		});


		var dateSettings = confman.getStdDatepicker();
		$("#add-milestone-end-date").editable(function(value){
			$("#add-milestone-end-date").text(value);
	    }, {
			type : 'datepicker',
			datepicker : dateSettings,
			name : "value"
		});

		$("#clone-milestone-end-date").editable(function(value){
			$("#clone-milestone-end-date").text(value);
	    }, {
			type : 'datepicker',
			datepicker : dateSettings,
			name : "value"
		});

		this.$textAreas = $("textarea");

		function decorateArea() {
			$(this).ckeditor(function() {}, {
				customConfig : squashtm.app.contextRoot + "/styles/ckeditor/ckeditor-config.js",
				language : squashtm.app.ckeditorLanguage
			});
		}

		this.$textAreas.each(decorateArea);


		$("#delete-milestone-popup").confirmDialog().on('confirmdialogconfirm', function(){

			var $this = $(this);
			var id = $this.data('entity-id');
			var ids = ( !! id) ? [id] : id ;
			var url = squashtm.app.contextRoot+'/administration/milestones/'+ ids.join(",");
			var table = $table();
			//var selectedRow = table.getRowsByIds(ids);

			$.ajax({
				url : url,
				type : 'delete'
			}).done(function(){
				table._fnAjaxUpdate();
			});


		});

		$("#delete-milestone-button").on('click', function(){
			var ids = $table().getSelectedIds();

			if (ids.length>0){
				var popup = $("#delete-milestone-popup");
				popup.data('entity-id', ids);
				popup.confirmDialog('open');
			}
			else{
				warningWithTranslation ('message.EmptyTableSelection');
			}
		});


	var addMilestoneDialog = $("#add-milestone-dialog");

	addMilestoneDialog.formDialog();


	function formatDate(date){
		var format = translator.get("squashtm.dateformatShort");
		var formatedDate = dateutils.format(date, format);
		return dateutils.dateExists(formatedDate, format) ? formatedDate :"";
	}


	addMilestoneDialog.on('formdialogconfirm', function(){
		var url = routing.buildURL('administration.milestones');
		var params = {
			label: $( '#add-milestone-label' ).val(),
			status: $( '#add-milestone-status' ).val(),
			endDate: getPostDate($( '#add-milestone-end-date' ).text()),
			description: $( '#add-milestone-description' ).val()
		};
		$.ajax({
			url : url,
			type : 'POST',
			dataType : 'json',
			data : params
		}).success(function(id){
			config.data.editableMilestoneIds.push(id);
			$('#milestones-table').squashTable()._fnAjaxUpdate();
			addMilestoneDialog.formDialog('close');
		});

	});

	addMilestoneDialog.on('formdialogcancel', function(){
		addMilestoneDialog.formDialog('close');
		});

	$('#new-milestone-button').on('click', function(){
		addMilestoneDialog.formDialog('open');
	});

	//Clone milestone
	var cloneMilestoneDialog = $("#clone-milestone-dialog");

	cloneMilestoneDialog.formDialog();


	$('#clone-milestone-button').on('click', function(){

		var ids = $table().getSelectedIds();
		if (ids.length>1){
			warningWithTranslation ('message.milestone.cantclonemultiple');
		} else if (ids.length == 1) {

			var mil = $table().getDataById(ids[0]);
			var trans = translator.get({
				statusFinished : "milestone.status.FINISHED",
				statusInProgress :"milestone.status.IN_PROGRESS"
				});
			if (mil.status == trans.statusFinished || mil.status == trans.statusInProgress){
			cloneMilestoneDialog.data('entity-id', ids);
			cloneMilestoneDialog.formDialog('open');
			} else {
				warningWithTranslation('message.milestone.invalidclonestatus');
			}


		} else {
			warningWithTranslation ('message.milestone.cantclonenothing');
		}
	});


	function warningWithTranslation(errorKey){
		var warn = translator.get({
			errorTitle : 'popup.title.Info',
			errorMessage : errorKey
		});
		$.squash.openMessage(warn.errorTitle, warn.errorMessage);
	}

	cloneMilestoneDialog.on('formdialogcancel', function(){
		cloneMilestoneDialog.formDialog('close');
		});

	cloneMilestoneDialog.on('formdialogconfirm', function(){
		var $this = $(this);
		var motherId  = $this.data('entity-id');
		var url = routing.buildURL('administration.milestones.clone', motherId);
		var params = {
				label: $( '#clone-milestone-label' ).val(),
				endDate: getPostDate($( '#clone-milestone-end-date' ).text()),
				description: $( '#clone-milestone-description' ).val(),
				bindToRequirements : cloneMilestoneDialog.find("input:checkbox[name='bindToRequirements']").prop("checked"),
				bindToTestCases : cloneMilestoneDialog.find("input:checkbox[name='bindToTestCases']").prop("checked"),
				bindToCampaigns : cloneMilestoneDialog.find("input:checkbox[name='bindToCampaigns']").prop("checked"),
			};
		$.ajax({
			url : url,
			type : 'POST',
			data : params
		}).success(function(id){
			config.data.editableMilestoneIds.push(id);
			$('#milestones-table').squashTable()._fnAjaxUpdate();
			cloneMilestoneDialog.formDialog('close');
		});
		});


	var uncheckCloneParam = function() {
		cloneMilestoneDialog.find(":checkbox").prop('checked', false);
	};
	var checkAllCloneParam = function() {
		cloneMilestoneDialog.find(":checkbox").prop('checked', true);
	};

	$("#checkAll").on('click', checkAllCloneParam);
	$("#uncheckAll").on('click', uncheckCloneParam);

	//Synchronize
	$("#synchronize-milestone-button").on('click', function(){
		var table = $table();
		var ids = table.getSelectedIds();
		//BEWARE lot's of check incoming
		if (ids.length < 2) {
			//error can't select less than 2
			warningWithTranslation('message.milestone.synchronize.notenought');
		} else if (ids.length > 2){
			//error can't select more than 2
			warningWithTranslation('message.milestone.synchronize.toomuch');
		} else {
			//maybe it's ok... let's see
			var mil1 = table.getDataById(ids[0]);
			var mil2 = table.getDataById(ids[1]);
			synchronizeMilestoneDialog.data('mil1', mil1);
			synchronizeMilestoneDialog.data('mil2', mil2);
			var trans = translator.get({
				rangeGlobal : "milestone.range.GLOBAL",
				statusInProgress :"milestone.status.IN_PROGRESS"
				});

			if (mil1.status == trans.statusInProgress || mil2.status == trans.statusInProgress){
				// you need at least one milestone in progress to synchronize

				if (config.data.isAdmin){
					//ok you're admin you can skip some additional check
					configAdminSynchroPopup();
					checkFirstRadio();
					synchronizeMilestoneDialog.formDialog('open');

				} else {
					//too bad you're not admin, you have to pass some more check...
					if (mil1.range == trans.rangeGlobal && mil2.range == trans.rangeGlobal){
						//you loose you're not admin and want to synchronize 2 global milestone
						warningWithTranslation('message.milestone.synchronize.wrongrange');
					} else if (mil1.range == trans.rangeGlobal && mil2.status != trans.statusInProgress || mil2.range == trans.rangeGlobal && mil1.status != trans.statusInProgress ) {
						//you have selected one global and a restricted non in progress milestone...too bad you loose again !

					} else {
						//You're still here ?? ok you can now have your pop up !
						configNonAdminSynchroPopup();
						checkFirstRadio();
						allowPerimeterOrNot();
						synchronizeMilestoneDialog.formDialog('open');

					}
				}

			} else {
				// 2 milestone not in progress, you loose again
				warningWithTranslation('message.milestone.synchronize.wrongstatus');
			}
		}

		function configAdminSynchroPopup(){
			var mil1 = synchronizeMilestoneDialog.data('mil1');
			var mil2 = synchronizeMilestoneDialog.data('mil2');
			$("#mil1").attr("disabled", mil1.status != trans.statusInProgress);
			$("#mil2").attr("disabled", mil2.status != trans.statusInProgress);
			$("#union").attr("disabled", mil1.status != trans.statusInProgress || mil2.status != trans.statusInProgress);
			$("#perim").attr("disabled", true);
			writeMilestonesLabel();

		}

		function configNonAdminSynchroPopup(){
			var mil1 = synchronizeMilestoneDialog.data('mil1');
			var mil2 = synchronizeMilestoneDialog.data('mil2');
			var mil1CantBeTarget = mil1.status != trans.statusInProgress ||  mil1.range == trans.rangeGlobal;
			var mil2CantBeTarget = mil2.status != trans.statusInProgress ||  mil2.range == trans.rangeGlobal;
			$("#mil1").attr("disabled", mil1CantBeTarget);
			$("#mil2").attr("disabled", mil2CantBeTarget);
			$("#union").attr("disabled", mil1CantBeTarget || mil2CantBeTarget);
			writeMilestonesLabel();
		}

		function writeMilestonesLabel(){
			var msg = translator.get({
				mil:"label.milestone.synchronize.target",
				union:"label.milestone.synchronize.union"
			});


			$("#mil1Label").text(msg.mil.split('"{0}"').join(mil1.label).split('"{1}"').join(mil2.label));
			$("#mil2Label").text(msg.mil.split('"{0}"').join(mil2.label).split('"{1}"').join(mil1.label));
			$("#unionLabel").text(msg.union.split('"{0}"').join(mil1.label).split('"{1}"').join(mil2.label));
			greyTextForDisabledLabel($("#mil1"), $("#mil1Label"));
			greyTextForDisabledLabel($("#mil2"), $("#mil2Label"));
			greyTextForDisabledLabel($("#union"),$("#unionLabel"));

		}

		function greyTextForDisabledLabel( radioButtonSelector,labelSelector){
			$radioButtonSelector = $(radioButtonSelector);
			$labelSelector = $(labelSelector);
			if ($radioButtonSelector.attr("disabled")){
				$labelSelector.addClass("nota-bene");
			} else {
				$labelSelector.removeClass("nota-bene");
			}

		}

		$("#mil1").on('change', allowPerimeterOrNot);
		$("#mil2").on('change', allowPerimeterOrNot);
		$("#union").on('change', allowPerimeterOrNot);

		function allowPerimeterOrNot(){
			var mil1 = synchronizeMilestoneDialog.data('mil1');
			var mil2 = synchronizeMilestoneDialog.data('mil2');

			if (!config.data.isAdmin){
				//admin don't have the perimeter checkbox
			$("#perim").attr("disabled", false);

			if ($("#union").prop('checked')){
				$("#perim").attr("disabled", true);
			}

			if ($("#mil1").prop('checked') && config.data.currentUser != mil1.owner){
				$("#perim").attr("disabled", true);
			}

			if ($("#mil2").prop('checked') && config.data.currentUser != mil2.owner){
				$("#perim").attr("disabled", true);
			}

			}
		}

		function checkFirstRadio(){

			if ($("#mil1").attr("disabled")){
				$("#mil2").prop('checked', true);
			} else {
				$("#mil1").prop('checked', true);
			}
		}


	});

	var synchronizeMilestoneDialog = $("#synchronize-milestone-dialog");
	synchronizeMilestoneDialog.formDialog();

	synchronizeMilestoneDialog.on('formdialogcancel', function(){
		synchronizeMilestoneDialog.formDialog('close');
		});

	synchronizeMilestoneDialog.on('formdialogconfirm', function(){
		var mil1 = synchronizeMilestoneDialog.data('mil1');
		var mil2 = synchronizeMilestoneDialog.data('mil2');
		$.ajax({
			url : routing.buildURL("milestone.synchronize" , mil1["entity-id"], mil2["entity-id"]),
			type : 'POST',
			data : {extendPerimeter: $("#perim").prop("checked"),
				isUnion:$("#union").prop("checked")}
		});



		synchronizeMilestoneDialog.formDialog('close');
		});
	});
});
