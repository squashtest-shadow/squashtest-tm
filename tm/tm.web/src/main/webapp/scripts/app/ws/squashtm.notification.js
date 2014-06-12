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
/**
 * Controller for the notification area.
 */
var squashtm = squashtm || {};

define([ "jquery", "app/pubsub", "squash.translator", "app/lnf/Forms", "jquery.squash.messagedialog" ], function($, ps,
		translator, Forms) {

	var _config = translator.get({
		errorTitle : "popup.title.info",
		infoTitle : "popup.title.error"
	});

	var _spinner = "#ajax-processing-indicator";
	var _widgetsInitialized = false;

	function initWidgets() {
		if (!_widgetsInitialized) {
			_widgetsInitialized = true;
			$(".unstyled-notification-pane").addClass("notification-pane").removeClass("unstyled-notification-pane");
			$(_spinner).addClass("not-processing").removeClass("processing");

		}
	}

	function handleJsonResponseError(request) {
		/*
		 * this pukes an exception if not valid json. there's no other jQuery way to tell
		 */
		var json = $.parseJSON(request.responseText);

		if (json !== null) {
			if (!!json.actionValidationError) {
				return $.squash.openMessage(_config.errorTitle, json.actionValidationError.message);
			} else if (!!json.fieldValidationErrors) {
				/* IE8 requires low tech code */
				var validationErrorList = json.fieldValidationErrors;
				if (validationErrorList.length > 0) {
					for ( var counter = 0; counter < validationErrorList.length; counter++) {
						var fve = validationErrorList[counter];
						if (!!request.label) {
							request.label.html(fve.errorMessage);
						} else if (!showBootstrapErrorMessage(fve) && !showLegacyErrorMessage(fve)) {
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
				"']");
		input = Forms.input($input);

		input.setState("error", fieldValidationError.errorMessage);

		return input.hasHelp;
	}

	function handleGenericResponseError(request) {
		var showError = function() {

			var popup = window.open('about:blank', 'error_details',
					'resizable=yes, scrollbars=yes, status=no, menubar=no, toolbar=no, dialog=yes, location=no');
			if (request.responseText) {
				popup.document.write(request.responseText);
			} else {
				popup.document.write(JSON.stringify(request));
			}
		};

		$('#show-generic-error-details').unbind('click').click(showError);

		$('#generic-error-notification-area').fadeIn('slow').delay(20000).fadeOut('slow');
	}

	function displayInformationNotification(message) {
		$.squash.openMessage(_config.infoTitle, message);
	}

	function initSpinner() {

		var $doc = $(document);

		/*
		 * Does not work with narrowed down selectors. see http://bugs.jquery.com/ticket/6161
		 */
		$doc.on('ajaxError', function(event, request, settings, ex) {

			// nothing to notify if the request was aborted
			if (request.status === 0) {
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

	function init() {
		initWidgets();
		initSpinner();
	}

	function getErrorMessage(request, index) {
		var json = $.parseJSON(request.responseText);

		if (json !== null) {
			if (json.actionValidationError !== null) {
				return json.actionValidationError.message;
			} else {
				if (json.fieldValidationErrors !== null) {
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
	function handleUnknownTypeError(xhr) {
		try {
			handleJsonResponseError(xhr);
		} catch (parseException) {
			handleGenericResponseError(xhr);
		}
	}
	squashtm.notification = {
		init : init,
		showInfo : displayInformationNotification,
		getErrorMessage : getErrorMessage,
		handleJsonResponseError : handleJsonResponseError,
		handleGenericResponseError : handleGenericResponseError,
		handleUnknownTypeError : handleUnknownTypeError

	};

	return squashtm.notification;
});
