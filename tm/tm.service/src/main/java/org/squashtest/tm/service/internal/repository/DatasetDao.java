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
package org.squashtest.tm.service.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.tm.core.dynamicmanager.annotation.DynamicDao;
import org.squashtest.tm.core.dynamicmanager.annotation.QueryParam;
import org.squashtest.tm.domain.testcase.Dataset;

@DynamicDao(entity = Dataset.class)
public interface DatasetDao extends CustomDatasetDao{

	void persist(Dataset newValue);

	Dataset findById(Long id);

	void removeAllByTestCaseIds(@QueryParam("testCaseIds") List<Long> testCaseIds);

	void removeAllValuesByTestCaseIds(@QueryParam("testCaseIds") List<Long> testCaseIds);
	/**
	 * Simply remove the given dataset
	 * 
	 * @param dataset : the dataset to remove
	 */
	void remove(Dataset dataset);

	Collection<Dataset> findAllByTestCase(@QueryParam("testCaseId") Long testCaseId);
}
