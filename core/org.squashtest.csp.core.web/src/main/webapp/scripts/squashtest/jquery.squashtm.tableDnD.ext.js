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
jQuery.tableDnD.mousemove = function(ev) {
    if (jQuery.tableDnD.dragObject == null) {
        return;
    }

    var dragObj = jQuery(jQuery.tableDnD.dragObject);
    var config = jQuery.tableDnD.currentTable.tableDnDConfig;
    var mousePos = jQuery.tableDnD.mouseCoords(ev);
    var y = mousePos.y - jQuery.tableDnD.mouseOffset.y;
    //auto scroll the window
    var yOffset = window.pageYOffset;
 	if (document.all) {
        // Windows version
        //yOffset=document.body.scrollTop;
        if (typeof document.compatMode != 'undefined' &&
             document.compatMode != 'BackCompat') {
           yOffset = document.documentElement.scrollTop;
        }
        else if (typeof document.body != 'undefined') {
           yOffset=document.body.scrollTop;
        }

    }
	    
	if (mousePos.y-yOffset < config.scrollAmount) {
    	window.scrollBy(0, -config.scrollAmount);
    } else {
        var windowHeight = window.innerHeight ? window.innerHeight
                : document.documentElement.clientHeight ? document.documentElement.clientHeight : document.body.clientHeight;
        if (windowHeight-(mousePos.y-yOffset) < config.scrollAmount) {
            window.scrollBy(0, config.scrollAmount);
        }
    }


    if (y != jQuery.tableDnD.oldY) {
        // work out if we're going up or down...
        var movingDown = y > jQuery.tableDnD.oldY;
        // update the old value
        jQuery.tableDnD.oldY = y;
        // update the style to show we're dragging
		if (config.onDragClass) {
			dragObj.addClass(config.onDragClass);
		} else {
            dragObj.css(config.onDragStyle);
		}
        // If we're over a row then move the dragged row to there so that the user sees the
        // effect dynamically
        var currentRow = jQuery.tableDnD.findDropTargetRow(dragObj, y);
        if (currentRow) {
            // TODO worry about what happens when there are multiple TBODIES
			
			if (movingDown && jQuery.tableDnD.dragObject != currentRow) {
				var toInsert = jQuery.tableDnD.dragObject;
				for (var i=toInsert.length-1;i>=0;i--){
					jQuery.tableDnD.dragObject.parentNode.insertBefore(toInsert[i], currentRow.nextSibling);
				}
			} else if (! movingDown && jQuery.tableDnD.dragObject != currentRow) {
				var toInsert = jQuery.tableDnD.dragObject;
				for (var i=0;i<toInsert.length;i++){
					jQuery.tableDnD.dragObject.parentNode.insertBefore(toInsert[i], currentRow);
				}
			}
			
        }
    }

    return false;
}


// initializer

function setDraggedRowsUp(allRows){
	
	var delegateParentNode= allRows[0].parentNode;				
	
	//TODO : override delegateParentNode.insertBefore()
	
	allRows.parentNode = delegateParentNode;	
	return allRows;
}


jQuery.tableDnD.makeDraggable = function(table) {


    var config = table.tableDnDConfig;
	if (table.tableDnDConfig.dragHandle) {
		// We only need to add the event to the specified cells
		var cells = jQuery("td."+table.tableDnDConfig.dragHandle, table);
		cells.each(function() {
			// The cell is bound to "this"
            jQuery(this).mousedown(function(ev) {
         	
				var allRows = $("tr.nodrop", table);    
				
                jQuery.tableDnD.dragObject = setDraggedRowsUp(allRows);
                jQuery.tableDnD.currentTable = table;
                jQuery.tableDnD.mouseOffset = jQuery.tableDnD.getMouseOffset(this, ev);
                if (config.onDragStart) {
                    // Call the onDrop method if there is one
                    config.onDragStart(table, this);
                }
                return false;
            });
		})
	} else {
		// For backwards compatibility, we add the event to the whole row
        var rows = jQuery("tr", table); // get all the rows as a wrapped set
        rows.each(function() {
			// Iterate through each row, the row is bound to "this"
			var row = jQuery(this);
			if (! row.hasClass("nodrag")) {
                row.mousedown(function(ev) {
                    if (ev.target.tagName == "TD") {
                    	var allRows = $("tr.nodrop", table);      
                    	allRows.parentNode = allRows[0].parentNode;         	      		
                		
                        jQuery.tableDnD.dragObject = allRows;
                        jQuery.tableDnD.currentTable = table;
                        jQuery.tableDnD.mouseOffset = jQuery.tableDnD.getMouseOffset(this, ev);
                        if (config.onDragStart) {
                            // Call the onDrop method if there is one
                            config.onDragStart(table, this);
                        }
                        return false;
                    }
                }).css("cursor", "move"); // Store the tableDnD object
			}
		});
	}
}
*/