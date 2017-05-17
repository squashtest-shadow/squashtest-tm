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

import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
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
public interface RequirementVersionLinkDao extends Repository<RequirementVersionLink, Long> {

	/**
	 * Find all the {@link RequirementVersionLink} in which the given {@link RequirementVersion} is involved.
	 * @param requirementVersionId The ID of the Requirement Version of which we want all the Links.
	 * @return The List of all the {@link RequirementVersionLink} in which the given RequirementVersion is involved.
	 */
	List<RequirementVersionLink> findAllByReqVersionId(@Param("requirementVersionId") long requirementVersionId);
}
