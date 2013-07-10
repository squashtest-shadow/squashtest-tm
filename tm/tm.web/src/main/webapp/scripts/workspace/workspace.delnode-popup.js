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
 * This is an abstract popup, that is usually instancied only once per workspace.  
 * Must be supplied the following options :
 * 
 * a tree, 
 * a permissions-rules, 
 * an implementation of getSimulXhr (using option extender from formDialog),
 * an implementation of getConfirmXhr (using option extender from formDialog)
 * 
 * The rest can also be overriden using the 'extender' parameter.
 * 
 * 
 */
define(['jquery', 'underscore', 'jquery.squash.formdialog'], function($, _){

	
	if (($.squash !== undefined) && ($.squash.delnodeDialog !== undefined)) {
		// plugin already loaded
		return;
	}
	
	$.widget("squash.delnodeDialog", $.squash.formDialog, {

		// ********** these options are MANDATORY **************
		options : {
			tree : null,
			rules : null
		},
		
		// ******************* needs override *******************
		getSimulXhr : function(nodes){
			throw "must be overriden, must return a xhr or an array of xhr"
		},

		getConfirmXhr : function(nodes){
			throw "must be overriden, must return a xhr or an array of xhr";
		},
		
		
		// ******************** deletion simulation *************
		
	
		/* because $.when(deferred(s)).done(something) is supplied with inconsistent arguments
		 * given the number of deferred in the .when() clause, we must force the use of an array argument
		 * so that the fillDetails callback won't have to discriminate.
		 */ 
		fillDetails : function(responsesArray){
			
			var htmlDetail = '';
			
			$.each(responsesArray, function(idx, arg){
				if (!!arg && !!arg[0]){
					htmlDetail += arg[0].message;
				}
			});
			
			if (htmlDetail.length > 0){
				this.element.find('delete-node-dialog-details').removeClass('not-displayed').html(htmlDetail);
			}
			else{
				this.element.find('delete-node-dialog-details').addClass('not-displayed');
			}		
			
			this.showContent('confirm');					
		},
		

		
		simulateDeletion : function(){
			var self = this;
			var tree = this.options.tree;
			var rules = this.options.rules;
			
			//first, check that the operation is allowed.
			this.showContent("pleasewait");
			
			var nodes = tree.jstree('get_selected');
			this.uiDialog.data('selected-nodes', nodes);
			
			if (! rules.canDelete(nodes)){
				this.showContent('rejected');
				return;
			}
			
			//else we can proceed.
			var xhrs = this.getSimulXhr(nodes) 
			
			/* because $.when(deferred(s)).done(something) is supplied with inconsistent arguments
			 * given the number of deferred in the .when() clause, we must force the use of an array argument
			 * so that the fillDetails callback won't have to discriminate.
			 */ 	
			$.when.apply ($, xhrs )	
			.done(function(){
				var clbkargs = (_.isArray(xhrs) && xhrs.length>1) ? arguments : [arguments];
				self.fillDetails.call(self, clbkargs);
			})
			.fail(function(){
				self.showContent('reject');
			});
		},
		
		// ********************** actual deletion*********************
		
		deletionSuccess : function(responsesArray){
			console.log(responsesArray)
			
			//TODO
		},
		
		_findPrevNode : function(nodes){
			var tree = this.options.tree;
			var oknode= tree.find(':library').filter(':first');
			
			if (nodes.length==0) return oknode;
			var ids = nodes.all('getResId');
			var loopnode = nodes.first().treeNode().getAncestors();
			
			loopnode.each(function(){
				var $this = $(this), $thisid = $this.attr('resid');
				if ($this.is(':library') || $.inArray($thisid, ids)== -1){
					oknode = $this.treeNode();
					return false;	//means 'beak' in .each
				}
			});
			
			return oknode;
			
		},
		
		performDeletion : function(){
			var self=this;
			var tree = this.options.tree;
			var nodes = this.uiDialog.data('selected-nodes');		
			var newSelection = this._findPrevNode(nodes);
			
			nodes.all('deselect');
			newSelection.select();
			
			this.showContent('pleasewait');
			
			/* because $.when(deferred(s)).done(something) is supplied with inconsistent arguments
			 * given the number of deferred in the .when() clause, we must force the use of an array argument
			 * so that the fillDetails callback won't have to discriminate.
			 */ 	
			var xhrs = this.getConfirmXhr(nodes);
			$.when.apply($, xhrs)
			.done(function(){
				var clbkargs = (_.isArray(xhrs) && xhrs.length>1) ? arguments : [arguments];
				self.deletionSuccess.call(self, clbkargs);			
			});
			
		}
		
	});


});