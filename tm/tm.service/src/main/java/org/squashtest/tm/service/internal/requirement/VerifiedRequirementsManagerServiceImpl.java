/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2013 Henix, henix.fr
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
import java.util.Set;

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
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
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
	private TestStepDao testStepDao;

	@Inject
	private RequirementVersionDao requirementVersionDao;

	@Inject
	private TestCaseCallTreeFinder callTreeFinder;

	
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
		List<RequirementVersion> requirementVersions = findRequirementVersions(requirementsIds);
		TestCase testCase = testCaseDao.findById(testCaseId);
		if(!requirementVersions.isEmpty()){
			return doAddVerifyingRequirementVersionsToTestCase(requirementVersions, testCase);
		}
		return Collections.emptyList();
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
			
			if (!requirementVersionsIds.isEmpty()) {
				List<RequirementVersionCoverage> requirementVersionCoverages = requirementVersionCoverageDao.byTestCaseAndRequirementVersions(requirementVersionsIds, testCaseId);
				for(RequirementVersionCoverage coverage : requirementVersionCoverages){
					requirementVersionCoverageDao.delete(coverage);
				}
				testCaseImportanceManagerService
						.changeImportanceIfRelationsRemovedFromTestCase(requirementVersionsIds, testCaseId);
			}
		}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public void removeVerifiedRequirementVersionFromTestCase(long requirementVersionId, long testCaseId) {
		RequirementVersionCoverage coverage = requirementVersionCoverageDao.byRequirementVersionAndTestCase(requirementVersionId, testCaseId);
		requirementVersionCoverageDao.delete(coverage);
		testCaseImportanceManagerService.changeImportanceIfRelationsRemovedFromTestCase(Arrays.asList(requirementVersionId),
				testCaseId);
	}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public int changeVerifiedRequirementVersionOnTestCase(long oldVerifiedRequirementVersionId,
			long newVerifiedRequirementVersionId, long testCaseId) {
		RequirementVersion newReq = requirementVersionDao.findById(newVerifiedRequirementVersionId);
		RequirementVersionCoverage coverage = requirementVersionCoverageDao.byRequirementVersionAndTestCase(oldVerifiedRequirementVersionId, testCaseId);
		coverage.setVerifiedRequirementVersion(newReq);
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
	 */
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ') or hasRole('ROLE_ADMIN')")
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestCaseId(
			long testCaseId, PagingAndSorting pagingAndSorting) {
		List<RequirementVersionCoverage> reqVersionCoverages = requirementVersionCoverageDao.findAllByTestCaseId(testCaseId, pagingAndSorting);
		long verifiedCount = requirementVersionCoverageDao.numberByTestCase(testCaseId);
		return new PagingBackedPagedCollectionHolder<List<VerifiedRequirement>>(pagingAndSorting, verifiedCount, convertInDirectlyVerified(reqVersionCoverages));
	}

	private List<VerifiedRequirement> convertInDirectlyVerified(List<RequirementVersionCoverage> reqVersionCoverages) {
		List<VerifiedRequirement> result = new ArrayList<VerifiedRequirement>(reqVersionCoverages.size());
		for(RequirementVersionCoverage rvc : reqVersionCoverages){
			VerifiedRequirement convertionResult = new VerifiedRequirement(rvc, true);
			result.add(convertionResult);
		}
		return result;
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
	
	@Override
	public Collection<VerifiedRequirementException> addVerifiedRequirementsToTestStep(List<Long> requirementsIds,
			long testStepId) {
		List<RequirementVersion> requirementVersions = findRequirementVersions(requirementsIds);
		if(!requirementVersions.isEmpty()){
			ActionTestStep step = testStepDao.findActionTestStepById(testStepId);
			TestCase testCase = step.getTestCase();
			Collection<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>();
			Iterator<RequirementVersion> iterator = requirementVersions.iterator();
			while (iterator.hasNext()) {
				RequirementVersion requirementVersion = iterator.next();
				try {
					RequirementVersionCoverage coverage = requirementVersionCoverageDao.byRequirementVersionAndTestCase(requirementVersion.getId(), testCase.getId());
					if(coverage == null){
						RequirementVersionCoverage newCoverage = new RequirementVersionCoverage(requirementVersion, testCase);
						newCoverage.addAllVerifyingSteps(Arrays.asList(step));
						requirementVersionCoverageDao.persist(newCoverage);
					}else{						
						coverage.addAllVerifyingSteps(Arrays.asList(step));
						iterator.remove();
					}					
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
	return Collections.emptyList();
	}
	
	private List<RequirementVersion> findRequirementVersions(List<Long> requirementsIds) {
		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao.findAllByIds(requirementsIds);
		if (!nodes.isEmpty()) {
			List<Requirement> requirements = new RequirementNodeWalker().walk(nodes);
				if (!requirements.isEmpty()) {
					List<RequirementVersion> requirementVersions = extractVersions(requirements);
					return requirementVersions;
				}
		}
		return Collections.emptyList();
	}

	@Override
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllVerifiedRequirementsByTestCaseId(long testCaseId,
			PagingAndSorting pas) {

		LOGGER.debug("Looking for verified requirements of TestCase[id:{}]", testCaseId);

		Set<Long> calleesIds = callTreeFinder.getTestCaseCallTree(testCaseId);

		calleesIds.add(testCaseId);

		LOGGER.debug("Fetching Requirements verified by TestCases {}",calleesIds.toString());
		
		List<RequirementVersion> pagedVersionVerifiedByCalles = requirementVersionCoverageDao.findDistinctRequirementVersionsByTestCases(calleesIds, pas);

		TestCase mainTestCase = testCaseDao.findById(testCaseId);

		List<VerifiedRequirement> pagedVerifiedReqs = buildVerifiedRequirementList(
				mainTestCase, pagedVersionVerifiedByCalles);

		long totalVerified = requirementVersionCoverageDao.numberDistinctVerifiedByTestCases(calleesIds);
		
		LOGGER.debug("Total count of verified requirements : {}", totalVerified);
	

		return new PagingBackedPagedCollectionHolder<List<VerifiedRequirement>>(pas, totalVerified, pagedVerifiedReqs);
	}
	
	
	private List<VerifiedRequirement> buildVerifiedRequirementList(
			final TestCase main , List<RequirementVersion> pagedVersionVerifiedByCalles) {


		List<VerifiedRequirement> toReturn = new ArrayList<VerifiedRequirement>(pagedVersionVerifiedByCalles.size());
		
		for (RequirementVersion rVersion : pagedVersionVerifiedByCalles){
			boolean isDirect = main.verifies(rVersion);
			toReturn.add(new VerifiedRequirement(rVersion, isDirect));
		}

		return toReturn;
	}

	@Override
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestStepId(
			long testStepId, PagingAndSorting paging) {
		TestStep step = testStepDao.findById(testStepId);
		return findAllDirectlyVerifiedRequirementsByTestCaseId(step.getTestCase().getId(), paging);
	}

	

	@Override
	public void removeVerifiedRequirementVersionsFromTestStep(List<Long> requirementVersionsIds, long testStepId) {
		List<RequirementVersionCoverage> coverages = requirementVersionCoverageDao.byRequirementVersionsAndTestStep(requirementVersionsIds, testStepId);
		for(RequirementVersionCoverage coverage : coverages){
			coverage.removeVerifyingStep(testStepId);
		}
	}
	


}
