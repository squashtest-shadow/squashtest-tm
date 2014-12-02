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
package org.squashtest.tm.web.internal.controller.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.squashtest.tm.domain.requirement.RequirementCategory;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementSearchCriteria;
import org.squashtest.tm.domain.requirement.VerificationCriterion;

/**
 * Adapts a {@link RequirementSearchParams} into a {@link RequirementSearchCriteria}
 * 
 * @author Gregory Fouquet
 * 
 */
@Deprecated // make something more 1108-ish
public class RequirementSearchCriteriaAdapter implements RequirementSearchCriteria {
	private final RequirementSearchParams params;

	private final List<RequirementCriticality> criticalities = new ArrayList<RequirementCriticality>(
			RequirementCriticality.values().length);
	private final List<String> categories = new ArrayList<String>();

	public RequirementSearchCriteriaAdapter(RequirementSearchParams params, boolean[] criticalitiesSelection,
			boolean[] categoriesSelection) {
		super();
		this.params = params;
		int j = 0;
		for (int i = criticalitiesSelection.length - 1; i >= 0; i--) {
			if (criticalitiesSelection[j]) {
				criticalities.add(RequirementCriticality.valueOf(i));
			}
			j++;
		}
		for (int i = 0; i < categoriesSelection.length; i++) {
			if (categoriesSelection[i]) {
				categories.add("CAT_"+RequirementCategory.valueOf(i).toString());
			}
		}
	}

	@Override
	public String getName() {
		return params.getName();
	}

	@Override
	public String getReference() {
		return params.getReference();
	}

	@Override
	public Collection<RequirementCriticality> getCriticalities() {
		return criticalities;
	}

	@Override
	public Collection<String> getCategories() {
		return categories;
	}

	@Override
	public VerificationCriterion getVerificationCriterion() {
		String name = params.getVerification();
		if (name != null) {
			return VerificationCriterion.valueOf(params.getVerification());
		}
		return null;
	}

	@Override
	public boolean libeleIsOnlyCriteria() {
		return (StringUtils.isBlank(getReference())) && getCriticalities().isEmpty() && getCategories().isEmpty();
	}

}
