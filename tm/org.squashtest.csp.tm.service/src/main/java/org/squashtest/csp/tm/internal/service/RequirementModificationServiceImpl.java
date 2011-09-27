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
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementCriticality;
import org.squashtest.csp.tm.domain.requirement.RequirementFolder;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.infrastructure.filter.CollectionSorting;
import org.squashtest.csp.tm.infrastructure.filter.FilteredCollectionHolder;
import org.squashtest.csp.tm.internal.repository.RequirementDao;
import org.squashtest.csp.tm.service.RequirementModificationService;


@Service("squashtest.tm.service.RequirementModificationService")
public class RequirementModificationServiceImpl implements
RequirementModificationService {

	@Inject
	private RequirementDao requirementDao;

	@Inject
	@Named("squashtest.tm.service.internal.RequirementManagementService")
	private NodeManagementService<Requirement, RequirementLibraryNode, RequirementFolder> requirementManagementService;

	
	@Inject
	private RequirementNodeDeletionHandler deletionHandler;
	
	
	public RequirementModificationServiceImpl(){
		super();
	}

	@Override
	@PreAuthorize("hasPermission(#reqId, 'org.squashtest.csp.tm.domain.requirement.Requirement','WRITE') or hasRole('ROLE_ADMIN')")		
	public void updateDescription(long reqId, String newDescription) {
		Requirement req = requirementDao.findById(reqId);
		req.setDescription(newDescription);
	}



	@Override
	@PostAuthorize("hasPermission(returnObject,'READ') or hasRole('ROLE_ADMIN')")	
	public Requirement find(long reqId) {
		return requirementDao.findById(reqId);
	}

	@Override
	@PreAuthorize("hasPermission(#reqId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void rename(long reqId, String newName) {
		requirementManagementService.renameNode(reqId, newName);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCase> findVerifyingTestCasesByRequirementId(long requirementId) {
		return requirementDao.findAllVerifyingTestCasesById(requirementId);
	}


	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'READ') or hasRole('ROLE_ADMIN')")
	public FilteredCollectionHolder<List<TestCase>> findVerifyingTestCasesByRequirementId(
			long requirementId, CollectionSorting filter) {
		List<TestCase> tcs = requirementDao.findAllVerifyingTestCasesByIdFiltered(requirementId, filter);
		long count = requirementDao.countVerifyingTestCasesById(requirementId);
		return new FilteredCollectionHolder<List<TestCase>>(count, tcs);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void updateRequirementCriticality(long requirementId, RequirementCriticality criticality) {
		Requirement requirement = requirementDao.findById(requirementId);
		requirement.setCriticality(criticality);
	}

	@Override
	@PreAuthorize("hasPermission(#requirementId, 'org.squashtest.csp.tm.domain.requirement.Requirement', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void updateRequirementReference(long requirementId, String reference) {
		Requirement requirement = requirementDao.findById(requirementId);
		requirement.setReference(reference);
	}

}
