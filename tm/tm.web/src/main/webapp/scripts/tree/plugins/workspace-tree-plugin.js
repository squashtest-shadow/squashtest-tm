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


define(['jquery', 'workspace.tree-node-copier', 'jstree'], function($, nodecopier){
	
	
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
			return targetNode.open() // first call will make the node load if
			// necessary.
			.then(createNode);
		} else {
			return createNode();
		}

	}
	

	/*
	 * **************************** check move section ****************************************
	 */

	/*
	 * Will check if a dnd move is legal. Note that this check is preemptive, contrarily to checkMoveIsAuthorized which
	 * needs to post-check.
	 * 
	 * NB : this method is called by the configuration of plugin "crrm" in the initialization object.
	 * 
	 */
	function treeCheckDnd(m) {

		var object = m.o;
		var dest = m.np;
		var src = m.op;

		try {
			var jqSrc = $(src).treeNode();
			var jqDest = $(dest).treeNode();
			var jqObject = $(object).treeNode();

			// check if the node is draggable first
			if (jqObject.is(':library')) {
				return false;
			}

			// check that the destination type is legal
			if (!jqDest.acceptsAsContent(jqObject) || !jqDest.isCreatable()) {
				return false;
			}

			// do not allow move if src is not deletable
			if (!jqSrc.isDeletable() && !squashtm.keyEventListener.ctrl) {
				return false;
			}

			// allow iteration or test suite copy only
			if ((jqObject.is(':resource') || (jqObject.is(':view'))) && !squashtm.keyEventListener.ctrl) {
				return false;
			}

			m.differentLibraries = moveFromDifferentLibraries(m);

		} catch (invalid_node) {
			return false;
		}
		return true;

	}

	/*
	 * This method checks if we can move the object is the dest folder returns true if it's ok to move the object. Note
	 * that contrary to treeCheckDnd(moveObject), that code is called only for "move", not "copy" operations, and thus is
	 * not part of the aforementioned function.
	 * 
	 * A second reasons is that we don't want to forbid the operation a-priori : we cancel it a-posteriori. Thus, the user
	 * will know why the operation could not be performed instead of wondering why the hell he cannot move the bloody
	 * node.
	 */
	function checkMoveIsAuthorized(data) {
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

	/**
	 * This method checks if the move will be inter-project or not.
	 */
	function moveFromDifferentLibraries(data) {
		var dest = data.np;
		var object = data.o;
		var movedFromLibs = object.collect(function(elt) {
			return $(elt).treeNode().getLibrary().attr("id");
		});
		if (movedFromLibs.lenght > 0) {
			return true;
		}
		var destLibrary = $(dest).treeNode().getLibrary().attr("id");
		if (movedFromLibs[0] != destLibrary) {
			return true;
		}
		return false;
	}
	
	/*
	 * 
	 * @param data : the move_node object @param url : the url to post to.
	 */
	function moveNode(data, url) {
		var isRoot = function(node) {
			return node.is(":library") ? 1 : 0;
		};

		var tree = data.inst;
		var nodeData = data.args[0];
		var newParent = nodeData.np;

		// first check if we don't need to perform an
		// operation
		if (nodeData.o.length === 0) {
			return;
		}

		// we also reject iterations.
		var firstNode = nodeData.o[0];
		if ($(firstNode).is(":iteration")) {
			return;
		}

		var dataSent = {
			"object-ids" : $(nodeData.o).treeNode().all('getResId'),
			"destination-id" : nodeData.np.attr('resid'),
			"destination-type" : isRoot(nodeData.np) ? "library" : "folder"
		};

		tree.open_node(newParent);

		return $.ajax({
			type : 'POST',
			url : url,
			data : dataSent,
			dataType : 'json'
		});
	}

	/**
	 * DnD move Method is called after every check id done to allow the operation.
	 */
	function doDnDMoveNodes(moveObject, data) {

		var url = $(moveObject.o).treeNode().getMoveUrl();

		moveNode(data, url).fail(function(jqXHR) {
			try {
				squashtm.notification.handleJsonResponseError(jqXHR).done(function() {
					$.jstree.rollback(data.rlbk);
				});
			} catch (e) {
				$.jstree.rollback(data.rlbk);
			}
		});

	}
	
	/**
	 * DnD copy node method. This method is called after all check is done to allow the operation.
	 */
	function doDnDCopyNodes(moveObject, data) {
		/* we need to destroy the copies first, since we'll use our owns. */
		destroyJTreeCopies(moveObject, data.inst);

		/* now let's post.  Again, as annoying as  it is, the url depends on the nature of the nodes. */
		var jqObjects = $(moveObject.o);
		var url = jqObjects.treeNode().getCopyUrl();
		var newData = moveObjectToCopyData(data);

		copyNode(newData, url).fail(function() {
			data.inst.refresh();
		}).done(function() {
			/* Begin [Feat 1299] this is to make the following cases work :
			  case 1 : one is viewing a campaign and copy-paste one of it's iterations with Ctrl + drag and drop
				=> the statistics of the  still displayed campaign have changed = > we need to refresh them
			   
			  case 2 : one is viewing an iteration and copy-paste one of it's test-suite with Ctrl + drag and drop 
			    => the statistics of the still displayed iteration have changed => we need to refresh them 
			 */
			if (typeof (refreshStatistics) == "function") {
				refreshStatistics();
			}
			// End [Feat 1299]
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

	function moveObjectToCopyData(moveObject) {

		var nodes = $(moveObject.args[0].o).treeNode();
		var target = $(moveObject.args[0].np).treeNode();

		return nodecopier.preparePasteData(nodes, target);

	}

	/*
	 * will erase fake copies in the tree, send the copied node data to the server, and insert the returned nodes.
	 * 
	 * @param data : the data associated to the event move_node.jstree
	 * 
	 * @param url : the url where to send the data.
	 * 
	 * @returns : a promise
	 * 
	 */
	function copyNode(data, url) {

		var deferred = $.Deferred();

		var tree = data.inst;
		var newParent = data.newParent;
		var dataSent = data.sendData;

		$.when(tree.open_node(newParent)).then(function() {

			$.ajax({
				type : 'POST',
				url : url,
				data : dataSent,
				dataType : 'json'
			}).success(function(jsonData) {
				insertCopiedNodes(jsonData, newParent, tree);
				tree.open_node(newParent, deferred.resolve);
			}).error(deferred.reject);
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
				 * the following should have been as a handler of before.jstree on call move_node. however many considerations
				 * lead to postprocess mode_node like now, rather than preprocess it. At least that event is triggered only once.
				 */
				.bind("move_node.jstree", function(event, data) {
							var moveObject = data.args[0];

							if (moveObject !== null && moveObject !== undefined && moveObject.cr !== undefined) {
								if (squashtm.keyEventListener.ctrl) {

									if (data.rslt.differentLibraries) {
										// warn user if move
										// is inter-project
										oneShotConfirm('Info', self._get_settings().workspace_tree.warnCopyToDifferentLibrary,
												squashtm.message.confirm, squashtm.message.cancel).done(function() {
											doDnDCopyNodes(moveObject, data);
										}).fail(function() {
											data.inst.refresh();
										});
									} else {
										doDnDCopyNodes(moveObject, data);
									}
								} else {
									// check if we can move
									// the object
									if (checkMoveIsAuthorized(data)) {

										// warn user if move
										// is inter-project
										if (data.rslt.differentLibraries) {
											oneShotConfirm('Info', self._get_settings().workspace_tree.warnMoveToDifferentLibrary,
													squashtm.message.confirm, squashtm.message.cancel).done(function() {
												doDnDMoveNodes(moveObject, data);
											}).fail(function() {
												$.jstree.rollback(data.rlbk);
											});
										} else {
											doDnDMoveNodes(moveObject, data);
										}

									} else {
										$.squash.openMessage('', self._get_settings().workspace_tree.cannotMoveMessage).done(function() {
											$.jstree.rollback(data.rlbk);
										});
									}
								}
							}
						});
				
			
				
			},

			_fn : {
				
				treeCheckDnd : function(){treeCheckDnd();},
				
				postNewNode : postNewNode, // see below

				refresh_selected : function() {
					var self = this;
					var selected = this.get_selected();
					selected.all('refresh');
				},
	
			}

		});
		
	};	
});
