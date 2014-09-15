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
package org.squashtest.tm.service.requirement;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementVersion;

/**
 * RequirementVersion management services which cannot be dyanmically generated.
 *
 * @author Gregory Fouquet
 *
 */
public interface CustomRequirementVersionManagerService {
	/**
	 * will change the requirement criticality and update the importance of any associated TestCase with importanceAuto
	 * == true.<br>
	 * (even through call steps)
	 *
	 * @param requirementVersionId
	 * @param criticality
	 */
	void changeCriticality(long requirementVersionId, @NotNull RequirementCriticality criticality);
	
	/**
	 * Fetches the paged, sorted collection of versions for the given requirement.
	 *
	 * @param requirementId
	 * @param pas
	 * @return
	 */
	@Transactional(readOnly = true)
	PagedCollectionHolder<List<RequirementVersion>> findAllByRequirement(long requirementId,
			@NotNull PagingAndSorting pas);
	
	/**
	 * Fetches all versions for the given requirement
	 * @param id
	 * @return
	 */
	@Transactional(readOnly=true)
	List<RequirementVersion> findAllByRequirement(long requirementId);	

}
