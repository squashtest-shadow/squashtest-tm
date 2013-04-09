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
	require([ "jquery", "domReady", "squashtest/attachment-bloc",
			"jquery.squash", "jqueryui", "jquery.squash.togglepanel",
			"add-attachment-popup.frag" ], function($, domReady) {

		var ABS = squashtm.app.attachmentBlocSettings;

		function reloadAttachments() {
			$("#attachment-container").load(ABS.attachmentsList,
					reloadAttachmentCallback);
		}

		function reloadAttachmentCallback() {
			handleNotFoundImages(squashtm.app.contextRoot
					+ "images/file_blank.png");
			openAttachmentIfNotEmpty();
		}

		domReady(function() {
			if (!ABS) {
				return;
			}

			var panelSettings = {
				initiallyOpen : ABS.attachmentBlocOpened,
				title : ABS.attachmentBlocTitle
			};
			$("#attachment-panel").togglePanel(panelSettings);
			$("#manage-attachment-bloc-button").click(function() {
				document.location.href = ABS.attachmentManagerUrl;
			});
			reloadAttachments();

			$(squashtm.app.addAttachmentPopupSettings).on("exitUpload",
					reloadAttachments);
		});

	});
});