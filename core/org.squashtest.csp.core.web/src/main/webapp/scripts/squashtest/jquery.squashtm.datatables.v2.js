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
 
 
 /*
 dependencies :
 
	jquery,
	tableDnD,
	dataTables
	KeyEventListener
	statusFactory
 
 
 */
 
 
 
 
/**
 * This file contains functions used by squashtest dataTables-based components.
 * @author Gregory Fouquet
 */
 

 
 /*
	==========================================
	Intoduction
	==========================================
 
	keys used for data lookup
	-------------------------
	
	That table uses mPropData for its columns. More explictly, it uses json data as a map.
	Specifically, keys we are looking for are :
	
	- 'entity-id' : the entity id
	- 'entity-index' : the position of the entity when the list is sorted
	
	In some cases more keys might be required for the modules decscribed below,
	refer to the documentation if need be.
	
	
	placeholders : 
	--------------
	
	When configuring a module sometimes you will see that a given string supports 
	placeholders. It means that anything between curly braces '{something}' 
	are placeholders that will be replaced by the corresponding value from aoData["something"].
	That's where the data keys are useful.
	
	==========================================
	Regular Datatable settings
	==========================================

	the inherited part of the datatable is configured using the first parameter 
	: 'datatableSettings'.
	Any regular datatable configuration is supported. 
	
	It uses defaults values yet the following parameters are still REQUIRED :
	
	- "oLanguage" (internationalization),
	- "sAjaxSource" (chargement ajax),
	- "aoColumnDefs" (les colonnes)
	
	
	==========================================
	Squash additional settings
	==========================================
	
	
	The squash specifics are configured using the second parameter : 
	'squashSettings'. It is an object that accepts the following members : 

	- functions.dropHandler : 
			what : a function that must handle the row drop.
			param : an obect { itemIds : array of row ids, newIndex : the drop position }
			default : nothing
			
	- functions.getODataId : 
			what : a function fetching the id from the data
			param : what $().dataTable().fnGetData() would normally accept
			default : return fnGetData()["entity-id"]
			
	- enableDnD : 
			if coalesce to true, will enable table drag and drop. If coalesce to false, it will not.
				  
	- enableHover : 
			if coalesce to true, will enable lines color change when hovered. 
			If coalesce to false, it will not.

			
	- attachments :
		If the table finds tds having a given cssClass (see cssMatcher) if will turn them into link to the attachment manager.
		'attachments' is an object. It must define at least url. It may also override the others of course.
			
		* url : url where the attachment manager is. Accepts placeholders. Note : that one accepts no defaults ! 
		* cssMatcher : the css class of cells that must be treated. defaults to 'has-attachment-cells'
		* aoDataNbAttach : the name of the column in aoData where to look for how many attachment the row has. defaults to "nb-attachments"
		* aoDataListId : the name of the column in aoData where to look for the attachment list id, defaults to "attach-list-id"	

		
	- rich editables configuration :
		if a property 'richeditables' is set, will attempt to turn some cells to rich editables. If undefined, nothing will happen.
		the property 'richeditables' is an compound object and must define at least 1 member for 'target'
		
		* conf : a regular object configuring the plugin $.ui.richEditable (see jquery.squashtm.jeditable.ext.js).
		* targets : a map of key-values. A key represents a css class and the value represents an url supporting placeholders.
					Any td having the given css class will be turned to a rich jeditable configured with 'conf' and posting to 
					the supplied url.
			  
	- execution status : 
		If a property 'executionstatus' is set, will attempt to decorate some cells with execution statuses. If undefined,
		nothing will happen. The matched cells are identified by css class 'has-status'. 
		
		'executionstatus' is an object defining the localized status text :
			* blocked : internationalized version of status 'blocked'
			* failure : internationalized version of status 'failure'
			* success : internationalized version of status 'success'
			* running : internationalized version of status 'running'
			* ready : internationalized version of status 'ready'
		
 */
