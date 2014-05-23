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
	         "squash.configmanager",
	         "app/ws/squashtm.workspace", 
	         "jquery.squash.togglepanel", 
	         "jquery.squash.formdialog",
	         "jquery.squash.jedpassword"], 
	         function($, pubsub, confman){
		
		// ********** function declarations *****************
		
		function initRenameButton(){
			$("#rename-ta-server-button").on('click', function(){
				$("#rename-ta-server-popup").formDialog('open');
			});
		}
		
		function initMainPanel(){
			
			var url = squashtm.pageConfiguration.url;
			
			$("#ta-server-info-panel").togglePanel();
			
			var normalJeditConf = confman.getStdJeditable(),
				ckedJeditConf = confman.getJeditableCkeditor();
			
			var urlConf = $.extend({ name : 'newURL'}, normalJeditConf);
			$("#ta-server-url").editable(url+'/baseURL', urlConf);
			
			var loginConf = $.extend({ name : 'newLogin'}, normalJeditConf);
			$("#ta-server-login").editable(url+'/login', loginConf);
			
			var passwordConf = $.extend({}, { name : 'newPassword' });
			$("#ta-server-password").jedpassword(url+'/password', passwordConf);
			
			var descriptionConf = $.extend({ name : 'newDescription'}, ckedJeditConf);
			$("#ta-server-description").editable(url+'/description', descriptionConf);
			
			$("#ta-server-manual-selection").on('change', function(){
				var checked = $(this).is(':checked');
				$.ajax({
					url : url+'/manualSelection',
					type : 'post',
					data : {manualSelection : checked}
				});
			});
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
		
		
		// **************** init ****************
		
		pubsub.subscribe('load.toolbar', initRenameButton);
		
		pubsub.subscribe('load.main-panel', initMainPanel);
		
		pubsub.subscribe('load.popups', initPopups);
		
		pubsub.subscribe('load.ready', initEnd);
		
		

		
	});

});
