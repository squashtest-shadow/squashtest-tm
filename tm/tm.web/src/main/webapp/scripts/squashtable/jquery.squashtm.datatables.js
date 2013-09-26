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
/**
 * This file contains functions used by squashtest dataTables-based components.
 * 
 * @author Gregory Fouquet
 */

var squashtm = squashtm || {};

/*
 * squashtm datatable pagination plugin. Based on the ExtJS style plugin by Zach Curtis
 * (http://zachariahtimothy.wordpress.com/) and simplified according to our needs.
 * 
 * @author bsiri.
 * 
 */
(function($) {
	$.fn.dataTableExt.oPagination.iFullNumbersShowPages = 1;

	$.fn.dataTableExt.oPagination.squash = {
		/*
		 * Function: oPagination.squash.fnInit Purpose: Initalise dom elements required for pagination with a list of
		 * the pages Returns: - Inputs: object:oSettings - dataTables settings object node:nPaging - the DIV which
		 * contains this pagination control function:fnCallbackDraw - draw function which must be called on update
		 */
		"fnInit" : function(oSettings, nPaging, fnCallbackDraw) {

			var initButton = function(object, cssClass) {
				object.button({
					text : false,
					icons : {
						primary : cssClass
					}
				});
			};

			var nFirst = $('<span />', {
				'class' : 'paginate_button first'
			});
			var nPrevious = $('<span />', {
				'class' : 'paginate_button previous'
			});
			var nNext = $('<span />', {
				'class' : 'paginate_button next'
			});
			var nLast = $('<span />', {
				'class' : 'paginate_button last'
			});
			var nPageTxt = $("<span />", {
				text : '1'
			});

			$(nPaging).append(nFirst).append(nPrevious).append(nPageTxt).append(nNext).append(nLast);

			// utf8 code for character "◀" = U25C0 and for "▶" = U25B6
			nFirst.text("◀◀");
			nPrevious.text("◀");
			nNext.text("▶");
			nLast.text("▶▶");

			$(nPaging).find('.paginate_button').button({
				disabled : false,
				text : true
			});

			nFirst.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "first");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength) / oSettings._iDisplayLength, 10));
			}).bind('selectstart', function() {
				return false;
			});

			nPrevious.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "previous");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength)/ oSettings._iDisplayLength, 10));
			}).bind('selectstart', function() {
				return false;
			});

			nNext.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "next");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength) / oSettings._iDisplayLength, 10));
			}).bind('selectstart', function() {
				return false;
			});

			nLast.click(function() {
				oSettings.oApi._fnPageChange(oSettings, "last");
				fnCallbackDraw(oSettings);
				nPageTxt.text(parseInt((oSettings._iDisplayStart+oSettings._iDisplayLength) / oSettings._iDisplayLength, 10));
			}).bind('selectstart', function() {
				return false;
			});

		},

		/*
		 * Function: oPagination.extStyle.fnUpdate Purpose: Update the list of page buttons shows Returns: - Inputs:
		 * object:oSettings - dataTables settings object function:fnCallbackDraw - draw function which must be called on
		 * update
		 */
		"fnUpdate" : function(oSettings, fnCallbackDraw) {
			if (!oSettings.aanFeatures.p) {
				return;
			}

			/* Loop over each instance of the pager */
			var an = oSettings.aanFeatures.p;

			for ( var i = 0, iLen = an.length; i < iLen; i++) {
				// var buttons = an[i].getElementsByTagName('span');
				var buttons = $(an[i]).find('span.paginate_button');
				if (oSettings._iDisplayStart === 0) {
					buttons.eq(0).button("option", "disabled", true);
					buttons.eq(1).button("option", "disabled", true);
					$($("span", an)[4]).text("1");
				} else {
					buttons.eq(0).button("option", "disabled", false);
					buttons.eq(1).button("option", "disabled", false);
				}

				if (oSettings.fnDisplayEnd() == oSettings.fnRecordsDisplay()) {
					buttons.eq(2).button("option", "disabled", true);
					buttons.eq(3).button("option", "disabled", true);
				} else {
					buttons.eq(2).button("option", "disabled", false);
					buttons.eq(3).button("option", "disabled", false);
				}
			}
		}
	};

	// defines datatable defaults settings and puts them in the squash
	// namespace.
	var datatableDefaults = {
		"bJQueryUI" : true,
		"bAutoWidth" : false,
		"bFilter" : false,
		"bPaginate" : true,
		"sPaginationType" : "squash",
		"iDisplayLength" : 50,
		"bServerSide" : true,
		"bRetrieve" : true,
		"sDom" : 't<"dataTables_footer"lp>'
	};

	squashtm.datatable = squashtm.datatable || {};
	squashtm.datatable.defaults = datatableDefaults;
})(jQuery);

