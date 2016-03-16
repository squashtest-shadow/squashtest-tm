/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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
 * Controller for the notification area.
 */
var squashtm = squashtm || {};

define([ "jquery", "app/pubsub", "squash.translator", "app/lnf/Forms", "jquery.squash.messagedialog" ], function($, ps,
		translator, Forms) {

	var _config = translator.get({
		errorTitle : "popup.title.error",
		infoTitle : "popup.title.info"
	});

	var _spinner = "#ajax-processing-indicator";
	var _widgetsInitialized = false;
	var _spinnerInitialized = false;

	function initWidgets() {
		if (!_widgetsInitialized) {
			_widgetsInitialized = true;
			$(".unstyled-notification-pane").addClass("notification-pane").removeClass("unstyled-notification-pane");
			$(_spinner).addClass("not-processing").removeClass("processing");
			$("#generic-error-dialog").messageDialog();
			$("#generic-warning-dialog").messageDialog();
		}
	}

	
	function initSpinner() {

		if (_spinnerInitialized){
			return;
		}
		
		_spinnerInitialized = true;
		
		var $doc = $(document);

		/*
		 * Does not work with narrowed down selectors. see http://bugs.jquery.com/ticket/6161
		 */
		$doc.on('ajaxError', function(event, request, settings, ex) {

			// nothing to notify if the request was aborted, or was treated elsewhere
			if (request.status === 0 || request.errorIsHandled === true) {
				return;
			}

			// Check if we get an Unauthorized access response, then
			// redirect to login page
			else if (401 == request.status) {
				window.parent.location.reload();
			} else {
				try {
					handleJsonResponseError(request);
				} catch (wtf) {
					handleGenericResponseError(request);
				}
			}
		});

		$.ajaxPrefilter(function(options, _, jqXHR) {
			$(_spinner).addClass("processing").removeClass("not-processing");

			jqXHR.always(function() {
				$(_spinner).removeClass("processing").addClass("not-processing");
			});
		});

	}


	// TODO : see if we can factor some logic with getErrorMessage
	function handleJsonResponseError(request) {
		/*
		 * this pukes an exception if not valid json. there's no other jQuery way to tell
		 */
		var json = $.parseJSON(request.responseText);

		if (json !== null) {
			if (!!json.actionValidationError) {
				showError(json.actionValidationError.message);
			} else if (!!json.fieldValidationErrors) {
				/* IE8 requires low tech code */
				var validationErrorList = json.fieldValidationErrors;
				if (validationErrorList.length > 0) {
					for ( var counter = 0; counter < validationErrorList.length; counter++) {
						var fve = validationErrorList[counter];
						if (!!request.label) {
							request.label.html(fve.errorMessage);
						} else if (!showBootstrapErrorMessage(fve) && !showLegacyErrorMessage(fve) 
								&& !showBootstrapErrorMessageWithDataObjectAndDataProp(fve)) {
							throw 'exception';
						}
					}
				}
			} else {
				throw 'exception';
			}

		} else {
			throw 'exception';
		}
	}

	
	
	// TODO : see if we can factor some logic with handleJsonResponseError
	function getErrorMessage(request, index) {
		var json = $.parseJSON(request.responseText);

		if (!! json ) {
			if (!! json.actionValidationError ) {
				return json.actionValidationError.message;
			} else {
				if (!! json.fieldValidationErrors) {
					/* IE8 requires low tech code */
					var validationErrorList = json.fieldValidationErrors;
					if (validationErrorList.length > 0) {
						var ind = (index !== undefined) ? index : 0;
						return validationErrorList[ind].errorMessage;
					}
				} else {
					throw 'exception';
				}
			}
		}
	}
	
	
	function showLegacyErrorMessage(fieldValidationError) {
		var labelId = fieldValidationError.fieldName + '-error';
		labelId = labelId.replace(".", "-").replace('[', '-').replace(']', '');// this is necessary because labelId is
		// used
		// as a css classname
		var label = $('span.error-message.' + labelId);

		if (label.length === 0) {
			return false;
		}

		label.html(fieldValidationError.errorMessage);
		return true;
	}

	function showBootstrapErrorMessage(fieldValidationError) {
		var inputName = fieldValidationError.fieldName;
		if (!!fieldValidationError.objectName) {
			inputName = fieldValidationError.objectName + "-" + inputName;
		}

		$input = $("input[name='" + inputName + "'], input[id='" + inputName + "'],  textarea[name='" + inputName +
				"'], #"+inputName);
		input = Forms.input($input);

		input.setState("error", fieldValidationError.errorMessage);

		return input.hasHelp;
	}
	
	function showBootstrapErrorMessageWithDataObjectAndDataProp(fieldValidationError){
		var inputName = fieldValidationError.fieldName;
		var objectName = fieldValidationError.objectName;
		
		$input = $("input[data-prop='" + inputName + "'][data-object='" + objectName + "']");
		
		input = Forms.input($input);

		input.setState("error", fieldValidationError.errorMessage);

		return input.hasHelp;
	}

	function handleGenericResponseError(request) {
		var handle = function() {

			var popup = window.open('about:blank', 'error_details',
					'resizable=yes, scrollbars=yes, status=no, menubar=no, toolbar=no, dialog=yes, location=no');
			if (request.responseText) {
				popup.document.write(request.responseText);
			} else {
				popup.document.write(JSON.stringify(request));
			}
		};

		$('#show-generic-error-details').unbind('click').click(handle);

		$('#generic-error-notification-area').fadeIn('slow').delay(20000).fadeOut('slow');
	}

	function displayInformationNotification(message) {
		$.squash.openMessage(_config.infoTitle, message);
	}
	
	function showError(message){
		var dialog = $("#generic-error-dialog"); 
		dialog.find('.generic-error-main').html(message);
		dialog.messageDialog('open');
	}
	
	function showWarning(message){
		var dialog = $("#generic-warning-dialog"); 
		dialog.find('.generic-warning-main').html(message);
		dialog.messageDialog('open');
	}

	function init() {
		initWidgets();
		initSpinner();
	}

	
	function handleUnknownTypeError(xhr) {
		try {
			handleJsonResponseError(xhr);
		} catch (parseException) {
			handleGenericResponseError(xhr);
		}
	}
	
	function showXhrInDialog(xhr){
		var msg = this.getErrorMessage(xhr);
		this.showError(msg);
	}
	
	squashtm.notification = {
		init : init,
		showInfo : displayInformationNotification,
		showError : showError,
		showWarning : showWarning,
		getErrorMessage : getErrorMessage,
		handleJsonResponseError : handleJsonResponseError,
		handleGenericResponseError : handleGenericResponseError,
		handleUnknownTypeError : handleUnknownTypeError,
		showXhrInDialog : showXhrInDialog
	};

	return squashtm.notification;
});
