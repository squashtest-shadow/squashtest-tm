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

define(["jquery",
        "app/pubsub",
		"squash.basicwidgets",
		"contextual-content-handlers",
		"jquery.squash.fragmenttabs",
		"workspace.event-bus",
		"milestones/milestone-panel",
		"jqueryui",
		"jquery.squash.formdialog"],
		function($, pubsub, basic, contentHandlers, Frag, eventBus, milestonePanel){
	
	"use strict";


	
	function initRenameDialog(settings){

		var identity = { resid : settings.testCaseId, restype : "test-cases"  },
			url = settings.urls.testCaseUrl,
			dialog = $("#rename-test-case-dialog");


		dialog.formDialog();

		
		$("#rename-test-case-button").on('click', function(){
			dialog.formDialog('open');
		});

		
		dialog.on( "formdialogopen", function(event, ui) {
			var hiddenRawName = $('#test-case-raw-name');
			var name = $.trim(hiddenRawName.text());
			$("#rename-test-case-input").val(name);
		});
		

		dialog.on('formdialogconfirm', function(){

			var newName = $("#rename-test-case-input").val();

			$.ajax({
				url : url,
				type : "POST",
				dataType : "json",
				data : { 'newName' : newName}
			}).success(function(){
				eventBus.trigger('node.rename', { identity : identity, newName : newName});
				dialog.formDialog('close');
			});

		});

		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});

	}

	function initRenameListener(settings){

		var nameHandler = contentHandlers.getNameAndReferenceHandler();
		nameHandler.identity = { resid : settings.testCaseId, restype : "test-cases" };
		nameHandler.nameDisplay = "#test-case-name";
		nameHandler.nameHidden = "#test-case-raw-name";
		nameHandler.referenceHidden = "#test-case-raw-reference";

	}

	function initFragmentTab(){

		var fragConf = {
			cookie : "testcase-tab-cookie"
		};
		Frag.init(fragConf);
	}

	function initButtons(settings){
		$("#print-test-case-button").on('click', function(){
			window.open(settings.urls.testCaseUrl+"?format=printable", "_blank");
		});
	}


	function init(settings){

		basic.init();
		initButtons(settings);
		initRenameDialog(settings);
		initRenameListener(settings);
		initFragmentTab();
	}


	return {
		init : init
	};

});