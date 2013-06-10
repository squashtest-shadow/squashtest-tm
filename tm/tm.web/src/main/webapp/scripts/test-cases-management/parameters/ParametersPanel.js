/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

define([ "jquery", "backbone", "./ParametersTable", "./NewParameterDialog",
		"jquery.squash.confirmdialog", "jquery.squash.togglepanel" ],
		function($, Backbone, ParametersTable, NewParameterDialog) {
			var teamMod = squashtm.app.teamMod;
			var ParametersPanel = Backbone.View.extend({
				
				el : "#parameters-panel-container",
				
				initialize : function() {
				this.settings = this.options.settings;
					this.language = this.settings.language;
					this.makeTogglePanel();
					this.table = new ParametersTable({settings : this.settings, parentTab : this.options.parentTab});
					this.showNewParameterDialog = $.proxy(
									this._showNewParameterDialog, this);
					this.configureButtons();
				},
				
				events : {
					
				},
				
				makeTogglePanel : function(){
					var self = this;
					var panelSettings = {
							initiallyOpen : true,
							title : self.language.parametersPanelTitle
						};
					this.$("#parameters-panel").togglePanel(panelSettings);
				},
				
				configureButtons : function() {
					var self = this;
					// ===============toogle buttons=================
					// this line below is here because toggle panel
					// buttons cannot be bound with the 'events'
					// property of Backbone.View.
					// my guess is that the event is bound to the button
					// before it is moved from it's "span.not-displayed"
					// to the toggle panel header.
					// TODO change our way to make toggle panels buttons
					// =============/toogle buttons===================
					this.$("#add-parameter-button").on('click',	self.showNewParameterDialog);
				},
				
				_showNewParameterDialog : function(event) {
					var self = this;

					function discard() {
						self.newParameterDialog.off("newParameter.cancel newParameter.confirm");
						self.newParameterDialog.undelegateEvents();
						self.newParameterDialog = null;
					}

					function discardAndRefresh() {
						discard();
						self.table.refresh();
					}

					self.newParameterDialog = new NewParameterDialog({settings : self.settings, model : {name:"", description:""}});
					self.newParameterDialog.on("newParameter.cancel", discard);
					self.newParameterDialog.on("newParameter.confirm", discardAndRefresh);
				}
			});
			return ParametersPanel;
});