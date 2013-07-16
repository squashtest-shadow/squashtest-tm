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

/*
 * Implements the dnd api 
 * 
 */

define(['jquery', 'workspace.tree-node-copier', 'workspace.permissions-rules-broker', 'squash.translator', 'jstree'], function($, nodecopier, rulesbroker, translator){
	
	
	/* *******************************************************************************
					Library part	
	******************************************************************************** */
	

	/*
	 * *************************** post new nodes operations **********************************************
	 */
	/**
	 * Post new contents to the url determined by the selected node of a tree and creates a new node with returned JSON
	 * data.
	 * 
	 * @param treeId
	 *          html id of the tree
	 * @param contentDiscriminator
	 *          discriminator to append to post url (determines content to be created)
	 * @param postParameters
	 *          map of post params
	 * @param selectNewNode
	 *          optional, default = true
	 */

	function postNewNode(contentDiscriminator, postParameters, selectNewNode) {
		if (selectNewNode === undefined) {
			selectNewNode = true;
		}
		// **************** variables init ******************

		var origNode = this.get_selected();
		var targetNode;

		if ((origNode.is(':library')) || (origNode.is(':folder')) ||
				((origNode.is(':campaign')) && (contentDiscriminator == "new-iteration")) ||
				((origNode.is(':requirement')) && (contentDiscriminator == "new-requirement")) ) {
			targetNode = origNode;
		} else {
			targetNode = origNode.getParent();
		}

		var url = targetNode.getContentUrl() + '/' + contentDiscriminator;
		var newNode = null;

		// ***************** function init ********************

		var postNode = function() {
			return $.ajax({
				url : url,
				data : postParameters,
				type : 'POST',
				dataType : 'json',
				contentType : "application/x-www-form-urlencoded;charset=UTF-8"
			});
		};

		var addNode = function(data) {
			var res = targetNode.appendNode(data);
			newNode = res[0];
			return res[1];
		};

		var selectNode = function() {
			targetNode.deselect();
			origNode.deselect();

			newNode.select();
			return targetNode.open();
		};

		var createNode = function() {
			if (selectNewNode) {
				return postNode().then(addNode).then(selectNode);
			} else {
				return postNode().then(addNode);
			}
		};

		// ********** actual code. ******************

		var isOpen = targetNode.isOpen();
		if (!isOpen) {
			return targetNode.open() // first call will make the node load if necessary.
			.then(createNode);
		} else {
			return createNode();
		}

	}
	

	/*
	 * **************************** dnd check section ****************************************
	 */

		
	/*
	 * Will check if a dnd move is legal. Note that this check is preemptive, contrarily to checkMoveIsAuthorized which
	 * needs to post-check.
	 * 
	 * NB : this method is called by the configuration of plugin "crrm" in the initialization object.
	 * 
	 */
	function check_move() {
		
		var rules = this._getRules();
		
		try{
			//this simple test will cut short useless tests.	
			var move = this._get_move();
			
			if (! move.np.is('li')){
				return false;
			}
			
			var	movednodes = $(move.o).treeNode();
			var	newparent = $(move.np).treeNode();
			
			return rules.canDnD(movednodes, newparent);
			
		} catch (invalid_node) {
			if (console && console.log){
				console.log(invalid_node.message);
			}
			return false;
		}

	}

	/*
	 * This method checks if we can move the object is the dest folder returns true if it's ok to move the object. Note
	 * that contrary to checkDnd(moveObject), that code is called only for "move", not "copy" operations, and thus is
	 * not part of the aforementioned function.
	 * 
	 * A second reasons is that we don't want to forbid the operation a-priori : we cancel it a-posteriori. Thus, the user
	 * will know why the operation could not be performed instead of wondering why he cannot move the  nodes.
	 */
	function check_name_available(data) {
		var dest = data.rslt.np;
		var object = data.rslt.o;

		// here are the names of all destination children, and the names of the
		// moved objects
		destNames = dest.children("ul").children("li").not(object).collect(function(elt) {
			return $(elt).attr('name');
		});
		movedNames = object.collect(function(elt) {
			return $(elt).attr('name');
		});

		var okay = true;
		for ( var i in movedNames) {
			if ($.inArray(movedNames[i], destNames) >= 0) {
				okay = false;
				break;
			}
		}
		return okay;
	}

	
	var warnIfisCrossProjectOperation = function(target, nodes){
		
		var defer = $.Deferred();
		
		var targetLib = target.getLibrary().getDomId(),
			destLibs = nodes.getLibrary().map(function(){
				return $(this).attr('id');
			}),
			isCrossProject = false;
		
		for ( var i = 0; i < destLibs.length; i++) {
			if (targetLib != destLibs[i]) {
				isCrossProject = true;
				break;
			}
		}
		
		if (isCrossProject) {
			oneShotConfirm('Info', translator.get('message.warnCopyToDifferentLibrary'),
					squashtm.message.confirm, squashtm.message.cancel)
			.done(function() {
				defer.resolve();
			})
			.fail(function() {
				defer.reject();
			});
		} else {
			defer.resolve();
		}		
		
		return defer.promise();
	};

	
	// ******************************* node move operations ****************************
	
	/*
	 * 
	 * @param data : the move_node object @param url : the url to post to.
	 */
	function moveNodes(data) {

		var tree = data.inst,
			nodeData = data.rslt,
			nodes = nodeData.o,
			target = nodeData.np;

		// first check if we don't need to perform an
		// operation
		if (nodeData.o.length === 0) {
			return;
		}

		// we also reject iterations and testsuite
		var firstNode = nodeData.o[0];
		if ($(firstNode).is(":iteration, :test-suite")) {
			return;
		}


		var rawurl = $(target).treeNode().getMoveUrl();
		var nodeIds = $(nodes).treeNode().all('getResId').join(',');
		var url = rawurl.replace('\{nodeIds\}', nodeIds);

		tree.open_node(target);

		return $.ajax({
			type : 'PUT',
			url : url,
			dataType : 'json'
		})
		.fail(function(){
			$.jstree.rollback(data.rlbk);
		});
	}



	/*
	 * ***************************** node copy section ****************************************
	 */
	
    /*
     * jstree inserts dumb copies when we ask for copies. We need to destroy them before inserting the correct ones
     * incoming from the server.
     * 
     * @param object : the move_object returned as part of the data of the event mode_node.jstree.
     * 
     */
    function destroyJTreeCopies(object, tree) {
            object.oc.each(function(index, elt) {
                    tree.delete_node(elt);
            });
    }

	/*
	 * will batch-insert nodes incoming from the server.
	 * 
	 * @param jsonResponse : the node formatted in json coming from the server.
	 * 
	 * @param currentNode : the node where we want them to be inserted.
	 * 
	 * @param tree : the tree instance.
	 */
	function insertCopiedNodes(jsonResponse, currentNode, tree) {
		for ( var i = 0; i < jsonResponse.length; i++) {
			tree.create_node(currentNode, 'last', jsonResponse[i], false, true);
		}
	}


	/*
	 * will erase fake copies in the tree, send the copied node data to the server, and insert the returned nodes.
	 * Note that this method is invoked by the tree-node-copier.
	 * 
	 * @param data : nodesIds
	 * 
	 * @param url : the url where to send the data.
	 * 
	 * @returns : a promise
	 * 
	 */
	function copyNodes(nodes, target) {

		var deferred = $.Deferred();

		var tree = this;
		
		var url = target.getCopyUrl();
		var nodeIds = nodes.all('getResId');
		
		var params = {
			'nodeIds[]' : nodeIds 
		};
		
		//special delivery for pasting iterations to campaigns
		if (target.is(':campaign')){
			params['next-iteration-number'] = (target.getChildren().length + 1);
		}
		
		$.when(tree.open_node(target)).then(function() {
			
			$.post(url, params, 'json')
			.done(function(jsonData) {
				insertCopiedNodes(jsonData, target, tree);
				tree.open_node(target, deferred.resolve);
				if (typeof (refreshStatistics) == "function") {
					refreshStatistics();
				}
			})
			.error(deferred.reject);

			
		});

		return deferred.promise();
	}


	/* *******************************************************************************
							// Library part	
	 ******************************************************************************** */
	
	/* *******************************************************************************
							Plugin definition
	 ******************************************************************************** */	

	return function(){
		

		$.jstree.plugin('workspace_tree', {
			defaults : {
				
			},

			__init : function() {


				var container = this.get_container();

				var self = this;

				container.bind("select_node.jstree", function(event, data) {
					data.rslt.obj.treeNode().deselectChildren();
					return true;
					
				})
				
				/*
				 * This event is triggered after the movement was performed. Some checks regarding the validity of this operation
				 * were not performed, we will perform them now. If the drop operation is invalid we will cancel it now and notify the 
				 * user of the specific reasons.
				 * 
				 * Note : the event 'before.jstree' was too buggy to use so we won't use it.
				 */
				.bind("move_node.jstree", function(event, data) {

					var moveObject = data.args[0];
					
					if (moveObject == null || moveObject == undefined || moveObject.cr == undefined) {
						return; //abort !
					}
					

					var rules = data.inst._getRules();
					var nodes = $(moveObject.o).treeNode();
					var target = $(moveObject.np).treeNode();
					
					
					//case dnd-copy
					if (squashtm.keyEventListener.ctrl) {
						destroyJTreeCopies(moveObject, data.inst);
						nodecopier.pasteNodesFromTree();
						return;
					} 
					
					//case dnd-move
					if (check_name_available(data)) {
						
						warnIfisCrossProjectOperation(target, nodes)
						.done(function(){
							moveNodes(data);
						})
						.fail(function(){
							$.jstree.rollback(data.rlbk);
						});

					} 
					else {
						$.squash.openMessage('', translator.get('squashtm.action.exception.cannotmovenode.label')).done(function() {
							$.jstree.rollback(data.rlbk);
						});
					}
				});
		
			
				
			},

			_fn : {
				
				check_move : check_move,
				
				postNewNode : postNewNode, 
				
				copyNodes : copyNodes,

				refresh_selected : function() {
					var self = this;
					var selected = this.get_selected();
					selected.all('refresh');
				},
				
				_getRules : function(){				
					//this code handle lazy initialisation for permissions-rules. 
					var settings = this._get_settings(); 
					
					if (settings.workspace_tree.rules == undefined){
						settings.workspace_tree.rules = rulesbroker.get();
					}
					
					return settings.workspace_tree.rules;
				},
				
				/*
				 * types : array of values of 'rel' attributes (eg ['folder', 'test-case'])
				 * ids : array of resid 
				 */
				delete_nodes : function(){
					var self = this, nodes = null;

					if ( (arguments.length==2) && (arguments[0] instanceof Array) && (arguments[1] instanceof Array) ){
						var types = arguments[0];
						var ids = arguments[1];
						
						var typeSelector = "";
						var idSelector = "";
						var tlen = types.length, ilen = ids.length;
						for (var i=0;i<tlen;i++){
							typeSelector += "[rel='"+types[i]+"'],";
						}
						for (var i=0;i< ilen;i++){
							idSelector += "[resid='"+ids[i]+"'],";
						};
						
						typeSelector = typeSelector.replace(/,$/,'');
						idSelector = idSelector.replace(/,$/,'');
						
						nodes = this.get_container().find(idSelector).filter(typeSelector);
					}
					else{
						nodes = arguments[0];
					}
					
					nodes.each(function(){
						self.delete_node(this);
					});
					
				},
	
			}

		});
		
	};	
});