(function($){

	squashtm = squashtm || {};
	squashtm.keyEventListener == squashtm.keyEventListener || new KeyEventListener();
	
	/*********************
	
		The following functions assume that the instance of the datatable is 'this'.
		
		Note the '_' prefixing each of them.
		
		Typically when the squash datatable initialize it will also declare public methods that will 
		access them. Those methods then have the same name, without the '_' prefix.
		
		In some of the functions here such methods belonging to 'this' are invoked. It's not a 
		typo : it's the expected behaviour.
		
	
	**********************/
 
	/**
	 * Adds a delete button in the last cell of a datatables row
	 * 
	 * @param row
	 *            row where to add a delete button
	 * @param buttonTemplateId
	 *            html id of the <a> used as a template
	 */

	function _addDeleteButtonToRow(row, buttonTemplateId) {
		var entityId = this.getODataId(row);
		$('td:last', row).append($('#' + buttonTemplateId).clone()).find('a').attr(
				'id', buttonTemplateId + ':' + entityId);
	}

	/*
			what : a function that must handle the row drop.
			param : an obect { itemIds : array of row ids, newIndex : the drop position }
			default : nothing
	*/
	function _dropHandler(dropData){
		
	}
	
/*
	what : a function fetching the id from the data
	param : what $().dataTable().fnGetData() would normally accept
	default : return aoData[0]; : the datatable expects the id to be first.
*/	
	function _getODataId(arg){
		var id = this.fnGetData(arg)["entity-id"];
		if ((id!="") && (! isNaN(id))){
			return id;
		}else{
			return null;
		}
	}
 
	/**
	 * Enables DnD on the given table.
	 * 
	 * Note : we calculate the 'offset' because the first displayed element is not necessarily the first item of the table.
	 * 			For instance, if we are displaying page 3 and drop our rows at the top of the table view, the drop index is not 0
	 * 			but (3*pagesize);
	 * 
	 * @this : the datatable instance
	 * @param dropCallback
	 *            function called on drop. args are row and drop position.
	 * @returns
	 */
	function _enableTableDragAndDrop() {
		self = this;
		this.tableDnD({
			dragHandle : "drag-handle",
			onDragStart : function(table, rows) { //remember that we are using our modified dnd : rows is a jQuery object 
				
				rows.find('.drag-handle').addClass('ui-state-active');
				
				var offset = self.fnGetData(0)['entity-index'];
				
				var index = rows.get(0).rowIndex - 1;
				self.data("previousRank", index);
				self.data("offset", offset);
				
			},
			
			onDrop : function(table, rows) { //again, that is now a jQuery object
					
				var newInd = rows.get(0).rowIndex - 1;
				var oldInd = self.data("previousRank");
				var offset = self.data("offset");
				if (newInd != oldInd) {
				
					var ids = [];
					rows.each(function(i,e){
						var id = self.getODataId(e);
						ids.push(id);
					});
					
					self.dropHandler({ 
						itemIds : ids, 
						newIndex : newInd+offset
					});
				}
			}
			
		});
	}

	
/*
	For the current datatable, will bind hover coloring 
*/	
	function _bindHover(){
		
		this.delegate('tr', 'mouseleave', function(){
			$(this).removeClass('ui-state-highlight');
		});
		
		this.delegate('tr', 'mouseenter', function(){
			var jqR = $(this);
			if (!jqR.hasClass('ui-state-row-selected')) {
				jqR.addClass('ui-state-highlight');
			}
		});
	
	}
 
 
/*
	Just does what the name says.
*/ 
	 

	function _addClickHandlerToSelectHandle(nRow) {
		var self = this;
		$(nRow).find('.select-handle').click(function() {
			var row = this.parentNode;

			var ctrl = squashtm.keyEventListener.ctrl;
			var shift = squashtm.keyEventListener.shift;

			if (!ctrl && !shift) {
				_toggleRowAndDropSelectedRange.call(self, row);

			} else if (ctrl && !shift) {
				_toggleRowAndKeepSelectedRange.call(self, row);

			} else if (!ctrl && shift) {
				_growSelectedRangeToRow.call(self, row);

			} else {
				_growSelectedRangeToRow.call(self, row);

			}

			_memorizeLastSelectedRow.call(self, row);
			clearRangeSelection();

			return true;
		});
	}


	/*
	that method programatically remove the highlight due to native range selection.
	*/
	function clearRangeSelection(){
		if (window.getSelection) {
			window.getSelection().removeAllRanges();
		}
		else if (document.selection) { // should come last; Opera!
			document.selection.empty();
		}
	}

	/* private */function _toggleRowAndDropSelectedRange(row) {
		var jqRow = $(row);
		jqRow.toggleClass('ui-state-row-selected').removeClass(
				'ui-state-highlight');
		jqRow.parent().find('.ui-state-row-selected').not(row).removeClass(
				'ui-state-row-selected');

	}

	/* private */function _toggleRowAndKeepSelectedRange(row) {
		$(row).toggleClass('ui-state-row-selected').removeClass(
				'ui-state-highlight');
	}

	/* private */function _growSelectedRangeToRow(row) {
		var rows = this.$("tr");
		var range = _computeSelectionRange.call(this, row);

		for ( var i = range[0]; i <= range[1]; i++) {
			var r = rows[i];
			$(r).addClass('ui-state-row-selected');
		}

		$(row).removeClass('ui-state-highlight');
	}
	/**
	 * Computes the 0-based range of row that should be selected. Note :
	 * row.rowIndex is a 1-based index.
	 * 
	 * @param row 
	 * @param table
	 * @returns
	 */
	/* private */function _computeSelectionRange(row) {
		var baseRow = this.data("lastSelectedRow");
		var baseIndex = baseRow ? baseRow.rowIndex : 1;
		var currentIndex = row.rowIndex;

		var rangeMin = Math.min(baseIndex, currentIndex);
		rangeMin = Math.max(rangeMin, 1);

		var rangeMax = Math.max(baseIndex, currentIndex);
		var rows = this.$("tr");
		rangeMax = Math.min(rangeMax, rows.length);

		return [ rangeMin - 1, rangeMax - 1 ];
	}

	/* private */function memorizeLastSelectedRow(row) {
		if ($(row).hasClass('ui-state-row-selected')) {
			this.data("lastSelectedRow", row);
		}
	}
	
	
	/**
	 * saves the ids of selected rows
	 */
	function _saveTableSelection() {
		var selectedIds = _getSelectedIds.call(this);
		this.attr('selectedIds', selectedIds);
	}
	

	function _restoreTableSelection() {
		var selectedIds = this.attr('selectedIds');
		if (selectedIds != null) {
			_selectRows.call(this, selectedIds);
		}
		this.removeAttr('selectedIds');
	}
	
	

	/* private */function _selectRows(ids) {
		var rows = dataTable.fnGetNodes();

		$(rows).each(function(index, row) {
			var rowId = this.getODataId(row);
			if (ids.indexOf(rowId) >= 0) {
				$(row).addClass('ui-state-row-selected');
			}
		});
	}
	

	/**
	 * @returns {Array} of ids of selected rows
	 */
	function _getSelectedIds() {
		var rows = this.fnGetNodes();
		var ids = new Array();
		
		var self = this;

		$(rows).each(function(index, row) {
			if ($(row).hasClass('ui-state-row-selected')) {
				var id = self.getODataId(row)
				if ((id!="") && (! isNaN(id))){
					ids.push(id);
				}
			}
		});

		return ids;
	}

	function _addHLinkToCellText(td, url, isOpenInTab) {
		
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
	
	
	function _resolvePlaceholders(input, data){
		var pattern = /\{\S+\}/;
		var result = input;
		
		do{
			var match = pattern.exec(result);
			if (match){
				var pHolder = match[0];
				var key= pHolder.substr(1,pHolder.length-2);	
				result = result.replace(pHolder, data[key]);
			}
		}while(match);
		
		return result;
	}
	 
	 /*
		'this' is the table. That function will be called as a draw callback.
	 */
	function _attachButtonsCallback(){
				
		var attachConf = this.squashSettings.attachments;
		
		var self = this;
		var cells = $('td.'+attachConf.cssMatcher, this);
		
		$(cells).each(function(i,cell){
			
			var data = self.fnGetData(cell.parentNode);
			
			//first : set the proper icon
			var nbAttach = data[attachConf.aoDataNbAttach];
			var linkClass = (nbAttach>0) ? "manage-attachments" : "add-attachments";
			
			//second : what url we navigate to when clicked.
			var url = _resolvePlaceholders.call(self, attachConf.url, data); 
			
			//design the link and voila !
			var link = '<a href="'+url+'" class="'+linkClass+'"></a>';
			
			$(cell).html(link);			
		});
	}
	
	/*
		again 'this' is the table instance.
	*/
	function _configureRichEditables(){
		
		var editableConf = this.squashSettings.richeditables;
		var self= this;
		
		if (! editableConf) return;
		
		var baseconf = editableConf.conf;
		var targets  = editableConf.targets;
			
		if (! targets)  	return;
		
		for (var css in targets){
			
			var cells = $('td.'+css, this);
			
			$(cells).each(function(i,cell){
				var data = self.fnGetData(cell.parentNode);
				var url = _resolvePlaceholders.call(self, targets[css], data); 
				var finalConf = $.extend(true,{ "url" : url}, baseconf);
				$(cell).richEditable(finalConf);
			});
		}
	}

	
	function _configureExecutionStatus(){
	
		var statusConf = this.squashSettings.executionstatus;
		var self= this;
		
		if (! statusConf) return;
		
		var factory = new squashtm.StatusFactory(statusConf);
		
		var cells = $('td.has-status', this);

		$(cells).each(function(i,cell){
			var data = cell.innerText;
			var newhtml = factory.getHtmlFor(data);
			cell.innerHTML = newhtml;
		});		
		
	}
	 
	 /******************************************************************
	 
	 now we can declare our plugin
	 
	 ******************************************************************/
	 	 
	var datatableDefaults = {
		"bJQueryUI": true,
		"bAutoWidth": false,
		"bFilter": false,		
		"bPaginate": true,
		"sPaginationType": "squash",
		"iDisplayLength": 50,
		"bProcessing": true,
		"bServerSide": true,		
		"bRetrieve" : true,				
		"sDom" : 't<"dataTables_footer"lirp>'
	};
	
	var squashDefaults = {
		attachments : {
			cssMatcher : "has-attachment-cell",
			aoDataNbAttach : "nb-attachments",
			aoDataListId : "attach-list-id"
		},
	
	};
	
		
	$.fn.squashTable = function(datatableSettings, squashSettings){
	 
		
		var datatableEffective = $.extend(true, {}, datatableDefaults, datatableSettings);
		var squashEffective = $.extend(true, {}, squashDefaults, squashSettings);
		
		/* ************** squash init first *********************** */
		
		this.squashSettings = squashEffective;
		
		this.addDeleteButtonToRow = _addDeleteButtonToRow;
		this.dropHandler = _dropHandler;
		this.getODataId = _getODataId;
		this.addClickHandlerToSelectHandle = _addClickHandlerToSelectHandle;
		this.saveTableSelection = _saveTableSelection;
		this.restoreTableSelection = _restoreTableSelection;
		this.getSelectedIds = _getSelectedIds;
		this.addHLinkToCellText = _addHLinkToCellText;

				
				
		//  function overrides
		
		if (squashEffective.functions){
			$.extend(this, squashEffective.functions);
		}
				
		
		/* ************* prepare a custom rowcallback and drawcallback if needed ***** */
		
		var userDrawCallback = datatableEffective["fnDrawCallback"]
		
		var customDrawCallback = function (oSettings){
			if (userDrawCallback) userDrawCallback.call(this, oSettings);
			_attachButtonsCallback.call(this);
			_configureRichEditables.call(this);
			_configureExecutionStatus.call(this);
		}
		
		datatableEffective["fnDrawCallback"] = customDrawCallback;
				
		/* ************* now call the base plugin ***************** */		
		

		this.dataTable(datatableEffective);

	 
		/* ****** last : event binding ***** */
		
		if (squashSettings.enableDnD){
			_enableTableDragAndDrop.call(this);
		};
		
		if (squashSettings.enableHover){
			_bindHover.call(this);
		};

		this.addClass("is-contextual");
	 }
	 
})(jQuery); 
	 




