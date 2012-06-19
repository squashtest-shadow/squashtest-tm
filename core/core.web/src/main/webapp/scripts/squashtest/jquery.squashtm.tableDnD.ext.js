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
 /**
  * Customization of jquery.tableDnD.js by Denis Howlett, credits to his original work.
  * 
  * The current extension add the support for multiline dnd. The modifications are the following : 
  * 
  * <ol>
  *	  <li>the .dragoObject is now a jQuery variable, holding all rows having the class ui-state-row-selected<li>
  *	  <li>jQuery.tableDnD.makeDraggable will call config.onDragStart with that new .dragObject.
  *	  		It does so whether configured to use a draghandle or not (the original plugin didn't). </li> 
  *	  <li>it uses explicitly the class "nodrop" to prevent dropping in the middle of the selected lines</li>
  *	  <li>a method adaptDragObject was added to the plugin and aims to adapt the new .dragObject (a jQuery object) to the old one
  *	   	(a regular javascript object), so that the rest of the plugin can use it normally.</li>
  * </ol>
  */ 
 /* 
  * Note : the original plugin sometimes checks if the current line is equal to .dragObject. That comparison now always fails safely.
  */

/*
 * Todo : possibly remove the makeDraggable part and just overwrite the mousedown event handler on the tr's it attached to them
 * 
 */
  

jQuery.tableDnD.adaptDragObject = function (allRows){
	//they all hopefully have the same parent
	var delegateParentNode= allRows.first().parent();			

	delegateParentNode.insertBefore = function (jqElts, where){

		//check 1 : if target is defined then we insert our elements before it, else we insert at the end of this 
		//(thus mimicking the regular insertBefore in javascript)
		if (where){
			//check : the target (where) must have the same parent or nothing should happen
			//note : remember that contains() is a $.fn extensions to cope with .is() of jQuery 1.5 (the one in jq 1.6 is way better)
			if (this.children().contains(where)){
				jqElts.insertBefore(where);
			}
		}
		
		else {		
			this.append(jqElts);
		}

		
	}
	
	allRows.parentNode = delegateParentNode;	
	return allRows;
}



jQuery.tableDnD.makeDraggable = function (table) {


    var config = table.tableDnDConfig;
	if (table.tableDnDConfig.dragHandle) {
		// We only need to add the event to the specified cells
		var cells = jQuery("td."+table.tableDnDConfig.dragHandle, table);
		cells.each(function () {
			// The cell is bound to "this"
            jQuery(this).mousedown(function (ev) {
         	
				var allRows = $(".ui-state-row-selected", table).add(this.parentNode);
				allRows.addClass('nodrop');	
				document.body.style.cursor = "n-resize";
                jQuery.tableDnD.dragObject = jQuery.tableDnD.adaptDragObject(allRows);
                jQuery.tableDnD.currentTable = table;
				
                jQuery.tableDnD.mouseOffset = jQuery.tableDnD.getMouseOffset(this, ev);
                if (config.onDragStart) {
                    // Call the onDrop method if there is one
                    config.onDragStart(table, allRows);
                }
                return false;
            });
		});
	} else {
	
		// For backwards compatibility, we add the event to the whole row
        var rows = jQuery("tr", table); // get all the rows as a wrapped set
        rows.each(function () {
			// Iterate through each row, the row is bound to "this"
			var row = jQuery(this);
			if (! row.hasClass("nodrag")) {
                row.mousedown(function (ev) {
                    if (ev.target.tagName == "TD") {
                    	var allRows = $(".ui-state-row-selected", table);   
						allRows.addClass('nodrop');						
                		
                        jQuery.tableDnD.dragObject = jQuery.tableDnD.adaptDragObject(allRows);
                        jQuery.tableDnD.currentTable = table;
                        jQuery.tableDnD.mouseOffset = jQuery.tableDnD.getMouseOffset(this, ev);
                        if (config.onDragStart) {
                            // Call the onDrop method if there is one
                            config.onDragStart(table, allRows);
                        }
                        return false;
                    }
                }).css("cursor", "move"); // Store the tableDnD object
			}
		});
	}
}

var tableDnDoldMouseUp = jQuery.tableDnD.mouseup;

jQuery.tableDnD.mouseup = function (event){
	if (jQuery.tableDnD.dragObject){
		jQuery.tableDnD.dragObject.removeClass('nodrop');
		document.body.style.cursor = "default";
	}
	tableDnDoldMouseUp.call(this,event);
}