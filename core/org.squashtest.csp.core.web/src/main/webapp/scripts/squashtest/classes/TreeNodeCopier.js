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
	


	// ***************** private methods *********************
	
	var displayError = function(){
		if (arguments.length==0){
			displayInformationNotification(this.errMessage);
		}else{
			displayInformationNotification(arguments[0]);
		}
	};
	

	var reset = function(){
		$.cookie('squash-copy-nodes', null);
	};
	
	var retrieve = function(){
		var data = $.cookie('squash-copy-nodes');
		return JSON.parse(data);
	};

	var store = function(nodesData, libraryId){
	
		var data = {
			library : libraryId,
			nodes : nodesData
		}
		
		var jsonData = JSON.stringify(data);
		
		$.cookie('squash-copy-nodes', jsonData);
	};
	
	
	var denyPaste = function(flag){
		switch (flag){
			case "not-unique-editable" : displayError(initObj.notOneEditable); break;
			case "wrong-library"	:	displayError(initObj.pasteNotSameProject); break;
			case "target-type-invalid" : displayError(initObj.pasteIterationNotHere); break;
			case "buffer-empty" : displayError("no nodes copied  - (todo : localization)"); break;		
		}
	}
	
	var preparePasteData = $.proxy(function(nodes, target){
	
		var destinationType;
		var url;
		
		// todo : makes something better if we can refractor the whole service in depth one day.
		switch(target.getDomType()){
			case "drive" : 		destinationType = "library"; 
								url = initObj.url;
								break;
								
			case "folder" : 	destinationType = "folder"; 
								url = initObj.url;
								break;
								
			case "file" : 		destinationType = "campaign"; 
								url = initObj.url+"-iterations";
								break;
								
			case "resource" : 	destinationType = "iteration"; 
								url = initObj.url+"-test-suites"; 
								break;
			default : "azeporiapzeorj"; //should not happen if this.mayPaste() did its job.
		}
		
		//here we mimick the move_object used by tree.moveNode, defined in
		//jquery.squashtm.jstree.ext.js.
		var pasteData = {
			inst : this.tree,
			sendData : {
				"object-ids" : nodes.all('getResId'),
				"destination-id" : target.attr('resid'),
				"destination-type" : destinationType
			},
			newParent : target,
			url : url
		}
	
		return pasteData;
	
	}, this);
	
	
	// ****************** public methods **********************
	
	// ****** returns a boolean *************
	
	this.mayCopy = function(){
		
		var nodes = this.tree.get_selected();
		
		var consistentKind = (
			nodes.areNodes() ||
			nodes.areResources() ||
			nodes.areViews()		
		);
		
		var sameLib = nodes.areSameLibs();
		
		return (consistentKind && sameLib);
		
	}
	
	this.copyNodesToCookie = function(){
		
		reset();	
		
		if ( ! this.mayCopy() ){
			displayError(initObj.errMessage);
			return;
		}
		
		var nodes = this.tree.get_selected();
		
		var nodesData = nodes.toData();
		var libId = nodes.getLibrary().getDomId();
		
		store(nodesData, libId);
	};
	
	// *** that function checks that the operation is indeed allowed
	// *** the returned value is a status as string giving informations about
	// *** why the user can't perform the operation
	this.mayPaste = function(){
		
		var data = retrieve();		
		if (data == null) return "buffer-empty";
		
		var nodes = this.tree.findNodes(data.nodes);
		if (nodes.length == 0) return "buffer-empty";
		
		var target = this.tree.get_selected();
		
		var isUnique = (target.length == 1);		
		var isEditable = target.isEditable();
		
		if (!(isUnique && isEditable)) return 'not-unique-editable';
		
		var sameLib = (target.getLibrary().getDomId() == data.library);
		
		var validTarget = (
			( target.match( { rel : 'drive' } ) && nodes.areNodes() )	||
			( target.match( { rel : 'folder'} ) && nodes.areNodes() )	||
			( target.match( { rel : 'file'} ) && nodes.areResources() ) ||
			( target.match( { rel : 'resource'}) && nodes.areViews() )
		);		
		
		if (! sameLib) return 'wrong-library';
		
		if (! validTarget) return 'target-type-invalid';
		
		return 'OK';
	};
	
	
	
	this.pasteNodesFromCookie = function(){
		
		var flag = this.mayPaste();
		
		if ( flag!="OK" ){
			denyPaste(flag);
			return;
		}
		
		var target = this.tree.get_selected();
		var data = retrieve();
		var nodes = this.tree.findNodes(data.nodes);
		
		target.open();
		
		var pasteData = preparePasteData(nodes, target);
		
		//another special delivery for iterations (also should be refractored)
		if (target.is(':campaign')){
			pasteData.sendData["next-iteration-number"] = target.getChildren().length;
		}
		
		//now we can proceed
		copyNode(pasteData, pasteData.url)
		.done(reset)
		.fail(this.tree.refresh);
		
	};
	
	
	
}

