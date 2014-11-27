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
require(["common"], function() {
	require(["jquery", "squash.translator", "workspace.routing","squash.configmanager","app/ws/squashtm.notification","squash.dateutils", "jeditable.datepicker",  "squashtable", 
	         "app/ws/squashtm.workspace", 
	         "jquery.squash.formdialog", "jquery.squash.confirmdialog"], 
			function($, translator, routing, confman, notification, dateutils){					
		


	   function getPostDate(localizedDate){
		try{
		var postDateFormat = $.datepicker.ATOM;   
		var date = $.datepicker.parseDate(translator.get("squashtm.dateformatShort.datepicker"), localizedDate);
		var postDate = $.datepicker.formatDate(postDateFormat, date);
		return postDate;
		} catch(err){ return null;}
		}

		
		$(function() {					
			$("#milestones-table").squashTable({"bServerSide":false},{});			
			$('#new-milestone-button').button();		
		});	
		
	
		var dateSettings = confman.getStdDatepicker(); 
		$("#add-milestone-end-date").editable(function(value){
			$("#add-milestone-end-date").text(value);
	    }, {
			type : 'datepicker',
			datepicker : dateSettings,
			name : "value"
		});
		
		this.$textAreas = $("textarea");
		function decorateArea() {
			$(this).ckeditor(function() {
			}, {
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
			var table = $("#milestones-table").squashTable();
			var selectedRow = table.getRowsByIds(ids);
			
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table._fnAjaxUpdate();
			});
			
			
		});

		$("#delete-milestone-button").on('click', function(){
			var ids = $("#milestones-table").squashTable().getSelectedIds();

			if (ids.length>0){
				var popup = $("#delete-milestone-popup");
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
		
		
		
		
	var addMilestoneDialog = $("#add-milestone-dialog");
		
	addMilestoneDialog.formDialog();
		
	
	function getTranslatedStatus(status){
		var i18nstatus = translator.get({value:"milestone.status." + status});
		return  i18nstatus.value;	
	}
	
	function getTranslatedRange(range){
		var i18nrange = translator.get({value:"milestone.range." + range});
		return  i18nrange.value;	
	}
	
	function formatDate(date){
		var format = translator.get("squashtm.dateformatShort");
		var formatedDate = dateutils.format(date, format);
		return dateutils.dateExists(formatedDate, format) ? formatedDate :"";
	}
	function addLinkToLabel(id, label){
		var html = '<a href="' + routing.buildURL('milestone.info', id) +'">' + label + '</a>';
		return html;
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
		}).success(function(data){
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
	
	});			
});		