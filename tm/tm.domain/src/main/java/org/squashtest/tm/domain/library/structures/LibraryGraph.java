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
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;


/**
 * 
 * 
 * @author bsiri
 *
 * @param acts like a primary key. It should be immutable and should be sufficient to identify a node. String, Long, ou autre sont de bons exemples.
 * @param <T>
 */
public class LibraryGraph<IDENT, T extends GraphNode<IDENT, T>> {

	private Set<T> nodes = new HashSet<T>();
	
	
	public Collection<T> getNodes(){
		return nodes;
	}
	
	public void addNode(T node){
		if (node != null && node.getKey() != null){
			createIfNotExists(node);
		}
	}
	
	/**
	 * will create either the parent or the child if they didn't exist already
	 * 
	 * @param parentData
	 * @param childData
	 */
	
	
	public void addEdge(T parentData, T childData){
		
		T parent = null;
		T child = null;
		
		if ( (parentData !=null) && (parentData.getKey()!=null)){
			parent=createIfNotExists(parentData);
		}
		
		if (childData!=null){
			child = createIfNotExists(childData);
		}
		
		if (parent!=null && ! parent.getChildren().contains(child)){
			parent.addChild(child);
		}
		
		if (child!=null && ! child.getParents().contains(parent)){
			child.addParent(parent);
		}
		
	}
		
	
	public T getNode(IDENT key){
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
		
		if (! nodes.contains(data)){
			nodes.add(data);
		}
		
		return getNode(data.getKey());

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
	
	
	// also known as an "Edge" of the graph. Sorry for the silly name.
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
	
	
	
	// ********* Simple class in which a node is solely represented by its key. The key is still whatever you need. **********
	
	public static final class SimpleNode<T> extends GraphNode<T, SimpleNode<T>>{

		public SimpleNode() {
			super();
		}

		public SimpleNode(T key) {
			super(key);
		}
		
	}
	
}
