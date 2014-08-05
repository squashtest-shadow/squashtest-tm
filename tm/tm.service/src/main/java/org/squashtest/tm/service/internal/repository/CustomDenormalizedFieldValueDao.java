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
package org.squashtest.tm.service.internal.repository;

public interface CustomDenormalizedFieldValueDao {

	/**
	 * <p>Creates all the denormalized custom field values for execution steps. 
	 * Remember that a denormalized custom field value is a "standalone" snapshot of the custom 
	 * field value of the test steps referenced by the execution steps.</p>
	 * <p>This method exists because the normal way is too damn slow in some cases.</p>
	 * 
	 * @param executionId the id of the execution of which the execution steps belongs to. 
	 */
	void fastCreateDenormalizedValuesForSteps(long executionId);
	
	
}
