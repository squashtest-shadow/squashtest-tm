
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
				
			
			this.element.each(function(){
				var jqThis = $(this);
				var wrapper = $('<div/>', {class:"toggle-panel ui-accordion ui-widget ui-helper-reset ui-accordion-icons"});
				var panelHead = $('<h3/>', {class:"ui-accordion-header ui-helper-reset ui-state-default ui-state-hover ui-state-active ui-corner-top"});
				var titlepanel = $('<div/>', {style:"overflow:hidden;"});
				var snapleft = $('<div class="snap-left"><a class="tg-link" href="#"></a></div>');
				var snapright = $('<div/>', {class:"snap-right"});

				jqThis.addClass("ui-accordion-content ui-helper-reset ui-widget-content ui-corner-bottom ui-accordion-content-active");
				titlepanel.append(snapleft);
				titlepanel.append(snapright);
				panelHead.append(titlepanel);
				
				wrapper.append(panelHead);
				
				wrapper.insertBefore(jqThis);	
				wrapper.append(jqThis);
				
				panelHead.click($.proxy(function(event){
					event.stopImmediatePropagation();
					widget.toggleContent.call(this);
				}, jqThis));
			});
			
		}, 
		
		_init : function(){
			var settings = this.options;
			try{
			this.element.each(function(){
				var jqThis = $(this);
				var panelHead = jqThis.prev();
				
				if (settings.initiallyOpen){
					panelHead.addClass('tg-open');
				}else{
					jqThis.hide();
					panelHead.toggleClass('ui-state-active ui-corner-top ui-corner-all');
				}
				
				panelHead.find(".snap-left a").text(settings.title);
				
				panelHead.parent().addClass(settings.cssClasses);
				
				if (settings.panelButtonsSelector){
					var inputs = $(settings.panelButtonsSelector);
					inputs.click(function(event){event.stopPropagation();});
					panelHead.find('.snap-right').append(inputs);
				}
				
			});
			}catch(e){alert('exception on create');}
		},
		
		toggleContent : function(){
			var panelHead = this.prev();
			
			if (! panelHead.length){
				return; 	//if the head is not found, that's probably because the body was detached due to the animating sequence.
			}
			
			this.toggle('blind', 500);			
			panelHead.toggleClass( "ui-state-active ui-corner-top ui-corner-all tg-open" );
			
			//now disable the buttons. 
			var disabled = (panelHead.hasClass('tg-open')) ? false : true;
			panelHead.find(':button, .button').button("option","disabled",disabled);
		}
	
	});
	
	
	
})(jQuery)