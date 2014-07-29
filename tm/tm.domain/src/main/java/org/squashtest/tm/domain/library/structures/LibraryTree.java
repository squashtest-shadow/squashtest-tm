/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
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
package org.squashtest.tm.domain.library.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;



/*
 * TODO : the current implementation is reaching its limits. Only few cases are implemented for now, but on the long run we may have to 
 * implement one specific solution for each of them, unless we make a more generic solution.
 * 
 * Basically detecting a network of locked nodes means :
 * 
 * 1) build the network of entities that are relevant for the current problem, 
 * 2) mark the first locked node (seeds),
 * 3) propagate to other nodes (the meaning of propagation may vary from one problem to another).
 * 
 * So instead of using things like trees and graphs and tons of variations of these let's think of a generic and configurable class. That will be useful
 * the day we'll need to chain the results of multiple instances to resolve complex dependencies. 
 */


/**
 * <p>
 * This tree can have multiple roots and internally its structure is layered. The details are :
 * <ul>
 *  <li> This is a layered tree : the layer <i>n</i> is the collection of nodes of depth <i>n</i>. Just like nodes, layers can be accessed via the proper methods. </li>
 *  <li> There may be more than one node at layer 0. ie, the root(s), </li>
 *  <li> The parent of a node of layer <i>n</i> belongs to layer <i>n-1</i>, except for layer 0, </li>
 *  <li> The order of two nodes within the same layer is undefined (weak ordering). </li>
 * </ul>
 *  
 * The implementation is simple because its only purpose is to provide a structure to store data in. The structure is the very goal here so its not supposed to be structurally modified or
 * rebalanced : its built once and for all.  
 * 
 * @see TreeNode
 * </p>
 *  
 *  @author bsiri
 */

public class  LibraryTree<T extends TreeNode<T>>{
	
	protected final Map<Integer, List<T>> layers = new HashMap<Integer, List<T>>();
	
	
	/**
	 * Given an integer, returns the layer at the corresponding depth in the tree. That integer must be comprised within the acceptable bounds of that tree, ie 0 and {@link #getDepth()}.
	 * The layer is returned as a list of nodes.
	 * 
	 * @param depth the depth of the layer we want an access to.
	 * @return the layer as a list of nodes.
	 * @throws IndexOutOfBoundsException
	 */
	public List<T> getLayer(Integer depth){
		
		if (depth<0){ throw new IndexOutOfBoundsException("Below lower bound : "+depth);}
		if (depth > Collections.max(layers.keySet())){ throw new IndexOutOfBoundsException("Above upper bound : "+depth);}
		
		return layers.get(depth);		
		
	}
	
	/**
	 * Given a TreeNodePair (see documentation of the inner class for details), will add the child node to the tree. 
	 * If the child node have no parents it will be added to layer 0 (ie, new root). 
	 * Else, the child node will belong to the layer following its parent's layer.
	 * 
	 * @param newPair a TreeNodePair with informations regarding parent and child node included.
	 * @throws NoSuchElementException if a parent node cannot be found.
	 */
	public void addNode(TreeNodePair newPair){
		
		T parent = getNode(newPair.getParentKey());
		T childNode = newPair.getChild();
		
		if (parent==null){
			
			if (layers.get(0)==null){ layers.put(0, new ArrayList<T>());}
			
			childNode.setParent(null);
			childNode.setTree(this);
			childNode.setDepth(0);
			layers.get(0).add(childNode);
		}
		else{
			parent.addChild(childNode);	
			
			int layerIndex =childNode.getDepth();
			if (layers.get(layerIndex)==null){ layers.put(layerIndex, new ArrayList<T>());}
			
			layers.get(layerIndex).add(childNode);
		}		
		
	}
	
