
(function($){
	
	$.widget( "ui.togglePanel", {
	
		options : {
			initiallyOpen : true,
			title : "informations",
			cssClasses : "",
			panelButtonsSelector : ""
		},
		
		_create : function(){
			var widget=this;
			
			var wrapper = $('<div/>', {class:"toggle-panel ui-accordion ui-widget ui-helper-reset ui-accordion-icons"});
			var panelHead = $('<h3/>', {class:"ui-accordion-header ui-helper-reset ui-state-default ui-state-hover ui-state-active ui-corner-top"});
			var titlepanel = $('<div/>', {style:"overflow:hidden;"});
			var snapleft = $('<div class="snap-left"><a class="tg-link" href="#"></a></div>');
			var snapright = $('<div/>', {class:"snap-right"});			
			this.element.addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active");				
		
			titlepanel.append(snapleft);
			titlepanel.append(snapright);
			panelHead.append(titlepanel);
			
			this.element.wrap(wrapper);
			panelHead.insertBefore(this.element);
			
			panelHead.click($.proxy(function(event){
				event.stopImmediatePropagation();
				widget.toggleContent.call(this.element);
			}, this));
		
		}, 
		
		_init : function(){
			var settings = this.options;
			
			var panelHead = this.element.prev();
			if (settings.initiallyOpen){
				panelHead.addClass('tg-open');
			}else{
				this.element.hide();
				panelHead.toggleClass('ui-state-active ui-corner-top ui-corner-all');
			}		
			
			panelHead.find(".snap-left a").text(settings.title);				
			panelHead.parent().addClass(settings.cssClasses);
			
			if (settings.panelButtonsSelector){
				var inputs = $(settings.panelButtonsSelector);
				inputs.click(function(event){event.stopPropagation();});
				panelHead.find('.snap-right').append(inputs);
			}	
		},
		
		toggleContent : function(){
			var panelHead = this.prev();
			
			if (! panelHead.length){
				return; 	//if the head is not found, that's usually because the body was detached due to the animating sequence. Sorry for this but the 			(:animated) selector wouldn't work
			}
			
			this.toggle('blind', 500);			
			panelHead.toggleClass( "ui-state-active ui-corner-top ui-corner-all tg-open" );
			
			//now disable the buttons. 
			var disabled = (panelHead.hasClass('tg-open')) ? false : true;
			panelHead.find(':button, .button').button("option","disabled",disabled);
		}
	
	});
	
})(jQuery);
