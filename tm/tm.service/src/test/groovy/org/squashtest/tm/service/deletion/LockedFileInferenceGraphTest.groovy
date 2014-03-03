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
package org.squashtest.tm.service.deletion

import org.apache.poi.hssf.record.formula.functions.T
import org.squashtest.tm.service.internal.deletion.LockedFileInferenceGraph

import spock.lang.Specification

class LockedFileInferenceGraphTest extends Specification {

	private toListOfArray(List<List<Object>> inputList){
		def result = new ArrayList<Object[]>();
		
		for (List<Object> list  : inputList){
			def array = new Object[4]
			array[0]=list[0]
			array[1]=list[1]
			array[2]=list[2]
			array[3]=list[3]
			result.add(array)
		}
		
		return result
	}
	
	
	//if there is a groovy way to do that please tell me
	private boolean containsValue(List<Object[]> list, Object[] value){
		for (Object[] item : list){
			boolean match = true;
			for (int i=0;i<value.length;i++){
				if ( item[i] != value[i]){
					match=false;
					break;
				}
			}
			if (match) return true;
		}
		return false;
		
	}
	
	
	private boolean areContentEquals(List<Long> list1, List<Long> list2){
		return ((list1.containsAll(list2)) && (list2.containsAll(list1)))
	}
	
	/*
	* structure : 1 calls 11 and 12,
	* 			   11 calls 21, 22 and 23,
	* 			   12 calls 22, 23 and 24
	* 			   23 calls 24
	*
	* 	21, 22, 24 and 25 call no one.
	*  1 and 25 are called by no one.
	*
	*/
	
	def "should build a graph using caller/called details"(){
		
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
			
			def allData = layer0 + layer1 + layer2
		
		when :
			def graph = new LockedFileInferenceGraph();
			graph.build(allData)
		
		then :
			def nodes = graph.getNodes()
			
			nodes.size == 8
			
			def node1 = graph.getNode(1l)
			def node11 = graph.getNode(11l)
			def node12 = graph.getNode(12l)
			def node21 = graph.getNode(21l)
			def node22 = graph.getNode(22l)
			def node23 = graph.getNode(23l)
			def node24 = graph.getNode(24l)
			def node25 = graph.getNode(25l)
			
			node1.name == "1"
			node1.parents.size == 0
			node1.children == [node11, node12]
			
			node11.name == "11"
			node11.parents == [node1]
			node11.children == [node21, node22, node23]
			
			node12.name == "12"
			node12.parents == [node1]
			node12.children == [node22, node23, node24]
			
			node21.name == "21"
			node21.parents == [node11]
			node21.children == []
			
			node22.name == "22"
			node22.parents== [node11, node12]
			node22.children == []
			
			node23.name == "23"
			node23.parents== [node11, node12]
			node23.children == [node24]
			
			node24.name == "24"
			node24.parents== [node12, node23]
			node24.children == []
			
			node25.name == "25"
			node25.parents.size == 0
			node25.children == []
			
		
	}
	
	
	
	
	
	def "should mark which nodes are deletable (#1)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
			
			def allData = layer0 + layer1 + layer2
	
		and :
			def graph = new LockedFileInferenceGraph();
			graph.build(allData)
			
		and :
			def candidates = [1l, 25l, 11l, 23l]
			def reallyDeletables = [1l, 25l, 11l ]
			def lockedExpected = [23l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key};
			def locked = graph.collectLockedCandidates().collect{it.key};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
			
	}
	
	
	def "should mark which nodes are deletable (#2)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
			
			def allData = layer0 + layer1 + layer2
	
		and :
			def graph = new LockedFileInferenceGraph();
			graph.build(allData)
			
		and :
			def candidates = [1l]
			def reallyDeletables = [1l]
			def lockedExpected = []
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key};
			def locked = graph.collectLockedCandidates().collect{it.key};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	def "should mark which nodes are deletable (#3)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
			
			def allData = layer0 + layer1 + layer2
	
		and :
			def graph = new LockedFileInferenceGraph();
			graph.build(allData)
			
		and :
			def candidates = [25l, 11l]
			def reallyDeletables = [25l]
			def lockedExpected = [11l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key};
			def locked = graph.collectLockedCandidates().collect{it.key};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	
	def "should mark which nodes are deletable (#4)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
			
			def allData = layer0 + layer1 + layer2
	
		and :
			def graph = new LockedFileInferenceGraph();
			graph.build(allData)
			
		and :
			def candidates = [24l]
			def reallyDeletables = [ ]
			def lockedExpected = [24l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key};
			def locked = graph.collectLockedCandidates().collect{it.key};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	def "should mark which nodes are deletable (#5)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
			
			def allData = layer0 + layer1 + layer2
	
		and :
			def graph = new LockedFileInferenceGraph();
			graph.build(allData)
			
		and :
			def candidates = [1l, 11l, 21l]
			def reallyDeletables = [ 1l, 11l, 21l]
			def lockedExpected = []
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key};
			def locked = graph.collectLockedCandidates().collect{it.key};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
	def "should mark which nodes are deletable (#6)"(){
		given :
			def layer0 = [ [null, null, 1l, "1"].toArray() ]
			def layer1 = [ [1l, "1", 11l, "11"].toArray(), [1l, "1", 12l, "12"].toArray()  ]
			def layer2 = [
							[11l, "11", 21l , "21"   ].toArray(), [11l, "11", 22l, "22"   ].toArray(), [11l, "11", 23l, "23"   ].toArray(),
							[12l, "12", 22l , "22"   ].toArray(), [12l, "12", 23l, "23"   ].toArray(), [12l, "12", 24l, "24"   ].toArray(),
							[23l, "23", 24l, "24"].toArray(),
							[null, null, 25l, "25" ].toArray()
						 ]
			
			def allData = layer0 + layer1 + layer2
	
		and :
			def graph = new LockedFileInferenceGraph();
			graph.build(allData)
			
		and :
			def candidates = [1l, 12l, 24l,]
			def reallyDeletables = [1l, 12l ]
			def lockedExpected = [24l]
			
		when :
			graph.setCandidatesToDeletion(candidates)
			graph.resolveLockedFiles()
			
			def deletables = graph.collectDeletableNodes().collect{it.key};
			def locked = graph.collectLockedCandidates().collect{it.key};
		
		then :
			areContentEquals(deletables, reallyDeletables)
			areContentEquals(locked, lockedExpected)
	}
	
	
	
}
