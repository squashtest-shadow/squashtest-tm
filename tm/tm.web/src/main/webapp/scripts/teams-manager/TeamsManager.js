/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define([ "jquery", "backbone", "./TeamsTable", "./NewTeamDialog", "jqueryui" ], function($, Backbone, TeamsTable,
		NewTeamDialog) {
	var View = Backbone.View.extend({
		el : "#team-table-pane",

		initialize : function() {
			this.teamsTable = new TeamsTable();

			this.newTeamDialog = new NewTeamDialog({
				model : {
					name : "",
					description : ""
				}
			});

			this.listenTo(this.newTeamDialog, "newteam.confirm", $.proxy(this.teamsTable.refresh, this.teamsTable));			
		},

		events : {
			"click #new-team-button" : "showNewTeamDialog"
		},

		showNewTeamDialog : function(event) {
			this.newTeamDialog.show();
		}

	});

	return View;
});