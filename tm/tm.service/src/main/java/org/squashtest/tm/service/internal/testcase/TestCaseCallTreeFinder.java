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
package org.squashtest.tm.service.internal.testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squashtest.tm.domain.NamedReference;
import org.squashtest.tm.domain.library.structures.LibraryGraph;
import org.squashtest.tm.domain.library.structures.LibraryGraph.SimpleNode;
import org.squashtest.tm.service.internal.repository.TestCaseDao;

/**
 * 
 * @author Gregory Fouquet
 *
 */
@Component
public class TestCaseCallTreeFinder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseCallTreeFinder.class);
	
	@Inject
	private TestCaseDao testCaseDao;
	
	/**
	 *  given the Id of a test case, will compute the subsequent test case call tree.
	 * 
	 * @param rootTcId. Null is not legal and unchecked.
	 * @return a set containing the ids of the called test cases, that will not include the calling test case id. Not null, possibly empty. 
	 */
	public Set<Long> getTestCaseCallTree(Long rootTcId) {

		Set<Long> calleesIds = new HashSet<Long>();
		List<Long> prevCalleesIds = testCaseDao.findAllDistinctTestCasesIdsCalledByTestCase(rootTcId);

		LOGGER.trace("TestCase #{} directly calls {}", rootTcId, prevCalleesIds);

		prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data

		while (!prevCalleesIds.isEmpty()) {
			// FIXME a tester avant correction : boucle infinie quand il y a un cycle dans les appels de cas de test
			calleesIds.addAll(prevCalleesIds);
			prevCalleesIds = testCaseDao.findAllTestCasesIdsCalledByTestCases(prevCalleesIds);

			LOGGER.trace("TestCase #{} indirectly calls {}", rootTcId, prevCalleesIds);
			prevCalleesIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data
		}

		return calleesIds;
	}
	
	/**
	 * same as {@link #getTestCaseCallTree(Long)}, but for multiple test cases
	 * 
	 * @param tcIds
	 * @return
	 */
	public Set<Long> getTestCaseCallTree(Collection<Long> tcIds){
		
		Set<Long> result = new HashSet<Long>();
		
		Collection<Long> process = tcIds;
		List<Long> next;
		
		while(! process.isEmpty()){
			next = testCaseDao.findAllTestCasesIdsCalledByTestCases(process);
			next.removeAll(result);	// remove results that were already processed
			
			result.addAll(next);
			process = next;
			
		}
		
		return result;
	}
	
	
	/**
	 * Returns 
	 * 
	 * @param rootTcId
	 * @return
	 */
	public Set<Long> getTestCaseCallers(Long rootTcId) {
		
		Set<Long> callerIds = new HashSet<Long>();
		List<Long> prevCallerIds = testCaseDao.findAllDistinctTestCasesIdsCallingTestCase(rootTcId);
	
		LOGGER.trace("TestCase #{} directly calls {}", prevCallerIds, rootTcId);

		prevCallerIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data

		while (!prevCallerIds.isEmpty()) {
			// FIXME a tester avant correction : boucle infinie quand il y a un cycle dans les appels de cas de test
			callerIds.addAll(prevCallerIds);
			prevCallerIds = testCaseDao.findAllTestCasesIdsCallingTestCases(prevCallerIds);

			LOGGER.trace("TestCase #{} indirectly calls {}", prevCallerIds, rootTcId);
			prevCallerIds.remove(rootTcId);// added to prevent infinite cycle in case of inconsistent data
		}

		return callerIds;
	}
	
	/**
	 * returns a graph of simple nodes representing the ancestry of the nodes in arguments. 
	 * 
	 * @param calledIds
	 * @return
	 */
	public LibraryGraph<NamedReference, SimpleNode<NamedReference>> getCallerGraph(List<Long> calledIds){
		
		// remember which nodes were processed (so that we can spare less DB calls in the worst cases scenarios)
		Set<Long> allIds = new HashSet<Long>();
		allIds.addAll(calledIds);

		// the temporary result variable
		List<Object[]> allpairs = new ArrayList<Object[]>();

		// a temporary buffer variable
		List<Long> currentCalled = new LinkedList<Long>(calledIds);

		// phase 1 : data collection
		
		while (!currentCalled.isEmpty()) {
			
			List<Object[]> currentPair = testCaseDao.findTestCasesHavingCallerDetails(currentCalled);

			allpairs.addAll(currentPair);

			/*
			 * collect the caller ids in the currentPair for the next loop, with the following restrictions : 
			 * 1) if the "caller" slot of the Object[] is not null, and 
			 * 2) if that node was not already processed 
			 * 
			 * then we can add that id.
			 */

			List<Long> nextCalled = new LinkedList<Long>();

			for (Object[] pair : currentPair) {
				// no caller -> no need for further processing
				if (pair[0] == null){
					continue;
				}
				
				NamedReference caller = (NamedReference)pair[0];
				Long key = caller.getId();
				if (! allIds.contains(key)) {
					nextCalled.add(key);
					allIds.add(key);
				}
				
			}

			currentCalled = nextCalled;

		}
		
		// phase 2 : make that graph
		
		LibraryGraph<NamedReference, SimpleNode<NamedReference>> graph = new LibraryGraph<NamedReference, SimpleNode<NamedReference>>();
		
		for (Object[] pair : allpairs){
			graph.addEdge(new SimpleNode<NamedReference>((NamedReference) pair[0]), new SimpleNode<NamedReference>((NamedReference)pair[1]));
		}

		return graph;
		
	}

}
