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
/**
 * Common functions for JsTree manipulation
 * 
 * @author Gregory Fouquet, Benoît Siri
 */

 
/**
 *
 * That function clean the contextual content from its temporary widgets.
 * The goal is to prevent undesired interactions between page controls.
 *
 * @param targetSelector 
 *			a jQuery selector being the handle to the contextual content div.
 */

(function($){
	/*
	
		Override of jstree.dnd.dnd_show. We want it to always target "inside" when the target is a container.
	
		For that purpose we need to modify the "marker". The marker represents the position where the node will be inserted, both graphically and internally. The marker can be set to 
		3 positions : before, inside and after. 
		
		The marker can be set to one of these positions if the node being browsed supports it. Let's call it the "drop profile" of the node. That profile is held by the this.data.dnd object.
		
			- For libraries (aka drives) and folders, the drop profile is true, true, true.
			- For leaves, the profile is true, false, true.
		
		The drop profile is unfortunately not configurable. Also, comparing the drop profile is the only way to differenciate a leaf from the rest.
		
		The present function will identify the hovered node and return the following marker position :
			- for leaves -> no modification
			- for folders, libraries -> force 'inside'.
	
	*/

	var overridenM;


	$.jstree._fn.dnd_show = function(){
				
			//that variable in the vanilla tree is part of the closure context we cannot access here, so we must fetch it by other means.
			if (overridenM===undefined){
				overridenM = $("div#jstree-marker");
			}	
			
			var o = ["before","inside","after"],
			r = false,
			rtl = this._get_settings().core.rtl,
			pos;		
			
			if(this.data.dnd.w < this.data.core.li_height/3) { o = ["before","inside","after"]; }
			else if(this.data.dnd.w <= this.data.core.li_height*2/3) {
				o = this.data.dnd.w < this.data.core.li_height/2 ? ["inside","before","after"] : ["inside","after","before"];
			}
			else { o = ["after","inside","before"]; }
			$.each(o, $.proxy(function (i, val) { 
				if(this.data.dnd[val]) {
					$.vakata.dnd.helper.children("ins").attr("class","jstree-ok");
					r = val;
					return false;
				}
			}, this));
			
			
			if(r === false) { 
				$.vakata.dnd.helper.children("ins").attr("class","jstree-invalid"); 
			}
			
			//here we override the function. if the profile matches the one of a container, we force r to "inside"
			if (this.data.dnd.before && this.data.dnd.inside && this.data.dnd.after){
				r = "inside";
			}
			
			pos = rtl ? (this.data.dnd.off.right - 18) : (this.data.dnd.off.left + 10);

			switch(r) {
				case "before":
					overridenM.css({ "left" : pos + "px", "top" : (this.data.dnd.off.top - 6) + "px" }).show();
					break;
				case "after":
					overridenM.css({ "left" : pos + "px", "top" : (this.data.dnd.off.top + this.data.core.li_height - 7) + "px" }).show();
					break;
				case "inside":
					overridenM.css({ "left" : pos + ( rtl ? -4 : 4) + "px", "top" : (this.data.dnd.off.top + this.data.core.li_height/2 - 5) + "px" }).show();
					break;
				default:
					overridenM.hide();
					break;
			}
			return r; 	
			
	}
	


	
	/*
	 * squash tree plugin
	 */
	 $.jstree.plugin("squash",{
		__init : function(){
			
			var tree= this;
			var s = this._get_settings().squash;
			tree.data.squash.timeout=s.timeout;
			tree.data.squash.isie=false;
			
			var container = this.get_container();
			
			/* we need our handlers to be bound first 
			 * note that we are bound to 'click' and not 'click.jstree'. That detail matters 
			 * in the handler just below.
			 */

			/* note about click events and browsers specificities :
			 * 	- ff, chrome : 2 clicks fire 2 click and 1 dblclick event. both event objects have a property .detail
			 *  - ie 8 : fire click and dblclick alternately.
			 *
			 * considering the discrepencies between those behavior the node click handling will branch wrt event.detail.
			 * FF and Chrome will simply use a clickhandler, while ie 8 will use both a click and a dblclick handler.
			 *
			 */
			container.bindFirst("live", function(){
			
				container.delegate("a",'click', function(event, data) {
					if (event.detail && event.detail>1){
						event.stopImmediatePropagation();		//cancel the multiple click event for ff and chrome
					}else{
						handleNodeClick(tree, event);
					}
				});
				
				container.delegate('a', 'dblclick ', function(event,data){
					handleNodeDblClick(tree, event);
				});
			});
			
			container
				//that section is copied/pasted from the original themeroller plugin, kudos mate.
				.addClass("ui-widget-content")
				.delegate("a","mouseenter.jstree", function () {
					$(this).addClass(s.item_h);
				})
				.delegate("a","mouseleave.jstree", function () {
					$(this).removeClass(s.item_h);
				})
				.bind("select_node.jstree", $.proxy(function (e, data) {
						data.rslt.obj.children("a").addClass(s.item_a);
						return true;
					}, this))
				.bind("deselect_node.jstree deselect_all.jstree", $.proxy(function (e, data) {
						this.get_container()
							.find("." + s.item_a).removeClass(s.item_a).end()
							.find(".jstree-clicked").addClass(s.item_a);
						return true;
					}, this));
		},
		_fn : {
			allowedOperations : function(){
				var selectedNodes = this.get_selected();					
				var operations = "";				
				//that variable will be set to true if at least one selected node is not editable.
				var noEdit = (selectedNodes.not(":editable").length > 0);
				
				//case 1 : not nodes, or not editable nodes : no operations allowed. 
				if (selectedNodes.length==0 || noEdit){
					operations = "";
				}
				//case 2 : more than one item selected : deletion and copy if nodes group does not include a library
				else if (selectedNodes.length != 1){
					operations = (! selectedNodes.is(":library")) ? "delete copy " : "";
				}
				//case 3 : one item is selected, button activation depend on their nature.
				else{
					switch(selectedNodes.attr('rel')){			
						case "drive" :
							operations="create-folder create-file paste import";
							break;
						
						case "folder" :
							operations="create-folder create-file rename delete copy paste";
							break;
							
						case "file" :
							operations="create-resource rename delete copy";
							break;
							
						case "resource" : 
							operations="rename delete";
							break;
					}
				}
				return operations;			
			}		
		},
		defaults : {
			"item_h" : "ui-state-active",
			"item_a" : "ui-state-default",
			"timeout" : 500
		}
	 });

	 /*
	  * specialization for tree-pickers.
	  * will maintain the order in which nodes were selected and redefine get_selected to return the nodes in that order.
	  */
	 $.jstree.plugin("treepicker",{
		__init : function(){
			this.data.treepicker.counter=0;
			this.data.treepicker.ordering = $();
			var container = this.get_container();
			
			container.bind("select_node.jstree", $.proxy(function (e, data) {
				var id=data.rslt.obj.attr('resid');
				var counter = this.data.treepicker.counter++;
				data.rslt.obj.attr('order',counter);
			}, this));
		},
		_fn : {
			get_selected : function(){
				var selected = this.data.ui.selected.toArray();
				selected.sort(function(a,b){
					var order_a = $(a).attr('order');
					var order_b = $(b).attr('order');
					return order_a - order_b;
				});
				
				return $(selected);
			}
		}
	 
	 
	 });
	 
	 
	 /*
	  * definition of the treemenu buttons. 
	  * Parameter : 
	  * 	- contentSelector : the selector of the content.
	  * 	- params : a map association <buttonPropertyName, buttonSelector>.
	  * 
	  * Note 1 : the way the menu was implemented forces us to ugly things and should need refactoring once it's included in the trunk of jQuery UI.
	  * Note 2 : I had no choice but modifying jquery.fg.menu.js directly, specifically the methods showMenu() and kill(), due to the careless managment of
	  * event unbinding.
	  */
	
	 $.fn.treeMenu = function(contentSelector, params){
	  	 this.menu({
			 content : $(contentSelector).html(),
			 showSpeed : 0
			
		 });
		 
		 //ugly thing here. The widget is lazily created and we don't want that if we want to bind our events on the menu item.
		 //so we force creation and hide it right away.
		 var menu = allUIMenus[allUIMenus.length-1];
		 menu.create();
		 menu.kill();
		 
		 this.buttons = {};
		 
		 for (var getter in params){
			//menu.create did create a clone of the content which class is fg-menu-container. We'll be looking at the item we want to bind
			//in the cloned content.
			var selector=".fg-menu-container "+params[getter];
			var button = $(selector);
			button.enable = function(){$(this).removeClass('menu-disabled');};
			button.disable = function(){$(this).addClass('menu-disabled');};
			button.click(function(event){event.preventDefault(); if ($(this).is('.menu-disabled')) event.stopImmediatePropagation();});
			
			this.buttons[getter] = button;
		 }
		 
		 return this;  
	 }
	 
	 
	
})(jQuery);





