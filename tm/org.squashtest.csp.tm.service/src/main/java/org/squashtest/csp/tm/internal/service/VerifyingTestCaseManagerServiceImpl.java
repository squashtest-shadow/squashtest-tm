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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.csp.core.domain.IdentifiedComparator;
import org.squashtest.csp.core.infrastructure.collection.PagedCollectionHolder;
import org.squashtest.csp.core.infrastructure.collection.PagingAndSorting;
import org.squashtest.csp.core.infrastructure.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.csp.tm.domain.RequirementAlreadyVerifiedException;
import org.squashtest.csp.tm.domain.VerifiedRequirementException;
import org.squashtest.csp.tm.domain.projectfilter.ProjectFilter;
import org.squashtest.csp.tm.domain.requirement.RequirementVersion;
import org.squashtest.csp.tm.domain.testcase.TestCase;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibrary;
import org.squashtest.csp.tm.domain.testcase.TestCaseLibraryNode;
import org.squashtest.csp.tm.internal.infrastructure.strategy.LibrarySelectionStrategy;
import org.squashtest.csp.tm.internal.repository.LibraryNodeDao;
import org.squashtest.csp.tm.internal.repository.RequirementVersionDao;
import org.squashtest.csp.tm.internal.repository.TestCaseDao;
import org.squashtest.csp.tm.internal.repository.TestCaseLibraryDao;
import org.squashtest.csp.tm.service.ProjectFilterModificationService;
import org.squashtest.csp.tm.service.TestCaseImportanceManagerService;
import org.squashtest.csp.tm.service.VerifyingTestCaseManagerService;

@Service("squashtest.tm.service.VerifyingTestCaseManagerService")
@Transactional
public class VerifyingTestCaseManagerServiceImpl implements VerifyingTestCaseManagerService {

	@Inject
	private TestCaseDao testCaseDao;
	@Inject
	private TestCaseLibraryDao testCaseLibraryDao;
	@Inject
	private RequirementVersionDao requirementVersionDao;
	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@Inject
	private ProjectFilterModificationService projectFilterModificationService;
	@Inject
	@Qualifier("squashtest.tm.service.TestCaseLibrarySelectionStrategy")
	private LibrarySelectionStrategy<TestCaseLibrary, TestCaseLibraryNode> libraryStrategy;
	@Inject
	@Qualifier("squashtest.tm.repository.TestCaseLibraryNodeDao")
	private LibraryNodeDao<TestCaseLibraryNode> testCaseLibraryNodeDao;

	@Override
	@PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
	public List<TestCaseLibrary> findLinkableTestCaseLibraries() {
		ProjectFilter pf = projectFilterModificationService.findProjectFilterByUserLogin();
		return pf.getActivated() ? libraryStrategy.getSpecificLibraries(pf.getProjects()) : testCaseLibraryDao
				.findAll();
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.csp.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	public Collection<VerifiedRequirementException> addVerifyingTestCasesToRequirementVersion(List<Long> testCasesIds,
			long requirementVersionId) {
		// nodes are returned unsorted
		List<TestCaseLibraryNode> nodes = testCaseLibraryNodeDao.findAllByIdList(testCasesIds);

		// now we resort them according to the order in which the testcaseids were given
		Collections.sort(nodes, IdentifiedComparator.getInstance());

		List<TestCase> testCases = new TestCaseNodeWalker().walk(nodes);

		if (!testCases.isEmpty()) {
			return doAddVerifyingTestCasesToRequirementVersion(testCases, requirementVersionId);
		}

		return Collections.emptyList();
	}

	private Collection<VerifiedRequirementException> doAddVerifyingTestCasesToRequirementVersion(
			List<TestCase> testCases, long requirementVersionId) {
		RequirementVersion requirementVersion = requirementVersionDao.findById(requirementVersionId);

		List<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>(testCases.size());

		for (TestCase testCase : testCases) {
			try {
				requirementVersion.addVerifyingTestCase(testCase);
			} catch (RequirementAlreadyVerifiedException ex) {
				rejections.add(ex);
			}
			testCaseImportanceManagerService.changeImportanceIfRelationsAddedToReq(testCases, requirementVersion);
		}

		return rejections;
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.csp.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeVerifyingTestCasesFromRequirementVersion(List<Long> testCasesIds, long requirementVersionId) {

		List<TestCase> testCases = testCaseDao.findAllByIdList(testCasesIds);

		if (!testCases.isEmpty()) {
			RequirementVersion requirementVersion = requirementVersionDao.findById(requirementVersionId);

			for (TestCase testCase : testCases) {
				requirementVersion.removeVerifyingTestCase(testCase);
			}
			testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromReq(testCasesIds,
					requirementVersionId);
		}
	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.csp.tm.domain.requirement.RequirementVersion', 'WRITE') or hasRole('ROLE_ADMIN')")
	public void removeVerifyingTestCaseFromRequirementVersion(long testCaseId, long requirementVersionId) {

		RequirementVersion req = requirementVersionDao.findById(requirementVersionId);
		TestCase testCase = testCaseDao.findById(testCaseId);

		req.removeVerifyingTestCase(testCase);
		testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromReq(Arrays.asList(testCaseId),
				requirementVersionId);
	}

	@Override
	public PagedCollectionHolder<List<TestCase>> findAllByRequirementVersion(long requirementVersionId,
			PagingAndSorting pagingAndSorting) {
		List<TestCase> verifiers = testCaseDao.findAllByVerifiedRequirementVersion(requirementVersionId,
				pagingAndSorting);

		long verifiersCount = testCaseDao.countByVerifiedRequirementVersion(requirementVersionId);

		return new PagingBackedPagedCollectionHolder<List<TestCase>>(pagingAndSorting, verifiersCount, verifiers);
	}

}
