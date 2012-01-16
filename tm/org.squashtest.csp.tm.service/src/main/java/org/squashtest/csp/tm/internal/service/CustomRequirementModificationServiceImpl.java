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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.service.CustomRequirementModificationService;


@Service("CustomRequirementModificationService")
public class CustomRequirementModificationServiceImpl implements
CustomRequirementModificationService {

	@Inject
	private RequirementDao requirementDao;
	
	@Inject private TestCaseDao testCaseDao;

	@SuppressWarnings("rawtypes")
	@Inject
	@Named("squashtest.tm.service.internal.RequirementManagementService")
	private NodeManagementService<Requirement, RequirementLibraryNode, RequirementFolder> requirementManagementService;
	
	public CustomRequirementModificationServiceImpl(){
		super();
	}

	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")	
	public Requirement findById(long reqId) {
		return requirementDao.findById(reqId);
	}

	@Override
	@PreAuthorize("hasPermission(#reqId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void rename(long reqId, String newName) {
		requirementManagementService.renameNode(reqId, newName);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<TestCase>> findVerifyingTestCasesByRequirementId(
			long requirementId, PagingAndSorting pagingAndSorting) {
		Requirement req = requirementDao.findById(requirementId);
		List<TestCase> verifiers = testCaseDao.findAllByVerifiedRequirementVersion(req.getCurrentVersion().getId(), pagingAndSorting);
		
		long verifiersCount = testCaseDao.countByVerifiedRequirementVersion(req.getCurrentVersion().getId());
		
		return new PagingBackedPagedCollectionHolder<List<TestCase>>(pagingAndSorting, verifiersCount, verifiers);
	}
}
