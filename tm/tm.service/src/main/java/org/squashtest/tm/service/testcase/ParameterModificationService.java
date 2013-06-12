/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
package org.squashtest.tm.service.testcase;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.dynamicmanager.annotation.DynamicManager;
import org.squashtest.tm.domain.testcase.Parameter;

@Transactional
public interface ParameterModificationService extends ParameterFinder{

	/**
	 * 
	 * @param parameter
	 */
	void persist(Parameter parameter);
	
	/**
	 * 
	 * @param parameterId
	 * @param name
	 */
	void changeName(long parameterId, String name);
	
	/**
	 * 
	 * @param parameterId
	 * @param description
	 */
	void changeDescription(long parameterId, String description);

	/**
	 * 
	 * @param parameter
	 */
	void remove(Parameter parameter);

	
	void removeAllByTestCaseIds(List<Long> testCaseIds);
	
	/**
	 * 
	 * @param stepId
	 * @return
	 */
	List<Parameter> checkForParamsInStep(long stepId);

	/**
	 * 
	 * @param parameterId
	 */
	void removeById(long parameterId);
}
