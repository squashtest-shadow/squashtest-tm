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
package org.squashtest.tm.service.requirement

import javax.inject.Inject

import org.spockframework.util.NotThreadSafe
import org.squashtest.tm.domain.project.Project
import org.squashtest.tm.domain.requirement.RequirementFolder
import org.squashtest.tm.domain.testcase.TestCaseFolder
import org.squashtest.tm.service.HibernateServiceSpecification;
import org.squashtest.tm.service.project.ProjectManagerService
import org.squashtest.tm.service.requirement.CustomRequirementModificationService
import org.squashtest.tm.service.requirement.RequirementLibrariesCrudService
import org.squashtest.tm.service.requirement.RequirementLibraryNavigationService
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService
import org.squashtest.tm.service.testcase.TestCaseLibrariesCrudService
import org.squashtest.tm.service.testcase.TestCaseLibraryNavigationService
import org.squashtest.tm.service.testcase.TestCaseModificationService


@NotThreadSafe
class VerifiedRequirementModificationServiceIT extends HibernateServiceSpecification {

	@Inject
	private TestCaseModificationService tcModservice

	@Inject
	private TestCaseLibraryNavigationService tcNavService

	@Inject
	private TestCaseLibrariesCrudService tcCrudService

	@Inject
	CustomRequirementModificationService reqModService;

	@Inject
	RequirementLibraryNavigationService reqNavService;

	@Inject
	RequirementLibrariesCrudService reqCrudService;

	@Inject
	VerifiedRequirementsManagerService verifReqService;

	@Inject
	private ProjectManagerService projectService;

	private int reqFolderId = -1;
	private int tcFolderId = -1;
	def tcLib
	def reqLib

	def setup(){
		Project project = new Project(name:"proj1");
		projectService.addProject project



		def libList= tcCrudService.findAllLibraries()
		tcLib = libList.get(libList.size()-1);

		def folder =  new TestCaseFolder(name:"folder")

		tcNavService.addFolderToLibrary(tcLib.id,folder)

		tcFolderId = folder.id;


		libList= reqCrudService.findAllLibraries()
		reqLib = libList.get(libList.size()-1);

		folder =  new RequirementFolder(name:"folder")

		reqNavService.addFolderToLibrary(reqLib.id,folder)

		reqFolderId = folder.id;
	}
	

}
