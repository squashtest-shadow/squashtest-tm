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
package org.squashtest.tm.service.internal.requirement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;
@Service("squashtest.tm.service.VerifiedRequirementsManagerService")
@Transactional
public class VerifiedRequirementsManagerServiceImpl implements VerifiedRequirementsManagerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VerifiedRequirementsManagerServiceImpl.class);
	private static final String LINK_TC_OR_ROLE_ADMIN = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'LINK') or hasRole('ROLE_ADMIN')";

	@Inject
	private TestCaseDao testCaseDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;
	
	@Inject
	private RequirementVersionCoverageDao requirementVersionCoverageDao;

	@Inject
	private TestCaseImportanceManagerService testCaseImportanceManagerService;

	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;

	@SuppressWarnings("rawtypes")
	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public Collection<VerifiedRequirementException> addVerifiedRequirementsToTestCase(List<Long> requirementsIds,
			long testCaseId) {
		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao.findAllByIds(requirementsIds);

		if (!nodes.isEmpty()) {
			return doAddVerifiedRequirementNodesToTestCase(nodes, testCaseId);
		}

		return Collections.emptyList();
	}

	@SuppressWarnings("rawtypes")
	private Collection<VerifiedRequirementException> doAddVerifiedRequirementNodesToTestCase(
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
		List<RequirementVersion> requirementVersions = extractVersions(requirements);
		Collection<VerifiedRequirementException> rejections = doAddVerifyingRequirementVersionsToTestCase(requirementVersions, testCase);		
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
		@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
		public void removeVerifiedRequirementVersionsFromTestCase(List<Long> requirementVersionsIds, long testCaseId) {
			List<RequirementVersion> reqs = requirementVersionDao.findAllByIds(requirementVersionsIds);
	
			if (!reqs.isEmpty()) {
				List<RequirementVersionCoverage> requirementVersionCoverages = requirementVersionCoverageDao.findForTestCaseAndRequirementVersions(requirementVersionsIds, testCaseId);
				for(RequirementVersionCoverage coverage : requirementVersionCoverages){
					coverage.checkDeletable();
					requirementVersionCoverageDao.delete(coverage);
				}
				testCaseImportanceManagerService
						.changeImportanceIfRelationsRemovedFromTestCase(requirementVersionsIds, testCaseId);
			}
		}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public void removeVerifiedRequirementVersionFromTestCase(long requirementVersionId, long testCaseId) {
		RequirementVersionCoverage coverage = requirementVersionCoverageDao.findForRequirementVersionAndTestCase(requirementVersionId, testCaseId);
		coverage.checkDeletable();
		requirementVersionCoverageDao.delete(coverage);
		testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromTestCase(Arrays.asList(requirementVersionId),
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
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public int changeVerifiedRequirementVersionOnTestCase(long oldVerifiedRequirementVersionId,
			long newVerifiedRequirementVersionId, long testCaseId) {
		RequirementVersion newReq = requirementVersionDao.findById(newVerifiedRequirementVersionId);
		RequirementVersionCoverage coverage = requirementVersionCoverageDao.findForRequirementVersionAndTestCase(oldVerifiedRequirementVersionId, testCaseId);
//		RequirementVersion old = coverage.getVerifiedRequirementVersion();
//		old.removeRequirementVersionCoverage(coverage);
		coverage.setVerifiedRequirementVersion(newReq);
//		newReq.addRequirementCoverage(coverage);
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
	 * org.squashtest.tm.service.foundation.collection.CollectionSorting)
	 */
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<RequirementVersion>> findAllDirectlyVerifiedRequirementsByTestCaseId(
			long testCaseId, PagingAndSorting pas) {
		List<RequirementVersion> verifiedReqs = requirementVersionDao.findAllVerifiedByTestCase(testCaseId, pas);
		long verifiedCount = requirementVersionDao.countVerifiedByTestCase(testCaseId);
		return new PagingBackedPagedCollectionHolder<List<RequirementVersion>>(pas, verifiedCount, verifiedReqs);
	}

	@Override
	public Collection<VerifiedRequirementException> addVerifyingRequirementVersionsToTestCase(
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCase) {
		Collection<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>();
		for (Entry<TestCase, List<RequirementVersion>> reqVsByTc : requirementVersionsByTestCase.entrySet()) {
			TestCase testCase = reqVsByTc.getKey();
			List<RequirementVersion> requirementVersions = reqVsByTc.getValue();
			Collection<VerifiedRequirementException> entrtyRejections = doAddVerifyingRequirementVersionsToTestCase(
					requirementVersions, testCase);
			rejections.addAll(entrtyRejections);
		}
		return rejections;

	}

	private Collection<VerifiedRequirementException> doAddVerifyingRequirementVersionsToTestCase(
			List<RequirementVersion> requirementVersions, TestCase testCase) {
		Collection<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>();
		Iterator<RequirementVersion> iterator = requirementVersions.iterator();
		while (iterator.hasNext()) {
			RequirementVersion requirementVersion = iterator.next();
			try {
				RequirementVersionCoverage coverage = new RequirementVersionCoverage(requirementVersion, testCase);
				requirementVersionCoverageDao.persist(coverage);
			} catch (RequirementAlreadyVerifiedException ex) {
				LOGGER.warn(ex.getMessage());
				rejections.add(ex);
				iterator.remove();
			}catch (RequirementVersionNotLinkableException ex) {
				LOGGER.warn(ex.getMessage());
				rejections.add(ex);
				iterator.remove();
			}
		}
		testCaseImportanceManagerService.changeImportanceIfRelationsAddedToTestCase(requirementVersions, testCase);
		return rejections;

	}
}
