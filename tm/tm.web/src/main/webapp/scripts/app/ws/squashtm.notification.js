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
var squashtm = squashtm || {};

define([ "jquery", "app/lnf/Forms" ], function($, Forms) {
	var _config = {};
	
	function handleJsonResponseError(request) {
		/* this pukes an exception if not valid json. there's no other jQuery way to tell */
		var json = $.parseJSON(request.responseText);
		
		if (json != null) {
			if (json.actionValidationError != null) {
				return $.squash.openMessage(_config.errorTitle, json.actionValidationError.message);
			} else {
				if (json.fieldValidationErrors != null) {
					/* IE8 requires low tech code */
					var validationErrorList = json.fieldValidationErrors;
					if (validationErrorList.length > 0) {
						var counter = 0;
						for (counter = 0; counter < validationErrorList.length; counter++) {
							var fve = validationErrorList[counter];
							if (!showLegacyErrorMessage(fve) && !showBootstrapErrorMessage(fve)) {
								throw 'exception';
							}
						}
					}
				} else {
					throw 'exception';
				}
			}
		}
	}
	
	function showLegacyErrorMessage(fieldValidationError) {
		var labelId = fieldValidationError.fieldName + '-error';
		labelId = labelId.replace(".", "-").replace('[', '-').replace(']','');// this is necessary because labelId is used as a css classname
		var label = $('span.error-message.' + labelId);

		if (label.length === 0) {
			return false;
		} 
		
		label.html(fieldValidationError.errorMessage);
		return true;
	}

	function showBootstrapErrorMessage(fieldValidationError) {
		var inputName = fieldValidationError.fieldName.replace(".", "-"),
			$input = $("input[name='" + inputName + "']"),
			input = Forms.input($input);
		
		input.setState("error", fieldValidationError.errorMessage);
		
		return input.hasHelp;
	}

	function handleGenericResponseError(request) {
		var showError = function () {
			var popup = window .open('about:blank', 'error_details',
					'resizable=yes, scrollbars=yes, status=no, menubar=no, toolbar=no, dialog=yes, location=no');
			popup.document.write(request.responseText);
		};
		
		$('#show-generic-error-details')
				.unbind('click')
				.click(showError);

		$('#generic-error-notification-area').fadeIn('slow').delay(20000).fadeOut('slow');
	}

	function displayInformationNotification(message) {
		$.squash.openMessage(_config.infoTitle, message);
	}
	
	function init(config) {
		_config.errorTitle = config.errorTitle;
		_config.infoTitle = config.infoTitle;
		
		var spinner = $("#ajax-processing-indicator");
		spinner.addClass("not-processing").removeClass("processing");
		
		$(".unstyled-notification-pane").addClass("notification-pane").removeClass("unstyled-notification-pane");
		
		/* Does not work with narrowed down selectors. see http://bugs.jquery.com/ticket/6161 */
		$(document).ajaxError(function (event, request, settings, ex) {
			// Check if we get an Unauthorized access response, then redirect to login page
			if (401 == request.status) {
				window.parent.location.reload();
			} else {
				try {
					handleJsonResponseError(request);
				} catch (wtf) {
					handleGenericResponseError(request);
				}
			}
		}).ajaxStart(function () {
			spinner.addClass("processing").removeClass("not-processing");
			
		}).ajaxStop(function () {
			spinner.removeClass("processing").addClass("not-processing");
			
		});
	}
	
	function getErrorMessage(request, index){
		var json = $.parseJSON(request.responseText);
		
		if (json != null) {
			if (json.actionValidationError != null) {
				return json.actionValidationError.message;
			} else {
				if (json.fieldValidationErrors != null) {
					/* IE8 requires low tech code */
					var validationErrorList = json.fieldValidationErrors;
					if (validationErrorList.length > 0) {
						var ind = (index!==undefined) ? index : 0;
						return validationErrorList[ind].errorMessage;
					}
				} else {
					throw 'exception';
				}
			}
		}		
	}

	squashtm.notification = {
		init : init, 
		showInfo : displayInformationNotification,
		getErrorMessage : getErrorMessage, 
		handleJsonResponseError : handleJsonResponseError
		
	};
	
	return squashtm.notification;
});
