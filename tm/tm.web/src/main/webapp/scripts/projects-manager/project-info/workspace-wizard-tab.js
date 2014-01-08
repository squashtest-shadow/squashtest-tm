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
define(
		[ "jquery", "backbone", "jquery.squash.togglepanel", "jqueryui" ],
		function($, Backbone) {
			var WizardPanelView = Backbone.View.extend({

				self : this,

				emptyrowTemplate: undefined,
				datarowTemplate: undefined,
				
				initialize : function() {
					this.$el.togglePanel();
					var body = this.$el.find('tbody');
					this.emptyrowTemplate = body.find('.template-emptyrow').clone().wrap('<div/>').parent().html();
					this.datarowTemplate = body.find('.template-datarow').clone().wrap('<div/>').parent().html();
					body.empty();
					body.removeClass('not-displayed');
				},

				events : {
					'click tr' : 'tickCheckbox',
					'click span.wizard-configure' : 'configureWizard',
					'change input.plugin-enabled' : 'updateModel'
				},

				render : function() {

					var tbody = this.$el.find('table tbody'), 
						model = this.options.model, 
						i = 0, 
						available = this.options.available, // TODO load through ajax on render
						availableLength = available.length, 
						collection = this.collection;

					tbody.empty();
					var rows = $();
					
					if (available.length === 0){
						rows = rows.add(this.emptyrowTemplate());
					}
					else{
						for ( var j = 0; j < availableLength; j++) {
							var item = available[j];
							var newhtml = this.datarowTemplate.replace('{{this.id}}', item.id)
												.replace('{{this.displayableName}}', item.displayableName);
							
							var newRow = $(newhtml);

							// check if this wizard is enabled
							if (collection.get(item.id) !== undefined) {
								this.toggleRowState(newRow);
							}
							rows = rows.add(newRow);

						}
					}

					tbody.append(rows);

				},

				tickCheckbox : function(event) {
					var $checkbox = $(event.currentTarget).find('input.plugin-enabled');
					if (!$(event.target).is('input.plugin-enabled')) {
						var state = $checkbox.prop('checked');
						$checkbox.prop('checked', !state);
						this.updateModel({
							target : $checkbox.get(0)
						});
					}
				},
				
				activateRow : function(row){
					row.find('input.plugin-enabled').prop('checked', true);
					row.find('.wizard-configure').removeClass('disabled-transparent').addClass('cursor-pointer');
				},
				
				deactivateRow : function(row){
					row.find('input.plugin-enabled').prop('checked', false);
					row.find('.wizard-configure').addClass('disabled-transparent').removeClass('cursor-pointer');
				},
				
				toggleRowState : function(row){
					var chbox = row.find('input.plugin-enabled'),
						confspan = row.find('.wizard-configure'),
						enabled = chbox.prop('checked');
					
					if (enabled){
						// deactivate
						this.deactivateRow(row);
					}
					else{
						// activate
						this.activateRow(row);
					}
					
				},

				updateModel : function(event) {

					var $target = $(event.target),
						isSelected = $target.prop('checked'),
						id = $target.parent('td').prev().text(),
						tr = $target.parents('tr');

					if (isSelected) {
						this.collection.add({
							id : id
						});
						this.activateRow(tr);
					} else {
						this.collection.remove(id);
						this.deactivateRow(tr);
					}
					
				},
				
				configureWizard : function(event){

					//we don't want to trigger a click on the 'tr' level
					event.stopImmediatePropagation();
					
					var $target = $(event.currentTarget);

					// open the configuration popup only if the link is enabled
					if ($target.hasClass('disabled-transparent')){
						return;
					}					
					else{
						// let's go configure
						console.log('item enabled : TODO configure it');
					}
				}

			});

			function initWizardTabView(settings) {

				var WizardEnabledCollection = Backbone.Collection.extend({
					url : settings.projectUrl,
					initialize : function() {
						this.on('add', function(model) {
							var self = this;
							var ajaxConf = {
								url : this.url + "/" + model.id + "/",
								type : 'POST',
								dataType : 'text',
								global : false
							// we will handle potential exceptions locally
							};
							$.ajax(ajaxConf).fail(function(xhr) {
								squashtm.notification.handleJsonResponseError(xhr);
								self.remove(model, {
									silent : true
								});
								self.panelView.render();
								return false;
							});
						});
						this.on('remove', function(model) {
							$.ajax({
								url : this.url + "/" + model.id + "/",
								type : 'DELETE'
							});
						});
					}
				});

				var models = $.map(settings.enabledWizards, function(wiz) {
					return new Backbone.Model({
						id : wiz
					});
				});

				var enabledCollection = new WizardEnabledCollection();
				enabledCollection.reset(models, {
					silent : true
				});

				var panelView = new WizardPanelView({
					el : "#workspace-wizards-panel",
					collection : enabledCollection,
					available : settings.availableWizards
				});

				panelView.render();

				enabledCollection.panelView = panelView;

			}

			return initWizardTabView;
		});