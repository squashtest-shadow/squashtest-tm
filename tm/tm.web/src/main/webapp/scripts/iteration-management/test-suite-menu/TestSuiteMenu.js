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
define([ "jquery", "underscore", "jqueryui" ],function($, _) {

	function TestSuiteMenuNewStatuses() {

		var initiallyChecked = [];
		var checkedIds = [];
		var initiallyNotChecked = [];
		var uncheckedIds = [];

		this.getChecked = function() {
			return this.checkedIds;
		};
		
		this.getUnchecked = function() {
			return this.uncheckedIds;
		};
		
		this.reset = function(checked, notChecked) {
			this.intiallyChecked = checked;
			this.initiallyNotChecked = notChecked;
			this.checkedIds = [];
			this.uncheckedIds = [];
		};

		this.change = function(id, checked) {
			if (checked) {
				this.unChekedIds = _.reject(this.unChekedIds, function(
						unchekedId) {
					return unchekedId === id;
				});
				if (!_.contains(this.initiallyChecked, id)) {
					this.checkedIds.push(id);
				}
			} else {
				this.checkedIds = _.reject(this.checkedIds, function(
						checkedId) {
					return checkedId === id;
				});
				if (!_.contains(this.initiallyNotChecked, id)) {
					this.uncheckedIds.push(id);
				}
			}
		};

		this.add = function(id) {
			this.initiallyNotChecked.push(id);
			this.checkedIds.push(id);
		};

	}

	function initWidgets() {

		$("#manage-test-suites-buttonmenu").buttonmenu({
			anchor : 'right',
			'no-auto-hide' : true
		});

		$("#suite-manager-menu-ok-button, #suite-manager-menu-cancel-button")
				.button();

		$("#suite-manager-menu-button").squashButton({
			'text' : false,
			icons : {
				primary : 'ui-icon-circle-plus'
			}
		});

		this.menucontrol = $("#manage-test-suites-buttonmenu");
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
				'id' : 'menu-suite-' + json.id,
				'type' : 'checkbox',
				'name' : 'menu-suite-item'
			});
			node.append(checkbox);
			var label = $("<label/>", {
				'for' : 'menu-suite-' + json.id,
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

			// append to the list

			this.menu.prepend(sorted);

		}, this);

		var getCheckboxes = $.proxy(function() {
			return this.menu.find('input[name="menu-suite-item"]');
		}, this);

		var getCheckboxBySuiteId = $.proxy(function(id) {
			return this.menu.find('input#menu-suite-' + id);
		}, this);

		var getDatatableSelected = $.proxy(function() {
			return $(this.datatableSelector).squashTable()
					.getSelectedIds();
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
							message.append("<div>"+ errors[0].errorMessage+ "</div>");
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
		 * **************************** public
		 * ******************************
		 */

		this.update = function(evt) {
			if ((evt === undefined) || (evt.evt_name == "node.rename")
					|| (evt.evt_name == "node.remove")
					|| (evt.evt_name == "node.refresh")) {
				initializeContent();

			} else if (evt.evt_name == "node.add") {
				addSuiteToMenuContent(evt);
			}
		};

		/* *********************** handlers ***************** */

		var addSuiteToMenuContent = $.proxy(function(evt) {
			this.testSuiteNewStatuses.add(evt.newSuite.id);
			var item = makeItem(evt.newSuite);
			item.find("input").attr("checked", "checked");
			item.attr("checked", "checked");
			this.menu.find('.suite-manager-controls').before(item);
		}, this);

		var addSuite = $.proxy(function() {
			var self = this;
			var name = $('#suite-manager-menu-input').val();
			this.model.postNew(name).error(displayAddSuiteError);
		}, this);

		// -------- binding -------------

		var bindCheckboxes = $.proxy(function(evt) {
			var self = this;
			this.menu.delegate('input:checkbox', 'change',
					function(evt) {
						var checkbx = $(evt.currentTarget);
						self.testSuiteNewStatuses.change(checkbx
								.data('suite-id'), checkbx
								.is(":checked"));

					});
		}, this);

		var bindOkButton = $.proxy(function() {
				var self = this;
				$('#suite-manager-menu-ok-button').on('click',function(evt) {
						if (!self.testPlanItemIds.length) {
							$(settings.emptySelectionMessageSelector).openMessage();
						} else {
							var toSend = {};
							var checkedSuiteIds = self.testSuiteNewStatuses.getChecked();
							var uncheckedSuiteIds = self.testSuiteNewStatuses.getUnchecked();
							if (uncheckedSuiteIds.length > 0 && checkedSuiteIds.length > 0) {
								toSend['bound-test-suites'] = checkedSuiteIds;
								toSend['unbound-test-suites'] = uncheckedSuiteIds;
								toSend['test-plan-items'] = self.testPlanItemIds;
								self.model.postBindChanged(toSend).success(function() {
										self.menucontrol.buttonmenu('close');
								});
							}
						}
					});
			}, this);

		var bindCancelButton = $.proxy(function() {
			var self = this;
			$('#suite-manager-menu-cancel-button').on('click',
					function(evt) {
						self.menucontrol.buttonmenu('close');
					});
		}, this);

		var bindAddButton = $.proxy(function() {
			$('#suite-manager-menu-button').on('click', function(evt) {
				addSuite();
			});
		}, this);

		var bindInput = $.proxy(function() {
			var input = $('#suite-manager-menu-input');
			input.on('keydown', function(evt) {
				if (evt.which == '8') {
					evt.stopImmediatePropagation();
					// backspace will nagivate to previous page if not canceled here
				}
				if (evt.which == '13') {
					addSuite();
					input.val('');
				}
			});
		}, this);

		var bindShowMenuButton = $.proxy(function() {
				var self = this;
				$("#manage-test-suites-buttonmenu").on('click',function(evt) {
					self.testPlanItemIds = getDatatableSelected();
					if (!self.testPlanItemIds.length) {
						// no item selected: close menu and warn
						self.menucontrol.buttonmenu('close');
						evt.stopImmediatePropagation();
						$(settings.emptySelectionMessageSelector).openMessage();

					} else {
						var suites = self.model.getData();

						// get item ids by suite id
						var itemIdsBySuite = {};
						_.each(suites,function(suite) {
								itemIdsBySuite[suite.id] = [];
							});

						_.each(self.testPlanItemIds,function(itemId) {
								var itemSuiteIds = $(self.datatableSelector).squashTable().getDataById(itemId)["suiteIds"];
								_.each(itemSuiteIds,function(itemSuiteId) {
										itemIdsBySuite[itemSuiteId].push(itemId);
									});
							});

						// compute statuses and update checkboxes
						var suiteStatuses = {};
						var unboundIds = [];
						var boundIds = [];
						getCheckboxes().prop('indeterminate',false); // reset the checkboxes
						_.each(itemIdsBySuite,function(itemIds, suiteId) {
								var status;
								var checkbox = getCheckboxBySuiteId(suiteId);
								if (itemIds.length == self.testPlanItemIds.length) {
									status = "checked";
									boundIds.push(suiteId);
									checkbox.prop('checked',true);
								} else if (itemIds.length === 0) {
									status = "unchecked";
									unboundIds.push(suiteId);
									checkbox.prop('checked',false);
								} else {
									status = "undefined";
									checkbox.prop("indeterminate",true);
								}
								suiteStatuses[suiteId] = status;
							});

						self.testSuiteNewStatuses.reset(boundIds, unboundIds); // reset the model
						$("#suite-manager-menu-input").val(""); // reset the input field
					}
				});
			}, this);

		var initHandlerBinding = $.proxy(function() {
			bindShowMenuButton();
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
		this.testSuiteNewStatuses = new TestSuiteMenuNewStatuses();

		initWidgets.call(this);

		initHandlerBinding();

		initializeContent();

	}

	return TestSuiteMenu;
});
