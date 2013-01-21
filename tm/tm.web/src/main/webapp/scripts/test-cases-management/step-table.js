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

define(["jquery", "squash.table-collapser"], function($, TableCollapser){
	
	
	// ********************************** table helper function ******************************
	
	
	
	
	
	
	// ************************************* table collapser code ****************************
	
	function oneCellIsInEditingState(){
		var collapsibleCells = this.collapsibleCells;
		for(var k = 0; k < collapsibleCells.length ; k++){
			if(collapsibleCells[k].editing){
				return  true;
			}
		}		
		return false;
	}
	function collapseCloseHandle(){
		var collapsibleCells = $(this.collapsibleCells);
		collapsibleCells.editable('disable');
		collapsibleCells.removeClass('editable');
		collapsibleCells.bind("click", this.openAllAndSetEditing);
	}
	function openAllAndSetEditing(eventObject){
		this.openAll();
		setTimeout(function() {
			$(eventObject.target).click();
		 }, 500);
	}
	function collapseOpenHandle(){
		var collapsibleCells = $(this.collapsibleCells);
		collapsibleCells.editable('enable');
		collapsibleCells.addClass('editable');
		collapsibleCells.unbind("click", this.openAllAndSetEditing);
	}
	
	function bindCollapser(settings){
		
		var collapser;
		var language = settings.language;
		
		var collapseButton = $('#collapse-steps-button');		
		var table = $('#test-steps-table');
		
		//enrich the collapser prototype with more methods
		
		TableCollapser.prototype.oneCellIsInEditingState = oneCellIsInEditingState;
		TableCollapser.prototype.collapseCloseHandle = collapseCloseHandle;
		TableCollapser.prototype.openAllAndSetEditing = openAllAndSetEditing;
		TableCollapser.prototype.collapseOpenHandle = collapseOpenHandle;
		
		//begin
		
		var columns = [2,3];
		collapser = new TableCollapser(table, columns); 
		collapser.onClose.addHandler(collapser.collapseCloseHandle);
		collapser.onOpen.addHandler(collapser.collapseOpenHandle);	
		//collapser.bindButtonToTable(collapseButton);
		collapseButton.click(function(){
			if(collapser.isOpen){
				if(collapser.oneCellIsInEditingState()){
					$.squash.openMessage(language.popupTitle, language.popupMessage);
				}else{
					collapser.closeAll();
					decorateStepTableButton("#collapse-steps-button", "ui-icon-zoomin");
					$("#collapse-steps-button").attr('title', language.btnExpand);
					$("#collapse-steps-button").button({label: language.btnExpand});
				}
			}else{
				collapser.openAll();
				decorateStepTableButton("#collapse-steps-button", "ui-icon-zoomout");
				$("#collapse-steps-button").attr('title', language.btnCollapse);
				$("#collapse-steps-button").button({label:language.btnCollapse});
			}
		});
		

		
		//end
		table.data('collapser', collapser);
		
		return collapser;
	 }	
	
	function init(settings){
		
		var collapserSettings = settings.collapser;
		var collapser = bindCollapser(collapserSettings);
		
		
		
	}
	
	
	return {
		init : init
	}
	
	
})