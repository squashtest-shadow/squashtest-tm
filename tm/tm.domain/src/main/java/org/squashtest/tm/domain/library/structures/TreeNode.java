/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.library.structures;


import java.util.ArrayList;
import java.util.List;

/**
 * 
 * <p>Please read also {@link LibraryTree}.</p>
 * 
 * <p>
 *  TreeNode is the type of node used by a LibraryTree. A TreeNode maintains informations regarding its position in the tree, ie its parent node, its layer/depth etc. Each node is identified
 *  by a key, that will be used to identify each node uniquely. While that class does not override equals/hashCode using the key as the sole criteria, it proposes instead a method
 *  ({@link #isSame(TreeNode)} for that.
 * </p>
 * 
 * <p>
 *  Subclasses of TreeNode should be genericized <i>Enum</i>-style, i.e. generics of themselves, and implement {@link #updateWith(TreeNode)}.
 * </p>
 * 
 * 
 * @author bsiri
 *
 * @param <T> the type of the actual subclass.
 */

public abstract class TreeNode<T extends TreeNode<T>> {
	
	private final List<T> children = new ArrayList<T>();
	private  T parent ;
	private LibraryTree<T> tree ;
	
	private int depth;
	private Long key;
	
	
	/**
	 * 
	 * @return the children nodes of this node.
	 */
	public List<T> getChildren(){
		return children ;
	}
	
	
	public TreeNode(){
		
	}
	
	public TreeNode(Long key){
		this.key=key;
	}

	
	LibraryTree<T> getTree(){
		return tree;
	}
	
	
	void setTree(LibraryTree<T> tree){
		this.tree= tree;
	}
	
	
	T getParent(){
		return parent;
	}
	
	
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
	
	/**
	 * 
	 * @return the depth of that node, i.e. the layer depth it belongs to.
	 */
	int getDepth(){
		return depth;
	}
	
	
	void setDepth(int depth){
		this.depth=depth;
	}

	/**
	 * @return the list of the ancestors of this node, from its parent to the ancestor root node (they come in reverse order). 
	 */
	List<T> getHierarchy(){
		List<T> result = new ArrayList<T>();
		
		T nodeIterator = (T)this;
		
		while(nodeIterator!=null){
			result.add(nodeIterator);
			nodeIterator=nodeIterator.getParent();
		}
		
		return result;
		
	}
	
	
	/**
	 * Resets and recomputes the depth of the node from scratch.
	 */
	void recomputeDepth(){
		depth = getHierarchy().size() - 1;
	}

	
	void setParent(T parent){
		this.parent=parent;
	}

	/**
	 * Adds a child to this node and wire their properties accordingly.	
	 * 
	 * @param child the new child.
	 */
	void addChild(T child){
		child.setParent((T)this);
		child.setTree(tree);
		child.setDepth(depth+1);
		children.add(child);
	}
	
	
	/**
	 * <p>
	 * That method tells if two nodes refer to the same node. Two nodes are considered as same if their keys are equal regardless of their
	 * other properties. A node compared to null is considered different. Two node having null keys are considered as same, thought that feature is
	 * not used in theory.
	 * </p>
	 * 
	 * @param node
	 * @return
	 */
	public boolean isSame(T node){
		boolean result;
		
		if (node==null){
			result=false;
		}
		else if (key==null){
			result = (node.getKey()==null);
		}
		else{
			result = key.equals(node.getKey());
		}
		
		return result;
	}
	
	
	/**
	 * strongly recommended to override/specialize that method when you subtype that class.
	 * @param newData
	 */
	protected abstract void updateWith(T newData);

}
 