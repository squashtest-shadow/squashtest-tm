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
package org.squashtest.tm.service.requirement;

import java.util.List;

import org.squashtest.tm.domain.requirement.RequirementLibrary;
import org.squashtest.tm.domain.testcase.TestCase;

/**
 * @author Gregory Fouquet
 * 
 */
public interface RequirementLibraryFinderService {

	/**
	 * Returns the collection of {@link RequirementLibrary} which Requirements can be linked by a {@link TestCase}
	 * 
	 * @return
	 */
	List<RequirementLibrary> findLinkableRequirementLibraries();
	

	/**
	 * Returns the path of a RequirementLibraryNode given its id. The format is standard, beginning with /&lt;project-name&gt;
	 * 
	 * @param entityId the id of the node.
	 * @return the path of that node.
	 */
	String getPathAsString(long entityId);	

}