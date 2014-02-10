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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

public class LibraryGraph<T extends GraphNode<T>> {

	private List<T> nodes = new ArrayList<T>();
	
	
	public List<T> getNodes(){
		return nodes;
	}
	
	public void setNodes( List<T> nodes){
		this.nodes = nodes;
	}
	

	
	
	/**
	 * will create either the parent or the child if they didn't exist already
	 * 
	 * @param parentData
	 * @param childData
	 */
	
	
	public void addNodes(T parentData, T childData){
		
		T parent = null;
		T child = null;
		
		if ( (parentData !=null) && (parentData.getKey()!=null)){
			parent=createIfNotExists(parentData);
		}
		
		if (childData!=null){
			child = createIfNotExists(childData);
		}
		
		if (parent!=null){
			parent.addChild(child);
		}
		
		if (child!=null){
			child.addParent(parent);
		}
		
		
		
	}
	
	
	public T getNode(Long key){
		 T toReturn = null;
		if (key!=null){
		
			for (T node : nodes){
				if (node.getKey().equals(key)){
					toReturn =  node;
				}
			}
		}
		return toReturn;
	}
	
	private T createIfNotExists(T data){
		T node = getNode(data.getKey());
		
		if (node==null){
			node = data;
			nodes.add(node);
		}
		
		return node;
	}
	
	
	
	public List<T> getOrphans(){
		List<T> copy = new LinkedList<T>(getNodes());
		
		CollectionUtils.filter(copy, new Predicate() {			
			@Override
			public boolean evaluate(Object object) {
				return ((T)object).getParents().isEmpty();
			}
		});
		
		return copy;
	}
	
	
	public List<T> getChildless(){
		List<T> copy = new LinkedList<T>(getNodes());
		
		CollectionUtils.filter(copy, new Predicate() {			
			@Override
			public boolean evaluate(Object object) {
				return ((T)object).getChildren().isEmpty();
			}
		});
		
		return copy;
	}	
	
	
	public <X> List<X> collect(Transformer transformer){
		return new ArrayList<X> (CollectionUtils.collect(getNodes(), transformer));
		
	}
	
	public List<T> filter(Predicate predicate){
		List<T> result = new ArrayList<T>(getNodes());
		
		CollectionUtils.filter(result, predicate);
		return result;
	}
	
	
	/**
	 * first we'll filter, then we'll collect. So write your predicate and transformer carefully.
	 * 
	 */
	public <X> List<X> filterAndcollect(Predicate predicate, Transformer transformer){
		List<T> filtered = filter(predicate);
		
		return new ArrayList<X> (CollectionUtils.collect(filtered, transformer));
	}
	
	
	public class GraphNodePair{
		private T parent;
		private T child;
		
		public T getParent() {
			return parent;
		}
		
		public void setParent(T parent) {
			this.parent = parent;
		}
		
		public T getChild() {
			return child;
		}
		
		public void setChild(T child) {
			this.child = child;
		}
		
		public GraphNodePair(){
			
		}
		
		public GraphNodePair(T parent, T child){
			this.parent=parent;
			this.child=child;
		}
	
	}
	
	public GraphNodePair newPair(){
		return new GraphNodePair();
	}
	
	public GraphNodePair newPair(T parent, T child){
		return new GraphNodePair(parent, child);
	}
	
	
}
