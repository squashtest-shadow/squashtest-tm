

(function($){
	
	$.fn.contextualContent = function(settings){
	
		this.listeners = [];
		this.currentUrl = "";
		this.currentXhr = { readyState : 4, abort : function(){} };		//we initialize it with a mock.
		
		var self = this;
		
		/* ******************* private **************** */
		
		var cleanContent = $.proxy(function(){
			$('.is-contextual').dialog("destroy").remove(); 
			this.empty();		
			this.listeners = [];
		}, this);

			
		
		/* ******************* public **************** */
		
				
		this.fire = function(origin, event){
			for (var i in this.listeners){
				var listener = this.listeners[i];
				if (listener !== origin){
					listener.update(event);
				}
			}
		}
				
		this.addListener = function(listener){
			this.listeners.push(listener);
		}
		
		
		this.loadWith = function(url){
			
			var defer = $.Deferred();
			var self = this;
			
			if (url == this.currentUrl){
				defer.reject;
				return defer.promise();			
			}else{
				this.currentXhr = $.ajax({
					url : url,
					type : 'GET', 
					dataType : 'html'
				})
				.success(function(data){
					self.currentUrl = url;
					cleanContent();
					self.html(data);
				});
				
				return this.currentXhr;
			}
			
		}

		this.unload = function(){
			cleanContent();
			this.currentUrl = "";
			if (this.currentXhr.readyState != 4) this.currentXhr.abort();
		}
		
		return this;
	
	}


})(jQuery);