/* *************************** node click behaviour ********************* */


/**
 * Behaviour of a node when clicked or double clicked There are two possible paths : 
 *  1) the node is not a container (files, resources) : 
 *				a/ click events : proceed, 
 *				b/ double click event (and further click event) : cancel that event and let the first one complete.
 *  2) the node is a container (libraries, folders, campaigns) : 
 * 				a/ click event : start a timer. If the timer is not canceled, fire a specific click.jstree event.
 *              b/  double click event (and further click event) : toggle the node and stop event propagation.
 *
 * Basically we'll stop the event propagation everytime except for case 1-a. The case 2-a actually do not let the event
 * propagate : it fires a new 'click.jstree' event instead. The reason for this is because the following handler is bound
 * to 'click' and we don't want it to be called again.
 *
 * cases 1-a and 2-a are treated in handleNodeClick, while 1-b and 2-b are treated in handleNodeDblClick. 
 *
 * @params : 
 *  - tree : the tree instance
 *  - clickEvent : the click event.
 *
 */ 
 
 /* 
  * here we want to delay the event for folders, libraries and campaign (waiting for a possible dblclick), while letting
  * the event through for the other kind of nodes.
  */
 function handleNodeClick(tree, event){
	var target = $(event.target);
	var node = target.parent();
	
	
	if (node.is(':library') || node.is(':folder') || node.attr('restype') == "campaigns"){
		if (event.ctrlKey) return true;
		event.stopImmediatePropagation();
	
		tree.data.squash.clicktimer = setTimeout(function(){	
			target.trigger('click.jstree');
		},tree.data.squash.timeout);
	}
 }
 
 /* 
  * here we handle dblclicks. basically we don't want the event to be processed twice, except for containers
  * that will toggle their open-close status.
  */
 function handleNodeDblClick(tree, event){
	var target = $(event.target);
	var node = target.parent();
	
	event.stopImmediatePropagation();
	clearTimeout(tree.data.squash.clicktimer);
	tree.toggle_node(node);			
}

