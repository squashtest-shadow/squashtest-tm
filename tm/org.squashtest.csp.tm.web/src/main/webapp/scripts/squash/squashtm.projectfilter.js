/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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

squashtm.projectfilter = (function ($, window) {
	var popupSelector = '#project-filter-popup';
	var projectFilterUrl;

	function setCheckBox(jqCheckbox, isEnabled) {
		if (isEnabled) {
			jqCheckbox.attr('checked', 'checked');
		} else {
			jqCheckbox.removeAttr('checked');
		}
	}

	/** initializes the project filter popup */
	function selectAllProjects() {
		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");

		if (boxes.length === 0) {
			return;
		}

		$(boxes).each(function () {
			setCheckBox($(this), true);
		});
	}

	function deselectAllProjects() {
		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");

		if (boxes.length === 0) {
			return;
		}

		$(boxes).each(function () {
			setCheckBox($(this), false);
		});
	}

	function invertAllProjects() {
		var boxes = $("#dialog-settings-filter-projectlist .project-checkbox");

		if (boxes.length === 0) {
			return;
		}

		$(boxes).each(function () {
			setCheckBox($(this), !$(this).is(":checked"));
		});
	}

	/**
	 * Code managing the loading phase of the popup. It expects the server to send the data as a json object, see
	 * tm.web.internal.model.jquery.FilterModel
	 * 
	 * note : each project in the array is an array made of the following : { Long , String , Boolean )
	 */
	function clearFilterProject() {
		$("#dialog-settings-filter-projectlist").empty();
	}

	function appendProjectItem(containerId, projectItemData, cssClass) {
		var jqNewItem = $(popupSelector + " .project-item-template .project-item").clone();
		jqNewItem.addClass(cssClass);

		var jqChkBx = jqNewItem.find(".project-checkbox");
		jqChkBx.attr('id', 'project-checkbox-' + parseInt(projectItemData[0]));

		var jqName = jqNewItem.find(".project-name");
		jqName.html(projectItemData[1]);

		setCheckBox(jqChkBx, projectItemData[2]);

		$("#" + containerId).append(jqNewItem);
	}

	function swapCssClass(cssClass) {
		if (cssClass === "odd") {
			return "even";
		}
		return "odd";
	}
	
	function populateFilterProject(jsonData) {
		var cssClass = "odd";
		var i = 0;
		for (i = 0; i < jsonData.projectData.length; i++) {
			appendProjectItem("dialog-settings-filter-projectlist", jsonData.projectData[i], cssClass);
			cssClass = swapCssClass(cssClass);
		}
		
	}
	
	function loadFilterProject() {
		clearFilterProject();
		
		$.get(projectFilterUrl, populateFilterProject, "json");
	}
	
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

		var params = {
			selector : popupSelector,
			title : conf.title,
			openedBy : '#menu-project-filter-link',
			closeOnSuccess : false,
			buttons : [ {
				text : conf.confirmLabel,
				click : sendNewFilter
			}, {
				text : conf.cancelLabel,
				click : function () {
					$(this).dialog('close');
				}
			} ],
			width : 400,
			open : loadFilterProject
		};

		squashtm.popup.create(params);

		$("#dialog-settings-filter-selectall").click(selectAllProjects);
		$("#dialog-settings-filter-deselectall").click(deselectAllProjects);
		$("#dialog-settings-filter-invertselect").click(invertAllProjects);
	}

	/**
	 * public module
	 */
	return {
		init : initPopup
	};
}(jQuery, window));
