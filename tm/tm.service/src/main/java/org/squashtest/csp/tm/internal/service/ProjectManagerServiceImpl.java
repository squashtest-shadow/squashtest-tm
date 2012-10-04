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
package org.squashtest.csp.tm.internal.service;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.service.security.ObjectIdentityService;
import org.squashtest.csp.tm.domain.campaign.CampaignLibrary;
import org.squashtest.csp.tm.domain.project.Project;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.internal.repository.CampaignLibraryDao;
import org.squashtest.csp.tm.internal.repository.ProjectDao;
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.service.ProjectManagerService;

@Service("squashtest.tm.service.ProjectManagerService")
@Transactional
public class ProjectManagerServiceImpl implements ProjectManagerService {

	@Inject
	private ProjectDao projectDao;

	@Inject
	private CampaignLibraryDao campaignLibraryDao;

	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;

	@Inject
	private RequirementLibraryDao requirementLibraryDao;

	@Inject
	private ObjectIdentityService objectIdentityService;

	@Override
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public void addProject(Project project) {
		CampaignLibrary cl = new CampaignLibrary();
		project.setCampaignLibrary(cl);
		campaignLibraryDao.persist(cl);

		RequirementLibrary rl = new RequirementLibrary();
		project.setRequirementLibrary(rl);
		requirementLibraryDao.persist(rl);

		TestCaseLibrary tcl = new TestCaseLibrary();
		project.setTestCaseLibrary(tcl);
		testCaseLibraryDao.persist(tcl);

		projectDao.persist(project);

		objectIdentityService.addObjectIdentity(project.getId(), project.getClass());
		objectIdentityService.addObjectIdentity(tcl.getId(), tcl.getClass());
		objectIdentityService.addObjectIdentity(rl.getId(), rl.getClass());
		objectIdentityService.addObjectIdentity(cl.getId(), cl.getClass());

	}

}
