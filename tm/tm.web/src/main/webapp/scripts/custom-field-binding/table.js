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


define(["jquery", "jquery.squash.datatables"], function($){
	
	return function(settings){
		
		
		var tableConf = {
			oLanguage :{
				sUrl : settings.languageUrl
			},
			sAjaxSource : settings.ajaxSource,
			iDeferLoading : settings.deferLoading,
			aoColumnDefs :[
				{'bSortable' : false, 'bVisible' : false, 'aTargets' : [0], 'mDataProp' : 'id'},
				{'bSortable' : false, 'bVisible' : true,  'aTargets' : [1], 'mDataProp' : 'position', 'sWidth' : '2em', 'sClass' : 'centered ui-state-default drag-handle select-handle'},
				{'bSortable' : false, 'bVisible' : true,  'aTargets' : [2], 'mDataProp' : 'customField["name"]'},
				{'bSortable' : false, 'bVisible' : true,  'aTargets' : [3], 'mDataProp' : null, 'sWidth' : '2em', 'sClass' : 'delete-button centered'}			
			]		
		};
	
		var squashConf = {
			
			dataKeys : {
				entityId : 'id',
				entityIndex :'position'
			},
			
			enableHover : true,
			
			confirmPopup : {
				oklabel : settings.oklabel,
				cancellabel : settings.cancellabel
			},
			
			enableDnD : true,
		
		};
		
		$(settings.selector).squashTable(tableConf, squashConf);

	
		return $(settings.selector).squashTable();
		
	}

});