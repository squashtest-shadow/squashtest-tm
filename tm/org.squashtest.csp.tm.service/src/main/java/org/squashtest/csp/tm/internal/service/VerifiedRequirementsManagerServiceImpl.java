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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.domain.IdentifiedComparator;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.Requirement;
import org.squashtest.csp.tm.domain.requirement.RequirementLibrary;
import org.squashtest.csp.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.RequirementLibraryDao;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;
import org.squashtest.csp.tm.service.VerifiedRequirementsManagerService;

@Service("squashtest.tm.service.VerifiedRequirementsManagerService")
@Transactional
public class VerifiedRequirementsManagerServiceImpl implements VerifiedRequirementsManagerService {
	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private RequirementLibraryDao requirementLibraryDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;
	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;
		@Inject
	private ProjectFilterModificationService projectFilterModificationService;

	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.service.RequirementLibrarySelectionStrategy")
	private LibrarySelectionStrategy<RequirementLibrary, RequirementLibraryNode> libraryStrategy;

	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;

	@Override
	public TestCase findTestCase(long testCaseId) {
		return testCaseDao.findById(testCaseId);
	}

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<RequirementLibrary> findLinkableRequirementLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : requirementLibraryDao
				.findAll();
	}

	@SuppressWarnings("rawtypes")
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void addVerifiedRequirementsToTestCase(final List<Long> requirementsIds, long testCaseId) {
		// nodes are returned unsorted
		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao.findAllByIdList(requirementsIds);

		if (!nodes.isEmpty()) {
			// now we resort them according to the order in which the requirementsIds were given
			Collections.sort(nodes, IdentifiedComparator.getInstance());

			List<Requirement> requirements = new RequirementNodeWalker().walk(nodes);
			if (!requirements.isEmpty()) {
				TestCase testCase = testCaseDao.findById(testCaseId);

				for (Requirement requirement : requirements) {
					testCase.addVerifiedRequirement(requirement.getCurrentVersion());
				}
			}
			testCaseImportanceManagerService.changeImportanceIfRelationsAddedToTestCases(requirements, testCase);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeVerifiedRequirementsFromTestCase(List<Long> requirementsIds, long testCaseId) {
		List<RequirementVersion> reqs = requirementVersionDao.findAllByIdList(requirementsIds);

		if (!reqs.isEmpty()) {
			TestCase testCase = testCaseDao.findById(testCaseId);

			for (RequirementVersion requirement : reqs) {
				testCase.removeVerifiedRequirement(requirement);
			}
			testCaseImportanceManagerService
					.changeImportanceIfRelationsRemovedFromTestCase(requirementsIds, testCaseId);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.csp.tm.domain.testcase.TestCase' , 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeVerifiedRequirementFromTestCase(long requirementId, long testCaseId) {
		RequirementVersion req = requirementVersionDao.findById(requirementId);

		TestCase testCase = testCaseDao.findById(testCaseId);
		testCase.removeVerifiedRequirement(req);
		testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromTestCase(Arrays.asList(requirementId),
				testCaseId);
	}

}
