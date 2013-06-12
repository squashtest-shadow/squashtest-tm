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

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.domain.testcase.TestCase;

@Transactional
public interface DatasetModificationService {

	/**
	 * 
	 * @param dataset
	 */
	void persist(Dataset dataset);
	
	/**
	 * 
	 * @param dataset
	 */
	void remove(Dataset dataset);
	
	/**
	 * 
	 * @param datasetId
	 */
	void removeById(long datasetId);
	
	/**
	 * 
	 * @param datasetId
	 * @param name
	 */
	void changeName(long datasetId, String name);
	
	/**
	 * 
	 * @param datasetId
	 * @param paramId
	 * @param value
	 */
	void changeParamValue(long datasetParamValueId, String value);

	void updateDatasetParameters(long testCaseId);
}
