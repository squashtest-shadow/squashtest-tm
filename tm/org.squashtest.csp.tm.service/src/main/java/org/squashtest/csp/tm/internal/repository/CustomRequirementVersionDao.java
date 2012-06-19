/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2012 Henix, henix.fr
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
package org.squashtest.csp.tm.internal.repository;

import java.util.Collection;
import java.util.List;

import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;

public interface CustomRequirementVersionDao {
	/**
	 * Returns paged and sorted collection of requirement versions verified by any of the given verifiers.
	 *
	 * @param verifiersIds
	 * @param sorting
	 * @return
	 */
	List<RequirementVersion> findAllVerifiedByTestCases(Collection<Long> verifiersIds, PagingAndSorting sorting);
	/**
	 * Counts the number of requirements verified by any of the given verifiers.
	 *
	 * @param verifiersIds
	 * @return
	 */
	long countVerifiedByTestCases(Collection<Long> verifiersIds);

	/**
	 * Returns paged and sorted collection of requirement versions verified by the test case of given id.
	 *
	 * @param verifierId
	 * @param sorting
	 * @return
	 */
	List<RequirementVersion> findAllVerifiedByTestCase(long verifierId, PagingAndSorting sorting);

	/**
	 * @param requirementId
	 * @param pas
	 * @return the paged, sorted versions of the given requirement.
	 */
	List<RequirementVersion> findAllByRequirement(long requirementId, PagingAndSorting pas);

}
