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
var squashtm = squashtm || {};
/**
 * Controller for the report workspace page (report-workspace.html) Depends on : -
 * jquery - jquery.squash.buttons.js
 */
define([ "jquery", "app/ws/squashtm.toggleworkspace", "jquery.squash.squashbutton", "jquery.squash.togglepanel" ],
		function($, ToggleWorkspace) {
			

			function findId(name) {
				var idIndex = name.lastIndexOf("-") + 1;
				return name.substring(idIndex);
			}

			function cleanContextual() {
				var contextualList = $(".is-contextual");

				if (contextualList.length === 0) {
					return;
				}

				contextualList.each(function() {
					var jqThis = $(this);
					if (jqThis.hasClass("ui-dialog-content")) {
						jqThis.formDialog("destroy").remove();
					}
				});
			}

			function loadContextualReport(reportItem) {
				cleanContextual();
				$("#contextual-content").html('');

				var reportUrl = $(reportItem).data('href');

				$("#contextual-content").load(reportUrl);
			}

			
			/**
			 * initializes the workspace.
			 * 
			 * @returns
			 */
			function init(options) {
				
				$("#outer-category-frame  .report-item").click(function() {
					loadContextualReport(this);
				});

				ToggleWorkspace.init(options); 

				/* decorate buttons */
				$.squash.decorateButtons();
			}

			squashtm.reportWorkspace = {
				
				loadContextualReport : loadContextualReport,
				init : init
			};

			return squashtm.reportWorkspace;
		});
