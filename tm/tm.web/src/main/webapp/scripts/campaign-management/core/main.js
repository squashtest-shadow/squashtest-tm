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


/*
 * Conf : see the conf of the main module + the following metadata generated in the main module : 
 * 
 * {
 *  data:{
 *  identity : {
 *		resid : equivalent to campaignId,
 *		restype : hardcoded to "campaigns", considering that we are in the campaign core init module
 *  },
 *  dashboard : see the parameters for the dashboard. Here we define a master and a cache key 
 * 
 * }
 * }
 * 
 */
define(["jquery", "squash.basicwidgets", "contextual-content-handlers", "jquery.squash.fragmenttabs", 
        "bugtracker/bugtracker-panel", "workspace.event-bus",  "squash.translator",  
        "dashboard/campaigns-dashboard/main", "../planning/main", "../test-plan-panel/main",
        "custom-field-values",
        "jqueryui", "jquery.squash.formdialog"], 
        function($, basicwidg, contentHandlers, Frag, bugtrackerPanel, eventBus, translator, 
        dashboard, planning, testplan, cufvalues){
	
	
	function init(conf){
		
		basicwidg.init();

		initTabs(conf);
		
		initCufs(conf);
		
		initRenameHandler(conf);
		
		initRenameDialog(conf);
		
		
		initPlanning(conf);
		
		initDashboard(conf);
		
		initTestplan(conf);
		
		
		initBugtracker(conf);
		
	}
	
	function initCufs(conf){
		if (conf.features.hasCUF){
			var url = conf.data.cufValuesUrl + "?boundEntityId="+conf.data.campaignId+"&boundEntityType=CAMPAIGN";
			$.getJSON(url)
			.success(function(jsonCufs){
				$("#campaign-custom-fields-content .waiting-loading").hide();
				var mode = (conf.features.writable) ? "jeditable" : "static";
				cufvalues.infoSupport.init("#campaign-custom-fields-content", jsonCufs, mode);
			});
		}
	}
	
	function initRenameHandler(conf){		
		var nameHandler = contentHandlers.getSimpleNameHandler();
		nameHandler.identity = conf.data.identity;
		nameHandler.nameDisplay = "#campaign-name";		
	}
	
	function initTabs(conf){
		var fragConf = {
			cookie : "iteration-tab-cookie",	// FIXME : was that a copy pasta ?
			activate : function(event, ui){
				if (ui.newPanel.is("#campaign-dashboard")){
					eventBus.trigger('dashboard.appear');
				}
			}
		};
		
		Frag.init(fragConf);
	}
	
	function initBugtracker(conf){
		if (conf.features.hasBugtracker){
			bugtrackerPanel.load({
				url : conf.data.bugtrackerUrl,
				style : "fragment-tab"
			});
		}
	}
	
	function initPlanning(conf){
		if (conf.features.writable){
			planning.init(conf);
		}
	}
	
	function initDashboard(conf){
		dashboard.init(conf.dashboard);
	}
	
	function initTestplan(conf){
		testplan.init(conf);
	}
	
	function initRenameDialog(conf){
		
		var dialog = $("#rename-campaign-dialog"),
			campaignUrl = conf.data.campaignUrl,
			campaignId = conf.data.campaignId;
		
		dialog.formDialog();
		
		dialog.on("formdialogopen", function(){
			var name = $('#campaign-name').text();
			var trimmed = $.trim(name);
			$("#rename-campaign-name").val(trimmed);
		});
		
		dialog.on('formdialogconfirm', function(){
			var newName = $("#rename-campaign-name").val();
			$.ajax({
				url : campaignUrl,
				type : 'POST',
				dataType  : 'json',
				data : { newName : newName}
			})
			.done(function(data){
				dialog.formDialog('close');
				eventBus.trigger('node.rename', { identity : conf.data.identity, newName : data.newName});
			});
		});
		
		dialog.on('formdialogcancel', function(){
			dialog.formDialog('close');
		});
		
		$("#rename-campaign-button").on('click', function(){
			dialog.formDialog('open');
		});
		
	}
	
	return {
		init : init
	};
	
	
});