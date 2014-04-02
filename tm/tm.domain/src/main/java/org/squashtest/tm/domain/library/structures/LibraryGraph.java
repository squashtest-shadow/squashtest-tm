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
		
		if (parent!=null && ! parent.getOutbounds().contains(child)){
			parent.addOutbound(child);
		}
		
		if (child!=null && ! child.getInbounds().contains(parent)){
			child.addInbound(parent);
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
	
	private T createIfNotExists(T node){
		
		if (! nodes.contains(node)){
			nodes.add(node);
		}
		
		return getNode(node.getKey());

	}
	
	
	
	public List<T> getOrphans(){
		List<T> copy = new LinkedList<T>(getNodes());
		
		CollectionUtils.filter(copy, new Predicate() {			
			@Override
			public boolean evaluate(Object object) {
				return ((T)object).getInbounds().isEmpty();
			}
		});
		
		return copy;
	}
	
	
	public List<T> getChildless(){
		List<T> copy = new LinkedList<T>(getNodes());
		
		CollectionUtils.filter(copy, new Predicate() {			
			@Override
			public boolean evaluate(Object object) {
				return ((T)object).getOutbounds().isEmpty();
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
	

	/** 
	 * <p> Will merge the structure of a graph into this graph. This means that nodes will be created if no equivalent exist already, 
	 * same goes for inbound/outbound edges . You must provide an implementation of {@link NodeTransformer}
	 * in order to allow the conversion of a node from the other graph into a node acceptable for this graph.</p>
	 * <p> The merge Nodes and edges inserted that way will not erase existing data provided if nodes having same keys are already present.</p> 
	 * 
	 * <p>The generics are the following : 
	 * 	<ul>
	 * 		<li>OIDENT : the class of the key of the other graph</li>
	 * 		<li>ON : the type definition of a node from the other graph</li>
	 * 		<li>OG : the type of the other graph </li>
	 * 	</ul>
	 * </p>
	 * 
	 * @param othergraph
	 */
	public 
	<OIDENT, 	ON extends GraphNode<OIDENT, ON>, 	OG extends LibraryGraph<OIDENT, ON>> 
	void mergeGraph(OG othergraph, NodeTransformer<ON,T> transformer){
		
		LinkedList<ON> processing = new LinkedList<ON>(othergraph.getOrphans());
		
		Set<ON> processed = new HashSet<ON>();
		
		while (! processing.isEmpty()){
			
			ON current = processing.pop();
			T newParent = transformer.createFrom(current);
			
			for (ON child : current.getOutbounds()){

				addEdge(newParent, transformer.createFrom(child));
				
				if (! processed.contains(child)){
					processing.add(child);
					processed.add(child);
				}
			}
			
			// in case the node had no children it might be useful to add itself again
			addNode(newParent);
		}
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
	
	public static interface NodeTransformer<FORMER, NEW>{
		
		NEW createFrom(FORMER node);
		
	}
	
}
