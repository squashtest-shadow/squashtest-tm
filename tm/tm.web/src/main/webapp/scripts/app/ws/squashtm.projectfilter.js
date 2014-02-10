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
/**
 * requires : * jquery.squash.projectpicker.js
 */
var squashtm = squashtm || {};

define([ "jquery", "jquery.squash.projectpicker" ],
		function($) {
			var popupSelector = "#project-filter-popup";
			var popupOpener = "#menu-project-filter-link";
			var projectFilterUrl;

			function extractId(strDomId) {
				var idTemplate = "project-checkbox-";
				var templateLength = idTemplate.length;
				var extractedId = strDomId.substring(templateLength);
				return extractedId;
			}

			function getSelectedProjectIds(containerId) {
				var selectedBoxes = $("#dialog-settings-filter-projectlist .project-checkbox:checked");
				var zeids = [];
				var i;

				for (i = 0; i < selectedBoxes.length; i++) {
					var jqBox = $(selectedBoxes[i]);

					zeids.push(extractId(jqBox.attr('id')));
				}

				return zeids;
			}

			function newFilterSuccess() {
				$(popupSelector).projectPicker('close');
				window.location.reload();
			}

			/**
			 * code managing the data transmissions
			 */
			function sendNewFilter() {
				var ids = getSelectedProjectIds();
				$.post(projectFilterUrl, {
					projectIds : ids
				}, newFilterSuccess);

			}

			function init() {
				projectFilterUrl = $("#project-filter-popup").data('url');

				var picker = $(popupSelector).projectPicker({
					url : projectFilterUrl, 
					loadOnce : "never",
					width : 400,
					confirm : sendNewFilter
				});

				$(popupOpener).click(function() {
					picker.projectPicker("open");
				});
				
				$("#menu-toggle-filter-ckbox").click(function(){
					
					function postStatus(enabled){
						$.post(squashtm.app.contextRoot+'/global-filter/filter-status', { isEnabled : enabled })
						.done(function(){
							window.location.reload();
						});
					}
					
					if ($(this).is(':checked')){
						postStatus(true);
					}
					else{
						postStatus(false);
					}
				});
			}
			

			/**
			 * public module
			 */
			squashtm.projectfilter = {
				init : init
			};

			return squashtm.projectfilter;
		});
