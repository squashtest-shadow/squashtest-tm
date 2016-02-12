/**
 * This file is part of the Squashtest platform.
 * Copyright (C) 2010 - 2015 Henix, henix.fr
 * <p/>
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * <p/>
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * this software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.domain.requirement;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Use to compare RequirementVersions numbers.
 *
 * @author mpagnon
 *
 */
public class RequirementVersionNumberComparator implements Comparator<RequirementVersion>, Serializable {

	@Override
	/**
	 * Compare requirement versions according to their version number for a descending order..
	 */
	public int compare(RequirementVersion o1, RequirementVersion o2) {
		// XXX replace with more efficient : return -(rv1.versionNumber - rv2.versionNumber)
		return -((Integer) o1.getVersionNumber()).compareTo(o2.getVersionNumber());
	}

}
