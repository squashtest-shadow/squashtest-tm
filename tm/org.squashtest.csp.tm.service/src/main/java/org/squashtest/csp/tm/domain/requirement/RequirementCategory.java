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
package org.squashtest.csp.tm.domain.requirement;

import java.util.Comparator;
import java.util.List;

import org.squashtest.csp.tm.domain.Level;

public enum RequirementCategory implements Level {
	FUNCTIONAL(0), NON_FUNCTIONAL(1), USE_CASE(2),BUSINESS(3), TEST_REQUIREMENT(4), UNDEFINED(5);

	private static final String I18N_KEY_ROOT = "requirement.category.";
	private final int level;

	private RequirementCategory(int level) {
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public static RequirementCategory valueOf(int level) {
		for (RequirementCategory cat : RequirementCategory.values()) {
			if (cat.level == level) {
				return cat;
			}
		}

		throw new IllegalArgumentException("Does not match any category level : " + level);
	}

	@Override
	public String getI18nKey() {
		return I18N_KEY_ROOT + name();
	}

}
