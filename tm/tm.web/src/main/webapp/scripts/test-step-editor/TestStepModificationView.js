/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
define([ "jquery", "backbone", "jquery.squash", "jqueryui",
		"jquery.squash.togglepanel", "jquery.ckeditor", "jeditable",
		"ckeditor", "jeditable.ckeditor", "jquery.squash.jeditable" ],
		function($, Backbone) {
			var editTCS = squashtm.app.editTCS;
			/*
			 * Defines the controller for the custom fields table.
			 */
			var TestStepModificationView = Backbone.View.extend({
				el : "#information-content",
				initialize : function() {
					this.configureTogglePanels();
					this.configureEditables();
					
					this.configureButtons();
				},

				events : {
					"click #previous-test-step-button" : "goPrevious",
					"click #next-test-step-button" : "goNext"
				},

				goPrevious : function() {
					if (editTCS.previousId) {
						document.location.href = squashtm.app.contextRoot
								+ "test-steps/" + editTCS.previousId;
					}
				},
				goNext : function() {
					if (editTCS.nextId) {
						document.location.href = squashtm.app.contextRoot
								+ "test-steps/" + editTCS.nextId;
					}

				},
				configureButtons : function() {
					$.squash.decorateButtons();
				},

				configureTogglePanels : function() {
					var informationSettings = {
						initiallyOpen : true,
						title : editTCS.informationPanelLabel
					};
					this.$("#test-step-info-panel").togglePanel(
							informationSettings);
				},

				configureEditables : function() {
					if(!editTCS.writable){
						return;
					}
					var settings = {
						url : editTCS.stepURL,
					};
					$.extend(settings, squashtm.app.ckeditorSettings);
					$('#test-step-action').richEditable(settings).addClass(
							"editable");
					$('#test-step-expected-result').richEditable(settings)
							.addClass("editable");
				},

			});
			return TestStepModificationView;
		});