function clearContextualContent(targetSelector){
	$('.is-contextual').each(function(){
		//todo : kill the damn ckeditor instances				
		$(this).dialog("destroy").remove(); 
	});
	$(targetSelector).empty();		
}





/* ***************************  post new nodes operations ********************************************** */
/**
 * Post new contents to the url determined by the selected node of a tree and
 * creates a new node with returned JSON data.
 * 
 * @param treeId
 *            html id of the tree
 * @param contentDiscriminator
 *            discriminator to append to post url (determines content to be
 *            created)
 * @param postParameters
 *            map of post params
 */
function postNewTreeContent(treeId, contentDiscriminator, postParameters) {

	/* **************** variables init ****************** */

	var tree = $('#' + treeId);
	var newNode = null;
	var url = tree.data('selectedNodeContentUrl') + '/' + contentDiscriminator;
	var currentNode = tree.jstree("get_selected");
	
	var isOpen = tree.jstree('is_open', currentNode);
	
	/* ***************** function init ******************** */

	var openNode = function(){
		var defer = $.Deferred();
		tree.jstree('open_node', currentNode, defer.resolve);
		return defer.promise();
	}
	
	
	var postNode = function(){
		var defer = $.Deferred();
		return $.ajax({
			url : url,
			data : postParameters,
			type : 'POST',
			dataType : 'json',
			success : defer.resolve,
			contentType: "application/x-www-form-urlencoded;charset=UTF-8"
		});
		return defer.promise();
	}
	
	var addNode = function(data){
		var defer = $.Deferred();
		newNode = tree.jstree('create_node',
			currentNode,
			'last',
			data,
			defer.resolve,
			true);
			
		return defer.promise;	
	}
	
	var selectNode = function(){
		tree.jstree('select_node', newNode);
		unselectFather(newNode, $('#' + treeId));
		openNode(); 	//yes, we need to repoen it. This is required if the newly added node is the first node that the parent contains.
	}

	
	var createNode = function(){
		postNode()
		.then(addNode)
		.then(selectNode)
		.then(openNode);
	}

	/* ********** actual code. ****************** */
	
	if (isOpen != true){
		openNode()			//first call will make the node load if necessary. 
		.then(createNode);
	}
	else{
		createNode();
	}

}

