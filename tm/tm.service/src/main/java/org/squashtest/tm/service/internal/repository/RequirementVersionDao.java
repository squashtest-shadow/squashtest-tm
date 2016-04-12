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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

/**
 *
 * @author Gregory Fouquet
 *
 */
public interface RequirementVersionDao extends Repository<RequirementVersion, Long>, CustomRequirementVersionDao {

	// note : native method from JPA repositorie
	@EmptyCollectionGuard
	List<RequirementVersion> findAll(Iterable<Long> ids);
	
	// note : native method from JPA repositorie
	RequirementVersion findById(long requirementId);

	// note : uses a named query in package-info or elsewhere
	long countVerifiedByTestCase(long testCaseId);

	// note : uses the Spring JPA dsl
	List<RequirementVersion> findAllByRequirement(Requirement node);

	// note : uses the Spring JPA dsl
	Page<RequirementVersion> findAllByRequirementId(long requirementId, Pageable pageable);
	
	/**
	 * @param requirementId
	 * @return the versions count for the given requirement.
	 */
	// note : uses the Spring JPA dsl
	long countByRequirementId(long requirementId);

	// note : uses the Spring JPA dsl
	RequirementVersion findByRequirementIdAndVersionNumber(Long requirementId, Integer versionNumber);
	
}
