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


require(["common", ], function(){
	
	require(["jquery", 
	         "app/pubsub", 
	         "app/ws/squashtm.workspace", 
	         "jquery.squash.togglepanel", 
	         "jquery.squash.formdialog"], 
	         function($, pubsub){
		
		pubsub.subscribe('load.toolbar', initRenameButton);
		
		pubsub.subscribe('load.main-panel', initMainPanel);
		
		pubsub.subscribe('load.popups', initPopups);
		
		pubsub.subscribe('load.ready', initEnd);
		
	});

	
	function initRenameButton(){
		$("#rename-ta-server-button").on('click', function(){
			$("#rename-ta-server-popup").formDialog('open');
		});
	}
	
	function initMainPanel(){
		$("#ta-server-info-panel").togglePanel();
	}
	
	
	// the only popup for now is the rename dialog
	function initPopups(){
		
		var conf = squashtm.pageConfiguration;
		
		var dialog = $("#rename-ta-server-popup").formDialog();
		
		dialog.on('formdialogopen', function(){
			var formername = $("#ta-server-name-header").text();
			dialog.find("#rename-ta-server-input").val(formername);
		});
		
		dialog.on('formdialogconfirm', function(){
			
			var name = dialog.find("#rename-ta-server-input").val();
			
			$.ajax({
				url : conf.url+'/name', 
				type : 'post',
				data : {newName : name}
			})
			.success(function(){
				$("#ta-server-name-header").text(name);
				dialog.formDialog('close');
			});
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
	}
	
	function initEnd(){
		$(".unstyled").fadeIn('fast', function(){
			$(this).removeClass('unstyled');
		});		
	}
});
