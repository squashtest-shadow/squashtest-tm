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
define([ "jquery","app/ws/squashtm.notification", "jquery.squash.formdialog" ],
		function($, WTF) {
		
// *************************************** BindPopup **********************************************

			var BindPopup = Backbone.View.extend({

				el : "#ta-projects-bind-popup",

				initialize : function(conf) {
					// properties
					this.selectedServerId = conf.TAServerId;
					this.serverHasChanged = true;
					// initialize
					this.$el.formDialog({
						height : 500
					});
					this.fatalError = this.$(".ta-projectsadd-fatalerror").popupError();
					this.error = this.$(".ta-projectsadd-error").popupError();
					// methods bound to this
					this.manageFatalError = $.proxy(this._manageFatalError, this);
					this.onChangeServerConfirmed = $.proxy(this._onChangeServerConfirmed, this);
					// event listening
					this.listenTo(self.popups.confirmChangePopup, "confirmChangeServerPopup.confirm.success",
							self.onChangeServerConfirmed);
				},

				events : {
					"formdialogconfirm" : "confirm",
					"formdialogcancel" : "cancel",
					"formdialogopen" : "open"
				},
				_onChangeServerConfirmed : function(newSelectedServer){
					if(newSelectedServer == this.selectedServerId){
						return;
					}else{
						this.selectedServerId = newSelectedServer;
						this.serverHasChanged = true;
					}
				},
				open : function() {
					var self = this;
					this.$el.formDialog('setState', 'pleasewait');
					if(this.serverHasChanged){
						$.ajax({
							url : squashtm.app.contextRoot + "/test-automation-servers/" + self.selectedId+"/available-ta-projects", 
							type : "get",
							
						}).done(self.buildAndDisplayProjectList).fail(self.manageFatalError);
					}
				},
				_manageFatalError : function(json){
					var message = WTF.getErrorMessage(jsonError);
					this.fatalError.find('span').text(message);
					this.fatalError.popupError('show');
				},
				_buildAndDisplayProjectList : function(json){
					this.buildProjecList(json);
					this.bindProjectListEvents();
					this.$el.formDialog('setState', 'main');
				},
				buildProjectList : function(projectList){
					var tablePanel = this.$(".ta-projectsadd-listdiv");
					tablePanel.empty();

					var i = 0;
					var rows = $();

					for (i = 0; i < json.length; i++) {
						var row = this.newRow(json[i]);
						rows = rows.add(row);
					}

					rows.filter("tr:odd").addClass("odd");
					rows.filter("tr:even").addClass("even");

					tablePanel.append(rows);
				},
				bindProjectListEvents : function(){
					var self = this;
					this.$(".ta-projecsadd-listdiv input:checkbox").change(function(event){
						var $checkbox = $(event.target);
						var $row = $checkbox.parents("tr")[0];
						var $tmLabelCell = $row.find("td.ta-project-tm-label");
						self.tmLabelEditable($tmLabelCell,$checkbox.is(":checked") );
						
					});
				},
				newRow : function(jsonItem) {
					var source = $("#default-item-tpl").html();
					var template = Handlebars.compile(source);
					var row = template(jsonItem);
					return row;
				},

				tmLabelEditable($tmLabelCell, editable){
					if(editable){
						$tmLabelCell.find("input").show();
						$tmLabelCell.addClass("edit-state");
					}else{
						$tmLabelCell.find("input").hide();
						$tmLabelCell.removeClass("edit-state");
					}
				},

				confirm : function() {
					this.trigger("bindTAProjectPopup.confirm");
					alert('Confirmed !');
				},
				cancel : function() {
					this.trigger("bindTAProjectPopup.cancel");
					this.$el.formDialog('close');
				},
				show : function() {
					this.$el.formDialog("open");

				}

			});
			return BindPopup;
});
