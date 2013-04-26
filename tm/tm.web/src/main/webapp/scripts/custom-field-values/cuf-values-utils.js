/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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

define(["jquery", "jqueryui"], function($){
	
	
	function appendCheckbox(element){
		var jqThis = (element instanceof jQuery) ? element : $(element);
		var checked = ( jqThis.text().toLowerCase() === "true" ) ? true : false;
		jqThis.empty();
		chkbx = $('<input type="checkbox"/>');
		chkbx.prop('checked', checked);			
		jqThis.append(chkbx); 		
		return chkbx;
	}
	
	function staticRendering(elts, cufDefinition){
		
		var elements = (elts instanceof jQuery) ? elts.get() : elts;
		if (elements.length===0) return;		
		
		//name of the property that gets/sets the text depending on the browser
		var txtppt = (elements[0].textContent!==undefined) ? "textContent" : "innerText";
		
		//loop variables
		var i=0, 
			length=elements.length,
			elt;
		
		var inputType = cufDefinition.inputType.enumName;
		
		if (inputType==="DATE_PICKER"){
			var format = cufDefinition.format;
			var text, formatted;
			for (i=0;i<length;i++){
				elt = elements[i];
				text = elt[txtppt];
				formatted = convertStrDate($.datepicker.ATOM, format, text);
				elt[txtppt]=formatted;
			}
		}
		else if (inputType==="CHECKBOX"){
			var chbx;
			for (i=0;i<length;i++){
				elt = elements[i];
				if (elt.type !="checkbox" ){
					chbx = appendCheckbox(elt);
					chbx.enable(false);
				}
				
			}
		}
		//else nothing
		
		
	}
	
	
	function convertStrDate(fromFormat, toFormat, strFromValue){
		var date = $.datepicker.parseDate(fromFormat, strFromValue);
		return $.datepicker.formatDate(toFormat, date);		
	}
	
	
	return {
		convertStrDate : convertStrDate,
		staticRendering : staticRendering,
		appendCheckbox : appendCheckbox
	}
	
});