/* **************************** check move section **************************************** */

/*
 * Will check if a dnd move is legal. Note that this check is preemptive, contrarily to checkMoveIsAuthorized
 * which needs to post-check. 
 *
 */
function treeCheckDnd(m){
	
	var object = m.o;
	var dest = m.np;
	var src = m.op;
	
	var jqSrc = $(src);
	var jqDest = $(dest);
	var jqObject = $(object);
	
	//check if the node is draggable first
	if (! jqObject.is(':editable')){
		return false;
	}
	

	//check if the src and dest are within the same project. If they aren't themselve a drive we
	//need to look for them.
	var srcDrive =  jqSrc.is(':library') ? jqSrc : jqSrc.parents(':library');
	var destDrive = jqDest.is(':library') ? jqDest : jqDest.parents(':library');
	
	if ((srcDrive==null) || (destDrive==null)){
		return false;
	}
	
	if (srcDrive.attr('resid')!=destDrive.attr('resid')){
		return false;
	}
	
	//in case we are moving an iteration, check the destination is 
	//of type campaign
	
	if ( ($(object).is(':iteration')) && (! $(dest).is(':file')) ){
		return false;
	}
	
	//if the object is an iteration, the destination must be the same campaign
	if(jqObject.is(':iteration') && jqSrc.attr('resid') != jqDest.attr('resid')){
		return false;
	}
	//prevent iteration copy
	if(jqObject.is(':iteration') && isCtrlClicked == true){
		return false;
	}
	
	return true;			
	
}

		
		
	/*
	  This method checks if we can move the object is the dest folder returns true if it's ok to move the object note that contrary to 
	  treeCheckDnd(moveObject), that code is called only for "move", not "copy" operations, and thus is not part of the aforementioned function.
	  
	  A second reasons is that we don't want to forbid the operation a-priori : we cancel it a-posteriori. Thus, the user will know
	  why the operation could not be performed instead of wondering why the hell he cannot move the bloody node.	  
	 */
	function checkMoveIsAuthorized(data){
		var dest = data.rslt.np;
		var object = data.rslt.o;
		//checks if there's an element with the same name in the dest folder
		//get all the children nodes
		elInDest = dest.children("ul").children("li");
		okToGo = true;
		//compare object name and type to each children attributes
		elInDest.each(function(index, element){
			if(object.attr('name') == $(element).attr('name') 
					&& object.attr('id') != $(element).attr('id')){
				//detect if there's a similar element in the container 
				//(check the id not to compare the object with itself)
				okToGo = false;
			}
		});
		return okToGo;
	}


/* ***************************** node copy section **************************************** */


/*
	jstree inserts dumb copies when we ask for copies. We need to destroy them before inserting the correct
	ones incoming from the server.
	
	@param object : the move_object returned as part of the data of the event mode_node.jstree.
	
*/
function destroyJTreeCopies(object, tree){
	object.oc.each(function(index, elt){
		tree.delete_node( elt);
	});
}


