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

define(["jquery"], function($){
	
	var nodeCreationDialogCUFValuesSupport = {
			

		/*
		 * settings :
		 *  - getURL : the url where to fetch the creator panel
		 *  - table : the <table/> element that (will) hold the elements, as a jQuery object.
		 */
		loadCUFValuesPanel : function(settings){
			var url = settings.getURL;
			var table = $(settings.table);
			
			var pleaseWait=$('<tr class="cuf-wait" style="line-height:10px;"><td colspan="2" class="waiting-loading"></td></tr>');
			
			table.find('.create-node-custom-field-row').remove(); //cleanup of the previous calls (if any)
			table.append(pleaseWait);	
			var self = this;
			
			$.get(url, null, null, "html")
			.success(function(html){
				table.find(".cuf-wait").remove();
				//because it wouldn't work otherwise, we must strip the result of the license header
				var fixed = $.trim(html.replace(/\<\!--[\s\S]*--\>/,''));
				table.append(fixed);
				self.resetCUFValues(table);
			});			
		},
	
		/*
		 * settings :
		 *  - table : the <table/> element that hold the elements, as a jQuery object.
		 */		
		resetCUFValues : function(table){
			var bindings = table.find(".create-node-custom-field");
			if (bindings.length>0){
				bindings.each(function(){
					var input = $(this);
					var defValue = input.data('default-value');
					
					if (input.is('input[type="checkbox"]')){
						input.prop('checked', (defValue===true));
					}
					else{
						input.val(defValue);
					}
				})
			}			
		},
		
		/*
		 * settings :
		 *  - table : the <table/> element that hold the elements, as a jQuery object.
		 *  
		 *  returns : a map of { id, value }, suitable for posting with the rest of the 
		 *  		  entity model 
		 */
		readCUFValues : function(table){
			var result = {};
			var cufs = table.find(".create-node-custom-field");
			if (cufs.length>0){
				cufs.each(function(){
					var jqThis = $(this);
					result[this.id] = (jqThis.is('input[type="checkbox"]')) ? jqThis.prop('checked') : jqThis.val();
				});
			}
			return result;
		}
			
	};
	
	return {
		
		getNodeCreationDialogCUFValuesSupport : function(){
			return nodeCreationDialogCUFValuesSupport;
		}
		
		
	}
	
})