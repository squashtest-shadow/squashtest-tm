/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
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
define(["jquery", "./permissions-rules", "jquery.squash.buttonmenu"], function ($, permissions) {

	function createWidgets() {
		$("#tree-create-button").buttonmenu();
		$("#tree-import-button").buttonmenu();
	}

	function decorateEnablingMethods(buttons) {
		var i = 0, len = buttons.length;

		function cssenable() {
			this.removeClass("disabled ui-state-disabled");
		}

		function cssdisable() {
			this.addClass("disabled ui-state-disabled");
		}

		function menuenable() {
			this.buttonmenu('enable');
		}

		function menudisable() {
			this.buttonmenu('disable');
		}

		for (i = 0; i < len; i++) {
			var jqbtn = buttons[i];
			if (jqbtn.attr('role') === "buttonmenu") {
				jqbtn.enable = menuenable;
				jqbtn.disable = menudisable;
			}
			else {
				jqbtn.enable = cssenable;
				jqbtn.disable = cssdisable;
			}
		}
	}

	function bindTreeEvents() {

		var btnselector = [
			"#tree-create-button",
			"#new-folder-tree-button",
			"#new-global-dataset-tree-button",
			"#new-composite-dataset-tree-button",
			"#new-dataset-template-tree-button",
			"#rename-node-tree-button",
			"#delete-node-tree-button"
		].join(", ");

		var buttons = [];

		$(btnselector).each(function () {
			var $this = $(this);
			buttons.push($this);
		});

		decorateEnablingMethods(buttons);

		var tree = $("#tree");

		function loopupdate(event, data) {

			var rules = permissions.buttonrules;
			var arbuttons = buttons;
			var nodes = tree.jstree("get_selected");
			var len = buttons.length;

			for (var i = 0; i < len; i++) {
				var btn = arbuttons[i];
				var id = btn.attr("id");
				var rule = rules[id];
				if (rule(nodes)) {
					btn.enable();
				} else {
					btn.disable();
				}
			}

			return true;

		}

		tree.on("select_node.jstree deselect_node.jstree deselect_all.jstree", loopupdate);

		//init the button states immediately
		loopupdate("", {
			rslt: {
				obj: tree.jstree("get_selected")
			}
		});
	}

	function init(settings) {
		createWidgets();
		bindTreeEvents();

		$("#tree_element_menu").removeClass("unstyled-pane");
	}

	return {
		init: init
	};
});
