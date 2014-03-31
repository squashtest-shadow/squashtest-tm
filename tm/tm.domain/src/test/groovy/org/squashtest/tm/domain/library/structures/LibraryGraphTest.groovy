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
package org.squashtest.tm.domain.library.structures

import org.squashtest.tm.domain.library.NodeReference;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;

import spock.lang.Specification


class LibraryGraphTest extends Specification {

	
	def "should build a graph using caller/called details"(){
		
		given :
			def layer0 = [ [ null, 1l ] ]
			def layer1 = [ [ 1l, 11l ], [ 1l, 12l ]  ]
			def layer2 = [
							[ 11l, 21l ], [ 11l,  22l ], [ 11l, 23l ],
							[ 12l, 22l ], [ 12l,  23l ], [ 12l, 24l ],
							[ 23l, 24l ],
							[ null, 25l ]
						 ]
			
			def allData = layer0 + layer1 + layer2
		
		and :
			LibraryGraph graph = new LibraryGraph()
			allData.each{ graph.addEdge(node(it[0]), node(it[1])) }
			
		when :
			
			Collection nodes = graph.getNodes()
		
		then :
			
			nodes.size() == 8
			
			def node1 = graph.getNode(ref(1l))
			def node11 = graph.getNode(ref(11l))
			def node12 = graph.getNode(ref(12l))
			def node21 = graph.getNode(ref(21l))
			def node22 = graph.getNode(ref(22l))
			def node23 = graph.getNode(ref(23l))
			def node24 = graph.getNode(ref(24l))
			def node25 = graph.getNode(ref(25l))
			
			
			node1.parents.size() == 0
			node1.children == [node11, node12] as Set
			
			node11.parents == [node1] as Set
			node11.children == [node21, node22, node23] as Set
				
			node12.parents == [node1] as Set
			node12.children == [node22, node23, node24] as Set
		
			node21.parents == [node11] as Set
			node21.children == [] as Set
			
			node22.parents== [node11, node12] as Set
			node22.children == [] as Set
			
			node23.parents== [node11, node12] as Set
			node23.children == [node24] as Set
			
			node24.parents== [node12, node23] as Set
			node24.children == [] as Set
			
			node25.parents.size() == 0
			node25.children == [] as Set
			
		
	}
	
	
	NodeReference ref(id){
		return new NodeReference(id, id?.toString(), false);
	}
	
	SimpleNode node(id){
		return (id != null) ? new SimpleNode(ref(id)) : null;
	}
	
	
}
