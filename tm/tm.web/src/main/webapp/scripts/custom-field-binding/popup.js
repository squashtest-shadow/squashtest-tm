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

define(["jquery.squash"], function(){
		
	return function(settings){
		
		var params = {
			selector : settings.selector,
			title : settings.title,
			closeOnSuccess : false,
			buttons : [
			    {
			    	'text' : settings.oklabel,
			    	'class' : "button-ok",
			    	'click' : function(){
			    		post();
			    	}
			    },
			    {
			    	'text' : settings.cancellabel,
			    	'class' : "button-cancel",
			    	'click' : function(){
			    		popup.dialog('close');
			    	}
			    }
			]
		};
		
		squashtm.popup.create(params);
		
		var popup = $(settings.selector);
		
		// ************* private attributes /  methods ***********
		
		var lineTemplate = popup.find(".row-template-holder tr");
		var table = popup.find("table");

		var reset = function(){
			table.find("tbody").empty();			
		};
		
		var populate = $.proxy(function(json){
			var i=0;
			var tbody = table.find("tbody").not(".row-template-holder");
			
			for (i=0;i<json.length;i++){
				var data = json[i];
				var newLine = lineTemplate.clone();
				var tds = newLine.find("td");
				
				tds.eq(1).text(data.name);
				tds.eq(2).text(data.inputType.friendlyName);
				tds.eq(3).text(data.optional);
				tds.eq(4).text(data.id);
				
				tbody.append(newLine);
			}
			
		}, popup);
		
		var reload = function(){
			
			$.ajax({
				type : 'GET',
				dataType : 'json',
				url : settings.getURL
			})
			.success(function(json){
				reset();
				populate(json);
			});
			
		};
		
		var post = $.proxy(function(){
			
			
		}, popup);
		
		//popup events
		
		popup.bind("dialogopen", function(){
			reload();
		});
		

		
		
		return popup;
	};
	
	
});