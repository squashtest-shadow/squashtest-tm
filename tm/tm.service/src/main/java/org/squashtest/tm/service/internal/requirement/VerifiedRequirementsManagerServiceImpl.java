/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) 2010 - 2015 Henix, henix.fr
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.squashtest.tm.core.foundation.collection.PagedCollectionHolder;
import org.squashtest.tm.core.foundation.collection.PagingAndSorting;
import org.squashtest.tm.core.foundation.collection.PagingBackedPagedCollectionHolder;
import org.squashtest.tm.domain.campaign.Iteration;
import org.squashtest.tm.domain.campaign.IterationTestPlanItem;
import org.squashtest.tm.domain.execution.Execution;
import org.squashtest.tm.domain.execution.ExecutionStatus;
import org.squashtest.tm.domain.execution.ExecutionStep;
import org.squashtest.tm.domain.milestone.Milestone;
import org.squashtest.tm.domain.requirement.Requirement;
import org.squashtest.tm.domain.requirement.RequirementCoverageStat;
import org.squashtest.tm.domain.requirement.RequirementCoverageStat.Rate;
import org.squashtest.tm.domain.requirement.RequirementLibraryNode;
import org.squashtest.tm.domain.requirement.RequirementStatus;
import org.squashtest.tm.domain.requirement.RequirementVersion;
import org.squashtest.tm.domain.testcase.ActionTestStep;
import org.squashtest.tm.domain.testcase.RequirementVersionCoverage;
import org.squashtest.tm.domain.testcase.TestCase;
import org.squashtest.tm.domain.testcase.TestCaseExecutionStatus;
import org.squashtest.tm.domain.testcase.TestStep;
import org.squashtest.tm.exception.UnknownEntityException;
import org.squashtest.tm.exception.requirement.RequirementAlreadyVerifiedException;
import org.squashtest.tm.exception.requirement.RequirementVersionNotLinkableException;
import org.squashtest.tm.exception.requirement.VerifiedRequirementException;
import org.squashtest.tm.service.advancedsearch.IndexationService;
import org.squashtest.tm.service.internal.repository.IterationDao;
import org.squashtest.tm.service.internal.repository.LibraryNodeDao;
import org.squashtest.tm.service.internal.repository.RequirementDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionCoverageDao;
import org.squashtest.tm.service.internal.repository.RequirementVersionDao;
import org.squashtest.tm.service.internal.repository.TestCaseDao;
import org.squashtest.tm.service.internal.repository.TestStepDao;
import org.squashtest.tm.service.internal.testcase.TestCaseCallTreeFinder;
import org.squashtest.tm.service.milestone.MilestoneFinderService;
import org.squashtest.tm.service.milestone.MilestoneManagerService;
import org.squashtest.tm.service.requirement.VerifiedRequirement;
import org.squashtest.tm.service.requirement.VerifiedRequirementsManagerService;
import org.squashtest.tm.service.security.PermissionEvaluationService;
import org.squashtest.tm.service.security.PermissionsUtils;
import org.squashtest.tm.service.security.SecurityCheckableObject;
import org.squashtest.tm.service.testcase.TestCaseImportanceManagerService;

import static org.squashtest.tm.service.security.Authorizations.*;

