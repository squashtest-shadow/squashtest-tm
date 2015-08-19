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
/*
 * options : {
 * 	entityUrl : if set, the component will update its content itself whenever a POST ajax request is made
 * 				in the document.
 * 	format : the date format
 * 	never : the label 'never' displayed when no modification ever happend on that entity.
 * }
 * 
 */
define(["jquery", "squash.dateutils", "squash.attributeparser", "squash.statusfactory"], 
		function($, dateutils, attrparser, statusfactory){
		
	function updateDateInformations(infos, options){					
		
		// update the dates
		var newExecutedOn = (infos.executedOn !== null && infos.executedOn.length>0) ? dateutils.format(infos.executedOn, options.format) : "";
		var newExecutedBy = (infos.executedBy !== null && infos.executedBy.length>0) ? '('+infos.executedBy+')' : options.never;
						
		$("#last-executed-on > .datetime").text(newExecutedOn);
		$("#last-executed-on > .author").text(newExecutedBy);
	
		var _html, _elt;
		// update the statuses
		if (!! infos.executionStatus){
			_html = statusfactory.getHtmlFor(infos.executionStatus);
			_elt = $(_html);
			_elt.css({
				'white-space' : 'nowrap',
				'display' : 'inline-block'
			});
			$("#execstatus-label").empty().append(_elt);
		}
		if (!! infos.automatedStatus){			
			_html = statusfactory.getHtmlFor(infos.automatedStatus);
			_elt = $(_html);
			_elt.css({
				'white-space' : 'nowrap',
				'display' : 'inline-block'
			});
			$("#autostatus-label").empty().append(_elt);
		}
		
		// update the URL : not done. Let's see if anyone reports that bug :P 
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
				executedOn : $("#last-executed-on > .datetime").text(),
				executedBy : $("#last-executed-on > .author").text()					
			};
			
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