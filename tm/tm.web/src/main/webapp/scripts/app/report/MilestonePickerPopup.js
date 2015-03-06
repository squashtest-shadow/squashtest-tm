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


define(['jquery', 'jquery.squash.formdialog', 'squashtable'], function($){
	
	function init(settings){
		
		// widgets 
		
		var dialog = $(settings.selector).formDialog();
		var table = dialog.find('table');
		
		var tableconf = {
			bServerSide : false,
			fnDrawCallback : function(){
				table.find('>tbody>tr>td.milestone-radio').each(function(){
					$(this).html('<input type="radio"/>');
				});
				table.find('>tbody>tr').addClass('cursor-pointer');
			}
					
		};
		
		table.squashTable(tableconf, {});
		
		// we want to deselect all lines except the one currently selected
		table.on('click', '>tbody>tr', function(evt){
			
			// don't trigger if the clicked element is 
			// the checkbox itself 
			if (! $(evt.target).is('input')){
				var chk = $(evt.currentTarget).find('.milestone-radio input');								
				var newstate = ! chk.is(':checked');
				chk.prop('checked', newstate);
				table.find('.milestone-radio input').not(chk).prop('checked', false);
			}
			else{
				table.find('.milestone-radio input').not(evt.target).prop('checked', false);		
			}
		});
		
		
		// events		
		dialog.on('formdialogconfirm', function(){
			
			var inputname = dialog.attr('id');
			var selectedInput = table.find('>tbody>tr>td.milestone-radio input:checked');
			
			if (selectedInput.length > 0){
				var row = selectedInput.get(0).parentNode.parentNode;
				var data = table.squashTable().fnGetData(row);
				
				var selId = data['entity-id'],
					label = data['label'];
				
				var resultspan  = $("#"+dialog.data('idresult'));
				resultspan.text(label);
				
				settings.model.setVal(inputname, [selId]);
				
			}
			
			dialog.formDialog('close');
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
	}
	
	return {
		init : init
	};
	
});