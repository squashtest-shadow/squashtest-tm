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
			'click tr' : 'tickCheckbox',
			'change input.plugin-enabled' : 'updateModel'
		},
		
		render : function(){
			
			var tbody = this.$el.find('table tbody'),
				model = this.options.model,
				i = 0,
				available = this.options.available,	//TODO : load through ajax on render
				availableLength = available.length,
				collection = this.collection;
			
			tbody.empty();
			
			var rows = $();
			for (var i=0;i<availableLength;i++){
				
				var item = available[i];
				var newRow = $('<tr class="cursor-arrow"/>');
				
				newRow.append($('<td class="not-displayed">'+item.id+'</td>'));
				newRow.append($('<td class="centered narrow"><input type="checkbox" class="plugin-enabled"/></td>'));
				newRow.append($('<td>'+item.displayableName+'</td>'));
				
				if (collection.get(item.id)!==undefined){
					newRow.find('input.plugin-enabled').prop('checked', true);
				}
				rows = rows.add(newRow);
				
			}
			
			tbody.append(rows);
			
		},
		
		tickCheckbox : function(event){
			var $checkbox = $(event.currentTarget).find('input.plugin-enabled');
			if (! $(event.target).is('input.plugin-enabled')){
				var state = $checkbox.prop('checked');
				$checkbox.prop('checked', !state);
				this.updateModel({target : $checkbox.get(0)});
			}
		},
		
		updateModel : function(event){
			
			var $target = $(event.target);
			var isSelected = $target.prop('checked');			
			var id = $target.parent('td').prev().text();

			if (isSelected){
				this.collection.add({id : id});
			}
			else{
				this.collection.remove(id);
			}
		}

	});
	
	
	function initWizardTabView(settings){	
		

		
		var WizardEnabledCollection = Backbone.Collection.extend({
			url : settings.projectUrl,
			initialize : function(){
				this.on('add', function(model){
					$.post(this.url+"/"+model.id+"/");
				});
				this.on('remove', function(model){
					$.ajax({url : this.url+"/"+model.id+"/", type : 'DELETE'});
				});
			}			
		});
				
		var models = $.map(settings.enabledWizards, function(wiz){
			return new Backbone.Model({id : wiz});
		});
		
		var enabledCollection = new WizardEnabledCollection();
		enabledCollection.reset(models, {silent : true});
				
		new WizardPanelView({ 
			el : "#workspace-wizards-panel", 
			collection : enabledCollection,
			available : settings.availableWizards
		}).render();
		
	};


	return initWizardTabView;
});