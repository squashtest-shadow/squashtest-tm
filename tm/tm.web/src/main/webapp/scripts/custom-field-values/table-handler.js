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


define(["jquery", "./jquery-cuf-values"],function($){
	
	
	// ********************************** Datatable configuration *******************************
	
	function createColumnDefs(cufDefinitions){
		
		var columns = [];		
		var i = 0, 
			total = cufDefinitions.length;
		
		for (i=0;i<total;i++){
			
			var currentDef = cufDefinitions[i];			
			var newColumn = { 
				'bVisible' : true, 
				'bSortable' : false, 
				'mDataProp' : "customFields."+currentDef.code+".value",
				'sClass' : 'custom-field-value custom-field-'+currentDef.code
			};			
			columns.push(newColumn);
			
		};
		
		return columns;
		
	}
	
	
	
	function mergeColumnDefs(regularColumnDefs, cufColumnDefs, insertionIndex){
		
		//ensure that the original columnDefs are sorted 
		var finalDefs = regularColumnDefs.sort(function(a,b){
			return a.aTargets[0] - b.aTargets[0];
		});
		
		//merge the arrays
		var spliceArgs = [insertionIndex, 0].concat(cufColumnDefs);
		Array.prototype.splice.apply(finalDefs, spliceArgs);
		
		//reindex the target columns
		var i=0,length=finalDefs.length;
		for (i=0;i<length;i++){
			var col = finalDefs[i];
			col.aTargets = col.aTargets || [];
			col.aTargets[0] = i;
		};
		
		//done
		return finalDefs;				
	}
	

	function mapDefinitionsToCode(cufDefinitions){
		
		var resultMap = {};
		
		var i = 0,
			length = cufDefinitions.length;
		
		for (i=0;i<length;i++){
			var currentDef = cufDefinitions[i];
			resultMap[currentDef.code] = currentDef;
		}
		
		return resultMap;
	}
	
	
	function makePostFunction(cufCode, table){
		return function(value){
			
			var row = $(this).parents('tr').get(0);
			var cufId = table.fnGetData(row).customFields[cufCode].id
			
			var url = squashtm.app.contextRoot + "/custom-fields/values/"+cufId;
			
			return $.ajax({
				url : url,
				type : 'POST',
				data : { value : value}
			});			
		}
	}
	
	
	function createCufValuesDrawCallback(cufDefinitions){
		
		return function(){
			
			var table = this;
			var defMap = mapDefinitionsToCode(cufDefinitions); 

			for (var code in defMap){
				var def = defMap[code];
				var cells = table.find('td.custom-field-'+code);
				var postFunction = makePostFunction(code, table);
				
				cells.customField(def, postFunction);
				
			}
			
		}
	}
	
	
	function decorateTableSettings(tableSettings, cufDefinitions, index){
		
		var cufDefs = createColumnDefs(cufDefinitions);
		
		var origDef = tableSettings.aoColumnDefs;
		tableSettings.aoColumnDefs = mergeColumnDefs(origDef, cufDefs, index);
		
		var oldDrawCallback = tableSettings.fnDrawCallback;
		var addendumCallback = createCufValuesDrawCallback(cufDefinitions);
		
		tableSettings.fnDrawCallback = function(){
			oldDrawCallback.apply(this, arguments);
			addendumCallback.call(this);
		}
		
		return tableSettings;
	}
	
	
	// ********************* DOM table configuration **************************
	
	function decorateDOMTable(zeTable, cufDefinitions, index){
		var table = (zeTable instanceof jQuery) ? zeTable : $(zeTable);
		
		//create the new header columns
		var newTDSet = $();
		
		var i=0, length=cufDefinitions.length;
		
		for (i=0; i<length; i++){
			var newTD = $('<th>'+cufDefinitions[i].label+'</th>');
			newTDSet = newTDSet.add(newTD);
		}
		
		//insert them
		var header = table.find('thead tr');
		var firstHeaders = header.find('th').slice(0, index);
		header.prepend(newTDSet);
		header.prepend(firstHeaders);
		
		
	}
	
	return {
		decorateTableSettings : decorateTableSettings,
		decorateDOMTable : decorateDOMTable
	}
	
});