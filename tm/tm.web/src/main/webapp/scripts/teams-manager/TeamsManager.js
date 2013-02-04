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
define(
		[ "jquery", "backbone", "./TeamsTable", "./NewTeamDialog",  
				"jqueryui" ],
		function($, Backbone, TeamsTable, NewTeamDialog ) {
			var View = Backbone.View
					.extend({
						el : "#team-table-pane",

						initialize : function() {
							this.teamsTable = new TeamsTable();
						},

						events : {
							"click #new-team-button" : "showNewTeamDialog",
						},

						showNewTeamDialog : function(event) {
							var self = this;

							function discard() {
								self.newTeamDialog
										.off("newteam.cancel newteam.confirm");
								self.newTeamDialog.undelegateEvents();
								self.newTeamDialog = null;
							}

							function discardAndRefresh() {
								discard();
								self.teamsTable.refresh();
							}

							self.newTeamDialog = new NewTeamDialog({
								model : {
									name : "",
									description : ""
								}
							});

							self.newTeamDialog.on("newteam.cancel",
									discard);
							self.newTeamDialog.on("newteam.confirm",
									discardAndRefresh);
						},

						
					});

			return View;
		});