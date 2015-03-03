/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
	 define([ "jquery", "jquery.cookie" ], function($) {

	  var COOKIE_NAME = "milestones";
	  var LOCAL_STORAGE = "milestones";
	  var oPath = {
				path : "/"
			};	  
	  
	  return {
	  	/* milestones-group is the component argument here but some stuff can't be directly argumented*/
	  	/* init the milestones select and create the cookie if needed*/
	  	init : function(component) {	
	  	if (typeof $.cookie(COOKIE_NAME) === 'undefined'){
		 		 //no cookie > take a cookie, it's dangerous to go alone
		 			$.cookie(COOKIE_NAME, "choose");
		 		} else {
		 		 //have a cookie
		 		}
	  		component.val($.cookie(COOKIE_NAME));
	  		
		 		if (localStorage.getItem(LOCAL_STORAGE) == 0 ) {
				 	if ($.cookie(COOKIE_NAME) == null || $.cookie(COOKIE_NAME) == "choose" ) {
				 	 	$("#milestone-group").append(new Option(translator.get('user-preferences.choosemilestone.label'), 'choose', true, true));
					 	$('.milestone-group option[value="choose"]');
				 	}
				 	else {
				 	}
				 	document.getElementById("milestone-group").disabled = true; 
	  		}
		 		else {
			 		document.getElementById("milestone-group").disabled = false; 
			 		if ($("#toggle-MODE-checkbox").prop('checked') == false) {
			 			$('#toggle-MODE-checkbox').switchButton({
			 			  checked: true
			 			});
			 			}	
			 		}
		 		
		  	$("#milestone-group").change(function(){
	  	  	$.cookie(COOKIE_NAME, encodeURIComponent($("#milestone-group").val()), oPath);
	  	  });
		 		
	  },
		
	  activateStatus : function(component){
			$.cookie(COOKIE_NAME, encodeURIComponent(component.val()), oPath);
			localStorage.setItem(LOCAL_STORAGE, 1);
		},
		
		deactivateStatus : function(component){
			// We need to keep the cookie value so keep it by doing nothing !
			localStorage.setItem(LOCAL_STORAGE, 0);
		}

    }

	});
