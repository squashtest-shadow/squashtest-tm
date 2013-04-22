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
/**
 * settings : 
 * {
 * 	data : {
 * 		tableData : the json model of the data displayed by the datatable 
 * 	},
 * 
 * 	urls : {
 * 		rootContext : the base url of the application
 * 		backUrl : the url where to go where to go when clicking the #back button
 * 		baseUrl : the base url of all regaring user adminitration (listing, adding, removing etc)
 * 	},
 * 	language : {
 * 		missingNewPassword : error message when the new password input wasn't typed in
 * 		missingConfirmPassword : same, for the confirmation input
 * 		differentConfirmation : error message when the new password and confirmation differ
 * 		deleteMessage : message displayed when clicking on the delete button
 * 		deleteTooltip : the tooltip for that button 
 * 		ok : label for ok
 * 		cancel : label for cancel 
 * }
 * }
 */

define(["jquery", "jquery.squash.datatables"], function($){
	
	function cleanUp(){
		$("#add-user-password").val('');
		$("#new-user-confirmpass").val('');		
	}
	

	function refreshUsers() {
		var dataTable = $('#users-list-table').squashTable();
		dataTable.fnDraw();
	}

	

	//note : I don't trust hasOwnProperty due to its cross-browser issues. We'll
	//do it low tech once again.
	function isFilled(selector){
		var value = $(selector).val();
		if (value.length==0){
			return false;
		}else{
			return true;
		}
		
	}
	
	
	function buildPasswordValidation(language){
		return function(){
			var lang = language;
			
			//first, clear error messages
			$("#add-user-table span.error-message").html('');
	
			var newPassOkay=true;
			var confirmPassOkay=true;
			var samePassesOkay=true;
			
			if (! isFilled("#add-user-password")){
				$("span.error-message.password-error").html(lang.missingNewPassword);
				newPassOkay=false;
			}
	
			if (! isFilled("#new-user-confirmpass")){
				$("span.error-message.confirmpass-error").html(lang.missingConfirmPassword);
				confirmPassOkay=false;
			}				
			
			if ((newPassOkay==true) && (confirmPassOkay==true)){
				var pass = $("#add-user-password").val();
				var confirm = $("#new-user-confirmpass").val();
				
				if ( pass != confirm){
					$("span.error-message.password-error").html(lang.differentConfirmation);
					samePassesOkay=false;
				}
			}
			
			return ( (newPassOkay) && (confirmPassOkay) &&(samePassesOkay) );
			
		}
	}
	
	function readForm(){
		return {
			login : $("#add-user-login").val(),
			firstName : $("#add-user-firstName").val(),
			lastName : $("#add-user-lastName").val(),
			email : $("#add-user-email").val(),
			groupId : $("#add-user-group").val(),
			password : $("#add-user-password").val()
		}
	}
	
	function buildAddUserConfirm(settings, validatePassword){
		return function(){
			if (! validatePassword()) return;
			var url = settings.urls.baseUrl+"/add";
			$.ajax({
				url : url,
				type : 'POST',
				dataType : 'json',
				data : readForm(),
			}).success(refreshUsers);
		}
	}
	
	
	function initButtons(settings){
		$('#add-user-button').button();
		$("#add-user-dialog").bind( "dialogclose", cleanUp);
		
		$("#back").button().click(function(){
			document.location.href = settings.urls.backUrl;
		});		
	}
	


	
	function initTable(settings){
		var datatableSettings = {
			"oLanguage": {
				"sUrl": settings.urls.rootContext+"datatables/messages"
			},
			"bJQueryUI": true,
			"bAutoWidth": false,
			"bFilter": true,
			"bPaginate": true,
			"sPaginationType": "squash",
			"iDisplayLength": 10,
			"iDeferLoading" : settings.data.tableData.length, 
			"bProcessing": true,
			"bServerSide": true,
			"sAjaxSource": settings.urls.baseUrl+'/table',
			"aaData" : settings.data.tableData,		
			"sDom" : 'ft<"dataTables_footer"lirp>',
			"aoColumnDefs": [
				{ 'bVisible':false, 'bSortable':false, 'mDataProp':'user-id', 		      'aTargets':['user-id'] },
				{ 'bVisible':true,  'bSortable':false,  'mDataProp':'user-index', 	      'aTargets':['user-index'], 'sWidth':'2em', 'sClass':'select-handle centered' },
				{ 'bVisible':true,  'bSortable':true,  'mDataProp':'user-login', 	      'aTargets':['user-login'], 'sClass':'user-reference'},
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-group', 	      'aTargets':['user-group'] },
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-firstname',      'aTargets':['user-firstname'] },
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-lastname',       'aTargets':['user-lastname'] },
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-email', 	      'aTargets':['user-email'] },
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-created-on',     'aTargets':['user-created-on'] },
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-created-by',     'aTargets':['user-created-by'] },
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-modified-on',    'aTargets':['user-modified-on'] },
				{ 'bVisible':true, 	'bSortable':true,  'mDataProp':'user-modified-by',    'aTargets':['user-modified-by'], 'sWidth':'2em'},
				{ 'bVisible':true, 	'bSortable':false, 'mDataProp':'empty-delete-holder', 'aTargets':['empty-delete-holder'], 'sWidth':'2em', 'sClass':'centered delete-button'}
				
			] 
		};
		
		var squashSettings = {
			enableHover : true,
			confirmPopup :{
				oklabel : settings.language.ok,
				cancellabel : settings.language.cancel
			},
			deleteButtons : {
				url : settings.urls.baseUrl+"/{user-id}",
				popupmessage : settings.language.deleteMessage,
				tooltip : settings.language.deleteTooltip,
				success : function(){
					refreshUsers();
				}
			},
			bindLinks : {
				list : [
				       {
				    	   url : settings.urls.baseUrl+'/{user-id}/info',
				    	   targetClass : 'user-reference'
				       }
				]
			}
		};
		
		$("#users-list-table").squashTable(datatableSettings, squashSettings);
			
	}
	
	function initDialog(settings){
		var passValidation = buildPasswordValidation(settings.language);
		var addUserConfirm = buildAddUserConfirm(settings, passValidation);
		$("#add-user-dialog").data('confirm-handler', addUserConfirm);	
	}
	
	
	function init(settings){
		initButtons(settings);
		initTable(settings);
		initDialog(settings);
	}
	
	
	return init;
	

});