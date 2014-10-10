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
define([ "jquery", "module", "./ieo-manager", "squash.resizer",
		"./jquery.ieo-control", "jquery.squash.formdialog" ],
		function($, module, OptimizedManager, resizer) {

			function initOpenURLDialog(){
				var openurlDialog = $("#open-address-dialog");
				openurlDialog.formDialog();
				
				openurlDialog.on('formdialogconfirm', function(){
					var url = $('#address-input').val();
					squashtm.ieomanager.fillRightPane(url);
					openurlDialog.formDialog('close');					
				});
				
				$("#open-address-dialog-button").on('click', function(){
					openurlDialog.formDialog('open');
				});
			}
	
			return function() {

				// init the manager

				var settings = module.config();

				var manager = new OptimizedManager(settings);

				// set it in the context
				squashtm = squashtm || {};
				squashtm.ieomanager = manager;

				// init the control
				var control = $("#ieo-control").ieoControl();

				// wire them
				manager.setControl(control);

				// the right panel
				manager.setRightPane($("#ieo-right-panel"));
				
				// init the open url dialog
				initOpenURLDialog();

				// make the panels resizeable

				resizer.init({
					leftSelector : "#ieo-left-panel",
					rightSelector : "#ieo-right-panel"
				});

			};

		});
