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

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.squashtest.tm.domain.project.GenericProject;
import org.squashtest.tm.domain.testautomation.TestAutomationProject;
import org.squashtest.tm.domain.testautomation.TestAutomationServer;

/**
 * @author Gregory Fouquet
 * 
 */
public interface GenericProjectDao extends Repository<GenericProject, Long>,  CustomGenericProjectDao {

	// note : uses the Spring JPA dsl 
	long countByName(String name);
	
	// note : native method from JPA repositorie
	long count();

	// note : native method from JPA repositorie
	List<GenericProject> findAll();

	// note : native method from JPA repositorie
	List<GenericProject> findAll(Iterable<Long> ids);
		
	// note : native method from JPA repositorie	
	List<GenericProject> findAll(Sort sorting);
	
	// note : uses the Spring JPA dsl 
	GenericProject findById(long projectId);

	/**
	 * Simply remove entity
	 * @param project : the {@link GenericProject} to remove
	 */
	// note : native method from JPA repositorie	
	void delete(GenericProject project);

	
	// ************************* test automation section **********************

	
	// note : uses a named query in package-info or elsewhere
	List<TestAutomationProject> findBoundTestAutomationProjects(@Param(ParameterNames.PROJECT_ID) long id);

	// note : uses a named query in package-info or elsewhere
	List<String> findBoundTestAutomationProjectJobNames(@Param(ParameterNames.PROJECT_ID) long id);

	// note : uses a named query in package-info or elsewhere
	List<String> findBoundTestAutomationProjectLabels(@Param(ParameterNames.PROJECT_ID) long projectId);

	// note : uses the Spring JPA dsl 
	List<GenericProject> findAllByIdIn(List<Long> idList, Sort sorting);

	// note : uses a named query in package-info or elsewhere
	TestAutomationServer findTestAutomationServer(@Param(ParameterNames.PROJECT_ID) long projectId);
	

}
