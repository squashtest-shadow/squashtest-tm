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


/*
 * settings (see the doc for jquery datatable for details about the native settings):
 * 	{
 * 		selector : the selector for the table,
 * 		languageUrl : the url where to fetch the localization conf object,
 * 		getUrl : the ajaxSource (native),
 * 		deleteUrl : the url where to send DELETE request,
 * 		deferLoading : the iDeferLoading (native),
 * 		oklabel : text for the ok button,
 * 		cancellabel : text for the cancel button,
 * 		renderingLocations : an array of RenderingLocation. These are the ones supported by the BindableEntity this table is treating.  
 * }
 */

define(["jquery", "jquery.squash.datatables"], function($){

	return function(settings){
		
		//initialize the column definitions
		var aoColumnDefs = [
			{'bSortable' : false, 'bVisible' : false, 'aTargets' : [0], 'mDataProp' : 'id'},
			{'bSortable' : false, 'bVisible' : true,  'aTargets' : [1], 'mDataProp' : 'position', 'sWidth' : '2em', 'sClass' : 'centered ui-state-default drag-handle select-handle'},
			{'bSortable' : false, 'bVisible' : true,  'aTargets' : [2], 'mDataProp' : 'customField.name'}			
		];
		
		var i = 0,
			array = settings.renderingLocations,
			arrayLength = array.length;
		
		for (i=0;i<arrayLength;i++){
			var columnDef = { 'bSortable' : false, 'bVisible' : true, 'aTargets' : [3+i], 'mDataProp' : 'renderingLocations.'+array[i], 'sWidth' : '15em', 'sClass' : 'centered custom-field-location'}
			aoColumnDefs.push(columnDef);
		}
		
		aoColumnDefs.push({'bSortable' : false, 'bVisible' : true,  'aTargets' : [3+arrayLength], 'mDataProp' : null, 'sWidth' : '2em', 'sClass' : 'delete-button centered'});	
		
		var tableConf = {
			oLanguage :{
				sUrl : settings.languageUrl
			},
			sAjaxSource : settings.getUrl,
			iDeferLoading : settings.deferLoading,
			aoColumnDefs : aoColumnDefs
		};
	
		var squashConf = {
			
			dataKeys : {
				entityId : 'id',
				entityIndex :'position'
			},
			
			confirmPopup : {
				oklabel : settings.oklabel,
				cancellabel : settings.cancellabel
			},
			
			deleteButtons : {
				url : settings.deleteUrl+"/{id}",
				popupmessage : settings.deleteMessage,
				tooltip : settings.deleteTooltip,
				success : function(){
					table.refresh();
				}
			},

			enableHover : true,
			
			enableDnD : true,
			
			fixObjectDOMInit : true,
			
			functions :{
				dropHandler : function(moveObject){
					$.ajax({
						url : settings.moveUrl+"/"+moveObject.itemIds.join(',')+"/position",
						type : 'POST',
						data : {'newPosition' : moveObject.newIndex}
					})
					.success(function(){
						table.refresh();
					});
				}
			}
		
		};
		
		$(settings.selector).squashTable(tableConf, squashConf);
		
		var table=$(settings.selector).squashTable();
	
		return table;
		
	};

});