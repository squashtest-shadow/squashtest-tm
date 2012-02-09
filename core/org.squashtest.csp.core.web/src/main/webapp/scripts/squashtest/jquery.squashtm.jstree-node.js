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
 * This file defines several things we want a node to be able to know/perform.
 * Also able to sublass itself to implement nature-specific behaviours (eg, accepts in-droppped
 * nodes, which ones etc).
 *
 * A TreeNode also knows the address of the resource it designate and where to fetch its content.
 *
 *
 */

(function($){
	
	/*
		we first define some closures.
	*/
	var buildGetContent = function(treeNode){
		var root = treeNode.getBaseUrl();
		switch (treeNode.getResType()){
				case "iterations" : 
				case "requirements" : 
				case "test-cases" : return function(){return null;}; break;
				default : return function(){
					var wkspce = treeNode.getWorkspace();
					var dmtpe = treeNode.getDomType();
					var id = treeNode.getResId();
					return this.getBrowserUrl()+'/'+dmtpe+"s/"+id+"/content";
				}
		}	
	}
	
	

	$.fn.treeNode = function(){
	
		//check validity of this call to treeNode();
		var lt = this.length;
		var tree = $.jstree._reference(this);
		var noLi = (! this.is('li') ) && (! this.is('a'));
		
		if ((lt==0) || (tree==null) || (noLi)){
			throw "this node is not a valid tree node";
		}

		this.tree = $.jstree._reference(this);
		this.reference= (this.is('a')) ? this.parent("li") : this;
		
		

		
		// ************* methods for 1-sized jquery object ************** 

		// ************ basic getters
		this.getTree = function(){
			return this.tree;
		}
		
		this.getDomId = function(){
			return this.reference.attr('id');
		}
		
		this.getDomType = function(){
			return this.reference.attr('rel');
		}
		
		this.getResId = function(){
			return this.reference.attr('resid');
		}
		
		this.getResType = function(){
			return this.reference.attr('restype');
		}
		
		this.isEditable = function(){
			return this.reference.attr('editable');
		}
		
		this.getName = function(){
			return this.reference.attr('name');
		}
				
	
		// ************ relationships getters
		this.getLibrary = function(){
			if (this.reference.is(':library')) {
				return this;
			}else{
				var library = this.reference.parents(':library');
				return library.treeNode();
			}
		}
		
		this.getParent = function(){
			return this.reference.parents("li").first().treeNode();
		}
		
		this.getWorkspace = function(){
			return this.getLibrary().getResType().replace('-libraries', '');
		}
		
				
	
		this.getPrevious = function(){
			if (this.is(':library')){
				return this;
			}
			
			var prev = this.reference.prev();
			
			if (prev.length>0){
				return prev.treeNode();
			}else{
				return this.getParent();
			}
		}
		
				
		// ***************** tree actions
		this.deselectChildren = function(){
			this.tree.deselect_all(this);
		}
		
		this.isOpen = function(){
			//isOpen returns true when open, but something not specified when it's not
			//hence the return thing below
			var isOpen = this.tree.is_open(this);
			return (isOpen != true) ? false : true;	
		}
		
		this.open = function(){
			var defer = $.Deferred();
			this.tree.open_node(this, defer.resolve);
			return defer.promise();
		}
		
		this.load = function(){
			var defer = $.Deferred();
			this.tree.load_node ( this , defer.resolve(), defer.reject);
			return defer.promise();
		}
		
		this.close = function(){
			this.tree.close_node(this);
		}
		
		this.appendNode = function(data){
			var defer = $.Deferred();
			var res = this.tree.create_node(this, 'last', data, defer.resolve, true);
			var newNode = res.treeNode();
			return [newNode, defer.promise()];
		}
		

		this.select = function(){
			this.tree.select_node(this);
		}
		
		this.deselect = function(){
			this.tree.deselect_node(this);
		}	

		
		// *********** tests
		this.isBrother = function(otherNode){
			var myParent = this.getParent();
			var itsParent = otherNode.getParent();
			return (myParent.getDomId() === itsParent.getDomId());
		}
		
		this.sameLib = function(otherNode){
			var myLib = this.getLibrary();
			var itsLib = otherNode.getLibrary();
			return (myLib.getDomId() == itsLib.getDomId());
		}
		
		this.isSame = function(otherNode){
			return (this.getDomId() == otherNode.getDomId());
		}
		
		
		
		// ************* methods for multiple matched elements ************

		// one method to rule them all	
		this.all = function(strOrArray){
			return this.collect(function(elt){
				if ( typeof strOrArray == 'string'){
					return $(elt).treeNode()[strOrArray]();
				}else{
					var data={};
					for (var i in strOrArray){
						var func = strOrArray[i];
						var res = $(elt).treeNode()[func]();
						data[func.toLowerCase().replace('get', '')] = res;
					}
					return data;
				}
			});
		}
		
		
		this.areSameLibs = function(){
			var libs = this.collect(function(elt){return $(elt).treeNode().getLibrary().getDomId();});
			return ($.unique(libs).length==1);
		}
		
		this.areAllBrothers = function(){
			var parents = this.collect(function(elt){return $(elt).treeNode().getParent().getDomId();});
			return ($.unique(parents).length==1);
		}
		
		
		// *************** urls 
		
		this.getResourceUrl = function(){
			return this.getBaseUrl()+"/"+this.getResType()+"/"+this.getResId();
		}
		
		this.getBaseUrl = function(){
			return this.tree.data.squash.rootUrl+"/";		
		}
		
		this.getBrowserUrl = function(){
			return this.getBaseUrl()+this.getWorkspace()+"-browser";
		}
		
		this.getContentUrl = buildGetContent(this);

	

		
		return this;
	}
	

})(jQuery);




