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

define([ "jquery", "backbone", "jquery.squash.togglepanel", "jqueryui" ], function($, Backbone){
			

	var WizardPanelView = Backbone.View.extend({
				
		self : this,
		
		initialize : function() {
			this.$el.togglePanel();
		},
	
		events : {
			'change input.plugin-enabled' : 'updateModel'
		},
		
		render : function(){
			
			var tbody = this.$el.find('table tbody'),
				model = this.options.model,
				i = 0,
				modelLength = model.length;
			
			tbody.empty();
			
			var rows = $();
			for (var i=0;i<modelLength;i++){
				var item = model.at(i);
				var newRow = $('tr');
				newRow.append($('<td class="not-displayed>'+item.id+'</td>'));
				newRow.append($('<td><input type="checkbox" class="plugin-enabled"/></td>'));
				newRow.append($('<td>'+item.displayableName+'</td>'));
				rows = rows.add(newRow);
			}
			
			tbody.append(rows);
			
		},
		
		updateModel : function(event){
			
			var $target = $(event.target);
			var newValue = $target.prop('checked');
			
			var id = $target.parent('td').prev().text();
			var model = this.collection.get(id).set({enabled : newValue});
		}

	});
	
	
	function initPanel(subSettings){		
		
		var WizardCollection = Backbone.Collection.extend({
			url : subSettings.url,
			initialize : function(){
				this.on('change:enabled', function(model){
					var url = this.url+"/"+model.id+"/";
					var method = (model.attributes.enabled) ? "POST" : "DELETE"
					$.ajax({url : url, type : method});
					//console.log(url+" : "+method);
				});
			}
		});		
		
		var wizardCollection = new WizardCollection();
		
		wizardCollection.add(subSettings.model);
		
		new WizardPanelView({ el : subSettings.selector, collection : wizardCollection });
		
	};
	
	
	function initWizardTabView(settings){
		initPanel({
			url : settings.projectUrl+"/test-case-library-wizards",
			selector : "#test-case-workspace-wizard-panel",
			model : settings.tcWorkspacePlugins
		});
		
		initPanel({
			url : settings.projectUrl+"/requirement-library-wizards",
			selector : "#requirement-workspace-wizard-panel",
			model : settings.reqWorkspacePlugins
		});
		
		initPanel({
			url : settings.projectUrl+"/campaign-library-wizards",
			selector : "#campaign-workspace-wizard-panel",
			model : settings.campWorkspacePlugins
		});	
		
	};

	return initWizardTabView;
});