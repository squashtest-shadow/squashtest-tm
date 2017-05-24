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

import org.springframework.data.repository.query.Param;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.requirement.RequirementVersionLink;

import java.util.List;

/**
 * Created by jlor on 19/05/2017.
 */
public interface CustomRequirementVersionLinkDao {

	/**
	 * Returns a paged and ordered list of all the {@link RequirementVersionLink} in which the given {@link RequirementVersion} is involved.
	 * @param requirementVersionId The ID of the Requirement Version of which we want all the Links.
	 * @return The List of all the {@link RequirementVersionLink} in which the given RequirementVersion is involved.
	 */
	List<RequirementVersionLink> findAllByReqVersionId(@Param("requirementVersionId") long requirementVersionId, PagingAndSorting pagingAndSorting);
	/**
	 *  Verifies if a link already exists between the two RequirementVersions which Ids are given as parameters.
	 * @param reqVersionId1
	 * @param reqVersionId2
	 * @return true if the link already exists between the two RequirementVersions, false otherwise.
	 */
	boolean linkAlreadyExists(Long reqVersionId1, Long reqVersionId2);
}
