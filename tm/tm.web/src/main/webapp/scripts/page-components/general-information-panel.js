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
 * options : {
 * 	entityUrl : if set, the component will update its content itself whenever a POST ajax request is made
 * 				in the document.
 * 	format : the date format
 * 	never : the label 'never' displayed when no modification ever happend on that entity.
 * }
 * 
 */
define(["jquery", "squash.dateutils", "squash.attributeparser"], function($, dateutils, attrparser){
		
	function updateDateInformations(infos, options){					
		
		var newCreatedOn = (infos.createdOn !== null && infos.createdOn.length>0) ? dateutils.format(infos.createdOn, options.format) : "";
		var newCreatedBy = (infos.createdBy !== null && infos.createdBy.length>0) ? '('+infos.createdBy+')' : options.never;
		
		var newModifiedOn = (infos.modifiedOn !== null && infos.modifiedOn.length>0) ? dateutils.format(infos.modifiedOn, options.format) : "";
		var newModifiedBy = (infos.modifiedBy !== null && infos.modifiedBy.length>0) ? '('+infos.modifiedBy+')' : options.never;
							
		$("#created-on > .datetime").text(newCreatedOn);
		$("#created-on > .author").text(newCreatedBy);
		
		$("#last-modified-on > .datetime").text(newModifiedOn);
		$("#last-modified-on > .author").text(newModifiedBy);
	
	}
	
	
	return {
		
		refresh : function(){
			var elt = $("#general-information-panel"),
				stropts = elt.data('def'),
				opts = attrparser.parse(stropts);
			
			if (opts.url){
				$.ajax({
					type : 'GET',
					url : opts.url+'/general',
					dataType : 'json'
				})
				.done(function(json){
					updateDateInformations(json, opts);
				});		
			}
		},
		
		init : function(){
			
			var elt = $("#general-information-panel"),
				stropts = elt.data('def'),
				opts = attrparser.parse(stropts);
			
			var infos = {
				createdOn : $("#created-on > .datetime").text(),
				createdBy : $("#created-on > .author").text(),
				modifiedOn : $("#last-modified-on > .datetime").text(),
				modifiedBy : $("#last-modified-on > .author").text()					
			} ;
			
			var self = this;
			
			updateDateInformations(infos, opts);
			
			if (!! opts.url){
				$("#general-information-panel").ajaxSuccess(function(event, xrh, settings) {
					if (settings.type == 'POST') {
						self.refresh();
					}
				});			
			}
		}
	}
	
});