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
package org.squashtest.tm.service.internal.batchimport;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.GraphNode;
import org.squashtest.tm.domain.library.structures.LibraryGraph;

class TestCaseCallGraph extends LibraryGraph<TestCaseTarget, TestCaseCallGraph.Node> {
	
	
	void addGraph(LibraryGraph<NamedReference, SimpleNode<NamedReference>> othergraph){
		
		mergeGraph(othergraph, new NodeTransformer<SimpleNode<NamedReference>, Node>() {
			@Override
			public Node createFrom(SimpleNode<NamedReference> node) {
				return new Node(new TestCaseTarget(node.getKey().getName()));
			}
		});	
		
	}
	
	public boolean knowsNode(TestCaseTarget target){
		return getNodes().contains(target);
	}
	
	public void addEdge(TestCaseTarget parent, TestCaseTarget child){
		addEdge(new Node(parent), new Node(child));
	}
	
	public void addNode(TestCaseTarget target){
		addNode(new Node(target));
	}
	
	
	@Override
	public void addEdge(Node src, Node dest) {
		if (! wouldCreateCycle(src.getKey(), dest.getKey())){
			super.addEdge(src, dest);
		}
		else{
			throw new IllegalArgumentException("cannot add to test case call graph an edge from '"+src.getKey().getPath()+"' to '"+dest.getKey().getPath()+"' : would create a cycle");
		}
	}
	
	/**
	 * says if the given target is called
	 * 
	 * @param target
	 * @return
	 */
	boolean isCalled(TestCaseTarget target){
		Node n = getNode(target);
		if (n!=null){
			return n.getInbounds().isEmpty() == false;
		}
		else{
			return false;
		}
	}
	
	
	/**
	 * says whether that new edge would create a cycle in the graph.
	 * 
	 * Namely, if the src node of the edge is already transitively called
	 * by the dest node. In other words, is dest node an ancestor of src node ?
	 * 
	 * @return
	 */
	boolean wouldCreateCycle(TestCaseTarget src, TestCaseTarget dest){

		boolean createsCycle = false;

		// quick check : if one of either node doesn't exist it's always ok
		if (getNode(dest) == null || getNode(src) == null){
			createsCycle = false;
		}
		
		// quick check : exclude self calls
		else if (src.equals(dest)){
			createsCycle = true;
		}
		
		
		else{
			// else we walk down the call tree of the dest
			
			// we keep track of processed nodes. It has the benefit of preventing multiple exploration of the same node.
			// it also breaks infinite loop but this method exists precisely to prevent this to happen.
			Set<Node> processed = new HashSet<Node>();	
			LinkedList<Node> nodes = new LinkedList<Node>();
			
			Node orig = getNode(dest);
			processed.add(orig);
			nodes.add(orig);
			
			do{
				Node current = nodes.pop();
				if (current.calls(src)){
					createsCycle = true;
					break;
				}
				else{
					for (Node child : current.getOutbounds()){
						if (! processed.contains(child)){
							nodes.add(child);
							processed.add(child);
						}
					}
					
				}
				
			}while(! nodes.isEmpty());		
		}
		
		return createsCycle;
		
	}
	
	
	void removeNode(TestCaseTarget target){
		Node n = getNode(target);
		if (n != null){
			n.getInbounds().remove(n);
			n.getOutbounds().remove(n);
			getNodes().remove(n);
		}
	}
	
	static final class Node extends GraphNode<TestCaseTarget, Node>{
		Node(TestCaseTarget target){
			super(target);
		}
		
		Node(SimpleNode<NamedReference> othernode){
			super(new TestCaseTarget(othernode.getKey().getName()));
		}
		
		boolean isMe(TestCaseTarget target){
			return target.equals(key);
		}
		
		boolean calls(TestCaseTarget callee){
			for (Node n : outbounds){
				if (n.isMe(callee)){
					return true;
				}
			}
			return false;
		}
	}
}

