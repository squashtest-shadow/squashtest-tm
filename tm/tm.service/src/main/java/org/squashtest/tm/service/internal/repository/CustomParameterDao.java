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
package org.squashtest.tm.service.internal.repository;

import java.util.List;

import org.squashtest.tm.domain.testcase.Parameter;

public interface CustomParameterDao {

	// XXX the name of that method is very misleading when compared with ParameterFinder#findAllforTestCase because unlike the service the dao
	// method will NOT bring up the inherited parameters
	List<Parameter> findAllByTestCase(Long testcaseId);

	List<Parameter> findAllByTestCases(List<Long> testcaseIds);

	List<Parameter> findAllByNameAndTestCases(String name, List<Long> testcaseIds);

	Parameter findParameterByNameAndTestCase(String name, Long testcaseId);
}
