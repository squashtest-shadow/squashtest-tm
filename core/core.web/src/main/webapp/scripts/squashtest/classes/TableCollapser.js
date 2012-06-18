/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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


function TableCollapser( dataTableP, columnsP){
	var collapser = this;
	var dataTable = dataTableP;
	var columns = columnsP;
	this.isOpen = true;
	var rows = new Array();
	this.collapsibleCells = new Array();
	this.onClose = new TableCollapserEvent();
	this.onOpen = new TableCollapserEvent();
	
	
	function setCellsData(){
		var rows = dataTableP.find('tbody tr');
		for(var i = 0; i < columns.length ; i++) {
			for(var j = 0; j < rows.length ; j++){
				collapser.collapsibleCells.push($(rows[j]).children('td')[columns[i]]);
			 }
		 }
		 for(var k = 0; k < collapser.collapsibleCells.length ; k++){
			var cell = $(collapser.collapsibleCells[k]);
			cell.data('completeHtml', cell.html());
			var truncated = cell.text();
			var maxChar = 50;
			if(truncated.length > maxChar){
				truncated = truncated.substring(0,50)+" [...]";
			}
			cell.data('truncatedHtml', truncated);
		 }
		 
	}
	this.bindButtonToTable = function (collapseButton){
		collapseButton.click(bindClick);
		
	}
	function bindClick(){
		if(collapser.isOpen){
			collapser.closeAll();
		}else{
			collapser.openAll();
		}
	}
	this.closeAll = function (){
		setCellsData();
		for(var k = 0; k < collapser.collapsibleCells.length ; k++){
			var cell = $(collapser.collapsibleCells[k]);
			cell.html(cell.data('truncatedHtml'));
		 }
		 collapser.onClose.execute();
		 collapser.isOpen = false;
		
	}
	
	this.openAll = function (){
		for(var k = 0; k < collapser.collapsibleCells.length ; k++){
			 var cell = $(collapser.collapsibleCells[k]);
				cell.html(cell.data('completeHtml'));
		}
		collapser.onOpen.execute();
		collapser.isOpen = true;
	}
	this.refreshTable = function (){
		if(!collapser.isOpen){
			collapser.closeAll();
		}
	}
}

function TableCollapserEvent(){
	this.eventHandlers = new Array();
}

TableCollapserEvent.prototype.addHandler = function (eventHandler){
	this.eventHandlers.push(eventHandler);
}

TableCollapserEvent.prototype.execute = function (args){

	for(var i = 0; i < this.eventHandlers.length; i++){
	this.eventHandlers[i](args);
	}
}