/**
 * Adds a delete button in the last cell of a datatables row
 * 
 * @param row
 *            row where to add a delete button
 * @param entityId
 *            id of the entity shown by the row
 * @param buttonTemplateId
 *            html id of the <a> used as a template
 */

function addDeleteButtonToRow(row, entityId, buttonTemplateId) {
	$(row).find('td:last').append($('#' + buttonTemplateId).clone()).find('a').attr('id',
			buttonTemplateId + ':' + entityId);
}

/**
 * Registers event handlers to enable rangle selection in tables.
 */
function enableTableRangeSelection() {
	$(document).keydown(function(evt) {
		handleCtrlDown(evt);
		handleShiftDown(evt);
	}).keyup(function(evt) {
		handleCtrlUp(evt);
		handleShiftUp(evt);
	});
}

/**
 * Handler for control pressed event
 * 
 * @param event
 */
/* private */
function handleCtrlDown(event) {
	if (event.which == 17) { // ctrl
		$(window).data('ctrlPressed', true);
	}
}
/**
 * Handler for ctrl release event
 * 
 * @param event
 */
/* private */
function handleCtrlUp(event) {
	if (event.which == 17) { // ctrl
		$(window).data('ctrlPressed', false);
	}
}

/**
 * Handler for shift pressed event
 * 
 * @param event
 */
/* private */
function handleShiftDown(event) {
	// shift pressed : the last selected row will be the base of the ranged
	// selection
	if (event.which == 16) {
		if (!$(window).data('shiftPressed')) { // holding shift down fires the
			// event repeatedly so we ignore
			// subsequent events
			$(window).data('shiftPressed', true);
		}
	}
}

/**
 * Handler for shift release event
 * 
 * @param event
 */
/* private */
function handleShiftUp(event) {
	if (event.which == 16) { // shift
		$(window).data('shiftPressed', false);
	}

}

/**
 * private to the DnD code.
 * 
 * we need to fetch the rank of the first tr of the table as an offset to which we'll add the position of lines we are
 * moving around.
 * 
 * @param domTable
 *            the dom table
 * 
 * @returns the real index of the first row.
 * 
 */
function getOffsetFromDomTable(domTable, fnGetRowIndex) {

	var dataTable = $(domTable).dataTable({
		"bRetrieve" : true
	});

	var firstData = dataTable.fnGetData(0);

	var position = fnGetRowIndex(firstData);

	return parseInt(position, 10) - 1;

}

/**
 * Enables DnD on the given table.
 * 
 * Note : we calculate the 'offset' because the first displayed element is not necessarily the first item of the table.
 * For instance, if we are displaying page 3 and drop our rows at the top of the table view, the drop index is not 0 but
 * (3*pagesize);
 * 
 * @param tableId
 *            html id of the table
 * @param dropCallback
 *            function called on drop. args are row and drop position.
 * @returns
 */
function enableTableDragAndDrop(tableId, fnGetRowIndex, dropHandler) {
	$('#' + tableId).tableDnD({
		dragHandle : "drag-handle",
		onDragStart : function(table, rows) { // remember that we are using
			// our modified dnd : rows is a
			// jQuery object

			rows.find('.drag-handle').addClass('ui-state-active');

			var offset = getOffsetFromDomTable(table, fnGetRowIndex);

			var index = rows.get(0).rowIndex - 1;
			$(table).data("previousRank", index);
			$(table).data("offset", offset);

		},

		onDrop : function(table, rows) { // again, that is now a jQuery
			// object

			var newInd = rows.get(0).rowIndex - 1;
			var oldInd = $(table).data("previousRank");
			var offset = $(table).data("offset");
			if (newInd != oldInd) {
				dropHandler(rows, newInd + offset);
			}
		}

	});
}

function bindHover(dataTable) {
	$('tbody tr', dataTable).live('mouseleave', function() {
		$(this).removeClass('ui-state-highlight');
	}).live('mouseenter', function() {
		if (!$(this).hasClass('ui-state-row-selected')) {
			$(this).addClass('ui-state-highlight');
		}
	});
}
/**
 * Decorates given buttons as delete buttons
 * 
 * @param buttons
 *            jquery selected objects to decorate.
 */
function decorateDeleteButtons(buttons) {
	buttons.button({
		text : false,
		icons : {
			primary : "ui-icon-trash"
		}
	});
}

