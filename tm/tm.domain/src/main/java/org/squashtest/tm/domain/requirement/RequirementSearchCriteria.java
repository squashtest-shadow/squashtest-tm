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
package org.squashtest.tm.domain.requirement;

import java.util.Collection;

/**
 * Criteria applied for a Requirement search.
 * 
 * @author Gregory Fouquet
 * 
 */
public interface RequirementSearchCriteria {
	/**
	 * Token to be matched by searched Requirements name.
	 * 
	 * @return
	 */
	String getName();

	/**
	 * Token to be matched by searched Requirements reference.
	 * 
	 * @return
	 */
	String getReference();

	/**
	 * Criticalities of searched Requirements.
	 * 
	 * @return List of {@link RequirementCriticality}. Should never return null.
	 */
	Collection<RequirementCriticality> getCriticalities();

	/**
	 * Verification criterion, meaning whether the searched Requirements should be verified by a test case or not.
	 * 
	 * @return the verification criterion. Should never return null.
	 */
	VerificationCriterion getVerificationCriterion();

	boolean libeleIsOnlyCriteria();
	/**
	 * Categories of searched Requirements.
	 * 
	 * @return List of {@link RequirementCategory}. Should never return null.
	 */
	Collection<RequirementCategory> getCategories();
}
