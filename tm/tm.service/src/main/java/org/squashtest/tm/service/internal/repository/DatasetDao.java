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

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.testcase.Dataset;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;


public interface DatasetDao extends Repository<Dataset, Long> , CustomDatasetDao{

	@NativeMethodFromJpaRepository
	void save(Dataset newValue);

	@UsesTheSpringJpaDsl
	Dataset findById(Long id);


	/**
	 * Will return the dataset matching the given name and belonging to the test case matchine the given id.
	 * 
	 * @param testCaseId : the id of the concerned test case
	 * @param name : the name of the dataset to find
	 * @return the test case's dataset matching the given id or <code>null</code>
	 */
	@UsesANamedQueryInPackageInfoOrElsewhere
	// note : this name is a valid jpa dsl expression, but to be fully ok with the named query it should be 
	// findByTestCaseIdAndNameOrderByNameAsc, which is less cool
	Dataset findByTestCaseIdAndName(@Param("testCaseId") Long testCaseId, @Param("name") String name);


	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@EmptyCollectionGuard
	void removeAllByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);

	@UsesANamedQueryInPackageInfoOrElsewhere
	@Modifying
	@EmptyCollectionGuard
	void removeAllValuesByTestCaseIds(@Param("testCaseIds") List<Long> testCaseIds);
	
	/**
	 * Simply remove the given dataset
	 * 
	 * @param dataset : the dataset to remove
	 */
	@NativeMethodFromJpaRepository
	void delete(Dataset dataset);

	@UsesTheSpringJpaDsl
	Collection<Dataset> findAllByTestCaseId(Long testCaseId);
	
	
}
