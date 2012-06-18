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
 *
 * That class will not handle the usual dom event (in particular the .jstree namespaced events). It will rather handle
 * the messages between the contextual content and the workspace tree. Squash uses messages - or events - defined in Events.js
 * in this same package directory.
 *
 *
 * Settings :
 * 	tree : the real tree instance (not the jQuery object).
 */
 function TreeEventHandler(settings){
 
 	this.tree = settings.tree;
 
 	this.update = function(event){
		// todo : make something smarter
		switch(event.evt_name){
			case "paste" : updateEventPaste(event, this.tree); break;
			default : this.tree.refresh_selected(); break;
		}
 	}

 }
 

 /* *************************** update Events ********************* */

 function updateEventPaste(event, tree){
 	var destination = tree.findNodes({ restype : event.evt_destination.obj_restype, resid : event.evt_destination.obj_id });
 	if(!destination.isOpen()){
 		destination.open();
 	}
 	destination.children("ul").remove();
 	destination.load()
 	.done(function(){	
 		if(event instanceof EventDuplicate){			
 			var source = tree.findNodes({ restype : event.evt_source.obj_restype , resid : event.evt_source.obj_id });
 			source.deselect();
 			var duplicate = tree.findNodes({ restype : event.evt_duplicate.obj_restype, resid : event.evt_duplicate.obj_id });
 			duplicate.select();
 			
 		}	
 	});

 }