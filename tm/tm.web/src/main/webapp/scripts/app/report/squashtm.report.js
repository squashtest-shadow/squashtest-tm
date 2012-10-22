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
/**
 * Controller for the report panel
 * 
 * depends on : 
 * jquery
 * jquery ui
 * jquery.jeditable.js
 * jquery.jeditable.datepicker.js
 * jquery.squashtm.plugin.js
 * jquery.squashtm.linkabletree.js
 * squashtm.reportworkspace.js
 * 
 * @author Gregory Fouquet
 */
define([ "jquery", "app/report/squashtm.reportworkspace", "jqueryui", "jeditable", "jeditable.datepicker", "jquery.squash", "jquery.squash.linkabletree", "jquery.squash.projectpicker"  ], function($, RWS) {
	var config = {
		contextPath: "",
		dateFormat: "dd/mm/yy", 
		noDateLabel: "---",
		okLabel: "OK",
		cancelLabel: "Cancel"
	};
	
	var formState = {};
	var selectedTab = false;

	var postDateFormat = $.datepicker.ATOM;
	var postNoDate = "--";
	
	function resetState() {
		formState = {};
		selectedTab = false;
	}

	function onSingleCheckboxChanged() {
		var option = this;
		var name = option.name;

		formState[name] = {
			value : option.value,
			selected : option.checked, 
			type: 'CHECKBOX'
		};
	}

	function onGroupedCheckboxesChanged() {
		var option = this;
		var name = option.name;
		var value = option.value;

		var groupState = formState[name] || [];
		formState[name] = groupState;

		var res = $(groupState).filter(function (index) {
			return this.value === value;
		});

		if (res[0]) {
			res[0].selected = this.checked;
		} else {
			groupState.push({
				value : value,
				selected : option.checked,
				type: 'CHECKBOXES_GROUP'
			});
		}
	}
	
	function onGroupedRadiosChanged() {
		var option = this;
		var name = option.name;
		var value = option.value;

		$(formState[name]).each(function () {
			if (this.value === value) {
				this.selected = option.checked;
			} else {
				this.selected = false;
			}
		});
	}

	function onListItemSelected() {
		var dropdown = $(this);
		var options = dropdown.find("option");
		
		var state = $.map(options, function (item, index) {
			return { value: item.value, selected: item.selected, type: 'DROPDOWN_LIST'  };
		}); 
		
		formState[dropdown.attr('name')] = state;
	}
	
	function onTextBlurred() {
		formState[this.name] = {value: this.value, type: 'TEXT'};
	}

	function onDatepickerChanged(value) {
		var localizedDate = value;
		
		var postDate;
		
		if (config.noDateLabel === value) {
			postDate = postNoDate;
		} else {
			var date = $.datepicker.parseDate(config.dateFormat, localizedDate);
			postDate = $.datepicker.formatDate(postDateFormat, date);
		}
		
		formState[this.id] = {value: postDate, type: 'DATE'};
	}
	
	function initDropdowns(panel) {
		var dropdowns = panel.find('select');
		dropdowns.change(onListItemSelected);
		dropdowns.change();
	}
	
	function initTexts(panel) {
		var texts = panel.find("input:text");
		texts.blur(onTextBlurred);
		texts.blur();		
	}
	
	function initDatepickers(panel) {
		var dateSettings = {
			dateFormat: config.dateFormat
		};
		
		var datepickers = panel.find(".rpt-date-crit");
		datepickers.editable(function (value, settings) {
			var self = this;
			onDatepickerChanged.apply(self, [value]);
			
			return value;
		}, {
	        type      : 'datepicker',
	        tooltip   : "Click to edit...",
	        datepicker: dateSettings
		});
		
		datepickers.each(function () {
			var self = this;
			var date =  self.innerText || config.noDateLabel;
			onDatepickerChanged.apply(self, [date]);
		});
	}
	
	function initRadios(panel) {
		var radios = panel.find("input:radio");
		radios.change(onGroupedRadiosChanged)
		.each(function () {
			var option = this;
			var name = option.name;
			
			formState[name] = formState[name] || [];
			formState[name].push({
				name: name,
				value : option.value,
				selected : option.checked,
				type: 'RADIO_BUTTONS_GROUP'
			});
		});
	}
	
	function initCheckboxes(panel) {
		var checkboxes = panel.find("input:checkbox");
		
		var groupedCheckboxes = checkboxes.filter(function (index) {
			var item = $(this);
			return item.data('grouped');
		});
		groupedCheckboxes.change(onGroupedCheckboxesChanged);
		groupedCheckboxes.change();
		
		var singleCheckboxes = checkboxes.filter(function (index) {
			var item = $(this);
			return !$(item).data('grouped');
		});
		singleCheckboxes.change(onSingleCheckboxChanged);
		singleCheckboxes.change();
	}
	
	function buildViewUrl(index, format) {
		// see [Issue 1205] for why "document.location.protocol"
		return document.location.protocol + '//' + document.location.host + config.reportUrl + "/views/" + index + "/formats/" + format;

	}
	
	function loadTab(tab) {
		var url = buildViewUrl(tab.index, "html");	
		$.ajax({
			type: 'post', 
			url: url, 
			data: JSON.stringify(formState),  
			contentType: "application/json"
		}).done(function (html) {
			tab.panel.innerHTML = html;
		});		
	}
	
	function generateView() {
		// collapses the form
		$("#report-criteria-panel").togglePanel("closeContent"); 
		// collapses the sidebar
		RWS.setReportWorkspaceExpandState();

		var tabPanel = $("#view-tabed-panel");

		if (!selectedTab) {
			tabPanel.tabs('select', 0);
		} else {
			loadTab(selectedTab);
		}

		$("#view-tabed-panel:hidden").show('blind', {}, 500);
	}
	
	function onViewTabSelected(event, ui) {
		selectedTab = ui;
		var tabs = $(this);
		tabs.find(".view-format-cmb").addClass('not-displayed');
		tabs.find("#view-format-cmb-" + ui.index).removeClass('not-displayed');
		
		loadTab(ui);
	}
	
	function doExport() {
		var viewIndex = selectedTab.index;
		var format = $("#view-format-cmb-" + viewIndex).val();

		var url = buildViewUrl(viewIndex, format);	
		var data = JSON.stringify(formState).replace(/"/g, '&quot;');
		
		$.open(url, {data: data}, {name: '_blank'});
	}
 
	function initViewTabs() {
		$("#view-tabed-panel").tabs({
			selected: -1,
			select: onViewTabSelected
		});
	}
	/**
	 * Converts a NODE_TYPE into a workspace-type
	 */
	function nodeTypeToWorkspaceType(nodeType) {
		return nodeType.toLowerCase().replace(/_/g, "-");
	}
	/**
	 * Fetches the workspace type from the given jquery object pointing to a treepicker
	 */
	function getWorkspaceType(treePicker) {
		return nodeTypeToWorkspaceType(treePicker.data("node-type"));
	}
	
	function setTreeState(tree, nodes) {
		var name = tree.attr('id');
		
		formState[name] = [];
		
		if (nodes && nodes.length === 0) {
			formState[name].push({value: "", nodeType: "", type: 'TREE_PICKER'});
			return;
		} 
		
		$(nodes).each(function () {
			var node = $(this);
			formState[name].push({value: node.attr("resid"), nodeType: node.attr("restype"), type: 'TREE_PICKER'});
		});
	}

	function initTreePickerCallback() {
		var tree = $(this);
		var workspaceType = getWorkspaceType(tree);
		

		$.get(config.contextPath + "/" + workspaceType + "-browser/drives", "linkables", "json")
		.done(function (data) {
			var settings = $.extend({}, config);
			settings.workspaceType = workspaceType;
			settings.jsonData = data;
			tree.linkableTree(settings);
		});
	
		setTreeState(tree, []);
	}
	
	function onConfirmTreePickerDialog() {
		var self = $(this);
		self.dialog("close");
		//dialog issue [Issue 1064]
		self.dialog().ajaxSuccess(
						function(){
							self.dialog('close');
						});
		//end issue
		var tree = self.find('.rpt-tree-crit');
		var nodes = tree.jstree('get_selected');
		
		setTreeState(tree, nodes);
	}

	function onCancelTreePickerDialog() {
		$(this).dialog('close');
	}

	function initTreePickerDialogCallback() {
		var dialog = $(this);
		
		dialog.createPopup({
			height: 500,
			buttons: [{
				text: config.okLabel, 
				click: onConfirmTreePickerDialog
			}, {
				text: config.cancelLabel, 
				click: onCancelTreePickerDialog
			}]
		});	
	}

	function initTreePickers(panel, settings) {
		panel.find('.rpt-tree-crit-open').click(function () {
			var dialogId = $(this).data('id-opened');
			var treePickerPopup = $("#" + dialogId);
			//dialog issue [Issue 1064]
			treePickerPopup.dialog().ajaxSuccess(function () {
					treePickerPopup.dialog('open');
			});
			//end issue
			treePickerPopup.dialog('open');
		});
		
		panel.find('.rpt-tree-crit').each(initTreePickerCallback);
					
		panel.find(".rpt-tree-crit-dialog").each(initTreePickerDialogCallback);
	}

	function onConfirmProjectPicker() {
		var picker = $(this);
		picker.projectPicker("close");
		var projects = picker.projectPicker("data");

		formState[this.id] = $.map(projects, function (item) {
			return {value: item.id, selected: item.selected, type: "PROJECT_PICKER"};
		});
	}	
	
	function initProjectPickerCallback() {
		var picker = $(this);
		
		picker.projectPicker({
			url: config.contextPath + "/projects?format=picker", 
			ok: {
				text: config.okLabel, 
				click: onConfirmProjectPicker
			},
			cancel: {
				text: config.cancelLabel 
			}, 
			loadOnce: true
		});	
		
		formState[this.id] = [{value: 0, selected: false, type: "PROJECT_PICKER"}];
	}

	function initProjectPickers(panel) {
		panel.find(".rpt-projects-crit-open").click(function () {
			var dialogId = $(this).data("id-opened");
			$("#" + dialogId).projectPicker("open");
		});
		
		panel.find(".rpt-projects-crit-container").each(initProjectPickerCallback);
	}

	function init(settings) {
		resetState();
		config = $.extend(config, settings);		
		
		var panel = $("#report-criteria-panel");
		panel.togglePanel({});

		$("#contextual-content").decorateButtons();

		initCheckboxes(panel);
		initRadios(panel);
		initDropdowns(panel);
		initTexts(panel);
		initDatepickers(panel);		
		initTreePickers(panel);
		initViewTabs();
		initProjectPickers(panel);

		$('#generate-view').click(generateView);
		$('#export').click(doExport);
	}

	squashtm.report = {
		init : init
	};
	
	return squashtm.report;
});
