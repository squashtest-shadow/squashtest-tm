/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
require([ "common", ], function() {

	require([ "jquery", "app/pubsub", "jeditable.simpleJEditable", "squash.configmanager", "app/ws/squashtm.workspace",
			"jquery.squash.togglepanel", "jquery.squash.formdialog", "jquery.squash.jedpassword",
			"jquery.squash.jeditable" ], function($, pubsub, SimpleJEditable, confman) {

		// ********** function declarations *****************

		function initRenameButton() {
			$("#rename-ta-server-button").on('click', function() {
				$("#rename-ta-server-popup").formDialog('open');
			});
		}

		function initAddDeleteButtons() {
			$("#add-user-automation-server").on('click', function() {
      				$("#add-user-automation-server-popup").formDialog('open');
      			});
			$("#delete-user-automation-server").on('click', function() {
				$("#remove-user-automation-server-confirm-dialog").formDialog('open');
			});
		}





		function initMainPanel() {

			var url = squashtm.pageConfiguration.url;

			new SimpleJEditable({
				targetUrl : url + '/baseURL',
				componentId : "ta-server-url",
				jeditableSettings : {
					callback : function(value, settings){
						$(this).siblings("#ta-server-url-link").attr("href", value);
					}
				}
			});

			new SimpleJEditable({
				targetUrl : url + '/login',
				componentId : "ta-server-login"
			});

			$("#ta-server-password").jedpassword(url + '/password', {
				name : 'value'
			});
			$("#ta-server-password").addClass("editable");

			var richEditSettings = confman.getJeditableCkeditor();
			richEditSettings.url = url + '/description';
			$("#ta-server-description").richEditable(richEditSettings).addClass("editable");

			$("#ta-server-manual-selection").on('change', function() {
				var checked = $(this).is(':checked');
				$.ajax({
					url : url + '/manualSelection',
					type : 'post',
					data : {
						value : checked
					}
				});
			});
		}

		// the only popup for now is the rename dialog
		function initPopups() {

			var conf = squashtm.pageConfiguration;

			var dialog = $("#rename-ta-server-popup").formDialog();

			dialog.on('formdialogopen', function() {
				var formername = $("#ta-server-name-header").text();
				dialog.find("#rename-ta-server-input").val(formername);
			});

			dialog.on('formdialogconfirm', function() {

				var name = dialog.find("#rename-ta-server-input").val();

				$.ajax({
					url : conf.url + '/name',
					type : 'post',
					data : {
						value : name
					}
				}).success(function() {
					$("#ta-server-name-header").text(name);
					dialog.formDialog('close');
				});
			});

			dialog.on('formdialogcancel', function() {
				dialog.formDialog('close');
			});


			dialog = $("#add-user-automation-server-popup");
			dialog.formDialog();

			dialog.on('formdialogopen', function() {

				dialog.formDialog('setState', 'loading');
				$.getJSON("${permissionPopupUrl}").done(function(json){
					if (json.length === 0) {
						dialog.formDialog('setState', "allbound");
					}
					else {
						dialog.data('source', json);
						$("#party-input-user").autocomplete({
							source : json
						});
						dialog.formDialog('setState', 'normal');
					}
				});
				});

			dialog.on('formdialogcancel', function() {
				dialog.formDialog('close');
			});

			dialog.on('formdialogconfirm', function() {

			console.log("ouverture popup ajout")

				var partyname = $("#party-input-user").val();
				var permission = $("#permission-input-user").val();
				var valideselection = $.grep(dialog.data('source'), function(e) {return (e.label === partyname);});

				if (valideselection.length === 1){
					var partyId = valideselection[0].id;
					var url = squashtm.app.contextRoot+"/administration/test-automation-servers/${serverId}/permissions/"+permission+"/";
					dialog.formDialog('setState', 'loading');
					$.ajax({
						url : url,
						type : 'PUT',
						success : function() {
							$("#test-automation-server-users").squashTable().refresh();
							dialog.formDialog('close');
						}
					})
				}else {
					dialog.formDialog('setState', 'noselect');
				}

			});

			dialog = $("#remove-user-automation-server-confirm-dialog");
			dialog.formDialog();

			dialog.on('formdialogopen', function() {

			});
		}

		function initEnd() {
			$(".unstyled").fadeIn('fast', function() {
				$(this).removeClass('unstyled');
			});
		}

		// **************** init ****************

		pubsub.subscribe('load.toolbar', initRenameButton);
		pubsub.subscribe('load.toolbarButtons', initAddDeleteButtons);


		pubsub.subscribe('load.main-panel', initMainPanel);

		pubsub.subscribe('load.popups', initPopups);

		pubsub.subscribe('load.ready', initEnd);

	});

});