	/**
	 * Same than {@link #addNode(TreeNodePair)}, but the TreeNodePair parameter will be built using the parameter provided here.
	 * 
	 * @param parentKey the key designating the parent node.
	 * @param childNode the child we want eventually to insert.
	 */
	public void  addNode(Long parentKey, T childNode){
		addNode(new TreeNodePair(parentKey, childNode));		
	}
	

	
	/**
	 * Accepts a list of TreeNodePair and will add all the nodes in that list (see TreeNodePair and TreeNode). Such list can be called a flat tree and passing one to this method 
	 * is a convenient way for tree initialization. 
	 * You do not need to pass the TreeNodePairs in any order : the method will take care of inserting them in the correct order (ie parents before children). 
	 * 
	 * @see {@link #sortData(List)}, TreeNode, TreeNodePair 
	 * 
	 * @param unsortedData the flat representation of the tree.
	 */
	public void addNodes(List<TreeNodePair> unsortedFlatTree){
		
		//we must ensure first that the data are sorted before inserting them.
		List<TreeNodePair> sortedFlatTree = sortData(unsortedFlatTree);
		
		for (TreeNodePair pair : sortedFlatTree){
			addNode(pair);
		}
	}

	
	/**
	 * 
	 * <p>This method accepts a list of TreeNodePair and returns the sorted version of that list. Sorting means here that nodes will be grouped by layers and the layer will be ordered.</p>
	 * 
	 * <p>
	 * 	For each TreeNodePair <i>d</i> from the input list:
	 * <ul>
	 *  <li>if there are no pair <i>x</i> such as <i>d.parent</i> == <i>x.child.key</i> (<i>d</i>'s parent is not part of the output list), then <i>d</i>.child is inserted first</li>
	 *  <li>if there is a node <i>x</i> such as <i>d</i>.parent == <i>x</i>.child.key (<i>d</i>'s parent is part of the output list) then <i>d</i> is inserted at position(<i>x</i>)+1</li>
	 * </ul>  
	 * <i>x</i> is taken from the output list. Note : the output implements a weak order. ie if two data have the same key their belong to the same layer of the tree, but their precise order within that layer is 
	 *  undefined.
	 *  
	 * </p>
	 * 
	 * @param unsortedData an unsorted list of TreeNodePair
	 * @return the sorted list of TreeNodePair
	 * @see TreeNode, TreeNodePair.
	 */
	protected List<TreeNodePair> sortData(List<TreeNodePair> unsortedData){
		List<TreeNodePair> sortedList = new ArrayList<TreeNodePair>(unsortedData.size());
		
		//the list below will hold lists of all keys (node identifier) we treated. Since those keys are our main comparators it's a good idea to keep a shorthand on them.
		List<Long> insertedNodes = new ArrayList<Long>(unsortedData.size());
		
		for (TreeNodePair data : unsortedData){
			//the following statement is true if the data has a parent in the output list
			Long parentKey = data.parentKey;
			
			int index = (insertedNodes.contains(parentKey)) ? insertedNodes.indexOf(parentKey)+1 : 0;

			insertedNodes.add(index, data.child.getKey());
			sortedList.add(index, data);
			
		}
		
		return sortedList;
		
	}
	
	
	/**
	 * 
	 * Accepts a identifier - aka key - and returns the corresponding node if found.
	 * 
	 * @param key the key identifying a node
	 * @return the node if found
	 * @throws NoSuchElementException if the node was not found.
	 * 
	 */
	public T getNode(Long key){
		
		if (key == null){ return null;}
		
		for (T node : getAllNodes()){
			if (node.getKey().equals(key)) {return node;}		
			
		}	
		
		throw new NoSuchElementException("No element tagged as "+ key.toString() + " found in this tree" );	
	}
	

	/**
	 * 
	 * <p>Accepts a {@link Closure} that will be applied on the nodes using bottom-up exploration. The method will walk up the tree :
	 * <ul> 
	 *  <li>layer <i>n+1</i> will be treated before layer <i>n</i> (reverse order)</li>
	 *  <li>nodes within a given layer will be treated regardless their ordering</li>
	 * </ul> 
	 * </p>
	 * @param closure code to apply on the nodes.
	 */
	public void doBottomUp(Closure closure){
		Integer layerIndex = Collections.max(layers.keySet());

		while (layerIndex >=0){
			List<T> layer = layers.get(layerIndex);
			CollectionUtils.forAllDo(layer, closure);			
			layerIndex--;
		}
	}
	
	

	/**
	 * <p>
	 * Accepts a {@link Closure} that will be applied on the nodes using top-down exploration. The method will walk down the tree : 
	 * <ul>
	 * 	<li>the layer <i>n</i> will be treated before layer <i>n+1</i> (natural order)</li>
	 *  <li>nodes within a given layer will be treated regardless their ordering</li>
	 * </ul>
	 * </p>
	 * @param closure code to apply on the nodes.
	 */
	public void doTopDown(Closure closure){
		Integer layerIndex = 0;

		while (layerIndex <= Collections.max(layers.keySet())){
			List<T> layer = layers.get(layerIndex);
			CollectionUtils.forAllDo(layer, closure);			
			layerIndex++;
		}		
	}
	
	
	
