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
define(
		[ "jquery", "backbone", "underscore", "jquery.squash.togglepanel", "jqueryui", "jquery.squash.formdialog" ],
		
		function($, Backbone, _) {
			var WizardPanelView = Backbone.View.extend({

				self : this,

				emptyrowTemplate: undefined,
				datarowTemplate: undefined,
				
				initialize : function() {
					this.$el.togglePanel();
					this._initWizardlist();
				},
				
				_initWizardlist : function(){
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
						tr = $target.parents('tr'),
						id = tr.data('pluginid');

					if (isSelected) {
						this.collection.add({
							id : id
						});
						this.activateRow(tr);
						tr.find(".wizard-configure").click();
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
						var id = $target.parents('tr').data('pluginid');
						var dialog = $("#wizard-configure-dialog");
						dialog.data('pluginid', id);
						dialog.formDialog('open');
					}
				}

			});
			
			
			function initConfdialog(settings){
				var dialog = $("#wizard-configure-dialog");
				dialog.formDialog({
					open : function(){
						var $this = $(this);
						
						$this.formDialog('setState','configure');
						
						var conflist = $this.find('.wizard-options');
						conflist.empty();
						
						var id = $this.data('pluginid');
						var url = settings.projectUrl+'/'+id+'/configuration';
						
						$.getJSON(url).done(function(conf){
							if (_.isEmpty(conf)){
								$this.formDialog('setState','noconf');
							}
							else{
								for (var ppt in conf){
									conflist.append("<div class='wizard-option display-table-row'><span class='display-table-cell'>"+ppt+"</span><input class='display-table-cell' type='text' value='"+conf[ppt]+"'/></div>");
								}
								$this.formDialog('setState','configure');
							}
						});
					}
				});
				
				dialog.on('formdialogconfirm', function(){
					var id = dialog.data('pluginid');
					var url = settings.projectUrl+'/'+id+'/configuration';
					var options = {};
					
					var optelts = dialog.find('.wizard-options .wizard-option');
					optelts.each(function(){
						var $this = $(this);
						var key = $this.find('span').text();
						var value = $this.find('input').val();
						options[key] = value;
					});
					
					$.ajax({
						url : url,
						type : 'POST',
						data : JSON.stringify(options),
						contentType  : 'application/json'
					}).done(function(){
						dialog.formDialog('close');
					}).fail(function(json){
						squashtm.notification.handleJsonResponseError(json);
					});
					
				});
				
				dialog.on('formdialogcancel', function(){
					dialog.formDialog('close');
				});
			}

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
				
				initConfdialog(settings);

				panelView.render();

				enabledCollection.panelView = panelView;

			}

			return initWizardTabView;
		});