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

define(["jquery", "underscore"], function($, _){
	
	var searchwidget = $.widget("search.searchMultiCascadeFlat", {
		options : {},
		
		_create : function(){
			this._super();
		},
		
		fieldvalue : function(value){
			//case : getter
			if (!value){
				var values = $('.multicascadeflat-secondary', this.element).val();
				return {type : 'LIST', values : values};
			}
			//case : setter
			else{
				var primarySelect = $(".multicascadeflat-primary", this.element),
					secondarySelect = $(".multicascadeflat-secondary", this.element);
				
				primarySelect.find('option').removeAttr('selected');
				secondarySelect.find('option').removeAttr('selected');
				
				if (!!value.values){
					
					_.each(this.options, function(primaryOpt){				
						_.each(primaryOpt.subInput.possibleValues, function(secondaryOpt){
							if (_.contains(value.values, secondaryOpt.code)){
								secondarySelect.find('[value="'+secondaryOpt.code+'"]').prop('selected', true);	
								primarySelect.find('[value="'+primaryOpt.code+'"]').prop('selected', true);
							}					
						});
					});
				}
			}
		},
		
		
		
		hidePrimary : function(code){
			var primarySelect = $(".multicascadeflat-primary", this.element),
				secondarySelect = $(".multicascadeflat-secondary", this.element);			
			
			primarySelect.find("option[value='"+code+"']", this.element).hide();
			for (var pi=0; pi<this.options.length;pi++){
				var primaryOpt = this.options[pi];
				if (primaryOpt.code === code){
					for (var si=0; si < primaryOpt.subInput.possibleValues.length; si++){
						var subopt = primaryOpt.subInput.possibleValues[si];
						secondarySelect.find('option[value="'+subopt.code+'"]').hide();
					}
				}
			}
		},
		
		showPrimary : function(code){
			var primarySelect = $(".multicascadeflat-primary", this.element),
				secondarySelect = $(".multicascadeflat-secondary", this.element);			
		
			primarySelect.find("option[value='"+code+"']", this.element).show();
			for (var pi=0; pi<this.options.length;pi++){
				var primaryOpt = this.options[pi];
				if (primaryOpt.code === code){
					for (var si=0; si < primaryOpt.subInput.possibleValues.length; si++){
						var subopt = primaryOpt.subInput.possibleValues[si];
						secondarySelect.find('option[value="'+subopt.code+'"]').show();
					}
				}
			}
		}
		
	});
	
});