/*
	will batch-insert nodes incoming from the server.
	
	@param jsonResponse : the node formatted in json coming from the server.
	
	@param currentNode : the node where we want them to be inserted.
	
	@param tree : the tree instance.
*/
function insertCopiedNodes(jsonResponse, currentNode, tree){
	for (var i=0;i< jsonResponse.length; i++){
		tree.create_node(
			currentNode,
			'last',
			jsonResponse[i],
			false,
			true
		);
	}
}

function moveObjectToCopyData(moveObject){
	
	var nodeData = moveObject.args[0];
	
	return  {
		inst : moveObject.inst,
		sendData : {
			"object-ids" : $(nodeData.o).collect(function(e){return $(e).attr('resid');}),
			"destination-id" : nodeData.np.attr('resid'),
			"destination-type" : isRoot(nodeData.np) ? "library" : "folder"
		},
		newParent : nodeData.np
	}
}

/*
	will erase fake copies in the tree, send the copied node data to the server, and insert the returned nodes.
	
	@param data : the data associated to the event move_node.jstree
	
	@param url : the url where to send the data.
	
	@returns : a promise

*/
function copyNode(data, url){
	
	var deferred = $.Deferred();

	var tree=data.inst;
	var newParent = data.newParent;	
	var dataSent = data.sendData;
	
	
	$.when(tree.open_node(newParent))
	.then(function(){
	
		$.ajax({
			type : 'POST',
			url : url,
			data : dataSent,
			dataType : 'json'
		})
		.success(function(jsonData){
			insertCopiedNodes(jsonData, newParent, tree);
			tree.open_node( newParent, deferred.resolve);
		})
		.error(deferred.fail);
	});
	
	return deferred.promise();
}


/* ******************************* node move section ******************************** */

/*
	we reject iterations. Beside that it's okay.
	
	@param data : the move_node object
	@param url : the url to post to.


*/
function moveNode(data, url){

	var tree=data.inst;
	var nodeData = data.args[0];
	var newParent = nodeData.np;
	
	//first check if we don't need to perform an operation
	if (nodeData.o.length==0){
		return;
	}
	
	//we also reject iterations.
	var firstNode=nodeData.o[0];
	if ($(firstNode).is(":iteration")){
		return;
	}
	
	var dataSent = {
		"object-ids" : $(nodeData.o).collect(function(e){return $(e).attr('resid');}),
		"destination-id" : nodeData.np.attr('resid'),
		"destination-type" : isRoot(nodeData.np) ? "library" : "folder"
	};
	
	tree.open_node(newParent);

	return $.ajax({
		type : 'POST',
		url : url,
		data : dataSent,
		dataType : 'json'
	})


}


/* ******************************* leaf URL management code ************************************* */ 

/**
 * Returns the url where to GET the content (ie children) of a node. This url
 * should return JSON tree nodes.
 * 
 * @param urlRoot
 * @param node
 * @returns {String}
 */
function nodeContentUrl(urlRoot, node) {
	return urlRoot + '/' + node.attr('rel') + 's/' + node.attr('resId')
			+ '/content';
}
/**
 * Returns the url where to GET the resource represented by a node. Thus url
 * usually returns HTML.
 * 
 * @param urlRoot
 * @param node
 */
function nodeResourceUrl(urlRoot, node) {
	return urlRoot + '/' + node.attr('resType') + '/' + node.attr('resId');
}
/**
 * Stores URLs relative to the current selected node in the tree
 * 
 * @param selResourceUrl
 * @param selResourceContentUrl
 * @param selNodeContentUrl
 * @param selResourceId
 */
function storeSelectedNodeUrls(treeId, selResourceUrl, selNodeContentUrl,
		selResourceId) {
	var tree = $('#' + treeId);
	tree.data('selectedResourceUrl', selResourceUrl);
	tree.data('selectedNodeContentUrl', selNodeContentUrl);
	tree.data('selectedResourceId', selResourceId);
}

/**
 * Unselects the nodes of the given tree which are not siblings of the given liNode.
 * 
 * @param liNode
 * @param tree
 */
