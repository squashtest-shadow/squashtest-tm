/**
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
package org.squashtest.csp.tm.domain.library.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;


/**
 * That tree has the following specifics : 
 * 
 *  - This is a layered tree. The layer n is the collection of nodes of depth n. So you can ask for all the nodes
 * of a particular layer.
 *  - There may be more than one node at layer 0. ie, the root(s). 
 *  - A node in layer n has a father in layer n-1, except for layer 0
 *  - The order of two nodes within the same layer is undefined. So that tree implements a weak order
 *  
 *  Note that in this simple implementation only the addition of nodes is allowed.
 */

public class  LibraryTree<T extends TreeNode<T>>{
	
	protected final Map<Integer, List<T>> layers = new HashMap<Integer, List<T>>();
	
	public List<T> getLayer(Integer depth){
		
		if (depth<0) throw new IndexOutOfBoundsException("Below lower bound : "+depth);
		if (depth > Collections.max(layers.keySet())) throw new IndexOutOfBoundsException("Above upper bound : "+depth);

		
		return layers.get(depth);		
		
	}
	

	public void addNode(TreeNodePair newPair){
		
		T parent = getNode(newPair.getParentKey());
		T childNode = newPair.getChild();
		
		if (parent==null){
			
			if (layers.get(0)==null) layers.put(0, new ArrayList<T>());
			
			childNode.setParent(null);
			childNode.setTree(this);
			childNode.setDepth(0);
			layers.get(0).add(childNode);
		}
		else{
			parent.addChild(childNode);	
			
			int layerIndex =childNode.getDepth();
			if (layers.get(layerIndex)==null) layers.put(layerIndex, new ArrayList<T>());
			
			layers.get(layerIndex).add(childNode);
		}		
		
	}
	
	public void  addNode(Long parentKey, T childNode){
		addNode(new TreeNodePair(parentKey, childNode));		
	}
	

	
	/**
	 * the parameter is a list of TreeNodePair, that associate a child node with the key of its parent. 
	 * The list may be unsorted, since that method will sort them first. Unsorted means that you doesn't 
	 * have to take care of the node declaration order.
	 * 
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
	 * Sorting algorithms that will sort the data such as data for child nodes will be inserted after the data for the parent node.
	 * 
	 * The TreeNodePair pairs the key of the parent node with all the data of the child node. 
	 * 
	 * we do not sort by comparing pairs of nodes, we sort them such as (looping over TreeNodePair d) :
	 *  - if there are no pair x such as d.parent == x.child.key (d's parent is not part of the output list), then d.child is inserted first.
	 *  - if there is a node x such as d.parent == x.child.key (d's parent is part of the output list) then d is inserted at position(x)+1
	 *  
	 *  x is taken from the output list.
	 *  
	 *  Note : the output implements a weak order. ie if two data have the same key their belong to the same layer of the tree, but their precise order within that layer is 
	 *  undefined.
	 * 
	 * @param unsortedData
	 * @return
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
	
	
	
	public T getNode(Long key){
		
		if (key == null) return null;
		
		for (T node : getAllNodes()){
			if (node.getKey().equals(key)) return node;			
			
		}	
		
		throw new NoSuchElementException("No element tagged as "+ key.toString() + " found in this tree" );	
	}
	

	
	
	/**
	 * that method will apply a closure on all nodes, with the following rules ;
	 * - layer n+1 will be treated before layer n
	 * - all nodes within a given layer will be applied the closure regardless their ordering. 
	 * 
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
	
	

	public void doTopDown(Closure closure){
		Integer layerIndex = 0;

		while (layerIndex <= Collections.max(layers.keySet())){
			List<T> layer = layers.get(layerIndex);
			CollectionUtils.forAllDo(layer, closure);			
			layerIndex++;
		}		
	}
	
	
	
	/**
	 * for each node, if its key one of the provided data, the node will be updated with those new data.
	 */
	public  void merge(List<T> mergeData){
	
		for (T data : mergeData){
			T node = getNode(data.getKey());
			node.updateWith(data);
		}
		
	}
	
	/**
	 * That method will return a weakly-sorted list (by depth) of the result of a closure applied on each nodes.
	 * 
	 * @param <X>
	 * @param transformer
	 * @return
	 */
	
	@SuppressWarnings("unchecked")
	public <X> List<X> collect(Transformer transformer){
		
		List<T> result = new LinkedList<T>();
		
		//doing so theoretically ensure that the nodes are sorted by depth
		for (List<T> layer : layers.values()) result.addAll(layer);
		
		return new ArrayList<X>(CollectionUtils.collect(getAllNodes(), transformer));	
				
	}
	
	/**
	 * short hand for #collect with a Transformer returning the data.key for each nodes.
	 * 
	 * @return just what I said.
	 */
	public List<Long> collectKeys(){
		return collect(new Transformer() {		
			@Override
			public Object transform(Object input) {
				return ((T)input).getKey();
			}
		});
		
	}
	

	
	public List<T> getAllNodes(){
		List<T> result = new ArrayList<T>();
		for (List<T> layer : layers.values()){
			result.addAll(layer);
		}
		return result;
	}
	
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

	
	//factories
	public TreeNodePair newPair(){
		return new TreeNodePair();
	}
	
	public TreeNodePair newPair(Long parentKey, T child){
		return new TreeNodePair(parentKey, child);
	}
	
}