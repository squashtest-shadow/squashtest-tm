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

define(["jquery", "jquery.squash.datatables"], function($){
	
	
	// ***************** user-permissions-table section *****************************	
	// ************* combo boxes *******************
	
	function bindSelectChange(settings){
		
		$("#user-permissions-table").on('change', 'td.permissions-cell select', function(){
			
			var projectId = settings.basic.projectId;
			
			var $this = $(this);
			var tr = $this.parents("tr").get(0);	
			var data = $("#user-permissions-table").squashTable().fnGetData(tr);
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
	

	
	function drawCallbackFactory(settings){

		var comboTemplate = $("div.permission-select-template > select");
		
		//sort the options of the select
		comboTemplate.append(
			comboTemplate.find('option').get().sort(function(a,b){ 
				return (a.innerText || a.textContent) > (b.innerText || b.textContent);
			})
		);	
		
		function decorateCombo(cell){			
			var value = cell.html();
			var combo = comboTemplate.clone();
			combo.val(value);
			cell.empty().append(combo);
		}
		
		return function(){
			$(this).find('td.permissions-cell').each(function(){
				decorateCombo($(this));
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
				"sUrl": squashtm.app.contextRoot+"/datatables/messages"
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
			"sAjaxSource": squashtm.app.contextRoot+"/generic-projects/"+settings.basic.projectId+"/user-permissions",
			"aaData" : userPermissions,		
			"sDom" : 'ft<"dataTables_footer"lirp>',
			"aaSorting": [[1,'asc']],
			"aoColumnDefs": [	
			    {'bSortable' : false, 'mDataProp' : 'user-index', 'aTargets' : [0], 'sWidth' : '2em', 'sClass' : 'centered'},
			    {'bSortable' : true , 'mDataProp' : 'user.login', 'aTargets' : [1], 'sClass' : 'user-reference centered'},
			    {'bSortable' : true , 'mDataProp' : 'permission-group.qualifiedName', 'aTargets' : [2], 'sClass' : 'permissions-cell centered'},
			    {'bSortable' : false, 'mDataProp' : 'empty-delete-holder', 'aTargets' : [3], 'sWidth' : '2em', 'sClass' : "delete-button centered" }
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
				url : squashtm.app.contextRoot+"/generic-projects/"+settings.basic.projectId+"/users/{user.id}/permissions",
				tooltip : language.deleteTooltip,
				success : refreshTableAndPopup
			},
			bindLinks : {
				list : [{ url : squashtm.app.contextRoot+"/administration/users/{user.id}/info", targetClass : 'user-reference' }]
			}
			
		}
		
		
		$("#user-permissions-table").squashTable(datatableSettings, squashSettings);
		
		bindSelectChange(settings);
	}
	
	
	return {
		initUserPermissions : initUserPermissions
	}
	
	
});
 