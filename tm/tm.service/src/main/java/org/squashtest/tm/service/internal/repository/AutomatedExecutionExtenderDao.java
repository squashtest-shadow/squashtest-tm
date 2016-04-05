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

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.repository.Repository;
import org.squashtest.tm.domain.testautomation.AutomatedExecutionExtender;
import org.squashtest.tm.service.testautomation.AutomatedExecutionSetIdentifier;

/**
 * @author bsiri
 * @author Gregory Fouquet
 * 
 */
public interface AutomatedExecutionExtenderDao extends Repository<AutomatedExecutionExtender, Long>{

	// note : uses the Spring JPA dsl 
	AutomatedExecutionExtender findById(long executionId);

	// note : native method from JPA repositories
	void save(AutomatedExecutionExtender extender);

	/**
	 * Returns the {@link AutomatedExecution}s which match the {@link AutomatedExecutionSetIdentifier}.
	 * 
	 * @param projectName
	 * 
	 * @return
	 */
	// note : uses a named query in package-info or elsewhere
	List<AutomatedExecutionExtender> findAllBySuiteIdAndTestName(@NotNull String suiteId, @NotNull String testName,
			@NotNull String projectName);
	
	// note : uses a named query in package-info or elsewhere
	List<AutomatedExecutionExtender> findAllBySuiteIdAndProjectId(@NotNull String suiteId, @NotNull Long projectId);
}
