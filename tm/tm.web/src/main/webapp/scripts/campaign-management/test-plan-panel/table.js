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
 * WARNING : THIS IS A STUB. MUCH OF THE CODE STILL LIES IN THE ORIGINAL TAG FILE. TODO : MOVE IT ALL HERE !
 * 
 * @see 'tags/capaigns-components/campaign-test-plan-table.tag'
 * @see 'iteration-management/test-plan-panel'
 *  
 * 			
 * 
 * 
 * configuration an object as follow :
 * 
 * {
 *		permissions : {
 *			editable : boolean, is the table content editable ?
 *			reorderable : boolean, is the test plan reorderable ?
 *		},
 *		basic : {
 *			campaignId : the id of the current iteration
 *		},
 *		messages : {
 *			allLabel : label meaning 'all' in the current locale
 *		},
 *		urls : {
 *			testplanUrl : base urls for test plan items,
 *		}
 *	}
 * 
 */

define(['jquery', './sortmode', 'squashtable', 'jeditable'],
        function($, smode) {

	function createTableConfiguration(conf){
		
		var drawCallback = function(){
			if (conf.permissions.editable){
				addLoginListToTestPlan();
			}
			//sort mode
			var settings = this.fnSettings();
			var aaSorting = settings.aaSorting;		
			this.data('sortmode').manage(aaSorting);
		};
		
		var tableSettings = {
			"aLengthMenu" : [[10, 25, 50, 100, -1], [10, 25, 50, 100, conf.messages.allLabel]],
			fnDrawCallback : drawCallback
		};
		
		var squashSettings = {};

		if (conf.permissions.reorderable){
			squashSettings.enableDnD = true;
			squashSettings.functions = {
				dropHandler : function(dropData){
					var ids = dropData.itemIds.join(',');
					var url	= conf.urls.testplanUrl + '/' + ids + '/position/' + dropData.newIndex;			
					$.post(url, function(){
						$("#test-cases-table").squashTable().refresh();
					});
						
				}
			}
		}
		
		return {
			tconf : tableSettings,
			sconf : squashSettings
		}
	}

	function _hideFilterFields(){
		$(".th_input", $("#test-cases-table")).hide();
	}
	
	function _showFilterFields(){
		$(".th_input", $("#test-cases-table")).show();
	}
	
	function _initializeFilterFields(initconf){	
		
		var users = initconf.basic.assignableUsers;
		var weights = initconf.basic.weights;
		var modes = initconf.basic.modes;
		
		$($("th", $("#test-cases-table"))[1]).append("<input class='th_input'/>");
		$($("th", $("#test-cases-table"))[2]).append("<input class='th_input'/>");
		$($("th", $("#test-cases-table"))[3]).append("<input class='th_input'/>");
		$($("th", $("#test-cases-table"))[4]).append("<select id='filter-user-combo' class='th_input'/>");
		$($("th", $("#test-cases-table"))[5]).append("<select id='filter-weight-combo' class='th_input'/>");
		$($("th", $("#test-cases-table"))[6]).append("<select id='filter-mode-combo' class='th_input'/>");
		
		$.each(users, function(index,value){
			var o = new Option(value, index);
			$(o).html(value);
			$("#filter-user-combo", $("#test-cases-table")).append(o);
		});

		$.each(weights, function(index,value){
			var o = new Option(value, index);
			$(o).html(value);
			$("#filter-weight-combo", $("#test-cases-table")).append(o);
		});
		
		
		$.each(modes, function(index,value){
			var o = new Option(value, index);
			$(o).html(value);
			$("#filter-mode-combo", $("#test-cases-table")).append(o);
		});
		
		$(".th_input").click(function(event){
			event.stopPropagation();
		});
		
		$(".th_input").change( function () {
			 $("#test-cases-table").squashTable().fnFilter(this.value, $(".th_input").index(this));
		});

		_hideFilterFields();
	}
	
	// **************** MAIN ****************
	
	return {
		init : function(enhconf){
			
			var tableconf = createTableConfiguration(enhconf);	

			var sortmode = smode.newInst(enhconf);
			tableconf.tconf.aaSorting = sortmode.loadaaSorting();
			
			var table = $("#test-cases-table").squashTable(tableconf.tconf, tableconf.sconf);
			table.data('sortmode', sortmode);
			this.lockSortMode = sortmode._lockSortMode;
			this.unlockSortMode = sortmode._unlockSortMode;	
			this.hideFilterFields = _hideFilterFields;
			this.showFilterFields = _showFilterFields;
			_initializeFilterFields(enhconf);
		}
	};
	
});