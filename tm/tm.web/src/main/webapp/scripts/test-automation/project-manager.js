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
 settings :
 - tmProjectURL : the url of the TM project
 - availableServers : an array of TestAutomationServer
 - TAServerId : the id of the selected server if there is one, or null if none
 */
define(["jquery", "squash.translator", "squash.configmanager", 
        "squashtable", "jeditable", "jquery.squash.formdialog"], function($, translator, confman) {
	
	function initSelect(conf){

		var data = {
			'0' : translator.get('label.NoServer')
		};
		
		for (var i=0, len=conf.availableServers.length; i<len ;i++){
			var server = conf.availableServers[i];
			data[server.id] = server.name;
		}
		
		if (conf.TAServerId !== null){
			data['selected'] = conf.TAServerId;
		}
		
		var selectConf = confman.getJeditableSelect();
		var url = conf.tmProjectURL + '/test-automation-server';
		selectConf.data = data;
		
		// warning : there will be tricky tricks involving the confirmChangePopup
		// but that's your job now, I'm in hollidays :P have fun
		selectConf.onsubmit = function(settings, original){
			$("#ta-server-confirm-popup").formDialog('open');
			return false;	// must return false to prevent the editable to submit right away
		}
		
		$("#selected-ta-server-span").editable(url, selectConf);
		
	}
	
	function initConfirmChangePopup(){
		
		var dialog = $("#ta-server-confirm-popup");
		var select = $("#selected-ta-server-span");
		
		dialog.formDialog();
		
		dialog.on('formdialogconfirm', function(){
			alert('confirmed !')
			// also make sure to resume the jeditable submission routine.
		});
		
		dialog.on('formdialogcancel', function(){
			alert('cancelled !')
		});
	}
	
	function initBindPopup(){
		var dialog = $("#ta-projects-bind-popup");
		
		dialog.formDialog({height : 500});
		
		dialog.on('formdialogopen', function(){
			dialog.formDialog('setState', 'pleasewait');
		});
		
		dialog.on('formdialogconfirm', function(){
			alert('Confirmed !');
		});
		
		dialog.on('formdialogcancel', function(){
			alert('Canceled !');
		});
		
		$("#ta-projects-bind-button").on('click', function(){
			dialog.formDialog('open');
		});
	}

	function initUnbindPopup(){
		
		var dialog = $("#ta-projects-unbind-popup"); 
		dialog.formDialog();
		
		dialog.on('formdialogconfirm', function(){
			var deletedId = dialog.data('entity-id');
			alert('Confirmed deletion of '+deletedId+'!' );
		});
		
		dialog.on('formdialogcancel', function(){
			alert('Canceled !');
		});
	}
	
	function initTable(){
		var table = $("#ta-projects-table").squashTable({}, {});
		
	}
	
	
	
	return {
		
		init : function(conf){
			
			initSelect(conf);
			initBindPopup();
			initConfirmChangePopup();
			initUnbindPopup();
			initTable();
			
		}	
				
	}
});