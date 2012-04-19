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


function TableCollapser(buttonP, dataTableP, columnsP){
	var collapser = this;
	var collapseButton = buttonP;
	var dataTable = dataTableP;
	var columns = columnsP;
	var isOpen = true;
	var rows = new Array();
	var collapsibleCells = new Array();
	this.onClose = new TableCollapserEvent();
	this.onOpen = new TableCollapserEvent();
	
	bindButtonToTable();
	
	function setCellsData(){
		var rows = dataTableP.find('tbody tr');
		 console.log("length "+columns.length);
		for(var i = 0; i < columns.length ; i++) {
			for(var j = 0; j < rows.length ; j++){
				collapsibleCells.push($(rows[j]).children('td')[columns[i]]);
			 }
		 }
		 console.log("collapsible "+collapsibleCells.length);
		 for(var k = 0; k < collapsibleCells.length ; k++){
			var cell = $(collapsibleCells[k]);
			console.log("cell text "+cell.text().substring(0,50));
			//cell.css('visibility', 'collapse');
			cell.data('completeHtml', cell.html());
			var truncated = cell.text();
			var maxChar = 50;
			if(truncated.length > maxChar){
				truncated = truncated.substring(0,50)+"[...]";
			}
			cell.data('truncatedHtml', truncated);
		 }
		 
	}
	function bindButtonToTable(){
		collapseButton.click(bindClick);
		
	}
	function bindClick(){
		if(isOpen){
			closeAll();
		}else{
			openAll();
		}
	}
	function closeAll(){
		console.log("closeAll");
		setCellsData();
		for(var k = 0; k < collapsibleCells.length ; k++){
			var cell = $(collapsibleCells[k]);
			//console.log("cell text"+cell.text());
			//cell.css('visibility', 'collapse');
			cell.html(cell.data('truncatedHtml'));
		 }
		 collapser.onClose.execute();
		 isOpen = false;
	}
	
	function openAll(){
		console.log("onpenAll");
		for(var k = 0; k < collapsibleCells.length ; k++){
			 var cell = $(collapsibleCells[k]);
				//console.log("cell text"+cell.text());
				//cell.css('visibility', 'visible');
				cell.html(cell.data('completeHtml'));
		}
		collapser.onOpen.execute();
		isOpen = true;
		
	}
	this.refreshTable = function (){
		if(!isOpen){
			closeAll();
		}
	}
}

function TableCollapserEvent(){
	this.eventHandlers = new Array();
}

TableCollapserEvent.prototype.addHandler = function(eventHandler){
	this.eventHandlers.push(eventHandler);
}

TableCollapserEvent.prototype.execute = function(args){

	for(var i = 0; i < this.eventHandlers.length; i++){
	this.eventHandlers[i](args);
	}
}


