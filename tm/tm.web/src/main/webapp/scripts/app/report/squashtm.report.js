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
var squashtm = squashtm || {};
/**
 * Controller for the report panel
 * 
 * depends on : jquery jquery ui jquery.jeditable.js jquery.jeditable.datepicker.js jquery.squash.plugin.js
 * jquery.squash.linkabletree.js squash.reportworkspace.js
 * 
 * @author Gregory Fouquet
 */
define([ "jquery", "app/report/squashtm.reportworkspace", "tree", "jqueryui", "jeditable", "jeditable.datepicker",
		"jquery.squash", "jquery.squash.projectpicker", "jquery.cookie",
		"datepicker/require.jquery.squash.datepicker-locales" ], function($, RWS, treebuilder) {
	var config = {
		contextPath : "",
		dateFormat : "dd/mm/yy",
		noDateLabel : "-",
		okLabel : "OK",
		cancelLabel : "Cancel"
	};

	var preferences = null;
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
			type : 'CHECKBOX'
		};
	}

	function onGroupedCheckboxesChanged() {
		var option = this;
		var name = option.name;
		var value = option.value;

		var groupState = formState[name] || [];
		formState[name] = groupState;

		var res = $(groupState).filter(function(index) {
			return this.value === value;
		});

		if (res[0]) {
			res[0].selected = this.checked;
		} else {
			groupState.push({
				value : value,
				selected : option.checked,
				type : 'CHECKBOXES_GROUP'
			});
		}
	}

	function onGroupedRadiosChanged() {
		var option = this;
		var name = option.name;
		var value = option.value;
		var givesAccessTo = (option.id).replace("-binder", "");

		$(formState[name]).each(function() {
			if (this.value === value) {
				this.selected = option.checked;
			} else {
				this.selected = false;
			}
		});

		// deactivate all elements which are linked to other options in
		// the group
		deactivateAllAssociatedButtons(name);

		// if the option has an associated element
		if (givesAccessTo !== undefined && givesAccessTo !== "none") {

			// find the right element and activate it
			$("#" + givesAccessTo + "-open").removeAttr("disabled");

		}

	}

	function deactivateAllAssociatedButtons(name) {

		var list = $("li [name=" + name + "]");
		var givesAccessTo;

		list.each(function() {

			givesAccessTo = (this.id).replace("-binder", "");

			if (givesAccessTo !== undefined && givesAccessTo !== "none") {
				// find the right element and deactivate it
				$("#" + givesAccessTo + "-open").attr("disabled", "disabled");
			}
		});
	}

	function onListItemSelected() {
		var dropdown = $(this);
		var options = dropdown.find("option");

		var state = $.map(options, function(item, index) {
			return {
				value : item.value,
				selected : item.selected,
				type : 'DROPDOWN_LIST'
			};
		});

		if (dropdown.attr('name')) {
			formState[dropdown.attr('name')] = state;
		}
	}

	function onTextBlurred() {
		formState[this.name] = {
			value : this.value,
			type : 'TEXT'
		};
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

		formState[this.id] = {
			value : postDate,
			type : 'DATE'
		};
	}

	function initDropdowns(panel) {
		var dropdowns = panel.find('select');
		dropdowns.change(onListItemSelected);
		dropdowns.change();

		if (preferences) {
			$.each(dropdowns, function(index, dropdown) {
				var name = dropdown.name;
				var preferenceForName = preferences[name];
				if (preferenceForName) {
					$.each(preferenceForName, function(index, element) {
						var value = element.value;
						var options = $("option", dropdown);
						$.each(options, function(index, option) {
							if (option.value == value && element.selected) {
								$(option).attr('selected', true);
							} else if (option.value == value && !element.selected) {
								$(option).attr('selected', false);
							}
						});
					});
				}
			});
			dropdowns.change();
		}
	}

	function initTexts(panel) {
		var texts = panel.find("input:text");
		texts.blur(onTextBlurred);
		texts.blur();
	}

	function initDatepickers(panel) {
		var dateSettings = {
			dateFormat : config.dateFormat
		};

		var datepickers = panel.find(".rpt-date-crit");

		// setting the locale
		var locale = datepickers.data('locale');
		var confLocale = $.datepicker.regional[locale];

		if (!!confLocale) {
			$.extend(dateSettings, confLocale);
		}

		// rest of the init
		datepickers.editable(function(value, settings) {
			var self = this;
			onDatepickerChanged.apply(self, [ value ]);

			return value;
		}, {
			type : 'datepicker',
			tooltip : "Click to edit...",
			datepicker : dateSettings
		});

		datepickers.each(function() {
			var self = this;
			var date = self.innerText || config.noDateLabel;
			onDatepickerChanged.apply(self, [ date ]);
		});

		if (preferences) {
			$.each(datepickers, function(index, datepicker) {
				var name = datepicker.id;
				var preferenceForName = preferences[name];
				if (preferenceForName && preferenceForName.value.length > 5) {
					var date = $.datepicker.parseDate("yy-mm-dd", preferenceForName.value);
					if (date) {
						$(datepicker).text($.datepicker.formatDate(config.dateFormat, date));
						var postDate = $.datepicker.formatDate(postDateFormat, date);
						formState[this.id] = {
							value : postDate,
							type : 'DATE'
						};
					}
				}
			});

			datepickers.change();
		}
	}

	function initRadios(panel) {
		var radios = panel.find("input:radio");

		radios.change(onGroupedRadiosChanged).each(function() {
			var option = this;
			var name = option.name;
			var givesAccessTo = (option.id).replace("-binder", "");

			formState[name] = formState[name] || [];
			formState[name].push({
				name : name,
				value : option.value,
				selected : option.checked,
				type : 'RADIO_BUTTONS_GROUP'
			});

			if (givesAccessTo !== undefined && givesAccessTo !== "none") {
				// find the right element and deactivate it
				$("#" + givesAccessTo + "-open").attr("disabled", "disabled");
			}
		});

		if (preferences) {
			$.each(radios, function(index, radio) {
				var name = radio.name;
				var givesAccessTo = (radio.id).replace("-binder", "");
				var preferenceForName = preferences[name];
				if (preferenceForName) {
					$.each(preferenceForName, function(index, element) {
						var value = element.value;
						if (radio.value == value && element.selected) {
							$(radio).attr('checked', true);
							$.each(formState[name], function(index, state) {
								if (state.value == radio.value) {
									state.selected = true;
									if (givesAccessTo !== undefined && givesAccessTo !== "none") {
										$("#" + givesAccessTo + "-open").removeAttr("disabled");
									}
								}
							});
						} else if (radio.value == value && !element.selected) {
							$.each(formState[name], function(index, state) {
								if (state.value == radio.value) {
									state.selected = false;

								}
							});
						}
					});
				}
			});
		}
	}

	function initCheckboxes(panel) {
		var checkboxes = panel.find("input:checkbox");

		var groupedCheckboxes = checkboxes.filter(function(index) {
			var item = $(this);
			return item.data('grouped');
		});
		groupedCheckboxes.change(onGroupedCheckboxesChanged);
		groupedCheckboxes.change();

		var singleCheckboxes = checkboxes.filter(function(index) {
			var item = $(this);
			return !$(item).data('grouped');
		});
		singleCheckboxes.change(onSingleCheckboxChanged);
		singleCheckboxes.change();

		if (preferences) {
			$.each(checkboxes, function(index, checkbox) {
				var name = checkbox.name;
				var preferenceForName = preferences[name];
				if (preferenceForName) {
					$.each(preferenceForName, function(index, element) {
						var value = element.value;
						if (checkbox.value == value && element.selected) {
							$(checkbox).attr('checked', true);
							$.each(formState[name], function(index, state) {
								if (state.value == checkbox.value) {
									state.selected = true;
								}
							});
						} else if (checkbox.value == value && !element.selected) {
							$(checkbox).removeAttr('checked');
							$.each(formState[name], function(index, state) {
								if (state.value == checkbox.value) {
									state.selected = false;

								}
							});
						}
					});
				}
			});
		}
	}

	function buildViewUrl(index, format) {
		// see [Issue 1205] for why "document.location.protocol"
		return document.location.protocol + '//' + document.location.host + config.reportUrl + "/views/" + index +
				"/formats/" + format;

	}

	function loadTab(tab) {
		var url = buildViewUrl(tab.newTab.index(), "html");
		$.ajax({
			type : 'post',
			url : url,
			data : JSON.stringify(formState),
			contentType : "application/json"
		}).done(function(html) {
			tab.newPanel.html(html);
		});
	}

	function isPerimeterValid() {

		var status = false;

		$.each(formState, function(key, value) {

			if (value[0]) {

				if ("PROJECT_PICKER" == value[0].type) {
					$.each(value, function(index, element) {
						if (element.selected) {
							status = true;
						}
					});
				}

				if ("RADIO_BUTTONS_GROUP" == value[0].type) {
					$.each(value, function(index, element) {
						if (element.selected & element.value == "EVERYTHING") {
							status = true;
						}
					});
				}

				if ("TREE_PICKER" == value[0].type) {
					$.each(value, function(index, element) {
						if (element.value) {
							status = true;
						}
					});
				}
			}
		});

		return status;
	}

	function generateView() {

		if (isPerimeterValid()) {
			// stores preferences in browser datastore
			$.cookie(config.reportUrl + "-prefs", JSON.stringify(formState));

			// collapses the form
			$("#report-criteria-panel").togglePanel("closeContent");
			// collapses the sidebar
			RWS.setReportWorkspaceExpandState();

			var tabPanel = $("#view-tabed-panel");

			if (!selectedTab) {
				tabPanel.tabs("option", "active", 0);
				// tab is inited, we dont need collapsible anymore,
				// otherwise click on active tab will trigger an event
				tabPanel.tabs("option", "collapsible", false);
			} else {
				loadTab(selectedTab);
			}

			$("#view-tabed-panel:hidden").show('blind', {}, 500);
		} else {
			var invalidPerimeterDialog = $("#invalid-perimeter").messageDialog();
			invalidPerimeterDialog.messageDialog('open');
		}
	}

	function onViewTabSelected(event, ui) {
		selectedTab = ui;
		var tabs = $(this);
		tabs.find(".view-format-cmb").addClass('not-displayed');
		tabs.find("#view-format-cmb-" + ui.newTab.index()).removeClass('not-displayed');

		loadTab(ui);
	}

	function doExport() {
		var viewIndex = selectedTab.newTab.index();
		var format = $("#view-format-cmb-" + viewIndex).val();

		var url = buildViewUrl(viewIndex, format);
		var data = JSON.stringify(formState).replace(/"/g, '&quot;');

		$.open(url, {
			data : data
		}, {
			name : '_blank',
			features : 'resizable=yes, scrollbars=yes'
		});
	}

	function initViewTabs() {
		$("#view-tabed-panel").tabs({
			active : false,
			collapsible : true, // we need collapsible for first init of
			// first tab
			activate : onViewTabSelected
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
			formState[name].push({
				value : "",
				nodeType : "",
				type : 'TREE_PICKER'
			});
			return;
		}

		$(nodes).each(function() {
			var node = $(this);
			formState[name].push({
				value : node.attr("resid"),
				nodeType : node.attr("restype"),
				type : 'TREE_PICKER'
			});
		});
	}

	function initTreePickerCallback() {
		var tree = $(this);
		var treeid = this.id;
		var workspaceType = getWorkspaceType(tree);

		$.get(config.contextPath + "/" + workspaceType + "-browser/drives", "linkables", "json").done(function(data) {
			var settings = $.extend({}, config);
			settings.workspace = workspaceType;
			settings.model = data;
			settings.treeselector = "#" + treeid;
			treebuilder.initLinkableTree(settings);
		});

		setTreeState(tree, []);
	}

	function onConfirmTreePickerDialog() {
		var self = $(this);
		self.dialog("close");
		// dialog issue [Issue 1064]
		self.dialog().ajaxSuccess(function() {
			self.dialog('close');
		});
		// end issue
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
			height : 500,
			buttons : [ {
				text : config.okLabel,
				click : onConfirmTreePickerDialog
			}, {
				text : config.cancelLabel,
				click : onCancelTreePickerDialog
			} ]
		});
	}

	function initTreePickers(panel) {
		panel.find('.rpt-tree-crit-open').click(function() {
			var dialogId = $(this).data('id-opened');
			var treePickerPopup = $("#" + dialogId);
			// dialog issue [Issue 1064]
			treePickerPopup.dialog().ajaxSuccess(function() {
				treePickerPopup.dialog('open');
			});
			// end issue
			treePickerPopup.dialog('open');
		});

		panel.find('.rpt-tree-crit').each(initTreePickerCallback);

		panel.find(".rpt-tree-crit-dialog").each(initTreePickerDialogCallback);

		if (preferences) {

			var pickers = panel.find('.rpt-tree-crit-open');

			$.each(pickers, function(index, picker) {
				var id = picker.id;
				var real_id = id.substr(0, id.length - 5);
				formState[real_id] = preferences[real_id];
			});
		}
	}

	function onProjectPickerChanged() {
		var picker = $(this);

		var options = $("option", picker);

		formState[this.id] = $.map(options, function(option) {
			return {
				value : option.value,
				selected : option.selected,
				type : "PROJECT_PICKER"
			};
		});
	}

	function initProjectPickerCallback() {

		var picker = $(this);
		var url = config.contextPath + "/projects?format=picker";
		var formid = this.id;

		// load options
		$.getJSON(url).done(function(data) {
			$.each(data.projectData, function(index, value) {
				if (index === 0) {
					picker.append("<option selected='selected' value='" + value[0] + "'>" + value[1] + "</option>");
				} else {
					picker.append("<option value='" + value[0] + "'>" + value[1] + "</option>");
				}
			});

			var options = $("option", picker);

			formState[formid] = $.map(options, function(option) {
				return {
					value : option.value,
					selected : option.selected,
					type : "PROJECT_PICKER"
				};
			});

			if (preferences) {
				var name = picker[0].id;
				var preferenceForName = preferences[name];
				options = $("option", picker[0]);

				$.each(options, function(index, option) {
					$.each(preferenceForName, function(index, element) {
						var value = element.value;
						if (option.value == value && element.selected) {
							$(option).attr("selected", true);
						} else if (option.value == value && !element.selected) {
							$(option).attr("selected", false);
						}
					});
				});

				picker.change();
			}
		});

		picker.change(onProjectPickerChanged);

	}

	function initProjectPickers(panel) {
		panel.find(".rpt-projects-crit-container").each(initProjectPickerCallback);
	}

	function init(settings) {

		resetState();
		config = $.extend(config, settings);
		// Get user preferences if they exist
		preferences = JSON.parse($.cookie(config.reportUrl + "-prefs"));

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