function addClickHandlerToSelectHandle(nRow, table) {
	$(nRow).find('.select-handle').click(function() {
		var row = this.parentNode;

		var ctrl = $(window).data('ctrlPressed');
		var shift = $(window).data('shiftPressed');

		if (!ctrl && !shift) {
			toggleRowAndDropSelectedRange(row);

		} else if (ctrl && !shift) {
			toggleRowAndKeepSelectedRange(row);

		} else if (!ctrl && shift) {
			growSelectedRangeToRow(row, table);

		} else {
			growSelectedRangeToRow(row, table);

		}

		memorizeLastSelectedRow(row, table);
		clearRangeSelection();

		return true;
	});
}

/*
 * that method programatically remove the highlight due to native range selection.
 */
function clearRangeSelection() {
	if (window.getSelection) {
		window.getSelection().removeAllRanges();
	} else if (document.selection) { // should come last; Opera!
		document.selection.empty();
	}
}

/* private */
function toggleRowAndDropSelectedRange(row) {
	$(row).toggleClass('ui-state-row-selected').removeClass('ui-state-highlight');
	$(row).parent().find('.ui-state-row-selected').not(row).removeClass('ui-state-row-selected');

}

/* private */
function toggleRowAndKeepSelectedRange(row) {
	$(row).toggleClass('ui-state-row-selected').removeClass('ui-state-highlight');
}

/* private */
function growSelectedRangeToRow(row, table) {
	var rows = $("tbody tr", table);
	var range = computeSelectionRange(row, table);

	for ( var i = range[0]; i <= range[1]; i++) {
		var r = rows[i];
		$(r).addClass('ui-state-row-selected');
	}

	$(row).removeClass('ui-state-highlight');
}
/**
 * Computes the 0-based range of row that should be selected. Note : row.rowIndex is a 1-based index.
 * 
 * @param row
 * @param table
 * @returns
 */
/* private */
function computeSelectionRange(row, table) {
	var baseRow = table.data("lastSelectedRow");
	var baseIndex = baseRow ? baseRow.rowIndex : 1;
	var currentIndex = row.rowIndex;

	var rangeMin = Math.min(baseIndex, currentIndex);
	rangeMin = Math.max(rangeMin, 1);

	var rangeMax = Math.max(baseIndex, currentIndex);
	var rows = $("tbody tr", table);
	rangeMax = Math.min(rangeMax, rows.length);

	return [ rangeMin - 1, rangeMax - 1 ];
}

/* private */
function memorizeLastSelectedRow(row, table) {
	if ($(row).hasClass('ui-state-row-selected')) {
		$(table).data("lastSelectedRow", row);
	}
}
/**
 * saves the ids of selected rows
 * 
 * @param dataTable
 * @param getRowIdCallback
 *            function which determines the id to store from the row data.
 */
function saveTableSelection(dataTable, getRowIdCallback) {
	var selectedIds = getIdsOfSelectedTableRows(dataTable, getRowIdCallback);
	dataTable.data('selectedIds', selectedIds);
}

/**
 * @param dataTable
 * @param getRowIdCallback
 *            function (rowData) which should determine id of row.
 * @returns {Array} of ids of selected rows
 */
function getIdsOfSelectedTableRows(dataTable, getRowIdCallback) {
	var rows = dataTable.fnGetNodes();
	var ids = [];

	$(rows).each(function(index, row) {
		if ($(row).hasClass('ui-state-row-selected')) {
			var data = dataTable.fnGetData(row);
			var id = getRowIdCallback(data);
			if ((!!id) && (!isNaN(id))) {
				ids.push(id);
			}
		}
	});

	return ids;
}

function findRowStatus(dataTable, rowId, getRowId, getRowStatus) {
	var rows = dataTable.fnGetNodes();
	var status = "";

	$(rows).each(function(index, row) {
		var data = dataTable.fnGetData(row);
		var id = getRowId(data);
		if (id == rowId) {
			status = getRowStatus(data);
		}
	});

	return status;
}

function getObsoleteStatusesOfSelectedTableRows(dataTable, getRowStatus) {
	var rows = dataTable.fnGetNodes();
	var obsoleteStatuses = [];

	$(rows).each(function(index, row) {
		if ($(row).hasClass('ui-state-row-selected')) {
			var data = dataTable.fnGetData(row);
			var status = getRowStatus(data);
			if (status == "OBSOLETE") {
				obsoleteStatuses.push(status);
			}
		}
	});

	return obsoleteStatuses;
}

function getIdsOfSelectedAssociationTableRows(dataTable, getRowIdCallback) {
	var rows = dataTable.fnGetNodes();
	var ids = [];

	for ( var i = 0; i < rows.length; i++) {
		var row = rows[i];
		if ($(row).hasClass('ui-state-row-selected')) {
			var data = dataTable.fnGetData(row);
			ids.push(getRowIdCallback(data));
		}
	}
	return ids;
}

