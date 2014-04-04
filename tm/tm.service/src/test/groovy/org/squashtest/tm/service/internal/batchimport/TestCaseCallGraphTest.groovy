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
package org.squashtest.tm.service.internal.batchimport

import spock.lang.Specification

import org.squashtest.tm.exception.CyclicStepCallException;
import org.squashtest.tm.service.internal.batchimport.TestCaseCallGraph.Node

class TestCaseCallGraphTest extends Specification {

	TestCaseCallGraph graph
	
	def setup(){
		graph = createGraph();
	}
	
	
	def "should tell that this node is not called by anyone (because it's a root node)"(){
		
		expect :
			graph.isCalled(grandpa) == false // no one ever calls grandpa :-(
		
		where :
			grandpa = target("/family/grandpa")
	}
	
	def "should tell that this node is not called by anyone (because it doesn't belong to the graph)"(){
		
		expect : 
			graph.isCalled(robert) == false
			
		where : 
			robert = target("/otherfamily/robert")
		
	}
	
	def "should tell that this node is being called by at least one other node"(){
		
		
		expect :
			graph.isCalled(kenny) == true
			
		where :
			kenny = target("/family/kenny")
		
	}
	
	
	def "should tell that this new edge wouldn't create a cycle"(){
		
		expect : 
			graph.wouldCreateCycle(grandpa, grandma) == false
			
		where :
			grandpa = target("/family/grandpa")
			grandma = target("/family/grandma")
		
	}
	
	def "should tell that this new edge wouldn't create a cycle (2)"(){
		
		expect :
			graph.wouldCreateCycle(grandpa, carole) == false
			
		where :
			grandpa = target("/family/grandpa")
			carole = target("/family/carole")
		
	}
	
	def "should tell that this new edge wouldn't create a cycle (unknown caller)"(){
		
		expect :
			graph.wouldCreateCycle(bob, carole) == false
			
		where :
			bob = target("/family/bob")
			carole = target("/family/carole")
		
	}
	
	
	def "should tell that this new edge wouldn't create a cycle (unknown callee)"(){
		
		expect :
			graph.wouldCreateCycle(ziggy, mike) == false
			
		where :
			ziggy = target("/family/ziggy")
			mike = target("/family/mike")
		
	}
	
	
	
	def "should tell that this new edge will create an edge"(){
		
		expect :
			graph.wouldCreateCycle(ziggy, grandma)
			
		where :
			ziggy = target("/family/ziggy")
			grandma = target("/family/grandma")
		
	}
	
	def "should tell that this new edge will create an edge (2)"(){
		
		expect :
			graph.wouldCreateCycle(ziggy, ziggy)
			
		where :
			ziggy = target("/family/ziggy")
		
	}
	
	
	def "should rant because a new edge would create a cycle"(){
		
		when :
			graph.addEdge(target("/family/ziggy"), target("/family/grandma"))
			
		then :
			thrown CyclicStepCallException
	}
	
	def "should disconnect a node (removing all connected edges)"(){
		
		given :
			def targetCharlie = target("/family/charlie")
			
			def charlieConnected = { node ->
				node.inbounds.contains ( graph.getNode (targetCharlie) ) ||
				node.outbounds.contains ( graph.getNode (targetCharlie) )
			}
		
		when :
			graph.removeNode(targetCharlie)
			

		
		then :
			graph.getNode(targetCharlie) == null
		
			graph.getNodes().collect { charlieConnected it }.inject { prev, current -> prev || current } == false
	}
	
	
	def "for multi connected nodes, should remove an edge but not the others"(){
		
		given :
			def charlie = target("/family/charlie")
			def sally = target("/family/sally")
		
		when :
			graph.removeEdge(charlie, sally)
		
		then :
			
			def cNode = graph.getNode(charlie)
			def sNode = graph.getNode(sally)
			
			cNode.outbounds.contains sNode
			sNode.inbounds.contains cNode
		
	}
	
	def "when removing a node, new connections previously forbidden are now possible"(){
		
		given :
			def grandpa = target("/family/grandpa")
			def martha = target("/family/martha")
			def jess = target("/family/jess")
		
		when :
			graph.removeNode(martha)
			graph.addEdge(jess, grandpa)
			
		then :
			notThrown CyclicStepCallException 
		
	}
	
	def "when removing an single edge, new connections previously forbidden are now possible"(){
		
		given :
			def grandma = target("/family/grandma")
			def charlie = target("/family/charlie")
			def leonard = target("/family/leonard")
		
		when :
			graph.removeEdge(grandma, charlie)
			graph.addEdge(leonard, grandma)
			
		then :
			notThrown CyclicStepCallException
		
	}
	
	def "when removing a multi edge, new connections still aren't available"(){
		
		given :
			def charlie = target("/family/charlie")
			def grandma = target("/family/grandma")
			def sally = target("/family/sally")
		
		when :
			graph.removeEdge(charlie, sally)
			graph.addEdge(sally, grandma)
			
		then :
			thrown CyclicStepCallException
	}
	
	def "when removing a multi connected node, new connections become available"(){
		
		given :
			def charlie = target("/family/charlie")
			def grandma = target("/family/grandma")
			def sally = target("/family/sally")
		
		when :
			graph.removeNode(charlie)
			graph.addEdge(sally, grandma)
			
		then :
			notThrown CyclicStepCallException
	}
	
	
	def TestCaseTarget target(name){
		return new TestCaseTarget(name)
	}
	
	def Node node(name){
		return new Node(new TestCaseTarget(name))
	}
	
	def TestCaseCallGraph createGraph(){
		
		def graph = new TestCaseCallGraph();
		
		// the graph says who phone calls who
		def grandpa = node("/family/grandpa")
		def grandma = node("/family/grandma")
		
		def charlie = node("/family/charlie")
		def martha = node("/family/martha")
		def kenny = node("/family/kenny")
		def sally = node("/family/sally")
		
		def tommy = node("/family/tommy")
		def ziggy = node("/family/ziggy")
		def carole = node("/family/carole")
		def jess = node("/family/jess")
		def leonard = node("/family/leonard")
		
		graph.addEdge grandpa, martha
		graph.addEdge grandma, charlie
		graph.addEdge grandpa, sally
		graph.addEdge grandma, kenny
		
		// grandpa really likes kenny
		graph.addEdge grandpa, kenny
		graph.addEdge grandpa, kenny
		
		// charlie really likes sally
		graph.addEdge charlie, sally
		graph.addEdge charlie, sally
		
		graph.addEdge charlie, tommy
		graph.addEdge charlie, leonard
		graph.addEdge martha, jess
		graph.addEdge sally, carole
		graph.addEdge kenny, ziggy
		
		graph.addEdge sally, martha
		
		graph.addEdge jess, ziggy
		
		return graph
		 
	}
	
}
