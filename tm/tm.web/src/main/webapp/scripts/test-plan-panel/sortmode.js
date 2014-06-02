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
 * That object makes things according to the state of sorting of a datatable.
 */

/*
 * configuration : add the following data attr to a test plan table
 * <pre>
 *   <table class="test-plan-table" data-entity-id="..." data-entity-type="...">
 * </pre>
 *
 */
define([ "jquery", "workspace.storage", "app/util/ButtonUtil" ],
	function($, storage, ButtonUtil) {
	"use strict";

	var tableSelector = ".test-plan-table";

	function SortMode(conf) {
		var isLocked = false;

		var $table = $(".test-plan-table");
		var entityId = $table.data("entity-id");
		var entityType = $table.data("entity-type");
		this.storage = storage;

		// **************** configuration ******************

		this.reorderable = conf.permissions.reorderable || false;

		if (!entityId) {
			throw "sortmode : entity id absent from table data attributes";
		}
		if (!entityType) {
			throw "sortmode : entity type absent from table data attributes";
		}

		this.key = entityType + "-sort-" + entityId;

		// ******************* logic ***********************

		this.resetTableOrder = function(table) {
			table.fnSettings().aaSorting = StaticSortMode.defaultSorting();
			this._disableSortMode();
		};

		this.manage = function(newSorting) {

			if (this._isDefaultSorting(newSorting)) {
				this._disableSortMode();
				this._deleteaaSorting();
			} else {
				if (!isLocked) {
					this._enableSortMode();
				}
				this._saveaaSorting(newSorting);
			}
		};

		this._enableSortMode = function() {
			$("#test-plan-sort-mode-message").show();
			$(".test-plan-table").find(".select-handle").removeClass("drag-handle");
			if (this.reorderable) {
				ButtonUtil.enable($("#reorder-test-plan-button"));
			}
		};

		this._disableSortMode = function() {
			$("#test-plan-sort-mode-message").hide();
			$(tableSelector).find(".select-handle").addClass("drag-handle");

			ButtonUtil.disable($("#reorder-test-plan-button"));

		};

		this._lockSortMode = function() {
			isLocked = true;
		};

		this._unlockSortMode = function() {
			isLocked = false;
		};

		this._isDefaultSorting = function(someSorting) {
			var defaultSorting = StaticSortMode.defaultSorting();
			return (someSorting.length === 1 && someSorting[0][0] === defaultSorting[0][0] && someSorting[0][1] === defaultSorting[0][1]);
		};

		// ******************** I/O ********************

		this.loadaaSorting = function() {
			var sorting = this.storage.get(this.key);
			if (!!sorting) {
				return sorting;
			} else {
				return StaticSortMode.defaultSorting();
			}
		};

		this._saveaaSorting = function(aaSorting) {
			var trimedSorting = [], _buf;

			for ( var i = 0, len = aaSorting.length; i < len; i++) {
				_buf = aaSorting[i];
				trimedSorting.push([ _buf[0], _buf[1] ]);
			}

			this.storage.set(this.key, trimedSorting);
		};

		this._deleteaaSorting = function() {
			this.storage.remove(this.key);
		};

	}

	var StaticSortMode = {
		newInst : function(conf) {
			return new SortMode(conf);
		},
		defaultSorting : function() {
			return [ [ 0, "asc" ] ];
		}
	};

	return StaticSortMode;

});