function restoreTableSelection(dataTable, getRowIdCallback) {
	var selectedIds = dataTable.data('selectedIds');
	if (!!selectedIds) {
		selectTableRowsOfIds(dataTable, selectedIds, getRowIdCallback);
	}
	dataTable.data('selectedIds', null);
}

/* private */
function selectTableRowsOfIds(dataTable, ids, getRowIdCallback) {
	var rows = dataTable.fnGetNodes();
	
	var self = this;
	$(rows).filter(function() {
		var data = dataTable.fnGetData(this);
		var rId = getRowIdCallback(data);
		return $.inArray(rId, ids) != -1;
	}).addClass('ui-state-row-selected');

}
function addHLinkToCellText(td, url, isOpenInTab) {

	var link = $('<a></a>');
	link.attr('href', url);
	if (isOpenInTab) {
		link.attr('target', '_blank');
	}

	$(td).contents().filter(function() {
		// IE doesn't define the constant Node so we'll use constant value
		// instead of Node.TEXT_NODE
		return this.nodeType == 3;
	}).wrap(link);
}
/**
 * Adds a "manage attachment" link to the row cell(s) of class has-attachment-cell
 * 
 * @param row
 *            row where to add an attachment button
 * @param entityId
 *            id of the entity shown by the row
 * @param buttonTemplateId
 *            html id of the <a> used as a template
 */
function addAttachmentButtonToRow(row, entityId, buttonTemplateId) {
	var cell = $('td.has-attachment-cell', row);
	var attCount = parseInt(cell.text(), 10);

	if (attCount > 0) {
		cell.html($('#' + buttonTemplateId).clone()).find('a').attr('id', buttonTemplateId + ':' + entityId);
	}
}
/**
 * Decorates given buttons as attachment button
 * 
 * @param buttons
 */
function decorateAttachmentButtons(buttons) {
	$(buttons).html("<img src='/squash/images/attach_2.png'>");
}
/**
 * Decorates given buttons for empty attachment list
 * 
 * @param buttons
 */
function decorateEmptyAttachmentButtons(buttons) {
	$(buttons).html("<img src='/squash/images/add.png'>");
}

/**
 * Adds a "manage attachment" link to the row cell(s) of class has-attachment-cell. DoV for Depending on the Value : the
 * button is different whether the attachment list is empty or not
 * 
 * @param row
 *            row where to add an attachment button
 * @param attCount
 *            number of attachments
 * @param buttonTemplateId
 *            html id of the <a> used as a template
 * @param buttonTemplateEmptyId
 *            html id of the <a> used as a template if the attachment list is empty
 */
function addAttachmentButtonToRowDoV(row, attCount, buttonTemplateId, buttonTemplateEmptyId) {
	var cell = $('td.has-attachment-cell', row);
	var entityId = cell.text();

	// no attachment count means the cell cannot be attached at all.
	if (attCount === "") {
		cell.html('');
	} else if (attCount > 0) {
		cell.html($('#' + buttonTemplateId).clone()).find('a').attr('id', buttonTemplateId + ':' + entityId);
	} else {
		cell.html($('#' + buttonTemplateEmptyId).clone()).find('a').attr('id', buttonTemplateEmptyId + ':' + entityId);
	}
}

squashtm.datatables = {
	addDeleteButtonToRow : addDeleteButtonToRow,
	enableTableRangeSelection : enableTableRangeSelection,
	getOffsetFromDomTable : getOffsetFromDomTable,
	enableTableDragAndDrop : enableTableDragAndDrop,
	bindHover : bindHover,
	decorateDeleteButtons : decorateDeleteButtons,
	addClickHandlerToSelectHandle : addClickHandlerToSelectHandle,
	clearRangeSelection : clearRangeSelection,
	saveTableSelection : saveTableSelection,
	getIdsOfSelectedTableRows : getIdsOfSelectedTableRows,
	findRowStatus : findRowStatus,
	getObsoleteStatusesOfSelectedTableRows : getObsoleteStatusesOfSelectedTableRows,
	getIdsOfSelectedAssociationTableRows : getIdsOfSelectedAssociationTableRows,
	restoreTableSelection : restoreTableSelection,
	addHLinkToCellText : addHLinkToCellText,
	addAttachmentButtonToRow : addAttachmentButtonToRow,
	decorateAttachmentButtons : decorateAttachmentButtons,
	decorateEmptyAttachmentButtons : decorateEmptyAttachmentButtons,
	addAttachmentButtonToRowDoV : addAttachmentButtonToRowDoV
};
