/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2014 Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.squashtest.tm.service.internal.requirement;

import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.SessionFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCriticality;
import org.squashtest.tm.domain.requirement.RequirementFolder;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.internal.customfield.PrivateCustomFieldValueService;
import org.squashtest.tm.service.internal.library.NodeManagementService;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.requirement.CustomRequirementModificationService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

@Service("CustomRequirementModificationService")
@Transactional
public class CustomRequirementModificationServiceImpl implements CustomRequirementModificationService {
	@Inject
	private RequirementDao requirementDao;
	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private PrivateCustomFieldValueService customFieldValueService;
	
	@Inject
	private SessionFactory sessionFactory;
	
	@Inject
	private IndexationService indexationService;

	@SuppressWarnings("rawtypes")
	@Inject
	@Named("squashtest.tm.service.internal.RequirementManagementService")
	private NodeManagementService<Requirement, RequirementLibraryNode, RequirementFolder> requirementManagementService;

	public CustomRequirementModificationServiceImpl() {
		super();
	}

	@Override
	@PreAuthorize("hasPermission(#reqId, 'org.squashtest.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void rename(long reqId, String newName) {
		requirementManagementService.renameNode(reqId, newName);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'CREATE') or hasRole('ROLE_ADMIN')")
	public void createNewVersion(long requirementId) {
		Requirement req = requirementDao.findById(requirementId);
		RequirementVersion previousVersion = req.getCurrentVersion();
		
		req.increaseVersion();
		sessionFactory.getCurrentSession().persist(req.getCurrentVersion());	
		RequirementVersion newVersion = req.getCurrentVersion();
		indexationService.reindexRequirementVersions(req.getRequirementVersions());
		customFieldValueService.copyCustomFieldValues(previousVersion, newVersion);
	}
	

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void changeCriticality(long requirementId, RequirementCriticality criticality) {
		Requirement requirement = requirementDao.findById(requirementId);
		RequirementCriticality oldCriticality = requirement.getCriticality();
		requirement.setCriticality(criticality);
		Long requirementVersionId = requirement.getCurrentVersion().getId();
		testCaseImportanceManagerService.changeImportanceIfRequirementCriticalityChanged(requirementVersionId,
				oldCriticality);

	}

	
}
