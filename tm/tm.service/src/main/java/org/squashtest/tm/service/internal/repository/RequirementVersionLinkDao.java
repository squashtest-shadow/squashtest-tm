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

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

import java.util.Collection;
import java.util.List;

/**
 * Data access methods for {@link RequirementVersionLink}. Methods are all dynamically generated.
 *
 * @author jlor
 *
 * Note: This Dao uses NamedQueries written in hibernate/package-info.
 */
public interface RequirementVersionLinkDao extends CrudRepository<RequirementVersionLink, Long>, CustomRequirementVersionLinkDao {

	/**
	 * Find all the {@link RequirementVersionLink}s in which the single {@link RequirementVersion} given is involved with
	 * the several other RequirementVersions given in the List.
	 * It returns the entries for both directions.
	 * @param requirementVersionId The ID of the single {@link RequirementVersion}.
	 * @param otherRequirementVersionsIds The IDs of the several other {@link RequirementVersion}s.
	 * @return The List of {@link RequirementVersionLink}s linking the single RequirementVersion given as first parameter
	 * and the other RequirementVersions given in the second parameter.
	 */
	//List<RequirementVersionLink> findByOneReqVersionAndSeveralOthers(@Param("requirementVersionId")long requirementVersionId, @Param("otherRequirementVersionsIds") List<Long> otherRequirementVersionsIds);

	/**
	 * Deletes all the RequirementVersionLinks that exist between the single given RequirementVersion and all the several others.
	 * The request deletes two RequirementVersionLink per pair of ids, one for each link direction.
	 * @param singleRequirementVersionId
	 * @param requirementVersionIdsToUnlink
	 */
	@Modifying
	void deleteAllLinks(@Param("singleRequirementVersionId") long singleRequirementVersionId,
						@Param("requirementVersionIdsToUnlink") Iterable<Long> requirementVersionIdsToUnlink);
}
