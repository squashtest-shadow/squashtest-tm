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

define(["jquery"], function(){
	
	function TableCollapserEvent() {
		this.eventHandlers = [];
	}

	TableCollapserEvent.prototype.addHandler = function (eventHandler) {
		this.eventHandlers.push(eventHandler);
	};

	TableCollapserEvent.prototype.execute = function (args) {
		for (var i = 0; i < this.eventHandlers.length; i++) {
			this.eventHandlers[i](args);
		}
	};
	
	
	function makeDefaultCellSelector(columnsP){
		return function(row){
			var columns = columnsP;
			var length = columns.length;
			var result = [];
			var tds = $(row).children('td');
			for (var i=0;i<length;i++){
				result.push(tds[columns[i]]);
			}
			return result;
		}
	}


	return function(dataTableP, columnsP) {
		
		var self = this;
		
		var dataTable = dataTableP;
		
		var cellSelector;
		if ($.isFunction(columnsP)){
			cellSelector = columnsP;
		}
		else{
			cellSelector = makeDefaultCellSelector(columnsP);
		}
		
		var columns = columnsP;
		this.isOpen = true;
		var rows = [];
		this.collapsibleCells = [];
		this.onClose = new TableCollapserEvent();
		this.onOpen = new TableCollapserEvent();

		var setCellsData = $.proxy(function(){
			var rows = dataTableP.children('tbody').children('tr');
			
			
			for (var j = 0; j < rows.length; j++) {
				var cells = cellSelector(rows[j]);
				this.collapsibleCells.push.apply(this.collapsibleCells, cells);
			}
			
			
			for (var k = 0; k < this.collapsibleCells.length; k++) {
				var cell = $(this.collapsibleCells[k]);
				cell.data('completeHtml', cell.html());
				var truncated = cell.text();
				var maxChar = 50;
				if (truncated.length > maxChar) {
					truncated = truncated.substring(0, 50) + " [...]";
				}
				cell.data('truncatedHtml', truncated);
			}

		}, this);
		
		var bindClick = $.proxy(function() {
			if (this.isOpen) {
				this.closeAll();
			} else {
				this.openAll();
			}
		}, this);
		
		this.bindButtonToTable = function (collapseButton) {
			collapseButton.click(bindClick);
			
		};
		
		this.closeAll = function () {
			setCellsData();
			for (var k = 0; k < this.collapsibleCells.length; k++) {
				var cell = $(this.collapsibleCells[k]);
				cell.html(cell.data('truncatedHtml'));
			}
			this.onClose.execute();
			this.isOpen = false;

		};

		this.openAll = function () {
			for (var k = 0; k < this.collapsibleCells.length; k++) {
				var cell = $(this.collapsibleCells[k]);
				cell.html(cell.data('completeHtml'));
			}
			this.onOpen.execute();
			this.isOpen = true;
		};
		
		this.refreshTable = function () {
			if (!this.isOpen) {
				this.closeAll();
			}
		};
	}	
});


