/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
package org.squashtest.tm.domain.testcase;

import java.util.List;

/***
 *	Implementing classes will carry the criteria used in the test case search functionalities.
 * 
 * 
 */
public interface TestCaseSearchCriteria {


	/**
	 * Tells whether the result set should include folders matching the criterion
	 * 
	 * @return
	 */
	boolean includeFoldersInResult();
	
	
	/**
	 * Tells whether the result set should group the data by projects. If false, the data will 
	 * only be sorted by name.
	 * 
	 * @return
	 */
	boolean isGroupByProject();
	
	
	boolean usesNameFilter();
	
	/**
	 * Tells whether a criterion regarding importance had been set in the query or not
	 * 
	 * @return
	 */
	boolean usesImportanceFilter();
	
	/**
	 * Tells whether a criterion regarding nature had been set in the query or not
	 * 
	 * @return
	 */
	boolean usesNatureFilter();
	
	/**
	 * Tells whether a criterion regarding type had been set in the query or not
	 * 
	 * @return
	 */
	boolean usesTypeFilter();
	
	
	/**
	 * Tells whether a criterion regarding status had been set in the query or not
	 * 
	 * @return
	 */
	boolean usesStatusFilter();
	
	

	/**
	 * returns the piece of the name of the test case that are looked for. Wildcards before and 
	 * after should not be included. 
	 * 
	 * @return
	 */
	String getNameFilter();
	
	
	
	
	/**
	 * gives the importances of the test cases we want to match. A result of length 0 is likely to be interpreted like 'fetch test cases having no importance', which
	 * in turn will likely result in a void result set since {@link TestCase}s cannot have null {@link TestCaseImportance} attribute.
	 * 
	 * @return
	 */
	List<TestCaseImportance> getImportanceFilterSet();

	
	/**
	 * gives the natures of the test cases we want to match. A result of length 0 is likely to be interpreted like 'fetch test cases having no nature', which
	 * in turn will likely result in a void result set since {@link TestCase}s cannot have null {@link TestCaseNature} attribute.
	 * 
	 * @return
	 */
	List<TestCaseNature> getNatureFilterSet();
	
	
	/**
	 * gives the types of the test cases we want to match. A result of length 0 is likely to be interpreted like 'fetch test cases having no type', which
	 * in turn will likely result in a void result set since {@link TestCase}s cannot have null {@link TestCaseType} attribute.
	 * 
	 * @return
	 */
	List<TestCaseType> getTypeFilterSet();
	
	/**
	 * gives the statuses of the test cases we want to match. A result of length 0 is likely to be interpreted like 'fetch test cases having no status', which
	 * in turn will likely result in a void result set since {@link TestCase}s cannot have null {@link TestCaseStatus} attribute.
	 * 
	 * @return
	 */
	List<TestCaseStatus> getStatusFilterSet();
}
