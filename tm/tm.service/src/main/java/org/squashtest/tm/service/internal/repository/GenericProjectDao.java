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
import org.squashtest.tm.service.annotation.EmptyCollectionGuard;

/**
 * @author Gregory Fouquet
 * 
 */
public interface GenericProjectDao extends Repository<GenericProject, Long>,  CustomGenericProjectDao {

	@UsesTheSpringJpaDsl
	long countByName(String name);
	
	@NativeMethodFromJpaRepository
	long count();

	@NativeMethodFromJpaRepository
	List<GenericProject> findAll();

	@NativeMethodFromJpaRepository
	@EmptyCollectionGuard
	List<GenericProject> findAll(Iterable<Long> ids);
		
	@NativeMethodFromJpaRepository
	List<GenericProject> findAll(Sort sorting);
	
	@UsesTheSpringJpaDsl
	GenericProject findById(long projectId);

	/**
	 * Simply remove entity
	 * @param project : the {@link GenericProject} to remove
	 */
	@NativeMethodFromJpaRepository
	void delete(GenericProject project);

	
	// ************************* test automation section **********************

	
	@UsesANamedQueryInPackageInfoOrElsewhere
	List<TestAutomationProject> findBoundTestAutomationProjects(@Param(ParameterNames.PROJECT_ID) long id);

	@UsesANamedQueryInPackageInfoOrElsewhere
	List<String> findBoundTestAutomationProjectJobNames(@Param(ParameterNames.PROJECT_ID) long id);

	@UsesANamedQueryInPackageInfoOrElsewhere
	List<String> findBoundTestAutomationProjectLabels(@Param(ParameterNames.PROJECT_ID) long projectId);

	@UsesTheSpringJpaDsl
	@EmptyCollectionGuard
	List<GenericProject> findAllByIdIn(List<Long> idList, Sort sorting);

	@UsesANamedQueryInPackageInfoOrElsewhere
	TestAutomationServer findTestAutomationServer(@Param(ParameterNames.PROJECT_ID) long projectId);
	

}