	/**
	 * <p>
	 * Accepts a list of TreeNodes and use their data to update existing nodes data. The TreeNodes of the input list are merely carrying informations : the key property will identify 
	 * actual nodes in the tree and the rest of their data will be used to update the found nodes. 
	 * </p>
	 * <p>The particulars of how data will be merged depends on how the TreeNodes implement {@link TreeNode#updateWith(TreeNode)}.</p>
	 * 
	 * @throws NoSuchElementException if one of the node was not found.
	 */
	public void merge(List<T> mergeData){
	
		for (T data : mergeData){
			T node = getNode(data.getKey());
			node.updateWith(data);
		}
		
	}
	
	/**
	 * <p>
	 * That method will gather arbitrary informations on every single nodes and return the list of the gathered informations. What will be gathered and how it is done is defined in the 
	 * {@link Transformer} parameter. The tree will be processed top-down, ie, walked down (see {@link #doTopDown(Closure)}).
	 * </p>
	 * 
	 * @param <X> the type of the data returned by the transformer.
	 * @param transformer the code to be applied over all the nodes.
	 * @return the list of the gathered data.
	 */	
	@SuppressWarnings("unchecked")
	public <X> List<X> collect(Transformer transformer){
		
		return new ArrayList<X>(CollectionUtils.collect(getAllNodes(), transformer));	
				
	}
	
	/**
	 * <p>short hand for {@link #collect(Transformer)} with a Transformer returning the data.key for each nodes.</p>
	 * 
	 * @return the list of the node keys.
	 */
	public List<Long> collectKeys(){
		return collect(new Transformer() {		
			@Override
			public Object transform(Object input) {
				return ((T)input).getKey();
			}
		});
		
	}
	

	/**
	 * @return all the nodes.
	 */
	public List<T> getAllNodes(){
		List<T> result = new ArrayList<T>();
		for (List<T> layer : layers.values()){
			result.addAll(layer);
		}
		return result;
	}

	
	/**
	 * return the depth of the tree, ie how many layers does the tree count.
	 * @return the depth.
	 */
	public int getDepth(){
		return Collections.max(layers.keySet())+1;
	}
	
	
	/* ******************************** scaffolding stuffs ******************************* */
	

	protected T createNewNode(T parent, int depth, T newNode){
		newNode.setParent(parent);
		newNode.setDepth(depth);
		newNode.setTree(this);
		return newNode;
	}
	

	
	/**
	 * A TreeNodePair is a scaffolding class which is mainly used when initializing a tree. It simply pairs a child treeNode with the key of its parent. A child node having a null parent
	 * will be considered as a root node.
	 * 
	 * @author bsiri
	 *
	 */
	public class TreeNodePair{
		private Long parentKey;
		private T child;
		
		public Long getParentKey() {
			return parentKey;
		}
		public void setParentKey(Long parentKey) {
			this.parentKey = parentKey;
		}
		public T getChild() {
			return child;
		}
		public void setChild(T child) {
			this.child = child;
		}
		
		public TreeNodePair(){
			
		}
		
		public TreeNodePair(Long parentKey, T child){
			this.parentKey = parentKey;
			this.child = child;
		}
		
		
	}

	
	/**
	 * Returns a new instance of a TreeNodePair. Basically the same thing than calling TreeNodePair constructors, that method exists mainly for semantic reasons (it guarantees that the 
	 * returned TreeNodePair instance is compatible with the tree (regarding generic types). 
	 * 
	 * @return a new instance of a TreeNodePair.
	 */
	public TreeNodePair newPair(){
		return new TreeNodePair();
	}
	
	
	/**
	 * An initializing version of {@link #newPair()}.
	 * 
	 * @param parentKey the identifier of the parent node. 
	 * @param child the child node.
	 * @return an initialized instance of TreeNodePair.
	 */
	public TreeNodePair newPair(Long parentKey, T child){
		return new TreeNodePair(parentKey, child);
	}
	
}