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

/*
 * That object makes things according to the state of sorting of a datatable.
 */

/*
 * configuration : {
 *		basic {
 *			iterationId : the id of the iteration
 *		}, 
 *		permissions : {
 *			reorderable : boolean, that tells whether the 'reorder' button is active.
 *		}
 *	}
 * 
 */
define(['jquery'], function($){
	
	
	function SortMode(conf) {

		var isLocked = false;
		
		this.storage = localStorage || {
			setItem : function(){},
			getItem : function(){},
			removeItem : function(){}
		};
		
		// **************** configuration ******************
		
		this.reorderable = conf.permissions.reorderable || false;
		
		if (conf.basic.iterationId === undefined){
			throw "sortmode : iteration id absent from the configuration";
		}
		
		this.key = 'itp-sort-'+conf.basic.iterationId;
		
		// ******************* logic ***********************
		
		this.resetTableOrder = function(table){
			table.fnSettings().aaSorting = StaticSortMode.defaultSorting();
			this._disableSortMode();
		};
		
		this.manage = function(newSorting){
			
			if (this._isDefaultSorting(newSorting)){
				this._disableSortMode();
				this._deleteaaSorting();
			}
			else{
				if(!isLocked){
					this._enableSortMode();
				}
				this._saveaaSorting(newSorting);
			}
		};

		
		this._enableSortMode = function(){
			$("#test-plan-sort-mode-message").show();
			$("#iteration-test-plans-table").find('.select-handle').removeClass('drag-handle');
			if (this.reorderable){
				$("#reorder-test-plan-button").squashButton('enable');
			}
		};
		
		this._disableSortMode = function(){
			$("#test-plan-sort-mode-message").hide();
			$("#iteration-test-plans-table").find('.select-handle').addClass('drag-handle');
			
			$("#reorder-test-plan-button").squashButton('disable');
			
		};
		
		this._lockSortMode = function(){
			isLocked = true;
		};

		this._unlockSortMode = function(){
			isLocked = false;
		};
		
		this._isDefaultSorting = function(someSorting){
			var defaultSorting = StaticSortMode.defaultSorting();
			return (someSorting.length === 1 &&
					someSorting[0][0] === defaultSorting[0][0] &&
					someSorting[0][1] === defaultSorting[0][1]);
		};
		
		
		// ******************** I/O ******************** 
		
		this.loadaaSorting = function(){
			var sorting = this.storage.getItem(this.key);
			if (!! sorting){
				return JSON.parse(sorting);
			}
			else{
				return StaticSortMode.defaultSorting();
			}
		};
		
		this._saveaaSorting = function(aaSorting){
			var trimedSorting = [],
				_buf;
			
			for (var i=0,len = aaSorting.length; i<len; i++){
				_buf = aaSorting[i];
				trimedSorting.push( [_buf[0], _buf[1]] );
			}
			
			this.storage.setItem(this.key, JSON.stringify(trimedSorting));
		};
		
		this._deleteaaSorting = function(){
			this.storage.removeItem(this.key);
		};
		
	}
	
	
	var StaticSortMode = {
		newInst : function (conf){
			return new SortMode(conf);
		},
		defaultSorting : function(){
			return [[0, 'asc']];
		}
	};
	
	return StaticSortMode;
	
});