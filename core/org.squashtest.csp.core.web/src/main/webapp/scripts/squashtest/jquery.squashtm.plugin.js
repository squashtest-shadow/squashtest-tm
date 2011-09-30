/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
/*
	that file will compile every little random plugins/redefinitions etc we need to stuff jQuery with.
	
*/

var squashtm ;

(function($){
	
	//custom selectors, eg $(tree).find(":folder") will select all the nodes corresponding to folders.
	
	$.extend($.expr[':'],{
		library : function(a){
			return $(a).is("[rel='drive']") ;
		},		
		folder : function(a){
			return $(a).is("[rel='folder']") ;
		},		
		file : function(a){
			return $(a).is("[rel='file']") ;
		},
		node : function(a){
			return $(a).is("[rel='folder']") || $(a).is("[rel='file']");
		},
		iteration : function(a){
			return $(a).is("[rel='resource']");
		},
		editable : function(a){
			return $(a).attr('editable') === 'true';
		}
	});
	

	//convenient function to gather data of a jQuery object.
	
	$.fn.collect = function(fnArg){	
		
		var res = [];
		if (this.length>0){		
			this.each(function(index, elt){
				res.push(fnArg(elt));
			});
		}			
		return res;
		
	}
	
	$.fn.contains = function(domElt){
		var vThis  = this.collect(function(e){return e;});
		
		for (var e in vThis){
			if (vThis[e] === domElt){
				return true;
			}
		}
		
		return false;
		
	}
	

	
	/*
		Squash TM domain name : variable $.fn.squashtm

	*/
	
	squashtm = {
		
		/*
			popup settings :
				- all normal $.ui.dialog valid options
				- selector : 	jquery selector of the dom element we are targetting (mandatory)
				- openedBy : 	selector for a clickable element that will open the popup (optional)
				- title : 		the title of the popup (mandatory)
				- isContextual : boolean telling if the said popup should be added the special class 'is-contextual', that will mark him as a removable popup when the context changes,
				- closeOnSuccess : boolean telling if the popup should be closed if an ajax request succeeds (optional)
				- ckeditor :  {
						- lang : the desired language for the ckeditor (optional)
						- styleUrl : the url for the ckeditor style.
				}
				
				- buttons : the button definition (mandatory)
				
		
		*/
	
			popup : {
				//begin popup.create
				create : function(settings){
					
					var target = $(settings.selector);
					target.addClass("popup-dialog");
				
					var defaults = {
						autoOpen : false,
						resizable : false,
						modal : true,
						width : 600,
						title : "default popup",
						position : ['center', 100],
						open : function(){
							squashtm.popup.cleanup.call(target);
						}, 
						close: function(){
							squashtm.popup.cleanup.call(target);
						},
						
						create : function(){
							target.find('textarea').each(function(){
								var jqT = $(this);
								if (settings.isContextual){
									jqT.addClass('is-contextual');
								}
								jqT.ckeditor(function(){}, 
									{
										//in this context 'this' is the defaults object
										//the following properties will appear
										//once we merged with the user-provided settings
										customConfig : settings.ckeditor.styleUrl || "/styles/ckeditor/ckeditor-config.js", 
										language : settings.ckeditor.lang || "en"
									}
								);
							});				
						}						
					}
					
					$.extend(true,defaults, settings);
					target.dialog(defaults);
					
					if (settings.closeOnSuccess===undefined || settings.closeOnSuccess){
						target.ajaxSuccess(function(){
							if (target.dialog('isOpen')===true) target.dialog('close');
						});
					}
					
					target.keypress(function(event){
						if (event.which == '13') {
							var buttons=target.dialog("option", "buttons" );
							var firstOne;
							for (var property in buttons){
								firstOne=buttons[property];
								break;
							}
							$(firstOne).click();
						}
					});
					
					if (settings.openedBy){
						$(settings.openedBy).click(function(){target.dialog('open'); return false;});
					}
					
					if (settings.isContextual){
						target.addClass('is-contextual');
					}
					
				},	//end popup.create
				//begin popup.cleanup
				cleanup : function(){
					this.find('input:text').val('');
					this.find('.error-message').text('');
					this.find('textarea').val('');						
				}
				//end popup.cleanup
			}
	}
	
	


})(jQuery);