/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
 * accepts as basic configuration : 
 * {
 *	basic : {
 *		campaignId : the id of the campaign
 *	},
 *	permissions : {
 *		reorderable : can the test plan be reordered by the user ?
 *		editable : is the test plan editable by the user ?
 *		linkable : can one add more test cases to the test plan ?
 *	},
 *	messages : {
 *		allLabel : a label that means 'all' in the current locale
 *	}
 * 
 * }
 * 
 * Note that this code is incomplete, see for instance the iteration-management for an example of what we 
 * are aiming to. 
 * 
 */

define(['squash.translator', './table', './popups', 'app/util/ButtonUtil' ], function(translator, table, popups, ButtonUtil ) {

	var filterOn = false;
	
	function enhanceConfiguration(origconf){

		var conf = $.extend({}, origconf);
		
		var baseURL = squashtm.app.contextRoot;
		
		conf.messages = translator.get({
			automatedExecutionTooltip : "label.automatedExecution"
		});
		
		conf.urls = {
			testplanUrl : baseURL + '/campaigns/'+conf.basic.campaignId+'/test-plan/'
		};
		
		return conf;
		
	}
	
	function _bindButtons(conf){
		
		
		if (conf.permissions.editable){
			$("#assign-users-button").on('click', function(){
				$("#camp-test-plan-batch-assign").formDialog('open');
			});
		}
		
		if (conf.permissions.reorderable){
			$("#reorder-test-plan-button").on('click', function(){
				$("#camp-test-plan-reorder-dialog").confirmDialog('open');
			});
		}	
		
		if (conf.permissions.linkable){
			$("#add-test-case-button").on('click', function(){
				document.location.href=conf.urls.testplanUrl + "/manager";
			});
		}
		
		
		$("#filter-test-plan-button").on('click', function(){
			
			if(filterOn){
				filterOn = false;
				table.hideFilterFields();
				table.unlockSortMode();
				$("#test-plan-sort-mode-message").show();
				$("#test-cases-table").find('.select-handle').removeClass('drag-handle');
				if (this.reorderable){
					ButtonUtil.enable($("#reorder-test-plan-button"));
				}

			} else {
				filterOn = true;
				table.showFilterFields();
				table.lockSortMode();
				$("#test-plan-sort-mode-message").hide();
				$("#test-cases-table").find('.select-handle').addClass('drag-handle');
				ButtonUtil.disable($("#reorder-test-plan-button"));

			}
		});
	}
	
	return {	
		init : function(origconf){			
			var conf = enhanceConfiguration(origconf);
			_bindButtons(conf);
			table.init(conf);
			popups.init(conf);
			filterOn = false;
		}
	};
	
	
});