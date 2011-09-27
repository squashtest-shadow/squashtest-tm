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
import java.util.List;

/**
 * note : the depth starts at 0. See LibraryTree for the rest of the documentation.
 * 
 * 
 * @author bsiri
 *
 * @param <T> 
 */



public abstract class TreeNode<T extends TreeNode<T>> {
	
	private final List<T> children = new ArrayList<T>();
	private  T parent ;
	private LibraryTree<T> tree ;
	
	private int depth;
	private Long key;
	
	
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

	int getDepth(){
		return depth;
	}
	
	void setDepth(int depth){
		this.depth=depth;
	}

	
	List<T> getHierarchy(){
		List<T> result = new ArrayList<T>();
		
		T nodeIterator = (T)this;
		
		while(nodeIterator!=null){
			result.add(nodeIterator);
			nodeIterator=nodeIterator.getParent();
		}
		
		return result;
		
	}
	
	
	void recomputeDepth(){
		depth = getHierarchy().size() - 1;
	}

	
	void setParent(T parent){
		this.parent=parent;
	}

	
	void addChild(T child){
		child.setParent((T)this);
		child.setTree(tree);
		child.setDepth(depth+1);
		children.add(child);
	}
	
	public boolean isSame(T node){
		boolean result;
		
		/*
		if (node == null) return false;
		if ((key==null) && (node.getKey()!=null)) return false;
		if ((key==null) && (node.getKey())==null) return true;
		return key.equals(node.getKey());
		*/
		
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
 