function unselectNonSiblings(liNode, tree) {
	if (liNode.attr('rel') == 'drive') {
		return;
	}

	var previouslySelected = findSelectedNodes(tree);

	if (previouslySelected.length > 0) {
		var parent = liNode.parents('li:first');
		var siblings = $(parent).children('ul').children('li');

		var notSelectables = $(previouslySelected).not(siblings);

		notSelectables.each(function(index, element) {
			tree.jstree('deselect_node', element);
		});
	}
}
/**
 * Unselects the node of the given tree that is the father of the given liNode.
 * 
 * @param liNode
 * @param tree
 */
function unselectFather(liNode, tree){
	var parent = liNode.parents('li:first');
	tree.jstree('deselect_node', parent);
}

function unselectDescendantsAndOtherProjectsSelections(liNode, tree){
	var previouslySelected = findSelectedNodes(tree);
	if (previouslySelected.length > 0) {
		var descendants = unselectDescendants(liNode, tree);
		previouslySelected = $(previouslySelected).not(descendants);
		if(previouslySelected.length > 0){
			unselectOtherProjectsSelections(liNode, tree, previouslySelected);
		}
	}
}
/**
 * Unselects the nodes of the given tree which are descendants of the given liNode.
 * 
 * @param liNode
 * @param tree
 */
function unselectDescendants(liNode, tree){
	var descendants = $(liNode).find('li');
		
	if(descendants.length > 0 ){
		$(descendants).each(function(index, element) {
			tree.jstree('deselect_node', element);
		});
	}
	return descendants;
}
/**
 * Unselects the nodes of the given tree which are not descendants of the same project as the given liNode.
 * 
 * @param liNode
 * @param tree
 * @param previouslySelected
 */
function unselectOtherProjectsSelections(liNode, tree, previouslySelected){
	var libraryOfSelectedNode ;
	if($(liNode).is('[rel|="drive"]')){
		libraryOfSelectedNode = $(liNode)[0];
	 }else{
		libraryOfSelectedNode = $(liNode).parents('[rel|="drive"]')[0];
	}
	var libraryOfSelectedNodeDescendants = $(libraryOfSelectedNode).find('li');
	libraryOfSelectedNodeDescendants.push(libraryOfSelectedNode);
	
	var nodesToUnselect = previouslySelected.not(libraryOfSelectedNodeDescendants);
	$(nodesToUnselect).each(function(index, element) {
			tree.jstree('deselect_node', element);
	});
	
}

function findSelectedNodes(tree) {
	return tree.find('.jstree-clicked').parent('li');
}

	
/* ****************************** other tree-related objects ************************************** */


function TreeNodeCopier(initObj){
	
	//properties
	this.tree = $.jstree._reference(initObj.treeSelector);
	this.errMessage= initObj.errMessage;
	this.url= initObj.url;
	
	
	//private methods		
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
	
	//public methods
	this.copyNodesToCookie = function(){
		var nodes = this.tree.get_selected();
		var ids = nodes.collect(function(elt){return $(elt).attr('resid');});
		var library = findParentLibrary(nodes);
		var libraryId = library.attr('resid');
		$.cookie('squash-copy-nodes-ids', ids.toString());			
		$.cookie('squash-copy-library-id', libraryId);
		
	}
	
	this.pasteNodesFromCookie = function(){
		var ids = $.cookie('squash-copy-nodes-ids').split(',');
		
		if (! ids) return;
		
		var target = this.tree.get_selected();
		
		if (target.length!=1 || (! target.is(':editable')) || (! checkSameProject(target))){
			displayError.call(this);
		}
		else{
							
			//here we mimick the move_object used by moveNode, describe earlier in the file
			var copyData = {
				inst : this.tree,
				sendData : {
					"object-ids" : ids,
					"destination-id" : target.attr('resid'),
					"destination-type" : isRoot(target) ? "library" : "folder"						
				},
				newParent : target
			}
			
			//then we send it to the copy routine
			copyNode(copyData, this.url)
			.fail(function(){this.tree.refresh();});
		}
	
	}		
	
}

