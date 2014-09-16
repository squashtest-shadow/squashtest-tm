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
/*
 * 
 * The purpose of this override of tagit is to enhance it with the behaviour it is lacking.
 * 
 * enable() and disable() are mostly scrapped from the original source. 
 * 
 */

/*
 * new features : 
 * 
 *  1/ init options :
 *  
 *  - constrained : boolean. If true, the user will be forced to choose a value that belongs to
 *	the availabletags (or other sources). Other inputs will be removed.
 *  - 'essspectacularrr !' : boolean. If true, a failed constrained check will be, well, spectacular.
 * 
 *  2/ methods : 
 *  
 *  - disable : Disables the widget
 *  - enable : Enables the widget
 *  - getSource: returns the tag source
 *  - belongsToAvailable(string) : tells wether the argument belongs to the source
 * 
 */

define(["jquery", "jqueryui", "jquery.tagit"], function($){
	
	$.widget('squash.squashTagit', $.ui.tagit, {
		
		options : {
			constrained : false,
			'essspectacularrr !' : false
		},
		
		_create : function(){
			this._super();
			
			this.element.addClass('squash-tagit');
			this.element.removeClass('ui-corner-all');
			
			if (this.options.constrained){
				this._setConstrainedMode();				
			}
		},
		
		_setConstrainedMode : function(){
			var formerCallbacks = this.options.afterTagAdded;
			var self = this;			
			
			var newCallback = function(event, ui){
				
				var passedCheck = true;
				
				if (! ui.duringInitialization){
					passedCheck = self.checkWarnRemove(ui.tag, ui.tagLabel);
				}
				
				if (passedCheck && !!formerCallbacks){
					formerCallbacks.call(self, event, ui);
				}
			};		
			
			this.options.afterTagAdded = newCallback;
		},
		
		
		disable : function(){
			
			var element = this.element;
			
			//disable the tags
			var tagli = element.find('li.tagit-choice-editable');
			tagli.removeClass('tagit-choice-editable');
			tagli.addClass('tagit-choice-read-only');
			
			//remove the 'remove icon'
			tagli.find('a').remove();
			
			//disable the text input
			this.tagInput.prop('disabled', true);
			
			//change the style :
			tagli.addClass('ui-state-disabled');
			
		},
		
		enable : function(){
			
			var element = this.element;
			
			//enable the tags
			var tagli = element.find('li.tagit-choice-read-only');
			tagli.removeClass('tagit-choice-read-only');
			tagli.addClass('tagit-choice-editable');
			
			//recreate the enable-icon
			 var removeTagIcon = $('<span></span>').addClass('ui-icon ui-icon-close');
	         var removeTag = $('<a><span class="text-icon">\xd7</span></a>') // \xd7 is an X
	             .addClass('tagit-close')
	             .append(removeTagIcon)
	             .click(function(e) {
	                 delegate.removeTag(tag);
	             });
	         
	         tagli.append(removeTag);
			
			//enable the input
			this.tagInput.prop('disabled', false);
			
			//re-enable the tag style
			tagli.removeClass('ui-state-disabled');		
			
		},
		
		getTagInput : function(){
			return this.tagInput;
			
		},
		
		getSource : function(){
			var available = this.options.availableTags;
			if (available.length > 0){
				return available;
			}
			else{
				return this.options.autocomplete.source;
			}
		},
		
		belongsToAvailable : function(label){
			var source = this.getSource();
			return ($.inArray(label, source)!==-1);
		},
		
		checkWarnRemove: function(tag, label){
			
			var self = this;
			
			if (! this.belongsToAvailable(label)){		

				tag.removeClass('ui-widget-default ui-widget-content ui-state-default');
				tag.addClass('invalid');
				
				if (self.options['essspectacularrr !']){
					self._espectacularRemove(tag);
				}
				else{
					self._simpleRemove(tag);
				}
				
				return false;
			}
			else{
				return true;
			}
		},
		
		_simpleRemove : function(tag){
			var self = this;
			tag.effect('shake', {times : 2}, 500, function(){
				self.removeTag(tag.get(0));
			});	
		},
		
		_espectacularRemove : function(tag){	
			var self = this;
			
			tag.css({
				'z-index' : '5000',
				'position' : 'absolute'
			});		
			
			tag.effect('scale', { percent : 300}, 500)	
				.effect('shake', { times : 10}, 500)
				.effect("explode", {}, 1000);
			
			self.removeTag(tag.get(0));	

		}
		
		
	});
	
	
});
