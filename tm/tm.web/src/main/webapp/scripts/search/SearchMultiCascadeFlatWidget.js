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

define(["jquery", "underscore", "jqueryui"], function($, _){
	
	var searchwidget = $.widget("search.searchMultiCascadeFlatWidget", {
		options : {},
		
		_primarySelect : function(){
			return $(".multicascadeflat-primary", this.element);
		},
		
		_secondarySelect : function(){
			return $(".multicascadeflat-secondary", this.element);	
		},
		
		_create : function(){
			this._super();
			var self = this;
			
			var primarySelect = this._primarySelect(),
				secondarySelect = this._secondarySelect();			
			
			// add the on change handlers on the primary select
			primarySelect.on('change', function(){
				self.update();
			});
			
			// add some dots as separators between items belonging to different lists
			for (var i=0; i< this.options.lists.length -1 ; i++){
				var items= this.options.lists[i].subInput.possibleValues;
				var lastCode = items[items.length-1].code;
				
				secondarySelect.find('option[value="'+lastCode+'"]').css('border-bottom-style', 'dotted');
			}
		},
		
		fieldvalue : function(value){
			//case : getter
			if (!value){
				var values = this._secondarySelect()
								 .find('option')
								 .filter(':visible:selected')
								 .map(function(i,e){ return e.value;})
								 .get();
				
				return {type : 'LIST', values : values};
			}
			//case : setter
			else{
				var primarySelect = this._primarySelect(),
					secondarySelect = this._secondarySelect();
				
				primarySelect.find('option').removeAttr('selected');
				secondarySelect.find('option').removeAttr('selected');
				
				if (!!value.values){
					
					_.each(this.options.lists, function(primaryOpt){				
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
		
		update : function(){
			var select = this._primarySelect().get(0);
			for (var i=0; i < select.length; i++){
				var opt = select[i];
				if (opt.selected){
					this.showSecondaryFrom(opt.value);
				}
				else{
					this.hideSecondaryFrom(opt.value);
				}				
			}			
		},
		
		showSecondaryFrom : function(primaryCode){
			this._loopAndApplySecondaryFrom(primaryCode, "show");
		},
		
		hideSecondaryFrom : function(primaryCode){
			this._loopAndApplySecondaryFrom(primaryCode, "hide");
		},
		
		hideAll : function(){
			this._primarySelect().find('option').hide();
			this._secondarySelect().find('option').hide();
		},
		
		showAll : function(){
			this._primarySelect().find('option').show();
			this._secondarySelect().find('option').show();			
		},
		
		hidePrimary : function(code){
			var primarySelect = this._primarySelect();		
			
			primarySelect.find("option[value='"+code+"']").hide();
			
			this.hideSecondaryFrom(code);
		},
		
		showPrimary : function(code){
			var primarySelect = this._primarySelect();		
		
			var opt = primarySelect.find("option[value='"+code+"']");
			opt.show();
			if (opt.is(':selected')){
				this.showSecondaryFrom(code);
			}

		},
		
		_loopAndApplySecondaryFrom : function(primaryCode, methodname){
			var secondarySelect = this._secondarySelect();
			
			for (var pi=0; pi<this.options.lists.length;pi++){
				var primaryOpt = this.options.lists[pi],
					primaryValues = primaryOpt.subInput.possibleValues;
				
				if (primaryOpt.code === primaryCode){
					for (var si=0; si < primaryValues.length; si++){
						var subopt = primaryValues[si];
						secondarySelect.find('option[value="'+subopt.code+'"]')[methodname]();
					}
				}
			}
		}
		
	});
	
	return searchwidget;
	
});