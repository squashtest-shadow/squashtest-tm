/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2011 Squashtest TM, Squashtest.org
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
import javax.inject.Named;

import org.hibernate.SessionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.service.CustomRequirementModificationService;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;

@Service("CustomRequirementModificationService")
public class CustomRequirementModificationServiceImpl implements CustomRequirementModificationService {
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private SessionFactory sessionFactory;

	@SuppressWarnings("rawtypes")
	@Inject
	@Named("squashtest.tm.service.internal.RequirementManagementService")
	private NodeManagementService<Requirement, RequirementLibraryNode, RequirementFolder> requirementManagementService;

	public CustomRequirementModificationServiceImpl() {
		super();
	}

	@Override
	@PreAuthorize("hasPermission(#reqId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'VALIDATE') or hasRole('ROLE_ADMIN')")
	public void rename(long reqId, String newName) {
		requirementManagementService.renameNode(reqId, newName);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'CREATE') or hasRole('ROLE_ADMIN')")
	public void createNewVersion(long requirementId) {
		Requirement req = requirementDao.findById(requirementId);
		req.increaseVersion();
		sessionFactory.getCurrentSession().persist(req.getCurrentVersion());
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'VALIDATE') or hasRole('ROLE_ADMIN')")
	public void changeCriticality(long requirementId, RequirementCriticality criticality) {
		Requirement requirement = requirementDao.findById(requirementId);
		RequirementCriticality oldCriticality = requirement.getCriticality();
		requirement.setCriticality(criticality);
		Long requirementVersionId = requirement.getCurrentVersion().getId();
		testCaseImportanceManagerService.changeImportanceIfRequirementCriticalityChanged(requirementVersionId,
				oldCriticality);

	}
}
