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

function TreeNodeCopier(initObj){
	
	// properties
	this.tree = $.jstree._reference(initObj.treeSelector);
	this.errMessage= initObj.errMessage;
	this.url= initObj.url;
	
	
	// private methods
	var displayError = function(){
		displayInformationNotification(this.errMessage);
	}
	
	var checkSameProject = function(target){			
		var targetLib = findParentLibrary(target);
		var previousLibId = $.cookie('squash-copy-library-id');
		return targetLib.attr('resid') === previousLibId;
	}
	
	var findParentLibrary = function (nodes){
		return nodes.is(':library') ? nodes : nodes.parents(":library");
	}
	
	// public methods
	this.copyNodesToCookie = function(){
		var nodes = this.tree.get_selected();
		var ids = nodes.collect(function(elt){return $(elt).attr('resid');});
		var library = findParentLibrary(nodes);
		var libraryId = library.attr('resid');
		$.cookie('squash-copy-nodes-ids', ids.toString());			
		$.cookie('squash-copy-library-id', libraryId);
		$.cookie('squash-copy-iterations-only', "0");
		if(nodes.filter(':iteration').length == nodes.length){
			$.cookie('squash-copy-iterations-only', "1");
		}
		
	}
	
	this.pasteNodesFromCookie = function(){
		var ids = $.cookie('squash-copy-nodes-ids').split(',');
		
		if (! ids) return;
		
		var target = this.tree.get_selected();
		var pasteAllowed = this.tree.selectionIsPasteAllowed(target);
		if (!checkSameProject(target)){
			this.errMessage = initObj.pasteNotSameProject;
			displayError.call(this);
		}
		else{ 
			if (pasteAllowed != "OK"){
				if(pasteAllowed == "notOneEditable"){
					this.errMessage =  initObj.notOneEditable;
				}
				else{ 
					if(pasteAllowed == "pasteIterationNotHere"){ this.errMessage =  initObj.pasteIterationNotHere;}
					else{ 
						if(pasteAllowed == "pasteNotHere") this.errMessage =  initObj.pasteNotHere;
						}
					}
				displayError.call(this);
			}
			else{
				if(!this.tree.is_open(target)){this.tree.open_node(target);}
				var destinationType = "folder";
				if(isRoot(target)){
					destinationType = "library";
				}
				// here we mimick the move_object used by moveNode, describe
				// earlier in the file
				var copyData = {
					inst : this.tree,
					sendData : {
						"object-ids" : ids,
						"destination-id" : target.attr('resid'),
						"destination-type" : destinationType
					},
					newParent : target
				}
				// if the destination is a campaign the request will not be the
				// same
				if(target.is(':campaign')){
					this.url = initObj.urlIteration ;
					var iterations = this.tree._get_children(target) ;
					copyData = {
							inst : this.tree,
							sendData : {
								"object-ids" : ids,
								"destination-id" : target.attr('resid'),
								"destination-type" : "campaign",
								"next-iteration-number" : iterations.length
							},
							newParent : target
						}
				}				
				
				// then we send it to the copy routine
				copyNode(copyData, this.url).fail(function(){this.tree.refresh();});
			}
		}
	
	}		
	
}

