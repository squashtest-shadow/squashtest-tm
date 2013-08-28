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

define(['jquery', 'jquery.cookie'], function($){
	
	
	function SortMode(conf){
		
		this.DEFAULT_SORTING = conf.default || [[0, 'asc']];
		this.key = conf.key;
		
		this.getaaSorting = function(){
			var tablecookie = $.cookie(this.key);
			if (!! tablecookie){
				return JSON.parse(tablecookie)
			}
			else{
				return this.DEFAULT_SORTING;
			}
		};
		
		this.manageSortMode = function(newSorting){
			if (this._isDefaultSorting(newSorting)){
				this._disableSortMode();
			}
			else{
				this._enableSortMode();
			}
			this.persistaaSorting(newSorting);
		};
		
		this._isDefaultSorting = function(someSorting){
			return (someSorting.length === 1 &&
					someSorting[0][0] === this.DEFAULT_SORTING[0][0] &&
					someSorting[0][1] === this.DEFAULT_SORTING[0][1])
		};
		
		this._enableSortMode = function(){
			$("#test-plan-sort-mode-message").show();
			$("#iteration-test-plans-table").find('.select-handle').removeClass('drag-handle');
		};
		
		this._disableSortMode = function(){
			$("#test-plan-sort-mode-message").hide();
			$("#iteration-test-plans-table").find('.select-handle').addClass('drag-handle');
		};
		
		this.persistaaSorting = function(aaSorting){
			var trimedSorting = [],
				_buf;
			
			for (var i=0,len = aaSorting.length; i<len; i++){
				_buf = aaSorting[i];
				trimedSorting.push( [_buf[0], _buf[1]] );
			}
			
			$.cookie(this.key, JSON.stringify(trimedSorting), { path : '/'});
		}
	}
	
	return {
		new : function(conf){
			return new SortMode(conf);
		}
	}
	
});