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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.domain.testcase.Dataset;
/**
 * 
 * @author flaurens, mpagnon
 *
 */
public interface CustomDatasetDao {
	
	/**
	 * Will return all datasets for the given test case.
	 * @param testCaseId
	 * 
	 * @return the list of all test cases's datasets.
	 */
	List<Dataset> findAllDatasetsByTestCase(Long testCaseId);
	
	/**
	 * Will return all datasets found for the given test cases ids.
	 * 
	 * @param testCaseIds : the concerned test cases ids.
	 * @return the list of all given test cases's datasets
	 */
	List<Dataset> findAllDatasetsByTestCases(List<Long> testCaseIds);
	
	/**
	 * Will return the dataset matching the given name and belonging to the test case matchine the given id.
	 * 
	 * @param testCaseId : the id of the concerned test case
	 * @param name : the name of the dataset to find
	 * @return the test case's dataset matching the given id or <code>null</code>
	 */
	Dataset findDatasetByTestCaseAndByName(Long testCaseId, String name);
}
