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
require([ "common" ], function(common) {
	require([ "jquery", "app/ws/squashtm.workspace", "domReady", "jquery.squash.squashbutton", "jform",
			"squashtest/add-attachment-popup" ], function($, WS, domReady) {

		var AAPS = squashtm.app.addAttachmentPopupSettings;

		// This section handles the transition between phase 1
		// and phase 2, ie all the operations between
		// the moment the user presses "submit" and the end of
		// the download :
		// - prelude to the upload : we'll need a 'ticket' for
		// later use (see below),
		// - close the uploader popup, open the progress bar,
		// - start the uploading request (still stuck to
		// HttpRequests, no XHR for this one)
		// - during the upload, poll the server for the upload
		// using the ticket obtained in step 1
		// - once it's done it will open the uploadSummary popup
		//
		// Notes :
		// - tickets are like sEcho for datatable : an
		// identifier used by the server to know which upload to
		// watch for.
		// - we need the instruction flow to run as if the Ajax
		// calls were synchronous. Since they aren't, we put the
		// next steps of the
		// code in the ajax success handlers.

		// This is the entry point : the user just clicked the
		// "submit" button.
		//
		// first let's warn the server we're going for an upload
		// and request an upload ticket
		// the server returns a string containing the number :
		// that's the ticket.

		function setupAndBeginUpload() {
			$.post(AAPS.uploadAttachmentUrl, function(data) {
				beginUpload(data);
			});
		}

		// once we got a ticket the real thing begins
		function beginUpload(ticket) {
			$("#attachment-progress-dialog").data("ticket", ticket);
			attachmentDisplayBar();
			attachmentSubmit(ticket);
		}

		// the #attachment-progress-dialog dialog opening
		// function is overloaded. See the javascript snippet at
		// the bottom to see how.
		// It will mainly handle the logic regarging the
		// progressbar and polling the server.

		function attachmentDisplayBar() {
			$("#attachment-upload-dialog").dialog("close");
			$("#attachment-progress-dialog").dialog("open");
		}

		// .ajaxSubmit({}) will not treat server response
		// status, will not fire error handlers, and returns a
		// fake xhr object.
		// It wont accept specific response data type so we must
		// parse html, strip the tags and parse json ourselves.
		//			
		// The only solution to process the response is to
		// analyse the content to infer the status.

		function attachmentSubmit(ticket) {
			$("#add-attachment-form").ajaxSubmit({
				url : AAPS.uploadAttachmentUrl + "?upload-ticket=" + ticket,
				dataType : "text/html",
				success : function() {
				},
				error : function() {
				},
				complete : function(jqXHR) {
					attachmentSubmitComplete(ticket, jqXHR);
				},
				target : '#dump'
			});
		}

		function formatToMegabyte(lMaxSize) {
			var mb = lMaxSize / 1048576;
			return mb.toFixed(3);
		}


		// see #attachmentSubmit for details regarding error
		// handling
		function attachmentSubmitComplete(ticket, jqXHR) {

			$("#attachment-progress-dialog").dialog("close");

			// because some browsers find it clever to wrap the
			// raw response inside html tags (no, it's not IE
			// for once)
			// we need to 'unwrap' our nested json response.
			//			
			// in our case, if the json response has an
			// attribute maxSize, then we got an error.

			var text = $(jqXHR.responseText).text();
			var json = $.parseJSON(text);

			if (json.maxSize === undefined) {
				openUploadSummary(ticket);
			} else {
				var maxSize = json.maxSize;
				var message = AAPS.uploadErrorMessage + " (" + formatToMegabyte(maxSize) + " " + AAPS.megaByteLabel;

				squashtm.notification.showInfo(message);

				exitUpload();
			}

		}

		/*******************************************************************************************************************
		 * ======================================================================================== third section : the
		 * final popup giving a summary of the whole operation.
		 * ============================================================================================
		 ******************************************************************************************************************/
		// todo : init the popup with json that'll be loaded
		// using the finalizeUpload on the controller
		function openUploadSummary(ticket) {
			resetUploadSummary();

			$.ajax({
				url : AAPS.uploadAttachmentUrl + "?upload-ticket=" + ticket,
				type : "DELETE",
				dataType : "json",
				success : populateAndOpenSummary
			});

		}

		function resetUploadSummary() {
			$("#attachment-upload-summary-body").html('');
		}

		function populateAndOpenSummary(json) {

			if (json != null) {
				if (!allTransferSuccessful(json)) {
					populateSummary(json);
					$("#attachment-upload-summary").dialog("open");
				} else {
					exitUpload();
				}
			}
		}

		function allTransferSuccessful(json) {
			var summaries = json[0];
			var i = 0;
			var allSuccess = true;
			for (i = 0; i < summaries.length; i++) {
				if (!!summaries[i].iStatus) {
					allSuccess = false;
					break;
				}
			}
			return allSuccess;
		}

		function populateSummary(json) {

			var summaries = json[0];
			var i = 0;

			for (i = 0; i < summaries.length; i++) {
				$("#attachment-upload-summary-body").append(
						"<div class=\"display-table-row\" >" + "<div class=\"display-table-cell\" >" +
								"<label style=\"font-weight:bold;\">" + summaries[i].name + "</label>" + "</div>" +
								"<div class=\"display-table-cell\" >" + "<span>" + summaries[i].status + "</span>" + "</div>" +
								"</div>");
			}

		}

		function exitUpload() {
			$(AAPS).trigger("exitUpload");
		}

		// the section below handle the polling routine.
		// it will periodically call the server for the upload
		// status, with the upload ticket as a reference.

		var uploadIntervalId;

		function updateProgressStatus(data) {

			if (data.percentage < 0) {
				uploadDisplayMessage("Warning : upload progress statistics are invalid");
			}

			else {
				uploadUpdateBar(data.percentage);
				uploadDisplayPercentage(data.percentage);

				if (data.percentage == 100) {
					uploadDisplayMessage(AAPS.uploadCompletedMessage);
				}
			}
		}

		function pollUploadStatus() {
			var ticket = $("#attachment-progress-dialog").data("ticket");
			url = AAPS.uploadAttachmentUrl + "?upload-ticket=" + ticket;
			$.get(url, function(data) {
				updateProgressStatus(data);
			}, "json");
		}

		function uploadUpdateBar(percentage) {
			$("#attachment-progressbar").progressbar("option", "value", percentage);
		}

		function uploadDisplayPercentage(percentage) {
			$("#attachment-progress-percentage").html(percentage.toString() + " &#37;");
		}

		function uploadDisplayMessage(message) {
			$("#attachment-progress-message").html(message);
		}

		domReady(function() {

			if (!AAPS) {
				return;
			}

			/** INIT UPLOAD POPUP * */
			var params = {
				selector : "#attachment-upload-dialog",
				openedBy : "#upload-attachment-button",
				title : AAPS.uploadPopupTitle,
				isContextual : true,
				buttons : [ {
					'text' : AAPS.uploadLabel,
					'click' : function() {
						setupAndBeginUpload();
					}
				}, {
					'text' : AAPS.cancelLabel,
					'click' : function() {
						$(this).data("answer", "cancel");
						$(this).dialog('close');
					}
				} ],
				width : 435,
				open : function() {
					var formInstance = $("#attachment-upload-dialog").data("formInstance");
					formInstance.clear();
				}
			};

			AAPS.uploadPopup = squashtm.popup.create(params);

			var uploadWaitParams = {
				selector : "#attachment-progress-dialog",
				title : AAPS.uploadPleaseWaitTitle,
				isContextual : true,
				buttons : [ {
					'text' : AAPS.cancelLabel,
					'click' : function() {
						$(this).dialog('close');
						window.location.reload();
					}
				} ],
				closeOnSuccess : false
			};

			AAPS.uploadWaitPopup = squashtm.popup.create(uploadWaitParams);

			var uploadSummaryParams = {
				selector : "#attachment-upload-summary",
				title : AAPS.uploadSummaryTitle,
				isContextual : true,
				buttons : [ {
					'text' : AAPS.okLabel,
					'click' : function() {
						$(this).dialog('close');
					}
				} ],
				closeOnSuccess : false,

				close : function() {
					exitUpload();
				}

			};

			AAPS.uploadSummaryPopup = squashtm.popup.create(uploadSummaryParams);

			/** additional setup for the file selection popup * */
			var itemTemplate = $("#add-attachments-templates  .attachment-item");
			var formInstance = $("#add-attachment-form").uploadPopup(itemTemplate);
			$("#attachment-upload-dialog").data("formInstance", formInstance);

			/**
			 * more initiatisation code for the progress bar popup *
			 */

			// popup additional init
			// make it a progressbar
			$("#attachment-progressbar").progressbar({
				value : 0
			});

			// overload the open event of this dialog : reset
			// the bar and init the poll loop;
			$("#attachment-progress-dialog").bind("dialogopen", function() {

				uploadUpdateBar(0);
				uploadDisplayPercentage(0);
				uploadDisplayMessage(AAPS.pleaseWaitMessage);

				uploadIntervalId = setInterval("pollUploadStatus()", 1000);

			});

			// overload the closing handler
			$("#attachment-progress-dialog").bind("dialogclose", function() {
				clearInterval(uploadIntervalId);
			});

		});
	});
});