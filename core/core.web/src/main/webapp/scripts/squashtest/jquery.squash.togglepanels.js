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
(function ($) {
	$.widget("ui.togglePanel", {
		options : {
			initiallyOpen : true,
			title : "",
			cssClasses : "",
			panelButtonsSelector : ""
		},
		
		_create : function () {
			this.originalTitle = this.element.attr('title');
			if (typeof this.originalTitle !== "string") {
				this.originalTitle = "";
			}

			this.options.title = this.options.title || this.originalTitle;
			
			var widget = this;
					
			var wrapper = $('<div/>', {'class': "toggle-panel ui-accordion ui-widget ui-helper-reset ui-accordion-icons"});
			var panelHead = $('<h3/>', {'class': "ui-accordion-header ui-helper-reset ui-state-default ui-state-focus ui-corner-top"});
			var titlepanel = $('<div/>', {'style': "overflow:hidden;"});
			var snapleft = $('<div class="snap-left"><a class="tg-link" href="javascript:void(0)"></a></div>');
			var snapright = $('<div/>', {'class': "snap-right"});			
			this.element.addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active");				
		
			titlepanel.append(snapleft)
				.append(snapright);
			panelHead.append(titlepanel);
			
			this.element.wrap(wrapper);
			panelHead.insertBefore(this.element);
			
			panelHead.click($.proxy(function (event) {
				event.stopImmediatePropagation();
				//widget.toggleContent.call(this.element);
				widget.toggleContent();
			}, this));
		
		}, 
		
		_init : function () {
			var settings = this.options;
			var title = settings.title || '&#160;';
			
			var panelHead = this.element.prev();
			if (settings.initiallyOpen) {
				panelHead.addClass('tg-open');
			} else {
				this.element.hide();
				panelHead.toggleClass('ui-state-focus ui-state-active ui-corner-top ui-corner-all');
			}		
			
			panelHead.find(".snap-left a").text(title);				
			panelHead.parent().addClass(settings.cssClasses);
			
			if (settings.panelButtonsSelector) {
				var inputs = $(settings.panelButtonsSelector);
				inputs.click(function (event) { event.stopPropagation(); });
				panelHead.find('.snap-right').append(inputs);
			}	
		},
		
		toggleContent : function () {
			//var panelHead = this.prev();
			var panelHead = this.element.prev();
			
			if (!panelHead.length) {
				return; 	//if the head is not found, that's usually because the body was detached due to the animating sequence. Sorry for this but the 			
				//(:animated) selector wouldn't work
			}
			
			this.element.toggle('blind', 500);			
			panelHead.toggleClass("ui-state-focus ui-state-active ui-corner-top ui-corner-all tg-open");
			
			//now disable the buttons. 
			var disabled = (panelHead.hasClass('tg-open')) ? false : true;
			panelHead.find(':button, .button').button("option", "disabled", disabled);
		},
		
		openContent : function () {
			var panelHead = this.element.prev();
			if (! panelHead.hasClass('tg-open')){
				this.toggleContent();
			}
		}
	});
}(jQuery));
