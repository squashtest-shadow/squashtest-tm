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
/**
 * Controller for the report panel
 * 
 * depends on : 
 * jquery
 * jquery ui
 * jquery.jeditable.js
 * jquery.jeditable.datepicker.js
 * jquery.squashtm.plugin.js
 * 
 * @author Gregory Fouquet
 */
squashtm.report = (function ($) {
	var config = {
		contextPath: "",
		dateFormat: "dd/mm/yy", 
		noDateLabel: "---"
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

		var res = $(formState[name]).each(function () {
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
		
		var datepickers = panel.find(".date-crit");
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
			onDatepickerChanged.apply(self, [self.innerText]);
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
	
	function generateView() {
		console.log(formState);
		var tabPanel = $("#view-tabed-panel");
		if (!selectedTab) {
			tabPanel.tabs('select', 0);			
		}
		$("#view-tabed-panel:hidden").show('blind', {}, 500);
	}
	
	function buildViewUrl(index, format) {
		return 'http://' + document.location.host + config.reportUrl + "/views/" + index + "/formats/" + format;
	}
	
	function selectViewTab(event, ui) {
		selectedTab = ui;
		var tabs = $(this);
		tabs.find(".view-format-cmb").addClass('not-displayed');
		tabs.find("#view-format-cmb-" + ui.index).removeClass('not-displayed');
		
		var url = buildViewUrl(ui.index, "html");	
		$.ajax({
			type: 'post', 
			url: url, 
			data: JSON.stringify(formState),  
			contentType: "application/json"
		}).done(function (html) {
			ui.panel.innerHTML = html;
		});		
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
			select: selectViewTab
		});
	}

	function init(settings) {
		resetState();
		config = $.extend(config, settings);		
		
		var panel = $("#report-criteria-panel");

		initCheckboxes(panel);
		initRadios(panel);
		initDropdowns(panel);
		initTexts(panel);
		initDatepickers(panel);
		
		panel.find('.nodes-crit-open').click(function () {
			console.log($(this));
			var dialogId = $(this).data('id-opened');
			console.log(dialogId);
			$("#" + dialogId).dialog('open');
		});
		
		treeSettings = $.extend({}, settings); 
		treeSettings.workspaceType = "Campaign";
		treeSettings.jsonData = [
		         				{
		        				    "data" : "A node",
		        				    "metadata" : { id : 23 },
		        				    "children" : [ "Child 1", "A Child 2" ]
		        				},
		        				{
		        				    "attr" : { "id" : "li.node.id1" },
		        				    "data" : {
		        				        "title" : "Long format demo",
		        				        "attr" : { "href" : "#" }
		        				    }
		        				}
		        				];
		
		panel.find('.nodes-crit').linkableTree(treeSettings);
					
		var treeDialogs = panel.find(".nodes-crit-container");
		treeDialogs.createPopup({
			buttons: [{
				text: /*[[ #{dialog.button.confirm.label} ]]*/ "Ok", 
				click: function () {
					var self = $(this);
					self.dialog("close");		
					var tree = self.find('.nodes-crit');
					var ids = tree.jstree('get_selected_ids', 'campaigns');
					var name = tree.attr('id');
					formState[name] = ids;
				}
			}, {
				text: /*[[ #{dialog.button.cancel.label} ]]*/ "Cancel", 
				click: function () {
					$(this).dialog('close');
				}
			}]
		});
		
		initViewTabs();

		$('#generate-view').click(generateView);
		$('#export').click(doExport);
	}

	return {
		init : init
	};
})(jQuery);