@Service("squashtest.tm.service.VerifiedRequirementsManagerService")
@Transactional
public class VerifiedRequirementsManagerServiceImpl implements
		VerifiedRequirementsManagerService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(VerifiedRequirementsManagerServiceImpl.class);
	private static final String LINK_TC_OR_ROLE_ADMIN = "hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'LINK')"
			+ OR_HAS_ROLE_ADMIN;

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

	@Inject
	private IndexationService indexationService;

	@Inject
	private MilestoneManagerService milestoneManager;

	@Inject
	private RequirementDao requirementDao;

	@Inject
	private IterationDao iterationDao;
	
	@SuppressWarnings("rawtypes")
	@Inject
	@Qualifier("squashtest.tm.repository.RequirementLibraryNodeDao")
	private LibraryNodeDao<RequirementLibraryNode> requirementLibraryNodeDao;
	@Inject
	private PermissionEvaluationService permissionService;

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public Collection<VerifiedRequirementException> addVerifiedRequirementsToTestCase(
			List<Long> requirementsIds, long testCaseId,
			Milestone activeMilestone) {

		List<RequirementVersion> requirementVersions = findRequirementVersions(
				requirementsIds, activeMilestone);

		TestCase testCase = testCaseDao.findById(testCaseId);
		if (!requirementVersions.isEmpty()) {
			return doAddVerifyingRequirementVersionsToTestCase(
					requirementVersions, testCase);
		}
		return Collections.emptyList();
	}

	private List<RequirementVersion> extractVersions(
			List<Requirement> requirements, Milestone activeMilestone) {

		List<RequirementVersion> rvs = new ArrayList<RequirementVersion>(
				requirements.size());
		for (Requirement requirement : requirements) {

			// normal mode
			if (activeMilestone == null) {
				rvs.add(requirement.getResource());
			}
			// milestone mode
			else {
				rvs.add(requirement.findByMilestone(activeMilestone));
			}

		}
		return rvs;
	}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public void removeVerifiedRequirementVersionsFromTestCase(
			List<Long> requirementVersionsIds, long testCaseId) {

		if (!requirementVersionsIds.isEmpty()) {

			List<RequirementVersionCoverage> requirementVersionCoverages = requirementVersionCoverageDao
					.byTestCaseAndRequirementVersions(requirementVersionsIds,
							testCaseId);

			for (RequirementVersionCoverage coverage : requirementVersionCoverages) {
				requirementVersionCoverageDao.delete(coverage);
			}

			indexationService.reindexTestCase(testCaseId);
			indexationService
					.reindexRequirementVersionsByIds(requirementVersionsIds);

			testCaseImportanceManagerService
					.changeImportanceIfRelationsRemovedFromTestCase(
							requirementVersionsIds, testCaseId);
		}
	}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public void removeVerifiedRequirementVersionFromTestCase(
			long requirementVersionId, long testCaseId) {
		RequirementVersionCoverage coverage = requirementVersionCoverageDao
				.byRequirementVersionAndTestCase(requirementVersionId,
						testCaseId);

		requirementVersionCoverageDao.delete(coverage);

		indexationService.reindexTestCase(testCaseId);
		indexationService.reindexRequirementVersion(requirementVersionId);
		testCaseImportanceManagerService
				.changeImportanceIfRelationsRemovedFromTestCase(
						Arrays.asList(requirementVersionId), testCaseId);
	}

	@Override
	@PreAuthorize(LINK_TC_OR_ROLE_ADMIN)
	public int changeVerifiedRequirementVersionOnTestCase(
			long oldVerifiedRequirementVersionId,
			long newVerifiedRequirementVersionId, long testCaseId) {
		RequirementVersion newReq = requirementVersionDao
				.findById(newVerifiedRequirementVersionId);
		RequirementVersionCoverage coverage = requirementVersionCoverageDao
				.byRequirementVersionAndTestCase(
						oldVerifiedRequirementVersionId, testCaseId);
		coverage.setVerifiedRequirementVersion(newReq);
		indexationService.reindexTestCase(testCaseId);
		indexationService
				.reindexRequirementVersion(oldVerifiedRequirementVersionId);
		indexationService
				.reindexRequirementVersion(oldVerifiedRequirementVersionId);
		testCaseImportanceManagerService
				.changeImportanceIfRelationsRemovedFromTestCase(
						Arrays.asList(newVerifiedRequirementVersionId),
						testCaseId);

		return newReq.getVersionNumber();
	}

	/*
	 * regarding the @PreAuthorize for the verified requirements :
	 * 
	 * I prefer to show all the requirements that the test case refers to even
	 * if some of those requirements belongs to a project the current user
	 * cannot "read", rather post filtering it.
	 * 
	 * The reason for that is that such policy is impractical for the same
	 * problem in the context of Iteration-TestCase associations : filtering the
	 * test cases wouldn't make much sense and would lead to partial executions
	 * of a campaign.
	 * 
	 * Henceforth the same policy applies to other cases of possible
	 * inter-project associations (like TestCase-Requirement associations in the
	 * present case), for the sake of coherence.
	 * 
	 * @author bsiri
	 * 
	 * (non-Javadoc)
	 */
	@Override
	@PreAuthorize("hasPermission(#testCaseId, 'org.squashtest.tm.domain.testcase.TestCase' , 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestCaseId(
			long testCaseId, PagingAndSorting pagingAndSorting) {
		List<RequirementVersionCoverage> reqVersionCoverages = requirementVersionCoverageDao
				.findAllByTestCaseId(testCaseId, pagingAndSorting);
		long verifiedCount = requirementVersionCoverageDao
				.numberByTestCase(testCaseId);
		return new PagingBackedPagedCollectionHolder<List<VerifiedRequirement>>(
				pagingAndSorting, verifiedCount,
				convertInDirectlyVerified(reqVersionCoverages));
	}

	private List<VerifiedRequirement> convertInDirectlyVerified(
			List<RequirementVersionCoverage> reqVersionCoverages) {
		List<VerifiedRequirement> result = new ArrayList<VerifiedRequirement>(
				reqVersionCoverages.size());
		for (RequirementVersionCoverage rvc : reqVersionCoverages) {
			VerifiedRequirement convertionResult = new VerifiedRequirement(rvc,
					true).withVerifyingStepsFrom(rvc.getVerifyingTestCase());
			result.add(convertionResult);
		}
		return result;
	}

	@Override
	public Collection<VerifiedRequirementException> addVerifyingRequirementVersionsToTestCase(
			Map<TestCase, List<RequirementVersion>> requirementVersionsByTestCase) {
		Collection<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>();
		for (Entry<TestCase, List<RequirementVersion>> reqVsByTc : requirementVersionsByTestCase
				.entrySet()) {
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
				RequirementVersionCoverage coverage = new RequirementVersionCoverage(
						requirementVersion, testCase);
				requirementVersionCoverageDao.persist(coverage);
				indexationService.reindexTestCase(testCase.getId());
				indexationService.reindexRequirementVersion(requirementVersion
						.getId());
			} catch (RequirementAlreadyVerifiedException ex) {
				LOGGER.warn(ex.getMessage());
				rejections.add(ex);
				iterator.remove();
			} catch (RequirementVersionNotLinkableException ex) {
				LOGGER.warn(ex.getMessage());
				rejections.add(ex);
				iterator.remove();
			}
		}
		testCaseImportanceManagerService
				.changeImportanceIfRelationsAddedToTestCase(
						requirementVersions, testCase);
		return rejections;

	}

	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'LINK')"
			+ OR_HAS_ROLE_ADMIN)
	public Collection<VerifiedRequirementException> addVerifiedRequirementsToTestStep(
			List<Long> requirementsIds, long testStepId,
			Milestone activeMilestone) {
		List<RequirementVersion> requirementVersions = findRequirementVersions(
				requirementsIds, activeMilestone);
		// init rejections
		Collection<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>();
		// check if list not empty
		if (!requirementVersions.isEmpty()) {
			// collect concerned entities
			ActionTestStep step = testStepDao
					.findActionTestStepById(testStepId);
			TestCase testCase = step.getTestCase();
			// iterate on requirement versions
			Iterator<RequirementVersion> iterator = requirementVersions
					.iterator();
			while (iterator.hasNext()) {
				try {
					RequirementVersion requirementVersion = iterator.next();
					PermissionsUtils.checkPermission(permissionService,
							new SecurityCheckableObject(requirementVersion,
									"LINK"));
					boolean newReqCoverage = addVerifiedRequirementVersionToTestStep(
							requirementVersion, step, testCase);
					if (!newReqCoverage) {
						iterator.remove();
					}
				} catch (RequirementAlreadyVerifiedException ex) {
					LOGGER.warn(ex.getMessage());
					iterator.remove();
					rejections.add(ex);
				} catch (RequirementVersionNotLinkableException ex) {
					LOGGER.warn(ex.getMessage());
					iterator.remove();
					rejections.add(ex);
				}
			}
			testCaseImportanceManagerService
					.changeImportanceIfRelationsAddedToTestCase(
							requirementVersions, testCase);

		}
		return rejections;
	}

	/**
	 * Will find the RequirementVersionCoverage for the given requirement
	 * version and test case to add the step to it. If not found, will create a
	 * new RequirementVersionCoverage for the test case and add the step to it.<br>
	 * 
	 * @param step
	 * @param testCase
	 * @return true if a new RequirementVersionCoverage has been created.
	 */
	private boolean addVerifiedRequirementVersionToTestStep(
			RequirementVersion requirementVersion, ActionTestStep step,
			TestCase testCase) {

		RequirementVersionCoverage coverage = requirementVersionCoverageDao
				.byRequirementVersionAndTestCase(requirementVersion.getId(),
						testCase.getId());
		if (coverage == null) {
			RequirementVersionCoverage newCoverage = new RequirementVersionCoverage(
					requirementVersion, testCase);
			newCoverage.addAllVerifyingSteps(Arrays.asList(step));
			requirementVersionCoverageDao.persist(newCoverage);
			indexationService.reindexTestCase(testCase.getId());
			indexationService.reindexRequirementVersion(requirementVersion
					.getId());
			return true;
		} else {
			coverage.addAllVerifyingSteps(Arrays.asList(step));
			return false;
		}

	}

	/**
	 * @see VerifiedRequirementsManagerService#addVerifiedRequirementVersionToTestStep(long,
	 *      long);
	 */
	@Override
	@PreAuthorize("hasPermission(#testStepId, 'org.squashtest.tm.domain.testcase.TestStep' , 'LINK') and hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion' , 'LINK')"
			+ OR_HAS_ROLE_ADMIN)
	public Collection<VerifiedRequirementException> addVerifiedRequirementVersionToTestStep(
			long requirementVersionId, long testStepId) {
		ActionTestStep step = testStepDao.findActionTestStepById(testStepId);
		TestCase testCase = step.getTestCase();
		RequirementVersion version = requirementVersionDao
				.findById(requirementVersionId);
		Collection<VerifiedRequirementException> rejections = new ArrayList<VerifiedRequirementException>(
				1);
		if (version == null) {
			throw new UnknownEntityException(requirementVersionId,
					RequirementVersion.class);
		}
		try {
			boolean newRequirementCoverageCreated = addVerifiedRequirementVersionToTestStep(
					version, step, testCase);
			if (newRequirementCoverageCreated) {
				testCaseImportanceManagerService
						.changeImportanceIfRelationsAddedToTestCase(
								Arrays.asList(version), testCase);
			}
		} catch (RequirementAlreadyVerifiedException ex) {
			LOGGER.warn(ex.getMessage());
			rejections.add(ex);
		} catch (RequirementVersionNotLinkableException ex) {
			LOGGER.warn(ex.getMessage());
			rejections.add(ex);
		}
		return rejections;
	}

	private List<RequirementVersion> findRequirementVersions(
			List<Long> requirementsIds, Milestone activeMilestone) {

		List<RequirementLibraryNode> nodes = requirementLibraryNodeDao
				.findAllByIds(requirementsIds);

		if (!nodes.isEmpty()) {
			List<Requirement> requirements = new RequirementNodeWalker()
					.walk(nodes);
			if (!requirements.isEmpty()) {
				return extractVersions(requirements, activeMilestone);
			}
		}
		return Collections.emptyList();
	}

	@Override
	@Transactional(readOnly = true)
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllVerifiedRequirementsByTestCaseId(
			long testCaseId, PagingAndSorting pas) {

		LOGGER.debug("Looking for verified requirements of TestCase[id:{}]",
				testCaseId);

		Set<Long> calleesIds = callTreeFinder.getTestCaseCallTree(testCaseId);

		calleesIds.add(testCaseId);

		LOGGER.debug("Fetching Requirements verified by TestCases {}",
				calleesIds.toString());

		List<RequirementVersion> pagedVersionVerifiedByCalles = requirementVersionCoverageDao
				.findDistinctRequirementVersionsByTestCases(calleesIds, pas);

		TestCase mainTestCase = testCaseDao.findById(testCaseId);

		List<VerifiedRequirement> pagedVerifiedReqs = buildVerifiedRequirementList(
				mainTestCase, pagedVersionVerifiedByCalles);

		long totalVerified = requirementVersionCoverageDao
				.numberDistinctVerifiedByTestCases(calleesIds);

		LOGGER.debug("Total count of verified requirements : {}", totalVerified);

		return new PagingBackedPagedCollectionHolder<List<VerifiedRequirement>>(
				pas, totalVerified, pagedVerifiedReqs);
	}

	@Override
	public List<VerifiedRequirement> findAllVerifiedRequirementsByTestCaseId(
			long testCaseId) {
		LOGGER.debug("Looking for verified requirements of TestCase[id:{}]",
				testCaseId);

		Set<Long> calleesIds = callTreeFinder.getTestCaseCallTree(testCaseId);

		calleesIds.add(testCaseId);

		LOGGER.debug("Fetching Requirements verified by TestCases {}",
				calleesIds.toString());

		List<RequirementVersion> pagedVersionVerifiedByCalles = requirementVersionCoverageDao
				.findDistinctRequirementVersionsByTestCases(calleesIds);

		TestCase mainTestCase = testCaseDao.findById(testCaseId);

		return buildVerifiedRequirementList(mainTestCase,
				pagedVersionVerifiedByCalles);
	}

	/**
	 * @see org.squashtest.tm.service.internal.requirement.VerifiedRequirementsManagerService#findisReqCoveredOfCallingTCWhenisReqCoveredChanged(long,
	 *      List)
	 */
	@Override
	public Map<Long, Boolean> findisReqCoveredOfCallingTCWhenisReqCoveredChanged(
			long updatedTestCaseId, Collection<Long> toUpdateIds) {
		Map<Long, Boolean> result;
		result = new HashMap<Long, Boolean>(toUpdateIds.size());
		if (testCaseHasDirectCoverage(updatedTestCaseId)
				|| testCaseHasUndirectRequirementCoverage(updatedTestCaseId)) {
			// set isReqCovered = true for all calling test cases
			for (Long id : toUpdateIds) {
				result.put(id, Boolean.TRUE);
			}
		} else {
			// check each calling testCase to see if their status changed
			for (Long id : toUpdateIds) {
				Boolean value = testCaseHasDirectCoverage(id)
						|| testCaseHasUndirectRequirementCoverage(id);
				result.put(id, value);
			}
		}

		return result;
	}

	/**
	 * @see org.squashtest.tm.service.internal.requirement.VerifiedRequirementsManagerService#testCaseHasUndirectRequirementCoverage(long)
	 */
	@Override
	public boolean testCaseHasUndirectRequirementCoverage(long updatedTestCaseId) {
		List<Long> calledTestCaseIds = testCaseDao
				.findAllDistinctTestCasesIdsCalledByTestCase(updatedTestCaseId);
		if (!calledTestCaseIds.isEmpty()) {
			for (Long id : calledTestCaseIds) {
				if (testCaseHasDirectCoverage(id)
						|| testCaseHasUndirectRequirementCoverage(id)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @see org.squashtest.tm.service.internal.requirement.VerifiedRequirementsManagerService#testCaseHasDirectCoverage(long)
	 */
	@Override
	public boolean testCaseHasDirectCoverage(long updatedTestCaseId) {
		return requirementVersionDao.countVerifiedByTestCase(updatedTestCaseId) > 0;
	}

	private List<VerifiedRequirement> buildVerifiedRequirementList(
			final TestCase main,
			List<RequirementVersion> pagedVersionVerifiedByCalles) {

		List<VerifiedRequirement> toReturn = new ArrayList<VerifiedRequirement>(
				pagedVersionVerifiedByCalles.size());

		for (RequirementVersion rVersion : pagedVersionVerifiedByCalles) {
			boolean isDirect = main.verifies(rVersion);
			toReturn.add(new VerifiedRequirement(rVersion, isDirect)
					.withVerifyingStepsFrom(main));
		}

		return toReturn;
	}

	@Override
	public PagedCollectionHolder<List<VerifiedRequirement>> findAllDirectlyVerifiedRequirementsByTestStepId(
			long testStepId, PagingAndSorting paging) {
		TestStep step = testStepDao.findById(testStepId);
		return findAllDirectlyVerifiedRequirementsByTestCaseId(step
				.getTestCase().getId(), paging);
	}

	@Override
	public void removeVerifiedRequirementVersionsFromTestStep(
			List<Long> requirementVersionsIds, long testStepId) {
		/*
		 * List<RequirementVersionCoverage> coverages =
		 * requirementVersionCoverageDao.byRequirementVersionsAndTestStep(
		 * requirementVersionsIds, testStepId); for (RequirementVersionCoverage
		 * coverage : coverages) { coverage.removeVerifyingStep(testStepId); }
		 */
		List<RequirementVersionCoverage> coverages = requirementVersionCoverageDao
				.byRequirementVersionsAndTestStep(requirementVersionsIds,
						testStepId);

		// if cast exception well, the input were wrong and the thread was bound
		// to grind to halt.
		ActionTestStep ts = (ActionTestStep) testStepDao.findById(testStepId);
		for (RequirementVersionCoverage cov : coverages) {
			ts.removeRequirementVersionCoverage(cov);
		}

	}

	@Override
	@PreAuthorize("hasPermission(#requirementVersionId, 'org.squashtest.tm.domain.requirement.RequirementVersion' , 'READ')"
			+ OR_HAS_ROLE_ADMIN)
	public RequirementCoverageStat findCoverageStat(Long requirementVersionId,
			Milestone currentMilestone, List<Long> iterationsIds) {
		RequirementCoverageStat stats = new RequirementCoverageStat();

		RequirementVersion mainVersion = requirementVersionDao.findById(requirementVersionId);
		Requirement mainRequirement = mainVersion.getRequirement();
		List<RequirementVersion> descendants = findValidDescendants(mainRequirement,currentMilestone);
		findCoverageRate(mainRequirement,mainVersion,descendants,stats);
		//if we have a perimeter (ie iteration(s)), we'll have to calculate verification and validation rates
		if (iterationsIds.size() > 0) {
			findExecutionRate(mainRequirement,mainVersion,descendants,stats,iterationsIds);
		}
		stats.convertRatesToPercent();
		return stats;
	}

	/**
	 * Extract a {@link Map}, key : {@link ExecutionStatus} value : {@link Long}.
	 * The goal is to perform arithmetic operation with this map to calculate several rates on {@link RequirementVersion}
	 * Constraints from specification Feat 4434 :
	 * <code>
	 * <ul>
	 * <li>Requirement without linked {@link TestStep} must be treated at {@link Execution} level</li>
	 * <li>Requirement with linked {@link TestStep} must be treated at {@link ExecutionStep} level</li>
	 * <li>Only last execution must be considered for a given {@link IterationTestPlanItem}</li>
	 * <li>FastPass must be considered for all case (ie even if the {@link RequirementVersion} is linked to {@link TestStep})</li>
	 * <li>Rate must be calculate on the designed {@link Requirement} and it's descendants</li>
	 * <li>The descendant list must be filtered by {@link Milestone} and exclude {@link RequirementVersion} with {@link RequirementStatus#OBSOLETE}</li>
	 * </ul>
	 * </code>
	 * @param mainRequirement
	 * @param mainVersion
	 * @param descendants
	 * @param stats
	 * @param iterationsIds
	 */
	private void findExecutionRate(Requirement mainRequirement,
			RequirementVersion mainVersion,
			List<RequirementVersion> descendants,
			RequirementCoverageStat stats, List<Long> iterationsIds) {
		
		Map<ExecutionStatus, Long> mainFullCoverageResult = new HashMap<ExecutionStatus, Long>();
		Map<ExecutionStatus, Long> descendantFullCoverageResult = new HashMap<ExecutionStatus, Long>();
		
		boolean hasDescendant = descendants.size()>0;
		Rate verificationRate = new Rate();
		Rate validationRate = new Rate();
		
		//see http://javadude.com/articles/passbyvalue.htm to understand why an array (or any object) is needed here
		Long[] mainUntestedElementsCount = new Long[1];
		Map<ExecutionStatus, Long> mainStatusMap = new HashMap<ExecutionStatus, Long>();
		makeStatusMap(mainVersion.getRequirementVersionCoverages(), mainUntestedElementsCount, mainStatusMap, iterationsIds);
		verificationRate.setRequirementVersionRate(doRateVerifiedCalculation(mainStatusMap, mainUntestedElementsCount[0]));
		
		if (hasDescendant) {
			verificationRate.setAncestor(true);
			
			Set<RequirementVersionCoverage> descendantCoverages = getCoverages(descendants);
			Long[] descendantTestedElementsCount = new Long[1];
			Map<ExecutionStatus, Long> descendantStatusMap = new HashMap<ExecutionStatus, Long>();
			makeStatusMap(descendantCoverages, descendantTestedElementsCount, descendantStatusMap, iterationsIds);
			verificationRate.setRequirementVersionChildrenRate(doRateVerifiedCalculation(descendantStatusMap, descendantTestedElementsCount[0]));
			
			Long[] allUntestedElementsCount = new Long[1];
			allUntestedElementsCount[0] = mainUntestedElementsCount[0] + descendantTestedElementsCount[0];
			Map<ExecutionStatus, Long> allStatusMap = mergeMapResult(mainStatusMap,descendantStatusMap);
			verificationRate.setRequirementVersionGlobalRate(doRateVerifiedCalculation(allStatusMap, allUntestedElementsCount[0]));
		}
		
		stats.addRate("verification", verificationRate);
		stats.addRate("validation", validationRate);
	}

	private Map<ExecutionStatus, Long> mergeMapResult(
			Map<ExecutionStatus, Long> mainStatusMap,
			Map<ExecutionStatus, Long> descendantStatusMap) {
		Map<ExecutionStatus, Long> mergedStatusMap = new HashMap<ExecutionStatus, Long>();
		EnumSet<ExecutionStatus> allStatus = EnumSet.allOf(ExecutionStatus.class);
		for (ExecutionStatus executionStatus : allStatus) {
			Long mainCount = mainStatusMap.get(executionStatus) == null ? 0 : mainStatusMap.get(executionStatus);
			Long descendantCount = descendantStatusMap.get(executionStatus) == null ? 0 : descendantStatusMap.get(executionStatus);
			Long totalCount = mainCount + descendantCount;
			mergedStatusMap.put(executionStatus, totalCount);
		}
		return mergedStatusMap;
	}

	private Set<RequirementVersionCoverage> getCoverages(
			List<RequirementVersion> descendants) {
		Set<RequirementVersionCoverage> covs = new HashSet<RequirementVersionCoverage>();
		for (RequirementVersion requirementVersion : descendants) {
			covs.addAll(requirementVersion.getRequirementVersionCoverages());
		}
		return covs;
	}

	private List<Long> filterTCIds(List<Long> TCIds,
			List<Long> tCWithItpiIds) {
		List<Long> filtered = new ArrayList<Long>();
		filtered.addAll(TCIds);
		filtered.removeAll(tCWithItpiIds);
		return filtered;
	}

	private List<Long> findAllTestCaseIds(List<RequirementVersionCoverage> coverages) {
		List<Long> testCaseIds = new ArrayList<Long>();
		for (RequirementVersionCoverage cov : coverages) {
			testCaseIds.add(cov.getVerifyingTestCase().getId());
		}
		return testCaseIds;
	}

	private List<Long> findTCWithItpi(
			List<Long> tcIds,
			List<Long> iterationsIds) {
		return iterationDao.findVerifiedTcIdsInIterations(tcIds,iterationsIds);
	}
	
	private void makeStatusMap(Set<RequirementVersionCoverage> covs, 
			Long[]untestedElementsCount, Map<ExecutionStatus, Long> statusMap, List<Long> iterationsIds){
		List<RequirementVersionCoverage> simpleCoverage = new ArrayList<RequirementVersionCoverage>();
		List<RequirementVersionCoverage> stepedCoverage = new ArrayList<RequirementVersionCoverage>();
		Map<Long, Long> nbSimpleCoverageByTestCase = new HashMap<Long, Long>();
		partRequirementVersionCoverage(covs,simpleCoverage,stepedCoverage,nbSimpleCoverageByTestCase);
		//Find the test case with at least one itpi
		List<Long> mainSimpleCoverageTCIds = findAllTestCaseIds(simpleCoverage);
		List<Long> mainVersionTCWithItpiIds = findTCWithItpi(mainSimpleCoverageTCIds,iterationsIds);
		//Filter to have the test case without itpi
		List<Long> mainVersionTCWithoutItpiIds = filterTCIds(mainSimpleCoverageTCIds,mainVersionTCWithItpiIds);
		untestedElementsCount[0] = calculateUntestedElementCount(mainVersionTCWithoutItpiIds,nbSimpleCoverageByTestCase);
		statusMap.putAll(findResults(mainVersionTCWithItpiIds, iterationsIds, nbSimpleCoverageByTestCase));
	}
	
	private Long calculateUntestedElementCount(
			List<Long> mainVersionTCWithoutItpiIds,
			Map<Long, Long> nbSimpleCoverageByTestCase) {
		// TODO Auto-generated method stub
		return null;
	}

	private double doRateVerifiedCalculation(Map<ExecutionStatus, Long> fullCoverageResult, Long untestedElementsCount){
		Set<ExecutionStatus> statusSet = getVerifiedStatus();
		return doRateCalculation(statusSet, fullCoverageResult, untestedElementsCount);
	}
	
	private double doRateCalculation(Set<ExecutionStatus> statusSet, Map<ExecutionStatus, Long> fullCoverageResult, Long untestedElementsCount){
		//Implicit conversion of all Long and Integer in floating point number to allow proper rate operation
		double execWithRequiredStatus = countforStatus(fullCoverageResult, statusSet);
		double allExecutionCount = getCandidateExecCount(fullCoverageResult);
		double nbTCWithoutItpi = untestedElementsCount;
		return execWithRequiredStatus/(allExecutionCount+nbTCWithoutItpi);
	}

	private Long getCandidateExecCount(
			Map<ExecutionStatus, Long> fullCoverageResult) {
		Long nbStatus = 0L;
		for (Long countForOneStatus : fullCoverageResult.values()) {
			nbStatus += countForOneStatus;
		}
		return nbStatus;
	}

	

	private Long countforStatus(Map<ExecutionStatus, Long> fullCoverageResult,
			Set<ExecutionStatus> statusSet) {
		Long count = 0L;
		for (ExecutionStatus executionStatus : fullCoverageResult.keySet()) {
			if (statusSet.contains(executionStatus)) {
				count += fullCoverageResult.get(executionStatus);
			}
		}
		return count;
	}

	private Set<ExecutionStatus> getVerifiedStatus() {
		Set<ExecutionStatus> verifiedStatus = new HashSet<ExecutionStatus>();
		verifiedStatus.add(ExecutionStatus.SUCCESS);
		verifiedStatus.add(ExecutionStatus.SETTLED);
		verifiedStatus.add(ExecutionStatus.FAILURE);
		verifiedStatus.add(ExecutionStatus.BLOCKED);
		verifiedStatus.add(ExecutionStatus.UNTESTABLE);
		return verifiedStatus;
	}

//	private Map<ExecutionStatus, Long> findResults(
//			List<Long> testCaseIds, List<Long> iterationIds) {
//		return iterationDao.findExecStatusForIterationsAndTestCases(testCaseIds, iterationIds);
//	}
	
	private Map<ExecutionStatus, Long> findResults(
			List<Long> testCaseIds, List<Long> iterationIds, Map<Long, Long> nbSimpleCoverageByTestCase) {
		List<TestCaseExecutionStatus> testCaseExecutionStatus = iterationDao.findExecStatusForIterationsAndTestCases(testCaseIds, iterationIds);
		Map<ExecutionStatus, Long> computedResults = new HashMap<ExecutionStatus, Long>();
		for (TestCaseExecutionStatus oneTCES : testCaseExecutionStatus) {
			ExecutionStatus status = oneTCES.getStatus();
			Long nbCoverage = nbSimpleCoverageByTestCase.get(oneTCES.getTestCaseId());
			if (computedResults.containsKey(status)) {
				computedResults.put(status, computedResults.get(status)+nbCoverage);
			}
			else {
				computedResults.put(status,nbCoverage);
			}
		}
		return computedResults;
	}

	/**
	 * Part the {@link RequirementVersionCoverage} list in two list :
	 * One with {@link RequirementVersionCoverage} with linked test steps
	 * One with {@link RequirementVersionCoverage} without linked test steps 
	 * @param requirementVersionCoverages
	 * @param simpleCoverage
	 * @param stepedCoverage
	 * @param nbSimpleCoverageByTestCase 
	 */
	private void partRequirementVersionCoverage(
			Set<RequirementVersionCoverage> requirementVersionCoverages,
			List<RequirementVersionCoverage> simpleCoverage,
			List<RequirementVersionCoverage> stepedCoverage, Map<Long, Long> nbSimpleCoverageByTestCase) {
		for (RequirementVersionCoverage requirementVersionCoverage : requirementVersionCoverages) {
			if (requirementVersionCoverage.hasSteps()) {
				stepedCoverage.add(requirementVersionCoverage);
			}
			else {
				simpleCoverage.add(requirementVersionCoverage);
				Long tcId = requirementVersionCoverage.getVerifyingTestCase().getId();
				if (nbSimpleCoverageByTestCase.containsKey(tcId)) {
					nbSimpleCoverageByTestCase.put(tcId,nbSimpleCoverageByTestCase.get(tcId)+1);
				}
				else {
					nbSimpleCoverageByTestCase.put(tcId, 1L);
				}
			}
		}
		
	}

	private void findCoverageRate(Requirement mainRequirement, RequirementVersion mainVersion,
			List<RequirementVersion> descendants, RequirementCoverageStat stats) {
		
		Rate coverageRate = new Rate();
		coverageRate.setRequirementVersionRate(calculateCoverageRate(mainVersion));
		
		if (mainRequirement.hasContent()) {
			coverageRate.setRequirementVersionChildrenRate(calculateCoverageRate(descendants));
			List<RequirementVersion> all = getAllRequirementVersion(mainVersion, descendants);
			coverageRate.setRequirementVersionGlobalRate(calculateCoverageRate(all));
			coverageRate.setAncestor(true);
		}
		stats.addRate("coverage",coverageRate);
	}

	private List<RequirementVersion> getAllRequirementVersion(
			RequirementVersion mainVersion, List<RequirementVersion> descendants) {
		List<RequirementVersion> all = new ArrayList<RequirementVersion>();
		all.add(mainVersion);
		all.addAll(descendants);
		return all;
	}

	private double calculateCoverageRate(List<RequirementVersion> rvs) {
		double total = 0;
		double size = rvs.size();
		for (RequirementVersion rv : rvs) {
			total += calculateCoverageRate(rv);
		}
		return total/size;
	}

	/**
	 * Coverage Rate is 100% for 1+ {@link TestCase} linked to this {@link RequirementVersion}. 0% if no link
	 * @param mainVersion
	 * @return
	 */
	private Long calculateCoverageRate(RequirementVersion mainVersion) {
		if (mainVersion.getRequirementVersionCoverages().size()> 0) {
			return 1L;
		}
		return 0L;
	}

	private List<RequirementVersion> findValidDescendants(Requirement requirement, Milestone activeMilestone){
		List<Long> candidatesIds = requirementDao.findDescendantRequirementIds(Arrays.asList( new Long[]{requirement.getId()}));
		List<Requirement> candidates = requirementDao.findAllByIds(candidatesIds);
		return extractCurrentVersions(candidates,activeMilestone);
	}

	private List<RequirementVersion> extractCurrentVersions(
			List<Requirement> requirements, Milestone activeMilestone) {
		List<RequirementVersion> rvs = new ArrayList<RequirementVersion>(
				requirements.size());
		for (Requirement requirement : requirements) {
			RequirementVersion rv = requirement.getResource();
			// normal mode
			if (activeMilestone == null
					&& rv.isNotObsolete()) {
				rvs.add(rv);
			}
			// milestone mode
			else {
				if (rv.getMilestones().contains(activeMilestone)
						&& rv.isNotObsolete()) {
					rvs.add(rv);
				}
			}
		}
		return rvs;
	}

}
