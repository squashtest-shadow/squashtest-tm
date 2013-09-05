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
define([ "jquery", "jqueryui", "jquery.squash.buttonmenu" ], function($) {

	function TestSuiteMenuCheckedSuites() {
		var checkedIds = [];
		this.get = function() {
			return this.checkedIds;
		};
		this.reset = function() {
			this.checkedIds = [];
		};
		this.add = function(id) {
			this.checkedIds.push(id);
		};
		this.remove = function(id) {

			var index = jQuery.inArray(id, this.checkedIds);
			if (index) {
				this.checkedIds.splice(index, 1);
			}

		};
	}

	function initWidgets() {

		$("#manage-test-suites-buttonmenu").buttonmenu({
			anchor : 'right',
			'no-auto-hide' : true
		});
		
		
		$("#suite-manager-menu-ok-button, #suite-manager-menu-cancel-button").button();	
		
		$("#suite-manager-menu-button").squashButton({
			'text' : false,
			icons : {
				primary : 'ui-icon-circle-plus'
			}
		});
		

		this.buttonMenu = $("#manage-test-suites-buttonmenu");
		this.menu = $("#manage-test-suites-menu");
	}


	function TestSuiteMenu(settings) {

		/* **************** private ************** */

		var self = this;

		var makeItem = $.proxy(function(json) {
			var node = $("<li/>", {
				'class' : 'suite-item'
			});
			var checkbox = $("<input/>", {
				'data-suite-id' : json.id,
				'id' : 'menu-suite-#' + json.id,
				'type' : 'checkbox',
				'name' : 'menu-suite-item'
			});
			node.append(checkbox);
			var label = $("<label/>", {
				'for' : 'menu-suite-#' + json.id,
				'class' : 'afterDisabled'
			});
			label.text(json.name);
			node.append(label);
			return node;
		}, this);

		var getItemDomText = function(elt) {
			if (elt.firstElementChild !== undefined) {
				return elt.firstElementChild.textContent;
			} else {
				return elt.firstChild.innerText;
			}
		};

		var getSpanDomId = function(elt) {
			return elt.getAttribute('value');
		};

		var getItemId = function(jqElt) {
			return $('span', jqElt).data('suite-id');
		};

		var initializeContent = $.proxy(function() {
			
			// wipe the previous items
			this.menu.find('li.suite-item').remove();
			
			// generate new content
			var model = this.model.getData();
			var items = [];
			
			for ( var i in model) {
				var node = makeItem(model[i]);
				items.push(node);
			}

			// sort new content
			var sorted = items.sort(function(a, b) {
				var textA = getItemDomText(a.get(0));
				var textB = getItemDomText(b.get(0));
				return (textA < textB) ? -1 : 1;
			});
			
			//append to the list
			
			this.menu.prepend(sorted);

			this.checkedSuites.reset();

		}, this);

		var getCheckboxes = $.proxy(function() {
			return this.menu.find('input[name="menu-suite-item"]');
		}, this);


		var getDatatableSelected = $.proxy(function() {
			return $(this.datatableSelector).squashTable();
		}, this);
		

		var displayAddSuiteError = $.proxy(function(xhr, text) {
			try {
				var errContent = jQuery.parseJSON(xhr.responseText);
				var message = $("<div/>", {
					'margin-top' : 'auto',
					'margin-bottom' : 'auto'
				});

				if (errContent.fieldValidationErrors !== undefined) {
					var errors = errContent.fieldValidationErrors;
					for ( var i = 0; i < errors.length; i++) {
						message.append("<div>" + errors[0].errorMessage + "</div>");
					}
				} else {
					message.append('<div>could not add your suite : unexpected error</div>');
				}

				oneShotDialog('Information', message);

			} catch (wtf) {
				// non json error : it must be handled
				// by the generic handler (see the
				// red thing showing up in the view
				// right now)
				// anyway, job done here
			}
		}, this);

		/*
		 * **************************** public ******************************
		 */

		this.update = function(evt) {
			// the only event ignored is "bind"
			var wasOpen;
			if ((evt === undefined) || (evt.evt_name == "node.rename") || (evt.evt_name == "node.remove") ||
					(evt.evt_name == "node.refresh")) {
				initializeContent();
			} else if (evt.evt_name == "node.add") {
				addSuiteToMenuContent(evt);
			}
		};

		/* *********************** handlers ***************** */

		var addSuiteToMenuContent = $.proxy(function(evt) {
			this.checkedSuites.add(evt.newSuite.id);
			var item = makeItem(evt.newSuite);
			item.find("input").attr("checked", "checked");
			$(item).attr("checked", "checked");
			this.menu.find('li.suite-item').last().after(item);
		}, this);

		var addSuite = $.proxy(function() {
			var self = this;
			var name = $('#suite-manager-menu-input').val();
			this.model.postNew(name).error(displayAddSuiteError);
		}, this);

		var stopEventPropagation = $.proxy(function() {
			var container = this.menu;
			container.on('click', 'div, ul, li,  label', function(evt) {
				evt.stopImmediatePropagation();
			});
		}, this);

		var bindCheckboxes = $.proxy(function(evt) {
			var self = this;
			var container = this.menu;
			container.delegate('input:checkbox', 'change', function(evt) {
				evt.stopImmediatePropagation();
				var checkbx = $(evt.currentTarget);
				if (checkbx.is(":checked")) {
					self.checkedSuites.add(checkbx.data('suite-id'));
				} else {
					self.checkedSuites.remove(checkbx.data('suite-id'));
				}

			});
		}, this);

		var bindOkButton = $.proxy(function() {
			var self = this;
			$('#suite-manager-menu-ok-button').on('click', function(evt) {
				evt.stopImmediatePropagation();
				if (!getDatatableSelected().length) {
					$(settings.emptySelectionMessageSelector).openMessage();
				} else {
					var toSend = {};
					var suiteIds = self.checkedSuites.get();
					if (suiteIds.length < 1) {
						$(settings.emptySuiteSelectionMessageSelector).openMessage();
					} else {
						toSend['test-suites'] = suiteIds;
						toSend['test-plan-items'] = getDatatableSelected();
						self.model.postBind(toSend).success(function() {
							self.menu.hide();
						});
					}
				}
			});
		}, this);

		var bindCancelButton = $.proxy(function() {
			var self = this;
			$('#suite-manager-menu-cancel-button').on('click', function(evt) {
				evt.stopImmediatePropagation();
				self.menu.hide();
			});
		}, this);

		var showMenuHandler = $.proxy(function() {
			if (!getDatatableSelected().length) {
				this.menu.hide();
				$(settings.emptySelectionMessageSelector).openMessage();
			}

			getCheckboxes().prop('checked', false); // reset the
			// checkboxes
			this.checkedSuites.reset(); // reset the model
			$("#suite-manager-menu-input").val(""); // reset the input field
		}, this);

		var bindAddButton = $.proxy(function() {
			$('#suite-manager-menu-button').on('click', function(evt) {
				evt.stopImmediatePropagation();
				addSuite();
			});
		}, this);

		var bindInput = $.proxy(function() {
			$('#suite-manager-menu-input').on('click', function(evt) {
				evt.stopImmediatePropagation();
			});
			$('#suite-manager-menu-input').on('keypress', function(evt) {
				evt.stopImmediatePropagation();
				if (evt.which == '13') {
					addSuite();
				}
			});
		}, this);

		var initHandlerBinding = $.proxy(function() {
			stopEventPropagation();
			bindAddButton();
			bindInput();
			bindOkButton();
			bindCancelButton();
			bindCheckboxes();

		}, this);


		/* *********************** init ********************* */

		this.datatableSelector = settings.datatableSelector;
		this.model = settings.model;
		this.model.addListener(this);
		
		this.instance = $(settings.instanceSelector);		
		this.checkedSuites = new TestSuiteMenuCheckedSuites();
		
		initWidgets.call(this);				
		
		initHandlerBinding();

		initializeContent();

	}

	return TestSuiteMenu;
});
