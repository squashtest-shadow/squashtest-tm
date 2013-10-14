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
 * settings : { data : { tableData : the json model of the data displayed by the
 * datatable },
 * 
 * urls : { rootContext : the base url of the application backUrl : the url
 * where to go where to go when clicking the #back button baseUrl : the base url
 * of all regaring user adminitration (listing, adding, removing etc) },
 * language : { missingNewPassword : error message when the new password input
 * wasn't typed in missingConfirmPassword : same, for the confirmation input
 * differentConfirmation : error message when the new password and confirmation
 * button ok : label for ok cancel : label
 * for cancel } }
 */

define([ "jquery", "squash.translator", "squashtable", "jquery.squash.confirmdialog" ], function($, translator) {

	// ---------------- add user dialog ----------------------------
	
	function cleanUp() {
		$("#add-user-password").val('');
		$("#new-user-confirmpass").val('');
	}


	// note : I don't trust hasOwnProperty due to its cross-browser issues.
	// We'll
	// do it low tech once again.
	function isFilled(selector) {
		var value = $(selector).val();
		if (!value.length) {
			return false;
		} else {
			return true;
		}

	}

	function buildPasswordValidation(settings) {
		if (settings.managedPassword === true) {
			// password is managed by provider, we don't perform any password check
			return function() { return true; };
		}
		
		var language = settings.language;
		return function() {
			var lang = language;

			// first, clear error messages
			$("#add-user-table span.error-message").html('');

			var newPassOkay = true;
			var confirmPassOkay = true;
			var samePassesOkay = true;

			if (!isFilled("#add-user-password")) {
				$("span.error-message.password-error").html(
						lang.missingNewPassword);
				newPassOkay = false;
			}

			if (!isFilled("#new-user-confirmpass")) {
				$("span.error-message.confirmpass-error").html(
						lang.missingConfirmPassword);
				confirmPassOkay = false;
			}

			if ((newPassOkay) && (confirmPassOkay)) {
				var pass = $("#add-user-password").val();
				var confirm = $("#new-user-confirmpass").val();

				if (pass != confirm) {
					$("span.error-message.password-error").html(
							lang.differentConfirmation);
					samePassesOkay = false;
				}
			}

			return ((newPassOkay) && (confirmPassOkay) && (samePassesOkay));

		};
	}

	function readForm(settings) {
		var form = {
			login : $("#add-user-login").val(),
			firstName : $("#add-user-firstName").val(),
			lastName : $("#add-user-lastName").val(),
			email : $("#add-user-email").val(),
			groupId : $("#add-user-group").val()
		};
		
		if (settings.managedPassword) {
			form.noPassword = true; 
		} else {
			form.password = $("#add-user-password").val();
		}
		
		return form;
	}

	function buildAddUserConfirm(settings, validatePassword) {
		return function() {
			if (!validatePassword()){
				return;
			}
			var url = settings.urls.baseUrl + "/new";
			$.ajax({
				url : url,
				type : 'POST',
				dataType : 'json',
				data : readForm(settings)
			}).success(function(){
				$('#users-list-table').squashTable().refresh();
			});
		};
	}
	
	
	// ------------- dialog init -----------------
	
	function initDialog(settings) {
		
		// new user popup
		
		var passValidation = buildPasswordValidation(settings);
		var addUserConfirm = buildAddUserConfirm(settings, passValidation);
		$("#add-user-dialog").data('confirm-handler', addUserConfirm);
		$("#add-user-dialog").bind("dialogclose", cleanUp);
		
		
		// activation / deactivation
		
		function xhrActivateDeactivate(action, callbackName){
			var $this = $(this),
				table = $("#users-list-table").squashTable();
					
			var userId = $this.data('entity-id'),
				userIds = (!! userId) ? [ userId ] : table.getSelectedIds();
	
			$this.data('entity-id');	//reset
			
			var url = squashtm.app.contextRoot+"/administration/users/"+userIds.join(',')+'/'+action;
			$.post(url)
			.done(function(){
				table[callbackName](userIds);
			});			
		}
		
		// confirm deactivation
		
		$("#activate-user-popup").confirmDialog().on('confirmdialogconfirm', function(){			
			xhrActivateDeactivate.call(this, 'activate', 'activateUsers');			
		});
		
		// confirm activation
		
		$("#deactivate-user-popup").confirmDialog().on('confirmdialogconfirm', function(){			
			xhrActivateDeactivate.call(this, 'deactivate', 'deactivateUsers');			
		});		
		
		// confirm deleteion
		$("#delete-user-popup").confirmDialog().on('confirmdialogconfirm', function(){
			var $this = $(this),
			table = $("#users-list-table").squashTable();
					
			var userId = $this.data('entity-id'),
				userIds = (!! userId) ? [ userId ] : table.getSelectedIds();
	
			$this.data('entity-id');	//reset		
			var url = squashtm.app.contextRoot+'/administration/users/'+userIds.join(',');
			$.ajax({
				url : url,
				type : 'delete'
			})
			.done(function(){
				table.refresh();
			});
		});
		
	}
	
	
	// ---------------------- button ----------------------

	function initButtons(settings) {
		
		
		function openpopup(selector){
			var ids = $("#users-list-table").squashTable().getSelectedIds();
			if (ids.length>0){
				var popup = $(selector);
				popup.data('entity-id',null);
				popup.confirmDialog('open');
			}
			else{
				var warn = translator.get({
					errorTitle : 'popup.title.Info',
					errorMessage : 'message.EmptyTableSelection'
				});
				$.squash.openMessage(warn.errorTitle, warn.errorMessage);
			}		
		}
		
		$('#add-user-button').button();
		
		$("#deactivate-user-button").button().on('click', function(){
			openpopup("#deactivate-user-popup");
		});
		
		$("#activate-user-button").button().on('click', function(){			
			openpopup("#activate-user-popup");			
		});
		
		$("#delete-user-button").button().on('click', function(){
			openpopup("#delete-user-popup");
		});
		
		$("#back").button().click(function() {
			document.location.href = settings.urls.backUrl;
		});
	}

	// ----------- table ---------------------
	
	function drawCallback(){
		var table = this;
		
		// activation button
		
		useractiveCells = table.find('tbody .user-active-cell');
		
		useractiveCells.each(function(){
			
			var value = table.fnGetData(this),
				$cell = $(this);
			
			var btnclass = (value) ? 'table-icon user-active-btn icon-user-activated' : 'table-icon user-active-btn icon-user-deactivated disabled-transparent';
			
			$cell.empty().append('<a href="#" class="'+btnclass+'"/>');
			
		});
		
	}
	
	function initTable(settings) {
			
		var datatableSettings = {
			"aaData" : settings.data.tableData,
			"fnDrawCallback" : drawCallback
		};
		
		var squashSettings = {
			functions : {
				
				_changeActivation : function(ids, value, cssclass){
					var _table = this;
					var rows = _table.getRowsByIds(ids);
					rows.each(function(){
						var data = _table.fnGetData(this);
						data['user-active'] = value;						
					});
					rows.find('a.user-active-btn').removeClass().addClass(cssclass);
				},
				
				activateUsers : function(ids){
					this._changeActivation(ids, true, "table-icon user-active-btn icon-user-activated");
				},
				
				deactivateUsers : function(ids){
					this._changeActivation(ids, false, "table-icon user-active-btn icon-user-deactivated disabled-transparent");
				}
			}
		};

		var table = $("#users-list-table").squashTable(datatableSettings, squashSettings);
		
		// various hooks
		
		table.on('click', 'a.user-active-btn', function(evt){
			
			var tr = this.parentNode.parentNode;
				data = table.fnGetData(tr),
				id = data['user-id'],
				active = data['user-active'];
			
			if (active){
				$("#deactivate-user-popup").data('entity-id', id).confirmDialog('open');
			}else{
				$("#activate-user-popup").data('entity-id', id).confirmDialog('open');				
			}	
			
			
		});

	}
	



	function init(settings) {
		initButtons(settings);
		initTable(settings);
		initDialog(settings);
	}

	return init;

});