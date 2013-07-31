/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.testcase;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseLibrary;

/**
 * @author Gregory
 *
 */
public interface TestCaseLibraryFinderService {

	/**
	 * Returns the collection of {@link TestCaseLibrary}s which TestCases can be linked by a {@link TestCase} via 
	 * a CallTestStep
	 * 
	 * @return
	 */
	List<TestCaseLibrary> findLinkableTestCaseLibraries();

	

	/**
	 * Returns the path of a TestCaseLibraryNode given its id. The format is standard, beginning with /&lt;project-name&gt;
	 * 
	 * @param entityId the id of the node.
	 * @return the path of that node.
	 */
	String getPathAsString(long entityId);	
	
	
	/**
	 * Passing the ids of some selected TestCaseLibrary and TestCaseLibraryNodes (in separate collections),
	 * will return the statistics covering all the TestCases encompassed by this selection. The test case ids 
	 * that cannot be accessed for security reason will be filtered out.
	 * 
	 * 
	 * @param libraryIds
	 * @param nodeIds
	 * @return TestcaseStatisticsBundle
	 */
	TestCaseStatisticsBundle getStatisticsForSelection(Collection<Long> libraryIds, Collection<Long> nodeIds);
	
	/**
	 * Passing the ids of some selected TestCaseLibrary and TestCaseLibraryNodes (in separate collections), 
	 * will return the ids of the TestCases encompassed by this selection. The test case ids that cannot be accessed
	 * for security reason will be filtered out.
	 * 
	 * @param libraryIds
	 * @param nodeIds
	 * @return
	 */
	Collection<Long> findTestCaseIdsFromSelection(Collection<Long> libraryIds, Collection<Long> nodeIds);
}