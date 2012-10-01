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
/**
 * requires : 
 * * jquery.squashtm.projectpicker.js
 */
var squashtm = squashtm || {};

squashtm.projectfilter = (function ($, window) {
	var popupSelector = "#project-filter-popup";
	var popupOpener = "#menu-project-filter-link";
	var projectFilterUrl;

	function extractId(strDomId) {
		var idTemplate = "project-checkbox-";
		var templateLength = idTemplate.length;
		var extractedId = strDomId.substring(templateLength);
		return extractedId;
	}

	function getSelectedProjectIds(containerId) {
		var selectedBoxes = $("#" + containerId + " .project-checkbox:checked");
		var zeids = [];
		var i;

		for (i = 0; i < selectedBoxes.length; i++) {
			var jqBox = $(selectedBoxes[i]);

			zeids.push(extractId(jqBox.attr('id')));
		}

		return zeids;
	}

	function newFilterSuccess() {
		$(popupSelector).dialog('close');
		window.location.reload();
	}
	
	/**
	 * code managing the data transmissions
	 */
	function sendNewFilter() {
		var isEnabled = $("#dialog-settings-isselected-checkbox").is(":checked");

		var ids = getSelectedProjectIds("dialog-settings-filter-projectlist");
		$.post(projectFilterUrl, {
			projectIds : ids
		}, newFilterSuccess);

	}

	function initPopup(conf) {
		projectFilterUrl = conf.url;
		
		var picker = $(popupSelector).projectPicker({
			url: conf.url, 
			ok: { text: conf.confirmLabel, click: sendNewFilter }, 
			cancel: { text: conf.cancelLabel }, 
			width: 400
		});
		
		$(popupOpener).click(function () { 
			picker.projectPicker("open"); 
		});
	}

	/**
	 * public module
	 */
	return {
		init : initPopup
	};
}(jQuery, window));
