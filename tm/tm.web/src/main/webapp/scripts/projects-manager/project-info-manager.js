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

require("jquery", "jquery.squash.datatables", function($){
	
	
	// ***************** user-permissions-table section *****************************
	
	
	// ************* combo boxes *******************
	
	function bindSelectChange(settings){
		
		$("#user-permissions-table").on('change', 'td.permissions-cell select', function(){
			
			var projectId = settings.basic.projectId;
			
			var $this = $(this);
			var tr = $this.parents("tr").get(0);	
			var data = $("#user-permisisons-table").squashTable().fnData(tr);
			var userId = data['user']['id'];			
			
			var url = squashtm.app.contextRoot+"/generic-projects/"+projectId+"/users/"+userId+"/permissions/"+$this.val();
	
			$.ajax({
				  type: 'POST',
				  url: url,
				  dataType: 'json',
				  success: refreshTableAndPopup	//defined in the surrounding html, need to be moved elsewhere too 
			});
		});
		
	}
	
	
	function comboTemplateFactory = function(settings){
		var comboTemplate = $("<select/>");			
		
		var permissions = settings.basic.availablePermissions,
			i = 0,
			length = permissions.length;
		
		for (i=0;i<length;i++){
			var option = $("<option/>", { 'value' : permissions[i].id, 'text' : permissions[i].qualifiedName }).appendTo(comboTemplate);
		}		
		
		return comboTemplate;
	}
	
	
	function drawCallbackFactory(settings){
		
		var comboTemplate = comboTemplateFactory(settings);
		
		function decorateCombos(cell){			
			var id = cell.html();
			var combo = comboTemplate.clone();
			combo.val(id);
			cell.empty().append(combo);
		}
		
		return function(){
			$(this).find('td.permission-cell').each(function(){
				decorateCombo(this);
			});		
		}
	}
	
	
	// ************ init table *********************
	
	
	function initUserPermissions(settings){
		
		var drawCallback = drawCallbackFactory(settings);		
		var userPermissions = settings.basic.userPermissions;
		
		var language = settings.language; 
		
		var datatableSettings = {
			"oLanguage": {
				"sUrl": settings.urls.contextRoot+"datatables/messages"
			},
			"bJQueryUI": true,
			"bAutoWidth": false,
			"bFilter": true,
			"bPaginate": true,
			"sPaginationType": "squash",
			"iDisplayLength": 10,
			"fnDrawCallback" : drawCallback,
			"iDeferLoading" : userPermissions.length,
			"bProcessing": true,
			"bServerSide": true,
			"sAjaxSource": squashtm.app.contextRoot+"generic-projects/"+settings.basic.projectId+"/users-permissions",
			"aaData" : userPermissions,		
			"sDom" : 'ft<"dataTables_footer"lirp>',
			"aoColumnDefs": [	
			    {'mDataProp' : 'user-index', 'aTargets' : [0], 'sWidth' : '2em'},
			    {'mDataProp' : 'user.login', 'aTargets' : [1], 'sClass' : 'user-reference centered'},
			    {'mDataProp' : 'user-permission.id', 'aTargets' : [2], 'sClass' : 'permissions-cell centered'},
			    {'mDataProp' : 'empty-delete-holder', 'aTargets' : [3], 'sWidth' : '2em', 'sClass' : "delete-button centered" }
			] 				
		};
		
		//configure the delete button and the hlink to the user
		var squashSettings = {
			enableHover : true,
			confirmPopup : {
				oklabel : language.ok,
				cancellabel : language.cancel
			},
			deleteButtons : {
				popupmessage : language.deleteMessage,
				url : squashtm.app.contextRoot+"generic-projects/"+settings.basic.projectId+"/users/{user.id}/permissions",
				tooltip : language.deleteTooltip,
				success : refreshTableAndPopup
			},
			bindLinks : {
				list : [{ url : squashtm.app.contextRoot+"/administration/users/{user.id}", targetClass : 'user-reference' }]
			}
			
		}
		
		
		$("#user-permissions-table").squashTable(datatableSettings, squashSettings);
		
		bindSelectChange(settings);
	}
	
	
	return {
		initUserPermissions : initUserPermissions,

	}
	
	
});
 