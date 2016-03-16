/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2016 Henix, henix.fr
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

import org.squashtest.tm.domain.testcase.Parameter;

public interface CustomParameterDao {

	/**
	 * Given a test case ID, returns the list of parameters that directly belong to that test case
	 * (inherited parameters are ignored).
	 * 
	 * @param testcaseId
	 * @return
	 */
	List<Parameter> findOwnParametersByTestCase(Long testcaseId);

	List<Parameter> findOwnParametersByTestCases(List<Long> testcaseIds);

	/**
	 * Given a test case ID, return the list of parameters that belongs to that test case
	 * AND thoses (transitively) delegated via call steps using the parameter delegation mode
	 * 
	 * @param testcaseIds
	 * @return
	 */
	List<Parameter> findAllParametersByTestCase(Long testcaseId);

	List<Parameter> findOwnParametersByNameAndTestCases(String name, List<Long> testcaseIds);

	Parameter findOwnParameterByNameAndTestCase(String name, Long testcaseId);
}
