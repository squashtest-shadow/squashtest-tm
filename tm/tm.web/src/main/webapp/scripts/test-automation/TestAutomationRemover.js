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
 - selector : an appropriate selector for the popup.
 - testAutomationURL : the url where to GET - POST things.
 - baseURL : the base url of the app.
 - successCallback : a callback if the test association succeeds. Will be given 1 argument, the path of the associated automated test.
 - messages : 
 - noTestSelected : message that must be displayed when nothing is selected
 */
define([ "jquery", "jquery.squash.confirmdialog" ], function() {
	function TestAutomationRemover(settings) {

		var automatedTestRemovalUrl = settings.automatedTestRemovalUrl;
		var successCallback = settings.successCallback;
		var confirmDialog = $(settings.confirmPopupSelector);

		confirmDialog.confirmDialog({
			confirm : sendRemovalRequest
		});

		function sendRemovalRequest() {
			return $.ajax({
				url : automatedTestRemovalUrl,
				type : 'DELETE',
				dataType : 'json'
			}).done(function() {
				successCallback();
				confirmDialog.confirmDialog("close");
			});
		}
	}

	return TestAutomationRemover;
});
