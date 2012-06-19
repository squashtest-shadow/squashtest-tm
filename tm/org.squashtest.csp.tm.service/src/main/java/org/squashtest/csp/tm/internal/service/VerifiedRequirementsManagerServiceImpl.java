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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.csp.tm.domain.VerifiedRequirementException;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;
import org.squashtest.csp.tm.service.VerifiedRequirementsManagerService;

@Service("squashtest.tm.service.VerifiedRequirementsManagerService")
@Transactional
public class VerifiedRequirementsManagerServiceImpl implements VerifiedRequirementsManagerService {
	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;

	@SuppressWarnings("rawtypes")
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'LINK') or hasRole('ROLE_ADMIN')")
	public Collection<VerifiedRequirementException> addVerifiedRequirementsToTestCase(List<Long> requirementsIds,
			long testCaseId) {
		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao.findAllByIdList(requirementsIds);

		if (!nodes.isEmpty()) {
			return doAddVerifiedRequirementsToTestCase(nodes, testCaseId);
		}

		return Collections.emptyList();
	}

	@SuppressWarnings("rawtypes")
	private Collection<VerifiedRequirementException> doAddVerifiedRequirementsToTestCase(
			List<RequirementLibraryNode> nodes, long testCaseId) {
		List<Requirement> requirements = new RequirementNodeWalker().walk(nodes);
		TestCase testCase = testCaseDao.findById(testCaseId);

		if (!requirements.isEmpty()) {
			return doAddVerifiedRequirementsToTestCase(requirements, testCase);
		}

		return Collections.emptyList();
	}

	private Collection<VerifiedRequirementException> doAddVerifiedRequirementsToTestCase(
			List<Requirement> requirements, TestCase testCase) {
		Collection<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>(
				requirements.size());

		for (Requirement requirement : requirements) {
			try {
				testCase.addVerifiedRequirement(requirement);
			} catch (VerifiedRequirementException ex) {
				rejections.add(ex);
			}
		}

		List<RequirementVersion> requirementVersions = extractVersions(requirements);
		testCaseImportanceManagerService.changeImportanceIfRelationsAddedToTestCase(requirementVersions, testCase);

		return rejections;
	}

	private List<RequirementVersion> extractVersions(List<Requirement> requirements) {
		List<RequirementVersion> rvs = new ArrayList<RequirementVersion>(requirements.size());
		for (Requirement requirement : requirements) {
			rvs.add(requirement.getResource());
		}
		return rvs;
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'LINK') or hasRole('ROLE_ADMIN')")
	public void removeVerifiedRequirementVersionsFromTestCase(List<Long> requirementsIds, long testCaseId) {
		List<RequirementVersion> reqs = requirementVersionDao.findAllByIdList(requirementsIds);

		if (!reqs.isEmpty()) {
			TestCase testCase = testCaseDao.findById(testCaseId);

			for (RequirementVersion requirement : reqs) {
				testCase.removeVerifiedRequirementVersion(requirement);
			}

			testCaseImportanceManagerService
					.changeImportanceIfRelationsRemovedFromTestCase(requirementsIds, testCaseId);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'LINK') or hasRole('ROLE_ADMIN')")
	public void removeVerifiedRequirementVersionFromTestCase(long requirementId, long testCaseId) {
		RequirementVersion req = requirementVersionDao.findById(requirementId);
		TestCase testCase = testCaseDao.findById(testCaseId);

		testCase.removeVerifiedRequirementVersion(req);

		testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromTestCase(Arrays.asList(requirementId),
				testCaseId);
	}

	/*
	 * This service associates a new verified requirement to the test case
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.squashtest.csp.tm.service.VerifiedRequirementsManagerService#changeVerifiedRequirementVersionOnTestCase(long,
	 * long, long)
	 */
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'LINK') or hasRole('ROLE_ADMIN')")
	public int changeVerifiedRequirementVersionOnTestCase(long oldVerifiedRequirementVersionId,
			long newVerifiedRequirementVersionId, long testCaseId) {
		RequirementVersion oldReq = requirementVersionDao.findById(oldVerifiedRequirementVersionId);
		RequirementVersion newReq = requirementVersionDao.findById(newVerifiedRequirementVersionId);
		TestCase testCase = testCaseDao.findById(testCaseId);

		testCase.removeVerifiedRequirementVersion(oldReq);

		testCase.addVerifiedRequirementVersion(newReq);

		testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromTestCase(
				Arrays.asList(newVerifiedRequirementVersionId), testCaseId);

		return newReq.getVersionNumber();
	}

	/*
	 * regarding the @PreAuthorize for the verified requirements :
	 * 
	 * I prefer to show all the requirements that the test case refers to even if some of those requirements belongs to
	 * a project the current user cannot "read", rather post filtering it.
	 * 
	 * The reason for that is that such policy is impractical for the same problem in the context of Iteration-TestCase
	 * associations : filtering the test cases wouldn't make much sense and would lead to partial executions of a
	 * campaign.
	 * 
	 * Henceforth the same policy applies to other cases of possible inter-project associations (like
	 * TestCase-Requirement associations in the present case), for the sake of coherence.
	 * 
	 * @author bsiri
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.squashtest.csp.tm.service.TestCaseModificationService#findVerifiedRequirementsByTestCaseId(long,
	 * org.squashtest.csp.tm.infrastructure.filter.CollectionSorting)
	 */
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<RequirementVersion>> findAllDirectlyVerifiedRequirementsByTestCaseId(
			long testCaseId, PagingAndSorting pas) {
		List<RequirementVersion> verifiedReqs = requirementVersionDao.findAllVerifiedByTestCase(testCaseId, pas);
		long verifiedCount = requirementVersionDao.countVerifiedByTestCase(testCaseId);
		return new PagingBackedPagedCollectionHolder<List<RequirementVersion>>(pas, verifiedCount, verifiedReqs);
	}

}
